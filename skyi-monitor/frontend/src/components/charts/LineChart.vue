<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch, defineProps } from 'vue';
import * as echarts from 'echarts';

// 定义组件属性
const props = defineProps({
  // 图表数据
  data: {
    type: Array,
    default: () => []
  },
  // 图表配置选项
  options: {
    type: Object,
    default: () => ({})
  }
});

// 图表DOM引用
const chartRef = ref<HTMLElement | null>(null);
// 图表实例
let chartInstance: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  // 创建ECharts实例
  chartInstance = echarts.init(chartRef.value);
  
  // 设置响应式调整大小
  window.addEventListener('resize', handleResize);
  
  // 渲染图表
  updateChart();
};

// 更新图表数据和配置
const updateChart = () => {
  if (!chartInstance) return;
  
  // 默认配置
  const defaultOptions = {
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.data.map((item: any) => item.timestamp || item.name || '')
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '数值',
        type: 'line',
        stack: '总量',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: props.data.map((item: any) => item.value || 0)
      }
    ]
  };
  
  // 合并用户自定义配置
  const mergedOptions = {
    ...defaultOptions,
    ...props.options
  };
  
  // 应用配置
  chartInstance.setOption(mergedOptions, true);
};

// 处理窗口大小调整
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

// 监听数据和配置变化
watch(() => props.data, updateChart, { deep: true });
watch(() => props.options, updateChart, { deep: true });

// 生命周期钩子
onMounted(() => {
  initChart();
});

onBeforeUnmount(() => {
  // 清理资源
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 300px;
}
</style> 