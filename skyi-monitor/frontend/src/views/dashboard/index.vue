<template>
  <div class="dashboard-container">
    <el-row :gutter="16">
      <el-col :span="24">
        <div class="dashboard-header">
          <h2 class="dashboard-title">系统监控仪表盘</h2>
          <div class="dashboard-actions">
            <el-input
              v-model="dashboardConfig.name"
              placeholder="仪表盘名称"
              style="width: 200px"
              class="dashboard-name-input"
            />
            <el-dropdown @command="loadDashboardConfig">
              <el-button type="primary" size="default">
                加载仪表盘<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    v-for="dashboard in savedDashboards" 
                    :key="dashboard.name"
                    :command="dashboard"
                  >
                    {{ dashboard.name }}
                  </el-dropdown-item>
                  <el-dropdown-item v-if="savedDashboards.length === 0" disabled>
                    暂无保存的仪表盘
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="success" @click="saveDashboardConfig">保存仪表盘</el-button>
            <el-button size="default" @click="showConfig = !showConfig">
              {{ showConfig ? '隐藏配置' : '显示配置' }}
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 资产概览部分 -->
    <el-row :gutter="16">
      <el-col :span="18">
        <el-card class="dashboard-card" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span>资产概览</span>
              <el-button-group>
                <el-button size="small" @click="refreshData">刷新</el-button>
              </el-button-group>
            </div>
          </template>
          
          <el-row :gutter="16">
            <el-col :span="6" v-for="stat in assetStats" :key="stat.title">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-title">{{ stat.title }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      
      <!-- 告警统计组件 -->
      <el-col :span="6">
        <dashboard-alert-stats />
      </el-col>
    </el-row>
    
    <!-- 动态图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <el-col 
        :xs="24" 
        :sm="24" 
        :md="chartSize === 'small' ? 8 : (chartSize === 'medium' ? 12 : 24)" 
        :lg="chartSize === 'small' ? 6 : (chartSize === 'medium' ? 8 : 12)" 
        :xl="chartSize === 'small' ? 4 : (chartSize === 'medium' ? 6 : 8)"
        v-for="chart in charts" 
        :key="chart.id"
      >
        <el-card class="chart-card" :body-style="{ padding: '0px' }" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span>{{ chart.title }}</span>
              <el-dropdown trigger="click" @command="handleChartCommand($event, chart)">
                <el-button size="small">
                  操作<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="refresh">刷新</el-dropdown-item>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="remove">移除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          
          <!-- 动态加载图表组件 -->
          <div class="chart-wrapper">
            <dynamic-chart-loader
              :type="chart.type"
              :metric-type="chart.metricType"
              :data="chart.data"
              :options="chart.options"
              @load-error="handleChartError($event, chart)"
            />
          </div>
        </el-card>
      </el-col>
      
      <!-- 添加图表按钮 -->
      <el-col 
        :xs="24" 
        :sm="24" 
        :md="chartSize === 'small' ? 8 : (chartSize === 'medium' ? 12 : 24)" 
        :lg="chartSize === 'small' ? 6 : (chartSize === 'medium' ? 8 : 12)" 
        :xl="chartSize === 'small' ? 4 : (chartSize === 'medium' ? 6 : 8)"
      >
        <div class="add-chart-placeholder" @click="showAddChartDialog">
          <el-icon class="add-icon"><plus /></el-icon>
          <span>添加图表</span>
        </div>
      </el-col>
    </el-row>
    
    <!-- 配置面板 -->
    <el-drawer
      v-model="showConfig"
      title="仪表盘配置"
      direction="rtl"
      size="30%"
    >
      <el-form label-position="top">
        <el-form-item label="仪表盘名称">
          <el-input v-model="dashboardConfig.name" placeholder="输入仪表盘名称" />
        </el-form-item>
        
        <el-form-item label="刷新间隔(秒)">
          <el-slider v-model="refreshInterval" :min="10" :max="300" :step="10" show-input />
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-select v-model="timeRange" style="width: 100%">
            <el-option label="最近30分钟" value="30m" />
            <el-option label="最近1小时" value="1h" />
            <el-option label="最近6小时" value="6h" />
            <el-option label="最近12小时" value="12h" />
            <el-option label="最近24小时" value="24h" />
            <el-option label="最近7天" value="7d" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="显示资产">
          <el-select v-model="selectedAssets" multiple style="width: 100%">
            <el-option
              v-for="asset in assets"
              :key="asset.id"
              :label="asset.name"
              :value="asset.id"
            />
          </el-select>
        </el-form-item>
        
        <el-divider>布局设置</el-divider>
        
        <el-form-item label="图表大小">
          <el-radio-group v-model="chartSize">
            <el-radio-button value="small">小</el-radio-button>
            <el-radio-button value="medium">中</el-radio-button>
            <el-radio-button value="large">大</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-divider>已保存仪表盘</el-divider>
        
        <el-table :data="savedDashboards" style="width: 100%" size="small">
          <el-table-column prop="name" label="仪表盘名称" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button 
                size="small" 
                type="text" 
                @click="loadDashboardConfig(scope.row)"
              >
                加载
              </el-button>
              <el-button 
                size="small" 
                type="text" 
                @click="deleteDashboardConfig(scope.row.name)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
    </el-drawer>
    
    <!-- 添加图表对话框 -->
    <el-dialog v-model="addChartDialogVisible" title="添加图表" width="50%">
      <el-form :model="newChartForm" label-position="top">
        <el-form-item label="图表标题" required>
          <el-input v-model="newChartForm.title" placeholder="请输入图表标题" />
        </el-form-item>
        
        <el-form-item label="图表类型" required>
          <el-select v-model="newChartForm.type" style="width: 100%">
            <el-option label="折线图" value="line" />
            <el-option label="柱状图" value="bar" />
            <el-option label="饼图" value="pie" />
            <el-option label="仪表盘" value="gauge" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="指标类型">
          <el-select v-model="newChartForm.metricType" style="width: 100%">
            <el-option label="CPU" value="cpu" />
            <el-option label="内存" value="memory" />
            <el-option label="磁盘" value="disk" />
            <el-option label="网络" value="network" />
            <el-option label="IO" value="io" />
            <el-option label="默认" value="default" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="数据源">
          <el-select v-model="newChartForm.assetId" style="width: 100%">
            <el-option
              v-for="asset in assets"
              :key="asset.id"
              :label="asset.name"
              :value="asset.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addChartDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddChart">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue';
