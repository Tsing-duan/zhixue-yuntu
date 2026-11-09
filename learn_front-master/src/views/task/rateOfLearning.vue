<template>
  <div class="rateOfLearning">
    <headerPage></headerPage>
    <div class="learning-content">
      <div class="learning-top">
        <!-- 课程标题 -->
        <div class="learning-taskName">
          <i class="el-icon-reading"></i>
          {{task.name}}
        </div>
        
        <!-- 学习统计卡片 -->
        <div class="learning-stats">
          <div class="stats-card">
            <div class="stats-icon video-icon">
              <i class="el-icon-video-camera"></i>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{wancheng}}/{{video}}</div>
              <div class="stats-label">{{ $t("视频进度") }}</div>
            </div>
          </div>
          <div class="stats-card">
            <div class="stats-icon homework-icon">
              <i class="el-icon-news"></i>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{wancheng1}}/{{home}}</div>
              <div class="stats-label">{{ $t("作业进度") }}</div>
            </div>
          </div>
          <div class="stats-card">
            <div class="stats-icon score-icon">
              <i class="el-icon-medal"></i>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{getScore()}}{{ $t("分") }}</div>
              <div class="stats-label">{{ $t("综合得分") }}</div>
            </div>
          </div>
          <div class="stats-card">
            <div class="stats-icon grade-icon" :class="getGradeClass()">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="stats-info">
              <div class="stats-number grade-text" :class="getGradeClass()">{{getAgree()}}</div>
              <div class="stats-label">{{ $t("学习评级") }}</div>
            </div>
          </div>
        </div>
        
        <!-- 章节进度列表 -->
        <div class="learning-list">
          <div class="learning-list-header">
            <span>{{ $t("章节名称") }}</span>
            <span>{{ $t("视频状态") }}</span>
            <span>{{ $t("作业状态") }}</span>
          </div>
          <div class="learning-list-body">
            <div v-for="(item,index) in chapter" :key="index" class="learning-item">
              <div class="chapter-name">
                <span class="chapter-number">{{index + 1}}</span>
                {{item.name}}
              </div>
              <div class="chapter-status">
                <span class="status-badge" :class="item.videoFlag == '已完成' ? 'status-completed' : 'status-pending'">
                  <i :class="item.videoFlag == '已完成' ? 'el-icon-check' : 'el-icon-time'"></i>
                  {{item.videoFlag}}
                </span>
              </div>
              <div class="chapter-status">
                <span class="status-badge" :class="item.home == '已完成' ? 'status-completed' : 'status-pending'">
                  <i :class="item.home == '已完成' ? 'el-icon-check' : 'el-icon-time'"></i>
                  {{item.home == '已完成' ? $t("已完成") : (item.homework == 1 ? $t("未完成") : $t("无作业"))}}
                </span>
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
import { t } from '@/i18n'

import { getApeTaskById, getTaskChapterStudy } from "../../api/api"
import headerPage from "../../components/header/header.vue"
import bottomPage from "../../components/bottom/bottom.vue"

