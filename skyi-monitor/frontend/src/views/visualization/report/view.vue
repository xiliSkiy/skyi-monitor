<template>
  <div class="report-view">
    <div class="page-header">
      <div class="title-section">
        <h1>{{ report.name }}</h1>
        <el-tag>{{ getCategoryLabel(report.category) }}</el-tag>
      </div>
      <div class="actions">
        <el-button @click="handleBack">返回</el-button>
        <el-button type="primary" @click="handleEdit">编辑</el-button>
        <el-button type="success" @click="handleExport">导出</el-button>
        <el-button type="warning" @click="handleRefresh">刷新</el-button>
      </div>
    </div>
    
    <div class="report-description" v-if="report.description">
      <el-alert
        :title="report.description"
        type="info"
        :closable="false"
        show-icon
      />
    </div>
    
    <div class="time-selector">
      <span>时间范围：</span>
      <el-select v-model="selectedTimeRange" @change="handleTimeRangeChange" style="width: 150px">
        <el-option label="最近1小时" value="1h" />
        <el-option label="最近6小时" value="6h" />
        <el-option label="最近12小时" value="12h" />
        <el-option label="最近24小时" value="1d" />
        <el-option label="最近7天" value="7d" />
        <el-option label="最近30天" value="30d" />
        <el-option label="自定义" value="custom" />
      </el-select>
      
      <el-date-picker
        v-if="selectedTimeRange === 'custom'"
        v-model="customTimeRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        style="margin-left: 10px; width: 360px"
        @change="handleCustomTimeRangeChange"
      />
    </div>
    
    <div class="report-content">
      <el-row :gutter="20">
        <el-col :span="24" v-if="report.dataSourceType === 'INFLUXDB'">
          <el-card class="chart-card">
            <div class="chart-container" ref="chartRef">
              <!-- 图表将在这里渲染 -->
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="24" v-if="report.dataSourceType === 'MYSQL'">
          <el-card class="table-card">
            <el-table
              :data="tableData"
              border
              style="width: 100%"
              max-height="500"
              v-loading="loading"
            >
              <el-table-column
                v-for="(column, index) in tableColumns"
                :key="index"
                :prop="column.prop"
                :label="column.label"
                :width="column.width"
              />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import * as echarts from 'echarts';

const route = useRoute();
const router = useRouter();

// 状态
const loading = ref(false);
const selectedTimeRange = ref('1d');
const customTimeRange = ref<[Date, Date]>([
  new Date(Date.now() - 24 * 60 * 60 * 1000),
  new Date()
]);

// 图表相关
const chartRef = ref<HTMLElement | null>(null);
let chart: echarts.ECharts | null = null;

// 表格数据
const tableData = ref<any[]>([]);
const tableColumns = ref<{ prop: string; label: string; width?: string }[]>([]);

// 模拟报表数据
const report = ref({
  id: Number(route.params.id) || 1,
  name: '系统性能概览',
  category: 'SYSTEM',
  description: '显示系统CPU、内存、磁盘和网络等关键性能指标',
  dataSourceType: 'INFLUXDB',
  metrics: ['cpu.usage', 'memory.usage', 'disk.usage'],
  timeRange: '1d',
  creator: '系统管理员',
  createTime: '2023-01-15T10:00:00',
  updateTime: '2023-01-15T10:00:00'
});

// 初始化
onMounted(() => {
  loadReportData();
});

// 加载报表数据
const loadReportData = () => {
  const id = route.params.id;
  console.log('加载报表数据，ID:', id);
  
  loading.value = true;
  
  // 模拟API调用延迟
  setTimeout(() => {
    if (report.value.dataSourceType === 'INFLUXDB') {
      initChart();
    } else if (report.value.dataSourceType === 'MYSQL') {
      initTable();
    }
    
    loading.value = false;
  }, 1000);
};

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  chart = echarts.init(chartRef.value);
  
  // 模拟数据
  const generateData = () => {
    const times = [];
    const cpuData = [];
    const memoryData = [];
    const diskData = [];
    
    // 根据选择的时间范围生成数据点
    let startTime: Date;
    let endTime = new Date();
    
    if (selectedTimeRange.value === 'custom') {
      startTime = customTimeRange.value[0];
      endTime = customTimeRange.value[1];
    } else {
      let hours = 24;
      
      switch (selectedTimeRange.value) {
        case '1h':
          hours = 1;
          break;
        case '6h':
          hours = 6;
          break;
        case '12h':
          hours = 12;
          break;
        case '7d':
          hours = 24 * 7;
          break;
        case '30d':
          hours = 24 * 30;
          break;
      }
      
      startTime = new Date(endTime.getTime() - hours * 60 * 60 * 1000);
    }
    
    const step = (endTime.getTime() - startTime.getTime()) / 50;
    
    for (let t = startTime.getTime(); t <= endTime.getTime(); t += step) {
      const date = new Date(t);
      times.push(date.toLocaleString());
      cpuData.push(Math.round(Math.random() * 50 + 20));
      memoryData.push(Math.round(Math.random() * 40 + 30));
      diskData.push(Math.round(Math.random() * 30 + 40));
    }
    
    return { times, cpuData, memoryData, diskData };
  };
  
  const { times, cpuData, memoryData, diskData } = generateData();
  
  const option = {
    title: {
      text: '系统资源使用率变化趋势'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['CPU使用率', '内存使用率', '磁盘使用率']
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
        data: cpuData,
        smooth: true
      },
      {
        name: '内存使用率',
        type: 'line',
        data: memoryData,
        smooth: true
      },
      {
        name: '磁盘使用率',
        type: 'line',
        data: diskData,
        smooth: true
      }
    ]
  };
  
  chart.setOption(option);
  
  window.addEventListener('resize', resizeChart);
};

