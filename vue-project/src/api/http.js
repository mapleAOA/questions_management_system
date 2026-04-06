import axios from 'axios'
import { TOKEN_KEY } from '@/stores/auth'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export class ApiError extends Error {
  constructor(message, code = 'REQUEST_ERROR', status = 0, payload = null) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
    this.payload = payload
  }
}

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 20000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const isTimeout = error?.code === 'ECONNABORTED' || /timeout/i.test(String(error?.message || ''))
    if (isTimeout) {
      throw new ApiError('请求超时，后端可能仍在处理中，请稍后刷新查看结果', 'TIMEOUT', 0)
    }
    if (!error.response) {
      throw new ApiError('网络错误，请检查后端服务是否启动', 'NETWORK_ERROR', 0)
    }
    const status = error.response.status
    const payload = error.response.data
    const msg = payload?.msg || error.message || '请求失败'
    const code = payload?.code || `HTTP_${status}`
    throw new ApiError(msg, code, status, payload)
  },
)

function unwrapApiResponse(body) {
  if (body && typeof body === 'object' && typeof body.success === 'boolean') {
    if (!body.success) {
      throw new ApiError(body.msg || '业务失败', body.code || 'BIZ_ERROR', 200, body)
    }
    if (Object.prototype.hasOwnProperty.call(body, 'data')) {
      return body.data
    }
    return body
  }
  return body
}

export async function request(config) {
  const response = await http.request(config)
  return unwrapApiResponse(response.data)
}

export { API_BASE_URL }
