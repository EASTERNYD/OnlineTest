import request from './request'

/**
 * 条件分页（登录，POST body）
 * body: { page, size, categoryId?, difficulty?, type?, keyword? }
 */
export function getQuestionPage(data) {
  return request.post('/question/page', data)
}

/** 热门题目（公开） */
export function getHotQuestions(limit = 10) {
  return request.get('/question/hot', { params: { limit } })
}

/** 详情（登录，浏览量 +1） */
export function getQuestion(id) {
  return request.get(`/question/${id}`)
}

/**
 * 新增（管理员）
 * 注意：options 需传 JSON 字符串，如 '["A. xx","B. xx"]'
 */
export function createQuestion(data) {
  return request.post('/question', data)
}

/** 修改（管理员），options 同上需传 JSON 字符串 */
export function updateQuestion(id, data) {
  return request.put(`/question/${id}`, data)
}

/** 删除（管理员） */
export function deleteQuestion(id) {
  return request.delete(`/question/${id}`)
}

/**
 * AI 生成题目（管理员，调用大模型，同步阻塞，耗时长）
 * body: { knowledgePoint, count?, difficulty?, categoryId? }
 * 返回题目数组（不落库），前端展示供勾选后逐条调 createQuestion 保存
 * 超时说明：后端读取超时 180s（llm.read-timeout），前端设为 195s 保证后端的
 * 友好超时提示（BizException → code 500 + msg）先于 axios 通用超时到达用户
 */
export function aiGenerateQuestion(data) {
  return request.post('/question/ai/generate', data, { timeout: 195000 })
}

/** Excel 预览（管理员，multipart/form-data，字段名 file） */
export function previewExcel(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/question/excel/preview', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** Excel 批量导入（管理员），返回成功条数 */
export function importExcel(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/question/excel/import', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 下载导入模板（公开，返回 Blob） */
export function downloadTemplate() {
  return request.get('/question/template', { responseType: 'blob' })
}
