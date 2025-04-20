<template>
  <div class="container" v-loading="loading">
    <div class="header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
      <div class="actions">
        <el-button type="primary" @click="editTask(taskDetail.id)">
          <el-icon><Edit /></el-icon> 编辑
        </el-button>
        <el-button type="success" @click="runTask(taskDetail.id)">
          <el-icon><VideoPlay /></el-icon> 立即执行
        </el-button>
      </div>
    </div>

    <!-- 任务详情卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>{{ taskDetail.name }}</h3>
          <el-tag :type="getStatusType(taskDetail.status)">{{ formatStatus(taskDetail.status) }}</el-tag>
        </div>
      </template>
      
      <el-descriptions border>
        <el-descriptions-item label="任务ID">{{ taskDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="任务编码">{{ taskDetail.code }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ taskDetail.type }}</el-descriptions-item>
        <el-descriptions-item label="协议">{{ taskDetail.protocol }}</el-descriptions-item>
        <el-descriptions-item label="采集间隔">{{ taskDetail.interval }}秒</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(taskDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(taskDetail.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="最后执行时间">{{ formatDateTime(taskDetail.lastExecuteTime) }}</el-descriptions-item>
        <el-descriptions-item label="最后执行状态" v-if="taskDetail.lastExecuteStatus !== null">
          <el-tag :type="getStatusType(taskDetail.lastExecuteStatus)">
            {{ formatExecutionStatus(taskDetail.lastExecuteStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ taskDetail.description || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 连接参数卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>连接参数</h3>
        </div>
      </template>
      
      <el-descriptions border v-if="taskDetail.connectionParams">
        <el-descriptions-item v-for="(value, key) in taskDetail.connectionParams" :key="key" :label="key">
          {{ key === 'password' ? '******' : value }}
        </el-descriptions-item>
      </el-descriptions>
      <div v-else class="no-data">暂无连接参数</div>
    </el-card>
    
    <!-- 采集指标卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>采集指标</h3>
        </div>
      </template>
      
      <el-table :data="taskDetail.metrics || []" border style="width: 100%">
        <el-table-column prop="name" label="指标名称" />
        <el-table-column prop="path" label="采集路径" />
        <el-table-column prop="dataType" label="数据类型" />
        <el-table-column prop="unit" label="单位" />
        <el-table-column prop="enabled" label="是否启用">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 执行历史卡片 -->
    <el-card class="execution-history">
      <template #header>
        <div class="card-header">
          <h3>执行历史</h3>
        </div>
      </template>
      
      <el-table :data="executionHistory" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="开始时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100">
          <template #default="scope">
            {{ scope.row.duration ? `${scope.row.duration}ms` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ formatExecutionStatus(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewExecutionDetail(scope.row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="executionHistory.length === 0" class="no-data">暂无执行历史</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Edit, VideoPlay } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCollectorTaskById, executeTaskNow } from '@/api/collector'
import { listCollectorTaskInstances } from '@/api/collector'

interface ExecutionRecord {
  id: number
  startTime: string
  endTime: string
  duration: number
  status: number
}

interface TaskDetail {
  id: number;
  name: string;
  type: string;
  code: string;
  status: number;
  protocol: string;
  assetId: number;
  interval: number;
  connectionParams: Record<string, string>;
  metrics: {
    name: string;
    path: string;
    dataType: string;
    unit?: string;
    enabled: boolean;
  }[];
  description: string;
  createTime: string;
  updateTime: string;
  lastExecuteTime: string | null;
  lastExecuteStatus: number | null;
}

const route = useRoute()
const router = useRouter()
const loading = ref(false)

// 从路由参数获取任务ID
const taskId = ref(parseInt(route.params.id as string))

// 任务详情
const taskDetail = ref<TaskDetail>({
  id: 0,
  name: '',
  type: '',
  code: '',
  status: 0,
  protocol: '',
  assetId: 0,
  interval: 0,
  connectionParams: {},
  metrics: [],
  description: '',
  createTime: '',
  updateTime: '',
  lastExecuteTime: null,
  lastExecuteStatus: null
})

// 执行历史
const executionHistory = ref<ExecutionRecord[]>([])

// 获取任务详情
const fetchTaskDetails = async () => {
  loading.value = true
  try {
    // 调用实际API获取任务详情
    const response = await getCollectorTaskById(taskId.value)
    console.log('任务详情:', response.data)
    // 使用类型断言确保类型兼容
    taskDetail.value = response.data as unknown as TaskDetail
    
    // 获取任务的执行历史记录
    await fetchExecutionHistory()
  } catch (error) {
    console.error('获取任务详情失败', error)
    ElMessage.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 获取执行历史
const fetchExecutionHistory = async () => {
  try {
    // 调用实际API获取任务的执行历史
    const response = await listCollectorTaskInstances({
      taskId: taskId.value,
      page: 0,
      size: 10
    })
    console.log('执行历史:', response.data)
    executionHistory.value = response.data.content || []
  } catch (error) {
    console.error('获取执行历史失败', error)
    ElMessage.error('获取执行历史失败')
  }
}

// 返回列表页
const goBack = () => {
  router.push('/collector/task')
}

// 编辑任务
const editTask = (id: number) => {
  router.push(`/collector/task/edit/${id}`)
}

// 立即执行任务
const runTask = async (id: number) => {
  try {
    const response = await executeTaskNow(id)
    console.log('执行任务结果:', response)
    ElMessage.success('任务已开始执行')
    // 刷新执行历史
    setTimeout(() => {
      fetchExecutionHistory()
    }, 1000)
  } catch (error) {
    console.error('执行任务失败', error)
    ElMessage.error('执行任务失败')
  }
}

// 查看执行详情
const viewExecutionDetail = (id: number) => {
  router.push(`/collector/schedule/detail/${id}`)
}

// 获取状态对应的标签类型
const getStatusType = (status: number | string) => {
  if (typeof status === 'string') {
    switch (status) {
      case 'active':
      case 'success':
        return 'success'
      case 'inactive':
        return 'info'
      case 'failed':
        return 'danger'
      default:
        return 'warning'
    }
  } else {
    switch (status) {
      case 1:
        return 'success'
      case 0:
        return 'danger'
      case 2:
        return 'warning'
      default:
        return 'info'
    }
  }
}

// 格式化状态文本
const formatStatus = (status: number) => {
  switch (status) {
    case 1:
      return '启用'
    case 0:
      return '禁用'
    default:
      return '未知'
  }
}

// 格式化执行状态文本
const formatExecutionStatus = (status: number) => {
  switch (status) {
    case 1:
      return '成功'
    case 0:
      return '失败'
    case 2:
      return '进行中'
    default:
      return '未知'
  }
}

// 格式化时间
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString()
}

onMounted(() => {
  fetchTaskDetails()
})
</script>

<style scoped>
.container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header .title {
  font-size: 20px;
  font-weight: bold;
}

.header .actions {
  display: flex;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-card, .config-card, .execution-history {
  margin-bottom: 20px;
}

.execution-history {
  margin-bottom: 0;
}

.no-data {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}
</style> 