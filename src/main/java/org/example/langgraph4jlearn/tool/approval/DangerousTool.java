package org.example.langgraph4jlearn.tool.approval;

import lombok.extern.slf4j.Slf4j;

/**
 * 模拟危险工具
 * 这个工具执行前需要人工审批
 */
@Slf4j
public class DangerousTool {
    
    /**
     * 删除数据操作（危险操作，需要审批）
     */
    public static String deleteData(String target) {
        log.info("执行删除操作: {}", target);
        return String.format("已成功删除数据: %s", target);
    }
    
    /**
     * 发送邮件操作（需要审批）
     */
    public static String sendEmail(String recipient, String subject) {
        log.info("发送邮件: 收件人={}, 主题={}", recipient, subject);
        return String.format("已成功发送邮件给 %s，主题: %s", recipient, subject);
    }
    
    /**
     * 执行系统命令（危险操作，需要审批）
     */
    public static String executeCommand(String command) {
        log.info("执行系统命令: {}", command);
        return String.format("已执行命令: %s\n结果: 操作成功完成", command);
    }
}
