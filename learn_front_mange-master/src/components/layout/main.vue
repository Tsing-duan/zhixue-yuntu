<template>
  <el-main class="el-main">
    <el-dialog append-to-body
      class="theme-dialog"
      :title="$t('修改密码')"
      :model-value="passwordDialogVisible"
      width="30%"
      :before-close="handlePasswordClose"
    >
      <span>{{ $t("请输入") }}{{ user.userName }}{{ $t("的旧密码：") }}</span>
      <el-input
        style="margin-top: 10px"
        show-password
        v-model="oldPassword"
        size="small"
        autocomplete="off"
      ></el-input>
      <span>{{ $t("请输入") }}{{ user.userName }}{{ $t("的新密码：") }}</span>
      <el-input
        style="margin-top: 10px"
        show-password
        v-model="newPassword"
        size="small"
        autocomplete="off"
      ></el-input>
      <span>{{ $t("请再次输入") }}{{ user.userName }}{{ $t("的新密码：") }}</span>
      <el-input
        style="margin-top: 10px"
        show-password
        v-model="confirmPassword"
        size="small"
        autocomplete="off"
      ></el-input>
      <template #footer><span  class="dialog-footer">
        <el-button size="small" @click="handlePasswordClose">{{ $t("取 消") }}</el-button>
        <el-button size="small" type="primary" @click="passwordSubmit"
          >{{ $t("确 定") }}</el-button
        >
      </span></template>
    </el-dialog>
    <RouteCache />
  </el-main>
</template>

<script>
import { t } from '@/i18n'

import { changePassword } from "../../api/api";
import { getPasswordError } from "../../utils/accountPasswordRules";
import RouteCache from "@/components/RouteCache.vue";
import { mapState } from "pinia";
import { useMenuStore, useSessionStore } from "@/store";
export default {
  name: "LayoutMain",
  components: { RouteCache },
  data() {
    return {
      user: {},
      oldPassword: "",
      newPassword: "",
      confirmPassword: "",
      passwordDialogVisible: false,
    };
  },
  methods: {
    handlePasswordClose() {
      this.oldPassword = "";
      this.newPassword = "";
      this.confirmPassword = "";
      this.$bus.$emit("password", false);
    },
    passwordSubmit() {
      if (!this.oldPassword || !this.oldPassword.trim()) {
        this.$message.warning(t("请输入旧密码"));
        return;
      }
      if (!this.newPassword || !this.newPassword.trim()) {
        this.$message.warning(t("请输入新密码"));
        return;
      }
      if (Number(this.user.userType) !== 0 && this.user.loginAccount !== "admin") {
        const passwordMessage = getPasswordError(this.newPassword);
        if (passwordMessage) {
          this.$message.warning(passwordMessage);
          return;
        }
      }
      if (this.newPassword !== this.confirmPassword) {
        this.$message.warning(t("两次输入的新密码不一致"));
        return;
      }
      var param = {
        id: this.user.id,
        password: this.oldPassword,
        newPassword: this.newPassword,
      };
      changePassword(param).then((res) => {
        if (res.code == 1000) {
          this.$notify.success({
            get title() { return t("成功") },
            get message() { return t("密码修改成功") },
          });
          this.$bus.$emit("password", false);
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
  },
  computed: {
    fullPath() {
      return this.$route.fullPath;
    },
    ...mapState(useSessionStore, ["getUser"]),
  },
  created() {},
  mounted() {
    // 注册监听全局的clearKeepAlive方法,可在其他组件中触发此方法

    // 监听collapse
    this.$bus.$on("password", (res) => {
      this.passwordDialogVisible = res;
    });
    try { this.user = this.$demo(JSON.parse(this.getUser)) } catch(e) { this.user = {} }
  },
};
</script>

<style scoped>
.el-main {
  background: #f5f7fb;
  overflow-y: scroll;
  height: calc(100vh - 100px);
  position: relative;
  padding: 18px;
  animation: mainFadeIn 0.28s ease;
}

.el-main.el-main::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.el-main.el-main::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

:deep(.theme-dialog) {
  border-radius: 16px;
  border: 1px solid #e8e8e5;
  overflow: hidden;
  box-shadow: 0 16px 40px rgba(30, 30, 30, 0.16);
}
:deep(.theme-dialog .el-dialog__header) {
  background: linear-gradient(120deg, #f7f7f5 0%, #f1f1ee 100%);
  padding: 14px 20px;
  border-bottom: 1px solid #e8e8e5;
}
:deep(.theme-dialog .el-dialog__title) {
  color: #2f2f2c;
  font-weight: 700;
}
:deep(.theme-dialog .el-dialog__body) {
  color: #3f3f3c;
  font-size: 14px;
  background: #fcfcfb;
}
:deep(.theme-dialog .el-input__inner) {
  border-radius: 10px;
  border: 1px solid #ddddda;
  background: #ffffff;
}

:deep(.theme-dialog .el-input__inner:focus) {
  border-color: #b7b7b2;
  box-shadow: 0 0 0 3px rgba(125, 125, 120, 0.12);
}
:deep(.theme-dialog .el-dialog__footer) {
  background: #fcfcfb;
  border-top: 1px solid #ecece8;
  padding-top: 12px;
}

:deep(.theme-dialog .el-button--primary) {
  background: #2f3437;
  border-color: #2f3437;
}

:deep(.theme-dialog .el-button--primary:hover) {
  background: #3c4246;
  border-color: #3c4246;
}

@keyframes mainFadeIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .el-main {
    padding: 10px;
  }
}
</style>
