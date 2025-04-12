import { Asset } from './asset';

/**
 * 采集任务类型
 */
export interface CollectorTask {
  id?: number;
  name: string;
  code: string;
  type: string;
  protocol: string;
  assetId: number;
  interval: number;
  metrics: CollectorMetric[];
  connectionParams: Record<string, string>;
  status: number;
  description?: string;
  lastExecuteTime?: string;
  lastExecuteStatus?: number;
  createTime?: string;
  updateTime?: string;
  
  // UI显示额外字段
  assetName?: string;
}

/**
 * 采集指标类型
 */
export interface CollectorMetric {
  name: string;
  description?: string;
  path: string;
  dataType: string;
  unit?: string;
  enabled: boolean;
}

/**
 * 采集任务调度配置类型
 */
export interface CollectorTaskSchedule {
  id?: number;
  name: string;
  taskId: number;
  taskName?: string;
  scheduleType: number;
  fixedRate?: number;
  cronExpression?: string;
  executeTime?: string;
  startTime?: string;
  endTime?: string;
  maxRetries?: number;
  retryInterval?: number;
  enabled: number;
  lastExecuteTime?: string;
  nextExecuteTime?: string;
  createTime?: string;
  updateTime?: string;
}

/**
 * 采集任务实例类型
 */
export interface CollectorTaskInstance {
  id?: number;
  taskId: number;
  assetId: number;
  startTime: string;
  endTime?: string;
  status: number;
  errorMessage?: string;
  dataPointCount?: number;
  createTime?: string;
  updateTime?: string;
  
  // UI显示额外字段
  taskName?: string;
  assetName?: string;
}

/**
 * 采集指标数据类型
 */
export interface CollectorMetricData {
  id?: number;
  taskId: number;
  instanceId: number;
  assetId: number;
  metricName: string;
  metricLabels?: Record<string, string>;
  metricValue: number;
  collectTime: string;
  createTime?: string;
  
  // UI显示额外字段
  taskName?: string;
  assetName?: string;
}

/**
 * 调度类型枚举
 */
export enum ScheduleType {
  FixedRate = 1,
  Cron = 2,
  OneTime = 3
}

/**
 * 采集任务查询参数
 */
export interface CollectorTaskQueryParams {
  name?: string;
  type?: string;
  status?: number;
  page: number;
  size: number;
}

/**
 * 调度配置查询参数
 */
export interface CollectorScheduleQueryParams {
  name?: string;
  taskId?: number;
  enabled?: number;
  page: number;
  size: number;
} 