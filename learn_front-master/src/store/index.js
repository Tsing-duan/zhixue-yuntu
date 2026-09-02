import Vue from 'vue'
import Vuex from 'vuex'
import { logout } from '@/api/api'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    HOST: process.env.VUE_APP_BASE_URL,
  },
  getters: {
  },
  mutations: {
  },
  actions: {
    logout({ commit }) {
      return new Promise(resolve => {
        logout().then(() => {}).catch(() => {})
        window.localStorage.removeItem("user_token")
        window.localStorage.removeItem("user_info")
        resolve()
      })
    }
  },
  modules: {
  }
})
