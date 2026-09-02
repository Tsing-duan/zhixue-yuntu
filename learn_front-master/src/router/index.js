import Vue from 'vue'
import VueRouter from 'vue-router'
import login from '@/views/login/login'
import index from '@/views/index/index'
import register from '@/views/register/register'
import task from '@/views/task/task'
import teacher from '@/views/teacher/teacher'
import test from '@/views/testP/test'
import notice from '@/views/notice/notice'
import message from '@/views/message/message'
import article from '@/views/article/article'
import taskInfo from '@/views/task/taskInfo'
import teacherInfo from '@/views/teacher/teacherInfo'
import assignInfo from '@/views/assign/assignInfo'
import testInfo from '@/views/testP/testInfo'
import articleInfo from '@/views/article/articleInfo'
import center from '@/views/center/center'
import myTask from '@/views/task/myTask'
import myAssign from '@/views/assign/myAssign'
import myArticle from '@/views/article/myArticle'
import myFavarArticle from '@/views/article/myFavarArticle'
import myWrongAssign from '@/views/assign/myWrongAssign'
import addArticle from '@/views/article/addArticle'
import editArticle from '@/views/article/editArticle'
import about from '@/views/about/about'
import forum from '@/views/forum/forum'
import forumInfo from '@/views/forum/forumInfo'
import question from '@/views/question/question'
import myFavor from '@/views/task/myFavor'
import rateOfLearning from '@/views/task/rateOfLearning'

import error404 from '@/views/error/404'
import aiChat from '@/views/ai/AiChat'

Vue.use(VueRouter)


const routes = [
  {
    path: '/',
    name: 'index',
    component: index
  },
  {
    path: '/myFavor',
    name: 'myFavor',
    component: myFavor
  },
  {
    path: '/forum',
    name: 'forum',
    component: forum
  },
  {
    path: '/question',
    name: 'question',
    component: question
  },
  {
    path: '/forumInfo',
    name: 'forumInfo',
    component: forumInfo
  },
  {
    path: '/rateOfLearning',
    name: 'rateOfLearning',
    component: rateOfLearning
  },
  {
    path: '/login',
    name: 'login',
    component: login
  },
  {
    path: '/register',
    name: 'register',
    component: register
  },
  {
    path: '/teacherRegister',
    redirect: '/login'
  },
  {
    path: '/task',
    name: 'task',
    component: task
  },
  {
    path: '/taskInfo',
    name: 'taskInfo',
    component: taskInfo
  },
  {
    path: '/teacher',
    name: 'teacher',
    component: teacher
  },
  {
    path: '/teacherInfo',
    name: 'teacherInfo',
    component: teacherInfo
  },
  {
    path: '/test',
    name: 'test',
    component: test
  },
  {
    path: '/testInfo',
    name: 'testInfo',
    component: testInfo
  },
  {
    path: '/article',
    name: 'article',
    component: article
  },
  {
    path: '/articleInfo',
    name: 'articleInfo',
    component: articleInfo
  },
  {
    path: '/notice',
    name: 'notice',
    component: notice
  },
  {
    path: '/message',
    name: 'message',
    component: message
  },
  {
    path: '/assignInfo',
    name: 'assignInfo',
    component: assignInfo
  },
  {
    path: '/about',
    name: 'about',
    component: about
  },
  {
    path: '/center',
    name: 'center',
    component: center
  },
  {
    path: '/myTask',
    name: 'myTask',
    component: myTask
  },
  {
    path: '/myAssign',
    name: 'myAssign',
    component: myAssign
  },
  {
    path: '/myArticle',
    name: 'myArticle',
    component: myArticle
  },
  {
    path: '/myFavarArticle',
    name: 'myFavarArticle',
    component: myFavarArticle
  },
  {
    path: '/myWrongAssign',
    name: 'myWrongAssign',
    component: myWrongAssign
  },
  {
    path: '/addArticle',
    name: 'addArticle',
    component: addArticle
  },
  {
    path: '/editArticle',
    name: 'editArticle',
    component: editArticle
  },
  {
    path: '/ai',
    name: 'ai',
    component: aiChat,
  },
  {
    path: '/404',
    name: '404',
    component: error404,
  },
  { 
    path: '/:pathMatch(.*)',
    redirect: '/404'
 },
  
]

// 防止连续点击多次路由报错
let routerPush = VueRouter.prototype.push;
let routerReplace = VueRouter.prototype.replace;
// push
VueRouter.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}
// replace
VueRouter.prototype.replace = function push(location) {
  return routerReplace.call(this, location).catch(err => err)
}

const router = new VueRouter({
  mode: 'history',
  routes
})

export default router
