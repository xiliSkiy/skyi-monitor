<template>
  <div ref="chartRef" class="chart-container">
    <div class="chart-header">
      <div class="chart-title">I/O 性能</div>
      <div class="chart-info">
        <span class="chart-read">读取: {{ formatSpeed(readSpeed) }}</span>
        <span class="chart-write">写入: {{ formatSpeed(writeSpeed) }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, defineProps } from 'vue';
import * as echarts from 'echarts';

// 定义数据项接口
interface IoDataItem {
  timestamp: string;
  read: number;        // 读速率，单位MB/s
  write: number;       // 写速率，单位MB/s
  iops?: number;       // IOPS (IO Operations Per Second)
  latency?: number;    // IO延迟，单位ms
  queueDepth?: number; // IO队列深度
  [key: string]: any;
}

// 定义组件属性
const props = defineProps({
  // 图表数据
  data: {
    type: Array as () => IoDataItem[],
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

// 格式化速度显示
const formatSpeed = (speed: number): string => {
  if (speed >= 1024) {
    return `${(speed / 1024).toFixed(2)} GB/s`;
  } else {
    return `${speed.toFixed(2)} MB/s`;
  }
};

// 计算当前读取速度 - 取最后一个数据点的值
const readSpeed = computed(() => {
  if (!props.data || props.data.length === 0) return 0;
  const lastItem = props.data[props.data.length - 1];
  return lastItem.read || 0;
});

// 计算当前写入速度 - 取最后一个数据点的值
const writeSpeed = computed(() => {
  if (!props.data || props.data.length === 0) return 0;
  const lastItem = props.data[props.data.length - 1];
  return lastItem.write || 0;
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
  const timestamps = props.data.map((item: IoDataItem) => item.timestamp || '');
  const readValues = props.data.map((item: IoDataItem) => item.read || 0);
  const writeValues = props.data.map((item: IoDataItem) => item.write || 0);
  
  // 检查是否有IOPS数据
  const hasIopsData = props.data.some(item => item.iops !== undefined);
  const iopsValues = hasIopsData 
    ? props.data.map(item => item.iops || 0)
    : [];
  
  // 检查是否有延迟数据
  const hasLatencyData = props.data.some(item => item.latency !== undefined);
  const latencyValues = hasLatencyData 
    ? props.data.map(item => item.latency || 0)
    : [];
  
  // IO特定的默认配置
  const defaultOptions = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      },
      formatter: function(params: any) {
        if (!params || params.length === 0) return '';
        
        const dataIndex = params[0].dataIndex;
        const timestamp = timestamps[dataIndex];
        const read = readValues[dataIndex];
        const write = writeValues[dataIndex];
        
        let result = `${timestamp}<br/>`;
        result += `读取: ${formatSpeed(read)}<br/>`;
        result += `写入: ${formatSpeed(write)}`;
        
        if (hasIopsData) {
          result += `<br/>IOPS: ${iopsValues[dataIndex]}`;
        }
        
        if (hasLatencyData) {
          result += `<br/>延迟: ${latencyValues[dataIndex]} ms`;
        }
        
        return result;
      }
    },
    legend: {
      data: ['读取', '写入'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
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
      name: 'MB/s',
      axisLabel: {
        formatter: function(value: number) {
          return formatSpeed(value);
        }
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '读取',
        type: 'line',
        smooth: true,
        lineStyle: {
          width: 2,
          color: '#1890ff'
        },
        areaStyle: {
          opacity: 0.2,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#1890ff' },
            { offset: 1, color: 'rgba(24,144,255,0.1)' }
          ])
        },
        data: readValues
      },
      {
        name: '写入',
        type: 'line',
        smooth: true,
        lineStyle: {
          width: 2,
          color: '#f5222d'
        },
        areaStyle: {
          opacity: 0.2,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f5222d' },
            { offset: 1, color: 'rgba(245,34,45,0.1)' }
          ])
        },
        data: writeValues
      }
    ]
  };
  
  // 如果有IOPS数据，添加到右侧Y轴
  if ((hasIopsData || hasLatencyData) && props.options.showAdditionalMetrics !== false) {
    // 先添加IOPS
    if (hasIopsData) {
      defaultOptions.yAxis = [
        defaultOptions.yAxis,
        {
          type: 'value',
          name: 'IOPS',
          position: 'right',
          axisLine: {
            show: true,
            lineStyle: {
              color: '#52c41a'
            }
          },
          axisLabel: {
            formatter: '{value}'
          },
          splitLine: {
            show: false
          }
        }
      ];
      
      defaultOptions.series.push({
        name: 'IOPS',
        type: 'line',
        yAxisIndex: 1,
        symbol: 'none',
        lineStyle: {
          width: 1.5,
          color: '#52c41a'
        },
        data: iopsValues
      });
      
      defaultOptions.legend.data.push('IOPS');
    }
    
    // 再添加延迟
    if (hasLatencyData) {
      const yAxisIndex = hasIopsData ? 2 : 1;
      
      if (Array.isArray(defaultOptions.yAxis)) {
        defaultOptions.yAxis.push({
          type: 'value',
          name: '延迟(ms)',
          position: 'right',
          offset: hasIopsData ? 80 : 0,
          axisLine: {
            show: true,
            lineStyle: {
              color: '#fa8c16'
            }
          },
          axisLabel: {
            formatter: '{value} ms'
          },
          splitLine: {
            show: false
          }
        });
      } else {
        defaultOptions.yAxis = [
          defaultOptions.yAxis,
          {
            type: 'value',
            name: '延迟(ms)',
            position: 'right',
            axisLine: {
              show: true,
              lineStyle: {
                color: '#fa8c16'
              }
            },
            axisLabel: {
              formatter: '{value} ms'
            },
            splitLine: {
              show: false
            }
          }
        ];
      }
      
      defaultOptions.series.push({
        name: '延迟',
        type: 'line',
        yAxisIndex: yAxisIndex,
        symbol: 'none',
        lineStyle: {
          width: 1.5,
          color: '#fa8c16'
        },
        data: latencyValues
      });
      
      defaultOptions.legend.data.push('延迟');
    }
  }
  
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

.chart-info {
  display: flex;
  gap: 16px;
}

.chart-read {
  color: #1890ff;
  font-weight: 500;
}

.chart-write {
  color: #f5222d;
  font-weight: 500;
}
</style> 