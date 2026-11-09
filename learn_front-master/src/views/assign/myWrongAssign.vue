<template>
  <div class="assignInfo">
    <headerPage></headerPage>
    <div class="assignInfo-content" v-loading="loading">
      <!-- 错题统计卡片 -->
      <div class="wrong-stats">
        <div class="wrong-stats-left">
          <i class="el-icon-warning-outline"></i>
          <div class="stats-info">
            <h3>{{ $t("📚 错题本") }}</h3>
            <div class="stats-number">{{ totalWrongCount }}</div>
            <span style="font-size: 12px; color: #64748b;">{{ $t("道错题待巩固") }}</span>
          </div>
        </div>
        <div class="wrong-stats-right">
          <p><i class="el-icon-info"></i>{{ $t("错题本会自动收录你做错的题目") }}</p>
          <p><i class="el-icon-star-on"></i>{{ $t("反复练习，巩固薄弱知识点") }}</p>
          <p><i class="el-icon-circle-check"></i>{{ $t("掌握后可继续学习新内容") }}</p>
        </div>
      </div>
      
      <!-- 错题列表 - 折叠面板 -->
      <el-collapse v-model="activeNames" accordion>
        <el-collapse-item 
          v-for="(item, index) in data" 
          :key="item.chapterId || index"
          :title="item.taskName + ' - ' + item.chapterName + $t('- 章节作业')" 
          :name="index"
        >
          <div class="assignInfo-top" style="width: 100%">
            <div class="assignInfo-list" style="width: 100%">
              <div 
                class="assignInfo-item" 
                :class="{ 'wrong-item': e.score != e.point, 'correct-item': e.score == e.point }"
                v-for="(e, s) in item.list" 
                :key="e.id || s"
              >
                <div>
                  {{ s + 1 }}.{{ e.title }}
                  <span>({{ e.score }}{{ $t("分)") }}</span>
                  <span v-if="e.type == 0">{{ $t("【单选题】") }}</span>
                  <span v-if="e.type == 1">{{ $t("【多选题】") }}</span>
                  <span v-if="e.type == 2">{{ $t("【填空题】") }}</span>
                  <span v-if="e.type == 3">{{ $t("【判断题】") }}</span>
                  <!-- 错误/正确标记 -->
                  <span v-if="e.score != e.point" class="wrong-badge">{{ $t("❌ 错误") }}</span>
                  <span v-if="e.score == e.point" class="correct-badge">{{ $t("✅ 正确") }}</span>
                </div>
                <div>
                  <!-- 单选题 -->
                  <el-radio-group v-model="e.solution" v-if="e.type == 0" :disabled="e.saving || e.deleting">
                    <el-radio v-for="(m, d) in e.content" :key="d" :value="m.value">
                      {{ m.value }}.{{ m.option }}
                    </el-radio>
                  </el-radio-group>
                  
                  <!-- 多选题 -->
                  <el-checkbox-group v-model="e.solution" v-if="e.type == 1" :disabled="e.saving || e.deleting">
                    <el-checkbox v-for="(m, d) in e.content" :key="d" :value="m.value">
                      {{ m.value }}.{{ m.option }}
                    </el-checkbox>
                  </el-checkbox-group>
                  
                  <!-- 填空题 -->
                  <el-input 
                    v-model="e.solution" 
                    v-if="e.type == 2" 
                    size="small"
                    :placeholder="$t('请输入答案')"
                    :disabled="e.saving || e.deleting"
                  ></el-input>
                  
                  <!-- 判断题 -->
                  <el-radio-group v-model="e.solution" v-if="e.type == 3" :disabled="e.saving || e.deleting">
                    <el-radio value="正确">{{ $t("✔️ 正确") }}</el-radio>
                    <el-radio value="错误">{{ $t("✖️ 错误") }}</el-radio>
                  </el-radio-group>
                </div>
                
                <!-- 显示正确答案 -->
                <div v-if="e.score != e.point" class="correct-answer-section">
                  <span class="correct-answer-label">{{ $t("📖 正确答案：") }}</span>
                  <span class="correct-answer-content">{{ e.answer }}</span>
                </div>

                <div class="wrong-actions">
                  <el-button
                    size="small"
                    type="primary"
                    :loading="e.saving"
                    @click="saveWrongItem(e)"
                  >{{ $t("保存修改") }}</el-button>
                  <el-button
                    size="small"
                    type="danger"
                    plain
                    :loading="e.deleting"
                    @click="removeWrongItem(e)"
                  >{{ $t("删除错题") }}</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
      
      <!-- 空状态 -->
      <div v-if="!loading && data.length === 0" class="wrong-empty">
        <i class="el-icon-circle-check"></i>
        <p>{{ $t("太棒了！暂无错题") }}</p>
        <span>{{ $t("继续保持，你的努力没有白费") }}</span>
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { getWrongWork, editApeHomeworkStudent, removeApeHomeworkStudent } from "../../api/api"
import headerPage from "../../components/header/header.vue"
import bottomPage from "../../components/bottom/bottom.vue"

export default {
  name: 'MyWrongAssign',
  data() {
    return {
      data: [],
      activeNames: 0,
      totalWrongCount: 0,
      loading: false
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    parseContent(content) {
      if (!content) {
        return []
      }
      if (Array.isArray(content)) {
        return content
      }
      try {
        return JSON.parse(content)
      } catch (e) {
        return []
      }
    },

    normalizeQuestion(question) {
      const item = { ...question }
      item.content = this.parseContent(item.content)
      if (item.type == 1) {
        item.solution = item.solution ? String(item.solution).split(",").filter(Boolean) : []
      } else {
        item.solution = item.solution || ""
      }
      item.saving = false
      item.deleting = false
      return item
    },

    groupWrongList(list) {
      const groupMap = {}
      const groups = []
      list.forEach(question => {
        const item = this.normalizeQuestion(question)
        const key = item.chapterId || "unknown"
        if (!groupMap[key]) {
          groupMap[key] = {
            chapterId: item.chapterId,
            taskName: item.taskName || "未知课程",
            chapterName: item.chapterName || "未知章节",
            list: []
          }
          groups.push(groupMap[key])
        }
        groupMap[key].list.push(item)
      })
      return groups
    },

    buildPayload(item) {
      const payload = { ...item }
      if (payload.type == 1 && Array.isArray(payload.solution)) {
        payload.solution = payload.solution.join(",")
      }
      payload.content = JSON.stringify(payload.content || [])
      delete payload.saving
      delete payload.deleting
      return payload
    },
    
    query() {
      this.loading = true
      getWrongWork().then(res => {
        if (res.code == 1000) {
          const list = Array.isArray(res.data) ? res.data : []
          this.data = this.groupWrongList(list)
          this.totalWrongCount = list.length
          this.activeNames = this.data.length > 0 ? 0 : ""
        } else {
          this.$message.error(res.message || "获取错题失败")
        }
      }).catch(() => {
        this.$message.error("获取错题失败，请稍后重试")
      }).finally(() => {
        this.loading = false
      })
    },

    saveWrongItem(item) {
      item["saving"] = true
      editApeHomeworkStudent(this.buildPayload(item)).then(res => {
        if (res.code == 1000) {
          this.$message.success(t("错题已更新"))
          this.query()
        } else {
          this.$message.error(res.message || "更新错题失败")
        }
      }).catch(() => {
        this.$message.error("更新错题失败，请稍后重试")
      }).finally(() => {
        item["saving"] = false
      })
    },

    removeWrongItem(item) {
      this.$confirm(t("确定要从错题本中删除这道题吗？"), t("删除提醒"), {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning",
        center: true
      }).then(() => {
        item["deleting"] = true
        removeApeHomeworkStudent({ ids: item.id }).then(res => {
          if (res.code == 1000) {
            this.$message.success(t("错题已删除"))
            this.query()
          } else {
            this.$message.error(res.message || "删除错题失败")
          }
        }).catch(() => {
          this.$message.error("删除错题失败，请稍后重试")
        }).finally(() => {
          item["deleting"] = false
        })
      }).catch(() => {})
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.query()
  }
}
</script>

<style scoped>
@import url("../../assets/css/assign/assignInfo.css");

/* 错题集额外样式 */
.assignInfo-content {
    padding: 0 24px;
}

.correct-answer-section {
    margin-top: 12px;
    padding: 12px 16px;
    background: #fef3c7;
    border-radius: 12px;
    border-left: 4px solid #f59e0b;
}

.correct-answer-label {
    font-weight: 600;
    color: #92400e;
    font-size: 13px;
}

.correct-answer-content {
    color: #78350f;
    font-size: 13px;
    margin-left: 8px;
}

.wrong-actions {
    margin-top: 12px;
    display: flex;
    gap: 8px;
    justify-content: flex-end;
}

/* 空状态样式 */
.wrong-empty {
    width: 100%;
    max-width: 1280px;
    background: #ffffff;
    border-radius: 24px;
    border: 1px solid rgba(226, 232, 240, 0.6);
    padding: 60px 20px;
    text-align: center;
    margin: 32px auto 0;
}

.wrong-empty i {
    font-size: 64px;
    color: #20B486;
    margin-bottom: 20px;
    display: block;
}

.wrong-empty p {
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 8px;
}

.wrong-empty span {
    font-size: 14px;
    color: #94a3b8;
}

/* 响应式 */
@media (max-width: 768px) {
  .assignInfo-content {
    padding: 0 16px;
  }
  
  .correct-answer-section {
    padding: 10px 12px;
  }
  
  .wrong-empty {
    padding: 40px 16px;
  }
  
  .wrong-empty i {
    font-size: 48px;
  }
  
  .wrong-empty p {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .correct-answer-section {
    flex-direction: column;
    gap: 4px;
  }
  
  .correct-answer-label {
    display: block;
  }
}
</style>
