<template>
  <div class="taskInfo">
    <headerPage></headerPage>
    <div class="taskInfo-content">
      <div class="course-hero">
        <div class="course-hero-copy">
          <div class="course-eyebrow">
            <span>{{ task.classification || '精品课程' }}</span>
            <span>{{ courseLevel }}</span>
            <span>{{ enrollStatusLabel }}</span>
          </div>
          <h1>{{ task.name || '课程详情' }}</h1>
          <p>{{ courseSummary }}</p>
          <div class="course-hero-actions">
            <el-button type="primary" icon="el-icon-video-play" @click="addTaskStudent">
              {{ enrollLabel }}
            </el-button>
            <el-button
              v-if="flag"
              type="warning"
              plain
              icon="el-icon-star-off"
              @click="removeFavor"
              :loading="favorLoading"
              :disabled="favorLoading"
            >
              已收藏
            </el-button>
            <el-button
              v-else
              type="success"
              plain
              icon="el-icon-star-on"
              @click="addFavor"
              :loading="favorLoading"
              :disabled="favorLoading"
            >
              收藏课程
            </el-button>
          </div>
        </div>

        <div class="course-hero-panel">
          <div class="course-stat">
            <span>{{ chapterCount }}</span>
            <small>课程章节</small>
          </div>
          <div class="course-stat">
            <span>{{ task.num || 0 }}</span>
            <small>学习人数</small>
          </div>
          <div class="course-stat">
            <span>{{ homeworkCount }}</span>
            <small>章节作业</small>
          </div>
          <div class="course-stat">
            <span>{{ favorCount }}</span>
            <small>收藏人数</small>
          </div>
        </div>
      </div>

      <div class="course-meta-strip">
        <div>
          <span>讲师</span>
          <strong>{{ task.teacherName || '暂未设置' }}</strong>
        </div>
        <div>
          <span>专业方向</span>
          <strong>{{ task.major || '通用课程' }}</strong>
        </div>
        <div>
          <span>课程分类</span>
          <strong>{{ task.classification || '未分类' }}</strong>
        </div>
        <div>
          <span>综合权重</span>
          <strong>{{ task.proportion || 100 }} 分</strong>
        </div>
      </div>

      <div class="course-body-grid">
        <div class="course-main">
          <div class="course-player">
            <video
              v-if="select && selectSrc"
              :key="videoKey"
              ref="videoPlayer"
              id="videoPlayer"
              :src="selectSrc"
              class="course-video"
              controls
              controlsList="nodownload"
              @ended="markChapterWatched"
            ></video>
            <img
              v-if="!select && task.image"
              :src="$store.state.HOST + task.image"
              :alt="task.name"
            >
            <div v-if="select && !selectSrc" class="player-mask player-empty-state">
              <i class="el-icon-folder-opened"></i>
              <span>本章节暂无视频，可继续查看作业或课件</span>
            </div>
            <div v-if="!select" class="player-mask">
              <i class="el-icon-video-play"></i>
              <span>选择右侧章节开始学习</span>
            </div>
          </div>

          <div class="course-action-bar">
            <button @click="toQuestion(task.id)">
              <i class="el-icon-question"></i>
              课程答疑
            </button>
            <button @click="toTeacher(task.teacherId)">
              <i class="el-icon-user"></i>
              讲师主页
            </button>
            <button @click="toArticle">
              <i class="el-icon-notebook-1"></i>
              课程笔记
            </button>
            <button @click="toAssign()">
              <i class="el-icon-news"></i>
              章节作业
            </button>
            <button @click="downloadfile">
              <i class="el-icon-download"></i>
              下载课件
            </button>
            <button @click="toLearning">
              <i class="el-icon-data-line"></i>
              学习进度
            </button>
          </div>

          <div class="course-section">
            <div class="section-title">
              <span>课程介绍</span>
              <small>Course Overview</small>
            </div>
            <p>{{ courseSummary }}</p>
          </div>

          <div class="course-section">
            <div class="section-title">
              <span>你将收获</span>
              <small>Learning Outcomes</small>
            </div>
            <div class="outcome-grid">
              <div v-for="item in learningOutcomes" :key="item.title" class="outcome-card">
                <i :class="item.icon"></i>
                <strong>{{ item.title }}</strong>
                <span>{{ item.desc }}</span>
              </div>
            </div>
          </div>
        </div>

        <aside class="course-aside">
          <div class="course-aside-head">
            <div>
              <span>课程目录</span>
              <small>{{ chapterCount }} 个章节</small>
            </div>
            <el-tag size="mini" type="success">{{ task.classification || '课程' }}</el-tag>
          </div>
          <div class="chapter-list">
            <button
              v-for="(item,index) in chapter"
              :key="item.id || index"
              @click="changeChapter(index)"
              class="chapter-item"
              :class="{ 'active-chapter': selectChapter && selectChapter.id === item.id }"
            >
              <span class="chapter-index">{{ index + 1 }}</span>
              <span class="chapter-content">
                <strong :title="item.name">{{ item.name }}</strong>
                <em>
                  <i class="el-icon-video-camera"></i>
                  {{ item.video ? (item.videoName || '课程视频') : '暂无视频' }}
                  <b v-if="item.homework == 1">含作业</b>
                  <b v-if="item.courseware">课件</b>
                </em>
              </span>
              <i class="el-icon-arrow-right"></i>
            </button>
            <div v-if="chapter.length === 0" class="taskInfo-video-empty">
              <i class="el-icon-folder-opened"></i>
              <p>暂无章节内容</p>
            </div>
          </div>
        </aside>
      </div>
    </div>
    
    <!-- 发表评论区域 -->
    <div class="taskInfo-bottom">
      <div class="taskInfo-comment">
        <div class="taskInfo-comment-font">发表评论</div>
        <div class="taskInfo-comment-content">
          <el-input 
            type="textarea" 
            resize="none" 
            rows="4" 
            v-model="content" 
            placeholder="分享你的学习心得、疑问或建议..."
          ></el-input>
          <div class="taskInfo-comment-btn">
            <el-button @click="saveApeTaskComment" size="mini" type="success" plain>提交评论</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 学员评论列表区域 -->
    <div class="taskInfo-bottom">
      <div class="taskInfo-comment">
        <div class="taskInfo-comment-font">学员评论</div>
        <div v-if="commentList.length <= 0" class="taskInfo-comment-empty">
          <el-empty description="还没有评论，快来发表第一条评论吧！"></el-empty>
        </div>
        <div v-if="commentList.length > 0" class="taskInfo-comment-list">
          <div v-for="(item,index) in commentList" :key="index">
            <!-- 奇数索引 - 左侧布局 -->
            <div v-if="(index+1)%2 == 1" class="taskInfo-comment-item">
              <div>
                <img
                  :src="$resolveAvatar(item.avatar, '/img/avatars/zhixue-default.png')"
                  :alt="item.createBy"
                  @error="$avatarError($event, '/img/avatars/zhixue-default.png')">
              </div>
              <div style="flex:1">
                <div class="taskInfo-comment-item1">
                  {{item.createBy}}
                  <span style="font-size: 12px; color: #94a3b8; font-weight: normal;">
                    · {{item.createTime}}
                  </span>
                </div>
                <div class="taskInfo-comment-item2">{{item.content}}</div>
              </div>
            </div>
            <!-- 偶数索引 - 右侧布局 -->
            <div v-if="(index+1)%2 == 0" class="taskInfo-comment-item-right">
              <div style="flex:1; text-align: right;">
                <div class="taskInfo-comment-item1">
                  {{item.createBy}}
                  <span style="font-size: 12px; color: #94a3b8; font-weight: normal;">
                    · {{item.createTime}}
                  </span>
                </div>
                <div class="taskInfo-comment-item3">{{item.content}}</div>
              </div>
              <div>
                <img
                  :src="$resolveAvatar(item.avatar, '/img/avatars/zhixue-default.png')"
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
import {
  saveApeTaskFavor,
  removeApeTaskFavor,
  getApeTaskFavorById,
  saveApeChapterVideo,
  getApeHomeworkByChapterId,
  getApeTaskById,
  saveApeTaskStudent,
  getTaskStudent,
  getApeChapterByTaskId,
  saveApeTaskComment,
  getApeTaskCommentListByTaskId
} from "../../api/api"
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'TaskInfo',
  data() {
    return {
      user: {},
      taskId: "",
      state: 2,
      task: {},
      chapter: [],
      selectChapter: "",
      selectSrc: "",
      select: false,
      videoKey: 0,
      commentList: [],
      content: "",
      favor: {},
      flag: false,
      favorCount: 0,
      favorLoading: false
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  computed: {
    chapterCount() {
      return this.chapter.length
    },
    homeworkCount() {
      return this.chapter.filter(item => item.homework == 1).length
    },
    courseSummary() {
      return this.task.taskDescribe || '课程简介正在完善中，请先查看章节目录和学习资料。'
    },
    courseLevel() {
      if (this.chapterCount >= 12) return '系统进阶'
      if (this.chapterCount >= 6) return '完整入门'
      return '轻量课程'
    },
    enrollLabel() {
      if (this.state === 0) return '继续学习'
      if (this.state === 1) return '未通过，重新报名'
      if (this.state === null || this.state === undefined) return '报名审核中'
      return '报名学习'
    },
    enrollStatusLabel() {
      if (this.state === 0) return '已加入课程'
      if (this.state === 1) return '未通过'
      if (this.state === null || this.state === undefined) return '等待审核'
      return '未报名'
    },
    learningOutcomes() {
      return [
        {
          icon: 'el-icon-reading',
          title: '建立知识框架',
          desc: '按章节推进学习，先理解主线，再补充细节。'
        },
        {
          icon: 'el-icon-video-camera',
          title: '跟随视频实践',
          desc: '通过课程视频和课件，把知识点落到具体操作。'
        },
        {
          icon: 'el-icon-edit-outline',
          title: '作业巩固反馈',
          desc: '通过章节作业、考试和错题记录检验掌握程度。'
        },
        {
          icon: 'el-icon-chat-line-round',
          title: '沉淀学习笔记',
          desc: '结合评论、答疑和笔记，把学习过程留下来。'
        }
      ]
    }
  },
  methods: {
    removeFavor() {
      this.favorLoading = true
      removeApeTaskFavor({ ids: this.favor.id }).then(res => {
        if (res.code == 1000) {
          this.$message({ type: 'success', message: '取消收藏成功' });
          // 以后端返回的最新数据为准
          if (res.data) {
            this.favorCount = res.data.favorCount || 0
            this.flag = false
            this.favor = {}
          } else {
            this.getApeTaskFavorById()
          }
          // 同步刷新课程详情中的收藏数
          this.refreshTaskFavorData()
        }
      }).finally(() => {
        this.favorLoading = false
      })
    },
    addFavor() {
      this.favorLoading = true
      var param = { taskId: this.taskId, userId: this.user.id }
      saveApeTaskFavor(param).then(res => {
        if (res.code == 1000) {
          this.$message({ type: 'success', message: '收藏成功' });
          // 以后端返回的最新数据为准
          if (res.data) {
            this.favorCount = res.data.favorCount || 0
            this.flag = true
          } else {
            this.getApeTaskFavorById()
          }
          // 同步刷新课程详情中的收藏数
          this.refreshTaskFavorData()
        }
      }).finally(() => {
        this.favorLoading = false
      })
    },
    refreshTaskFavorData() {
      // 重新请求课程详情以同步最新的 isFavorite 和 favorCount
      getApeTaskById({ id: this.taskId }).then(res => {
        if (res.code == 1000 && res.data) {
          if (res.data.favorCount !== undefined) {
            this.favorCount = res.data.favorCount
          }
          if (res.data.isFavorite !== undefined) {
            this.flag = res.data.isFavorite == 1
          }
        }
      })
    },
    getApeTaskFavorById() {
      getApeTaskFavorById({ taskId: this.taskId, userId: this.user.id }).then(res => {
        if (res.code == 1000) {
          this.favor = res.data
          this.flag = true
        } else {
          this.favor = {}
          this.flag = false
        }
      })
    },
    getApeTaskCommentListByTaskId() {
      getApeTaskCommentListByTaskId({ id: this.taskId }).then(res => {
        if (res.code == 1000) {
          this.commentList = res.data
        }
      })
    },
    toQuestion(id) {
      if (this.state != 0) {
        this.$message({ type: 'warning', message: '报名课程通过后才能进行答疑' });
        return
      }
      this.$router.push("/question?id=" + id)
    },
    saveApeTaskComment() {
      if (this.state != 0) {
        this.$message({ type: 'warning', message: '报名课程通过后才能发表评论' });
        return
      }
      if (!this.content.trim()) {
        this.$message({ type: 'warning', message: '请输入评论内容' });
        return
      }
      var param = { content: this.content, taskId: this.taskId }
      saveApeTaskComment(param).then(res => {
        if (res.code == 1000) {
          this.$message({ type: 'success', message: '评论成功' });
          this.content = ""
          this.getApeTaskCommentListByTaskId()
        }
      })
    },
    toTeacher(id) {
      this.$router.push("/teacherInfo?id=" + id)
    },
    toArticle() {
      this.$router.push("/article?name=" + this.task.name)
    },
    toLearning() {
      if (this.state != 0) {
        this.$message({ type: 'warning', message: '请先报名课程' });
        return
      }
      this.$router.push("/rateOfLearning?id=" + this.task.id)
    },
    downloadfile() {
      if (this.state != 0) {
        this.$message({ type: 'warning', message: '报名课程通过后才能下载课件' });
        return
      }
      if (this.selectChapter == "") {
        this.$message({ type: 'warning', message: '请先选择章节再下载课件' });
        return
      }
      if (this.selectChapter.courseware) {
        window.location.href = this.$store.state.HOST + this.selectChapter.courseware
      } else {
        this.$message({ type: 'warning', message: '该章节暂无课件' });
        return
      }
    },
    changeChapter(index) {
      if (this.state != 0) {
        this.$message({ type: 'warning', message: '报名课程通过后才能观看视频' });
        return
      }
      const chapter = this.chapter[index]
      if (!chapter) {
        return
      }
      this.selectChapter = chapter
      this.selectSrc = chapter.video ? this.$store.state.HOST + chapter.video : ''
      this.videoKey += 1
      this.select = true
      
      this.$nextTick(() => {
        if (this.selectSrc && this.$refs.videoPlayer) {
          this.$refs.videoPlayer.load()
        }
      })
    },
    markChapterWatched() {
      if (this.selectChapter && this.selectChapter.id) {
        var param = { chapterId: this.selectChapter.id }
        saveApeChapterVideo(param)
      }
    },
    addTaskStudent() {
      if (this.state == 0) {
        if (this.chapter.length > 0) {
          this.changeChapter(0)
        } else {
          this.$message({ type: 'warning', message: '课程已报名，但暂无章节内容' });
        }
      } else if (this.state == 1) {
        this.$message({ type: 'warning', message: '报名正在审核中，请耐心等待教师通过' });
      } else {
        var param = { taskId: this.taskId }
        saveApeTaskStudent(param).then(res => {
          if (res.code == 1000) {
            this.$message({ type: 'success', message: '报名成功，请等待教师通过!' });
            getTaskStudent({ id: this.taskId }).then(res => {
              if (res.code == 1000) {
                this.state = res.data
              }
            })
          }
        })
      }
    },
    toAssign() {
      if (this.state != 0) {
        this.$message({ type: 'warning', message: '报名课程通过后才能完成作业' });
        return
      }
      if (this.selectChapter == "") {
        this.$message({ type: 'warning', message: '请先选择课程章节!' });
        return
      }
      getApeHomeworkByChapterId({ id: this.selectChapter.id }).then(res => {
        if (res.code == 1000) {
          this.$router.push("/assignInfo?id=" + this.selectChapter.id)
        } else {
          this.$message({ type: 'warning', message: '该章节暂未发布作业!' });
        }
      })
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    try { this.user = JSON.parse(window.localStorage.getItem("user_info")) } catch(e) { this.user = {} }
    this.taskId = this.$route.query.id
    
    getApeTaskById({ id: this.taskId }).then(res => {
      if (res.code == 1000) {
        this.task = res.data
        // 同步收藏数和收藏状态
        if (res.data.favorCount !== undefined) {
          this.favorCount = res.data.favorCount
        }
        if (res.data.isFavorite !== undefined) {
          this.flag = res.data.isFavorite == 1
        }
      }
    })
    getTaskStudent({ id: this.taskId }).then(res => {
      if (res.code == 1000) {
        this.state = res.data
      }
    })
    getApeChapterByTaskId({ id: this.taskId }).then(res => {
      if (res.code == 1000) {
        this.chapter = res.data
      }
    })
    this.getApeTaskCommentListByTaskId()
    this.getApeTaskFavorById()
  },
}
</script>

<style scoped>
@import url("../../assets/css/task/taskInfo.css");
</style>
