<template>
  <div class="app-wrapper">
    <div class="sidebar-container">
      <div class="logo-container">
        <img src="@/assets/images/logo.png" alt="Logo" class="logo" />
        <span class="title">SKYI Monitor</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </div>
    
    <div class="main-container">
      <div class="navbar">
        <div class="left-menu">
          <i 
            class="el-icon-s-fold hamburger"
            :class="{ 'is-active': !isCollapse }"
            @click="toggleSidebar"
          />
          <breadcrumb />
        </div>
        <div class="right-menu">
          <el-dropdown trigger="click">
            <div class="avatar-wrapper">
              <img src="@/assets/images/avatar.jpg" class="user-avatar" />
              <i class="el-icon-caret-bottom" />
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item>修改密码</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <app-main />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SidebarItem from './SidebarItem.vue'
import AppMain from './AppMain.vue'
import Breadcrumb from './Breadcrumb.vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

// 获取路由
const routes = computed(() => {
  return router.options.routes.filter((route: any) => !route.meta?.hidden)
})

// 获取当前激活的菜单
const activeMenu = computed(() => {
  const { path, matched } = route
  // 如果当前路由设置了 activeMenu 属性，则使用它
  if (matched.length > 0 && matched[matched.length - 1].meta?.activeMenu) {
    return matched[matched.length - 1].meta.activeMenu
  }
  return path
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
  
  .sidebar-container {
    width: 220px;
    height: 100%;
    background-color: #304156;
    transition: width 0.3s;
    overflow: hidden;
    
    .logo-container {
      display: flex;
      align-items: center;
      height: 64px;
      padding: 0 16px;
      
      .logo {
        width: 32px;
        height: 32px;
        margin-right: 10px;
      }
      
      .title {
        color: #fff;
        font-size: 18px;
        font-weight: bold;
      }
    }
  }
  
  .main-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    
    .navbar {
      display: flex;
      justify-content: space-between;
      height: 64px;
      padding: 0 16px;
      background-color: #fff;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
      
      .left-menu {
        display: flex;
        align-items: center;
        
        .hamburger {
          padding: 0 15px;
          font-size: 20px;
          cursor: pointer;
          transition: transform 0.3s;
          
          &.is-active {
            transform: rotate(180deg);
          }
        }
      }
      
      .right-menu {
        display: flex;
        align-items: center;
        
        .avatar-wrapper {
          display: flex;
          align-items: center;
          cursor: pointer;
          
          .user-avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            margin-right: 8px;
          }
        }
      }
    }
  }
}
</style> 