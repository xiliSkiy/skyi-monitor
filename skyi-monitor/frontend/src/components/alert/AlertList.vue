<template>
  <div class="alert-list">
    <el-table :data="alerts" style="width: 100%" v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          <el-link type="primary" @click="handleViewDetail(scope.row)">{{ scope.row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="severity" label="级别" width="100">
        <template #default="scope">
          <el-tag :type="getSeverityType(scope.row.severity)" effect="dark">{{ getSeverityLabel(scope.row.severity) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="asset" label="资产" width="120" />
      <el-table-column prop="metric" label="指标" width="120" />
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.status === 'ACTIVE'" size="small" type="primary" @click="handleAcknowledge(scope.row)">确认</el-button>
          <el-button v-if="scope.row.status !== 'RESOLVED'" size="small" type="success" @click="handleResolve(scope.row)">解决</el-button>
          <el-button size="small" @click="handleViewDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination" v-if="showPagination">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, defineEmits } from 'vue';

interface Alert {
  id: string | number;
  title: string;
  severity: string;
  status: string;
  asset: string;
  metric: string;
  createTime: string | Date;
  [key: string]: any;
}

const props = defineProps({
  alerts: {
    type: Array as () => Alert[],
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

const emit = defineEmits(['view', 'acknowledge', 'resolve', 'size-change', 'current-change']);

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

// 处理确认告警
const handleAcknowledge = (alert: Alert): void => {
  emit('acknowledge', alert);
};

// 处理解决告警
const handleResolve = (alert: Alert): void => {
  emit('resolve', alert);
};

// 处理每页数量变化
const handleSizeChange = (size: number): void => {
  emit('size-change', size);
};

// 处理页码变化
const handleCurrentChange = (page: number): void => {
  emit('current-change', page);
};
</script>

<style lang="scss" scoped>
.alert-list {
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style> 