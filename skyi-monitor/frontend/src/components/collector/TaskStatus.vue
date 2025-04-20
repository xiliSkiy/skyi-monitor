<template>
  <div class="task-status">
    <div class="status-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="status-card">
            <div class="status-card-content">
              <div class="status-value">{{ stats.total || 0 }}</div>
              <div class="status-label">总任务数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="status-card status-running">
            <div class="status-card-content">
              <div class="status-value">{{ stats.running || 0 }}</div>
              <div class="status-label">运行中</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="status-card status-completed">
            <div class="status-card-content">
              <div class="status-value">{{ stats.completed || 0 }}</div>
              <div class="status-label">已完成</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="status-card status-failed">
            <div class="status-card-content">
              <div class="status-value">{{ stats.failed || 0 }}</div>
              <div class="status-label">失败</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="status-chart" ref="chartRef"></div>
    
    <div class="status-metrics">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="metric-card">
            <template #header>
              <div class="card-header">
                <span>采集指标数</span>
              </div>
            </template>
            <div class="metric-value">{{ stats.metricsCollected || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="metric-card">
            <template #header>
              <div class="card-header">
                <span>今日数据点</span>
              </div>
            </template>
            <div class="metric-value">{{ formatNumber(stats.dataPointsToday || 0) }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="metric-card">
            <template #header>
              <div class="card-header">
                <span>成功率</span>
              </div>
            </template>
            <div class="metric-value">{{ calculateSuccessRate() }}%</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, defineProps, watch } from 'vue';
import * as echarts from 'echarts';

interface StatusStats {
  total: number;
  running: number;
  pending: number;
  completed: number;
  failed: number;
  cancelled: number;
  metricsCollected: number;
  dataPointsToday: number;
  history?: Array<{
    date: string;
    successful: number;
    failed: number;
  }>;
}

const props = defineProps({
  stats: {
    type: Object as () => StatusStats,
    default: () => ({
      total: 0,
      running: 0,
      pending: 0,
      completed: 0,
      failed: 0,
      cancelled: 0,
      metricsCollected: 0,
      dataPointsToday: 0,
      history: []
    })
  }
});

const chartRef = ref<HTMLElement | null>(null);
let chart: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  // 创建图表实例
  chart = echarts.init(chartRef.value);
  
  // 更新图表数据
  updateChart();
  
  // 窗口大小变化时，重新调整图表大小
  window.addEventListener('resize', resizeChart);
};

// 调整图表大小
const resizeChart = () => {
  if (chart) {
    chart.resize();
  }
};

// 更新图表数据
const updateChart = () => {
  if (!chart) return;
  
  // 准备数据
  const history = props.stats.history || [];
  const dates = history.map(item => item.date);
  const successData = history.map(item => item.successful);
  const failedData = history.map(item => item.failed);
  
  // 图表配置
  const option = {
    title: {
      text: '任务执行历史'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['成功', '失败']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '成功',
        type: 'bar',
        stack: 'total',
        itemStyle: {
          color: '#67C23A'
        },
        data: successData
      },
      {
        name: '失败',
        type: 'bar',
        stack: 'total',
        itemStyle: {
          color: '#F56C6C'
        },
        data: failedData
      }
    ]
  };
  
  // 设置图表选项
  chart.setOption(option);
};

// 监听数据变化
watch(() => props.stats, updateChart, { deep: true });

// 计算成功率
const calculateSuccessRate = (): number => {
  const { completed, failed } = props.stats;
  const total = completed + failed;
  
  if (total === 0) return 100;
  
  return Math.round((completed / total) * 100);
};

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M';
  }
  
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K';
  }
  
  return num.toString();
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
.task-status {
  .status-overview {
    margin-bottom: 20px;
    
    .status-card {
      text-align: center;
      
      .status-card-content {
        padding: 10px 0;
        
        .status-value {
          font-size: 24px;
          font-weight: bold;
        }
        
        .status-label {
          margin-top: 5px;
          color: #909399;
        }
      }
      
      &.status-running {
        .status-value {
          color: #E6A23C;
        }
      }
      
      &.status-completed {
        .status-value {
          color: #67C23A;
        }
      }
      
      &.status-failed {
        .status-value {
          color: #F56C6C;
        }
      }
    }
  }
  
  .status-chart {
    height: 300px;
    margin-bottom: 20px;
  }
  
  .status-metrics {
    .metric-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .metric-value {
        font-size: 24px;
        font-weight: bold;
        text-align: center;
        padding: 20px 0;
      }
    }
  }
}
</style> 