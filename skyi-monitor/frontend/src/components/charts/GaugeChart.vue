<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue';
import * as echarts from 'echarts/core';
import { GaugeChart as GaugeChartComponent } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// 注册必要的组件
echarts.use([
  GaugeChartComponent,
  TitleComponent,
  TooltipComponent,
  CanvasRenderer
]);

const props = defineProps({
  data: {
    type: [Array, Object],
    default: () => []
  },
  options: {
    type: Object,
    default: () => ({})
  }
});

const chartRef = ref<HTMLElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

// 从数据中提取值
const gaugeValue = computed(() => {
  if (Array.isArray(props.data) && props.data.length > 0) {
    return props.data[0].value || 0;
  } else if (typeof props.data === 'object' && props.data) {
    return (props.data as any).value || 0;
  }
  return 0;
});

// 从数据中提取名称
const gaugeName = computed(() => {
  if (Array.isArray(props.data) && props.data.length > 0) {
    return props.data[0].name || '指标';
  } else if (typeof props.data === 'object' && props.data) {
    return (props.data as any).name || '指标';
  }
  return '指标';
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
  
  // 仪表盘配置
  const option = {
    title: {
      text: props.options.title || gaugeName.value,
      left: 'center'
    },
    tooltip: {
      formatter: '{a} <br/>{b}: {c}%'
    },
    series: [
      {
        name: props.options.seriesName || gaugeName.value,
        type: 'gauge',
        min: props.options.min || 0,
        max: props.options.max || 100,
        detail: {
          formatter: '{value}%',
          fontSize: 16
        },
        data: [
          {
            value: gaugeValue.value,
            name: gaugeName.value
          }
        ],
        axisLine: {
          lineStyle: {
            width: 30,
            color: props.options.axisLineColor || [
              [0.3, '#67e0e3'],
              [0.7, '#37a2da'],
              [1, '#fd666d']
            ]
          }
        },
        pointer: {
          itemStyle: {
            color: 'auto'
          }
        },
        axisTick: {
          distance: -30,
          length: 8,
          lineStyle: {
            color: '#fff',
            width: 2
          }
        },
        splitLine: {
          distance: -30,
          length: 30,
          lineStyle: {
            color: '#fff',
            width: 4
          }
        },
        axisLabel: {
          color: 'inherit',
          distance: 40,
          fontSize: 14
        },
        ...props.options.series
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