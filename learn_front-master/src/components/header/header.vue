<template>
    <header class="header" :class="{ scrolled: isScrolled, 'navigation-open': navigationOpen, 'locale-en': $locale === 'en' }" @keydown.esc.stop.prevent="closeNavigation(true)" @focusout="handleFocusOut">
        <router-link class="system-title" to="/" :aria-label="$t('智学云途主页')">
            <img class="system-mark" src="../../assets/image/brand/zhixue-mark.svg" alt="">
            <span class="full-brand">{{ $t("智学云途") }}</span><span class="short-brand" aria-hidden="true">Zhixue</span>
        </router-link>
        <button ref="navigationToggle" class="navigation-toggle" type="button" aria-controls="student-primary-navigation" :aria-expanded="navigationOpen" :aria-label="navigationOpen ? $t('关闭导航菜单') : $t('打开导航菜单')" @click="toggleNavigation">
            <i class="el-icon-menu" aria-hidden="true"></i><span>{{ $t("导航") }}</span>
        </button>
        <nav id="student-primary-navigation" class="menu-list" :aria-label="$t('主导航')" @click="closeNavigation()">
            <router-link
                v-for="item in menuItems"
                :key="item.path"
                :to="item.path"
                :class="{ 'menu_item': $route.path === item.path }"
            >
                <i class="menu-icon" :class="item.icon" aria-hidden="true"></i>
                {{ $t(item.title) }}
            </router-link>
            <router-link
                class="ai-menu-item"
                to="/ai"
                :class="{ 'menu_item': $route.path === '/ai' }"
            >
                <i class="el-icon-cpu" aria-hidden="true"></i> {{ $t(aiMenuTitle) }}
            </router-link>
        </nav>
        <LanguageSwitch />
        <div class="center">
            <el-dropdown ref="accountMenu" @command="handleCommand" placement="bottom-end" trigger="click">
                <button type="button" class="el-dropdown-link" :aria-label="$t('账号菜单：') + (userInfo.userName || $t('个人中心'))" @click="closeNavigation()" @keydown.esc.stop.prevent="$refs.accountMenu.handleClose()">
                    <img
                        :src="$resolveAvatar(userInfo.avatar, '/img/avatars/zhixue-default.png')"
                        :alt="userInfo.userName"
                        @error="$avatarError($event, '/img/avatars/zhixue-default.png')">
                    <span class="username" :title="userInfo.userName">{{ userInfo.userName }}</span>
                    <i class="el-icon-arrow-down" aria-hidden="true"></i>
                </button>
                <template #dropdown><el-dropdown-menu >
                    <el-dropdown-item command="a">
                        <div class="drop-item"><i class="el-icon-user"></i>{{ $t("个人中心") }}</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="b">
                        <div class="drop-item"><i class="el-icon-s-management"></i>{{ $t("我的课程") }}</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="i">
                        <div class="drop-item"><i class="el-icon-star-off"></i>{{ $t("收藏课程") }}</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="c">
                        <div class="drop-item"><i class="el-icon-edit"></i>{{ $t("记笔记") }}</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="d">
                        <div class="drop-item"><i class="el-icon-reading"></i>{{ $t("我的作业") }}</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="e">
                        <div class="drop-item"><i class="el-icon-notebook-1"></i>{{ $t("我的笔记") }}</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="f">
                        <div class="drop-item"><i class="el-icon-star-off"></i>{{ $t("收藏笔记") }}</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="g">
                        <div class="drop-item"><i class="el-icon-circle-close"></i>{{ $t("错题集") }}</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="j" divided>
                        <div class="drop-item"><i class="el-icon-cpu"></i>{{ $t(aiMenuTitle) }}</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="h">
                        <div class="drop-item"><i class="el-icon-back"></i>{{ $t("退出登陆") }}</div>
                    </el-dropdown-item>
                </el-dropdown-menu></template>
            </el-dropdown>
        </div>
    </header>
</template>

