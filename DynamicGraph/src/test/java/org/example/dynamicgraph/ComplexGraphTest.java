package org.example.dynamicgraph;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.example.dynamicgraph.builder.DynamicGraphBuilder;
import org.example.dynamicgraph.config.TenantGraphConfig;
import org.example.dynamicgraph.core.DynamicState;
import org.example.dynamicgraph.service.JsonConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 复杂图测试 - 条件边、循环、人在回路
 */
@DisplayName("复杂动态图测试")
class ComplexGraphTest {
    
    private DynamicGraphBuilder graphBuilder;
    private JsonConfigService jsonConfigService;
    
    @BeforeEach
    void setUp() {
        graphBuilder = new DynamicGraphBuilder();
        jsonConfigService = new JsonConfigService();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔥 复杂图构建测试 - 条件边、循环、人在回路");
        System.out.println("=".repeat(70));
    }
    
    @Test
    @DisplayName("重试循环图 - 失败重试机制")
    void testRetryLoopGraph() throws IOException, GraphStateException {
        String retryGraphJson = """
            {
              "tenantId": "retry_tenant",
              "graphName": "重试循环流程",
              "nodes": [
                {
                  "id": "retry",
                  "type": "retryNode",
                  "params": {
                    "failureRate": 0.6,
                    "maxRetries": 3
                  }
                },
                {
                  "id": "decision", 
                  "type": "decisionNode",
                  "params": {
                    "decisionType": "quality_check"
                  }
                },
                {
                  "id": "success",
                  "type": "appendNode",
                  "params": {
                    "suffix": "-final-success"
                  }
                },
                {
                  "id": "failure",
                  "type": "logNode",
                  "params": {
                    "prefix": "[最终失败]"
                  }
                }
              ],
              "edges": [
                {
                  "from": "START",
                  "to": "retry"
                },
                {
                  "from": "retry",
                  "to": "decision"
                },
                {
                  "from": "decision",
                  "to": "success",
                  "condition": "data_contains_success",
                  "conditional": true
                },
                {
                  "from": "decision",
                  "to": "retry", 
                  "condition": "retry_count_lt_3",
                  "conditional": true
                },
                {
                  "from": "decision",
                  "to": "failure",
                  "condition": "continue",
                  "conditional": true
                },
                {
                  "from": "success",
                  "to": "END"
                },
                {
                  "from": "failure",
                  "to": "END"
                }
              ]
            }
            """;
            
        System.out.println("📋 重试循环配置:");
        System.out.println(retryGraphJson);
        
        TenantGraphConfig config = jsonConfigService.parseFromJson(retryGraphJson);
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());
        // 显示图结构
        System.out.println("\n📊 Mermaid图表:");
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());
        
        // 执行图
        Map<String, Object> input = Map.of(DynamicState.DATA_KEY, "retry-test");
        DynamicState result = getLastState(graph, input);
        
        // 验证结果包含处理过程
        assertNotNull(result);
        String finalData = result.getDataAsString();
        System.out.println("🎯 最终结果: " + finalData);
        
        // 结果应该包含成功或失败标识
        assertTrue(finalData.contains("success") || finalData.contains("error"), 
                  "结果应该包含成功或失败标识");
    }
    
    @Test
    @DisplayName("人工审批流程 - 人在回路")
    void testHumanApprovalWorkflow() throws IOException, GraphStateException {
        String approvalGraphJson = """
            {
              "tenantId": "approval_tenant",
              "graphName": "人工审批流程",
              "nodes": [
                {
                  "id": "check",
                  "type": "decisionNode",
                  "params": {
                    "decisionType": "approval_needed"
                  }
                },
                {
                  "id": "autoApprove",
                  "type": "appendNode",
                  "params": {
                    "suffix": "-auto-approved"
                  }
                },
                {
                  "id": "humanApproval",
                  "type": "humanApprovalNode",
                  "params": {
                    "autoApprove": true
                  }
                },
                {
                  "id": "process",
                  "type": "upperCaseNode",
                  "params": {}
                },
                {
                  "id": "log",
                  "type": "logNode",
                  "params": {
                    "prefix": "[审批完成]"
                  }
                }
              ],
              "edges": [
                {
                  "from": "START",
                  "to": "check"
                },
                {
                  "from": "check",
                  "to": "autoApprove",
                  "condition": "data_contains_auto",
                  "conditional": true
                },
                {
                  "from": "check", 
                  "to": "humanApproval",
                  "condition": "need_human_approval",
                  "conditional": true
                },
                {
                  "from": "autoApprove",
                  "to": "process"
                },
                {
                  "from": "humanApproval",
                  "to": "process"
                },
                {
                  "from": "process",
                  "to": "log"
                },
                {
                  "from": "log",
                  "to": "END"
                }
              ]
            }
            """;
            
        System.out.println("📋 人工审批流程配置:");
        System.out.println(approvalGraphJson);
        
        TenantGraphConfig config = jsonConfigService.parseFromJson(approvalGraphJson);
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);

        // 显示图结构
        System.out.println("\n📊 Mermaid图表:");
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());
        
        // 测试需要人工审批的情况
        System.out.println("\n🧑‍💼 测试人工审批流程:");
        Map<String, Object> input = Map.of(DynamicState.DATA_KEY, "expensive-critical-item");
        DynamicState result = getLastState(graph, input);
        
        // 验证结果
        assertNotNull(result);
        String finalData = result.getDataAsString();
        System.out.println("🎯 审批结果: " + finalData);
        
        // 应该包含审批相关的标识
        assertTrue(finalData.contains("EXPENSIVE") || finalData.contains("CRITICAL"), 
                  "结果应该被正确处理");
    }
    
    @Test
    @DisplayName("多层条件分支图")
    void testMultiLevelConditionalGraph() throws IOException, GraphStateException {
        String multiLevelJson = """
            {
              "tenantId": "multi_level_tenant",
              "graphName": "多层条件分支流程",
              "nodes": [
                {
                  "id": "riskAssess",
                  "type": "decisionNode",
                  "params": {
                    "decisionType": "risk_assessment"
                  }
                },
                {
                  "id": "lowRisk",
                  "type": "appendNode",
                  "params": {
                    "suffix": "-low-risk-path"
                  }
                },
                {
                  "id": "mediumRisk",
                  "type": "appendNode",
                  "params": {
                    "suffix": "-medium-risk-path"
                  }
                },
                {
                  "id": "highRisk",
                  "type": "appendNode",
                  "params": {
                    "suffix": "-high-risk-path"
                  }
                },
                {
                  "id": "qualityCheck",
                  "type": "decisionNode",
                  "params": {
                    "decisionType": "quality_check"
                  }
                },
                {
                  "id": "finalProcess",
                  "type": "upperCaseNode",
                  "params": {}
                }
              ],
              "edges": [
                {
                  "from": "START",
                  "to": "riskAssess"
                },
                {
                  "from": "riskAssess",
                  "to": "lowRisk",
                  "condition": "data_contains_low_risk",
                  "conditional": true
                },
                {
                  "from": "riskAssess",
                  "to": "mediumRisk",
                  "condition": "data_contains_medium_risk",
                  "conditional": true
                },
                {
                  "from": "riskAssess",
                  "to": "highRisk",
                  "condition": "continue",
                  "conditional": true
                },
                {
                  "from": "lowRisk",
                  "to": "finalProcess"
                },
                {
                  "from": "mediumRisk",
                  "to": "qualityCheck"
                },
                {
                  "from": "highRisk",
                  "to": "qualityCheck"
                },
                {
                  "from": "qualityCheck",
                  "to": "finalProcess"
                },
                {
                  "from": "finalProcess",
                  "to": "END"
                }
              ]
            }
            """;
            
        System.out.println("📋 多层条件分支配置:");
        
        TenantGraphConfig config = jsonConfigService.parseFromJson(multiLevelJson);
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        
        // 显示图结构
        System.out.println("\n📊 Mermaid图表:");
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());
        
        // 测试不同输入的分支情况
        System.out.println("\n🔀 测试多种分支情况:");
        
        // 测试1: 普通情况 -> 高风险路径
        Map<String, Object> input1 = Map.of(DynamicState.DATA_KEY, "normal-request");
        DynamicState result1 = getLastState(graph, input1);
        System.out.println("普通请求结果: " + result1.getDataAsString());
        
        // 测试2: 重要情况 -> 中等风险路径
        Map<String, Object> input2 = Map.of(DynamicState.DATA_KEY, "important-request");
        DynamicState result2 = getLastState(graph, input2);
        System.out.println("重要请求结果: " + result2.getDataAsString());
        
        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.getDataAsString().length() > "normal-request".length());
        assertTrue(result2.getDataAsString().length() > "important-request".length());
    }
    
    /**
     * 辅助方法：获取图执行的最终状态
     */
    private DynamicState getLastState(CompiledGraph<DynamicState> graph, Map<String, Object> input) {
        DynamicState lastState = null;
        for (NodeOutput<DynamicState> output : graph.stream(input)) {
            lastState = output.state();
        }
        return lastState;
    }
}
