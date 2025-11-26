package org.example.dynamicgraph.builder;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.dynamicgraph.config.EdgeConfig;
import org.example.dynamicgraph.config.NodeConfig;
import org.example.dynamicgraph.config.TenantGraphConfig;
import org.example.dynamicgraph.core.DynamicState;
import org.example.dynamicgraph.nodes.DynamicNode;
import org.example.dynamicgraph.registry.NodeRegistry;
import org.example.dynamicgraph.router.ConditionalRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 动态图构建器
 * 核心类：根据配置动态构建 LangGraph4j 图
 */
public class DynamicGraphBuilder {
    
    private final NodeRegistry nodeRegistry;
    
    public DynamicGraphBuilder() {
        this.nodeRegistry = new NodeRegistry();
    }
    
    public DynamicGraphBuilder(NodeRegistry nodeRegistry) {
        this.nodeRegistry = nodeRegistry;
    }
    
    /**
     * 根据租户配置构建图
     * 
     * @param config 租户图配置
     * @return 编译后的可执行图
     */
    public CompiledGraph<DynamicState> buildGraph(TenantGraphConfig config) throws GraphStateException {
        System.out.println("=== 开始构建动态图 ===");
        System.out.println("租户: " + config.getTenantId());
        System.out.println("图名: " + config.getGraphName());
        
        // 1. 创建StateGraph
        StateGraph<DynamicState> stateGraph = new StateGraph<>(
            DynamicState.SCHEMA,
            initData -> new DynamicState(initData)
        );
        
        // 2. 添加节点
        for (NodeConfig nodeConfig : config.getNodes()) {
            addNodeToGraph(stateGraph, nodeConfig);
        }
        
        // 3. 添加边（普通边和条件边）
        addEdgesToGraph(stateGraph, config.getEdges());
        
        // 4. 编译图
        CompiledGraph<DynamicState> compiledGraph = stateGraph.compile();
        
        System.out.println("=== 图构建完成 ===");
        return compiledGraph;
    }
    
    /**
     * 向图中添加节点
     */
    private void addNodeToGraph(StateGraph<DynamicState> stateGraph, NodeConfig nodeConfig) throws GraphStateException {
        System.out.println("添加节点: " + nodeConfig.getId() + " (类型: " + nodeConfig.getType() + ")");
        
        // 创建节点实例
        DynamicNode dynamicNode = nodeRegistry.createNode(nodeConfig.getType());
        
        // 包装为 NodeAction
        NodeAction<DynamicState> nodeAction = state -> {
            System.out.println("执行节点: " + nodeConfig.getId());
            
            // 调用动态节点的执行方法
            Map<String, Object> result = dynamicNode.execute(state.data(), nodeConfig.getParams());
            
            return result;
        };
        
        // 添加到图中
        stateGraph.addNode(nodeConfig.getId(), node_async(nodeAction));
    }
    
    /**
     * 批量添加边到图中（暂时将条件边作为普通边处理）
     */
    private void addEdgesToGraph(StateGraph<DynamicState> stateGraph, List<EdgeConfig> edges) throws GraphStateException {
        System.out.println("📊 图边配置分析:");
        System.out.println("总边数: " + edges.size());
        
        long conditionalCount = edges.stream().filter(EdgeConfig::isConditional).count();
        System.out.println("条件边数: " + conditionalCount + " (暂时作为普通边处理)");
        System.out.println("普通边数: " + (edges.size() - conditionalCount));
        
        // 暂时将所有边都作为普通边处理，避免复杂的条件路由
        for (EdgeConfig edge : edges) {
            if (edge.isConditional()) {
                System.out.println("⚠️ 条件边暂时简化处理: " + edge.getFrom() + " -> " + edge.getTo() 
                                 + " (条件: " + edge.getCondition() + ")");
            }
            addSingleEdge(stateGraph, edge);
        }
        
        if (conditionalCount > 0) {
            System.out.println("ℹ️ 注意：条件边功能正在开发中，当前所有边都作为普通边处理");
        }
    }
    
    /**
     * 添加条件边 - 暂时使用简化实现
     * TODO: 完善条件路由API调用
     */
    private void addConditionalEdges(StateGraph<DynamicState> stateGraph, 
                                   String sourceNode, 
                                   List<EdgeConfig> conditionalEdges) throws GraphStateException {
        
        System.out.println("添加条件边组: " + sourceNode + " -> [条件路由]");
        
        // 暂时只添加第一个条件边作为普通边
        // TODO: 实现真正的条件路由逻辑
        if (!conditionalEdges.isEmpty()) {
            EdgeConfig firstEdge = conditionalEdges.get(0);
            System.out.println("  暂时使用普通边: " + firstEdge.getCondition() + " -> " + firstEdge.getTo());
            addSingleEdge(stateGraph, new EdgeConfig(sourceNode, firstEdge.getTo()));
        }
    }
    
    /**
     * 添加单个普通边
     */
    private void addSingleEdge(StateGraph<DynamicState> stateGraph, EdgeConfig edgeConfig) throws GraphStateException {
        System.out.println("添加边: " + edgeConfig.getFrom() + " -> " + edgeConfig.getTo());
        
        String from = edgeConfig.getFrom();
        String to = edgeConfig.getTo();
        
        // 处理特殊节点标识
        if ("START".equals(from)) {
            from = START;
        }
        if ("END".equals(to)) {
            to = END;
        }
        
        stateGraph.addEdge(from, to);
    }
    
    /**
     * 获取节点注册表（用于扩展）
     */
    public NodeRegistry getNodeRegistry() {
        return nodeRegistry;
    }
}
