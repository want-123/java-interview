package com.mk.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 面试模拟工具
 * 用于生成面试问题并评估回答质量
 */
@Component
public class InterviewSimulationTool {
    
    @Tool("生成面试问题，参数说明："
            + "jobType-岗位类型（如java, frontend, backend等），"
            + "difficulty-难度级别（如easy, medium, hard），"
            + "questionCount-问题数量（默认为5）")
    public String generateInterviewQuestions(String jobType, String difficulty, Integer questionCount) {
        if (questionCount == null) {
            questionCount = 5;
        }
        
        System.out.println("------------------------执行Tools:生成面试问题----------------");
        
        // 根据岗位类型和难度级别生成面试问题
        List<String> questions = generateQuestionsByTypeAndDifficulty(jobType, difficulty, questionCount);
        
        // 生成问题列表
        StringBuilder questionList = new StringBuilder();
        questionList.append("# 面试问题列表\n\n");
        questionList.append("## 岗位类型：").append(jobType).append("\n");
        questionList.append("## 难度级别：").append(difficulty).append("\n\n");
        
        for (int i = 0; i < questions.size(); i++) {
            questionList.append((i + 1)).append(". ").append(questions.get(i)).append("\n\n");
        }
        
        return questionList.toString();
    }
    
    @Tool("评估面试回答质量，参数说明："
            + "question-面试问题，"
            + "answer-面试回答，"
            + "jobType-岗位类型（如java, frontend, backend等）")
    public String evaluateInterviewAnswer(String question, String answer, String jobType) {
        System.out.println("------------------------执行Tools:评估面试回答----------------");
        
        // 评估回答质量
        int score = evaluateAnswerQuality(question, answer, jobType);
        
        // 生成评估报告
        StringBuilder evaluation = new StringBuilder();
        evaluation.append("# 面试回答评估报告\n\n");
        evaluation.append("## 面试问题：\n").append(question).append("\n\n");
        evaluation.append("## 面试回答：\n").append(answer).append("\n\n");
        evaluation.append("## 评估分数：").append(score).append("/100\n\n");
        
        // 根据分数生成评价
        if (score >= 90) {
            evaluation.append("## 评价：优秀\n回答非常全面，深入理解了问题的核心，展现了扎实的专业知识和丰富的实践经验。\n\n");
        } else if (score >= 80) {
            evaluation.append("## 评价：良好\n回答比较全面，对问题有较好的理解，展现了不错的专业知识和实践经验。\n\n");
        } else if (score >= 70) {
            evaluation.append("## 评价：一般\n回答基本合理，对问题有一定的理解，但在深度和广度上还有提升空间。\n\n");
        } else {
            evaluation.append("## 评价：需要改进\n回答不够全面，对问题的理解不够深入，建议加强相关知识的学习和实践。\n\n");
        }
        
        // 生成改进建议
        String suggestion = generateImprovementSuggestion(question, answer, jobType);
        evaluation.append("## 改进建议：\n").append(suggestion).append("\n");
        
        return evaluation.toString();
    }
    
    /**
     * 根据岗位类型和难度级别生成面试问题
     */
    private List<String> generateQuestionsByTypeAndDifficulty(String jobType, String difficulty, int questionCount) {
        List<String> questions = new ArrayList<>();
        
        if ("java".equalsIgnoreCase(jobType)) {
            if ("easy".equalsIgnoreCase(difficulty)) {
                questions.add("请解释Java中的面向对象编程的基本概念");
                questions.add("请解释Java中的继承和多态");
                questions.add("请解释Java中的异常处理机制");
                questions.add("请解释Java中的String类为什么是不可变的");
                questions.add("请解释Java中的基本数据类型和引用数据类型的区别");
            } else if ("medium".equalsIgnoreCase(difficulty)) {
                questions.add("请解释Java中的线程安全问题及解决方案");
                questions.add("请解释Java中的垃圾回收机制");
                questions.add("请解释Java中的类加载机制");
                questions.add("请解释Java中的集合框架");
                questions.add("请解释Java中的IO流");
            } else if ("hard".equalsIgnoreCase(difficulty)) {
                questions.add("请解释Java中的并发编程和锁机制");
                questions.add("请解释Java中的JVM内存模型");
                questions.add("请解释Java中的设计模式及其应用场景");
                questions.add("请解释Java中的性能优化策略");
                questions.add("请解释Java中的分布式系统设计");
            }
        } else if ("frontend".equalsIgnoreCase(jobType)) {
            if ("easy".equalsIgnoreCase(difficulty)) {
                questions.add("请解释HTML5的新特性");
                questions.add("请解释CSS3的新特性");
                questions.add("请解释JavaScript中的变量声明方式");
                questions.add("请解释JavaScript中的事件处理");
                questions.add("请解释前端开发中的响应式设计");
            } else if ("medium".equalsIgnoreCase(difficulty)) {
                questions.add("请解释JavaScript中的闭包和作用域");
                questions.add("请解释前端框架（如React、Vue）的工作原理");
                questions.add("请解释前端开发中的状态管理");
                questions.add("请解释前端开发中的性能优化");
                questions.add("请解释前端开发中的跨域问题及解决方案");
            } else if ("hard".equalsIgnoreCase(difficulty)) {
                questions.add("请解释前端开发中的微前端架构");
                questions.add("请解释前端开发中的服务端渲染");
                questions.add("请解释前端开发中的PWA（渐进式Web应用）");
                questions.add("请解释前端开发中的WebAssembly");
                questions.add("请解释前端开发中的安全问题及解决方案");
            }
        } else if ("backend".equalsIgnoreCase(jobType)) {
            if ("easy".equalsIgnoreCase(difficulty)) {
                questions.add("请解释RESTful API的设计原则");
                questions.add("请解释数据库中的事务和ACID特性");
                questions.add("请解释HTTP协议的基本原理");
                questions.add("请解释服务器端开发中的会话管理");
                questions.add("请解释服务器端开发中的认证和授权");
            } else if ("medium".equalsIgnoreCase(difficulty)) {
                questions.add("请解释分布式系统中的CAP理论");
                questions.add("请解释服务器端开发中的缓存策略");
                questions.add("请解释服务器端开发中的消息队列");
                questions.add("请解释服务器端开发中的负载均衡");
                questions.add("请解释服务器端开发中的数据库优化");
            } else if ("hard".equalsIgnoreCase(difficulty)) {
                questions.add("请解释分布式系统中的一致性算法");
                questions.add("请解释服务器端开发中的高可用架构");
                questions.add("请解释服务器端开发中的微服务架构");
                questions.add("请解释服务器端开发中的性能监控和调优");
                questions.add("请解释服务器端开发中的安全漏洞及防护措施");
            }
        }
        
        // 确保返回指定数量的问题
        if (questions.size() > questionCount) {
            return questions.subList(0, questionCount);
        }
        return questions;
    }
    
