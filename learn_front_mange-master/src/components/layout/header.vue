<template>
  <el-header class="notion-header">
    <div class="header-left">
      <button type="button" class="icon-div action-btn" :aria-label="$t('切换侧栏')" :aria-expanded="!collapse" @click="changeCollapse">
        <i
          class="icon"
          :class="collapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
        ></i>
      </button>
      <div class="workspace-chip">
        <img
          class="chip-logo"
          src="../../assets/image/zhixue-mark.svg"
          alt=""
        />
        <span class="chip-text">{{ $t("智学云途 · 管理端") }}</span>
      </div>
    </div>

    <div class="header-right">
      <LanguageSwitch />
      <div class="search-input">
        <el-input
          v-model="search"
          size="small"
          :placeholder="$t('搜索菜单或内容')"

        ><template #suffix><i class="el-icon-search" aria-hidden="true"></i></template></el-input>
      </div>
      <div class="icon-div action-btn" @click="full">
        <i class="icon el-icon-full-screen"></i>
      </div>
      <div class="icon-div notice-btn">
        <el-dropdown
          trigger="click"
          @command="handleMessageCommand"
          @visible-change="handleMessageVisible"
        >
          <span class="el-dropdown-link action-btn notice-action">
            <span class="notice-dot"></span>
            <i class="icon el-icon-bell"></i>
          </span>
          <template #dropdown><el-dropdown-menu

            class="profile-dropdown notion-dropdown"
          >
            <el-dropdown-item disabled class="notion-menu-title"
              >{{ $t("消息中心") }}</el-dropdown-item
            >
            <el-dropdown-item command="unread">
              <i class="el-icon-message"></i>{{ unreadLabel }}（{{
                unreadCount
              }}）
            </el-dropdown-item>
            <el-dropdown-item command="announcement">
              <i class="el-icon-notebook-2"></i>{{ $t("系统公告") }}</el-dropdown-item>
          </el-dropdown-menu></template>
        </el-dropdown>
      </div>
      <div class="icon-div avatar-wrap">
        <div>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">
              <img
                class="avatar-img"
                :src="avatarSrc"
                @error="$avatarError($event, '/img/avatars/zhixue-admin-default.png')" />
            </span>
            <template #dropdown><el-dropdown-menu

              class="profile-dropdown notion-dropdown"
            >
              <el-dropdown-item disabled class="notion-menu-title"
                >{{ $t("账号中心") }}</el-dropdown-item
              >
              <el-dropdown-item command="password"
                ><i class="el-icon-ship"></i>{{ $t("修改密码") }}</el-dropdown-item
              >
              <el-dropdown-item command="lock"
                ><i class="el-icon-lock"></i>{{ $t("锁屏") }}</el-dropdown-item
              >
              <el-dropdown-item divided class="logout-item" command="logout"
                ><i class="el-icon-umbrella"></i>{{ $t("退出") }}</el-dropdown-item
              >
            </el-dropdown-menu></template>
          </el-dropdown>
        </div>
      </div>
      <div class="user-name-wrap">
        <div class="user-name">{{ user.userName }}</div>
      </div>
    </div>
  </el-header>
</template>

<script>
import { t } from '@/i18n'

