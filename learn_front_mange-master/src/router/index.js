import Vue from "vue";
import VueRouter from "vue-router";
import login from "@/views/system/login/login";
import error404 from "@/views/error/404";
import error403 from "@/views/error/403";
import layout from "@/views/layout/index";
import user from "@/views/system/user/user";

Vue.use(VueRouter);

const AiData = () => import('@/views/system/aiData/index')

const routes = [
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
    path: "/user",
    component: layout,
    children: [
      {
        path: "",
        name: "用户管理",
        component: user,
      },
    ],
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

// 防止连续点击多次路由报错
let routerPush = VueRouter.prototype.push;
let routerReplace = VueRouter.prototype.replace;
// push
VueRouter.prototype.push = function push(location) {
  return routerPush.call(this, location).catch((err) => err);
};
// replace
VueRouter.prototype.replace = function push(location) {
  return routerReplace.call(this, location).catch((err) => err);
};

const router = new VueRouter({
  mode: "history",
  routes,
});

export default router;
