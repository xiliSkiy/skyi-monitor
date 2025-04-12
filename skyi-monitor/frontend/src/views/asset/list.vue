<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="form-inline">
        <el-form-item label="资产名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入资产名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="资产类型">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable>
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table
      v-loading="loading"
      :data="assetList"
      border
      stripe
      style="width: 100%; margin-top: 10px"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="资产名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="code" label="资产编码" min-width="120" show-overflow-tooltip />
      <el-table-column prop="type" label="资产类型" width="100">
        <template #default="scope">
          <el-tag :type="getTypeTagType(scope.row.type)">
            {{ formatType(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" width="140" />
      <el-table-column prop="owner" label="负责人" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column fixed="right" label="操作" width="180">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      :page-sizes="[10, 20, 30, 50]"
      :page-size="queryParams.size"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pagination"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { listAssets, deleteAsset } from '@/api/asset'
import type { Asset, AssetQueryParams } from '@/types/asset'

const router = useRouter()

const loading = ref(false)
const total = ref(0)
const assetList = ref<Asset[]>([])
const queryParams = reactive<AssetQueryParams>({
  name: '',
  type: '',
  status: undefined,
  page: 1,
  size: 10
})

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

// 获取资产列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listAssets(queryParams)
    assetList.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取资产列表失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.name = ''
  queryParams.type = ''
  queryParams.status = undefined
  queryParams.page = 1
  getList()
}

// 每页数量改变
const handleSizeChange = (size: number) => {
  queryParams.size = size
  getList()
}

// 页码改变
const handleCurrentChange = (page: number) => {
  queryParams.page = page
  getList()
}

// 跳转到详情页
const handleDetail = (row: Asset) => {
  router.push({ path: `/asset/detail/${row.id}` })
}

// 跳转到编辑页
const handleUpdate = (row: Asset) => {
  router.push({ path: `/asset/edit/${row.id}` })
}

// 跳转到新增页
const handleAdd = () => {
  router.push({ path: '/asset/create' })
}

// 删除
const handleDelete = async (row: Asset) => {
  try {
    await deleteAsset(row.id as number)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('删除失败', error)
  }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  padding-bottom: 10px;
  background-color: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 