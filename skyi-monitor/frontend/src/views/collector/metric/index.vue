<template>
  <div class="metric-page">
    <el-card class="search-card">
      <template #header>
        <div class="search-header">
          <span>指标管理</span>
          <el-button type="primary" @click="handleAdd">新建指标</el-button>
        </div>
      </template>
      <el-form :model="searchForm" inline>
        <el-form-item label="指标编码">
          <el-input v-model="searchForm.code" placeholder="请输入指标编码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="指标名称">
          <el-input v-model="searchForm.name" placeholder="请输入指标名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="指标类别">
          <el-select v-model="searchForm.category" placeholder="请选择类别" clearable>
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="采集方式">
          <el-select v-model="searchForm.collectionMethod" placeholder="请选择采集方式" clearable>
            <el-option v-for="item in collectionMethodOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="list-card">
      <el-button type="success" @click="handleImport">批量导入</el-button>
      <el-button type="primary" @click="handleExport">批量导出</el-button>
      <el-table v-loading="loading" :data="tableData" border style="width: 100%; margin-top: 15px;">
        <el-table-column prop="code" label="指标编码" width="180" />
        <el-table-column prop="name" label="指标名称" width="180" />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="dataType" label="数据类型" width="120">
          <template #default="scope">
            <el-tag :type="getDataTypeTagType(scope.row.dataType)">{{ scope.row.dataType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="100" />
        <el-table-column prop="collectionMethod" label="采集方式" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联资产类型" min-width="180">
          <template #default="scope">
            <el-tag v-for="item in scope.row.assetTypeMappings" :key="item.id" size="small" class="asset-type-tag">
              {{ item.assetType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="320">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            <el-button size="small" type="info" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入指标定义" width="600px">
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleUploadChange"
        :limit="1"
        accept=".json"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传JSON格式文件，大小不超过2MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmImport">确认导入</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { UploadFile } from 'element-plus'
import { getMetrics, deleteMetric, enableMetric, disableMetric, importMetrics, exportMetrics } from '@/api/metrics'

const router = useRouter()

// 数据加载状态
const loading = ref(false)

// 分页相关参数
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 表格数据
const tableData = ref([])

// 导入对话框
const importDialogVisible = ref(false)
const uploadFile = ref<UploadFile | null>(null)

// 搜索表单
const searchForm = reactive({
  code: '',
  name: '',
  category: '',
  collectionMethod: '',
  status: ''
})

// 表单选项
const categoryOptions = [
  { label: '系统', value: '系统' },
  { label: '服务器', value: '服务器' },
  { label: '数据库', value: '数据库' },
  { label: '中间件', value: '中间件' },
  { label: '应用', value: '应用' },
  { label: '网络', value: '网络' }
]

const collectionMethodOptions = [
  { label: 'SNMP', value: 'snmp' },
  { label: 'SSH', value: 'ssh' },
  { label: 'HTTP', value: 'http' },
  { label: 'JDBC', value: 'jdbc' },
  { label: 'JMX', value: 'jmx' },
  { label: 'WMI', value: 'wmi' }
]

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

// 获取数据列表
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value - 1, // 后端页码从0开始
      size: size.value,
      ...searchForm
    }
    const res = await getMetrics(params)
    if (res.code === 200) {
      tableData.value = res.data.content
      total.value = res.data.totalElements
    } else {
      ElMessage.error(res.message || '获取指标列表失败')
    }
  } catch (error) {
    console.error('获取指标列表出错:', error)
    ElMessage.error('获取指标列表出错')
  } finally {
    loading.value = false
  }
}

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

// 搜索
const handleSearch = () => {
  page.value = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key as keyof typeof searchForm] = ''
  })
  page.value = 1
  fetchData()
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  size.value = val
  fetchData()
}

// 页码变化
const handleCurrentChange = (val: number) => {
  page.value = val
  fetchData()
}

// 新增指标
const handleAdd = () => {
  router.push('/collector/metric/edit')
}

// 编辑指标
const handleEdit = (row: any) => {
  router.push(`/collector/metric/edit/${row.id}`)
}

// 查看指标详情
const handleView = (row: any) => {
  router.push(`/collector/metric/detail/${row.id}`)
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  try {
    loading.value = true
    const func = row.status === 1 ? disableMetric : enableMetric
    const res = await func(row.id)
    if (res.code === 200) {
      ElMessage.success(`${row.status === 1 ? '禁用' : '启用'}指标成功`)
      fetchData()
    } else {
      ElMessage.error(res.message || `${row.status === 1 ? '禁用' : '启用'}指标失败`)
    }
  } catch (error) {
    console.error(`${row.status === 1 ? '禁用' : '启用'}指标出错:`, error)
    ElMessage.error(`${row.status === 1 ? '禁用' : '启用'}指标出错`)
  } finally {
    loading.value = false
  }
}

// 删除指标
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除指标 ${row.name}(${row.code}) 吗？`, '删除确认', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      loading.value = true
      const res = await deleteMetric(row.id)
      if (res.code === 200) {
        ElMessage.success('删除指标成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '删除指标失败')
      }
    } catch (error) {
      console.error('删除指标出错:', error)
      ElMessage.error('删除指标出错')
    } finally {
      loading.value = false
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 处理上传文件变化
const handleUploadChange = (file: UploadFile) => {
  uploadFile.value = file
}

// 导入指标
const handleImport = () => {
  importDialogVisible.value = true
}

// 确认导入
const confirmImport = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  const reader = new FileReader()
  reader.onload = async (e) => {
    try {
      const content = e.target?.result as string
      const metrics = JSON.parse(content)
      
      loading.value = true
      const res = await importMetrics(metrics)
      if (res.code === 200) {
        ElMessage.success('导入指标成功')
        importDialogVisible.value = false
        fetchData()
      } else {
        ElMessage.error(res.message || '导入指标失败')
      }
    } catch (error) {
      console.error('导入指标出错:', error)
      ElMessage.error('导入指标出错，请检查文件格式是否正确')
    } finally {
      loading.value = false
    }
  }
  reader.readAsText(uploadFile.value.raw as Blob)
}

// 导出指标
const handleExport = async () => {
  try {
    loading.value = true
    const category = searchForm.category || undefined
    const res = await exportMetrics(category)
    if (res.code === 200) {
      // 导出为JSON文件
      const blob = new Blob([JSON.stringify(res.data, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `metrics_${category || 'all'}_${new Date().getTime()}.json`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      ElMessage.success('导出指标成功')
    } else {
      ElMessage.error(res.message || '导出指标失败')
    }
  } catch (error) {
    console.error('导出指标出错:', error)
    ElMessage.error('导出指标出错')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.metric-page {
  padding: 20px;
  
  .search-card {
    margin-bottom: 20px;
    
    .search-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .list-card {
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
    
    .asset-type-tag {
      margin-right: 5px;
      margin-bottom: 5px;
    }
  }
}
</style> 