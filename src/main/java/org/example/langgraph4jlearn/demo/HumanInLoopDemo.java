package org.example.langgraph4jlearn.demo;

import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.example.langgraph4jlearn.agent.state.HumanState;

import java.util.Map;
import java.util.Scanner;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 人在回路演示
 * 展示如何在工作流中集成人工干预和决策
 */
public class HumanInLoopDemo {

    /**
     * 步骤1节点：初始处理
     */
    private static final AsyncNodeAction<HumanState> step1 = node_async(state -> {
        System.out.println("\n=== 执行步骤1 ===");
        System.out.println("当前消息: " + state.messages());
        
        int counter = state.stepCounter() + 1;
        
        return Map.of(
                "messages", "步骤1完成 (第" + counter + "次)",
                "step_counter", counter
        );
    });

    /**
     * 人工反馈节点：等待人工输入
     * 这个节点本身不做任何处理，只是一个占位符
     * 实际的人工输入通过updateState来完成
     */
    private static final AsyncNodeAction<HumanState> humanFeedback = node_async(state -> {
        System.out.println("\n=== 等待人工反馈 ===");
        System.out.println("当前状态: " + state.data());
        
        // 这里不做任何处理，等待外部通过updateState更新状态
        return Map.of();
    });

    /**
     * 步骤3节点：最终处理
     */
    private static final AsyncNodeAction<HumanState> step3 = node_async(state -> {
        System.out.println("\n=== 执行步骤3 ===");
        System.out.println("收到的人工反馈: " + state.humanFeedback().orElse("无"));
        
        return Map.of("messages", "步骤3完成 - 工作流结束");
    });

    /**
     * 评估人工反馈的边
     * 根据反馈内容决定下一步去哪里
     */
    private static final AsyncEdgeAction<HumanState> evalHumanFeedback = edge_async(state -> {
        String feedback = state.humanFeedback()
                .orElseThrow(() -> new IllegalStateException("需要人工反馈"));
        
        System.out.println("评估反馈: " + feedback);
        
        // 根据反馈决定路由
        return switch (feedback.toLowerCase()) {
            case "next" -> "next";      // 继续下一步
            case "back" -> "back";      // 返回步骤1
            default -> "unknown";        // 未知指令，重新等待
        };
    });

    /**
     * 构建工作流图
     */
    public static StateGraph<HumanState> buildWorkflow() throws GraphStateException {
        return new StateGraph<>(HumanState.SCHEMA, HumanState::new)
                .addNode("step_1", step1)
                .addNode("human_feedback", humanFeedback)
                .addNode("step_3", step3)
                .addEdge(START, "step_1")
                .addEdge("step_1", "human_feedback")
                .addConditionalEdges("human_feedback", evalHumanFeedback,
                        Map.of(
                                "back", "step_1",
                                "next", "step_3",
                                "unknown", "human_feedback"
                        ))
                .addEdge("step_3", END);
    }

    /**
     * 运行交互式演示
     */
    public static void runInteractiveDemo() throws Exception {
        System.out.println("========================================");
        System.out.println("     人在回路工作流交互式演示");
        System.out.println("========================================");

        // 构建工作流
        var workflow = buildWorkflow();

        // 配置内存检查点保存器
        var saver = new MemorySaver();

        // 编译配置：在human_feedback节点前中断
        var compileConfig = CompileConfig.builder()
                .checkpointSaver(saver)
                .interruptBefore("human_feedback")
                .build();

        // 编译图
        var graph = workflow.compile(compileConfig);

        // 初始输入
        Map<String, Object> initialInput = Map.of(
                "messages", "工作流开始",
                "step_counter", 0
        );

        // 线程配置
        var invokeConfig = RunnableConfig.builder()
                .threadId("demo-thread-1")
                .build();

        System.out.println("\n>>> 启动工作流...");

        // 第一次运行：执行到第一个中断点
        for (var event : graph.stream(initialInput, invokeConfig)) {
//            System.out.println("事件: " + event);
        }

        // 创建Scanner用于用户输入
        Scanner scanner = new Scanner(System.in);
        boolean continueLoop = true;

        while (continueLoop) {
            // 获取当前状态
            var currentState = graph.getState(invokeConfig);
//            System.out.println("\n>>> 当前状态:");
////            System.out.println(currentState);
//            System.out.println("\n>>> 下一个节点: " + currentState.next());

            // 如果下一个节点是__END__或者没有下一个节点，说明已完成
            if (currentState.next() == null || "__END__".equals(currentState.next())) {
                System.out.println("\n✓ 工作流已完成！");
                continueLoop = false;
                continue;
            }

            // 等待用户输入
            System.out.println("\n>>> 请输入反馈 (next=继续下一步, back=返回步骤1, 其他=重新输入): ");
            String userInput = scanner.nextLine().trim();

            if (userInput.isEmpty()) {
                userInput = "next"; // 默认值
            }

            System.out.println("收到输入: " + userInput);

            // 更新状态（模拟human_feedback节点的输出）
            var updateConfig = graph.updateState(
                    invokeConfig,
                    Map.of("human_feedback", userInput),
                    null
            );

            System.out.println("\n>>> 继续执行工作流...");

            // 继续执行
            for (var event : graph.stream(GraphInput.resume(), updateConfig)) {
//                System.out.println("事件: " + event);
            }
        }

        scanner.close();
        System.out.println("\n>>> 演示结束！");
    }

    /**
     * 主函数
     */
    public static void main(String[] args) {
        try {
            runInteractiveDemo();
        } catch (Exception e) {
            System.err.println("执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
