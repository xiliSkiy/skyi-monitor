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
        
        <!-- 数据库类型配置 -->
        <template v-if="taskForm.type === 'DATABASE'">
          <el-form-item label="数据库类型" prop="configuration.dbType">
            <el-select v-model="taskForm.configuration.dbType" placeholder="请选择数据库类型">
              <el-option label="MySQL" value="mysql" />
              <el-option label="PostgreSQL" value="postgresql" disabled />
              <el-option label="Oracle" value="oracle" disabled />
              <el-option label="SQL Server" value="sqlserver" disabled />
            </el-select>
          </el-form-item>
          
          <el-form-item label="主机地址" prop="configuration.host">
            <el-input v-model="taskForm.configuration.host" placeholder="请输入数据库主机地址" />
          </el-form-item>
          
          <el-form-item label="端口" prop="configuration.port">
            <el-input-number v-model="taskForm.configuration.port" :min="1" :max="65535" />
          </el-form-item>
          
          <el-form-item label="数据库名" prop="configuration.database">
            <el-input v-model="taskForm.configuration.database" placeholder="请输入数据库名" />
          </el-form-item>
          
          <el-form-item label="用户名" prop="configuration.username">
            <el-input v-model="taskForm.configuration.username" placeholder="请输入数据库用户名" />
          </el-form-item>
          
          <el-form-item label="密码" prop="configuration.password">
            <el-input 
              v-model="taskForm.configuration.password" 
              placeholder="请输入数据库密码" 
              type="password" 
              show-password
            />
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
import { createCollectorTask, updateCollectorTask, getCollectorTaskById } from '@/api/collector'

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
  },
  {
    label: 'MySQL指标',
    options: [
      { label: '连接数', value: 'connection_count' },
      { label: '慢查询数', value: 'slow_query_count' },
      { label: '运行时间', value: 'uptime' },
      { label: '查询总数', value: 'questions' },
      { label: '接收字节数', value: 'bytes_received' },
      { label: '发送字节数', value: 'bytes_sent' },
      { label: 'InnoDB行锁定时间', value: 'innodb_row_lock_time' },
      { label: 'InnoDB缓冲池使用率', value: 'innodb_buffer_pool_usage' }
    ]
  }
]

// 表单数据
const taskForm = reactive({
  name: '',
  type: '',
  target: '',
  status: 'active',
  protocol: '',
  code: '',
  configuration: {
    username: '',
    password: '',
    port: 22,
    authType: 'none',
    token: '',
    interval: 60,
    timeout: 30,
    metrics: [] as string[],
    // 数据库特有配置
    dbType: 'mysql',
    host: '',
    database: ''
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
  ],
  // 数据库相关验证
  'configuration.host': [
    { required: true, message: '请输入数据库主机地址', trigger: 'blur' }
  ],
  'configuration.port': [
    { required: true, message: '请输入数据库端口', trigger: 'blur' }
  ],
  'configuration.username': [
    { required: true, message: '请输入数据库用户名', trigger: 'blur' }
  ],
  'configuration.password': [
    { required: true, message: '请输入数据库密码', trigger: 'blur' }
  ]
})

// 处理任务类型变更
const handleTypeChange = (type: string) => {
  // 根据不同类型重置部分配置和指标
  taskForm.configuration.metrics = [] // 清空已选指标
  
  if (type === 'SSH') {
    taskForm.configuration.port = 22
  } else if (type === 'HTTP') {
    taskForm.configuration.authType = 'none'
  } else if (type === 'DATABASE') {
    // 数据库采集的默认配置
    taskForm.configuration.port = 3306
    taskForm.configuration.dbType = 'mysql'
    taskForm.protocol = 'mysql'
    // 设置target为host的值
    if (taskForm.configuration.host) {
      taskForm.target = taskForm.configuration.host
    }
  }
}

