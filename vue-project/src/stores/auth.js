import { defineStore } from 'pinia'
import { authApi } from '@/api/services'

export const TOKEN_KEY = 'qb_token'
export const USER_KEY = 'qb_user'

function safeParse(json, fallback = null) {
  if (!json) {
    return fallback
  }
  try {
    return JSON.parse(json)
  } catch {
    return fallback
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: safeParse(localStorage.getItem(USER_KEY), null),
  }),
  getters: {
    role(state) {
      const raw = state.user?.role || (Array.isArray(state.user?.roles) ? state.user.roles[0] : '')
      return String(raw || '').replace(/^ROLE_/, '').toUpperCase()
    },
    isLoggedIn(state) {
      return !!state.token
    },
    displayName(state) {
      return state.user?.displayName || state.user?.username || ''
    },
  },
  actions: {
    setSession(token, user) {
      this.token = token || ''
      this.user = user || null
      if (this.token) {
        localStorage.setItem(TOKEN_KEY, this.token)
      } else {
        localStorage.removeItem(TOKEN_KEY)
      }
      if (this.user) {
        localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      } else {
        localStorage.removeItem(USER_KEY)
      }
    },
    clearSession() {
      this.setSession('', null)
    },
    hasAnyRole(targetRoles = []) {
      if (!Array.isArray(targetRoles) || targetRoles.length === 0) {
        return true
      }
      const normalize = (role) => String(role || '').replace(/^ROLE_/, '').toUpperCase()
      const mine = normalize(this.role)
      const wants = targetRoles.map(normalize)
      return wants.includes(mine)
    },
    async login(payload) {
      const result = await authApi.login(payload)
      if (!result?.success || !result?.token) {
        throw new Error(result?.msg || 'login failed')
      }
      this.setSession(result.token, result.user)
      return result.user
    },
    async register(payload) {
      const result = await authApi.register(payload)
      if (!result?.success || !result?.token) {
        throw new Error(result?.msg || 'register failed')
      }
      this.setSession(result.token, result.user)
      return result.user
    },
    async fetchProfile() {
      const user = await authApi.me()
      this.user = user
      localStorage.setItem(USER_KEY, JSON.stringify(user))
      return user
    },
    async ensureProfile() {
      if (!this.token) {
        return null
      }
      if (this.user) {
        return this.user
      }
      return this.fetchProfile()
    },
    async logout() {
      try {
        await authApi.logout()
      } catch {
        // 后端会话失效时也允许本地退出，避免卡在当前页面
      } finally {
        this.clearSession()
      }
    },
  },
})
