import { t } from '@/i18n'
import type { App } from 'vue'
import { ElNotification } from 'element-plus'
import { getDictByCode, getDictByCodeAndLabel } from '@/api/api'
export function installDictionary(app: App) {
  app.config.globalProperties.getByCode = async (code: string) => {
    const result = await getDictByCode({ code })
    if (Number(result.code) === 1000) return result.data
    ElNotification.error({ title: t("错误"), message: t("字典获取错误，请稍后重试") })
    return []
  }
  app.config.globalProperties.getByLabel = async (code: string, label: string) => {
    const result = await getDictByCodeAndLabel({ code, label })
    if (Number(result.code) === 1000) return result.data
    ElNotification.error({ title: t("错误"), message: t("字典值获取错误，请稍后重试") })
    return ''
  }
}
