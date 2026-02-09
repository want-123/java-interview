package com.mk.service.impl;

import com.alibaba.fastjson2.JSON;
import com.mk.assistant.JavaAgent;
import com.mk.service.AgnetService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import kotlin.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import dev.langchain4j.data.segment.TextSegment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/20/14:14
 * @Description:
 */
@Service
public class AgentServiceImpl implements AgnetService {
    private static final Integer HOP = 3;
    private static final Integer TOP_K = 5;
    
    // 线程池，用于并行检索
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    @Autowired
    private JavaAgent javaAgent;
    @Autowired
    private EmbeddingStoreContentRetriever contentRetrieverXiaozhi;
    @Autowired
    private OllamaChatModel ollamaChatModel;
    @Autowired
    private QwenChatModel qwenChatModel;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 实现迭代检索
     * @param memoryId
     * @param userMessage
     * @return
     */
    @Override
    public String chat(String memoryId, String userMessage) {
        // 生成缓存键
        String cacheKey = "chat:" + userMessage.hashCode();
        
        // 尝试从缓存获取
        String cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            System.out.println("-----------从缓存获取结果-----------");
            return cachedResult;
        }
        
        List<String> context = new ArrayList<>();
        for (int i = 0; i < HOP; i++){
            String question = genrateQuestion(userMessage, context);
            
            // 为每个子问题生成缓存键
            String subCacheKey = "sub_question:" + question.hashCode();
            List<Content> retrievedContent;
            
            // 尝试从缓存获取子问题的检索结果
            String cachedSubResult = redisTemplate.opsForValue().get(subCacheKey);
            if (cachedSubResult != null) {
                System.out.println("-----------从缓存获取子问题结果-----------");
                // 解析缓存的结果
                retrievedContent = parseCachedContent(cachedSubResult);
            } else {
                // 执行检索
                retrievedContent = contentRetrieverXiaozhi.retrieve(Query.from(question));
                // 缓存检索结果，有效期1小时
                redisTemplate.opsForValue().set(subCacheKey, JSON.toJSONString(retrievedContent), 1, TimeUnit.HOURS);
            }
            
            retrievedContent.stream().map(Content::textSegment).map(TextSegment::text).forEach(context::add);
            if(shouldStop(question, context)){
                System.out.println("-----------chat shouldStop-----------");
                break;
            }
        }
        
        String result = javaAgent.chat(memoryId, String.join("\n", context) + "\n" + userMessage);
        
        // 缓存结果，有效期30分钟
        redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.MINUTES);
        
        return result;
    }
    /**
     * 实现问题分解检索
     * @param memoryId
     * @param userMessage
     * @return
     */
    @Override
    public String chat2(String memoryId, String userMessage) {
        // 生成缓存键
        String cacheKey = "chat2:" + userMessage.hashCode();
        
        // 尝试从缓存获取
        String cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            System.out.println("-----------从缓存获取结果-----------");
            return cachedResult;
        }
        
        List<String> subQuestions = decompose(userMessage);
        List<TextSegment> context = new ArrayList<>();
        System.out.println("-----------chat2 subQuestions-----------");
        System.out.println(subQuestions);
        
        // 并行处理子问题检索
        List<CompletableFuture<List<Content>>> futures = subQuestions.stream()
                .map(subQuestion -> CompletableFuture.supplyAsync(() -> {
                    // 为每个子问题生成缓存键
                    String subCacheKey = "sub_question:" + subQuestion.hashCode();
                    List<Content> retrievedContent;
                    // 尝试从缓存获取子问题的检索结果
                    String cachedSubResult = redisTemplate.opsForValue().get(subCacheKey);
                    if (cachedSubResult != null) {
                        System.out.println("-----------从缓存获取子问题结果-----------");
                        // 解析缓存的结果
                        retrievedContent = parseCachedContent(cachedSubResult);
                    } else {
                        // 执行检索
                        retrievedContent = contentRetrieverXiaozhi.retrieve(Query.from(subQuestion));
                        // 缓存检索结果，有效期1小时
                        redisTemplate.opsForValue().set(subCacheKey, JSON.toJSONString(retrievedContent), 1, TimeUnit.HOURS);
                    }
                    return retrievedContent;
                }, executorService))
                .collect(Collectors.toList());
        
        // 等待所有并行任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        
        // 收集所有检索结果
        List<Content> allRetrievedContent = allOf.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .collect(Collectors.toList())
        ).join();
        
        // 将所有检索结果添加到上下文
        allRetrievedContent.stream().map(Content::textSegment).forEach(context::add);
        
