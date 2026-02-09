package com.mk.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码评估工具
 * 用于分析Java代码的质量和潜在问题
 */
@Component
public class CodeEvaluationTool {
    
    @Tool("评估Java代码质量，参数说明："
            + "code-Java代码内容，"
            + "language-编程语言（默认为java）")
    public String evaluateCode(String code, String language) {
        if (language == null) {
            language = "java";
        }
        
        System.out.println("------------------------执行Tools:代码评估----------------");
        
        // 1. 代码质量分析
        List<String> qualityIssues = analyzeCodeQuality(code);
        
        // 2. 潜在问题检测
        List<String> potentialIssues = detectPotentialIssues(code);
        
        // 3. 优化建议
        List<String> optimizationSuggestions = generateOptimizationSuggestions(code);
        
        // 4. 生成评估报告
        StringBuilder report = new StringBuilder();
        report.append("# Java代码评估报告\n\n");
        
        if (qualityIssues.isEmpty()) {
            report.append("## 代码质量：良好\n未发现明显的代码质量问题。\n\n");
        } else {
            report.append("## 代码质量问题：\n");
            for (String issue : qualityIssues) {
                report.append("- ").append(issue).append("\n");
            }
            report.append("\n");
        }
        
        if (potentialIssues.isEmpty()) {
            report.append("## 潜在问题：无\n未发现明显的潜在问题。\n\n");
        } else {
            report.append("## 潜在问题：\n");
            for (String issue : potentialIssues) {
                report.append("- ").append(issue).append("\n");
            }
            report.append("\n");
        }
        
        if (optimizationSuggestions.isEmpty()) {
            report.append("## 优化建议：无\n代码已经相当优化。\n\n");
        } else {
            report.append("## 优化建议：\n");
            for (String suggestion : optimizationSuggestions) {
                report.append("- ").append(suggestion).append("\n");
            }
            report.append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * 分析代码质量
     */
    private List<String> analyzeCodeQuality(String code) {
        List<String> issues = new ArrayList<>();
        
        // 1. 检查代码长度
        if (code.length() > 5000) {
            issues.add("代码过长，建议拆分为多个方法或类");
        }
        
        // 2. 检查方法长度
        String[] lines = code.split("\\n");
        int methodStart = -1;
        int braceCount = 0;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            if (line.startsWith("public") || line.startsWith("private") || line.startsWith("protected")) {
                if (line.contains("(")) {
                    methodStart = i;
                    braceCount = 0;
                }
            }
            
            if (methodStart != -1) {
                for (char c : line.toCharArray()) {
                    if (c == '{') {
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                        if (braceCount == 0) {
                            int methodLength = i - methodStart + 1;
                            if (methodLength > 50) {
                                issues.add("方法过长（" + methodLength + "行），建议拆分为多个方法");
                            }
                            methodStart = -1;
                            break;
                        }
                    }
                }
            }
        }
        
        // 3. 检查命名规范
        if (!code.matches(".*[a-zA-Z0-9_]+.*")) {
            issues.add("代码中可能存在不规范的命名");
        }
        
        return issues;
    }
    
    /**
     * 检测潜在问题
     */
    private List<String> detectPotentialIssues(String code) {
        List<String> issues = new ArrayList<>();
        
        // 1. 检查空指针异常
        if (code.contains("== null") || code.contains("!= null")) {
            issues.add("可能存在空指针异常风险，建议使用Optional或空指针检查");
        }
        
        // 2. 检查资源泄露
        if (code.contains("new FileInputStream") || code.contains("new FileOutputStream") || code.contains("new Connection")) {
            if (!code.contains("try-with-resources") && !code.contains("finally")) {
                issues.add("可能存在资源泄露风险，建议使用try-with-resources或在finally中关闭资源");
            }
        }
        
        // 3. 检查线程安全
        if (code.contains("static") && code.contains("ArrayList") || code.contains("static") && code.contains("HashMap")) {
            issues.add("可能存在线程安全问题，建议使用线程安全的集合类");
        }
        
        return issues;
    }
    
    /**
     * 生成优化建议
     */
    private List<String> generateOptimizationSuggestions(String code) {
        List<String> suggestions = new ArrayList<>();
        
        // 1. 建议使用Lambda表达式
        if (code.contains("new Runnable()") || code.contains("new Comparator()")) {
            suggestions.add("建议使用Lambda表达式简化代码");
        }
        
        // 2. 建议使用Stream API
        if (code.contains("for (") && code.contains(".add(")) {
            suggestions.add("建议使用Stream API简化集合操作");
        }
        
        // 3. 建议使用局部变量
        if (code.contains("get(")) {
            suggestions.add("建议使用局部变量存储频繁访问的值，提高性能");
        }
        
        return suggestions;
    }
}
