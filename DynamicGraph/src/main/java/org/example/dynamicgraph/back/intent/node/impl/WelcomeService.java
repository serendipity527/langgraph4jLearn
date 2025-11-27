package org.example.dynamicgraph.back.intent.node.impl;

import org.example.dynamicgraph.back.intent.node.GraphNode;
import org.example.dynamicgraph.back.intent.state.IntentState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 欢迎场景服务节点
 */
@Component("WelcomeService")
public class WelcomeService implements GraphNode<IntentState> {

    @Override
    public Map<String, Object> execute(IntentState state) {
        System.out.println("👋 WELCOME场景 - 处理欢迎请求");
        String response = "欢迎您！很高兴为您服务。请问有什么可以帮助您的？";
        return Map.of(
            IntentState.OUTPUT_KEY, response,
            IntentState.MESSAGES_KEY, List.of("WELCOME场景处理完成")
        );
    }
}
