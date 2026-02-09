package com.mk.store;

import com.mk.util.ContextCompressor;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/15/15:05
 * @Description:
 */

public class CompressedMessageWindowChatMemory implements ChatMemory{
    private final ContextCompressor contextCompressor;
    // 核心配置：复用的存储层和消息窗口大小
    private final ChatMemoryStore chatMemoryStore;
    private final int maxMessages;
    private final Object id;

    public CompressedMessageWindowChatMemory(Builder builder) {
        this.id = builder.id;
        this.chatMemoryStore = builder.store;
        this.maxMessages = builder.maxMessages;
        this.contextCompressor = builder.contextCompressor;
    }
    @Override
    public Object id() {
        return this.id;
    }

    @Override
    public void add(ChatMessage chatMessage) {
        Object memoryId = this.id;
        List<ChatMessage> messages = new LinkedList(chatMemoryStore.getMessages(memoryId));

        // 复用MessageWindowChatMemory的消息窗口逻辑（系统消息去重、容量限制）
        if (chatMessage instanceof dev.langchain4j.data.message.SystemMessage systemMessage) {
            messages.removeIf(m -> m instanceof dev.langchain4j.data.message.SystemMessage);
            messages.add(0, systemMessage); // 系统消息放最前面
        } else {
            messages.add(chatMessage);
        }
        // 保证消息数量不超过maxMessages（复用原有清理逻辑）
        ensureCapacity(messages, maxMessages);
        chatMemoryStore.updateMessages(memoryId, messages);
    }

    @Override
    public List<ChatMessage> messages() {
        long startTime = System.currentTimeMillis();
        List<ChatMessage> messages = new LinkedList(this.chatMemoryStore.getMessages(this.id()));
        ensureCapacity(messages, this.maxMessages);
        // 压缩消息 (这部分还可以优化)
        messages = contextCompressor.compress(messages, 1024, 5);
        long endTime = System.currentTimeMillis();
        long costTime = endTime - startTime;
        System.out.printf("【messages，执行耗时：%d 毫秒%n", costTime);
        //System.out.println("【messages】：" + messages);
        return messages;
    }

    @Override
    public void clear() {
        this.chatMemoryStore.deleteMessages(this.id());
    }
    // 校验当前memoryId是否存在
    private void ensureCapacity(List<ChatMessage> messages, int maxMessages) {
        while (messages.size() > maxMessages) {
            int evictIndex = messages.get(0) instanceof dev.langchain4j.data.message.SystemMessage ? 1 : 0;
            messages.remove(evictIndex);
        }
    }
    public static CompressedMessageWindowChatMemory.Builder builder() {
        return new CompressedMessageWindowChatMemory.Builder();
    }


    public static class Builder {
        private Object id = "default";
        private Integer maxMessages;
        private ChatMemoryStore store;

        private ContextCompressor contextCompressor;

        public Builder() {
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder maxMessages(Integer maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder chatMemoryStore(ChatMemoryStore store) {
            this.store = store;
            return this;
        }

        public Builder contextCompressor(ContextCompressor contextCompressor) {
            this.contextCompressor = contextCompressor;
            return this;
        }
        public CompressedMessageWindowChatMemory build() {
            return new CompressedMessageWindowChatMemory(this);
        }

        public ChatMemoryStore store() {
            return (ChatMemoryStore)(this.store);
        }

        public ContextCompressor contextCompressor() {
            return this.contextCompressor;
        }
    }
}
