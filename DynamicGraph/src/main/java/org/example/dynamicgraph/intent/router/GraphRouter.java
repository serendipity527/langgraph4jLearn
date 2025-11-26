package org.example.dynamicgraph.intent.router;

/**
 * 图路由接口 - 条件分支路由器需要实现此接口
 */
public interface GraphRouter<S> {
    
    /**
     * 根据状态返回下一个节点的路由key
     * @param state 当前状态
     * @return 路由key（对应conditionalEdges.paths中的key）
     */
    String route(S state);
}
