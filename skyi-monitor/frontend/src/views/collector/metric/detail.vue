<template>
  <div class="metric-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>指标详情</span>
          <div class="header-buttons">
            <el-button type="primary" @click="handleEdit">编辑</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs>
        <el-tab-pane label="基本信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="指标编码">{{ metricData.code }}</el-descriptions-item>
            <el-descriptions-item label="指标名称">{{ metricData.name }}</el-descriptions-item>
            <el-descriptions-item label="指标类别">{{ metricData.category }}</el-descriptions-item>
            <el-descriptions-item label="数据类型">
              <el-tag :type="getDataTypeTagType(metricData.dataType)">{{ metricData.dataType }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="单位">{{ metricData.unit || '-' }}</el-descriptions-item>
            <el-descriptions-item label="采集方式">{{ metricData.collectionMethod }}</el-descriptions-item>
            <el-descriptions-item label="默认值">{{ metricData.defaultValue ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="metricData.status === 1 ? 'success' : 'info'">
                {{ metricData.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最小阈值">{{ metricData.thresholdMin ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="最大阈值">{{ metricData.thresholdMax ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(metricData.createdTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(metricData.updatedTime) }}</el-descriptions-item>
          </el-descriptions>
          
          <el-divider content-position="left">采集参数模板</el-divider>
          <div class="param-template">
            <pre>{{ formatJSON(metricData.paramTemplate) }}</pre>
          </div>
          
          <el-divider content-position="left">描述</el-divider>
          <div class="description">
            {{ metricData.description || '暂无描述' }}
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="协议映射">
          <el-table :data="metricData.protocolMappings || []" border style="width: 100%">
            <el-table-column prop="protocol" label="协议" width="120" />
            <el-table-column prop="path" label="指标路径" min-width="200" />
            <el-table-column prop="expression" label="解析表达式" min-width="200" />
            <el-table-column label="参数" min-width="200">
              <template #default="scope">
                <pre>{{ formatJSON(scope.row.parameters) }}</pre>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="资产类型映射">
          <el-table :data="metricData.assetTypeMappings || []" border style="width: 100%">
            <el-table-column prop="assetType" label="资产类型" min-width="200" />
            <el-table-column label="默认启用" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.defaultEnabled === 1 ? 'success' : 'info'">
                  {{ scope.row.defaultEnabled === 1 ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMetricById } from '@/api/metrics'

const route = useRoute()
const router = useRouter()

// 数据加载状态
const loading = ref(false)

// 指标数据
const metricData = reactive({
  id: undefined as number | undefined,
  code: '',
  name: '',
  description: '',
  category: '',
  dataType: '',
  unit: '',
  defaultValue: undefined as number | undefined,
  thresholdMin: undefined as number | undefined,
  thresholdMax: undefined as number | undefined,
  collectionMethod: '',
  paramTemplate: '',
  status: 0,
  protocolMappings: [] as any[],
  assetTypeMappings: [] as any[],
  createdTime: '',
  updatedTime: ''
})

// 获取数据类型对应的标签类型
const getDataTypeTagType = (dataType: string) => {
  const map: Record<string, string> = {
    'gauge': '',
    'counter': 'success', 
    'histogram': 'warning',
    'summary': 'info'
  }
  return map[dataType] || ''
}

// 格式化JSON
const formatJSON = (jsonStr: string): string => {
  if (!jsonStr) return '-'
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch (error) {
    return jsonStr
  }
}

// 格式化日期时间
const formatDateTime = (dateTime?: string): string => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString()
}

// 获取指标详情
const fetchMetricDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('指标ID不能为空')
    return
  }
  
  loading.value = true
  try {
    const res = await getMetricById(id as string)
    if (res.code === 200) {
      Object.assign(metricData, res.data)
    } else {
      ElMessage.error(res.message || '获取指标详情失败')
    }
  } catch (error) {
    console.error('获取指标详情出错:', error)
    ElMessage.error('获取指标详情出错')
  } finally {
    loading.value = false
  }
}

// 编辑
const handleEdit = () => {
  router.push(`/collector/metric/edit/${metricData.id}`)
}

// 返回
const handleBack = () => {
  router.push('/collector/metric')
}

// 初始化
onMounted(() => {
  fetchMetricDetail()
})
</script>

<style lang="scss" scoped>
.metric-detail-page {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-buttons {
      .el-button {
        margin-left: 10px;
      }
    }
  }
  
  .param-template, .description {
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
    
    pre {
      margin: 0;
      white-space: pre-wrap;
      word-wrap: break-word;
    }
  }
}
</style> 