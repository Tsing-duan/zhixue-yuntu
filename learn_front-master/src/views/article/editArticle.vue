<template>
  <div class="addArticle">
    <headerPage></headerPage>
    <div class="addArticle-content">
      <div class="addArticle-form">
        <!-- 页面标题 -->
        <div class="addArticle-title">
          编辑笔记
        </div>
        
        <!-- 表单容器 -->
        <div class="addArticle-list">
          <!-- 标题 -->
          <div class="addArticle-item">
            <div>标题</div>
            <div>
              <el-input 
                size="mini" 
                v-model="form.title" 
                placeholder="请输入笔记标题"
                clearable
              ></el-input>
            </div>
          </div>
          
          <!-- 摘要 -->
          <div class="addArticle-item">
            <div>摘要</div>
            <div>
              <el-input 
                type="textarea" 
                :rows="4" 
                v-model="form.desc" 
                size="mini" 
                placeholder="请输入笔记摘要"
                maxlength="200"
                show-word-limit
              ></el-input>
            </div>
          </div>
          
          <!-- 内容（富文本编辑器） -->
          <div class="addArticle-item">
            <div>内容</div>
            <div>
              <quill-editor 
                ref="myQuillEditor" 
                :options="editorOption"
                v-model="form.content"
              />
              <el-upload 
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
            <el-button @click="submit" size="mini" type="primary">✏️ 保存修改</el-button>
          </div>
        </div>
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { getApeArticleById, updateApeArticle } from '../../api/api' 
import { mixin } from '../../minix/index';
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'EditArticle',
  mixins: [mixin],
  data() {
    return {
      articleId: "",
      form: {
        desc: "",
        content: "",
        title: ""
      },
      editorOption: {
        placeholder: "在这里编辑你的笔记内容...",
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
    submit() {
      if (!this.form.title) {
        this.$message({ message: '请输入笔记标题', type: 'warning' })
        return
      }
      if (!this.form.content) {
        this.$message({ message: '请输入笔记内容', type: 'warning' })
        return
      }
      if (!this.form.desc) {
        this.$message({ message: '请输入笔记摘要', type: 'warning' })
        return
      }
      
      var param = {
        id: this.articleId,
        title: this.form.title,
        articleDesc: this.form.desc,
        content: this.form.content,
        state: 2
      }
      updateApeArticle(param).then(res => {
        if(res.code == 1000) {
          this.$message({
            message: '修改成功，请等待管理员审核',
            type: 'success'
          })
          setTimeout(() => {
            this.$router.push("/articleInfo?id=" + this.articleId)
          }, 1500)
        } else {
          this.$message({
            message: res.message,
            type: 'error'
          })
        }
      }).catch(() => {
        this.$message({
          message: '修改失败，请稍后重试',
          type: 'error'
        })
      })
    },
    handleQillSuccess(res) {
      if(res.code == 1000){
        this.$notify({
          title: '上传成功',
          type: 'success'
        });
        let imgurl = this.$store.state.HOST + res.message
        let quill = this.$refs.myQuillEditor.quill
        let position = quill.getSelection().index
        quill.insertEmbed(position, 'image', imgurl)
        quill.setSelection(position + 1)
      } else {
        this.$notify({
          title: '上传失败',
          type: 'error'
        });
      }
    },
    getApeArticleById() {
      getApeArticleById({ id: this.articleId }).then(res => {
        if(res.code == 1000) {
          this.form.title = res.data.title
          this.form.desc = res.data.articleDesc
          this.form.content = res.data.content
        }
      }).catch(() => {
        this.$message({
          message: '获取笔记信息失败',
          type: 'error'
        })
      })
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' })
    var id = this.$route.query.id
    if (id) {
      this.articleId = id
      this.getApeArticleById()
    } else {
      this.$message({
        message: '参数错误',
        type: 'error'
      })
      this.$router.push("/article")
    }
  }
}
</script>

<style scoped>
@import url("../../assets/css/article/addArticle.css");
</style>