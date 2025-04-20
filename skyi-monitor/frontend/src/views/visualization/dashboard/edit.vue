<template>
  <div class="dashboard-edit-container">
    <div class="form-header">
      <h2>{{ isEdit ? '编辑仪表盘' : '创建仪表盘' }}</h2>
      <div class="actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
      </div>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="dashboard-form"
    >
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        <el-form-item label="仪表盘名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入仪表盘名称" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入仪表盘标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入仪表盘描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="共享设置">
          <el-switch
            v-model="form.isShared"
            active-text="共享"
            inactive-text="私有"
          />
        </el-form-item>
        <el-form-item label="刷新间隔">
          <el-input-number
            v-model="form.refreshInterval"
            :min="0"
            :step="10"
            placeholder="自动刷新间隔（秒）"
          />
          <span class="form-item-help">设置为0表示不自动刷新</span>
        </el-form-item>
      </el-card>

      <el-card class="box-card layout-card">
        <template #header>
          <div class="card-header">
            <span>布局设置</span>
          </div>
        </template>
        <el-form-item label="布局配置" prop="layout">
          <el-input
            v-model="form.layout"
            type="textarea"
            placeholder="请输入布局配置(JSON格式)"
            :rows="5"
          />
          <div class="form-item-help">
            布局配置采用JSON格式，定义仪表盘的整体布局结构
          </div>
        </el-form-item>
      </el-card>

      <el-card v-if="isEdit" class="box-card component-card">
        <template #header>
          <div class="card-header">
            <span>组件管理</span>
            <el-button type="primary" size="small" @click="handleAddComponent">
              添加组件
            </el-button>
          </div>
        </template>
        <el-table 
          v-if="components.length > 0" 
          :data="components" 
          border 
          style="width: 100%"
        >
          <el-table-column prop="title" label="组件标题" min-width="120" />
          <el-table-column prop="type" label="组件类型" min-width="100">
            <template #default="scope">
              {{ getComponentTypeName(scope.row.type) }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="实时数据" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.realtime ? 'success' : 'info'">
                {{ scope.row.realtime ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="handleEditComponent(scope.row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteComponent(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="empty-component">
          <el-empty description="暂无组件，请添加组件"></el-empty>
        </div>
      </el-card>
    </el-form>

    <!-- 组件编辑对话框 -->
    <el-dialog
      v-model="componentDialogVisible"
      :title="editingComponent.id ? '编辑组件' : '添加组件'"
      width="650px"
      append-to-body
    >
      <el-form
        ref="componentFormRef"
        :model="editingComponent"
        :rules="componentRules"
        label-width="100px"
      >
        <el-form-item label="组件标题" prop="title">
          <el-input v-model="editingComponent.title" placeholder="请输入组件标题" />
        </el-form-item>
        <el-form-item label="组件类型" prop="type">
          <el-select v-model="editingComponent.type" placeholder="请选择组件类型" style="width: 100%">
            <el-option
              v-for="item in componentTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="editingComponent.description"
            type="textarea"
            placeholder="请输入组件描述"
            :rows="2"
          />
        </el-form-item>
        <el-form-item label="位置配置" prop="position">
          <el-input
            v-model="editingComponent.position"
            type="textarea"
            placeholder="请输入位置配置(JSON格式)"
            :rows="3"
          />
          <span class="form-item-help">位置配置包含x, y, w, h信息，采用JSON格式</span>
        </el-form-item>
        <el-form-item label="数据源" prop="dataSource">
          <el-input
            v-model="editingComponent.dataSource"
            type="textarea"
            placeholder="请输入数据源配置(JSON格式)"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="样式配置">
          <el-input
            v-model="editingComponent.styles"
            type="textarea"
            placeholder="请输入样式配置(JSON格式)"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="实时数据">
          <el-switch v-model="editingComponent.realtime" />
        </el-form-item>
        <el-form-item label="刷新间隔">
          <el-input-number
            v-model="editingComponent.refreshInterval"
            :min="0"
            :step="10"
            placeholder="刷新间隔（秒）"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-input-number
            v-model="editingComponent.timeRange"
            :min="0"
            :step="60"
            placeholder="时间范围（秒）"
          />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="editingComponent.sortOrder" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="componentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveComponent">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getDashboardDetail,
  createDashboard,
  updateDashboard,
  getComponentList,
  createComponent,
  updateComponent,
  deleteComponent
} from '@/api/visualization'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const componentFormRef = ref<FormInstance>()
const loading = ref(false)
const componentDialogVisible = ref(false)

// 是否为编辑模式
const isEdit = computed(() => {
  return route.params.id !== undefined
})

// 仪表盘表单数据
const form = reactive({
  id: '',
  name: '',
  title: '',
  description: '',
  layout: '',
  isShared: false,
  refreshInterval: 60
})

// 表单验证规则
const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入仪表盘名称', trigger: 'blur' }],
  title: [{ required: true, message: '请输入仪表盘标题', trigger: 'blur' }]
})

// 组件列表
const components = ref<any[]>([])

// 当前编辑的组件
const editingComponent = reactive({
  id: '',
  dashboardId: '',
  type: '',
  title: '',
  description: '',
  position: '',
  dataSource: '',
  styles: '',
  realtime: false,
  refreshInterval: 0,
  timeRange: 3600,
  sortOrder: 0
})

