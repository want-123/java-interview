package com.mk.util;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import io.milvus.client.MilvusClient;
import io.milvus.param.R;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.highlevel.dml.GetIdsParam;
import io.milvus.param.highlevel.dml.response.GetResponse;

import java.util.List;

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

        for(Content content:list){
            R<GetResponse> response = milvusClient.get(GetIdsParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withPrimaryIds(List.of(content.textSegment().metadata().getInteger("id")))
                    .build());

        }
        return ;
    }
}
