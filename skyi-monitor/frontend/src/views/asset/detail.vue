<template>
  <div class="app-container">
    <el-card class="box-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>资产详情</span>
          <div class="operations">
            <el-button type="primary" size="small" @click="handleEdit">编辑</el-button>
            <el-button size="small" @click="goBack">返回</el-button>
          </div>
        </div>
      </template>
      
      <div class="detail-container" v-if="assetInfo">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="资产ID">{{ assetInfo.id }}</el-descriptions-item>
          <el-descriptions-item label="资产名称">{{ assetInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="资产编码">{{ assetInfo.code }}</el-descriptions-item>
          <el-descriptions-item label="资产类型">
            <el-tag :type="getTypeTagType(assetInfo.type)">
              {{ formatType(assetInfo.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ assetInfo.ipAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="端口">{{ assetInfo.port || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ assetInfo.department || '-' }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ assetInfo.owner || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="assetInfo.status === 1 ? 'success' : 'danger'">
              {{ assetInfo.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ assetInfo.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ assetInfo.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ assetInfo.description || '-' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="tags-section" v-if="assetInfo.tags && assetInfo.tags.length > 0">
          <h3>标签</h3>
          <el-table :data="assetInfo.tags" border style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="tagKey" label="标签键" />
            <el-table-column prop="tagValue" label="标签值" />
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAssetById } from '@/api/asset'
import type { Asset } from '@/types/asset'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const assetInfo = ref<Asset>()

// 资产类型选项
const typeOptions = [
  { value: 'server', label: '服务器' },
  { value: 'database', label: '数据库' },
  { value: 'middleware', label: '中间件' },
  { value: 'application', label: '应用系统' }
]

// 格式化类型
const formatType = (type: string) => {
  const t = typeOptions.find(item => item.value === type)
  return t ? t.label : type
}

// 获取类型标签样式
const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    'server': 'primary',
    'database': 'success',
    'middleware': 'warning',
    'application': 'info'
  }
  return map[type] || 'info'
}

// 获取资产详情
const getDetail = async (id: number) => {
  loading.value = true
  try {
    const res = await getAssetById(id)
    assetInfo.value = res.data
  } catch (error) {
    console.error('获取资产详情失败', error)
    ElMessage.error('获取资产详情失败')
  } finally {
    loading.value = false
  }
}

// 编辑
const handleEdit = () => {
  router.push({ path: `/asset/edit/${route.params.id}` })
}

// 返回列表
const goBack = () => {
  router.push('/asset/list')
}

onMounted(() => {
  if (route.params.id) {
    getDetail(Number(route.params.id))
  }
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .detail-container {
    padding: 20px 0;
  }
  
  .tags-section {
    margin-top: 30px;
    
    h3 {
      margin-bottom: 15px;
    }
  }
}
</style> 