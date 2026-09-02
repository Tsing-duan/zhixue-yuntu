<template>
    <div class="header" :class="{ scrolled: isScrolled }">
        <div class="system-title" @click="$router.push('/')">
            <img class="system-mark" src="../../assets/image/brand/zhixue-mark.svg" alt="">
            <span>智学云途</span>
        </div>
        <div class="menu-list">
            <div
                v-for="item in menuItems"
                :key="item.path"
                @click="toPage(item.name)"
                :class="{ 'menu_item': $route.path === item.path }"
            >
                <i class="menu-icon" :class="item.icon"></i>
                {{ item.title }}
            </div>
            <div
                class="ai-menu-item"
                @click="$router.push('/ai')"
                :class="{ 'menu_item': $route.path === '/ai' }"
            >
                <i class="el-icon-cpu"></i> {{ aiMenuTitle }}
            </div>
        </div>
        <div class="center">
            <el-dropdown @command="handleCommand" placement="bottom-end">
                <span class="el-dropdown-link">
                    <img
                        :src="$resolveAvatar(userInfo.avatar, '/img/avatars/zhixue-default.png')"
                        :alt="userInfo.userName"
                        @error="$avatarError($event, '/img/avatars/zhixue-default.png')">
                </span>
                <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="a">
                        <div class="drop-item"><i class="el-icon-user"></i>个人中心</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="b">
                        <div class="drop-item"><i class="el-icon-s-management"></i>我的课程</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="i">
                        <div class="drop-item"><i class="el-icon-star-off"></i>收藏课程</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="c">
                        <div class="drop-item"><i class="el-icon-edit"></i>记笔记</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="d">
                        <div class="drop-item"><i class="el-icon-reading"></i>我的作业</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="e">
                        <div class="drop-item"><i class="el-icon-notebook-1"></i>我的笔记</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="f">
                        <div class="drop-item"><i class="el-icon-star-off"></i>收藏笔记</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="g">
                        <div class="drop-item"><i class="el-icon-circle-close"></i>错题集</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="j" divided>
                        <div class="drop-item"><i class="el-icon-cpu"></i>{{ aiMenuTitle }}</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="h">
                        <div class="drop-item"><i class="el-icon-back"></i>退出登陆</div>
                    </el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
            <span class="username">{{ userInfo.userName }}</span>
        </div>
    </div>
</template>

<script>
import { getUserInfo } from '@/api/api'

export default {
    name: 'Header',
    data() {
        return {
            userInfo: {},
            isScrolled: false,
            menuItems: [
                { name: 'index', path: '/', title: '主页', icon: 'el-icon-house' },
                { name: 'task', path: '/task', title: '课程', icon: 'el-icon-reading' },
                { name: 'teacher', path: '/teacher', title: '教师', icon: 'el-icon-user' },
                { name: 'test', path: '/test', title: '考试', icon: 'el-icon-edit-outline' },
                { name: 'article', path: '/article', title: '笔记', icon: 'el-icon-notebook-2' },
                { name: 'forum', path: '/forum', title: '讨论', icon: 'el-icon-chat-dot-round' },
                { name: 'notice', path: '/notice', title: '公告', icon: 'el-icon-bell' },
                { name: 'message', path: '/message', title: '留言', icon: 'el-icon-message' },
                { name: 'about', path: '/about', title: '关于', icon: 'el-icon-info' }
            ]
        }
    },
    computed: {
        aiMenuTitle() {
            const info = window.localStorage.getItem('user_info')
            if (info) {
                try {
                    const user = JSON.parse(info)
                    return user.userType === 1 ? 'AI教学助手' : 'AI学习助手'
                } catch (e) {}
            }
            return 'AI学习助手'
        }
    },
    methods: {
        handleCommand(command) {
            const routes = {
                a: '/center',
                b: '/myTask',
                i: '/myFavor',
                c: '/addArticle',
                d: '/myAssign',
                e: '/myArticle',
                f: '/myFavarArticle',
                g: '/myWrongAssign',
                j: '/ai',
                h: null
            };
            
            if (command === 'h') {
                this.logout();
            } else if (routes[command]) {
                this.$router.push(routes[command]);
            }
        },
        
        logout() {
            this.$store.dispatch('logout').then(() => {
                window.localStorage.removeItem('user_info');
                window.localStorage.removeItem('user_token');
                this.$message({
                    message: '退出成功',
                    type: 'success',
                    duration: 1500,
                    customClass: 'custom-message'
                });
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            });
        },
        
        toPage(name) {
            const routeMap = {
                about: '/about',
                index: '/',
                task: '/task',
                teacher: '/teacher',
                message: '/message',
                test: '/test',
                article: '/article',
                notice: '/notice',
                forum: '/forum'
            };
            
            const path = routeMap[name];
            if (path) {
                this.$router.push(path);
            }
        },
        
        handleScroll() {
            this.isScrolled = window.scrollY > 10;
        },

        refreshUserInfo() {
            const userInfo = window.localStorage.getItem('user_info');
            if (userInfo) {
                try { this.userInfo = JSON.parse(userInfo) } catch(e) { this.userInfo = {} }
            }
        },

        syncUserInfo() {
            if (!window.localStorage.getItem('user_token')) return;
            getUserInfo().then(res => {
                if (res.code === 1000 && res.data) {
                    this.userInfo = res.data;
                    window.localStorage.setItem('user_info', JSON.stringify(res.data));
                    window.dispatchEvent(new Event('userInfoUpdated'));
                }
            }).catch(() => {});
        }
    },
    
    mounted() {
        this.refreshUserInfo();
        this.syncUserInfo();

        window.addEventListener('scroll', this.handleScroll);
        window.addEventListener('userInfoUpdated', this.refreshUserInfo);
    },

    beforeDestroy() {
        window.removeEventListener('scroll', this.handleScroll);
        window.removeEventListener('userInfoUpdated', this.refreshUserInfo);
    }
}
</script>

<style scoped>
@import url("../../assets/css/index/header.css");

.ai-menu-item {
    font-weight: 500;
    color: #20B486 !important;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    white-space: nowrap;
    max-width: 112px !important;
}
.ai-menu-item:hover {
    color: #1a9d6f !important;
}
</style>
