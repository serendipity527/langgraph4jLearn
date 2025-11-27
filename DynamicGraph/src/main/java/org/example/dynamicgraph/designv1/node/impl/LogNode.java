package org.example.dynamicgraph.designv1.node.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;

/**
 * 日志追加节点 - 向 logs 字段追加消息
 * 用于演示 appender 模式的 Schema
 */
@NoArgsConstructor
@AllArgsConstructor
public class LogNode implements NodeAction<AgentState> {

    private String message;

    @Override
    public Map<String, Object> apply(AgentState state) {
        // 返回一个 List，如果 Schema 中 logs 是 appender 模式，会追加而非覆盖
        return Map.of("logs", List.of(message));
    }
}
