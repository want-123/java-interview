package com.mk.mcp.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;


/**
 * MCP服务接口
 * 用于管理MCP Server的配置信息
 */
public interface McpServerService {
    /**
     * 添加MCP Server
     * @param serverConfig MCP Server配置信息
     * @return 响应结果
     */
    ResponseEntity<Map<String, Object>> addMcpServer(Map<String, Object> serverConfig);
    
    /**
     * 移除MCP Server
     * @param serverId MCP Server ID
     * @return 响应结果
     */
    ResponseEntity<Map<String, Object>> removeMcpServer(String serverId);
    
    /**
     * 获取所有MCP Server配置
     * @return 响应结果
     */
    ResponseEntity<Map<String, Object>> getAllMcpServers();
    
    /**
     * 获取指定MCP Server配置
     * @param serverId MCP Server ID
     * @return 响应结果
     */
    ResponseEntity<Map<String, Object>> getMcpServer(String serverId);
}
