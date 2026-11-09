import { createRouter, createWebHistory } from 'vue-router'
const login = () => import('@/views/login/login.vue')
const index = () => import('@/views/index/index.vue')
const register = () => import('@/views/register/register.vue')
const task = () => import('@/views/task/task.vue')
const teacher = () => import('@/views/teacher/teacher.vue')
const test = () => import('@/views/testP/test.vue')
const notice = () => import('@/views/notice/notice.vue')
const message = () => import('@/views/message/message.vue')
const article = () => import('@/views/article/article.vue')
const taskInfo = () => import('@/views/task/taskInfo.vue')
const teacherInfo = () => import('@/views/teacher/teacherInfo.vue')
const assignInfo = () => import('@/views/assign/assignInfo.vue')
const testInfo = () => import('@/views/testP/testInfo.vue')
const articleInfo = () => import('@/views/article/articleInfo.vue')
const center = () => import('@/views/center/center.vue')
const myTask = () => import('@/views/task/myTask.vue')
const myAssign = () => import('@/views/assign/myAssign.vue')
const myArticle = () => import('@/views/article/myArticle.vue')
const myFavarArticle = () => import('@/views/article/myFavarArticle.vue')
const myWrongAssign = () => import('@/views/assign/myWrongAssign.vue')
const addArticle = () => import('@/views/article/addArticle.vue')
const editArticle = () => import('@/views/article/editArticle.vue')
const about = () => import('@/views/about/about.vue')
const forum = () => import('@/views/forum/forum.vue')
const forumInfo = () => import('@/views/forum/forumInfo.vue')
const question = () => import('@/views/question/question.vue')
const myFavor = () => import('@/views/task/myFavor.vue')
const rateOfLearning = () => import('@/views/task/rateOfLearning.vue')

const error404 = () => import('@/views/error/404.vue')
const aiChat = () => import('@/views/ai/AiChat.vue')




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
  { path: '/403', component: () => import('@/views/error/403.vue') },
  {
    path: '/404',
    name: '404',
    component: error404,
  },
  { 
    path: '/:pathMatch(.*)*',
    redirect: '/404'
 },
  
]


const router = createRouter({ history: createWebHistory(), routes })
export default router
