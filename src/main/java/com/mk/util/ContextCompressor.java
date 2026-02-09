package com.mk.util;


import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;

import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/15/15:07
 * @Description:
 */
@Component
@Slf4j
public class ContextCompressor {
    @Autowired
    private OllamaChatModel ollamaChatModel;

    public List<ChatMessage> compress(List<ChatMessage> originalMessages, int maxSummaryLength, int keepLatestMessages) {
        // 1. 消息数量不足时，直接返回原始消息（无需压缩）
        if (originalMessages.size() <= keepLatestMessages) {
            return originalMessages;
        }

        // 2. 拆分：早期历史（待摘要） + 最新消息（保留原始）
        List<ChatMessage> historyToSummarize = originalMessages.subList(0, originalMessages.size() - keepLatestMessages);
        List<ChatMessage> latestMessages = originalMessages.subList(originalMessages.size() - keepLatestMessages, originalMessages.size());

        // 3. 转换历史消息为文本格式
        String historyText = historyToSummarize.stream()
                .map(msg -> msg.toString())
                .collect(Collectors.joining("\n"));

        // 4. 构建摘要提示词，引导LLM生成精简摘要
        String promptTemplate = """
                请压缩以下对话历史，保留所有关键信息（用户核心需求、问题、回答要点），去除冗余内容。
                要求：
                1. 长度不超过 %d 个字符
                2. 原意不变，不遗漏重要信息
                3. 语言简洁、符合对话逻辑

                对话历史：
                %s
                """;
        Prompt prompt = new Prompt(String.format(promptTemplate, maxSummaryLength, historyText));

        // 5. 调用LLM生成摘要
        String response = ollamaChatModel.chat(prompt.toString());

        // 6. 封装摘要为SystemMessage，和最新消息合并返回
        ChatMessage summaryMessage = (ChatMessage) new SystemMessage("【历史对话摘要】：" + response);
        return  Stream.concat(Stream.of(summaryMessage), latestMessages.stream())
                .collect(Collectors.toList());
    }
}
