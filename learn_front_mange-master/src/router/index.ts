import { createRouter, createWebHistory } from 'vue-router'
import login from "@/views/system/login/login.vue";
import error404 from "@/views/error/404.vue";
import error403 from "@/views/error/403.vue";
import layout from "@/views/layout/index.vue";




const AiData = () => import('@/views/system/aiData/index.vue')

const routes = [
  { path: "/:pathMatch(.*)*", name: "bootstrap-route", component: error404 },
  {
    path: "/login",
    name: "登陆",
    component: login,
  },
  {
    path: "/403",
    name: "403",
    component: error403,
  },
  {
    path: "/404",
    name: "404",
    component: error404,
  },
  {
    path: "/aiData",
    component: layout,
    children: [
      {
        path: "",
        name: "AI数据整理",
        component: AiData,
      },
    ],
  },
];


const router = createRouter({ history: createWebHistory(), routes })
export default router