import { removeToken } from "@/utils/token";
import { mapState } from "pinia";
import { useMenuStore, useSessionStore } from "@/store";
import {
  logout,
  getUser,
  getApeMessagePage,
  getApeQuestionPage,
} from "@/api/api";
import defaultAvatar from "@/assets/image/avator.png";
export default {
  name: "LayoutHeader",
  data() {
    return {
      user: {},
      unreadCount: 3,
      search: "",
      collapse: window.innerWidth < 768,
    };
  },
  computed: {
    ...mapState(useMenuStore, {
      displayMenus: (state) => state.displayMenus,
      activeMenuArrary: (state) => state.activeMenuArrary,
    }),
    ...mapState(useSessionStore, ["getUser"]),
    isTeacherUser() {
      return Number(this.user && this.user.userType) === 1;
    },
    unreadLabel() {
      return this.isTeacherUser ? t("待回复答疑") : t("未读消息");
    },
    avatarSrc() {
      return this.resolveAvatar(this.user && this.user.avatar);
    },
  },
  watch: {
    getUser: {
      immediate: true,
      handler(value) {
        this.applyUserFromStore(value);
      },
    },
  },
  methods: {
    resolveAvatar(avatar) {
      return this.$resolveAvatar(avatar, "/img/avatars/zhixue-admin-default.png") || defaultAvatar;
    },
    applyUserFromStore(value) {
      if (!value) {
        this.user = {};
        return;
      }
      try {
        this.user = this.$demo(JSON.parse(value) || {});
      } catch (e) {
        this.user = {};
      }
    },
    refreshCurrentUser() {
      return getUser()
        .then((res) => {
          if (res.code == 1000 && res.data) {
            this.user = res.data;
            this.$session.setUser(JSON.stringify(res.data));
          }
        })
        .catch(() => {});
    },
    changeCollapse() {
      this.collapse = !this.collapse;
      this.$bus.$emit("collapse", this.collapse);
    },
    full() {
      let de = document.documentElement;
      if (de.requestFullscreen) {
        de.requestFullscreen();
      } else if (de.mozRequestFullScreen) {
        de.mozRequestFullScreen();
      } else if (de.webkitRequestFullScreen) {
        de.webkitRequestFullScreen();
      }
    },
    handleMessageCommand(command) {
      if (command == "unread") {
        if (this.isTeacherUser) {
          this.goByMenuKeywords(["答疑", "question"], "/question", "答疑管理");
          this.$message({
            message: this.unreadCount + ' ' + t('条待回复答疑'),
            type: "info",
          });
          return;
        }
        this.goByMenuKeywords(["消息", "message"], "/message", "消息管理");
        this.$message({
          message: this.unreadCount + ' ' + t('条未读消息'),
          type: "info",
        });
      } else if (command == "announcement") {
        this.goByMenuKeywords(["公告", "notice"], "/notice", "公告管理");
        this.$message({ get message() { return t("已为你打开公告列表") }, type: "info" });
      }
    },
    async loadUnreadCount() {
      if (this.isTeacherUser) {
        await this.loadUnansweredQuestionCount();
        return;
      }
      const pageSize = 200;
      let pageNumber = 1;
      let total = 0;
      let counted = 0;
      let unread = 0;
      do {
        const params = {
          userId: "",
          userName: "",
          content: "",
          answer: "",
          flag: 1,
          pageNumber,
          pageSize,
        };
        const res = await getApeMessagePage(params);
        if (!res || res.code != 1000 || !res.data) {
          break;
        }
        const records = res.data.records || [];
        total = Number(res.data.total || 0);
        unread += this.countUnreadInRecords(records);
        counted += records.length;
        pageNumber += 1;
      } while (counted < total && pageNumber <= 100);
      this.unreadCount = unread;
    },
    async loadUnansweredQuestionCount() {
      const teacherId =
        this.user && this.user.id != null ? String(this.user.id) : "";
      if (!teacherId) {
        this.unreadCount = 0;
        return;
      }
      const pageSize = 200;
      let pageNumber = 1;
      let total = 0;
      let counted = 0;
      let unanswered = 0;
      do {
        const params = {
          userName: "",
          content: "",
          answer: "",
          teacherId,
          taskName: "",
          pageNumber,
          pageSize,
        };
        const res = await getApeQuestionPage(params);
        if (!res || res.code != 1000 || !res.data) {
          break;
        }
        const records = res.data.records || [];
        total = Number(res.data.total || 0);
        unanswered += this.countUnreadInRecords(records);
        counted += records.length;
        pageNumber += 1;
      } while (counted < total && pageNumber <= 100);
      this.unreadCount = unanswered;
    },
    handleMessageVisible(visible) {
      if (visible) {
        this.loadUnreadCount().catch(() => {
          this.unreadCount = 0;
        });
      }
    },
    countUnreadInRecords(records) {
      let total = 0;
      for (let i = 0; i < records.length; i++) {
        const answer =
          records[i] && records[i].answer != null
            ? (records[i].answer + "").trim()
            : "";
        if (!answer) {
          total += 1;
        }
      }
      return total;
    },
    goByMenuKeywords(keywords, fallbackUrl, fallbackName) {
      const target = this.findMenuByKeywords(this.displayMenus, keywords);
      const url = target && target.routeUrl ? target.routeUrl : fallbackUrl;
      const name = target && target.menuName ? target.menuName : fallbackName;
      this.$router.push({
        path: url,
      });
      this.$menus.setActiveMenu(url);
      for (let i = 0; i < this.activeMenuArrary.length; i++) {
        if (this.activeMenuArrary[i].url == url) {
          return;
        }
      }
      this.$menus.addActiveMenu({
        name,
        url,
      });
    },
    findMenuByKeywords(menuList, keywords) {
      if (!menuList || !menuList.length) {
        return null;
      }
      for (let i = 0; i < menuList.length; i++) {
        const menu = menuList[i];
        const menuName = ((menu.menuName || "") + "").toLowerCase();
        const routeUrl = ((menu.routeUrl || "") + "").toLowerCase();
        for (let j = 0; j < keywords.length; j++) {
          const key = (keywords[j] + "").toLowerCase();
          if (menuName.indexOf(key) > -1 || routeUrl.indexOf(key) > -1) {
            return menu;
          }
        }
        if (menu.child && menu.child.length) {
          const childMatched = this.findMenuByKeywords(menu.child, keywords);
          if (childMatched) {
            return childMatched;
          }
        }
      }
      return null;
    },
    handleCommand(command) {
      if (command == "center") {
      } else if (command == "password") {
        //修改密码
        this.$bus.$emit("password", true);
      } else if (command == "setting") {
      } else if (command == "lock") {
        this.$bus.$emit("lock", 1);
      } else if (command == "logout") {
        this.$session.logout().then(() => {
          this.$router.replace('/login');
          this.$message.success(t("退出登陆"));
        });
      }
    },
  },
  created() {},
  beforeUnmount() { this.compactMedia?.removeEventListener('change', this.onCompactChange); },
  mounted() {
    this.compactMedia = window.matchMedia('(max-width: 767px)');
    this.onCompactChange = (event) => { this.collapse = event.matches; this.$bus.$emit('collapse', this.collapse); };
    this.compactMedia.addEventListener('change', this.onCompactChange);
    this.applyUserFromStore(this.getUser);
    this.refreshCurrentUser().finally(() => {
      this.loadUnreadCount().catch(() => {
        this.unreadCount = 0;
      });
    });
  },
};
</script>

