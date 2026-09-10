import request from './request'

/** 登录（公开） */
export function login(data) {
  return request.post('/user/login', data)
}

/** 当前用户信息（登录） */
export function getUserInfo() {
  return request.get('/user/info')
}

/** 退出登录（登录） */
export function logout() {
  return request.post('/user/logout')
}
