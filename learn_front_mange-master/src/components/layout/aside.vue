<template>
  <el-aside class="notion-aside" :width="isCollapse ? '72px' : '248px'">
    <div class="brand-wrap" :class="{ collapsed: isCollapse }">
      <div class="brand-logo-box">
        <img
          class="brand-logo"
          src="../../assets/image/operation.png"
          :alt="$t('管理后台')"
        />
      </div>
      <div class="brand-copy" v-show="!isCollapse">
        <div class="brand-title">{{ $t("智学云途") }}</div>
        <div class="brand-subtitle">{{ $t("管理后台") }}</div>
      </div>
    </div>
    <el-menu
      @select="handleSelect"
      :collapse="isCollapse"
      :collapse-transition="false"
      class="el-menu-vertical notion-menu"
      :unique-opened="true"
      background-color="#f7f7f5"
      :text-color="textColor"
      :default-active="this.$route.path + '@' + (this.$route.meta.title || this.$route.name)"
      active-text-color="#1f1f1f"
    >
      <span v-for="(item, index) in displayMenus" :key="index">
        <el-menu-item
          v-if="item.menuType == 1"
          :index="item.routeUrl + '@' + item.menuName"
        >
          <span class="menu-dot"></span>
          <i :class="item.icon"></i>
          <template #title><span >{{ $t(item.menuName) }}</span></template>
        </el-menu-item>
        <el-sub-menu v-if="item.menuType == 0" :index="index + ''">
          <template #title>
            <span class="menu-dot"></span>
            <i :class="item.icon"></i>
            <span v-if="!isCollapse">{{ $t(item.menuName) }}</span>
          </template>
          <span v-for="(element, subscript) in item.child" :key="subscript">
            <el-sub-menu
              v-if="element.menuType == 0"
              :index="index + '-' + subscript"
            >
              <template #title>
                <i :class="element.icon"></i>
                <span>{{ $t(element.menuName) }}</span>
              </template>
              <template v-for="(one, sub) in element.child"><el-menu-item

                v-if="one.menuType == 1"
                :index="one.routeUrl + '@' + one.menuName"
              >
                <span class="menu-dot"></span>
                <i :class="one.icon"></i>
                <template #title><span >{{ $t(one.menuName) }}</span></template>
              </el-menu-item></template>
            </el-sub-menu>
            <el-menu-item
              v-if="element.menuType == 1"
              :index="element.routeUrl + '@' + element.menuName"
            >
              <span class="menu-dot"></span>
              <i :class="element.icon"></i>
              <template #title><span >{{ $t(element.menuName) }}</span></template>
            </el-menu-item>
          </span>
        </el-sub-menu>
      </span>
    </el-menu>
    <!-- AI 数据整理入口 -->
    <div class="ai-entry" @click="goAiData">
      <i class="el-icon-cpu"></i>
      <span v-show="!isCollapse">{{ $t("AI 数据整理") }}</span>
    </div>
  </el-aside>
</template>

<script>
import { mapState } from "pinia";
import { useMenuStore, useSessionStore } from "@/store";
export default {
  name: "asside",
  data() {
    return {
      isCollapse: window.innerWidth < 768,
      textColor: "#595959",
    };
  },
  computed: {
    ...mapState(useMenuStore, {
      activeMenuArrary: (state) => state.activeMenuArrary,
      displayMenus: (state) => state.displayMenus,
    }),
  },
  methods: {
    goAiData() {
      this.$router.push('/aiData')
      this.$menus.setActiveMenu('/aiData')
      const found = this.activeMenuArrary.some(m => m.url === '/aiData')
      if (!found) {
        this.$menus.addActiveMenu({ name: 'AI数据整理', url: '/aiData' })
      }
    },
    handleSelect(key) {
      var menu = key.split("@");
      this.$router.push({
        path: menu[0],
      });
      this.$menus.setActiveMenu(menu[0]);
      for (let i = 0; i < this.activeMenuArrary.length; i++) {
        if (this.activeMenuArrary[i].url == menu[0]) {
          return;
        }
      }
      var param = {
        name: menu[1],
        url: menu[0],
      };
      this.$menus.addActiveMenu(param);
    },
  },
  created() {},
  mounted() {
    // 监听collapse
    this.$bus.$on("collapse", (res) => {
      this.isCollapse = res;
    });
    this.$menus.setActiveMenu(this.$route.fullPath);
    for (let i = 0; i < this.activeMenuArrary.length; i++) {
      if (this.activeMenuArrary[i].url == this.$route.fullPath) {
        return;
      }
    }
    var param = {
      name: (this.$route.meta.title || this.$route.name),
      url: this.$route.fullPath,
    };
    this.$menus.addActiveMenu(param);
  },
};
</script>

