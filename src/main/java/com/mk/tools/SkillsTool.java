package com.mk.tools;

import com.mk.util.ExternalScriptExecutor;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2026/02/10/13:46
 * @Description:
 */
@Component
public class SkillsTool {
    // ========== 基础配置 ==========
    private static final String SKILLS_ROOT = "skills/";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    // 正则：解析SKILL.md各区块
    private static final Pattern METADATA_PATTERN = Pattern.compile("---\\s*name:\\s*(.*?)\\s*description:\\s*(.*?)\\s*---", Pattern.DOTALL);
    private static final Pattern FLOW_PATTERN = Pattern.compile("## 流程描述\\s*(.*?)\\s*## 执行脚本", Pattern.DOTALL);
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("## 执行脚本（script）\\s*```\\s*script_path:\\s*(.*?)\\s*script_type:\\s*(.*?)\\s*params:\\s*(.*?)\\s*(timeout:\\s*(.*?)\\s*)?```", Pattern.DOTALL);
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("## 引用文件（reference）\\s*(.*)", Pattern.DOTALL);

    // ========== 线程池配置（管理外部脚本执行） ==========
    private static final ExecutorService SCRIPT_EXECUTOR = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("external-script-thread-" + System.currentTimeMillis() % 1000);
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    // ====================== 工具1：获取所有技能元数据 ======================
    @Tool("获取当前Agent系统拥有的所有skills元数据，返回JSON格式：[{\"name\":\"技能名\",\"description\":\"描述\",\"skillId\":\"技能文件夹名\"}]")
    public String getAllSkillsMetadata() {
        List<Map<String, String>> metadataList = new ArrayList<>();

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream rootStream = classLoader.getResourceAsStream(SKILLS_ROOT);
            if (rootStream == null) {
                return "{\"error\":\"skills目录不存在：" + SKILLS_ROOT + "\"}";
            }

            Path rootPath = Paths.get(Objects.requireNonNull(classLoader.getResource(SKILLS_ROOT)).toURI());
            Files.list(rootPath).filter(Files::isDirectory).forEach(skillDir -> {
                String skillId = skillDir.getFileName().toString();
                String skillMdPath = SKILLS_ROOT + skillId + "/SKILL.md";
                String mdContent = readFileFromResource(skillMdPath);

                if (mdContent == null) {
                    metadataList.add(Map.of(
                            "skillId", skillId,
                            "name", "未知",
                            "description", "SKILL.md文件不存在"
                    ));
                    return;
                }

                Matcher matcher = METADATA_PATTERN.matcher(mdContent);
                if (matcher.find()) {
                    metadataList.add(Map.of(
                            "skillId", skillId,
                            "name", matcher.group(1).trim(),
                            "description", matcher.group(2).trim()
                    ));
                } else {
                    metadataList.add(Map.of(
                            "skillId", skillId,
                            "name", "未知",
                            "description", "元数据解析失败"
                    ));
                }
            });

            return formatToJsonString(metadataList);

        } catch (Exception e) {
            return "{\"error\":\"读取技能元数据失败：" + e.getMessage() + "\"}";
        }
    }

    // ====================== 工具2：获取指定技能的流程描述 ======================
    @Tool("获取指定skillId对应的SKILL.md中的流程描述，参数：skillId（技能文件夹名）")
    public String getSkillFlowDescription(String skillId) {
        if (skillId == null || skillId.isEmpty()) {
            return "{\"error\":\"skillId不能为空\"}";
        }

        String skillMdPath = SKILLS_ROOT + skillId + "/SKILL.md";
        String mdContent = readFileFromResource(skillMdPath);
        if (mdContent == null) {
            return "{\"error\":\"技能不存在：" + skillId + "\"}";
        }

        Matcher matcher = FLOW_PATTERN.matcher(mdContent);
        if (matcher.find()) {
            return String.format(
                    "{\"skillId\":\"%s\",\"flowDescription\":\"%s\"}",
                    skillId,
                    escapeJson(matcher.group(1).trim())
            );
        } else {
            return "{\"skillId\":\"" + skillId + "\",\"error\":\"流程描述解析失败\"}";
        }
    }

    // ====================== 工具3：调用外部Python/Shell脚本（核心） ======================
    @Tool("调用外部Python/Shell脚本执行指定skillId的逻辑，参数：skillId（技能文件夹名），返回线程信息、脚本输出、执行状态")
    public String executeSkillScript(String skillId) {
        // 1. 参数校验
        if (skillId == null || skillId.isEmpty()) {
            return "{\"error\":\"skillId不能为空\"}";
        }

        // 2. 读取并解析SKILL.md中的脚本配置
        String skillMdPath = SKILLS_ROOT + skillId + "/SKILL.md";
        String mdContent = readFileFromResource(skillMdPath);
        if (mdContent == null) {
            return "{\"error\":\"技能不存在：" + skillId + "\"}";
        }

        Matcher scriptMatcher = SCRIPT_PATTERN.matcher(mdContent);
        if (!scriptMatcher.find()) {
            return "{\"skillId\":\"" + skillId + "\",\"error\":\"脚本配置解析失败\"}";
        }

        try {
            // 解析脚本配置
            String scriptPathRel = scriptMatcher.group(1).trim(); // 相对路径
            String scriptType = scriptMatcher.group(2).trim().toLowerCase();
            String paramsJson = scriptMatcher.group(3).trim();
            int timeout = scriptMatcher.group(5) != null ? Integer.parseInt(scriptMatcher.group(5).trim()) : 5;

            // 转换参数JSON为列表
            List<String> scriptParams = OBJECT_MAPPER.readValue(paramsJson, new TypeReference<List<String>>() {});

            // 3. 构建脚本绝对路径（安全校验：仅允许skills目录内的脚本）
            String scriptAbsolutePath = getScriptAbsolutePath(skillId, scriptPathRel);
            if (scriptAbsolutePath == null) {
                return "{\"skillId\":\"" + skillId + "\",\"error\":\"脚本路径非法，仅允许访问skills目录内的脚本\"}";
            }

            // 4. 封装外部脚本执行任务
            Callable<Map<String, Object>> scriptTask = () -> {
                ExternalScriptExecutor executor = new ExternalScriptExecutor();
                return executor.execute(scriptType, scriptAbsolutePath, scriptParams, timeout);
            };

            // 5. 提交任务到线程池执行
            long startTime = System.currentTimeMillis();
            Future<Map<String, Object>> future = SCRIPT_EXECUTOR.submit(scriptTask);
            Map<String, Object> result = future.get(timeout + 2, TimeUnit.SECONDS); // 预留2秒缓冲

            // 6. 组装返回结果
            result.put("skillId", skillId);
            result.put("threadName", Thread.currentThread().getName());
            result.put("executeCostMs", System.currentTimeMillis() - startTime);
            return OBJECT_MAPPER.writeValueAsString(result);

        } catch (TimeoutException e) {
            return "{\"skillId\":\"" + skillId + "\",\"error\":\"脚本执行超时\",\"timeoutSeconds\":\"5\"}";
        } catch (Exception e) {
            return "{\"skillId\":\"" + skillId + "\",\"error\":\"脚本执行失败：" + e.getMessage() + "\"}";
        }
    }

    // ====================== 工具4：读取指定技能的reference引用文件 ======================
    @Tool("读取指定skillId对应的SKILL.md中的reference引用文件细节，参数：skillId（技能文件夹名）")
    public String getSkillReferenceFiles(String skillId) {
        if (skillId == null || skillId.isEmpty()) {
            return "{\"error\":\"skillId不能为空\"}";
        }

        String skillMdPath = SKILLS_ROOT + skillId + "/SKILL.md";
        String mdContent = readFileFromResource(skillMdPath);
        if (mdContent == null) {
            return "{\"error\":\"技能不存在：" + skillId + "\"}";
        }

        Matcher matcher = REFERENCE_PATTERN.matcher(mdContent);
        if (!matcher.find()) {
            return "{\"skillId\":\"" + skillId + "\",\"error\":\"引用文件解析失败\"}";
        }

        String referenceContent = matcher.group(1).trim();
        List<Map<String, String>> referenceFiles = new ArrayList<>();
        String[] lines = referenceContent.split("\\n");
        for (String line : lines) {
            if (line.trim().isEmpty() || !line.contains("：")) {
                continue;
            }

            String[] parts = line.split("：");
            if (parts.length < 2) {
                continue;
            }

            String fileName = parts[0].trim().replace("- ", "");
            String filePathRel = parts[1].trim();
            String filePathAbs = SKILLS_ROOT + skillId + "/" + filePathRel;
            String fileContent = readFileFromResource(filePathAbs);

            referenceFiles.add(Map.of(
                    "fileName", fileName,
                    "filePath", filePathAbs,
                    "content", fileContent == null ? "文件不存在" : escapeJson(fileContent)
            ));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("skillId", skillId);
        result.put("referenceFiles", referenceFiles);
        try {
            return OBJECT_MAPPER.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"skillId\":\"" + skillId + "\",\"error\":\"引用文件结果序列化失败：" + e.getMessage() + "\"}";
        }
    }

    // ========== 辅助方法 ==========
    /**
     * 读取classpath下的文件内容
     */
    private String readFileFromResource(String filePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8))) {

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();

        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    /**
     * 构建脚本绝对路径并做安全校验（仅允许skills目录内）
     */
    private String getScriptAbsolutePath(String skillId, String scriptPathRel) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String skillRoot = SKILLS_ROOT + skillId + "/";
            String fullPathRel = skillRoot + scriptPathRel.replace("./", "");
            Path scriptPath = Paths.get(Objects.requireNonNull(classLoader.getResource(fullPathRel)).toURI());

            // 安全校验：脚本路径必须在skills目录下
            Path skillsRootPath = Paths.get(Objects.requireNonNull(classLoader.getResource(SKILLS_ROOT)).toURI());
            if (!scriptPath.toAbsolutePath().startsWith(skillsRootPath.toAbsolutePath())) {
                return null;
            }
            return scriptPath.toAbsolutePath().toString();

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON转义（处理换行、双引号）
     */
    private String escapeJson(String content) {
        return content.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * 格式化列表为JSON字符串
     */
    private String formatToJsonString(List<Map<String, String>> list) {
        try {
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            return list.toString().replace("=", ":").replace("{", "{\"").replace("}", "\"}").replace(", ", "\", \"");
        }
    }

    /**
     * 关闭线程池（应用退出时调用）
     */
    public void shutdownExecutor() {
        SCRIPT_EXECUTOR.shutdown();
        try {
            if (!SCRIPT_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                SCRIPT_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            SCRIPT_EXECUTOR.shutdownNow();
        }
    }
}
