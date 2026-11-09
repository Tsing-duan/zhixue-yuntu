<template>
  <div class="ai-data-page">
    <!-- 管理员：用量卡片 -->
    <div class="usage-cards" v-if="isAdmin">
      <div class="usage-card"><div class="usage-icon prompt-icon"><i class="el-icon-reading"></i></div><div class="usage-info"><span class="usage-num">{{ fmtNum(usage.weekPromptTokens) }}</span><span class="usage-label">{{ $t("近7天输入 tokens") }}</span></div></div>
      <div class="usage-card"><div class="usage-icon completion-icon"><i class="el-icon-edit-outline"></i></div><div class="usage-info"><span class="usage-num">{{ fmtNum(usage.weekCompletionTokens) }}</span><span class="usage-label">{{ $t("近7天输出 tokens") }}</span></div></div>
      <div class="usage-card"><div class="usage-icon total-icon"><i class="el-icon-data-line"></i></div><div class="usage-info"><span class="usage-num">{{ fmtNum(usage.weekTotalTokens) }}</span><span class="usage-label">{{ $t("近7天总计 tokens") }}</span></div></div>
      <div class="usage-card"><div class="usage-icon call-icon"><i class="el-icon-phone-outline"></i></div><div class="usage-info"><span class="usage-num">{{ usage.weekCount || 0 }}</span><span class="usage-label">{{ $t("近7天成功生成次数") }}</span></div></div>
      <div class="usage-card cost-card"><div class="usage-icon cost-icon"><i class="el-icon-money"></i></div><div class="usage-info"><span class="usage-num cost-num">{{ costEstimateText }}</span><span class="usage-label">{{ $t("近7天预估费用") }}</span></div></div>
    </div>

    <!-- 教师：所选课程/考试数据卡片 -->
    <div class="usage-cards" v-if="!isAdmin && activeAgent === 'course-health' && courseStats">
      <div class="usage-card"><div class="usage-icon prompt-icon"><i class="el-icon-user"></i></div><div class="usage-info"><span class="usage-num">{{ courseStats.enrolled || 0 }}</span><span class="usage-label">{{ $t("报名人数") }}</span></div></div>
      <div class="usage-card"><div class="usage-icon completion-icon"><i class="el-icon-notebook-2"></i></div><div class="usage-info"><span class="usage-num">{{ courseStats.chapterCount || 0 }}</span><span class="usage-label">{{ $t("章节数") }}</span></div></div>
      <div class="usage-card"><div class="usage-icon total-icon"><i class="el-icon-edit-outline"></i></div><div class="usage-info"><span class="usage-num">{{ courseStats.homeworkTotal || 0 }}</span><span class="usage-label">{{ $t("作业题目") }}</span></div></div>
      <div class="usage-card"><div class="usage-icon call-icon"><i class="el-icon-document-checked"></i></div><div class="usage-info"><span class="usage-num">{{ courseStats.homeworkSubmitted || 0 }}</span><span class="usage-label">{{ $t("已提交人次") }}</span></div></div>
      <div class="usage-card cost-card"><div class="usage-icon cost-icon"><i class="el-icon-s-check"></i></div><div class="usage-info"><span class="usage-num cost-num">{{ courseStats.examCount || 0 }}</span><span class="usage-label">{{ $t("考试数") }}</span></div></div>
    </div>
    <div class="usage-cards" v-if="!isAdmin && activeAgent === 'exam-analysis' && examStats">
      <div class="usage-card"><div class="usage-icon prompt-icon"><i class="el-icon-user"></i></div><div class="usage-info"><span class="usage-num">{{ examStats.studentCount || 0 }}</span><span class="usage-label">{{ $t("参与人数") }}</span></div></div>
      <div class="usage-card"><div class="usage-icon completion-icon"><i class="el-icon-data-line"></i></div><div class="usage-info"><span class="usage-num">{{ examStats.avgScore || 0 }}</span><span class="usage-label">{{ $t("平均分 /") }}{{ examStats.totalScore }}</span></div></div>
      <div class="usage-card"><div class="usage-icon total-icon"><i class="el-icon-s-flag"></i></div><div class="usage-info"><span class="usage-num">{{ examStats.passRate || 0 }}%</span><span class="usage-label">{{ $t("及格率（≥60%）") }}</span></div></div>
      <div class="usage-card"><div class="usage-icon call-icon"><i class="el-icon-circle-check"></i></div><div class="usage-info"><span class="usage-num">{{ examStats.passCount || 0 }}/{{ examStats.studentCount || 0 }}</span><span class="usage-label">{{ $t("及格/总人数") }}</span></div></div>
      <div class="usage-card cost-card"><div class="usage-icon cost-icon"><i class="el-icon-timer"></i></div><div class="usage-info"><span class="usage-num cost-num">{{ examStats.totalTime || 0 }}{{ $t("分钟") }}</span><span class="usage-label">{{ $t("考试时长") }}</span></div></div>
    </div>

    <!-- Agent 工具区（原侧边栏布局，不过滤管理员可见项） -->
    <div class="agent-section">
      <div class="agent-sidebar">
        <div
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
        </div>
      </div>

      <div class="agent-main">
        <!-- 平台运营周报 -->
        <template v-if="activeAgent === 'platform-report'">
          <div class="agent-header">
            <h3>{{ $t("平台运营周报") }}</h3>
            <p>{{ $t("汇总用户、课程、考试、内容等全平台数据，AI 生成运营分析报告。") }}</p>
            <el-button type="primary" size="small" :loading="agentLoading" @click="runPlatformReport">{{ $t("生成周报") }}</el-button>
          </div>
        </template>

        <!-- 课程健康度诊断 -->
        <template v-if="activeAgent === 'course-health'">
          <div class="agent-header">
            <h3>{{ $t("课程健康度诊断") }}</h3>
            <p>{{ $t("分析课程选课率、学习进度、作业完成度和考试表现。") }}</p>
            <div class="agent-params">
              <el-select v-model="courseHealthForm.taskId" :placeholder="$t('选择课程')" clearable filterable size="small" @change="onCourseChange">
                <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id"></el-option>
              </el-select>
              <el-button type="primary" size="small" :loading="agentLoading" :disabled="!courseHealthForm.taskId" @click="runCourseHealth">{{ $t("生成诊断") }}</el-button>
            </div>
          </div>
        </template>

        <!-- 考试质量分析 -->
        <template v-if="activeAgent === 'exam-analysis'">
          <div class="agent-header">
            <h3>{{ $t("考试质量分析") }}</h3>
            <p>{{ $t("分析考试成绩分布、题目难度、常见错误类型。") }}</p>
            <div class="agent-params">
              <el-select v-model="examAnalysisForm.testId" :placeholder="$t('选择考试')" clearable filterable size="small" @change="onExamChange">
                <el-option v-for="e in examOptions" :key="e.id" :label="e.name + '（' + e.taskName + '）'" :value="e.id"></el-option>
              </el-select>
              <el-button type="primary" size="small" :loading="agentLoading" :disabled="!examAnalysisForm.testId" @click="runExamAnalysis">{{ $t("生成分析") }}</el-button>
            </div>
          </div>
        </template>

        <!-- 错误 / 加载 / 结果 -->
        <div v-if="agentError" class="agent-error"><i class="el-icon-warning"></i>{{ agentError }}</div>
        <div v-if="agentLoading" class="agent-loading"><span></span><span></span><span></span></div>
        <div v-if="agentReply && !agentLoading" class="agent-result">
          <div class="result-toolbar">
            <el-button size="small" plain  @click="copyResult"><template #icon><i class="el-icon-document-copy" aria-hidden="true"></i></template>{{ $t("复制") }}</el-button>
            <span v-if="selectedHistoryId" class="history-selected-tip">{{ $t("历史记录") }}</span>
          </div>
          <pre>{{ agentReply }}</pre>
        </div>
      </div>

      <aside class="agent-history-panel">
        <div class="history-panel-head">
          <div>
            <strong>{{ $t("历史记录") }}</strong>
            <span>{{ activeModeTitle }}</span>
          </div>
          <el-button
            size="small"
            plain

            :loading="historyLoading"
            @click="loadHistory(selectedHistoryId)"
          ><template #icon><i class="el-icon-refresh" aria-hidden="true"></i></template></el-button>
        </div>
        <div v-if="historyLoading && historyList.length === 0" class="history-empty">{{ $t("正在加载历史") }}</div>
        <div v-else-if="historyList.length === 0" class="history-empty">{{ $t("暂无历史记录") }}</div>
        <div v-else class="history-list">
          <button
            v-for="record in historyList"
            :key="record.id"
            class="history-record"
            :class="{ active: selectedHistoryId === String(record.id) }"
            @click="selectHistory(record)"
          >
            <span class="history-record-title">{{ historyTitle(record) }}</span>
            <span class="history-record-time">{{ record.createTime || '-' }}</span>
            <i
              v-if="deletingHistoryId !== String(record.id)"
              class="el-icon-delete history-record-delete"
              @click.stop="confirmDeleteHistory(record)"
            ></i>
            <i v-else class="el-icon-loading history-record-delete"></i>
          </button>
        </div>
      </aside>
    </div>

    <!-- 图表区（跟随 agent） -->
    <div class="charts-section">
      <!-- 平台周报 → Token 用量趋势 -->
      <template v-if="activeAgent === 'platform-report'">
        <div class="chart-panel">
          <h4>{{ $t("近7天 Token 用量趋势") }}</h4>
          <div ref="tokenChart" class="chart-box"></div>
        </div>
      </template>

      <!-- 课程健康度 → 环形图 + 指标卡片 -->
      <template v-if="activeAgent === 'course-health'">
        <div class="chart-panel" v-if="courseStats">
          <h4>{{ courseStats.courseName }}{{ $t("— 健康度概览") }}</h4>
          <el-row :gutter="14">
            <el-col :md="12"><div ref="coursePieChart" class="chart-box"></div></el-col>
            <el-col :md="12">
              <div class="stat-mini-cards">
                <div class="stat-mini"><span class="sm-num">{{ courseStats.enrolled || 0 }}</span><span class="sm-label">{{ $t("报名人数") }}</span></div>
                <div class="stat-mini"><span class="sm-num">{{ courseStats.chapterCount || 0 }}</span><span class="sm-label">{{ $t("章节数") }}</span></div>
                <div class="stat-mini"><span class="sm-num">{{ courseStats.homeworkTotal || 0 }}</span><span class="sm-label">{{ $t("作业题目") }}</span></div>
                <div class="stat-mini"><span class="sm-num">{{ courseStats.homeworkSubmitted || 0 }}</span><span class="sm-label">{{ $t("已提交人次") }}</span></div>
                <div class="stat-mini"><span class="sm-num">{{ courseStats.examCount || 0 }}</span><span class="sm-label">{{ $t("考试数") }}</span></div>
                <div class="stat-mini"><span class="sm-num">{{ courseStats.examTotalScore || 0 }}</span><span class="sm-label">{{ $t("考试总分") }}</span></div>
              </div>
            </el-col>
          </el-row>
        </div>
        <div class="chart-panel chart-placeholder" v-else>
          <i class="el-icon-pie-chart"></i>{{ $t("选择课程后自动显示健康度图表") }}</div>
      </template>

      <!-- 考试质量分析 → 成绩分布 + 正确率 -->
      <template v-if="activeAgent === 'exam-analysis'">
        <div class="chart-panel" v-if="examStats">
          <h4>{{ examStats.testName }}{{ $t("— 成绩分析") }}</h4>
          <el-row :gutter="14">
            <el-col :md="12"><div ref="scoreDistChart" class="chart-box"></div></el-col>
            <el-col :md="12"><div ref="qAccuracyChart" class="chart-box"></div></el-col>
          </el-row>
          <div class="stat-mini-cards" style="margin-top:10px">
            <div class="stat-mini"><span class="sm-num">{{ examStats.studentCount || 0 }}</span><span class="sm-label">{{ $t("参与人数") }}</span></div>
            <div class="stat-mini"><span class="sm-num">{{ examStats.avgScore || 0 }}</span><span class="sm-label">{{ $t("平均分 /") }}{{ examStats.totalScore }}</span></div>
            <div class="stat-mini"><span class="sm-num">{{ examStats.passRate || 0 }}%</span><span class="sm-label">{{ $t("及格率（≥60%）") }}</span></div>
            <div class="stat-mini"><span class="sm-num">{{ examStats.passCount || 0 }}/{{ examStats.studentCount || 0 }}</span><span class="sm-label">{{ $t("及格/总人数") }}</span></div>
          </div>
        </div>
        <div class="chart-panel chart-placeholder" v-else>
          <i class="el-icon-pie-chart"></i>{{ $t("选择考试后自动显示成绩图表") }}</div>
      </template>
    </div>
  </div>
