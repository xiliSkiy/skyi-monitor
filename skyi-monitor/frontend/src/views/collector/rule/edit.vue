<template>
  <div class="collector-rule-edit">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑调度规则' : '创建调度规则' }}</h1>
      <div class="action-buttons">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="saveRule" :loading="saving">保存</el-button>
      </div>
    </div>

    <el-form
      ref="formRef"
      :model="ruleForm"
      :rules="rules"
      label-width="120px"
      v-loading="loading"
      class="rule-form"
    >
      <el-card>
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="ruleForm.name" placeholder="请输入规则名称" />
        </el-form-item>
        
        <el-form-item label="规则类型" prop="type">
          <el-select v-model="ruleForm.type" placeholder="请选择规则类型" @change="handleTypeChange">
            <el-option label="cron表达式" value="cron" />
            <el-option label="固定间隔" value="interval" />
            <el-option label="固定时间" value="fixedTime" />
          </el-select>
        </el-form-item>
        
        <el-form-item 
          v-if="ruleForm.type === 'cron'" 
          label="cron表达式" 
          prop="expression"
        >
          <el-input v-model="ruleForm.expression" placeholder="请输入cron表达式，如: 0 0 12 * * ?" />
          <div class="form-help">
            <el-link type="primary" href="https://crontab.guru/" target="_blank">
              cron表达式在线生成工具
            </el-link>
          </div>
        </el-form-item>
        
        <el-form-item 
          v-if="ruleForm.type === 'interval'" 
          label="间隔时间(秒)" 
          prop="intervalSeconds"
        >
          <el-input-number v-model="ruleForm.intervalSeconds" :min="10" :max="86400" />
        </el-form-item>
        
        <el-form-item 
          v-if="ruleForm.type === 'fixedTime'" 
          label="执行时间" 
          prop="fixedTime"
        >
          <el-time-picker 
            v-model="ruleForm.fixedTime"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择执行时间"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="ruleForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="ruleForm.description" 
            type="textarea" 
            placeholder="请输入规则描述" 
            :rows="3"
          />
        </el-form-item>
      </el-card>

      <el-card class="task-card">
        <template #header>
          <div class="card-header">
            <span>关联采集任务</span>
          </div>
        </template>
        
        <el-form-item label="采集任务" prop="taskIds">
          <el-select
            v-model="ruleForm.taskIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择关联的采集任务"
          >
            <el-option
              v-for="task in taskOptions"
              :key="task.id"
              :label="task.name"
              :value="task.id"
            />
          </el-select>
          <div class="form-help">
            关联的采集任务将按照此规则执行
          </div>
        </el-form-item>
        
        <el-form-item label="超时时间(秒)" prop="timeout">
          <el-input-number v-model="ruleForm.timeout" :min="5" :max="3600" />
          <div class="form-help">
            任务执行超过此时间将被强制结束
          </div>
        </el-form-item>
        
        <el-form-item label="重试次数" prop="retryCount">
          <el-input-number v-model="ruleForm.retryCount" :min="0" :max="5" />
          <div class="form-help">
            任务执行失败后的重试次数，0表示不重试
          </div>
        </el-form-item>
        
        <el-form-item label="重试间隔(秒)" prop="retryInterval">
          <el-input-number v-model="ruleForm.retryInterval" :min="5" :max="300" />
          <div class="form-help">
            任务执行失败后的重试间隔时间
          </div>
        </el-form-item>
      </el-card>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { createCollectorRule, updateCollectorRule, getCollectorRuleById } from '@/api/collector'
import { listCollectorTasks } from '@/api/collector'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

// 判断是编辑还是创建
const isEdit = computed(() => route.params.id !== undefined)
const ruleId = computed(() => isEdit.value ? Number(route.params.id) : undefined)

// 可选任务列表
interface TaskOption {
  id: number;
  name: string;
  code: string;
  type: string;
  status: number;
}

const taskOptions = ref<TaskOption[]>([])

