package com.mk.mcp;

import com.mk.mcp.client.McpServerManager;
import com.mk.mcp.service.McpServerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MCP服务测试类
 * 用于测试MCP服务的功能
 */
@SpringBootTest
public class McpServiceTest {
    
    @Autowired
    private McpServerService mcpServerService;
    
    @Autowired
    private McpServerManager mcpServerManager;
    
    private String testServerId = "test-server-001";
    
    @BeforeEach
    public void setUp() {
        // 初始化测试环境
        System.out.println("Setting up MCP service test...");
    }
    
    /**
     * 测试添加MCP Server
     */
    @Test
    public void testAddMcpServer() {
        // 创建测试配置
        Map<String, Object> serverConfig = new HashMap<>();
        Map<String, Object> mcpServers = new HashMap<>();
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("type", "sse");
        serverInfo.put("url", "https://test.example.com/mcp/sse");
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer test-token");
        serverInfo.put("headers", headers);
        mcpServers.put(testServerId, serverInfo);
        serverConfig.put("mcpServers", mcpServers);
        
        // 添加MCP Server
        ResponseEntity<Map<String, Object>> response = mcpServerService.addMcpServer(serverConfig);
        
        // 验证响应
        assertNotNull(response);
        assertEquals(200, response.getBody().get("code"));
        assertEquals("MCP Server added successfully", response.getBody().get("message"));
    }
    
    /**
     * 测试获取所有MCP Server
     */
    @Test
    public void testGetAllMcpServers() {
        // 获取所有MCP Server
        ResponseEntity<Map<String, Object>> response = mcpServerService.getAllMcpServers();
        
        // 验证响应
        assertNotNull(response);
        assertEquals(200, response.getBody().get("code"));
        assertEquals("Get all MCP Servers successfully", response.getBody().get("message"));
        assertNotNull(response.getBody().get("data"));
    }
    
    /**
     * 测试获取指定MCP Server
     */
    @Test
    public void testGetMcpServer() {
        // 获取指定MCP Server
        ResponseEntity<Map<String, Object>> response = mcpServerService.getMcpServer(testServerId);
        
        // 验证响应
        assertNotNull(response);
        assertEquals(200, response.getBody().get("code"));
        assertEquals("Get MCP Server successfully", response.getBody().get("message"));
        assertNotNull(response.getBody().get("data"));
    }
    
    /**
     * 测试移除MCP Server
     */
    @Test
    public void testRemoveMcpServer() {
        // 移除MCP Server
        ResponseEntity<Map<String, Object>> response = mcpServerService.removeMcpServer(testServerId);
        
        // 验证响应
        assertNotNull(response);
        assertEquals(200, response.getBody().get("code"));
        assertEquals("MCP Server removed successfully", response.getBody().get("message"));
    }
    
    /**
     * 测试MCP Server管理器初始化
     */
    @Test
    public void testMcpServerManagerInit() {
        // 初始化MCP Server管理器
        mcpServerManager.init();
        
        // 验证初始化是否成功
        Map<String, Object> availableServers = mcpServerManager.getAvailableServers();
        assertNotNull(availableServers);
    }
    
    /**
     * 测试MCP Server健康检查
     */
    @Test
    public void testMcpServerHealthCheck() {
        // 首先添加测试服务器
        Map<String, Object> serverConfig = new HashMap<>();
        Map<String, Object> mcpServers = new HashMap<>();
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("type", "sse");
        serverInfo.put("url", "https://www.baidu.com"); // 使用百度作为测试URL
        mcpServers.put(testServerId, serverInfo);
        serverConfig.put("mcpServers", mcpServers);
        mcpServerService.addMcpServer(serverConfig);
        
        // 更新服务器配置
        mcpServerManager.updateServers();
        
        // 检查健康状态
        boolean isHealthy = mcpServerManager.checkHealth(testServerId);
        System.out.println("Health check result for " + testServerId + ": " + isHealthy);
        
        // 移除测试服务器
        mcpServerService.removeMcpServer(testServerId);
    }
}
