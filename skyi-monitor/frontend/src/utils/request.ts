import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

// 创建Axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 可以在这里添加认证信息等
    return config
  },
  (error) => {
    console.error('Request error: ', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    
    // 如果状态码不是200，则判断为错误
    if (res.code !== 200) {
      ElMessage({
        message: res.message || '请求出错了',
        type: 'error',
        duration: 5 * 1000
      })
      
      // 401: 未授权，需要登录；403: 禁止访问
      if (res.code === 401 || res.code === 403) {
        // 这里可以添加跳转到登录页的逻辑
      }
      
      return Promise.reject(new Error(res.message || '请求出错了'))
    } else {
      return res
    }
  },
  (error) => {
    console.error('Response error: ', error)
    ElMessage({
      message: error.message || '请求出错了',
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

// 封装请求方法
const request = <T = any>(config: AxiosRequestConfig): Promise<T> => {
  return service(config) as unknown as Promise<T>
}

export default request 