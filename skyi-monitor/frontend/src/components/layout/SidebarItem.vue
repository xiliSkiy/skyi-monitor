<template>
  <div v-if="!item.meta || !item.meta.hidden">
    <template v-if="!hasOneShowingChild(item.children, item) || onlyOneChild.children">
      <el-sub-menu :index="resolvePath(item.path)" popper-append-to-body>
        <template #title>
          <el-icon v-if="item.meta && item.meta.icon">
            <component :is="item.meta.icon.replace('el-icon-', '')"></component>
          </el-icon>
          <span v-if="item.meta && item.meta.title">{{ item.meta.title }}</span>
        </template>
        <sidebar-item
          v-for="child in item.children"
          :key="child.path"
          :item="child"
          :base-path="resolvePath(child.path)"
          class="nest-menu"
        />
      </el-sub-menu>
    </template>
    
    <template v-else>
      <el-menu-item :index="resolvePath(onlyOneChild.path)">
        <el-icon v-if="onlyOneChild.meta && onlyOneChild.meta.icon">
          <component :is="onlyOneChild.meta.icon.replace('el-icon-', '')"></component>
        </el-icon>
        <template #title>
          <span v-if="onlyOneChild.meta && onlyOneChild.meta.title">{{ onlyOneChild.meta.title }}</span>
        </template>
      </el-menu-item>
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import path from 'path-browserify'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

export default defineComponent({
  name: 'SidebarItem',
  props: {
    item: {
      type: Object,
      required: true
    },
    basePath: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const onlyOneChild = ref<any>(null)

    const hasOneShowingChild = (children = [], parent: any) => {
      const showingChildren = children.filter((item: any) => {
        if (item.meta && item.meta.hidden) {
          return false
        } else {
          onlyOneChild.value = item
          return true
        }
      })

      if (showingChildren.length === 1) {
        return true
      }

      if (showingChildren.length === 0) {
        onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
        return true
      }

      return false
    }

    const resolvePath = (routePath: string) => {
      if (/^(https?:|mailto:|tel:)/.test(routePath)) {
        return routePath
      }
      return path.resolve(props.basePath, routePath)
    }

    return {
      onlyOneChild,
      hasOneShowingChild,
      resolvePath
    }
  }
})
</script>

<style lang="scss" scoped>
.el-menu-item, .el-sub-menu {
  font-size: 14px;
  border-left: 3px solid transparent;
  transition: all 0.3s;
  
  &:hover {
    background-color: #263445 !important;
    border-left-color: #409EFF;
  }
  
  &.is-active {
    background-color: #1f2d3d !important;
    border-left-color: #409EFF;
    
    &::before {
      opacity: 1;
    }
  }
  
  .el-icon {
    margin-right: 10px;
    width: 18px;
    height: 18px;
    vertical-align: middle;
  }
  
  span {
    vertical-align: middle;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.nest-menu .el-menu-item {
  padding-left: 40px !important;
  
  &::before {
    content: '';
    position: absolute;
    left: 25px;
    top: 50%;
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background-color: #909399;
    opacity: 0.5;
    transform: translateY(-50%);
    transition: opacity 0.3s;
  }
}

// 提供过渡动效
.el-menu-item, .el-sub-menu__title {
  &:hover {
    transform: translateX(3px);
  }
}
</style> 