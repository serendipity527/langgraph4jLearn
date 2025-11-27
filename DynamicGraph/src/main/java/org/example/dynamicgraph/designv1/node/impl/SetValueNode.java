package org.example.dynamicgraph.designv1.node.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

/**
 * 设置值节点 - 直接实现 NodeAction
 */
@NoArgsConstructor
@AllArgsConstructor
public class SetValueNode implements NodeAction<AgentState> {

    private String key;
    private String value;

    @Override
    public Map<String, Object> apply(AgentState state) {
        return Map.of(key, value);
    }
}
