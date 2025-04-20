<template>
  <div ref="chartRef" class="chart-container">
    <div class="chart-header">
      <div class="chart-title">CPU 使用率</div>
      <div class="chart-value" :class="valueClass">{{ currentValue }}%</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, defineProps } from 'vue';
import * as echarts from 'echarts';

// 定义组件属性
const props = defineProps({
  // 图表数据 - 格式: [{timestamp: string, value: number}]
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

// 计算当前值 - 取最后一个数据点的值
const currentValue = computed(() => {
  if (!props.data || props.data.length === 0) return '0.0';
  const lastItem = props.data[props.data.length - 1];
  return typeof lastItem.value === 'number' 
    ? lastItem.value.toFixed(1) 
    : '0.0';
});

// 根据当前值计算CSS类
const valueClass = computed(() => {
  const value = parseFloat(currentValue.value);
  if (value >= 90) return 'danger';
  if (value >= 75) return 'warning';
  return 'normal';
});

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  // 为图表预留空间
  const chartContainer = document.createElement('div');
  chartContainer.style.width = '100%';
  chartContainer.style.height = 'calc(100% - 50px)';
  chartContainer.style.marginTop = '50px';
  chartRef.value.appendChild(chartContainer);
  
  // 创建ECharts实例
  chartInstance = echarts.init(chartContainer);
  
  // 设置响应式调整大小
  window.addEventListener('resize', handleResize);
  
  // 渲染图表
  updateChart();
};

// 更新图表数据和配置
const updateChart = () => {
  if (!chartInstance) return;
  
  // 处理数据
  const timestamps = props.data.map((item: any) => item.timestamp || '');
  const values = props.data.map((item: any) => item.value || 0);
  
  // CPU特定的默认配置
  const defaultOptions = {
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10px',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params: any) {
        const dataIndex = params[0].dataIndex;
        const timestamp = timestamps[dataIndex];
        const value = values[dataIndex];
        return `${timestamp}<br/>CPU使用率: ${value.toFixed(2)}%`;
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: timestamps,
      axisLabel: {
        formatter: function(value: string) {
          // 仅显示时间部分
          return value.split(' ').pop() || value;
        }
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: 'CPU使用率',
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#1890ff' },
            { offset: 1, color: 'rgba(24,144,255,0.1)' }
          ])
        },
        itemStyle: {
          color: '#1890ff'
        },
        lineStyle: {
          width: 2
        },
        data: values,
        markLine: {
          silent: true,
          lineStyle: {
            color: '#ff4d4f'
          },
          data: [
            {
              yAxis: 90,
              name: '警戒线',
              label: {
                formatter: '警戒线: 90%',
                position: 'start'
              }
            }
          ]
        }
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
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.chart-header {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  z-index: 1;
}

.chart-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.chart-value {
  font-size: 20px;
  font-weight: 600;
}

.chart-value.normal {
  color: #67c23a;
}

.chart-value.warning {
  color: #e6a23c;
}

.chart-value.danger {
  color: #f56c6c;
}
</style> 