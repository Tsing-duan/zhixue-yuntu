import { t, locale } from '@/i18n'
import { localizeDemo, restoreDemoPayload } from '@/i18n/demo'
import axios, { type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { pinia, TOKEN_KEY, useSessionStore } from '@/store'
import { sessionGeneration } from './sessionLifecycle'
import type { ApiResult } from '@/types/api'
declare module 'axios' {
  interface AxiosRequestConfig { sessionIndependent?: boolean; sessionEpoch?: number }
}
export class SessionChangedError extends Error { constructor() { super('Session changed; stale response discarded') } }
const instance = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL, timeout: 60000 })
instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  config.sessionEpoch = sessionGeneration()
  config.headers.set('Accept-Language', locale.value)
  if (config.method === 'post' && !(config.data instanceof FormData)) config.data = { ...restoreDemoPayload(config.data), _t: Math.floor(Date.now()/1000) }
  if (config.method === 'get') config.params = { random: Math.random(), ...config.params }
  if (!config.headers.has('x_access_token')) config.headers.set('x_access_token', localStorage.getItem(TOKEN_KEY) || '')
  return config
}, undefined, { synchronous: true })
export function handleUnauthorized() {
  useSessionStore(pinia).clear()
  ElMessage.error(t("登录已失效，请重新登录"))
  if (location.pathname !== '/login') location.replace('/login')
}
function stale(config?: AxiosRequestConfig) { return !config?.sessionIndependent && config?.sessionEpoch !== sessionGeneration() }
instance.interceptors.response.use(response => {
  if (stale(response.config)) throw new SessionChangedError()
  const data = response.data as ApiResult
  if (data && typeof data === 'object') { data.data = localizeDemo(data.data); if (data.message) data.message = t(data.message) }
  if ([1011,1008,1006,1009].includes(Number(data?.code))) {
    if (!response.config.sessionIndependent) handleUnauthorized()
    return Promise.reject(new Error(data.message || t("会话已失效")))
  }
  return response
}, error => {
  if (stale(error.config)) return Promise.reject(new SessionChangedError())
  if (!error.config?.sessionIndependent) {
    if (error.response?.status === 401) handleUnauthorized()
    else if (error.response?.status === 403) {
      ElMessage.error(t("权限不足，无法访问该功能"))
      if (location.pathname !== '/403') location.replace('/403')
    } else if (!axios.isCancel(error)) ElMessage.error(error.code === 'ECONNABORTED' ? t("请求超时，请稍后重试") : t("请求失败，请检查网络连接"))
  }
  return Promise.reject(error)
})
export function get<T = any>(url: string, params?: any): Promise<ApiResult<T>> { return instance.get<ApiResult<T>>(url, { params }).then(r => r.data) }
export function post<T = any>(url: string, data?: any): Promise<ApiResult<T>> { return instance.post<ApiResult<T>>(url, data).then(r => r.data) }
/** Preserve the response body expected by legacy API helpers. */
const request = {
  get: <T = any>(url: string, config?: AxiosRequestConfig) => instance.get<ApiResult<T>>(url, config).then(r => r.data),
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => instance.post<ApiResult<T>>(url, data, config).then(r => r.data),
  request: <T = any>(config: AxiosRequestConfig) => instance.request<ApiResult<T>>(config).then(r => r.data),
}
export default request
