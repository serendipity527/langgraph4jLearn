package org.example.langgraph4jlearn.agent.node.medical;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;

import java.util.Map;

/**
 * 对话询问智能体节点
 * 负责收集患者症状信息并进行初步诊断
 */
@Slf4j
public class ConsultationNode implements NodeAction<MedicalAgentState> {
    
    @Override
    public Map<String, Object> apply(MedicalAgentState state) {
        log.info("【对话询问智能体】开始收集患者信息...");
        
        String userQuery = state.userQuery();
        
        // 模拟收集患者信息和初步诊断
        String consultationInfo = performConsultation(userQuery);
        
        log.info("预问诊信息收集完成: {}", consultationInfo);
        
        return Map.of(
                MedicalAgentState.CONSULTATION_INFO, consultationInfo,
                MedicalAgentState.MESSAGES, "【对话询问】" + consultationInfo
        );
    }
    
    /**
     * 执行预问诊
     */
    private String performConsultation(String query) {
        // 实际应用中应该通过LLM进行多轮对话
        return String.format("根据患者描述'%s'，初步判断可能需要关注以下方面：\n" +
                "1. 症状持续时间和严重程度\n" +
                "2. 既往病史和用药情况\n" +
                "3. 建议进行相关检查", 
                query);
    }
}
