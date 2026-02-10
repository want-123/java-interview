package com.mk.util;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2026/02/10/13:52
 * @Description:
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 外部脚本（Python/Shell）执行器
 */
public class ExternalScriptExecutor {
    /**
     * 执行外部脚本
     * @param scriptType 脚本类型：python/shell/bash
     * @param scriptPath 脚本绝对路径
     * @param params 脚本参数列表
     * @param timeout 超时时间（秒）
     * @return 执行结果（输出、错误、退出码、状态）
     */
    public Map<String, Object> execute(String scriptType, String scriptPath, List<String> params, int timeout) throws IOException, InterruptedException {
        Map<String, Object> result = new HashMap<>();
        List<String> command = new ArrayList<>();

        // 1. 构建执行命令
        switch (scriptType) {
            case "python":
                command.add("python3"); // 或python（根据系统配置）
                break;
            case "shell":
            case "bash":
                command.add("/bin/bash");
                break;
            default:
                result.put("status", "failed");
                result.put("error", "不支持的脚本类型：" + scriptType);
                return result;
        }
        command.add(scriptPath);
        command.addAll(params);

        // 2. 启动进程
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // 合并标准输出和错误输出
        pb.environment().put("PYTHONIOENCODING", "utf-8"); // 解决Python中文乱码
        Process process = pb.start();

        // 3. 异步读取输出（避免进程阻塞）
        StringBuilder output = new StringBuilder();
        Thread outputReader = new Thread(() -> {
            try (InputStream is = process.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                output.append("读取脚本输出失败：").append(e.getMessage());
            }
        });
        outputReader.start();

        // 4. 等待进程结束（超时控制）
        boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
        outputReader.join(1000); // 等待输出读取线程结束

        // 5. 处理执行结果
        if (!finished) {
            process.destroy(); // 超时销毁进程
            result.put("status", "timeout");
            result.put("error", "脚本执行超时（" + timeout + "秒）");
        } else {
            int exitCode = process.exitValue();
            result.put("status", exitCode == 0 ? "success" : "failed");
            result.put("exitCode", exitCode);
            if (exitCode != 0) {
                result.put("error", "脚本执行失败，退出码：" + exitCode);
            }
        }
        result.put("output", output.toString().trim());
        result.put("command", String.join(" ", command));
        return result;
    }
}
