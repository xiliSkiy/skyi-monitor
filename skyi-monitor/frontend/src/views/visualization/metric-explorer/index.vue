<template>
  <div class="metric-explorer">
    <div class="explorer-header">
      <h1>指标浏览器</h1>
      <div class="time-selector">
        <span>时间范围：</span>
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :shortcuts="dateShortcuts"
          @change="handleTimeRangeChange"
        />
      </div>
    </div>
    
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="filter-card">
          <template #header>
            <div class="card-header">
              <span>指标筛选</span>
            </div>
          </template>
          
          <div class="filter-section">
            <h3>资产</h3>
            <el-select v-model="selectedAsset" placeholder="选择资产" clearable style="width: 100%">
              <el-option
                v-for="asset in assets"
                :key="asset.id"
                :label="asset.name"
                :value="asset.id"
              />
            </el-select>
          </div>
          
          <div class="filter-section">
            <h3>指标类型</h3>
            <el-checkbox-group v-model="selectedMetricTypes">
              <el-checkbox v-for="type in metricTypes" :key="type.value" :value="type.value">
                {{ type.label }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
          
          <div class="filter-section">
            <h3>指标列表</h3>
            <el-input 
              v-model="metricSearchKeyword" 
              placeholder="搜索指标" 
              prefix-icon="el-icon-search" 
              clearable
            />
            <div class="metric-list">
              <el-checkbox-group v-model="selectedMetrics">
                <el-checkbox 
                  v-for="metric in filteredMetrics" 
                  :key="metric.name" 
                  :value="metric.name"
                >
                  {{ metric.name }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
          </div>
          
          <div class="filter-actions">
            <el-button type="primary" @click="applyFilters">应用筛选</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>指标可视化</span>
              <div class="chart-actions">
                <el-button-group>
                  <el-button size="small" @click="refreshData">刷新</el-button>
                  <el-button size="small" @click="exportData">导出</el-button>
                  <el-button size="small" @click="saveAsReport">保存为报表</el-button>
                </el-button-group>
              </div>
            </div>
          </template>
          
          <div class="chart-container" ref="chartRef">
            <!-- 图表将在这里渲染 -->
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import * as echarts from 'echarts';

// 时间范围
const timeRange = ref<[Date, Date]>([
  new Date(Date.now() - 24 * 60 * 60 * 1000), // 默认24小时前
  new Date()
]);

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一小时',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000);
      return [start, end];
    }
  },
  {
    text: '最近6小时',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 6 * 3600 * 1000);
      return [start, end];
    }
  },
  {
    text: '最近12小时',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 12 * 3600 * 1000);
      return [start, end];
    }
  },
  {
    text: '最近24小时',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 24 * 3600 * 1000);
      return [start, end];
    }
  },
  {
    text: '最近7天',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 7 * 24 * 3600 * 1000);
      return [start, end];
    }
  }
];

// 资产列表
const assets = ref([
  { id: 1, name: '服务器1' },
  { id: 2, name: '服务器2' },
  { id: 3, name: '数据库服务器' },
  { id: 4, name: '网络设备1' }
]);

// 指标类型
const metricTypes = [
  { label: 'CPU', value: 'cpu' },
  { label: '内存', value: 'memory' },
  { label: '磁盘', value: 'disk' },
  { label: '网络', value: 'network' },
  { label: '应用', value: 'application' }
];

// 指标列表
const metrics = ref([
  { name: 'cpu.usage', type: 'cpu', unit: '%' },
  { name: 'cpu.load', type: 'cpu', unit: '' },
  { name: 'memory.used', type: 'memory', unit: 'MB' },
  { name: 'memory.free', type: 'memory', unit: 'MB' },
  { name: 'memory.usage', type: 'memory', unit: '%' },
  { name: 'disk.used', type: 'disk', unit: 'GB' },
  { name: 'disk.free', type: 'disk', unit: 'GB' },
  { name: 'disk.usage', type: 'disk', unit: '%' },
  { name: 'network.in', type: 'network', unit: 'KB/s' },
  { name: 'network.out', type: 'network', unit: 'KB/s' },
  { name: 'application.response', type: 'application', unit: 'ms' },
  { name: 'application.requests', type: 'application', unit: 'rps' }
]);

// 筛选状态
const selectedAsset = ref(null);
const selectedMetricTypes = ref(['cpu', 'memory']);
const selectedMetrics = ref(['cpu.usage', 'memory.usage']);
const metricSearchKeyword = ref('');

