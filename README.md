# LangGraph4j 学习项目

这是一个基于 LangGraph4j 的 Agent 开发学习项目，展示了如何使用 LangGraph4j 构建状态化的、多步骤的 AI Agent 应用。

## 🚀 快速开始

### 前置要求

- Java 17+
- Maven 3.6+
- IDE（推荐使用 IntelliJ IDEA）

### 克隆并运行

```bash
# 进入项目目录
cd langgraph4jLearn

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## 📚 项目内容

### 1. SimpleAgent - 最简单的 Agent 示例

一个包含两个节点的基础 Agent，演示了 LangGraph4j 的核心概念。

#### 图结构
```
START -> GreeterNode -> ResponderNode -> END
```

#### 快速测试

**通过 API 测试：**

```bash
curl -X POST http://localhost:8080/api/simple-agent/execute \
  -H "Content-Type: application/json" \
  -d '{"message": "你好！"}'
```

**通过单元测试：**

```bash
mvn test -Dtest=SimpleAgentTest
```

**通过 Java 代码：**

```java
SimpleAgent agent = new SimpleAgent();
SimpleAgentState finalState = agent.executeAndGetState("你好世界！");
System.out.println(finalState.messages());
```

输出示例：
```
[你好世界！, 你好！欢迎使用LangGraph4j！, 收到！准备开始工作！]
```

## 📖 文档

- [项目结构说明](./docs/project-structure.md) - 完整的项目目录结构和模块说明
- [SimpleAgent 使用指南](./docs/simple-agent-guide.md) - SimpleAgent 的详细使用文档

## 🏗️ 项目结构

```
langgraph4jLearn/
├── src/main/java/
│   └── org/example/langgraph4jlearn/
│       ├── agent/
│       │   ├── graph/           # Agent 图定义
│       │   │   └── SimpleAgent.java
│       │   ├── node/            # 节点实现
│       │   │   ├── GreeterNode.java
│       │   │   └── ResponderNode.java
│       │   └── state/           # 状态定义
│       │       └── SimpleAgentState.java
│       └── controller/          # REST 控制器
│           └── SimpleAgentController.java
├── src/test/java/              # 测试代码
└── docs/                       # 项目文档
```

## 🔑 核心概念

### StateGraph（状态图）

LangGraph4j 的核心类，用于定义 Agent 的结构：

```java
StateGraph<SimpleAgentState> graph = new StateGraph<>(
    SimpleAgentState.SCHEMA,    // 状态 Schema
    SimpleAgentState::new       // 状态工厂函数
);
```

### AgentState（状态）

图中所有节点共享的状态，是一个 `Map<String, Object>`：

```java
public class SimpleAgentState extends AgentState {
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
        MESSAGES_KEY, Channels.appender(ArrayList::new)
    );
}
```

### Node（节点）

执行具体操作的单元，接收状态并返回更新：

```java
public class GreeterNode implements NodeAction<SimpleAgentState> {
    @Override
    public Map<String, Object> apply(SimpleAgentState state) {
        return Map.of("messages", "你好！");
    }
}
```

### Edge（边）

定义节点之间的流转：

```java
graph.addEdge(START, "greeter");        // 正常边
graph.addEdge("greeter", "responder");
graph.addEdge("responder", END);
```

## 🛠️ 技术栈

- **LangGraph4j**: 1.7.4 - AI Agent 工作流框架
- **Spring Boot**: 3.5.8 - Web 框架
- **Lombok** - 简化 Java 代码
- **JUnit 5** - 单元测试

## 📦 依赖说明

主要依赖在 `pom.xml` 中定义：

```xml
<dependencies>
    <!-- LangGraph4j 核心 -->
    <dependency>
        <groupId>org.bsc.langgraph4j</groupId>
        <artifactId>langgraph4j-core</artifactId>
    </dependency>
    
    <!-- LangChain4j 集成 -->
    <dependency>
        <groupId>org.bsc.langgraph4j</groupId>
        <artifactId>langgraph4j-langchain4j</artifactId>
    </dependency>
    
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

## 🧪 测试

运行所有测试：

```bash
mvn test
```

运行特定测试：

```bash
mvn test -Dtest=SimpleAgentTest
```

## 🎯 下一步

1. **探索更多示例**
   - 添加条件边实现分支逻辑
   - 集成 LLM（OpenAI、Azure OpenAI）
   - 实现工具调用

2. **学习高级特性**
   - Checkpoint（检查点）机制
   - 子图（SubGraph）
   - Human-in-the-loop

3. **参考资源**
   - [LangGraph4j GitHub](https://github.com/langgraph4j/langgraph4j)
   - [LangGraph4j 官方文档](https://github.com/langgraph4j/langgraph4j/blob/main/README.md)

## 📝 许可

本项目仅用于学习目的。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**项目开始时间**: 2025-11-24  
**LangGraph4j 版本**: 1.7.4
