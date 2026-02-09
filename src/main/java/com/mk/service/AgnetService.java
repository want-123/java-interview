package com.mk.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/20/14:13
 * @Description:
 */
public interface AgnetService {
    String chat(String memoryId, String userMessage);
    String chat2(String memoryId, String userMessage);


}
