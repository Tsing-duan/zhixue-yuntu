<template>
<div>
  <el-dialog append-to-body :title="$t('修改用户')" width="40%" :destroy-on-close="true" :model-value="updateUserVisible" :before-close="handleClose">
    <el-form :model="form" :rules="rules" ref="ruleForm">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("用户昵称:") }}</span>
            <el-form-item prop="userName" style="margin-bottom:0">
              <el-input v-model="form.userName" size="small" :placeholder="$t('请输入用户昵称')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("手机号码:") }}</span>
            <el-form-item prop="tel" style="margin-bottom:0">
              <el-input v-model="form.tel" size="small" :placeholder="$t('请输入手机号码')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("邮箱:") }}</span>
            <el-form-item prop="email" style="margin-bottom:0">
              <el-input v-model="form.email" size="small" :placeholder="$t('请输入邮箱')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("登陆账号:") }}</span>
            <el-form-item prop="loginAccount" style="margin-bottom:0">
              <el-input v-model="form.loginAccount" size="small" :placeholder="$t('请输入登陆账号')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("用户性别:") }}</span>
            <el-form-item prop="sex" style="margin-bottom:0">
              <el-select size="small" v-model="form.sex" :placeholder="$t('请选择用户性别')">
                <el-option :label="$t('男')" value="0"></el-option>
                <el-option :label="$t('女')" value="1"></el-option>
              </el-select>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("岗位:") }}</span>
            <el-form-item prop="postId" style="margin-bottom:0">
              <el-select clearable size="small" v-model="form.postId" :placeholder="$t('请选择岗位')">
                <el-option v-for="(item,index) in post" :key="index" :label="item.postName" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("归属部门:") }}</span>
            <el-form-item prop="deptId" style="margin-bottom:0">
              <el-cascader
                    :model-value="parentId"
                    @change="handleChange"
                    size="small"
                    :options="data"
                    :props="{ checkStrictly: true,value:'id',label:'deptName',children:'children' }"
                    clearable>
                </el-cascader>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("角色:") }}</span>
            <el-form-item prop="roleIds" style="margin-bottom:0">
              <el-select size="small" multiple  v-model="form.roleIds" :placeholder="$t('请选择角色')">
                <el-option v-for="(item,index) in role" :label="item.roleName" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("状态:") }}</span>
             <el-form-item prop="name" style="margin-bottom:0">
                <el-radio-group style="margin-top:1px;height:40px;display:flex;align-items: center" v-model="form.status">
                  <el-radio value="0">{{ $t("正常") }}</el-radio>
                  <el-radio value="1">{{ $t("停用") }}</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("备注:") }}</span>
            <el-input type="textarea" v-model="form.remark"></el-input>
        </el-col>
      </el-row>
    </el-form>
    <template #footer><div  class="dialog-footer">
      <el-button size="small" type="primary" @click="submit">{{ $t("确 定") }}</el-button>
      <el-button size="small" @click="handleClose">{{ $t("取 消") }}</el-button>
    </div></template>
  </el-dialog>
</div>
</template>

<script>
import { t } from '@/i18n'

  import {getUserById,editUser} from '../../../api/api'
  import {validateAccount} from '../../../utils/accountPasswordRules'
  export default {
    data() {
      const validateNonAdminAccount = (rule, value, callback) => {
        if (Number(this.form.userType) === 0) {
          if (!value || !value.trim()) {
            callback(new Error('请输入登陆账号'))
            return
          }
          callback()
          return
        }
        validateAccount(rule, value, callback)
      }
      return{
        formLabelWidth: '80px',
        form: {
          idArrary: "",
          deptId: "",
          userName: "",
          loginAccount: "",
          userType: 0,
          email: "",
          tel: "",
          sex: "",
          avatar: "",
          password: "",
          status: "0",
          remark: "",
          roleIds: []
        },
        parentId: [],
        rules: {
          userName: [
            { required: true, get message() { return t("请输入用户昵称") }, trigger: 'blur' },
          ],
          loginAccount: [
            { validator: validateNonAdminAccount, trigger: 'blur' },
          ],
          password: [
            { required: true, get message() { return t("请输入登陆密码") }, trigger: 'blur' },
          ],
        }
      }
    },
    props: ['updateUserVisible','updateId','data','post','role'],
    methods: {
      submit() {
        this.$refs["ruleForm"].validate((valid) => {
          if (!valid) {
            return false;
          }
          editUser(this.form).then(res => {
          if(res.code == 1000) {
            this.$notify.success({
                  get title() { return t("成功") },
                  get message() { return t("保存成功") }
                });
            this.handleClose()
          } else {
            this.$notify.error({
              get title() { return t("错误") },
              message: res.message
            });
          }
          }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
        })
      },
      handleChange(value) {
        this.form.idArrary = ""
        this.form.deptId = ""
        var length = value.length
        if(length > 0) {
          this.form.idArrary = value.join(",")
          this.form.deptId = value[length-1]
        }
      },
      handleClose() {
        this.$emit("updateUserFalse")
      }
    },
    watch: {
  updateId(newVal) {
    if (newVal) {
      getUserById({ id: newVal }).then(res => {
        if (res.code == 1000) {
          // 合并而不是整体覆盖，避免后端 null 冲掉默认结构
          this.form = {
            ...this.form,
            ...res.data,
            sex: res.data.sex != null ? String(res.data.sex) : "",
            status: res.data.status != null ? String(res.data.status) : "0",
            roleIds: Array.isArray(res.data.roleIds) ? res.data.roleIds : []
          }

          const idArrary = this.form.idArrary
          if (idArrary && idArrary !== "0") {
            this.parentId = String(idArrary).split(",")
          } else {
            this.parentId = []
          }
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message
          })
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
    }
  }
}

 }
</script>

<style   scoped>
  .el-col {
      display: flex;
      flex-direction: row;
      align-items: center;
  }
  .search-title {
      font-family: '黑体';
      float: right;
      white-space: nowrap;
      font-size: 14px;
      width: 63px;
      text-align: right;
  }
</style>
