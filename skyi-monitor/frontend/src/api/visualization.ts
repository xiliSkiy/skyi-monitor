import request from '@/utils/request'

/**
 * 仪表盘相关API
 */

// 获取仪表盘列表
export function getDashboardList(params: any) {
  return request({
    url: '/visualization/dashboards',
    method: 'get',
    params
  })
}

// 获取仪表盘详情
export function getDashboardDetail(id: number | string) {
  return request({
    url: `/visualization/dashboards/${id}`,
    method: 'get'
  })
}

// 创建仪表盘
export function createDashboard(data: any) {
  return request({
    url: '/visualization/dashboards',
    method: 'post',
    data
  })
}

// 更新仪表盘
export function updateDashboard(id: number | string, data: any) {
  return request({
    url: `/visualization/dashboards/${id}`,
    method: 'put',
    data
  })
}

// 删除仪表盘
export function deleteDashboard(id: number | string) {
  return request({
    url: `/visualization/dashboards/${id}`,
    method: 'delete'
  })
}

// 设置默认仪表盘
export function setDefaultDashboard(id: number | string) {
  return request({
    url: `/visualization/dashboards/${id}/default`,
    method: 'put'
  })
}

/**
 * 仪表盘组件相关API
 */

// 获取组件列表
export function getComponentList(dashboardId: number | string) {
  return request({
    url: `/visualization/dashboards/${dashboardId}/components`,
    method: 'get'
  })
}

// 创建组件
export function createComponent(dashboardId: number | string, data: any) {
  return request({
    url: `/visualization/dashboards/${dashboardId}/components`,
    method: 'post',
    data
  })
}

// 更新组件
export function updateComponent(dashboardId: number | string, componentId: number | string, data: any) {
  return request({
    url: `/visualization/dashboards/${dashboardId}/components/${componentId}`,
    method: 'put',
    data
  })
}

// 删除组件
export function deleteComponent(dashboardId: number | string, componentId: number | string) {
  return request({
    url: `/visualization/dashboards/${dashboardId}/components/${componentId}`,
    method: 'delete'
  })
}

/**
 * 指标数据查询API
 */

// 查询指标数据
export function queryMetricData(data: any) {
  return request({
    url: '/visualization/metrics/query',
    method: 'post',
    data
  })
}

// 查询多个指标数据
export function queryMultiMetricData(data: any) {
  return request({
    url: '/visualization/metrics/multi-query',
    method: 'post',
    data
  })
}

// 获取指标最新值
export function getLatestMetricValue(name: string, tags: Record<string, string>) {
  return request({
    url: '/visualization/metrics/latest',
    method: 'get',
    params: { name, tags }
  })
}

// 获取指标名称列表
export function getMetricNames() {
  return request({
    url: '/visualization/metrics/names',
    method: 'get'
  })
}

// 获取指标标签键列表
export function getMetricTagKeys(metricName: string) {
  return request({
    url: '/visualization/metrics/tag-keys',
    method: 'get',
    params: { metricName }
  })
}

// 获取指标标签值列表
export function getMetricTagValues(metricName: string, tagKey: string) {
  return request({
    url: '/visualization/metrics/tag-values',
    method: 'get',
    params: { metricName, tagKey }
  })
}

/**
 * 报表相关API
 */

// 获取报表列表
export function getReportList(params: any) {
  return request({
    url: '/visualization/reports',
    method: 'get',
    params
  })
}

// 获取报表详情
export function getReportDetail(id: number | string) {
  return request({
    url: `/visualization/reports/${id}`,
    method: 'get'
  })
}

// 创建报表
export function createReport(data: any) {
  return request({
    url: '/visualization/reports',
    method: 'post',
    data
  })
}

// 更新报表
export function updateReport(id: number | string, data: any) {
  return request({
    url: `/visualization/reports/${id}`,
    method: 'put',
    data
  })
}

// 删除报表
export function deleteReport(id: number | string) {
  return request({
    url: `/visualization/reports/${id}`,
    method: 'delete'
  })
}

// 手动生成报表
export function generateReport(id: number | string) {
  return request({
    url: `/visualization/reports/${id}/generate`,
    method: 'post'
  })
}

// 下载报表
export function downloadReport(id: number | string) {
  return request({
    url: `/visualization/reports/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
} 