<template>
  <div class="addArticle">
    <headerPage></headerPage>
    <div class="addArticle-content">
      <div class="addArticle-form">
        <!-- 页面标题 -->
        <div class="addArticle-title">{{ $t("记笔记") }}</div>
        
        <!-- 表单容器 -->
        <div class="addArticle-list">
          <!-- 标题 -->
          <div class="addArticle-item">
            <div>{{ $t("标题") }}</div>
            <div>
              <el-input 
                size="small"
                v-model="form.title" 
                :placeholder="$t('请输入笔记标题')"
                clearable
              ></el-input>
            </div>
          </div>
          
          <!-- 所属课程 -->
          <div class="addArticle-item">
            <div>{{ $t("所属课程") }}</div>
            <div>
              <el-select 
                size="small"
                v-model="form.taskId" 
                :placeholder="$t('请选择所属课程')"
                clearable
              >
                <el-option 
                  v-for="(item,index) in task" 
                  :key="index"
                  :label="item.taskName" 
                  :value="item.taskId"
                ></el-option>
              </el-select>
            </div>
          </div>
          
          <!-- 摘要 -->
          <div class="addArticle-item">
            <div>{{ $t("摘要") }}</div>
            <div>
              <el-input 
                type="textarea" 
                :rows="4" 
                v-model="form.articleDesc" 
                size="small"
                :placeholder="$t('请输入笔记摘要')"
                maxlength="200"
                show-word-limit
              ></el-input>
            </div>
          </div>
          
          <!-- 内容（富文本编辑器） -->
          <div class="addArticle-item">
            <div>{{ $t("内容") }}</div>
            <div>
              <quill-editor 
                ref="myQuillEditor" 
                :options="editorOption"
                v-model="form.content"
              />
              <el-upload :on-error="$uploadError"
                class="Quill-uploader" 
                :show-file-list="false" 
                accept="image/*" 
                v-show="0" 
                ref="updateUpload" 
                :multiple="false" 
                :action="uploadImageUrl()"
                        :headers="uploadHeaders()"
                :on-success="handleQillSuccess"
              >
              </el-upload>
            </div>
          </div>
          
          <!-- 提交按钮 -->
          <div style="display: flex; justify-content: center; margin-top: 16px;">
            <el-button @click="submit" size="small" type="primary">{{ $t("📝 发布笔记") }}</el-button>
          </div>
        </div>
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { t } from '@/i18n'
 
import { getApeMyTaskList, saveApeArticle } from '../../api/api' 
import { mixin } from '../../minix/index';
import headerPage from "../../components/header/header.vue"
import bottomPage from "../../components/bottom/bottom.vue"

export default {
  name: 'AddArticle',
  mixins: [mixin],
  data() {
    return {
      task: [],
      form: {
        articleDesc: "",
        content: "",
        title: "",
        taskId: "",
        state: 2
      },
      editorOption: {
        get placeholder() { return t("在这里写下你的笔记内容...") },
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
              [{ font: [] }], 
              [{ align: [] }], 
              ["clean"], 
              ["image"]
            ],
            handlers: {
              'image': function (value) {
                if (value) {
                  document.querySelector('.Quill-uploader .el-upload__input').click()
                } else {
                  this.quill.format('image', false);
                }
              }
            }
          }
        }
      },
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    getApeMyTaskList() {
      var param = { state: 0 }
      getApeMyTaskList(param).then(res => {
        if (res.code == 1000) {
          this.task = res.data
        }
      }).catch(() => {
        this.$notify.error({
          get title() { return t("错误") },
          message: '获取课程列表失败，请稍后重试'
        });
      })
    },
    submit() {
      if (!this.form.title) {
        this.$message({ get message() { return t("请输入笔记标题") }, type: 'warning' })
        return
      }
      if (!this.form.content) {
        this.$message({ get message() { return t("请输入笔记内容") }, type: 'warning' })
        return
      }
      if (!this.form.articleDesc) {
        this.$message({ get message() { return t("请输入笔记摘要") }, type: 'warning' })
        return
      }
      if (!this.form.taskId) {
        this.$message({ get message() { return t("请选择所属课程") }, type: 'warning' })
        return
      }
      saveApeArticle(this.form).then(res => {
        if(res.code == 1000) {
          this.$message({
            get message() { return t("发布成功，请等待管理员审核") },
            type: 'success'
          })
          this.form = {
            articleDesc: "",
            content: "",
            title: "",
            taskId: "",
            state: 2
          }
          // 重置富文本编辑器
          this.$refs.myQuillEditor.quill.setText('')
        } else {
          this.$message({
            message: res.message,
            type: 'error'
          })
        }
      }).catch(() => {
        this.$message({
          get message() { return t("发布失败，请稍后重试") },
          type: 'error'
        })
      })
    },
    handleQillSuccess(res) {
      if(res.code == 1000){
        this.$notify({
          get title() { return t("上传成功") },
          type: 'success'
        });
        let imgurl = this.$config.HOST + res.message
        let quill = this.$refs.myQuillEditor.quill
        let position = (quill.getSelection(true)?.index ?? Math.max(0, quill.getLength() - 1))
        quill.insertEmbed(position, 'image', imgurl)
        quill.setSelection(position + 1)
      } else {
        this.$notify({
          title: '上传失败',
          type: 'error'
        });
      }
    },
  },
  mounted() {
    this.getApeMyTaskList()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}
</script>

<style scoped>
@import url("../../assets/css/article/addArticle.css");
</style>