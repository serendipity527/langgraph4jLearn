package org.example.langgraph4jlearn.agent.node.MedicalAssistant;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalSystemState;
import org.example.langgraph4jlearn.enums.SystemStage;

import java.util.Map;

@Slf4j
public class EmergencyGuideNode implements NodeAction<MedicalSystemState> {
    
    private static final String EMERGENCY_GUIDE_TEXT = """
            ========================================
            ⚠️ 紧急医疗指引 ⚠️
            ========================================
            
            您的症状可能涉及紧急医疗状况，请立即采取以下行动：
            
            🚨 立即拨打急救电话：120
            
            🏥 或前往最近的医院急诊科
            
            ⏰ 时间就是生命，请勿延误！
            
            💡 等待救援期间：
               - 保持冷静
               - 如有他人在场，请寻求帮助
               - 不要擅自服药
               - 记录症状发生时间
            
            ⚠️ 本系统无法处理紧急医疗状况
            ⚠️ 请立即寻求专业医疗救助
            
            ========================================
            """;
    
    @Override
    public Map<String, Object> apply(MedicalSystemState state) {
        log.error("=== 紧急指引节点执行 ===");
        log.error("用户查询: {}", state.userQuery());
        log.error(EMERGENCY_GUIDE_TEXT);
        
        return Map.of(
                MedicalSystemState.STAGE, SystemStage.TERMINATED,
                MedicalSystemState.RESPONSE, EMERGENCY_GUIDE_TEXT,
                MedicalSystemState.MESSAGES, "系统: 已触发紧急指引",
                MedicalSystemState.CONTEXT, "紧急情况，系统终止服务"
        );
    }
}
