import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'

// 创建axios实例（统一通过 /api 代理转发到后端 /backend）
const service = axios.create({
  baseURL: '/api',
  timeout: 60000  // 60秒超时，支持各种大小的AI模型
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    
    // 添加用户信息到header
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (userInfo.userId) {
      config.headers['userId'] = userInfo.userId
      config.headers['realName'] = encodeURIComponent(userInfo.realName || '')
    }
    
    return config
  },
  error => {
    console.log(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果是blob类型（文件下载），直接返回data
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    // 如果code不是200，表示出错
    if (res.code !== 200) {
      Message({
        message: res.message || '请求失败',
        type: 'error',
        duration: 3000
      })
      
      // 401: Token过期
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  error => {
    console.log('err' + error)
    Message({
      message: error.message || '请求失败',
      type: 'error',
      duration: 3000
    })
    return Promise.reject(error)
  }
)

export default service







