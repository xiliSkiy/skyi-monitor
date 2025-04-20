<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as echarts from 'echarts/core';
import { BarChart as BarChartComponent } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// 注册必要的组件
echarts.use([
  BarChartComponent,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  CanvasRenderer
]);

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  options: {
    type: Object,
    default: () => ({})
  }
});

const chartRef = ref<HTMLElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  // 创建图表实例
  chartInstance = echarts.init(chartRef.value);
  
  // 渲染图表
  updateChart();
  
  // 添加窗口大小调整监听
  window.addEventListener('resize', handleResize);
};

// 更新图表数据和配置
const updateChart = () => {
  if (!chartInstance) return;
  
  // 处理时间序列数据
  const timeData = props.data || [];
  
  // 设置X轴数据和Y轴数据
  const xAxisData = timeData.map(item => item.timestamp || item.name || '');
  const seriesData = timeData.map(item => item.value || 0);
  
  // 基础配置
  const option = {
    title: {
      text: props.options.title || ''
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisLabel: {
        interval: 0,
        rotate: xAxisData.length > 8 ? 45 : 0
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: props.options.seriesName || '数值',
        type: 'bar',
        data: seriesData,
        itemStyle: {
          color: props.options.color || '#5470c6'
        },
        label: {
          show: props.options.showLabel || false,
          position: 'top'
        }
      }
    ],
    ...props.options
  };
  
  // 设置图表配置
  chartInstance.setOption(option);
};

// 处理窗口大小调整
const handleResize = () => {
  chartInstance?.resize();
};

// 监听数据变化
watch(() => props.data, updateChart, { deep: true });
watch(() => props.options, updateChart, { deep: true });

// 生命周期钩子
onMounted(() => {
  initChart();
});

onBeforeUnmount(() => {
  // 清理事件监听和图表实例
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
  chartInstance = null;
});
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 100%;
  min-height: 250px;
}
</style> 