<template>
  <div class="metric-edit-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑指标' : '新建指标' }}</span>
        </div>
      </template>
      
      <el-form 
        ref="formRef" 
        :model="formData" 
        :rules="rules" 
        label-width="120px" 
        class="metric-form"
        v-loading="loading"
      >
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="指标编码" prop="code">
                  <el-input v-model="formData.code" placeholder="请输入指标编码" :disabled="isEdit" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="指标名称" prop="name">
                  <el-input v-model="formData.name" placeholder="请输入指标名称" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="指标类别" prop="category">
                  <el-select v-model="formData.category" placeholder="请选择指标类别" style="width: 100%">
                    <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="数据类型" prop="dataType">
                  <el-select v-model="formData.dataType" placeholder="请选择数据类型" style="width: 100%">
                    <el-option v-for="item in dataTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="单位" prop="unit">
                  <el-input v-model="formData.unit" placeholder="请输入单位" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="采集方式" prop="collectionMethod">
                  <el-select v-model="formData.collectionMethod" placeholder="请选择采集方式" style="width: 100%">
                    <el-option v-for="item in collectionMethodOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="默认值" prop="defaultValue">
                  <el-input-number v-model="formData.defaultValue" :precision="2" :step="0.1" :min="0" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="formData.status">
                    <el-radio :value="1">启用</el-radio>
                    <el-radio :value="0">禁用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="最小阈值" prop="thresholdMin">
                  <el-input-number v-model="formData.thresholdMin" :precision="2" :step="0.1" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="最大阈值" prop="thresholdMax">
                  <el-input-number v-model="formData.thresholdMax" :precision="2" :step="0.1" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="采集参数模板" prop="paramTemplate">
              <el-input
                v-model="formData.paramTemplate"
                type="textarea"
                :rows="4"
                placeholder="请输入JSON格式的采集参数模板"
              />
            </el-form-item>
            
            <el-form-item label="描述" prop="description">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="3"
                placeholder="请输入描述信息"
              />
            </el-form-item>
          </el-tab-pane>
          
          <el-tab-pane label="协议映射" name="protocol">
            <div class="operation-bar">
              <el-button type="primary" @click="addProtocolMapping">添加协议映射</el-button>
            </div>
            
            <el-table :data="formData.protocolMappings" border style="width: 100%; margin-top: 15px;">
              <el-table-column label="协议" width="120">
                <template #default="scope">
                  <el-select v-model="scope.row.protocol" placeholder="请选择协议" style="width: 100%">
                    <el-option
                      v-for="item in protocolOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="指标路径" min-width="200">
                <template #default="scope">
                  <el-input v-model="scope.row.path" placeholder="请输入指标路径" />
                </template>
              </el-table-column>
              <el-table-column label="解析表达式" min-width="200">
                <template #default="scope">
                  <el-input v-model="scope.row.expression" placeholder="请输入解析表达式" />
                </template>
              </el-table-column>
              <el-table-column label="参数" min-width="200">
                <template #default="scope">
                  <el-input v-model="scope.row.parameters" placeholder="请输入JSON格式参数" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="scope">
                  <el-button type="danger" size="small" @click="removeProtocolMapping(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <el-tab-pane label="资产类型映射" name="assetType">
            <div class="operation-bar">
              <el-button type="primary" @click="addAssetTypeMapping">添加资产类型映射</el-button>
            </div>
            
            <el-table :data="formData.assetTypeMappings" border style="width: 100%; margin-top: 15px;">
              <el-table-column label="资产类型" min-width="200">
                <template #default="scope">
                  <el-select v-model="scope.row.assetType" placeholder="请选择资产类型" style="width: 100%">
                    <el-option
                      v-for="item in assetTypeOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="默认启用" width="120">
                <template #default="scope">
                  <el-switch v-model="scope.row.defaultEnabled" :active-value="1" :inactive-value="0" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="scope">
                  <el-button type="danger" size="small" @click="removeAssetTypeMapping(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
        
        <div class="form-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { getMetricById, createMetric, updateMetric } from '@/api/metrics'
import type { FormInstance, FormRules } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 当前激活的标签页
const activeTab = ref('basic')

// 是否为编辑模式
const isEdit = computed(() => !!route.params.id)

// 表单数据
const formData = reactive({
  id: undefined as number | undefined,
  code: '',
  name: '',
  description: '',
  category: '',
  dataType: 'gauge',
  unit: '',
  defaultValue: undefined as number | undefined,
  thresholdMin: undefined as number | undefined,
  thresholdMax: undefined as number | undefined,
  collectionMethod: '',
  paramTemplate: '',
  status: 1,
  protocolMappings: [] as any[],
  assetTypeMappings: [] as any[],
})

// 表单验证规则
const rules = reactive<FormRules>({
  code: [
    { required: true, message: '请输入指标编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_.-]+$/, message: '指标编码只能包含字母、数字、下划线、点和短横线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入指标名称', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择指标类别', trigger: 'change' }
  ],
  dataType: [
    { required: true, message: '请选择数据类型', trigger: 'change' }
  ],
  collectionMethod: [
    { required: true, message: '请选择采集方式', trigger: 'change' }
  ],
  paramTemplate: [
    { validator: validateJSON, trigger: 'blur' }
  ]
})

// 选项数据
const categoryOptions = [
  { label: '系统', value: '系统' },
  { label: '服务器', value: '服务器' },
  { label: '数据库', value: '数据库' },
  { label: '中间件', value: '中间件' },
  { label: '应用', value: '应用' },
  { label: '网络', value: '网络' }
]

