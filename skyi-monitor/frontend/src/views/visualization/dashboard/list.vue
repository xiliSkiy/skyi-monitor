<template>
  <div class="dashboard-list-container">
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="仪表盘名称">
          <el-input v-model="queryParams.name" placeholder="请输入仪表盘名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <div class="table-operations">
        <el-button type="primary" @click="handleCreate">
          <el-icon><plus /></el-icon>
          新建仪表盘
        </el-button>
      </div>

      <el-table v-loading="loading" :data="dashboardList" border style="width: 100%">
        <el-table-column prop="name" label="仪表盘名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="isDefault" label="默认" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.isDefault" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isShared" label="共享" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.isShared" type="primary">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="创建者" min-width="100" />
        <el-table-column prop="createTime" label="创建时间" min-width="150">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="success" :disabled="scope.row.isDefault" @click="handleSetDefault(scope.row)">
              设为默认
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        :total="total"
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDashboardList, setDefaultDashboard, deleteDashboard } from '@/api/visualization'
import { formatDateTime } from '@/utils/formatter'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const dashboardList = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  title: ''
})

// 查询仪表盘列表
const getList = async () => {
  loading.value = true
  try {
    const response = await getDashboardList(queryParams)
    dashboardList.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('获取仪表盘列表失败', error)
    ElMessage.error('获取仪表盘列表失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮点击事件
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置按钮点击事件
const resetQuery = () => {
  queryParams.name = ''
  queryParams.title = ''
  queryParams.pageNum = 1
  getList()
}

// 每页数量变化事件
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

// 当前页变化事件
const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

// 新建仪表盘事件
const handleCreate = () => {
  router.push('/visualization/dashboards/create')
}

// 查看仪表盘事件
const handleView = (row: any) => {
  router.push(`/visualization/dashboards/view/${row.id}`)
}

// 编辑仪表盘事件
const handleEdit = (row: any) => {
  router.push(`/visualization/dashboards/edit/${row.id}`)
}

// 设为默认仪表盘事件
const handleSetDefault = async (row: any) => {
  try {
    await setDefaultDashboard(row.id)
    ElMessage.success('设置默认仪表盘成功')
    getList()
  } catch (error) {
    console.error('设置默认仪表盘失败', error)
    ElMessage.error('设置默认仪表盘失败')
  }
}

// 删除仪表盘事件
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除仪表盘"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDashboard(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除仪表盘失败', error)
      ElMessage.error('删除仪表盘失败')
    }
  }).catch(() => {
    // 取消删除操作
  })
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.dashboard-list-container {
  padding: 20px;

  .filter-container {
    margin-bottom: 20px;
  }

  .table-container {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .table-operations {
      margin-bottom: 20px;
    }
  }

  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style> 