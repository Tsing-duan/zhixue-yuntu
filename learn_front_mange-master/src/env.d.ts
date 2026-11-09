/// <reference types="vite/client" />
import type { componentBus } from './utils/eventBus'
import type { useConfigStore, useSessionStore, useMenuStore } from './store'
declare module 'vue' {
  interface ComponentCustomProperties {
    $uploadError: (error: { status?: number }) => void
    $bus: ReturnType<typeof componentBus>
    $menus: ReturnType<typeof useMenuStore>
    $config: ReturnType<typeof useConfigStore>
    $session: ReturnType<typeof useSessionStore>
  }
}
export {}
