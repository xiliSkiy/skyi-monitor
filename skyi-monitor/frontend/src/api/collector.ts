import request from '@/utils/request'
import type { ApiResponse, PaginationResult } from '@/types/api'
import type { CollectorTask } from '@/types/collector'
import {
  CollectorTaskSchedule,
  CollectorTaskInstance,
  CollectorMetricData,
  CollectorScheduleQueryParams
} from '@/types/collector'

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
 * @returns 操作结果
 */
export function deleteCollectorTask(id: number) {
  return request({
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
 * 查询采集任务列表
 */
export interface CollectorTaskQueryParams {
  name?: string;
  type?: string;
  protocol?: string;
  status?: number | null;
  assetId?: number;
  page?: number;
  size?: number;
}

/**
 * 查询采集任务列表
 * @param params 查询参数
 * @returns 任务列表
 */
export function listCollectorTasks(params: CollectorTaskQueryParams) {
  return request({
    url: '/collector/tasks',
    method: 'get',
    params
  })
}

/**
 * 启用采集任务
 * @param id 任务ID
 * @returns 操作结果
 */
export function startCollectorTask(id: number) {
  return request({
    url: `/collector/tasks/${id}/start`,
    method: 'put'
  })
}

/**
 * 禁用采集任务
 * @param id 任务ID
 * @returns 操作结果
 */
export function stopCollectorTask(id: number) {
  return request({
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

/**
 * 查询任务实例列表
 * @param params 查询参数
 * @returns 任务实例列表
 */
export function listCollectorTaskInstances(params: any) {
  return request({
    url: '/collector/instances',
    method: 'get',
    params
  })
}

/**
 * 获取任务实例详情
 * @param id 实例ID
 * @returns 实例详情
 */
export function getCollectorTaskInstanceById(id: number) {
  return request({
    url: `/collector/instances/${id}`,
    method: 'get'
  })
}

/**
 * 获取任务最近一次执行实例
 * @param taskId 任务ID
 * @returns 最近一次执行实例
 */
export function getLatestTaskInstance(taskId: number) {
  return request({
    url: '/collector/instances/latest',
    method: 'get',
    params: { taskId }
  })
}

/**
 * 立即执行任务
 * @param id 任务ID
 * @returns 执行结果
 */
export function executeTaskNow(id: number) {
  return request({
    url: `/collector/tasks/${id}/execute`,
    method: 'post'
  })
}

/**
 * 获取采集指标数据
 * @param params 查询参数
 * @returns 采集指标数据
 */
export function getMetricDataByInstanceId(params: {
  instanceId: number;
  page?: number;
  size?: number;
}) {
  return request<ApiResponse<PaginationResult<CollectorMetricData>>>({
    url: '/collector/metrics',
    method: 'get',
    params
  })
}

/**
 * 获取调度规则列表
 * @param params 查询参数
 * @returns 调度规则列表
 */
export function listCollectorRules(params: any) {
  return request({
    url: '/collector/rules',
    method: 'get',
    params
  })
}

/**
 * 获取调度规则详情
 * @param id 规则ID
 * @returns 规则详情
 */
export function getCollectorRuleById(id: number) {
  return request({
    url: `/collector/rules/${id}`,
    method: 'get'
  })
}

/**
 * 创建调度规则
 * @param data 规则数据
 * @returns 创建结果
 */
export function createCollectorRule(data: any) {
  return request({
    url: '/collector/rules',
    method: 'post',
    data
  })
}

/**
 * 更新调度规则
 * @param id 规则ID
 * @param data 规则数据
 * @returns 更新结果
 */
export function updateCollectorRule(id: number, data: any) {
  return request({
    url: `/collector/rules/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除调度规则
 * @param id 规则ID
 * @returns 删除结果
 */
export function deleteCollectorRule(id: number) {
  return request({
    url: `/collector/rules/${id}`,
    method: 'delete'
  })
}

/**
 * 启用调度规则
 * @param id 规则ID
 * @returns 操作结果
 */
export function enableCollectorRule(id: number) {
  return request({
    url: `/collector/rules/${id}/enable`,
    method: 'put'
  })
}

/**
 * 禁用调度规则
 * @param id 规则ID
 * @returns 操作结果
 */
export function disableCollectorRule(id: number) {
  return request({
    url: `/collector/rules/${id}/disable`,
    method: 'put'
  })
} 