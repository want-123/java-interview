package com.mk.util;

import com.mk.entity.CustomAgentContext;
import java.util.LinkedList;
import java.util.Deque;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/19/17:57
 * @Description:
 */
public class MultiAgentContextHolder {
    // ThreadLocal存储Agent上下文栈（Deque：双端队列，模拟栈） 注意：只能使用单线程下的多Agent顺序方法或者嵌套访问！！！
    private static final ThreadLocal<Deque<CustomAgentContext>> AGENT_CONTEXT_STACK = ThreadLocal.withInitial(LinkedList::new);

    // ========== 栈操作：压栈（调用Agent前） ==========
    public static void push(CustomAgentContext context) {
        Deque<CustomAgentContext> stack = AGENT_CONTEXT_STACK.get();
        stack.push(context); // 压入栈顶
    }

    // ========== 栈操作：出栈（Agent调用完成后） ==========
    public static CustomAgentContext pop() {
        Deque<CustomAgentContext> stack = AGENT_CONTEXT_STACK.get();
        return stack.isEmpty() ? null : stack.pop(); // 弹出栈顶
    }

    // ========== 获取当前执行的Agent上下文（栈顶） ==========
    public static CustomAgentContext getCurrent() {
        Deque<CustomAgentContext> stack = AGENT_CONTEXT_STACK.get();
        return stack.isEmpty() ? null : stack.peek(); // 仅获取栈顶，不弹出
    }

    // ========== 获取整个上下文栈（用于回溯） ==========
    public static Deque<CustomAgentContext> getStack() {
        return AGENT_CONTEXT_STACK.get();
    }

    // ========== 清空栈（防止内存泄漏） ==========
    public static void clear() {
        AGENT_CONTEXT_STACK.get().clear();
        AGENT_CONTEXT_STACK.remove();
    }
}
