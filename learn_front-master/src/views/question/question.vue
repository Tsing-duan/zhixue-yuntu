<template>
  <div class="message">
    <headerPage></headerPage>
    <div class="message-page-content">
      <!-- 提问输入卡片 -->
      <div class="message-page-add">
        <div>
          <div>发布提问</div>
          <el-input 
            type="textarea" 
            resize="none" 
            :rows="8" 
            v-model="content" 
            placeholder="描述你在学习中遇到的问题，老师会尽快为你解答..."
            maxlength="500"
            show-word-limit
          ></el-input>
          <div class="message-page-btns">
            <el-button size="mini" icon="el-icon-message" type="primary" plain @click="add">提交提问</el-button>
            <el-button size="mini" icon="el-icon-refresh" type="warning" plain @click="refresh">清空内容</el-button>
          </div>
        </div>
      </div>
      
      <!-- 页面标题 -->
      <div class="message-page-title">
        <i class="title-icon el-icon-question"></i>
        我的答疑
        <span style="font-size: 14px; color: #94a3b8; margin-left: 12px; font-weight: normal;">
          共 {{ total }} 条提问
        </span>
      </div>
      
      <!-- 答疑列表 -->
      <div class="message-page-list">
        <div v-for="(item,index) in tableData" :key="index" class="message-page-item">
          <div class="message-page-top">
            <div>
              {{index + 1}}. {{item.content}}
            </div>
            <div>
              <i class="el-icon-success"></i>
            </div>
          </div>
          <div class="messsage-page-huifu">
            <span>教师回复</span>
            <div v-if="item.answer" class="has-reply">
              <i class="el-icon-chat-line-round" style="margin-right: 6px;"></i>
              {{item.answer}}
            </div>
            <div v-else class="no-reply">
              <i class="el-icon-time"></i>
              暂无回复，请耐心等待
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="message-page-fenye" v-if="total > 10">
        <el-pagination
          background
          :page-size="pageSize"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
      
      <!-- 底部装饰 -->
      <div class="message-page-bottom">
        <img src="../../assets/image/02.jpg" alt="" @error="$event.target.style.display='none'">
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { getApeQuestionPage, saveApeQuestion } from '../../api/api' 
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'Question',
  data() {
    return {
      taskId: "",
      user: {},
      pageSize: 10,
      pageNumber: 1,
      tableData: [],
      total: 0,
      content: ""
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    add() {
      if (!this.content.trim()) {
        this.$message({
          message: '请输入提问内容',
          type: 'warning'
        });
        return
      }
      var param = {
        taskId: this.taskId,
        content: this.content
      }
      saveApeQuestion(param).then(res => {
        if(res.code == 1000) {
          this.$message({
            message: '提问成功，请等待教师回复',
            type: 'success'
          });
          this.refresh()
          this.query()
        } else {
          this.$notify.error({
            title: '错误',
            message: res.message
          });
        }
      }).catch(() => {
        this.$notify.error({
          title: '错误',
          message: '提问失败，请稍后重试'
        });
      })
    },
    refresh() {
      this.content = ""
    },
    handleCurrentChange(val) {
      this.pageNumber = val
      this.query()
      window.scrollTo({ top: 400, behavior: 'smooth' })
    },
    query() {
      var param = {
        taskId: this.taskId,
        userId: this.user.id,
        pageSize: this.pageSize,
        pageNumber: this.pageNumber,
      }
      getApeQuestionPage(param).then(res => {
        if(res.code == 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
        } else {
          this.$notify.error({
            title: '错误',
            message: res.message
          });
        }
      }).catch(() => {
        this.$notify.error({
          title: '错误',
          message: '获取答疑列表失败，请稍后重试'
        });
      })
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.taskId = this.$route.query.id
    try { this.user = JSON.parse(window.localStorage.getItem("user_info")) } catch(e) { this.user = {} }
    this.query()
  }
}
</script>

<style scoped>
@import url("../../assets/css/question/question.css");
</style>
