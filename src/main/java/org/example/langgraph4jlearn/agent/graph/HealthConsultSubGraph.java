package org.example.langgraph4jlearn.agent.graph;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.EdgeAction;
import org.example.langgraph4jlearn.agent.state.ComprehensiveWorkflowState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

@Slf4j
public class HealthConsultSubGraph {
    
    public static final String HEALTH_TRIAGE = "health_triage";
    public static final String SYMPTOM_ANALYSIS = "symptom_analysis";
    public static final String NUTRITION_ADVICE = "nutrition_advice";
    public static final String EXERCISE_PLAN = "exercise_plan";
    public static final String HEALTH_SUMMARY = "health_summary";
    
    private StateGraph<ComprehensiveWorkflowState> subGraph;
    
    public HealthConsultSubGraph() {
        buildSubGraph();
    }
    
    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> triageNode = node_async(state -> {
        log.info("🏥 [子图-triage] 健康分诊");
        String input = state.userInput().toLowerCase();
        String category;
        if (input.contains("头痛") || input.contains("发烧") || input.contains("症状")) {
            category = "symptom";
        } else if (input.contains("饮食") || input.contains("营养") || input.contains("吃")) {
            category = "nutrition";
        } else {
            category = "exercise";
        }
        log.info("🏥 分诊类型: {}", category);
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, "subgraph:" + HEALTH_TRIAGE,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.HEALTH_QUERY, state.userInput(),
                ComprehensiveWorkflowState.HEALTH_CATEGORY, category,
                ComprehensiveWorkflowState.MESSAGES, "🏥 [健康子图] 分诊: " + category,
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] 子图-triage: " + category
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> symptomNode = node_async(state -> {
        log.info("🩺 [子图-symptom] 症状分析");
        String advice = "症状建议: 1.多休息 2.多喝水 3.持续3天请就医";
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, "subgraph:" + SYMPTOM_ANALYSIS,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.HEALTH_ADVICE, advice,
                ComprehensiveWorkflowState.MESSAGES, "🩺 [健康子图] 症状分析完成",
                ComprehensiveWorkflowState.TOOL_RESULTS, "症状分析: " + state.healthQuery(),
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] 子图-symptom: 完成"
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> nutritionNode = node_async(state -> {
        log.info("🥗 [子图-nutrition] 营养建议");
        String advice = "营养建议: 1.均衡饮食 2.控糖控盐 3.每日饮水1500ml";
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, "subgraph:" + NUTRITION_ADVICE,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.HEALTH_ADVICE, advice,
                ComprehensiveWorkflowState.MESSAGES, "🥗 [健康子图] 营养建议完成",
                ComprehensiveWorkflowState.TOOL_RESULTS, "营养建议: " + state.healthQuery(),
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] 子图-nutrition: 完成"
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> exerciseNode = node_async(state -> {
        log.info("🏃 [子图-exercise] 运动计划");
        String advice = "运动计划: 1.每周3-5次有氧 2.适当力量训练 3.注意热身拉伸";
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, "subgraph:" + EXERCISE_PLAN,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.HEALTH_ADVICE, advice,
                ComprehensiveWorkflowState.MESSAGES, "🏃 [健康子图] 运动计划完成",
                ComprehensiveWorkflowState.TOOL_RESULTS, "运动计划: " + state.healthQuery(),
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] 子图-exercise: 完成"
        );
    });
    
    private final AsyncNodeAction<ComprehensiveWorkflowState> summaryNode = node_async(state -> {
        log.info("📋 [子图-summary] 健康总结");
        String summary = String.format("健康咨询总结 - 类型:%s, 建议:%s", 
                state.healthCategory(), state.healthAdvice());
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, "subgraph:" + HEALTH_SUMMARY,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.MESSAGES, "📋 [健康子图] 咨询完成",
                ComprehensiveWorkflowState.TOOL_RESULTS, summary,
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] 子图-summary: 完成"
        );
    });
    
    private EdgeAction<ComprehensiveWorkflowState> routeByCategory() {
        return state -> {
            String category = state.healthCategory();
            log.info("🔀 [子图路由] 类型: {}", category);
            return category;
        };
    }
    
    private void buildSubGraph() {
        try {
            log.info("🏗️ 构建健康咨询子图...");
            this.subGraph = new StateGraph<>(ComprehensiveWorkflowState.SCHEMA, ComprehensiveWorkflowState::new);
            
            subGraph.addNode(HEALTH_TRIAGE, triageNode);
            subGraph.addNode(SYMPTOM_ANALYSIS, symptomNode);
            subGraph.addNode(NUTRITION_ADVICE, nutritionNode);
            subGraph.addNode(EXERCISE_PLAN, exerciseNode);
            subGraph.addNode(HEALTH_SUMMARY, summaryNode);
            
            subGraph.addEdge(START, HEALTH_TRIAGE);
            subGraph.addConditionalEdges(HEALTH_TRIAGE, edge_async(routeByCategory()),
                    Map.of("symptom", SYMPTOM_ANALYSIS, "nutrition", NUTRITION_ADVICE, "exercise", EXERCISE_PLAN));
            subGraph.addEdge(SYMPTOM_ANALYSIS, HEALTH_SUMMARY);
            subGraph.addEdge(NUTRITION_ADVICE, HEALTH_SUMMARY);
            subGraph.addEdge(EXERCISE_PLAN, HEALTH_SUMMARY);
            subGraph.addEdge(HEALTH_SUMMARY, END);
            
            log.info("✅ 健康咨询子图构建完成");
        } catch (Exception e) {
            log.error("❌ 构建子图失败", e);
            throw new RuntimeException("构建健康咨询子图失败", e);
        }
    }
    
    public StateGraph<ComprehensiveWorkflowState> getSubGraph() {
        return this.subGraph;
    }
    
    public CompiledGraph<ComprehensiveWorkflowState> compile() throws GraphStateException {
        return subGraph.compile();
    }
}
