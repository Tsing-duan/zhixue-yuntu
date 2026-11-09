<template>
  <div class="common-layout">
    <el-container v-show="lock == 0">
      <el-asside></el-asside>
      <el-container direction="vertical" class="workspace-body">
        <el-header></el-header>
        <div class="main-menu">
          <div class="scroll-left" @click="leftScroll()">
            <i class="el-icon-arrow-left"></i>
          </div>
          <div class="menu" id="menu">
            <div
              class="menu-item"
              :class="item.url == activeMenu ? 'active-item' : ''"
              v-for="(item, index) in activeMenuArrary"
              :key="index"
              @click="openMenu(item.url)"
            >
              <span
                :class="item.url == activeMenu ? 'active' : ''"
                class="menu-name"
                >{{ $t(item.name) }}</span
              >
              <i
                v-if="item.url != '/index'"
                class="menu-icon el-icon-close"
                @click.stop="closeMenu(item.url)"
              ></i>
            </div>
          </div>
          <div class="scroll-right" @click="rightScroll()">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
        <el-main></el-main>
      </el-container>
    </el-container>
    <el-container v-show="lock == 1">
      <div class="lock">
        <img class="lock-back" src="../../assets/image/lock-back.jpg" />
        <div class="form">
          <div class="content">
            <el-input

              type="password"
              v-model="pass"
              size="small"
              :placeholder="$t('请输入密码')"
            ><template #prefix><i class="el-icon-star-off" aria-hidden="true"></i></template></el-input>
            <div @click="unlock"><i class="el-icon-d-arrow-right"></i></div>
          </div>
        </div>
      </div>
    </el-container>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { mapState } from "pinia";
import { useMenuStore, useSessionStore } from "@/store";
import { getLock, setLock } from "@/utils/lock";
import { verPassword } from "@/api/api";
import elAsside from "../../components/layout/aside.vue";
import elMain from "../../components/layout/main.vue";
import elHeader from "../../components/layout/header.vue";

export default {
  name: "index",
  data() {
    return {
      lock: 0,
      pass: "",
    };
  },
  components: {
    elAsside,
    elMain,
    elHeader,
  },
  computed: {
    ...mapState(useMenuStore, {
      activeMenuArrary: (state) => state.activeMenuArrary,
      activeMenu: (state) => state.activeMenu,
    }),
  },
  methods: {
    leftScroll() {
      var container = document.getElementById("menu");
      // 向左滚动100像素
      container.scrollLeft -= 100;
    },
    rightScroll() {
      var container = document.getElementById("menu");
      // 向右滚动100像素
      container.scrollLeft += 100;
    },
    openMenu(url) {
      if (this.activeMenu != url) {
        this.$router.push({
          path: url,
        });
        this.$menus.setActiveMenu(url);
      } else {
        this.scrollCurrentViewToTop();
      }
    },
    scrollCurrentViewToTop() {
      this.$nextTick(() => {
        const mainContainer = document.querySelector(".el-main");
        if (mainContainer && typeof mainContainer.scrollTo === "function") {
          mainContainer.scrollTo({
            top: 0,
            behavior: "smooth",
          });
        } else if (mainContainer) {
          mainContainer.scrollTop = 0;
        }
        if (document.documentElement) {
          document.documentElement.scrollTop = 0;
        }
        if (document.body) {
          document.body.scrollTop = 0;
        }
      });
    },
    closeMenu(url) {
      const index = this.activeMenuArrary.findIndex(item => item.url === url);
      if (index < 0 || this.activeMenuArrary.length <= 1) return;
      if (this.activeMenu === url) {
        const next = this.activeMenuArrary[index - 1] || this.activeMenuArrary[index + 1];
        this.$menus.setActiveMenu(next.url);
        this.$router.push(next.url);
      }
      this.$menus.reduceActiveMenu(index);
    },
    unlock() {
      if (!this.pass) {
        this.$message({
          get message() { return t("请输入密码验证") },
          type: "warning",
        });
        return;
      }
      verPassword({ password: this.pass }).then((res) => {
        if (res.code == 1000) {
          this.$message({
            get message() { return t("验证通过") },
            type: "success",
          });
          this.pass = "";
          this.$bus.$emit("lock", 0);
        } else {
          this.$message({
            message: res.message,
            type: "error",
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
  },
  created() {},
  mounted() {
    // console.clear()
    this.$nextTick(() => {
      this.lock = getLock();
      this.$bus.$on("lock", (res) => {
        setLock(res);
        this.lock = res;
      });
    });
  },
};
</script>

<style scoped>
.common-layout :deep(.el-menu-item) {
  min-width: 0;
}
.main-menu {
  width: 100%;
  height: 42px;
  border-bottom: 1px solid #e6eaf0;
  background: #ffffff;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6px;
  padding: 0 8px;
  box-sizing: border-box;
}
.menu {
  display: flex;
  flex-direction: row;
  align-items: center;
  height: 100%;
  width: calc(100% - 60px);
  overflow: auto;
  flex-grow: 0;
  flex-basis: auto;
  gap: 6px;
}
.scroll-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  cursor: pointer;
  border: 1px solid #dbe3ef;
  border-radius: 9px;
  color: #667085;
  background: #f8fafc;
  transition: all 0.2s ease;
}
.menu::-webkit-scrollbar {
  width: 0px;
  height: 0px;
}
.scroll-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  cursor: pointer;
  border: 1px solid #dbe3ef;
  border-radius: 9px;
  color: #667085;
  background: #f8fafc;
  transition: all 0.2s ease;
}
.scroll-left:hover,
.scroll-right:hover {
  color: #1d4ed8;
  background: #eff6ff;
}
.menu-item {
  min-width: 110px;
  max-width: 160px;
  height: 30px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #dbe3ef;
  border-radius: 9px;
  cursor: pointer;
  background-color: #f8fafc;
  flex-shrink: 0;
  padding: 0 10px;
  box-sizing: border-box;
  transition: all 0.2s ease;
}
.menu-item:hover {
  background: #f1f5f9;
  border-color: #cbd6e2;
}
.menu-name {
  margin-left: 0;
  font-size: 12px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Microsoft YaHei", sans-serif;
  color: #596579;
  line-height: 1;
}
.active {
  color: #1d4ed8;
  font-weight: 700;
}
.active-item {
  background: #eff6ff;
  border-color: #b9cff4;
}
.menu-icon {
  cursor: pointer;
  font-size: 12px;
  color: #98a2b3;
  margin-left: 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
}
.menu-icon:hover {
  color: #1d4ed8;
  background: rgba(37, 99, 235, 0.08);
}
.lock {
  width: 100%;
  height: 100vh;
}
.lock-back {
  width: 100%;
  height: 100%;
  z-index: -1;
  position: fixed;
  object-fit: cover;
}
.form {
  z-index: 999;
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}
.content {
  display: flex;
  align-items: center;
  color: black;
  font-size: 20px;
  cursor: pointer;
}
</style>

<style scoped>
.workspace-body { min-width: 0; }
</style>
