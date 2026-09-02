<template>
  <div class="login-page">
    <div class="paper-noise"></div>

    <div class="login-shell">
      <div class="login-art" aria-hidden="true">
        <img
          class="login-art-image"
          src="../../assets/image/brand/zhixue-login-scene-v2.webp"
          alt=""
        />
      </div>
      <div class="login-card">
        <el-form :model="userInfo" :rules="rules" ref="ruleForm" class="login-form">
          <div class="login-title">
            <img
              class="brand-logo"
              src="../../assets/image/brand/zhixue-mark.svg"
              alt="智学云途"
            />
            <h2>智学云途</h2>
            <p class="slogan-text">让学习管理更智能，让学习过程更清晰</p>
          </div>

          <div class="login-input">
            <el-form-item prop="username">
              <div class="input-row">
                <label>学习账号</label>
                <el-input
                  prefix-icon="el-icon-user"
                  v-model="userInfo.username"
                  placeholder="请输入学习账号"
                  @keyup.enter.native="toLogin"
                ></el-input>
              </div>
            </el-form-item>
            <el-form-item prop="password">
              <div class="input-row">
                <label>密码</label>
                <el-input
                  prefix-icon="el-icon-lock"
                  placeholder="请输入密码"
                  v-model="userInfo.password"
                  show-password
                  @keyup.enter.native="toLogin"
                ></el-input>
              </div>
            </el-form-item>
          </div>

          <el-form-item>
            <div class="button-row">
              <el-button class="login-btn" type="primary" @click="toLogin()"
                :loading="loading" :disabled="loading"
                ><i v-if="!loading" class="el-icon-cloudy"></i>{{ loading ? '登录中...' : '登录' }}</el-button
              >
              <el-button class="register-btn" type="info" @click="toRegister()"
                ><i class="el-icon-user"></i>注册学生账号</el-button
              >
            </div>
          </el-form-item>
          <a class="manager-entry" :href="managerLoginUrl" target="_blank" rel="noopener">
            <img src="../../assets/image/brand/zhixue-mark.svg" alt="" />
            管理端 / 教师入口
          </a>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { login, getUser } from "../../api/api";

export default {
  data() {
    return {
      loading: false,
      userInfo: {
        username: "",
        password: "",
      },
      rules: {
        username: [{ required: true, message: "请输入用户账号", trigger: "blur" }],
        password: [{ required: true, message: "请输入用户密码", trigger: "blur" }],
      },
    };
  },
  components: {
    // bottomPage, // Removed
  },
  computed: {
    managerLoginUrl() {
      return `${window.location.protocol}//${window.location.hostname}:3000/`;
    },
  },
  methods: {
    toRegister() {
      this.$router.push("/register");
    },
    toLogin() {
      this.$refs["ruleForm"].validate((valid) => {
        if (!valid) return false;
        if (this.loading) return;
        this.loading = true;
        var params = {
          username: this.userInfo.username,
          password: this.userInfo.password,
        };
        login(params).then((res) => {
          if (res.code == 1000) {
            this.$message({
              message: "登陆成功",
              type: "success",
              duration: 1200,
              showClose: true,
            });
            var token = res.data.token;
            window.localStorage.setItem("user_token", token);
            var that = this;
            this.getUserInfo().then(() => {
              that.$router.push("/");
            }).catch(() => {
              that.$router.push("/");
            });
          } else {
            this.$message.error(res.message);
            this.loading = false;
          }
        }).catch(() => {
          this.$message.error("登录失败，请检查网络连接");
          this.loading = false;
        });
      });
    },
    getUserInfo() {
      return getUser().then((res) => {
        if (res.code == 1000) {
          window.localStorage.setItem("user_info", JSON.stringify(res.data));
        }
      });
    },
  },
  created() {},
  mounted() {},
};
</script>

