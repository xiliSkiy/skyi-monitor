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
    'line-chart': () => import('@/components/charts/LineChart.vue'),
    'bar-chart': () => import('@/components/charts/BarChart.vue'),
    'pie-chart': () => import('@/components/charts/PieChart.vue'),
    'gauge-chart': () => import('@/components/charts/GaugeChart.vue'),
    
    // 特定指标图表
    'cpu-chart': () => import('@/components/charts/metrics/CpuChart.vue'),
    'memory-chart': () => import('@/components/charts/metrics/MemoryChart.vue'),
    'disk-chart': () => import('@/components/charts/metrics/DiskChart.vue'),
    'network-chart': () => import('@/components/charts/metrics/NetworkChart.vue')
  },
  
  // 资产相关组件
  asset: {
    'asset-card': () => import('@/components/asset/AssetCard.vue'),
    'asset-list': () => import('@/components/asset/AssetList.vue'),
    'asset-topology': () => import('@/components/asset/AssetTopology.vue')
  },
  
  // 采集相关组件
  collector: {
    'task-card': () => import('@/components/collector/TaskCard.vue'),
    'task-status': () => import('@/components/collector/TaskStatus.vue'),
    'schedule-calendar': () => import('@/components/collector/ScheduleCalendar.vue')
  },
  
  // 告警相关组件
  alert: {
    'alert-card': () => import('@/components/alert/AlertCard.vue'),
    'alert-list': () => import('@/components/alert/AlertList.vue'),
    'alert-timeline': () => import('@/components/alert/AlertTimeline.vue')
  },
  
  // 通用组件
  common: {
    'dynamic-chart-loader': () => import('@/components/charts/DynamicChartLoader.vue')
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
  const categoryMap = componentRegistry[category as keyof typeof componentRegistry];
  if (!categoryMap) {
    error.value = `未知的组件类别: ${category}`;
    return;
  }
  
  const componentLoader = categoryMap[type as keyof typeof categoryMap];
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