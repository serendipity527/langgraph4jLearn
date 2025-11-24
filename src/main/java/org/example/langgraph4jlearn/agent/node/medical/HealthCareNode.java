package org.example.langgraph4jlearn.agent.node.medical;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;

import java.util.Map;

/**
 * 健康保健智能体节点
 * 负责提供健康管理服务和产品推荐
 */
@Slf4j
public class HealthCareNode implements NodeAction<MedicalAgentState> {
    
    @Override
    public Map<String, Object> apply(MedicalAgentState state) {
        log.info("【健康保健智能体】开始提供保健服务...");
        
        String healthPlan = state.healthPlan();
        
        // 模拟健康保健服务
        String healthCareService = provideHealthCareService(healthPlan);
        
        log.info("健康保健服务生成完成");
        
        return Map.of(
                MedicalAgentState.CONVERSION_RESULT, healthCareService,
                MedicalAgentState.MESSAGES, "【健康保健】" + healthCareService
        );
    }
    
    /**
     * 提供健康保健服务
     */
    private String provideHealthCareService(String healthPlan) {
        return """
                健康保健服务推荐：
                
                【健康管理套餐】
                1. 基础套餐（￥299/月）
                   - 每日健康打卡提醒
                   - 营养师在线咨询
                   - 健康知识推送
                
                2. 进阶套餐（￥599/月）
                   - 基础套餐所有服务
                   - 个性化运动计划
                   - 每月1次体质检测
                   - 专属健康管家
                
                【保健品推荐】
                3. 根据您的健康状况，推荐：
                   - 复合维生素片
                   - 钙片（中老年配方）
                   - 深海鱼油
                
                【健身服务】
                4. 合作健身中心：
                   - XX健身（季卡8折优惠）
                   - 瑜伽课程（新会员免费体验）
                   - 游泳馆（团购价）
                
                【定期体检】
                5. 年度体检套餐：
                   - 基础体检（￥399）
                   - 全面体检（￥1299）
                   - 高端体检（￥2999）
                """;
    }
}
