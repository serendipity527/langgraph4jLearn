package org.example.langgraph4jlearn.agent.graph;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.EdgeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.example.langgraph4jlearn.agent.state.ComprehensiveWorkflowState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * Adaptive RAG 图 - 自适应检索增强生成
 * 
 * 核心思想: 根据查询复杂度动态选择检索策略
 * 
 * 架构:
 *   ┌──────────────────────────────────────────────────────────────┐
 *   │                      START                                   │
 *   └─────────────────────────┬────────────────────────────────────┘
 *                             ▼
 *   ┌──────────────────────────────────────────────────────────────┐
 *   │              Query Analyzer (查询分析)                        │
 *   │         分析查询复杂度: simple | standard | complex           │
 *   └─────────────────────────┬────────────────────────────────────┘
 *                             │ 条件路由
 *         ┌───────────────────┼───────────────────┐
 *         ▼                   ▼                   ▼
 *   ┌──────────┐       ┌──────────┐       ┌──────────────┐
 *   │ Direct   │       │ Standard │       │ Multi-Step   │
 *   │ Generate │       │ Retrieve │       │ Retrieve     │
 *   │(直接生成)│       │(标准检索)│       │(多步检索)    │
 *   └────┬─────┘       └────┬─────┘       └──────┬───────┘
 *        │                  │                    │
 *        │                  ▼                    ▼
 *        │           ┌──────────┐       ┌──────────────┐
 *        │           │ Grade    │       │ Query        │
 *        │           │ Documents│◄──────│ Transform    │
 *        │           │(评估文档)│       │(查询重写)    │
 *        │           └────┬─────┘       └──────────────┘
 *        │                │                    ▲
 *        │      relevant? │                    │ not relevant
 *        │         ┌──────┴──────┐             │
 *        │         ▼             ▼─────────────┘
 *        │   ┌──────────┐
 *        │   │ Generate │
 *        └──►│ Answer   │
 *            │(生成答案)│
 *            └────┬─────┘
 *                 ▼
 *          ┌──────────┐
 *          │ Grade    │
 *          │ Answer   │
 *          │(评估答案)│
 *          └────┬─────┘
 *               │
 *      useful?  │
 *   ┌───────────┼───────────┐
 *   ▼           ▼           ▼
 * [END]    [Regenerate] [Re-retrieve]
 * 
 * 特点:
 * - 查询路由: 根据复杂度选择不同策略
 * - 文档评估: 检查检索文档相关性
 * - 查询重写: 优化不相关的查询
 * - 答案评估: 检测幻觉和答案质量
 * - 自适应循环: 可重新生成或重新检索
 */
@Slf4j
public class AdaptiveRAGGraph {
    
    // 节点名称
    public static final String QUERY_ANALYZER = "query_analyzer";
    public static final String DIRECT_GENERATE = "direct_generate";
    public static final String STANDARD_RETRIEVE = "standard_retrieve";
    public static final String MULTI_STEP_RETRIEVE = "multi_step_retrieve";
    public static final String GRADE_DOCUMENTS = "grade_documents";
    public static final String QUERY_TRANSFORM = "query_transform";
    public static final String GENERATE_ANSWER = "generate_answer";
    public static final String GRADE_ANSWER = "grade_answer";
    
    // 状态字段
    public static final String QUERY_TYPE = "query_type";
    public static final String RETRIEVED_DOCS = "retrieved_docs";
    public static final String DOC_RELEVANCE = "doc_relevance";
    public static final String GENERATED_ANSWER = "generated_answer";
    public static final String ANSWER_QUALITY = "answer_quality";
    public static final String TRANSFORM_COUNT = "transform_count";
    public static final String REGENERATE_COUNT = "regenerate_count";
    
    private StateGraph<ComprehensiveWorkflowState> stateGraph;
    
    public AdaptiveRAGGraph() {
        buildGraph();
    }
    
    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    // ========== Query Analyzer (查询分析器) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> queryAnalyzerNode = node_async(state -> {
        log.info("🔎 [Query Analyzer] 分析查询复杂度");
        String query = state.userInput().toLowerCase();
        String queryType;
        
        // 分析查询复杂度
        if (query.contains("什么是") || query.contains("定义") || query.length() < 10) {
            queryType = "simple";  // 简单查询，直接生成
        } else if (query.contains("比较") || query.contains("分析") || query.contains("为什么") || query.contains("如何")) {
            queryType = "complex"; // 复杂查询，需要多步检索
        } else {
            queryType = "standard"; // 标准查询，常规RAG
        }
        
