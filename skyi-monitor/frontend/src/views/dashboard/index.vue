<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">资产总数</div>
            <div class="stat-value">{{ stats.totalAssets }}</div>
            <div class="stat-footer">
              <el-tag type="success">今日新增: {{ stats.todayAddedAssets }}</el-tag>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">服务器</div>
            <div class="stat-value">{{ stats.serverCount }}</div>
            <div class="stat-footer">
              <el-progress :percentage="stats.serverAvailability" :format="percentFormat" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">数据库</div>
            <div class="stat-value">{{ stats.databaseCount }}</div>
            <div class="stat-footer">
              <el-progress :percentage="stats.databaseAvailability" :format="percentFormat" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">应用系统</div>
            <div class="stat-value">{{ stats.appCount }}</div>
            <div class="stat-footer">
              <el-progress :percentage="stats.appAvailability" :format="percentFormat" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="dashboard-content">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="monitor-card">
            <template #header>
              <div class="card-header">
                <span>资产类型分布</span>
              </div>
            </template>
            <div id="asset-type-chart" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="monitor-card">
            <template #header>
              <div class="card-header">
                <span>资产状态</span>
              </div>
            </template>
            <div id="asset-status-chart" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="monitor-card">
            <template #header>
              <div class="card-header">
                <span>部门资产分布</span>
              </div>
            </template>
            <div id="department-chart" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-card class="top-asset-card">
        <template #header>
          <div class="card-header">
            <span>最近添加的资产</span>
            <el-button type="primary" size="small" @click="gotoAssetList">查看更多</el-button>
          </div>
        </template>
        <el-table :data="recentAssets" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="资产名称" min-width="120" show-overflow-tooltip />
          <el-table-column prop="code" label="资产编码" min-width="120" show-overflow-tooltip />
          <el-table-column prop="type" label="资产类型" width="100">
            <template #default="scope">
              <el-tag :type="getTypeTagType(scope.row.type)">
                {{ formatType(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column fixed="right" label="操作" width="120">
            <template #default="scope">
              <el-button link type="primary" @click="handleDetail(scope.row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { listAssets } from '@/api/asset'
import type { Asset } from '@/types/asset'

const router = useRouter()
const recentAssets = ref<Asset[]>([])

// 仪表盘统计数据
const stats = reactive({
  totalAssets: 120,
  todayAddedAssets: 5,
  serverCount: 45,
  serverAvailability: 95,
  databaseCount: 30,
  databaseAvailability: 92,
  appCount: 28,
  appAvailability: 98
})

// 资产类型选项
const typeOptions = [
  { value: 'server', label: '服务器' },
  { value: 'database', label: '数据库' },
  { value: 'middleware', label: '中间件' },
  { value: 'application', label: '应用系统' }
]

// 格式化类型
const formatType = (type: string) => {
  const t = typeOptions.find(item => item.value === type)
  return t ? t.label : type
}

// 获取类型标签样式
const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    'server': 'primary',
    'database': 'success',
    'middleware': 'warning',
    'application': 'info'
  }
  return map[type] || 'info'
}

// 格式化百分比
const percentFormat = (percentage: number) => {
  return percentage + '%'
}

// 初始化资产类型图表
const initAssetTypeChart = () => {
  const chartDom = document.getElementById('asset-type-chart')
  if (!chartDom) return
  
  const chart = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['服务器', '数据库', '中间件', '应用系统', '其他']
    },
    series: [
      {
        name: '资产类型',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 45, name: '服务器' },
          { value: 30, name: '数据库' },
          { value: 17, name: '中间件' },
          { value: 28, name: '应用系统' },
          { value: 10, name: '其他' }
        ]
      }
    ]
  }
  
  chart.setOption(option)
}

// 初始化资产状态图表
const initAssetStatusChart = () => {
  const chartDom = document.getElementById('asset-status-chart')
  if (!chartDom) return
  
  const chart = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['正常', '故障', '维护中', '下线']
    },
    series: [
      {
        name: '资产状态',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 110, name: '正常' },
          { value: 5, name: '故障' },
          { value: 3, name: '维护中' },
          { value: 2, name: '下线' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  chart.setOption(option)
}

// 初始化部门资产分布图表
const initDepartmentChart = () => {
  const chartDom = document.getElementById('department-chart')
  if (!chartDom) return
  
  const chart = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: ['IT部门', '研发部门', '运维部门', '测试部门', '市场部门']
    },
    series: [
      {
        name: '资产数量',
        type: 'bar',
        data: [42, 30, 25, 18, 5]
      }
    ]
  }
  
  chart.setOption(option)
}

// 获取最近添加的资产
const getRecentAssets = async () => {
  try {
    const res = await listAssets({
      page: 0,
      size: 5
    })
    recentAssets.value = res.data.content
  } catch (error) {
    console.error('获取最近资产失败', error)
  }
}

// 跳转到资产详情
const handleDetail = (row: Asset) => {
  router.push({ path: `/asset/detail/${row.id}` })
}

// 跳转到资产列表
const gotoAssetList = () => {
  router.push('/asset/list')
}

onMounted(() => {
  // 初始化图表
  initAssetTypeChart()
  initAssetStatusChart()
  initDepartmentChart()
  
  // 获取最近添加的资产
  getRecentAssets()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  
  .dashboard-header {
    margin-bottom: 20px;
    
    .stat-card {
      height: 120px;
      text-align: center;
      
      .stat-title {
        font-size: 14px;
        color: #606266;
        margin-bottom: 10px;
      }
      
      .stat-value {
        font-size: 28px;
        font-weight: bold;
        margin-bottom: 15px;
      }
      
      .stat-footer {
        margin-top: 10px;
      }
    }
  }
  
  .dashboard-content {
    .monitor-card {
      height: 350px;
      margin-bottom: 20px;
      
      .chart {
        height: 280px;
      }
    }
    
    .top-asset-card {
      margin-top: 20px;
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
    
    .card-header {
      font-weight: bold;
    }
  }
}
</style> 