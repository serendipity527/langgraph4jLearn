package org.example.langgraph4jlearn.controller;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.example.langgraph4jlearn.agent.graph.ComprehensiveWorkflowGraph;
import org.example.langgraph4jlearn.agent.state.ComprehensiveWorkflowState;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 综合工作流 Controller
 * 
 * 提供 REST API 展示 LangGraph4j 的完整功能:
 * - 同步执行 (invoke)
 * - 流式执行 (stream) 使用 SSE
 * - 人在回路 (Human-in-the-loop)
 * - 状态检查 (getSnapshot)
 * - 状态更新 (updateState)
 * - 恢复执行 (resume)
 */
@Slf4j
@RestController
@RequestMapping("/api/comprehensive")
@CrossOrigin(origins = "*")
public class ComprehensiveWorkflowController {
    
    private final ComprehensiveWorkflowGraph workflowGraph;
    private final MemorySaver checkpointSaver;
    private final Map<String, String> sessionThreadMap;  // sessionId -> threadId
    
    public ComprehensiveWorkflowController() {
        this.workflowGraph = new ComprehensiveWorkflowGraph();
        this.checkpointSaver = new MemorySaver();
        this.sessionThreadMap = new ConcurrentHashMap<>();
    }
    
    // ========== 健康检查 ==========
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "Comprehensive Workflow Demo",
                "description", "展示 LangGraph4j 完整功能的综合示例",
                "features", List.of(
                        "StateGraph", "Nodes", "Edges", "Conditional Edges",
                        "Channels/Reducers", "Checkpointer", "Human-in-the-Loop",
                        "Stream/Invoke", "Cycle/Loop"
                )
        );
    }
    
    // ========== 简单执行 (无人在回路) ==========
    
    /**
     * 同步执行工作流 (invoke)
     * 适用于安全操作和工具操作
     * 
     * POST /api/comprehensive/invoke
     * Body: {"userInput": "查询天气"}
     */
    @PostMapping("/invoke")
    public Map<String, Object> invoke(@RequestBody Map<String, String> request) {
        String userInput = request.getOrDefault("userInput", "默认输入");
        
        log.info("📥 收到同步执行请求: {}", userInput);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var compiledGraph = workflowGraph.compile();
            
            Map<String, Object> initialState = Map.of(
                    ComprehensiveWorkflowState.USER_INPUT, userInput,
                    ComprehensiveWorkflowState.MAX_RETRIES, 3
            );
            
            var result = compiledGraph.invoke(initialState);
            ComprehensiveWorkflowState finalState = result.get();
            
            response.put("success", true);
            response.put("intent", finalState.intent());
            response.put("finalResult", finalState.finalResult());
            response.put("messages", finalState.messages());
            response.put("executionLog", finalState.executionLog());
            response.put("toolResults", finalState.toolResults());
            response.put("totalSteps", finalState.stepCount());
            response.put("status", finalState.workflowStatus());
            
        } catch (Exception e) {
            log.error("执行失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 流式执行工作流 (stream) - 使用 SSE
     * 
     * GET /api/comprehensive/stream?userInput=xxx
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> stream(@RequestParam String userInput) {
        log.info("🌊 收到流式执行请求: {}", userInput);
        
        return Flux.<Map<String, Object>>create(sink -> {
            try {
                var compiledGraph = workflowGraph.compile();
                
                Map<String, Object> initialState = Map.of(
                        ComprehensiveWorkflowState.USER_INPUT, userInput,
                        ComprehensiveWorkflowState.MAX_RETRIES, 3
                );
                
                for (NodeOutput<ComprehensiveWorkflowState> nodeOutput : compiledGraph.stream(initialState)) {
                    ComprehensiveWorkflowState state = nodeOutput.state();
                    
                    Map<String, Object> event = new HashMap<>();
                    event.put("node", nodeOutput.node());
                    event.put("currentStep", state.currentStep());
                    event.put("stepCount", state.stepCount());
                    event.put("intent", state.intent());
                    event.put("messages", state.messages());
                    event.put("status", state.workflowStatus());
                    
                    sink.next(event);
                }
                
                sink.complete();
                
            } catch (Exception e) {
                log.error("流式执行失败", e);
                sink.error(e);
            }
        }).delayElements(Duration.ofMillis(100));  // 添加延迟以便观察
    }
    
    // ========== 人在回路 (Human-in-the-Loop) ==========
    
    /**
     * 启动带人在回路的工作流
     * 如果是危险操作，会在审批节点中断
     * 
     * POST /api/comprehensive/start-hitl
     * Body: {"userInput": "删除所有数据"}
     */
    @PostMapping("/start-hitl")
    public Map<String, Object> startWithHumanInLoop(@RequestBody Map<String, String> request) {
        String userInput = request.getOrDefault("userInput", "");
        String sessionId = UUID.randomUUID().toString();
        String threadId = "thread-" + sessionId;
        
        log.info("🚀 启动人在回路工作流 - sessionId: {}, input: {}", sessionId, userInput);
        
        sessionThreadMap.put(sessionId, threadId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 编译图 (带人在回路)
            var compiledGraph = workflowGraph.compileWithHumanInLoop(checkpointSaver);
            
            // 初始状态
            Map<String, Object> initialState = Map.of(
                    ComprehensiveWorkflowState.USER_INPUT, userInput,
                    ComprehensiveWorkflowState.MAX_RETRIES, 3
            );
            
            // 运行配置 (Thread ID)
            var invokeConfig = RunnableConfig.builder()
                    .threadId(threadId)
                    .build();
            
            // 执行到中断点
            List<Map<String, Object>> executionEvents = new ArrayList<>();
            for (var nodeOutput : compiledGraph.stream(initialState, invokeConfig)) {
                Map<String, Object> event = new HashMap<>();
                event.put("node", nodeOutput.node());
                event.put("step", nodeOutput.state().currentStep());
                event.put("intent", nodeOutput.state().intent());
                executionEvents.add(event);
            }
            
            // 获取当前状态 (getSnapshot)
            var currentSnapshot = compiledGraph.getState(invokeConfig);
            ComprehensiveWorkflowState state = currentSnapshot.state();
            
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("currentNode", currentSnapshot.node());
            response.put("nextNode", currentSnapshot.next());
            response.put("intent", state.intent());
            response.put("requiresApproval", state.requiresApproval());
            response.put("approvalStatus", state.approvalStatus());
            response.put("messages", state.messages());
            response.put("executionLog", state.executionLog());
            response.put("executionEvents", executionEvents);
            
            // 检查是否在等待审批
            boolean waitingForApproval = state.requiresApproval() && 
                    "waiting".equals(state.approvalStatus());
            response.put("waitingForApproval", waitingForApproval);
            
            if (waitingForApproval) {
                response.put("message", "⚠️ 检测到危险操作，需要人工审批。请调用 /approve 或 /reject 接口。");
            } else {
                response.put("message", "✅ 工作流执行完成，无需审批。");
            }
            
        } catch (Exception e) {
            log.error("启动人在回路工作流失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取会话状态 (getSnapshot)
     * 
     * GET /api/comprehensive/status/{sessionId}
     */
    @GetMapping("/status/{sessionId}")
    public Map<String, Object> getStatus(@PathVariable String sessionId) {
        log.info("📊 获取会话状态: {}", sessionId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String threadId = sessionThreadMap.get(sessionId);
            if (threadId == null) {
                response.put("success", false);
                response.put("error", "无效的会话ID");
                return response;
            }
            
            var compiledGraph = workflowGraph.compileWithHumanInLoop(checkpointSaver);
            var invokeConfig = RunnableConfig.builder()
                    .threadId(threadId)
                    .build();
            
            var snapshot = compiledGraph.getState(invokeConfig);
            ComprehensiveWorkflowState state = snapshot.state();
            
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("threadId", threadId);
            response.put("currentNode", snapshot.node());
            response.put("nextNode", snapshot.next());
            response.put("currentStep", state.currentStep());
            response.put("stepCount", state.stepCount());
            response.put("intent", state.intent());
            response.put("requiresApproval", state.requiresApproval());
            response.put("approvalStatus", state.approvalStatus());
            response.put("workflowStatus", state.workflowStatus());
            response.put("messages", state.messages());
            response.put("executionLog", state.executionLog());
            response.put("toolResults", state.toolResults());
            
        } catch (Exception e) {
            log.error("获取状态失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 批准操作 (updateState + resume)
     * 
     * POST /api/comprehensive/approve
     * Body: {"sessionId": "xxx"}
     */
    @PostMapping("/approve")
    public Map<String, Object> approve(@RequestBody Map<String, String> request) {
        return handleApprovalDecision(request, "approve");
    }
    
    /**
     * 拒绝操作 (updateState + resume)
     * 
     * POST /api/comprehensive/reject
     * Body: {"sessionId": "xxx"}
     */
    @PostMapping("/reject")
    public Map<String, Object> reject(@RequestBody Map<String, String> request) {
        return handleApprovalDecision(request, "reject");
    }
    
    /**
     * 通用审批决策处理
     */
    private Map<String, Object> handleApprovalDecision(Map<String, String> request, String decision) {
        String sessionId = request.get("sessionId");
        
        log.info("📝 处理审批决策: sessionId={}, decision={}", sessionId, decision);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String threadId = sessionThreadMap.get(sessionId);
            if (threadId == null) {
                response.put("success", false);
                response.put("error", "无效的会话ID");
                return response;
            }
            
            var compiledGraph = workflowGraph.compileWithHumanInLoop(checkpointSaver);
            var invokeConfig = RunnableConfig.builder()
                    .threadId(threadId)
                    .build();
            
            // updateState - 更新人工反馈
            var updateConfig = compiledGraph.updateState(
                    invokeConfig,
                    Map.of(ComprehensiveWorkflowState.HUMAN_FEEDBACK, decision),
                    null
            );
            
            // resume - 恢复执行
            List<Map<String, Object>> resumeEvents = new ArrayList<>();
            for (var nodeOutput : compiledGraph.stream(GraphInput.resume(), updateConfig)) {
                Map<String, Object> event = new HashMap<>();
                event.put("node", nodeOutput.node());
                event.put("step", nodeOutput.state().currentStep());
                event.put("approvalStatus", nodeOutput.state().approvalStatus());
                resumeEvents.add(event);
            }
            
            // 获取最终状态
            var finalSnapshot = compiledGraph.getState(invokeConfig);
            ComprehensiveWorkflowState finalState = finalSnapshot.state();
            
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("decision", decision);
            response.put("approvalStatus", finalState.approvalStatus());
            response.put("workflowStatus", finalState.workflowStatus());
            response.put("finalResult", finalState.finalResult());
            response.put("messages", finalState.messages());
            response.put("executionLog", finalState.executionLog());
            response.put("toolResults", finalState.toolResults());
            response.put("resumeEvents", resumeEvents);
            response.put("totalSteps", finalState.stepCount());
            
            if ("approve".equals(decision)) {
                response.put("message", "✅ 操作已批准并执行完成");
            } else {
                response.put("message", "❌ 操作已拒绝");
            }
            
        } catch (Exception e) {
            log.error("处理审批决策失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 流式执行带人在回路 (SSE)
     * 
     * GET /api/comprehensive/stream-hitl?userInput=xxx&sessionId=xxx
     */
    @GetMapping(value = "/stream-hitl", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> streamWithHumanInLoop(
            @RequestParam String userInput,
            @RequestParam(required = false) String sessionId) {
        
        String finalSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();
        String threadId = "thread-" + finalSessionId;
        sessionThreadMap.put(finalSessionId, threadId);
        
        log.info("🌊 开始流式人在回路执行: sessionId={}", finalSessionId);
        
        return Flux.<Map<String, Object>>create(sink -> {
            try {
                var compiledGraph = workflowGraph.compileWithHumanInLoop(checkpointSaver);
                
                Map<String, Object> initialState = Map.of(
                        ComprehensiveWorkflowState.USER_INPUT, userInput,
                        ComprehensiveWorkflowState.MAX_RETRIES, 3
                );
                
                var invokeConfig = RunnableConfig.builder()
                        .threadId(threadId)
                        .build();
                
                for (NodeOutput<ComprehensiveWorkflowState> nodeOutput : compiledGraph.stream(initialState, invokeConfig)) {
                    ComprehensiveWorkflowState state = nodeOutput.state();
                    
                    Map<String, Object> event = new HashMap<>();
                    event.put("type", "node_output");
                    event.put("sessionId", finalSessionId);
                    event.put("node", nodeOutput.node());
                    event.put("currentStep", state.currentStep());
                    event.put("stepCount", state.stepCount());
                    event.put("intent", state.intent());
                    event.put("requiresApproval", state.requiresApproval());
                    event.put("approvalStatus", state.approvalStatus());
                    event.put("messages", state.messages());
                    event.put("workflowStatus", state.workflowStatus());
                    
                    sink.next(event);
                }
                
                // 发送最终状态
                var finalSnapshot = compiledGraph.getState(invokeConfig);
                ComprehensiveWorkflowState finalState = finalSnapshot.state();
                
                Map<String, Object> finalEvent = new HashMap<>();
                finalEvent.put("type", "final_state");
                finalEvent.put("sessionId", finalSessionId);
                finalEvent.put("currentNode", finalSnapshot.node());
                finalEvent.put("nextNode", finalSnapshot.next());
                finalEvent.put("waitingForApproval", 
                        finalState.requiresApproval() && "waiting".equals(finalState.approvalStatus()));
                finalEvent.put("workflowStatus", finalState.workflowStatus());
                finalEvent.put("messages", finalState.messages());
                
                sink.next(finalEvent);
                sink.complete();
                
            } catch (Exception e) {
                log.error("流式执行失败", e);
                Map<String, Object> errorEvent = Map.of(
                        "type", "error",
                        "error", e.getMessage()
                );
                sink.next(errorEvent);
                sink.complete();
            }
        }).delayElements(Duration.ofMillis(200));
    }
    
    /**
     * 清理会话
     * 
     * DELETE /api/comprehensive/session/{sessionId}
     */
    @DeleteMapping("/session/{sessionId}")
    public Map<String, Object> clearSession(@PathVariable String sessionId) {
        sessionThreadMap.remove(sessionId);
        log.info("🗑️ 清理会话: {}", sessionId);
        
        return Map.of(
                "success", true,
                "message", "会话已清理"
        );
    }
    
    /**
     * 获取所有活跃会话
     * 
     * GET /api/comprehensive/sessions
     */
    @GetMapping("/sessions")
    public Map<String, Object> listSessions() {
        return Map.of(
                "success", true,
                "sessions", sessionThreadMap.keySet(),
                "count", sessionThreadMap.size()
        );
    }
}
