package org.example.dynamicgraph.back.registry;

import org.example.dynamicgraph.back.nodes.DynamicNode;
import org.example.dynamicgraph.back.nodes.impl.AppendNode;
import org.example.dynamicgraph.back.nodes.impl.DecisionNode;
import org.example.dynamicgraph.back.nodes.impl.HumanApprovalNode;
import org.example.dynamicgraph.back.nodes.impl.LogNode;
import org.example.dynamicgraph.back.nodes.impl.RetryNode;
import org.example.dynamicgraph.back.nodes.impl.UpperCaseNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 节点注册中心
 * 管理所有可用的节点类型
 */
public class NodeRegistry {
    
    private final Map<String, Supplier<DynamicNode>> nodeFactories;
    
    public NodeRegistry() {
        this.nodeFactories = new HashMap<>();
        initializeBuiltinNodes();
    }
    
    /**
     * 初始化内置节点
     */
    private void initializeBuiltinNodes() {
        // 基础节点
        register("logNode", LogNode::new);
        register("upperCaseNode", UpperCaseNode::new);
        register("appendNode", AppendNode::new);
        
        // 复杂节点
        register("retryNode", RetryNode::new);
        register("decisionNode", DecisionNode::new);
        register("humanApprovalNode", HumanApprovalNode::new);
    }
    
    /**
     * 注册新的节点类型
     * 
     * @param nodeType 节点类型标识
     * @param factory 节点工厂函数
     */
    public void register(String nodeType, Supplier<DynamicNode> factory) {
        nodeFactories.put(nodeType, factory);
        System.out.println("[Registry] 注册节点类型: " + nodeType);
    }
    
    /**
     * 创建节点实例
     * 
     * @param nodeType 节点类型
     * @return 节点实例
     * @throws IllegalArgumentException 如果节点类型不存在
     */
    public DynamicNode createNode(String nodeType) {
        Supplier<DynamicNode> factory = nodeFactories.get(nodeType);
        if (factory == null) {
            throw new IllegalArgumentException("未知的节点类型: " + nodeType + 
                ". 可用类型: " + nodeFactories.keySet());
        }
        
        DynamicNode node = factory.get();
        System.out.println("[Registry] 创建节点实例: " + nodeType);
        return node;
    }
    
    /**
     * 获取所有可用的节点类型
     */
    public Map<String, Supplier<DynamicNode>> getAllNodeTypes() {
        return new HashMap<>(nodeFactories);
    }
    
    /**
     * 检查节点类型是否存在
     */
    public boolean hasNodeType(String nodeType) {
        return nodeFactories.containsKey(nodeType);
    }
}
