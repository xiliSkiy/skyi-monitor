<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue';
import * as echarts from 'echarts/core';
import { LineChart as LineChartComponent } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// 定义数据项接口
interface ChartDataItem {
  name: string;
  value: number;
  time?: string;
  date?: string;
  [key: string]: any;
}

// 注册必要的组件
echarts.use([
  LineChartComponent,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  CanvasRenderer
]);

const props = defineProps({
  data: {
    type: Array as () => ChartDataItem[],
    default: () => []
  },
  options: {
    type: Object,
    default: () => ({})
  }
});

const chartRef = ref<HTMLElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

// 从数据中提取系列和分类
const processedData = computed(() => {
  if (!Array.isArray(props.data) || props.data.length === 0) return { series: [], categories: [] };
  
  // 提取所有类别名称
  const categories = Array.from(new Set(props.data.map((item: ChartDataItem) => item.name || '')));
  
  // 按类别组织数据
  const seriesData = categories.map(category => {
    const categoryData = props.data.filter((item: ChartDataItem) => item.name === category);
    return {
      name: category,
      type: 'line',
      data: categoryData.map((item: ChartDataItem) => item.value),
      // 允许通过options定制线条样式
      ...props.options.seriesStyle
    };
  });
  
  // 提取时间轴（X轴）数据，假设每个类别的数据都有相同的时间点
  const xAxisData = props.data.filter((item: ChartDataItem) => item.name === categories[0])
    .map((item: ChartDataItem) => item.time || item.date || '');
  
  return {
    series: seriesData,
    categories: xAxisData
  };
});

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
  
  // 折线图配置
  const option = {
    title: {
      text: props.options.title || '折线图',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: processedData.value.series.map(s => s.name),
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: processedData.value.categories,
      ...props.options.xAxis
    },
    yAxis: {
      type: 'value',
      ...props.options.yAxis
    },
    series: processedData.value.series,
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