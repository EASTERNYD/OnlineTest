import request from './request'

/** Dashboard 数据概览（管理员） */
export function getDashboardStats() {
  return request.get('/stats/dashboard')
}
