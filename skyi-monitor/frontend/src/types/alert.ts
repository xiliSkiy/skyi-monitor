/**
 * 告警通知接口
 */
export interface AlertNotification {
  id?: number;
  alertId: number;
  alertUuid: string;
  channel: NotificationChannel;
  recipient?: string;
  content?: string;
  status: NotificationStatus;
  failReason?: string;
  retryCount?: number;
  sentTime?: string;
  createTime?: string;
  updateTime?: string;
}

/**
 * 通知渠道枚举
 */
export enum NotificationChannel {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  WEBHOOK = 'WEBHOOK',
  DINGTALK = 'DINGTALK',
  WEIXIN = 'WEIXIN'
}

/**
 * 通知状态枚举
 */
export enum NotificationStatus {
  PENDING = 'PENDING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED'
}

/**
 * 告警级别枚举
 */
export enum AlertSeverity {
  CRITICAL = 'CRITICAL',
  MAJOR = 'MAJOR',
  MINOR = 'MINOR',
  WARNING = 'WARNING',
  INFO = 'INFO'
}

/**
 * 告警状态枚举
 */
export enum AlertStatus {
  ACTIVE = 'ACTIVE',
  ACKNOWLEDGED = 'ACKNOWLEDGED',
  RESOLVED = 'RESOLVED',
  EXPIRED = 'EXPIRED'
}

/**
 * 告警类型枚举
 */
export enum AlertType {
  THRESHOLD = 'THRESHOLD',
  TREND = 'TREND',
  PATTERN = 'PATTERN'
}

/**
 * 告警接口
 */
export interface Alert {
  id?: number;
  uuid: string;
  name: string;
  message: string;
  severity: AlertSeverity;
  status: AlertStatus;
  type: AlertType;
  assetId: number;
  assetName: string;
  assetType: string;
  metricName: string;
  metricValue?: number;
  threshold?: number;
  notified: boolean;
  acknowledgedBy?: string;
  acknowledgedTime?: string;
  resolvedBy?: string;
  resolvedTime?: string;
  resolveComment?: string;
  createTime?: string;
  updateTime?: string;
}

/**
 * 告警查询参数接口
 */
export interface AlertQueryParams {
  status?: string;
  severity?: string;
  assetId?: number;
  assetType?: string;
  startTime?: string;
  endTime?: string;
  page: number;
  size: number;
}

/**
 * 分页结果接口
 */
export interface PaginationResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  empty: boolean;
}

/**
 * API响应接口
 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
} 