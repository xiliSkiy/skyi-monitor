import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/layout/Layout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '仪表盘', icon: 'el-icon-monitor' }
        }
      ]
    },
    {
      path: '/asset',
      component: Layout,
      redirect: '/asset/list',
      meta: { title: '资产管理', icon: 'el-icon-shopping-cart' },
      children: [
        {
          path: 'list',
          name: 'AssetList',
          component: () => import('@/views/asset/list.vue'),
          meta: { title: '资产列表', icon: 'el-icon-document' }
        },
        {
          path: 'create',
          name: 'AssetCreate',
          component: () => import('@/views/asset/edit.vue'),
          meta: { title: '创建资产', icon: 'el-icon-plus' }
        },
        {
          path: 'edit/:id',
          name: 'AssetEdit',
          component: () => import('@/views/asset/edit.vue'),
          meta: { title: '编辑资产', icon: 'el-icon-edit', hidden: true }
        },
        {
          path: 'detail/:id',
          name: 'AssetDetail',
          component: () => import('@/views/asset/detail.vue'),
          meta: { title: '资产详情', icon: 'el-icon-info', hidden: true }
        }
      ]
    },
    {
      path: '/collector',
      component: Layout,
      redirect: '/collector/task',
      meta: { title: '采集管理', icon: 'el-icon-data-analysis' },
      children: [
        {
          path: 'task',
          name: 'CollectorTaskList',
          component: () => import('@/views/collector/task/list.vue'),
          meta: { title: '采集任务', icon: 'el-icon-data-line' }
        },
        {
          path: 'task/create',
          name: 'CollectorTaskCreate',
          component: () => import('@/views/collector/task/edit.vue'),
          meta: { title: '创建采集任务', icon: 'el-icon-plus' }
        },
        {
          path: 'task/edit/:id',
          name: 'CollectorTaskEdit',
          component: () => import('@/views/collector/task/edit.vue'),
          meta: { title: '编辑采集任务', icon: 'el-icon-edit', hidden: true }
        },
        {
          path: 'task/detail/:id',
          name: 'CollectorTaskDetail',
          component: () => import('@/views/collector/task/detail.vue'),
          meta: { title: '采集任务详情', icon: 'el-icon-info', hidden: true }
        },
        {
          path: 'schedule',
          name: 'CollectorScheduleList',
          component: () => import('@/views/collector/schedule/list.vue'),
          meta: { title: '采集调度', icon: 'el-icon-time' }
        },
        {
          path: 'schedule/create',
          name: 'CollectorScheduleCreate',
          component: () => import('@/views/collector/schedule/edit.vue'),
          meta: { title: '创建调度', icon: 'el-icon-plus' }
        },
        {
          path: 'schedule/create/:taskId',
          name: 'CollectorScheduleCreateWithTask',
          component: () => import('@/views/collector/schedule/edit.vue'),
          meta: { title: '创建调度', icon: 'el-icon-plus', hidden: true }
        },
        {
          path: 'schedule/edit/:id',
          name: 'CollectorScheduleEdit',
          component: () => import('@/views/collector/schedule/edit.vue'),
          meta: { title: '编辑调度', icon: 'el-icon-edit', hidden: true }
        }
      ]
    },
    {
      path: '/404',
      component: () => import('@/views/error/404.vue'),
      meta: { hidden: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/404',
      meta: { hidden: true }
    }
  ]
})

export default router 