<style scoped lang="scss">
.notion-aside {
  position: relative;
  background: #ffffff;
  border-right: 1px solid #e6eaf0;
  box-shadow: 1px 0 0 rgba(15, 23, 42, 0.02);
  transition: width 0.28s ease;
}

.brand-wrap {
  display: flex;
  height: 72px;
  margin: 12px 12px 8px;
  padding: 10px 12px;
  background: #f8fafc;
  border: 1px solid #e6eaf0;
  border-radius: 12px;
  align-items: center;
  justify-content: center;
  gap: 10px;
  overflow: hidden;
  box-sizing: border-box;
  transition: all 0.25s ease;
}

.brand-wrap.collapsed {
  justify-content: center;
  padding: 10px;
}

.brand-logo-box {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid #edf1f7;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.brand-logo {
  width: 44px;
  height: 44px;
  padding: 2px;
  box-sizing: border-box;
  object-fit: contain;
  display: block;
}

.brand-copy {
  min-width: 0;
  flex: 1;
}

.brand-title {
  color: #172033;
  font-size: 15px;
  font-weight: 700;
  line-height: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.brand-subtitle {
  color: #667085;
  font-size: 12px;
  line-height: 16px;
}

.notion-menu {
  height: calc(100vh - 92px);
  overflow-x: hidden;
  overflow-y: auto;
  background: transparent !important;
  border-right: none !important;
  padding: 6px 10px 14px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Microsoft YaHei", sans-serif;
  font-weight: 500;
}

.notion-menu::-webkit-scrollbar,
.notion-aside::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.notion-menu::-webkit-scrollbar-thumb {
  background: #d9d9d5;
  border-radius: 999px;
}

.menu-dot {
  display: none;
}

.notion-menu:deep(.el-menu-item),
.notion-menu:deep(.el-sub-menu__title) {
  height: 40px !important;
  line-height: 40px !important;
  border-radius: 9px;
  margin: 2px 0;
  padding-left: 12px !important;
  color: #4b5563 !important;
  transition: all 0.2s ease;
}

.notion-menu:deep(.el-menu-item i),
.notion-menu:deep(.el-sub-menu__title i) {
  color: #8a95a3;
  margin-right: 10px;
  transition: all 0.2s ease;
}

.notion-menu:deep(.el-menu-item:hover),
.notion-menu:deep(.el-sub-menu__title:hover) {
  background-color: #f1f5f9 !important;
  color: #1f2937 !important;
}

.notion-menu:deep(.el-menu-item:hover i),
.notion-menu:deep(.el-sub-menu__title:hover i) {
  color: #2563eb;
}

.notion-menu:deep(.el-menu-item.is-active) {
  background: #eff6ff !important;
  color: #1d4ed8 !important;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #2563eb;
}

.notion-menu:deep(.el-menu-item.is-active i) {
  color: #2563eb;
}

.notion-menu:deep(.el-sub-menu .el-menu-item) {
  background: transparent !important;
  padding-left: 38px !important;
}

.notion-menu:deep(.el-sub-menu .el-sub-menu__title) {
  background: transparent !important;
}

.notion-menu:deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

.notion-menu:deep(.el-sub-menu .el-sub-menu__icon-arrow) {
  color: #9aa4b2;
  margin-top: -6px;
}

.notion-menu:deep(.el-menu--collapse .el-menu-item),
.notion-menu:deep(.el-menu--collapse .el-sub-menu__title) {
  padding: 0 !important;
  justify-content: center;
}

.notion-menu:deep(.el-menu--collapse .menu-dot) {
  margin-right: 0;
}

@keyframes menuFadeSlide {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.notion-menu:deep(.el-menu-item),
.notion-menu:deep(.el-sub-menu) {
  animation: menuFadeSlide 0.32s ease both;
}

.notion-menu:deep(.el-sub-menu .el-menu-item) {
  animation-duration: 0.26s;
}

/* AI 数据整理入口 */
.ai-entry {
  position: absolute;
  bottom: 16px;
  left: 12px;
  right: 12px;
  height: 42px;
  border-radius: 10px;
  background: linear-gradient(135deg, #eef3fb, #e8f7f2);
  border: 1px solid #b9cff4;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.22s ease;
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
}
.ai-entry i {
  font-size: 18px;
  color: #3b5bdb;
}
.ai-entry:hover {
  background: linear-gradient(135deg, #dce7fa, #d3f1e4);
  border-color: #8db8e8;
  color: #1d4ed8;
  box-shadow: 0 2px 10px rgba(37, 99, 235, 0.12);
}
</style>
