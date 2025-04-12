<template>
  <div class="collector-task-edit">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑采集任务' : '创建采集任务' }}</h1>
      <div class="action-buttons">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="saveTask" :loading="saving">保存</el-button>
      </div>
    </div>

    <el-form
      ref="formRef"
      :model="taskForm"
      :rules="rules"
      label-width="120px"
      v-loading="loading"
      class="task-form"
    >
      <el-card>
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="taskForm.type" placeholder="请选择任务类型" @change="handleTypeChange">
            <el-option v-for="item in taskTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="采集目标" prop="target">
          <el-input v-model="taskForm.target" placeholder="请输入采集目标" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="taskForm.status"
            :active-value="'active'"
            :inactive-value="'inactive'"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-card>

      <el-card class="configuration-card">
        <template #header>
          <div class="card-header">
            <span>配置信息</span>
          </div>
        </template>
        
        <!-- SSH类型配置 -->
        <template v-if="taskForm.type === 'SSH'">
          <el-form-item label="用户名" prop="configuration.username">
            <el-input v-model="taskForm.configuration.username" placeholder="请输入SSH用户名" />
          </el-form-item>
          
          <el-form-item label="密码" prop="configuration.password">
            <el-input 
              v-model="taskForm.configuration.password" 
              placeholder="请输入SSH密码" 
              type="password" 
              show-password
            />
          </el-form-item>
          
          <el-form-item label="端口" prop="configuration.port">
            <el-input-number v-model="taskForm.configuration.port" :min="1" :max="65535" />
          </el-form-item>
        </template>
        
        <!-- HTTP类型配置 -->
        <template v-if="taskForm.type === 'HTTP'">
          <el-form-item label="认证方式" prop="configuration.authType">
            <el-select v-model="taskForm.configuration.authType" placeholder="请选择认证方式">
              <el-option label="无需认证" value="none" />
              <el-option label="Basic认证" value="basic" />
              <el-option label="Bearer Token" value="token" />
            </el-select>
          </el-form-item>
          
          <el-form-item v-if="taskForm.configuration.authType === 'basic'" label="用户名" prop="configuration.username">
            <el-input v-model="taskForm.configuration.username" placeholder="请输入用户名" />
          </el-form-item>
          
          <el-form-item v-if="taskForm.configuration.authType === 'basic'" label="密码" prop="configuration.password">
            <el-input 
              v-model="taskForm.configuration.password" 
              placeholder="请输入密码" 
              type="password" 
              show-password
            />
          </el-form-item>
          
          <el-form-item v-if="taskForm.configuration.authType === 'token'" label="Token" prop="configuration.token">
            <el-input v-model="taskForm.configuration.token" placeholder="请输入Token" />
          </el-form-item>
        </template>
        
        <!-- 通用配置 -->
        <el-form-item label="采集间隔(秒)" prop="configuration.interval">
          <el-input-number v-model="taskForm.configuration.interval" :min="5" :max="86400" />
        </el-form-item>
        
        <el-form-item label="超时时间(秒)" prop="configuration.timeout">
          <el-input-number v-model="taskForm.configuration.timeout" :min="1" :max="300" />
        </el-form-item>
        
        <el-form-item label="采集指标" prop="configuration.metrics">
          <el-select
            v-model="taskForm.configuration.metrics"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择采集指标"
          >
            <el-option-group
              v-for="group in metricOptions"
              :key="group.label"
              :label="group.label"
            >
              <el-option
                v-for="item in group.options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-option-group>
          </el-select>
        </el-form-item>
      </el-card>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

// 判断是编辑还是创建
const isEdit = computed(() => route.params.id !== undefined)
const taskId = computed(() => (isEdit.value ? Number(route.params.id) : undefined))

// 任务类型选项
const taskTypes = [
  { label: 'SSH采集', value: 'SSH' },
  { label: 'HTTP采集', value: 'HTTP' },
  { label: 'SNMP采集', value: 'SNMP' },
  { label: '数据库采集', value: 'DATABASE' }
]

// 指标选项
const metricOptions = [
  {
    label: '系统资源',
    options: [
      { label: 'CPU使用率', value: 'cpu_usage' },
      { label: '内存使用率', value: 'memory_usage' },
      { label: '磁盘使用率', value: 'disk_usage' },
      { label: '网络流量', value: 'network_traffic' }
    ]
  },
  {
    label: '应用指标',
    options: [
      { label: 'JVM堆内存', value: 'jvm_heap' },
      { label: 'JVM非堆内存', value: 'jvm_nonheap' },
      { label: '线程数', value: 'thread_count' },
      { label: 'GC次数', value: 'gc_count' }
    ]
  }
]

// 表单数据
const taskForm = reactive({
  name: '',
  type: '',
  target: '',
  status: 'active',
  configuration: {
    username: '',
    password: '',
    port: 22,
    authType: 'none',
    token: '',
    interval: 60,
    timeout: 30,
    metrics: []
  }
})

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ],
  target: [
    { required: true, message: '请输入采集目标', trigger: 'blur' }
  ],
  'configuration.interval': [
    { required: true, message: '请设置采集间隔', trigger: 'change' }
  ],
  'configuration.metrics': [
    { required: true, message: '请选择至少一个采集指标', trigger: 'change' }
  ]
})

// 处理任务类型变更
const handleTypeChange = (type: string) => {
  // 根据不同类型重置部分配置
  if (type === 'SSH') {
    taskForm.configuration.port = 22
  } else if (type === 'HTTP') {
    taskForm.configuration.authType = 'none'
  }
}

// 获取任务详情（编辑模式）
const fetchTaskDetail = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    // 这里应该是实际的API调用
    // const res = await api.getTaskDetail(taskId.value)
    // const taskData = res.data
    
    // 模拟数据
    const taskData = {
      id: taskId.value,
      name: 'Linux服务器采集',
      type: 'SSH',
      target: '192.168.1.100',
      status: 'active',
      configuration: {
        username: 'admin',
        password: '******',
        port: 22,
        interval: 60,
        timeout: 30,
        metrics: ['cpu_usage', 'memory_usage', 'disk_usage']
      }
    }
    
    // 填充表单数据
    taskForm.name = taskData.name
    taskForm.type = taskData.type
    taskForm.target = taskData.target
    taskForm.status = taskData.status
    
    // 填充配置数据
    Object.assign(taskForm.configuration, taskData.configuration)
  } catch (error) {
    ElMessage.error('获取任务详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 保存任务
const saveTask = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      // 构建任务数据
      const taskData = {
        ...taskForm,
        id: taskId.value
      }
      
      // 这里应该是实际的API调用
      // if (isEdit.value) {
      //   await api.updateTask(taskData)
      // } else {
      //   await api.createTask(taskData)
      // }
      
      // 模拟保存成功
      setTimeout(() => {
        ElMessage.success(`${isEdit.value ? '更新' : '创建'}任务成功`)
        goBack()
      }, 1000)
    } catch (error) {
      ElMessage.error(`${isEdit.value ? '更新' : '创建'}任务失败`)
      console.error(error)
    } finally {
      saving.value = false
    }
  })
}

// 返回列表
const goBack = () => {
  router.push('/collector/task')
}

onMounted(() => {
  fetchTaskDetail()
})
</script>

<style scoped>
.collector-task-edit {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-form :deep(.el-card) {
  margin-bottom: 20px;
}

.configuration-card :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.task-form :deep(.el-input),
.task-form :deep(.el-select) {
  width: 100%;
  max-width: 500px;
}
</style> 