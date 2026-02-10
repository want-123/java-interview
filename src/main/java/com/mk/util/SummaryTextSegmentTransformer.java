package com.mk.util;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.segment.TextSegmentTransformer;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2026/02/10/18:31
 * @Description:
 */
public class SummaryTextSegmentTransformer implements TextSegmentTransformer {
    private final ChatLanguageModel llm;
    private final String summaryPrompt;

    // 构造器：传入LLM和自定义摘要Prompt
    public SummaryTextSegmentTransformer(ChatLanguageModel llm, String summaryPrompt) {
        this.llm = llm;
        this.summaryPrompt = summaryPrompt;
    }
    @Override
    public TextSegment transform(TextSegment textSegment) {
        // 1. 获取分块原文（TextSegment的text()就是分块原文）
        String originalText = textSegment.text();

        // 2. 生成摘要（用LLM处理原文）
        String summaryText = llm.chat(String.format(summaryPrompt, originalText));

        // 3. 构建新的元数据：保留原有元数据 + 新增「原文」+ 自定义分块ID
        Metadata newMetadata = new Metadata(textSegment.metadata().toMap()); // 继承原有元数据
        newMetadata.put("originalText", originalText); // 存入原文（核心！）
        newMetadata.put("chunkId", UUID.randomUUID().toString()); // 新增唯一标识
        // 4. 返回新的 TextSegment：content=摘要，metadata=含原文的元数据
        return TextSegment.from(summaryText, newMetadata);
    }
}
