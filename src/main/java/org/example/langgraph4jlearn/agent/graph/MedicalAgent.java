package org.example.langgraph4jlearn.agent.graph;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.action.EdgeAction;
import org.example.langgraph4jlearn.agent.node.medical.*;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 医疗健康多智能体图
 * 
 * 图结构：
 * START -> routing -> [consultation|drugInfo|healthPlan] -> riskControl 
 *       -> resultSummary -> [doctorRec|drugPurchase|healthCare] -> END
 */
@Slf4j
public class MedicalAgent {
    
    // 节点名称常量
    private static final String ROUTING = "routing";
    private static final String CONSULTATION = "consultation";
    private static final String DRUG_INFO = "drugInfo";
    private static final String HEALTH_PLAN = "healthPlan";
    private static final String RISK_CONTROL = "riskControl";
    private static final String RESULT_SUMMARY = "resultSummary";
    private static final String DOCTOR_REC = "doctorRecommendation";
    private static final String DRUG_PURCHASE = "drugPurchase";
    private static final String HEALTH_CARE = "healthCare";
    
    // 节点实例
    private final RoutingDiagnosisNode routingNode;
    private final ConsultationNode consultationNode;
    private final DrugInfoNode drugInfoNode;
    private final HealthPlanNode healthPlanNode;
    private final RiskControlNode riskControlNode;
    private final ResultSummaryNode resultSummaryNode;
    private final DoctorRecommendationNode doctorRecNode;
    private final DrugPurchaseNode drugPurchaseNode;
    private final HealthCareNode healthCareNode;
    
    private StateGraph<MedicalAgentState> graph;
    
    public MedicalAgent() {
        // 初始化所有节点
        this.routingNode = new RoutingDiagnosisNode();
        this.consultationNode = new ConsultationNode();
        this.drugInfoNode = new DrugInfoNode();
        this.healthPlanNode = new HealthPlanNode();
        this.riskControlNode = new RiskControlNode();
        this.resultSummaryNode = new ResultSummaryNode();
        this.doctorRecNode = new DoctorRecommendationNode();
        this.drugPurchaseNode = new DrugPurchaseNode();
        this.healthCareNode = new HealthCareNode();
        
        buildGraph();
    }
    
    /**
     * 构建医疗Agent图
     */
    private void buildGraph() {
        try {
            log.info("开始构建MedicalAgent图...");
            
            // 创建StateGraph实例
            this.graph = new StateGraph<>(
                    MedicalAgentState.SCHEMA,
                    MedicalAgentState::new
            );
            
            // 添加所有节点
            graph.addNode(ROUTING, node_async(routingNode));
            graph.addNode(CONSULTATION, node_async(consultationNode));
            graph.addNode(DRUG_INFO, node_async(drugInfoNode));
            graph.addNode(HEALTH_PLAN, node_async(healthPlanNode));
            graph.addNode(RISK_CONTROL, node_async(riskControlNode));
            graph.addNode(RESULT_SUMMARY, node_async(resultSummaryNode));
            graph.addNode(DOCTOR_REC, node_async(doctorRecNode));
            graph.addNode(DRUG_PURCHASE, node_async(drugPurchaseNode));
            graph.addNode(HEALTH_CARE, node_async(healthCareNode));
            
            // 定义边和条件路由
            
            // 1. START -> routing
            graph.addEdge(START, ROUTING);
            
            // 2. routing -> [consultation|drugInfo|healthPlan] (条件路由)
            graph.addConditionalEdges(
                    ROUTING,
                    edge_async(routeByIntent()),
                    Map.of(
                            "预问诊", CONSULTATION,
                            "药品", DRUG_INFO,
                            "健康计划", HEALTH_PLAN
                    )
            );
            
            // 3. 三个专业智能体 -> riskControl
            graph.addEdge(CONSULTATION, RISK_CONTROL);
            graph.addEdge(DRUG_INFO, RISK_CONTROL);
            graph.addEdge(HEALTH_PLAN, RISK_CONTROL);
            
            // 4. riskControl -> resultSummary
            graph.addEdge(RISK_CONTROL, RESULT_SUMMARY);
            
            // 5. resultSummary -> [doctorRec|drugPurchase|healthCare] (条件路由)
            graph.addConditionalEdges(
                    RESULT_SUMMARY,
                    edge_async(routeByConversionType()),
                    Map.of(
                            "医生推荐", DOCTOR_REC,
                            "药品购买", DRUG_PURCHASE,
                            "健康保健", HEALTH_CARE
                    )
            );
            
            // 6. 三个转化智能体 -> END
            graph.addEdge(DOCTOR_REC, END);
            graph.addEdge(DRUG_PURCHASE, END);
            graph.addEdge(HEALTH_CARE, END);
            
            log.info("MedicalAgent图构建完成");
            
        } catch (Exception e) {
            log.error("构建图失败", e);
            throw new RuntimeException("构建MedicalAgent图失败", e);
        }
    }
    
