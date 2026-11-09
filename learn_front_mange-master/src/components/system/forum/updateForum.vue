<template>
<div>
  <el-dialog append-to-body :title="$t('编辑论坛')" width="40%" :destroy-on-close="true" :model-value="updateVisible" :before-close="handleClose">
    <el-form :model="form" :rules="rules" ref="ruleForm">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("讨论名称:") }}</span>
            <div style="width:100%">
              <el-form-item prop="name" style="margin-bottom:0">
                  <el-input v-model="form.name" size="small" :placeholder="$t('请输入讨论名称')" autocomplete="off"></el-input>
              </el-form-item>
            </div>
        </el-col>
        <el-col style="margin-top:20px" :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("讨论内容:") }}</span>
            <div style="width:100%">
              <el-form-item prop="content" style="margin-bottom:0">
                  <el-input type="textarea" v-model="form.content" size="small" :placeholder="$t('请输入讨论内容')" autocomplete="off"></el-input>
              </el-form-item>
            </div>
        </el-col>
        <el-col style="margin-top:20px" :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("备注:") }}</span>
            <div style="width:100%">
              <el-form-item prop="remark" style="margin-bottom:0">
                  <el-input type="textarea" v-model="form.remark" size="small" :placeholder="$t('请输入备注')" autocomplete="off"></el-input>
              </el-form-item>
            </div>
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

  import {editApeForum,getApeForumById} from '../../../api/api'
  export default {
    data() {
      return{
        formLabelWidth: '80px',
        form: {
          name: "",
          content: "",
          remark: "",
        },
        rules: {
          name: [
            { required: true, get message() { return t("请输入讨论名称") }, trigger: 'blur' },
          ],
          content: [
            { required: true, get message() { return t("请输入讨论内容") }, trigger: 'blur' },
          ],
        },
      }
    },
    props: ['updateVisible','updateId'],
    methods: {
      submit() {
        this.$refs["ruleForm"].validate((valid) => {
          if (valid) {
            editApeForum(this.form).then(res => {
              if(res.code == 1000) {
                this.$notify.success({
                  get title() { return t("成功") },
                  get message() { return t("保存成功") }
                });
                this.handleClose();
              } else {
                this.$notify.error({
                  get title() { return t("错误") },
                  message: res.message
                });
              }
            }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
          } else {
            return false;
          }
        });
      },
      handleClose() {
        this.$emit("updateFalse")
      },
     
    },
    created() {
     
    },
    mounted() {
      
    },
    watch: {
      updateId(newVal) {
        if(newVal) {
          getApeForumById({id:newVal}).then(res => {
            if(res.code == 1000) {
              this.form = res.data
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

<style scoped>
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