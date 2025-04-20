<template>
  <div class="app-wrapper">
    <div class="sidebar-container" :class="{ 'is-collapsed': isCollapse }">
      <div class="logo-container">
        <img src="@/assets/images/logo.png" alt="Logo" class="logo" />
        <span class="title" v-show="!isCollapse">SKYI Monitor</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          unique-opened
          router
        >
          <sidebar-item
            v-for="route in routes"
            :key="route.path"
            :item="route"
            :base-path="route.path"
          />
        </el-menu>
      </el-scrollbar>
    </div>
    
    <div class="main-container">
      <div class="navbar">
        <div class="left-menu">
          <el-icon class="hamburger" :class="{ 'is-active': !isCollapse }" @click="toggleSidebar">
            <Fold v-if="isCollapse" />
            <Expand v-else />
          </el-icon>
          <breadcrumb />
        </div>
        <div class="right-menu">
          <el-dropdown trigger="click">
            <div class="avatar-wrapper">
              <img src="@/assets/images/avatar.jpg" class="user-avatar" />
              <span class="user-name">管理员</span>
              <el-icon class="dropdown-icon"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item>
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
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
import { Fold, Expand, CaretBottom, User, Lock, SwitchButton } from '@element-plus/icons-vue'
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
  background-color: #f5f7fa;
  
  .sidebar-container {
    width: 220px;
    height: 100%;
    background-color: #304156;
    transition: all 0.3s ease;
    overflow: hidden;
    box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
    z-index: 1001;
    
    &.is-collapsed {
      width: 64px;
    }
    
    .logo-container {
      display: flex;
      align-items: center;
      height: 64px;
      padding: 0 16px;
      overflow: hidden;
      background-color: #263445;
      border-bottom: 1px solid #1f2d3d;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
      
      .logo {
        width: 32px;
        height: 32px;
        border-radius: 4px;
        margin-right: 10px;
        transition: all 0.3s;
      }
      
      .title {
        color: #fff;
        font-size: 16px;
        font-weight: 600;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        transition: all 0.3s;
      }
    }
    
    .el-scrollbar {
      height: calc(100% - 64px);
    }
  }
  
  .main-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    min-width: 0;
    
    .navbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      height: 64px;
      padding: 0 16px;
      background-color: #fff;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
      position: relative;
      
      .left-menu {
        display: flex;
        align-items: center;
        
        .hamburger {
          margin-right: 15px;
          font-size: 20px;
          cursor: pointer;
          transition: transform 0.3s;
          color: #5a5e66;
          
          &.is-active {
            transform: rotate(180deg);
          }
          
          &:hover {
            color: #409EFF;
          }
        }
      }
      
      .right-menu {
        display: flex;
        align-items: center;
        
        .avatar-wrapper {
          display: flex;
          align-items: center;
          padding: 0 8px;
          height: 40px;
          border-radius: 20px;
          cursor: pointer;
          transition: all 0.3s;
          
          &:hover {
            background-color: rgba(0, 0, 0, 0.05);
          }
          
          .user-avatar {
            width: 30px;
            height: 30px;
            border-radius: 50%;
            margin-right: 8px;
          }
          
          .user-name {
            font-size: 14px;
            color: #606266;
          }
          
          .dropdown-icon {
            margin-left: 6px;
            color: #909399;
          }
        }
      }
    }
  }
}

// 美化嵌套滚动条
:deep(.el-scrollbar__bar) {
  width: 4px !important;
  
  &.is-horizontal {
    display: none;
  }
}

// 美化下拉菜单
:deep(.el-dropdown-menu) {
  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    
    .el-icon {
      margin-right: 8px;
    }
  }
}

// 收起菜单时Logo居中
.is-collapsed {
  .logo-container {
    padding: 0;
    justify-content: center;
    
    .logo {
      margin: 0;
    }
  }
}
</style> 