import DynamicChartLoader from '@/components/charts/DynamicChartLoader.vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { getMetricsByAssetType } from '@/api/metrics';
import { listAssets } from '@/api/asset';
import DashboardAlertStats from '@/components/dashboard/AlertStats.vue';

// 定义图表类型接口
interface ChartData {
  id: number;
  title: string;
  type: string;
  metricType: string;
  assetId?: number | null;
  data: any[];
  options: Record<string, any>;
}

// 资产统计数据
const assetStats = reactive([
  { title: '服务器', value: 12 },
  { title: '数据库', value: 5 },
  { title: '中间件', value: 8 },
  { title: '应用', value: 15 }
]);

// 图表数据
const charts = ref<ChartData[]>([
  {
    id: 1,
    title: 'CPU使用率',
    type: 'line',
    metricType: 'cpu',
    assetId: null,
    data: generateRandomTimeData(20, 0, 100),
    options: {}
  },
  {
    id: 2,
    title: '内存使用情况',
    type: 'line',
    metricType: 'memory',
    assetId: null,
    data: generateRandomTimeData(20, 0, 16),
    options: {}
  },
  {
    id: 3,
    title: '磁盘空间',
    type: 'pie',
    metricType: 'disk',
    assetId: null,
    data: [
      { name: '已用', value: 450 },
      { name: '可用', value: 550 }
    ],
    options: {}
  }
]);

// 资产列表
const assets = ref([
  { id: 1, name: '应用服务器-01', type: 'server' },
  { id: 2, name: '应用服务器-02', type: 'server' },
  { id: 3, name: 'MySQL主库', type: 'database' },
  { id: 4, name: 'Redis集群', type: 'middleware' }
]);

// 配置相关
const showConfig = ref(false);
const refreshInterval = ref(60);
const timeRange = ref('1h');
const selectedAssets = ref([1, 2]);
const chartSize = ref('medium');

// 仪表盘配置
const dashboardConfig = ref({
  name: '默认仪表盘',
  layout: 'grid',
  charts: [] as ChartData[]
});

// 已保存的仪表盘配置列表
const savedDashboards = ref<any[]>([]);

// 添加图表相关
const addChartDialogVisible = ref(false);
const newChartForm = reactive({
  title: '',
  type: 'line',
  metricType: 'default',
  assetId: null as number | null
});

// 定时刷新任务
let refreshTimer: number | null = null;

// 初始化加载数据
const loading = ref(false);

