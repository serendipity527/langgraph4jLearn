package org.example.dynamicgraph.back.router;

import org.example.dynamicgraph.back.core.DynamicState;

import java.util.List;

/**
 * 条件路由器
 * 根据状态和条件规则决定下一个节点
 */
public class ConditionalRouter {
    
    private final List<RouteRule> rules;
    
    public ConditionalRouter(List<RouteRule> rules) {
        this.rules = rules;
    }
    
    /**
     * 根据当前状态确定下一个节点
     */
    public String route(DynamicState state) {
        for (RouteRule rule : rules) {
            if (rule.matches(state)) {
                System.out.println("[Router] 匹配规则: " + rule.getCondition() + " -> " + rule.getTargetNode());
                return rule.getTargetNode();
            }
        }
        
        // 默认路由（如果没有规则匹配）
        System.out.println("[Router] 使用默认路由");
        return rules.isEmpty() ? null : rules.get(rules.size() - 1).getTargetNode();
    }
    
    /**
     * 路由规则
     */
    public static class RouteRule {
        private final String condition;
        private final String targetNode;
        
        public RouteRule(String condition, String targetNode) {
            this.condition = condition;
            this.targetNode = targetNode;
        }
        
        /**
         * 检查规则是否匹配当前状态
         */
        public boolean matches(DynamicState state) {
            // 简化的条件匹配逻辑
            switch (condition) {
                case "data_contains_error":
                    return state.getDataAsString().toLowerCase().contains("error");
                case "data_contains_success":
                    return state.getDataAsString().toLowerCase().contains("success");
                case "data_length_gt_10":
                    return state.getDataAsString().length() > 10;
                case "retry_count_lt_3":
                    return getRetryCount(state) < 3;
                case "need_human_approval":
                    return state.getDataAsString().toLowerCase().contains("approve");
                case "continue":
                    return true; // 默认继续
                default:
                    return false;
            }
        }
        
        private int getRetryCount(DynamicState state) {
            // 从消息中计算重试次数
            return (int) state.getMessages().stream()
                    .filter(msg -> msg.toString().contains("重试"))
                    .count();
        }
        
        public String getCondition() {
            return condition;
        }
        
        public String getTargetNode() {
            return targetNode;
        }
    }
}
