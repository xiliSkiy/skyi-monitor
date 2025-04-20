<template>
  <div class="alert-stats-component">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>告警统计</span>
          <el-button type="text" @click="refresh">刷新</el-button>
        </div>
      </template>
      <div class="alert-stats" v-loading="loading">
        <div class="stats-summary">
          <div class="total-container">
            <div class="total-label">总告警</div>
            <div class="total-value">{{ totalAlerts }}</div>
          </div>
          <div class="stats-items">
            <div class="stat-item critical" @click="navigateToAlert('CRITICAL')">
              <div class="count">{{ stats.critical || 0 }}</div>
              <div class="label">严重</div>
            </div>
            <div class="stat-item major" @click="navigateToAlert('MAJOR')">
              <div class="count">{{ stats.major || 0 }}</div>
              <div class="label">重要</div>
            </div>
            <div class="stat-item minor" @click="navigateToAlert('MINOR')">
              <div class="count">{{ stats.minor || 0 }}</div>
              <div class="label">次要</div>
            </div>
            <div class="stat-item warning" @click="navigateToAlert('WARNING')">
              <div class="count">{{ stats.warning || 0 }}</div>
              <div class="label">警告</div>
            </div>
            <div class="stat-item info" @click="navigateToAlert('INFO')">
              <div class="count">{{ stats.info || 0 }}</div>
              <div class="label">信息</div>
            </div>
          </div>
        </div>
        <div class="chart-container" v-if="showChart">
          <div ref="chartRef" class="chart"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getAlertStatsBySeverity } from '@/api/alert';
import * as echarts from 'echarts/core';
import { PieChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer,
  LabelLayout
]);

const props = defineProps({
  showChart: {
    type: Boolean,
    default: true
  }
});

const router = useRouter();
const loading = ref(false);
const chartRef = ref<HTMLElement | null>(null);
let chart: echarts.ECharts | null = null;

// 统计数据
const stats = reactive({
  critical: 0,
  major: 0,
  minor: 0,
  warning: 0,
  info: 0
});

// 计算总告警数
const totalAlerts = computed(() => {
  return stats.critical + stats.major + stats.minor + stats.warning + stats.info;
});

// 获取告警统计数据
const fetchAlertStats = async () => {
  loading.value = true;
  try {
    const response = await getAlertStatsBySeverity();
    if (response.code === 200) {
      // 转换后端返回的数据格式
      const data = response.data || {};
      stats.critical = data.CRITICAL || 0;
      stats.major = data.MAJOR || 0;
      stats.minor = data.MINOR || 0;
      stats.warning = data.WARNING || 0;
      stats.info = data.INFO || 0;
      
      if (props.showChart) {
        nextTick(() => {
          initChart();
        });
      }
    } else {
      ElMessage.error(response.message || '获取告警统计失败');
    }
  } catch (error) {
    console.error('获取告警统计出错', error);
  } finally {
    loading.value = false;
  }
};

// 刷新数据
const refresh = () => {
  fetchAlertStats();
};

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;

  // 如果图表已经存在，销毁它
  if (chart) {
    chart.dispose();
  }

  // 创建新图表
  chart = echarts.init(chartRef.value);
  
  // 准备数据
  const chartData = [
    { value: stats.critical, name: '严重', itemStyle: { color: '#F56C6C' } },
    { value: stats.major, name: '重要', itemStyle: { color: '#E6A23C' } },
    { value: stats.minor, name: '次要', itemStyle: { color: '#E8CB45' } },
    { value: stats.warning, name: '警告', itemStyle: { color: '#F2C037' } },
    { value: stats.info, name: '信息', itemStyle: { color: '#909399' } }
  ].filter(item => item.value > 0);  // 只显示有数据的类别

  // 设置图表选项
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      data: chartData.map(item => item.name)
    },
    series: [
      {
        name: '告警级别',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: chartData
      }
    ]
  };
  
  // 设置图表选项并渲染
  chart.setOption(option);
};

// 跳转到告警列表页面并带上过滤条件
const navigateToAlert = (severity: string) => {
  router.push({
    path: '/alert/list',
    query: { severity }
  });
};

// 组件挂载时获取数据
onMounted(() => {
  fetchAlertStats();
  
  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    if (chart) {
      chart.resize();
    }
  });
});
</script>

<style lang="scss" scoped>
.alert-stats-component {
  height: 100%;
  
  .el-card {
    height: 100%;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-stats {
  display: flex;
  flex-direction: column;
  height: calc(100% - 30px);
}

.stats-summary {
  display: flex;
  flex-direction: column;
  margin-bottom: 15px;
}

.total-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 15px;
  
  .total-label {
    font-size: 16px;
    margin-right: 10px;
  }
  
  .total-value {
    font-size: 24px;
    font-weight: bold;
  }
}

.stats-items {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s;
  min-width: 60px;
  
  &:hover {
    transform: scale(1.05);
  }
  
  .count {
    font-size: 18px;
    font-weight: bold;
  }
  
  .label {
    font-size: 12px;
    margin-top: 4px;
  }
  
  &.critical {
    color: #F56C6C;
    background-color: rgba(245, 108, 108, 0.1);
  }
  
  &.major {
    color: #E6A23C;
    background-color: rgba(230, 162, 60, 0.1);
  }
  
  &.minor {
    color: #E8CB45;
    background-color: rgba(232, 203, 69, 0.1);
  }
  
  &.warning {
    color: #F2C037;
    background-color: rgba(242, 192, 55, 0.1);
  }
  
  &.info {
    color: #909399;
    background-color: rgba(144, 147, 153, 0.1);
  }
}

.chart-container {
  flex: 1;
  min-height: 200px;
  
  .chart {
    width: 100%;
    height: 100%;
  }
}
</style> 