<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
        <span 
          v-if="index === breadcrumbs.length - 1 || !item.redirect" 
          class="no-redirect"
        >
          {{ item.meta.title }}
        </span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const breadcrumbs = ref<any[]>([])

// 获取面包屑数据
const getBreadcrumb = () => {
  // 不在首页显示面包屑
  if (route.path === '/dashboard') {
    breadcrumbs.value = []
    return
  }
  
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  const first = matched[0]
  
  // 如果不是主页开始，就添加首页
  if (first && first.path !== '/dashboard') {
    matched.unshift({
      path: '/dashboard',
      redirect: undefined,
      meta: { title: '首页' }
    } as any)
  }
  
  breadcrumbs.value = matched
}

// 点击面包屑
const handleLink = (item: any) => {
  const { redirect, path } = item
  if (redirect) {
    router.push(redirect)
    return
  }
  router.push(path)
}

// 监听路由变化
watch(
  () => route.path,
  () => {
    getBreadcrumb()
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.app-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 64px;
  margin-left: 8px;
  
  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
}

.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.5s;
}

.breadcrumb-enter-from,
.breadcrumb-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.breadcrumb-move {
  transition: all 0.5s;
}

.breadcrumb-leave-active {
  position: absolute;
}
</style> 