import request from './request'

/**
 * 上传文件（管理员），multipart/form-data，字段名 file
 * 返回 { url, name }，url 为相对路径（如 /upload/xxx.png）
 */
export function uploadFile(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/file/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
