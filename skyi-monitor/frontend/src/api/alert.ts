import request from '@/utils/request'

/**
 * 获取告警列表
 * @param params 查询参数
 * @returns 告警列表
 */
export function getAlerts(params: any) {
  return request({
    url: '/api/alert/alerts',
    method: 'get',
    params
  })
}

/**
 * 获取告警详情
 * @param id 告警ID
 * @returns 告警详情
 */
export function getAlertDetail(id: string | number) {
  return request({
    url: `/api/alert/alerts/${id}`,
    method: 'get'
  })
}

/**
 * 确认告警
 * @param id 告警ID
 * @param data 确认信息
 * @returns 操作结果
 */
export function acknowledgeAlert(id: string | number, data: any) {
  return request({
    url: `/api/alert/alerts/${id}/acknowledge`,
    method: 'post',
    data
  })
}

/**
 * 解决告警
 * @param id 告警ID
 * @param data 解决信息
 * @returns 操作结果
 */
export function resolveAlert(id: string | number, data: any) {
  return request({
    url: `/api/alert/alerts/${id}/resolve`,
    method: 'post',
    data
  })
}

/**
 * 获取资产告警
 * @param assetId 资产ID
 * @param params 查询参数
 * @returns 资产告警列表
 */
export function getAssetAlerts(assetId: string | number, params: any) {
  return request({
    url: `/api/alert/assets/${assetId}/alerts`,
    method: 'get',
    params
  })
}

/**
 * 获取告警统计信息
 * @returns 统计信息
 */
export function getAlertStats() {
  return request({
    url: '/api/alert/stats',
    method: 'get'
  })
}

/**
 * 按告警级别统计
 * @returns 按级别统计的结果
 */
export function getAlertStatsBySeverity() {
  return request({
    url: '/api/alert/stats/severity',
    method: 'get'
  })
}

/**
 * 按资产类型统计告警
 * @returns 按资产类型统计的结果
 */
export function getAlertStatsByAssetType() {
  return request({
    url: '/api/alert/stats/asset-type',
    method: 'get'
  })
}

/**
 * 按日期范围统计告警
 * @param params 日期范围参数
 * @returns 按日期统计的结果
 */
export function getAlertStatsByDateRange(params: any) {
  return request({
    url: '/api/alert/stats/date-range',
    method: 'get',
    params
  })
}

/**
 * 获取告警相关的通知记录
 * @param alertId 告警ID
 * @returns 通知记录列表
 */
export function getAlertNotifications(alertId: string | number) {
  return request({
    url: `/api/alert/alerts/${alertId}/notifications`,
    method: 'get'
  })
}
 