<template>
  <div class="component-detail">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-page-header @back="goBack" :content="component ? component.name + '详情' : '组件详情'" />
        </div>
      </template>
      
      <div v-if="component" class="detail-content">
        <div class="basic-info">
          <h2>{{ component.name }} <el-tag size="small">{{ component.type }}</el-tag></h2>
          <p class="description">{{ component.description }}</p>
        </div>
        
        <div class="preview">
          <h3>组件预览</h3>
          <div class="preview-image">
            <img :src="component.preview" :alt="component.name" />
          </div>
        </div>
        
        <div class="usage-info">
          <h3>使用说明</h3>
          <p>该组件用于数据可视化展示，可以添加到仪表盘中作为展示组件。</p>
          
          <h3>配置选项</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="数据源配置">支持InfluxDB、MySQL等多种数据源</el-descriptions-item>
            <el-descriptions-item label="刷新间隔">支持自定义刷新间隔，默认为60秒</el-descriptions-item>
            <el-descriptions-item label="样式配置">支持自定义颜色、字体大小等样式</el-descriptions-item>
            <el-descriptions-item label="交互功能">支持点击、悬停等交互事件</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="action-buttons">
          <el-button type="primary" @click="handleUseComponent">使用此组件</el-button>
          <el-button @click="goBack">返回</el-button>
        </div>
      </div>
      
      <div v-else class="loading">
        <el-empty description="组件不存在或正在加载中" />
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
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

const component = ref<Component | null>(null);

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
  // 这里使用模拟数据
  const found = componentsData.find(c => c.id === id);
  if (found) {
    component.value = found;
  }
};

// 返回上一页
const goBack = () => {
  router.go(-1);
};

// 使用组件
const handleUseComponent = () => {
  if (component.value) {
    router.push({
      name: 'ComponentUse',
      params: { id: component.value.id }
    });
  }
};

onMounted(() => {
  const id = parseInt(route.params.id as string);
  if (!isNaN(id)) {
    fetchComponentDetail(id);
  }
});
</script>

<style lang="scss" scoped>
.component-detail {
  padding: 20px;
  
  .card-header {
    display: flex;
    align-items: center;
  }
  
  .detail-content {
    .basic-info {
      margin-bottom: 20px;
      
      h2 {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 10px;
      }
      
      .description {
        color: #666;
        line-height: 1.5;
      }
    }
    
    .preview {
      margin-bottom: 30px;
      
      h3 {
        margin-bottom: 15px;
        font-size: 18px;
        font-weight: 500;
      }
      
      .preview-image {
        text-align: center;
        
        img {
          max-width: 100%;
          max-height: 300px;
          border-radius: 4px;
          border: 1px solid #ebeef5;
        }
      }
    }
    
    .usage-info {
      margin-bottom: 30px;
      
      h3 {
        margin: 20px 0 15px;
        font-size: 18px;
        font-weight: 500;
      }
      
      p {
        color: #666;
        line-height: 1.5;
      }
    }
    
    .action-buttons {
      margin-top: 30px;
      text-align: center;
    }
  }
  
  .loading {
    padding: 40px 0;
  }
}
</style> 