package org.example.dynamicgraph.designv1.edge;

import lombok.Data;

/**
 * 普通边配置 - 对应 stateGraph.addEdge(from, to)
 */
@Data
public class EdgeConfig {
    private String from;
    private String to;
}
