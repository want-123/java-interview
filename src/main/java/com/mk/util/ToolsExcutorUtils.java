package com.mk.util;

import dev.langchain4j.service.IllegalConfigurationException;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolService;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/21/21:40
 * @Description:
 */
@Component
public class ToolsExcutorUtils {

    /**
     * 关键：扫描结果直接写进这个 Map，测试方法里打印的就是它
     */
    private final Map<String, ToolExecutor> toolExecutorMap = new HashMap<>();

    /**
     * 对外唯一入口，支持可变参数，也支持 List 重载
     */
    public Map<String, ToolExecutor> tools(Object... beans) {
        toolExecutorMap.clear();          // 每次调用都重新扫
        for (Object bean : beans) {
            scan(bean);
        }
        return Map.copyOf(toolExecutorMap); // 返回不可变视图
    }

    /* 重载：你想传 List 也可以 */
    public Map<String, ToolExecutor> tools(List<?> beans) {
        return tools(beans.toArray());
    }

    /* 真正扫描逻辑 */
    private void scan(Object bean) {
        if (bean == null) return;
        Class<?> clazz = org.springframework.aop.framework.AopProxyUtils.ultimateTargetClass(bean);

        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(dev.langchain4j.agent.tool.Tool.class)) continue;

            /* 生成官方 ToolSpecification */
            dev.langchain4j.agent.tool.ToolSpecification spec =
                    dev.langchain4j.agent.tool.ToolSpecifications.toolSpecificationFrom(m);

            /* 重复 KEY 保护 */
            if (toolExecutorMap.containsKey(spec.name())) {
                throw new IllegalConfigurationException("Duplicated tool: " + spec.name());
            }
            /* 创建 Executor 并入库 */
            toolExecutorMap.put(spec.name(),
                    new DefaultToolExecutor(bean, m));
        }
    }
}