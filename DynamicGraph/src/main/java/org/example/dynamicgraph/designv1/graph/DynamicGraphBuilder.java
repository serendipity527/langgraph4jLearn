package org.example.dynamicgraph.designv1.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.example.dynamicgraph.designv1.edge.ConditionalEdgeConfig;
import org.example.dynamicgraph.designv1.edge.EdgeConfig;
import org.example.dynamicgraph.designv1.node.NodeConfig;
import org.example.dynamicgraph.designv1.node.NodeRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * JSON 配置 -> LangGraph4j 图 构建器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicGraphBuilder {

    private final NodeRegistry nodeRegistry;
    private final ObjectMapper objectMapper;

    /**
     * 动态状态（直接使用 LangGraph4j 的 AgentState）
     */
    public static class DynamicAgentState extends AgentState {

        public DynamicAgentState(Map<String, Object> initData) {
            super(initData);
        }

        public String getString(String key) {
            return this.<String>value(key).orElse("");
        }
    }

    /**
     * 从 GraphConfig 构建图
     */
    public CompiledGraph<DynamicAgentState> build(GraphConfig config) throws GraphStateException {
        log.info("Building graph: {}", config.getName());

        // 根据配置动态构建 Schema
        Map<String, Channel<?>> schema = buildSchema(config);

        StateGraph<DynamicAgentState> stateGraph = new StateGraph<>(
            schema,
            DynamicAgentState::new
        );

        // 添加节点
        for (NodeConfig nodeConfig : config.getNodes()) {
            addNode(stateGraph, nodeConfig);
        }

        // 添加边
        addEdges(stateGraph, config);

        return stateGraph.compile();
    }

    /**
     * 从 JSON 构建图
     */
    public CompiledGraph<DynamicAgentState> buildFromJson(String json) throws Exception {
        GraphConfig config = objectMapper.readValue(json, GraphConfig.class);
        return build(config);
    }

    private void addNode(StateGraph<DynamicAgentState> stateGraph, NodeConfig nodeConfig)
            throws GraphStateException {
        
        String nodeId = nodeConfig.getId();
        NodeAction<AgentState> nodeAction = nodeRegistry.get(nodeConfig.getComponentType());
        
        stateGraph.addNode(nodeId, node_async(state -> {
            log.debug("[{}] executing", nodeId);
            return nodeAction.apply(state);
        }));
    }

    private void addEdges(StateGraph<DynamicAgentState> stateGraph, GraphConfig config)
            throws GraphStateException {
        
        // 入口边
        stateGraph.addEdge(START, config.getEntryNode());

        // 普通边 - addEdge(from, to)
        for (EdgeConfig edge : config.getEdges()) {
            stateGraph.addEdge(edge.getFrom(), normalizeTarget(edge.getTo()));
        }

        // 条件边 - addConditionalEdges(from, condition, routes)
        for (ConditionalEdgeConfig edge : config.getConditionalEdges()) {
            addConditionalEdge(stateGraph, edge);
        }
    }

    private void addConditionalEdge(StateGraph<DynamicAgentState> stateGraph, 
                                    ConditionalEdgeConfig edge) throws GraphStateException {
        
        String from = edge.getFrom();
        String conditionKey = edge.getConditionKey();
        Map<String, String> routes = edge.getRoutes();
        String defaultTarget = edge.getDefaultTarget();
        
        // 标准化路由目标
        Map<String, String> normalizedRoutes = new HashMap<>();
        routes.forEach((k, v) -> normalizedRoutes.put(k, normalizeTarget(v)));
        
        if (defaultTarget != null) {
            normalizedRoutes.put("__default__", normalizeTarget(defaultTarget));
        }

        stateGraph.addConditionalEdges(
            from,
            edge_async(state -> {
                String value = state.getString(conditionKey);
                return normalizedRoutes.containsKey(value) ? value : "__default__";
            }),
            normalizedRoutes
        );
    }

    private String normalizeTarget(String target) {
        if (target == null || "__end__".equals(target) || "END".equals(target)) {
            return END;
        }
        return target;
    }

    /**
     * 根据配置动态构建 State Schema
     */
    private Map<String, Channel<?>> buildSchema(GraphConfig config) {
        Map<String, Channel<?>> schema = new HashMap<>();
        
        Map<String, GraphConfig.StateFieldConfig> stateSchema = config.getStateSchema();
        
        if (stateSchema == null || stateSchema.isEmpty()) {
            // 默认 Schema：只有 messages 是 appender
            schema.put("messages", Channels.appender(ArrayList::new));
            return schema;
        }

        // 根据配置构建 Schema
        stateSchema.forEach((fieldName, fieldConfig) -> {
            String type = fieldConfig.getType();
            if ("appender".equals(type)) {
                schema.put(fieldName, Channels.appender(ArrayList::new));
            }
            // "value" 类型不需要显式定义 Channel，LangGraph4j 默认就是覆盖模式
        });

        return schema;
    }
}
