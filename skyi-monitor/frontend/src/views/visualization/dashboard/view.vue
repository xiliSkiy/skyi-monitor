<template>
  <div class="dashboard-view-container">
    <div class="dashboard-header">
      <div class="dashboard-info">
        <h1 class="dashboard-title">{{ dashboard.title }}</h1>
        <p v-if="dashboard.description" class="dashboard-description">{{ dashboard.description }}</p>
      </div>
      <div class="dashboard-actions">
        <el-dropdown trigger="click" @command="handleTimeRangeCommand">
          <el-button>
            <span>{{ timeRangeLabel }}</span>
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="item in timeRangeOptions" :key="item.value" :command="item.value">
                {{ item.label }}
              </el-dropdown-item>
              <el-dropdown-item divided command="custom">自定义时间范围</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <el-tooltip content="自动刷新" placement="top" :disabled="autoRefresh">
          <el-tooltip content="停止刷新" placement="top" :disabled="!autoRefresh">
            <el-button :class="{ active: autoRefresh }" @click="toggleAutoRefresh">
              <el-icon><refresh /></el-icon>
            </el-button>
          </el-tooltip>
        </el-tooltip>
        
        <el-button type="primary" @click="handleEdit">
          <el-icon><edit /></el-icon>
          编辑仪表盘
        </el-button>
      </div>
    </div>
    
    <div v-loading="loading" class="dashboard-content">
      <template v-if="components.length > 0">
        <dashboard-grid-layout
          :layout="gridLayout"
          :col-num="24"
          :row-height="30"
          :margin="[10, 10]"
          :is-draggable="false"
          :is-resizable="false"
        >
          <template v-for="item in components" :key="item.id">
            <dashboard-grid-item :x="getPositionX(item)" :y="getPositionY(item)" 
                    :w="getPositionW(item)" :h="getPositionH(item)" :i="item.id">
              <component-container
                :component="item"
                :start-time="startTime"
                :end-time="endTime"
                :auto-refresh="autoRefresh && item.refreshInterval > 0"
                @refresh-data="handleComponentRefresh"
              />
            </dashboard-grid-item>
          </template>
        </dashboard-grid-layout>
      </template>
      <el-empty v-else description="暂无组件，请添加组件"></el-empty>
    </div>

    <!-- 自定义时间范围选择器对话框 -->
    <el-dialog v-model="timeRangeDialogVisible" title="选择时间范围" width="450px">
      <el-form :model="customTimeRange" label-width="80px">
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="customTimeRange.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="customTimeRange.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="timeRangeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="applyCustomTimeRange">
            应用
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Refresh, Edit } from '@element-plus/icons-vue'
import { getDashboardDetail, getComponentList } from '@/api/visualization'
import DashboardGridLayout from '@/components/visualization/DashboardGridLayout.vue'
import DashboardGridItem from '@/components/visualization/DashboardGridItem.vue'
import ComponentContainer from '@/components/visualization/ComponentContainer.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const dashboard = ref<any>({})
const components = ref<any[]>([])
const autoRefresh = ref(false)
const refreshTimers = reactive<Record<string, number>>({})
const timeRangeDialogVisible = ref(false)

// 时间范围选项
const timeRangeOptions = [
  { label: '最近15分钟', value: '15m' },
  { label: '最近30分钟', value: '30m' },
  { label: '最近1小时', value: '1h' },
  { label: '最近3小时', value: '3h' },
  { label: '最近6小时', value: '6h' },
  { label: '最近12小时', value: '12h' },
  { label: '最近24小时', value: '1d' },
  { label: '最近2天', value: '2d' },
  { label: '最近7天', value: '7d' },
  { label: '最近30天', value: '30d' }
]

// 当前选择的时间范围
const selectedTimeRange = ref('3h')
const customTimeRange = reactive({
  startTime: new Date(Date.now() - 3 * 60 * 60 * 1000),
  endTime: new Date()
})

// 计算实际的开始和结束时间
const startTime = computed(() => {
  return customTimeRange.startTime
})

const endTime = computed(() => {
  return customTimeRange.endTime
})

// 当前时间范围的显示标签
const timeRangeLabel = computed(() => {
  const option = timeRangeOptions.find(item => item.value === selectedTimeRange.value)
  if (option) {
    return option.label
  }
  return '自定义时间范围'
})

// 网格布局配置
const gridLayout = computed(() => {
  if (dashboard.value.layout) {
    try {
      return JSON.parse(dashboard.value.layout)
    } catch (e) {
      console.error('布局配置解析失败', e)
    }
  }
  return []
})

// 获取仪表盘详情
const getDashboardInfo = async () => {
  loading.value = true
  try {
    const res = await getDashboardDetail(route.params.id as string)
    dashboard.value = res.data
    
    // 获取组件列表
    await getComponentsInfo()
    
    // 如果仪表盘配置了自动刷新，则启用自动刷新
    if (dashboard.value.refreshInterval > 0) {
      autoRefresh.value = true
      setupAutoRefresh()
    }
  } catch (error) {
    console.error('获取仪表盘详情失败', error)
    ElMessage.error('获取仪表盘详情失败')
  } finally {
    loading.value = false
  }
}