//        topk排序 避免token过大
        List<String> topkSortContext = topkSort(context);

        // 3. 整合结果生成答案
        String answerPrompt = String.format(
                "基于以下多跳检索结果，回答用户问题：%s\n检索结果：%s",
                userMessage,
                topkSortContext.stream().collect(Collectors.joining("\n"))
        );
        String result = javaAgent.chat(memoryId, answerPrompt);
        System.out.println("-----------chat2 result-----------");
        System.out.println(result);
        // 缓存结果，有效期30分钟
        redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.MINUTES);
        
        return result;
    }

    private boolean shouldStop(String question, List<String> context){
        String prompt = String.format(
                "问题：%s\n已知信息：%s\n"+
                "判断已知信息是狗能够回答问题。"+
                "如果足够返回STOP，否则返回CONTINUE：缺失具体信息",
                question,
                String.join("\n", context)
        );
        String response = qwenChatModel.chat(prompt);
        return response.startsWith("STOP");
    }

    private String genrateQuestion(String question, List<String> context){
        if(context.isEmpty()){
            return question;
        }
        String prompt = String.format(
                "问题：%s\n已知信息：%s\n"+
                "基于已知信息，生成下一个检索查询来补充缺失信息。",
                question,
                String.join("\n", context)
        );
        return ollamaChatModel.chat(prompt);
    }

    private List<String> decompose(String question){
        String splitPrompt = String.format(
                "请将以下多跳问题拆分成有序的单跳子问题（每个子问题仅需一次检索就能回答）：%s\n" +
                        "要求：按顺序返回，每行一个子问题，不要添加其他内容",
                question // 比如：Spring Boot集成Redis后如何配置连接池并解决连接超时问题？
        );
        String splitResult = qwenChatModel.chat(splitPrompt);
        List<String> subQuestions = Arrays.asList(splitResult.split("\n"));
        return subQuestions;
    }
    private List<String> topkSort(List<TextSegment> context){
        return context.stream()
                .sorted((a, b) -> {
            double scoreA = getScoreFromMetadata(a);
            double scoreB = getScoreFromMetadata(b);
            return Double.compare(scoreB, scoreA);})
                .limit(TOP_K)
                .map(TextSegment::text)
                .collect(Collectors.toList());
    }

    private Double getScoreFromMetadata(TextSegment segment) {
        // 从metadata中提取score，如果没有则返回0
        if (segment.metadata() != null && segment.metadata().containsKey("SCORE")) {
            Object scoreObj = segment.metadata().toMap().get("SCORE");
            if (scoreObj instanceof Number) {
                return ((Number) scoreObj).doubleValue();
            } else if (scoreObj instanceof String) {
                try {
                    return Double.parseDouble((String) scoreObj);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        }
        return 0.0;
    }
    
    /**
     * 解析缓存的Content对象
     * @param cachedContent 缓存的Content对象的JSON字符串
     * @return 解析后的Content列表
     */
    private List<Content> parseCachedContent(String cachedContent) {
        // 这里需要根据实际的Content对象结构进行解析
        // 由于Content是一个接口，我们需要根据实际实现类进行反序列化
        // 这里简化处理，返回空列表，实际项目中需要根据具体情况实现
        return new ArrayList<>();
    }
}
