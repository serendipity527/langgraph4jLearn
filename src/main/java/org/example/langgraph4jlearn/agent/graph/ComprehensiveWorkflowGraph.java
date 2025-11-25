package org.example.langgraph4jlearn.agent.graph;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.EdgeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.example.langgraph4jlearn.agent.state.ComprehensiveWorkflowState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 综合工作流图 - 展示 LangGraph4j 核心知识点
 * 
 * 知识点:
 * - StateGraph, Node, Edge, Conditional Edge
 * - SubGraph (子图) - 健康咨询作为子图
 * - Checkpointer, Human-in-the-loop
 * - Cycle (循环)
 * 
 * 结构:
 * START -> input -> intent_classifier 
 *   -> safe -> result -> END
 *   -> tool -> validation [-> retry (Cycle)]  -> result -> END
 *   -> health -> [健康子图] -> result -> END
 *   -> dangerous -> approval (HITL中断) -> handler -> [exec|reject] -> END
 */
@Slf4j
public class ComprehensiveWorkflowGraph {
    
    public static final String INPUT_PROCESSOR = "input_processor";
    public static final String INTENT_CLASSIFIER = "intent_classifier";
    public static final String SAFE_OPERATION = "safe_operation";
    public static final String TOOL_EXECUTOR = "tool_executor";
    public static final String VALIDATION = "validation";
    public static final String HEALTH_SUBGRAPH = "health_subgraph";  // 子图节点
    public static final String APPROVAL_REQUEST = "approval_request";
    public static final String APPROVAL_HANDLER = "approval_handler";
    public static final String DANGEROUS_OPERATION = "dangerous_operation";
    public static final String REJECTION_HANDLER = "rejection_handler";
    public static final String RESULT_AGGREGATOR = "result_aggregator";
    
    private StateGraph<ComprehensiveWorkflowState> stateGraph;
    
