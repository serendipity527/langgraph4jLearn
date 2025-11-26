package org.example.dynamicgraph.nodes.impl;

import org.example.dynamicgraph.core.DynamicState;
import org.example.dynamicgraph.nodes.DynamicNode;

import java.util.Map;

/**
 * 决策节点 - 基于数据内容做决策，用于条件路由
 */
public class DecisionNode implements DynamicNode {
    
    @Override
    public Map<String, Object> execute(Map<String, Object> state, Map<String, Object> params) {
        String data = (String) state.get(DynamicState.DATA_KEY);
        String decisionType = (String) params.getOrDefault("decisionType", "default");
        
        System.out.println("[Decision] 决策类型: " + decisionType + ", 数据: " + data);
        
        String result;
        switch (decisionType) {
            case "quality_check":
                result = checkQuality(data);
                break;
            case "risk_assessment": 
                result = assessRisk(data);
                break;
            case "approval_needed":
                result = needsApproval(data);
                break;
            default:
                result = data + "-decided";
        }
        
        System.out.println("[Decision] 决策结果: " + result);
        
        return Map.of(
            DynamicState.DATA_KEY, result,
            DynamicState.MESSAGES_KEY, "决策完成: " + decisionType
        );
    }
    
    private String checkQuality(String data) {
        // 模拟质量检查
        if (data.length() > 15) {
            return data + "-high-quality";
        } else if (data.length() > 8) {
            return data + "-medium-quality"; 
        } else {
            return data + "-low-quality";
        }
    }
    
    private String assessRisk(String data) {
        // 模拟风险评估
        if (data.toLowerCase().contains("critical") || data.toLowerCase().contains("urgent")) {
            return data + "-high-risk";
        } else if (data.toLowerCase().contains("important")) {
            return data + "-medium-risk";
        } else {
            return data + "-low-risk";
        }
    }
    
    private String needsApproval(String data) {
        // 模拟审批需求判断
        if (data.toLowerCase().contains("expensive") || data.toLowerCase().contains("critical")) {
            return data + "-needs-approve";
        } else {
            return data + "-auto-approve";
        }
    }
    
    @Override
    public String getNodeType() {
        return "decisionNode";
    }
}
