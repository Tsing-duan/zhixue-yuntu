<template>
  <div class="ai-agent-page">
    <headerPage></headerPage>

    <div class="agent-container">
      <aside class="agent-sidebar">
        <div class="brand-block">
          <div class="brand-icon"><i class="el-icon-cpu"></i></div>
          <div>
            <h2>智学云途 · 小智</h2>
            <p>{{ isTeacher ? 'AI 教学 Agent 工作台' : 'AI 学习 Agent 工作台' }}</p>
          </div>
        </div>

        <button
          v-for="item in agentModes"
          :key="item.key"
          class="agent-mode"
          :class="{ active: activeAgent === item.key }"
          @click="switchAgent(item.key)"
        >
          <i :class="item.icon"></i>
          <span>
            <strong>{{ item.title }}</strong>
            <em>{{ item.desc }}</em>
          </span>
        </button>

      </aside>

      <main class="agent-main">
        <section class="agent-panel">
          <div class="panel-head">
            <div>
              <span class="agent-tag">{{ activeMeta.tag }}</span>
              <h1>{{ activeMeta.title }}</h1>
              <p>{{ activeMeta.detail }}</p>
            </div>
            <el-button
              type="primary"
              class="generate-btn"
              :loading="agentLoading"
              @click="handleAgentPrimaryAction"
            >
              {{ agentPrimaryActionText }}
            </el-button>
          </div>

          <div class="agent-body">
            <aside class="agent-history-list">
              <div class="history-list-head">
                <strong>最近</strong>
                <span v-if="historyLoading">加载中</span>
              </div>
              <div v-if="historyLoading && agentHistoryList.length === 0" class="history-empty">
                正在加载历史结果
              </div>
              <div v-else-if="agentHistoryList.length === 0" class="history-empty">
                生成后会出现在这里
              </div>
              <div v-else class="history-list-scroll">
                <div
                  v-for="record in agentHistoryList"
                  :key="record.id"
                  class="history-item"
                  :class="{ active: String(record.id) === String(agentHistoryId) }"
                  role="button"
                  tabindex="0"
                  @click="selectAgentHistory(record)"
                  @keydown.enter.prevent="selectAgentHistory(record)"
                >
                  <span>{{ getHistoryTitle(record) }}</span>
                  <button
                    type="button"
                    class="history-delete"
                    title="删除"
                    @click.stop="confirmDeleteAgentHistory(record.id)"
                  >
                    <i class="el-icon-delete"></i>
                  </button>
                </div>
              </div>
            </aside>

            <div class="agent-content">
              <div v-if="agentHistoryId && !agentLoading" class="history-notice">
                <span>
                  <i class="el-icon-time"></i>
                  生成于 {{ agentHistoryTime || '刚刚' }}
                </span>
                <el-button type="text" @click="showAgentForm = !showAgentForm">
                  {{ showAgentForm ? '收起参数表单' : '调整参数生成' }}
                </el-button>
              </div>

              <div v-show="showAgentForm || !agentHistoryId" class="agent-form">
                <template v-if="activeAgent === 'study'">
                  <el-form label-position="top">
                    <el-row :gutter="16">
                      <el-col :md="10" :sm="24">
                        <el-form-item label="关联课程">
                          <el-select v-model="studyForm.taskId" placeholder="全部已报名课程" clearable filterable>
                            <el-option
                              v-for="course in courseOptions"
                              :key="course.id"
                              :label="course.name"
                              :value="course.id"
                            ></el-option>
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :md="7" :sm="12">
                        <el-form-item label="计划周期">
                          <el-input-number v-model="studyForm.days" :min="3" :max="90"></el-input-number>
                        </el-form-item>
                      </el-col>
                      <el-col :md="7" :sm="12">
                        <el-form-item label="每周小时">
                          <el-input-number v-model="studyForm.hours" :min="1" :max="40"></el-input-number>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-form-item label="学习目标">
                      <el-input v-model="studyForm.goal" placeholder="例如：两周内补齐 Java 基础并完成章节作业"></el-input>
                    </el-form-item>
                    <el-form-item label="额外要求">
                      <el-input
                        v-model="studyForm.extra"
                        type="textarea"
                        :rows="3"
                        placeholder="例如：每天最多 1 小时，周末可以安排复盘"
                      ></el-input>
                    </el-form-item>
                  </el-form>
                </template>

                <template v-if="activeAgent === 'wrong'">
                  <el-form label-position="top">
                    <el-form-item label="分析目标">
                      <el-input v-model="wrongForm.target" placeholder="例如：找出最薄弱的知识点并安排订正"></el-input>
                    </el-form-item>
                  </el-form>
                  <div class="wrong-toolbar" v-if="wrongItems.length > 0">
                    <el-button size="mini" type="text" @click="selectAllWrong">全选</el-button>
                    <el-button size="mini" type="text" @click="selectedIds = []">取消全选</el-button>
                    <span class="selected-count">已选 {{ selectedIds.length }} / {{ wrongItems.length }} 道</span>
                  </div>
                  <div v-if="wrongItems.length > 0" class="wrong-checklist">
                    <div
                      v-for="item in wrongItems"
                      :key="item.id"
                      class="wrong-check-item"
                      :class="{ checked: selectedIds.includes(item.id) }"
                      @click="toggleWrongItem(item.id)"
                    >
                      <el-checkbox :value="selectedIds.includes(item.id)" @change="() => {}" />
                      <span class="wc-source">{{ item.source }}</span>
                      <span class="wc-course">{{ item.courseName }}</span>
                      <span class="wc-title">{{ item.title }}</span>
                      <span class="wc-answer">答：{{ item.studentAnswer }} → 正确：{{ item.correctAnswer }}</span>
                    </div>
                  </div>
                  <div v-else-if="wrongCount !== null" class="mini-stat">
                    <i class="el-icon-warning-outline"></i>
                    暂无错题记录，继续保持！
                  </div>
                  <div v-else class="mini-stat" style="background:#f0f4ff;border-color:#c5d5f0;color:#3b5bdb;">
                    <i class="el-icon-loading"></i>
                    加载错题列表中...
                  </div>
                </template>

                <template v-if="activeAgent === 'lesson'">
                  <el-form label-position="top">
                    <el-row :gutter="16">
                      <el-col :md="10" :sm="24">
                        <el-form-item label="关联课程">
                          <el-select v-model="lessonForm.taskId" placeholder="选择课程，可不选" clearable filterable>
                            <el-option
                              v-for="course in courseOptions"
                              :key="course.id"
                              :label="course.name"
                              :value="course.id"
                            ></el-option>
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :md="7" :sm="12">
                        <el-form-item label="课时分钟">
                          <el-input-number v-model="lessonForm.minutes" :min="20" :max="120"></el-input-number>
                        </el-form-item>
                      </el-col>
                      <el-col :md="7" :sm="12">
                        <el-form-item label="授课对象">
                          <el-input v-model="lessonForm.students" placeholder="例如：大一学生"></el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-form-item label="备课主题">
                      <el-input v-model="lessonForm.topic" placeholder="例如：面向对象封装与继承"></el-input>
                    </el-form-item>
                    <el-form-item label="教学目标">
                      <el-input
                        v-model="lessonForm.goal"
                        type="textarea"
                        :rows="3"
                        placeholder="例如：学生能够解释核心概念，并独立完成基础练习"
                      ></el-input>
                    </el-form-item>
                  </el-form>
                </template>

                <template v-if="activeAgent === 'summary'">
                  <el-form label-position="top">
                    <el-form-item label="关联课程">
                      <el-select v-model="summaryForm.taskId" placeholder="全部相关课程" clearable filterable>
                        <el-option
                          v-for="course in courseOptions"
                          :key="course.id"
                          :label="course.name"
                          :value="course.id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item label="总结侧重点">
                      <el-input
                        v-model="summaryForm.focus"
                        type="textarea"
                        :rows="3"
                        placeholder="例如：突出章节主线、重点难点和复习清单"
                      ></el-input>
                    </el-form-item>
                  </el-form>
                </template>
              </div>

              <div v-if="agentError" class="agent-error">
                <i class="el-icon-warning"></i>{{ agentError }}
              </div>

              <div v-if="agentReply || agentLoading" class="agent-result">
                <div class="result-head">
                  <span>{{ agentHistoryId ? '历史结果' : activeMeta.title + '结果' }}</span>
                  <div class="result-actions">
                    <el-button v-if="agentReply" size="mini" plain @click="copyAgentReply">复制</el-button>
                    <el-button
                      v-if="agentHistoryId"
                      size="mini"
                      plain
                      type="danger"
                      icon="el-icon-delete"
                      title="删除"
                      @click="confirmDeleteAgentHistory(agentHistoryId)"
                    ></el-button>
                  </div>
                </div>
                <div v-if="agentLoading" class="agent-loading">
                  <span></span><span></span><span></span>
                </div>
                <pre v-else>{{ agentReply }}</pre>
              </div>
            </div>
          </div>
        </section>

        <section class="chat-panel">
          <div class="chat-head">
            <div>
              <h3>自由追问</h3>
              <p>继续围绕学习、错题、备课或课程内容提问</p>
            </div>
          </div>

          <div class="chat-messages" ref="msgContainer">
            <div class="chat-empty" v-if="messages.length === 0 && !loading && !chatHistoryLoading">
              <i class="el-icon-chat-line-round"></i>
              <p>{{ welcomeMsg }}</p>
              <div class="quick-asks">
                <el-tag
                  v-for="question in quickQuestions"
                  :key="question"
                  class="quick-tag"
                  type="info"
                  @click="quickAsk(question)"
                >
                  {{ question }}
                </el-tag>
              </div>
            </div>

            <div v-for="(msg, idx) in messages" :key="idx" :class="['chat-row', msg.role]">
              <div class="chat-avatar">
                <i :class="msg.role === 'ai' ? 'el-icon-cpu' : 'el-icon-user'"></i>
              </div>
              <div class="chat-bubble">
                <div class="chat-text">{{ msg.content }}</div>
                <div class="chat-meta">
                  <span class="chat-time">{{ msg.time }}</span>
                  <button
                    v-if="msg.role === 'ai' && msg.historyId"
                    class="chat-delete"
                    title="删除"
                    @click="confirmDeleteChatHistory(msg.historyId)"
                  >
                    <i class="el-icon-delete"></i>
                  </button>
                </div>
              </div>
            </div>

            <div v-if="loading" class="chat-row ai">
              <div class="chat-avatar"><i class="el-icon-cpu"></i></div>
              <div class="chat-bubble">
                <div class="typing"><span></span><span></span><span></span></div>
              </div>
            </div>
          </div>

          <div v-if="errorMsg" class="agent-error">
            <i class="el-icon-warning"></i>{{ errorMsg }}
          </div>

          <div class="chat-input">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              placeholder="输入问题，Enter 发送，Shift+Enter 换行"
              :disabled="loading"
              @keyup.enter.native="handleEnter"
            ></el-input>
            <el-button
              type="primary"
              class="send-btn"
              :disabled="!canSend"
              :loading="loading"
              @click="sendMsg"
            >
              发送
            </el-button>
          </div>
        </section>
      </main>
    </div>

    <bottomPage></bottomPage>

  </div>
