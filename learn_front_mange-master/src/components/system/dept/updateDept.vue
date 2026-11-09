<template>
<div>
  <el-dialog append-to-body :title="$t('修改部门')" width="40%" :destroy-on-close="true" :model-value="updateDeptVisible" :before-close="handleClose">
    <el-form :model="form" :rules="rules">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("上级部门:") }}</span>
            <el-form-item prop="parentId" style="margin-bottom:0">
                <el-cascader
                    :model-value="parentId"
                    @change="handleChange"
                    size="small"
                    :options="dropData"
                    :props="{ checkStrictly: true,value:'id',label:'deptName',children:'children' }"
                    clearable>
                </el-cascader>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("部门名称:") }}</span>
            <el-form-item prop="deptName" style="margin-bottom:0">
                <el-input v-model="form.deptName" size="small" :placeholder="$t('请输入部门名称')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("显示顺序:") }}</span>
            <el-form-item prop="orderNum" style="margin-bottom:0">
              <el-input-number size="small" v-model="form.orderNum" :min="1"  :label="$t('显示顺序')"></el-input-number>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("负责人:") }}</span>
            <el-form-item prop="leader" style="margin-bottom:0">
                <el-input v-model="form.leader" size="small" :placeholder="$t('请输入负责人')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("联系电话:") }}</span>
            <el-form-item prop="phone" style="margin-bottom:0">
                <el-input v-model="form.phone" size="small" :placeholder="$t('请输入联系电话')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("联系邮箱:") }}</span>
            <el-form-item prop="email" style="margin-bottom:0">
                <el-input v-model="form.email" size="small" :placeholder="$t('请输入邮箱')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("部门状态:") }}</span>
             <el-form-item prop="status" style="margin-bottom:0">
                <el-radio-group style="margin-top:1px;height:40px;display:flex;align-items: center" v-model="form.status">
                  <el-radio value="0">{{ $t("正常") }}</el-radio>
                  <el-radio value="1">{{ $t("停用") }}</el-radio>
                </el-radio-group>
            </el-form-item>
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

  import {getById,editDept} from '../../../api/api'
  export default {
    data() {
      return{
        formLabelWidth: '80px',
        form: {
          parentId: "",
          deptName: "",
          orderNum: 1,
          leader: "",
          phone: "",
          email: "",
          status: "0",
          idArrary: "",
        },
        parentId: [],
        rules: {
          deptName: [
            { required: true, get message() { return t("请输入部门名称") }, trigger: 'blur' },
          ],
          leader: [
            { required: true, get message() { return t("请输入负责人") }, trigger: 'blur' },
          ],
          phone: [
            { required: true, get message() { return t("请输入联系电话") }, trigger: 'blur' },
          ],
          email: [
            { required: true, get message() { return t("请输入联系邮箱") }, trigger: 'blur' },
          ],
        },
      }
    },
    props: ['updateDeptVisible','dropData','updateId'],
    methods: {
      submit() {
        editDept(this.form).then(res => {
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
        this.$emit("updateDeptFalse")
      },
      handleChange(value) {
        this.form.idArrary = ""
        var length = value.length
        if(length > 0) {
          this.form.idArrary = value.join(",")
          this.form.parentId = value[length-1]
        }
      }
    },
    watch: {
      updateId(newVal) {
        if(newVal) {
          getById({id:newVal}).then(res => {
            if(res.code == 1000) {
              this.form = res.data
              this.form.status = res.data.status + ""
              if (this.form.idArrary != "0") {
                this.parentId = this.form.idArrary.split(",")
              }
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
