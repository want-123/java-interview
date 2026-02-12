package com.mk.util;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.QueryTransformer;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2026/02/12/19:16
 * @Description:
 */
public class RewriteQueryTransformer implements QueryTransformer {
    private static QwenChatModel qwenChatModel;

    public RewriteQueryTransformer() {
    }

    public RewriteQueryTransformer(QwenChatModel qwenChatModel) {
        this.qwenChatModel = qwenChatModel;
    }

    @Override
    public Collection<Query> transform(Query query) {
        String rewrittenQuery = qwenChatModel.chat(String.format("请重写以下用户查询，使其语义更完整、表述更清晰，适合用于文本向量相似度检索，保持原始意图不变：\n" +
                "用户查询：%s\n" +
                "仅输出重写后的查询文本，不要添加其他内容。", query.text()));
        return List.of(Query.from(rewrittenQuery, query.metadata()));
    }
}
