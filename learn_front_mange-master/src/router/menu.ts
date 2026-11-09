import type { Menu } from '@/types/api'
import type { RouteRecordRaw } from 'vue-router'
const views = import.meta.glob('../views/**/*.vue')
export function loadView(view: string) {
  const name = '../views' + (view.startsWith('/') ? view : '/' + view) + (view.endsWith('.vue') ? '' : '.vue')
  const loader = views[name]
  if (!loader) throw new Error(`菜单组件不存在：${view}`)
  return loader
}
export function buildMenuRoutes(menus: Menu[]): RouteRecordRaw[] {
  const children: RouteRecordRaw[] = []
  const routes: RouteRecordRaw[] = []
  const paths = new Set<string>()
  for (const menu of menus.filter(m => Number(m.menuType) === 1)) {
    if (!menu.routeUrl?.startsWith('/') || paths.has(menu.routeUrl)) throw new Error('菜单路由为空或重复')
    paths.add(menu.routeUrl)
    let props: Record<string, unknown> | undefined
    if (menu.param) {
      try { const parsed = JSON.parse(menu.param); if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) props = parsed; else throw new Error() }
      catch { throw new Error(`菜单参数格式错误：${menu.menuName}`) }
    }
    const route = { path: menu.routeUrl, name: `menu:${menu.id}`, meta: { title: menu.menuName }, component: loadView(menu.componentUrl), props } as RouteRecordRaw
    if (Number(menu.target) === 1) children.push(route); else routes.push(route)
  }
  routes.push({ path: '/', name: 'main', component: () => import('@/views/layout/index.vue'), redirect: paths.has('/index') ? '/index' : (menus.find(m => Number(m.menuType) === 1)?.routeUrl || '/403'), children })
  routes.push({ path: '/:pathMatch(.*)*', name: 'not-found', redirect: menus.length ? '/404' : '/403' })
  return routes
}
export function displayMenus(menus: Menu[]): Menu[] {
  const build = (parentId: string, seen = new Set<string>()): Menu[] => menus
    .filter(m => String(m.parentId) === parentId && Number(m.menuType) !== 2 && Number(m.visible) === 0 && !seen.has(m.id))
    .map(m => ({ ...m, child: build(m.id, new Set([...seen,m.id])) }))
  return build('0')
}