export default {
  name: 'RateOfLearning',
  data() {
    return {
      taskId: "",
      task: {},
      chapter: [],
      video: 0,
      wancheng: 0,
      home: 0,
      wancheng1: 0
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    getGradeClass() {
      var score = this.getScore()
      if (score >= 90) return 'grade-excellent'
      if (score >= 75) return 'grade-good'
      if (score >= 60) return 'grade-pass'
      return 'grade-fail'
    },
    getAgree() {
      var total = this.video + this.home
      var wan = this.wancheng1 + this.wancheng
      if (total === 0) return t("未开始")
      var score = (wan / total * this.task.proportion).toFixed(2)
      if (score < 60) return t("不及格")
      if (score >= 60 && score < 75) return t("及格")
      if (score >= 75 && score < 90) return t("良好")
      if (score >= 90) return t("优秀")
      return t("未开始")
    },
    getScore() {
      var total = this.video + this.home
      var wan = this.wancheng1 + this.wancheng
      if (total === 0) return '0'
      return (wan / total * this.task.proportion).toFixed(2)
    },
    getApeTaskById() {
      getApeTaskById({ id: this.taskId }).then(res => {
        if (res.code == 1000) {
          this.task = res.data
        }
      }).catch(() => {
        this.$message.error(t("获取课程信息失败"))
      })
    },
    getTaskChapterStudy() {
      getTaskChapterStudy({ id: this.taskId }).then(res => {
        if (res.code == 1000) {
          this.chapter = res.data
          this.video = this.chapter.length
          this.home = 0
          this.wancheng = 0
          this.wancheng1 = 0
          for (let i = 0; i < this.chapter.length; i++) {
            var item = this.chapter[i]
            if (item.homework == 1) {
              this.home = this.home + 1
            }
            if (item.home == '已完成') {
              this.wancheng1 = this.wancheng1 + 1
            }
            if (item.videoFlag == '已完成') {
              this.wancheng = this.wancheng + 1
            }
          }
        }
      }).catch(() => {
        this.$message.error(t("获取学习进度失败"))
      })
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    var taskId = this.$route.query.id
    if (taskId) {
      this.taskId = taskId
    }
    this.getApeTaskById()
    this.getTaskChapterStudy()
  }
}
</script>

<style scoped>
.rateOfLearning {
    width: 100%;
    min-height: 100vh;
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

/* ========== 主内容区域 ========== */
.learning-content {
    width: 100%;
    min-height: 550px;
    display: flex;
    justify-content: center;
    font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Segoe UI', 'PingFang SC', sans-serif;
    padding: 0 24px;
    position: relative;
}

/* 背景装饰 */
.learning-content::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 60%;
    height: 500px;
    background: radial-gradient(circle, rgba(32, 180, 134, 0.03) 0%, transparent 70%);
    pointer-events: none;
}

.learning-top {
    width: 100%;
    max-width: 1280px;
    position: relative;
    z-index: 1;
}

/* ========== 课程标题 ========== */
.learning-taskName {
    margin-top: 32px;
    font-size: 28px;
    font-weight: 700;
    color: #0f172a;
    letter-spacing: -0.5px;
    display: flex;
    align-items: center;
    gap: 12px;
    padding-left: 20px;
    position: relative;
}

.learning-taskName::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 24px;
    background: linear-gradient(135deg, #20B486, #58C7A4);
    border-radius: 4px;
}

.learning-taskName i {
    font-size: 28px;
    color: #20B486;
}

/* ========== 统计卡片区域 ========== */
.learning-stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-top: 32px;
    margin-bottom: 32px;
}

.stats-card {
    background: #ffffff;
    border-radius: 20px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    transition: all 0.3s cubic-bezier(0.2, 0.9, 0.4, 1.1);
    border: 1px solid rgba(226, 232, 240, 0.6);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.stats-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(32, 180, 134, 0.1);
    border-color: rgba(32, 180, 134, 0.3);
}

.stats-icon {
    width: 52px;
    height: 52px;
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
}

.video-icon {
    background: rgba(32, 180, 134, 0.1);
    color: #20B486;
}

.homework-icon {
    background: rgba(245, 158, 11, 0.1);
    color: #f59e0b;
}

.score-icon {
    background: rgba(59, 130, 246, 0.1);
    color: #3b82f6;
}

.grade-icon {
    background: rgba(139, 92, 246, 0.1);
    color: #8b5cf6;
}

.stats-info {
    flex: 1;
}

.stats-number {
    font-size: 24px;
    font-weight: 800;
    color: #1e293b;
    line-height: 1.2;
}

.stats-label {
    font-size: 13px;
    color: #64748b;
    margin-top: 4px;
}

/* 评级文字样式 */
.grade-text {
    font-size: 20px;
}

.grade-text.grade-excellent {
    color: #f59e0b;
}

.grade-text.grade-good {
    color: #20B486;
}

.grade-text.grade-pass {
    color: #3b82f6;
}

.grade-text.grade-fail {
    color: #ef4444;
}

/* ========== 章节进度列表 ========== */
.learning-list {
    background: #ffffff;
    border-radius: 24px;
    border: 1px solid rgba(226, 232, 240, 0.6);
    box-shadow: 0 20px 40px -12px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    margin-top: 20px;
    margin-bottom: 40px;
}

.learning-list-header {
    display: grid;
    grid-template-columns: 1fr 120px 120px;
    background: #f8fafc;
    padding: 16px 24px;
    border-bottom: 1px solid #eef2f8;
    font-weight: 600;
    color: #475569;
    font-size: 14px;
}

.learning-list-body {
    max-height: 500px;
    overflow-y: auto;
}

.learning-list-body::-webkit-scrollbar {
    width: 6px;
}

.learning-list-body::-webkit-scrollbar-track {
    background: #e2e8f0;
    border-radius: 10px;
}

.learning-list-body::-webkit-scrollbar-thumb {
    background: #20B486;
    border-radius: 10px;
}

.learning-item {
    display: grid;
    grid-template-columns: 1fr 120px 120px;
    align-items: center;
    padding: 16px 24px;
    border-bottom: 1px solid #eef2f8;
    transition: all 0.3s ease;
}

.learning-item:hover {
    background: #f8fafc;
}

.chapter-name {
    display: flex;
    align-items: center;
    gap: 12px;
    font-weight: 500;
    color: #1e293b;
}

.chapter-number {
    width: 28px;
    height: 28px;
    background: #e8f5ed;
    color: #20B486;
    border-radius: 10px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 600;
}

.chapter-status {
    display: flex;
    justify-content: flex-start;
}

.status-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 5px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
}

.status-completed {
    background: #e8f5ed;
    color: #20B486;
}

.status-pending {
    background: #fef3c7;
    color: #d97706;
}

.status-badge i {
    font-size: 12px;
}

/* ========== 响应式设计 ========== */
@media (max-width: 1024px) {
    .learning-stats {
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;
    }
    
    .learning-taskName {
        font-size: 24px;
    }
}

@media (max-width: 768px) {
    .learning-content {
        padding: 0 16px;
    }
    
    .learning-taskName {
        font-size: 20px;
        margin-top: 24px;
    }
    
    .learning-stats {
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;
    }
    
    .stats-card {
        padding: 16px;
    }
    
    .stats-icon {
        width: 44px;
        height: 44px;
        font-size: 20px;
    }
    
    .stats-number {
        font-size: 20px;
    }
    
    .learning-list-header {
        display: none;
    }
    
    .learning-item {
        grid-template-columns: 1fr;
        gap: 12px;
        padding: 16px;
    }
    
    .chapter-name {
        font-weight: 600;
    }
    
    .chapter-status {
        padding-left: 40px;
    }
}

@media (max-width: 480px) {
    .learning-stats {
        grid-template-columns: 1fr;
    }
    
    .learning-taskName {
        font-size: 18px;
    }
    
    .stats-number {
        font-size: 18px;
    }
    
    .grade-text {
        font-size: 16px;
    }
}
</style>