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
 * 综合工作流图
 * 
 * 本示例展示 LangGraph4j 的核心知识点：
 * 
 * 🧱 核心组件:
 * - StateGraph (状态图构建器)
 * - State (状态定义 / Schema)
 * - Node (节点 / 具体的执行逻辑)
 * - Edge (边 / 节点间的连接)
 * - CompiledGraph (编译后的图 / Runnable)
 * 
 * 🔄 流控制与路由:
 * - START (起始节点 / 入口)
 * - END (结束节点 / 出口)
 * - Normal Edge (普通边)
 * - Conditional Edge (条件边)
 * - Router / Condition (路由逻辑 / 决策函数)
 * - Cycle (循环 / 环路处理)
 * 
 * 💾 状态管理:
 * - State Schema (状态结构定义)
 * - Channels (通道)
 * - Reducer (归约器 / 状态合并策略)
 * - State Update (状态更新机制)
 * 
 * 🏃 运行与执行:
 * - compile() (编译方法)
 * - invoke() (同步调用)
 * - stream() (流式调用)
 * - Recursion Limit (递归深度限制)
 * 
 * 🧠 记忆与持久化:
 * - Checkpointer (检查点接口)
 * - MemorySaver (内存持久化实现)
 * - Thread ID / Config (会话/线程配置)
 * 
 * 🛑 人机交互:
 * - Interrupt (中断机制)
 * - interruptBefore (节点执行前中断)
 * - getSnapshot (获取当前快照)
 * - updateState (人工修改状态)
 * - Resume (恢复执行)
 * 
 * 🤖 代理模式:
 * - Tool Node (工具调用节点)
 * 
 * 工作流结构:
 * START → input_processor → intent_classifier ─┬→ safe_operation → result_aggregator → END
 *                                              │
 *                                              ├→ tool_executor → validation ─┬→ retry (循环回tool_executor)
 *                                              │                              │
 *                                              │                              └→ result_aggregator → END
 *                                              │
 *                                              └→ approval_request (人在回路中断) → approval_handler ─┬→ dangerous_operation → result_aggregator → END
 *                                                                                                   │
 *                                                                                                   └→ rejection_handler → END
 */
@Slf4j
public class ComprehensiveWorkflowGraph {
    
    // ========== 节点名称常量 ==========
    public static final String INPUT_PROCESSOR = "input_processor";
    public static final String INTENT_CLASSIFIER = "intent_classifier";
    public static final String SAFE_OPERATION = "safe_operation";
    public static final String TOOL_EXECUTOR = "tool_executor";
    public static final String VALIDATION = "validation";
    public static final String APPROVAL_REQUEST = "approval_request";
    public static final String APPROVAL_HANDLER = "approval_handler";
    public static final String DANGEROUS_OPERATION = "dangerous_operation";
    public static final String REJECTION_HANDLER = "rejection_handler";
    public static final String RESULT_AGGREGATOR = "result_aggregator";
    
    private StateGraph<ComprehensiveWorkflowState> stateGraph;
    
    public ComprehensiveWorkflowGraph() {
        buildGraph();
    }
    
    // ========== 节点实现 (Node - 具体的执行逻辑) ==========
    
