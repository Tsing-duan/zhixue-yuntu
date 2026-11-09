import { t } from '@/i18n'
const ACCOUNT_PATTERN = /^[A-Za-z0-9_]{6,10}$/
const PASSWORD_HAS_LETTER = /[A-Za-z]/
const PASSWORD_HAS_DIGIT = /\d/

export function validateAccount(rule, value, callback) {
  if (!value || !value.trim()) {
    callback(new Error(t("账号不能为空")))
    return
  }
  if (!ACCOUNT_PATTERN.test(value)) {
    callback(new Error(t("账号长度为6-10位，且只能包含字母、数字、下划线")))
    return
  }
  callback()
}

export function validatePassword(rule, value, callback) {
  const message = getPasswordError(value)
  if (message) {
    callback(new Error(message))
    return
  }
  callback()
}

export function getPasswordError(value) {
  if (!value || !value.trim()) {
    return t("密码不能为空")
  }
  if (value.length < 6 || value.length > 10) {
    return t("密码长度为6-10位")
  }
  if (!PASSWORD_HAS_LETTER.test(value) || !PASSWORD_HAS_DIGIT.test(value)) {
    return t("密码至少包含字母和数字")
  }
  return ''
}