// 筛选后的指标列表
const filteredMetrics = computed(() => {
  return metrics.value.filter(metric => {
    const keywordMatch = !metricSearchKeyword.value || 
      metric.name.toLowerCase().includes(metricSearchKeyword.value.toLowerCase());
    
    const typeMatch = selectedMetricTypes.value.length === 0 || 
      selectedMetricTypes.value.includes(metric.type);
    
    return keywordMatch && typeMatch;
  });
});

// 图表相关
const chartRef = ref<HTMLElement | null>(null);
let chart: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  chart = echarts.init(chartRef.value);
  
  // 模拟数据
  const generateData = () => {
    const times = [];
    const data1 = [];
    const data2 = [];
    
    const startTime = timeRange.value[0].getTime();
    const endTime = timeRange.value[1].getTime();
    const step = (endTime - startTime) / 50;
    
    for (let t = startTime; t <= endTime; t += step) {
      const date = new Date(t);
      times.push(date.toLocaleTimeString());
      data1.push(Math.round(Math.random() * 50 + 20)); // cpu usage 20-70%
      data2.push(Math.round(Math.random() * 40 + 30)); // memory usage 30-70%
    }
    
    return { times, data1, data2 };
  };
  
  const { times, data1, data2 } = generateData();
  
  const option = {
    title: {
      text: '资源使用率变化趋势'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['CPU使用率', '内存使用率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: 'CPU使用率',
        type: 'line',
        data: data1,
        smooth: true,
        lineStyle: {
          width: 2
        }
      },
      {
        name: '内存使用率',
        type: 'line',
        data: data2,
        smooth: true,
        lineStyle: {
          width: 2
        }
      }
    ]
  };
  
  chart.setOption(option);
  
  window.addEventListener('resize', resizeChart);
};

// 调整图表大小
const resizeChart = () => {
  if (chart) {
    chart.resize();
  }
};

// 应用筛选条件
const applyFilters = () => {
  console.log('应用筛选:', {
    asset: selectedAsset.value,
    metricTypes: selectedMetricTypes.value,
    metrics: selectedMetrics.value,
    timeRange: timeRange.value
  });
  
  // 在实际应用中，这里会调用API获取数据并更新图表
  
  // 测试: 更新图表
  updateChart();
};

// 重置筛选条件
const resetFilters = () => {
  selectedAsset.value = null;
  selectedMetricTypes.value = ['cpu', 'memory'];
  selectedMetrics.value = ['cpu.usage', 'memory.usage'];
  metricSearchKeyword.value = '';
};

// 刷新数据
const refreshData = () => {
  console.log('刷新数据');
  updateChart();
};

// 导出数据
const exportData = () => {
  console.log('导出数据');
  // 实现导出功能
};

// 保存为报表
const saveAsReport = () => {
  console.log('保存为报表');
  // 实现保存为报表功能
};

// 更新图表
const updateChart = () => {
  if (!chart) return;
  
  // 模拟数据更新
  const generateData = () => {
    const times = [];
    const data1 = [];
    const data2 = [];
    
    const startTime = timeRange.value[0].getTime();
    const endTime = timeRange.value[1].getTime();
    const step = (endTime - startTime) / 50;
    
    for (let t = startTime; t <= endTime; t += step) {
      const date = new Date(t);
      times.push(date.toLocaleTimeString());
      data1.push(Math.round(Math.random() * 50 + 20)); // cpu usage 20-70%
      data2.push(Math.round(Math.random() * 40 + 30)); // memory usage 30-70%
    }
    
    return { times, data1, data2 };
  };
  
  const { times, data1, data2 } = generateData();
  
  chart.setOption({
    xAxis: {
      data: times
    },
    series: [
      {
        name: 'CPU使用率',
        data: data1
      },
      {
        name: '内存使用率',
        data: data2
      }
    ]
  });
};

// 处理时间范围变化
const handleTimeRangeChange = () => {
  console.log('时间范围变化:', timeRange.value);
  updateChart();
};

// 组件挂载后初始化图表
onMounted(() => {
  initChart();
});

// 组件卸载前销毁图表
onBeforeUnmount(() => {
  if (chart) {
    chart.dispose();
    chart = null;
  }
  window.removeEventListener('resize', resizeChart);
});
</script>

<style lang="scss" scoped>
.metric-explorer {
  padding: 20px;
  
  .explorer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
    
    .filter-section {
      margin-bottom: 15px;
      
      h3 {
        margin-top: 0;
        margin-bottom: 10px;
        font-size: 16px;
      }
      
      .metric-list {
        max-height: 200px;
        overflow-y: auto;
        margin-top: 10px;
        padding: 5px;
        border: 1px solid #EBEEF5;
        border-radius: 4px;
      }
    }
    
    .filter-actions {
      margin-top: 20px;
      text-align: center;
    }
  }
  
  .chart-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .chart-container {
      height: 500px;
    }
  }
}
</style> 