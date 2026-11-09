import type { t, Locale } from '../i18n'
import type en from 'element-plus/es/locale/lang/en'
declare module 'vue' {
  interface ComponentCustomProperties {
    $t: typeof t
    $locale: Locale
    $elementLocale: typeof en
    $demo: <T>(value: T) => T
    $isDemo: boolean
    $demoSource: (record: Record<string, any>, field: string) => any
  }
}
export {}
