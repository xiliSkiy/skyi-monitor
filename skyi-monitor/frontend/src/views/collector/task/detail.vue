<template>
  <div class="collector-task-detail">
    <div class="page-header">
      <h1>采集任务详情</h1>
      <div class="action-buttons">
        <el-button type="primary" @click="goBack">返回列表</el-button>
        <el-button type="warning" @click="editTask">编辑任务</el-button>
        <el-button type="success" @click="runTask">立即执行</el-button>
      </div>
    </div>

    <el-card v-loading="loading" class="task-info">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-tag :type="task.status === 'active' ? 'success' : 'danger'">
            {{ task.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务名称">{{ task.name }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ task.type }}</el-descriptions-item>
        <el-descriptions-item label="采集目标">{{ task.target }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ task.createdTime }}</el-descriptions-item>
        <el-descriptions-item label="最近运行时间">{{ task.lastRunTime }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ task.createdBy }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="configuration-info">
      <template #header>
        <div class="card-header">
          <span>配置信息</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item v-for="(value, key) in task.configuration" :key="key" :label="key">
          {{ value }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="execution-history">
      <template #header>
        <div class="card-header">
          <span>执行历史</span>
        </div>
      </template>
      <el-table :data="executionHistory" border stripe>
        <el-table-column prop="id" label="执行ID" width="80" />
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="endTime" label="结束时间" />
        <el-table-column prop="duration" label="持续时间(秒)" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewExecutionDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const taskId = ref(Number(route.params.id))

// 任务详情数据
const task = ref({
  id: 0,
  name: '',
  type: '',
  target: '',
  createdTime: '',
  lastRunTime: '',
  status: '',
  createdBy: '',
  configuration: {}
})

// 执行历史数据
const executionHistory = ref([])

// 获取任务详情
const fetchTaskDetail = async () => {
  loading.value = true
  try {
    // 这里应该是实际的API调用
    // const res = await api.getTaskDetail(taskId.value)
    // task.value = res.data
    
    // 模拟数据
    task.value = {
      id: taskId.value,
      name: 'Linux服务器采集',
      type: 'SSH',
      target: '192.168.1.100',
      createdTime: '2023-10-01 10:00:00',
      lastRunTime: '2023-10-05 08:30:00',
      status: 'active',
      createdBy: '管理员',
      configuration: {
        '用户名': 'admin',
        '端口': '22',
        '采集间隔': '60秒',
        '超时时间': '30秒',
        '采集指标': 'CPU使用率,内存使用率,磁盘使用率'
      }
    }
    
    // 模拟执行历史数据
    executionHistory.value = [
      {
        id: 1001,
        startTime: '2023-10-05 08:30:00',
        endTime: '2023-10-05 08:30:45',
        duration: 45,
        status: 'success'
      },
      {
        id: 1000,
        startTime: '2023-10-04 08:30:00',
        endTime: '2023-10-04 08:30:50',
        duration: 50,
        status: 'failed'
      }
    ]
  } catch (error) {
    ElMessage.error('获取任务详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取状态对应的类型
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'success': 'success',
    'failed': 'danger',
    'running': 'primary',
    'pending': 'info'
  }
  return statusMap[status] || 'info'
}

// 编辑任务
const editTask = () => {
  router.push(`/collector/task/edit/${taskId.value}`)
}

// 立即执行任务
const runTask = async () => {
  try {
    // 这里应该是实际的API调用
    // await api.runTask(taskId.value)
    ElMessage.success('任务已开始执行')
  } catch (error) {
    ElMessage.error('执行任务失败')
    console.error(error)
  }
}

// 查看执行详情
const viewExecutionDetail = (executionId: number) => {
  router.push(`/collector/task/execution/${executionId}`)
}

// 返回列表
const goBack = () => {
  router.push('/collector/task/list')
}

onMounted(() => {
  fetchTaskDetail()
})
</script>

<style scoped>
.collector-task-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-info,
.configuration-info,
.execution-history {
  margin-bottom: 20px;
}

.execution-history .el-table {
  margin-top: 10px;
}
</style> 