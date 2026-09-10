import axios from 'axios'
import { ElMessage } from 'element-plus'
import { TOKEN_KEY } from '@/utils/constants'

/**
 * Axios 实例
 * - 开发环境走 Vite 代理（/api、/upload → http://localhost:8080）
 * - 请求拦截：自动携带 Authorization: Bearer <token>
 * - 响应拦截：后端两种错误形态都要处理：
 *   1. HTTP 200 + body.code != 200  → 业务错误（500/404 等），提示后 reject
 *   2. HTTP 401/403（JWT 拦截器直接 setStatus）→ 清 token 跳登录 / 提示无权限
 * - 成功时直接返回 data 字段（Result 已解包）
 */
const service = axios.create({
  baseURL: '/api/v1',
  timeout: 30000
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (res) => {
    // 文件下载（responseType = 'blob'）：直接返回二进制
    if (res.config.responseType === 'blob') {
      return res.data
    }
    const { code, msg, data } = res.data
    if (code === 200) {
      return data
    }
    // HTTP 200 但业务失败
    ElMessage.error(msg || '请求失败')
    return Promise.reject(new Error(msg || `业务错误 code=${code}`))
  },
  (err) => {
    const status = err.response?.status
    const msg = err.response?.data?.msg
    if (status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('exam_user')
      ElMessage.error('登录已过期，请重新登录')
      // 直接整页跳转，避免与 Pinia/router 产生循环依赖
      window.location.href = '/login'
    } else if (status === 403) {
      ElMessage.error(msg || '无权限操作')
    } else if (status === 500) {
      ElMessage.error(msg || '服务器内部错误')
    } else if (status === 404) {
      ElMessage.error(msg || '资源不存在')
    } else {
      ElMessage.error(err.message || '网络异常，请稍后重试')
    }
    return Promise.reject(err)
  }
)

export default service