// 组件表单验证规则
const componentRules = reactive<FormRules>({
  title: [{ required: true, message: '请输入组件标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择组件类型', trigger: 'change' }],
  position: [{ required: true, message: '请输入位置配置', trigger: 'blur' }],
  dataSource: [{ required: true, message: '请输入数据源配置', trigger: 'blur' }]
})

// 组件类型选项
const componentTypes = [
  { value: 'LINE_CHART', label: '折线图' },
  { value: 'BAR_CHART', label: '柱状图' },
  { value: 'PIE_CHART', label: '饼图' },
  { value: 'GAUGE', label: '仪表盘' },
  { value: 'TABLE', label: '表格' },
  { value: 'VALUE', label: '单值' },
  { value: 'TEXT', label: '文本' },
  { value: 'HEAT_MAP', label: '热力图' },
  { value: 'SCATTER_CHART', label: '散点图' },
  { value: 'RADAR_CHART', label: '雷达图' }
]

// 获取组件类型名称
const getComponentTypeName = (type: string) => {
  const typeObj = componentTypes.find(item => item.value === type)
  return typeObj ? typeObj.label : type
}

// 获取仪表盘详情
const getDashboardInfo = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const res = await getDashboardDetail(route.params.id as string)
    Object.assign(form, res.data)
    // 获取组件列表
    getComponentsInfo()
  } catch (error) {
    console.error('获取仪表盘详情失败', error)
    ElMessage.error('获取仪表盘详情失败')
  } finally {
    loading.value = false
  }
}

// 获取组件列表
const getComponentsInfo = async () => {
  try {
    const res = await getComponentList(route.params.id as string)
    components.value = res.data
  } catch (error) {
    console.error('获取组件列表失败', error)
    ElMessage.error('获取组件列表失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 检查JSON格式的字段是否合法
      if (form.layout && !isValidJSON(form.layout)) {
        ElMessage.error('布局配置不是有效的JSON格式')
        return
      }
      
      if (isEdit.value) {
        await updateDashboard(route.params.id as string, form)
        ElMessage.success('仪表盘更新成功')
      } else {
        await createDashboard(form)
        ElMessage.success('仪表盘创建成功')
      }
      
      // 返回列表页
      router.push('/visualization/dashboards')
    } catch (error) {
      console.error(isEdit.value ? '更新仪表盘失败' : '创建仪表盘失败', error)
      ElMessage.error(isEdit.value ? '更新仪表盘失败' : '创建仪表盘失败')
    } finally {
      loading.value = false
    }
  })
}

// 取消操作
const handleCancel = () => {
  router.push('/visualization/dashboards')
}

// 添加组件
const handleAddComponent = () => {
  Object.assign(editingComponent, {
    id: '',
    dashboardId: route.params.id,
    type: '',
    title: '',
    description: '',
    position: '',
    dataSource: '',
    styles: '',
    realtime: false,
    refreshInterval: 0,
    timeRange: 3600,
    sortOrder: components.value.length
  })
  componentDialogVisible.value = true
}

// 编辑组件
const handleEditComponent = (row: any) => {
  Object.assign(editingComponent, row)
  componentDialogVisible.value = true
}

// 删除组件
const handleDeleteComponent = (row: any) => {
  ElMessageBox.confirm(`确定要删除组件"${row.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteComponent(route.params.id as string, row.id)
      ElMessage.success('删除组件成功')
      getComponentsInfo()
    } catch (error) {
      console.error('删除组件失败', error)
      ElMessage.error('删除组件失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 保存组件
const saveComponent = async () => {
  await componentFormRef.value?.validate(async (valid) => {
    if (!valid) return
    
    try {
      // 检查JSON格式的字段是否合法
      if (editingComponent.position && !isValidJSON(editingComponent.position)) {
        ElMessage.error('位置配置不是有效的JSON格式')
        return
      }
      
      if (editingComponent.dataSource && !isValidJSON(editingComponent.dataSource)) {
        ElMessage.error('数据源配置不是有效的JSON格式')
        return
      }
      
      if (editingComponent.styles && !isValidJSON(editingComponent.styles)) {
        ElMessage.error('样式配置不是有效的JSON格式')
        return
      }
      
      if (editingComponent.id) {
        // 更新组件
        await updateComponent(
          route.params.id as string,
          editingComponent.id,
          editingComponent
        )
        ElMessage.success('组件更新成功')
      } else {
        // 创建组件
        await createComponent(route.params.id as string, editingComponent)
        ElMessage.success('组件添加成功')
      }
      
      componentDialogVisible.value = false
      getComponentsInfo()
    } catch (error) {
      console.error(editingComponent.id ? '更新组件失败' : '添加组件失败', error)
      ElMessage.error(editingComponent.id ? '更新组件失败' : '添加组件失败')
    }
  })
}

// 检查是否为有效的JSON字符串
const isValidJSON = (str: string) => {
  if (!str) return true
  try {
    JSON.parse(str)
    return true
  } catch (error) {
    return false
  }
}

onMounted(() => {
  getDashboardInfo()
})
</script>

<style lang="scss" scoped>
.dashboard-edit-container {
  padding: 20px;
  
  .form-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
    }
  }
  
  .dashboard-form {
    .el-card {
      margin-bottom: 20px;
    }
    
    .form-item-help {
      font-size: 12px;
      color: #909399;
      margin-top: 5px;
    }
  }
  
  .empty-component {
    padding: 30px 0;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style> 