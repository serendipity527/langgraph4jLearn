package org.example.dynamicgraph.designv1.node.impl;

import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 意图识别节点 - 直接实现 NodeAction
 */
@Component("intentRecognition")
public class IntentRecognitionNode implements NodeAction<AgentState> {

    @Override
    public Map<String, Object> apply(AgentState state) {
        String input = state.<String>value("input").orElse("");
        String intent = recognizeIntent(input);
        return Map.of("intent", intent);
    }

    private String recognizeIntent(String input) {
        if (input.contains("你好") || input.contains("hello") || input.contains("hi")) {
            return "WELCOME";
        } else if (input.contains("天气") || input.contains("weather")) {
            return "WEATHER";
        } else if (input.contains("订单") || input.contains("order")) {
            return "ORDER";
        }
        return "CHAT";
    }
}
