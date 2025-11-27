package org.example.dynamicgraph.back.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 节点配置
 */
import lombok.Getter;
import lombok.Setter;

/**
 * 节点配置
 */
@Setter
@Getter
public class NodeConfig {
    // Getters and Setters
    private String id;           // 节点ID
    private String type;         // 节点类型
    private Map<String, Object> params; // 节点参数
    
    public NodeConfig() {
        this.params = new HashMap<>();
    }
    
    public NodeConfig(String id, String type) {
        this.id = id;
        this.type = type;
        this.params = new HashMap<>();
    }
    
    public NodeConfig(String id, String type, Map<String, Object> params) {
        this.id = id;
        this.type = type;
        this.params = params != null ? params : new HashMap<>();
    }

    @Override
    public String toString() {
        return "NodeConfig{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", params=" + params +
                '}';
    }
}