</template>

<script>
import {
  aiChat,
  aiStudyPlan,
  aiWrongAgent,
  aiLessonPrepare,
  aiCourseSummary,
  getAiHistory,
  deleteAiHistory,
  getApeMyTaskList,
  getApeTaskByTeacherId,
  getWrongList
} from '../../api/api'
import headerPage from '../../components/header/header'
import bottomPage from '../../components/bottom/bottom'

export default {
  name: 'AiChat',
  components: { headerPage, bottomPage },
  data() {
    return {
      activeAgent: 'study',
      agentLoading: false,
      agentReply: '',
      agentHistoryList: [],
      agentHistoryId: '',
      agentHistoryTime: '',
      agentHistoryInput: '',
      historyLoading: false,
      showAgentForm: true,
      agentError: '',
      wrongCount: null,
      courseOptions: [],
      studyForm: {
        taskId: '',
        goal: '两周内跟上课程进度，并把薄弱章节补齐',
        days: 14,
        hours: 6,
        extra: '每天安排可执行的小任务，包含复习和错题巩固'
      },
      wrongItems: [],
      selectedIds: [],
      wrongForm: {
        limit: 12,
        target: '找出我最薄弱的知识点，并给出订正和复习步骤'
      },
      lessonForm: {
        taskId: '',
        topic: '结合当前课程章节设计一节课',
        minutes: 45,
        students: '平台课程学生',
        goal: '让学生理解核心概念，并能完成基础练习'
      },
      summaryForm: {
        taskId: '',
        focus: '突出课程主线、章节知识图谱、重点难点和复习建议'
      },
      inputText: '',
      messages: [],
      loading: false,
      chatHistoryLoading: false,
      errorMsg: '',
      isTeacher: false
    }
  },
  computed: {
    canSend() {
      return this.inputText.trim().length > 0 && !this.loading
    },
    agentPrimaryActionText() {
      if (this.agentLoading) return '生成中'
      if (this.agentHistoryId && !this.showAgentForm) return '新聊天'
      return '生成方案'
    },
    agentModes() {
      const modes = [
        {
          key: 'study',
          icon: 'el-icon-guide',
          title: 'AI 自动学习规划',
          tag: 'Study Plan',
          desc: '按课程进度安排学习节奏',
          detail: '结合已报名课程、章节观看和作业完成情况，生成阶段目标与每日行动清单。'
        },
        {
          key: 'wrong',
          icon: 'el-icon-aim',
          title: 'AI 错题 Agent',
          tag: 'Wrong Agent',
          desc: '分析错题原因并安排巩固',
          detail: '读取错题集中的作业与考试错题，归纳薄弱点、错误类型和订正路径，教师可用于班级诊断。'
        },
        {
          key: 'lesson',
          icon: 'el-icon-notebook-2',
          title: 'AI 教师备课助手',
          tag: 'Lesson Prep',
          desc: '生成课堂流程与教学设计',
          detail: '基于课程章节、题目和教师输入，生成教学目标、课堂活动和课后作业建议。',
          teacherOnly: true
        },
        {
          key: 'summary',
          icon: 'el-icon-document-checked',
          title: 'AI 自动总结课程',
          tag: 'Course Summary',
          desc: '提炼课程主线和复习重点',
          detail: '把课程简介、章节和作业概况整理成可复盘的课程总结。'
        }
      ]
      if (!this.isTeacher) {
        return modes.filter(m => !m.teacherOnly)
      }
      return modes
    },
    activeMeta() {
      return this.agentModes.find(item => item.key === this.activeAgent) || this.agentModes[0]
    },
    welcomeMsg() {
      return this.isTeacher
        ? '可以继续追问备课、教学活动、课程总结或学生问题。'
        : '可以继续追问学习计划、课程内容、作业思路或错题订正。'
    },
    quickQuestions() {
      if (this.isTeacher) {
        return ['帮我把刚才的备课方案压缩成课堂流程表', '给这节课设计3个互动问题']
      }
      return ['把学习计划拆成今天要做的3件事', '根据错题结果给我一套复习顺序']
    }
  },
  methods: {
    switchAgent(key) {
      if (this.activeAgent === key) return
      this.activeAgent = key
      this.agentError = ''
      this.wrongCount = null
      this.wrongItems = []
      this.selectedIds = []
      this.agentHistoryList = []
      this.clearAgentHistory()
      this.loadLatestAgentHistory()
      if (key === 'wrong') this.loadWrongList()
    },
    getAgentType(key) {
      const map = {
        study: 'study-plan',
        wrong: 'wrong-agent',
        lesson: 'lesson-prepare',
        summary: 'course-summary',
        chat: 'chat'
      }
      return map[key || this.activeAgent] || key
    },
    extractHistoryRecords(res) {
      if (!res || !res.data) return []
      if (Array.isArray(res.data)) return res.data
      return Array.isArray(res.data.records) ? res.data.records : []
    },
    clearAgentHistory() {
      this.agentReply = ''
      this.agentHistoryId = ''
      this.agentHistoryTime = ''
      this.agentHistoryInput = ''
      this.showAgentForm = true
    },
    startNewAgentChat() {
      this.agentError = ''
      this.agentReply = ''
      this.agentHistoryId = ''
      this.agentHistoryTime = ''
      this.agentHistoryInput = ''
      this.wrongCount = null
      this.showAgentForm = true
    },
    applyAgentHistory(record) {
      this.agentReply = record && record.aiReply ? record.aiReply : ''
      this.agentHistoryId = record && record.id ? String(record.id) : ''
      this.agentHistoryTime = this.formatHistoryTime(record && record.createTime)
      this.agentHistoryInput = record && record.userInput ? record.userInput : ''
      this.showAgentForm = !this.agentHistoryId
    },
    selectAgentHistory(record) {
      if (!record || this.agentLoading) return
      this.agentError = ''
      this.applyAgentHistory(record)
    },
    loadLatestAgentHistory(preferredId) {
      const agentKey = this.activeAgent
      this.historyLoading = true
      getAiHistory({
        agentType: this.getAgentType(agentKey),
        pageNumber: 1,
        pageSize: 12
      }).then(res => {
        if (agentKey !== this.activeAgent) return
        if (res.code === 1000) {
          const records = this.extractHistoryRecords(res)
          this.agentHistoryList = records
          if (records.length > 0) {
            const selected = preferredId
              ? records.find(item => String(item.id) === String(preferredId))
              : null
            this.applyAgentHistory(selected || records[0])
          } else {
            this.clearAgentHistory()
          }
        } else {
          this.agentError = res.message || 'AI 历史加载失败'
        }
      }).catch(() => {
        if (agentKey === this.activeAgent) {
          this.agentError = 'AI 历史加载失败，不影响重新生成'
        }
      }).finally(() => {
        if (agentKey === this.activeAgent) {
          this.historyLoading = false
        }
      })
    },
    getHistoryTitle(record) {
      const input = ((record && record.userInput) || '').replace(/\r/g, '').replace(/\\n/g, '\n').trim()
      const fields = [
        { label: '学习目标：', stops: ['计划周期：', '每周可投入时间：', '额外要求：'] },
        { label: '学生希望：', stops: [] },
        { label: '备课主题：', stops: ['课时长度：', '授课对象：', '教学目标：'] },
        { label: '总结侧重点：', stops: [] }
      ]
      for (const field of fields) {
        const value = this.extractHistoryField(input, field.label, field.stops)
        if (value) {
          return this.compactTitle(value)
        }
      }
      const firstLine = input.split('\n').find(line => line.trim())
      if (firstLine) {
        return this.compactTitle(firstLine.replace(/^请生成一份?/, '').replace(/^请自动/, ''))
      }
      return this.compactTitle((record && record.aiReply) || this.activeMeta.title)
    },
    extractHistoryField(input, label, stopLabels) {
      const index = input.indexOf(label)
      if (index === -1) return ''
      let value = input.slice(index + label.length)
      let end = value.length
      stopLabels.forEach(stop => {
        const stopIndex = value.indexOf(stop)
        if (stopIndex !== -1) {
          end = Math.min(end, stopIndex)
        }
      })
      value = value.slice(0, end)
      return value.split('\n')[0].trim()
    },
    compactTitle(value) {
      const text = String(value || '').replace(/\s+/g, ' ').trim()
      if (!text) return this.activeMeta.title
      return text.length > 22 ? text.slice(0, 22) + '...' : text
    },
    buildAgentHistoryInput(agentKey) {
      if (agentKey === 'study') {
        return '学习目标：' + (this.studyForm.goal || this.activeMeta.title)
      }
      if (agentKey === 'wrong') {
        return '学生希望：' + (this.wrongForm.target || this.activeMeta.title)
      }
      if (agentKey === 'lesson') {
        return '备课主题：' + (this.lessonForm.topic || this.activeMeta.title)
      }
      return '总结侧重点：' + (this.summaryForm.focus || this.activeMeta.title)
    },
    upsertAgentHistoryRecord(record) {
      if (!record || !record.id) return
      const id = String(record.id)
      const normalized = Object.assign({}, record, { id })
      this.agentHistoryList = [
        normalized,
        ...this.agentHistoryList.filter(item => String(item.id) !== id)
      ].slice(0, 12)
    },
    checkRole() {
      const info = window.localStorage.getItem('user_info')
      if (info) {
        try {
          const user = JSON.parse(info)
          this.isTeacher = user.userType === 1
        } catch (e) {
          this.isTeacher = false
        }
      }
    },
    loadCourses() {
      const map = {}
      if (this.isTeacher) {
        getApeTaskByTeacherId().then(res => {
          if (res.code === 1000 && Array.isArray(res.data)) {
            res.data.forEach(item => {
              if (item.id) {
                map[item.id] = {
                  id: item.id,
                  name: item.name || '未命名课程'
                }
              }
            })
            this.courseOptions = Object.values(map)
          }
        }).catch(() => {})
        return
      }

      getApeMyTaskList({ state: 0 }).then(res => {
        if (res.code === 1000 && Array.isArray(res.data)) {
          res.data.forEach(item => {
            if (item.taskId) {
              map[item.taskId] = {
                id: item.taskId,
                name: item.taskName || '未命名课程'
              }
            }
          })
          this.courseOptions = Object.values(map)
        }
      }).catch(() => {})
    },
    handleAgentPrimaryAction() {
      if (this.agentLoading) return
      if (this.agentHistoryId && !this.showAgentForm) {
        this.startNewAgentChat()
        return
      }
      this.runAgent()
    },
    loadWrongList() {
      getWrongList().then(res => {
        if (this.activeAgent !== 'wrong') return
        if (res.code === 1000 && Array.isArray(res.data)) {
          this.wrongItems = res.data
          this.wrongCount = res.data.length
        }
      }).catch(() => {})
    },
    toggleWrongItem(id) {
      const idx = this.selectedIds.indexOf(id)
      if (idx >= 0) this.selectedIds.splice(idx, 1)
      else this.selectedIds.push(id)
    },
    selectAllWrong() {
      this.selectedIds = this.wrongItems.map(item => item.id)
    },

    runAgent() {
      const agentKey = this.activeAgent
      this.agentLoading = true
      this.agentError = ''
      this.agentReply = ''
      this.agentHistoryId = ''
      this.agentHistoryTime = ''
      this.agentHistoryInput = ''

      let request
      if (agentKey === 'study') {
        request = aiStudyPlan(Object.assign({}, this.studyForm))
      } else if (agentKey === 'wrong') {
        const params = Object.assign({}, this.wrongForm)
        if (this.selectedIds.length > 0) {
          params.itemIds = this.selectedIds
        }
        request = aiWrongAgent(params)
      } else if (agentKey === 'lesson') {
        request = aiLessonPrepare(Object.assign({}, this.lessonForm))
      } else {
        request = aiCourseSummary(Object.assign({}, this.summaryForm))
      }

      request.then(res => {
        if (agentKey !== this.activeAgent) return
        if (res.code === 1000) {
          const data = res.data || {}
          this.agentReply = data.reply || ''
          this.agentHistoryId = data.historyId ? String(data.historyId) : ''
          this.agentHistoryTime = this.formatHistoryTime(data.createTime) || this.formatHistoryTime(new Date())
          this.showAgentForm = false
          if (this.agentHistoryId) {
            this.upsertAgentHistoryRecord({
              id: this.agentHistoryId,
              userInput: this.buildAgentHistoryInput(agentKey),
              aiReply: this.agentReply,
              createTime: data.createTime || new Date()
            })
          }
          if (agentKey === 'wrong' && res.data) {
            this.wrongCount = res.data.wrongCount
          }
          if (!this.agentHistoryId) {
            this.loadLatestAgentHistory()
          }
        } else {
          this.agentError = res.message || 'AI Agent 生成失败'
        }
      }).catch(() => {
        if (agentKey === this.activeAgent) {
          this.agentError = '网络请求失败，请稍后重试'
        }
      }).finally(() => {
        if (agentKey === this.activeAgent) {
          this.agentLoading = false
        }
      })
    },
    copyAgentReply() {
      if (!this.agentReply) return
      navigator.clipboard.writeText(this.agentReply).then(() => {
        this.$message.success('已复制')
      }).catch(() => {
        this.$message.warning('复制失败，请手动选择文本')
      })
    },
    handleEnter(e) {
      if (e.shiftKey) return
      e.preventDefault()
      this.sendMsg()
    },
    quickAsk(text) {
      this.inputText = text
      this.sendMsg()
    },
    sendMsg() {
      const text = this.inputText.trim()
      if (!text || this.loading) return

      this.errorMsg = ''
      const time = this.formatTime(new Date())
      const userMessage = { role: 'user', content: text, time, historyId: '' }
      this.messages.push(userMessage)
      this.inputText = ''
      this.loading = true
      this.$nextTick(() => this.scrollBottom())

      aiChat({
        role: this.isTeacher ? 'teacher' : 'student',
        message: text
      }).then(res => {
        if (res.code === 1000) {
          const data = res.data || {}
          const reply = data.reply || ''
          const historyId = data.historyId || ''
          const replyTime = this.formatHistoryTime(data.createTime) || this.formatTime(new Date())
          userMessage.historyId = historyId
          this.messages.push({ role: 'ai', content: reply, time: replyTime, historyId })
        } else {
          this.errorMsg = res.message || 'AI 服务返回异常'
        }
      }).catch(() => {
        this.errorMsg = '网络请求失败，请检查网络连接后重试'
      }).finally(() => {
        this.loading = false
        this.$nextTick(() => this.scrollBottom())
      })
    },
    loadChatHistory() {
      this.chatHistoryLoading = true
      getAiHistory({
        agentType: this.getAgentType('chat'),
        pageNumber: 1,
        pageSize: 20
      }).then(res => {
        if (res.code === 1000) {
          const records = this.extractHistoryRecords(res).slice().reverse()
          const messages = []
          records.forEach(record => {
            const time = this.formatHistoryTime(record.createTime)
            if (record.userInput) {
              messages.push({
                role: 'user',
                content: record.userInput,
                time,
                historyId: record.id
              })
            }
            if (record.aiReply) {
              messages.push({
                role: 'ai',
                content: record.aiReply,
                time,
                historyId: record.id
              })
            }
          })
          this.messages = messages
          this.$nextTick(() => this.scrollBottom())
        } else {
          this.errorMsg = res.message || 'AI 聊天历史加载失败'
        }
      }).catch(() => {
        this.errorMsg = 'AI 聊天历史加载失败，不影响继续提问'
      }).finally(() => {
        this.chatHistoryLoading = false
      })
    },
    confirmDeleteAgentHistory(id) {
      const historyId = id || this.agentHistoryId
      if (!historyId) return
      const selectedId = this.agentHistoryId
      this.$confirm('确定删除这条 AI 结果吗？', '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteAiHistory({ id: historyId })
      }).then(res => {
        if (res.code === 1000) {
          this.$message.success('已删除')
          this.agentHistoryList = this.agentHistoryList.filter(item => String(item.id) !== String(historyId))
          this.loadLatestAgentHistory(String(historyId) === String(selectedId) ? '' : selectedId)
        } else {
          this.$message.error(res.message || '删除失败')
        }
      }).catch(() => {})
    },
    confirmDeleteChatHistory(historyId) {
      if (!historyId) return
      this.$confirm('确定删除这条聊天记录吗？', '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteAiHistory({ id: historyId })
      }).then(res => {
        if (res.code === 1000) {
          this.messages = this.messages.filter(item => item.historyId !== historyId)
          this.$message.success('已删除')
        } else {
          this.$message.error(res.message || '删除失败')
        }
      }).catch(() => {})
    },
    formatHistoryTime(value) {
      if (!value) return ''
      if (typeof value === 'string') {
        return value.replace('T', ' ').slice(0, 19)
      }
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return String(value)
      }
      return date.getFullYear() + '-' +
        (date.getMonth() + 1).toString().padStart(2, '0') + '-' +
        date.getDate().toString().padStart(2, '0') + ' ' +
        this.formatTime(date)
    },
    formatTime(date) {
      return date.getHours().toString().padStart(2, '0') + ':' +
        date.getMinutes().toString().padStart(2, '0')
    },
    scrollBottom() {
      const el = this.$refs.msgContainer
      if (el) {
        el.scrollTop = el.scrollHeight
      }
    }
  },

  created() {
    this.checkRole()
    this.loadCourses()
  },
  mounted() {
    this.loadLatestAgentHistory()
    this.loadChatHistory()
  }
}
</script>

