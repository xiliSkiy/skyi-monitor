import request from '@/utils/request'
import { Asset, AssetQueryParams, ApiResponse, PaginationResult } from '@/types/asset'

/**
 * 创建资产
 * @param data 资产数据
 * @returns 
 */
export function createAsset(data: Asset) {
  return request<ApiResponse<Asset>>({
    url: '/assets',
    method: 'post',
    data
  })
}

/**
 * 更新资产
 * @param id 资产ID
 * @param data 资产数据
 * @returns 
 */
export function updateAsset(id: number, data: Asset) {
  return request<ApiResponse<Asset>>({
    url: `/assets/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除资产
 * @param id 资产ID
 * @returns 
 */
export function deleteAsset(id: number) {
  return request<ApiResponse<null>>({
    url: `/assets/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取资产
 * @param id 资产ID
 * @returns 
 */
export function getAssetById(id: number) {
  return request<ApiResponse<Asset>>({
    url: `/assets/${id}`,
    method: 'get'
  })
}

/**
 * 根据编码获取资产
 * @param code 资产编码
 * @returns 
 */
export function getAssetByCode(code: string) {
  return request<ApiResponse<Asset>>({
    url: `/assets/code/${code}`,
    method: 'get'
  })
}

/**
 * 分页查询资产列表
 * @param params 查询参数
 * @returns 
 */
export function listAssets(params: AssetQueryParams) {
  return request<ApiResponse<PaginationResult<Asset>>>({
    url: '/assets',
    method: 'get',
    params
  })
}

/**
 * 根据类型查询资产列表
 * @param type 资产类型
 * @returns 
 */
export function listAssetsByType(type: string) {
  return request<ApiResponse<Asset[]>>({
    url: `/assets/type/${type}`,
    method: 'get'
  })
}

/**
 * 根据部门查询资产列表
 * @param department 部门
 * @returns 
 */
export function listAssetsByDepartment(department: string) {
  return request<ApiResponse<Asset[]>>({
    url: `/assets/department/${department}`,
    method: 'get'
  })
}

/**
 * 根据负责人查询资产列表
 * @param owner 负责人
 * @returns 
 */
export function listAssetsByOwner(owner: string) {
  return request<ApiResponse<Asset[]>>({
    url: `/assets/owner/${owner}`,
    method: 'get'
  })
} 