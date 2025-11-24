package org.example.langgraph4jlearn.agent.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.SimpleAgentState;

import java.util.Map;

/**
 * Greeter节点 - 添加问候消息
 */
@Slf4j
public class GreeterNode implements NodeAction<SimpleAgentState> {
    
    @Override
    public Map<String, Object> apply(SimpleAgentState state) {
        log.info("GreeterNode执行中. 当前消息: {}", state.messages());
        return Map.of(SimpleAgentState.MESSAGES_KEY, "你好！欢迎使用LangGraph4j！");
    }
}
