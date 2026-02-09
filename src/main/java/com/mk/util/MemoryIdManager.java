package com.mk.util;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/17/14:30
 * @Description:
 */
@Component
public class MemoryIdManager {
    @Autowired
    private ChatMemoryProvider chatMemoryProvider;
    public String generateMemoryId(Long userId) {
        // 命名规范：uid_${用户ID}_sid_${随机串}，方便后续按用户筛选
        return String.format("uid_%d_sid_%s", userId, UUID.randomUUID().toString().substring(0, 8));
    }

    public void clearMemoryId(String memoryId) {
        chatMemoryProvider.get(memoryId).clear();
    }

}
