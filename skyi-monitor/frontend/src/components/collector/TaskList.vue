<template>
  <div class="task-list">
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索任务名称或目标"
        prefix-icon="el-icon-search"
        clearable
        style="width: 250px; margin-right: 15px;"
      />
      
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px; margin-right: 15px;">
        <el-option label="全部" value="" />
        <el-option label="运行中" value="RUNNING" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="失败" value="FAILED" />
        <el-option label="等待中" value="PENDING" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      
      <el-select v-model="typeFilter" placeholder="类型筛选" clearable style="width: 140px; margin-right: 15px;">
        <el-option label="全部" value="" />
        <el-option label="SNMP" value="SNMP" />
        <el-option label="API" value="API" />
        <el-option label="SSH" value="SSH" />
        <el-option label="脚本" value="SCRIPT" />
      </el-select>
      
      <el-button type="primary" @click="handleAddTask">新建任务</el-button>
    </div>
    
    <el-table
      :data="filteredTasks"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
      :height="tableHeight"
    >
      <el-table-column prop="title" label="任务名称" min-width="180">
        <template #default="scope">
          <el-link type="primary" @click="handleViewTask(scope.row)">{{ scope.row.title }}</el-link>
        </template>
      </el-table-column>
      
      <el-table-column prop="target" label="采集目标" min-width="150" />
      
      <el-table-column prop="type" label="类型" width="120">
        <template #default="scope">
          <el-tag type="info">{{ scope.row.type }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="protocol" label="协议" width="120" />
      
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="scheduledTime" label="计划时间" width="160">
        <template #default="scope">
          {{ formatTime(scope.row.scheduledTime) }}
        </template>
      </el-table-column>
      
      <el-table-column prop="lastExecutionTime" label="上次执行" width="160">
        <template #default="scope">
          {{ formatTime(scope.row.lastExecutionTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" fixed="right" width="240">
        <template #default="scope">
          <el-button size="small" @click="handleExecuteTask(scope.row)" :disabled="!canExecute(scope.row)">
            立即执行
          </el-button>
          <el-button size="small" type="primary" @click="handleEditTask(scope.row)">
            编辑
          </el-button>
          <el-button size="small" type="danger" @click="handleCancelTask(scope.row)" :disabled="!canCancel(scope.row)">
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination" v-if="showPagination">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue';

interface Task {
  id: string | number;
  title: string;
  target: string;
  type: string;
  protocol: string;
  status: string;
  scheduledTime: string | Date;
  lastExecutionTime: string | Date;
  nextExecutionTime: string | Date;
  interval: number;
  [key: string]: any;
}

const props = defineProps({
  tasks: {
    type: Array as () => Task[],
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits([
  'add-task',
  'edit-task',
  'view-task',
  'execute-task',
  'cancel-task',
  'page-change',
  'size-change'
]);

// 筛选参数
const searchKeyword = ref('');
const statusFilter = ref('');
const typeFilter = ref('');
const tableHeight = ref('calc(100vh - 230px)');

// 筛选任务列表
const filteredTasks = computed(() => {
  if (!props.tasks || props.tasks.length === 0) return [];
  
  return props.tasks.filter((task: Task) => {
    // 关键词筛选
    const keywordMatch = !searchKeyword.value || 
      task.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) || 
      task.target.toLowerCase().includes(searchKeyword.value.toLowerCase());
    
    // 状态筛选
    const statusMatch = !statusFilter.value || task.status === statusFilter.value;
    
    // 类型筛选
    const typeMatch = !typeFilter.value || task.type === typeFilter.value;
    
    return keywordMatch && statusMatch && typeMatch;
  });
});

// 格式化时间
const formatTime = (time: string | Date | null | undefined): string => {
  if (!time) return '-';
  
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

// 判断任务是否可以立即执行
const canExecute = (task: Task): boolean => {
  return task.status !== 'RUNNING';
};

// 判断任务是否可以取消
const canCancel = (task: Task): boolean => {
  return task.status === 'RUNNING' || task.status === 'PENDING';
};

// 处理新建任务
const handleAddTask = (): void => {
  emit('add-task');
};

// 处理编辑任务
const handleEditTask = (task: Task): void => {
  emit('edit-task', task);
};

// 处理查看任务
const handleViewTask = (task: Task): void => {
  emit('view-task', task);
};

// 处理执行任务
const handleExecuteTask = (task: Task): void => {
  emit('execute-task', task);
};

// 处理取消任务
const handleCancelTask = (task: Task): void => {
  emit('cancel-task', task);
};

// 处理分页大小改变
const handleSizeChange = (size: number): void => {
  emit('size-change', size);
};

// 处理页码改变
const handleCurrentChange = (page: number): void => {
  emit('page-change', page);
};

// 监听搜索条件变化，如果使用本地分页则需要更新
watch([searchKeyword, statusFilter, typeFilter], () => {
  if (!props.showPagination) {
    // 本地分页，重置到第一页
    emit('page-change', 1);
  }
});

// 组件加载后调整表格高度
onMounted(() => {
  window.addEventListener('resize', () => {
    tableHeight.value = 'calc(100vh - 230px)';
  });
});
</script>

<style lang="scss" scoped>
.task-list {
  height: 100%;
  
  .filter-bar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    margin-bottom: 10px;
  }
  
  .pagination {
    margin-top: 20px;
    text-align: center;
  }
}
</style> 