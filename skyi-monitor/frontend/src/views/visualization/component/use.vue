<template>
  <div class="component-use">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-page-header @back="goBack" :content="component ? '使用 ' + component.name : '使用组件'" />
        </div>
      </template>
      
      <div v-if="component" class="use-content">
        <el-form :model="formData" label-width="120px">
          <el-form-item label="组件名称">
            <span>{{ component.name }}</span>
          </el-form-item>
          
          <el-form-item label="组件类型">
            <el-tag size="small">{{ component.type }}</el-tag>
          </el-form-item>
          
          <el-form-item label="添加至仪表盘">
            <el-select v-model="formData.dashboardId" placeholder="选择仪表盘" style="width: 100%">
              <el-option
                v-for="dashboard in dashboards"
                :key="dashboard.id"
                :label="dashboard.name"
                :value="dashboard.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="组件标题">
            <el-input v-model="formData.title" placeholder="请输入组件标题" />
          </el-form-item>
          
          <el-form-item label="数据源配置">
            <el-select v-model="formData.dataSource" placeholder="选择数据源" style="width: 100%">
              <el-option label="InfluxDB" value="influxdb" />
              <el-option label="MySQL" value="mysql" />
              <el-option label="模拟数据" value="mock" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="数据刷新间隔">
            <el-input-number v-model="formData.refreshInterval" :min="5" :max="3600" :step="5" />
            <span class="input-suffix">秒</span>
          </el-form-item>
          
          <el-form-item label="指标选择">
            <el-select v-model="formData.metrics" multiple placeholder="选择要展示的指标" style="width: 100%">
              <el-option
                v-for="metric in metrics"
                :key="metric.name"
                :label="metric.name"
                :value="metric.name"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="展示样式">
            <el-radio-group v-model="formData.style">
              <el-radio value="default">默认样式</el-radio>
              <el-radio value="dark">暗色主题</el-radio>
              <el-radio value="light">亮色主题</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="尺寸设置">
            <div class="size-inputs">
              <el-input-number v-model="formData.width" :min="1" :max="12" :step="1" />
              <span class="size-divider">x</span>
              <el-input-number v-model="formData.height" :min="1" :max="12" :step="1" />
            </div>
            <div class="size-hint">（宽 x 高，单位为网格）</div>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSubmit">添加到仪表盘</el-button>
            <el-button @click="goBack">返回</el-button>
          </el-form-item>
        </el-form>
        
        <div class="preview-container">
          <h3>预览效果</h3>
          <div class="preview-box" :class="'style-' + formData.style">
            <div class="preview-header">
              <span>{{ formData.title || '未命名组件' }}</span>
              <el-dropdown>
                <span class="el-dropdown-link">
                  <i class="el-icon-more"></i>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>刷新数据</el-dropdown-item>
                    <el-dropdown-item>编辑配置</el-dropdown-item>
                    <el-dropdown-item>删除组件</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div class="preview-body">
              <img :src="component.preview" :alt="component.name" />
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="loading">
        <el-empty description="组件不存在或正在加载中" />
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

interface Component {
  id: number;
  name: string;
  type: string;
  description: string;
  preview?: string;
}

interface Dashboard {
  id: number;
  name: string;
}

interface Metric {
  name: string;
  unit: string;
}

const component = ref<Component | null>(null);

// 表单数据
const formData = reactive({
  dashboardId: null as number | null,
  title: '',
  dataSource: 'mock',
  refreshInterval: 60,
  metrics: [] as string[],
  style: 'default',
  width: 6,
  height: 4
});

// 仪表盘列表
const dashboards = [
  { id: 1, name: '系统监控仪表盘' },
  { id: 2, name: '网络状态仪表盘' },
  { id: 3, name: '应用性能仪表盘' }
];

// 指标列表
const metrics = [
  { name: 'cpu.usage', unit: '%' },
  { name: 'memory.usage', unit: '%' },
  { name: 'disk.usage', unit: '%' },
  { name: 'network.in', unit: 'KB/s' },
  { name: 'network.out', unit: 'KB/s' }
];

