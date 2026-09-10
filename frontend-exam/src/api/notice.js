import request from './request'

/** 分页列表（管理员） */
export function getNoticePage(params) {
  return request.get('/notice/list', { params })
}

/** 已发布公告列表（公开） */
export function getEnabledNotices() {
  return request.get('/notice/enabled')
}

/** 最新 N 条（公开） */
export function getLatestNotices(limit = 5) {
  return request.get('/notice/latest', { params: { limit } })
}

/** 详情（登录） */
export function getNotice(id) {
  return request.get(`/notice/${id}`)
}

/** 新增（管理员） */
export function createNotice(data) {
  return request.post('/notice', data)
}

/** 更新（管理员） */
export function updateNotice(id, data) {
  return request.put(`/notice/${id}`, data)
}

/** 状态切换（管理员） */
export function updateNoticeStatus(id, status) {
  return request.patch(`/notice/${id}/status`, null, { params: { status } })
}

/** 删除（管理员） */
export function deleteNotice(id) {
  return request.delete(`/notice/${id}`)
}
