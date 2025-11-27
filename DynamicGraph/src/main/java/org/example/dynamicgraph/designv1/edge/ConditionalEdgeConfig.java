package org.example.dynamicgraph.designv1.edge;

import lombok.Data;

import java.util.Map;

/**
 * 条件边配置 - 对应 stateGraph.addConditionalEdges()
 */
@Data
public class ConditionalEdgeConfig {
    /** 源节点 */
    private String from;
    
    /** 条件 key（从 state 中读取） */
    private String conditionKey;
    
    /** 路由映射: 条件值 -> 目标节点 */
    private Map<String, String> routes;
    
    /** 默认目标（可选） */
    private String defaultTarget;
}
