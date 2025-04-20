<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCollectorTaskInstances, getCollectorTaskInstanceById } from '@/api/collector'

const router = useRouter()
const loading = ref(false)

// 查询条件
const queryParams = reactive({
  taskId: null,
  status: null,
  startTime: null,
  endTime: null,
  page: 0,
  size: 10
})

// 实例列表数据
const instanceList = ref([])
const total = ref(0)

// 状态映射
const statusMap: Record<number, string> = {
  0: '失败',
  1: '成功',
  2: '进行中'
}

// 状态标签类型映射
const statusTagType: Record<number, string> = {
  0: 'danger',
  1: 'success',
  2: 'warning'
}

// 当前页码（计算属性）
const currentPage = ref(1)

// 更新当前页码，同步到queryParams.page
const updateQueryPage = () => {
  queryParams.page = currentPage.value - 1
}

// 查询任务实例列表
const fetchInstanceList = async () => {
  loading.value = true
  try {
    const res = await listCollectorTaskInstances(queryParams)
    instanceList.value = res.data.content
    total.value = res.data.totalElements
    // 更新当前页码
    currentPage.value = queryParams.page + 1
  } catch (error) {
    console.error('获取采集任务实例列表出错:', error)
    ElMessage.error('获取采集任务实例列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.taskId = null
  queryParams.status = null
  queryParams.startTime = null
  queryParams.endTime = null
  queryParams.page = 0
  currentPage.value = 1
  fetchInstanceList()
}

// 处理分页变化
const handlePageChange = (val: number) => {
  currentPage.value = val
  queryParams.page = val - 1
  fetchInstanceList()
}

// 处理每页条数变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  queryParams.page = 0
  fetchInstanceList()
}

// 查看任务实例详情
const handleViewDetail = (instanceId: number) => {
  router.push(`/collector/schedule/detail/${instanceId}`)
}

// 格式化时间
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString()
}

// 格式化执行时长
const formatDuration = (duration: number | null) => {
  if (!duration && duration !== 0) return '-'
  
  if (duration < 1000) {
    return `${duration}ms`
  } else if (duration < 60000) {
    return `${(duration / 1000).toFixed(2)}s`
  } else {
    return `${Math.floor(duration / 60000)}m ${Math.floor((duration % 60000) / 1000)}s`
  }
}

onMounted(() => {
  fetchInstanceList()
})
</script>

<template>
  <div class="collector-schedule">
    <div class="page-header">
      <h1>采集调度</h1>
    </div>

    <el-card>
      <div class="search-form">
        <el-form :model="queryParams" inline>
          <el-form-item label="任务ID">
            <el-input v-model="queryParams.taskId" placeholder="请输入任务ID" clearable />
          </el-form-item>
          
          <el-form-item label="执行状态">
            <el-select v-model="queryParams.status" placeholder="请选择执行状态" clearable>
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="0" />
              <el-option label="进行中" :value="2" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="执行时间">
            <el-date-picker
              v-model="queryParams.startTime"
              type="datetime"
              placeholder="开始时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
            />
            <span class="range-separator">至</span>
            <el-date-picker
              v-model="queryParams.endTime"
              type="datetime"
              placeholder="结束时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="fetchInstanceList">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table
        v-loading="loading"
        :data="instanceList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="id" label="实例ID" min-width="80" />
        <el-table-column prop="taskId" label="任务ID" min-width="80" />
        <el-table-column prop="taskName" label="任务名称" min-width="120" />
        <el-table-column prop="taskType" label="任务类型" min-width="100" />
        <el-table-column label="开始时间" min-width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" min-width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="执行时长" min-width="100">
          <template #default="scope">
            {{ formatDuration(scope.row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="metricCount" label="指标数" min-width="80" />
        <el-table-column label="状态" min-width="80">
          <template #default="scope">
            <el-tag :type="statusTagType[scope.row.status]">
              {{ statusMap[scope.row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="scope">
            <el-button type="primary" text @click="handleViewDetail(scope.row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:currentPage="currentPage"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.collector-schedule {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.range-separator {
  margin: 0 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 