    public ComprehensiveWorkflowGraph() {
        buildGraph();
    }
    
    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    // ========== 节点实现 ==========
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> inputNode = node_async(state -> {
        log.info("📥 [input] 处理输入: {}", state.userInput());
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, INPUT_PROCESSOR,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.MESSAGES, "系统: 收到输入 - " + state.userInput(),
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] input: 开始处理",
                ComprehensiveWorkflowState.WORKFLOW_STATUS, "processing"
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> intentNode = node_async(state -> {
        log.info("🧠 [intent] 分析意图...");
        String input = state.userInput().toLowerCase();
        String intent;
        boolean requiresApproval = false;
        
        if (input.contains("删除") || input.contains("delete") || input.contains("危险")) {
            intent = "dangerous";
            requiresApproval = true;
        } else if (input.contains("工具") || input.contains("tool") || input.contains("计算")) {
            intent = "tool";
        } else if (input.contains("健康") || input.contains("health") || input.contains("头痛") || 
                   input.contains("饮食") || input.contains("运动") || input.contains("营养")) {
            intent = "health";  // 新增健康意图
        } else {
            intent = "safe";
        }
        log.info("🎯 意图: {} (审批: {})", intent, requiresApproval);
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, INTENT_CLASSIFIER,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.INTENT, intent,
                ComprehensiveWorkflowState.REQUIRES_APPROVAL, requiresApproval,
                ComprehensiveWorkflowState.MESSAGES, "系统: 意图=" + intent,
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] intent: " + intent
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> safeNode = node_async(state -> {
        log.info("✅ [safe] 执行安全操作");
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, SAFE_OPERATION,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.TOOL_RESULTS, "安全操作完成: " + state.userInput(),
                ComprehensiveWorkflowState.MESSAGES, "系统: 安全操作完成",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] safe: 完成"
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> toolNode = node_async(state -> {
        log.info("🔧 [tool] 执行工具 (尝试 {})", state.retryCount() + 1);
        Random random = new Random();
        boolean success = random.nextDouble() > 0.4;
        String result = success ? "工具执行成功" : "工具执行需验证";
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, TOOL_EXECUTOR,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.RETRY_COUNT, state.retryCount() + 1,
                ComprehensiveWorkflowState.TOOL_RESULTS, result,
                ComprehensiveWorkflowState.MESSAGES, "系统: " + result,
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] tool: " + (success ? "成功" : "需重试")
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> validationNode = node_async(state -> {
        log.info("🔍 [validation] 验证结果");
        List<String> results = state.toolResults();
        String last = results.isEmpty() ? "" : results.get(results.size() - 1);
        boolean needRetry = last.contains("需验证") && state.retryCount() < state.maxRetries();
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, VALIDATION,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.MESSAGES, needRetry ? "系统: 验证失败,重试" : "系统: 验证通过",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] validation: " + (needRetry ? "重试" : "通过")
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> approvalNode = node_async(state -> {
        log.info("⏸️ [approval] 请求审批");
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, APPROVAL_REQUEST,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.APPROVAL_STATUS, "waiting",
                ComprehensiveWorkflowState.MESSAGES, "⚠️ 系统: 危险操作,需人工审批",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] approval: 等待审批"
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> handlerNode = node_async(state -> {
        log.info("📋 [handler] 处理审批");
        String feedback = state.humanFeedback().orElse("reject");
        String status = feedback.equalsIgnoreCase("approve") ? "approved" : "rejected";
        log.info("审批结果: {}", status);
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, APPROVAL_HANDLER,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.APPROVAL_STATUS, status,
                ComprehensiveWorkflowState.MESSAGES, "系统: 审批" + (status.equals("approved") ? "通过✅" : "拒绝❌"),
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] handler: " + status
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> dangerousNode = node_async(state -> {
        log.info("⚡ [dangerous] 执行危险操作");
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, DANGEROUS_OPERATION,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.TOOL_RESULTS, "危险操作已执行(已审批)",
                ComprehensiveWorkflowState.MESSAGES, "系统: 危险操作执行完成",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] dangerous: 完成"
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> rejectNode = node_async(state -> {
        log.info("🚫 [reject] 操作被拒绝");
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, REJECTION_HANDLER,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.WORKFLOW_STATUS, "rejected",
                ComprehensiveWorkflowState.MESSAGES, "系统: 操作被拒绝",
                ComprehensiveWorkflowState.FINAL_RESULT, "操作被用户拒绝",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] reject: 拒绝"
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> resultNode = node_async(state -> {
        log.info("📊 [result] 聚合结果");
        StringBuilder summary = new StringBuilder("=== 工作流摘要 ===\n");
        summary.append("输入: ").append(state.userInput()).append("\n");
        summary.append("意图: ").append(state.intent()).append("\n");
        summary.append("步骤: ").append(state.stepCount() + 1).append("\n");
        for (String r : state.toolResults()) {
            summary.append("结果: ").append(r).append("\n");
        }
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, RESULT_AGGREGATOR,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.WORKFLOW_STATUS, "completed",
                ComprehensiveWorkflowState.FINAL_RESULT, summary.toString(),
                ComprehensiveWorkflowState.MESSAGES, "系统: 工作流完成 ✓",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] result: 完成"
        );
    });
    
    // ========== 路由函数 ==========
    
    private EdgeAction<ComprehensiveWorkflowState> routeByIntent() {
        return state -> {
            String intent = state.intent();
            log.info("🔀 [router] 意图路由: {}", intent);
            return intent;
        };
    }
    
    private EdgeAction<ComprehensiveWorkflowState> routeAfterValidation() {
        return state -> {
            List<String> results = state.toolResults();
            String last = results.isEmpty() ? "" : results.get(results.size() - 1);
            if (last.contains("需验证") && state.retryCount() < state.maxRetries()) {
                log.info("🔄 [router] 重试 {}/{}", state.retryCount(), state.maxRetries());
                return "retry";
            }
            return "continue";
        };
    }
    
    private EdgeAction<ComprehensiveWorkflowState> routeByApproval() {
        return state -> state.approvalStatus().equals("approved") ? "approved" : "rejected";
    }
    
    // ========== 构建图 ==========
    
    private void buildGraph() {
        try {
            log.info("🏗️ 构建综合工作流图(含子图)...");
            
            // 创建健康咨询子图并编译
            HealthConsultSubGraph healthSubGraph = new HealthConsultSubGraph();
            CompiledGraph<ComprehensiveWorkflowState> compiledHealthSubGraph = healthSubGraph.compile();
            
            this.stateGraph = new StateGraph<>(ComprehensiveWorkflowState.SCHEMA, ComprehensiveWorkflowState::new);
            
            // 添加普通节点
            stateGraph.addNode(INPUT_PROCESSOR, inputNode);
            stateGraph.addNode(INTENT_CLASSIFIER, intentNode);
            stateGraph.addNode(SAFE_OPERATION, safeNode);
            stateGraph.addNode(TOOL_EXECUTOR, toolNode);
            stateGraph.addNode(VALIDATION, validationNode);
            stateGraph.addNode(APPROVAL_REQUEST, approvalNode);
            stateGraph.addNode(APPROVAL_HANDLER, handlerNode);
            stateGraph.addNode(DANGEROUS_OPERATION, dangerousNode);
            stateGraph.addNode(REJECTION_HANDLER, rejectNode);
            stateGraph.addNode(RESULT_AGGREGATOR, resultNode);
            
            // ⭐ 添加子图作为节点 (SubGraph)
            stateGraph.addNode(HEALTH_SUBGRAPH, compiledHealthSubGraph);
            log.info("✅ 健康咨询子图已添加为节点: {}", HEALTH_SUBGRAPH);
            
            // 定义边
            stateGraph.addEdge(START, INPUT_PROCESSOR);
            stateGraph.addEdge(INPUT_PROCESSOR, INTENT_CLASSIFIER);
            
            // 条件路由 - 4个分支: safe, tool, health(子图), dangerous
            stateGraph.addConditionalEdges(INTENT_CLASSIFIER, edge_async(routeByIntent()),
                    Map.of("safe", SAFE_OPERATION, 
                           "tool", TOOL_EXECUTOR, 
                           "health", HEALTH_SUBGRAPH,  // 子图分支
                           "dangerous", APPROVAL_REQUEST));
            
            stateGraph.addEdge(SAFE_OPERATION, RESULT_AGGREGATOR);
            stateGraph.addEdge(TOOL_EXECUTOR, VALIDATION);
            
            // Cycle: validation -> retry -> tool_executor
            stateGraph.addConditionalEdges(VALIDATION, edge_async(routeAfterValidation()),
                    Map.of("retry", TOOL_EXECUTOR, "continue", RESULT_AGGREGATOR));
            
            // 子图完成后 -> 结果聚合
            stateGraph.addEdge(HEALTH_SUBGRAPH, RESULT_AGGREGATOR);
            
            // HITL 分支
            stateGraph.addEdge(APPROVAL_REQUEST, APPROVAL_HANDLER);
            stateGraph.addConditionalEdges(APPROVAL_HANDLER, edge_async(routeByApproval()),
                    Map.of("approved", DANGEROUS_OPERATION, "rejected", REJECTION_HANDLER));
            stateGraph.addEdge(DANGEROUS_OPERATION, RESULT_AGGREGATOR);
            stateGraph.addEdge(REJECTION_HANDLER, END);
            stateGraph.addEdge(RESULT_AGGREGATOR, END);
            
            log.info("✅ 综合工作流图构建完成(含子图)");
        } catch (Exception e) {
            log.error("❌ 构建图失败", e);
            throw new RuntimeException("构建工作流图失败", e);
        }
    }
    
    public StateGraph<ComprehensiveWorkflowState> getGraph() {
        return this.stateGraph;
    }
    
    public CompiledGraph<ComprehensiveWorkflowState> compile() throws GraphStateException {
        return stateGraph.compile();
    }
    
    public CompiledGraph<ComprehensiveWorkflowState> compileWithHumanInLoop(MemorySaver saver) throws GraphStateException {
        return stateGraph.compile(CompileConfig.builder()
                .checkpointSaver(saver)
                .interruptAfter(APPROVAL_REQUEST)
                .build());
    }
}