// 随机生成时间序列数据
function generateRandomTimeData(count: number, min: number, max: number) {
  const now = new Date();
  const data = [];
  
  for (let i = 0; i < count; i++) {
    const timestamp = new Date(now.getTime() - (count - i) * 60000);
    const value = parseFloat((Math.random() * (max - min) + min).toFixed(2));
    
    data.push({
      timestamp: timestamp.toLocaleString(),
      value
    });
  }
  
  return data;
}

// 刷新数据
async function refreshData() {
  loading.value = true;
  try {
    // 获取资产统计信息
    await fetchAssetStats();
    
    // 刷新图表数据
    await Promise.all(charts.value.map(async (chart) => {
      if (chart.assetId && chart.metricType) {
        await fetchMetricData(chart);
      } else {
        // 对于没有关联资产或指标类型的图表，继续使用模拟数据
        if (chart.type === 'line' || chart.type === 'bar') {
          chart.data = generateRandomTimeData(20, 0, 100);
        }
      }
    }));
    
    ElMessage.success('数据已刷新');
  } catch (error) {
    console.error('刷新数据失败:', error);
    ElMessage.error('刷新数据失败');
  } finally {
    loading.value = false;
  }
}

// 获取资产统计信息
async function fetchAssetStats() {
  try {
    const res = await listAssets({ page: 1, size: 1000 });
    if (res.data) {
      const assetData = res.data.content || [];
      
      // 按类型统计资产数量
      const stats = {
        server: assetData.filter((a: any) => a.type === 'server').length,
        database: assetData.filter((a: any) => a.type === 'database').length,
        middleware: assetData.filter((a: any) => ['middleware', 'redis', 'kafka', 'rabbitmq'].includes(a.type)).length,
        application: assetData.filter((a: any) => a.type === 'application').length
      };
      
      // 更新统计数据
      assetStats[0].value = stats.server;
      assetStats[1].value = stats.database;
      assetStats[2].value = stats.middleware;
      assetStats[3].value = stats.application;
      
      // 更新资产列表
      assets.value = assetData.map((item: any) => ({
        id: item.id,
        name: item.name,
        type: item.type
      }));
    }
  } catch (error) {
    console.error('获取资产统计信息失败:', error);
  }
}

// 获取特定图表的指标数据
async function fetchMetricData(chart: ChartData) {
  if (!chart.assetId || !chart.metricType) return;
  
  try {
    // 根据指标类型构建查询参数
    const params = {
      assetId: chart.assetId,
      metricType: chart.metricType,
      timeRange: timeRange.value
    };
    
    // 这里应该调用实际的API获取数据
    // 由于没有完整的API实现，使用模拟数据
    if (chart.type === 'line' || chart.type === 'bar') {
      chart.data = generateRandomTimeData(20, 0, 100);
    }
  } catch (error) {
    console.error(`获取图表[${chart.title}]数据失败:`, error);
  }
}

// 图表操作处理
function handleChartCommand(command: string, chart: ChartData) {
  switch (command) {
    case 'refresh':
      if (chart.type === 'line' || chart.type === 'bar') {
        chart.data = generateRandomTimeData(20, 0, 100);
      }
      ElMessage.success(`已刷新: ${chart.title}`);
      break;
    case 'edit':
      ElMessage.info('编辑功能开发中');
      break;
    case 'remove':
      charts.value = charts.value.filter(item => item.id !== chart.id);
      ElMessage.success(`已移除: ${chart.title}`);
      break;
  }
}

// 处理图表加载错误
function handleChartError(event: any, chart: ChartData) {
  console.error('Chart loading error:', event, chart);
  ElMessage.error(`图表加载失败: ${chart.title}`);
}

// 显示添加图表对话框
function showAddChartDialog() {
  addChartDialogVisible.value = true;
  Object.assign(newChartForm, {
    title: '',
    type: 'line',
    metricType: 'default',
    assetId: null
  });
}

// 处理添加图表
function handleAddChart() {
  if (!newChartForm.title) {
    ElMessage.warning('请输入图表标题');
    return;
  }
  
  const newChart: ChartData = {
    id: Date.now(),
    title: newChartForm.title,
    type: newChartForm.type,
    metricType: newChartForm.metricType,
    assetId: newChartForm.assetId,
    data: newChartForm.type === 'pie' ? [
      { name: '类别A', value: 35 },
      { name: '类别B', value: 25 },
      { name: '类别C', value: 40 }
    ] : generateRandomTimeData(20, 0, 100),
    options: {}
  };
  
  charts.value.push(newChart);
  addChartDialogVisible.value = false;
  
  // 自动保存当前配置
  saveDashboardConfig();
  
  ElMessage.success('图表已添加');
}

