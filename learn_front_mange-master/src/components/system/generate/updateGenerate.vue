<template>
<div>
  <el-dialog append-to-body :title="$t('编辑生成')" width="65%" :destroy-on-close="true" :model-value="updateGenVisible" :before-close="handleClose">
    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('基本信息')" name="first">
      </el-tab-pane>
      <el-tab-pane :label="$t('字段信息')" name="second">
      </el-tab-pane>
      <el-tab-pane :label="$t('生成信息')" name="third">
      </el-tab-pane>
    </el-tabs>
    <el-form v-show="activeName == 'first'" :model="form">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("表名称:") }}</span>
            <el-form-item prop="tableName" style="margin-bottom:0">
              <el-input v-model="form.tableName" size="small" :placeholder="$t('请输入表名称')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("表描述:") }}</span>
            <el-form-item prop="tableComment" style="margin-bottom:0">
              <el-input v-model="form.tableComment" size="small" :placeholder="$t('请输入表描述')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("实体类名称:") }}</span>
            <el-form-item prop="className" style="margin-bottom:0">
              <el-input v-model="form.className" size="small" :placeholder="$t('请输入实体类名称')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("作者:") }}</span>
            <el-form-item prop="functionAuthor" style="margin-bottom:0">
              <el-input v-model="form.functionAuthor" size="small" :placeholder="$t('请输入作者')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("备注:") }}</span>
            <el-input type="textarea" v-model="form.remark"></el-input>
        </el-col>
      </el-row>
    </el-form>
    <div class="table" v-show="activeName == 'second'">
      <el-table
        :data="column"
        :header-cell-style="{
        'color': '#4A2B90',
        'background-color': '#ECE9F4',
        }"
        :row-style="{
        'color': '#888897',
        'font-size': '15px',
        'font-family':'黑体',
        'white-space': 'nowrap'
        }"
        stripe
        style="width: 100%">
        <el-table-column
        type="selection"
        width="55">
        </el-table-column>
        <el-table-column
        prop="columnName"
        :label="$t('字段名称')"
        >
        </el-table-column>
        <el-table-column
        prop="columnComment"
        :label="$t('字段描述')"
        >
        </el-table-column>
        <el-table-column
        prop="columnType"
        :label="$t('字段类型')"
        >
        </el-table-column>
        <el-table-column
        prop="javaField"
        :label="$t('Java属性')"
        >
        </el-table-column>
        <el-table-column
        prop="javaType"
        :label="$t('Java类型')"
        >
          <template #default="scope">
              <el-select size="small" v-model="column[scope.$index].javaType" :placeholder="$t('请选择')">
                  <el-option label="Long" value="Long"></el-option>
                  <el-option label="String" value="String"></el-option>
                  <el-option label="Integer" value="Integer"></el-option>
                  <el-option label="Double" value="Double"></el-option>
                  <el-option label="BigDecimal" value="BigDecimal"></el-option>
                  <el-option label="Date" value="Date"></el-option>
                  <el-option label="Boolean" value="Boolean"></el-option>
              </el-select>
          </template>
        </el-table-column>
        <el-table-column
        prop="isInsert"
        :label="$t('插入')"
        width=40
        >
          <template #default="scope">
            <el-checkbox true-label="1" false-label="0" v-model="column[scope.$index].isInsert"></el-checkbox>
          </template>
        </el-table-column>
        <el-table-column
        prop="isEdit"
        :label="$t('编辑')"
        width=40
        >
          <template #default="scope">
            <el-checkbox true-label="1" false-label="0" v-model="column[scope.$index].isEdit"></el-checkbox>
          </template>
        </el-table-column>
        <el-table-column
        prop="isList"
        :label="$t('列表')"
        width=40
        >
          <template #default="scope">
            <el-checkbox true-label="1" false-label="0" v-model="column[scope.$index].isList"></el-checkbox>
          </template>
        </el-table-column>
        <el-table-column
        prop="isQuery"
        :label="$t('查询')"
        width=40
        >
          <template #default="scope">
            <el-checkbox true-label="1" false-label="0" v-model="column[scope.$index].isQuery"></el-checkbox>
          </template>
        </el-table-column>
        <el-table-column
        prop="queryType"
        :label="$t('查询方式')"
        >
          <template #default="scope">
              <el-select size="small" v-model="column[scope.$index].queryType" :placeholder="$t('请选择')">
                  <el-option label="=" value="EQ"></el-option>
                  <el-option label="!=" value="NE"></el-option>
                  <el-option label=">" value="GT"></el-option>
                  <el-option label=">=" value="GTE"></el-option>
                  <el-option label="<" value="LT"></el-option>
                  <el-option label="<=" value="LTE"></el-option>
                  <el-option label="LIKE" value="LIKE"></el-option>
                  <el-option label="BETWEEN" value="BETWEEN"></el-option>
              </el-select>
          </template>
        </el-table-column>
        <el-table-column
        prop="isRequired"
        :label="$t('必填')"
        width=40
        >
          <template #default="scope">
            <el-checkbox true-label="1" false-label="0" v-model="column[scope.$index].isRequired"></el-checkbox>
          </template>
        </el-table-column>
        <el-table-column
        prop="htmlType"
        :label="$t('显示类型')"
        >
            <template #default="scope">
              <el-select size="small" v-model="column[scope.$index].htmlType" :placeholder="$t('请选择')">
                  <el-option :label="$t('文本框')" value="文本框"></el-option>
                  <el-option :label="$t('文本域')" value="文本域"></el-option>
                  <el-option :label="$t('下拉框')" value="下拉框"></el-option>
                  <el-option :label="$t('单选框')" value="单选框"></el-option>
                  <el-option :label="$t('日期控件')" value="日期控件"></el-option>
              </el-select>
            </template>
        </el-table-column>
        <el-table-column
        prop="dictType"
        :label="$t('字典类型')"
        >
          <template #default="scope">
            <el-select size="small" clearable v-model="column[scope.$index].dictType" :placeholder="$t('请选择')">
                <el-option v-for="(item,index) in dict" :key="index" :label="item.dictCode" :value="item.dictCode"></el-option>
            </el-select>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-form v-show="activeName == 'third'" :model="form">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("生成模板:") }}</span>
            <el-form-item prop="tplCategory" style="margin-bottom:0">
              <el-select size="small" v-model="form.tplCategory" :placeholder="$t('请选择')">
                  <el-option
                    :label="$t('单表（增删改查）')"
                    value="crud">
                  </el-option>
              </el-select>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">
                <el-tooltip class="item" effect="dark" :content="$t('生成在哪个java包下，例如 com.ape.apeadmin')" placement="top-start">
                    <i class="el-icon-question"></i>
                </el-tooltip>{{ $t("生成包路径:") }}</span>
            <el-form-item prop="packageName" style="margin-bottom:0">
                <el-input v-model="form.packageName" size="small" :placeholder="$t('请输入生成包路径')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">
                <el-tooltip class="item" effect="dark" :content="$t('可理解为子系统名，例如 system')" placement="top-start">
                    <i class="el-icon-question"></i>
                </el-tooltip>{{ $t("生成模块名:") }}</span>
            <el-form-item prop="moduleName" style="margin-bottom:0">
                <el-input v-model="form.moduleName" size="small" :placeholder="$t('请输入生成模块名')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">
                <el-tooltip class="item" effect="dark" :content="$t('可理解为功能英文名，例如 user')" placement="top-start">
                    <i class="el-icon-question"></i>
                </el-tooltip>{{ $t("生成业务名:") }}</span>
            <el-form-item prop="businessName" style="margin-bottom:0">
                <el-input v-model="form.businessName" size="small" :placeholder="$t('请输入生成业务名')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">
                <el-tooltip class="item" effect="dark" :content="$t('用作类描述，例如 用户')" placement="top-start">
                    <i class="el-icon-question"></i>
                </el-tooltip>{{ $t("生成功能名:") }}</span>
            <el-form-item prop="functionName" style="margin-bottom:0">
                <el-input v-model="form.functionName" size="small" :placeholder="$t('请输入生成功能名')" autocomplete="off"></el-input>
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

  import {editGen,getGenById,getDictTypeList} from '../../../api/api'
  export default {
    data() {
      return{
        activeName: 'second',
        column: [],
        form: {
          tableName: "",
          tableComment: "",
          className: "",
          tplCategory: "",
          packageName: "",
          moduleName: "",
          businessName: "",
          functionName: "",
          functionAuthor: "",
          genPath: "",
          genType: "0",
          options: "",
          remark: "",
        },
        dict: [],
      }
    },
    props: ['updateGenVisible','updateId'],
    methods: {
      submit() {
        if(!this.form.tableName) {
            this.$notify.error({
              get title() { return t("错误") },
              get message() { return t("请输入表名称") }
            });
            return;
        }
        if(!this.form.tableComment) {
            this.$notify.error({
              get title() { return t("错误") },
              get message() { return t("请输入表描述") }
            });
            return;
        }
        if(!this.form.className) {
            this.$notify.error({
              get title() { return t("错误") },
              get message() { return t("请输入实体类名称") }
            });
            return;
        }
        if(!this.form.functionAuthor) {
            this.$notify.error({
              get title() { return t("错误") },
              get message() { return t("请输入作者") }
            });
            return;
        }
        if(!this.form.packageName) {
            this.$notify.error({
              get title() { return t("错误") },
              get message() { return t("请输入生成包路径") }
            });
            return;
        }
        if(!this.form.moduleName) {
            this.$notify.error({
              get title() { return t("错误") },
              get message() { return t("请输入生成模块名") }
            });
            return;
        }
        if(!this.form.businessName) {
            this.$notify.error({
              get title() { return t("错误") },
              get message() { return t("请输入生成业务名") }
            });
            return;
        }
        if(!this.form.functionName) {
            this.$notify.error({
              get title() { return t("错误") },
              get message() { return t("请输入生成功能名") }
            });
            return;
        }
        editGen({apeGenTable: this.form,columns: this.column}).then(res => {
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
      },
      handleClose() {
        this.$emit("updateGenFalse")
      },
    },
    watch: {
      updateId(newVal) {
        if(newVal) {
          getGenById({id:newVal}).then(res => {
            if(res.code == 1000) {
              this.form = res.data.table
              this.column = res.data.column
              for(let i = 0;i < this.column.length;i++) {
                this.column[i].isPk = this.column[i].isPk + ""
                this.column[i].isRequired = this.column[i].isRequired + ""
                this.column[i].isInsert = this.column[i].isInsert + ""
                this.column[i].isEdit = this.column[i].isEdit + ""
                this.column[i].isList = this.column[i].isList + ""
                this.column[i].isQuery = this.column[i].isQuery + ""
              }
            } else {
              this.$notify.error({
                get title() { return t("错误") },
                message: res.message
              });
            }
          }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
          getDictTypeList().then(res => {
            if(res.code == 1000) {
              this.dict = res.data
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
  .table {
      background: #ffffff;
      border-radius: 7px;
      box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
      margin-top: 10px;
      height: 300px;
      overflow-y: scroll;
  }
  .search-title {
      font-family: '黑体';
      float: right;
      white-space: nowrap;
      font-size: 14px;
      width: 100px;
      text-align: right;
  }
  .el-tree {
      border: 1px solid #BDC1C9;
  }
</style>