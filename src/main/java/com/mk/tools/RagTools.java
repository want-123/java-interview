package com.mk.tools;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.R;
import io.milvus.param.highlevel.dml.GetIdsParam;
import io.milvus.param.highlevel.dml.response.GetResponse;
import io.milvus.response.QueryResultsWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2026/02/07/20:08
 * @Description:
 */
@Component
public class RagTools {
    @Autowired
    private EmbeddingStore embeddingStore;
    @Autowired
    private ContentRetriever contentRetriever;

    @Autowired
    private MilvusClient milvusClient;

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

    @Tool("根据用户问题召回相关的文档片段")
    public String serach(String question){
        List<Content> chunks = contentRetriever.retrieve(Query.from(question));
        StringBuilder sb = new StringBuilder();
        for(Content chunk:chunks){
            TextSegment textSegment = chunk.textSegment();
            sb.append(String.format("ChunkID：%s | 内容：%s | 元数据：%s\\n",
                    textSegment.metadata().getInteger("id"),
                    textSegment.text(),
                    textSegment.metadata().toMap().toString()
                    ));
        }
        String answer = sb.toString();
        return answer;
    }
    @Tool("根据ChunkID查询对应的文档片段完整内容和元数据")
    private String searchByChunkId(Integer chunkId){
        try {
            // 1. 查询对应Segment
            String segment = getSegmentByUserId(chunkId);
            if (segment == null) {
                return "ChunkID[" + chunkId + "]对应的内容已被删除";
            }

            // 3. 格式化返回
            return String.format("ChunkID：%s\n完整内容：%s",
                    chunkId,
                    segment
                   );
        } catch (Exception e) {
            e.printStackTrace();
            return "查询失败：" + e.getMessage();
        }
    }

    private String getSegmentByUserId(Integer chunkId) {
        // 1. 获取所有Segment
        R<GetResponse> response = milvusClient.get(GetIdsParam.newBuilder().withCollectionName(milvusCollectionName).withPrimaryIds(List.of(chunkId)).build());
        List<QueryResultsWrapper.RowRecord> rowRecords = response.getData().getRowRecords();
        // 2. 筛选对应Segment
        for (QueryResultsWrapper.RowRecord record : rowRecords) {
            if (record.contains("id") && record.get("id").equals(chunkId)) {
                return record.getFieldValues().toString();
            }
        }
        return null;
    }
}
