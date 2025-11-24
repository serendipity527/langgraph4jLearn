package org.example.langgraph4jlearn.agent.node.medical;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;

import java.util.Map;

/**
 * 流程诊断智能体节点
 * 负责分析用户意图并路由到相应的专业智能体
 */
@Slf4j
public class RoutingDiagnosisNode implements NodeAction<MedicalAgentState> {
    
    @Override
    public Map<String, Object> apply(MedicalAgentState state) {
        log.info("【流程诊断智能体】开始分析用户意图...");
        
        String userQuery = state.userQuery();
        log.info("用户查询: {}", userQuery);
        
        // 简单的意图识别逻辑（实际应用中应使用LLM）
        String intent = detectIntent(userQuery);
        
        log.info("识别到的意图: {}", intent);
        
        return Map.of(
                MedicalAgentState.INTENT, intent,
                MedicalAgentState.MESSAGES, String.format("【流程诊断】识别意图为: %s", intent)
        );
    }
    
    /**
     * 检测用户意图
     */
    private String detectIntent(String query) {
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("症状") || lowerQuery.contains("不舒服") || 
            lowerQuery.contains("疼痛") || lowerQuery.contains("问诊")) {
            return "预问诊";
        } else if (lowerQuery.contains("药") || lowerQuery.contains("medicine") || 
                   lowerQuery.contains("处方")) {
            return "药品";
        } else if (lowerQuery.contains("健康") || lowerQuery.contains("计划") || 
                   lowerQuery.contains("养生") || lowerQuery.contains("保健")) {
            return "健康计划";
        }
        
        // 默认返回预问诊
        return "预问诊";
    }
}
