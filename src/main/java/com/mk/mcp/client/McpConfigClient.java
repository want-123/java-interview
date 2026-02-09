package com.mk.mcp.client;

import java.util.Map;

/**
 * MCP配置客户端
 * 用于与MCP服务端通信，获取MCP Server的配置信息
 */
public interface McpConfigClient {
    /**
     * 获取所有MCP Server配置
     * @return 所有MCP Server配置信息
     */
    Map<String, Object> getAllMcpServers();
    
    /**
     * 获取指定MCP Server配置
     * @param serverId MCP Server ID
     * @return MCP Server配置信息
     */
    Map<String, Object> getMcpServer(String serverId);
    
    /**
     * 添加MCP Server配置
     * @param serverConfig MCP Server配置信息
     * @return 响应结果
     */
    Map<String, Object> addMcpServer(Map<String, Object> serverConfig);
    
    /**
     * 移除MCP Server配置
     * @param serverId MCP Server ID
     * @return 响应结果
     */
    Map<String, Object> removeMcpServer(String serverId);
}
