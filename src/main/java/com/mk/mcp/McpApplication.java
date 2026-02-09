package com.mk.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * MCP服务启动类
 * 用于启动MCP服务，管理MCP Server的配置信息
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.mk"})
public class McpApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(McpApplication.class, args);
    }
}