        log.info("🔎 查询类型: {} (query: {})", queryType, query);
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, QUERY_ANALYZER,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ComprehensiveWorkflowState.INTENT, "rag:" + queryType,
                QUERY_TYPE, queryType,
                TRANSFORM_COUNT, 0,
                REGENERATE_COUNT, 0,
                ComprehensiveWorkflowState.MESSAGES, "🔎 [Query Analyzer] 查询类型: " + queryType,
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Query Analyzer: " + queryType
        );
    });
    
    // ========== Direct Generate (直接生成) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> directGenerateNode = node_async(state -> {
        log.info("⚡ [Direct Generate] 简单查询，直接生成答案");
        
        String answer = "【直接生成答案】\n" +
                "查询: " + state.userInput() + "\n" +
                "答案: 这是一个简单查询，基于模型内置知识直接生成答案。\n" +
                "无需检索外部文档。";
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, DIRECT_GENERATE,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                GENERATED_ANSWER, answer,
                ComprehensiveWorkflowState.TOOL_RESULTS, "⚡ Direct: " + answer,
                ComprehensiveWorkflowState.MESSAGES, "⚡ [Direct Generate] 直接生成完成",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Direct Generate: 完成"
        );
    });
    
    // ========== Standard Retrieve (标准检索) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> standardRetrieveNode = node_async(state -> {
        log.info("📚 [Standard Retrieve] 执行标准RAG检索");
        
        // 模拟检索过程
        String docs = "【检索到的文档】\n" +
                "Doc1: LangGraph是一个用于构建状态化多参与者应用的框架...\n" +
                "Doc2: RAG结合了检索和生成，提高答案准确性...\n" +
                "Doc3: 向量数据库用于存储和检索文档嵌入...";
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, STANDARD_RETRIEVE,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                RETRIEVED_DOCS, docs,
                ComprehensiveWorkflowState.TOOL_RESULTS, "📚 Retrieve: 检索到3篇相关文档",
                ComprehensiveWorkflowState.MESSAGES, "📚 [Standard Retrieve] 检索完成",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Standard Retrieve: 3 docs"
        );
    });
    
    // ========== Multi-Step Retrieve (多步检索) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> multiStepRetrieveNode = node_async(state -> {
        log.info("🔄 [Multi-Step Retrieve] 执行多步深度检索");
        
        // 模拟多步检索
        String docs = "【多步检索结果】\n" +
                "Step1-Doc1: 深度分析文档A - 核心概念解释...\n" +
                "Step1-Doc2: 相关背景知识B...\n" +
                "Step2-Doc1: 扩展检索 - 案例研究C...\n" +
                "Step2-Doc2: 对比分析文档D...\n" +
                "Step3-Doc1: 最新研究进展E...";
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, MULTI_STEP_RETRIEVE,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                RETRIEVED_DOCS, docs,
                ComprehensiveWorkflowState.TOOL_RESULTS, "🔄 Multi-Step: 多步检索完成(5篇文档)",
                ComprehensiveWorkflowState.MESSAGES, "🔄 [Multi-Step Retrieve] 多步检索完成",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Multi-Step Retrieve: 5 docs"
        );
    });
    
    // ========== Grade Documents (文档评估) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> gradeDocumentsNode = node_async(state -> {
        log.info("📊 [Grade Documents] 评估文档相关性");
        
        int transformCount = state.<Integer>value(TRANSFORM_COUNT).orElse(0);
        // 模拟评估：第一次可能不相关，重写后相关
        boolean relevant = transformCount > 0 || new Random().nextDouble() > 0.3;
        String relevance = relevant ? "relevant" : "not_relevant";
        
        log.info("📊 文档相关性: {} (transformCount: {})", relevance, transformCount);
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, GRADE_DOCUMENTS,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                DOC_RELEVANCE, relevance,
                ComprehensiveWorkflowState.MESSAGES, "📊 [Grade Documents] 相关性: " + (relevant ? "✅相关" : "❌不相关"),
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Grade Documents: " + relevance
        );
    });
    
    // ========== Query Transform (查询重写) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> queryTransformNode = node_async(state -> {
        log.info("✏️ [Query Transform] 重写优化查询");
        
        int transformCount = state.<Integer>value(TRANSFORM_COUNT).orElse(0);
        String originalQuery = state.userInput();
        String transformedQuery = "【重写后的查询】\n" +
                "原始: " + originalQuery + "\n" +
                "优化: 请详细解释 " + originalQuery + " 的核心概念、应用场景和最佳实践";
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, QUERY_TRANSFORM,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                TRANSFORM_COUNT, transformCount + 1,
                ComprehensiveWorkflowState.TOOL_RESULTS, "✏️ Transform: " + transformedQuery,
                ComprehensiveWorkflowState.MESSAGES, "✏️ [Query Transform] 查询已重写 (第" + (transformCount + 1) + "次)",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Query Transform: 重写#" + (transformCount + 1)
        );
    });
    
    // ========== Generate Answer (生成答案) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> generateAnswerNode = node_async(state -> {
        log.info("💡 [Generate Answer] 基于检索文档生成答案");
        
        String docs = state.<String>value(RETRIEVED_DOCS).orElse("");
        String queryType = state.<String>value(QUERY_TYPE).orElse("standard");
        
        String answer = "【RAG生成答案】\n" +
                "查询类型: " + queryType + "\n" +
                "基于检索文档数: " + (docs.split("Doc").length - 1) + "\n" +
                "答案: 根据检索到的相关文档，综合分析如下...\n" +
                "1. 核心概念已在Doc1中详细解释\n" +
                "2. 实践应用参考Doc2的案例\n" +
                "3. 最新进展见Doc3的研究结果";
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, GENERATE_ANSWER,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                GENERATED_ANSWER, answer,
                ComprehensiveWorkflowState.TOOL_RESULTS, "💡 Generate: " + answer,
                ComprehensiveWorkflowState.MESSAGES, "💡 [Generate Answer] 答案生成完成",
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Generate Answer: 完成"
        );
    });
    
    // ========== Grade Answer (答案评估) ==========
    private final AsyncNodeAction<ComprehensiveWorkflowState> gradeAnswerNode = node_async(state -> {
        log.info("✅ [Grade Answer] 评估生成答案质量");
        
        int regenerateCount = state.<Integer>value(REGENERATE_COUNT).orElse(0);
        // 模拟评估：有幻觉则需要重新生成
        String quality;
        if (regenerateCount >= 1) {
            quality = "useful"; // 重新生成后通常质量更好
        } else {
            double rand = new Random().nextDouble();
            if (rand > 0.7) quality = "useful";
            else if (rand > 0.4) quality = "not_useful";
            else quality = "hallucination";
        }
        
        log.info("✅ 答案质量: {} (regenerateCount: {})", quality, regenerateCount);
        
        String qualityDesc = switch(quality) {
            case "useful" -> "✅有用";
            case "not_useful" -> "⚠️不够有用";
            case "hallucination" -> "❌检测到幻觉";
            default -> "未知";
        };
        
        return Map.of(
                ComprehensiveWorkflowState.CURRENT_STEP, GRADE_ANSWER,
                ComprehensiveWorkflowState.STEP_COUNT, state.stepCount() + 1,
                ANSWER_QUALITY, quality,
                REGENERATE_COUNT, regenerateCount,
                ComprehensiveWorkflowState.MESSAGES, "✅ [Grade Answer] 质量: " + qualityDesc,
                ComprehensiveWorkflowState.EXECUTION_LOG, "[" + timestamp() + "] Grade Answer: " + quality
        );
    });
    
    // ========== 路由函数 ==========
    
    // 查询类型路由
    private EdgeAction<ComprehensiveWorkflowState> routeByQueryType() {
        return state -> {
            String queryType = state.<String>value(QUERY_TYPE).orElse("standard");
            log.info("🔀 [Router] 查询类型路由: {}", queryType);
            return queryType;
        };
    }
    
    // 文档相关性路由
    private EdgeAction<ComprehensiveWorkflowState> routeByDocRelevance() {
        return state -> {
            String relevance = state.<String>value(DOC_RELEVANCE).orElse("relevant");
            int transformCount = state.<Integer>value(TRANSFORM_COUNT).orElse(0);
            
            if ("relevant".equals(relevance)) {
                return "generate";
            } else if (transformCount < 2) {
                return "transform"; // 最多重写2次
            } else {
                return "generate"; // 超过次数后直接生成
            }
        };
    }
    
    // 答案质量路由
    private EdgeAction<ComprehensiveWorkflowState> routeByAnswerQuality() {
        return state -> {
            String quality = state.<String>value(ANSWER_QUALITY).orElse("useful");
            int regenerateCount = state.<Integer>value(REGENERATE_COUNT).orElse(0);
            
            if ("useful".equals(quality)) {
                return "finish";
            } else if ("hallucination".equals(quality) && regenerateCount < 2) {
                return "re_retrieve"; // 有幻觉则重新检索
            } else if ("not_useful".equals(quality) && regenerateCount < 2) {
                return "regenerate"; // 不够有用则重新生成
            } else {
                return "finish"; // 超过重试次数
            }
        };
    }
    
    // ========== 构建图 ==========
    private void buildGraph() {
        try {
            log.info("🏗️ 构建 Adaptive RAG 图...");
            
            this.stateGraph = new StateGraph<>(ComprehensiveWorkflowState.SCHEMA, ComprehensiveWorkflowState::new);
            
            // 添加节点
            stateGraph.addNode(QUERY_ANALYZER, queryAnalyzerNode);
            stateGraph.addNode(DIRECT_GENERATE, directGenerateNode);
            stateGraph.addNode(STANDARD_RETRIEVE, standardRetrieveNode);
            stateGraph.addNode(MULTI_STEP_RETRIEVE, multiStepRetrieveNode);
            stateGraph.addNode(GRADE_DOCUMENTS, gradeDocumentsNode);
            stateGraph.addNode(QUERY_TRANSFORM, queryTransformNode);
            stateGraph.addNode(GENERATE_ANSWER, generateAnswerNode);
            stateGraph.addNode(GRADE_ANSWER, gradeAnswerNode);
            
            // 定义边
            stateGraph.addEdge(START, QUERY_ANALYZER);
            
            // 查询分析后的条件路由
            stateGraph.addConditionalEdges(QUERY_ANALYZER, edge_async(routeByQueryType()),
                    Map.of(
                            "simple", DIRECT_GENERATE,
                            "standard", STANDARD_RETRIEVE,
                            "complex", MULTI_STEP_RETRIEVE
                    ));
            
            // 直接生成 -> 答案评估
            stateGraph.addEdge(DIRECT_GENERATE, GRADE_ANSWER);
            
            // 标准检索 -> 文档评估
            stateGraph.addEdge(STANDARD_RETRIEVE, GRADE_DOCUMENTS);
            
            // 多步检索 -> 文档评估
            stateGraph.addEdge(MULTI_STEP_RETRIEVE, GRADE_DOCUMENTS);
            
            // 文档评估的条件路由
            stateGraph.addConditionalEdges(GRADE_DOCUMENTS, edge_async(routeByDocRelevance()),
                    Map.of(
                            "generate", GENERATE_ANSWER,
                            "transform", QUERY_TRANSFORM
                    ));
            
            // 查询重写 -> 重新检索(标准检索)
            stateGraph.addEdge(QUERY_TRANSFORM, STANDARD_RETRIEVE);
            
            // 生成答案 -> 答案评估
            stateGraph.addEdge(GENERATE_ANSWER, GRADE_ANSWER);
            
            // 答案评估的条件路由
            stateGraph.addConditionalEdges(GRADE_ANSWER, edge_async(routeByAnswerQuality()),
                    Map.of(
                            "finish", END,
                            "regenerate", GENERATE_ANSWER,
                            "re_retrieve", STANDARD_RETRIEVE
                    ));
            
            log.info("✅ Adaptive RAG 图构建完成");
        } catch (Exception e) {
            log.error("❌ 构建 Adaptive RAG 图失败", e);
            throw new RuntimeException("构建 Adaptive RAG 图失败", e);
        }
    }
    
    // ========== 公共方法 ==========
    
    public StateGraph<ComprehensiveWorkflowState> getGraph() {
        return this.stateGraph;
    }
    
    public CompiledGraph<ComprehensiveWorkflowState> compile() throws GraphStateException {
        return stateGraph.compile();
    }
    
    public CompiledGraph<ComprehensiveWorkflowState> compileWithCheckpoint(MemorySaver saver) throws GraphStateException {
        return stateGraph.compile(CompileConfig.builder()
                .checkpointSaver(saver)
                .build());
    }
}
