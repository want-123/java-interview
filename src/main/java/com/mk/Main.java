package com.mk;

import com.mk.mcp.client.McpServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 应用程序主类
 * 用于启动整个应用，包括MCP服务
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.mk"})
@EnableScheduling
public class Main implements CommandLineRunner {
    
    @Autowired
    private McpServerManager mcpServerManager;
    
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 初始化MCP Server管理器
        System.out.println("Initializing MCP Server Manager...");
        mcpServerManager.init();
        System.out.println("MCP Server Manager initialized successfully!");
    }
    
    /**
     * 定期更新MCP Server配置
     */
    @Scheduled(fixedRateString = "${mcp.config-update.interval:60000}")
    public void scheduledUpdateMcpServers() {
        try {
            System.out.println("Updating MCP Server configurations...");
            mcpServerManager.updateServers();
            System.out.println("MCP Server configurations updated successfully!");
        } catch (Exception e) {
            System.err.println("Failed to update MCP Server configurations: " + e.getMessage());
        }
    }
}
