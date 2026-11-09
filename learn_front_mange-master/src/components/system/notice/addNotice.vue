<template>
<div>
  <el-dialog append-to-body :title="$t('新增通知公告')" width="40%" :destroy-on-close="true" :model-value="addNoticeVisible" :before-close="handleClose">
    <el-form :model="form" :rules="rules" ref="ruleForm">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("标题:") }}</span>
            <el-form-item prop="title" style="margin-bottom:0">
                <el-input v-model="form.title" size="small" :placeholder="$t('请输入标题')" autocomplete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("公告类型:") }}</span>
            <el-form-item prop="type" style="margin-bottom:0">
              <el-select size="small" v-model="form.type" :placeholder="$t('请选择')">
                <el-option :label="$t('公告')" value="0"></el-option>
                <el-option :label="$t('通知')" value="1"></el-option>
              </el-select>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <span class="search-title">{{ $t("状态:") }}</span>
             <el-form-item prop="state" style="margin-bottom:0">
                <el-radio-group style="margin-top:1px;height:40px;display:flex;align-items: center" v-model="form.state">
                  <el-radio value="0">{{ $t("正常") }}</el-radio>
                  <el-radio value="1">{{ $t("关闭") }}</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("内容:") }}</span>
            <quill-editor ref="myQuillEditor" :options="editorOption"
            v-model="form.content"/>
            <el-upload :on-error="$uploadError" class="Quill-uploader" :show-file-list="false" accept="image/*" v-show="0" ref="updateUpload" :multiple="false" :action="uploadImageUrl()"
                        :headers="uploadHeaders()"
                :on-success="handleQillSuccess">
            </el-upload>
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

  import { mixin } from '../../../minix/index';
  import {saveAccount} from '../../../api/api'
  export default {
    mixins: [mixin],
    data() {
      return{
        editorOption: {
          get placeholder() { return t("请输入文字内容") },
          modules: { 
            toolbar: {
                container: [
                    ["bold", "italic", "underline", "strike"], 
                    ["blockquote", "code-block"], 
                    [{ header: 1 }, { header: 2 }], 
                    [{ list: "ordered" }, { list: "bullet" }], 
                    [{ script: "sub" }, { script: "super" }], 
                    [{ indent: "-1" }, { indent: "+1" }], 
                    [{ direction: "rtl" }], 
                    [{ size: ["small", false, "large", "huge"] }], 
                    [{ header: [1, 2, 3, 4, 5, 6, false] }], 
                    [{ color: [] }, { background: [] }], 
                    [{ font: [] }], [{ align: [] }], 
                    ["clean"], ["image"]
                ],
                handlers: {
                    'image': function ( value ) {
                        if ( value ) {
                            // 触发上传插件
                            document.querySelector('.Quill-uploader .el-upload__input').click()
                        } else {
                            this.quill.format( 'image', false );
                        }
                    }
                }
            },
            syntax: { 
                highlight: text => { 
                    return hljs.highlightAuto(text).value; // 这里就是代码高亮需要配置的地方 
                }
            }
          }
        },
        formLabelWidth: '60px',
        form: {
          title: "",
          type: "0",
          state: "0",
          content: "",
        },
        rules: {
          title: [
            { required: true, get message() { return t("请输入标题") }, trigger: 'blur' },
          ],
        },
      }
    },
    props: ['addNoticeVisible'],
    methods: {
      submit() {
        this.$refs["ruleForm"].validate((valid) => {
          if (valid) {
            saveAccount(this.form).then(res => {
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
        this.form = {
          title: "",
          type: "0",
          state: "0",
          content: ""
        },
        this.$emit("addNoticeFalse")
      },
      handleQillSuccess(res){
            let _this = this;
            if(res.code == 1000){
                _this.$notify({
                    get title() { return t("上传成功") },
                    type: 'success'
                });
                let imgurl = this.$config.HOST + res.message
                let quill = this.$refs.myQuillEditor.quill
                let position = (quill.getSelection(true)?.index ?? Math.max(0, quill.getLength() - 1))
                quill.insertEmbed(position, 'image', imgurl)
                quill.setSelection(position + 1)
            }else{
                _this.$notify({
                    title: '上传失败',
                    type: 'error'
                });
            }
        },
    },
    created() {
     
    },
    mounted() {
      
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
  :deep(.ql-editor) {
    min-height: 150px !important;
  }
</style>