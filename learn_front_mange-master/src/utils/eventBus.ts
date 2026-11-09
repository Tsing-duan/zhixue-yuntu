import mitt, { type Handler } from 'mitt'
import type { App, ComponentPublicInstance } from 'vue'
const events = mitt<Record<string, unknown>>()
/** Options API bridge with ownership: subscriptions are removed when their component unmounts. */
const owners = new WeakMap<ComponentPublicInstance, Array<() => void>>()
export function componentBus(owner: ComponentPublicInstance) {
  return {
    $emit: (name: string, value?: unknown) => events.emit(name, value),
    $on(name: string, handler: Handler<unknown>) {
      events.on(name, handler)
      const cleanups = owners.get(owner) || []
      cleanups.push(() => events.off(name, handler)); owners.set(owner, cleanups)
    },
    $off: (name: string, handler?: Handler<unknown>) => events.off(name, handler),
  }
}
export function releaseBus(owner: ComponentPublicInstance) { owners.get(owner)?.forEach(fn => fn()); owners.delete(owner) }

export function installEventBus(app: App) {
  app.mixin({ beforeCreate() { this.$bus = componentBus(this) }, beforeUnmount() { releaseBus(this) } })
}
