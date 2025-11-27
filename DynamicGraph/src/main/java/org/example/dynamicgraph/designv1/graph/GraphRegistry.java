package org.example.dynamicgraph.designv1.graph;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.example.dynamicgraph.designv1.graph.DynamicGraphBuilder.DynamicAgentState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图注册表 - 管理多个 LangGraph4j 图（多租户场景）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraphRegistry {
    
    private final DynamicGraphBuilder graphBuilder;
    
    /**
     * 图 ID -> 编译后的 LangGraph4j 图
     */
    private final Map<String, CompiledGraph<DynamicAgentState>> graphs = new ConcurrentHashMap<>();
    
    /**
     * 注册图（从配置）
     */
    public CompiledGraph<DynamicAgentState> register(GraphConfig config) throws GraphStateException {
        CompiledGraph<DynamicAgentState> graph = graphBuilder.build(config);
        graphs.put(config.getId(), graph);
        log.info("Graph registered: {}", config.getId());
        return graph;
    }
    
    /**
     * 注册图（从 JSON）
     */
    public CompiledGraph<DynamicAgentState> registerFromJson(String graphId, String json) throws Exception {
        CompiledGraph<DynamicAgentState> graph = graphBuilder.buildFromJson(json);
        graphs.put(graphId, graph);
        log.info("Graph registered from JSON: {}", graphId);
        return graph;
    }
    
    /**
     * 获取图
     */
    public Optional<CompiledGraph<DynamicAgentState>> get(String graphId) {
        return Optional.ofNullable(graphs.get(graphId));
    }
    
    /**
     * 获取图（不存在则抛异常）
     */
    public CompiledGraph<DynamicAgentState> getOrThrow(String graphId) {
        CompiledGraph<DynamicAgentState> graph = graphs.get(graphId);
        if (graph == null) {
            throw new IllegalArgumentException("Graph not found: " + graphId);
        }
        return graph;
    }
    
    /**
     * 移除图
     */
    public void unregister(String graphId) {
        CompiledGraph<DynamicAgentState> graph = graphs.remove(graphId);
        if (graph != null) {
            log.info("Graph unregistered: {}", graphId);
        }
    }
    
    /**
     * 检查图是否存在
     */
    public boolean contains(String graphId) {
        return graphs.containsKey(graphId);
    }
    
    /**
     * 获取所有图 ID
     */
    public Set<String> getGraphIds() {
        return graphs.keySet();
    }
    
    /**
     * 获取注册的图数量
     */
    public int size() {
        return graphs.size();
    }
    
    /**
     * 清理
     */
    public void clear() {
        log.info("Clearing {} graphs", graphs.size());
        graphs.clear();
    }
}
