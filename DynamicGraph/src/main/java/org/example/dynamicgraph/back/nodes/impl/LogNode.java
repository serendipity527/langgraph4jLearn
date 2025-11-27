package org.example.dynamicgraph.back.nodes.impl;

import org.example.dynamicgraph.back.core.DynamicState;
import org.example.dynamicgraph.back.nodes.DynamicNode;

import java.util.Map;

/**
 * 日志打印节点
 * 用于调试和状态查看
 */
public class LogNode implements DynamicNode {
    
    @Override
    public Map<String, Object> execute(Map<String, Object> state, Map<String, Object> params) {
        String prefix = (String) params.getOrDefault("prefix", "[LOG]");
        
        System.out.println(prefix + " 当前状态:");
        System.out.println("  Messages: " + state.get(DynamicState.MESSAGES_KEY));
        System.out.println("  Data: " + state.get(DynamicState.DATA_KEY));
        
        // 在messages中添加日志记录
        String logMessage = prefix + " 节点执行完成";
        return Map.of(DynamicState.MESSAGES_KEY, logMessage);
    }
    
    @Override
    public String getNodeType() {
        return "logNode";
    }
}
