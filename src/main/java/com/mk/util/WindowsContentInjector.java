package com.mk.util;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import io.milvus.client.MilvusClient;
import io.milvus.grpc.IDs;
import io.milvus.grpc.QueryResults;
import io.milvus.grpc.SearchResultData;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.highlevel.dml.GetIdsParam;
import io.milvus.param.highlevel.dml.response.GetResponse;
import io.milvus.response.QueryResultsWrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2026/02/10/19:32
 * @Description:
 */
public class WindowsContentInjector implements ContentInjector {
    public static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = PromptTemplate.from("{{userMessage}}\n\nAnswer using the following information:\n{{contents}}");
    private  PromptTemplate promptTemplate;
    private  List<String> metadataKeysToInclude;
    private MilvusClient milvusClient;
    private Integer windowSize;

    private String collectionName;

    public WindowsContentInjector(List<String> metadataKeysToInclude) {
        this.metadataKeysToInclude = metadataKeysToInclude;
    }

    public WindowsContentInjector(PromptTemplate promptTemplate) {
        this.promptTemplate = promptTemplate;
    }

    public WindowsContentInjector(MilvusClient milvusClient) {
        this.milvusClient = milvusClient;
    }

    public WindowsContentInjector(PromptTemplate promptTemplate, List<String> metadataKeysToInclude) {
        this.promptTemplate = promptTemplate;
        this.metadataKeysToInclude = metadataKeysToInclude;
    }

    @Override
    public ChatMessage inject(List<Content> list, ChatMessage chatMessage) {
        DefaultContentInjector defaultContentInjector = DefaultContentInjector.builder()
                .metadataKeysToInclude(metadataKeysToInclude)
                .promptTemplate(promptTemplate)
                .build();
        List<Content> contents = new ArrayList<>();

        for(Content content:list){
            Integer anchor = content.textSegment().metadata().getInteger("id");
            // 构建范围查询条件
            int startIdx = Math.max(0, anchor - windowSize);
            int endIdx = anchor + windowSize; // 假设segment_index连续

            // Milvus表达式：同文档 && segment_index在范围内
            String expr = String.format(
                    "id >= %d && segment_index <= %d",
                    startIdx, endIdx
            );
            QueryParam queryParam = QueryParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withExpr(expr)
                    .addOutField("id")
                    .addOutField("segment_index")
                    .addOutField("text")
                    .addOutField("document_id")
                    .build();

            R<QueryResults> response = milvusClient.query(queryParam);
            QueryResultsWrapper wrapper = new QueryResultsWrapper(response.getData());
            // 解析并按segment_index排序
            List<TextSegment> segments = new ArrayList<>();
            for (int i = 0; i < wrapper.getRowCount(); i++) {
                Integer id = (Integer) wrapper.getDynamicWrapper().get(i,"id");
                Integer segIdx = (Integer) wrapper.getDynamicWrapper().get(i, "segment_index");
                String text = (String) wrapper.getDynamicWrapper().get(i, "text");
                String docId = (String) wrapper.getDynamicWrapper().get(i, "document_id");

                segments.add(new TextSegment(text, Metadata.from(Map.of(
                        "id", id,
                        "document_id", docId,
                        "segment_index", segIdx
                ))));
            }

            // 按segment_index升序排序，确保文本顺序正确
            segments.sort(Comparator.comparingInt(s -> s.metadata().getInteger("segment_index").intValue()));

            List<Content> ss = segments.stream()
                    .map(s -> TextSegment.from(s.text(), Metadata.from(Map.of(
                            "id", s.metadata().getInteger("id"),
                            "document_id", s.metadata().getString("document_id"),
                            "segment_index", s.metadata().getInteger("segment_index"),
                            "is_anchor", s.metadata().getInteger("id").equals(anchor) // 标记是否为锚点
                    ))))
                   .map(s -> Content.from(s))
                    .collect(Collectors.toList());
            contents.addAll(ss);
        }

        return defaultContentInjector.inject(contents, chatMessage);
    }

}
