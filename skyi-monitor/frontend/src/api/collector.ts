import request from '@/utils/request'
import {
  CollectorTask,
  CollectorTaskSchedule,
  CollectorTaskInstance,
  CollectorMetricData,
  CollectorTaskQueryParams,
  CollectorScheduleQueryParams
} from '@/types/collector'
import { ApiResponse, PaginationResult } from '@/types/asset'

/**
 * 创建采集任务
 * @param data 任务数据
 * @returns 
 */
export function createCollectorTask(data: CollectorTask) {
  return request<ApiResponse<CollectorTask>>({
    url: '/collector/tasks',
    method: 'post',
    data
  })
}

/**
 * 更新采集任务
 * @param id 任务ID
 * @param data 任务数据
 * @returns 
 */
export function updateCollectorTask(id: number, data: CollectorTask) {
  return request<ApiResponse<CollectorTask>>({
    url: `/collector/tasks/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除采集任务
 * @param id 任务ID
 * @returns 
 */
export function deleteCollectorTask(id: number) {
  return request<ApiResponse<null>>({
    url: `/collector/tasks/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取采集任务
 * @param id 任务ID
 * @returns 
 */
export function getCollectorTaskById(id: number) {
  return request<ApiResponse<CollectorTask>>({
    url: `/collector/tasks/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询采集任务列表
 * @param params 查询参数
 * @returns 
 */
export function listCollectorTasks(params: CollectorTaskQueryParams) {
  return request<ApiResponse<PaginationResult<CollectorTask>>>({
    url: '/collector/tasks',
    method: 'get',
    params
  })
}

/**
 * 启动采集任务
 * @param id 任务ID
 * @returns 
 */
export function startCollectorTask(id: number) {
  return request<ApiResponse<CollectorTask>>({
    url: `/collector/tasks/${id}/start`,
    method: 'put'
  })
}

/**
 * 停止采集任务
 * @param id 任务ID
 * @returns 
 */
export function stopCollectorTask(id: number) {
  return request<ApiResponse<CollectorTask>>({
    url: `/collector/tasks/${id}/stop`,
    method: 'put'
  })
}

/**
 * 立即执行采集任务
 * @param id 任务ID
 * @returns 任务实例ID
 */
export function executeCollectorTaskNow(id: number) {
  return request<ApiResponse<number>>({
    url: `/collector/tasks/${id}/execute`,
    method: 'post'
  })
}

/**
 * 测试采集任务连接
 * @param data 连接参数
 * @returns 
 */
export function testCollectorConnection(data: {
  type: string;
  protocol: string;
  connectionParams: Record<string, string>;
}) {
  return request<ApiResponse<boolean>>({
    url: '/collector/tasks/test-connection',
    method: 'post',
    data
  })
}

// 调度配置相关API

/**
 * 创建调度配置
 * @param data 调度配置数据
 * @returns 
 */
export function createSchedule(data: CollectorTaskSchedule) {
  return request<ApiResponse<CollectorTaskSchedule>>({
    url: '/collector/schedules',
    method: 'post',
    data
  })
}

/**
 * 更新调度配置
 * @param id 调度配置ID
 * @param data 调度配置数据
 * @returns 
 */
export function updateSchedule(id: number, data: CollectorTaskSchedule) {
  return request<ApiResponse<CollectorTaskSchedule>>({
    url: `/collector/schedules/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除调度配置
 * @param id 调度配置ID
 * @returns 
 */
export function deleteSchedule(id: number) {
  return request<ApiResponse<null>>({
    url: `/collector/schedules/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取调度配置
 * @param id 调度配置ID
 * @returns 
 */
export function getScheduleById(id: number) {
  return request<ApiResponse<CollectorTaskSchedule>>({
    url: `/collector/schedules/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询调度配置列表
 * @param params 查询参数
 * @returns 
 */
export function listSchedules(params: CollectorScheduleQueryParams) {
  return request<ApiResponse<PaginationResult<CollectorTaskSchedule>>>({
    url: '/collector/schedules',
    method: 'get',
    params
  })
}

/**
 * 根据任务ID查询调度配置列表
 * @param taskId 任务ID
 * @returns 
 */
export function listSchedulesByTaskId(taskId: number) {
  return request<ApiResponse<CollectorTaskSchedule[]>>({
    url: `/collector/schedules/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 启用调度配置
 * @param id 调度配置ID
 * @returns 
 */
export function enableSchedule(id: number) {
  return request<ApiResponse<CollectorTaskSchedule>>({
    url: `/collector/schedules/${id}/enable`,
    method: 'put'
  })
}

/**
 * 禁用调度配置
 * @param id 调度配置ID
 * @returns 
 */
export function disableSchedule(id: number) {
  return request<ApiResponse<CollectorTaskSchedule>>({
    url: `/collector/schedules/${id}/disable`,
    method: 'put'
  })
}

/**
 * 立即执行调度
 * @param id 调度配置ID
 * @returns 任务实例ID
 */
export function executeScheduleNow(id: number) {
  return request<ApiResponse<number>>({
    url: `/collector/schedules/${id}/execute`,
    method: 'post'
  })
} 