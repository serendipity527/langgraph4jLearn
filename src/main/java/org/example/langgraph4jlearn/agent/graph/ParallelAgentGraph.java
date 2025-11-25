package org.example.langgraph4jlearn.agent.graph;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.example.langgraph4jlearn.agent.state.ComprehensiveWorkflowState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 并行多智能体协作图 - 展示 Parallel 模式
 * 
 * 架构:
 *   ┌──────────────────────────────────────────┐
 *   │           Dispatcher (分发器)             │
 *   │       (分析任务 -> 并行分发)              │
 *   └─────────────────┬────────────────────────┘
 *                     │ 并行执行 (Send API)
 *     ┌───────────────┼───────────────┐
 *     ▼               ▼               ▼
 * ┌────────┐    ┌──────────┐    ┌──────────┐
 * │Research│    │ Analyst  │    │ Writer   │
 * │ Agent  │    │  Agent   │    │  Agent   │
 * │(搜索)  │    │ (分析)   │    │ (撰写)   │
 * └────┬───┘    └────┬─────┘    └────┬─────┘
 *      │             │               │
 *      └─────────────┼───────────────┘
 *                    ▼ 等待所有完成
 *              ┌──────────┐
 *              │Collector │
 *              │ (收集)   │
 *              └────┬─────┘
 *                   ▼
 *              ┌──────────┐
 *              │   END    │
 *              └──────────┘
 * 
 * 特点:
 * - 使用Java并行流实现真正的并行执行
 * - 所有Agent同时启动，并行处理
 * - Collector等待所有Agent完成后汇总结果
 */
@Slf4j
public class ParallelAgentGraph {
    
    // 节点名称
    public static final String DISPATCHER = "dispatcher";
    public static final String PARALLEL_EXECUTOR = "parallel_executor";
    public static final String COLLECTOR = "collector";
    
    // 状态字段
    public static final String PARALLEL_RESULTS = "parallel_results";
    public static final String PARALLEL_START_TIME = "parallel_start_time";
    public static final String PARALLEL_END_TIME = "parallel_end_time";
    
