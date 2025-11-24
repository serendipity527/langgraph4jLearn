package org.example.langgraph4jlearn.agent.node.MedicalAssistant;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalSystemState;
import org.example.langgraph4jlearn.enums.SystemStage;

import java.util.Map;

@Slf4j
public class UserConsentNode implements NodeAction<MedicalSystemState> {
    
    @Override
    public Map<String, Object> apply(MedicalSystemState state) {
        log.info("=== 用户同意节点执行 ===");
        
        Boolean consented = state.userConsented();
        log.info("用户同意状态: {}", consented);
        
        if (consented) {
            return Map.of(
                    MedicalSystemState.STAGE, SystemStage.TRIAGE,
                    MedicalSystemState.MESSAGES, "用户已同意免责条款，进入意图识别阶段"
            );
        } else {
            return Map.of(
                    MedicalSystemState.STAGE, SystemStage.TERMINATED,
                    MedicalSystemState.MESSAGES, "用户未同意免责条款，系统终止",
                    MedicalSystemState.RESPONSE, "感谢您的关注，如需使用本系统请同意免责条款。"
            );
        }
    }
}