// 初始化表格
const initTable = () => {
  // 模拟MySQL查询结果数据
  tableColumns.value = [
    { prop: 'host', label: '主机名', width: '150' },
    { prop: 'cpu', label: 'CPU使用率(%)', width: '120' },
    { prop: 'memory', label: '内存使用率(%)', width: '120' },
    { prop: 'disk', label: '磁盘使用率(%)', width: '120' },
    { prop: 'network_in', label: '网络流入(KB/s)', width: '150' },
    { prop: 'network_out', label: '网络流出(KB/s)', width: '150' },
    { prop: 'time', label: '采集时间', width: '180' }
  ];
  
  tableData.value = [
    { host: 'server-01', cpu: 45.2, memory: 62.8, disk: 73.5, network_in: 1024.5, network_out: 512.8, time: '2023-03-01 10:00:00' },
    { host: 'server-02', cpu: 32.1, memory: 55.3, disk: 65.2, network_in: 876.3, network_out: 435.2, time: '2023-03-01 10:00:00' },
    { host: 'server-03', cpu: 78.5, memory: 85.2, disk: 45.8, network_in: 2048.7, network_out: 1024.3, time: '2023-03-01 10:00:00' },
    { host: 'server-04', cpu: 25.3, memory: 42.1, disk: 58.6, network_in: 512.4, network_out: 256.2, time: '2023-03-01 10:00:00' },
    { host: 'server-05', cpu: 65.7, memory: 72.4, disk: 88.9, network_in: 1536.8, network_out: 768.5, time: '2023-03-01 10:00:00' }
  ];
};

// 调整图表大小
const resizeChart = () => {
  if (chart) {
    chart.resize();
  }
};

// 获取分类标签
const getCategoryLabel = (category: string): string => {
  switch (category) {
    case 'SYSTEM':
      return '系统报表';
    case 'RESOURCE':
      return '资源报表';
    case 'PERFORMANCE':
      return '性能报表';
    case 'BUSINESS':
      return '业务报表';
    case 'CUSTOM':
      return '自定义报表';
    default:
      return category;
  }
};

// 返回
const handleBack = () => {
  router.push('/visualization/reports');
};

// 编辑
const handleEdit = () => {
  router.push(`/visualization/reports/edit/${report.value.id}`);
};

// 导出
const handleExport = () => {
  console.log('导出报表:', report.value.name);
  // 实现导出功能
};

// 刷新
const handleRefresh = () => {
  console.log('刷新报表');
  loadReportData();
};

// 处理时间范围变化
const handleTimeRangeChange = () => {
  console.log('时间范围变化:', selectedTimeRange.value);
  
  if (selectedTimeRange.value !== 'custom') {
    // 如果不是自定义时间范围，重新加载数据
    loadReportData();
  }
};

// 处理自定义时间范围变化
const handleCustomTimeRangeChange = () => {
  console.log('自定义时间范围变化:', customTimeRange.value);
  loadReportData();
};

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
.report-view {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .title-section {
      display: flex;
      align-items: center;
      gap: 10px;
      
      h1 {
        margin: 0;
      }
    }
    
    .actions {
      display: flex;
      gap: 10px;
    }
  }
  
  .report-description {
    margin-bottom: 20px;
  }
  
  .time-selector {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    
    span {
      margin-right: 10px;
    }
  }
  
  .report-content {
    .chart-card, .table-card {
      .chart-container {
        height: 500px;
      }
    }
  }
}
</style> 