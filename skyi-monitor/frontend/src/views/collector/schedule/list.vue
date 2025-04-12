<template>
  <div class="collector-schedule-list">
    <div class="page-header">
      <h1>采集调度列表</h1>
      <el-button type="primary" @click="createSchedule">创建调度</el-button>
    </div>
    
    <el-table :data="scheduleList" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="调度名称" />
      <el-table-column prop="taskName" label="关联任务" />
      <el-table-column label="调度类型" width="120">
        <template #default="{ row }">
          <el-tag :type="getScheduleTypeTag(row.scheduleType)">
            {{ getScheduleTypeName(row.scheduleType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="执行规则" min-width="150">
        <template #default="{ row }">
          <span v-if="row.scheduleType === 1">每 {{ row.fixedRate }} 秒</span>
          <span v-else-if="row.scheduleType === 2">{{ row.cronExpression }}</span>
          <span v-else-if="row.scheduleType === 3">{{ formatDate(row.executeTime) }}</span>
          <span v-else>--</span>
        </template>
      </el-table-column>
      <el-table-column label="下次执行时间">
        <template #default="{ row }">
          {{ formatDate(row.nextExecuteTime) || '--' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'danger'">
            {{ row.enabled === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="editSchedule(row.id)">编辑</el-button>
          <el-button 
            :type="row.enabled === 1 ? 'danger' : 'success'" 
            size="small" 
            @click="toggleEnabled(row)"
          >
            {{ row.enabled === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button 
            type="warning" 
            size="small" 
            @click="executeNow(row.id)"
            :disabled="row.enabled !== 1"
          >
            立即执行
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const loading = ref(false)
const scheduleList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取调度类型标签类型
const getScheduleTypeTag = (type: number) => {
  const typeMap = {
    1: 'primary',  // 固定频率
    2: 'success',  // Cron表达式
    3: 'warning'   // 一次性
  }
  return typeMap[type] || 'info'
}

// 获取调度类型名称
const getScheduleTypeName = (type: number) => {
  const typeMap = {
    1: '固定频率',
    2: 'Cron表达式',
    3: '一次性'
  }
  return typeMap[type] || '未知'
}

// 格式化日期
const formatDate = (date: string | null | undefined) => {
  if (!date) return ''
  return formatDateTime(new Date(date))
}

// 获取调度列表
const fetchScheduleList = async () => {
  loading.value = true
  try {
    // 这里应该是实际的API调用
    // const res = await api.listSchedules({ 
    //   page: currentPage.value, 
    //   size: pageSize.value 
    // })
    // scheduleList.value = res.data.records
    // total.value = res.data.total
    
    // 模拟数据
    scheduleList.value = [
      {
        id: 1,
        name: '每分钟采集',
        taskId: 1,
        taskName: 'Linux服务器采集',
        scheduleType: 1,
        fixedRate: 60,
        enabled: 1,
        nextExecuteTime: new Date(Date.now() + 30000).toISOString()
      },
      {
        id: 2,
        name: '每天凌晨采集',
        taskId: 2,
        taskName: 'Windows服务器采集',
        scheduleType: 2,
        cronExpression: '0 0 0 * * ?',
        enabled: 1,
        nextExecuteTime: new Date(new Date().setHours(24, 0, 0, 0)).toISOString()
      },
      {
        id: 3,
        name: '一次性采集',
        taskId: 1,
        taskName: 'Linux服务器采集',
        scheduleType: 3,
        executeTime: new Date(Date.now() + 3600000).toISOString(),
        enabled: 0,
        nextExecuteTime: new Date(Date.now() + 3600000).toISOString()
      }
    ]
    total.value = 3
  } catch (error) {
    ElMessage.error('获取调度列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 创建调度
const createSchedule = () => {
  router.push('/collector/schedule/create')
}

// 编辑调度
const editSchedule = (id: number) => {
  router.push(`/collector/schedule/edit/${id}`)
}

// 启用/禁用调度
const toggleEnabled = async (row: any) => {
  try {
    const newStatus = row.enabled === 1 ? 0 : 1
    const actionText = newStatus === 1 ? '启用' : '禁用'
    
    // 确认对话框
    await ElMessageBox.confirm(
      `确定要${actionText}此调度配置吗？`,
      `${actionText}调度`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里应该是实际的API调用
    // if (newStatus === 1) {
    //   await api.enableSchedule(row.id)
    // } else {
    //   await api.disableSchedule(row.id)
    // }
    
    // 模拟成功
    row.enabled = newStatus
    ElMessage.success(`${actionText}成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error(error)
    }
  }
}

// 立即执行
const executeNow = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要立即执行此调度吗？',
      '立即执行',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里应该是实际的API调用
    // await api.executeScheduleNow(id)
    
    // 模拟成功
    ElMessage.success('执行任务已提交')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('执行失败')
      console.error(error)
    }
  }
}

// 处理每页大小变化
const handleSizeChange = () => {
  currentPage.value = 1
  fetchScheduleList()
}

// 处理页码变化
const handleCurrentChange = () => {
  fetchScheduleList()
}

onMounted(() => {
  fetchScheduleList()
})
</script>

<style scoped>
.collector-schedule-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style> 