<template>
  <div class="index">
    <loginPage v-if="!isLogin" />
    <template v-else>
      <headerPage />
      <main class="learning-home">
        <section class="welcome" aria-labelledby="welcome-title">
          <div class="welcome-copy">
            <p class="eyebrow">{{ $t("在这里，一起学习") }}</p>
            <h1 id="welcome-title">{{ greetingText }}</h1>
            <p class="intro">{{ $t("选一门想学的课，也看看同学们留下的笔记与问题。") }}</p>
            <form class="course-search" @submit.prevent="searchTask">
              <label for="home-course-search">{{ $t("今天想学点什么？") }}</label>
              <div class="search-row">
                <el-input id="home-course-search" v-model="searchKey" :placeholder="$t('输入课程名称')" clearable />
                <el-button type="primary" native-type="submit">{{ $t("搜索") }}</el-button>
              </div>
            </form>
          </div>
          <figure class="campus-scene">
            <img src="../../assets/image/index/index-image.png" :alt="$t('两位同学在图书馆阅读、做笔记的校园插画')" />
            <figcaption>{{ $t("一起读书，一起讨论") }}</figcaption>
          </figure>
        </section>
        <section aria-labelledby="today-title" class="task-section">
          <div class="section-heading"><h2 id="today-title">{{ $t("接下来学什么？") }}</h2><span>{{ $t("常用入口") }}</span></div>
          <div class="workbench-grid">
            <button v-for="item in quickActions" :key="item.title" class="workbench-card" @click="$router.push(item.path)">
              <i :class="item.icon" aria-hidden="true"></i>
              <span>{{ item.title }}</span><small>{{ item.desc }}</small>
              <span class="card-arrow" aria-hidden="true">↗</span>
            </button>
          </div>
        </section>
        <section class="course-discovery" aria-labelledby="courses-title">
          <div class="section-heading"><div><p class="eyebrow">{{ $t("把好奇心带进课堂") }}</p><h2 id="courses-title">{{ $t("从一门课程开始") }}</h2></div><router-link to="/task">{{ $t("全部课程 →") }}</router-link></div>
          <p v-if="courses.loading" class="resource-empty" role="status">{{ $t("正在加载课程…") }}</p>
          <div v-else-if="courses.error" class="resource-empty" role="status"><p>{{ $t("课程暂时加载失败") }}</p><el-button size="small" @click="loadCourses">{{ $t("重新加载课程") }}</el-button></div>
          <div v-else-if="courses.items.length" class="home-course-grid">
            <router-link v-for="(item, index) in courses.items" :key="item.id" class="home-course-card" :to="{ path: '/taskInfo', query: { id: item.id } }">
              <div class="home-course-cover" :class="'cover-tone-' + index">
                <img v-if="item.image && !failedCovers[item.id]" :src="$resolveAvatar(item.image)" :alt="item.name + $t('课程封面')" loading="lazy" @error="failedCovers[item.id] = true" />
                <template v-else><i class="el-icon-reading" aria-hidden="true"></i><span>{{ item.classification || $t("课程探索") }}</span></template>
              </div>
              <div class="home-course-copy"><h3>{{ item.name }}</h3><p>{{ item.taskDescribe || $t("打开课程，了解章节与学习内容。") }}</p><div class="home-course-teacher"><i class="el-icon-user" aria-hidden="true"></i><span>{{ item.teacherName || $t("授课教师待补充") }}</span><span aria-hidden="true">→</span></div></div>
            </router-link>
          </div>
          <div v-else class="resource-empty"><p>{{ $t("还没有可浏览的课程") }}</p><router-link to="/task">{{ $t("前往课程页 →") }}</router-link></div>
          <div v-if="major.length" class="explore-majors"><span>{{ $t("按专业探索") }}</span><div class="major-list"><button v-for="item in major" :key="item.id || item.name" @click="majorTask($demoSource(item, 'name'))">{{ item.name }} <span aria-hidden="true">↗</span></button></div></div>
          <div class="teacher-gathering">
            <div class="teacher-gathering-label"><h3>{{ $t("认识授课老师") }}</h3><router-link to="/teacher">{{ $t("查看教师介绍 →") }}</router-link></div>
            <p v-if="teachers.loading" class="resource-empty" role="status">{{ $t("正在加载教师…") }}</p>
            <div v-else-if="teachers.error" class="resource-empty" role="status"><span>{{ $t("教师暂时加载失败") }}</span><el-button size="small" @click="loadTeachers">{{ $t("重新加载教师") }}</el-button></div>
            <div v-else-if="teachers.items.length" class="home-teachers">
              <router-link v-for="item in teachers.items" :key="item.id" :to="{ path: '/teacherInfo', query: { id: item.id } }" class="home-teacher">
                <img :src="$resolveAvatar(item.avatar, '/img/avatars/zhixue-teacher-default.png')" alt="" loading="lazy" @error="$avatarError($event, '/img/avatars/zhixue-teacher-default.png')" />
                <span><strong>{{ item.userName }}</strong><small>{{ item.school || item.agree || $t("查看教师介绍") }}</small></span><span aria-hidden="true">↗</span>
              </router-link>
            </div>
            <p v-else class="resource-empty">{{ $t("暂无可展示的教师介绍") }}</p>
          </div>
        </section>
        <section class="ai-entry" aria-labelledby="ai-entry-title">
          <div class="ai-mark" aria-hidden="true"><i class="el-icon-cpu"></i></div>
          <div class="ai-copy"><p class="eyebrow">{{ $t("学习遇到卡点时") }}</p><h2 id="ai-entry-title">{{ isTeacher ? $t("AI 教学助手") : $t("AI 学习助手") }}</h2><p>{{ $t("整理错题、制定学习计划，或梳理一门课程的重点。") }}</p></div>
          <el-button type="primary" @click="$router.push('/ai')">{{ $t("打开学习助手") }}<span aria-hidden="true">→</span></el-button>
        </section>
        <div class="community-heading"><p class="eyebrow">{{ $t("让学习有来有往") }}</p><h2>{{ $t("看看同学们的分享") }}</h2></div>
        <div class="resources-grid">
          <section class="resource-panel" aria-labelledby="notes-title">
            <div class="section-heading"><h2 id="notes-title">{{ $t("最新笔记") }}</h2><router-link to="/article">{{ $t("全部笔记 →") }}</router-link></div>
            <p v-if="notesLoading" class="resource-empty" role="status">{{ $t("正在加载笔记…") }}</p>
            <div v-else-if="notesError" class="resource-empty" role="status"><p>{{ $t("笔记暂时加载失败") }}</p><el-button size="small" @click="loadNotes">{{ $t("重新加载") }}</el-button></div>
            <ul v-else-if="article.length" class="note-list">
              <li v-for="item in article.slice(0, 3)" :key="item.id || item.title">
                <router-link :to="{ path: '/articleInfo', query: { id: item.id } }" class="note-link">
                  <img class="note-avatar" :src="$resolveAvatar(item.avatar, '/img/avatars/zhixue-default.png')" alt="" loading="lazy" @error="$avatarError($event, '/img/avatars/zhixue-default.png')" />
                  <div><h3>{{ item.title }}</h3><p>{{ item.articleDesc || $t("打开笔记，查看完整内容") }}</p><div class="note-author"><span v-if="item.createBy">{{ item.createBy }}</span><time v-if="item.createTime" :datetime="item.createTime.replace(' ', 'T')">{{ item.createTime.slice(0, 10) }}</time></div></div><span aria-hidden="true">→</span>
                </router-link>
              </li>
            </ul>
            <div v-else class="resource-empty"><p>{{ $t("还没有笔记") }}</p><router-link to="/addArticle">{{ $t("分享你的学习笔记 →") }}</router-link></div>
            <router-link class="community-link" to="/forum">{{ $t("去讨论区交流学习中的问题 →") }}</router-link>
          </section>
          <section class="resource-panel discussion-panel" aria-labelledby="discussions-title">
            <div class="section-heading"><h2 id="discussions-title">{{ $t("一起聊聊") }}</h2><router-link to="/forum">{{ $t("全部讨论 →") }}</router-link></div>
            <p class="panel-description">{{ $t("带着问题来，也把你的思路分享给同学。") }}</p>
            <p v-if="discussions.loading" class="resource-empty" role="status">{{ $t("正在加载讨论…") }}</p>
            <div v-else-if="discussions.error" class="resource-empty" role="status"><p>{{ $t("讨论暂时加载失败") }}</p><el-button size="small" @click="loadDiscussions">{{ $t("重新加载讨论") }}</el-button></div>
            <ul v-else-if="discussions.items.length" class="discussion-list">
              <li v-for="item in discussions.items" :key="item.id"><router-link :to="{ path: '/forumInfo', query: { id: item.id } }" class="discussion-link">
                <span class="discussion-mark" aria-hidden="true"><i class="el-icon-chat-dot-round"></i></span><div><h3>{{ item.name }}</h3><div class="note-author"><span v-if="item.createBy">{{ item.createBy }}</span><time v-if="item.createTime" :datetime="item.createTime.replace(' ', 'T')">{{ item.createTime.slice(0, 10) }}</time></div></div><span aria-hidden="true">→</span>
              </router-link></li>
            </ul>
            <div v-else class="resource-empty"><p>{{ $t("还没有公开讨论") }}</p><router-link to="/forum">{{ $t("进入讨论区，分享一个问题 →") }}</router-link></div>
            <div class="share-invitation"><i class="el-icon-edit-outline" aria-hidden="true"></i><p>{{ $t("刚弄懂的知识，也可能帮到别人。") }}</p><router-link to="/addArticle">{{ $t("写一篇学习笔记 →") }}</router-link></div>
          </section>
        </div>
      </main>
      <bottomPage />
    </template>
  </div>
