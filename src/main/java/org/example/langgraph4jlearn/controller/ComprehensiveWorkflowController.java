package org.example.langgraph4jlearn.controller;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.example.langgraph4jlearn.agent.graph.AdaptiveRAGGraph;
import org.example.langgraph4jlearn.agent.graph.ComprehensiveWorkflowGraph;
import org.example.langgraph4jlearn.agent.graph.MultiAgentGraph;
import org.example.langgraph4jlearn.agent.graph.ParallelAgentGraph;
import org.example.langgraph4jlearn.agent.state.ComprehensiveWorkflowState;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/comprehensive")
@CrossOrigin(origins = "*")
public class ComprehensiveWorkflowController {
    
    private final ComprehensiveWorkflowGraph workflowGraph;
    private final MultiAgentGraph multiAgentGraph;
    private final ParallelAgentGraph parallelAgentGraph;
    private final AdaptiveRAGGraph adaptiveRAGGraph;
    private final MemorySaver checkpointSaver;
    private final Map<String, String> sessionThreadMap = new ConcurrentHashMap<>();
    
    public ComprehensiveWorkflowController() {
        this.workflowGraph = new ComprehensiveWorkflowGraph();
        this.multiAgentGraph = new MultiAgentGraph();
        this.parallelAgentGraph = new ParallelAgentGraph();
        this.adaptiveRAGGraph = new AdaptiveRAGGraph();
        this.checkpointSaver = new MemorySaver();
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "service", "comprehensive-workflow");
    }
    
    @PostMapping("/invoke")
    public Map<String, Object> invoke(@RequestBody Map<String, String> request) {
        String userInput = request.getOrDefault("userInput", "");
        log.info("📥 同步执行: {}", userInput);
        Map<String, Object> response = new HashMap<>();
        try {
            var compiled = workflowGraph.compile();
            var result = compiled.invoke(Map.of(
                    ComprehensiveWorkflowState.USER_INPUT, userInput,
                    ComprehensiveWorkflowState.MAX_RETRIES, 3
            ));
            ComprehensiveWorkflowState state = result.get();
            response.put("success", true);
            response.put("intent", state.intent());
            response.put("messages", state.messages());
            response.put("executionLog", state.executionLog());
            response.put("toolResults", state.toolResults());
            response.put("finalResult", state.finalResult());
            response.put("workflowStatus", state.workflowStatus());
            response.put("totalSteps", state.stepCount());
        } catch (Exception e) {
            log.error("执行失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> stream(@RequestParam String userInput) {
        log.info("🌊 流式执行: {}", userInput);
        return Flux.create(sink -> {
            try {
                var compiled = workflowGraph.compile();
                for (var nodeOutput : compiled.stream(Map.of(
                        ComprehensiveWorkflowState.USER_INPUT, userInput,
                        ComprehensiveWorkflowState.MAX_RETRIES, 3))) {
                    ComprehensiveWorkflowState state = nodeOutput.state();
                    Map<String, Object> data = new HashMap<>();
                    data.put("node", nodeOutput.node());
                    data.put("currentStep", state.currentStep());
                    data.put("intent", state.intent());
                    data.put("stepCount", state.stepCount());
                    data.put("messages", state.messages());
                    sink.next(data);
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    
    /**
     * 使用ThreadId执行（不带HITL中断，完整执行）
     */
    @PostMapping("/start-with-thread")
    public Map<String, Object> startWithThread(@RequestBody Map<String, String> request) {
        String userInput = request.getOrDefault("userInput", "");
        String threadId = request.getOrDefault("threadId", "default-thread");
        log.info("🚀 ThreadId执行(无中断): threadId={}, input={}", threadId, userInput);
        Map<String, Object> response = new HashMap<>();
        try {
            // 使用带checkpointer但不带中断的编译
            var compiled = workflowGraph.getGraph().compile(
                    org.bsc.langgraph4j.CompileConfig.builder()
                            .checkpointSaver(checkpointSaver)
                            .build());
            var config = RunnableConfig.builder().threadId(threadId).build();
            List<Map<String, Object>> events = new ArrayList<>();
            for (var nodeOutput : compiled.stream(Map.of(
                    ComprehensiveWorkflowState.USER_INPUT, userInput,
                    ComprehensiveWorkflowState.MAX_RETRIES, 3), config)) {
                Map<String, Object> evt = new HashMap<>();
                evt.put("node", nodeOutput.node());
                evt.put("step", nodeOutput.state().currentStep());
                evt.put("intent", nodeOutput.state().intent());
                events.add(evt);
            }
            var snapshot = compiled.getState(config);
            ComprehensiveWorkflowState state = snapshot.state();
            response.put("success", true);
            response.put("threadId", threadId);
            response.put("currentNode", snapshot.node());
            response.put("intent", state.intent());
            response.put("messages", state.messages());
            response.put("executionLog", state.executionLog());
            response.put("executionEvents", events);
            response.put("stepCount", state.stepCount());
            response.put("waitingForApproval", false);
            response.put("approvalStatus", state.approvalStatus());
            response.put("message", "✅ 完成（ThreadId模式，无HITL中断）");
        } catch (Exception e) {
            log.error("启动失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 人在回路模式执行（危险操作会中断等待审批）
     */
    @PostMapping("/start-hitl")
    public Map<String, Object> startWithHitl(@RequestBody Map<String, String> request) {
        String userInput = request.getOrDefault("userInput", "");
        String threadId = request.getOrDefault("threadId", "default-thread");
        log.info("🔒 HITL模式启动: threadId={}, input={}", threadId, userInput);
        Map<String, Object> response = new HashMap<>();
        try {
            var compiled = workflowGraph.compileWithHumanInLoop(checkpointSaver);
            var config = RunnableConfig.builder().threadId(threadId).build();
            List<Map<String, Object>> events = new ArrayList<>();
            for (var nodeOutput : compiled.stream(Map.of(
                    ComprehensiveWorkflowState.USER_INPUT, userInput,
                    ComprehensiveWorkflowState.MAX_RETRIES, 3), config)) {
                Map<String, Object> evt = new HashMap<>();
                evt.put("node", nodeOutput.node());
                evt.put("step", nodeOutput.state().currentStep());
                evt.put("intent", nodeOutput.state().intent());
                events.add(evt);
            }
            var snapshot = compiled.getState(config);
            ComprehensiveWorkflowState state = snapshot.state();
            response.put("success", true);
            response.put("threadId", threadId);
            response.put("currentNode", snapshot.node());
            response.put("intent", state.intent());
            response.put("messages", state.messages());
            response.put("executionLog", state.executionLog());
            response.put("executionEvents", events);
            response.put("stepCount", state.stepCount());
            boolean waiting = state.requiresApproval() && "waiting".equals(state.approvalStatus());
            response.put("waitingForApproval", waiting);
            response.put("approvalStatus", state.approvalStatus());
            response.put("workflowStatus", state.workflowStatus());
            response.put("finalResult", state.finalResult());
            response.put("message", waiting ? "⚠️ 等待人工审批" : "✅ 完成");
        } catch (Exception e) {
            log.error("HITL启动失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/memory/{threadId}")
    public Map<String, Object> getMemory(@PathVariable String threadId) {
        log.info("📚 获取记忆: {}", threadId);
        Map<String, Object> response = new HashMap<>();
        try {
            var compiled = workflowGraph.compileWithHumanInLoop(checkpointSaver);
            var config = RunnableConfig.builder().threadId(threadId).build();
            var snapshot = compiled.getState(config);
            if (snapshot == null || snapshot.state() == null) {
                response.put("success", false);
                response.put("error", "该ThreadId没有记忆");
                return response;
            }
            ComprehensiveWorkflowState state = snapshot.state();
            response.put("success", true);
            response.put("threadId", threadId);
            response.put("snapshot", Map.of("currentNode", snapshot.node(), "nextNode", snapshot.next()));
            response.put("state", Map.of(
                    "userInput", state.userInput(),
                    "intent", state.intent(),
                    "currentStep", state.currentStep(),
                    "stepCount", state.stepCount(),
                    "approvalStatus", state.approvalStatus(),
                    "workflowStatus", state.workflowStatus(),
                    "healthCategory", state.healthCategory(),
                    "healthAdvice", state.healthAdvice()
            ));
            response.put("appendedChannels", Map.of(
                    "messages", state.messages(),
                    "executionLog", state.executionLog(),
                    "toolResults", state.toolResults()
            ));
            String statusDesc = "waiting".equals(state.approvalStatus()) ? "🟡 等待审批" :
                    "completed".equals(state.workflowStatus()) ? "🟢 已完成" : "🔵 进行中";
            response.put("statusDescription", statusDesc);
        } catch (Exception e) {
            log.error("获取记忆失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/continue-with-thread")
    public Map<String, Object> continueWithThread(@RequestBody Map<String, String> request) {
        String threadId = request.getOrDefault("threadId", "");
        String decision = request.getOrDefault("decision", "reject");
        log.info("📝 继续执行: threadId={}, decision={}", threadId, decision);
        Map<String, Object> response = new HashMap<>();
        try {
            var compiled = workflowGraph.compileWithHumanInLoop(checkpointSaver);
            var config = RunnableConfig.builder().threadId(threadId).build();
            var current = compiled.getState(config);
            if (current == null || current.state() == null) {
                response.put("success", false);
                response.put("error", "该ThreadId没有记忆");
                return response;
            }
            var updateConfig = compiled.updateState(config,
                    Map.of(ComprehensiveWorkflowState.HUMAN_FEEDBACK, decision), null);
            List<Map<String, Object>> events = new ArrayList<>();
            for (var nodeOutput : compiled.stream(GraphInput.resume(), updateConfig)) {
                Map<String, Object> evt = new HashMap<>();
                evt.put("node", nodeOutput.node());
                evt.put("step", nodeOutput.state().currentStep());
                events.add(evt);
            }
            var finalSnapshot = compiled.getState(config);
            ComprehensiveWorkflowState state = finalSnapshot.state();
            response.put("success", true);
            response.put("threadId", threadId);
            response.put("decision", decision);
            response.put("approvalStatus", state.approvalStatus());
            response.put("workflowStatus", state.workflowStatus());
            response.put("finalResult", state.finalResult());
            response.put("messages", state.messages());
            response.put("executionLog", state.executionLog());
            response.put("resumeEvents", events);
            response.put("totalSteps", state.stepCount());
        } catch (Exception e) {
            log.error("继续执行失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    // ========== 多智能体 API ==========
    
    /**
     * 多智能体同步执行
     */
    @PostMapping("/multi-agent/invoke")
    public Map<String, Object> multiAgentInvoke(@RequestBody Map<String, String> request) {
        String userInput = request.getOrDefault("userInput", "");
        log.info("🤖 多智能体执行: {}", userInput);
        Map<String, Object> response = new HashMap<>();
        try {
            var compiled = multiAgentGraph.compile();
            var result = compiled.invoke(Map.of(
                    ComprehensiveWorkflowState.USER_INPUT, userInput
            ));
            ComprehensiveWorkflowState state = result.get();
            response.put("success", true);
            response.put("intent", state.intent());
            response.put("messages", state.messages());
            response.put("executionLog", state.executionLog());
            response.put("toolResults", state.toolResults());
            response.put("finalResult", state.finalResult());
            response.put("workflowStatus", state.workflowStatus());
            response.put("totalSteps", state.stepCount());
        } catch (Exception e) {
            log.error("多智能体执行失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 多智能体流式执行
     */
    @GetMapping(value = "/multi-agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> multiAgentStream(@RequestParam String userInput) {
        log.info("🤖🌊 多智能体流式执行: {}", userInput);
        return Flux.create(sink -> {
            try {
                var compiled = multiAgentGraph.compile();
                for (var nodeOutput : compiled.stream(Map.of(
                        ComprehensiveWorkflowState.USER_INPUT, userInput))) {
                    ComprehensiveWorkflowState state = nodeOutput.state();
                    Map<String, Object> data = new HashMap<>();
                    data.put("node", nodeOutput.node());
                    data.put("currentStep", state.currentStep());
                    data.put("intent", state.intent());
                    data.put("stepCount", state.stepCount());
                    data.put("messages", state.messages());
                    data.put("toolResults", state.toolResults());
                    sink.next(data);
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    
    // ========== 并行多智能体 API ==========
    
    /**
     * 并行多智能体执行 - 所有Agent同时执行
     */
    @PostMapping("/parallel-agent/invoke")
    public Map<String, Object> parallelAgentInvoke(@RequestBody Map<String, String> request) {
        String userInput = request.getOrDefault("userInput", "");
        log.info("⚡ 并行多智能体执行: {}", userInput);
        Map<String, Object> response = new HashMap<>();
        try {
            var compiled = parallelAgentGraph.compile();
            var result = compiled.invoke(Map.of(
                    ComprehensiveWorkflowState.USER_INPUT, userInput
            ));
            ComprehensiveWorkflowState state = result.get();
            response.put("success", true);
            response.put("intent", state.intent());
            response.put("messages", state.messages());
            response.put("executionLog", state.executionLog());
            response.put("toolResults", state.toolResults());
            response.put("finalResult", state.finalResult());
            response.put("workflowStatus", state.workflowStatus());
            response.put("totalSteps", state.stepCount());
            response.put("mode", "parallel");
        } catch (Exception e) {
            log.error("并行多智能体执行失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 并行多智能体流式执行
     */
    @GetMapping(value = "/parallel-agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> parallelAgentStream(@RequestParam String userInput) {
        log.info("⚡🌊 并行多智能体流式执行: {}", userInput);
        return Flux.create(sink -> {
            try {
                var compiled = parallelAgentGraph.compile();
                for (var nodeOutput : compiled.stream(Map.of(
                        ComprehensiveWorkflowState.USER_INPUT, userInput))) {
                    ComprehensiveWorkflowState state = nodeOutput.state();
                    Map<String, Object> data = new HashMap<>();
                    data.put("node", nodeOutput.node());
                    data.put("currentStep", state.currentStep());
                    data.put("intent", state.intent());
                    data.put("stepCount", state.stepCount());
                    data.put("messages", state.messages());
                    data.put("toolResults", state.toolResults());
                    data.put("executionLog", state.executionLog());
                    data.put("mode", "parallel");
                    sink.next(data);
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    
    // ========== Adaptive RAG API ==========
    
    /**
     * Adaptive RAG 同步执行
     */
    @PostMapping("/adaptive-rag/invoke")
    public Map<String, Object> adaptiveRagInvoke(@RequestBody Map<String, String> request) {
        String userInput = request.getOrDefault("userInput", "");
        log.info("📖 Adaptive RAG 执行: {}", userInput);
        Map<String, Object> response = new HashMap<>();
        try {
            var compiled = adaptiveRAGGraph.compile();
            var result = compiled.invoke(Map.of(
                    ComprehensiveWorkflowState.USER_INPUT, userInput
            ));
            ComprehensiveWorkflowState state = result.get();
            response.put("success", true);
            response.put("intent", state.intent());
            response.put("messages", state.messages());
            response.put("executionLog", state.executionLog());
            response.put("toolResults", state.toolResults());
            response.put("finalResult", state.finalResult());
            response.put("workflowStatus", state.workflowStatus());
            response.put("totalSteps", state.stepCount());
            response.put("mode", "adaptive-rag");
        } catch (Exception e) {
            log.error("Adaptive RAG 执行失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * Adaptive RAG 流式执行
     */
    @GetMapping(value = "/adaptive-rag/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> adaptiveRagStream(@RequestParam String userInput) {
        log.info("📖🌊 Adaptive RAG 流式执行: {}", userInput);
        return Flux.create(sink -> {
            try {
                var compiled = adaptiveRAGGraph.compile();
                for (var nodeOutput : compiled.stream(Map.of(
                        ComprehensiveWorkflowState.USER_INPUT, userInput))) {
                    ComprehensiveWorkflowState state = nodeOutput.state();
                    Map<String, Object> data = new HashMap<>();
                    data.put("node", nodeOutput.node());
                    data.put("currentStep", state.currentStep());
                    data.put("intent", state.intent());
                    data.put("stepCount", state.stepCount());
                    data.put("messages", state.messages());
                    data.put("toolResults", state.toolResults());
                    data.put("executionLog", state.executionLog());
                    data.put("mode", "adaptive-rag");
                    sink.next(data);
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}
