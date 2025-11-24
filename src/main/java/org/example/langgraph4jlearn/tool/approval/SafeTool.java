package org.example.langgraph4jlearn.tool.approval;

import lombok.extern.slf4j.Slf4j;

/**
 * 模拟安全工具
 * 这个工具执行前不需要人工审批
 */
@Slf4j
public class SafeTool {
    
    /**
     * 查询数据操作（安全操作，无需审批）
     */
    public static String queryData(String query) {
        log.info("执行查询操作: {}", query);
        return String.format("查询结果: 找到 5 条符合条件 '%s' 的记录", query);
    }
    
    /**
     * 获取系统信息（安全操作，无需审批）
     */
    public static String getSystemInfo() {
        log.info("获取系统信息");
        return "系统信息: \n- CPU: 使用率 45%\n- 内存: 使用率 60%\n- 磁盘: 使用率 70%";
    }
    
    /**
     * 计算操作（安全操作，无需审批）
     */
    public static String calculate(String expression) {
        log.info("执行计算: {}", expression);
        // 简单模拟计算
        return String.format("计算结果: %s = 42", expression);
    }
}
