package org.example.langgraph4jlearn.agent.node.medical;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;

import java.util.Map;

/**
 * 药品详情智能体节点
 * 负责提供药品信息、用法用量、注意事项等
 */
@Slf4j
public class DrugInfoNode implements NodeAction<MedicalAgentState> {
    
    @Override
    public Map<String, Object> apply(MedicalAgentState state) {
        log.info("【药品详情智能体】开始查询药品信息...");
        
        String userQuery = state.userQuery();
        
        // 模拟查询药品信息
        String drugInfo = queryDrugInfo(userQuery);
        
        log.info("药品信息查询完成: {}", drugInfo);
        
        return Map.of(
                MedicalAgentState.DRUG_INFO, drugInfo,
                MedicalAgentState.MESSAGES, "【药品详情】" + drugInfo
        );
    }
    
    /**
     * 查询药品信息
     */
    private String queryDrugInfo(String query) {
        // 实际应用中应该查询药品数据库
        return String.format("针对查询'%s'，提供以下药品信息：\n" +
                "【药品名称】示例药品\n" +
                "【主要成分】活性成分说明\n" +
                "【适应症】适用于相关症状的治疗\n" +
                "【用法用量】每次1-2片，每日3次\n" +
                "【注意事项】孕妇、哺乳期妇女慎用；过敏体质者禁用", 
                query);
    }
}
