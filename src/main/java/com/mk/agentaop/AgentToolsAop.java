package com.mk.agentaop;

import com.mk.customagentannotation.AgentAnnotation;
import com.mk.customagentannotation.SecurityProperty;
import com.mk.customagentannotation.ToolsSecurity;
import com.mk.entity.CustomAgentContext;
import com.mk.entity.SysAgentAttr;
import com.mk.mapper.AgentMapper;
import com.mk.util.MultiAgentContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/19/16:55
 * @Description:
 */
@Component
@Aspect
public class AgentToolsAop {
    @Autowired
    private AgentMapper agentMapper;
    @Pointcut(
            "@within(com.mk.customagentannotation.AgentAnnotation) " +
            "&& execution(* com.mk.assistant.JavaAgent.chat*(..))")
    public void javaAgentChatPointcut() {}

    @Pointcut("@annotation(com.mk.customagentannotation.ToolsSecurity)")
    public void toolsSecurityPointcut() {}

    @Around("javaAgentChatPointcut()")
    public Object interceptJavaAgentChat(ProceedingJoinPoint joinPoint) throws Throwable {
        // ========== 步骤1：提取Agent核心信息（从自定义注解） ==========
        // 获取接口上的@AgentAnnotation注解
        Class<?> agentInterface = joinPoint.getTarget().getClass().getInterfaces()[0];
        AgentAnnotation agentAnnotation = agentInterface.getAnnotation(AgentAnnotation.class);
        long agentId = agentAnnotation.id(); // 从注解获取Agent ID（如1）
        // ========== 步骤2：构建多Agent上下文，压入栈 ==========
        CustomAgentContext agentContext = new CustomAgentContext();
        agentContext.setAgentId(agentId); // 拼接唯一Agent ID（如java-agent-1）
        MultiAgentContextHolder.push(agentContext);

        // ========== 步骤4：记录拦截日志（审计/监控） ==========
        System.out.println("=== JavaAgent Chat方法拦截 ===");
        System.out.println("Agent ID：" + agentContext.getAgentId());

        // ========== 步骤5：执行原方法（放行chat调用） ==========
        Object result = null;
        try {
            result = joinPoint.proceed(); // 执行chat方法（会触发Tool调用）
        } catch (Exception e) {
            System.out.println("JavaAgent Chat方法执行异常：" + e.getMessage());
            throw e; // 抛出异常，由全局异常处理器处理
        } finally {
            // ========== 步骤6：核心：执行完成后出栈，防止栈混乱 ==========
            MultiAgentContextHolder.pop();
            // 兜底：若栈为空，清空ThreadLocal
            if (MultiAgentContextHolder.getStack().isEmpty()) {
                MultiAgentContextHolder.clear();
            }
        }
        // ========== 步骤7：（可选）处理返回结果 ==========
//        System.out.println("Agent[" + agentContext.getAgentId() + "] Chat方法返回结果：" + result);
        return result;
    }

    @Around("toolsSecurityPointcut()")
    public Object interceptToolsSecurity(ProceedingJoinPoint joinPoint) {
        System.out.println("=== ToolsSecurity 方法拦截 ===");
        try {
            CustomAgentContext agentContext = MultiAgentContextHolder.getCurrent();
            if(agentContext == null) throw new RuntimeException("当前上下文为空，请检查是否已调用MultiAgentContextHolder.push()");
            List<SysAgentAttr> sysAgentAttrs = agentMapper.selectUserAttrByAgentId(agentContext.getAgentId());
            Map<String, String> agentAttrMap = sysAgentAttrs.stream()
                    .collect(Collectors.toMap(SysAgentAttr::getAttrKey, SysAgentAttr::getAttrValue,(existing, replacement) -> existing));
            System.out.println("Agent[" + agentContext.getAgentId() + "] 属性：" + agentAttrMap);
            // 2. 修正：获取方法上的@ToolsSecurity注解（核心）
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method targetMethod = signature.getMethod();
            ToolsSecurity toolsSecurity = targetMethod.getAnnotation(ToolsSecurity.class);
            if(toolsSecurity != null){
                for (SecurityProperty property : toolsSecurity.properties()) {
                    String key = property.key();
                    String value = property.value();
                    System.out.println("key:" + key + " value:" + value);
                    if (!agentAttrMap.containsKey(key) || !agentAttrMap.get(key).equals(value)) {
                        throw new RuntimeException("权限不足, 当前Agent不能调用该Tools");
                    }
                }
            }
        }catch (Exception e) {
            System.out.println("ToolsSecurity 方法执行异常：" + e.getMessage());
            throw e;
        }
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            System.out.println("ToolsSecurity 方法执行异常：" + e.getMessage());
            throw new RuntimeException(e);
        }
        return result;
    }


}
