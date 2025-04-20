<template>
  <div class="dashboard-grid-layout">
    <grid-layout
      :layout="layout"
      :col-num="colNum"
      :row-height="rowHeight"
      :is-draggable="isDraggable"
      :is-resizable="isResizable"
      :vertical-compact="true"
      :margin="margin"
      :use-css-transforms="true"
      :responsive="true"
    >
      <slot></slot>
    </grid-layout>
  </div>
</template>

<script lang="ts" setup>
import { VueGridLayout } from 'vue-grid-layout'

const GridLayout = VueGridLayout.GridLayout

defineProps({
  layout: {
    type: Array,
    required: true
  },
  colNum: {
    type: Number,
    default: 12
  },
  rowHeight: {
    type: Number,
    default: 30
  },
  isDraggable: {
    type: Boolean,
    default: true
  },
  isResizable: {
    type: Boolean,
    default: true
  },
  margin: {
    type: Array,
    default: () => [10, 10]
  }
})
</script>

<style lang="scss" scoped>
.dashboard-grid-layout {
  width: 100%;
  position: relative;
}
</style> 