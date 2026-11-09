// @vitest-environment jsdom
import { it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
vi.mock('@/utils/request', () => ({ default: { get: vi.fn(() => Promise.reject(new Error('offline'))) } }))
import { useSessionStore, TOKEN_KEY, USER_KEY } from './index'
import { sessionGeneration, onSessionReset } from '@/utils/sessionLifecycle'
const storage = new Map<string, string>()
beforeEach(() => { vi.stubGlobal('localStorage', { getItem: (key: string) => storage.get(key) ?? null, setItem: (key: string, value: string) => storage.set(key, String(value)), removeItem: (key: string) => storage.delete(key), clear: () => storage.clear() }); localStorage.clear(); setActivePinia(createPinia()) })
it('changes identity atomically and invalidates pending responses and private data', () => {
 const store=useSessionStore(); store.setToken('first'); store.setUser('first-private-data')
 const before=sessionGeneration(); const reset=vi.fn(); const dispose=onSessionReset(reset)
 store.setToken('second'); expect(store.user).toBe(''); expect(localStorage.getItem(USER_KEY)).toBeNull()
 expect(localStorage.getItem(TOKEN_KEY)).toBe('second'); expect(sessionGeneration()).toBeGreaterThan(before);expect(reset).toHaveBeenCalled();dispose()
})
it('logs out locally even when the server is offline', async () => {
 const store=useSessionStore();store.setToken('first');store.setUser('private');localStorage.setItem('lock','true')
 await store.logout();expect(store.token).toBe('');expect(store.user).toBe('');expect(localStorage.getItem(TOKEN_KEY)).toBeNull();expect(localStorage.getItem('lock')).toBeNull()
})
