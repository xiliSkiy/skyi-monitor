<template>
  <div class="app-container">
    <div class="page-header">
      <el-page-header content="告警详情" @back="goBack">
        <template #extra>
          <el-button v-if="alertDetail.status === 'ACTIVE'" type="primary" @click="handleAcknowledge">确认告警</el-button>
          <el-button v-if="alertDetail.status !== 'RESOLVED'" type="success" @click="handleResolve">解决告警</el-button>
        </template>
      </el-page-header>
    </div>

    <el-row :gutter="20" class="detail-container">
      <el-col :span="24">
        <el-card shadow="never" v-loading="loading">
          <el-descriptions title="告警信息" :column="3" border>
            <el-descriptions-item label="告警标题">{{ alertDetail.name }}</el-descriptions-item>
            <el-descriptions-item label="告警级别">
              <el-tag :type="getSeverityType(alertDetail.severity)" effect="dark">{{ getSeverityLabel(alertDetail.severity) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="告警状态">
              <el-tag :type="getStatusType(alertDetail.status)">{{ getStatusLabel(alertDetail.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="资产名称">{{ alertDetail.assetName }}</el-descriptions-item>
            <el-descriptions-item label="资产类型">{{ alertDetail.assetType }}</el-descriptions-item>
            <el-descriptions-item label="指标名称">{{ alertDetail.metricName }}</el-descriptions-item>
            <el-descriptions-item label="当前值">{{ alertDetail.metricValue }}</el-descriptions-item>
            <el-descriptions-item label="阈值">{{ alertDetail.threshold }}</el-descriptions-item>
            <el-descriptions-item label="告警类型">{{ getAlertTypeLabel(alertDetail.type) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ alertDetail.createTime ? formatTime(alertDetail.createTime) : '' }}</el-descriptions-item>
            <el-descriptions-item label="最后更新时间">{{ alertDetail.updateTime ? formatTime(alertDetail.updateTime) : '' }}</el-descriptions-item>
            <el-descriptions-item label="通知状态">{{ alertDetail.notified ? '已通知' : '未通知' }}</el-descriptions-item>
            <el-descriptions-item :span="3" label="告警内容">{{ alertDetail.message }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="status-container" v-if="alertDetail.status !== 'ACTIVE'">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>处理信息</span>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item v-if="alertDetail.status === 'ACKNOWLEDGED' || alertDetail.status === 'RESOLVED'" label="确认人">{{ alertDetail.acknowledgedBy || '未知' }}</el-descriptions-item>
            <el-descriptions-item v-if="alertDetail.status === 'ACKNOWLEDGED' || alertDetail.status === 'RESOLVED'" label="确认时间">{{ alertDetail.acknowledgedTime ? formatTime(alertDetail.acknowledgedTime) : '' }}</el-descriptions-item>
            <el-descriptions-item v-if="alertDetail.status === 'RESOLVED'" label="解决人">{{ alertDetail.resolvedBy || '未知' }}</el-descriptions-item>
            <el-descriptions-item v-if="alertDetail.status === 'RESOLVED'" label="解决时间">{{ alertDetail.resolvedTime ? formatTime(alertDetail.resolvedTime) : '' }}</el-descriptions-item>
            <el-descriptions-item v-if="alertDetail.status === 'RESOLVED'" :span="2" label="解决备注">{{ alertDetail.resolveComment || '无' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="timeline-container">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>通知记录</span>
              <el-button type="text" @click="fetchNotifications">刷新</el-button>
            </div>
          </template>

          <div v-loading="notificationsLoading">
            <el-empty v-if="notifications.length === 0" description="暂无通知记录" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="(notification, index) in notifications"
                :key="index"
                :type="getNotificationStatusType(notification.status)"
                :timestamp="notification.createTime ? formatTime(notification.createTime) : ''"
              >
                <h4>{{ getNotificationChannelLabel(notification.channel) }}</h4>
                <p>状态: {{ getNotificationStatusLabel(notification.status) }}</p>
                <p v-if="notification.recipient">接收人: {{ notification.recipient }}</p>
                <p v-if="notification.content">消息: {{ notification.content }}</p>
                <p v-if="notification.failReason">失败原因: {{ notification.failReason }}</p>
                <el-button type="text" size="small" @click="showNotificationDetail(notification)">查看详情</el-button>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 确认告警对话框 -->
    <el-dialog
      title="确认告警"
      v-model="acknowledgeDialogVisible"
      width="30%"
    >
      <el-form :model="acknowledgeForm" label-width="80px">
        <el-form-item label="备注" prop="comment">
          <el-input
            v-model="acknowledgeForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入确认备注（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="acknowledgeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAcknowledge" :loading="acknowledgeLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 解决告警对话框 -->
    <el-dialog
      title="解决告警"
      v-model="resolveDialogVisible"
      width="30%"
    >
      <el-form :model="resolveForm" label-width="80px" :rules="resolveFormRules" ref="resolveFormRef">
        <el-form-item label="备注" prop="comment">
          <el-input
            v-model="resolveForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入解决备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resolveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmResolve" :loading="resolveLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 通知详情对话框 -->
    <notification-dialog
      v-model:visible="notificationDialogVisible"
      :notification="currentNotification"
      @retry="handleRetryNotification"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  getAlertDetail,
  acknowledgeAlert,
  resolveAlert,
  getAlertNotifications
} from '@/api/alert';
import { 
  Alert, 
  AlertNotification, 
  NotificationChannel, 
  NotificationStatus,
  AlertSeverity,
  AlertStatus,
  AlertType
} from '@/types/alert';
import NotificationDialog from './components/NotificationDialog.vue';

const route = useRoute();
const router = useRouter();

// 告警详情数据
const alertDetail = reactive<Alert>({} as Alert);
const loading = ref(false);

// 通知记录数据
const notifications = ref<AlertNotification[]>([]);
const notificationsLoading = ref(false);

// 确认告警相关
const acknowledgeDialogVisible = ref(false);
const acknowledgeLoading = ref(false);
const acknowledgeForm = reactive({
  comment: ''
});

// 解决告警相关
const resolveDialogVisible = ref(false);
const resolveLoading = ref(false);
const resolveForm = reactive({
  comment: ''
});
const resolveFormRules = {
  comment: [
    { required: true, message: '请输入解决备注', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ]
};
const resolveFormRef = ref();

// 通知详情对话框
const notificationDialogVisible = ref(false);
const currentNotification = ref<AlertNotification>({} as AlertNotification);

// 获取告警详情
const fetchAlertDetail = async () => {
  const alertId = route.params.id as string;
  if (!alertId) {
    ElMessage.error('告警ID不能为空');
    return;
  }

  loading.value = true;
  try {
    const response = await getAlertDetail(alertId);
    if (response.code === 200) {
      Object.assign(alertDetail, response.data || {});
    } else {
      ElMessage.error(response.message || '获取告警详情失败');
    }
  } catch (error) {
    console.error('获取告警详情出错', error);
    ElMessage.error('获取告警详情出错');
  } finally {
    loading.value = false;
  }
};

// 获取通知记录
const fetchNotifications = async () => {
  const alertId = route.params.id as string;
  if (!alertId) return;

  notificationsLoading.value = true;
  try {
    const response = await getAlertNotifications(alertId);
    if (response.code === 200) {
      notifications.value = response.data || [];
    } else {
      ElMessage.error(response.message || '获取通知记录失败');
    }
  } catch (error) {
    console.error('获取通知记录出错', error);
    ElMessage.error('获取通知记录出错');
  } finally {
    notificationsLoading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 处理确认告警
const handleAcknowledge = () => {
  acknowledgeForm.comment = '';
  acknowledgeDialogVisible.value = true;
};

// 确认操作
const confirmAcknowledge = async () => {
  acknowledgeLoading.value = true;
  try {
    const response = await acknowledgeAlert(route.params.id as string, {
      acknowledgedBy: 'admin', // 当前系统可能没有用户系统，暂时写死
      comment: acknowledgeForm.comment
    });
    
    if (response.code === 200) {
      ElMessage.success('告警确认成功');
      acknowledgeDialogVisible.value = false;
      fetchAlertDetail(); // 刷新详情
    } else {
      ElMessage.error(response.message || '告警确认失败');
    }
  } catch (error) {
    console.error('告警确认出错', error);
    ElMessage.error('告警确认操作失败');
  } finally {
    acknowledgeLoading.value = false;
  }
};

// 处理解决告警
const handleResolve = () => {
  resolveForm.comment = '';
  resolveDialogVisible.value = true;
};

// 解决操作
const confirmResolve = async () => {
  resolveFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) return;
    
    resolveLoading.value = true;
    try {
      const response = await resolveAlert(route.params.id as string, {
        resolvedBy: 'admin', // 当前系统可能没有用户系统，暂时写死
        comment: resolveForm.comment
      });
      
      if (response.code === 200) {
        ElMessage.success('告警解决成功');
        resolveDialogVisible.value = false;
        fetchAlertDetail(); // 刷新详情
      } else {
        ElMessage.error(response.message || '告警解决失败');
      }
    } catch (error) {
      console.error('告警解决出错', error);
      ElMessage.error('告警解决操作失败');
    } finally {
      resolveLoading.value = false;
    }
  });
};

// 获取告警级别标签
const getSeverityLabel = (severity: string): string => {
  switch (severity) {
    case 'CRITICAL':
      return '严重';
    case 'MAJOR':
      return '重要';
    case 'MINOR':
      return '次要';
    case 'WARNING':
      return '警告';
    case 'INFO':
      return '信息';
    default:
      return '未知';
  }
};

// 获取告警级别类型
const getSeverityType = (severity: string): string => {
  switch (severity) {
    case 'CRITICAL':
      return 'danger';
    case 'MAJOR':
      return 'error';
    case 'MINOR':
      return 'warning';
    case 'WARNING':
      return 'warning';
    case 'INFO':
      return 'info';
    default:
      return 'info';
  }
};

// 获取告警状态标签
const getStatusLabel = (status: string): string => {
  switch (status) {
    case 'ACTIVE':
      return '活动';
    case 'ACKNOWLEDGED':
      return '已确认';
    case 'RESOLVED':
      return '已解决';
    case 'EXPIRED':
      return '已过期';
    default:
      return '未知';
  }
};

// 获取告警状态类型
const getStatusType = (status: string): string => {
  switch (status) {
    case 'ACTIVE':
      return 'danger';
    case 'ACKNOWLEDGED':
      return 'warning';
    case 'RESOLVED':
      return 'success';
    case 'EXPIRED':
      return 'info';
    default:
      return 'info';
  }
};

// 获取告警类型标签
const getAlertTypeLabel = (type: string): string => {
  switch (type) {
    case 'THRESHOLD':
      return '阈值告警';
    case 'TREND':
      return '趋势告警';
    case 'PATTERN':
      return '模式告警';
    default:
      return '未知类型';
  }
};

// 获取通知渠道标签
const getNotificationChannelLabel = (channel: string): string => {
  switch (channel) {
    case 'EMAIL':
      return '邮件通知';
    case 'SMS':
      return '短信通知';
    case 'WEBHOOK':
      return 'Webhook通知';
    case 'DINGTALK':
      return '钉钉通知';
    case 'WEIXIN':
      return '微信通知';
    default:
      return '未知渠道';
  }
};

// 获取通知状态标签
const getNotificationStatusLabel = (status: string): string => {
  switch (status) {
    case 'SUCCESS':
      return '发送成功';
    case 'FAILED':
      return '发送失败';
    case 'PENDING':
      return '待发送';
    default:
      return '未知状态';
  }
};

// 获取通知状态类型
const getNotificationStatusType = (status: string): string => {
  switch (status) {
    case 'SUCCESS':
      return 'success';
    case 'FAILED':
      return 'danger';
    case 'PENDING':
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

// 显示通知详情
const showNotificationDetail = (notification: AlertNotification) => {
  currentNotification.value = notification;
  notificationDialogVisible.value = true;
};

// 处理重试发送通知
const handleRetryNotification = async (notification: AlertNotification) => {
  // 这里需要实现重试逻辑，可能需要添加API
  ElMessage.info('重试功能待实现');
  // 重新获取通知记录
  await fetchNotifications();
  notificationDialogVisible.value = false;
};

// 组件挂载时加载数据
onMounted(() => {
  fetchAlertDetail();
  fetchNotifications();
});
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.detail-container,
.status-container,
.timeline-container {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 