package com.mk.mcp.controller;

import com.mk.mcp.service.McpServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MCP服务控制器
 * 用于处理MCP Server的HTTP请求
 */
@RestController
@RequestMapping("/api/mcp")
public class McpServerController {
    
    @Autowired
    private McpServerService mcpServerService;
    
    /**
     * 添加MCP Server
     * @param serverConfig MCP Server配置信息
     * @return 响应结果
     */
    @PostMapping("/servers")
    public ResponseEntity<Map<String, Object>> addMcpServer(@RequestBody Map<String, Object> serverConfig) {
        return mcpServerService.addMcpServer(serverConfig);
    }
    
    /**
     * 移除MCP Server
     * @param serverId MCP Server ID
     * @return 响应结果
     */
    @DeleteMapping("/servers/{serverId}")
    public ResponseEntity<Map<String, Object>> removeMcpServer(@PathVariable String serverId) {
        return mcpServerService.removeMcpServer(serverId);
    }
    
    /**
     * 获取所有MCP Server配置
     * @return 响应结果
     */
    @GetMapping("/servers")
    public ResponseEntity<Map<String, Object>> getAllMcpServers() {
        return mcpServerService.getAllMcpServers();
    }
    
    /**
     * 获取指定MCP Server配置
     * @param serverId MCP Server ID
     * @return 响应结果
     */
    @GetMapping("/servers/{serverId}")
    public ResponseEntity<Map<String, Object>> getMcpServer(@PathVariable String serverId) {
        return mcpServerService.getMcpServer(serverId);
    }
}
