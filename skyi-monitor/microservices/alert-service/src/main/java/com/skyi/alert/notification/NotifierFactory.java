package com.skyi.alert.notification;

import com.skyi.alert.model.NotificationChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知器工厂
 */
@Component
public class NotifierFactory {

    private final Map<NotificationChannel, Notifier> notifierMap = new HashMap<>();
    
    @Autowired
    public NotifierFactory(EmailNotifier emailNotifier) {
        notifierMap.put(NotificationChannel.EMAIL, emailNotifier);
        // 可以在这里注册其他类型的通知器
        // notifierMap.put(NotificationChannel.SMS, smsNotifier);
        // notifierMap.put(NotificationChannel.WEBHOOK, webhookNotifier);
        // notifierMap.put(NotificationChannel.DINGTALK, dingtalkNotifier);
        // notifierMap.put(NotificationChannel.WEIXIN, weixinNotifier);
    }
    
    /**
     * 获取通知器
     *
     * @param channel 通知渠道
     * @return 通知器
     */
    public Notifier getNotifier(NotificationChannel channel) {
        return notifierMap.get(channel);
    }
    
    /**
     * 检查通知器是否存在
     *
     * @param channel 通知渠道
     * @return 是否存在
     */
    public boolean hasNotifier(NotificationChannel channel) {
        return notifierMap.containsKey(channel);
    }
} 