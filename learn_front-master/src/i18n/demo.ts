import { locale } from './index'
import source from '../../../shared/i18n/demo-content.json'
type Entry = { original: string; 'zh-CN': string; en: string }
const catalog: Record<string, Record<string, Entry>> = source
export const isDemo = import.meta.env.VITE_DEMO_CONTENT === true
const localized = new WeakSet<object>()
/** Only explicitly enabled fixtures with both matching ID and original field text are localized. */
export function localizeDemo<T>(value: T, enabled = isDemo): T {
  if (!enabled || value === null || typeof value !== 'object') return value
  const record = value as Record<string, any>
  if (localized.has(record)) return value
  localized.add(record)
  if (Array.isArray(value)) { value.forEach(item => localizeDemo(item, enabled)); return value }
  Object.values(record).forEach(item => localizeDemo(item, enabled))
  const entries = catalog[String(record.id)]
  if (!entries) return value
  const rawFields: Record<string, () => unknown> = {}
  for (const [field, entry] of Object.entries(entries)) {
    if (record[field] !== entry.original) continue
    let raw: unknown = record[field]
    rawFields[field] = () => raw
    Object.defineProperty(record, field, {
      enumerable: true, configurable: true,
      get: () => raw === entry.original ? entry[locale.value] : raw,
      set: value => { raw = value },
    })
  }
  if (Object.keys(rawFields).length) Object.defineProperty(record, 'toJSON', {
    configurable: true, enumerable: false,
    value() { return Object.fromEntries(Object.keys(record).map(field => [field, rawFields[field] ? rawFields[field]() : record[field]])) },
  })
  return value
}

/** Preserve fixture source when legacy forms spread a localized record before submitting it. */
export function restoreDemoPayload<T>(value: T, enabled = isDemo): T {
  if (!enabled || value === null || typeof value !== 'object' || value instanceof FormData || value instanceof Date) return value
  if (Array.isArray(value)) return value.map(item => restoreDemoPayload(item, enabled)) as T
  const record = value as Record<string, any>
  const entries = catalog[String(record.id)] || {}
  return Object.fromEntries(Object.entries(record).map(([field, current]) => {
    const entry = entries[field]
    const original = entry && (current === entry['zh-CN'] || current === entry.en) ? entry.original : current
    return [field, restoreDemoPayload(original, enabled)]
  })) as T
}

/** Canonical values for filters and select models; display labels may be localized. */
export function demoSource(record: Record<string, any>, field: string, enabled = isDemo) {
  return restoreDemoPayload(record, enabled)[field]
}
