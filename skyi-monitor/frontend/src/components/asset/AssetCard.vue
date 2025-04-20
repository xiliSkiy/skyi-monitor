<template>
  <div class="asset-card">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ title }}</span>
          <el-tag size="small">{{ assetType }}</el-tag>
        </div>
      </template>
      <div class="card-content">
        <p><strong>IP地址:</strong> {{ ipAddress }}</p>
        <p v-if="port"><strong>端口:</strong> {{ port }}</p>
        <p><strong>状态:</strong> <el-tag :type="statusType">{{ status }}</el-tag></p>
        <p v-if="description"><strong>描述:</strong> {{ description }}</p>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

const props = defineProps({
  title: {
    type: String,
    default: '资产'
  },
  assetType: {
    type: String,
    default: '未知'
  },
  ipAddress: {
    type: String,
    default: '-'
  },
  port: {
    type: [Number, String],
    default: null
  },
  status: {
    type: String,
    default: '未知'
  },
  description: {
    type: String,
    default: ''
  }
});

const statusType = computed(() => {
  const status = props.status;
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
});
</script>

<style lang="scss" scoped>
.asset-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .card-content {
    p {
      margin: 8px 0;
    }
  }
}
</style> 