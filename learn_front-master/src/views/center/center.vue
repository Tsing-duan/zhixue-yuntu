<template>
  <div class="centerPage">
    <headerPage></headerPage>
    <div class="centerPage-content">
      <el-card class="box-card" shadow="hover">
        <div class="content">
          <!-- 左侧表单 -->
          <div class="master">
            <el-form :model="user" :rules="rules" ref="ruleForm" label-width="110px">
              <el-form-item :label="$t('登陆账号')" prop="loginAccount">
                <el-input size="small" disabled v-model="user.loginAccount"></el-input>
              </el-form-item>
              <el-form-item :label="$t('用户名')" prop="userName" required>
                <el-input size="small" v-model="user.userName" :placeholder="$t('请输入用户名')"></el-input>
              </el-form-item>
              <el-form-item :label="$t('邮箱')" prop="email" required>
                <el-input size="small" v-model="user.email" :placeholder="$t('请输入邮箱地址')"></el-input>
              </el-form-item>
              <el-form-item :label="$t('联系电话')" prop="tel" required>
                <el-input size="small" v-model="user.tel" :placeholder="$t('请输入手机号码')"></el-input>
              </el-form-item>
              <el-form-item :label="$t('性别')" prop="sex" required>
                <el-radio-group v-model="user.sex">
                  <el-radio value="0">{{ $t("男") }}</el-radio>
                  <el-radio value="1">{{ $t("女") }}</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item :label="$t('院校')" prop="school" required>
                <el-autocomplete size="small" v-model="user.school" :placeholder="$t('请选择或输入院校')"
                  :fetch-suggestions="querySchool" clearable></el-autocomplete>
              </el-form-item>
              <el-form-item :label="$t('专业')" prop="major" required>
                <el-autocomplete size="small" v-model="user.major" :placeholder="$t('请选择或输入专业')"
                  :fetch-suggestions="queryMajor" clearable></el-autocomplete>
              </el-form-item>
              <el-form-item :label="$t('上次修改密码')" prop="pwdUpdateDate" class="pwd-time-item">
                <el-input size="small" disabled v-model="user.pwdUpdateDate"></el-input>
              </el-form-item>
            </el-form>
            <div class="submit">
              <el-button type="primary" plain size="small" @click="submit" ><template #icon><i class="el-icon-check" aria-hidden="true"></i></template>{{ $t("保存修改") }}</el-button>
            </div>
          </div>
          
          <!-- 右侧头像区域 -->
          <div class="slave">
            <div class="img">
              <el-image 
                class="avatar-img"
                :src="$resolveAvatar(user.avatar, '/img/avatars/zhixue-default.png')"
                :preview-src-list="avatar">
                <template #error><div  class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div></template>
              </el-image>
            </div>
            <div class="btns">
              <el-upload :on-error="$uploadError"
                ref="upload"
                :action="uploadAvatarUrl() + '/' + this.user.id"
                :headers="{ x_access_token: uploadToken }"
                :show-file-list="false"
                :before-upload="beforeAvatorUpload"
                :on-success="handleAvatorSuccess"
                accept="image/*"
              >
                <el-button size="small"  type="primary" plain class="uniform-btn"><template #icon><i class="el-icon-upload" aria-hidden="true"></i></template>{{ $t("更换头像") }}</el-button>
              </el-upload>
              <el-button size="small"  @click="changePassword" type="warning" plain class="uniform-btn"><template #icon><i class="el-icon-key" aria-hidden="true"></i></template>{{ $t("修改密码") }}</el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 修改密码弹窗 -->
    <el-dialog append-to-body
      :title="$t('修改密码')"
      :model-value="passwordDialogVisible"
      width="420px"
      center
      :before-close="handlePasswordClose"
    >
      <div class="password-dialog">
        <div class="dialog-item">
          <span class="dialog-label">{{ $t("旧密码") }}</span>
          <el-input show-password v-model="oldPassword" :placeholder="$t('请输入旧密码')" clearable></el-input>
        </div>
        <div class="dialog-item">
          <span class="dialog-label">{{ $t("新密码") }}</span>
          <el-input show-password v-model="newPassword" :placeholder="$t('请输入新密码')" clearable></el-input>
        </div>
        <div class="dialog-item">
          <span class="dialog-label">{{ $t("确认密码") }}</span>
          <el-input show-password v-model="confirmPassword" :placeholder="$t('请再次输入新密码')" clearable></el-input>
        </div>
      </div>
      <template #footer><span  class="dialog-footer">
        <el-button size="small" @click="handlePasswordClose">{{ $t("取 消") }}</el-button>
        <el-button size="small" type="primary" @click="passwordSubmit">{{ $t("确 定") }}</el-button>
      </span></template>
    </el-dialog>
    
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { mixin } from "../../minix";
import headerPage from "../../components/header/header.vue"
import bottomPage from "../../components/bottom/bottom.vue"
import { getUserInfo, setUserInfo, setUserAvatar, changePassword, getApeSchoolList, getApeMajorList } from '../../api/api'
import { getPasswordError } from '../../utils/accountPasswordRules'

