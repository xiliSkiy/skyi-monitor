<template>
  <div class="collector-task-list">
    <div class="page-header">
      <h1>采集任务列表</h1>
      <el-button type="primary" @click="createTask">创建采集任务</el-button>
    </div>
    
    <el-table :data="taskList" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="任务名称" />
      <el-table-column prop="type" label="任务类型" />
      <el-table-column prop="target" label="采集目标" />
      <el-table-column prop="createdTime" label="创建时间" />
      <el-table-column prop="lastRunTime" label="最近运行时间" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
            {{ row.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="viewDetail(row.id)">查看</el-button>
          <el-button type="warning" size="small" @click="editTask(row.id)">编辑</el-button>
          <el-button type="success" size="small" @click="createSchedule(row.id)">创建计划</el-button>
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
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const taskList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchTaskList = async () => {
  loading.value = true
  try {
    // 这里应该是实际的API调用
    // const res = await api.getTaskList({ page: currentPage.value, pageSize: pageSize.value })
    // taskList.value = res.data.records
    // total.value = res.data.total
    
    // 模拟数据
    taskList.value = [
      {
        id: 1,
        name: 'Linux服务器采集',
        type: 'SSH',
        target: '192.168.1.100',
        createdTime: '2023-10-01 10:00:00',
        lastRunTime: '2023-10-05 08:30:00',
        status: 'active'
      },
      {
        id: 2,
        name: 'Windows服务器采集',
        type: 'WMI',
        target: '192.168.1.101',
        createdTime: '2023-10-02 14:20:00',
        lastRunTime: '2023-10-05 09:15:00',
        status: 'inactive'
      }
    ]
    total.value = 2
  } catch (error) {
    ElMessage.error('获取采集任务列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
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

const createSchedule = (taskId: number) => {
  router.push(`/collector/schedule/create/${taskId}`)
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

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style> 