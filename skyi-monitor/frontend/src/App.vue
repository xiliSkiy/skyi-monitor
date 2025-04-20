<template>
  <div class="app-container">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import '@/assets/styles/index.scss'

// 确保样式加载
onMounted(() => {
  console.log('应用初始化完成')
})
</script>

<style lang="scss">
.app-container {
  width: 100%;
  height: 100%;
}
</style> 