<style scoped>
.ai-agent-page {
  min-height: 100vh;
  background: #f6f8fb;
}

.agent-container {
  display: flex;
  gap: 18px;
  max-width: 1320px;
  margin: 22px auto 0;
  padding: 0 22px 42px;
}

.agent-sidebar {
  width: 286px;
  flex-shrink: 0;
  background: #ffffff;
  border: 1px solid #e7edf4;
  border-radius: 8px;
  padding: 18px;
  height: fit-content;
  position: sticky;
  top: 82px;
}

.brand-block {
  display: flex;
  gap: 12px;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #edf2f7;
  margin-bottom: 14px;
}

.brand-icon {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e8f7f2;
  color: #159a75;
  font-size: 22px;
}

.brand-block h2 {
  margin: 0;
  color: #172033;
  font-size: 18px;
  line-height: 1.3;
}

.brand-block p {
  margin: 4px 0 0;
  color: #718096;
  font-size: 13px;
}

.agent-mode {
  width: 100%;
  border: 1px solid transparent;
  background: transparent;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
  display: flex;
  gap: 10px;
  text-align: left;
  cursor: pointer;
  transition: all 0.18s ease;
}

.agent-mode i {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #f1f5f9;
  color: #3b5bdb;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.agent-mode span {
  min-width: 0;
}

