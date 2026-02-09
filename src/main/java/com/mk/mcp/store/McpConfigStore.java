package com.mk.mcp.store;

import java.util.Map;

/**
 * MCP配置存储接口
 * 用于持久化存储MCP Server配置信息
 */
public interface McpConfigStore {
    /**
     * 保存MCP Server配置
     * @param serverId MCP Server ID
     * @param serverConfig MCP Server配置信息
     */
    void saveConfig(String serverId, Map<String, Object> serverConfig);
    
    /**
     * 获取MCP Server配置
     * @param serverId MCP Server ID
     * @return MCP Server配置信息
     */
    Map<String, Object> getConfig(String serverId);
    
    /**
     * 删除MCP Server配置
     * @param serverId MCP Server ID
     */
    void deleteConfig(String serverId);
    
    /**
     * 获取所有MCP Server配置
     * @return 所有MCP Server配置信息
     */
    Map<String, Map<String, Object>> getAllConfigs();
    
    /**
     * 清空所有MCP Server配置
     */
    void clearAllConfigs();
}
