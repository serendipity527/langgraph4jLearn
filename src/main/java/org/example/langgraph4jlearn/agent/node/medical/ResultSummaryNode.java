package org.example.langgraph4jlearn.agent.node.medical;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;

import java.util.Map;

/**
 * 结果汇总智能体节点
 * 负责汇总所有智能体的输出并生成最终结果
 */
@Slf4j
public class ResultSummaryNode implements NodeAction<MedicalAgentState> {
    
    @Override
    public Map<String, Object> apply(MedicalAgentState state) {
        log.info("【结果汇总智能体】开始汇总结果...");
        
        // 收集所有信息
        String intent = state.intent();
        String consultationInfo = state.consultationInfo();
        String drugInfo = state.drugInfo();
        String healthPlan = state.healthPlan();
        String riskAssessment = state.riskAssessment();
        
        // 生成汇总报告
        String finalResult = summarizeResults(intent, consultationInfo, drugInfo, healthPlan, riskAssessment);
        
        // 确定转化类型
        String conversionType = determineConversionType(intent);
        
        log.info("结果汇总完成，转化类型: {}", conversionType);
        
        return Map.of(
                MedicalAgentState.FINAL_RESULT, finalResult,
                MedicalAgentState.CONVERSION_TYPE, conversionType,
                MedicalAgentState.MESSAGES, "【结果汇总】汇总完成，准备转化"
        );
    }
    
    /**
     * 汇总结果
     */
    private String summarizeResults(String intent, String consultation, 
                                    String drug, String healthPlan, String risk) {
        StringBuilder summary = new StringBuilder();
        summary.append("=" .repeat(50)).append("\n");
        summary.append("医疗健康咨询结果汇总\n");
        summary.append("=" .repeat(50)).append("\n\n");
        
        summary.append("【咨询类型】").append(intent).append("\n\n");
        
        if (consultation != null && !consultation.isEmpty()) {
            summary.append("【预问诊信息】\n").append(consultation).append("\n\n");
        }
        
        if (drug != null && !drug.isEmpty()) {
            summary.append("【药品信息】\n").append(drug).append("\n\n");
        }
        
        if (healthPlan != null && !healthPlan.isEmpty()) {
            summary.append("【健康计划】\n").append(healthPlan).append("\n\n");
        }
        
        if (risk != null && !risk.isEmpty()) {
            summary.append("【风险评估】\n").append(risk).append("\n\n");
        }
        
        summary.append("=" .repeat(50)).append("\n");
        
        return summary.toString();
    }
    
    /**
     * 确定转化类型
     */
    private String determineConversionType(String intent) {
        if (intent == null) {
            return "健康保健";
        }
        
        return switch (intent) {
            case "预问诊" -> "医生推荐";
            case "药品" -> "药品购买";
            case "健康计划" -> "健康保健";
            default -> "健康保健";
        };
    }
}