.agent-mode strong {
  display: block;
  color: #243044;
  font-size: 14px;
  line-height: 1.35;
}

.agent-mode em {
  display: block;
  margin-top: 3px;
  color: #7b8798;
  font-size: 12px;
  font-style: normal;
  line-height: 1.45;
}

.agent-mode:hover,
.agent-mode.active {
  background: #f4fbf8;
  border-color: #b7e8d7;
}

.agent-mode.active i {
  background: #20b486;
  color: #ffffff;
}

.agent-main {
  flex: 1;
  min-width: 0;
  display: grid;
  gap: 18px;
}

.agent-panel,
.chat-panel {
  background: #ffffff;
  border: 1px solid #e7edf4;
  border-radius: 8px;
  overflow: hidden;
}

.agent-body {
  display: flex;
  min-height: 360px;
}

.agent-history-list {
  width: 272px;
  flex-shrink: 0;
  padding: 18px 16px;
  border-right: 1px solid #edf2f7;
  background: #ffffff;
}

.history-list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.history-list-head strong {
  color: #111827;
  font-size: 18px;
  line-height: 1.3;
}

.history-list-head span,
.history-empty {
  color: #94a3b8;
  font-size: 12px;
}

.history-list-scroll {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.history-item {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  min-height: 44px;
  padding: 0 8px 0 12px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: #111827;
  cursor: pointer;
  outline: none;
  transition: background 0.16s ease, color 0.16s ease;
}

.history-item:hover,
.history-item:focus,
.history-item.active {
  background: #f1f2f4;
}

.history-item.active {
  font-weight: 600;
}

.history-item span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 15px;
  line-height: 1.4;
}

