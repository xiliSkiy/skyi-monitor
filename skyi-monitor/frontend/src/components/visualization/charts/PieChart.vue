<template>
  <div class="chart-container" ref="chartRef"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch, toRefs } from 'vue'
import * as echarts from 'echarts/core'
import { PieChart as EChartsPieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册必要的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  EChartsPieChart,
  CanvasRenderer
])

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  },
  config: {
    type: Object,
    default: () => ({})
  }
})

const { data, config } = toRefs(props)

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  // 创建图表实例
  chartInstance = echarts.init(chartRef.value)
  
  // 设置图表选项
  updateChart()
  
  // 响应式处理
  const resizeHandler = () => {
    chartInstance?.resize()
  }
  
  window.addEventListener('resize', resizeHandler)
  
  // 组件卸载时移除事件监听
  onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeHandler)
    chartInstance?.dispose()
    chartInstance = null
  })
}

// 更新图表数据
const updateChart = () => {
  if (!chartInstance) return
  
  // 处理数据
  const pieData = processData()
  
  // 图表配置
  const option = {
    title: {
      text: config.value.title || '',
      subtext: config.value.subtitle || '',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: config.value.legendOrient || 'horizontal',
      bottom: config.value.legendPosition === 'bottom' ? 10 : 'auto',
      top: config.value.legendPosition === 'top' ? 10 : 'auto',
      left: (config.value.legendPosition === 'left') ? 10 : 'auto',
      right: (config.value.legendPosition === 'right') ? 10 : 'auto'
    },
    series: [
      {
        name: data.value.name || '数据分布',
        type: 'pie',
        radius: config.value.radius || '50%',
        center: ['50%', '50%'],
        data: pieData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          show: config.value.showLabel !== false,
          formatter: '{b}: {d}%'
        },
        labelLine: {
          show: config.value.showLabelLine !== false
        }
      }
    ]
  }
  
  // 设置配置项
  chartInstance.setOption(option)
}

// 处理数据
const processData = () => {
  if (!data.value || !data.value.categories) return []
  
  // 对于饼图，数据格式为 [{name: '类别1', value: 100}, {name: '类别2', value: 200}]
  return data.value.categories.map((item: any) => {
    return {
      name: item.name,
      value: item.value
    }
  })
}

// 监听数据变化
watch([data, config], () => {
  updateChart()
}, { deep: true })

onMounted(() => {
  initChart()
})
</script>

<style lang="scss" scoped>
.chart-container {
  width: 100%;
  height: 100%;
  min-height: 300px;
}
</style> 