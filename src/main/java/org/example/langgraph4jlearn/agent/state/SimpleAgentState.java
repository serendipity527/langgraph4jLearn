package org.example.langgraph4jlearn.agent.state;

import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 简单Agent状态类
 * 继承LangGraph4j的AgentState，定义状态结构
 */
public class SimpleAgentState extends org.bsc.langgraph4j.state.AgentState {
    
    // 状态键常量
    public static final String MESSAGES_KEY = "messages";
    
    /**
     * 定义状态Schema
     * messages: 消息列表（追加式更新）
     */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES_KEY, Channels.appender(ArrayList::new)
    );
    
    /**
     * 构造函数
     */
    public SimpleAgentState(Map<String, Object> initData) {
        super(initData);
    }
    
    /**
     * 获取消息列表
     */
    public List<String> messages() {
        return this.<List<String>>value(MESSAGES_KEY).orElse(List.of());
    }
}
