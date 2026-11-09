import { t } from '@/i18n'
import { ElMessage } from 'element-plus'
import { handleUnauthorized } from './request'
/** Element Plus uploads use XHR directly, so they need the same session boundary as Axios. */
export function handleUploadError(error: { status?: number }) {
  if (error.status === 401) { handleUnauthorized(); return }
  if (error.status === 403) {
    ElMessage.error(t("权限不足，无法上传文件"))
    if (location.pathname !== '/403') location.replace('/403')
    return
  }
  ElMessage.error(t("上传失败，请检查网络后重试"))
}
