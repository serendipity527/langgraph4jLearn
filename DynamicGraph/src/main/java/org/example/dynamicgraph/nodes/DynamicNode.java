package org.example.dynamicgraph.nodes;

import java.util.Map;

/**
 * 动态节点接口
 * 所有可动态配置的节点都需要实现这个接口
 */
public interface DynamicNode {
    
    /**
     * 执行节点逻辑
     * 
     * @param state 当前状态数据
     * @param params 节点配置参数
     * @return 返回需要更新到状态中的数据
     */
    Map<String, Object> execute(Map<String, Object> state, Map<String, Object> params);
    
    /**
     * 获取节点类型标识
     */
    String getNodeType();
}
