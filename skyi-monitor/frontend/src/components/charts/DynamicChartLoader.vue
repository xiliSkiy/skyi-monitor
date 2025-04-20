<template>
  <component 
    :is="chartComponent" 
    v-if="chartComponent" 
    :data="data" 
    :options="options"
    v-bind="$attrs"
  />
  <div v-else class="chart-loader-fallback">
    <el-skeleton :rows="5" animated />
    <div v-if="loading" class="chart-loading-text">加载中...</div>
    <div v-else-if="error" class="chart-error-text">{{ error }}</div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, defineProps, defineEmits, watchEffect } from 'vue';

// 定义组件属性
const props = defineProps({
  // 图表类型
  type: {
    type: String,
    required: true
  },
  // 图表数据
  data: {
    type: [Array, Object],
    default: () => []
  },
  // 图表配置选项
  options: {
    type: Object,
    default: () => ({})
  },
  // 指标类型（用于特定图表的处理）
  metricType: {
    type: String,
    default: 'default'
  }
});

const emit = defineEmits(['load-success', 'load-error']);

// 状态变量
const loading = ref(true);
const error = ref('');
const chartComponent = ref(null);

// 图表类型与组件的映射关系
const chartComponentMap = {
  // 基础图表类型
  line: () => import('./LineChart.vue'),
  bar: () => import('./BarChart.vue'),
  pie: () => import('./PieChart.vue'),
  gauge: () => import('./GaugeChart.vue'),
  
  // 特定监控指标图表类型
  'cpu': () => import('./metrics/CpuChart.vue'),
  'memory': () => import('./metrics/MemoryChart.vue'),
  'disk': () => import('./metrics/DiskChart.vue'),
  'network': () => import('./metrics/NetworkChart.vue'),
  'io': () => import('./metrics/IoChart.vue')
};

// 根据指标类型和图表类型选择合适的组件
const getComponentType = computed(() => {
  // 先尝试使用特定指标类型的专用图表
  if (props.metricType && chartComponentMap[props.metricType]) {
    return props.metricType;
  }
  
  // 如果没有找到特定指标类型的图表，则使用基础图表类型
  return props.type;
});

// 动态加载对应的图表组件
const loadChartComponent = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const componentType = getComponentType.value;
    
    // 检查是否存在对应的组件
    if (!chartComponentMap[componentType]) {
      throw new Error(`不支持的图表类型: ${componentType}`);
    }
    
    // 动态导入图表组件
    const module = await chartComponentMap[componentType]();
    chartComponent.value = module.default;
    
    emit('load-success', { type: componentType });
    loading.value = false;
  } catch (err) {
    console.error('加载图表组件失败:', err);
    error.value = err.message || '加载图表组件失败';
    emit('load-error', { error: err });
    loading.value = false;
  }
};

// 监听属性变化，重新加载图表组件
watchEffect(() => {
  const componentType = getComponentType.value;
  if (componentType) {
    loadChartComponent();
  }
});

onMounted(() => {
  loadChartComponent();
});
</script>

<style scoped>
.chart-loader-fallback {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}

.chart-loading-text {
  margin-top: 10px;
  color: #909399;
}

.chart-error-text {
  margin-top: 10px;
  color: #f56c6c;
}
</style> 