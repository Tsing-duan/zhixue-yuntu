<template>
  <router-view v-slot="{ Component, route }">
    <keep-alive :key="session.epoch" :include="included" :max="10">
      <component :is="wrapper(route.fullPath)" :key="route.fullPath" :view="Component" />
    </keep-alive>
  </router-view>
</template>
<script setup lang="ts">
import { cloneVNode, computed, defineComponent, markRaw, watch, type Component, type PropType, type VNode } from 'vue'
import { useMenuStore, useSessionStore } from '@/store'
const menus = useMenuStore(); const session = useSessionStore()
const wrappers = new Map<string, Component>()
const names = new Map<string, string>()
let serial = 0
function name(path: string) { if (!names.has(path)) names.set(path, `CachedPage${++serial}`); return names.get(path)! }
function wrapper(path: string) {
  if (!wrappers.has(path)) wrappers.set(path, markRaw(defineComponent({ name: name(path), props: { view: Object as PropType<VNode> }, setup: props => () => props.view ? cloneVNode(props.view) : null })))
  return wrappers.get(path)!
}
const included = computed(() => menus.activeMenuArrary.map(tab => name(tab.url)))
watch(() => session.epoch, () => { wrappers.clear(); names.clear() })
watch(() => menus.activeMenuArrary.map(t => t.url), urls => {
  for (const path of wrappers.keys()) if (!urls.includes(path)) { wrappers.delete(path); names.delete(path) }
})
</script>
