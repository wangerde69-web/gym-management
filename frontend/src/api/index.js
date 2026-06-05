import axios from 'axios'

// 创建 Axios 实例，统一配置基础路径和超时时间
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动附加本地存储的认证 Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
})

// 响应拦截器：统一提取响应数据，处理 401 未授权跳转
request.interceptors.response.use(
  res => res.data,
  err => {
    // 401 未授权：清除 token 并跳转到对应登录页
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      const isAdmin = window.location.pathname.startsWith('/admin')
      window.location.href = isAdmin ? '/admin-login' : '/login'
      return Promise.reject(err)
    }
    return Promise.reject(err)
  }
)

export default request