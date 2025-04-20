<template>
  <div class="component-library">
    <h1>组件库</h1>
    <el-row :gutter="20">
      <el-col :span="8" v-for="(component, index) in components" :key="index">
        <el-card class="component-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ component.name }}</span>
              <el-tag size="small">{{ component.type }}</el-tag>
            </div>
          </template>
          <div class="component-content">
            <p>{{ component.description }}</p>
            <div class="component-preview" v-if="component.preview">
              <img :src="component.preview" alt="组件预览" />
            </div>
            <div class="component-actions">
              <el-button type="primary" size="small" @click="handleUseComponent(component)">使用</el-button>
              <el-button size="small" @click="handleViewDetail(component)">详情</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

interface Component {
  id: number;
  name: string;
  type: string;
  description: string;
  preview?: string;
}

// 示例组件数据
const components = ref<Component[]>([
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
]);

const router = useRouter();

// 使用组件
const handleUseComponent = (component: Component) => {
  console.log('使用组件:', component);
  // 跳转到组件使用页面
  router.push({
    name: 'ComponentUse',
    params: { id: component.id.toString() }
  });
};

// 查看组件详情
const handleViewDetail = (component: Component) => {
  console.log('查看详情:', component);
  // 跳转到组件详情页面
  router.push({
    name: 'ComponentDetail',
    params: { id: component.id.toString() }
  });
};
</script>

<style lang="scss" scoped>
.component-library {
  padding: 20px;
  
  h1 {
    margin-bottom: 20px;
  }
  
  .component-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .component-content {
      .component-preview {
        margin: 10px 0;
        text-align: center;
        
        img {
          max-width: 100%;
          height: auto;
          border-radius: 4px;
        }
      }
      
      .component-actions {
        margin-top: 15px;
        text-align: right;
      }
    }
  }
}
</style> 