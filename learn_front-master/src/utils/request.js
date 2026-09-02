import axios from 'axios'
import store from '@/store'
import { Message, MessageBox } from 'element-ui'  //导入element-ui组件库

function clearLocalSession() {
    window.localStorage.removeItem('user_token')
    window.localStorage.removeItem('user_info')
    window.sessionStorage.clear()
    if (window.caches) {
        window.caches.keys().then(keys => Promise.all(keys.map(key => window.caches.delete(key)))).catch(() => {})
    }
}

function handleUnauthorized() {
    clearLocalSession()
    Message.error('登录已失效，请重新登录')
    if (window.location.pathname !== '/login') {
        window.location.replace('/login')
    }
    return new Promise(() => {})
}

function handleForbidden() {
    Message.error('权限不足，无法访问该功能')
    return new Promise(() => {})
}
 
// 创建axios的对象
const instance = axios.create({
    baseURL: process.env.VUE_APP_BASE_URL,
    timeout: 60000
})
 
// 请求拦截
// 所有的网络请求都会走这个方法,可以在请求添加自定义内容
instance.interceptors.request.use(
    function (config) {
        if (config.method === 'post') {
            config.data = {
              ...config.data,
              _t: Date.parse(new Date()) / 1000 // 时间戳
            }
        } else if (config.method === 'get') {
            config.params = {
              random: Math.random(), // 随机数
              ...config.params
            }
        }
        config.headers.x_access_token = window.localStorage.getItem("user_token") // 请求头添加token值
        return config
    },
    function (err) {
        return Promise.reject(err)
    }
)
 
// 响应拦截
// 此处可以根据服务器返回的状态码做相应的数据
instance.interceptors.response.use(
    function (response) {
        const res = response
        if (res.data.code === 1011 || res.data.code == 1008 || res.data.code == 1006) {
            return handleUnauthorized()
        } else if(res.data.code == 1009) {
            MessageBox.alert('该账号已被锁定', '错误', {
                confirmButtonText: '确定',
                type: 'error'
            }).then(() => {
                store.dispatch('logout').then(() => {
                    location.reload()
                })
            })
            return Promise.reject('error')
        } else if (res.status != 200) {
            MessageBox.alert('请求异常 [' + res.status + ']', '错误', {
                confirmButtonText: '确定',
                type: 'error'
            })
            return Promise.reject(new Error('HTTP状态码: ' + res.status))
        } else {
            return res.data
        }
    },
    function (err) {
        if (err.response && err.response.status === 401) {
            return handleUnauthorized()
        }
        if (err.response && err.response.status === 403) {
            return handleForbidden()
        }
        let errMsg = '系统内部错误，请联系管理员维护'
        if (err.response) {
            const status = err.response.status
            const url = err.response.config ? err.response.config.url : ''
            const data = typeof err.response.data === 'string' ? err.response.data : JSON.stringify(err.response.data)
            errMsg = '请求失败 [' + status + '] ' + url + '\n' + data
        } else if (err.request) {
            errMsg = '网络请求失败，请检查后端服务是否启动\nURL: ' + (err.config ? err.config.url : 'unknown')
        } else {
            errMsg = '请求配置错误: ' + err.message
        }
        MessageBox.alert(errMsg, '错误', {
            confirmButtonText: '确定',
            type: 'error'
        })
        return Promise.reject(err)
    }
)
 
// 封装get和post请求
export function get(url, params) {
    return instance.get(url, {params})
}
 
export function post(url, data) {
    return instance.post(url, data)
}
 
export default instance;
