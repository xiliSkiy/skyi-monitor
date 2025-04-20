<template>
  <div class="collector-rule">
    <div class="page-header">
      <h1>调度规则</h1>
      <el-button type="primary" @click="createRule">
        <el-icon><Plus /></el-icon> 新建规则
      </el-button>
    </div>

    <el-card>
      <div class="search-form">
        <el-form :model="queryParams" inline>
          <el-form-item label="规则名称">
            <el-input v-model="queryParams.name" placeholder="请输入规则名称" clearable />
          </el-form-item>
          
          <el-form-item label="规则类型">
            <el-select v-model="queryParams.type" placeholder="请选择规则类型" clearable>
              <el-option label="cron表达式" value="cron" />
              <el-option label="固定间隔" value="interval" />
              <el-option label="固定时间" value="fixedTime" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="fetchRuleList">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table
        v-loading="loading"
        :data="ruleList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="id" label="规则ID" min-width="80" />
        <el-table-column prop="name" label="规则名称" min-width="120" />
        <el-table-column prop="type" label="规则类型" min-width="100">
          <template #default="scope">
            {{ formatRuleType(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="expression" label="表达式" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column label="创建时间" min-width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button type="primary" text @click="editRule(scope.row.id)">
              编辑
            </el-button>
            <el-button 
              v-if="scope.row.status === 0" 
              type="success" 
              text 
              @click="enableRule(scope.row.id)"
            >
              启用
            </el-button>
            <el-button 
              v-else 
              type="warning" 
              text 
              @click="disableRule(scope.row.id)"
            >
              禁用
            </el-button>
            <el-button type="danger" text @click="deleteRule(scope.row.id)">
              删除
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

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  listCollectorRules, 
  deleteCollectorRule, 
  enableCollectorRule, 
  disableCollectorRule 
} from '@/api/collector'

const router = useRouter()
const loading = ref(false)

// 查询条件
const queryParams = reactive({
  name: '',
  type: '',
  status: null,
  page: 0,
  size: 10
})

// 规则列表数据
const ruleList = ref([])
const total = ref(0)
const currentPage = ref(1)

// 查询规则列表
const fetchRuleList = async () => {
  loading.value = true
  try {
    const res = await listCollectorRules(queryParams)
    ruleList.value = res.data.content
    total.value = res.data.totalElements
    // 更新当前页码
    currentPage.value = queryParams.page + 1
  } catch (error) {
    console.error('获取调度规则列表出错:', error)
    ElMessage.error('获取调度规则列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.name = ''
  queryParams.type = ''
  queryParams.status = null
  queryParams.page = 0
  currentPage.value = 1
  fetchRuleList()
}

// 处理分页变化
const handlePageChange = (val: number) => {
  currentPage.value = val
  queryParams.page = val - 1
  fetchRuleList()
}

// 处理每页条数变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  queryParams.page = 0
  currentPage.value = 1
  fetchRuleList()
}

// 创建规则
const createRule = () => {
  router.push('/collector/rule/create')
}

// 编辑规则
const editRule = (id: number) => {
  router.push(`/collector/rule/edit/${id}`)
}

// 删除规则
const deleteRule = (id: number) => {
  ElMessageBox.confirm('确定要删除该调度规则吗？此操作不可恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCollectorRule(id)
      ElMessage.success('删除成功')
      fetchRuleList()
    } catch (error) {
      console.error('删除调度规则失败:', error)
      ElMessage.error('删除调度规则失败')
    }
  }).catch(() => {})
}

// 启用规则
const enableRule = async (id: number) => {
  try {
    await enableCollectorRule(id)
    ElMessage.success('启用成功')
    fetchRuleList()
  } catch (error) {
    console.error('启用调度规则失败:', error)
    ElMessage.error('启用调度规则失败')
  }
}

// 禁用规则
const disableRule = async (id: number) => {
  try {
    await disableCollectorRule(id)
    ElMessage.success('禁用成功')
    fetchRuleList()
  } catch (error) {
    console.error('禁用调度规则失败:', error)
    ElMessage.error('禁用调度规则失败')
  }
}

// 格式化规则类型
const formatRuleType = (type: string) => {
  switch (type) {
    case 'cron':
      return 'cron表达式'
    case 'interval':
      return '固定间隔'
    case 'fixedTime':
      return '固定时间'
    default:
      return type
  }
}

// 格式化时间
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString()
}

onMounted(() => {
  fetchRuleList()
})
</script>

<style scoped>
.collector-rule {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 