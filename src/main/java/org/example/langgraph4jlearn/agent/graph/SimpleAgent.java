package org.example.langgraph4jlearn.agent.graph;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.example.langgraph4jlearn.agent.node.GreeterNode;
import org.example.langgraph4jlearn.agent.node.ResponderNode;
import org.example.langgraph4jlearn.agent.state.SimpleAgentState;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 简单Agent图
 * 这是一个最基础的LangGraph4j实现示例
 * 
 * 图结构:
 * START -> greeter -> responder -> END
 */
@Slf4j
public class SimpleAgent {
    
    private final GreeterNode greeterNode;
    private final ResponderNode responderNode;
    private StateGraph<SimpleAgentState> graph;
    
    public SimpleAgent() {
        this.greeterNode = new GreeterNode();
        this.responderNode = new ResponderNode();
        buildGraph();
    }
    
    /**
     * 构建 Agent图
     */
    private void buildGraph() {
        try {
            log.info("开始构建SimpleAgent图...");
            
            // 创建StateGraph实例，传入Schema和状态工厂函数
            this.graph = new StateGraph<>(
                    SimpleAgentState.SCHEMA,
                    SimpleAgentState::new
            );
            
            // 添加节点
            graph.addNode("greeter", node_async(greeterNode));
            graph.addNode("responder", node_async(responderNode));
            
            // 添加边：定义流程
            graph.addEdge(START, "greeter");        // START -> greeter
            graph.addEdge("greeter", "responder");  // greeter -> responder
            graph.addEdge("responder", END);        // responder -> END
            
            log.info("SimpleAgent图构建完成");
            
        } catch (Exception e) {
            log.error("构建图失败", e);
            throw new RuntimeException("构建SimpleAgent图失败", e);
        }
    }
    
    /**
     * 执行Agent
     * 
     * @param initialMessage 初始消息
     * @throws GraphStateException 图执行异常
     */
    public void execute(String initialMessage) throws GraphStateException {
        log.info("SimpleAgent开始执行，初始消息: {}", initialMessage);
        
        // 编译图
        var compiledGraph = graph.compile();
        
        // 创建初始状态（使用Map）
        Map<String, Object> initialState = Map.of(
                SimpleAgentState.MESSAGES_KEY, initialMessage
        );
        
        // 流式执行图
        for (var nodeOutput : compiledGraph.stream(initialState)) {
            log.info("节点输出: {}", nodeOutput);
        }
        
        log.info("SimpleAgent执行完成");
    }
    
    /**
     * 执行并返回最终状态
     * 
     * @param initialMessage 初始消息
     * @return 最终状态
     * @throws Exception 执行异常
     */
    public SimpleAgentState executeAndGetState(String initialMessage) throws Exception {
        log.info("SimpleAgent开始执行，初始消息: {}", initialMessage);
        
        // 编译图
        var compiledGraph = graph.compile();
        
        // 创建初始状态
        Map<String, Object> initialState = Map.of(
                SimpleAgentState.MESSAGES_KEY, initialMessage
        );
        
        // 执行图并获取最终状态
        var result = compiledGraph.invoke(initialState);
        SimpleAgentState finalState = result.get();
        
        log.info("SimpleAgent执行完成，最终消息: {}", finalState.messages());
        
        return finalState;
    }
}
