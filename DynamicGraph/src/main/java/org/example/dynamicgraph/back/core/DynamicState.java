package org.example.dynamicgraph.back.core;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 动态状态类，支持通用的状态管理
 */
public class DynamicState extends AgentState {
    
    public static final String MESSAGES_KEY = "messages";
    public static final String DATA_KEY = "data";
    
    /**
     * 通用Schema：支持消息列表和通用数据存储
     */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
        MESSAGES_KEY, Channels.appender(ArrayList::new),
        DATA_KEY, Channels.base(() -> "")
    );
    
    public DynamicState(Map<String, Object> initData) {
        super(initData);
    }
    
    /**
     * 获取消息列表
     */
    public List<String> getMessages() {
        return this.<List<String>>value(MESSAGES_KEY).orElse(new ArrayList<>());
    }
    
    /**
     * 获取通用数据
     */
    public <T> Optional<T> getData() {
        return this.value(DATA_KEY);
    }
    
    /**
     * 获取通用数据，如果不存在返回默认值
     */
    public <T> T getData(T defaultValue) {
        return this.<T>value(DATA_KEY).orElse(defaultValue);
    }
    
    /**
     * 获取字符串数据
     */
    public String getDataAsString() {
        return getData("").toString();
    }
    
    /**
     * 打印当前状态信息（用于调试）
     */
    public void printState() {
        System.out.println("=== 当前状态 ===");
        System.out.println("Messages: " + getMessages());
        System.out.println("Data: " + getData().orElse("null"));
        System.out.println("===============");
    }
}
