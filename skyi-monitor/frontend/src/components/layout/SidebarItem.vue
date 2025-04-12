<template>
  <div v-if="!item.meta || !item.meta.hidden">
    <template v-if="!hasOneShowingChild(item.children, item) || onlyOneChild.children">
      <el-sub-menu :index="resolvePath(item.path)" popper-append-to-body>
        <template #title>
          <i v-if="item.meta && item.meta.icon" :class="item.meta.icon"></i>
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
        <i v-if="onlyOneChild.meta && onlyOneChild.meta.icon" :class="onlyOneChild.meta.icon"></i>
        <span v-if="onlyOneChild.meta && onlyOneChild.meta.title">{{ onlyOneChild.meta.title }}</span>
      </el-menu-item>
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import path from 'path-browserify'

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
  
  i {
    margin-right: 10px;
  }
}
</style> 