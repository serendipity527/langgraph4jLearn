package org.example.dynamicgraph.designv1.node.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

/**
 * 字符串转换节点 - 直接实现 NodeAction
 */
@NoArgsConstructor
@AllArgsConstructor
public class TransformNode implements NodeAction<AgentState> {

    private String inputKey = "input";
    private String outputKey = "output";
    private String operation = "echo";  // upper, lower, reverse, echo

    @Override
    public Map<String, Object> apply(AgentState state) {
        String input = state.<String>value(inputKey).orElse("");
        
        String result = switch (operation) {
            case "upper" -> input.toUpperCase();
            case "lower" -> input.toLowerCase();
            case "reverse" -> new StringBuilder(input).reverse().toString();
            default -> input;
        };

        return Map.of(outputKey, result);
    }
}
