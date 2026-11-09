<template>
  <div class="center">
    <el-card class="box-card">
      <div class="content">
        <div class="master">
          <el-form
            style="margin-right: 20px"
            :model="user"
            :rules="rules"
            ref="ruleForm"
            label-width="140px"
          >
            <el-form-item :label="$t('登陆账号')" prop="loginAccount">
              <el-input
                size="small"
                disabled
                v-model="user.loginAccount"
              ></el-input>
            </el-form-item>
            <el-form-item :label="$t('用户名')" prop="userName">
              <el-input size="small" v-model="user.userName"></el-input>
            </el-form-item>
            <el-form-item :label="$t('邮箱')" prop="email">
              <el-input size="small" v-model="user.email"></el-input>
            </el-form-item>
            <el-form-item :label="$t('联系电话')" prop="tel">
              <el-input size="small" v-model="user.tel"></el-input>
            </el-form-item>
            <el-form-item :label="$t('性别')" prop="tel">
              <el-radio-group v-model="user.sex">
                <el-radio value="0">{{ $t("男") }}</el-radio>
                <el-radio value="1">{{ $t("女") }}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('登陆IP')" prop="loginIp">
              <el-input size="small" disabled v-model="user.loginIp"></el-input>
            </el-form-item>
            <el-form-item :label="$t('登陆时间')" prop="loginDate">
              <el-input
                size="small"
                disabled
                v-model="user.loginDate"
              ></el-input>
            </el-form-item>
            <el-form-item :label="$t('上次修改密码时间')" prop="pwdUpdateDate">
              <el-input
                size="small"
                disabled
                v-model="user.pwdUpdateDate"
              ></el-input>
            </el-form-item>
          </el-form>
          <div class="submit">
            <el-button type="primary" plain size="small" @click="submit"
              >{{ $t("保存") }}</el-button
            >
          </div>
        </div>
        <div class="slave">
          <div class="img">
            <el-image
              style="
                object-fit: cover;
                width: 300px;
                height: 300px;
                overflow: hidden;
                border-radius: 50%;
              "
              :src="$resolveAvatar(user.avatar, '/img/avatars/zhixue-admin-default.png')"
              :preview-src-list="avatar"
            >
            </el-image>
          </div>
          <div class="btns">
            <div>
              <el-upload :on-error="$uploadError"
                ref="upload"
                :action="uploadAvatarUrl() + '/' + this.user.id"
                :headers="{ x_access_token: uploadToken }"
                :show-file-list="false"
                :multiple="false"
                :before-upload="beforeAvatorUpload"
                :on-success="handleAvatorSuccess"
                accept="image/*"
              >
                <el-button size="small"
                  ><template #icon><i class="el-icon-picture-outline-round" aria-hidden="true"></i></template>{{ $t("修改头像") }}</el-button
                >
              </el-upload>
            </div>
            <div style="margin-top: 15px">
              <el-button size="small"  @click="changePassword"
                ><template #icon><i class="el-icon-key" aria-hidden="true"></i></template>{{ $t("修改密码") }}</el-button
              >
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { mixin } from "../../../minix";
import { getUser, setUserInfo, setUserAvatar } from "../../../api/api";
export default {
  mixins: [mixin],
  computed: {
    uploadToken() {
      return window.localStorage.getItem('token') || ''
    }
  },
  data() {
    var checkPhone = (rule, value, callback) => {
      if (!value) {
        return callback(new Error(t("请输入联系电话")));
      } else {
        const reg = /^1[3-9]\d{9}$/;
        if (reg.test(value)) {
          callback();
        } else {
          return callback(new Error(t("请输入正确的11位手机号码")));
        }
      }
    };
    return {
      user: {
        avatar:
          "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fsafe-img.xhscdn.com%2Fbw1%2Fb4a87154-18b6-4163-ac80-f4dc4bf58d09%3FimageView2%2F2%2Fw%2F1080%2Fformat%2Fjpg&refer=http%3A%2F%2Fsafe-img.xhscdn.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1693364028&t=c518c6c4a6ee2009b961f712a57c211f",
      },
      avatar: [
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fsafe-img.xhscdn.com%2Fbw1%2Fb4a87154-18b6-4163-ac80-f4dc4bf58d09%3FimageView2%2F2%2Fw%2F1080%2Fformat%2Fjpg&refer=http%3A%2F%2Fsafe-img.xhscdn.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1693364028&t=c518c6c4a6ee2009b961f712a57c211f",
      ],
      rules: {
        userName: [
          { required: true, get message() { return t("请输入用户名") }, trigger: "blur" },
        ],
        email: [
          { required: true, get message() { return t("请输入邮箱地址") }, trigger: "blur" },
          { type: "email", get message() { return t("请输入正确的邮箱地址") }, trigger: "blur" },
        ],
        tel: [
          {
            required: true,
            validator: checkPhone,
            get message() { return t("请输入正确的联系电话") },
            trigger: "blur",
          },
        ],
      },
    };
  },
  methods: {
    submit() {
      this.$refs["ruleForm"].validate((valid) => {
        if (valid) {
          setUserInfo(this.user).then((res) => {
            if (res.code == 1000) {
              this.$message({
                type: "success",
                get message() { return t("保存成功!") },
              });
              this.getUserInfo();
            } else {
              this.$notify.error({
                get title() { return t("错误") },
                message: res.message,
              });
            }
          }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
        } else {
          return false;
        }
      });
    },
    getUserInfo() {
      getUser().then((res) => {
        if (res.code == 1000) {
          this.user = res.data;
          this.user.sex = res.data.sex + "";
          this.avatar[0] = this.$resolveAvatar(this.user.avatar, '/img/avatars/zhixue-admin-default.png');
          this.setUser();
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    setUser() {
      this.$session.setUser(JSON.stringify(this.user));
    },
    changePassword() {
      //修改密码
      this.$bus.$emit("password", true);
    },
    handleAvatorSuccess(res) {
      let _this = this;
      if (res.code == 1000) {
        _this.$message({
          type: "success",
          get message() { return t("上传成功!") },
        });
        this.getUserInfo();
      } else {
        _this.$notify.error({
          get title() { return t("错误") },
          message: res.message,
        });
      }
    },
  },
  created() {},
  mounted() {
    this.getUserInfo();
  },
};
</script>

<style scoped>
.center {
  color: #3f3f3c;
}
.box-card {
  border-radius: 12px;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
  background: #fcfcfb;
}
.content {
  width: 100%;
  height: 550px;
  display: flex;
  flex-direction: row;
  font-family: "黑体";
}
.master {
  width: 60%;
  border-right: 1px solid #e8e8e5;
  padding-right: 20px;
}
.submit {
  width: 100%;
  text-align: center;
}
.slave {
  width: 40%;
}
.img {
  width: 100%;
  height: 75%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.btns {
  width: 100%;
  height: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
:deep(.el-form-item__label) {
  color: #585854;
}
:deep(.el-input__inner) {
  border-radius: 10px;
  border-color: #dfdfda;
  color: #3f3f3c;
  background: #ffffff;
}
:deep(.el-input__inner:focus) {
  border-color: #bdbdb6;
}
:deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #2f3437;
  background: #2f3437;
}
:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #2f3437;
}
:deep(.el-button--primary) {
  background: #2f3437;
  border-color: #2f3437;
  color: #ffffff;
}
:deep(.el-button--primary:hover), :deep(.el-button--primary:focus) {
  background: #3a4044;
  border-color: #3a4044;
}
:deep(.el-button) {
  border-radius: 10px;
  border-color: #d9d9d4;
  background: #f5f5f2;
  color: #3f3f3c;
}
:deep(.el-button:hover), :deep(.el-button:focus) {
  background: #ededea;
  border-color: #cfcfc8;
  color: #2f2f2c;
}
</style>
