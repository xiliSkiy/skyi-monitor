<template>
  <div class="collector-schedule-edit">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑采集调度' : '创建采集调度' }}</h1>
      <div class="action-buttons">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="saveSchedule" :loading="saving">保存</el-button>
      </div>
    </div>

    <el-form
      ref="formRef"
      :model="scheduleForm"
      :rules="rules"
      label-width="120px"
      v-loading="loading"
      class="schedule-form"
    >
      <el-card>
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        
        <el-form-item label="调度名称" prop="name">
          <el-input v-model="scheduleForm.name" placeholder="请输入调度名称" />
        </el-form-item>
        
        <el-form-item label="关联任务" prop="taskId">
          <el-select 
            v-model="scheduleForm.taskId" 
            placeholder="请选择关联任务"
            :disabled="hasTaskIdFromRoute"
          >
            <el-option 
              v-for="task in taskOptions" 
              :key="task.id" 
              :label="task.name" 
              :value="task.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="调度类型" prop="scheduleType">
          <el-radio-group v-model="scheduleForm.scheduleType">
            <el-radio :value="1">固定频率</el-radio>
            <el-radio :value="2">Cron表达式</el-radio>
            <el-radio :value="3">一次性执行</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 调度配置 - 根据类型显示不同的配置项 -->
        <div v-if="scheduleForm.scheduleType === 1">
          <el-form-item label="执行频率(秒)" prop="fixedRate">
            <el-input-number v-model="scheduleForm.fixedRate" :min="5" :max="86400" />
          </el-form-item>
        </div>
        
        <div v-if="scheduleForm.scheduleType === 2">
          <el-form-item label="Cron表达式" prop="cronExpression">
            <el-input 
              v-model="scheduleForm.cronExpression" 
              placeholder="请输入Cron表达式，如 0 0 0 * * ?"
            />
            <div class="help-text">
              Cron格式: 秒 分 时 日 月 周 [年]，例如 "0 0 12 * * ?" 表示每天中午12点
            </div>
          </el-form-item>
        </div>
        
        <div v-if="scheduleForm.scheduleType === 3">
          <el-form-item label="执行时间" prop="executeTime">
            <el-date-picker
              v-model="scheduleForm.executeTime"
              type="datetime"
              placeholder="选择执行时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </div>
        
        <el-form-item label="有效期">
          <el-col :span="11">
            <el-form-item prop="startTime">
              <el-date-picker
                v-model="scheduleForm.startTime"
                type="datetime"
                placeholder="开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="2" style="text-align: center;">-</el-col>
          <el-col :span="11">
            <el-form-item prop="endTime">
              <el-date-picker
                v-model="scheduleForm.endTime"
                type="datetime"
                placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-form-item>
        
        <el-form-item label="启用状态" prop="enabled">
          <el-switch
            v-model="scheduleForm.enabled"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-card>
      
      <el-card class="retry-config">
        <template #header>
          <div class="card-header">
            <span>重试配置</span>
          </div>
        </template>
        
        <el-form-item label="最大重试次数" prop="maxRetries">
          <el-input-number v-model="scheduleForm.maxRetries" :min="0" :max="10" />
          <div class="help-text">设置为0表示不重试</div>
        </el-form-item>
        
        <el-form-item v-if="scheduleForm.maxRetries > 0" label="重试间隔(秒)" prop="retryInterval">
          <el-input-number v-model="scheduleForm.retryInterval" :min="5" :max="3600" />
        </el-form-item>
      </el-card>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

// 判断是编辑还是创建
const isEdit = computed(() => route.params.id !== undefined)
const scheduleId = computed(() => isEdit.value ? Number(route.params.id) : undefined)

// 判断是否从任务详情页过来创建调度
const hasTaskIdFromRoute = computed(() => route.params.taskId !== undefined)
const taskIdFromRoute = computed(() => hasTaskIdFromRoute.value ? Number(route.params.taskId) : undefined)

// 任务选项
const taskOptions = ref([
  { id: 1, name: 'Linux服务器采集' },
  { id: 2, name: 'Windows服务器采集' },
  { id: 3, name: 'MySQL数据库采集' }
])

