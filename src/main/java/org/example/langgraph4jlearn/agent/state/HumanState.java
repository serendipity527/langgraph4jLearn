package org.example.langgraph4jlearn.agent.state;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 人在回路状态类
 * 用于演示如何在图执行过程中等待人工输入和反馈
 */
public class HumanState extends AgentState {
    
    // 状态键常量
    public static final String MESSAGES_KEY = "messages";
    public static final String HUMAN_FEEDBACK_KEY = "human_feedback";
    public static final String STEP_COUNTER_KEY = "step_counter";
    
    /**
     * 定义状态Schema
     * - messages: 消息列表（追加式更新）
     * - human_feedback: 人工反馈（默认lastValue行为）
     * - step_counter: 步骤计数器（默认lastValue行为）
     * 注意：只需要为需要特殊行为（如追加）的字段定义Channel
     */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES_KEY, Channels.appender(ArrayList::new)
    );
    
    /**
     * 构造函数
     */
    public HumanState(Map<String, Object> initData) {
        super(initData);
    }
    
    /**
     * 获取消息列表
     */
    public List<String> messages() {
        return this.<List<String>>value(MESSAGES_KEY).orElse(new ArrayList<>());
    }
    
    /**
     * 获取人工反馈
     */
    public Optional<String> humanFeedback() {
        return value(HUMAN_FEEDBACK_KEY);
    }
    
    /**
     * 获取步骤计数器
     */
    public Integer stepCounter() {
        return this.<Integer>value(STEP_COUNTER_KEY).orElse(0);
    }
}