.history-delete {
  width: 26px;
  height: 26px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  opacity: 0;
  flex-shrink: 0;
  transition: all 0.16s ease;
}

.history-item:hover .history-delete,
.history-item:focus .history-delete,
.history-item.active .history-delete {
  opacity: 1;
}

.history-delete:hover {
  background: #fee2e2;
  color: #dc2626;
}

.agent-content {
  flex: 1;
  min-width: 0;
}

.panel-head {
  padding: 24px 26px;
  border-bottom: 1px solid #edf2f7;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.agent-tag {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: #edf7ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
}

.panel-head h1 {
  margin: 10px 0 8px;
  color: #111827;
  font-size: 24px;
  line-height: 1.25;
}

.panel-head p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.7;
  max-width: 720px;
}

.generate-btn,
.send-btn {
  background: #20b486;
  border-color: #20b486;
}

.agent-form {
  padding: 22px 26px 8px;
}

.agent-form .el-select {
  width: 100%;
}

.history-notice {
  margin: 18px 26px 0;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
}

.history-notice i {
  color: #20b486;
  margin-right: 4px;
}

.mini-stat {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 13px;
}

.agent-error {
  margin: 0 26px 18px;
  padding: 11px 14px;
  border-radius: 8px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.agent-result {
  margin: 6px 26px 26px;
  border: 1px solid #dce7f2;
  border-radius: 8px;
  background: #fbfdff;
  overflow: hidden;
}

.result-head {
  height: 46px;
  padding: 0 14px;
  border-bottom: 1px solid #e7edf4;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #334155;
  font-weight: 600;
  font-size: 14px;
}

.result-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.agent-result pre {
  margin: 0;
  padding: 18px;
  white-space: pre-wrap;
  word-break: break-word;
  color: #263244;
  line-height: 1.8;
  font-size: 14px;
  font-family: inherit;
}

.agent-loading,
.typing {
  display: flex;
  gap: 5px;
  padding: 18px;
}

.agent-loading span,
.typing span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #94a3b8;
  animation: typing 1.2s infinite ease-in-out;
}

