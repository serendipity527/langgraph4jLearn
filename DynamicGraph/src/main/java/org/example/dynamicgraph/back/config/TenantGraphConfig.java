package org.example.dynamicgraph.back.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 租户图配置
 */
@Setter
@Getter
public class TenantGraphConfig {
    // Getters and Setters
    private String tenantId;           // 租户ID
    private String graphName;          // 图名称
    private List<NodeConfig> nodes;    // 节点配置列表
    private List<EdgeConfig> edges;    // 边配置列表
    
    public TenantGraphConfig() {}
    
    public TenantGraphConfig(String tenantId, String graphName, 
                            List<NodeConfig> nodes, List<EdgeConfig> edges) {
        this.tenantId = tenantId;
        this.graphName = graphName;
        this.nodes = nodes;
        this.edges = edges;
    }

    @Override
    public String toString() {
        return "TenantGraphConfig{" +
                "tenantId='" + tenantId + '\'' +
                ", graphName='" + graphName + '\'' +
                ", nodes=" + nodes +
                ", edges=" + edges +
                '}';
    }
}
