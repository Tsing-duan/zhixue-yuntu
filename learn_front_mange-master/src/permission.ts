import router from './router'
import { pinia, useSessionStore, TOKEN_KEY, useMenuStore } from './store'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { ElMessage } from 'element-plus'
import { onSessionReset, sessionGeneration } from '@/utils/sessionLifecycle'
NProgress.configure({ showSpinner: false })
let removeRoutes: Array<() => void> = []
onSessionReset(() => { removeRoutes.forEach(remove => remove()); removeRoutes = []; useMenuStore(pinia).$reset() })
router.beforeEach(async to => {
  NProgress.start()
  const session = useSessionStore(pinia)
  const stored = localStorage.getItem(TOKEN_KEY) || ''
  if (session.token !== stored) session.setToken(stored)
  if (!stored) return ['/login','/register'].includes(to.path) ? true : '/login'
  if (to.path === '/login') return '/index'
  const menus = useMenuStore(pinia)
  if (!menus.flag) {
    const generation = sessionGeneration()
    try {
      const routes = await menus.generateRoutes()
      removeRoutes = routes.map(route => router.addRoute(route))
      return { path: to.path, query: to.query, hash: to.hash, replace: true }
    } catch (error) {
      if (generation !== sessionGeneration()) return false
      session.clear()
      ElMessage.error(error instanceof Error ? error.message : '菜单加载失败，请重新登录')
      return '/login'
    }
  }
  if (to.path === '/user' && !menus.menus.some(menu => menu.routeUrl === '/user' && Number(menu.menuType) === 1)) return '/403'
  return true
})
router.afterEach(() => NProgress.done())
router.onError(() => NProgress.done())
window.addEventListener('storage', event => {
  if (event.key === TOKEN_KEY || event.key === null) {
    useSessionStore(pinia).clear()
    void router.replace('/login')
  }
})