// 表单数据
const ruleForm = reactive({
  name: '',
  type: 'cron',
  expression: '',
  intervalSeconds: 60,
  fixedTime: '00:00:00',
  status: 1,
  description: '',
  taskIds: [] as number[],
  timeout: 60,
  retryCount: 0,
  retryInterval: 30
})

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择规则类型', trigger: 'change' }
  ],
  expression: [
    { required: true, message: '请输入cron表达式', trigger: 'blur' }
  ],
  intervalSeconds: [
    { required: true, message: '请设置间隔时间', trigger: 'change' }
  ],
  fixedTime: [
    { required: true, message: '请选择执行时间', trigger: 'change' }
  ],
  taskIds: [
    { required: true, message: '请选择关联的采集任务', trigger: 'change' }
  ]
})

// 处理规则类型变更
const handleTypeChange = (type: string) => {
  // 根据不同类型调整表单校验和默认值
  // 实际逻辑不复杂，主要是在UI上显示不同的表单项
}

// 获取规则详情（编辑模式）
const fetchRuleDetail = async () => {
  if (!isEdit.value || !ruleId.value) return
  
  loading.value = true
  try {
    const res = await getCollectorRuleById(ruleId.value)
    const ruleData = res.data
    
    // 填充表单数据
    Object.assign(ruleForm, {
      name: ruleData.name,
      type: ruleData.type,
      status: ruleData.status,
      description: ruleData.description,
      taskIds: ruleData.taskIds,
      timeout: ruleData.timeout,
      retryCount: ruleData.retryCount,
      retryInterval: ruleData.retryInterval
    })
    
    // 根据规则类型设置相应的表达式字段
    if (ruleData.type === 'cron') {
      ruleForm.expression = ruleData.expression
    } else if (ruleData.type === 'interval') {
      ruleForm.intervalSeconds = parseInt(ruleData.expression)
    } else if (ruleData.type === 'fixedTime') {
      ruleForm.fixedTime = ruleData.expression
    }
    
  } catch (error) {
    console.error('获取规则详情失败:', error)
    ElMessage.error('获取规则详情失败')
  } finally {
    loading.value = false
  }
}

// 获取任务列表
const fetchTaskList = async () => {
  try {
    const res = await listCollectorTasks({
      page: 0,
      size: 1000,
      status: 1  // 只获取启用状态的任务
    })
    taskOptions.value = res.data.content as unknown as TaskOption[]
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败')
  }
}

// 保存规则
const saveRule = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      // 构建规则数据，将表单数据转换为API所需格式
      const ruleData: any = {
        name: ruleForm.name,
        type: ruleForm.type,
        status: ruleForm.status,
        description: ruleForm.description,
        taskIds: ruleForm.taskIds,
        timeout: ruleForm.timeout,
        retryCount: ruleForm.retryCount,
        retryInterval: ruleForm.retryInterval
      }
      
      // 根据规则类型设置表达式
      if (ruleForm.type === 'cron') {
        ruleData.expression = ruleForm.expression
      } else if (ruleForm.type === 'interval') {
        ruleData.expression = ruleForm.intervalSeconds.toString()
      } else if (ruleForm.type === 'fixedTime') {
        ruleData.expression = ruleForm.fixedTime
      }
      
      // 根据是否编辑模式选择API
      if (isEdit.value && ruleId.value) {
        await updateCollectorRule(ruleId.value, ruleData)
        ElMessage.success('更新规则成功')
      } else {
        await createCollectorRule(ruleData)
        ElMessage.success('创建规则成功')
      }
      
      // 返回列表页
      goBack()
    } catch (error) {
      console.error('保存规则失败:', error)
      ElMessage.error(`${isEdit.value ? '更新' : '创建'}规则失败`)
    } finally {
      saving.value = false
    }
  })
}

// 返回列表
const goBack = () => {
  router.push('/collector/rule')
}

onMounted(() => {
  // 获取可选任务列表
  fetchTaskList()
  
  // 如果是编辑模式，获取规则详情
  if (isEdit.value) {
    fetchRuleDetail()
  }
})
</script>

<style scoped>
.collector-rule-edit {
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

.rule-form :deep(.el-card) {
  margin-bottom: 20px;
}

.task-card :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.rule-form :deep(.el-input),
.rule-form :deep(.el-select) {
  width: 100%;
  max-width: 500px;
}

.form-help {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 