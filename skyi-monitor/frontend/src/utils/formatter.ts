/**
 * 格式化日期时间
 * @param {string | number | Date} time 时间
 * @param {string} pattern 格式化模式，默认为 yyyy-MM-dd HH:mm:ss
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(time: string | number | Date, pattern = 'yyyy-MM-dd HH:mm:ss'): string {
  if (!time) {
    return ''
  }
  
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if (typeof time === 'string') {
      if (/^[0-9]+$/.test(time)) {
        time = parseInt(time)
      } else {
        // 处理 ISO 格式日期字符串等
        time = time.replace(new RegExp(/-/gm), '/')
      }
    }
    
    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000
    }
    date = new Date(time)
  }
  
  const formatObj: Record<string, number> = {
    y: date.getFullYear(),
    M: date.getMonth() + 1,
    d: date.getDate(),
    H: date.getHours(),
    m: date.getMinutes(),
    s: date.getSeconds(),
    S: date.getMilliseconds()
  }
  
  return pattern.replace(/(y+|M+|d+|H+|m+|s+|S+)/g, function(match) {
    const key = match.charAt(0) as keyof typeof formatObj
    const value = formatObj[key]
    
    // 根据格式返回对应长度的值
    if (key === 'y') {
      return String(value).substr(4 - match.length)
    } else if (key === 'S') {
      return match.length > 1 ? String(value).padStart(match.length, '0') : String(value)
    } else {
      return match.length > 1 ? String(value).padStart(2, '0') : String(value)
    }
  })
}

/**
 * 格式化日期
 * @param {string | number | Date} time 时间
 * @returns {string} 格式化后的日期字符串，如：2023-05-20
 */
export function formatDate(time: string | number | Date): string {
  return formatDateTime(time, 'yyyy-MM-dd')
}

/**
 * 格式化时间
 * @param {string | number | Date} time 时间
 * @returns {string} 格式化后的时间字符串，如：10:30:45
 */
export function formatTime(time: string | number | Date): string {
  return formatDateTime(time, 'HH:mm:ss')
}

/**
 * 格式化文件大小
 * @param {number} size 文件大小（字节）
 * @returns {string} 格式化后的文件大小
 */
export function formatFileSize(size: number): string {
  if (!size) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
  let index = 0
  
  while (size >= 1024 && index < units.length - 1) {
    size /= 1024
    index++
  }
  
  return size.toFixed(2) + ' ' + units[index]
}

/**
 * 格式化数字，添加千位分隔符
 * @param {number} num 数字
 * @returns {string} 格式化后的数字字符串
 */
export function formatNumber(num: number): string {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 格式化百分比
 * @param {number} value 值（0-1之间）
 * @param {number} digits 小数位数
 * @returns {string} 格式化后的百分比字符串
 */
export function formatPercent(value: number, digits = 2): string {
  return (value * 100).toFixed(digits) + '%'
} 