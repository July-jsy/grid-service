import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true,
})

request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code !== 0) return Promise.reject(new Error(result.message))
    return result.data
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('grid-user')
      if (location.pathname !== '/login') location.href = '/login'
    }
    return Promise.reject(new Error(error.response?.data?.message || error.message || '请求失败'))
  },
)

export default request
