package com.mk.config;


import com.mk.assistant.JavaAgent;
import com.mk.store.CompressedMessageWindowChatMemory;
import com.mk.store.MongoChatMemoryStore;
import com.mk.tools.InterviewAppointmentTool;
import com.mk.util.ContextCompressor;
import com.mk.util.RewriteQueryTransformer;
import com.mk.util.WindowsContentInjector;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolService;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeDatabaseResponse;
import io.milvus.grpc.ListDatabasesResponse;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.CreateDatabaseParam;
import io.milvus.param.collection.DescribeDatabaseParam;
import io.milvus.param.collection.FieldType;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/08/15:24
 * @Description:
 */
@Configuration
public class MemoryChatAssistantConfig {
    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Autowired
    private ContextCompressor contextCompressor;

    @Autowired
    private EmbeddingModel qwenEmbeddingModel;
    @Autowired
    private QwenChatModel qwenChatModel;
    
    // Milvus配置参数
    @Value("${milvus.host}")
    private String milvusHost;
    
    @Value("${milvus.port}")
    private int milvusPort;
    
    @Value("${milvus.collection-name}")
    private String milvusCollectionName;
    
    @Value("${milvus.dim}")
    private int milvusDimension;
    @Value("${milvus.database:xiaozhi}")
    private String milvusDatabase;

    @Value("${rag.stb-type")
    private String stbType;
    @Bean
    ChatMemory chatMemory() {
//设置聊天记忆记录的message数量
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryStore(mongoChatMemoryStore)//配置自定义的持久化存储
                .build();
    }
    //根据memoryID进行对话隔离
    @Bean
    ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> CompressedMessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(mongoChatMemoryStore)//配置自定义的持久化存储
                .contextCompressor(contextCompressor)
                .build();
    }
    @Bean
    ChatMemoryProvider compressedMessageWindowChatMemoryProvider(){
        return memoryId -> CompressedMessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(mongoChatMemoryStore)//配置自定义的持久化存储
                .contextCompressor(contextCompressor)
                .build();
    }
    @Bean
    ContentRetriever contentRetrieverXiaozhi() {
        Document document = ClassPathDocumentLoader.loadDocument("面试题带答案版本-可编辑.md");

        // 使用内存作为向量存储（测试用）
        InMemoryEmbeddingStore embeddingStore = new InMemoryEmbeddingStore();
        
        // 分割->嵌入->向量存储
        EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .documentSplitter(new DocumentByParagraphSplitter(1024, 256))
                .build();
        embeddingStoreIngestor.ingest(document);

        // 优化检索参数
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(8) // 增加返回结果数量，提高召回率
                .minScore(0.65) // 降低最小分数阈值，提高召回率
                .build();
    }
    @Bean
    public MilvusServiceClient milvusClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(milvusHost)
                .withPort(milvusPort)
                .build();
        MilvusServiceClient milvusClient = new MilvusServiceClient(connectParam);
        //验证数据库是否存在
        DescribeDatabaseParam describeDatabaseParam = DescribeDatabaseParam.newBuilder()
                .withDatabaseName(milvusDatabase)
                .build();
        R<DescribeDatabaseResponse> describeDatabase = milvusClient.describeDatabase(describeDatabaseParam);
        if(describeDatabase.getStatus() == R.success().getStatus()){
            System.out.println("数据库已存在");
        }else{
            //创建数据库
            CreateDatabaseParam createDatabaseParam = CreateDatabaseParam.newBuilder()
                    .withDatabaseName(milvusDatabase)
                    .build();
            R<RpcStatus> createDatabase = milvusClient.createDatabase(createDatabaseParam);
            if(createDatabase.getStatus() == R.success().getStatus()){
                System.out.println("数据库创建成功");
            }else{
                System.out.println("数据库创建失败 : " + createDatabase.getMessage());
            }
        }

        //创建Colections
        //1.创建schama

        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(milvusCollectionName)
                // 字段1：主键字段（chunkID，INT64类型）
                .addFieldType(FieldType.newBuilder()
                        .withName("id")
                        .withDataType(DataType.Int64)
                        .withPrimaryKey(true) // 设为主键
                        .withAutoID(false)    // 不自动生成ID
                        .build())
                // 字段2：向量字段（用户画像向量，128维FLOAT_VECTOR）
                .addFieldType(FieldType.newBuilder()
                        .withName("vector")
                        .withDataType(DataType.FloatVector)
                        .withDimension(128) // 向量维度，根据业务调整
                        .build())
                // 字段3：chunk（chunk原文，String类型）
                .addFieldType(FieldType.newBuilder()
                        .withName("chunk")
                        .withDataType(DataType.String)
                        .build())
                .withShardsNum(2) // 分片数，默认1，根据数据量调整
                .build();
        R<RpcStatus> createCollection = milvusClient.createCollection(createCollectionParam);
        if(createCollection.getStatus() == R.success().getStatus()){
            System.out.println("集合创建成功");
        }else {
            System.out.println("集合创建失败 : " + createCollection.getMessage());
        }

        return milvusClient;
    }
    
    // Milvus向量存储配置（保留供将来使用）

    @Bean
    EmbeddingStore embeddingStore() {
        Document document = ClassPathDocumentLoader.loadDocument("面试题带答案版本-可编辑.md");

        document.metadata().putAll(Map.of("document_type", "interview_questions", // 文档类型：面试题
                "create_time", "2026-02-07",           // 创建时间
                "author", "admin"              ));// 将元数据绑定到文档

        // 使用Milvus作为向量存储
        MilvusEmbeddingStore embeddingStore = MilvusEmbeddingStore.builder()
                .milvusClient(milvusClient())
                .databaseName(milvusDatabase) // 指定数据库名（关键！）
                .collectionName(milvusCollectionName) // 指定自定义集合名
                .vectorFieldName("vector") // 映射自定义向量字段（关键！）
                .idFieldName("id") // 映射自定义主键字段（关键！）
                .textFieldName("chunk") // 映射自定义文本字段（关键！）
                .dimension(milvusDimension)
                .build();
        // 分割->嵌入->向量存储

        EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore())
                .documentSplitter(new DocumentByParagraphSplitter(1024, 256))
                .build();
        embeddingStoreIngestor.ingest(document);
        return embeddingStore;
    }

    @Bean
    ContentRetriever contentRetrieverMilvus() {

        // 优化检索参数
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore())
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(8) // 增加返回结果数量，提高召回率
                .minScore(0.65) // 降低最小分数阈值，提高召回率
                .build();
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()

                .contentInjector(DefaultContentInjector.builder()
                        .promptTemplate(PromptTemplate.from("{{userMessage}}\n{{contents}}"))
                        .build())
                .build();
        return contentRetriever;
    }
    @Bean
    WindowsContentInjector windowsContentInjector() {
        return new WindowsContentInjector(PromptTemplate.from("{{userMessage}}\n{{contents}}"), List.of("chunk"));
    }
    @Bean
    RewriteQueryTransformer rewriteQueryTransformer() {
        return new RewriteQueryTransformer(qwenChatModel);
    }


    @Bean
    RetrievalAugmentor retrievalAugmentor() {
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentInjector(windowsContentInjector())
                .queryTransformer(rewriteQueryTransformer())
                .queryRouter(new DefaultQueryRouter(List.of(contentRetrieverMilvus(), contentRetrieverXiaozhi())))
                .contentAggregator(ReRankingContentAggregator.builder()
                        .build())
                .build();
        return retrievalAugmentor;
    }




}
