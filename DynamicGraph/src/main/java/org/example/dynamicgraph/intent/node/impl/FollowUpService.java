package org.example.dynamicgraph.intent.node.impl;

import org.example.dynamicgraph.intent.node.GraphNode;
import org.example.dynamicgraph.intent.state.IntentState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 跟进场景服务节点
 */
@Component("FollowUpService")
public class FollowUpService implements GraphNode<IntentState> {

    @Override
    public Map<String, Object> execute(IntentState state) {
        System.out.println("📎 FollowUP场景 - 处理跟进请求");
        String response = "好的，我来帮您跟进之前的问题。请告诉我具体需要跟进什么内容？";
        return Map.of(
            IntentState.OUTPUT_KEY, response,
            IntentState.MESSAGES_KEY, List.of("FollowUP场景处理完成")
        );
    }
}
