package com.skyi.alert.notification;

import com.skyi.alert.model.Alert;
import com.skyi.alert.model.AlertNotification;
import com.skyi.alert.model.NotificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮件通知发送器
 */
@Slf4j
@Component
public class EmailNotifier implements Notifier {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired(required = false)
    private TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${alert.notification.email.subject-prefix:【SKYI监控告警】}")
    private String subjectPrefix;

    @Override
    public AlertNotification send(Alert alert, String recipient, AlertNotification notification) {
        try {
            // 构建邮件内容
            String content = buildEmailContent(alert);
            
            // 构建标题
            String subject = buildEmailSubject(alert);
            
            // 发送邮件
            sendEmail(recipient, subject, content);
            
            // 更新通知状态
            notification.setContent(content);
            notification.setStatus(NotificationStatus.SUCCESS);
            notification.setRetryCount(notification.getRetryCount() != null ? notification.getRetryCount() + 1 : 1);
            
            log.info("邮件告警通知发送成功: alertId={}, recipient={}", alert.getId(), recipient);
            return notification;
        } catch (Exception e) {
            log.error("邮件告警通知发送失败: alertId={}, recipient={}, 原因: {}", 
                    alert.getId(), recipient, e.getMessage());
            
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailReason(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() != null ? notification.getRetryCount() + 1 : 1);
            
            return notification;
        }
    }

    /**
     * 构建邮件标题
     */
    private String buildEmailSubject(Alert alert) {
        return subjectPrefix + alert.getSeverity().name() + "-" + alert.getName();
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(Alert alert) {
        // 如果有模板引擎，使用模板构建
        if (templateEngine != null) {
            return buildEmailContentWithTemplate(alert);
        }
        
        // 否则使用简单文本构建
        StringBuilder content = new StringBuilder();
        content.append("<h2>告警详情</h2>");
        content.append("<hr/>");
        content.append("<p><strong>告警名称:</strong> ").append(alert.getName()).append("</p>");
        content.append("<p><strong>告警级别:</strong> ").append(alert.getSeverity()).append("</p>");
        content.append("<p><strong>告警内容:</strong> ").append(alert.getMessage()).append("</p>");
        content.append("<p><strong>资产名称:</strong> ").append(alert.getAssetName()).append("</p>");
        content.append("<p><strong>资产类型:</strong> ").append(alert.getAssetType()).append("</p>");
        content.append("<p><strong>指标名称:</strong> ").append(alert.getMetricName()).append("</p>");
        content.append("<p><strong>当前值:</strong> ").append(alert.getMetricValue()).append("</p>");
        
        if (alert.getThreshold() != null) {
            content.append("<p><strong>阈值:</strong> ").append(alert.getThreshold()).append("</p>");
        }
        
        content.append("<p><strong>告警时间:</strong> ").append(alert.getStartTime()).append("</p>");
        content.append("<hr/>");
        content.append("<p>请尽快处理！</p>");
        
        return content.toString();
    }

    /**
     * 使用模板构建邮件内容
     */
    private String buildEmailContentWithTemplate(Alert alert) {
        Context context = new Context();
        Map<String, Object> variables = new HashMap<>();
        variables.put("alert", alert);
        context.setVariables(variables);
        
        return templateEngine.process("mail/alert-notification", context);
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        
        mailSender.send(message);
    }
} 