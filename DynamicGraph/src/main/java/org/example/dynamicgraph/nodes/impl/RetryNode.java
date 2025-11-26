package org.example.dynamicgraph.nodes.impl;

import org.example.dynamicgraph.core.DynamicState;
import org.example.dynamicgraph.nodes.DynamicNode;

import java.util.Map;
import java.util.Random;

/**
 * 重试节点 - 模拟可能失败需要重试的操作
 */
public class RetryNode implements DynamicNode {
    
    private final Random random = new Random();
    
    @Override
    public Map<String, Object> execute(Map<String, Object> state, Map<String, Object> params) {
        String data = (String) state.get(DynamicState.DATA_KEY);
        double failureRate = (Double) params.getOrDefault("failureRate", 0.3);
        int maxRetries = (Integer) params.getOrDefault("maxRetries", 3);
        
        // 模拟随机失败
        boolean success = random.nextDouble() > failureRate;
        
        if (success) {
            System.out.println("[Retry] 操作成功: " + data);
            return Map.of(
                DynamicState.DATA_KEY, data + "-success",
                DynamicState.MESSAGES_KEY, "操作成功完成"
            );
        } else {
            System.out.println("[Retry] 操作失败，需要重试: " + data);
            return Map.of(
                DynamicState.DATA_KEY, data + "-error", 
                DynamicState.MESSAGES_KEY, "操作失败，重试中"
            );
        }
    }
    
    @Override
    public String getNodeType() {
        return "retryNode";
    }
}
