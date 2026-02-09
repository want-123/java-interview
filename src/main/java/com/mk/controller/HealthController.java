package com.mk.controller;

import com.mk.common.Result;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于监控系统各服务的状态
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "健康检查")
public class HealthController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private MinioClient minioClient;
    
    /**
     * 系统健康状态检查
     */
    @Operation(summary = "系统健康状态检查")
    @GetMapping("/status")
    public Result<?> healthStatus() {
        Map<String, Object> statusMap = new HashMap<>();
        
        // 检查数据库连接
        statusMap.put("database", checkDatabase());
        
        // 检查Redis连接
        statusMap.put("redis", checkRedis());
        
        // 检查MinIO连接
        statusMap.put("minio", checkMinIO());
        
        // 检查系统状态
        statusMap.put("system", checkSystem());
        
        // 检查整体状态
        boolean allHealthy = statusMap.values().stream()
                .allMatch(status -> status instanceof Map && ((Map<?, ?>) status).get("status").equals("UP"));
        
        if (allHealthy) {
            return Result.success(statusMap);
        } else {
            return Result.error(503, "部分服务不可用", statusMap);
        }
    }
    
    /**
     * 检查数据库连接
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbStatus = new HashMap<>();
        try {
            jdbcTemplate.execute("SELECT 1");
            dbStatus.put("status", "UP");
            dbStatus.put("message", "数据库连接正常");
        } catch (Exception e) {
            dbStatus.put("status", "DOWN");
            dbStatus.put("message", "数据库连接异常: " + e.getMessage());
        }
        return dbStatus;
    }
    
    /**
     * 检查Redis连接
     */
    private Map<String, Object> checkRedis() {
        Map<String, Object> redisStatus = new HashMap<>();
        try {
            redisTemplate.opsForValue().set("health_check", "ok", 10);
            String value = redisTemplate.opsForValue().get("health_check");
            if ("ok".equals(value)) {
                redisStatus.put("status", "UP");
                redisStatus.put("message", "Redis连接正常");
            } else {
                redisStatus.put("status", "DOWN");
                redisStatus.put("message", "Redis连接异常: 无法获取值");
            }
        } catch (Exception e) {
            redisStatus.put("status", "DOWN");
            redisStatus.put("message", "Redis连接异常: " + e.getMessage());
        }
        return redisStatus;
    }
    
    /**
     * 检查MinIO连接
     */
    private Map<String, Object> checkMinIO() {
        Map<String, Object> minioStatus = new HashMap<>();
        try {
            minioClient.listBuckets();
            minioStatus.put("status", "UP");
            minioStatus.put("message", "MinIO连接正常");
        } catch (Exception e) {
            minioStatus.put("status", "DOWN");
            minioStatus.put("message", "MinIO连接异常: " + e.getMessage());
        }
        return minioStatus;
    }
    
    /**
     * 检查系统状态
     */
    private Map<String, Object> checkSystem() {
        Map<String, Object> systemStatus = new HashMap<>();
        try {
            // 检查系统内存
            Runtime runtime = Runtime.getRuntime();
            long freeMemory = runtime.freeMemory() / 1024 / 1024;
            long totalMemory = runtime.totalMemory() / 1024 / 1024;
            long maxMemory = runtime.maxMemory() / 1024 / 1024;
            
            systemStatus.put("status", "UP");
            systemStatus.put("message", "系统运行正常");
            systemStatus.put("freeMemory", freeMemory + "MB");
            systemStatus.put("totalMemory", totalMemory + "MB");
            systemStatus.put("maxMemory", maxMemory + "MB");
            systemStatus.put("cpuCores", runtime.availableProcessors());
        } catch (Exception e) {
            systemStatus.put("status", "DOWN");
            systemStatus.put("message", "系统状态检查异常: " + e.getMessage());
        }
        return systemStatus;
    }
}
