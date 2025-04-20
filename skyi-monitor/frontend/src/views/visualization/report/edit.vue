<template>
  <div class="report-edit">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑报表' : '创建报表' }}</h1>
      <div class="actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </div>
    
    <el-form :model="reportForm" :rules="rules" ref="reportFormRef" label-width="100px">
      <el-card class="form-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        
        <el-form-item label="报表名称" prop="name">
          <el-input v-model="reportForm.name" placeholder="请输入报表名称" />
        </el-form-item>
        
        <el-form-item label="分类" prop="category">
          <el-select v-model="reportForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="系统报表" value="SYSTEM" />
            <el-option label="资源报表" value="RESOURCE" />
            <el-option label="性能报表" value="PERFORMANCE" />
            <el-option label="业务报表" value="BUSINESS" />
            <el-option label="自定义报表" value="CUSTOM" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input v-model="reportForm.description" type="textarea" :rows="3" placeholder="请输入报表描述" />
        </el-form-item>
      </el-card>
      
      <el-card class="form-card">
        <template #header>
          <div class="card-header">
            <span>数据源配置</span>
          </div>
        </template>
        
        <el-form-item label="数据源类型" prop="dataSourceType">
          <el-radio-group v-model="reportForm.dataSourceType">
            <el-radio value="INFLUXDB">时序数据库</el-radio>
            <el-radio value="MYSQL">MySQL数据库</el-radio>
            <el-radio value="API">API接口</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 时序数据库配置 -->
        <template v-if="reportForm.dataSourceType === 'INFLUXDB'">
          <el-form-item label="指标选择" prop="metrics">
            <el-select
              v-model="reportForm.metrics"
              multiple
              filterable
              placeholder="请选择指标"
              style="width: 100%"
            >
              <el-option
                v-for="metric in availableMetrics"
                :key="metric.name"
                :label="metric.label"
                :value="metric.name"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="时间范围" prop="timeRange">
            <el-select v-model="reportForm.timeRange" placeholder="请选择时间范围" style="width: 100%">
              <el-option label="最近1小时" value="1h" />
              <el-option label="最近6小时" value="6h" />
              <el-option label="最近12小时" value="12h" />
              <el-option label="最近24小时" value="1d" />
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
              <el-option label="自定义" value="custom" />
            </el-select>
          </el-form-item>
          
          <el-form-item v-if="reportForm.timeRange === 'custom'" label="自定义范围">
            <el-date-picker
              v-model="reportForm.customTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 100%"
            />
          </el-form-item>
        </template>
        
        <!-- MySQL数据库配置 -->
        <template v-if="reportForm.dataSourceType === 'MYSQL'">
          <el-form-item label="SQL查询" prop="sqlQuery">
            <el-input
              v-model="reportForm.sqlQuery"
              type="textarea"
              :rows="5"
              placeholder="请输入SQL查询语句"
            />
          </el-form-item>
        </template>
        
        <!-- API接口配置 -->
        <template v-if="reportForm.dataSourceType === 'API'">
          <el-form-item label="API地址" prop="apiUrl">
            <el-input v-model="reportForm.apiUrl" placeholder="请输入API地址" />
          </el-form-item>
          
          <el-form-item label="请求方法" prop="apiMethod">
            <el-select v-model="reportForm.apiMethod" placeholder="请选择请求方法" style="width: 100%">
              <el-option label="GET" value="GET" />
              <el-option label="POST" value="POST" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="请求参数" prop="apiParams">
            <el-input
              v-model="reportForm.apiParams"
              type="textarea"
              :rows="3"
              placeholder="请输入JSON格式的请求参数"
            />
          </el-form-item>
        </template>
      </el-card>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { FormInstance } from 'element-plus';

const route = useRoute();
const router = useRouter();
const reportFormRef = ref<FormInstance>();

// 判断是编辑还是创建
const isEdit = computed(() => route.params.id !== undefined);

// 表单数据
const reportForm = reactive({
  id: undefined as number | undefined,
  name: '',
  category: '',
  description: '',
  dataSourceType: 'INFLUXDB',
  metrics: [] as string[],
  timeRange: '24h',
  customTimeRange: [] as Date[],
  sqlQuery: '',
  apiUrl: '',
  apiMethod: 'GET',
  apiParams: ''
});

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入报表名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度应为2到50个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  metrics: [
    { type: 'array', required: true, message: '请至少选择一个指标', trigger: 'change' }
  ],
  timeRange: [
    { required: true, message: '请选择时间范围', trigger: 'change' }
  ],
  sqlQuery: [
    { required: true, message: '请输入SQL查询语句', trigger: 'blur' }
  ],
  apiUrl: [
    { required: true, message: '请输入API地址', trigger: 'blur' }
  ],
  apiMethod: [
    { required: true, message: '请选择请求方法', trigger: 'change' }
  ]
};

// 可用指标列表
const availableMetrics = [
  { name: 'cpu.usage', label: 'CPU使用率' },
  { name: 'memory.usage', label: '内存使用率' },
  { name: 'disk.usage', label: '磁盘使用率' },
  { name: 'network.in', label: '网络流入' },
  { name: 'network.out', label: '网络流出' },
  { name: 'system.load', label: '系统负载' },
  { name: 'application.response_time', label: '应用响应时间' },
  { name: 'application.request_count', label: '应用请求数' },
  { name: 'application.error_rate', label: '应用错误率' },
  { name: 'database.connections', label: '数据库连接数' },
  { name: 'database.queries', label: '数据库查询数' }
];

// 初始化
onMounted(() => {
  if (isEdit.value) {
    loadReportData();
  }
});

// 加载报表数据（编辑模式）
const loadReportData = () => {
  const id = route.params.id;
  console.log('加载报表数据，ID:', id);
  
  // 模拟获取数据
  // 实际应用中应该从API获取
  if (id === '1') {
    Object.assign(reportForm, {
      id: 1,
      name: '系统性能概览',
      category: 'SYSTEM',
      description: '显示系统CPU、内存、磁盘和网络等关键性能指标',
      dataSourceType: 'INFLUXDB',
      metrics: ['cpu.usage', 'memory.usage', 'disk.usage'],
      timeRange: '24h'
    });
  }
};

// 取消
const handleCancel = () => {
  router.back();
};

// 保存
const handleSave = async () => {
  if (!reportFormRef.value) return;
  
  await reportFormRef.value.validate((valid, fields) => {
    if (!valid) {
      console.error('表单验证失败:', fields);
      return;
    }
    
    console.log('保存报表:', reportForm);
    
    // 模拟保存操作
    // 实际应用中应该调用API保存
    setTimeout(() => {
      router.push('/visualization/reports');
    }, 1000);
  });
};
</script> 