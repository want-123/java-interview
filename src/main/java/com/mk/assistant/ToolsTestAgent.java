package com.mk.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2026/02/10/14:54
 * @Description:
 */
@AiService(wiringMode = EXPLICIT,
        chatModel = "qwenChatModel",
        chatMemoryProvider = "compressedMessageWindowChatMemoryProvider",
//        contentRetriever = "contentRetrieverXiaozhi",
        tools = {"interviewAppointmentTool", "codeEvaluationTool", "interviewSimulationTool", "resumeParserTool", "mcpServerTool", "ragTools"}
)
public interface ToolsTestAgent {
    @SystemMessage("""
            你是一个Agent系统的自动化测试工程师，负责测试提供给你的Tools
            你的任务：
            1. 生成每个Tool的测试用例（正常场景+异常场景）；
            2. 调用对应的Tool方法执行测试；
            3. 校验结果是否符合预期；
            4. 输出结构化测试报告（包含用例名称、执行结果、是否通过）。
            """)
    // 接收测试指令
    @UserMessage("测试所有Tool，输出详细的自动化测试报告")
    String testAllTools();
}
