package org.example.dynamicgraph.intent.node.impl;

import org.example.dynamicgraph.intent.node.GraphNode;
import org.example.dynamicgraph.intent.state.IntentState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 聊天场景服务节点
 */
@Component("ChatService")
public class ChatService implements GraphNode<IntentState> {

    @Override
    public Map<String, Object> execute(IntentState state) {
        System.out.println("💬 CHAT对话 - 处理聊天请求");
        String input = state.getInput();
        String response = "收到您的消息：'" + input + "'。这是一个普通的聊天对话回复。";
        return Map.of(
            IntentState.OUTPUT_KEY, response,
            IntentState.MESSAGES_KEY, List.of("CHAT对话处理完成")
        );
    }
}
