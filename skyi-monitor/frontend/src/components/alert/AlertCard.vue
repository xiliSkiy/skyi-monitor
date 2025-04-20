<template>
  <div class="alert-card">
    <el-card shadow="hover" :class="severityClass">
      <template #header>
        <div class="card-header">
          <span class="title">{{ title }}</span>
          <div class="right">
            <el-tag :type="severityType" effect="dark">{{ severityLabel }}</el-tag>
            <span class="time">{{ formattedTime }}</span>
          </div>
        </div>
      </template>
      <div class="card-content">
        <p class="message">{{ message }}</p>
        <p v-if="asset"><strong>资产:</strong> {{ asset }}</p>
        <p v-if="metric"><strong>指标:</strong> {{ metric }}</p>
        <p v-if="value"><strong>当前值:</strong> {{ value }} {{ unit }}</p>
        <p v-if="threshold"><strong>阈值:</strong> {{ threshold }} {{ unit }}</p>
      </div>
      <div class="card-footer">
        <el-button v-if="status === 'ACTIVE'" size="small" type="primary" @click="handleAcknowledge">确认</el-button>
        <el-button v-if="status !== 'RESOLVED'" size="small" type="success" @click="handleResolve">解决</el-button>
        <el-button size="small" @click="handleDetail">详情</el-button>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  },
  title: {
    type: String,
    default: '告警通知'
  },
  message: {
    type: String,
    default: ''
  },
  severity: {
    type: String,
    default: 'WARNING'
  },
  status: {
    type: String,
    default: 'ACTIVE'
  },
  asset: {
    type: String,
    default: ''
  },
  metric: {
    type: String,
    default: ''
  },
  value: {
    type: [String, Number],
    default: ''
  },
  threshold: {
    type: [String, Number],
    default: ''
  },
  unit: {
    type: String,
    default: ''
  },
  time: {
    type: [String, Date],
    required: true
  }
});

const emit = defineEmits(['acknowledge', 'resolve', 'detail']);

// 告警级别标签
const severityLabel = computed(() => {
  const severity = props.severity;
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
});

// 告警级别对应的Element类型
const severityType = computed(() => {
  const severity = props.severity;
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
});

// 告警卡片样式
const severityClass = computed(() => {
  return `alert-card-${props.severity.toLowerCase()}`;
});

// 格式化时间
const formattedTime = computed(() => {
  if (!props.time) return '';
  
  try {
    const date = new Date(props.time);
    return date.toLocaleString();
  } catch (e) {
    return String(props.time);
  }
});

// 处理确认告警
const handleAcknowledge = () => {
  emit('acknowledge', props.id);
};

// 处理解决告警
const handleResolve = () => {
  emit('resolve', props.id);
};

// 处理查看详情
const handleDetail = () => {
  emit('detail', props.id);
};
</script>

<style lang="scss" scoped>
.alert-card {
  margin-bottom: 12px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title {
      font-weight: bold;
    }
    
    .right {
      display: flex;
      align-items: center;
      
      .time {
        margin-left: 10px;
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .card-content {
    .message {
      font-weight: bold;
      margin-bottom: 10px;
    }
    
    p {
      margin: 5px 0;
    }
  }
  
  .card-footer {
    margin-top: 15px;
    text-align: right;
  }
  
  // 告警级别样式
  &.alert-card-critical {
    border-left: 4px solid #F56C6C;
  }
  
  &.alert-card-major {
    border-left: 4px solid #E6A23C;
  }
  
  &.alert-card-minor {
    border-left: 4px solid #E6A23C;
  }
  
  &.alert-card-warning {
    border-left: 4px solid #F2C037;
  }
  
  &.alert-card-info {
    border-left: 4px solid #909399;
  }
}
</style> 