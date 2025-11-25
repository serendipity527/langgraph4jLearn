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
 * 多智能体协作图 - 展示 Supervisor 模式
 * 
 * 架构:
 *   ┌──────────────────────────────────────────┐
 *   │           Supervisor Agent               │
 *   │    (分析任务 -> 分发给专业智能体)           │
 *   └─────────────────┬────────────────────────┘
 *                     │ 条件路由
 *     ┌───────────────┼───────────────┐
 *     ▼               ▼               ▼
 * ┌────────┐    ┌──────────┐    ┌──────────┐
 * │Research│    │ Analyst  │    │ Writer   │
 * │ Agent  │    │  Agent   │    │  Agent   │
 * │(搜索)  │    │ (分析)   │    │ (撰写)   │
 * └────┬───┘    └────┬─────┘    └────┬─────┘
 *      │             │               │
 *      └─────────────┼───────────────┘
 *                    ▼
 *              ┌──────────┐
 *              │Aggregator│
 *              │ (汇总)   │
 *              └────┬─────┘
 *                   ▼
 *              ┌──────────┐
 *              │   END    │
 *              └──────────┘
 * 
 * 特点:
 * - 每个Agent有独立的"思考"和"执行"过程
 * - Supervisor可以多次分发任务(循环)
 * - Agent之间通过共享State通信
 */
@Slf4j
public class MultiAgentGraph {
    
    // 节点名称
    public static final String SUPERVISOR = "supervisor";
    public static final String RESEARCH_AGENT = "research_agent";
    public static final String ANALYST_AGENT = "analyst_agent";
    public static final String WRITER_AGENT = "writer_agent";
    public static final String AGGREGATOR = "aggregator";
    
    // 状态字段
    public static final String AGENT_TASK = "agent_task";
    public static final String AGENT_OUTPUTS = "agent_outputs";
    public static final String COMPLETED_AGENTS = "completed_agents";
    public static final String PENDING_AGENTS = "pending_agents";
    
    private StateGraph<ComprehensiveWorkflowState> stateGraph;
    
    public MultiAgentGraph() {
        buildGraph();
    }
    
    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    // ========== Supervisor Agent ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> supervisorNode = node_async(state -> {
        log.info("👔 [Supervisor] 分析任务并分配智能体");
        String input = state.userInput().toLowerCase();
        List<String> pendingAgents = new ArrayList<>();
        String task = "";
        
        // 根据输入分析需要哪些智能体
        if (input.contains("研究") || input.contains("research") || input.contains("搜索") || input.contains("查找")) {
            pendingAgents.add("research");
            task = "research";
        }
        if (input.contains("分析") || input.contains("analyze") || input.contains("数据") || input.contains("统计")) {
            pendingAgents.add("analyst");
            task = "analyst";
        }
        if (input.contains("写") || input.contains("write") || input.contains("报告") || input.contains("文章")) {
            pendingAgents.add("writer");
            task = "writer";
        }
        
        // 如果包含"全部"或没有明确指定,则启动所有智能体
        if (input.contains("全部") || input.contains("all") || pendingAgents.isEmpty()) {
            pendingAgents = Arrays.asList("research", "analyst", "writer");
            task = "all";
        }
        
        // 如果只有一个任务，直接使用
        if (pendingAgents.size() == 1) {
            task = pendingAgents.get(0);
        } else if (pendingAgents.size() > 1) {
            task = "multi"; // 多智能体协作
        }
        
