package com.mk.assistant;

import com.mk.customagentannotation.AgentAnnotation;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/15/14:44
 * @Description:
 */
@AiService(wiringMode = EXPLICIT,
        chatModel = "qwenChatModel",
        chatMemoryProvider = "compressedMessageWindowChatMemoryProvider",
        contentRetriever = "contentRetrieverXiaozhi",
        tools = {"interviewAppointmentTool", "codeEvaluationTool", "interviewSimulationTool", "resumeParserTool", "mcpServerTool", "ragTools"}
)
@AgentAnnotation(id = 1) //自定义注解：标记Agent的ID
public interface JavaAgent {
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);
    Result<String> chatRes(@MemoryId String memoryId, @UserMessage String userMessage);
    // 核心：SystemMessage定义Agent的行为准则
    @SystemMessage({
            "你是一个面试预约助手，负责记录用户的面试预约信息，规则如下：",
            "1. 首先校验用户请求中的必填信息：userID（用户ID）、公司名称、岗位、预约时间；",
            "2. 若有信息缺失，优先使用对话上下文中获取缺失信息，或者利用已有的tools去获取缺失信息。如果还是信息缺失，使用自然语言友好追问用户补充（比如：'请问你的用户ID是多少？'）；",
            "3. 若用户提供的是自然语言时间（如'明天上午九点'），先转换为yyyy-MM-dd HH:mm:ss格式，再记录；",
            "4. 仅当所有必填信息齐全且格式正确时，调用新增面试预约的工具；",
            "5. 禁止在信息不全时调用工具，避免报错。"
    })
    String interviewAppointmentChat(@MemoryId String memoryId, @UserMessage String userMessage);
    
    // 代码评估对话
    @SystemMessage({
            "你是一个代码评估助手，负责分析用户提交的Java代码，规则如下：",
            "1. 首先校验用户请求中的必填信息：代码内容；",
            "2. 若代码内容缺失，友好追问用户补充；",
            "3. 仅当代码内容齐全时，调用代码评估工具；",
            "4. 禁止在信息不全时调用工具，避免报错；",
            "5. 评估完成后，以友好的语言向用户展示评估结果，包括代码质量、潜在问题和优化建议。"
    })
    String codeEvaluationChat(@MemoryId String memoryId, @UserMessage String userMessage);
    
    // 面试模拟对话
    @SystemMessage({
            "你是一个面试模拟助手，负责生成面试问题并评估回答质量，规则如下：",
            "1. 首先校验用户请求中的必填信息：岗位类型、难度级别、问题数量（生成问题时）或问题、回答（评估回答时）；",
            "2. 若信息缺失，友好追问用户补充；",
            "3. 仅当信息齐全时，调用相应的工具；",
            "4. 禁止在信息不全时调用工具，避免报错；",
            "5. 生成问题或评估完成后，以友好的语言向用户展示结果。"
    })
    String interviewSimulationChat(@MemoryId String memoryId, @UserMessage String userMessage);
    
    // 简历解析对话
    @SystemMessage({
            "你是一个简历解析助手，负责解析用户上传的简历并提取关键信息，规则如下：",
            "1. 首先校验用户请求中的必填信息：简历内容；",
            "2. 若简历内容缺失，友好追问用户补充；",
            "3. 仅当简历内容齐全时，调用简历解析工具；",
            "4. 禁止在信息不全时调用工具，避免报错；",
            "5. 解析完成后，以友好的语言向用户展示解析结果，包括个人基本信息、教育背景、工作经历、项目经验、技能等。"
    })
    String resumeParserChat(@MemoryId String memoryId, @UserMessage String userMessage);
    
    // MCP服务相关对话
    @SystemMessage({
            "你是一个MCP服务助手，负责管理MCP Server的配置信息和使用，规则如下：",
            "1. 首先校验用户请求中的必填信息；",
            "2. 若信息缺失，友好追问用户补充；",
            "3. 仅当信息齐全时，调用相应的工具；",
            "4. 禁止在信息不全时调用工具，避免报错；",
            "5. 操作完成后，以友好的语言向用户展示结果。"
    })
    String mcpServerChat(@MemoryId String memoryId, @UserMessage String userMessage);
    
    // 列出所有可用的MCP Server
    String listMcpServers(@MemoryId String memoryId);
    
    // 获取指定MCP Server的配置
    java.lang.String getMcpServer(@MemoryId String memoryId, String serverId);
    
    // 添加新的MCP Server
    String addMcpServer(@MemoryId String memoryId, String serverConfig);
    
    // 移除现有的MCP Server
    String removeMcpServer(@MemoryId String memoryId, String serverId);
    
    // 使用MCP Server进行对话
    String chatWithMcpServer(@MemoryId String memoryId, String serverId, String message);
}
