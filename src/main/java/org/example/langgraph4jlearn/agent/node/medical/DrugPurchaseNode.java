package org.example.langgraph4jlearn.agent.node.medical;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;

import java.util.Map;

/**
 * 药品购买智能体节点
 * 负责提供药品购买渠道和优惠信息
 */
@Slf4j
public class DrugPurchaseNode implements NodeAction<MedicalAgentState> {
    
    @Override
    public Map<String, Object> apply(MedicalAgentState state) {
        log.info("【药品购买智能体】开始提供购买信息...");
        
        String drugInfo = state.drugInfo();
        
        // 模拟药品购买信息
        String purchaseInfo = providePurchaseInfo(drugInfo);
        
        log.info("药品购买信息生成完成");
        
        return Map.of(
                MedicalAgentState.CONVERSION_RESULT, purchaseInfo,
                MedicalAgentState.MESSAGES, "【药品购买】" + purchaseInfo
        );
    }
    
    /**
     * 提供购买信息
     */
    private String providePurchaseInfo(String drugInfo) {
        return """
                药品购买渠道推荐：
                
                【线下购买】
                1. 附近药店：
                   - XX大药房（距离您500米）营业时间：8:00-22:00
                   - XX连锁药店（距离您1公里）营业时间：24小时
                
                【在线购买】
                2. 官方医药电商平台：
                   - 支持医保支付
                   - 1小时送药上门
                   - 药师在线咨询
                
                【优惠活动】
                3. 当前优惠：
                   - 新用户立减10元
                   - 满100减20
                   - 部分药品买一送一
                
                【购药提醒】
                - 处方药需凭医生处方购买
                - 请核对药品名称、规格和有效期
                - 如有疑问，请咨询药师
                """;
    }
}
