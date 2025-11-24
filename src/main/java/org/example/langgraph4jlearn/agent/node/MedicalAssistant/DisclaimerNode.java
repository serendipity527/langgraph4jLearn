package org.example.langgraph4jlearn.agent.node.MedicalAssistant;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalSystemState;
import org.example.langgraph4jlearn.enums.SystemStage;

import java.util.Map;

@Slf4j
public class DisclaimerNode implements NodeAction<MedicalSystemState> {
    
    private static final String DISCLAIMER_TEXT = """
            ========================================
            医疗AI助手免责声明
            ========================================
            
            1. 本系统仅提供健康咨询参考，不构成医疗诊断或治疗建议
            2. 如遇紧急情况，请立即拨打120或前往医院急诊
            3. 本系统建议不能替代专业医生的诊断和治疗
            4. 请勿将本系统建议作为自行用药或治疗的唯一依据
            5. 使用本系统即表示您已理解并同意上述条款
            
            ========================================
            """;
    
    @Override
    public Map<String, Object> apply(MedicalSystemState state) {
        log.info("=== 免责声明节点执行 ===");
        log.info(DISCLAIMER_TEXT);
        
        return Map.of(
                MedicalSystemState.DISCLAIMER_SHOWN, true,
                MedicalSystemState.STAGE, SystemStage.USER_CONSENT,
                MedicalSystemState.MESSAGES, "系统: " + DISCLAIMER_TEXT
        );
    }
}
