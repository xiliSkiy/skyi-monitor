<template>
  <el-dialog
    title="通知详情"
    v-model="dialogVisible"
    width="50%"
    :before-close="handleClose"
  >
    <el-descriptions border :column="1">
      <el-descriptions-item label="通知ID">{{ notification.id }}</el-descriptions-item>
      <el-descriptions-item label="告警ID">{{ notification.alertId }}</el-descriptions-item>
      <el-descriptions-item label="告警UUID">{{ notification.alertUuid }}</el-descriptions-item>
      <el-descriptions-item label="通知渠道">
        <el-tag :type="getChannelTagType(notification.channel)">
          {{ getNotificationChannelLabel(notification.channel) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="接收人">{{ notification.recipient || '无' }}</el-descriptions-item>
      <el-descriptions-item label="通知状态">
        <el-tag :type="getStatusTagType(notification.status)">
          {{ getNotificationStatusLabel(notification.status) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="重试次数">{{ notification.retryCount || 0 }}</el-descriptions-item>
      <el-descriptions-item label="发送时间">{{ notification.sentTime ? formatTime(notification.sentTime) : '未发送' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ notification.createTime ? formatTime(notification.createTime) : '' }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ notification.updateTime ? formatTime(notification.updateTime) : '' }}</el-descriptions-item>
      <el-descriptions-item v-if="notification.failReason" label="失败原因">{{ notification.failReason }}</el-descriptions-item>
      <el-descriptions-item label="通知内容">
        <div v-if="notification.content" class="notification-content">{{ notification.content }}</div>
        <div v-else>无内容</div>
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button v-if="notification.status === 'FAILED'" type="primary" @click="handleRetry">重试发送</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { defineProps, defineEmits, ref, toRefs, watch } from 'vue';
import { AlertNotification, NotificationChannel, NotificationStatus } from '@/types/alert';

const props = defineProps<{
  visible: boolean;
  notification: AlertNotification;
}>();

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void;
  (e: 'retry', notification: AlertNotification): void;
}>();

const { visible, notification } = toRefs(props);
const dialogVisible = ref(false);

// 监听外部visible属性变化
watch(visible, (val) => {
  dialogVisible.value = val;
});

// 监听内部dialogVisible变化
watch(dialogVisible, (val) => {
  emit('update:visible', val);
});

// 获取通知渠道标签
const getNotificationChannelLabel = (channel: NotificationChannel): string => {
  switch (channel) {
    case NotificationChannel.EMAIL:
      return '邮件通知';
    case NotificationChannel.SMS:
      return '短信通知';
    case NotificationChannel.WEBHOOK:
      return 'Webhook通知';
    case NotificationChannel.DINGTALK:
      return '钉钉通知';
    case NotificationChannel.WEIXIN:
      return '微信通知';
    default:
      return '未知渠道';
  }
};

// 获取通知状态标签
const getNotificationStatusLabel = (status: NotificationStatus): string => {
  switch (status) {
    case NotificationStatus.SUCCESS:
      return '发送成功';
    case NotificationStatus.FAILED:
      return '发送失败';
    case NotificationStatus.PENDING:
      return '待发送';
    default:
      return '未知状态';
  }
};

// 获取渠道标签类型
const getChannelTagType = (channel: NotificationChannel): string => {
  switch (channel) {
    case NotificationChannel.EMAIL:
      return 'primary';
    case NotificationChannel.SMS:
      return 'success';
    case NotificationChannel.WEBHOOK:
      return 'warning';
    case NotificationChannel.DINGTALK:
      return 'info';
    case NotificationChannel.WEIXIN:
      return '';
    default:
      return 'info';
  }
};

// 获取状态标签类型
const getStatusTagType = (status: NotificationStatus): string => {
  switch (status) {
    case NotificationStatus.SUCCESS:
      return 'success';
    case NotificationStatus.FAILED:
      return 'danger';
    case NotificationStatus.PENDING:
      return 'info';
    default:
      return 'info';
  }
};

// 格式化时间
const formatTime = (time: string | Date): string => {
  if (!time) return '';
  
  try {
    const date = new Date(time);
    return date.toLocaleString();
  } catch (e) {
    return String(time);
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
};

// 重试发送
const handleRetry = () => {
  emit('retry', notification.value);
};
</script>

<style scoped>
.notification-content {
  max-height: 150px;
  overflow-y: auto;
  white-space: pre-wrap;
  background-color: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
}
</style> 