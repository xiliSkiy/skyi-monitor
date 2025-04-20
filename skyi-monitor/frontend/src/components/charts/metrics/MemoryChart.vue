<template>
  <div ref="chartRef" class="chart-container">
    <div class="chart-header">
      <div class="chart-title">内存使用率</div>
      <div class="chart-value" :class="valueClass">{{ currentValue }}%</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, defineProps } from 'vue';
import * as echarts from 'echarts';

// 定义数据项接口
interface MemoryDataItem {
  timestamp: string;
  value: number;
  total?: number;
  used?: number;
  [key: string]: any;
}

// 定义组件属性
const props = defineProps({
  // 图表数据 - 格式: [{timestamp: string, value: number, total?: number, used?: number}]
  data: {
    type: Array as () => MemoryDataItem[],
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

// 获取内存使用详情
const memoryDetail = computed(() => {
  if (!props.data || props.data.length === 0) return { used: '0', total: '0' };
  const lastItem = props.data[props.data.length - 1];
  
  let used = '0';
  let total = '0';
  
  if (lastItem.used !== undefined && lastItem.total !== undefined) {
    // 如果数据中包含了used和total
    const usedGB = (lastItem.used / 1024).toFixed(2);
    const totalGB = (lastItem.total / 1024).toFixed(2);
    used = `${usedGB}`;
    total = `${totalGB}`;
  }
  
  return { used, total };
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
  const timestamps = props.data.map((item: MemoryDataItem) => item.timestamp || '');
  const values = props.data.map((item: MemoryDataItem) => item.value || 0);
  
  // 内存特定的默认配置
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
        const item = props.data[dataIndex];
        
        let detail = '';
        if (item.used !== undefined && item.total !== undefined) {
          const usedGB = (item.used / 1024).toFixed(2);
          const totalGB = (item.total / 1024).toFixed(2);
          detail = `<br/>已用: ${usedGB} GB<br/>总计: ${totalGB} GB`;
        }
        
        return `${timestamp}<br/>内存使用率: ${value.toFixed(2)}%${detail}`;
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
        name: '内存使用率',
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#52c41a' },
            { offset: 1, color: 'rgba(82,196,26,0.1)' }
          ])
        },
        itemStyle: {
          color: '#52c41a'
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
}

.chart-header {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 500;
  color: #606266;
}

.chart-value {
  font-size: 24px;
  font-weight: bold;
}

.chart-value.normal {
  color: #52c41a;
}

.chart-value.warning {
  color: #faad14;
}

.chart-value.danger {
  color: #ff4d4f;
}
</style> 