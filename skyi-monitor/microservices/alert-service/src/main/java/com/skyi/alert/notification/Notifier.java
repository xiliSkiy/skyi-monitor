package com.skyi.alert.notification;

import com.skyi.alert.model.Alert;
import com.skyi.alert.model.AlertNotification;

/**
 * 通知器接口
 */
public interface Notifier {

    /**
     * 发送通知
     *
     * @param alert        告警对象
     * @param recipient    接收人
     * @param notification 通知对象
     * @return 更新后的通知对象
     */
    AlertNotification send(Alert alert, String recipient, AlertNotification notification);
} 