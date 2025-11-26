package org.example.dynamicgraph.intent.node.impl;

import org.example.dynamicgraph.intent.node.GraphNode;
import org.example.dynamicgraph.intent.state.IntentState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 意图识别服务节点
 */
@Component("IntentRecognitionService")
public class IntentRecognitionService implements GraphNode<IntentState> {

    public enum IntentType {
        WELCOME,
        CHAT,
        FOLLOWUP
    }

    @Override
    public Map<String, Object> execute(IntentState state) {
        String input = state.getInput();
        System.out.println("🔍 意图识别节点 - 输入: " + input);

        // 意图识别逻辑
        String intent;
        if (input.contains("你好") || input.contains("hello") || input.contains("hi")) {
            intent = IntentType.WELCOME.name();
        } else if (input.contains("继续") || input.contains("跟进") || input.contains("follow")) {
            intent = IntentType.FOLLOWUP.name();
        } else {
            intent = IntentType.CHAT.name();
        }

        System.out.println("📋 识别结果: " + intent);
        return Map.of(
            IntentState.INTENT_KEY, intent,
            IntentState.MESSAGES_KEY, List.of("意图识别完成: " + intent)
        );
    }
}
