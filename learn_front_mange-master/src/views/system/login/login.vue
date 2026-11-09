<template>
  <div class="login-page">
    <div class="login-language"><LanguageSwitch /></div>
    <div class="paper-noise"></div>

    <div class="login-shell">
      <div class="content">
        <img
          class="brand-logo"
          src="../../../assets/image/zhixue-mark.svg"
          :alt="$t('智学云途')"
        />
        <div class="brand-text">
          <h1>{{ $t("智学云途 · 管理端") }}</h1>
          <p>{{ $t("一站式在线教育管理平台") }}</p>
        </div>
      </div>

      <div class="login-card">
        <div class="login-form">
          <div class="login-title">
            <h2>{{ $t("登录") }}</h2>
            <span></span>
          </div>

          <div class="login-input">
            <div class="input-item">
              <label>{{ $t("用户名") }}</label>
              <el-input

                v-model="username"
                :placeholder="$t('请输入用户名')"
                @keyup.enter="login"
              ><template #prefix><i class="el-icon-user" aria-hidden="true"></i></template></el-input>
            </div>
            <div class="input-item">
              <label>{{ $t("密码") }}</label>
              <el-input

                :placeholder="$t('请输入密码')"
                v-model="password"
                show-password
                @keyup.enter="login"
              ><template #prefix><i class="el-icon-lock" aria-hidden="true"></i></template></el-input>
            </div>
          </div>

          <el-button class="login-btn" type="primary" @click="login()"
            :loading="loading" :disabled="loading"
            >{{ loading ? $t("登录中...") : $t("登录") }}</el-button
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { login, getUser } from "../../../api/api";
import { setLock } from "@/utils/lock";
export default {
  data() {
    return {
      loading: false,
      username: "",
      password: "",
    };
  },
  methods: {
    login() {
      if (!this.username) {
        this.$message({ get message() { return t("请输入用户名") }, type: "warning" });
        return;
      }
      if (!this.password) {
        this.$message({ get message() { return t("请输入密码") }, type: "warning" });
        return;
      }
      if (this.loading) return;
      this.loading = true;
      var params = {
        username: this.username,
        password: this.password,
      };
      login(params).then((res) => {
        if (res.code == 1000) {
          this.$message({
            get message() { return t("登陆成功") },
            type: "success",
            duration: 1200,
            showClose: true,
          });
          var token = res.data.token;
          this.$session.setToken(token);
          setLock(false);
          this.getUserInfo()
            .then(() => {
              this.$router.push("/index");
            })
            .catch(() => {
              this.$router.push("/index");
            });
        } else {
          this.$message.error(res.message);
          this.loading = false;
        }
      }).catch(() => {
        this.$message.error(t("登录失败，请检查网络连接"));
        this.loading = false;
      });
    },
    getUserInfo() {
      return getUser().then((res) => {
        if (res.code == 1000) {
          this.$session.setUser(JSON.stringify(res.data));
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
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
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 20px;
  box-shadow: 0 10px 24px rgba(15, 15, 15, 0.08);
  overflow: hidden;
  z-index: 1;
}
.content {
  padding: 56px 52px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 22px;
  border-right: 1px solid var(--line);
  animation: fadeUp 0.5s ease-out;
}
.brand-logo {
  width: 88px;
  height: 88px;
  object-fit: contain;
}
.brand-kicker {
  margin: 20px 0 8px;
  color: #8a8a8a;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.brand-text h1 {
  margin: 0;
  font-size: 40px;
  letter-spacing: -0.02em;
  font-weight: 700;
  color: var(--ink-1);
  line-height: 1.15;
}
.brand-text p {
  margin: 0;
  color: var(--ink-2);
  font-size: 15px;
  line-height: 1.7;
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
}
.login-title {
  width: 100%;
  margin-bottom: 16px;
}
.login-title h2 {
  margin: 0;
  color: var(--ink-1);
  font-size: 32px;
  letter-spacing: -0.02em;
}
.login-title span {
  margin-top: 6px;
  color: #777;
  display: inline-block;
  font-size: 14px;
}
.login-input {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 6px;
}
.login-btn {
  margin-top: 18px;
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
.login-btn:hover {
  transform: translateY(-1px);
  background: var(--btn-hover);
}
.input-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: stretch;
}
.input-item label {
  width: 100%;
  font-size: 13px;
  color: #4f4f4f;
  font-weight: 600;
}
.input-item :deep(.el-input) {
  width: 100%;
}
.input-item :deep(.el-input__inner) {
  border-radius: 10px;
  background: #fbfbfa;
  border: 1px solid var(--line-strong);
  color: #222;
  height: 42px;
  font-weight: 500;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}
.input-item :deep(.el-input__inner:focus) {
  border-color: #8c8c8c;
  box-shadow: 0 0 0 3px rgba(23, 23, 23, 0.08);
  background: #fff;
}
.input-item :deep(.el-input__prefix i) {
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
    grid-template-columns: 1fr;
    min-height: auto;
  }
  .content {
    border-right: none;
    border-bottom: 1px solid var(--line);
    padding: 34px 30px 30px;
  }
  .login-card {
    padding: 34px 30px;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 14px;
  }
  .brand-logo {
    width: 64px;
    height: 64px;
  }
  .brand-text h1 {
    font-size: 28px;
  }
  .brand-text p {
    font-size: 14px;
  }
  .login-title h2 {
    font-size: 28px;
  }
  .content,
  .login-card {
    padding: 24px 18px;
  }
}
</style>

<style scoped>.login-language{position:absolute;right:20px;top:16px;z-index:10}</style>
