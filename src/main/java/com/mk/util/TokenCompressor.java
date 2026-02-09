package com.mk.util;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/09/21:10
 * @Description:
 */
@Component
@Slf4j
public class TokenCompressor {
    @Autowired
    private OllamaChatModel ollamaChatModel;

    private static final String prompt = """
            请对给Java问题进行压缩 1/4 token 数并保留关键信息，只输出结果。
            """;

    public  String compressJavaQuestion(String question) {
        UserMessage userMessage = UserMessage.from(prompt + question);
        return ollamaChatModel.chat(userMessage).aiMessage().text();
    }
}
