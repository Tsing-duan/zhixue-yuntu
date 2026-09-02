<template>
  <div class="index">
    <!-- 滚动进度条 -->
    <div class="scroll-progress" :style="{ width: scrollProgress + '%' }"></div>
    
    <loginPage v-if="!isLogin"></loginPage>
    <template v-else>
      <headerPage></headerPage>
      
      <!-- 学习工作台 -->
      <div class="index-search">
        <div class="index-search-content">
          <div class="student-hero">
            <div class="student-hero-label">学习工作台</div>
            <h1>{{ greetingText }}</h1>
            <p>从课程、考试、笔记和错题入口开始，继续推进今天的学习任务。</p>
            <div class="student-hero-stats">
              <div>
                <strong>{{data.count || 0}}</strong>
                <span>课程资源</span>
              </div>
              <div>
                <strong>{{data.count1 || 0}}</strong>
                <span>授课教师</span>
              </div>
              <div>
                <strong>{{article.length || 0}}</strong>
                <span>推荐笔记</span>
              </div>
            </div>
          </div>
          <el-input 
            class="searchKey" 
            v-model="searchKey" 
            suffix-icon="el-icon-search" 
            placeholder="请输入想要搜索的课程名称并按回车键搜索" 
            @change="searchTask"
            @keyup.enter.native="searchTask"
          ></el-input>
        </div>
        <div class="index-image">
          <img class="index-search-image" src="../../assets/image/index/index-image.png" alt="banner">
        </div>
      </div>

      <div class="student-workbench">
        <div class="most-content">
          <div class="workbench-grid">
            <button
              v-for="item in quickActions"
              :key="item.title"
              class="workbench-card"
              @click="$router.push(item.path)"
            >
              <i :class="item.icon"></i>
              <span>{{ item.title }}</span>
              <small>{{ item.desc }}</small>
            </button>
          </div>
        </div>
      </div>
      
      <!-- AI 助手入口 -->
      <div class="ai-entry-section" v-if="isLogin">
        <div class="most-content">
          <div class="page-title">
            <div>{{ isTeacher ? 'AI 教学助手' : 'AI 学习助手' }}</div>
            <div>Smart <span>Assistant</span></div>
          </div>
          <div class="ai-entry-card" @click="$router.push('/ai')">
            <div class="ai-entry-left">
              <div class="ai-entry-icon">
                <i class="el-icon-cpu"></i>
              </div>
              <div class="ai-entry-info">
                <h3>智学云途 · 小智</h3>
                <p>{{ isTeacher ? '教案辅助、课程整理、学生问题总结、教学活动设计，让教学更高效' : '课程答疑、学习建议、作业思路提示、学习计划生成，让学习更轻松' }}</p>
              </div>
            </div>
            <div class="ai-entry-right">
              <el-button type="success" icon="el-icon-right" circle></el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 最受欢迎专业 -->
      <div class="most-major">
        <div class="most-content"> 
          <div class="page-title">
            <div>最受欢迎的专业</div>
            <div>Most <span>Popular Major</span></div>
          </div>
          <div class="major-list">
            <div class="major-item" v-for="(item,index) in major" :key="index">
              <div class="major-name">
                <i :class="getMajorIcon(index)" style="font-size:21px"></i>
                <div>{{item.name}}</div>
              </div>
              <el-button @click="majorTask(item.name)" plain size="mini" type="success" icon="el-icon-top-right"></el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 平台数据 -->
      <div class="shuju">
        <div class="most-content">
          <div class="page-title">
            <div>平台数据</div>
            <div>Our <span>Achievement</span></div>
          </div>
          <div class="shuju-total">
            <div class="shuju-items-container">
              <div class="shuju-item">
                <img src="../../assets/image/index/Frame 67 (7).png" alt="teacher">
                <div class="shuju-name">
                  <div>{{data.count1}}+</div>
                  <div>教师 Teacher</div>
                </div>
              </div>
              <div class="shuju-item">
                <img src="../../assets/image/index/Frame 67 (4).png" alt="video">
                <div class="shuju-name">
                  <div>{{data.count}}+</div>
                  <div>教程 Video</div>
                </div>
              </div>
              <div class="shuju-item">
                <img src="../../assets/image/index/Frame 67 (6).png" alt="student">
                <div class="shuju-name">
                  <div>{{data.count2}}+</div>
                  <div>学生 Student</div>
                </div>
              </div>
              <div class="shuju-item">
                <img src="../../assets/image/index/Frame 67 (5).png" alt="users">
                <div class="shuju-name">
                  <div>{{data.total}}+</div>
                  <div>用户 Users</div>
                </div>
              </div>
            </div>
            <div class="shuju-image">
              <img src="../../assets/image/index/Group 10.png" alt="achievement">
            </div>
          </div>
        </div>
      </div>
      
      <!-- 推荐笔记 -->
      <div class="most-note">
        <div class="most-content">
          <div class="page-title">
            <div> 推荐笔记</div>
            <div>Recommended <span>Notes</span></div>
          </div>
          <div class="note-list">
            <div v-for="(item,index) in article" :key="index" class="note-item">
              <div class="note-image"><img src="../../assets/image/index/Vector (1).png" alt="note"></div>
              <div class="note-user">
                <img
                  class="note-avatar"
                  :src="$resolveAvatar(item.avatar, '/img/avatars/zhixue-default.png')"
                  alt="avatar"
                  @error="$avatarError($event, '/img/avatars/zhixue-default.png')">
                <div class="note-username">
                  <div>{{item.createBy}} · {{item.createTime}}</div>
                  <div>{{item.title}}</div>
                </div>
              </div>
              <div class="note-font">
                {{item.articleDesc}}
              </div>
            </div>
          </div>
          <div class="task-btns">
            <el-button @click="toArticle" size="mini" type="success" icon="el-icon-arrow-right">查看全部笔记</el-button>
          </div>
        </div>
      </div>
      
      <!-- 留言区域 -->
      <div class="liuyan">
        <div class="most-content">
          <div class="liuyan-font">
            <div class="liuyan-image">
              <img style="width:80%" src="../../assets/image/index/Group 12.png" alt="message">
            </div>
            <div class="liuyan-content">
              <div>💬 你的任何疑问都可以留言咨询</div>
              <div>You can leave a message for consultation if you have any questions</div>
              <el-button @click="toMessage" icon="el-icon-question" type="success">留言咨询</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <bottomPage></bottomPage>
    </template>
  </div>
