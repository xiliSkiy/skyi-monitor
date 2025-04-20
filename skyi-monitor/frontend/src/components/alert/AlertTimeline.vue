<template>
  <div class="alert-timeline">
    <el-timeline>
      <el-timeline-item
        v-for="(alert, index) in alerts"
        :key="index"
        :type="getTimelineItemType(alert.severity)"
        :color="getTimelineItemColor(alert.severity)"
        :timestamp="formatTime(alert.createTime)"
        :hollow="alert.status === 'RESOLVED'"
      >
        <div class="timeline-card" @click="handleViewDetail(alert)">
          <div class="card-header">
            <h4>{{ alert.title }}</h4>
            <div class="tags">
              <el-tag :type="getSeverityType(alert.severity)" size="small" effect="dark">{{ getSeverityLabel(alert.severity) }}</el-tag>
              <el-tag :type="getStatusType(alert.status)" size="small">{{ getStatusLabel(alert.status) }}</el-tag>
            </div>
          </div>
          <div class="card-content">
            <p>{{ alert.message }}</p>
            <p v-if="alert.asset"><strong>资产:</strong> {{ alert.asset }}</p>
            <p v-if="alert.metric"><strong>指标:</strong> {{ alert.metric }}</p>
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, defineEmits } from 'vue';

interface Alert {
  id: string | number;
  title: string;
  message?: string;
  severity: string;
  status: string;
  asset?: string;
  metric?: string;
  createTime: string | Date;
  [key: string]: any;
}

const props = defineProps({
  alerts: {
    type: Array as () => Alert[],
    default: () => []
  }
});

const emit = defineEmits(['view']);

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

// 获取告警时间线项类型
const getTimelineItemType = (severity: string): string => {
  switch (severity) {
    case 'CRITICAL':
    case 'MAJOR':
      return 'danger';
    case 'MINOR':
    case 'WARNING':
      return 'warning';
    case 'INFO':
      return 'info';
    default:
      return 'primary';
  }
};

// 获取告警时间线项颜色
const getTimelineItemColor = (severity: string): string => {
  switch (severity) {
    case 'CRITICAL':
      return '#F56C6C';
    case 'MAJOR':
      return '#E6A23C';
    case 'MINOR':
      return '#E6A23C';
    case 'WARNING':
      return '#F2C037';
    case 'INFO':
      return '#909399';
    default:
      return '#409EFF';
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

// 处理查看详情
const handleViewDetail = (alert: Alert): void => {
  emit('view', alert);
};
</script>

<style lang="scss" scoped>
.alert-timeline {
  padding: 10px;
  
  .timeline-card {
    padding: 10px 15px;
    border-radius: 4px;
    background-color: #f5f7fa;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      background-color: #ecf5ff;
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      
      h4 {
        margin: 0;
        font-size: 16px;
      }
      
      .tags {
        display: flex;
        gap: 5px;
      }
    }
    
    .card-content {
      p {
        margin: 5px 0;
        font-size: 14px;
      }
    }
  }
}
</style> 