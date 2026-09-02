<template>
  <div class="articleInfo">
    <headerPage></headerPage>
    <div class="articleInfo-content">
      <!-- 笔记头部 -->
      <div class="articleInfo-title">
        <div>
          <div class="articleInfo-title1">{{article.title}}</div>
          <div class="articleInfo-title2">{{article.createBy}} · {{article.createTime}}</div>
        </div>
        <div style="display: flex; gap: 12px; align-items: center;">
          <el-button
            v-if="article.favor == 1"
            type="warning"
            @click="removeFavor"
            icon="el-icon-star-off"
            circle
            :loading="favorLoading"
            :disabled="favorLoading"
          ></el-button>
          <el-button
            v-if="article.favor == 0"
            type="success"
            @click="addFavor"
            icon="el-icon-star-on"
            circle
            :loading="favorLoading"
            :disabled="favorLoading"
          ></el-button>
          <span style="font-size: 14px; color: #64748b; white-space: nowrap;">
            <i class="el-icon-star-off"></i> {{ favorCount }} 收藏
          </span>
          <el-button
            v-if="article.createBy == user.userName"
            @click="editArticle(article.id)"
            type="primary"
            icon="el-icon-edit"
            circle
          ></el-button>
        </div>
      </div>
      
      <!-- 笔记内容卡片 -->
      <div class="articleInfo-desc">
        <div class="articleInfo-desc1">
          <span>摘要</span>
          {{article.articleDesc}}
        </div>
        <div class="articleInfo-desc2">
          <span>正文</span>
          <div v-html="sanitizedContent"></div>
        </div>
      </div>
    </div>
    
    <!-- 发表评论区域 -->
    <div class="articleInfo-bottom">
      <div class="articleInfo-comment">
        <div class="articleInfo-comment-font">发表评论</div>
        <div class="articleInfo-comment-content">
          <el-input 
            type="textarea" 
            resize="none" 
            rows="5" 
            v-model="content" 
            placeholder="分享你的想法或见解..."
          ></el-input>
          <div class="articleInfo-comment-btn">
            <el-button @click="saveApeArticleComment" size="mini" type="success" plain>提交评论</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评论列表区域 -->
    <div class="articleInfo-bottom">
      <div class="articleInfo-comment">
        <div class="articleInfo-comment-font">学员评论</div>
        <div v-if="commentList.length <= 0" class="articleInfo-comment-empty">
          <el-empty description="还没有评论，快来发表第一条评论吧！"></el-empty>
        </div>
        <div v-if="commentList.length > 0" class="articleInfo-comment-list">
          <div v-for="(item,index) in commentList" :key="index">
            <!-- 奇数索引 - 左侧布局 -->
            <div v-if="(index+1)%2 == 1" class="articleInfo-comment-item">
              <div>
                <img
                  :src="resolveAvatar(item.avatar)"
                  :alt="item.createBy"
                  @error="$avatarError($event, '/img/avatars/zhixue-default.png')">
              </div>
              <div style="flex:1">
                <div class="articleInfo-comment-item1">
                  {{item.createBy}}
                  <span style="font-size: 12px; color: #94a3b8; font-weight: normal;">
                    · {{item.createTime}}
                  </span>
                </div>
                <div class="articleInfo-comment-item2">{{item.content}}</div>
              </div>
            </div>
            <!-- 偶数索引 - 右侧布局 -->
            <div v-if="(index+1)%2 == 0" class="articleInfo-comment-item-right">
              <div style="flex:1; text-align: right;">
                <div class="articleInfo-comment-item1">
                  {{item.createBy}}
                  <span style="font-size: 12px; color: #94a3b8; font-weight: normal;">
                    · {{item.createTime}}
                  </span>
                </div>
                <div class="articleInfo-comment-item3">{{item.content}}</div>
              </div>
              <div>
                <img
                  :src="resolveAvatar(item.avatar)"
                  :alt="item.createBy"
                  @error="$avatarError($event, '/img/avatars/zhixue-default.png')">
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import DOMPurify from 'dompurify'
import {
  saveApeArticleFavor,
  removeApeArticleFavor,
  getApeArticleById,
  saveApeArticleComment,
  getApeArticleCommentByArticleId
} from '../../api/api'
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'ArticleInfo',
  data() {
    return {
      user: {},
      articleId: "",
      article: {},
      content: "",
      commentList: [],
      favorCount: 0,
      favorLoading: false
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  computed: {
    sanitizedContent() {
      return this.article.content ? DOMPurify.sanitize(this.article.content) : ''
    }
  },
  methods: {
    removeFavor() {
      this.favorLoading = true
      var param = {
        articleId: this.articleId,
        userId: this.user.id
      }
      removeApeArticleFavor(param).then(res => {
        if(res.code == 1000) {
          this.$message({ type: 'success', message: '取消收藏成功!' });
          // 以后端返回的最新数据为准
          if (res.data) {
            this.favorCount = res.data.favorCount || 0
            this.article.favor = 0
          }
          // 同步刷新笔记详情
          this.getApeArticleById()
        }
      }).finally(() => {
        this.favorLoading = false
      })
    },
    addFavor() {
      this.favorLoading = true
      var param = {
        articleId: this.articleId,
        userId: this.user.id
      }
      saveApeArticleFavor(param).then(res => {
        if(res.code == 1000) {
          this.$message({ type: 'success', message: '收藏成功!' });
          // 以后端返回的最新数据为准
          if (res.data) {
            this.favorCount = res.data.favorCount || 0
            this.article.favor = 1
          }
          // 同步刷新笔记详情
          this.getApeArticleById()
        }
      }).finally(() => {
        this.favorLoading = false
      })
    },
    editArticle() {
      this.$router.push("/editArticle?id=" + this.articleId)
    },
    saveApeArticleComment() {
      if (!this.content.trim()) {
        this.$message({ type: 'warning', message: '请输入评论内容' });
        return
      }
      var param = {
        taskId: this.articleId,
        content: this.content
      }
      saveApeArticleComment(param).then(res => {
        if (res.code == 1000) {
          this.$message({ type: 'success', message: '评论成功!' });
          this.content = ""
          this.getApeArticleCommentByArticleId()
        }
      })
    },
    getApeArticleCommentByArticleId() {
      getApeArticleCommentByArticleId({ id: this.articleId }).then(res => {
        if (res.code == 1000) {
          this.commentList = res.data
        }
      })
    },
    getApeArticleById() {
      getApeArticleById({ id: this.articleId }).then(res => {
        if(res.code == 1000) {
          this.article = res.data
          // 同步收藏数
          if (res.data.favorCount !== undefined) {
            this.favorCount = res.data.favorCount
          }
        }
      })
    },
    resolveAvatar(avatar) {
      return this.$resolveAvatar(avatar, "/img/avatars/zhixue-default.png")
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    var articleId = this.$route.query.id
    if (articleId) {
      this.articleId = articleId
    }
    this.getApeArticleById()
    this.getApeArticleCommentByArticleId()
    try { this.user = JSON.parse(window.localStorage.getItem("user_info")) } catch(e) { this.user = {} }
  }
}
</script>

<style scoped>
@import url("../../assets/css/article/articleInfo.css");

/* 富文本内容样式覆盖 */
::v-deep img {
  max-width: 100% !important;
  border-radius: 16px;
}

::v-deep p {
  margin-bottom: 16px;
  line-height: 1.8;
}

::v-deep h1, ::v-deep h2, ::v-deep h3 {
  margin: 24px 0 16px 0;
  color: #0f172a;
}

::v-deep code {
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 8px;
  font-family: monospace;
  font-size: 13px;
}

::v-deep pre {
  background: #1e293b;
  color: #e2e8f0;
  padding: 16px;
  border-radius: 16px;
  overflow-x: auto;
  margin: 16px 0;
}

::v-deep blockquote {
  border-left: 4px solid #20B486;
  padding-left: 20px;
  margin: 16px 0;
  color: #475569;
  font-style: italic;
}
</style>