<style scoped>
:deep(.el-header) {
  padding: 0;
}

.notion-header {
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 1px solid #e6eaf0;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.02);
  width: 100%;
  height: 58px !important;
  display: flex;
  align-items: center;
  padding: 0 16px 0 10px;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-div {
  width: 40px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.action-btn {
  position: relative;
  border-radius: 9px;
  color: #596579;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: #f1f5f9;
  color: #1d4ed8;
}

.icon {
  font-size: 18px;
}

.workspace-chip {
  height: 34px;
  border: 1px solid #dbe3ef;
  border-radius: 999px;
  background: #f8fafc;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-sizing: border-box;
}

.chip-logo {
  width: 22px;
  height: 22px;
  display: block;
  object-fit: contain;
}

.chip-text {
  font-size: 13px;
  color: #243145;
  font-weight: 600;
}

.header-right {
  width: auto;
  min-width: 420px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
}

.search-input {
  width: 320px;
  min-width: 240px;
}

.avatar-wrap {
  width: 52px;
}

.avatar-img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #edf2f7;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
  transition: all 0.2s ease;
}

.avatar-img:hover {
  transform: scale(1.06);
}

.user-name-wrap {
  width: 112px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.user-name {
  max-width: 102px;
  color: #243145;
  font-size: 13px;
  font-weight: 700;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.notice-dot {
  position: absolute;
  right: 5px;
  top: 5px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #e85656;
  box-shadow: 0 0 0 2px #ffffff;
}

.notice-action {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

@keyframes headerFade {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.notion-header,
.workspace-chip,
.action-btn,
.search-input {
  animation: headerFade 0.3s ease both;
}

:deep(.el-input__inner) {
  border-radius: 999px;
  border: 1px solid #dbe3ef;
  background: #f8fafc;
  color: #243145;
  height: 34px;
  line-height: 34px;
  transition: all 0.2s ease;
}

:deep(.el-input__inner:focus) {
  border-color: #93b4ec;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

:deep(.el-input__icon) {
  color: #8a95a3;
}

:deep(.profile-dropdown) {
  border-radius: 10px;
  border: 1px solid #e6eaf0;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.14);
  font-weight: 600;
  color: #243145;
}

:deep(.notion-dropdown.el-dropdown-menu) {
  background: #ffffff;
  border-radius: 12px;
  padding: 6px;
}

:deep(.notion-menu-title) {
  font-size: 12px;
  color: #9a9a96 !important;
  cursor: default !important;
  line-height: 28px !important;
}

:deep(.notion-menu-title:hover),
:deep(.notion-menu-title:focus) {
  background: transparent !important;
}

:deep(.notion-dropdown .el-dropdown-menu__item) {
  min-width: 170px;
  color: #344054;
  line-height: 36px;
  transition: all 0.2s ease;
  border-radius: 8px;
}

:deep(.notion-dropdown .el-dropdown-menu__item:hover) {
  background: #f1f5f9;
  color: #1d4ed8;
}

:deep(.notion-dropdown .el-dropdown-menu__item i) {
  color: #8a95a3;
  margin-right: 6px;
}

:deep(.logout-item) {
  color: #e44848;
}

@media (max-width: 1200px) {
  .header-right {
    min-width: 360px;
  }
  .search-input {
    width: 260px;
  }
}

@media (max-width: 768px) {
  .notion-header {
    height: 54px !important;
    padding-right: 8px;
  }
  .workspace-chip {
    display: none;
  }
  .search-input {
    display: none;
  }
  .header-right {
    width: auto;
    min-width: 0;
  }
  .user-name-wrap {
    display: none;
  }
  .icon-div {
    width: 36px;
    height: 36px;
  }
}
</style>

<style scoped>button.action-btn { border: 0; background: transparent; padding: 0; }</style>
