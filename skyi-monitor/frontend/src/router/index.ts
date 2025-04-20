import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '../components/layout/Layout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      component: Layout,
      redirect: '/dashboard/index',
      children: [
        {
          path: 'index',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '仪表盘', icon: 'Odometer' }
        }
      ]
    },
    {
      path: '/asset',
      component: Layout,
      redirect: '/asset/list',
      meta: { title: '资产管理', icon: 'Box' },
      children: [
        {
          path: 'list',
          name: 'AssetList',
          component: () => import('@/views/asset/list.vue'),
          meta: { title: '资产列表', icon: 'List' }
        },
        {
          path: 'detail/:id',
          name: 'AssetDetail',
          component: () => import('@/views/asset/detail.vue'),
          meta: { title: '资产详情', activeMenu: '/asset/list', hidden: true }
        },
        {
          path: 'topology',
          name: 'AssetTopology',
          component: () => import('@/views/asset/topology.vue'),
          meta: { title: '资产拓扑', icon: 'Connection' }
        },
        {
          path: 'group',
          name: 'AssetGroup',
          component: () => import('@/views/asset/group.vue'),
          meta: { title: '资产分组', icon: 'Folder' }
        }
      ]
    },
    {
      path: '/collector',
      component: Layout,
      redirect: '/collector/task',
      meta: { title: '采集管理', icon: 'DataLine' },
      children: [
        {
          path: 'task',
          name: 'CollectorTask',
          component: () => import('@/views/collector/task/index.vue'),
          meta: { title: '采集任务', icon: 'Clock' }
        },
        {
          path: 'task/create',
          name: 'CollectorTaskCreate',
          component: () => import('@/views/collector/task/edit.vue'),
          meta: { title: '创建任务', activeMenu: '/collector/task', hidden: true }
        },
        {
          path: 'task/edit/:id',
          name: 'CollectorTaskEdit',
          component: () => import('@/views/collector/task/edit.vue'),
          meta: { title: '编辑任务', activeMenu: '/collector/task', hidden: true }
        },
        {
          path: 'schedule',
          name: 'CollectorSchedule',
          component: () => import('@/views/collector/schedule/index.vue'),
          meta: { title: '调度管理', icon: 'Calendar' }
        },
        {
          path: 'schedule/create',
          name: 'CollectorScheduleCreate',
          component: () => import('@/views/collector/schedule/edit.vue'),
          meta: { title: '创建调度', activeMenu: '/collector/schedule', hidden: true }
        },
        {
          path: 'schedule/edit/:id',
          name: 'CollectorScheduleEdit',
          component: () => import('@/views/collector/schedule/edit.vue'),
          meta: { title: '编辑调度', activeMenu: '/collector/schedule', hidden: true }
        },
        {
          path: 'execution',
          name: 'CollectorExecution',
          component: () => import('@/views/collector/execution/index.vue'),
          meta: { title: '执行历史', icon: 'Timer' }
        },
        {
          path: 'execution/detail/:id',
          name: 'CollectorExecutionDetail',
          component: () => import('@/views/collector/execution/detail.vue'),
          meta: { title: '执行详情', activeMenu: '/collector/execution', hidden: true }
        }
      ]
    },
    {
      path: '/alert',
      component: Layout,
      redirect: '/alert/notification',
      meta: { title: '告警中心', icon: 'Bell' },
      children: [
        {
          path: 'notification',
          name: 'AlertNotification',
          component: () => import('@/views/alert/notification/index.vue'),
          meta: { title: '告警通知', icon: 'Message' }
        },
        {
          path: 'rule',
          name: 'AlertRule',
          component: () => import('@/views/alert/rule/index.vue'),
          meta: { title: '告警规则', icon: 'Guide' }
        },
        {
          path: 'rule/create',
          name: 'AlertRuleCreate',
          component: () => import('@/views/alert/rule/edit.vue'),
          meta: { title: '创建规则', activeMenu: '/alert/rule', hidden: true }
        },
        {
          path: 'rule/edit/:id',
          name: 'AlertRuleEdit',
          component: () => import('@/views/alert/rule/edit.vue'),
          meta: { title: '编辑规则', activeMenu: '/alert/rule', hidden: true }
        },
        {
          path: 'detail/:id',
          name: 'AlertDetail',
          component: () => import('@/views/alert/detail.vue'),
          meta: { title: '告警详情', activeMenu: '/alert/notification', hidden: true }
        },
        {
          path: 'history',
          name: 'AlertHistory',
          component: () => import('@/views/alert/history/index.vue'),
          meta: { title: '历史告警', icon: 'Histogram' }
        },
        {
          path: 'channel',
          name: 'AlertChannel',
          component: () => import('@/views/alert/channel/index.vue'),
          meta: { title: '通知渠道', icon: 'ChatDotRound' }
        },
        {
          path: 'channel/create',
          name: 'AlertChannelCreate',
          component: () => import('@/views/alert/channel/edit.vue'),
          meta: { title: '创建渠道', activeMenu: '/alert/channel', hidden: true }
        },
        {
          path: 'channel/edit/:id',
          name: 'AlertChannelEdit',
          component: () => import('@/views/alert/channel/edit.vue'),
          meta: { title: '编辑渠道', activeMenu: '/alert/channel', hidden: true }
        }
      ]
    },
    {
      path: '/visualization',
      component: Layout,
      redirect: '/visualization/dashboards',
      meta: { title: '可视化管理', icon: 'PieChart' },
      children: [
        {
          path: 'dashboards',
          name: 'DashboardList',
          component: () => import('@/views/visualization/dashboard/list.vue'),
          meta: { title: '仪表盘管理', icon: 'Monitor' }
        },
        {
          path: 'dashboards/create',
          name: 'DashboardCreate',
          component: () => import('@/views/visualization/dashboard/edit.vue'),
          meta: { title: '创建仪表盘', activeMenu: '/visualization/dashboards', hidden: true }
        },
        {
          path: 'dashboards/edit/:id',
          name: 'DashboardEdit',
          component: () => import('@/views/visualization/dashboard/edit.vue'),
          meta: { title: '编辑仪表盘', activeMenu: '/visualization/dashboards', hidden: true }
        },
        {
          path: 'dashboards/view/:id',
          name: 'DashboardView',
          component: () => import('@/views/visualization/dashboard/view.vue'),
          meta: { title: '查看仪表盘', activeMenu: '/visualization/dashboards', hidden: true }
        },
        {
          path: 'components',
          name: 'ComponentLibrary',
          component: () => import('@/views/visualization/component/index.vue'),
          meta: { title: '组件库', icon: 'Grid' }
        },
        {
          path: 'components/detail/:id',
          name: 'ComponentDetail',
          component: () => import('@/views/visualization/component/detail.vue'),
          meta: { title: '组件详情', activeMenu: '/visualization/components', hidden: true }
        },
        {
          path: 'components/use/:id',
          name: 'ComponentUse',
          component: () => import('@/views/visualization/component/use.vue'),
          meta: { title: '使用组件', activeMenu: '/visualization/components', hidden: true }
        },
        {
          path: 'reports',
          name: 'ReportList',
          component: () => import('@/views/visualization/report/list.vue'),
          meta: { title: '报表管理', icon: 'Document' }
        },
        {
          path: 'reports/create',
          name: 'ReportCreate',
          component: () => import('@/views/visualization/report/edit.vue'),
          meta: { title: '创建报表', activeMenu: '/visualization/reports', hidden: true }
        },
        {
          path: 'reports/edit/:id',
          name: 'ReportEdit',
          component: () => import('@/views/visualization/report/edit.vue'),
          meta: { title: '编辑报表', activeMenu: '/visualization/reports', hidden: true }
        },
        {
          path: 'reports/view/:id',
          name: 'ReportView',
          component: () => import('@/views/visualization/report/view.vue'),
          meta: { title: '查看报表', activeMenu: '/visualization/reports', hidden: true }
        },
        {
          path: 'metric-explorer',
          name: 'MetricExplorer',
          component: () => import('@/views/visualization/metric-explorer/index.vue'),
          meta: { title: '指标浏览器', icon: 'Trend' }
        }
      ]
    },
    {
      path: '/system',
      component: Layout,
      redirect: '/system/user',
      meta: { title: '系统管理', icon: 'Setting' },
      children: [
        {
          path: 'user',
          name: 'SystemUser',
          component: () => import('@/views/system/user/index.vue'),
          meta: { title: '用户管理', icon: 'User' }
        },
        {
          path: 'role',
          name: 'SystemRole',
          component: () => import('@/views/system/role/index.vue'),
          meta: { title: '角色管理', icon: 'UserFilled' }
        },
        {
          path: 'log',
          name: 'SystemLog',
          component: () => import('@/views/system/log/index.vue'),
          meta: { title: '系统日志', icon: 'DocumentCopy' }
        },
        {
          path: 'setting',
          name: 'SystemSetting',
          component: () => import('@/views/system/setting/index.vue'),
          meta: { title: '系统设置', icon: 'Tools' }
        }
      ]
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { hidden: true }
    },
    {
      path: '/404',
      name: 'NotFound',
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

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 在这里可以添加登录验证等逻辑
  next()
})

export default router 