<template>
  <div class="article">
    <headerPage></headerPage>
    <div class="article-page-content">
      <!-- 搜索区域 -->
      <div class="article-page-search">
        <div>
          <div>笔记名称：</div>
          <div>
            <el-input 
              size="mini" 
              v-model="title" 
              placeholder="请输入笔记名称"
              clearable
            ></el-input>
          </div>
        </div>
        <div>
          <div>课程名称：</div>
          <div>
            <el-input 
              size="mini" 
              v-model="taskName" 
              placeholder="请输入课程名称"
              clearable
            ></el-input>
          </div>
        </div>
        <div class="article-page-btns">
          <el-button icon="el-icon-search" size="mini" type="primary" plain @click="searchPage()">搜索笔记</el-button>
          <el-button icon="el-icon-refresh" size="mini" type="warning" plain @click="refresh()">重置筛选</el-button>
        </div>
      </div>
      
      <!-- 页面标题 -->
      <div class="article-page-title">
        <i class="title-icon el-icon-notebook-2"></i>
        精选笔记
        <span style="font-size: 14px; color: #94a3b8; margin-left: 12px; font-weight: normal;">
          共 {{ total }} 篇笔记
        </span>
      </div>
      
      <!-- 笔记列表网格 -->
      <div class="article-page-list">
        <div 
          @click="toArticleInfo(item.id)" 
          v-for="(item,index) in tableData" 
          :key="index" 
          class="article-page-item"
        >
          <div class="article-page-image">
            <img src="../../assets/image/index/Vector (1).png" alt="笔记图标">
          </div>
          <div class="article-page-user">
            <img
              class="article-page-avatar"
              :src="resolveAvatar(item.avatar)"
              :alt="item.createBy"
              @error="$avatarError($event, '/img/avatars/zhixue-default.png')">
            <div class="article-page-username">
              <div>{{item.createBy}} · {{item.createTime}}</div>
              <div>{{item.title}}</div>
            </div>
          </div>
          <div class="article-page-font">
            {{item.articleDesc}}
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="article-page-fenye" v-if="total > 10">
        <el-pagination
          background
          :page-size="pageSize"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
      
      <!-- 底部装饰 -->
      <div class="article-page-bottom">
        <img src="../../assets/image/10.jpg" alt="" @error="$event.target.style.display='none'">
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { getApeArticlePage } from '../../api/api' 
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'Article',
  data() {
    return {
      title: "",
      taskName: "",
      pageSize: 10,
      pageNumber: 1,
      tableData: [],
      total: 0,
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    searchPage() {
      this.pageNumber = 1
      this.query()
    },
    query() {
      var param = {
        state: 0,
        title: this.title,
        taskName: this.taskName,
        pageSize: this.pageSize,
        pageNumber: this.pageNumber,
        type: 0,
      }
      getApeArticlePage(param).then(res => {
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
          message: '获取笔记列表失败，请稍后重试'
        });
      })
    },
    refresh() {
      this.title = ""
      this.taskName = ""
      this.pageNumber = 1
      this.query()
    },
    handleCurrentChange(val) {
      this.pageNumber = val
      this.query()
      window.scrollTo({ top: 400, behavior: 'smooth' })
    },
    resolveAvatar(avatar) {
      return this.$resolveAvatar(avatar, "/img/avatars/zhixue-default.png")
    },
    toArticleInfo(id) {
      this.$router.push("/articleInfo?id=" + id)
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    var name = this.$route.query.name
    if (name) {
      this.taskName = name
    }
    this.query()
  }
}
</script>

<style scoped>
@import url("../../assets/css/article/article.css");
</style>
