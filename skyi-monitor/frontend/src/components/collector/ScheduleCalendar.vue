<template>
  <div class="schedule-calendar">
    <el-calendar>
      <template #date-cell="{ data }">
        <div class="calendar-cell">
          <p class="date">{{ data.day.split('-').slice(1).join('-') }}</p>
          <div class="task-count" v-if="getTaskCount(data.day) > 0" @click="handleDateClick(data.day)">
            <el-badge :value="getTaskCount(data.day)" type="primary">
              <span class="task-text">任务</span>
            </el-badge>
          </div>
        </div>
      </template>
    </el-calendar>
    
    <el-dialog v-model="dialogVisible" title="采集任务列表" width="70%">
      <el-table :data="currentDateTasks" style="width: 100%">
        <el-table-column prop="title" label="任务名称" width="180" />
        <el-table-column prop="target" label="采集目标" width="150" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="scheduledTime" label="计划时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.scheduledTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="handleViewTask(scope.row)">详情</el-button>
            <el-button size="small" type="primary" @click="handleEditTask(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';

interface Task {
  id: string | number;
  title: string;
  target: string;
  type: string;
  status: string;
  scheduledTime: string | Date;
  [key: string]: any;
}

const props = defineProps({
  tasks: {
    type: Array as () => Task[],
    default: () => []
  }
});

const emit = defineEmits(['view-task', 'edit-task']);

// 对话框控制
const dialogVisible = ref(false);
const selectedDate = ref('');

// 获取指定日期的任务数量
const getTaskCount = (date: string): number => {
  return props.tasks.filter(task => {
    const taskDate = formatDateString(new Date(task.scheduledTime));
    return taskDate === date;
  }).length;
};

// 当前选中日期的任务列表
const currentDateTasks = computed((): Task[] => {
  if (!selectedDate.value) return [];
  
  return props.tasks.filter(task => {
    const taskDate = formatDateString(new Date(task.scheduledTime));
    return taskDate === selectedDate.value;
  });
});

// 格式化日期为YYYY-MM-DD格式
const formatDateString = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  
  return `${year}-${month}-${day}`;
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

// 获取状态标签
const getStatusLabel = (status: string): string => {
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
};

// 获取状态类型
const getStatusType = (status: string): string => {
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
};

// 处理日期点击
const handleDateClick = (date: string): void => {
  selectedDate.value = date;
  dialogVisible.value = true;
};

// 处理查看任务
const handleViewTask = (task: Task): void => {
  emit('view-task', task);
};

// 处理编辑任务
const handleEditTask = (task: Task): void => {
  emit('edit-task', task);
};
</script>

<style lang="scss" scoped>
.schedule-calendar {
  .calendar-cell {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .date {
      text-align: right;
      margin: 0;
    }
    
    .task-count {
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: 8px;
      cursor: pointer;
      
      .task-text {
        margin-right: 5px;
      }
    }
  }
}
</style> 