let generation = 0
const listeners = new Set<() => void>()
export const sessionGeneration = () => generation
export function onSessionReset(listener: () => void) { listeners.add(listener); return () => listeners.delete(listener) }
export function resetSessionResources() { generation++; listeners.forEach(fn => fn()) }
