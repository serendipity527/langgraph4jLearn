package org.example.dynamicgraph.intent.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.example.dynamicgraph.intent.config.GraphConfig;
import org.example.dynamicgraph.intent.node.GraphNode;
import org.example.dynamicgraph.intent.router.GraphRouter;
import org.example.dynamicgraph.intent.state.IntentState;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 意图图构建器 - 根据JSON配置动态构建图
 */
@Component
public class IntentGraphBuilder {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    public IntentGraphBuilder(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 从JSON字符串构建图
     */
    public CompiledGraph<IntentState> buildFromJson(String json) throws IOException, GraphStateException {
        GraphConfig config = objectMapper.readValue(json, GraphConfig.class);
        return build(config);
    }

    /**
     * 从配置对象构建图
     */
    public CompiledGraph<IntentState> build(GraphConfig config) throws GraphStateException {
        System.out.println("=== 开始构建动态图 ===");
        System.out.println("图ID: " + config.getMeta().getGraphId());
        System.out.println("描述: " + config.getMeta().getDescription());

        // 1. 创建StateGraph
        StateGraph<IntentState> stateGraph = new StateGraph<>(
            IntentState.SCHEMA,
            IntentState::new
        );

        // 2. 添加节点
        for (GraphConfig.NodeDef nodeDef : config.getNodes()) {
            addNode(stateGraph, nodeDef);
        }

        // 3. 添加入口边
        String entryPoint = config.getSettings().getEntryPoint();
        System.out.println("添加入口边: START -> " + entryPoint);
        stateGraph.addEdge(START, entryPoint);

        // 4. 添加普通边
        if (config.getEdges() != null) {
            for (GraphConfig.EdgeDef edgeDef : config.getEdges()) {
                addEdge(stateGraph, edgeDef);
            }
        }

        // 5. 添加条件边
        if (config.getConditionalEdges() != null) {
            for (GraphConfig.ConditionalEdgeDef condEdgeDef : config.getConditionalEdges()) {
                addConditionalEdge(stateGraph, condEdgeDef);
            }
        }

        // 6. 编译图
        CompiledGraph<IntentState> compiledGraph = stateGraph.compile();
        System.out.println("=== 图构建完成 ===");
        
        return compiledGraph;
    }

    /**
     * 添加节点
     */
    @SuppressWarnings("unchecked")
    private void addNode(StateGraph<IntentState> stateGraph, GraphConfig.NodeDef nodeDef) throws GraphStateException {
        String nodeId = nodeDef.getId();
        String componentName = nodeDef.getComponent();
        
        System.out.println("添加节点: " + nodeId + " -> Bean: " + componentName);
        
        // 从Spring容器获取Bean
        GraphNode<IntentState> nodeBean = (GraphNode<IntentState>) applicationContext.getBean(componentName);
        
        // 添加到图中
        stateGraph.addNode(nodeId, node_async(nodeBean::execute));
    }

    /**
     * 添加普通边
     */
    private void addEdge(StateGraph<IntentState> stateGraph, GraphConfig.EdgeDef edgeDef) throws GraphStateException {
        String from = edgeDef.getFrom();
        String to = edgeDef.getTo();
        
        // 处理特殊节点
        if ("END".equals(to)) {
            to = END;
        }
        if ("START".equals(from)) {
            from = START;
        }
        
        System.out.println("添加边: " + edgeDef.getFrom() + " -> " + edgeDef.getTo());
        stateGraph.addEdge(from, to);
    }

    /**
     * 添加条件边
     */
    @SuppressWarnings("unchecked")
    private void addConditionalEdge(StateGraph<IntentState> stateGraph, GraphConfig.ConditionalEdgeDef condEdgeDef) throws GraphStateException {
        String from = condEdgeDef.getFrom();
        String routerName = condEdgeDef.getRouter();
        Map<String, String> paths = condEdgeDef.getPaths();
        
        System.out.println("添加条件边: " + from + " -> Router: " + routerName);
        System.out.println("  路径映射: " + paths);
        
        // 从Spring容器获取Router Bean
        GraphRouter<IntentState> router = (GraphRouter<IntentState>) applicationContext.getBean(routerName);
        
        // 构建路径映射（处理END）
        Map<String, String> resolvedPaths = new HashMap<>();
        for (Map.Entry<String, String> entry : paths.entrySet()) {
            String target = "END".equals(entry.getValue()) ? END : entry.getValue();
            resolvedPaths.put(entry.getKey(), target);
        }
        
        // 添加条件边
        stateGraph.addConditionalEdges(
            from,
            edge_async(router::route),
            resolvedPaths
        );
    }
}