</template>

<script>
import { getApeMajorList, getIndexAchievement, getIndexArticleList } from '../../api/api' 
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom" 
import loginPage from "../login/login"

export default {
  name: 'Index',
  data() {
    return {
      searchKey: "",
      major: [],
      data: {
        count: 0,
        count1: 0,
        count2: 0,
        total: 0
      },
      article: [],
      isLogin: false,
      isTeacher: false,
      userInfo: {},
      scrollProgress: 0
    }
  },
  components: {
    headerPage,
    bottomPage,
    loginPage
  },
  computed: {
    greetingText() {
      const name = this.userInfo && this.userInfo.userName ? this.userInfo.userName : '同学'
      return name + '，欢迎回来'
    },
    quickActions() {
      return [
        { title: '继续学习', desc: '查看课程与章节', icon: 'el-icon-reading', path: '/task' },
        { title: '参加考试', desc: '查看待完成考试', icon: 'el-icon-edit-outline', path: '/test' },
        { title: '错题复盘', desc: '整理薄弱知识点', icon: 'el-icon-document-checked', path: '/myWrongAssign' },
        { title: '学习笔记', desc: '记录和浏览笔记', icon: 'el-icon-notebook-2', path: '/article' }
      ]
    }
  },
  methods: {
    searchTask() {
      if (this.searchKey && this.searchKey.trim()) {
        this.$router.push("/task?name=" + this.searchKey.trim())
      }
    },
    majorTask(major) {
      if (major) {
        this.$router.push("/task?major=" + encodeURIComponent(major))
      }
    },
    toArticle() {
      this.$router.push("/article")
    },
    toMessage() {
      this.$router.push("/message")
    },
    getMajorIcon(index) {
      const icons = [
        "el-icon-medal-1", "el-icon-lollipop", "el-icon-milk-tea", "el-icon-moon-night",
        "el-icon-sunset", "el-icon-truck", "el-icon-bicycle", "el-icon-timer",
        "el-icon-mic", "el-icon-cpu", "el-icon-thumb", "el-icon-mouse",
        "el-icon-finished", "el-icon-help", "el-icon-odometer", "el-icon-sugar"
      ]
      return icons[index % icons.length]
    },
    handleScroll() {
      const winScroll = document.documentElement.scrollTop || document.body.scrollTop
      const height = document.documentElement.scrollHeight - document.documentElement.clientHeight
      const scrolled = (winScroll / height) * 100
      this.scrollProgress = scrolled
    },
    getApeMajorList() {
      getApeMajorList().then(res => {
        if(res.code == 1000) {
          // 如需过滤特定专业，在 ape_param 中配置 MAJOR_EXCLUDE 参数
          let list = res.data.slice(0, 16)
          this.major = list
        }
      })
    },
    getIndexAchievement() {
      getIndexAchievement().then(res => {
        if(res.code == 1000) {
          this.data = res.data
        }
      })
    },
    getIndexArticleList() {
      getIndexArticleList().then(res => {
        if(res.code == 1000) {
          this.article = res.data
        }
      })
    },
    checkLoginStatus() {
      this.isLogin = !!window.localStorage.getItem('user_token')
      if (this.isLogin) {
        const info = window.localStorage.getItem('user_info')
        if (info) {
          try {
            const user = JSON.parse(info)
            this.userInfo = user
            this.isTeacher = user.userType === 1
          } catch (e) {}
        }
      }
    }
  },
  created() {
    this.checkLoginStatus()
  },
  mounted() {
    this.getApeMajorList()
    this.getIndexAchievement()
    this.getIndexArticleList()
    window.addEventListener('scroll', this.handleScroll)
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.handleScroll)
  }
}
</script>

<style scoped>
@import url("../../assets/css/index/index.css");

.index {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fb 0%, #ffffff 100%);
}

/* AI 入口卡片 */
.ai-entry-section {
  margin: 40px 0;
}

.ai-entry-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, rgba(32, 180, 134, 0.06), rgba(56, 189, 248, 0.06));
  border: 1.5px solid rgba(32, 180, 134, 0.2);
  border-radius: 16px;
  padding: 28px 32px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.ai-entry-card:hover {
  border-color: #20B486;
  box-shadow: 0 8px 24px rgba(32, 180, 134, 0.12);
  transform: translateY(-2px);
}

.ai-entry-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.ai-entry-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, #20B486, #38bdf8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  flex-shrink: 0;
}

.ai-entry-info h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #1e293b;
}

.ai-entry-info p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  max-width: 500px;
}

.ai-entry-right .el-button {
  width: 44px;
  height: 44px;
  font-size: 18px;
}

@media (max-width: 768px) {
  .ai-entry-card {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  .ai-entry-left {
    flex-direction: column;
  }
  .ai-entry-info p {
    max-width: 100%;
  }
}
</style>
