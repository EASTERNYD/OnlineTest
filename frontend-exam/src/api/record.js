import request from './request'

/** 当前用户的考试记录列表（登录） */
export function getRecordList() {
  return request.get('/record/list')
}

/** 记录详情（登录，仅本人，含逐题明细） */
export function getRecordDetail(id) {
  return request.get(`/record/${id}`)
}

/** 删除记录（登录，仅本人） */
export function deleteRecord(id) {
  return request.delete(`/record/${id}`)
}

/** 成绩排行（登录） */
export function getRanking() {
  return request.get('/record/ranking')
}

/** 管理员：所有学生记录分页（?page=&size=&userId=&examId=） */
export function getAdminRecordPage(params) {
  return request.get('/record/admin/list', { params })
}

/** 管理员：任意记录详情 */
export function getAdminRecordDetail(id) {
  return request.get(`/record/admin/${id}`)
}

/** 管理员：删除任意记录 */
export function deleteAdminRecord(id) {
  return request.delete(`/record/admin/${id}`)
}
