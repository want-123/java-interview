package com.mk.util;

import com.alibaba.fastjson2.JSON;
import com.mk.entity.JavaCodeQuestion;

import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/11/17:31
 * @Description:
 */
@Component
public class JavaProblemExtractor {
    @Autowired
    private static OllamaChatModel ollamaChatModel;
    // 2. 文本预处理：清理无关字符，统一格式
    private static String preprocessText(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }
        // 去除多余换行、制表符、特殊符号
        return rawText.replaceAll("\\s+", " ")
                .replaceAll("[\\n\\t\\r]", " ")
                .replaceAll("[^\u4e00-\u9fa5a-zA-Z0-9\\s\\.,;:()（）【】]", "")
                .trim();
    }

    // 3. 调用AI大模型API解析结构化信息:json串
    private static String callAIApi(String processedText) {

        // 3.1 构造AI提示词（核心：引导AI返回指定格式的JSON）
        String prompt = "请严格按照以下JSON格式提取这段Java编程题的结构化信息，仅返回JSON字符串，不要额外内容：\n" +
                "{\n" +
                "  \"title\": \"题目标题\",\n" +
                "  \"description\": \"题目描述\",\n" +
                "  \"inputRequire\": \"输入要求\",\n" +
                "  \"outputRequire\": \"输出要求\",\n" +
                "  \"knowledgePoint\": \"考点\",\n" +
                "  \"difficulty\": \"难度（简单/中等/困难）\",\n" +
                "  \"sampleInput\": \"示例输入\",\n" +
                "  \"sampleOutput\": \"示例输出\"\n" +
                "}\n" +
                "需要提取的编程题文本：" + processedText;
        //调用AI生成实体结构化信息
        String aiResponse =  ollamaChatModel.chat(prompt);
        return aiResponse;
    }

    // 4. 核心提取方法：整合预处理+AI解析+JSON转换
    public static JavaCodeQuestion extractProblem(String rawProblemText) {
        try {
            // 步骤1：文本预处理
            String processedText = preprocessText(rawProblemText);
            // 步骤2：调用AI API获取结构化JSON
            String aiJsonResult = callAIApi(processedText);
            // 步骤3：JSON转换为Java对象
            return JSON.parseObject(aiJsonResult, JavaCodeQuestion.class);
        } catch (Exception e) {
            System.err.println("提取失败：" + e.getMessage());
            return null;
        }
    }
}