    private StateGraph<ComprehensiveWorkflowState> stateGraph;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    
    public ParallelAgentGraph() {
        buildGraph();
    }
    
    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }
    
    // ========== Dispatcher (分发器) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> dispatcherNode = node_async(state -> {
        log.info("📤 [Dispatcher] 准备并行分发任务");
        String startTime = timestamp();
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, DISPATCHER,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.INTENT, "parallel_agent",
                PARALLEL_START_TIME, startTime,
                ComprehensiveWorkflowState.MESSAGES, "📤 [Dispatcher] 任务分发，启动并行执行",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + startTime + "] Dispatcher: 开始并行分发"
        );
    });
    
    // ========== Parallel Executor (并行执行器) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> parallelExecutorNode = node_async(state -> {
        log.info("⚡ [Parallel Executor] 并行执行所有Agent");
        
        String userInput = state.userInput();
        List<String> allResults = Collections.synchronizedList(new ArrayList<>());
        List<String> allLogs = Collections.synchronizedList(new ArrayList<>());
        List<String> allMessages = Collections.synchronizedList(new ArrayList<>());
        
        // 定义三个Agent的任务
        CompletableFuture<Void> researchFuture = CompletableFuture.runAsync(() -> {
            String time = timestamp();
            log.info("🔍 [Research Agent] 开始执行 @ {}", time);
            allLogs.add("[" + time + "] 🔍 Research Agent: 开始搜索");
            
            // 模拟搜索耗时
            try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            String result = "【Research Agent】找到相关文献15篇，数据源3个，关键词: AI, LangGraph";
            allResults.add("🔍 " + result);
            allMessages.add("🔍 [Research] 搜索完成");
            
            String endTime = timestamp();
            allLogs.add("[" + endTime + "] 🔍 Research Agent: 完成搜索");
            log.info("🔍 [Research Agent] 完成 @ {}", endTime);
        }, executorService);
        
        CompletableFuture<Void> analystFuture = CompletableFuture.runAsync(() -> {
            String time = timestamp();
            log.info("📊 [Analyst Agent] 开始执行 @ {}", time);
            allLogs.add("[" + time + "] 📊 Analyst Agent: 开始分析");
            
            // 模拟分析耗时
            try { Thread.sleep(600); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            String result = "【Analyst Agent】数据趋势上升23%，多智能体效率提升明显，风险评估: 低";
            allResults.add("📊 " + result);
            allMessages.add("📊 [Analyst] 分析完成");
            
            String endTime = timestamp();
            allLogs.add("[" + endTime + "] 📊 Analyst Agent: 完成分析");
            log.info("📊 [Analyst Agent] 完成 @ {}", endTime);
        }, executorService);
        
        CompletableFuture<Void> writerFuture = CompletableFuture.runAsync(() -> {
            String time = timestamp();
            log.info("✍️ [Writer Agent] 开始执行 @ {}", time);
            allLogs.add("[" + time + "] ✍️ Writer Agent: 开始撰写");
            
            // 模拟撰写耗时
            try { Thread.sleep(700); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            String result = "【Writer Agent】报告框架已完成，包含摘要、分析、结论三部分";
            allResults.add("✍️ " + result);
            allMessages.add("✍️ [Writer] 撰写完成");
            
            String endTime = timestamp();
            allLogs.add("[" + endTime + "] ✍️ Writer Agent: 完成撰写");
            log.info("✍️ [Writer Agent] 完成 @ {}", endTime);
        }, executorService);
        
        // 等待所有Agent完成
        CompletableFuture.allOf(researchFuture, analystFuture, writerFuture).join();
        
        String endTime = timestamp();
        allLogs.add("[" + endTime + "] ⚡ 所有Agent并行执行完成");
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put(ComprehensiveWorkflowState.CURRENT_STEP, PARALLEL_EXECUTOR);
        result.put(ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1);
        result.put(PARALLEL_END_TIME, endTime);
        
        // 添加所有结果到tool_results
        for (String r : allResults) {
            result.put(ComprehensiveWorkflowState.TOOL_RESULTS, r);
        }
        
        // 添加所有日志
        for (String l : allLogs) {
            result.put(ComprehensiveWorkflowState.EXECUTION_LOG, l);
        }
        
        // 添加所有消息
        for (String m : allMessages) {
            result.put(ComprehensiveWorkflowState.MESSAGES, m);
        }
        
        return result;
    });
    
    // ========== Collector (收集器) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> collectorNode = node_async(state -> {
        log.info("📋 [Collector] 收集并汇总所有并行结果");
        
        List<String> results = state.toolResults();
        String startTime = state.<String>value(PARALLEL_START_TIME).orElse("?");
        String endTime = timestamp();
        
        StringBuilder summary = new StringBuilder();
        summary.append("=== 并行多智能体协作摘要 ===\n");
        summary.append("任务: ").append(state.userInput()).append("\n");
        summary.append("执行模式: ⚡ 并行执行\n");
        summary.append("开始时间: ").append(startTime).append("\n");
        summary.append("结束时间: ").append(endTime).append("\n");
        summary.append("总步数: ").append(state.stepCount() + 1).append("\n");
        summary.append("\n--- 各Agent并行输出 ---\n");
        for (String r : results) {
            summary.append(r).append("\n");
        }
        summary.append("\n✅ 并行协作完成！所有Agent同时执行，效率最大化！");
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, COLLECTOR,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.WORKFLOW_STATUS, "completed",
                ComprehensiveWorkflowState.FINAL_RESULT, summary.toString(),
                ComprehensiveWorkflowState.MESSAGES, "📋 [Collector] 并行执行汇总完成！",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + endTime + "] Collector: 汇总完成"
        );
    });
    
    // ========== 构建图 ==========
    private void buildGraph() {
        try {
            log.info("🏗️ 构建并行多智能体图...");
            
            this.stateGraph = new StateGraph<>(ComprehensiveWorkflowState.SCHEMA, ComprehensiveWorkflowState::new);
            
            // 添加节点
            stateGraph.addNode(DISPATCHER, dispatcherNode);
            stateGraph.addNode(PARALLEL_EXECUTOR, parallelExecutorNode);
            stateGraph.addNode(COLLECTOR, collectorNode);
            
            // 定义边: START -> Dispatcher -> Parallel Executor -> Collector -> END
            stateGraph.addEdge(START, DISPATCHER);
            stateGraph.addEdge(DISPATCHER, PARALLEL_EXECUTOR);
            stateGraph.addEdge(PARALLEL_EXECUTOR, COLLECTOR);
            stateGraph.addEdge(COLLECTOR, END);
            
            log.info("✅ 并行多智能体图构建完成");
        } catch (Exception e) {
            log.error("❌ 构建并行多智能体图失败", e);
            throw new RuntimeException("构建并行多智能体图失败", e);
        }
    }
    
    public StateGraph<ComprehensiveWorkflowState> getGraph() {
        return this.stateGraph;
    }
    
    public CompiledGraph<ComprehensiveWorkflowState> compile() throws GraphStateException {
        return stateGraph.compile();
    }
    
    public CompiledGraph<ComprehensiveWorkflowState> compileWithCheckpoint(MemorySaver saver) throws GraphStateException {
        return stateGraph.compile(CompileConfig.builder()
                .checkpointSaver(saver)
                .build());
    }
    
    // 关闭执行器
    public void shutdown() {
        executorService.shutdown();
    }
}
