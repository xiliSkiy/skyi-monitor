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

// 采集任务相关类型定义

// 采集任务类型
export type TaskType = 'SNMP' | 'API' | 'SSH' | 'SCRIPT';

// 任务状态
export type TaskStatus = 'CREATED' | 'RUNNING' | 'PAUSED' | 'COMPLETED' | 'CANCELED' | 'FAILED';

// 执行状态
export type ExecutionStatus = 'SUCCESS' | 'FAILURE' | 'RUNNING' | 'CANCELED';

// 时间间隔单位
export type IntervalUnit = 'SECONDS' | 'MINUTES' | 'HOURS' | 'DAYS';

/**
 * 调度类型枚举
 */
export enum ScheduleType {
  FixedRate = 1,
  Cron = 2,
  OneTime = 3
}

// HTTP方法
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

// HTTP头部
export interface HttpHeader {
  key: string;
  value: string;
}

// 脚本参数
export interface ScriptParam {
  key: string;
  value: string;
}

// 采集任务定义
export interface Task {
  id?: string;
  title: string;
  description?: string;
  target: string;
  port?: string;
  type: TaskType;
  status?: TaskStatus;
  
  // SNMP 配置
  snmpVersion?: string;
  community?: string;
  username?: string;
  authProtocol?: string;
  authKey?: string;
  privProtocol?: string;
  privKey?: string;
  oids?: string[];
  
  // API 配置
  httpMethod?: HttpMethod;
  headers?: HttpHeader[];
  requestBody?: string;
  jsonPath?: string;
  
  // SSH 配置
  sshUsername?: string;
  sshKeyFile?: string;
  sshCommand?: string;
  
  // 脚本配置
  scriptType?: string;
  scriptContent?: string;
  scriptParams?: ScriptParam[];
  
  // 调度配置
  scheduleType: ScheduleType;
  executeTime?: number; // 单次执行时间
  interval?: number; // 间隔值
  intervalUnit?: IntervalUnit; // 间隔单位
  cronExpression?: string; // cron表达式
  
  // 标签
  tags?: string[];
  
  // 元数据
  createTime?: number;
  updateTime?: number;
  lastExecuteTime?: number;
}

// 任务执行记录
export interface TaskExecution {
  id: string;
  taskId: string;
  executeTime: number;
  endTime?: number;
  status: ExecutionStatus;
  duration: number;
  node?: string;
  resultData?: any;
  rawOutput?: string;
  errorMessage?: string;
}

// 采集指标
export interface CollectedMetric {
  name: string;
  value: number | string;
  timestamp: number;
  tags?: Record<string, string>;
  unit?: string;
}

// 分页请求参数
export interface TaskQueryParams {
  page: number;
  size: number;
  status?: TaskStatus;
  type?: TaskType;
  keyword?: string;
  tags?: string[];
  startTime?: number;
  endTime?: number;
}

// 分页响应
export interface TaskPage {
  content: Task[];
  total: number;
  page: number;
  size: number;
} 