const dataTypeOptions = [
  { label: 'Gauge(仪表)', value: 'gauge' },
  { label: 'Counter(计数器)', value: 'counter' },
  { label: 'Histogram(直方图)', value: 'histogram' },
  { label: 'Summary(摘要)', value: 'summary' }
]

const collectionMethodOptions = [
  { label: 'SNMP', value: 'snmp' },
  { label: 'SSH', value: 'ssh' },
  { label: 'HTTP', value: 'http' },
  { label: 'JDBC', value: 'jdbc' },
  { label: 'JMX', value: 'jmx' },
  { label: 'WMI', value: 'wmi' }
]

const protocolOptions = [
  { label: 'SNMP', value: 'snmp' },
  { label: 'SSH', value: 'ssh' },
  { label: 'HTTP', value: 'http' },
  { label: 'JDBC', value: 'jdbc' },
  { label: 'JMX', value: 'jmx' },
  { label: 'WMI', value: 'wmi' }
]

const assetTypeOptions = [
  { label: '服务器', value: 'server' },
  { label: '数据库', value: 'database' },
  { label: 'MySQL', value: 'mysql' },
  { label: 'Oracle', value: 'oracle' },
  { label: 'Elasticsearch', value: 'elasticsearch' },
  { label: '应用', value: 'application' },
  { label: '网络设备', value: 'network' },
  { label: '存储设备', value: 'storage' },
  { label: '系统', value: 'system' }
]

// 校验JSON格式
function validateJSON(rule: any, value: string, callback: any) {
  if (!value) {
    callback()
    return
  }
  
  try {
    JSON.parse(value)
    callback()
  } catch (error) {
    callback(new Error('请输入有效的JSON格式'))
  }
}

// 获取指标详情
const fetchMetricDetail = async (id: string | number) => {
  loading.value = true
  try {
    const res = await getMetricById(id)
    if (res.code === 200) {
      // 将返回的数据填充到表单中
      Object.assign(formData, res.data)
    } else {
      ElMessage.error(res.message || '获取指标详情失败')
    }
  } catch (error) {
    console.error('获取指标详情出错:', error)
    ElMessage.error('获取指标详情出错')
  } finally {
    loading.value = false
  }
}

// 添加协议映射
const addProtocolMapping = () => {
  formData.protocolMappings.push({
    protocol: '',
    path: '',
    expression: '',
    parameters: ''
  })
}

// 移除协议映射
const removeProtocolMapping = (index: number) => {
  formData.protocolMappings.splice(index, 1)
}

// 添加资产类型映射
const addAssetTypeMapping = () => {
  formData.assetTypeMappings.push({
    assetType: '',
    defaultEnabled: 1
  })
}

// 移除资产类型映射
const removeAssetTypeMapping = (index: number) => {
  formData.assetTypeMappings.splice(index, 1)
}

// 取消
const handleCancel = () => {
  router.back()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid, fields) => {
    if (!valid) {
      activeTab.value = 'basic' // 切换到基本信息选项卡
      return
    }
    
    loading.value = true
    try {
      // 校验协议映射
      if (formData.protocolMappings.length === 0) {
        ElMessage.warning('请至少添加一个协议映射')
        activeTab.value = 'protocol'
        loading.value = false
        return
      }
      
      for (const mapping of formData.protocolMappings) {
        if (!mapping.protocol || !mapping.path) {
          ElMessage.warning('协议映射的协议和路径不能为空')
          activeTab.value = 'protocol'
          loading.value = false
          return
        }
      }
      
      // 校验资产类型映射
      if (formData.assetTypeMappings.length === 0) {
        ElMessage.warning('请至少添加一个资产类型映射')
        activeTab.value = 'assetType'
        loading.value = false
        return
      }
      
      for (const mapping of formData.assetTypeMappings) {
        if (!mapping.assetType) {
          ElMessage.warning('资产类型映射的资产类型不能为空')
          activeTab.value = 'assetType'
          loading.value = false
          return
        }
      }
      
      // 提交表单
      const submitFunc = isEdit.value ? updateMetric : createMetric
      const res = await submitFunc(isEdit.value ? (route.params.id as string) : '', formData)
      
      if (res.code === 200) {
        ElMessage.success(`${isEdit.value ? '更新' : '创建'}指标成功`)
        router.push('/collector/metric')
      } else {
        ElMessage.error(res.message || `${isEdit.value ? '更新' : '创建'}指标失败`)
      }
    } catch (error) {
      console.error(`${isEdit.value ? '更新' : '创建'}指标出错:`, error)
      ElMessage.error(`${isEdit.value ? '更新' : '创建'}指标出错`)
    } finally {
      loading.value = false
    }
  })
}

// 初始化
onMounted(() => {
  // 初始化协议映射和资产类型映射，确保它们是数组
  if (!formData.protocolMappings) {
    formData.protocolMappings = []
  }
  
  if (!formData.assetTypeMappings) {
    formData.assetTypeMappings = []
  }
  
  // 如果是编辑模式，获取指标详情
  if (isEdit.value) {
    const id = route.params.id as string
    fetchMetricDetail(id)
  } else {
    // 新增模式，添加默认的协议映射和资产类型映射
    addProtocolMapping()
    addAssetTypeMapping()
  }
})
</script>

<style lang="scss" scoped>
.metric-edit-page {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .metric-form {
    .operation-bar {
      margin-bottom: 15px;
      display: flex;
      justify-content: flex-end;
    }
    
    .form-footer {
      margin-top: 30px;
      display: flex;
      justify-content: center;
      
      .el-button {
        min-width: 120px;
        margin: 0 20px;
      }
    }
  }
}
</style> 