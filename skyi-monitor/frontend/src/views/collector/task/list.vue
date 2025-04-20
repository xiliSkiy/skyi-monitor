<template>
  <div class="collector-task-list">
    <div class="page-header">
      <h1>采集任务列表</h1>
      <el-button type="primary" @click="createTask">创建采集任务</el-button>
    </div>
    
    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="任务名称">
          <el-input v-model="queryParams.name" placeholder="请输入任务名称" clearable />
        </el-form-item>
        
        <el-form-item label="任务类型">
          <el-select v-model="queryParams.type" placeholder="请选择任务类型" clearable>
            <el-option label="服务器" value="server" />
            <el-option label="数据库" value="database" />
            <el-option label="中间件" value="middleware" />
            <el-option label="应用" value="application" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="协议类型">
          <el-select v-model="queryParams.protocol" placeholder="请选择协议类型" clearable>
            <el-option label="SNMP" value="snmp" />
            <el-option label="SSH" value="ssh" />
            <el-option label="JMX" value="jmx" />
            <el-option label="MySQL" value="mysql" />
            <el-option label="HTTP" value="http" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="searchTasks">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-table :data="taskList" v-loading="loading" border stripe class="task-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="任务名称" min-width="120" />
      <el-table-column prop="code" label="任务编码" min-width="120" />
      <el-table-column label="任务类型" min-width="100">
        <template #default="{ row }">
          {{ formatTaskType(row.type) }}
        </template>
      </el-table-column>
      <el-table-column label="协议类型" min-width="100">
        <template #default="{ row }">
          {{ row.protocol }}
        </template>
      </el-table-column>
      <el-table-column label="采集间隔" min-width="100">
        <template #default="{ row }">
          {{ row.interval }}秒
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="最近执行时间" min-width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.lastExecuteTime) || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="viewDetail(row.id)">查看</el-button>
          <el-button type="warning" size="small" @click="editTask(row.id)">编辑</el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="deleteTask(row.id)"
          >
            删除
          </el-button>
          <el-button 
            :type="row.status === 1 ? 'danger' : 'success'" 
            size="small" 
            @click="toggleTaskStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
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
      class="pagination"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  listCollectorTasks, 
  deleteCollectorTask, 
  startCollectorTask, 
  stopCollectorTask 
} from '@/api/collector'
import type { CollectorTask } from '@/types/collector'

const router = useRouter()
const loading = ref(false)
const taskList = ref<CollectorTask[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 查询参数
const queryParams = reactive({
  name: '',
  type: '',
  protocol: '',
  status: null as number | null,
  page: 0,
  size: 10
})

// 格式化任务类型
const formatTaskType = (type: string) => {
  switch (type) {
    case 'server':
      return '服务器'
    case 'database':
      return '数据库'
    case 'middleware':
      return '中间件'
    case 'application':
      return '应用'
    default:
      return type
  }
}

// 格式化时间
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return null
  return new Date(dateTime).toLocaleString()
}

const fetchTaskList = async () => {
  loading.value = true
  try {
    // 把分页参数添加到查询参数中
    queryParams.page = currentPage.value - 1 // 后端从0开始计数
    queryParams.size = pageSize.value
    
    // 实际的API调用
    const res = await listCollectorTasks(queryParams)
    
    if (res && res.data) {
      taskList.value = res.data.content || []
      total.value = res.data.totalElements || 0
    }
    
    console.log('获取采集任务列表:', res)
  } catch (error) {
    ElMessage.error('获取采集任务列表失败')
    console.error('获取任务列表出错:', error)
  } finally {
    loading.value = false
  }
}

// 搜索任务
const searchTasks = () => {
  currentPage.value = 1
  fetchTaskList()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.name = ''
  queryParams.type = ''
  queryParams.protocol = ''
  queryParams.status = null
  currentPage.value = 1
  fetchTaskList()
}

const createTask = () => {
  router.push('/collector/task/create')
}

const editTask = (id: number) => {
  router.push(`/collector/task/edit/${id}`)
}

const viewDetail = (id: number) => {
  router.push(`/collector/task/detail/${id}`)
}

// 删除任务
const deleteTask = (id: number) => {
  ElMessageBox.confirm('确定要删除该采集任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCollectorTask(id)
      ElMessage.success('删除成功')
      fetchTaskList()
    } catch (error) {
      console.error('删除任务失败:', error)
      ElMessage.error('删除任务失败')
    }
  }).catch(() => {})
}

// 启用/禁用任务状态
const toggleTaskStatus = async (row: CollectorTask) => {
  const isEnable = row.status !== 1
  const message = isEnable ? '启用' : '禁用'
  
  if (!row.id) {
    ElMessage.error('任务ID不存在')
    return
  }
  
  ElMessageBox.confirm(`确定要${message}该采集任务吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      if (isEnable) {
        await startCollectorTask(row.id as number)
      } else {
        await stopCollectorTask(row.id as number)
      }
      ElMessage.success(`${message}成功`)
      fetchTaskList()
    } catch (error) {
      console.error(`${message}任务失败:`, error)
      ElMessage.error(`${message}任务失败`)
    }
  }).catch(() => {})
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchTaskList()
}

const handleCurrentChange = () => {
  fetchTaskList()
}

onMounted(() => {
  fetchTaskList()
})
</script>

<style scoped>
.collector-task-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.task-table {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 