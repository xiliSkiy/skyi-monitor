<template>
  <div class="task-card">
    <el-card shadow="hover" :class="statusClass">
      <template #header>
        <div class="card-header">
          <span class="title">{{ title }}</span>
          <el-tag :type="statusType">{{ statusLabel }}</el-tag>
        </div>
      </template>
      <div class="card-content">
        <p><strong>目标:</strong> {{ target }}</p>
        <p><strong>类型:</strong> {{ type }}</p>
        <p><strong>协议:</strong> {{ protocol }}</p>
        <p><strong>计划时间:</strong> {{ formatTime(scheduledTime) }}</p>
        <p v-if="lastExecutionTime"><strong>上次执行:</strong> {{ formatTime(lastExecutionTime) }}</p>
        <p v-if="nextExecutionTime"><strong>下次执行:</strong> {{ formatTime(nextExecutionTime) }}</p>
        <p v-if="interval"><strong>执行间隔:</strong> {{ interval }}秒</p>
      </div>
      <div class="card-footer">
        <el-button v-if="status !== 'RUNNING'" size="small" type="primary" @click="handleExecute">立即执行</el-button>
        <el-button size="small" @click="handleDetail">查看详情</el-button>
        <el-button v-if="status === 'PENDING' || status === 'FAILED'" size="small" type="danger" @click="handleCancel">取消任务</el-button>
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
    default: '采集任务'
  },
  target: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: ''
  },
  protocol: {
    type: String,
    default: ''
  },
  status: {
    type: String,
    default: 'PENDING'
  },
  scheduledTime: {
    type: [String, Date],
    default: ''
  },
  lastExecutionTime: {
    type: [String, Date],
    default: ''
  },
  nextExecutionTime: {
    type: [String, Date],
    default: ''
  },
  interval: {
    type: [String, Number],
    default: 0
  }
});

const emit = defineEmits(['execute', 'detail', 'cancel']);

// 状态标签
const statusLabel = computed((): string => {
  const status = props.status;
  switch (status) {
    case 'RUNNING':
      return '运行中';
    case 'COMPLETED':
      return '已完成';
    case 'FAILED':
      return '失败';
    case 'PENDING':
      return '等待中';
    case 'CANCELLED':
      return '已取消';
    default:
      return '未知';
  }
});

// 状态类型
const statusType = computed((): string => {
  const status = props.status;
  switch (status) {
    case 'RUNNING':
      return 'warning';
    case 'COMPLETED':
      return 'success';
    case 'FAILED':
      return 'danger';
    case 'PENDING':
      return 'info';
    case 'CANCELLED':
      return 'info';
    default:
      return 'info';
  }
});

// 卡片样式
const statusClass = computed((): string => {
  return `task-${props.status.toLowerCase()}`;
});

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

// 立即执行
const handleExecute = (): void => {
  emit('execute', props.id);
};

// 查看详情
const handleDetail = (): void => {
  emit('detail', props.id);
};

// 取消任务
const handleCancel = (): void => {
  emit('cancel', props.id);
};
</script>

<style lang="scss" scoped>
.task-card {
  margin-bottom: 12px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title {
      font-weight: bold;
    }
  }
  
  .card-content {
    p {
      margin: 6px 0;
    }
  }
  
  .card-footer {
    margin-top: 15px;
    text-align: right;
    
    .el-button {
      margin-left: 5px;
    }
  }
  
  // 状态样式
  &.task-running {
    border-left: 4px solid #E6A23C;
  }
  
  &.task-completed {
    border-left: 4px solid #67C23A;
  }
  
  &.task-failed {
    border-left: 4px solid #F56C6C;
  }
  
  &.task-pending {
    border-left: 4px solid #909399;
  }
  
  &.task-cancelled {
    border-left: 4px solid #909399;
  }
}
</style>
