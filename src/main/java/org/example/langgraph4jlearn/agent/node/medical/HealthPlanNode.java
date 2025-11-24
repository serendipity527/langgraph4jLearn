package org.example.langgraph4jlearn.agent.node.medical;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;

import java.util.Map;

/**
 * 健康规划智能体节点
 * 负责制定个性化健康管理计划
 */
@Slf4j
public class HealthPlanNode implements NodeAction<MedicalAgentState> {
    
    @Override
    public Map<String, Object> apply(MedicalAgentState state) {
        log.info("【健康规划智能体】开始制定健康计划...");
        
        String userQuery = state.userQuery();
        
        // 模拟制定健康计划
        String healthPlan = createHealthPlan(userQuery);
        
        log.info("健康计划制定完成: {}", healthPlan);
        
        return Map.of(
                MedicalAgentState.HEALTH_PLAN, healthPlan,
                MedicalAgentState.MESSAGES, "【健康规划】" + healthPlan
        );
    }
    
    /**
     * 创建健康计划
     */
    private String createHealthPlan(String query) {
        // 实际应用中应该根据用户情况定制计划
        return String.format("基于您的需求'%s'，为您制定以下健康管理计划：\n" +
                "【饮食建议】均衡营养，多吃蔬菜水果，少油少盐\n" +
                "【运动建议】每周至少3次有氧运动，每次30分钟\n" +
                "【作息建议】保持规律作息，每天睡眠7-8小时\n" +
                "【定期检查】建议每半年进行一次体检\n" +
                "【心理健康】保持积极心态，适当放松减压", 
                query);
    }
}
