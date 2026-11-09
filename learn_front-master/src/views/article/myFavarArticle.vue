<template>
  <div class="article">
    <headerPage></headerPage>
    <div class="article-page-content">
      <!-- 页面标题 -->
      <div class="article-page-title">
        <i class="title-icon el-icon-star-off"></i>{{ $t("收藏笔记") }}<span style="font-size: 14px; color: #94a3b8; margin-left: 12px; font-weight: normal;">{{ $t("共") }}{{ total }}{{ $t("篇收藏") }}</span>
      </div>
      
      <!-- 笔记列表网格 -->
      <div class="article-page-list">
        <div 
          @click="toArticleInfo(item.articleId)" 
          v-for="(item,index) in tableData" 
          :key="index" 
          class="article-page-item"
        >
          <div class="article-page-image">
            <img src="../../assets/image/index/Vector (1).png" :alt="$t('笔记图标')">
          </div>
          <div class="article-page-user">
            <img
              class="article-page-avatar"
              :src="$resolveAvatar(item.avatar, '/img/avatars/zhixue-default.png')"
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
          v-model:page-size="pageSize"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
      
      <!-- 底部装饰 -->
      <div class="article-page-bottom">
        <img src="../../assets/image/38.jpg" alt="" @error="$event.target.style.display='none'">
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { getApeArticleFavorPage } from '../../api/api' 
import headerPage from "../../components/header/header.vue"
import bottomPage from "../../components/bottom/bottom.vue"

export default {
  name: 'MyFavarArticle',
  data() {
    return {
      user: {},
      title: "",
      pageSize: 10,
      pageNumber: 1,
      tableData: [],
      total: 0
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    query() {
      var param = {
        userId: this.user.id,
        pageSize: this.pageSize,
        pageNumber: this.pageNumber
      }
      getApeArticleFavorPage(param).then(res => {
        if(res.code == 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message
          });
        }
      }).catch(() => {
        this.$notify.error({
          get title() { return t("错误") },
          message: '获取收藏笔记列表失败，请稍后重试'
        });
      })
    },
    handleCurrentChange(val) {
      this.pageNumber = val
      this.query()
      window.scrollTo({ top: 400, behavior: 'smooth' })
    },
    toArticleInfo(id) {
      this.$router.push("/articleInfo?id=" + id)
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.user = this.$demo(JSON.parse(window.localStorage.getItem("user_info")))
    this.query()
  }
}
</script>

<style scoped>
@import url("../../assets/css/article/article.css");

/* 收藏笔记页面特有的样式覆盖 */
.article-page-title::before {
    background: linear-gradient(135deg, #f59e0b, #fbbf24);
}

.article-page-title::after {
    background: linear-gradient(90deg, #f59e0b, transparent);
}

.article-page-item::before {
    background: linear-gradient(90deg, #f59e0b, #fbbf24, #f59e0b);
}

.article-page-avatar {
    border-color: #f59e0b;
}

.article-page-fenye .el-pagination .el-pager li.active {
    background: linear-gradient(135deg, #f59e0b, #fbbf24);
}

.article-page-fenye .el-pagination .el-pager li:hover:not(.active) {
    background: rgba(245, 158, 11, 0.1);
    color: #f59e0b;
    border-color: #f59e0b;
}

.article-page-fenye .el-pagination button:hover {
    background: rgba(245, 158, 11, 0.1);
    color: #f59e0b;
    border-color: #f59e0b;
}
</style>
