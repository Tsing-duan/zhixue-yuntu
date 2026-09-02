<template>
  <div class="message">
    <headerPage></headerPage>
    <div class="message-page-content">
      <!-- 留言输入卡片 -->
      <div class="message-page-add">
        <div>
          <div>发布留言</div>
          <el-input 
            type="textarea" 
            resize="none" 
            :rows="8" 
            v-model="content" 
            placeholder="写下你想说的话，我们会尽快回复..."
            maxlength="500"
            show-word-limit
          ></el-input>
          <div class="message-page-btns">
            <el-button size="mini" icon="el-icon-message" type="primary" plain @click="add">发布留言</el-button>
            <el-button size="mini" icon="el-icon-refresh" type="warning" plain @click="refresh">清空内容</el-button>
          </div>
        </div>
      </div>
      
      <!-- 页面标题 -->
      <div class="message-page-title">
        <i class="title-icon el-icon-chat-dot-round"></i>
        我的留言
        <span style="font-size: 14px; color: #94a3b8; margin-left: 12px; font-weight: normal;">
          共 {{ total }} 条留言
        </span>
      </div>
      
      <!-- 留言列表 -->
      <div class="message-page-list">
        <div v-for="(item,index) in tableData" :key="index" class="message-page-item">
          <div class="message-page-top">
            <div>
              <i class="el-icon-chat-dot-round" style="color: #20B486; margin-right: 12px;"></i>
              {{item.content}}
            </div>
            <div>
              <i class="el-icon-success"></i>
            </div>
          </div>
          <div class="messsage-page-huifu">
            <span>回复内容</span>
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
import { getApeMessagePage, saveApeMessage } from '../../api/api' 
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'Message',
  data() {
    return {
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
          message: '请输入留言内容',
          type: 'warning'
        });
        return
      }
      var param = {
        content: this.content
      }
      saveApeMessage(param).then(res => {
        if(res.code == 1000) {
          this.$message({
            message: '留言成功，请等待回复',
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
          message: '留言失败，请稍后重试'
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
        flag: 0,
        pageSize: this.pageSize,
        pageNumber: this.pageNumber,
      }
      getApeMessagePage(param).then(res => {
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
          message: '获取留言列表失败，请稍后重试'
        });
      })
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.query()
  }
}
</script>

<style scoped>
@import url("../../assets/css/message/message.css");
</style>