</template>
<script>
import { t } from '@/i18n'

import { getApeMajorList, getIndexArticleList, getApeTaskPage, getTeacherPage, getApeForumPage } from '../../api/api'
import headerPage from '../../components/header/header.vue'
import bottomPage from '../../components/bottom/bottom.vue'
import loginPage from '../login/login.vue'
export default {
  name: 'Index',
  components: { headerPage, bottomPage, loginPage },
  data: () => ({ searchKey: '', major: [], article: [], courses: { items: [], loading: true, error: false }, teachers: { items: [], loading: true, error: false }, discussions: { items: [], loading: true, error: false }, failedCovers: {}, notesLoading: true, notesError: false, isLogin: false, isTeacher: false, userInfo: {} }),
  computed: {
    greetingText() { return (this.userInfo.userName || '同学') + t("，欢迎回来") },
    quickActions() {
      return [
        { get title() { return t("发现课程") }, get desc() { return t("探索课程与学习方向") }, icon: 'el-icon-reading', path: '/task' },
        { get title() { return t("参加考试") }, get desc() { return t("查看考试安排") }, icon: 'el-icon-edit-outline', path: '/test' },
        { get title() { return t("错题复盘") }, get desc() { return t("整理薄弱知识点") }, icon: 'el-icon-document-checked', path: '/myWrongAssign' },
        { get title() { return t("学习笔记") }, get desc() { return t("记录和浏览笔记") }, icon: 'el-icon-notebook-2', path: '/article' }
      ]
    }
  },
  methods: {
    async loadPreview(state, request, params) {
      state.loading = true
      state.error = false
      try {
        const res = await request(params)
        if (res.code !== 1000 || !Array.isArray(res.data?.records)) throw new Error('Preview unavailable')
        state.items = res.data.records.slice(0, 3)
      } catch { state.error = true }
      finally { state.loading = false }
    },
    loadCourses() { return this.loadPreview(this.courses, getApeTaskPage, { pageNumber: 1, pageSize: 3, state: 0, type: 0 }) },
    loadTeachers() { return this.loadPreview(this.teachers, getTeacherPage, { pageNumber: 1, pageSize: 3 }) },
    loadDiscussions() { return this.loadPreview(this.discussions, getApeForumPage, { pageNumber: 1, pageSize: 3, state: 0 }) },
    async loadNotes() {
      this.notesLoading = true
      this.notesError = false
      try {
        const res = await getIndexArticleList()
        if (res.code !== 1000) throw new Error('Notes unavailable')
        this.article = res.data || []
      } catch { this.notesError = true }
      finally { this.notesLoading = false }
    },
    searchTask() {
      if (this.searchKey.trim()) this.$router.push({ path: '/task', query: { name: this.searchKey.trim() } })
    },
    majorTask(major) { if (major) this.$router.push({ path: '/task', query: { major } }) }
  },
  created() {
    this.isLogin = !!window.localStorage.getItem('user_token')
    try { this.userInfo = this.$demo(JSON.parse(window.localStorage.getItem('user_info') || '{}')) || {} } catch { this.userInfo = {} }
    this.isTeacher = this.userInfo.userType === 1
  },
  mounted() {
    if (!this.isLogin) return
    getApeMajorList().then(res => { if (res.code === 1000) this.major = (res.data || []).slice(0, 16) }).catch(() => {})
    this.loadNotes()
    this.loadCourses()
    this.loadTeachers()
    this.loadDiscussions()
  }
}
</script>

