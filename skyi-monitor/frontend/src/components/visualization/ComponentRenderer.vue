<template>
  <div class="component-renderer">
    <!-- 根据组件类型渲染不同的图表组件 -->
    <component :is="currentComponent" 
      v-if="currentComponent" 
      :data="data" 
      :config="config"
      :style="styles"
    />
    <div v-else class="no-renderer">
      <el-empty description="暂不支持的组件类型"></el-empty>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
// 使用完整路径导入组件，避免命名冲突
import VisualizationLineChart from '@/components/visualization/charts/LineChart.vue'
import VisualizationBarChart from '@/components/visualization/charts/BarChart.vue'
import VisualizationPieChart from '@/components/visualization/charts/PieChart.vue'
import GaugeChart from './charts/GaugeChart.vue'
import TableView from './charts/TableView.vue'
import ValueDisplay from './charts/ValueDisplay.vue'
import TextView from './charts/TextView.vue'
import HeatMapChart from './charts/HeatMapChart.vue'
import ScatterChart from './charts/ScatterChart.vue'
import RadarChart from './charts/RadarChart.vue'

// 组件类型映射
const componentMap = {
  LINE_CHART: VisualizationLineChart,
  BAR_CHART: VisualizationBarChart,
  PIE_CHART: VisualizationPieChart,
  GAUGE: GaugeChart,
  TABLE: TableView,
  VALUE: ValueDisplay,
  TEXT: TextView,
  HEAT_MAP: HeatMapChart,
  SCATTER_CHART: ScatterChart,
  RADAR_CHART: RadarChart
}

const props = defineProps({
  component: {
    type: Object,
    required: true
  },
  data: {
    type: Object,
    default: () => ({})
  },
  startTime: {
    type: Date,
    required: true
  },
  endTime: {
    type: Date,
    required: true
  }
})

// 根据组件类型选择要渲染的组件
const currentComponent = computed(() => {
  if (!props.component.type || !componentMap[props.component.type as keyof typeof componentMap]) {
    return null
  }
  return componentMap[props.component.type as keyof typeof componentMap]
})

// 解析组件配置
const config = computed(() => {
  if (!props.component.dataSource) {
    return {}
  }
  
  try {
    return JSON.parse(props.component.dataSource)
  } catch (error) {
    console.error('解析数据源配置失败', error)
    return {}
  }
})

// 解析样式配置
const styles = computed(() => {
  if (!props.component.styles) {
    return {}
  }
  
  try {
    return JSON.parse(props.component.styles)
  } catch (error) {
    console.error('解析样式配置失败', error)
    return {}
  }
})
</script>

<style lang="scss" scoped>
.component-renderer {
  height: 100%;
  width: 100%;
  
  .no-renderer {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style> 