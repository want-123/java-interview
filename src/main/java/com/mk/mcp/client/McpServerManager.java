package com.mk.mcp.client;

import java.util.Map;

/**
 * MCP Server管理器
 * 用于管理与MCP Server的连接
 */
public interface McpServerManager {
    /**
     * 初始化MCP Server连接
     */
    void init();
    
    /**
     * 获取MCP Server连接
     * @param serverId MCP Server ID
     * @return MCP Server连接
     */
    Object getConnection(String serverId);
    
    /**
     * 关闭MCP Server连接
     * @param serverId MCP Server ID
     */
    void closeConnection(String serverId);
    
    /**
     * 关闭所有MCP Server连接
     */
    void closeAllConnections();
    
    /**
     * 更新MCP Server配置
     */
    void updateServers();
    
    /**
     * 获取所有可用的MCP Server
     * @return 所有可用的MCP Server
     */
    Map<String, Object> getAvailableServers();
    
    /**
     * 检查MCP Server健康状态
     * @param serverId MCP Server ID
     * @return 健康状态
     */
    boolean checkHealth(String serverId);
}
