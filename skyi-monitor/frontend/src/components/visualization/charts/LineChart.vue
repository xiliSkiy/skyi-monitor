<template>
  <div class="chart-container" ref="chartRef"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch, toRefs } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart as EChartsLineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { formatDateTime } from '@/utils/formatter'

// 注册必要的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent,
  LegendComponent,
  EChartsLineChart,
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
  const seriesData = processData()
  
  // 图表配置
  const option = {
    title: {
      text: config.value.title || '',
      subtext: config.value.subtitle || '',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params: any[]) {
        if (params.length === 0) return ''
        
        const time = formatDateTime(params[0].axisValue)
        let html = `<div>${time}</div>`
        
        params.forEach(param => {
          html += `<div style="margin-top:5px;">
            <span style="display:inline-block;margin-right:5px;border-radius:50%;width:10px;height:10px;background-color:${param.color};"></span>
            ${param.seriesName}: ${param.value[1]}
          </div>`
        })
        
        return html
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: config.value.title ? '60' : '30',
      containLabel: true
    },
    xAxis: {
      type: 'time',
      boundaryGap: false,
      axisLabel: {
        formatter: function(value: number) {
          return formatDateTime(value, 'HH:mm:ss')
        }
      }
    },
    yAxis: {
      type: 'value',
      name: config.value.yAxisName || '',
      axisLabel: {
        formatter: config.value.yAxisFormatter || '{value}'
      }
    },
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        show: true,
        type: 'slider',
        top: '90%',
        start: 0,
        end: 100
      }
    ],
    series: seriesData
  }
  
  // 设置配置项
  chartInstance.setOption(option)
}

// 处理数据
const processData = () => {
  if (!data.value) return []
  
  const result = []
  
  // 处理数据点
  if (data.value.dataPoints && Array.isArray(data.value.dataPoints)) {
    const points = data.value.dataPoints.map((point: any) => {
      return [new Date(point.timestamp).getTime(), point.value]
    })
    
    result.push({
      name: data.value.name || '指标值',
      type: 'line',
      smooth: config.value.smooth !== false,
      symbol: config.value.showSymbol ? 'circle' : 'none',
      symbolSize: 4,
      sampling: 'average',
      itemStyle: {
        color: config.value.color || '#5470c6'
      },
      areaStyle: config.value.showArea ? {
        opacity: 0.3
      } : undefined,
      data: points
    })
  }
  
  return result
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