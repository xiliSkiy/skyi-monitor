<template>
  <grid-item
    :x="x"
    :y="y"
    :w="w"
    :h="h"
    :i="i"
    :min-w="minW"
    :min-h="minH"
    :drag-allow-from="dragAllowFrom"
    :drag-ignore-from="dragIgnoreFrom"
    class="dashboard-grid-item"
  >
    <div class="grid-item-content">
      <slot></slot>
    </div>
  </grid-item>
</template>

<script lang="ts" setup>
import { VueGridLayout } from 'vue-grid-layout'

const GridItem = VueGridLayout.GridItem

defineProps({
  x: {
    type: Number,
    required: true
  },
  y: {
    type: Number,
    required: true
  },
  w: {
    type: Number,
    required: true
  },
  h: {
    type: Number,
    required: true
  },
  i: {
    type: [String, Number],
    required: true
  },
  minW: {
    type: Number,
    default: 2
  },
  minH: {
    type: Number,
    default: 2
  },
  dragAllowFrom: {
    type: String,
    default: '.grid-item-header'
  },
  dragIgnoreFrom: {
    type: String,
    default: '.no-drag'
  }
})
</script>

<style lang="scss" scoped>
.dashboard-grid-item {
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s;
  overflow: hidden;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  .grid-item-content {
    width: 100%;
    height: 100%;
    padding: 10px;
    box-sizing: border-box;
    overflow: auto;
  }
}
</style> 