<style scoped>
.note-avatar{width:38px;height:38px;object-fit:cover;border-radius:50%;flex-shrink:0}.note-author{display:flex;flex-wrap:wrap;gap:4px 12px;font-size:11px;color:#687b91;overflow-wrap:anywhere}.community-link{display:block;margin-top:14px;padding-top:16px;border-top:1px solid #edf0f4;color:#245d98;font-size:13px;text-decoration:none}.community-link:hover{text-decoration:underline}
.index{min-height:100vh;background:#f5f7fa;color:#243448}.learning-home{max-width:1160px;margin:0 auto;padding:42px 28px 48px}.welcome{display:flex;align-items:center;justify-content:space-between;gap:40px;padding-bottom:32px;border-bottom:1px solid #dfe5ed}.welcome-copy{min-width:0;flex:1}.eyebrow{font-size:12px;font-weight:600;letter-spacing:.04em;color:#58708c;margin:0 0 10px}h1{font-size:clamp(24px,2.5vw,34px);line-height:1.35;margin:0;overflow-wrap:anywhere;font-weight:650}.intro{font-size:14px;line-height:1.7;color:#66768a;margin:12px 0 0}.course-search{width:320px;flex-shrink:0}.course-search label{display:block;font-size:13px;font-weight:600;margin-bottom:10px}.search-row{display:flex;gap:8px}.search-row :deep(.el-input__wrapper){min-height:40px}.search-row .el-button{height:42px}.task-section{margin-top:30px}.section-heading{display:flex;align-items:center;justify-content:space-between;gap:16px;margin-bottom:18px}.section-heading h2,.ai-copy h2{font-size:19px;margin:0;font-weight:650}.section-heading>span{font-size:12px;color:#748297}.section-heading a,.all-courses,.resource-empty a{font-size:13px;color:#245d98;text-decoration:none}.workbench-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:14px}.workbench-card{position:relative;display:flex;flex-direction:column;align-items:flex-start;gap:10px;padding:22px;background:#fff;border:1px solid #e0e6ef;border-radius:12px;text-align:left;cursor:pointer;color:inherit;font-family:inherit;transition:border-color .15s,box-shadow .15s}.workbench-card>i{font-size:24px;color:#2c6da8;margin-bottom:6px}.workbench-card>span:not(.card-arrow){font-size:16px;font-weight:600}.workbench-card small{font-size:12px;color:#6f7c8d}.workbench-card .card-arrow{position:absolute;right:18px;top:22px;color:#8b9aad}.workbench-card:hover{border-color:#88aacb;box-shadow:0 3px 12px #2434480a}button:focus-visible,a:focus-visible{outline:3px solid #8eb7de;outline-offset:3px}.ai-entry{display:flex;align-items:center;gap:20px;margin:26px 0;padding:24px;border:1px solid #cddfe5;border-radius:12px;background:#edf5f5}.ai-mark{display:flex;align-items:center;justify-content:center;background:white;border:1px solid #d9e9e9;border-radius:10px;width:48px;height:48px;flex-shrink:0;font-size:25px;color:#357b7d}.ai-copy{flex:1}.ai-copy .eyebrow{margin-bottom:6px;color:#457577}.ai-copy p:last-child{font-size:13px;line-height:1.6;color:#597174;margin:8px 0 0}.ai-entry>.el-button{height:42px}.resources-grid{display:grid;grid-template-columns:1.4fr 1fr;gap:20px}.resource-panel{padding:24px;background:white;border:1px solid #e0e6ef;border-radius:12px}.note-list{list-style:none;padding:0;margin:0}.note-list li+li{border-top:1px solid #edf0f4}.note-link{display:flex;gap:14px;align-items:center;padding:16px 0;color:inherit;text-decoration:none}.note-link>i{font-size:22px;color:#7793ad}.note-link>div{min-width:0;flex:1}.note-link h3{font-size:14px;margin:0 0 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;font-weight:600}.note-link p{font-size:12px;color:#6f7c8d;margin:0 0 5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.note-link small{font-size:11px;color:#7f8c9e}.note-link:hover h3{text-decoration:underline;color:#245d98}.note-link>span{color:#8c9bad}.panel-description,.resource-empty{font-size:13px;line-height:1.7;color:#748297}.major-list{display:flex;flex-wrap:wrap;gap:8px;margin:20px 0 24px}.major-list button{padding:10px 14px;min-height:40px;border:1px solid #dce5ee;border-radius:8px;background:#f8fafc;color:#425b75;font:inherit;font-size:13px;cursor:pointer}.major-list button:hover{border-color:#88aacb}.major-list span{margin-left:12px}.resource-empty{padding:20px 0}
@media(max-width:800px){.learning-home{padding:26px 20px}.welcome{align-items:stretch;flex-direction:column;gap:22px}.course-search{width:100%}.workbench-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.resources-grid{grid-template-columns:1fr}.ai-entry{flex-wrap:wrap;gap:14px}.ai-entry>.el-button{margin-left:62px}.ai-copy{min-width:180px}.resource-panel{padding:20px}}
@media(max-width:420px){.learning-home{padding:22px 14px}.workbench-card{padding:17px 14px}.workbench-card small{font-size:11px}.ai-entry{padding:18px}.ai-entry>.el-button{margin-left:0;width:100%}.ai-mark{display:none}}
/* Campus imagery and real learning content, scoped to the student home. */
.index{background:#f7f8f6}.learning-home{padding-top:28px}
.welcome{display:grid;grid-template-columns:minmax(0,1.18fr) minmax(0,1fr);gap:28px;padding:28px;border:1px solid #e7e7d9;border-radius:18px;background:#fffdf5}
.welcome .eyebrow{color:#8b6b39}.welcome h1{font-size:clamp(23px,2.4vw,31px)}.welcome .intro{max-width:420px}.course-search{width:100%;max-width:400px;margin-top:24px}
.campus-scene{position:relative;align-self:stretch;margin:0;min-height:240px;overflow:hidden;border-radius:12px;background:#f2d68b}.campus-scene img{width:100%;height:100%;position:absolute;inset:0;object-fit:cover;object-position:center 64%}.campus-scene figcaption{position:absolute;bottom:14px;left:14px;padding:7px 12px;border-radius:20px;background:#fffaf0eb;color:#665332;font-size:12px}
.workbench-card{padding:18px 20px;gap:8px}.workbench-card>i{margin-bottom:2px}.workbench-card:nth-child(2)>i{color:#ad7946}.workbench-card:nth-child(3)>i{color:#4a8b78}.workbench-card:nth-child(4)>i{color:#8e75a1}
.course-discovery{margin-top:34px}.course-discovery .eyebrow,.community-heading .eyebrow{margin-bottom:7px;color:#7b8775}
.home-course-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}.home-course-card{display:block;border:1px solid #dee6e5;border-radius:14px;overflow:hidden;background:white;color:inherit;text-decoration:none;transition:box-shadow .15s,border-color .15s}.home-course-card:hover{border-color:#8cb7ac;box-shadow:0 6px 18px #1b46300c}
.home-course-cover{height:120px;display:flex;align-items:center;justify-content:center;gap:14px;background:linear-gradient(125deg,#dfefeb,#f0f6e7);color:#4a8071;overflow:hidden}.cover-tone-1{background:linear-gradient(125deg,#f7ecd8,#fcf3e9);color:#a0844f}.cover-tone-2{background:linear-gradient(125deg,#e5eaf6,#f1eef8);color:#7b7fa3}.home-course-cover>i{font-size:40px}.home-course-cover>span{font-size:13px;max-width:60%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.home-course-cover img{width:100%;height:100%;object-fit:cover}
.home-course-copy{padding:17px 18px}.home-course-copy h3{margin:0;font-size:16px;line-height:1.5;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.home-course-copy>p{font-size:12px;line-height:1.7;color:#718079;margin:8px 0 16px;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;min-height:40px;overflow-wrap:anywhere}.home-course-teacher{display:flex;align-items:center;gap:7px;color:#53736b;font-size:12px}.home-course-teacher>span:first-of-type{min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.home-course-teacher>span:last-child{margin-left:auto}
.explore-majors{display:flex;align-items:center;gap:18px;margin:18px 0}.explore-majors>span{font-size:12px;color:#718079;flex-shrink:0}.explore-majors .major-list{margin:0;gap:6px}.explore-majors .major-list button{background:#fff;font-size:12px;min-height:40px;padding:9px 12px}
.teacher-gathering{display:flex;align-items:center;gap:24px;background:#eff4ef;border:1px solid #e0e8de;border-radius:12px;padding:18px 20px;margin-top:18px}.teacher-gathering-label{flex-shrink:0}.teacher-gathering-label h3{font-size:14px;margin:0 0 6px}.teacher-gathering-label a{font-size:12px;color:#567b6d;text-decoration:none}.home-teachers{display:flex;flex-wrap:wrap;gap:12px;min-width:0}.home-teacher{display:flex;align-items:center;gap:10px;min-width:0;max-width:270px;padding:8px 12px;background:#ffffffb5;border-radius:10px;color:inherit;text-decoration:none}.home-teacher:hover{background:white}.home-teacher img{width:40px;height:40px;object-fit:cover;border-radius:50%;flex-shrink:0}.home-teacher>span:first-of-type{min-width:0}.home-teacher strong,.home-teacher small{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.home-teacher strong{font-size:13px;font-weight:600}.home-teacher small{font-size:11px;color:#758177;margin-top:5px}.home-teacher>span:last-child{color:#799788}.teacher-gathering .resource-empty{margin:0;padding:0}.teacher-gathering .resource-empty .el-button{margin-left:12px}
.community-heading{margin:32px 0 18px}.community-heading h2{font-size:20px;margin:0;font-weight:650}.resources-grid{grid-template-columns:1fr 1fr}.resource-panel{border-color:#e0e7e1}.discussion-panel{background:#fffef9}.discussion-panel .panel-description{margin-top:-4px}.discussion-list{padding:0;margin:0;list-style:none}.discussion-list li+li{border-top:1px solid #eeeede}.discussion-link{display:flex;align-items:center;gap:12px;min-height:76px;padding:12px 0;color:inherit;text-decoration:none}.discussion-link>div{min-width:0;flex:1}.discussion-link h3{margin:0 0 8px;font-size:14px;font-weight:600;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.discussion-link:hover h3{color:#245d98;text-decoration:underline}.discussion-link>span:last-child{color:#94a28c}.discussion-mark{width:36px;height:36px;display:flex;align-items:center;justify-content:center;flex-shrink:0;border-radius:11px;background:#f1eddb;color:#9a8a53;font-size:19px}.share-invitation{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding-top:16px;border-top:1px solid #eeeede;color:#79816c;font-size:12px}.share-invitation p{margin:0}.share-invitation a{color:#657c4c;text-decoration:none;min-height:32px;display:inline-flex;align-items:center}.share-invitation a:hover{text-decoration:underline}
@media(max-width:900px){.welcome{gap:22px;padding:24px;grid-template-columns:minmax(0,1.2fr) minmax(0,1fr)}.campus-scene{min-height:250px}.home-course-grid{gap:12px}.home-course-copy{padding:14px}.teacher-gathering{align-items:flex-start}.resources-grid{grid-template-columns:1fr}}
@media(max-width:600px){.welcome{grid-template-columns:1fr;padding:20px;gap:20px}.campus-scene{min-height:0;height:150px}.campus-scene img{object-position:center 62%}.course-search{margin-top:20px;max-width:none}.welcome .intro{font-size:13px}.home-course-grid{grid-template-columns:1fr}.home-course-card{display:grid;grid-template-columns:96px minmax(0,1fr)}.home-course-cover{height:auto;min-height:138px;flex-direction:column;gap:10px}.home-course-cover>i{font-size:32px}.home-course-cover>span{max-width:80%;font-size:10px}.home-course-copy h3{font-size:14px}.home-course-copy>p{font-size:11px;margin-bottom:10px}.home-course-teacher{font-size:11px}.teacher-gathering{flex-direction:column;gap:12px;padding:16px}.teacher-gathering-label{width:100%;display:flex;align-items:center;justify-content:space-between;gap:10px}.teacher-gathering-label h3{margin:0}.home-teacher{max-width:100%;width:100%;box-sizing:border-box}.home-teachers{width:100%}.explore-majors{align-items:flex-start;gap:12px}.explore-majors>span{padding-top:12px}.course-discovery>.section-heading h2{font-size:18px}.course-discovery>.section-heading>a{white-space:nowrap}.community-heading h2{font-size:19px}}
</style>
