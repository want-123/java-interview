package com.mk.util;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2026/02/10/18:54
 * @Description:
 */
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.segment.TextSegmentTransformer;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 自定义TextSegmentTransformer：为每个原始TextSegment生成潜在问题列表，并绑定原文元数据
 */
class QuestionGenerationTextSegmentTransformer implements TextSegmentTransformer {

    // 依赖LLM生成潜在问题
    private final ChatLanguageModel llm;
    // 生成问题的数量（可配置）
    private final int questionCount;

    // 构造器：传入LLM和问题数量
    public QuestionGenerationTextSegmentTransformer(ChatLanguageModel llm, int questionCount) {
        this.llm = llm;
        this.questionCount = questionCount;
    }


    /**
     * 调用LLM生成潜在问题的核心逻辑
     */
    private List<String> generatePotentialQuestions(String originalText) {
        // 构造Prompt，明确生成指定数量的潜在问题
        String prompt = String.format("""
                你是一个专业的问题生成助手，请针对以下文本生成%d个它能精准回答的潜在用户问题：
                要求：
                1. 问题必须能被该文本完整回答；
                2. 每个问题单独一行，无编号、无标点前缀；
                3. 语言简洁，符合用户日常查询习惯。
                
                文本内容：
                %s
                
                潜在问题：
                """, questionCount, originalText);

        // 调用LLM并解析结果
        String llmResponse = llm.chat(prompt);
        return List.of(llmResponse.split("\n"))
                .stream()
                .map(String::trim)
                .filter(q -> !q.isEmpty() && !q.isBlank()) // 过滤空行
                .collect(Collectors.toList());
    }

    @Override
    public TextSegment transform(TextSegment textSegment) {
        return textSegment;
    }

    @Override
    public List<TextSegment> transformAll(List<TextSegment> segments) {
        List<TextSegment> questionSegments = new ArrayList<>();

        for (TextSegment originalSeg : segments) {
            // 1. 提取原始文本
            String originalText = originalSeg.text();
            // 2. 生成潜在问题列表（1:N的核心）
            List<String> potentialQuestions = generatePotentialQuestions(originalText);
            // 3. 每个问题生成一个TextSegment，绑定原文元数据
            for (String question : potentialQuestions) {
                // 元数据：继承原始片段元数据 + 绑定原文 + 问题标识
                Metadata questionMetadata = originalSeg.metadata().copy();
                questionMetadata.put("originalText", originalText); // 核心：映射原文
                questionMetadata.put("questionId", UUID.randomUUID().toString());
                questionMetadata.put("sourceSegmentId", (String) originalSeg.metadata().toMap().get("chunkNumber"));
                // 4. 构建问题TextSegment（文本=问题，元数据=含原文的映射）
                TextSegment questionSeg = TextSegment.from(question, questionMetadata);
                questionSegments.add(questionSeg);
            }
        }
        return questionSegments;
    }
}