    /**
     * 评估回答质量
     */
    private int evaluateAnswerQuality(String question, String answer, String jobType) {
        int score = 0;
        
        // 1. 回答长度（20分）
        if (answer.length() > 100) {
            score += 20;
        } else if (answer.length() > 50) {
            score += 15;
        } else if (answer.length() > 20) {
            score += 10;
        } else {
            score += 5;
        }
        
        // 2. 回答内容的相关性（30分）
        if (answer.contains(question.substring(0, Math.min(20, question.length())))) {
            score += 30;
        } else if (answer.contains(question.substring(0, Math.min(10, question.length())))) {
            score += 20;
        } else {
            score += 10;
        }
        
        // 3. 回答内容的专业性（50分）
        if ("java".equalsIgnoreCase(jobType)) {
            if (answer.contains("面向对象") || answer.contains("线程") || answer.contains("垃圾回收") || answer.contains("设计模式")) {
                score += 50;
            } else if (answer.contains("类") || answer.contains("对象") || answer.contains("方法") || answer.contains("异常")) {
                score += 30;
            } else {
                score += 10;
            }
        } else if ("frontend".equalsIgnoreCase(jobType)) {
            if (answer.contains("React") || answer.contains("Vue") || answer.contains("JavaScript") || answer.contains("CSS")) {
                score += 50;
            } else if (answer.contains("前端") || answer.contains("HTML") || answer.contains("浏览器") || answer.contains("响应式")) {
                score += 30;
            } else {
                score += 10;
            }
        } else if ("backend".equalsIgnoreCase(jobType)) {
            if (answer.contains("RESTful") || answer.contains("数据库") || answer.contains("缓存") || answer.contains("分布式")) {
                score += 50;
            } else if (answer.contains("服务器") || answer.contains("API") || answer.contains("HTTP") || answer.contains("安全")) {
                score += 30;
            } else {
                score += 10;
            }
        }
        
        return score;
    }
    
    /**
     * 生成改进建议
     */
    private String generateImprovementSuggestion(String question, String answer, String jobType) {
        StringBuilder suggestion = new StringBuilder();
        
        if ("java".equalsIgnoreCase(jobType)) {
            if (!answer.contains("面向对象")) {
                suggestion.append("- 建议加强对面向对象编程概念的理解和应用\n");
            }
            if (!answer.contains("线程")) {
                suggestion.append("- 建议加强对并发编程的学习\n");
            }
            if (!answer.contains("设计模式")) {
                suggestion.append("- 建议学习常见的设计模式及其应用场景\n");
            }
        } else if ("frontend".equalsIgnoreCase(jobType)) {
            if (!answer.contains("React") && !answer.contains("Vue")) {
                suggestion.append("- 建议学习主流前端框架的使用\n");
            }
            if (!answer.contains("JavaScript")) {
                suggestion.append("- 建议加强对JavaScript的深入学习\n");
            }
            if (!answer.contains("性能优化")) {
                suggestion.append("- 建议学习前端性能优化策略\n");
            }
        } else if ("backend".equalsIgnoreCase(jobType)) {
            if (!answer.contains("RESTful")) {
                suggestion.append("- 建议学习RESTful API的设计原则\n");
            }
            if (!answer.contains("数据库")) {
                suggestion.append("- 建议加强对数据库的学习和优化\n");
            }
            if (!answer.contains("分布式")) {
                suggestion.append("- 建议学习分布式系统的设计原则\n");
            }
        }
        
        // 通用建议
        suggestion.append("- 建议在回答问题时结合具体的项目经验进行说明\n");
        suggestion.append("- 建议在回答问题时保持逻辑清晰，层次分明\n");
        suggestion.append("- 建议在回答问题后主动询问面试官是否还有其他问题\n");
        
        return suggestion.toString();
    }
}
