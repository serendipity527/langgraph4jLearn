package org.example.langgraph4jlearn.agent.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.SimpleAgentState;

import java.util.List;
import java.util.Map;

/**
 * Responder节点 - 根据消息生成响应
 */
@Slf4j
public class ResponderNode implements NodeAction<SimpleAgentState> {
    
    @Override
    public Map<String, Object> apply(SimpleAgentState state) {
        log.info("ResponderNode执行中. 当前消息: {}", state.messages());
        
        List<String> currentMessages = state.messages();
        if (currentMessages.stream().anyMatch(msg -> msg.contains("欢迎"))) {
            return Map.of(SimpleAgentState.MESSAGES_KEY, "收到！准备开始工作！");
        }
        
        return Map.of(SimpleAgentState.MESSAGES_KEY, "未检测到问候消息。");
    }
}
