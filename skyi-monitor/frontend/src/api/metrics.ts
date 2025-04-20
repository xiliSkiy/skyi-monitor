import request from '@/utils/request'

/**
 * 分页查询指标列表
 * @param params 查询参数
 * @returns 指标列表
 */
export function getMetrics(params: any) {
  return request({
    url: '/collector/metrics',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询指标
 * @param id 指标ID
 * @returns 指标信息
 */
export function getMetricById(id: number | string) {
  return request({
    url: `/collector/metrics/${id}`,
    method: 'get'
  })
}

/**
 * 根据编码查询指标
 * @param code 指标编码
 * @returns 指标信息
 */
export function getMetricByCode(code: string) {
  return request({
    url: `/collector/metrics/code/${code}`,
    method: 'get'
  })
}

/**
 * 创建指标
 * @param data 指标信息
 * @returns 创建结果
 */
export function createMetric(data: any) {
  return request({
    url: '/collector/metrics',
    method: 'post',
    data
  })
}

/**
 * 更新指标
 * @param id 指标ID
 * @param data 指标信息
 * @returns 更新结果
 */
export function updateMetric(id: number | string, data: any) {
  return request({
    url: `/collector/metrics/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除指标
 * @param id 指标ID
 * @returns 删除结果
 */
export function deleteMetric(id: number | string) {
  return request({
    url: `/collector/metrics/${id}`,
    method: 'delete'
  })
}

/**
 * 启用指标
 * @param id 指标ID
 * @returns 启用结果
 */
export function enableMetric(id: number | string) {
  return request({
    url: `/collector/metrics/${id}/enable`,
    method: 'put'
  })
}

/**
 * 禁用指标
 * @param id 指标ID
 * @returns 禁用结果
 */
export function disableMetric(id: number | string) {
  return request({
    url: `/collector/metrics/${id}/disable`,
    method: 'put'
  })
}

/**
 * 根据资产类型查询指标列表
 * @param assetType 资产类型
 * @returns 指标列表
 */
export function getMetricsByAssetType(assetType: string) {
  return request({
    url: `/collector/metrics/asset-type/${assetType}`,
    method: 'get'
  })
}

/**
 * 导入指标
 * @param data 指标列表
 * @returns 导入结果
 */
export function importMetrics(data: any) {
  return request({
    url: '/collector/metrics/import',
    method: 'post',
    data
  })
}

/**
 * 导出指标
 * @param category 指标类别
 * @returns 指标列表
 */
export function exportMetrics(category?: string) {
  return request({
    url: '/collector/metrics/export',
    method: 'get',
    params: { category }
  })
} 