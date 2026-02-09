package com.mk.entity;



import dev.langchain4j.rag.content.Content;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/16/19:37
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagReply {
    private String answer;          // AI 答案
    private List<Ref> sources;      // 文档片段

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Ref {
        private String text;        // 原文
        private Map<String,Object> meta; // 元数据
//        private Double score;       // 相似度
    }
}