<style scoped>
.login-page {
  --paper: #f7f6f3;
  --panel: #ffffff;
  --ink-1: #191919;
  --ink-2: #5f5f5f;
  --line: #e7e5df;
  --line-strong: #d9d6ce;
  --btn: #171717;
  --btn-hover: #2b2b2b;
  position: relative;
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow-x: hidden;
  overflow-y: auto;
  background: var(--paper);
  font-family: "IBM Plex Sans", "PingFang SC", "Microsoft YaHei", sans-serif;
  padding: 24px;
  box-sizing: border-box;
}
.paper-noise {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(
    rgba(0, 0, 0, 0.03) 0.7px,
    transparent 0.7px
  );
  background-size: 8px 8px;
  opacity: 0.45;
  pointer-events: none;
}
.login-shell {
  width: min(980px, 100%);
  min-height: 560px;
  margin: auto;
  display: flex; /* Changed from grid to flex to center content */
  justify-content: center; /* Center content horizontally */
  align-items: center; /* Center content vertically */
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 20px;
  box-shadow: 0 10px 24px rgba(15, 15, 15, 0.08);
  overflow: hidden;
  z-index: 1;
}
.login-card {
  padding: 56px 52px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: stretch;
  animation: fadeUp 0.65s ease-out;
}
.login-form {
  width: 100%;
  max-width: 320px;
  margin: 0 auto;
  text-align: center; /* Center elements within the form */
}
.login-title {
  width: 100%;
  margin-bottom: 24px; /* Increased margin for better spacing */
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px; /* Space between logo and title text */
}
.login-title h2 {
  margin: 0;
  color: var(--ink-1);
  font-size: 32px;
  letter-spacing: -0.02em;
}
.slogan-text {
  margin-top: 8px;
  color: var(--ink-2); /* Muted color */
  font-size: 14px; /* Smaller font size */
  font-style: italic; /* Artistic */
  letter-spacing: 0.02em;
}
.brand-logo {
  border-radius: 18px;
  width: 112px;
  height: 88px;
  object-fit: contain;
  border: 1px solid var(--line);
  background: #fff;
}
.login-input {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 6px;
}
.input-row {
  display: flex;
  align-items: center; /* Align label and input vertically */
  gap: 10px; /* Space between label and input */
}
.input-row label {
  flex-shrink: 0; /* Prevent label from shrinking */
  font-size: 14px; /* Slightly larger for better readability */
  color: var(--ink-2);
  font-weight: 500;
  width: 60px; /* Fixed width for label for alignment */
  text-align: right; /* Align label text to right */
}
.input-row .el-input {
  flex-grow: 1; /* Allow input to take remaining space */
}
.button-row {
  display: flex;
  gap: 10px; /* Space between buttons */
  margin-top: 18px; /* Maintain margin above buttons */
}
.login-btn, .register-btn {
  margin-top: 0; /* Remove individual button margin-top */
  flex-grow: 1; /* Allow buttons to grow and share space */
  border-radius: 10px;
  width: 100%;
  height: 44px;
  font-weight: 600;
  font-size: 15px;
  letter-spacing: 0.02em;
  background: var(--btn);
  border: 1px solid #000;
  transition: transform 0.2s ease, background 0.2s ease;
}
.register-btn {
  margin-top: 0; /* Remove individual button margin-top */
  background: #606266;
  border: 1px solid #606266;
}
.login-btn:hover, .register-btn:hover {
  transform: translateY(-1px);
  background: var(--btn-hover);
}
.el-form-item {
  margin-bottom: 0px; /* Remove default form item margin */
}
.el-form-item__content {
    line-height: normal; /* Fix line height issue with el-form-item */
}
.login-input .el-form-item {
  margin-bottom: 14px; /* Margin between input form items */
}
.login-input .el-form-item:last-of-type {
  margin-bottom: 0; /* No margin for the last input item */
}
.login-input ::v-deep .el-input {
  width: 100%;
}
.login-input ::v-deep .el-input__inner {
  border-radius: 10px;
  background: #fbfbfa;
  border: 1px solid var(--line-strong);
  color: #222;
  height: 42px;
  font-weight: 500;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}
.login-input ::v-deep .el-input__inner:focus {
  border-color: #8c8c8c;
  box-shadow: 0 0 0 3px rgba(23, 23, 23, 0.08);
  background: #fff;
}
.login-input ::v-deep .el-input__prefix i {
  color: #9a9a9a;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1080px) {
  .login-shell {
    min-height: auto;
  }
  .login-card {
    padding: 34px 30px;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 14px;
  }
  .login-title h2 {
    font-size: 28px;
  }
  .login-card {
    padding: 24px 18px;
  }
}
</style>