    /**
     * 输入处理节点
     * 初始化工作流，处理用户输入
     */
    private final AsyncNodeAction<ComprehensiveWorkflowState> inputProcessorNode = node_async(state -> {
        log.info("📥 [input_processor] 处理用户输入: {}", state.userInput());
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, INPUT_PROCESSOR,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.MESSAGES, "系统: 收到用户输入 - " + state.userInput(),
                ComprehensiveWorkflowState.EXECUTION_LOG, String.format("[%s] input_processor: 开始处理输入", timestamp),
                ComprehensiveWorkflowState.WORKFLOW_STATUS, "processing"
        );
    });
    
    /**
     * 意图分类节点
     * 分析用户输入，确定意图类型
     */
    private final AsyncNodeAction<ComprehensiveWorkflowState> intentClassifierNode = node_async(state -> {
        log.info("🧠 [intent_classifier] 分析意图...");
        
        String input = state.userInput().toLowerCase();
        String intent;
        boolean requiresApproval;
        
        // 模拟意图分类逻辑
        if (input.contains("删除") || input.contains("delete") || input.contains("危险") || input.contains("dangerous")) {
            intent = "dangerous";
            requiresApproval = true;
        } else if (input.contains("工具") || input.contains("tool") || input.contains("计算") || input.contains("搜索")) {
            intent = "tool";
            requiresApproval = false;
        } else {
            intent = "safe";
            requiresApproval = false;
        }
        
        log.info("🎯 识别意图: {} (需要审批: {})", intent, requiresApproval);
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, INTENT_CLASSIFIER,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.INTENT, intent,
                ComprehensiveWorkflowState.REQUIRES_APPROVAL, requiresApproval,
                ComprehensiveWorkflowState.MESSAGES, String.format("系统: 意图识别完成 - %s", intent),
                ComprehensiveWorkflowState.EXECUTION_LOG, String.format("[%s] intent_classifier: 意图=%s", 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), intent)
        );
    });
    
    /**
     * 安全操作节点
     * 执行无风险的操作
     */
    private final AsyncNodeAction<ComprehensiveWorkflowState> safeOperationNode = node_async(state -> {
        log.info("✅ [safe_operation] 执行安全操作");
        
        // 模拟安全操作
        String result = "安全操作已完成: 查询结果 - " + state.userInput();
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, SAFE_OPERATION,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.TOOL_RESULTS, result,
                ComprehensiveWorkflowState.MESSAGES, "系统: " + result,
                ComprehensiveWorkflowState.EXECUTION_LOG, String.format("[%s] safe_operation: 执行完成",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        );
    });
    
    /**
     * 工具执行节点 (Tool Node)
     * 模拟工具调用
     */
    private final AsyncNodeAction<ComprehensiveWorkflowState> toolExecutorNode = node_async(state -> {
        log.info("🔧 [tool_executor] 执行工具调用 (第{}次尝试)", state.retryCount() + 1);
        
        // 模拟工具执行，可能失败需要重试
        Random random = new Random();
        boolean success = random.nextDouble() > 0.4; // 60% 成功率
        
        String toolResult;
        if (success) {
            toolResult = "工具执行成功: 处理了 \"" + state.userInput() + "\"";
        } else {
            toolResult = "工具执行遇到问题，需要验证";
        }
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, TOOL_EXECUTOR,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.RETRY_COUNT, state.retryCount() + 1,
                ComprehensiveWorkflowState.TOOL_RESULTS, toolResult,
                ComprehensiveWorkflowState.MESSAGES, "系统: " + toolResult,
                ComprehensiveWorkflowState.EXECUTION_LOG, String.format("[%s] tool_executor: 尝试#%d %s",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        state.retryCount() + 1,
                        success ? "成功" : "需重试")
        );
    });
    
    /**
     * 验证节点
     * 检查工具执行结果，决定是否需要重试 (实现 Cycle 循环)
     */
    private final AsyncNodeAction<ComprehensiveWorkflowState> validationNode = node_async(state -> {
        log.info("🔍 [validation] 验证工具执行结果");
        
        List<String> results = state.toolResults();
        String lastResult = results.isEmpty() ? "" : results.get(results.size() - 1);
        boolean needsRetry = lastResult.contains("需要验证") && state.retryCount() < state.maxRetries();
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, VALIDATION,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.MESSAGES, needsRetry ? 
                        String.format("系统: 验证失败，准备重试 (%d/%d)", state.retryCount(), state.maxRetries()) :
                        "系统: 验证通过",
                ComprehensiveWorkflowState.EXECUTION_LOG, String.format("[%s] validation: %s",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        needsRetry ? "需要重试" : "通过")
        );
    });
    
    /**
     * 审批请求节点 (Human-in-the-loop 中断点)
     * 请求人工审批危险操作
     */
    private final AsyncNodeAction<ComprehensiveWorkflowState> approvalRequestNode = node_async(state -> {
        log.info("⏸️ [approval_request] 请求人工审批");
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, APPROVAL_REQUEST,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.APPROVAL_STATUS, "waiting",
                ComprehensiveWorkflowState.MESSAGES, "⚠️ 系统: 检测到危险操作，需要人工审批。请确认是否继续执行？",
                ComprehensiveWorkflowState.EXECUTION_LOG, String.format("[%s] approval_request: 等待人工审批",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        );
    });
    
    /**
     * 审批处理节点
     * 处理人工审批结果
     */
    private final AsyncNodeAction<ComprehensiveWorkflowState> approvalHandlerNode = node_async(state -> {
        log.info("📋 [approval_handler] 处理审批结果");
        
        String feedback = state.humanFeedback().orElse("reject");
        String status = feedback.equalsIgnoreCase("approve") || 
                       feedback.equalsIgnoreCase("yes") ||
                       feedback.equalsIgnoreCase("同意") ? "approved" : "rejected";
        
        log.info("审批结果: {}", status);
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, APPROVAL_HANDLER,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.APPROVAL_STATUS, status,
                ComprehensiveWorkflowState.MESSAGES, "系统: 审批结果 - " + (status.equals("approved") ? "已批准 ✅" : "已拒绝 ❌"),
                ComprehensiveWorkflowState.EXECUTION_LOG, String.format("[%s] approval_handler: %s",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), status)
        );
    });
    
    /**
     * 危险操作执行节点
     * 只有在审批通过后才执行
     */
    private final AsyncNodeAction<ComprehensiveWorkflowState> dangerousOperationNode = node_async(state -> {
        log.info("⚡ [dangerous_operation] 执行危险操作");
        
        String result = "危险操作已执行: " + state.userInput() + " (已通过审批)";
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, DANGEROUS_OPERATION,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.TOOL_RESULTS, result,
                ComprehensiveWorkflowState.MESSAGES, "系统: " + result,
                ComprehensiveWorkflowState.EXECUTION_LOG, String.format("[%s] dangerous_operation: 执行完成",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        );
    });
    
    /**
     * 拒绝处理节点
     * 处理审批被拒绝的情况
     */
    private final AsyncNodeAction<ComprehensiveWorkflowState> rejectionHandlerNode = node_async(state -> {
        log.info("🚫 [rejection_handler] 操作被拒绝");
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, REJECTION_HANDLER,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.WORKFLOW_STATUS, "rejected",
                ComprehensiveWorkflowState.MESSAGES, "系统: 操作已被拒绝，工作流终止",
                ComprehensiveWorkflowState.FINAL_RESULT, "操作被用户拒绝",
                ComprehensiveWorkflowState.EXECUTION_LOG, String.format("[%s] rejection_handler: 操作被拒绝",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        );
    });
    
    /**
     * 结果聚合节点
     * 汇总所有执行结果
     */
    private final AsyncNodeAction<ComprehensiveWorkflowState> resultAggregatorNode = node_async(state -> {
        log.info("📊 [result_aggregator] 聚合结果");
        
        StringBuilder summary = new StringBuilder();
        summary.append("=== 工作流执行摘要 ===\n");
        summary.append("输入: ").append(state.userInput()).append("\n");
        summary.append("意图: ").append(state.intent()).append("\n");
        summary.append("总步骤数: ").append(state.stepCount() + 1).append("\n");
        summary.append("工具执行结果:\n");
        for (String result : state.toolResults()) {
            summary.append("  - ").append(result).append("\n");
        }
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, RESULT_AGGREGATOR,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.WORKFLOW_STATUS, "completed",
                ComprehensiveWorkflowState.FINAL_RESULT, summary.toString(),
                ComprehensiveWorkflowState.MESSAGES, "系统: 工作流执行完成 ✓",
                ComprehensiveWorkflowState.EXECUTION_LOG, String.format("[%s] result_aggregator: 工作流完成",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        );
    });
    
    // ========== 条件路由函数 (Router / Condition) ==========
    
    /**
     * 意图路由 - 根据意图决定下一个节点
     */
    private EdgeAction<ComprehensiveWorkflowState> routeByIntent() {
        return state -> {
            String intent = state.intent();
            log.info("🔀 [router] 根据意图路由: {}", intent);
            
            return switch (intent) {
                case "dangerous" -> "dangerous";
                case "tool" -> "tool";
                default -> "safe";
            };
        };
    }
    
    /**
     * 验证路由 - 决定是重试还是继续 (实现 Cycle)
     */
    private EdgeAction<ComprehensiveWorkflowState> routeAfterValidation() {
        return state -> {
            List<String> results = state.toolResults();
            String lastResult = results.isEmpty() ? "" : results.get(results.size() - 1);
            
            if (lastResult.contains("需要验证") && state.retryCount() < state.maxRetries()) {
                log.info("🔄 [router] 需要重试，当前重试次数: {}/{}", state.retryCount(), state.maxRetries());
                return "retry";
            }
            
            log.info("✅ [router] 验证通过或达到最大重试次数");
            return "continue";
        };
    }
    
    /**
     * 审批结果路由 - 根据审批结果决定下一步
     */
    private EdgeAction<ComprehensiveWorkflowState> routeByApprovalStatus() {
        return state -> {
            String status = state.approvalStatus();
            log.info("🔀 [router] 审批状态路由: {}", status);
            
            if ("approved".equals(status)) {
                return "approved";
            } else {
                return "rejected";
            }
        };
    }
    
    // ========== 图构建方法 ==========
    
    /**
     * 构建状态图
     * 展示 StateGraph、Node、Edge、Conditional Edge 的使用
     */
    private void buildGraph() {
        try {
            log.info("🏗️ 开始构建综合工作流图...");
            
            // 创建 StateGraph (状态图构建器)
            this.stateGraph = new StateGraph<>(
                    ComprehensiveWorkflowState.SCHEMA,  // State Schema
                    ComprehensiveWorkflowState::new     // State 构造函数
            );
            
            // 添加节点 (Node)
            stateGraph.addNode(INPUT_PROCESSOR, inputProcessorNode);
            stateGraph.addNode(INTENT_CLASSIFIER, intentClassifierNode);
            stateGraph.addNode(SAFE_OPERATION, safeOperationNode);
            stateGraph.addNode(TOOL_EXECUTOR, toolExecutorNode);
            stateGraph.addNode(VALIDATION, validationNode);
            stateGraph.addNode(APPROVAL_REQUEST, approvalRequestNode);
            stateGraph.addNode(APPROVAL_HANDLER, approvalHandlerNode);
            stateGraph.addNode(DANGEROUS_OPERATION, dangerousOperationNode);
            stateGraph.addNode(REJECTION_HANDLER, rejectionHandlerNode);
            stateGraph.addNode(RESULT_AGGREGATOR, resultAggregatorNode);
            
            // 定义边 (Edge)
            
            // 1. START → input_processor (普通边)
            stateGraph.addEdge(START, INPUT_PROCESSOR);
            
            // 2. input_processor → intent_classifier (普通边)
            stateGraph.addEdge(INPUT_PROCESSOR, INTENT_CLASSIFIER);
            
            // 3. intent_classifier → [safe|tool|dangerous] (条件边 - 根据意图路由)
            stateGraph.addConditionalEdges(
                    INTENT_CLASSIFIER,
                    edge_async(routeByIntent()),
                    Map.of(
                            "safe", SAFE_OPERATION,
                            "tool", TOOL_EXECUTOR,
                            "dangerous", APPROVAL_REQUEST
                    )
            );
            
            // 4. safe_operation → result_aggregator (普通边)
            stateGraph.addEdge(SAFE_OPERATION, RESULT_AGGREGATOR);
            
            // 5. tool_executor → validation (普通边)
            stateGraph.addEdge(TOOL_EXECUTOR, VALIDATION);
            
            // 6. validation → [retry|continue] (条件边 - 实现循环/Cycle)
            stateGraph.addConditionalEdges(
                    VALIDATION,
                    edge_async(routeAfterValidation()),
                    Map.of(
                            "retry", TOOL_EXECUTOR,        // 循环回 tool_executor
                            "continue", RESULT_AGGREGATOR
                    )
            );
            
            // 7. approval_request → approval_handler (普通边 - 人在回路中断后继续)
            stateGraph.addEdge(APPROVAL_REQUEST, APPROVAL_HANDLER);
            
            // 8. approval_handler → [approved|rejected] (条件边)
            stateGraph.addConditionalEdges(
                    APPROVAL_HANDLER,
                    edge_async(routeByApprovalStatus()),
                    Map.of(
                            "approved", DANGEROUS_OPERATION,
                            "rejected", REJECTION_HANDLER
                    )
            );
            
            // 9. dangerous_operation → result_aggregator (普通边)
            stateGraph.addEdge(DANGEROUS_OPERATION, RESULT_AGGREGATOR);
            
            // 10. rejection_handler → END (普通边)
            stateGraph.addEdge(REJECTION_HANDLER, END);
            
            // 11. result_aggregator → END (普通边)
            stateGraph.addEdge(RESULT_AGGREGATOR, END);
            
            log.info("✅ 综合工作流图构建完成");
            
        } catch (Exception e) {
            log.error("❌ 构建图失败", e);
            throw new RuntimeException("构建综合工作流图失败", e);
        }
    }
    
    /**
     * 获取状态图 (用于 Studio 可视化)
     */
    public StateGraph<ComprehensiveWorkflowState> getGraph() {
        return this.stateGraph;
    }
    
    /**
     * 编译图 - 不带检查点 (简单执行)
     */
    public CompiledGraph<ComprehensiveWorkflowState> compile() throws GraphStateException {
        return stateGraph.compile();
    }
    
    /**
     * 编译图 - 带检查点和人在回路支持
     * 在 approval_request 节点后中断，等待人工审批
     * 
     * @param checkpointSaver 检查点保存器 (MemorySaver)
     */
    public CompiledGraph<ComprehensiveWorkflowState> compileWithHumanInLoop(MemorySaver checkpointSaver) 
            throws GraphStateException {
        var compileConfig = CompileConfig.builder()
                .checkpointSaver(checkpointSaver)                  // Checkpointer
                .interruptAfter(APPROVAL_REQUEST)                  // interruptAfter - 在审批请求后中断
                .build();
        
        return stateGraph.compile(compileConfig);
    }
    
    /**
     * 编译图 - 带递归限制
     */
    public CompiledGraph<ComprehensiveWorkflowState> compileWithRecursionLimit(int limit) 
            throws GraphStateException {
        var compileConfig = CompileConfig.builder()
                .recursionLimit(limit)  // Recursion Limit
                .build();
        
        return stateGraph.compile(compileConfig);
    }
    
    /**
     * 简单执行 (invoke)
     */
    public ComprehensiveWorkflowState execute(String userInput) throws Exception {
        log.info("🚀 开始执行工作流 - 输入: {}", userInput);
        
        var compiledGraph = compile();
        
        Map<String, Object> initialState = Map.of(
                ComprehensiveWorkflowState.USER_INPUT, userInput,
                ComprehensiveWorkflowState.MAX_RETRIES, 3
        );
        
        var result = compiledGraph.invoke(initialState);
        return result.get();
    }
    
    /**
     * 流式执行 (stream)
     */
    public void executeStream(String userInput) throws Exception {
        log.info("🌊 开始流式执行工作流 - 输入: {}", userInput);
        
        var compiledGraph = compile();
        
        Map<String, Object> initialState = Map.of(
                ComprehensiveWorkflowState.USER_INPUT, userInput,
                ComprehensiveWorkflowState.MAX_RETRIES, 3
        );
        
        for (var nodeOutput : compiledGraph.stream(initialState)) {
            log.info("📤 节点输出: {}", nodeOutput);
        }
    }
}