.agent-loading span:nth-child(2),
.typing span:nth-child(2) {
  animation-delay: 0.18s;
}

.agent-loading span:nth-child(3),
.typing span:nth-child(3) {
  animation-delay: 0.36s;
}

.chat-head {
  padding: 18px 22px;
  border-bottom: 1px solid #edf2f7;
}

.chat-head h3 {
  margin: 0;
  color: #1f2937;
  font-size: 18px;
}

.chat-head p {
  margin: 4px 0 0;
  color: #7b8798;
  font-size: 13px;
}

.chat-messages {
  min-height: 260px;
  max-height: 430px;
  overflow-y: auto;
  padding: 20px 22px;
  background: #ffffff;
}

.chat-empty {
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #64748b;
}

.chat-empty i {
  color: #20b486;
  font-size: 38px;
  margin-bottom: 10px;
}

.chat-empty p {
  margin: 0;
  line-height: 1.7;
}

.quick-asks {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 14px;
}

.quick-tag {
  cursor: pointer;
}

.chat-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.chat-row.user {
  flex-direction: row-reverse;
}

.chat-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #e8f7f2;
  color: #159a75;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.chat-row.user .chat-avatar {
  background: #eef2ff;
  color: #4f46e5;
}

.chat-bubble {
  max-width: 72%;
}