// 示例组件数据
const componentsData: Component[] = [
  {
    id: 1,
    name: '折线图',
    type: '图表',
    description: '用于展示连续数据变化趋势',
    preview: '/assets/previews/line-chart.png'
  },
  {
    id: 2,
    name: '柱状图',
    type: '图表',
    description: '用于比较多组数据的大小',
    preview: '/assets/previews/bar-chart.png'
  },
  {
    id: 3,
    name: '饼图',
    type: '图表',
    description: '用于显示部分占整体的比例',
    preview: '/assets/previews/pie-chart.png'
  },
  {
    id: 4,
    name: '表格',
    type: '数据展示',
    description: '用于展示结构化数据',
    preview: '/assets/previews/table.png'
  },
  {
    id: 5,
    name: '仪表盘',
    type: '指标展示',
    description: '用于展示关键指标的实时状态',
    preview: '/assets/previews/gauge.png'
  },
  {
    id: 6,
    name: '热力图',
    type: '图表',
    description: '用于展示数据密度和分布',
    preview: '/assets/previews/heatmap.png'
  }
];

// 获取组件详情
const fetchComponentDetail = (id: number) => {
  // 实际应用中应该从API获取数据
  const found = componentsData.find(c => c.id === id);
  if (found) {
    component.value = found;
    // 设置默认标题
    formData.title = found.name;
  }
};

// 返回上一页
const goBack = () => {
  router.go(-1);
};

// 提交表单
const handleSubmit = () => {
  if (!formData.dashboardId) {
    // 显示错误提示
    return;
  }
  
  // 构建要提交的数据
  const submitData = {
    ...formData,
    componentId: component.value?.id,
    componentType: component.value?.type
  };
  
  console.log('提交数据:', submitData);
  
  // 实际应用中应该调用API
  // 模拟提交成功
  setTimeout(() => {
    router.push({
      name: 'DashboardEdit',
      params: { id: formData.dashboardId }
    });
  }, 500);
};

onMounted(() => {
  const id = parseInt(route.params.id as string);
  if (!isNaN(id)) {
    fetchComponentDetail(id);
  }
});
</script>

<style lang="scss" scoped>
.component-use {
  padding: 20px;
  
  .card-header {
    display: flex;
    align-items: center;
  }
  
  .use-content {
    display: flex;
    gap: 30px;
    
    .el-form {
      flex: 1;
    }
    
    .input-suffix {
      margin-left: 10px;
      color: #606266;
    }
    
    .size-inputs {
      display: flex;
      align-items: center;
      
      .size-divider {
        margin: 0 10px;
        color: #606266;
      }
    }
    
    .size-hint {
      font-size: 12px;
      color: #909399;
      margin-top: 5px;
    }
    
    .preview-container {
      flex: 1;
      
      h3 {
        margin-top: 0;
        margin-bottom: 15px;
        font-size: 16px;
        font-weight: 500;
      }
      
      .preview-box {
        border: 1px solid #EBEEF5;
        border-radius: 4px;
        overflow: hidden;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        
        &.style-dark {
          background-color: #303133;
          color: #E4E7ED;
          
          .preview-header {
            background-color: #1E1E1E;
            border-bottom: 1px solid #3E3E3E;
          }
        }
        
        &.style-light {
          background-color: #F5F7FA;
          
          .preview-header {
            background-color: #FFFFFF;
          }
        }
        
        .preview-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 10px 15px;
          background-color: #FFFFFF;
          border-bottom: 1px solid #EBEEF5;
        }
        
        .preview-body {
          padding: 15px;
          display: flex;
          justify-content: center;
          align-items: center;
          min-height: 300px;
          
          img {
            max-width: 100%;
            max-height: 270px;
          }
        }
      }
    }
  }
  
  .loading {
    padding: 40px 0;
  }
}

@media (max-width: 1200px) {
  .component-use .use-content {
    flex-direction: column;
  }
}
</style> 