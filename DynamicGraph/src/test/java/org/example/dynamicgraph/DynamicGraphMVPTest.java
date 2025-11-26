package org.example.dynamicgraph;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.example.dynamicgraph.builder.DynamicGraphBuilder;
import org.example.dynamicgraph.config.TenantGraphConfig;
import org.example.dynamicgraph.core.DynamicState;
import org.example.dynamicgraph.factory.TestConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 动态图 MVP 核心验证测试
 * 
 * 验证目标：
 * 1. 同一个构建器能根据不同配置生成不同行为的图
 * 2. 图能正确执行并产生预期结果
 * 3. 不同租户的图相互独立
 */
@DisplayName("动态图构建 MVP 测试")
class DynamicGraphMVPTest {
    
    private DynamicGraphBuilder graphBuilder;
    
    @BeforeEach
    void setUp() {
        graphBuilder = new DynamicGraphBuilder();
        System.out.println("\n" + "=".repeat(50));
        System.out.println("开始新的测试用例");
        System.out.println("=".repeat(50));
    }
    
    @Test
    @DisplayName("租户A - 简单串行处理测试")
    void testTenantASimpleFlow() throws GraphStateException {
        // 1. 获取租户A配置
        TenantGraphConfig config = TestConfigFactory.createTenantAConfig();
        System.out.println("租户A配置: " + config);
        
        // 2. 构建图
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        assertNotNull(graph, "图构建失败");
        
        // 3. 执行图
        Map<String, Object> initialData = Map.of(DynamicState.DATA_KEY, "hello");
        
        DynamicState finalState = null;
        for (NodeOutput<DynamicState> output : graph.stream(initialData)) {
            finalState = output.state();
            System.out.println("中间状态: " + finalState.getDataAsString());
        }
        
        // 4. 验证结果
        assertNotNull(finalState, "最终状态不应为null");
        String result = finalState.getDataAsString();
        
        System.out.println("最终结果: " + result);
        assertEquals("HELLO", result, "租户A应该将输入转为大写");
        
        // 验证消息记录
        assertTrue(finalState.getMessages().size() > 0, "应该有执行日志");
    }
    
    @Test
    @DisplayName("租户B - 文本处理流程测试")
    void testTenantBTextProcessing() throws GraphStateException {
        // 1. 获取租户B配置
        TenantGraphConfig config = TestConfigFactory.createTenantBConfig();
        System.out.println("租户B配置: " + config);
        
        // 2. 构建图
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        assertNotNull(graph, "图构建失败");
        
        // 3. 执行图
        Map<String, Object> initialData = Map.of(DynamicState.DATA_KEY, "hello");
        
        DynamicState finalState = null;
        for (NodeOutput<DynamicState> nodeOutput : graph.stream(initialData)) {
            finalState = nodeOutput.state();
            System.out.println("中间状态: " + finalState.getDataAsString());
        }
        
        // 4. 验证结果
        assertNotNull(finalState, "最终状态不应为null");
        String result = finalState.getDataAsString();
        
        System.out.println("最终结果: " + result);
        assertEquals("HELLO-TEST", result, "租户B应该追加后缀并转大写");
        
        // 验证消息记录
        assertTrue(finalState.getMessages().size() > 0, "应该有执行日志");
    }
    
    @Test
    @DisplayName("租户C - 最简流程测试")
    void testTenantCMinimalFlow() throws GraphStateException {
        // 1. 获取租户C配置
        TenantGraphConfig config = TestConfigFactory.createTenantCConfig();
        System.out.println("租户C配置: " + config);
        
        // 2. 构建图
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        assertNotNull(graph, "图构建失败");
        
        // 3. 执行图
        Map<String, Object> initialData = Map.of(
            DynamicState.DATA_KEY, "simple-test",
            DynamicState.MESSAGES_KEY, "初始消息"
        );
        
        DynamicState finalState = null;
        for (NodeOutput<DynamicState> graphOutput : graph.stream(initialData)) {
            finalState = graphOutput.state();
            System.out.println("中间状态: " + finalState.getDataAsString());
        }
        
        // 4. 验证结果
        assertNotNull(finalState, "最终状态不应为null");
        String result = finalState.getDataAsString();
        
        System.out.println("最终结果: " + result);
        assertEquals("simple-test", result, "租户C应该保持原始数据不变");
        
        // 验证消息记录
        assertTrue(finalState.getMessages().size() > 1, "应该包含初始消息和日志消息");
    }
    
    @Test
    @DisplayName("动态配置验证 - 同一构建器不同行为")
    void testDynamicConfigurationSameBuilderdifferentBehavior() throws GraphStateException {
        System.out.println("=== 验证同一构建器生成不同行为的图 ===");
        
        // 使用相同的输入数据
        String inputText = "test";
        Map<String, Object> inputData = Map.of(DynamicState.DATA_KEY, inputText);
        
        // 1. 租户A处理
        CompiledGraph<DynamicState> graphA = graphBuilder.buildGraph(TestConfigFactory.createTenantAConfig());
        DynamicState resultA = getLastState(graphA, inputData);
        
        // 2. 租户B处理  
        CompiledGraph<DynamicState> graphB = graphBuilder.buildGraph(TestConfigFactory.createTenantBConfig());
        DynamicState resultB = getLastState(graphB, inputData);
        
        // 3. 验证不同的处理结果
        String outputA = resultA.getDataAsString();
        String outputB = resultB.getDataAsString();
        
        System.out.println("输入: " + inputText);
        System.out.println("租户A输出: " + outputA);
        System.out.println("租户B输出: " + outputB);
        
        assertNotEquals(outputA, outputB, "不同租户应该产生不同的输出");
        assertEquals("TEST", outputA, "租户A: 仅转大写");
        assertEquals("TEST-TEST", outputB, "租户B: 追加后缀并转大写");
    }
    
    /**
     * 辅助方法：获取图执行的最终状态
     */
    private DynamicState getLastState(CompiledGraph<DynamicState> graph, Map<String, Object> input) {
        DynamicState lastState = null;
        for (NodeOutput<DynamicState> graphResult : graph.stream(input)) {
            lastState = graphResult.state();
        }
        return lastState;
    }
}
