package com.mk.mcp.client.impl;

import com.mk.mcp.client.McpConfigClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于RestTemplate的MCP配置客户端实现
 * 用于与MCP服务端通信，获取MCP Server的配置信息
 */
@Component
public class RestMcpConfigClient implements McpConfigClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${mcp.service.url:http://localhost:8080/api/mcp}")
    private String mcpServiceUrl;
    
    @Override
    public Map<String, Object> getAllMcpServers() {
        try {
            String url = mcpServiceUrl + "/servers";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Failed to get all MCP Servers: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "Failed to get all MCP Servers: " + e.getMessage());
            return errorResponse;
        }
    }
    
    @Override
    public Map<String, Object> getMcpServer(String serverId) {
        try {
            String url = mcpServiceUrl + "/servers/" + serverId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Failed to get MCP Server: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "Failed to get MCP Server: " + e.getMessage());
            return errorResponse;
        }
    }
    
    @Override
    public Map<String, Object> addMcpServer(Map<String, Object> serverConfig) {
        try {
            String url = mcpServiceUrl + "/servers";
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(serverConfig);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Failed to add MCP Server: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "Failed to add MCP Server: " + e.getMessage());
            return errorResponse;
        }
    }
    
    @Override
    public Map<String, Object> removeMcpServer(String serverId) {
        try {
            String url = mcpServiceUrl + "/servers/" + serverId;
            HttpEntity<Void> requestEntity = new HttpEntity<>(null);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Failed to remove MCP Server: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "Failed to remove MCP Server: " + e.getMessage());
            return errorResponse;
        }
    }
}
