<template>
  <div class="report-list">
    <div class="page-header">
      <h1>报表管理</h1>
      <el-button type="primary" @click="handleCreateReport">创建报表</el-button>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索报表名称"
        prefix-icon="el-icon-search"
        clearable
        style="width: 250px; margin-right: 15px;"
      />
      
      <el-select v-model="categoryFilter" placeholder="分类筛选" clearable style="width: 150px; margin-right: 15px;">
        <el-option label="全部" value="" />
        <el-option label="系统报表" value="SYSTEM" />
        <el-option label="资源报表" value="RESOURCE" />
        <el-option label="性能报表" value="PERFORMANCE" />
        <el-option label="业务报表" value="BUSINESS" />
        <el-option label="自定义报表" value="CUSTOM" />
      </el-select>
      
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>
    
    <el-table
      :data="filteredReports"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
    >
      <el-table-column prop="name" label="报表名称" min-width="180">
        <template #default="scope">
          <el-link type="primary" @click="handleViewReport(scope.row)">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      
      <el-table-column prop="category" label="分类" width="120">
        <template #default="scope">
          <el-tag>{{ getCategoryLabel(scope.row.category) }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
      
      <el-table-column prop="creator" label="创建人" width="120" />
      
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      
      <el-table-column prop="updateTime" label="更新时间" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.updateTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="scope">
          <el-button size="small" @click="handleViewReport(scope.row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEditReport(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDeleteReport(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
    >
      <span>确定要删除报表 "{{ currentReport?.name }}" 吗？此操作不可恢复。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 报表接口
interface Report {
  id: number;
  name: string;
  category: string;
  description: string;
  creator: string;
  createTime: string;
  updateTime: string;
}

// 模拟报表数据
const reports = ref<Report[]>([
  {
    id: 1,
    name: '系统性能概览',
    category: 'SYSTEM',
    description: '显示系统CPU、内存、磁盘和网络等关键性能指标',
    creator: '系统管理员',
    createTime: '2023-01-15T10:00:00',
    updateTime: '2023-01-15T10:00:00'
  },
  {
    id: 2,
    name: '资源使用趋势',
    category: 'RESOURCE',
    description: '展示过去7天内系统资源使用的变化趋势',
    creator: '系统管理员',
    createTime: '2023-01-16T14:30:00',
    updateTime: '2023-02-01T09:15:00'
  },
  {
    id: 3,
    name: '网络流量分析',
    category: 'PERFORMANCE',
    description: '分析网络流入流出流量，识别高峰期和异常情况',
    creator: '网络管理员',
    createTime: '2023-01-20T16:45:00',
    updateTime: '2023-01-20T16:45:00'
  },
  {
    id: 4,
    name: '应用性能监控',
    category: 'PERFORMANCE',
    description: '监控关键应用的响应时间、错误率和请求量',
    creator: '应用管理员',
    createTime: '2023-01-25T11:20:00',
    updateTime: '2023-02-10T15:30:00'
  },
  {
    id: 5,
    name: '自定义业务指标',
    category: 'BUSINESS',
    description: '展示与业务相关的自定义指标和KPI',
    creator: '业务分析师',
    createTime: '2023-02-05T09:10:00',
    updateTime: '2023-02-15T14:25:00'
  }
]);

// 列表状态
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(reports.value.length);
const searchKeyword = ref('');
const categoryFilter = ref('');

// 删除对话框状态
const deleteDialogVisible = ref(false);
const currentReport = ref<Report | null>(null);

// 筛选报表
const filteredReports = computed(() => {
  return reports.value.filter(report => {
    const keywordMatch = !searchKeyword.value || 
      report.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) || 
      report.description.toLowerCase().includes(searchKeyword.value.toLowerCase());
    
    const categoryMatch = !categoryFilter.value || report.category === categoryFilter.value;
    
    return keywordMatch && categoryMatch;
  });
});

// 格式化时间
const formatTime = (time: string): string => {
  if (!time) return '';
  
  try {
    const date = new Date(time);
    return date.toLocaleString();
  } catch (e) {
    return time;
  }
};

// 获取分类标签
const getCategoryLabel = (category: string): string => {
  switch (category) {
    case 'SYSTEM':
      return '系统报表';
    case 'RESOURCE':
      return '资源报表';
    case 'PERFORMANCE':
      return '性能报表';
    case 'BUSINESS':
      return '业务报表';
    case 'CUSTOM':
      return '自定义报表';
    default:
      return category;
  }
};

// 创建报表
const handleCreateReport = () => {
  router.push('/visualization/reports/create');
};

// 查看报表
const handleViewReport = (report: Report) => {
  router.push(`/visualization/reports/view/${report.id}`);
};

// 编辑报表
const handleEditReport = (report: Report) => {
  router.push(`/visualization/reports/edit/${report.id}`);
};

// 删除报表
const handleDeleteReport = (report: Report) => {
  currentReport.value = report;
  deleteDialogVisible.value = true;
};

// 确认删除
const confirmDelete = () => {
  if (!currentReport.value) return;
  
  // 模拟删除操作
  const index = reports.value.findIndex(item => item.id === currentReport.value?.id);
  if (index > -1) {
    reports.value.splice(index, 1);
    total.value = reports.value.length;
  }
  
  deleteDialogVisible.value = false;
  currentReport.value = null;
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
};

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = '';
  categoryFilter.value = '';
  currentPage.value = 1;
};

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
};

// 当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
};
</script>

<style lang="scss" scoped>
.report-list {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
    }
  }
  
  .search-bar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    margin-bottom: 10px;
  }
  
  .pagination {
    margin-top: 20px;
    text-align: center;
  }
}
</style> 