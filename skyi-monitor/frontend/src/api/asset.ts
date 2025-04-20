import request from '@/utils/request'
import { Asset, AssetQueryParams, ApiResponse, PaginationResult } from '@/types/asset'

/**
 * 创建资产
 * @param data 资产数据
 * @returns 创建结果
 */
export function createAsset(data: Partial<Asset>) {
  return request<ApiResponse<Asset>>({
    url: '/asset/assets',
    method: 'post',
    data
  })
}

/**
 * 更新资产
 * @param id 资产ID
 * @param data 资产数据
 * @returns 更新结果
 */
export function updateAsset(id: number | string, data: Partial<Asset>) {
  return request<ApiResponse<Asset>>({
    url: `/asset/assets/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除资产
 * @param id 资产ID
 * @returns 删除结果
 */
export function deleteAsset(id: number | string) {
  return request<ApiResponse<null>>({
    url: `/asset/assets/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取资产
 * @param id 资产ID
 * @returns 资产详情
 */
export function getAssetById(id: number | string) {
  return request<ApiResponse<Asset>>({
    url: `/asset/assets/${id}`,
    method: 'get'
  })
}

/**
 * 根据编码获取资产
 * @param code 资产编码
 * @returns 资产信息
 */
export function getAssetByCode(code: string) {
  return request<ApiResponse<Asset>>({
    url: `/asset/assets/code/${code}`,
    method: 'get'
  })
}

/**
 * 分页查询资产列表
 * @param params 查询参数
 * @returns 资产列表
 */
export function listAssets(params: AssetQueryParams) {
  return request<ApiResponse<PaginationResult<Asset>>>({
    url: '/api/asset/assets',
    method: 'get',
    params
  })
}

/**
 * 根据类型查询资产列表
 * @param type 资产类型
 * @returns 资产列表
 */
export function listAssetsByType(type: string) {
  return request<ApiResponse<Asset[]>>({
    url: `/api/asset/assets/type/${type}`,
    method: 'get'
  })
}

/**
 * 根据部门查询资产列表
 * @param department 部门
 * @returns 资产列表
 */
export function listAssetsByDepartment(department: string) {
  return request<ApiResponse<Asset[]>>({
    url: `/api/asset/assets/department/${department}`,
    method: 'get'
  })
}

/**
 * 根据负责人查询资产列表
 * @param owner 负责人
 * @returns 资产列表
 */
export function listAssetsByOwner(owner: string) {
  return request<ApiResponse<Asset[]>>({
    url: `/api/asset/assets/owner/${owner}`,
    method: 'get'
  })
}

/**
 * 获取资产列表
 * @param params 查询参数
 * @returns 资产列表
 */
export function getAssets(params: {
  pageNum?: number
  pageSize?: number
  name?: string
  type?: string
  status?: number
}) {
  return request<ApiResponse<PaginationResult<Asset>>>({
    url: '/api/asset/assets',
    method: 'get',
    params
  })
} 