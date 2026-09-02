<template>
  <div class="articleInfo">
    <headerPage></headerPage>
    <div class="articleInfo-content">
      <!-- 讨论主题卡片 -->
      <div class="articleInfo-title">
        <div>
          <div class="articleInfo-title1">{{forum.name}}</div>
          <div class="articleInfo-title2">
            <i class="el-icon-user"></i> {{forum.createBy}} · {{forum.createTime}}
          </div>
          <div class="articleInfo-title2">
            <i class="el-icon-chat-line-round"></i> {{forum.content}}
          </div>
        </div>
      </div>
      
      <!-- 发布讨论内容 -->
      <div class="articleInfo-bottom">
        <div class="articleInfo-comment">
          <div class="articleInfo-comment-font">发表回复</div>
          <div class="articleInfo-comment-content">
            <el-input 
              type="textarea" 
              resize="none" 
              rows="5" 
              v-model="content" 
              placeholder="分享你的观点..."
            ></el-input>
            <div class="articleInfo-comment-btn">
              <el-button @click="saveApeForumItem" size="mini" type="success" plain>发布回复</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 回复列表 -->
      <div class="articleInfo-bottom">
        <div class="articleInfo-comment">
          <div class="articleInfo-comment-font">
            回复列表
            <span style="font-size: 14px; color: #94a3b8; margin-left: 8px; font-weight: normal;">
              共 {{ forumList.length }} 条回复
            </span>
          </div>
          <div v-if="forumList.length <= 0" class="articleInfo-comment-empty">
            <el-empty description="还没有回复，快来发表第一条回复吧！"></el-empty>
          </div>
          <div v-if="forumList.length > 0" class="articleInfo-comment-list">
            <div v-for="(item,index) in forumList" :key="index">
              <!-- 奇数索引 - 左侧布局 -->
              <div v-if="(index+1)%2 == 1" class="articleInfo-comment-item">
                <div>
                  <img
                    :src="$resolveAvatar(item.userAvatar, '/img/avatars/zhixue-default.png')"
                    :alt="item.userName"
                    @error="$avatarError($event, '/img/avatars/zhixue-default.png')">
                </div>
                <div style="flex:1">
                  <div class="articleInfo-comment-item1">
                    {{item.userName}}
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
                    {{item.userName}}
                    <span style="font-size: 12px; color: #94a3b8; font-weight: normal;">
                      · {{item.createTime}}
                    </span>
                  </div>
                  <div class="articleInfo-comment-item3">{{item.content}}</div>
                </div>
                <div>
                  <img
                    :src="$resolveAvatar(item.userAvatar, '/img/avatars/zhixue-default.png')"
                    :alt="item.userName"
                    @error="$avatarError($event, '/img/avatars/zhixue-default.png')">
                </div>
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
import { getApeForumById, getApeForumItemList, saveApeForumItem } from '../../api/api' 
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'ForumInfo',
  data() {
    return {
      id: "",
      user: {},
      forum: {},
      content: "",
      forumList: []
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    saveApeForumItem() {
      if (!this.content.trim()) {
        this.$message({
          type: 'warning',
          message: '请输入讨论内容'
        });
        return
      }
      var param = {
        forumId: this.id,
        content: this.content
      }
      saveApeForumItem(param).then(res => {
        if (res.code == 1000) {
          this.$message({
            type: 'success',
            message: '提交成功，请等待管理员审核'
          });
          this.content = ""
          this.getApeForumItemList()
        }
      }).catch(() => {
        this.$message.error('发布失败，请稍后重试')
      })
    },
    getApeForumById() {
      getApeForumById({ id: this.id }).then(res => {
        if (res.code == 1000) {
          this.forum = res.data
        }
      }).catch(() => {
        this.$message.error('获取讨论信息失败')
      })
    },
    getApeForumItemList() {
      var param = {
        forumId: this.id,
        state: 0
      }
      getApeForumItemList(param).then(res => {
        if (res.code == 1000) {
          this.forumList = res.data
        }
      }).catch(() => {
        this.$message.error('获取回复列表失败')
      })
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.id = this.$route.query.id
    this.getApeForumById()
    this.getApeForumItemList()
    this.user = JSON.parse(window.localStorage.getItem("user_info"))
  }
}
</script>

<style scoped>
@import url("../../assets/css/article/articleInfo.css");

/* 讨论详情额外样式 */
.articleInfo-title {
  margin-top: 32px;
}

.articleInfo-title2 i {
  margin-right: 8px;
  color: #20B486;
}
</style>