export default {
  name: 'Center',
  mixins: [mixin],
  data() {
    var checkPhone = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('请输入联系电话'));
      } else {
        const reg = /^1[3-9]\d{9}$/
        if (reg.test(value)) {
          callback();
        } else {
          return callback(new Error('请输入正确的11位手机号码'));
        }
      }
    };
    return {
      school: [],
      major: [],
      user: {},
      avatar: [],
      confirmPassword: "",
      oldPassword: "",
      newPassword: "",
      passwordDialogVisible: false,
      rules: {
        userName: [
          { required: true, get message() { return t("请输入用户名") }, trigger: 'blur' },
          { min: 2, max: 20, get message() { return t("长度在 2 到 20 个字符") }, trigger: 'blur' }
        ],
        email: [
          { required: true, get message() { return t("请输入邮箱地址") }, trigger: 'blur' },
          { type: 'email', get message() { return t("请输入正确的邮箱地址") }, trigger: 'blur' }
        ],
        tel: [
          { required: true, validator: checkPhone, trigger: 'blur' },
        ],
        sex: [
          { required: true, get message() { return t("请选择性别") }, trigger: 'change' },
        ],
        school: [
          { required: true, get message() { return t("请选择院校") }, trigger: 'change' },
        ],
        major: [
          { required: true, get message() { return t("请选择专业") }, trigger: 'change' },
        ],
      },
    }
  },
  computed: {
    uploadToken() {
      return window.localStorage.getItem('user_token') || ''
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    getApeSchoolList() {
      getApeSchoolList().then(res => {
        if (res.code == 1000) {
          this.school = res.data
        } else {
          this.$notify.error({ get title() { return t("错误") }, message: res.message });
        }
      }).catch(() => {
        this.$notify.error({ get title() { return t("错误") }, get message() { return t("获取院校列表失败") } });
      })
    },
    getApeMajorList() {
      getApeMajorList().then(res => {
        if (res.code == 1000) {
          this.major = res.data
        } else {
          this.$notify.error({ get title() { return t("错误") }, message: res.message });
        }
      }).catch(() => {
        this.$notify.error({ get title() { return t("错误") }, get message() { return t("获取专业列表失败") } });
      })
    },
    handlePasswordClose() {
      this.passwordDialogVisible = false
      this.oldPassword = ""
      this.newPassword = ""
      this.confirmPassword = ""
    },
    passwordSubmit() {
      if (!this.oldPassword) {
        this.$message.warning(t("请输入旧密码"))
        return
      }
      if (!this.newPassword) {
        this.$message.warning(t("请输入新密码"))
        return
      }
      const passwordMessage = getPasswordError(this.newPassword)
      if (passwordMessage) {
        this.$message.warning(passwordMessage)
        return
      }
      if (this.newPassword !== this.confirmPassword) {
        this.$message.warning(t("两次输入的新密码不一致"))
        return
      }
      var param = {
        id: this.user.id,
        password: this.oldPassword,
        newPassword: this.newPassword
      }
      changePassword(param).then(res => {
        if (res.code == 1000) {
          this.$notify.success({ get title() { return t("成功") }, get message() { return t("密码修改成功，请重新登录") } });
          this.handlePasswordClose()
          setTimeout(() => {
            this.$session.logout()
            this.$router.push('/')
          }, 1500)
        } else {
          this.$notify.error({ get title() { return t("错误") }, message: res.message });
        }
      }).catch(() => {
        this.$notify.error({ get title() { return t("错误") }, message: '密码修改失败' });
      })
    },
    submit() {
      this.$refs["ruleForm"].validate((valid) => {
        if (valid) {
          setUserInfo(this.user).then(res => {
            if (res.code == 1000) {
              this.$message({ type: 'success', get message() { return t("保存成功!") } });
              this.getUserInfo()
            } else {
              this.$notify.error({ get title() { return t("错误") }, message: res.message });
            }
          }).catch(() => {
            this.$notify.error({ get title() { return t("错误") }, get message() { return t("保存失败") } });
          })
        } else {
          return false;
        }
      });
    },
    getUserInfo() {
      getUserInfo().then(res => {
        if (res.code == 1000) {
          this.user = res.data
          this.user.sex = res.data.sex + ""
          this.avatar[0] = this.$resolveAvatar(this.user.avatar, '/img/avatars/zhixue-default.png')
          // 同步更新 localStorage，确保 header 头像一致
          window.localStorage.setItem("user_info", JSON.stringify(res.data))
          // 通知 header 刷新
          window.dispatchEvent(new Event("userInfoUpdated"))
        } else {
          this.$notify.error({ get title() { return t("错误") }, message: res.message });
        }
      }).catch(() => {
        this.$notify.error({ get title() { return t("错误") }, get message() { return t("获取用户信息失败") } });
      })
    },
    querySchool(queryString, cb) {
      const list = this.school.map(s => ({ value: s.name }))
      const results = queryString
        ? list.filter(s => s.value.toLowerCase().includes(queryString.toLowerCase()))
        : list
      cb(results)
    },
    queryMajor(queryString, cb) {
      const list = this.major.map(m => ({ value: m.name }))
      const results = queryString
        ? list.filter(m => m.value.toLowerCase().includes(queryString.toLowerCase()))
        : list
      cb(results)
    },
    changePassword() {
      this.passwordDialogVisible = true
    },
    handleAvatorSuccess(res) {
      if (res.code == 1000) {
        this.$message({ type: 'success', get message() { return t("头像上传成功!") } });
        this.getUserInfo()
      } else {
        this.$notify.error({ get title() { return t("错误") }, message: res.message });
      }
    },
  },
  mounted() {
    this.getUserInfo()
    this.getApeSchoolList()
    this.getApeMajorList()
  }
}
</script>

