<template>
  <el-main class="el-main">
    <el-dialog
      custom-class="theme-dialog"
      title="修改密码"
      :visible.sync="passwordDialogVisible"
      width="30%"
      :before-close="handlePasswordClose"
    >
      <span>请输入{{ user.userName }}的旧密码：</span>
      <el-input
        style="margin-top: 10px"
        show-password
        v-model="oldPassword"
        size="mini"
        autocomplete="off"
      ></el-input>
      <span>请输入{{ user.userName }}的新密码：</span>
      <el-input
        style="margin-top: 10px"
        show-password
        v-model="newPassword"
        size="mini"
        autocomplete="off"
      ></el-input>
      <span>请再次输入{{ user.userName }}的新密码：</span>
      <el-input
        style="margin-top: 10px"
        show-password
        v-model="confirmPassword"
        size="mini"
        autocomplete="off"
      ></el-input>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="handlePasswordClose">取 消</el-button>
        <el-button size="mini" type="primary" @click="passwordSubmit"
          >确 定</el-button
        >
      </span>
    </el-dialog>
    <keep-alive max="10">
      <router-view :key="fullPath"></router-view>
    </keep-alive>
  </el-main>
</template>

<script>
import { changePassword } from "../../api/api";
import { getPasswordError } from "../../utils/accountPasswordRules";
import { mapState, mapGetters } from "vuex";
export default {
  name: "LayoutMain",
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
        this.$message.warning("请输入旧密码");
        return;
      }
      if (!this.newPassword || !this.newPassword.trim()) {
        this.$message.warning("请输入新密码");
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
        this.$message.warning("两次输入的新密码不一致");
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
            title: "成功",
            message: "密码修改成功",
          });
          this.$bus.$emit("password", false);
        } else {
          this.$notify.error({
            title: "错误",
            message: res.message,
          });
        }
      });
    },
    // 根据fullUrl清除keepAlive
    clearKeepAlive(fullUrl) {
      this.$children.forEach((item) => {
        item.$children.forEach((child) => {
          if (child.$vnode.data.key == fullUrl) {
            this._myDestory(child);
          }
        });
      });
    },
    // 封装清除某个组件的keepAlive状态,并销毁
    _myDestory(keepAliveComponent) {
      // 你可以根据自己的业务更改此处的判断逻辑，酌情决定是否摧毁本层缓存。
      if (
        keepAliveComponent.$vnode &&
        keepAliveComponent.$vnode.data.keepAlive
      ) {
        if (
          keepAliveComponent.$vnode.parent &&
          keepAliveComponent.$vnode.parent.componentInstance &&
          keepAliveComponent.$vnode.parent.componentInstance.cache
        ) {
          if (keepAliveComponent.$vnode.componentOptions) {
            var key =
              keepAliveComponent.$vnode.key == null
                ? keepAliveComponent.$vnode.componentOptions.Ctor.cid +
                  (keepAliveComponent.$vnode.componentOptions.tag
                    ? `::${keepAliveComponent.$vnode.componentOptions.tag}`
                    : "")
                : keepAliveComponent.$vnode.key;
            var cache =
              keepAliveComponent.$vnode.parent.componentInstance.cache;
            var keys = keepAliveComponent.$vnode.parent.componentInstance.keys;
            if (cache[key]) {
              if (keys.length) {
                var index = keys.indexOf(key);
                if (index > -1) {
                  keys.splice(index, 1);
                }
              }
              delete cache[key];
            }
          }
        }
      }
      keepAliveComponent.$destroy();
    },
  },
  computed: {
    fullPath() {
      return this.$route.fullPath;
    },
    ...mapGetters("user", ["getUser"]),
  },
  created() {},
  mounted() {
    // 注册监听全局的clearKeepAlive方法,可在其他组件中触发此方法
    this.$bus.$on("clearKeepAlive", this.clearKeepAlive);
    // 监听collapse
    this.$bus.$on("password", (res) => {
      this.passwordDialogVisible = res;
    });
    try { this.user = JSON.parse(this.getUser) } catch(e) { this.user = {} }
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

::v-deep .theme-dialog {
  border-radius: 16px;
  border: 1px solid #e8e8e5;
  overflow: hidden;
  box-shadow: 0 16px 40px rgba(30, 30, 30, 0.16);
}
::v-deep .theme-dialog .el-dialog__header {
  background: linear-gradient(120deg, #f7f7f5 0%, #f1f1ee 100%);
  padding: 14px 20px;
  border-bottom: 1px solid #e8e8e5;
}
::v-deep .theme-dialog .el-dialog__title {
  color: #2f2f2c;
  font-weight: 700;
}
::v-deep .theme-dialog .el-dialog__body {
  color: #3f3f3c;
  font-size: 14px;
  background: #fcfcfb;
}
::v-deep .theme-dialog .el-input__inner {
  border-radius: 10px;
  border: 1px solid #ddddda;
  background: #ffffff;
}

::v-deep .theme-dialog .el-input__inner:focus {
  border-color: #b7b7b2;
  box-shadow: 0 0 0 3px rgba(125, 125, 120, 0.12);
}
::v-deep .theme-dialog .el-dialog__footer {
  background: #fcfcfb;
  border-top: 1px solid #ecece8;
  padding-top: 12px;
}

::v-deep .theme-dialog .el-button--primary {
  background: #2f3437;
  border-color: #2f3437;
}

::v-deep .theme-dialog .el-button--primary:hover {
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
