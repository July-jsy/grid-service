import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器：自动附带 Token
request.interceptors.request.use((config) => {
  const stored = sessionStorage.getItem('grid-user')
  if (stored) {
    try {
      const user = JSON.parse(stored)
      if (user.token) config.headers.Authorization = `Bearer ${user.token}`
    } catch {}
  }
  return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code !== 0) return Promise.reject(new Error(result.message))
    return result.data
  },
  (error) => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem('grid-user')
      if (location.pathname !== '/login') location.href = '/login'
    }
    return Promise.reject(new Error(error.response?.data?.message || error.message || '请求失败'))
  },
)

export default request
