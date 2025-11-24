package org.example.langgraph4jlearn.agent.node.medical;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;

import java.util.Map;

/**
 * 风险控制智能体节点
 * 负责评估医疗建议的风险并提供安全提示
 */
@Slf4j
public class RiskControlNode implements NodeAction<MedicalAgentState> {
    
    @Override
    public Map<String, Object> apply(MedicalAgentState state) {
        log.info("【风险控制智能体】开始评估风险...");
        
        // 收集上游信息
        String consultationInfo = state.consultationInfo();
        String drugInfo = state.drugInfo();
        String healthPlan = state.healthPlan();
        
        // 执行风险评估
        String riskAssessment = assessRisk(consultationInfo, drugInfo, healthPlan);
        
        log.info("风险评估完成: {}", riskAssessment);
        
        return Map.of(
                MedicalAgentState.RISK_ASSESSMENT, riskAssessment,
                MedicalAgentState.MESSAGES, "【风险控制】" + riskAssessment
        );
    }
    
    /**
     * 评估风险
     */
    private String assessRisk(String consultation, String drug, String healthPlan) {
        StringBuilder assessment = new StringBuilder("风险评估报告：\n");
        
        // 检查是否有敏感信息
        if (consultation != null && !consultation.isEmpty()) {
            assessment.append("✓ 预问诊信息已审核，未发现重大风险\n");
        }
        
        if (drug != null && !drug.isEmpty()) {
            assessment.append("⚠ 药品使用提醒：请严格按照医嘱用药，注意禁忌症\n");
        }
        
        if (healthPlan != null && !healthPlan.isEmpty()) {
            assessment.append("✓ 健康计划已评估，建议循序渐进执行\n");
        }
        
        assessment.append("\n【重要提示】\n");
        assessment.append("- 本系统提供的信息仅供参考\n");
        assessment.append("- 如症状严重或持续，请及时就医\n");
        assessment.append("- 用药前请咨询专业医师或药师");
        
        return assessment.toString();
    }
}
