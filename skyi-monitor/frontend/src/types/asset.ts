/**
 * 资产标签接口
 */
export interface AssetTag {
  id?: number
  tagKey: string
  tagValue: string
}

/**
 * 资产接口
 */
export interface Asset {
  id?: number
  name: string
  code: string
  type: string
  ipAddress?: string
  port?: number
  department?: string
  owner?: string
  status: number
  description?: string
  tags?: AssetTag[]
  createTime?: string
  updateTime?: string
}

/**
 * 分页查询参数接口
 */
export interface AssetQueryParams {
  name?: string
  type?: string
  status?: number
  page: number
  size: number
}

/**
 * 分页结果接口
 */
export interface PaginationResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  empty: boolean
}

/**
 * API响应接口
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
} 