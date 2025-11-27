package org.example.dynamicgraph.designv1.node;

import lombok.Data;

/**
 * 节点配置（对应 JSON 中的节点定义）
 */
@Data
public class NodeConfig {
    
    /**
     * 节点唯一标识
     */
    private String id;
    
    /**
     * 组件类型（对应 NodeRegistry 中注册的节点名称）
     */
    private String componentType;
    
    /**
     * 节点描述（可选，用于调试和可视化）
     */
    private String description;
    
    // ============ 静态工厂方法 ============
    
    public static NodeConfig of(String id, String componentType) {
        NodeConfig config = new NodeConfig();
        config.setId(id);
        config.setComponentType(componentType);
        return config;
    }
}
