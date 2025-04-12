/**
 * 格式化日期时间
 * @param date 日期对象
 * @param format 格式化模板，默认为 yyyy-MM-dd HH:mm:ss
 * @returns 格式化后的日期字符串
 */
export function formatDateTime(date: Date, format: string = 'yyyy-MM-dd HH:mm:ss'): string {
  if (!date) {
    return '';
  }
  
  const year = date.getFullYear().toString();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const seconds = date.getSeconds().toString().padStart(2, '0');
  
  return format
    .replace('yyyy', year)
    .replace('MM', month)
    .replace('dd', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
}

/**
 * 格式化为日期
 * @param date 日期对象
 * @returns 格式化后的日期字符串，格式为 yyyy-MM-dd
 */
export function formatDate(date: Date): string {
  return formatDateTime(date, 'yyyy-MM-dd');
}

/**
 * 格式化为时间
 * @param date 日期对象
 * @returns 格式化后的时间字符串，格式为 HH:mm:ss
 */
export function formatTime(date: Date): string {
  return formatDateTime(date, 'HH:mm:ss');
} 