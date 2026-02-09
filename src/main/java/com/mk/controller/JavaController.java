package com.mk.controller;

import com.mk.assistant.JavaAgent;
import com.mk.common.Result;
import com.mk.entity.MultimodalParseResult;
import com.mk.entity.MultimodelEntity;
import com.mk.entity.RagReply;
import com.mk.service.AgnetService;
import com.mk.util.JavaMultimodalParser;
import com.mk.util.MemoryIdManager;
import com.mk.util.UserContext;
import dev.langchain4j.model.chat.response.ChatResponse;
// import dev.langchain4j.service.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/15/20:21
 * @Description:
 */
@RestController
@RequestMapping("/api/java")
@Tag(name = "Java面试")
public class JavaController {
    @Autowired
    private JavaAgent xiaozhiAgent;
    @Autowired
    private JavaMultimodalParser javaMultimodalParser;
    @Autowired
    private MemoryIdManager memoryIdManager;

    @Autowired
    private AgnetService agentService;
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "对话")
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody MultimodelEntity multimodelEntity) {
        try {
            String memoryId = multimodelEntity.getMemoryId();
            Long userId = multimodelEntity.getUserId();
            if(userId == null){
                userId = UserContext.getCurrentUser().getUserId();
            }
            if(memoryId == null){
                memoryId = memoryIdManager.generateMemoryId(userId);
            }
            MultimodalParseResult parseResult = javaMultimodalParser.parse(multimodelEntity);
            String result = agentService.chat2(memoryId, multimodelEntity.getContent() +"\n" + parseResult.getContent()); //分解检索
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "对话失败：" + e.getMessage());
        }
    }

    @Operation(summary = "对话2")
    @PostMapping("/chatRes")
    public Result<?> chatRes(@RequestBody MultimodelEntity multimodelEntity) {
        try {
            //System.out.println("MultimodelEntity" + multimodelEntity);
            String memoryId = multimodelEntity.getMemoryId();
            Long userId = multimodelEntity.getUserId();
            if(userId == null) throw new RuntimeException("用户ID不能为空");
            if(memoryId == null){
                memoryId = memoryIdManager.generateMemoryId(userId);
            }
            MultimodalParseResult parseResult = javaMultimodalParser.parse(multimodelEntity);

            dev.langchain4j.service.Result<String> chatResponse = xiaozhiAgent.chatRes(memoryId, multimodelEntity.getContent() +"\n" + parseResult.getContent());
            List<RagReply.Ref> refs = chatResponse.sources()
                    .stream()
                    .map(c -> new RagReply.Ref(
                            c.textSegment().text(),
                            c.textSegment().metadata().toMap()))
                    .collect(Collectors.toList());

            RagReply reply = RagReply.builder()
                    .answer(chatResponse.content())
                    .sources(refs)   // 用自己的 DTO
                    .build();
            return Result.success(reply);
        } catch (Exception e) {
            return Result.error(500, "对话失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除会话memoryId")
    @PostMapping("/deleteMemory")
    public Result<?> deleteMemory(@RequestBody MultimodelEntity multimodelEntity) {
        try {
            memoryIdManager.clearMemoryId(multimodelEntity.getMemoryId());
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }
    
    @Operation(summary = "代码评估对话")
    @PostMapping("/codeEvaluation")
    public Result<?> codeEvaluation(@RequestBody MultimodelEntity multimodelEntity) {
        try {
            String memoryId = multimodelEntity.getMemoryId();
            Long userId = multimodelEntity.getUserId();
            if(userId == null){
                userId = UserContext.getCurrentUser().getUserId();
            }
            if(memoryId == null){
                memoryId = memoryIdManager.generateMemoryId(userId);
            }
            MultimodalParseResult parseResult = javaMultimodalParser.parse(multimodelEntity);
            String result = xiaozhiAgent.codeEvaluationChat(memoryId, multimodelEntity.getContent() +"\n" + parseResult.getContent());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "代码评估失败：" + e.getMessage());
        }
    }
    
    @Operation(summary = "面试模拟对话")
    @PostMapping("/interviewSimulation")
    public Result<?> interviewSimulation(@RequestBody MultimodelEntity multimodelEntity) {
        try {
            String memoryId = multimodelEntity.getMemoryId();
            Long userId = multimodelEntity.getUserId();
            if(userId == null){
                userId = UserContext.getCurrentUser().getUserId();
            }
            if(memoryId == null){
                memoryId = memoryIdManager.generateMemoryId(userId);
            }
            MultimodalParseResult parseResult = javaMultimodalParser.parse(multimodelEntity);
            String result = xiaozhiAgent.interviewSimulationChat(memoryId, multimodelEntity.getContent() +"\n" + parseResult.getContent());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "面试模拟失败：" + e.getMessage());
        }
    }
    
    @Operation(summary = "简历解析对话")
    @PostMapping("/resumeParser")
    public Result<?> resumeParser(@RequestBody MultimodelEntity multimodelEntity) {
        try {
            String memoryId = multimodelEntity.getMemoryId();
            Long userId = multimodelEntity.getUserId();
            if(userId == null){
                userId = UserContext.getCurrentUser().getUserId();
            }
            if(memoryId == null){
                memoryId = memoryIdManager.generateMemoryId(userId);
            }
            MultimodalParseResult parseResult = javaMultimodalParser.parse(multimodelEntity);
            String result = xiaozhiAgent.resumeParserChat(memoryId, multimodelEntity.getContent() +"\n" + parseResult.getContent());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "简历解析失败：" + e.getMessage());
        }
    }
}
