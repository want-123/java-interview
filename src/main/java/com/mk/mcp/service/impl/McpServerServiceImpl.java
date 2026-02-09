package com.mk.mcp.service.impl;

import com.mk.mcp.service.McpServerService;
import com.mk.mcp.store.McpConfigStore;
import com.mk.mcp.store.RedisMcpConfigStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP服务实现类
 * 用于管理MCP Server的配置信息
 */
@Service
public class McpServerServiceImpl implements McpServerService {
    
    // 存储MCP Server配置信息（内存缓存）
    private final Map<String, Map<String, Object>> mcpServers = new ConcurrentHashMap<>();
    
    @Autowired
    private McpConfigStore mcpConfigStore;
    
    @Override
    public ResponseEntity<Map<String, Object>> addMcpServer(Map<String, Object> serverConfig) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 解析配置信息
            if (serverConfig.containsKey("mcpServers")) {
                Map<String, Object> servers = (Map<String, Object>) serverConfig.get("mcpServers");
                
                // 遍历添加所有MCP Server
                for (Map.Entry<String, Object> entry : servers.entrySet()) {
                    String serverId = entry.getKey();
                    Map<String, Object> serverInfo = (Map<String, Object>) entry.getValue();
                    
                    // 保存到内存缓存
                    mcpServers.put(serverId, serverInfo);
                    // 保存到Redis持久化存储
                    mcpConfigStore.saveConfig(serverId, serverInfo);
                    
                    System.out.println("Added MCP Server: " + serverId + " - " + serverInfo);
                }
                
                response.put("code", 200);
                response.put("message", "MCP Server added successfully");
                response.put("data", mcpServers);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "Invalid config format: missing mcpServers field");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Failed to add MCP Server: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Override
    public ResponseEntity<Map<String, Object>> removeMcpServer(String serverId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (mcpServers.containsKey(serverId)) {
                // 从内存缓存中移除
                mcpServers.remove(serverId);
                // 从Redis持久化存储中移除
                mcpConfigStore.deleteConfig(serverId);
                
                System.out.println("Removed MCP Server: " + serverId);
                
                response.put("code", 200);
                response.put("message", "MCP Server removed successfully");
                response.put("data", mcpServers);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 404);
                response.put("message", "MCP Server not found: " + serverId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Failed to remove MCP Server: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Override
    public ResponseEntity<Map<String, Object>> getAllMcpServers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 从Redis加载最新配置
            Map<String, Map<String, Object>> allConfigs = mcpConfigStore.getAllConfigs();
            // 更新内存缓存
            mcpServers.clear();
            mcpServers.putAll(allConfigs);
            
            Map<String, Object> data = new HashMap<>();
            data.put("mcpServers", mcpServers);
            
            response.put("code", 200);
            response.put("message", "Get all MCP Servers successfully");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Failed to get all MCP Servers: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Override
    public ResponseEntity<Map<String, Object>> getMcpServer(String serverId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 先从内存缓存获取
            Map<String, Object> serverInfo = mcpServers.get(serverId);
            
            // 如果内存缓存中没有，从Redis加载
            if (serverInfo == null) {
                serverInfo = mcpConfigStore.getConfig(serverId);
                if (serverInfo != null) {
                    mcpServers.put(serverId, serverInfo);
                }
            }
            
            if (serverInfo != null) {
                Map<String, Object> data = new HashMap<>();
                data.put(serverId, serverInfo);
                
                response.put("code", 200);
                response.put("message", "Get MCP Server successfully");
                response.put("data", data);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 404);
                response.put("message", "MCP Server not found: " + serverId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Failed to get MCP Server: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 初始化方法
     * 在服务启动时从Redis加载配置信息到内存缓存中
     */
    @PostConstruct
    public void init() {
        try {
            // 从Redis加载所有配置
            Map<String, Map<String, Object>> allConfigs = mcpConfigStore.getAllConfigs();
            if (!allConfigs.isEmpty()) {
                mcpServers.putAll(allConfigs);
                System.out.println("Loaded " + allConfigs.size() + " MCP Server configs from Redis");
            }
        } catch (Exception e) {
            System.err.println("Failed to load MCP Server configs from Redis: " + e.getMessage());
        }
    }
}