        log.info("👔 [Supervisor] 任务类型: {}, 分配智能体: {}", task, pendingAgents);
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, SUPERVISOR,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.INTENT, "multi_agent:" + task,
                AGENT_TASK, task,
                ComprehensiveWorkflowState.MESSAGES, "👔 [Supervisor] 任务分析完成，分配给: " + pendingAgents,
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Supervisor: 分配 " + pendingAgents
        );
    });
    
    // ========== Research Agent ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> researchNode = node_async(state -> {
        log.info("🔍 [Research Agent] 执行搜索任务");
        
        // 模拟搜索过程
        String searchResult = "【Research Agent 搜索结果】\n" +
                "- 找到相关文献 15 篇\n" +
                "- 发现关键数据源 3 个\n" +
                "- 整理关键词: AI, LangGraph, Multi-Agent";
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, RESEARCH_AGENT,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.TOOL_RESULTS, "🔍 Research: " + searchResult,
                ComprehensiveWorkflowState.MESSAGES, "🔍 [Research Agent] 搜索完成",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Research Agent: 完成搜索"
        );
    });
    
    // ========== Analyst Agent ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> analystNode = node_async(state -> {
        log.info("📊 [Analyst Agent] 执行分析任务");
        
        // 模拟分析过程
        String analysisResult = "【Analyst Agent 分析结果】\n" +
                "- 数据趋势: 上升 23%\n" +
                "- 关键洞察: 多智能体协作效率提升明显\n" +
                "- 风险评估: 低风险";
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, ANALYST_AGENT,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.TOOL_RESULTS, "📊 Analyst: " + analysisResult,
                ComprehensiveWorkflowState.MESSAGES, "📊 [Analyst Agent] 分析完成",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Analyst Agent: 完成分析"
        );
    });
    
    // ========== Writer Agent ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> writerNode = node_async(state -> {
        log.info("✍️ [Writer Agent] 执行撰写任务");
        
        // 模拟撰写过程，整合其他Agent的结果
        List<String> results = state.toolResults();
        StringBuilder report = new StringBuilder("【Writer Agent 报告】\n");
        report.append("=== 综合报告 ===\n");
        report.append("基于团队协作完成以下工作:\n");
        for (String r : results) {
            if (r.startsWith("🔍") || r.startsWith("📊")) {
                report.append("- 引用: ").append(r.substring(0, Math.min(50, r.length()))).append("...\n");
            }
        }
        report.append("结论: 多智能体协作任务成功完成！");
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, WRITER_AGENT,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.TOOL_RESULTS, "✍️ Writer: " + report,
                ComprehensiveWorkflowState.MESSAGES, "✍️ [Writer Agent] 报告撰写完成",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Writer Agent: 完成撰写"
        );
    });
    
    // ========== Aggregator ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> aggregatorNode = node_async(state -> {
        log.info("📋 [Aggregator] 汇总所有智能体结果");
        
        List<String> results = state.toolResults();
        StringBuilder summary = new StringBuilder("=== 多智能体协作摘要 ===\n");
        summary.append("任务: ").append(state.userInput()).append("\n");
        summary.append("参与智能体: ").append(state.intent().replace("multi_agent:", "")).append("\n");
        summary.append("总步数: ").append(state.stepCount() + 1).append("\n");
        summary.append("--- 各智能体输出 ---\n");
        for (String r : results) {
            summary.append(r).append("\n");
        }
        summary.append("=== 协作完成 ===");
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, AGGREGATOR,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.WORKFLOW_STATUS, "completed",
                ComprehensiveWorkflowState.FINAL_RESULT, summary.toString(),
                ComprehensiveWorkflowState.MESSAGES, "📋 [Aggregator] 汇总完成，多智能体协作成功！",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Aggregator: 汇总完成"
        );
    });
    
    // ========== 路由函数 ==========
    private EdgeAction<ComprehensiveWorkflowState> routeByTask() {
        return state -> {
            String task = state.<String>value(AGENT_TASK).orElse("all");
            log.info("🔀 [Router] 任务路由: {}", task);
            return task;
        };
    }
    
    // ========== 构建图 ==========
    private void buildGraph() {
        try {
            log.info("🏗️ 构建多智能体协作图...");
            
            this.stateGraph = new StateGraph<>(ComprehensiveWorkflowState.SCHEMA, ComprehensiveWorkflowState::new);
            
            // 添加节点
            stateGraph.addNode(SUPERVISOR, supervisorNode);
            stateGraph.addNode(RESEARCH_AGENT, researchNode);
            stateGraph.addNode(ANALYST_AGENT, analystNode);
            stateGraph.addNode(WRITER_AGENT, writerNode);
            stateGraph.addNode(AGGREGATOR, aggregatorNode);
            
            // 定义边
            stateGraph.addEdge(START, SUPERVISOR);
            
            // Supervisor 条件路由到不同智能体
            stateGraph.addConditionalEdges(SUPERVISOR, edge_async(routeByTask()),
                    Map.of(
                            "research", RESEARCH_AGENT,
                            "analyst", ANALYST_AGENT,
                            "writer", WRITER_AGENT,
                            "all", RESEARCH_AGENT,     // 全部时从Research开始
                            "multi", RESEARCH_AGENT   // 多智能体时从Research开始
                    ));
            
            // 智能体完成后的路由
            // Research -> Analyst (如果是多智能体) 或 -> Aggregator
            stateGraph.addConditionalEdges(RESEARCH_AGENT, edge_async(state -> {
                String task = state.<String>value(AGENT_TASK).orElse("");
                if ("all".equals(task) || "multi".equals(task)) {
                    return "next_analyst";
                }
                return "finish";
            }), Map.of("next_analyst", ANALYST_AGENT, "finish", AGGREGATOR));
            
            // Analyst -> Writer (如果是多智能体) 或 -> Aggregator
            stateGraph.addConditionalEdges(ANALYST_AGENT, edge_async(state -> {
                String task = state.<String>value(AGENT_TASK).orElse("");
                if ("all".equals(task) || "multi".equals(task)) {
                    return "next_writer";
                }
                return "finish";
            }), Map.of("next_writer", WRITER_AGENT, "finish", AGGREGATOR));
            
            // Writer -> Aggregator
            stateGraph.addEdge(WRITER_AGENT, AGGREGATOR);
            
            // Aggregator -> END
            stateGraph.addEdge(AGGREGATOR, END);
            
            log.info("✅ 多智能体协作图构建完成");
        } catch (Exception e) {
            log.error("❌ 构建多智能体图失败", e);
            throw new RuntimeException("构建多智能体图失败", e);
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
}
