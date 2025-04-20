<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as echarts from 'echarts/core';
import { PieChart as PieChartComponent } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// 注册必要的组件
echarts.use([
  PieChartComponent,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer
]);

// 定义数据项接口
interface DataItem {
  name: string;
  value: number;
  [key: string]: any;
}

const props = defineProps({
  data: {
    type: Array as () => DataItem[],
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
  
  // 处理饼图数据
  const pieData = props.data || [];
  
  // 基础配置
  const option = {
    title: {
      text: props.options.title || '',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      data: pieData.map(item => item.name)
    },
    series: [
      {
        name: props.options.seriesName || '数据',
        type: 'pie',
        radius: props.options.radius || ['50%', '70%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 5,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true
        },
        data: pieData
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