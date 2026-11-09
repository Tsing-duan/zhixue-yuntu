import { defineStore } from 'pinia'
import { getMenuByUser } from '@/api/api'
import type { Menu, Tab } from '@/types/api'
import type { RouteRecordRaw } from 'vue-router'
import { buildMenuRoutes, displayMenus } from '@/router/menu'
import { sessionGeneration } from '@/utils/sessionLifecycle'
export const useMenuStore = defineStore('menu', {
  state: () => ({ activeMenuArrary: [] as Tab[], activeMenu: '/index', flag: false, menus: [] as Menu[], routes: [] as RouteRecordRaw[], displayMenus: [] as Menu[], btnMenus: [] as Menu[] }),
  getters: { getMenu: s => s.menus, getFlag: s => s.flag, getRoutes: s => s.routes, getDisplayMenus: s => s.displayMenus, getBtnMenus: s => s.btnMenus },
  actions: {
    setActiveMenu(value: string) { this.activeMenu = value },
    addActiveMenu(value: Tab) { if (!this.activeMenuArrary.some(t => t.url === value.url)) this.activeMenuArrary.push(value) },
    setActiveMenuArrary(value: Tab[]) { this.activeMenuArrary = value },
    reduceActiveMenu(index: number) { if (index >= 0) this.activeMenuArrary.splice(index,1) },
    setMenus(value: Menu[]) { this.menus = value },
    setFlag(value: boolean) { this.flag = value },
    setRoutes(value: RouteRecordRaw[]) { this.routes = value },
    setDisplayMenus(value: Menu[]) { this.displayMenus = value },
    setBtnMenus(value: Menu[]) { this.btnMenus = value },
    async generateRoutes() {
      const generation = sessionGeneration()
      const result = await getMenuByUser()
      if (generation !== sessionGeneration()) throw new Error('登录身份已变化，请重新登录')
      if (Number(result.code) !== 1000 || !Array.isArray(result.data)) throw new Error(result.message || '菜单加载失败，请重新登录')
      const menus = result.data as Menu[]
      const routes = buildMenuRoutes(menus)
      this.menus = menus; this.displayMenus = displayMenus(menus)
      this.btnMenus = menus.filter(m => Number(m.menuType) === 2)
      // Routes are registered by the guard; keep their component loaders out of reactive UI state.
      const first = menus.find(m => m.routeUrl === '/index' && Number(m.menuType) === 1) || menus.find(m => Number(m.menuType) === 1 && Number(m.visible) === 0)
      if (first) { this.activeMenu = first.routeUrl; this.addActiveMenu({ name: first.menuName, url: first.routeUrl }) }
      this.flag = true
      return routes
    },
  },
})
