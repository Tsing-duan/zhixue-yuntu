import { createPinia, defineStore } from 'pinia'
import { resetSessionResources } from '@/utils/sessionLifecycle'
import request from '@/utils/request'
export const pinia = createPinia()
export const TOKEN_KEY = 'token'
export const USER_KEY = 'user'
export const useConfigStore = defineStore('config', { state: () => ({ HOST: import.meta.env.VITE_API_BASE_URL as string }) })
export const useSessionStore = defineStore('session', {
  state: () => ({ token: localStorage.getItem(TOKEN_KEY) || '', user: localStorage.getItem(USER_KEY) || '', epoch: 0 }),
  getters: { getToken: state => state.token, getUser: state => state.user },
  actions: {
    setToken(value: string) {
      if (value !== this.token) { resetSessionResources(); this.epoch++; this.user = ''; localStorage.removeItem(USER_KEY) }
      this.token = value
      if (value) localStorage.setItem(TOKEN_KEY, value); else localStorage.removeItem(TOKEN_KEY)
    },
    setUser(value: string) { this.user = value; if (value) localStorage.setItem(USER_KEY, value); else localStorage.removeItem(USER_KEY) },
    clear() {
      this.setToken(''); this.setUser(''); localStorage.removeItem('lock')
      resetSessionResources(); this.epoch++
    },
    async logout() {
      const token = this.token
      this.clear()
      try { await request.get('/login/logout', { headers: { x_access_token: token }, sessionIndependent: true }) } catch { /* Local logout must succeed offline too. */ }
    },
  },
})
export { useMenuStore } from './menu'
