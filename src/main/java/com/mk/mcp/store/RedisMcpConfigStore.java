package com.mk.mcp.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的MCP配置存储实现
 * 用于持久化存储MCP Server配置信息
 */
@Component
public class RedisMcpConfigStore implements McpConfigStore {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String MCP_CONFIG_PREFIX = "mcp:server:";
    private static final String MCP_ALL_SERVERS_KEY = "mcp:all_servers";
    private static final long CONFIG_EXPIRATION = 7 * 24 * 60 * 60; // 7天过期时间
    
    @Override
    public void saveConfig(String serverId, Map<String, Object> serverConfig) {
        // 保存单个MCP Server配置
        String configKey = MCP_CONFIG_PREFIX + serverId;
        redisTemplate.opsForHash().putAll(configKey, serverConfig);
        redisTemplate.expire(configKey, CONFIG_EXPIRATION, TimeUnit.SECONDS);
        
        // 更新所有MCP Server列表
        redisTemplate.opsForSet().add(MCP_ALL_SERVERS_KEY, serverId);
        redisTemplate.expire(MCP_ALL_SERVERS_KEY, CONFIG_EXPIRATION, TimeUnit.SECONDS);
        
        System.out.println("Saved MCP Server config to Redis: " + serverId);
    }
    
    @Override
    public Map<String, Object> getConfig(String serverId) {
        String configKey = MCP_CONFIG_PREFIX + serverId;
        Map<Object, Object> hashEntries = redisTemplate.opsForHash().entries(configKey);
        
        // 转换为Map<String, Object>
        Map<String, Object> config = new HashMap<>();
        for (Map.Entry<Object, Object> entry : hashEntries.entrySet()) {
            config.put(entry.getKey().toString(), entry.getValue());
        }
        
        return config.isEmpty() ? null : config;
    }
    
    @Override
    public void deleteConfig(String serverId) {
        // 删除单个MCP Server配置
        String configKey = MCP_CONFIG_PREFIX + serverId;
        redisTemplate.delete(configKey);
        
        // 从所有MCP Server列表中移除
        redisTemplate.opsForSet().remove(MCP_ALL_SERVERS_KEY, serverId);
        
        System.out.println("Deleted MCP Server config from Redis: " + serverId);
    }
    
    @Override
    public Map<String, Map<String, Object>> getAllConfigs() {
        Map<String, Map<String, Object>> allConfigs = new HashMap<>();
        
        // 获取所有MCP Server ID
        Set<Object> serverIds = redisTemplate.opsForSet().members(MCP_ALL_SERVERS_KEY);
        
        if (serverIds != null) {
            for (Object serverIdObj : serverIds) {
                String serverId = serverIdObj.toString();
                Map<String, Object> config = getConfig(serverId);
                if (config != null) {
                    allConfigs.put(serverId, config);
                }
            }
        }
        
        return allConfigs;
    }
    
    @Override
    public void clearAllConfigs() {
        // 获取所有MCP Server ID
        Set<Object> serverIds = redisTemplate.opsForSet().members(MCP_ALL_SERVERS_KEY);
        
        if (serverIds != null) {
            for (Object serverIdObj : serverIds) {
                String serverId = serverIdObj.toString();
                String configKey = MCP_CONFIG_PREFIX + serverId;
                redisTemplate.delete(configKey);
            }
        }
        
        // 删除所有MCP Server列表
        redisTemplate.delete(MCP_ALL_SERVERS_KEY);
        
        System.out.println("Cleared all MCP Server configs from Redis");
    }
}
