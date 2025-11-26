package org.example.dynamicgraph.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 边配置
 */
@Setter
@Getter
public class EdgeConfig {
    // Getters and Setters
    private String from;  // 源节点ID
    private String to;    // 目标节点ID
    private String condition; // 条件表达式（可选）
    private boolean conditional; // 是否为条件边
    
    public EdgeConfig() {}
    
    public EdgeConfig(String from, String to) {
        this.from = from;
        this.to = to;
        this.conditional = false;
    }
    
    public EdgeConfig(String from, String to, String condition) {
        this.from = from;
        this.to = to;
        this.condition = condition;
        this.conditional = true;
    }

    @Override
    public String toString() {
        return "EdgeConfig{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }
}
