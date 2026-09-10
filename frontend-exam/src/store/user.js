import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/user'
import { TOKEN_KEY, USER_KEY, ROLE } from '@/utils/constants'

/**
 * 用户状态：token + 用户信息（含 role）
 * token / userInfo 持久化到 localStorage，刷新页面不丢失
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === ROLE.ADMIN)

  /** 登录：保存 token 与用户信息 */
  async function login(form) {
    const data = await loginApi(form)
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      role: data.role
    }
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
    return data
  }

  /** 刷新用户信息（可选，用于 GET /user/info） */
  async function fetchInfo() {
    const data = await getUserInfo()
    userInfo.value = {
      userId: data.id,
      username: data.username,
      nickname: data.nickname,
      role: data.role
    }
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
    return data
  }

  /** 退出登录：通知后端（JWT 无状态，前端丢弃 token 即可） */
  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // 后端失败也照常清理本地状态
    }
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, userInfo, isLoggedIn, isAdmin, login, fetchInfo, logout }
})
