<template>
  <component 
    :is="resolvedComponent" 
    v-if="resolvedComponent" 
    v-bind="$attrs"
  />
  <div v-else-if="loading" class="dynamic-component-loading">
    <el-skeleton :rows="3" animated />
    <div class="loading-text">加载组件中...</div>
  </div>
  <div v-else-if="error" class="dynamic-component-error">
    <el-alert
      :title="error"
      type="error"
      :closable="false"
      show-icon
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, defineProps, defineEmits } from 'vue';

// 定义组件属性
const props = defineProps({
  // 组件类型
  type: {
    type: String,
    required: true
  },
  // 组件类别
  category: {
    type: String,
    default: 'common'
  },
  // 是否懒加载
  lazy: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['loaded', 'error']);

// 状态变量
const loading = ref(false);
const error = ref('');
const resolvedComponent = ref(null);

// 组件映射关系
const componentRegistry = {
  // 指标相关组件
  metric: {
    // 图表组件
    'line-chart': () => import('./charts/LineChart.vue'),
    'bar-chart': () => import('./charts/BarChart.vue'),
    'pie-chart': () => import('./charts/PieChart.vue'),
    'gauge-chart': () => import('./charts/GaugeChart.vue'),
    
    // 特定指标图表
    'cpu-chart': () => import('./charts/metrics/CpuChart.vue'),
    'memory-chart': () => import('./charts/metrics/MemoryChart.vue'),
    'disk-chart': () => import('./charts/metrics/DiskChart.vue'),
    'network-chart': () => import('./charts/metrics/NetworkChart.vue'),
    
    // 指标数据卡片
    'metric-card': () => import('./metrics/MetricCard.vue'),
    'metric-trend-card': () => import('./metrics/MetricTrendCard.vue')
  },
  
  // 资产相关组件
  asset: {
    'asset-card': () => import('./asset/AssetCard.vue'),
    'asset-list': () => import('./asset/AssetList.vue'),
    'asset-topology': () => import('./asset/AssetTopology.vue')
  },
  
  // 采集相关组件
  collector: {
    'task-card': () => import('./collector/TaskCard.vue'),
    'task-status': () => import('./collector/TaskStatus.vue'),
    'schedule-calendar': () => import('./collector/ScheduleCalendar.vue')
  },
  
  // 告警相关组件
  alert: {
    'alert-card': () => import('./alert/AlertCard.vue'),
    'alert-list': () => import('./alert/AlertList.vue'),
    'alert-timeline': () => import('./alert/AlertTimeline.vue')
  },
  
  // 通用组件
  common: {
    'data-table': () => import('./common/DataTable.vue'),
    'status-badge': () => import('./common/StatusBadge.vue'),
    'json-viewer': () => import('./common/JsonViewer.vue')
  }
};

// 加载组件
const loadComponent = async () => {
  const { type, category } = props;
  
  if (!type) {
    error.value = '未指定组件类型';
    return;
  }
  
  // 查找组件映射
  const categoryMap = componentRegistry[category];
  if (!categoryMap) {
    error.value = `未知的组件类别: ${category}`;
    return;
  }
  
  const componentLoader = categoryMap[type];
  if (!componentLoader) {
    error.value = `未知的组件类型: ${type}`;
    return;
  }
  
  loading.value = true;
  error.value = '';
  
  try {
    // 动态导入组件
    const module = await componentLoader();
    resolvedComponent.value = module.default;
    emit('loaded', { type, category });
  } catch (err) {
    console.error('加载组件失败:', err);
    error.value = err.message || '加载组件失败';
    emit('error', { type, category, error: err });
  } finally {
    loading.value = false;
  }
};

// 监听类型变化重新加载
watch(() => [props.type, props.category], () => {
  if (props.lazy) return;
  loadComponent();
});

// 初始加载
onMounted(() => {
  loadComponent();
});
</script>

<style scoped>
.dynamic-component-loading,
.dynamic-component-error {
  padding: 20px;
  border-radius: 4px;
  background-color: #f5f7fa;
  min-height: 100px;
}

.loading-text {
  margin-top: 10px;
  color: #909399;
  text-align: center;
}
</style> 