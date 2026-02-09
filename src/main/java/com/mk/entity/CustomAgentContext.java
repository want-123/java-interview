package com.mk.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/19/17:58
 * @Description:
 */
@Data
public class CustomAgentContext {
    // Agent核心标识
    private Long agentId; // 唯一Agent ID（如order-agent-001）
    private String agentType; // Agent类型
    private String businessDomain; // 业务域

    // 调用追踪字段（核心：关联多Agent调用）
    private String traceId; // 整个业务流程的唯一追踪ID
    private String spanId; // 当前Agent调用的子ID（唯一）
    private String parentSpanId; // 父Agent的SpanID（嵌套调用时）

    // 扩展字段
    private Map<String, Object> extParams = new HashMap<>();

    // 便捷方法：生成默认TraceID（首次调用时）
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 便捷方法：生成SpanID
    public static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    // 扩展方法
    public void putExtParam(String key, Object value) {
        this.extParams.put(key, value);
    }

    public <T> T getExtParam(String key, Class<T> clazz) {
        return clazz.cast(this.extParams.get(key));
    }
}
