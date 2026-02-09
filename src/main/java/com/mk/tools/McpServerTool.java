package com.mk.tools;

import com.mk.mcp.client.McpServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP Server工具类
 * 用于实现JavaAgent接口中定义的MCP相关方法
 */
@Component
public class McpServerTool {
    
    @Autowired
    private McpServerManager mcpServerManager;
    
    /**
     * 列出所有可用的MCP Server
     * @return 所有可用的MCP Server列表
     */
    public String listMcpServers() {
        try {
            Map<String, Object> availableServers = mcpServerManager.getAvailableServers();
            if (availableServers.isEmpty()) {
                return "No MCP Servers available.";
            }
            
            StringBuilder result = new StringBuilder("Available MCP Servers:\n");
            for (Map.Entry<String, Object> entry : availableServers.entrySet()) {
                String serverId = entry.getKey();
                Map<String, Object> serverConfig = (Map<String, Object>) entry.getValue();
                
                result.append("- Server ID: ").append(serverId).append("\n");
                if (serverConfig.containsKey("type")) {
                    result.append("  Type: ").append(serverConfig.get("type")).append("\n");
                }
                if (serverConfig.containsKey("url")) {
                    result.append("  URL: ").append(serverConfig.get("url")).append("\n");
                }
                result.append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "Failed to list MCP Servers: " + e.getMessage();
        }
    }
    
    /**
     * 获取指定MCP Server的配置
     * @param serverId MCP Server ID
     * @return MCP Server配置信息
     */
    public String getMcpServer(String serverId) {
        try {
            Map<String, Object> availableServers = mcpServerManager.getAvailableServers();
            if (!availableServers.containsKey(serverId)) {
                return "MCP Server not found: " + serverId;
            }
            
            Map<String, Object> serverConfig = (Map<String, Object>) availableServers.get(serverId);
            StringBuilder result = new StringBuilder("MCP Server Configuration:\n");
            result.append("Server ID: " + serverId + "\n");
            
            for (Map.Entry<String, Object> entry : serverConfig.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                result.append(key).append(": ").append(value).append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "Failed to get MCP Server: " + e.getMessage();
        }
    }
    
    /**
     * 检查MCP Server的健康状态
     * @param serverId MCP Server ID
     * @return 健康状态信息
     */
    public String checkMcpServerHealth(String serverId) {
        try {
            boolean isHealthy = mcpServerManager.checkHealth(serverId);
            if (isHealthy) {
                return "MCP Server " + serverId + " is healthy.";
            } else {
                return "MCP Server " + serverId + " is not healthy.";
            }
        } catch (Exception e) {
            return "Failed to check MCP Server health: " + e.getMessage();
        }
    }
    
    /**
     * 更新MCP Server配置
     * @return 更新结果
     */
    public String updateMcpServers() {
        try {
            mcpServerManager.updateServers();
            return "MCP Servers updated successfully.";
        } catch (Exception e) {
            return "Failed to update MCP Servers: " + e.getMessage();
        }
    }
}