// 获取任务详情（编辑模式）
const fetchTaskDetail = async () => {
  if (!isEdit.value || !taskId.value) return
  
  loading.value = true
  try {
    // 实际的API调用
    const res = await getCollectorTaskById(taskId.value)
    const taskData = res.data
    
    console.log('获取任务详情:', taskData)
    
    // 填充表单数据
    taskForm.name = taskData.name
    taskForm.code = taskData.code
    taskForm.type = taskData.type.toUpperCase() // 转换为大写以匹配前端枚举值
    taskForm.target = taskData.connectionParams?.host || ''
    taskForm.status = taskData.status === 1 ? 'active' : 'inactive'
    taskForm.protocol = taskData.protocol
    
    // 填充配置数据
    if (taskData.connectionParams) {
      const connParams = taskData.connectionParams
      // 通用配置
      taskForm.configuration.username = connParams.username || ''
      taskForm.configuration.password = connParams.password || ''
      
      // 根据不同类型处理特殊配置
      if (taskForm.type === 'SSH') {
        taskForm.configuration.port = parseInt(connParams.port || '22')
      } else if (taskForm.type === 'HTTP') {
        taskForm.configuration.authType = connParams.authType || 'none'
        taskForm.configuration.token = connParams.token || ''
      } else if (taskForm.type === 'DATABASE') {
        taskForm.configuration.port = parseInt(connParams.port || '3306')
        taskForm.configuration.host = connParams.host || ''
        taskForm.configuration.database = connParams.database || 'mysql'
        taskForm.configuration.dbType = connParams.dbType || 'mysql'
      }
    }
    
    taskForm.configuration.interval = taskData.interval
    
    // 填充指标数据
    if (taskData.metrics && Array.isArray(taskData.metrics)) {
      taskForm.configuration.metrics = taskData.metrics.map(m => m.path)
    }
    
  } catch (error) {
    console.error('获取任务详情出错:', error)
    ElMessage.error('获取任务详情失败')
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
      // 构建连接参数
      const connectionParams: Record<string, string> = {}
      
      if (taskForm.type === 'SSH') {
        connectionParams.host = taskForm.target
        connectionParams.port = taskForm.configuration.port.toString()
        connectionParams.username = taskForm.configuration.username
        connectionParams.password = taskForm.configuration.password
      } else if (taskForm.type === 'HTTP') {
        connectionParams.url = taskForm.target
        if (taskForm.configuration.authType === 'basic') {
          connectionParams.username = taskForm.configuration.username
          connectionParams.password = taskForm.configuration.password
        } else if (taskForm.configuration.authType === 'token') {
          connectionParams.token = taskForm.configuration.token
        }
      } else if (taskForm.type === 'DATABASE') {
        connectionParams.host = taskForm.configuration.host || taskForm.target
        connectionParams.port = taskForm.configuration.port.toString()
        connectionParams.username = taskForm.configuration.username
        connectionParams.password = taskForm.configuration.password
        connectionParams.database = taskForm.configuration.database || 'mysql'
      }
      
      // 构建任务数据
      const taskData = {
        name: taskForm.name,
        code: taskForm.code || Date.now().toString(), // 使用时间戳作为临时编码
        type: taskForm.type === 'DATABASE' ? 'database' : taskForm.type.toLowerCase(),
        protocol: taskForm.protocol || (taskForm.type === 'DATABASE' ? 'mysql' : ''),
        assetId: 1, // 这里应该从选择框中获取，暂时使用固定值
        interval: taskForm.configuration.interval,
        status: taskForm.status === 'active' ? 1 : 0,
        description: '',
        // 转换格式以符合后端API要求
        metrics: taskForm.configuration.metrics.map(metric => ({
          name: metric,
          path: metric,
          dataType: 'gauge',
          enabled: true
        })),
        connectionParams: connectionParams
      }
      
      console.log('发送的任务数据:', taskData)
      
      // 实际的API调用
      if (isEdit.value && taskId.value) {
        const res = await updateCollectorTask(taskId.value, taskData)
        console.log('更新任务结果:', res)
      } else {
        const res = await createCollectorTask(taskData)
        console.log('创建任务结果:', res)
      }
      
      ElMessage.success(`${isEdit.value ? '更新' : '创建'}任务成功`)
      goBack()
    } catch (error) {
      console.error('保存任务时出错:', error)
      ElMessage.error(`${isEdit.value ? '更新' : '创建'}任务失败`)
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