</template>

<script>
import { t } from '@/i18n'

import * as echarts from 'echarts'
import {
  getApeTaskList, getApeTestPage, getApeTaskByTeacherId,
  aiDataAgent, getAiHistory, deleteAiHistory, getAiUsage, getCourseStats, getExamStats
} from '../../../api/api'

export default {
  name: 'AiData',
  data() {
    return {
      activeAgent: 'platform-report',
      agentLoading: false, agentReply: '', agentError: '',
      historyLoading: false, historyList: [], selectedHistoryId: '', deletingHistoryId: '',
      isAdmin: false,
      courseOptions: [], examOptions: [],
      courseHealthForm: { taskId: '' }, examAnalysisForm: { testId: '' },
      courseStats: null, examStats: null,
      usage: { weekPromptTokens: 0, weekCompletionTokens: 0, weekTotalTokens: 0, weekCount: 0, estimatedCost: null, costEstimateStatus: 'UNCONFIGURED', dailyUsage: [] }
    }
  },
  computed: {
    costEstimateText() {
      if (this.usage.costEstimateStatus === 'UNCONFIGURED' || this.usage.estimatedCost == null) {
        return t("未配置估算")
      }
      return `¥${this.usage.estimatedCost}`
    },
    agentModes() {
      const modes = [
        { key: 'platform-report', icon: 'el-icon-s-data', get title() { return t("平台运营周报") }, get desc() { return t("全平台数据概览与趋势分析") }, adminOnly: true },
        { key: 'course-health', icon: 'el-icon-s-marketing', get title() { return t("课程健康度诊断") }, get desc() { return t("选课、进度、作业、考试表现") }, adminOnly: false },
        { key: 'exam-analysis', icon: 'el-icon-s-check', get title() { return t("考试质量分析") }, get desc() { return t("成绩分布、题目难度、错误类型") }, adminOnly: false }
      ]
      return modes.filter(m => !m.adminOnly || this.isAdmin)
    },
    activeModeTitle() {
      const mode = this.agentModes.find(m => m.key === this.activeAgent)
      return mode ? mode.title : t("AI 数据整理")
    }
  },
  watch: { '$locale'() { this.$nextTick(() => this.initCharts()) } },
  methods: {
    checkRole() {
      try { const u = JSON.parse(window.localStorage.getItem('user') || '{}'); this.isAdmin = u.userType === 0 } catch (e) { this.isAdmin = false }
    },
    switchAgent(key) {
      this.activeAgent = key; this.agentReply = ''; this.agentError = ''
      this.selectedHistoryId = ''; this.historyList = []
      this.courseStats = null; this.examStats = null
      this.loadHistory()
      this.$nextTick(() => { this.initCharts() })
    },
    async runPlatformReport() {
      this.agentLoading = true; this.agentError = ''; this.agentReply = ''
      try {
        const res = await aiDataAgent({ agentType: 'platform-report' })
        this.agentReply = res.code === 1000 ? ((res.data || {}).reply || '') : ''
        this.selectedHistoryId = res.code === 1000 && (res.data || {}).historyId ? String(res.data.historyId) : ''
        if (res.code === 1000) this.loadHistory(this.selectedHistoryId)
        if (res.code !== 1000) this.agentError = res.message || '生成失败'
      } catch (e) { this.agentError = '请求失败' } finally { this.agentLoading = false }
    },
    async onCourseChange() {
      this.courseStats = null; this.agentReply = ''; this.agentError = ''
      this.selectedHistoryId = ''
      if (!this.courseHealthForm.taskId) return
      try { const res = await getCourseStats({ taskId: this.courseHealthForm.taskId }); if (res.code === 1000) { this.courseStats = res.data; this.$nextTick(() => this.initCourseChart()) } } catch (e) {}
    },
    async runCourseHealth() {
      this.agentLoading = true; this.agentError = ''; this.agentReply = ''
      try {
        const res = await aiDataAgent({ agentType: 'course-health', taskId: this.courseHealthForm.taskId })
        this.agentReply = res.code === 1000 ? ((res.data || {}).reply || '') : ''
        this.selectedHistoryId = res.code === 1000 && (res.data || {}).historyId ? String(res.data.historyId) : ''
        if (res.code === 1000) this.loadHistory(this.selectedHistoryId)
        if (res.code !== 1000) this.agentError = res.message || '生成失败'
      } catch (e) { this.agentError = '请求失败' } finally { this.agentLoading = false }
    },
    async onExamChange() {
      this.examStats = null; this.agentReply = ''; this.agentError = ''
      this.selectedHistoryId = ''
      if (!this.examAnalysisForm.testId) return
      try { const res = await getExamStats({ testId: this.examAnalysisForm.testId }); if (res.code === 1000) { this.examStats = res.data; this.$nextTick(() => this.initExamChart()) } } catch (e) {}
    },
    async runExamAnalysis() {
      this.agentLoading = true; this.agentError = ''; this.agentReply = ''
      try {
        const res = await aiDataAgent({ agentType: 'exam-analysis', testId: this.examAnalysisForm.testId })
        this.agentReply = res.code === 1000 ? ((res.data || {}).reply || '') : ''
        this.selectedHistoryId = res.code === 1000 && (res.data || {}).historyId ? String(res.data.historyId) : ''
        if (res.code === 1000) this.loadHistory(this.selectedHistoryId)
        if (res.code !== 1000) this.agentError = res.message || '生成失败'
      } catch (e) { this.agentError = '请求失败' } finally { this.agentLoading = false }
    },
    copyResult() { if (this.agentReply) navigator.clipboard.writeText(this.agentReply).then(() => this.$message.success(t("已复制"))).catch(() => this.$message.warning(t("复制失败"))) },
    loadHistory(selectedId) {
      this.historyLoading = true
      getAiHistory({ agentType: this.activeAgent, pageNumber: 1, pageSize: 20 }).then(res => {
        if (res.code === 1000 && res.data) {
          this.historyList = res.data.records || []
          const nextSelectedId = selectedId || this.selectedHistoryId
          if (nextSelectedId && this.historyList.some(item => String(item.id) === String(nextSelectedId))) {
            this.selectedHistoryId = String(nextSelectedId)
          } else if (this.selectedHistoryId && !this.historyList.some(item => String(item.id) === String(this.selectedHistoryId))) {
            this.selectedHistoryId = ''
          }
        } else {
          this.agentError = res.message || '历史记录加载失败'
        }
      }).catch(() => {
        this.agentError = '历史记录加载失败，不影响重新生成'
      }).finally(() => { this.historyLoading = false })
    },
    selectHistory(record) {
      if (!record) return
      this.selectedHistoryId = String(record.id || '')
      this.agentReply = record.aiReply || ''
      this.agentError = ''
    },
    confirmDeleteHistory(record) {
      if (!record || !record.id) return
      this.$confirm(t("确定删除这条 AI 历史记录吗？"), t("删除确认"), {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const id = String(record.id)
        this.deletingHistoryId = id
        return deleteAiHistory({ id }).then(res => {
          if (res.code === 1000) {
            this.historyList = this.historyList.filter(item => String(item.id) !== id)
            if (this.selectedHistoryId === id) {
              this.selectedHistoryId = ''
              this.agentReply = ''
            }
            this.$message.success(t("已删除"))
          } else {
            this.$message.error(res.message || '删除失败')
          }
        }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
      }).catch(() => {}).finally(() => { this.deletingHistoryId = '' })
    },
    historyTitle(record) {
      const title = this.activeModeTitle
      const user = record && record.userName ? ' / ' + record.userName : ''
      return title + user
    },
    initCharts() {
      if (this.activeAgent === 'platform-report') this.initTokenChart()
      else if (this.activeAgent === 'course-health' && this.courseStats) this.initCourseChart()
      else if (this.activeAgent === 'exam-analysis' && this.examStats) this.initExamChart()
    },
    chartFor(el) {
      const existing = echarts.getInstanceByDom(el)
      if (existing) return existing
      const chart = echarts.init(el)
      const observer = new ResizeObserver(() => { if (!chart.isDisposed()) chart.resize() })
      observer.observe(el)
      this.chartResources.push({ chart, observer })
      return chart
    },
    initTokenChart() {
      const el = this.$refs.tokenChart; if (!el) return; const chart = this.chartFor(el)
      const days = this.usage.dailyUsage || []
      chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: [t("输入"), t("输出")], top: 0, left: 'center' },
        grid: { left: 50, right: 20, top: 32, bottom: 24 },
        xAxis: { type: 'category', data: days.map(d => d.date.slice(5)), axisLabel: { margin: 8 } },
        yAxis: { type: 'value' },
        series: [
          { name: t('输入'), type: 'bar', stack: 'total', data: days.map(d => d.promptTokens), itemStyle: { color: '#5b8deb' } },
          { name: t('输出'), type: 'bar', stack: 'total', data: days.map(d => d.completionTokens), itemStyle: { color: '#3eb87a' } }
        ]
      })
    },
    initCourseChart() {
      const el = this.$refs.coursePieChart; if (!el || !this.courseStats) return; const chart = this.chartFor(el)
      const s = this.courseStats
      chart.setOption({
        tooltip: { trigger: 'item' }, legend: { orient: 'vertical', right: 10 },
        series: [{
          type: 'pie', radius: ['45%', '72%'], center: ['45%', '50%'], label: { formatter: '{b}\n{c}' },
          data: [
            { value: s.homeworkSubmitted || 0, name: t('已提交作业'), itemStyle: { color: '#3eb87a' } },
            { value: Math.max(0, (s.homeworkTotal || 0) - (s.homeworkSubmitted || 0)), name: t('未提交作业'), itemStyle: { color: '#f0c060' } },
            { value: s.enrolled || 0, name: t('报名人数'), itemStyle: { color: '#5b8deb' } },
            { value: s.examCount || 0, name: t('关联考试'), itemStyle: { color: '#8b5cf6' } }
          ]
        }]
      })
    },
    initExamChart() {
      const sel = this.$refs.scoreDistChart; const qel = this.$refs.qAccuracyChart
      if (!sel || !qel || !this.examStats) return; const stats = this.examStats
      const sChart = this.chartFor(sel)
      const dist = stats.scoreDistribution || []
      sChart.setOption({
        tooltip: { trigger: 'axis' }, grid: { left: 40, right: 20, top: 10, bottom: 30 },
        xAxis: { type: 'category', data: dist.map(d => d.label) }, yAxis: { type: 'value' },
        series: [{ type: 'bar', data: dist.map(d => d.count), itemStyle: { color: '#5b8deb' }, barWidth: '55%' }]
      })

      const qChart = this.chartFor(qel)
      const qa = stats.questionAccuracy || []
      qChart.setOption({
        tooltip: { trigger: 'axis' }, grid: { left: 100, right: 30, top: 10, bottom: 20 },
        xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
        yAxis: { type: 'category', data: qa.map(q => 'Q' + q.sort + ' ' + q.title).reverse() },
        series: [{ type: 'bar', data: qa.map(q => q.rate).reverse(), barWidth: '60%',
          itemStyle: { color: p => p.value >= 60 ? '#3eb87a' : '#ef6c6c' } }]
      })
    },
    loadCourses() {
      const map = {}; const done = () => { this.courseOptions = Object.values(map) }
      const request = this.isAdmin ? getApeTaskList() : getApeTaskByTeacherId()
      request.then(res => { if (res.code === 1000 && Array.isArray(res.data)) { res.data.forEach(i => { if (i.id) map[i.id] = i }); done() } }).catch(() => {})
    },
    loadExams() {
      const map = {}; const done = () => { this.examOptions = Object.values(map) }
      getApeTestPage({ pageNumber: 1, pageSize: 200, state: 0, type: 0 }).then(res => { if (res.code === 1000 && res.data && res.data.records) { res.data.records.forEach(i => { if (i.id) map[i.id] = i }); done() } }).catch(() => {})
      getApeTestPage({ pageNumber: 1, pageSize: 200, state: 0, type: 1 }).then(res => { if (res.code === 1000 && res.data && res.data.records) { res.data.records.forEach(i => { if (i.id) map[i.id] = i }); done() } }).catch(() => {})
    },
    loadUsage() {
      if (!this.isAdmin) return
      getAiUsage().then(res => { if (res.code === 1000 && res.data) { this.usage = res.data; this.$nextTick(() => { if (this.activeAgent === 'platform-report') this.initTokenChart() }) } }).catch(() => {})
    },
    fmtNum(n) { if (n == null || n === 0) return '0'; if (n >= 10000) return (n / 10000).toFixed(1) + 'w'; if (n >= 1000) return (n / 1000).toFixed(1) + 'k'; return String(n) }
  },
  created() { this.chartResources = [] },
  beforeUnmount() { this.chartResources.forEach(({chart, observer}) => { observer.disconnect(); chart.dispose() }); this.chartResources = [] },
  mounted() {
    this.checkRole()
    if (!this.isAdmin) this.activeAgent = 'course-health'
    this.loadCourses(); this.loadExams(); this.loadUsage(); this.loadHistory()
  }
}
</script>

