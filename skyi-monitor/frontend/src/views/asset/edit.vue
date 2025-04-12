<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑资产' : '创建资产' }}</span>
        </div>
      </template>
      <div class="form-container">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          :disabled="loading"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="资产名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入资产名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="资产编码" prop="code">
                <el-input 
                  v-model="form.code" 
                  placeholder="请输入资产编码" 
                  :disabled="isEdit"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="资产类型" prop="type">
                <el-select v-model="form.type" placeholder="请选择资产类型" style="width: 100%">
                  <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio :label="1">启用</el-radio>
                  <el-radio :label="0">停用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="IP地址" prop="ipAddress">
                <el-input v-model="form.ipAddress" placeholder="请输入IP地址" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="端口" prop="port">
                <el-input-number v-model="form.port" :min="1" :max="65535" placeholder="请输入端口" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="所属部门" prop="department">
                <el-input v-model="form.department" placeholder="请输入所属部门" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="负责人" prop="owner">
                <el-input v-model="form.owner" placeholder="请输入负责人" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="描述" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入描述"
            />
          </el-form-item>
          
          <el-form-item label="标签">
            <div class="tags-container">
              <div 
                v-for="(tag, index) in form.tags" 
                :key="index" 
                class="tag-item"
              >
                <el-input v-model="tag.tagKey" placeholder="标签键" />
                <el-input v-model="tag.tagValue" placeholder="标签值" />
                <el-button @click="removeTag(index)" type="danger" text circle>
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <el-button @click="addTag" type="primary" plain>添加标签</el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="loading">
              {{ isEdit ? '更新' : '创建' }}
            </el-button>
            <el-button @click="goBack">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { createAsset, updateAsset, getAssetById } from '@/api/asset'
import type { Asset, AssetTag } from '@/types/asset'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 判断是否是编辑模式
const isEdit = computed(() => {
  return route.params.id !== undefined
})

// 资产类型选项
const typeOptions = [
  { value: 'server', label: '服务器' },
  { value: 'database', label: '数据库' },
  { value: 'middleware', label: '中间件' },
  { value: 'application', label: '应用系统' }
]

// 表单数据
const form = reactive<Asset>({
  name: '',
  code: '',
  type: '',
  ipAddress: '',
  port: undefined,
  department: '',
  owner: '',
  status: 1,
  description: '',
  tags: []
})

// 表单校验规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入资产名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入资产编码', trigger: 'blur' },
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择资产类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  ipAddress: [
    { pattern: /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/, message: 'IP地址格式不正确', trigger: 'blur' }
  ],
  port: [
    { type: 'number', min: 1, max: 65535, message: '端口范围为1-65535', trigger: 'blur' }
  ]
})

// 添加标签
const addTag = () => {
  if (!form.tags) {
    form.tags = []
  }
  form.tags.push({ tagKey: '', tagValue: '' })
}

// 移除标签
const removeTag = (index: number) => {
  if (form.tags) {
    form.tags.splice(index, 1)
  }
}

// 获取资产详情
const getDetail = async (id: number) => {
  loading.value = true
  try {
    const res = await getAssetById(id)
    Object.assign(form, res.data)
    if (!form.tags) {
      form.tags = []
    }
  } catch (error) {
    console.error('获取资产详情失败', error)
    ElMessage.error('获取资产详情失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  // 过滤掉空的标签
  if (form.tags && form.tags.length > 0) {
    form.tags = form.tags.filter(tag => tag.tagKey && tag.tagValue)
  }
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    if (isEdit.value) {
      await updateAsset(Number(route.params.id), form)
      ElMessage.success('更新成功')
    } else {
      await createAsset(form)
      ElMessage.success('创建成功')
    }
    
    // 返回列表页
    goBack()
  } catch (error) {
    console.error('提交失败', error)
    loading.value = false
  }
}

// 返回列表页
const goBack = () => {
  router.push('/asset/list')
}

onMounted(() => {
  if (isEdit.value && route.params.id) {
    getDetail(Number(route.params.id))
  }
  
  // 确保标签数组已初始化
  if (!form.tags || !Array.isArray(form.tags)) {
    form.tags = []
  }
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  
  .form-container {
    padding: 20px;
  }
  
  .tags-container {
    display: flex;
    flex-direction: column;
    gap: 10px;
    
    .tag-item {
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }
}
</style> 