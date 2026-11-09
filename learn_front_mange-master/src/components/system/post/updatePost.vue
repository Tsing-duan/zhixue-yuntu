<template>
<div>
  <el-dialog append-to-body :title="$t('修改岗位')" width="40%" :destroy-on-close="true" :model-value="updatePostVisible" :before-close="handleClose">
    <el-form :model="form" :rules="rules" ref="ruleForm">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("岗位名称:") }}</span>
            <el-form-item prop="postName" style="margin-bottom:0">
                <el-input v-model="form.postName" size="small" :placeholder="$t('请输入岗位名称')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
         <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("岗位编码:") }}</span>
            <el-form-item prop="postCode" style="margin-bottom:0">
                <el-input v-model="form.postCode" size="small" :placeholder="$t('请输入岗位编码')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("岗位顺序:") }}</span>
            <el-form-item prop="postSort" style="margin-bottom:0">
              <el-input-number size="small" v-model="form.postSort" :min="1"  :label="$t('岗位顺序')"></el-input-number>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("岗位状态:") }}</span>
             <el-form-item prop="status" style="margin-bottom:0">
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

  import {editPost,getPostById} from '../../../api/api'
  export default {
    data() {
      return{
        formLabelWidth: '80px',
        form: {
          postCode: "",
          postName: "",
          status: "0",
          postSort: 1,
          remark: ""
        },
        rules: {
          postCode: [
            { required: true, get message() { return t("请输入岗位编码") }, trigger: 'blur' },
          ],
          postName: [
            { required: true, get message() { return t("请输入岗位名称") }, trigger: 'blur' },
          ],
        },
      }
    },
    props: ['updatePostVisible','updateId'],
    methods: {
      submit() {
        editPost(this.form).then(res => {
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
      },
      handleClose() {
        this.$emit("updatePostFalse")
      },
    },
    watch: {
      updateId(newVal) {
        if(newVal) {
          getPostById({id:newVal}).then(res => {
            if(res.code == 1000) {
              this.form = res.data
              this.form.status = res.data.status + ""
            } else {
              this.$notify.error({
                get title() { return t("错误") },
                message: res.message
              });
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
      margin-top: 12px
  }
  .search-title {
      font-family: '黑体';
      float: right;
      white-space: nowrap;
      font-size: 14px;
      width: 84px;
      text-align: right;
  }
  .el-tree {
      border: 1px solid #BDC1C9;
  }
</style>
