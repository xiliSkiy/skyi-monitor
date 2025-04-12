<template>
  <div class="error-container">
    <el-result
      icon="error"
      title="404"
      sub-title="很抱歉，您访问的页面不存在"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">回到首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style> 