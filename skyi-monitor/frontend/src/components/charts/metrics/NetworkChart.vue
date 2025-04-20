<template>
  <div ref="chartRef" class="chart-container">
    <div class="chart-header">
      <div class="chart-title">网络流量</div>
      <div class="chart-info">
        <span class="chart-upload">↑ {{ formatSpeed(uploadSpeed) }}</span>
        <span class="chart-download">↓ {{ formatSpeed(downloadSpeed) }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, defineProps } from 'vue';
import * as echarts from 'echarts';

// 定义数据项接口
interface NetworkDataItem {
  timestamp: string;
  upload: number;      // 上传速率，单位KB/s
  download: number;    // 下载速率，单位KB/s
  packetSent?: number; // 发送的数据包数量
  packetRecv?: number; // 接收的数据包数量
  connections?: number; // 连接数
  [key: string]: any;
}

// 定义组件属性
const props = defineProps({
  // 图表数据
  data: {
    type: Array as () => NetworkDataItem[],
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
  if (speed >= 1024 * 1024) {
    return `${(speed / (1024 * 1024)).toFixed(2)} GB/s`;
  } else if (speed >= 1024) {
    return `${(speed / 1024).toFixed(2)} MB/s`;
  } else {
    return `${speed.toFixed(2)} KB/s`;
  }
};

// 计算当前上传速度 - 取最后一个数据点的值
const uploadSpeed = computed(() => {
  if (!props.data || props.data.length === 0) return 0;
  const lastItem = props.data[props.data.length - 1];
  return lastItem.upload || 0;
});

// 计算当前下载速度 - 取最后一个数据点的值
const downloadSpeed = computed(() => {
  if (!props.data || props.data.length === 0) return 0;
  const lastItem = props.data[props.data.length - 1];
  return lastItem.download || 0;
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
  const timestamps = props.data.map((item: NetworkDataItem) => item.timestamp || '');
  const uploadValues = props.data.map((item: NetworkDataItem) => item.upload || 0);
  const downloadValues = props.data.map((item: NetworkDataItem) => item.download || 0);
  
  // 检查是否有连接数据
  const hasConnectionData = props.data.some(item => item.connections !== undefined);
  const connectionValues = hasConnectionData 
    ? props.data.map(item => item.connections || 0)
    : [];
  
  // 网络特定的默认配置
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
        const upload = uploadValues[dataIndex];
        const download = downloadValues[dataIndex];
        
        let result = `${timestamp}<br/>`;
        result += `上传: ${formatSpeed(upload)}<br/>`;
        result += `下载: ${formatSpeed(download)}`;
        
        if (hasConnectionData) {
          result += `<br/>连接数: ${connectionValues[dataIndex]}`;
        }
        
        return result;
      }
    },
    legend: {
      data: ['上传', '下载'],
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
      name: 'KB/s',
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
        name: '上传',
        type: 'line',
        smooth: true,
        symbol: 'none',
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
        data: uploadValues
      },
      {
        name: '下载',
        type: 'line',
        smooth: true,
        symbol: 'none',
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
        data: downloadValues
      }
    ]
  };
  
  // 如果有连接数数据，添加到右侧Y轴
  if (hasConnectionData && props.options.showConnections !== false) {
    defaultOptions.yAxis = [
      defaultOptions.yAxis,
      {
        type: 'value',
        name: '连接数',
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
      name: '连接数',
      type: 'line',
      yAxisIndex: 1,
      symbol: 'none',
      lineStyle: {
        width: 1.5,
        color: '#52c41a',
        type: 'dashed'
      },
      data: connectionValues
    });
    
    defaultOptions.legend.data.push('连接数');
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

.chart-upload {
  color: #f5222d;
  font-weight: 500;
}

.chart-download {
  color: #1890ff;
  font-weight: 500;
}
</style> 