<style scoped>
.ai-data-page { color: #3f3f3c; }

.usage-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(170px, 1fr)); gap: 12px; margin-bottom: 18px; }
.usage-card { background: #fcfcfb; border: 1px solid #e8e8e5; border-radius: 11px; padding: 12px 14px; display: flex; align-items: center; gap: 10px; box-shadow: 0 2px 6px rgba(34,34,34,0.04); }
.cost-card { background: linear-gradient(135deg, #fffdf5, #fff8e5); border-color: #edd89e; }
.usage-icon { width: 38px; height: 38px; border-radius: 9px; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
.prompt-icon { background: #eef3fb; color: #3b5bdb; } .completion-icon { background: #e9f6ee; color: #2f7d50; } .total-icon { background: #f1effb; color: #6c5ce7; } .call-icon { background: #f3f3f0; color: #5b5b56; } .cost-icon { background: #fef3e0; color: #d4891a; }
.usage-info { min-width: 0; } .usage-num { display: block; font-size: 19px; font-weight: 700; color: #2f2f2c; } .cost-num { color: #b7770c; } .usage-label { display: block; font-size: 11px; color: #8c8c86; margin-top: 2px; }

.agent-section { display: flex; gap: 16px; background: #fcfcfb; border: 1px solid #e8e8e5; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(34,34,34,0.04); margin-bottom: 18px; }
.agent-sidebar { width: 220px; flex-shrink: 0; background: #f9f9f8; border-right: 1px solid #ededeb; padding: 14px 10px; }
.agent-mode { width: 100%; border: 1px solid transparent; background: transparent; border-radius: 8px; padding: 10px; margin-bottom: 6px; display: flex; gap: 8px; text-align: left; cursor: pointer; transition: all .16s; }
.agent-mode i { width: 28px; height: 28px; border-radius: 7px; background: #f0f0ed; color: #5b5b56; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 14px; }
.agent-mode strong { display: block; font-size: 13px; color: #3f3f3c; } .agent-mode em { display: block; font-size: 11px; color: #9c9c94; font-style: normal; margin-top: 2px; }
.agent-mode:hover, .agent-mode.active { background: #f0f0eb; border-color: #d5d5cf; } .agent-mode.active i { background: #2f3437; color: #fff; }

.agent-main { flex: 1; min-width: 0; padding: 18px 22px; }
.agent-header h3 { margin: 0 0 6px; font-size: 18px; color: #2f2f2c; } .agent-header p { margin: 0 0 14px; font-size: 13px; color: #7a7a72; }
.agent-params { display: flex; gap: 10px; align-items: center; margin-bottom: 16px; } .agent-params .el-select { width: 320px; }

.agent-error { padding: 10px 14px; border-radius: 8px; background: #fef2f2; border: 1px solid #fecaca; color: #dc2626; display: flex; align-items: center; gap: 8px; font-size: 13px; margin-top: 14px; }
.agent-loading { display: flex; gap: 5px; padding: 30px 0; } .agent-loading span { width: 8px; height: 8px; border-radius: 50%; background: #b0b0aa; animation: typing 1.2s infinite ease-in-out; } .agent-loading span:nth-child(2) { animation-delay: .18s; } .agent-loading span:nth-child(3) { animation-delay: .36s; }
@keyframes typing { 0%,60%,100% { transform: translateY(0); opacity: .4 } 30% { transform: translateY(-5px); opacity: 1 } }

.agent-result { margin-top: 14px; border: 1px solid #e0e0da; border-radius: 8px; background: #fdfdfc; overflow: hidden; }
.result-toolbar { height: 40px; padding: 0 12px; border-bottom: 1px solid #ededeb; display: flex; align-items: center; gap: 8px; }
.history-selected-tip { font-size: 12px; color: #7a7a72; padding-left: 2px; }
.agent-result pre { margin: 0; padding: 16px; white-space: pre-wrap; word-break: break-word; line-height: 1.8; font-size: 14px; font-family: inherit; color: #3f3f3c; max-height: 500px; overflow-y: auto; }

.agent-history-panel { width: 260px; flex-shrink: 0; border-left: 1px solid #ededeb; background: #fafaf9; padding: 14px 12px; }
.history-panel-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 10px; }
.history-panel-head strong { display: block; font-size: 14px; color: #2f2f2c; }
.history-panel-head span { display: block; margin-top: 2px; font-size: 11px; color: #9c9c94; }
.history-panel-head .el-button { width: 30px; height: 30px; padding: 0; border-radius: 8px; flex-shrink: 0; }
.history-empty { min-height: 90px; border: 1px dashed #ddddd7; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #9c9c94; font-size: 13px; }
.history-list { display: flex; flex-direction: column; gap: 7px; max-height: 370px; overflow-y: auto; padding-right: 2px; }
.history-record { position: relative; width: 100%; min-height: 58px; border: 1px solid #e4e4df; border-radius: 8px; background: #fff; padding: 9px 34px 9px 10px; text-align: left; cursor: pointer; transition: all .16s; }
.history-record:hover, .history-record.active { border-color: #c8c8c1; background: #f2f2ee; }
.history-record.active { box-shadow: inset 3px 0 0 #2f3437; }
.history-record-title { display: block; color: #3f3f3c; font-size: 13px; line-height: 1.3; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.history-record-time { display: block; margin-top: 5px; color: #9c9c94; font-size: 11px; line-height: 1.2; }
.history-record-delete { position: absolute; top: 9px; right: 9px; width: 18px; height: 18px; color: #aaa9a2; font-size: 14px; display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity .16s, color .16s; }
.history-record:hover .history-record-delete, .history-record.active .history-record-delete { opacity: 1; }
.history-record-delete:hover { color: #dc2626; }

.charts-section { margin-bottom: 18px; }
.chart-panel { background: #fcfcfb; border: 1px solid #e8e8e5; border-radius: 12px; padding: 16px 18px; box-shadow: 0 2px 8px rgba(34,34,34,0.04); }
.chart-panel h4 { margin: 0 0 10px; font-size: 15px; color: #3f3f3c; }
.chart-box { width: 100%; height: 260px; }
.chart-placeholder { display: flex; align-items: center; justify-content: center; gap: 8px; height: 100px; color: #b0b0aa; font-size: 14px; } .chart-placeholder i { font-size: 26px; }
.chart-legend { margin-top: 8px; display: flex; gap: 18px; font-size: 12px; color: #7a7a72; }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 4px; } .prompt-dot { background: #5b8deb; } .completion-dot { background: #3eb87a; }

.stat-mini-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 10px; }
.stat-mini { background: #f8f8f6; border: 1px solid #ededeb; border-radius: 8px; padding: 10px 12px; text-align: center; } .sm-num { display: block; font-size: 20px; font-weight: 700; color: #2f2f2c; } .sm-label { display: block; font-size: 11px; color: #8c8c86; margin-top: 3px; }

:deep(.el-select .el-input__inner) { border-radius: 8px; border-color: #d9d9d4; background: #fff; }
:deep(.el-button--primary) { background: #2f3437; border-color: #2f3437; border-radius: 8px; } :deep(.el-button--primary:hover) { background: #3a4044; border-color: #3a4044; }

@media (max-width: 1100px) {
  .agent-section { flex-wrap: wrap; }
  .agent-main { flex-basis: calc(100% - 236px); }
  .agent-history-panel { width: 100%; border-left: 0; border-top: 1px solid #ededeb; }
  .history-list { max-height: 240px; }
}

@media (max-width: 720px) {
  .agent-section { display: block; }
  .agent-sidebar { width: auto; border-right: 0; border-bottom: 1px solid #ededeb; }
  .agent-main { padding: 16px; }
  .agent-params { display: block; }
  .agent-params .el-select { width: 100%; margin-bottom: 10px; }
}
</style>