// 保存仪表盘配置
function saveDashboardConfig() {
  // 构建仪表盘配置对象
  const config = {
    name: dashboardConfig.value.name,
    layout: dashboardConfig.value.layout,
    refreshInterval: refreshInterval.value,
    timeRange: timeRange.value,
    selectedAssets: selectedAssets.value,
    chartSize: chartSize.value,
    charts: charts.value.map(chart => ({
      id: chart.id,
      title: chart.title,
      type: chart.type,
      metricType: chart.metricType,
      assetId: chart.assetId,
      options: chart.options
    }))
  };
  
  // 保存到 localStorage
  const dashboards = JSON.parse(localStorage.getItem('dashboards') || '[]');
  
  // 查找是否已有同名仪表盘
  const existingIndex = dashboards.findIndex((d: any) => d.name === config.name);
  if (existingIndex >= 0) {
    dashboards[existingIndex] = config;
  } else {
    dashboards.push(config);
  }
  
  localStorage.setItem('dashboards', JSON.stringify(dashboards));
  savedDashboards.value = dashboards;
  
  ElMessage.success('仪表盘配置已保存');
}

// 加载仪表盘配置
function loadDashboardConfig(config: any) {
  if (!config) return;
  
  dashboardConfig.value.name = config.name;
  dashboardConfig.value.layout = config.layout || 'grid';
  
  // 加载配置项
  refreshInterval.value = config.refreshInterval || 60;
  timeRange.value = config.timeRange || '1h';
  selectedAssets.value = config.selectedAssets || [];
  chartSize.value = config.chartSize || 'medium';
  
  // 加载图表
  charts.value = config.charts.map((chart: any) => ({
    ...chart,
    data: chart.type === 'pie' ? [
      { name: '类别A', value: 35 },
      { name: '类别B', value: 25 },
      { name: '类别C', value: 40 }
    ] : generateRandomTimeData(20, 0, 100)
  }));
  
  // 刷新数据
  refreshData();
  
  // 重新设置刷新定时器
  setupRefreshTimer();
  
  ElMessage.success(`已加载仪表盘: ${config.name}`);
}

// 加载已保存的仪表盘配置
function loadSavedDashboards() {
  const dashboards = JSON.parse(localStorage.getItem('dashboards') || '[]');
  savedDashboards.value = dashboards;
  
  // 如果有保存的仪表盘，默认加载第一个
  if (dashboards.length > 0) {
    loadDashboardConfig(dashboards[0]);
  }
}

// 删除保存的仪表盘配置
function deleteDashboardConfig(name: string) {
  const dashboards = JSON.parse(localStorage.getItem('dashboards') || '[]');
  const newDashboards = dashboards.filter((d: any) => d.name !== name);
  localStorage.setItem('dashboards', JSON.stringify(newDashboards));
  savedDashboards.value = newDashboards;
  
  ElMessage.success(`已删除仪表盘: ${name}`);
}

// 设置定时刷新
function setupRefreshTimer() {
  clearRefreshTimer();
  refreshTimer = window.setInterval(() => {
    refreshData();
  }, refreshInterval.value * 1000);
}

// 监听刷新间隔变化
watch(refreshInterval, (newValue) => {
  setupRefreshTimer();
});

// 清除定时刷新
function clearRefreshTimer() {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
}

// 生命周期钩子
onMounted(() => {
  // 加载已保存的仪表盘配置
  loadSavedDashboards();
  
  // 获取资产列表和统计信息
  fetchAssetStats();
  
  // 设置定时刷新
  setupRefreshTimer();
});

onBeforeUnmount(() => {
  clearRefreshTimer();
});
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-title {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

.dashboard-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card {
  text-align: center;
  margin-bottom: 16px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-title {
  font-size: 14px;
  color: #606266;
}

.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  margin-bottom: 16px;
  height: 350px;
}

.chart-wrapper {
  height: 280px;
  padding: 16px;
}

.add-chart-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 350px;
  background-color: #f5f7fa;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 16px;
}

.add-chart-placeholder:hover {
  background-color: #e6f1fc;
  border-color: #409EFF;
}

.add-icon {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

.add-chart-placeholder span {
  color: #606266;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.dashboard-name-input {
  margin-right: 10px;
}
</style> 