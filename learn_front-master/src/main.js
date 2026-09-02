import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import './permission'
import elementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import VueQuillEditor from 'vue-quill-editor'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'
import 'quill/dist/quill.bubble.css'
import './assets/css/student-polish.css'

document.title = '智学云途'
Vue.use(VueQuillEditor)
Vue.config.productionTip = false
Vue.use(elementUI)

//EventBus事件中转
Vue.prototype.$bus = new Vue()

const DEFAULT_AVATAR = '/img/avatars/zhixue-default.png'

function assetUrl(base, path, fallback = DEFAULT_AVATAR) {
  const value = path || fallback
  if (!value) {
    return ''
  }
  if (/^(https?:)?\/\//.test(value) || value.indexOf('data:') === 0 || value.indexOf('blob:') === 0) {
    return value
  }
  const host = (base || '').replace(/\/$/, '')
  const filePath = value.charAt(0) === '/' ? value : '/' + value
  return host + filePath
}

Vue.prototype.$resolveAvatar = function (avatar, fallback) {
  return assetUrl(store.state.HOST, avatar, fallback)
}

Vue.prototype.$avatarError = function (event, fallback) {
  const target = event && event.target
  if (!target || target.dataset.avatarFallback === '1') {
    return
  }
  target.dataset.avatarFallback = '1'
  target.src = this.$resolveAvatar('', fallback)
}

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
