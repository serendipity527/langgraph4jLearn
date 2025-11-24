package org.example.langgraph4jlearn.agent.graph;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.EdgeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.example.langgraph4jlearn.agent.node.MedicalAssistant.DisclaimerNode;
import org.example.langgraph4jlearn.agent.node.MedicalAssistant.UserConsentNode;
import org.example.langgraph4jlearn.agent.node.MedicalAssistant.TriageNode;
import org.example.langgraph4jlearn.agent.node.MedicalAssistant.EmergencyGuideNode;
import org.example.langgraph4jlearn.agent.state.MedicalSystemState;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class MedicalAssistantGraph {
    
    private final DisclaimerNode disclaimerNode;
    private final UserConsentNode userConsentNode;
    private final TriageNode triageNode;
    private final EmergencyGuideNode emergencyGuideNode;
    private StateGraph<MedicalSystemState> graph;
    
    public MedicalAssistantGraph() {
        this.disclaimerNode = new DisclaimerNode();
        this.userConsentNode = new UserConsentNode();
        this.triageNode = new TriageNode();
        this.emergencyGuideNode = new EmergencyGuideNode();
        buildGraph();
    }
    
    private void buildGraph() {
        try {
            log.info("开始构建医疗助手图 - 阶段2：意图识别与紧急分诊");
            
            this.graph = new StateGraph<>(
                    MedicalSystemState.SCHEMA,
                    MedicalSystemState::new
            );
            
            // 添加节点
            graph.addNode("disclaimer", node_async(disclaimerNode));
            graph.addNode("user_consent", node_async(userConsentNode));
            graph.addNode("triage", node_async(triageNode));
            graph.addNode("emergency_guide", node_async(emergencyGuideNode));
            
            // 定义流程
            graph.addEdge(START, "disclaimer");
            graph.addEdge("disclaimer", "user_consent");
            
            // 用户同意后进入分诊
            graph.addConditionalEdges(
                    "user_consent",
                    edge_async(routeAfterConsent()),
                    Map.of(
                            "end", END,
                            "triage", "triage"
                    )
            );
            
            // 分诊后根据是否紧急进行路由
            graph.addConditionalEdges(
                    "triage",
                    edge_async(routeAfterTriage()),
                    Map.of(
                            "emergency", "emergency_guide",
                            "normal", END  // 阶段2非紧急情况暂时结束，后续阶段会继续
                    )
            );
            
            // 紧急指引后结束
            graph.addEdge("emergency_guide", END);
            
            log.info("医疗助手图构建完成 - 阶段2");
            
        } catch (Exception e) {
            log.error("构建图失败", e);
            throw new RuntimeException("构建医疗助手图失败", e);
        }
    }
    
    private EdgeAction<MedicalSystemState> routeAfterConsent() {
        return state -> {
            if (state.userConsented()) {
                log.info("用户同意条款，进入分诊");
                return "triage";
            } else {
                log.info("用户拒绝条款，结束流程");
                return "end";
            }
        };
    }
    
    private EdgeAction<MedicalSystemState> routeAfterTriage() {
        return state -> {
            if (state.isEmergency()) {
                log.warn("紧急情况，触发紧急指引");
                return "emergency";
            } else {
                log.info("非紧急情况，继续正常流程");
                return "normal";
            }
        };
    }
    
    public StateGraph<MedicalSystemState> getGraph() {
        return this.graph;
    }
    
    /**
     * 编译图 - 不带检查点（用于简单执行）
     */
    public CompiledGraph<MedicalSystemState> compile() throws Exception {
        return graph.compile();
    }
    
    /**
     * 编译图 - 带检查点和人在回路支持
     * 在user_consent节点前中断，等待用户同意
     */
    public CompiledGraph<MedicalSystemState> compileWithHumanInLoop(MemorySaver checkpointSaver) throws Exception {
        var compileConfig = CompileConfig.builder()
                .checkpointSaver(checkpointSaver)
                .interruptBefore("user_consent")  // 在用户同意节点前中断
                .build();
        
        return graph.compile(compileConfig);
    }
    
    public MedicalSystemState execute(Map<String, Object> initialState) throws Exception {
        log.info("医疗助手开始执行 - 阶段2");
        
        var compiledGraph = graph.compile();
        var result = compiledGraph.invoke(initialState);
        MedicalSystemState finalState = result.get();
        
        log.info("医疗助手执行完成 - 阶段2，最终阶段: {}", finalState.stage());
        
        return finalState;
    }
}
