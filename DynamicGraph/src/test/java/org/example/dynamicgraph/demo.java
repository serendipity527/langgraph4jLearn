package org.example.dynamicgraph;

import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class demo {
    /**
     * 意图类型枚举
     */
    enum IntentType {
        WELCOME,    // 欢迎场景
        CHAT,       // 聊天对话
        FOLLOWUP    // 跟进场景
    }

    static class IntentState extends AgentState {
        public static final String INPUT_KEY = "input";
        public static final String INTENT_KEY = "intent";
        public static final String OUTPUT_KEY = "output";
        public static final String MESSAGES_KEY = "messages";


        public static final Map<String, Channel<?>> SCHEMA = Map.of(
                INPUT_KEY, Channels.base(() -> ""),
                INTENT_KEY, Channels.base(() -> ""),
                OUTPUT_KEY, Channels.base(() -> ""),
                MESSAGES_KEY, Channels.appender(ArrayList::new)
        );
        /**
         * Constructs an AgentState with the given initial data.
         *
         * @param initData the initial data for the agent state
         */
        public IntentState(Map<String, Object> initData) {
            super(initData);
        }
        public String getInput() {
            return this.<String>value(INPUT_KEY).orElse("");
        }

        public String getIntent() {
            return this.<String>value(INTENT_KEY).orElse("");
        }

        public String getOutput() {
            return this.<String>value(OUTPUT_KEY).orElse("");
        }

        public List<String> getMessages() {
            return this.<List<String>>value(MESSAGES_KEY).orElse(new ArrayList<>());
        }
    }
    /**
     * 意图识别节点
     */
    private Map<String, Object> intentRecognitionNode(IntentState state) {
        String input = state.getInput();
        System.out.println("🔍 意图识别节点 - 输入: " + input);

        // 简单的意图识别逻辑
        String intent;
        if (input.contains("你好") || input.contains("hello") || input.contains("hi")) {
            intent = IntentType.WELCOME.name();
        } else if (input.contains("继续") || input.contains("跟进") || input.contains("follow")) {
            intent = IntentType.FOLLOWUP.name();
        } else {
            intent = IntentType.CHAT.name();
        }

        System.out.println("📋 识别结果: " + intent);
        return Map.of(
                IntentState.INTENT_KEY, intent,
                IntentState.MESSAGES_KEY, List.of("意图识别完成: " + intent)
        );
    }
    private  Map<String,Object> welcome(IntentState state) {
        System.out.println("👋 WELCOME场景 - 处理欢迎请求");
        String response = "欢迎您！很高兴为您服务。请问有什么可以帮助您的？";
        return Map.of(
                IntentState.OUTPUT_KEY, response,
                IntentState.MESSAGES_KEY, List.of(response)
        );
    }
    /**
     * CHAT对话节点
     */
    private Map<String, Object> chatSceneNode(IntentState state) {
        System.out.println("💬 CHAT对话 - 处理聊天请求");
        String input = state.getInput();
        String response = "收到您的消息：'" + input + "'。这是一个普通的聊天对话回复。";
        return Map.of(
                IntentState.OUTPUT_KEY, response,
                IntentState.MESSAGES_KEY, List.of("CHAT对话处理完成")
        );
    }

    /**
     * FollowUP场景节点
     */
    private Map<String, Object> followUpSceneNode(IntentState state) {
        System.out.println("📎 FollowUP场景 - 处理跟进请求");
        String response = "好的，我来帮您跟进之前的问题。请告诉我具体需要跟进什么内容？";
        return Map.of(
                IntentState.OUTPUT_KEY, response,
                IntentState.MESSAGES_KEY, List.of("FollowUP场景处理完成")
        );
    }

    /**
     * 条件路由函数
     */
    private String routeByIntent(IntentState state) {
        String intent = state.getIntent();
        System.out.println("🔀 条件路由 - 当前意图: " + intent);
        return intent;
    }

    @Test
    public void test() throws GraphStateException {
        // 构建图
        StateGraph<IntentState> stateGraph = new StateGraph<>(
                IntentState.SCHEMA,
                IntentState::new
        );

        // 添加节点
        stateGraph.addNode("intentRecognition", node_async(this::intentRecognitionNode));
        stateGraph.addNode("welcomeScene", node_async(this::welcome));
        stateGraph.addNode("chatScene", node_async(this::chatSceneNode));
        stateGraph.addNode("followUpScene", node_async(this::followUpSceneNode));
        // 添加边
        // START -> 意图识别
        stateGraph.addEdge(START, "intentRecognition");

        // 意图识别 -> 条件分支
        stateGraph.addConditionalEdges(
                "intentRecognition",
                edge_async(this::routeByIntent),
                Map.of(
                        IntentType.WELCOME.name(), "welcomeScene",
                        IntentType.CHAT.name(), "chatScene",
                        IntentType.FOLLOWUP.name(), "followUpScene"
                )
        );

        // 各场景 -> END
        stateGraph.addEdge("welcomeScene", END);
        stateGraph.addEdge("chatScene", END);
        stateGraph.addEdge("followUpScene", END);

        // 编译图
        CompiledGraph<IntentState> graph = stateGraph.compile();

        // 打印Mermaid图
        System.out.println("\n📊 Mermaid 流程图:");
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).content());

        String input = "你好，我是新用户";
        Map<String, Object> inputMap = Map.of(IntentState.INPUT_KEY, input);

        for (NodeOutput<IntentState> output : graph.stream(inputMap)) {
            System.out.println("  节点: " + output.node());
        }

    }
}
