<template>
<div>
  <el-dialog append-to-body :title="$t('编辑字典')" width="40%" :destroy-on-close="true" :model-value="updateDictVisible" :before-close="handleClose">
    <el-form :model="form" :rules="rules" ref="ruleForm">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("字典编码:") }}</span>
            <el-form-item prop="dictCode" style="margin-bottom:0">
                <el-input v-model="form.dictCode" size="small" :placeholder="$t('请输入字典编码')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("字典标签:") }}</span>
            <el-form-item prop="dictLabel" style="margin-bottom:0">
                <el-input v-model="form.dictLabel" size="small" :placeholder="$t('请输入字典标签')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("字典排序:") }}</span>
            <el-form-item prop="dictSort" style="margin-bottom:0">
                <el-input-number size="small" v-model="form.dictSort" :min="1"  :label="$t('字典排序')"></el-input-number>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("字典键值:") }}</span>
            <el-form-item prop="dictValue" style="margin-bottom:0">
                <el-input v-model="form.dictValue" size="small" :placeholder="$t('请输入字典键值')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("样式属性:") }}</span>
            <el-form-item prop="cssClass" style="margin-bottom:0">
                <el-input v-model="form.cssClass" size="small" :placeholder="$t('请输入样式属性')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("回显样式:") }}</span>
            <el-form-item prop="listClass" style="margin-bottom:0">
                <el-select clearable  size="small" v-model="form.listClass" :placeholder="$t('请选择回显样式')">
                  <el-option label="primary" value="primary"></el-option>
                  <el-option label="success" value="success"></el-option>
                  <el-option label="info" value="info"></el-option>
                  <el-option label="warning" value="warning"></el-option>
                  <el-option label="danger" value="danger"></el-option>
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

  import {editDict,getDictById} from '../../../api/api'
  export default {
    data() {
      return{
        formLabelWidth: '80px',
        form: {
          dictCode: "",
          dictLabel: "",
          dictSort: "",
          dictValue: "",
          cssClass: "",
          listClass: "",
          status: "0",
          remark: "",
        },
        rules: {
          dictCode: [
            { required: true, get message() { return t("请输入字典编码") }, trigger: 'blur' },
          ],
          dictSort: [
            { required: true, get message() { return t("请输入字典排序") }, trigger: 'blur' },
          ],
          dictLabel: [
            { required: true, get message() { return t("请输入字典标签") }, trigger: 'blur' },
          ],
          dictValue: [
            { required: true, get message() { return t("请输入字典键值") }, trigger: 'blur' },
          ],
        },
      }
    },
    props: ['updateDictVisible','updateId'],
    methods: {
      submit() {
        this.$refs["ruleForm"].validate((valid) => {
          if (valid) {
            editDict(this.form).then(res => {
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
        this.$emit("updateDictFalse")
      },
     
    },
    watch: {
      updateId(newVal) {
        if(newVal) {
          getDictById({id:newVal}).then(res => {
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