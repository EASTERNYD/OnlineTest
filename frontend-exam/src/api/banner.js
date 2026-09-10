import request from './request'

/** 分页列表（管理员） */
export function getBannerPage(params) {
  return request.get('/banner/list', { params })
}

/** 已启用列表（公开） */
export function getEnabledBanners() {
  return request.get('/banner/enabled')
}

/** 详情（登录） */
export function getBanner(id) {
  return request.get(`/banner/${id}`)
}

/** 新增（管理员） */
export function createBanner(data) {
  return request.post('/banner', data)
}

/** 修改（管理员） */
export function updateBanner(id, data) {
  return request.put(`/banner/${id}`, data)
}

/** 状态切换（管理员） */
export function updateBannerStatus(id, status) {
  return request.patch(`/banner/${id}/status`, null, { params: { status } })
}

/** 删除（管理员） */
export function deleteBanner(id) {
  return request.delete(`/banner/${id}`)
}
