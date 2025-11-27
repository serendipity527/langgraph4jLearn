package org.example.dynamicgraph.designv1.node;

import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 节点注册表 - 管理所有 NodeAction 实例
 */
@Component
public class NodeRegistry {
    
    @Autowired(required = false)
    private ApplicationContext applicationContext;
    
    /**
     * 节点类型 -> NodeAction 实例
     */
    private final Map<String, NodeAction<AgentState>> registry = new HashMap<>();
    
    @PostConstruct
    public void init() {
        if (applicationContext != null) {
            // 自动扫描所有 NodeAction 实现
            Map<String, NodeAction> beans = applicationContext.getBeansOfType(NodeAction.class);
            beans.forEach((beanName, bean) -> {
                NodeAction<AgentState> action = bean;
                registry.put(beanName, action);
            });
        }
    }
    
    /**
     * 手动注册节点
     */
    public void register(String type, NodeAction<AgentState> node) {
        registry.put(type, node);
    }
    
    /**
     * 获取节点
     */
    public NodeAction<AgentState> get(String type) {
        NodeAction<AgentState> node = registry.get(type);
        if (node == null) {
            throw new IllegalArgumentException("Unknown node type: " + type);
        }
        return node;
    }
    
    /**
     * 获取所有已注册的节点类型
     */
    public Set<String> getRegisteredTypes() {
        return Collections.unmodifiableSet(registry.keySet());
    }
    
    /**
     * 检查节点是否存在
     */
    public boolean contains(String type) {
        return registry.containsKey(type);
    }
    
    /**
     * 获取注册的节点数量
     */
    public int size() {
        return registry.size();
    }
}
