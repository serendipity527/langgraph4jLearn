package org.example.dynamicgraph.intent.config;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 图配置 - 对应JSON结构
 */
@Data
public class GraphConfig {
    
    private Meta meta;
    private Settings settings;
    private List<NodeDef> nodes;
    private List<EdgeDef> edges;
    private List<ConditionalEdgeDef> conditionalEdges;

    @Data
    public static class Meta {
        private String graphId;
        private String description;
    }

    @Data
    public static class Settings {
        private String entryPoint;
    }

    @Data
    public static class NodeDef {
        private String id;
        private String component;
    }

    @Data
    public static class EdgeDef {
        private String from;
        private String to;
    }

    @Data
    public static class ConditionalEdgeDef {
        private String from;
        private String router;
        private Map<String, String> paths;
    }
}
