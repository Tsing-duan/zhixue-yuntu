import { ref, computed, type App } from 'vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import messages from '../../../shared/i18n/en.json'
export type Locale = 'zh-CN' | 'en'
const dictionary: Record<string, string> = messages
const key = 'zhixue.locale'
function savedLocale(): Locale {
  const cookie = typeof document === 'undefined' ? '' : document.cookie.split('; ').find(s => s.startsWith('zhixue_locale='))?.split('=')[1]
  const saved = cookie || (typeof localStorage === 'undefined' ? '' : localStorage.getItem(key))
  return saved === 'en' ? 'en' : 'zh-CN'
}
export const locale = ref<Locale>(savedLocale())
export const elementLocale = computed(() => locale.value === 'en' ? en : zhCn)
export function t(value: unknown): string {
  if (value == null) return ''
  const text = String(value)
  if (locale.value !== 'en') return text
  return dictionary[text] ?? dictionary[text.trim()] ?? text
}
export function setLocale(value: string) {
  if (value !== 'en' && value !== 'zh-CN') return
  locale.value = value
  localStorage.setItem(key, value)
  document.cookie = `zhixue_locale=${value}; Path=/; Max-Age=31536000; SameSite=Lax`
  document.documentElement.lang = value
  document.title = value === 'en' ? 'Zhixue Cloud' : '智学云途'
}
export function installI18n(app: App) {
  app.config.globalProperties.$t = t
  Object.defineProperty(app.config.globalProperties, '$locale', { get: () => locale.value })
  Object.defineProperty(app.config.globalProperties, '$elementLocale', { get: () => elementLocale.value })
  document.documentElement.lang = locale.value
  document.title = t('智学云途')
  window.addEventListener('focus', () => { const saved = savedLocale(); if (saved !== locale.value) setLocale(saved) })
}

/** Built-in example text follows locale until the user edits it. Never applied to server content. */
export function localizeExamples<T extends Record<string, any>>(form: T, fields: (keyof T)[]): T {
  for (const field of fields) {
    const source = String(form[field])
    let edited = false
    let value: any
    Object.defineProperty(form, field, { enumerable: true, configurable: true,
      get: () => edited ? value : t(source),
      set: next => { edited = true; value = next },
    })
  }
  return form
}
export function englishLabel(source: string) { return dictionary[source] ?? source }
