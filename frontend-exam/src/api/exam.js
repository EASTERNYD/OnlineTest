import request from './request'

/** 试卷分页（登录，?page=&size=） */
export function getExamPage(params) {
  return request.get('/exam/list', { params })
}

/** 试卷详情（管理员，题目含答案与解析） */
export function getExamDetail(id) {
  return request.get(`/exam/${id}`)
}

/** 手动创建试卷（管理员）body: { name, totalScore, duration, questionIds } */
export function createExam(data) {
  return request.post('/exam', data)
}

/** 智能组卷（管理员）body: { name, duration, rules: [{categoryId,count,difficulty?}] }，返回新试卷 ID */
export function composeExam(data) {
  return request.post('/exam/ai/compose', data)
}

/** 更新试卷（管理员） */
export function updateExam(id, data) {
  return request.put(`/exam/${id}`, data)
}

/** 发布/停止（管理员）?status= */
export function updateExamStatus(id, status) {
  return request.patch(`/exam/${id}/status`, null, { params: { status } })
}

/** 删除（管理员） */
export function deleteExam(id) {
  return request.delete(`/exam/${id}`)
}

/** 开始考试（登录），返回 { recordId, examId, name, duration, totalScore, questions } */
export function startExam(id) {
  return request.post(`/exam/${id}/start`)
}

/** 提交答案（登录）body: { recordId, answers: {"题目ID":"答案"} } */
export function submitExam(id, data) {
  return request.post(`/exam/${id}/submit`, data)
}
