package org.example.langgraph4jlearn.agent.node.medical;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;

import java.util.Map;

/**
 * 医生推荐智能体节点
 * 负责推荐合适的医生和医疗机构
 */
@Slf4j
public class DoctorRecommendationNode implements NodeAction<MedicalAgentState> {
    
    @Override
    public Map<String, Object> apply(MedicalAgentState state) {
        log.info("【医生推荐智能体】开始推荐医生...");
        
        String consultationInfo = state.consultationInfo();
        
        // 模拟医生推荐
        String recommendation = recommendDoctor(consultationInfo);
        
        log.info("医生推荐完成: {}", recommendation);
        
        return Map.of(
                MedicalAgentState.CONVERSION_RESULT, recommendation,
                MedicalAgentState.MESSAGES, "【医生推荐】" + recommendation
        );
    }
    
    /**
     * 推荐医生
     */
    private String recommendDoctor(String consultationInfo) {
        return """
                根据您的症状，为您推荐以下医生：
                
                【推荐医生1】
                姓名：张医生
                职称：主任医师
                专长：内科、消化系统疾病
                医院：市人民医院
                可预约时间：周一至周五 上午
                
                【推荐医生2】
                姓名：李医生
                职称：副主任医师
                专长：全科医学
                医院：社区卫生服务中心
                可预约时间：每天 全天
                
                【在线咨询】
                您也可以选择在线咨询服务，我们的医生将在24小时内回复
                """;
    }
}
