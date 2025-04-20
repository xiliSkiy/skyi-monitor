<template>
  <div class="component-container">
    <div class="component-header">
      <div class="component-title">{{ component.title }}</div>
      <div class="component-actions">
        <el-tooltip content="刷新" placement="top">
          <el-button type="text" size="small" @click="refreshData">
            <el-icon><refresh /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
    <div class="component-content">
      <component-renderer
        v-if="!loading"
        :component="component"
        :data="chartData"
        :start-time="startTime"
        :end-time="endTime"
      />
      <div v-else class="loading-container">
        <el-skeleton animated :rows="5" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, toRefs } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { queryMetricData } from '@/api/visualization'
import ComponentRenderer from './ComponentRenderer.vue'

const props = defineProps({
  component: {
    type: Object,
    required: true
  },
  startTime: {
    type: Date,
    required: true
  },
  endTime: {
    type: Date,
    required: true
  },
  autoRefresh: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['refresh-data'])

const { component, startTime, endTime, autoRefresh } = toRefs(props)

const loading = ref(false)
const chartData = ref<any>(null)

// 定义数据源类型接口
interface DataSource {
  metricName?: string;
  tags?: Record<string, string>;
  aggregateFunction?: string;
  interval?: string;
  [key: string]: any;
}

// 加载组件数据
const loadData = async () => {
  loading.value = true
  try {
    let dataSource: DataSource = {}
    
    // 尝试解析数据源配置
    if (component.value.dataSource) {
      try {
        dataSource = JSON.parse(component.value.dataSource) as DataSource
      } catch (error) {
        console.error('解析数据源配置失败', error)
        ElMessage.error('数据源配置格式错误')
        return
      }
    }
    
    // 构建查询参数
    const queryParams = {
      name: dataSource.metricName,
      tags: dataSource.tags || {},
      startTime: startTime.value,
      endTime: endTime.value,
      aggregateFunction: dataSource.aggregateFunction || 'avg',
      interval: dataSource.interval || '1m'
    }
    
    // 查询数据
    const response = await queryMetricData(queryParams)
    chartData.value = response.data
  } catch (error) {
    console.error('加载组件数据失败', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  loadData()
  emit('refresh-data', component.value.id)
}

// 监听时间范围变化
watch([startTime, endTime], () => {
  loadData()
}, { deep: true })

// 监听自动刷新状态
watch(autoRefresh, (newVal) => {
  if (newVal) {
    loadData()
  }
})

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.component-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .component-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 0 10px 0;
    border-bottom: 1px solid #ebeef5;
    
    .component-title {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
    
    .component-actions {
      display: flex;
      gap: 5px;
    }
  }
  
  .component-content {
    flex: 1;
    position: relative;
    overflow: hidden;
    padding-top: 10px;
    
    .loading-container {
      padding: 20px;
    }
  }
}
</style> 