<style scoped>
.centerPage {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.centerPage-content {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 0 24px;
}

/* 卡片样式 */
.box-card {
  margin-top: 32px;
  margin-bottom: 32px;
  width: 100%;
  max-width: 1100px;
  border-radius: 24px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  box-shadow: 0 20px 40px -12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.box-card :deep(.el-card__body) {
  padding: 32px;
}

/* 内容区域 */
.content {
  width: 100%;
  display: flex;
  flex-direction: row;
  gap: 40px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Segoe UI', 'PingFang SC', sans-serif;
}

/* 左侧表单区域 */
.master {
  flex: 1.5;
  border-right: 1px solid #eef2f8;
  padding-right: 32px;
}

.master .el-form-item {
  margin-bottom: 20px;
}

.master .el-form-item__label {
  font-weight: 500;
  color: #1e293b;
  font-size: 14px;
  white-space: normal;
  line-height: 1.4;
  padding-top: 8px;
}

/* 针对密码时间字段的特殊样式 */
.master .pwd-time-item .el-form-item__label {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 60px;
  line-height: 1.3;
}

.master .pwd-time-item .el-form-item__content {
  display: flex;
  align-items: center;
  min-height: 60px;
}

.master .el-input__inner,
.master .el-select .el-input__inner {
  border-radius: 12px;
  border: 1.5px solid #e2e8f0;
  transition: all 0.3s ease;
  height: 38px;
}

.master .el-input__inner:focus,
.master .el-select .el-input__inner:focus {
  border-color: #20B486;
  box-shadow: 0 0 0 3px rgba(32, 180, 134, 0.1);
}

.master .el-radio-group {
  display: flex;
  gap: 20px;
}

.master .el-radio {
  margin-right: 0;
}

/* 必填星号颜色 */
.master .el-form-item.is-required .el-form-item__label:before {
  color: #f56c6c;
}

/* 提交按钮 */
.submit {
  width: 100%;
  text-align: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eef2f8;
}

.submit .el-button {
  padding: 10px 32px;
  border-radius: 40px;
  font-weight: 500;
  background: linear-gradient(135deg, #20B486, #58C7A4);
  border: none;
  color: #fff;
  transition: all 0.3s ease;
}

.submit .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(32, 180, 134, 0.3);
}

/* 右侧头像区域 */
.slave {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.img {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 28px;
}

.avatar-img {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #fff;
  box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
  cursor: pointer;
}

.avatar-img:hover {
  transform: scale(1.02);
}

.image-slot {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #94a3b8;
}

/* 按钮区域 - 两个按钮大小一致 */
.btns {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 140px;
}

/* 统一按钮样式 */
.btns .uniform-btn {
  width: 140px !important;
  border-radius: 40px;
  padding: 10px 0 !important;
  font-weight: 500;
  transition: all 0.3s ease;
  margin: 0;
  box-sizing: border-box;
  text-align: center;
}

/* 上传组件容器 */
.btns .el-upload {
  display: block;
  width: 140px;
}

.btns .el-button--primary.is-plain {
  background: transparent;
  border-color: #20B486;
  color: #20B486;
}

.btns .el-button--primary.is-plain:hover {
  background: linear-gradient(135deg, #20B486, #58C7A4);
  border-color: transparent;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(32, 180, 134, 0.3);
}

.btns .el-button--warning.is-plain {
  background: transparent;
  border-color: #f59e0b;
  color: #f59e0b;
}

.btns .el-button--warning.is-plain:hover {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  border-color: transparent;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(245, 158, 11, 0.3);
}

/* 密码弹窗样式 */
.password-dialog {
  padding: 8px 0;
}

.dialog-item {
  margin-bottom: 20px;
}

.dialog-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 8px;
}

.dialog-item .el-input__inner {
  border-radius: 12px;
  border: 1.5px solid #e2e8f0;
  height: 40px;
}

.dialog-item .el-input__inner:focus {
  border-color: #20B486;
  box-shadow: 0 0 0 3px rgba(32, 180, 134, 0.1);
}

/* 弹窗 footer 样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .centerPage-content {
    padding: 0 16px;
  }
  
  .box-card :deep(.el-card__body) {
    padding: 24px;
  }
  
  .content {
    flex-direction: column;
    gap: 32px;
  }
  
  .master {
    border-right: none;
    padding-right: 0;
  }
  
  .slave {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 24px;
  }
  
  .img {
    margin-bottom: 0;
  }
  
  .btns {
    width: auto;
    flex-direction: row;
    gap: 16px;
  }
  
  .btns .uniform-btn {
    width: 130px !important;
  }
  
  .btns .el-upload {
    width: 130px;
  }
  
  .avatar-img,
  .image-slot {
    width: 120px;
    height: 120px;
  }
}

@media (max-width: 480px) {
  .slave {
    flex-direction: column;
  }
  
  .btns {
    width: 100%;
    flex-direction: column;
    align-items: center;
  }
  
  .btns .uniform-btn {
    width: 140px !important;
  }
  
  .btns .el-upload {
    width: 140px;
  }
}
</style>
