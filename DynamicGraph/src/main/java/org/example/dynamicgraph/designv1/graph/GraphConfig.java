package org.example.dynamicgraph.designv1.graph;

import lombok.Data;
import org.example.dynamicgraph.designv1.edge.ConditionalEdgeConfig;
import org.example.dynamicgraph.designv1.edge.EdgeConfig;
import org.example.dynamicgraph.designv1.node.NodeConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图配置 - JSON 反序列化目标
 * 
 * 分离普通边和条件边，贴近 LangGraph4j 的 API 设计
 */
@Data
public class GraphConfig {
    
    /** 图的唯一标识 */
    private String id;
    
    /** 图名称 */
    private String name;
    
    /** 入口节点 ID */
    private String entryNode;
    
    /** 
     * 状态 Schema 定义
     * key: 字段名
     * value: Channel 类型配置
     */
    private Map<String, StateFieldConfig> stateSchema = new HashMap<>();
    
    /** 节点列表 */
    private List<NodeConfig> nodes = new ArrayList<>();
    
    /** 普通边 - 对应 stateGraph.addEdge(from, to) */
    private List<EdgeConfig> edges = new ArrayList<>();
    
    /** 条件边 - 对应 stateGraph.addConditionalEdges() */
    private List<ConditionalEdgeConfig> conditionalEdges = new ArrayList<>();

    /**
     * 状态字段配置
     */
    @Data
    public static class StateFieldConfig {
        /** 
         * Channel 类型: 
         * - "appender": 追加模式（如消息历史）
         * - "value": 覆盖模式（默认）
         */
        private String type = "value";
        
        /** 默认值（可选） */
        private Object defaultValue;
    }
}
