package com.mk.entity;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/11/17:27
 * @Description:
 */
@Data
public class JavaCodeQuestion{
    private String title;        // 题目标题
    private String description;  // 题目描述
    private String inputRequire; // 输入要求
    private String outputRequire;// 输出要求
    private String knowledgePoint;// 考点（如数组、多线程、集合等）
    private String difficulty;   // 难度（简单/中等/困难）
    private String sampleInput;  // 示例输入
    private String sampleOutput; // 示例输出

    @Override
    public String toString() {
        return "Java编程题结构化信息：\n" +
                "标题：" + title + "\n" +
                "描述：" + description + "\n" +
                "输入要求：" + inputRequire + "\n" +
                "输出要求：" + outputRequire + "\n" +
                "考点：" + knowledgePoint + "\n" +
                "难度：" + difficulty + "\n" +
                "示例输入：" + sampleInput + "\n" +
                "示例输出：" + sampleOutput;
    }
}