// 表单数据
const scheduleForm = reactive({
  name: '',
  taskId: undefined as number | undefined,
  scheduleType: 1,
  fixedRate: 60,
  cronExpression: '',
  executeTime: '',
  startTime: '',
  endTime: '',
  maxRetries: 0,
  retryInterval: 30,
  enabled: 1
})

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入调度名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符', trigger: 'blur' }
  ],
  taskId: [
    { required: true, message: '请选择关联任务', trigger: 'change' }
  ],
  scheduleType: [
    { required: true, message: '请选择调度类型', trigger: 'change' }
  ],
  fixedRate: [
    { required: true, message: '请设置执行频率', trigger: 'change' }
  ],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' }
  ],
  executeTime: [
    { required: true, message: '请选择执行时间', trigger: 'change' }
  ]
})

// 监听调度类型变化，根据不同类型设置不同的验证规则
watch(() => scheduleForm.scheduleType, (newType) => {
  const fixedRateRule = { required: true, message: '请设置执行频率', trigger: 'change' }
  const cronExpressionRule = { required: true, message: '请输入Cron表达式', trigger: 'blur' }
  const executeTimeRule = { required: true, message: '请选择执行时间', trigger: 'change' }
  
  // 重置所有规则
  rules.fixedRate = []
  rules.cronExpression = []
  rules.executeTime = []
  
  // 根据类型设置相应的规则
  if (newType === 1) {
    rules.fixedRate = [fixedRateRule]
  } else if (newType === 2) {
    rules.cronExpression = [cronExpressionRule]
  } else if (newType === 3) {
    rules.executeTime = [executeTimeRule]
  }
})

// 获取调度详情（编辑模式）
const fetchScheduleDetail = async () => {
  if (!isEdit.value) {
    // 创建模式 - 如果有指定的任务ID，则设置
    if (hasTaskIdFromRoute.value) {
      scheduleForm.taskId = taskIdFromRoute.value
      // 根据任务自动生成名称
      const task = taskOptions.value.find(t => t.id === taskIdFromRoute.value)
      if (task) {
        scheduleForm.name = `${task.name} - 自动采集`
      }
    }
    return
  }
  
  loading.value = true
  try {
    // 这里应该是实际的API调用
    // const res = await api.getScheduleById(scheduleId.value)
    // const scheduleData = res.data
    
    // 模拟数据
    const scheduleData = {
      id: scheduleId.value,
      name: '每小时Linux采集',
      taskId: 1,
      scheduleType: 1,
      fixedRate: 3600,
      startTime: '2023-10-01 00:00:00',
      endTime: '2023-12-31 23:59:59',
      maxRetries: 3,
      retryInterval: 60,
      enabled: 1
    }
    
    // 填充表单数据
    Object.assign(scheduleForm, scheduleData)
  } catch (error) {
    ElMessage.error('获取调度详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 保存调度
const saveSchedule = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      // 构建保存数据
      const scheduleData = {
        ...scheduleForm,
        id: scheduleId.value
      }
      
      // 这里应该是实际的API调用
      // if (isEdit.value) {
      //   await api.updateSchedule(scheduleId.value, scheduleData)
      // } else {
      //   await api.createSchedule(scheduleData)
      // }
      
      // 模拟保存成功
      setTimeout(() => {
        ElMessage.success(`${isEdit.value ? '更新' : '创建'}调度成功`)
        goBack()
      }, 1000)
    } catch (error) {
      ElMessage.error(`${isEdit.value ? '更新' : '创建'}调度失败`)
      console.error(error)
    } finally {
      saving.value = false
    }
  })
}

// 返回列表
const goBack = () => {
  router.push('/collector/schedule')
}

onMounted(() => {
  fetchScheduleDetail()
})
</script>

<style scoped>
.collector-schedule-edit {
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

.schedule-form :deep(.el-card) {
  margin-bottom: 20px;
}

.schedule-form :deep(.el-input),
.schedule-form :deep(.el-select),
.schedule-form :deep(.el-date-editor) {
  width: 100%;
  max-width: 500px;
}

.retry-config :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.help-text {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 