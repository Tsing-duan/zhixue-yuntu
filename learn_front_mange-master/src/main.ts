import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { pinia, useConfigStore, useSessionStore, useMenuStore } from './store'
import './permission'
import ElementPlus from 'element-plus'
import { installI18n } from './i18n'
import LanguageSwitch from './components/LanguageSwitch.vue'
import DemoNotice from './components/DemoNotice.vue'
import { localizeDemo, isDemo, demoSource } from './i18n/demo'
import 'element-plus/dist/index.css'
import 'quill/dist/quill.snow.css'
import './assets/css/legacy-icons.css'
import './assets/css/modernization.css'
import { installDictionary } from './utils/dict'
import RichTextEditor from './components/RichTextEditor.vue'
import { installEventBus } from './utils/eventBus'
import { handleUploadError } from './utils/upload'
const app = createApp(App)
app.config.globalProperties.$uploadError = handleUploadError
app.use(pinia)
app.use(ElementPlus)
installI18n(app)
app.component('LanguageSwitch', LanguageSwitch)
app.component('DemoNotice', DemoNotice)
app.config.globalProperties.$demoSource = demoSource
app.config.globalProperties.$demo = localizeDemo
app.config.globalProperties.$isDemo = isDemo
app.component('quill-editor', RichTextEditor)
installEventBus(app)
app.config.globalProperties.$config = useConfigStore(pinia)
app.config.globalProperties.$session = useSessionStore(pinia)
app.config.globalProperties.$menus = useMenuStore(pinia)
installDictionary(app)
const fallback = '/img/avatars/zhixue-admin-default.png'
app.config.globalProperties.$resolveAvatar = (avatar?: string, defaultPath = fallback) => {
  const value = avatar || defaultPath
  if (/^(https?:)?\/\//.test(value) || /^(data:|blob:)/.test(value)) return value
  return useConfigStore(pinia).HOST.replace(/\/$/, '') + (value.startsWith('/') ? value : '/' + value)
}
app.config.globalProperties.$avatarError = (event: Event, defaultPath?: string) => {
  const target = event.target as HTMLImageElement
  if (!target || target.dataset.avatarFallback === '1') return
  target.dataset.avatarFallback = '1'; target.src = app.config.globalProperties.$resolveAvatar('', defaultPath)
}
app.use(router)
router.isReady().then(() => app.mount('#app'))