.chat-row.user .chat-bubble {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.chat-text {
  border-radius: 8px;
  padding: 11px 13px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #334155;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
}

.chat-row.user .chat-text {
  background: #20b486;
  border-color: #20b486;
  color: #ffffff;
}

.chat-time {
  margin-top: 4px;
  color: #94a3b8;
  font-size: 12px;
}

.chat-meta {
  min-height: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-row.user .chat-meta {
  justify-content: flex-end;
}

.chat-delete {
  border: 0;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  padding: 0;
  font-size: 13px;
  line-height: 1;
}

.chat-delete:hover {
  color: #dc2626;
}

.chat-input {
  border-top: 1px solid #edf2f7;
  padding: 16px 22px;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.send-btn {
  flex-shrink: 0;
}

.wrong-toolbar {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 0; margin-bottom: 6px;
}
.selected-count { font-size: 12px; color: #3b5bdb; font-weight: 600; margin-left: auto; }
.wrong-checklist {
  max-height: 240px; overflow-y: auto;
  border: 1px solid #e2e8f0; border-radius: 9px;
  background: #fcfcfb;
}
.wrong-check-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 10px; border-bottom: 1px solid #f0f0ed;
  cursor: pointer; transition: background .14s;
}
.wrong-check-item:last-child { border-bottom: 0; }
.wrong-check-item:hover { background: #f7f8fb; }
.wrong-check-item.checked { background: #eff6ff; }
.wc-source {
  display: inline-flex; align-items: center; height: 20px; padding: 0 6px;
  border-radius: 4px; font-size: 10px; font-weight: 600; color: #fff; flex-shrink: 0;
  background: #f59e0b;
}
.wc-course { font-size: 12px; color: #64748b; flex-shrink: 0; max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.wc-title { font-size: 13px; color: #334155; flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.wc-answer { font-size: 11px; color: #94a3b8; flex-shrink: 0; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.45;
  }
  30% {
    transform: translateY(-5px);
    opacity: 1;
  }
}

@media (max-width: 980px) {
  .agent-container {
    flex-direction: column;
  }

  .agent-sidebar {
    position: static;
    width: auto;
  }

  .panel-head {
    flex-direction: column;
  }

  .agent-body {
    flex-direction: column;
  }

  .agent-history-list {
    width: auto;
    border-right: 0;
    border-bottom: 1px solid #edf2f7;
  }

  .history-list-scroll {
    display: flex;
    gap: 8px;
    overflow-x: auto;
    padding-bottom: 2px;
  }

  .history-item {
    min-width: 220px;
  }

  .history-delete {
    opacity: 1;
  }

  .chat-bubble {
    max-width: 86%;
  }
}

@media (max-width: 640px) {
  .agent-container {
    padding: 0 14px 32px;
  }

  .panel-head,
  .agent-history-list,
  .agent-form,
  .history-notice,
  .chat-head,
  .chat-messages,
  .chat-input {
    padding-left: 16px;
    padding-right: 16px;
  }

  .chat-input {
    flex-direction: column;
  }

  .send-btn {
    width: 100%;
  }
}
</style>
