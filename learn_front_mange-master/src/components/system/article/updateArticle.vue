<template>
<div>
  <el-dialog append-to-body :title="$t('编辑笔记')" width="40%" :destroy-on-close="true" :model-value="updateVisible" :before-close="handleClose">
    <el-form :model="form" :rules="rules" ref="ruleForm">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("所属课程:") }}</span>
                <div style="width:100%">
                    <el-form-item prop="taskId" style="margin-bottom:0">
                        <el-select :disabled="type == 0 ? true:false" clearable style="margin-top:10px" size="small" v-model="form.taskId" :placeholder="$t('请选择')">
                            <el-option v-for="(item,index) in task" :key="index"
                                :label="item.name"
                                :value="item.id">
                            </el-option>
                        </el-select>
                    </el-form-item>
                </div>
        </el-col>
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("标题:") }}</span>
                <div style="width:100%">
                  <el-form-item prop="title" style="margin-bottom:0">
                      <el-input v-model="form.title" size="small" :placeholder="$t('请输入标题')" autocomplete="off"></el-input>
                  </el-form-item>
                </div>
        </el-col>
        <el-col style="margin-top:25px" :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("摘要:") }}</span>
                <div style="width:100%">
                  <el-form-item prop="articleDesc" style="margin-bottom:0">
                      <el-input type="textarea" v-model="form.articleDesc" size="small" :placeholder="$t('请输入摘要')" autocomplete="off"></el-input>
                  </el-form-item>
                </div>
        </el-col>
        <el-col style="margin-top:25px" :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("内容:") }}</span>
                <div style="width:100%">
                  <el-form-item prop="content" style="margin-bottom:0">
                    <quill-editor ref="myQuillEditor" :options="editorOption"
                    v-model="form.content"/>
                    <el-upload :on-error="$uploadError" class="Quill-uploader" :show-file-list="false" accept="image/*" v-show="0" ref="updateUpload" :multiple="false" :action="uploadImageUrl()"
                        :headers="uploadHeaders()"
                        :on-success="handleQillSuccess">
                    </el-upload>
                  </el-form-item>
                </div>
        </el-col>
        <el-col style="margin-top:25px" :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
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

  import { mixin } from '../../../minix/index';
  import {editApeArticle,getApeArticleById,getApeTaskList,getApeTaskByTeacherId} from '../../../api/api'
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
        formLabelWidth: '80px',
        form: {
          taskId: "",
          title: "",
          articleDesc: "",
          content: "",
          state: "",
          remark: "",
          taskName: "",
        },
        task: [],
        rules: { 
          taskId: [
            { required: true, get message() { return t("请选择课程") }, trigger: 'blur' },
          ],
          title: [
            { required: true, get message() { return t("请输入标题") }, trigger: 'blur' },
          ],
          articleDesc: [
            { required: true, get message() { return t("请输入摘要") }, trigger: 'blur' },
          ],
          content: [
            { required: true, get message() { return t("请输入笔记内容") }, trigger: 'blur' },
          ],
        },
      }
    },
    props: ['updateVisible','updateId','taskId','type','flag'],
    methods: {
      submit() {
        this.$refs["ruleForm"].validate((valid) => {
          if (valid) {
            this.form.state = 2
            editApeArticle(this.form).then(res => {
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
      handleClose() {
        this.$emit("updateFalse")
      },
      getApeTaskList() {
          getApeTaskList().then(res => {
              if(res.code == 1000) {
                this.task = res.data;
              } else {
                this.$notify.error({
                  get title() { return t("错误") },
                  message: res.message
                });
              }
          }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
      },
      getApeTaskByTeacherId() {
        getApeTaskByTeacherId().then(res => {
              if(res.code == 1000) {
                this.task = res.data;
              } else {
                this.$notify.error({
                  get title() { return t("错误") },
                  message: res.message
                });
              }
          }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
      },
    },
    created() {
     
    },
    mounted() {
      if(this.flag == 2) {
        this.getApeTaskByTeacherId()
      } else {
        this.getApeTaskList()
      }
    },
    watch: {
      updateId(newVal) {
        if(newVal) {
          getApeArticleById({id:newVal}).then(res => {
            if(res.code == 1000) {
              this.form = res.data
              this.form.state = res.data.state + ""
            } else {
              this.$notify.error({
                get title() { return t("错误") },
                message: res.message
              });
            }
          }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
        }
      },
      updateVisible(newVal) {
        if(newVal) {
          if(this.taskId) {
            this.form.taskId = this.taskId
          }
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