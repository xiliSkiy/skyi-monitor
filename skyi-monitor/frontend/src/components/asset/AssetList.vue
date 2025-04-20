<template>
  <div class="asset-list">
    <el-table :data="assets" style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="名称" min-width="120">
        <template #default="scope">
          <el-link type="primary" @click="handleAssetClick(scope.row)">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="assetType" label="类型" width="120">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.assetType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" width="140" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="owner" label="负责人" width="100" />
      <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleViewDetail(scope.row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination" v-if="showPagination">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, defineEmits } from 'vue';

const props = defineProps({
  assets: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits([
  'view',
  'edit',
  'asset-click',
  'size-change',
  'current-change'
]);

// 根据资产状态获取标签类型
const getStatusType = (status) => {
  switch (status) {
    case '在线':
    case 'ACTIVE':
      return 'success';
    case '离线':
    case 'INACTIVE':
      return 'danger';
    case '维护中':
    case 'MAINTAINING':
      return 'warning';
    case '异常':
    case 'ERROR':
      return 'error';
    default:
      return 'info';
  }
};

// 处理查看详情
const handleViewDetail = (asset) => {
  emit('view', asset);
};

// 处理编辑资产
const handleEdit = (asset) => {
  emit('edit', asset);
};

// 处理资产点击
const handleAssetClick = (asset) => {
  emit('asset-click', asset);
};

// 处理每页数量变化
const handleSizeChange = (size) => {
  emit('size-change', size);
};

// 处理页码变化
const handleCurrentChange = (page) => {
  emit('current-change', page);
};
</script>

<style lang="scss" scoped>
.asset-list {
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style> 