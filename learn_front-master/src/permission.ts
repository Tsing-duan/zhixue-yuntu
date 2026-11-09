import router from './router'
import { pinia, useSessionStore, TOKEN_KEY } from './store'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { ElMessage } from 'element-plus'
import { onSessionReset } from '@/utils/sessionLifecycle'
NProgress.configure({ showSpinner: false })
router.beforeEach(async to => {
  NProgress.start()
  const session = useSessionStore(pinia)
  const stored = localStorage.getItem(TOKEN_KEY) || ''
  if (session.token !== stored) session.setToken(stored)
  if (!stored) return ['/login','/register'].includes(to.path) ? true : '/login'
  if (to.path === '/login') return '/'
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
