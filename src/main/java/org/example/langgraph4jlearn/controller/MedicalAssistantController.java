package org.example.langgraph4jlearn.controller;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.example.langgraph4jlearn.agent.graph.MedicalAssistantGraph;
import org.example.langgraph4jlearn.agent.state.MedicalSystemState;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 医疗助手Controller
 * 支持人在回路的免责声明确认流程
 */
@Slf4j
@RestController
@RequestMapping("/api/medical-assistant")
@CrossOrigin(origins = "*")
public class MedicalAssistantController {
    
    private final MedicalAssistantGraph graph;
    private final MemorySaver checkpointSaver;
    private final Map<String, String> sessionThreadMap; // sessionId -> threadId映射
    
    public MedicalAssistantController() {
        this.graph = new MedicalAssistantGraph();
        this.checkpointSaver = new MemorySaver();
        this.sessionThreadMap = new ConcurrentHashMap<>();
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Medical Assistant with Human-in-the-Loop");
        response.put("description", "医疗助手 - 支持免责声明人在回路确认");
        return response;
    }
    
    /**
     * 启动新的医疗咨询会话
     * 显示免责声明并等待用户确认
     * 
     * POST /api/medical-assistant/start
     * Body: {"userQuery": "我想咨询健康问题"}
     */
    @PostMapping("/start")
    public Map<String, Object> startConsultation(@RequestBody Map<String, String> request) {
        String userQuery = request.getOrDefault("userQuery", "");
        String sessionId = UUID.randomUUID().toString();
        String threadId = "thread-" + sessionId;
        
        log.info("启动新的医疗咨询会话，sessionId: {}, threadId: {}", sessionId, threadId);
        
        sessionThreadMap.put(sessionId, threadId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 编译图（带人在回路）
            var compiledGraph = graph.compileWithHumanInLoop(checkpointSaver);
            
            // 初始输入
            Map<String, Object> initialInput = Map.of(
                    MedicalSystemState.USER_QUERY, userQuery,
                    MedicalSystemState.USER_CONSENTED, false
            );
            
            // 运行配置
            var invokeConfig = RunnableConfig.builder()
                    .threadId(threadId)
                    .build();
            
            // 执行到第一个中断点（user_consent节点前）
            for (var event : compiledGraph.stream(initialInput, invokeConfig)) {
                log.debug("事件: {}", event);
            }
            
            // 获取当前状态
            var currentState = compiledGraph.getState(invokeConfig);
            
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("currentNode", currentState.node());
            response.put("nextNode", currentState.next());
            response.put("disclaimerShown", currentState.state().disclaimerShown());
            response.put("messages", currentState.state().messages());
            response.put("waitingForConsent", true);
            response.put("message", "免责声明已展示，等待用户确认");
            
        } catch (Exception e) {
            log.error("启动咨询会话失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 用户确认或拒绝免责声明
     * 
     * POST /api/medical-assistant/consent
     * Body: {"sessionId": "xxx", "consented": true}
     */
    @PostMapping("/consent")
    public Map<String, Object> submitConsent(@RequestBody Map<String, Object> request) {
        String sessionId = (String) request.get("sessionId");
        Boolean consented = (Boolean) request.getOrDefault("consented", false);
        
        log.info("收到用户同意反馈，sessionId: {}, consented: {}", sessionId, consented);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String threadId = sessionThreadMap.get(sessionId);
            if (threadId == null) {
                response.put("success", false);
                response.put("error", "无效的会话ID");
                return response;
            }
            
            // 编译图
            var compiledGraph = graph.compileWithHumanInLoop(checkpointSaver);
            
            // 运行配置
            var invokeConfig = RunnableConfig.builder()
                    .threadId(threadId)
                    .build();
            
            // 更新状态：添加用户同意信息
            var updateConfig = compiledGraph.updateState(
                    invokeConfig,
                    Map.of(MedicalSystemState.USER_CONSENTED, consented),
                    null
            );
            
            // 继续执行
            List<String> executionMessages = new ArrayList<>();
            for (var event : compiledGraph.stream(GraphInput.resume(), updateConfig)) {
                log.debug("继续执行事件: {}", event);
                executionMessages.add(event.toString());
            }
            
            // 获取最终状态
            var finalState = compiledGraph.getState(invokeConfig);
            
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("consented", consented);
            response.put("currentNode", finalState.node());
            response.put("nextNode", finalState.next());
            response.put("stage", finalState.state().stage().toString());
            response.put("messages", finalState.state().messages());
            response.put("isEmergency", finalState.state().isEmergency());
            response.put("intent", finalState.state().intent());
            response.put("finalResponse", finalState.state().response());
            response.put("executionLog", executionMessages);
            
            if (consented) {
                response.put("message", "用户已同意，流程继续");
            } else {
                response.put("message", "用户拒绝，流程终止");
            }
            
        } catch (Exception e) {
            log.error("处理用户同意失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取会话状态
     * 
     * GET /api/medical-assistant/status/{sessionId}
     */
    @GetMapping("/status/{sessionId}")
    public Map<String, Object> getSessionStatus(@PathVariable String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String threadId = sessionThreadMap.get(sessionId);
            if (threadId == null) {
                response.put("success", false);
                response.put("error", "无效的会话ID");
                return response;
            }
            
            var compiledGraph = graph.compileWithHumanInLoop(checkpointSaver);
            var invokeConfig = RunnableConfig.builder()
                    .threadId(threadId)
                    .build();
            
            var state = compiledGraph.getState(invokeConfig);
            
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("threadId", threadId);
            response.put("currentNode", state.node());
            response.put("nextNode", state.next());
            response.put("stage", state.state().stage().toString());
            response.put("messages", state.state().messages());
            response.put("userConsented", state.state().userConsented());
            response.put("disclaimerShown", state.state().disclaimerShown());
            
        } catch (Exception e) {
            log.error("获取会话状态失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 清理会话
     * 
     * DELETE /api/medical-assistant/session/{sessionId}
     */
    @DeleteMapping("/session/{sessionId}")
    public Map<String, Object> clearSession(@PathVariable String sessionId) {
        sessionThreadMap.remove(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "会话已清理");
        return response;
    }
}
