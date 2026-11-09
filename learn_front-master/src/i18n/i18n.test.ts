// @vitest-environment jsdom
import { beforeEach, expect, it, vi } from 'vitest'
import { locale, setLocale, t, localizeExamples } from './index'
import { localizeDemo, restoreDemoPayload, demoSource } from './demo'
import catalog from '../../../shared/i18n/demo-content.json'
beforeEach(() => { const storage = new Map<string, string>(); vi.stubGlobal('localStorage', { getItem: (key: string) => storage.get(key) ?? null, setItem: (key: string, value: string) => storage.set(key, value) }); setLocale('zh-CN') })
const [id, fields] = Object.entries(catalog).find(([, fields]) => 'taskDescribe' in fields)!
const [field, entry] = Object.entries(fields)[0]
it('defaults to Chinese and persists a selected language without changing unknown text', () => {
  expect(locale.value).toBe('zh-CN'); expect(t('课程')).toBe('课程')
  setLocale('en'); expect(t('课程')).toBe('Courses'); expect(localStorage.getItem('zhixue.locale')).toBe('en')
  expect(document.documentElement.lang).toBe('en'); expect(document.cookie).toContain('zhixue_locale=en')
  expect(t('用户原创内容，不应翻译')).toBe('用户原创内容，不应翻译')
  setLocale('invalid'); expect(locale.value).toBe('en')
})
it('switches built-in examples but preserves a user edit across languages', () => {
  const form = localizeExamples({ goal: '学习目标', enumValue: '男' }, ['goal'])
  setLocale('en'); expect(form.goal).toBe(t('学习目标')); expect(form.enumValue).toBe('男')
  form.goal = 'My own 中文学习目标'; setLocale('zh-CN'); expect(form.goal).toBe('My own 中文学习目标')
})
it('localizes only enabled fixtures with matching ID and original field value', () => {
  const fixture = localizeDemo({ id, [field]: entry.original }, true)
  expect(fixture[field]).toBe(entry['zh-CN']); setLocale('en'); expect(fixture[field]).toBe(entry.en)
  expect(localizeDemo({ id: 'real-user-record', [field]: entry.original }, true)[field]).toBe(entry.original)
  expect(localizeDemo({ id, [field]: '真实用户新写的内容' }, true)[field]).toBe('真实用户新写的内容')
  expect(localizeDemo({ id, [field]: entry.original }, false)[field]).toBe(entry.original)
})
it('preserves raw source through storage and legacy form spreads, while keeping explicit edits', () => {
  const fixture = localizeDemo({ id, [field]: entry.original, state: '男' }, true)
  setLocale('en')
  expect(JSON.parse(JSON.stringify(fixture))[field]).toBe(entry.original)
  expect(restoreDemoPayload({ ...fixture }, true)[field]).toBe(entry.original)
  expect(restoreDemoPayload({ ...fixture }, true).state).toBe('男')
  fixture[field] = 'An intentional user edit'
  expect(JSON.parse(JSON.stringify(fixture))[field]).toBe('An intentional user edit')
  expect(restoreDemoPayload({ ...fixture }, true)[field]).toBe('An intentional user edit')
})

it('keeps filter and select values canonical in both display languages', () => {
  const fixture = localizeDemo({ id, [field]: entry.original }, true)
  for (const language of ['zh-CN', 'en']) {
    setLocale(language)
    expect(demoSource(fixture, field, true)).toBe(entry.original)
    expect(fixture[field]).toBe(entry[language as 'zh-CN' | 'en'])
  }
})
