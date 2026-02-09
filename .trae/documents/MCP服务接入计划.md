# MCP服务接入计划（基于用户提供的配置格式）

## 架构设计方案

### 推荐方案：Agent通过MCP服务获取元数据后直接与MCP Server建立连接

**核心思路**：
1. MCP服务作为配置中心，管理MCP Server的配置信息（采用用户提供的JSON格式）
2. Agent服务从MCP服务获取MCP Server的元数据信息
3. Agent服务根据元数据直接与MCP Server建立client连接
4. 支持按照用户提供的JSON格式动态添加MCP Server

**优势**：
- 符合用户提供的配置格式和使用场景
- 降低延迟，提高通信效率
- 提高可靠性，避免单点故障
- 支持灵活的MCP Server管理
- 符合微服务架构最佳实践

## 实现步骤

### 1. 设计MCP服务核心组件

#### MCP服务接口
```java
public interface McpServerService {
    // 注册MCP Server（使用用户提供的JSON格式）
    ResponseEntity<Map<String, Object>> addMcpServer(@RequestBody Map<String, Object> serverConfig);
    
    // 移除MCP Server
    ResponseEntity<Map<String, Object>> removeMcpServer(@PathVariable String serverId);
    
    // 获取所有MCP Server配置
    ResponseEntity<Map<String, Object>> getAllMcpServers();
    
    // 获取指定MCP Server配置
    ResponseEntity<Map<String, Object>> getMcpServer(@PathVariable String serverId);
}
```

#### 支持的配置格式（基于用户示例）

**注册请求格式**：
```json
{
  "mcpServers": {
    "qwenimage": {
      "type": "sse",
      "url": "https://dashscope.aliyuncs.com/api/v1/mcps/amap-maps/sse",
      "headers": {
        "Authorization": "Bearer sk-1f7bfcabb7874aa48813eddef5b3044c"
      }
    }
  }
}
```

### 2. 实现MCP服务

#### 核心功能
- 支持用户提供的JSON格式配置
- 动态添加和移除MCP Server
- 配置持久化存储
- 配置验证和管理

#### 技术选型
- Spring Boot 3.0+ 构建MCP服务
- Redis存储MCP Server配置
- Spring Web实现RESTful API
- 集成Spring Validation实现请求验证

### 3. 实现Agent服务连接管理

#### 核心组件
- `McpServerManager`：管理与MCP Server的连接
- `McpConfigClient`：与MCP服务交互的客户端
- `McpConnectionFactory`：根据配置创建MCP Server连接
- `McpConnectionPool`：管理MCP Server连接池

#### 工作流程
1. Agent启动时，从MCP服务获取MCP Server配置
2. 根据配置创建对应的连接（如SSE连接）
3. 维护连接状态和健康检查
4. 当配置更新时，自动更新连接
5. 当连接失败时，自动重连或切换到备用MCP Server

### 4. 配置与部署

#### 配置项
- MCP服务地址和端口
- 配置更新间隔
- 连接超时和重试策略
- 健康检查间隔

#### 部署建议
- MCP服务可部署为单实例或集群
- 使用Redis存储配置信息
- 为MCP服务配置健康检查
- 支持Docker容器化部署

## 代码实现计划

1. **创建MCP服务模块**
   - 实现`McpServerService`接口
   - 实现符合用户提供格式的请求处理
   - 实现配置存储和管理
   - 实现配置验证和健康检查

2. **创建MCP客户端模块**
   - 实现`McpConfigClient`
   - 实现`McpServerManager`
   - 实现`McpConnectionFactory`
   - 实现`McpConnectionPool`

3. **集成到Agent服务**
   - 修改`JavaAgent`接口，添加MCP相关功能
   - 实现Agent与MCP Server的通信逻辑
   - 添加配置项和启动参数
   - 实现配置更新和连接管理

4. **测试与验证**
   - 测试动态添加和移除MCP Server
   - 测试连接管理和故障转移
   - 测试与用户提供格式的兼容性
   - 验证性能和可靠性

## 预期效果

- 支持按照用户提供的JSON格式动态添加MCP Server
- Agent能够自动获取MCP Server配置并建立连接
- 提供可靠的MCP Server管理机制
- 实现连接状态监控和自动重连
- 保持系统的可扩展性和可维护性
- 符合用户的实际使用场景和需求

此方案基于用户提供的具体配置格式，提供了一个灵活、可靠、符合用户需求的MCP服务接入方案。