<script>
import { t, locale } from '@/i18n'

import { getUserInfo } from '@/api/api'

export default {
    name: 'Header',
    data() {
        return {
            userInfo: {},
            isScrolled: false,
            navigationOpen: false,
            menuItems: [
                { name: 'index', path: '/', get title() { return t("主页") }, icon: 'el-icon-house' },
                { name: 'task', path: '/task', get title() { return t("课程") }, icon: 'el-icon-reading' },
                { name: 'teacher', path: '/teacher', get title() { return t("教师") }, icon: 'el-icon-user' },
                { name: 'test', path: '/test', get title() { return t("考试") }, icon: 'el-icon-edit-outline' },
                { name: 'article', path: '/article', get title() { return t("笔记") }, icon: 'el-icon-notebook-2' },
                { name: 'forum', path: '/forum', get title() { return t("讨论") }, icon: 'el-icon-chat-dot-round' },
                { name: 'notice', path: '/notice', get title() { return t("公告") }, icon: 'el-icon-bell' },
                { name: 'message', path: '/message', get title() { return t("留言") }, icon: 'el-icon-message' },
                { name: 'about', path: '/about', get title() { return t("关于") }, icon: 'el-icon-info' }
            ]
        }
    },
    computed: {
        interfaceLocale() { return locale.value },
        aiMenuTitle() {
            const info = window.localStorage.getItem('user_info')
            if (info) {
                try {
                    const user = JSON.parse(info)
                    return user.userType === 1 ? t("AI教学助手") : t("AI学习助手")
                } catch (e) {}
            }
            return t("AI学习助手")
        }
    },
    watch: {
        '$route.fullPath'() { this.closeNavigation() },
        interfaceLocale() { this.updateNavigationMedia() }
    },
    methods: {
        toggleNavigation() {
            this.$refs.accountMenu.handleClose()
            this.navigationOpen = !this.navigationOpen
        },
        closeNavigation(restoreFocus = false) {
            const wasOpen = this.navigationOpen
            this.navigationOpen = false
            if (restoreFocus && wasOpen) this.$refs.navigationToggle?.focus()
        },
        handleOutsidePointer(event) {
            if (!this.$el.contains(event.target)) this.closeNavigation()
        },
        handleFocusOut(event) {
            if (event.relatedTarget && !this.$el.contains(event.relatedTarget)) this.closeNavigation()
        },
        handleNavigationResize() { this.closeNavigation() },
        updateNavigationMedia() {
            this.navigationMedia?.removeEventListener('change', this.handleNavigationResize)
            this.navigationMedia = window.matchMedia(this.interfaceLocale === 'en' ? '(max-width: 1599px)' : '(max-width: 1199px)')
            this.navigationMedia.addEventListener('change', this.handleNavigationResize)
            this.closeNavigation()
        },
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
            this.$session.logout().then(() => {
                window.localStorage.removeItem('user_info');
                window.localStorage.removeItem('user_token');
                this.$message({
                    get message() { return t("退出成功") },
                    type: 'success',
                    duration: 1500,
                    customClass: 'custom-message'
                });
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            });
        },
        
        handleScroll() {
            this.isScrolled = window.scrollY > 10;
        },

        refreshUserInfo() {
            const userInfo = window.localStorage.getItem('user_info');
            if (userInfo) {
                try { this.userInfo = this.$demo(JSON.parse(userInfo)) } catch(e) { this.userInfo = {} }
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
        document.addEventListener('pointerdown', this.handleOutsidePointer);
        this.updateNavigationMedia();
    },

    beforeUnmount() {
        window.removeEventListener('scroll', this.handleScroll);
        window.removeEventListener('userInfoUpdated', this.refreshUserInfo);
        document.removeEventListener('pointerdown', this.handleOutsidePointer);
        this.navigationMedia?.removeEventListener('change', this.handleNavigationResize);
    }
}
</script>

<style scoped>
@import url("../../assets/css/index/header.css");
</style>
