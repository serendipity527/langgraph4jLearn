package org.example.dynamicgraph.intent.node;

import java.util.Map;

/**
 * 图节点接口 - 所有节点Bean都需要实现此接口
 */
public interface GraphNode<S> {
    
    /**
     * 执行节点逻辑
     * @param state 当前状态
     * @return 更新后的状态Map
     */
    Map<String, Object> execute(S state);
}
