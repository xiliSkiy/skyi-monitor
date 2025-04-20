<template>
  <div class="asset-topology" ref="chartContainer"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  data: {
    type: Object,
    required: true
  },
  options: {
    type: Object,
    default: () => ({})
  }
});

const chartContainer = ref<HTMLElement | null>(null);
let chart: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  if (!chartContainer.value) return;
  
  // 创建图表实例
  chart = echarts.init(chartContainer.value);
  
  // 设置默认选项
  const defaultOptions = {
    title: {
      text: '资产拓扑关系图',
      top: 'top',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}'
    },
    legend: {
      top: 'bottom',
      data: ['服务器', '数据库', '应用', '中间件']
    },
    series: [
      {
        type: 'graph',
        layout: 'force',
        symbolSize: 45,
        roam: true,
        label: {
          show: true,
          position: 'right'
        },
        force: {
          repulsion: 300,
          edgeLength: 100
        },
        lineStyle: {
          color: '#aaa',
          curveness: 0.1,
          width: 2
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 5
          }
        },
        data: [],
        links: []
      }
    ]
  };
  
  // 合并选项
  const options = {
    ...defaultOptions,
    ...props.options
  };
  
  // 如果有数据，更新图表
  if (props.data && props.data.nodes) {
    options.series[0].data = props.data.nodes;
    options.series[0].links = props.data.links;
  }
  
  // 设置图表选项
  chart.setOption(options);
  
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
  if (!chart || !props.data) return;
  
  chart.setOption({
    series: [
      {
        data: props.data.nodes || [],
        links: props.data.links || []
      }
    ]
  });
};

// 监听数据变化
watch(() => props.data, updateChart, { deep: true });
watch(() => props.options, () => {
  // 当选项变化时，重新初始化图表
  if (chart) {
    chart.dispose();
  }
  initChart();
}, { deep: true });

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
.asset-topology {
  width: 100%;
  height: 500px;
}
</style> 