    /**
     * 根据意图路由的EdgeAction
     */
    private EdgeAction<MedicalAgentState> routeByIntent() {
        return state -> {
            String intent = state.intent();
            log.info("根据意图路由: {}", intent);
            
            if (intent == null || intent.isEmpty()) {
                return "预问诊"; // 默认路由
            }
            
            return intent;
        };
    }
    
    /**
     * 根据转化类型路由的EdgeAction
     */
    private EdgeAction<MedicalAgentState> routeByConversionType() {
        return state -> {
            String conversionType = state.conversionType();
            log.info("根据转化类型路由: {}", conversionType);
            
            if (conversionType == null || conversionType.isEmpty()) {
                return "健康保健"; // 默认路由
            }
            
            return conversionType;
        };
    }
    
    /**
     * 获取状态图（用于Studio可视化）
     */
    public StateGraph<MedicalAgentState> getGraph() {
        return this.graph;
    }
    
    /**
     * 执行Agent
     * 
     * @param userQuery 用户查询
     * @throws GraphStateException 图执行异常
     */
    public void execute(String userQuery) throws GraphStateException {
        log.info("=" .repeat(60));
        log.info("MedicalAgent开始执行");
        log.info("用户查询: {}", userQuery);
        log.info("=" .repeat(60));
        
        // 编译图
        var compiledGraph = graph.compile();
        
        // 创建初始状态
        Map<String, Object> initialState = Map.of(
                MedicalAgentState.USER_QUERY, userQuery
        );
        
        // 流式执行图
        for (var nodeOutput : compiledGraph.stream(initialState)) {
            log.info("\n--- 节点输出 ---");
            log.info("{}", nodeOutput);
            log.info("---------------\n");
        }
        
        log.info("=" .repeat(60));
        log.info("MedicalAgent执行完成");
        log.info("=" .repeat(60));
    }
    
    /**
     * 执行并返回最终状态
     * 
     * @param userQuery 用户查询
     * @return 最终状态
     * @throws Exception 执行异常
     */
    public MedicalAgentState executeAndGetState(String userQuery) throws Exception {
        log.info("=" .repeat(60));
        log.info("MedicalAgent开始执行");
        log.info("用户查询: {}", userQuery);
        log.info("=" .repeat(60));
        
        // 编译图
        var compiledGraph = graph.compile();
        
        // 创建初始状态
        Map<String, Object> initialState = Map.of(
                MedicalAgentState.USER_QUERY, userQuery
        );
        
        // 执行图并获取最终状态
        var result = compiledGraph.invoke(initialState);
        MedicalAgentState finalState = result.get();
        
        log.info("\n" + "=" .repeat(60));
        log.info("执行完成！最终结果：");
        log.info("=" .repeat(60));
        log.info("\n{}\n", finalState.finalResult());
        log.info("{}", finalState.conversionResult());
        log.info("=" .repeat(60));
        
        return finalState;
    }
}
