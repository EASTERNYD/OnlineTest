import request from './request'

/** 分类列表（登录，含题目数） */
export function getCategoryList() {
  return request.get('/category/list')
}

/** 分类树（登录） */
export function getCategoryTree() {
  return request.get('/category/tree')
}

/** 新增（管理员） */
export function createCategory(data) {
  return request.post('/category', data)
}

/** 更新（管理员） */
export function updateCategory(id, data) {
  return request.put(`/category/${id}`, data)
}

/** 删除（管理员，被题目引用时失败） */
export function deleteCategory(id) {
  return request.delete(`/category/${id}`)
}