// 获取组件列表
const getComponentsInfo = async () => {
  try {
    const res = await getComponentList(route.params.id as string)
    components.value = res.data
  } catch (error) {
    console.error('获取组件列表失败', error)
    ElMessage.error('获取组件列表失败')
  }
}

// 处理时间范围选择
const handleTimeRangeCommand = (command: string) => {
  if (command === 'custom') {
    timeRangeDialogVisible.value = true
    return
  }
  
  selectedTimeRange.value = command
  updateTimeRange(command)
}

// 更新时间范围
const updateTimeRange = (range: string) => {
  const now = new Date()
  let startTime: Date
  
  switch (range) {
    case '15m': startTime = new Date(now.getTime() - 15 * 60 * 1000); break
    case '30m': startTime = new Date(now.getTime() - 30 * 60 * 1000); break
    case '1h': startTime = new Date(now.getTime() - 60 * 60 * 1000); break
    case '3h': startTime = new Date(now.getTime() - 3 * 60 * 60 * 1000); break
    case '6h': startTime = new Date(now.getTime() - 6 * 60 * 60 * 1000); break
    case '12h': startTime = new Date(now.getTime() - 12 * 60 * 60 * 1000); break
    case '1d': startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000); break
    case '2d': startTime = new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000); break
    case '7d': startTime = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000); break
    case '30d': startTime = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000); break
    default: return
  }
  
  customTimeRange.startTime = startTime
  customTimeRange.endTime = now
}

// 应用自定义时间范围
const applyCustomTimeRange = () => {
  if (customTimeRange.startTime >= customTimeRange.endTime) {
    ElMessage.error('开始时间必须早于结束时间')
    return
  }
  
  // 将选择器设置为自定义
  selectedTimeRange.value = 'custom'
  timeRangeDialogVisible.value = false
}

// 切换自动刷新状态
const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  
  if (autoRefresh.value) {
    setupAutoRefresh()
  } else {
    clearAutoRefresh()
  }
}

// 设置自动刷新
const setupAutoRefresh = () => {
  // 清除现有的定时器
  clearAutoRefresh()
  
  // 设置仪表盘整体刷新定时器
  if (dashboard.value.refreshInterval > 0) {
    refreshTimers['dashboard'] = window.setInterval(() => {
      // 仪表盘整体刷新时，更新时间范围和重新获取数据
      if (selectedTimeRange.value !== 'custom') {
        updateTimeRange(selectedTimeRange.value)
      } else {
        // 自定义时间范围情况下，只更新结束时间为当前时间
        customTimeRange.endTime = new Date()
      }
    }, dashboard.value.refreshInterval * 1000)
  }
  
  // 设置各组件的刷新定时器
  components.value.forEach(component => {
    if (component.refreshInterval > 0 && component.realtime) {
      refreshTimers[component.id] = window.setInterval(() => {
        // 触发组件刷新
        handleComponentRefresh(component.id)
      }, component.refreshInterval * 1000)
    }
  })
}

// 清除自动刷新定时器
const clearAutoRefresh = () => {
  Object.values(refreshTimers).forEach(timerId => {
    window.clearInterval(timerId)
  })
  
  // 清空定时器对象
  Object.keys(refreshTimers).forEach(key => {
    delete refreshTimers[key]
  })
}

// 处理组件刷新
const handleComponentRefresh = (componentId: string) => {
  console.log(`刷新组件: ${componentId}`)
  // 此处可以添加其他组件刷新逻辑，如触发组件内的数据更新
}

// 编辑仪表盘
const handleEdit = () => {
  router.push(`/visualization/dashboards/edit/${route.params.id}`)
}

// 获取组件位置信息
const getPositionX = (item: any) => {
  try {
    const position = JSON.parse(item.position)
    return position.x || 0
  } catch {
    return 0
  }
}

const getPositionY = (item: any) => {
  try {
    const position = JSON.parse(item.position)
    return position.y || 0
  } catch {
    return 0
  }
}

const getPositionW = (item: any) => {
  try {
    const position = JSON.parse(item.position)
    return position.w || 6
  } catch {
    return 6
  }
}

const getPositionH = (item: any) => {
  try {
    const position = JSON.parse(item.position)
    return position.h || 6
  } catch {
    return 6
  }
}

onMounted(() => {
  getDashboardInfo()
  // 初始设置时间范围
  updateTimeRange(selectedTimeRange.value)
})

onBeforeUnmount(() => {
  // 组件卸载前清除所有定时器
  clearAutoRefresh()
})
</script>

<style lang="scss" scoped>
.dashboard-view-container {
  height: 100%;
  overflow: auto;
  background-color: #f5f7fa;
  
  .dashboard-header {
    background-color: #fff;
    padding: 16px 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .dashboard-info {
      .dashboard-title {
        font-size: 20px;
        font-weight: 500;
        margin: 0;
        color: #303133;
      }
      
      .dashboard-description {
        margin: 8px 0 0;
        font-size: 14px;
        color: #606266;
      }
    }
    
    .dashboard-actions {
      display: flex;
      gap: 8px;
      
      .el-button.active {
        background-color: #ecf5ff;
        color: #409eff;
      }
    }
  }
  
  .dashboard-content {
    padding: 20px;
    min-height: calc(100vh - 120px);
  }
}
</style> 