<template>
  <div class="container" v-loading="loading">
    <div class="header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
    </div>

    <!-- 实例详情卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>采集任务实例</h3>
          <el-tag :type="getStatusType(instanceDetail.status)">{{ formatStatus(instanceDetail.status) }}</el-tag>
        </div>
      </template>
      
      <el-descriptions border>
        <el-descriptions-item label="实例ID">{{ instanceDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="任务ID">{{ instanceDetail.taskId }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ instanceDetail.taskName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ instanceDetail.taskType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资产ID">{{ instanceDetail.assetId }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ instanceDetail.assetName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(instanceDetail.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(instanceDetail.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ formatDuration(instanceDetail.duration) }}</el-descriptions-item>
        <el-descriptions-item label="指标数量">{{ instanceDetail.metricCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="instanceDetail.errorMessage">
          <el-alert
            :title="instanceDetail.errorMessage"
            type="error"
            show-icon
            :closable="false"
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 采集数据卡片 -->
    <el-card class="metrics-card">
      <template #header>
        <div class="card-header">
          <h3>采集数据</h3>
          <el-input
            v-model="metricSearch"
            placeholder="搜索指标"
            prefix-icon="Search"
            clearable
            style="width: 200px"
          />
        </div>
      </template>
      
      <el-table :data="filteredMetrics" style="width: 100%">
        <el-table-column prop="metricName" label="指标名称" />
        <el-table-column prop="metricValue" label="指标值" />
        <el-table-column label="采集时间">
          <template #default="scope">
            {{ formatDateTime(scope.row.collectTime) }}
          </template>
        </el-table-column>
        <el-table-column label="标签">
          <template #default="scope">
            <el-tag
              v-for="(value, key) in scope.row.metricLabels"
              :key="key"
              size="small"
              class="metric-tag"
            >
              {{ key }}: {{ value }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="metricData.length === 0" class="no-data">暂无采集数据</div>
      
      <div class="pagination" v-if="metricData.length > 0">
        <el-pagination
          v-model:currentPage="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="metricData.length"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCollectorTaskInstanceById, getMetricDataByInstanceId } from '@/api/collector'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

// 从路由参数获取实例ID
const instanceId = ref(parseInt(route.params.id as string))

// 实例详情
interface InstanceDetail {
  id: number;
  taskId: number;
  taskName: string;
  taskType: string;
  assetId: number;
  assetName: string;
  startTime: string;
  endTime: string;
  status: number;
  errorMessage: string;
  metricCount: number;
  duration: number;
}

const instanceDetail = ref<InstanceDetail>({
  id: 0,
  taskId: 0,
  taskName: '',
  taskType: '',
  assetId: 0,
  assetName: '',
  startTime: '',
  endTime: '',
  status: 0,
  errorMessage: '',
  metricCount: 0,
  duration: 0
})

// 采集数据
interface MetricData {
  id: number;
  taskId: number;
  instanceId: number;
  metricName: string;
  metricValue: number;
  metricLabels: Record<string, string>;
  collectTime: string;
}

const metricData = ref<MetricData[]>([])
const metricSearch = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 过滤后的指标数据
const filteredMetrics = computed(() => {
  let filtered = metricData.value
  
  if (metricSearch.value) {
    const searchText = metricSearch.value.toLowerCase()
    filtered = filtered.filter(item => 
      item.metricName.toLowerCase().includes(searchText)
    )
  }
  
  // 分页
  const startIndex = (currentPage.value - 1) * pageSize.value
  return filtered.slice(startIndex, startIndex + pageSize.value)
})

// 获取实例详情
const fetchInstanceDetail = async () => {
  loading.value = true
  try {
    // 调用实际API获取实例详情
    const response = await getCollectorTaskInstanceById(instanceId.value)
    console.log('实例详情:', response.data)
    instanceDetail.value = response.data as unknown as InstanceDetail
    
    // 获取指标数据
    await fetchMetricData()
  } catch (error) {
    console.error('获取实例详情失败', error)
    ElMessage.error('获取实例详情失败')
  } finally {
    loading.value = false
  }
}

// 获取指标数据
const fetchMetricData = async () => {
  try {
    // 使用API函数获取指标数据
    const response = await getMetricDataByInstanceId({
      instanceId: instanceId.value,
      page: 0,
      size: 1000
    })
    console.log('指标数据:', response.data)
    if (response.data && response.data.content) {
      metricData.value = response.data.content as unknown as MetricData[]
    } else {
      metricData.value = []
    }
  } catch (error) {
    console.error('获取指标数据失败', error)
    metricData.value = []
  }
}

// 返回列表页
const goBack = () => {
  router.push('/collector/schedule')
}

// 获取状态对应的标签类型
const getStatusType = (status: number) => {
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

// 格式化状态文本
const formatStatus = (status: number) => {
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
  fetchInstanceDetail()
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-card, .metrics-card {
  margin-bottom: 20px;
}

.metrics-card {
  margin-bottom: 0;
}

.no-data {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.metric-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}
</style> 