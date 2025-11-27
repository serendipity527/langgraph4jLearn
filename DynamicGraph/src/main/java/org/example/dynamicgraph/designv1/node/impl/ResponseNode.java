package org.example.dynamicgraph.designv1.node.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

/**
 * 响应生成节点 - 直接实现 NodeAction
 */
@NoArgsConstructor
@AllArgsConstructor
public class ResponseNode implements NodeAction<AgentState> {

    private String template = "";
    private String outputKey = "response";

    public ResponseNode(String template) {
        this.template = template;
    }

    @Override
    public Map<String, Object> apply(AgentState state) {
        return Map.of(outputKey, template);
    }
}
