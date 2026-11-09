<template>
  <div class="assignInfo">
    <headerPage></headerPage>
    <div class="assignInfo-content">
      <div class="assignInfo-top">
        <!-- 作业标题 -->
        <div class="assignInfo-title">
          {{chapter.taskName}} - {{chapter.name}}{{ $t("- 章节作业") }}</div>
        
        <!-- 作业状态 -->
        <div class="assignInfo-state">
          <span>{{flag ? $t("✅ 已提交") : $t("📝 未提交")}}</span>
          <span>{{ $t("📊 总分：") }}{{total}}{{ $t("分") }}</span>
          <span v-if="flag">{{ $t("🏆 得分：") }}{{defen}}{{ $t("分") }}</span>
        </div>
        
        <!-- 题目列表 -->
        <div class="assignInfo-list">
          <div class="assignInfo-item" v-for="(item,index) in homework" :key="index">
            <div>
              {{index + 1}}.{{item.title}} 
              <span>({{item.score}}{{ $t("分)") }}</span>
              <span v-if="item.type == 0">{{ $t("【单选题】") }}</span>
              <span v-if="item.type == 1">{{ $t("【多选题】") }}</span>
              <span v-if="item.type == 2">{{ $t("【填空题】") }}</span>
              <span v-if="item.type == 3">{{ $t("【判断题】") }}</span>
              <!-- 批改后显示正确答案 -->
              <span 
                v-if="flag && item.score != item.point" 
                style="background: #fee2e2; color: #dc2626; margin-left: 12px;"
              >{{ $t("正确答案：") }}{{item.answer}}
              </span>
              <span 
                v-if="flag && item.score == item.point" 
                style="background: #e8f5ed; color: #20B486; margin-left: 12px;"
              >{{ $t("正确答案：") }}{{item.answer}}
              </span>
            </div>
            <div>
              <!-- 单选题 -->
              <el-radio-group v-model="item.solution" v-if="item.type == 0" :disabled="flag">
                <el-radio v-for="(e,s) in item.content" :key="s" :value="e.value">
                  {{e.value}}.{{e.option}}
                </el-radio>
              </el-radio-group>
              
              <!-- 多选题 -->
              <el-checkbox-group v-model="item.solution" v-if="item.type == 1" :disabled="flag">
                <el-checkbox v-for="(e,s) in item.content" :key="s" :value="e.value">
                  {{e.value}}.{{e.option}}
                </el-checkbox>
              </el-checkbox-group>
              
              <!-- 填空题 -->
              <el-input 
                v-model="item.solution" 
                v-if="item.type == 2" 
                size="small"
                :placeholder="$t('请输入答案')"
                :disabled="flag"
                clearable
              ></el-input>
              
              <!-- 判断题 -->
              <el-radio-group v-model="item.solution" v-if="item.type == 3" :disabled="flag">
                <el-radio value="正确">{{ $t("✔️ 正确") }}</el-radio>
                <el-radio value="错误">{{ $t("✖️ 错误") }}</el-radio>
              </el-radio-group>
            </div>
          </div>
          
          <!-- 提交按钮 -->
          <div v-if="!flag">
            <el-button @click="submit" size="small" type="primary">{{ $t("📝 提交作业") }}</el-button>
          </div>
        </div>
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { 
  getApeChapterById, 
  getApeHomeworkStudentList, 
  saveApeHomeworkStudent, 
  getApeHomeworkStudentFlag 
} from "../../api/api"
import headerPage from "../../components/header/header.vue"
import bottomPage from "../../components/bottom/bottom.vue"

export default {
  name: 'AssignInfo',
  data() {
    return {
      chapterId: "",
      chapter: {},
      homework: [],
      total: 0,
      defen: 0,
      flag: false
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    getApeChapterById() {
      getApeChapterById({ id: this.chapterId }).then(res => {
        if(res.code == 1000) {
          this.chapter = res.data
        }
      }).catch(() => {
        this.$message.error(t("获取章节信息失败"))
      })
    },
    getApeHomeworkStudentList() {
      var param = { chapterId: this.chapterId }
      getApeHomeworkStudentList(param).then(res => {
        if (res.code == 1000) {
          this.homework = res.data
          this.total = 0
          this.defen = 0
          for(let i = 0; i < this.homework.length; i++) {
            var item = this.homework[i]
            this.total = this.total + item.score
            this.defen = this.defen + item.point
            if (item.type == "0" || item.type == "1") {
              if (item.content) {
                item.content = JSON.parse(item.content)
              }
              if(item.type == 1 && item.solution) {
                item.solution = item.solution.split(",")
              } else if(item.type == 1 && !item.solution) {
                item.solution = []
              }
            }
          }
        }
      }).catch(() => {
        this.$message.error(t("获取作业列表失败"))
      })
    },
    submit() {
      this.$confirm(t("确定要提交作业内容吗？提交后将无法修改！"), t("提交提醒"), {
        confirmButtonText: '确定提交',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(() => {
        for(let i = 0; i < this.homework.length; i++) {
          var item = this.homework[i]
          if(item.type == 1 && Array.isArray(item.solution)) {
            item.solution = item.solution.join(",")
          }
        }
        var param = { homework: this.homework }
        saveApeHomeworkStudent(param).then(res => {
          if (res.code == 1000) {
            this.$message({
              type: 'success',
              get message() { return t("提交成功！") }
            });
            setTimeout(() => {
              this.$router.push("/taskInfo?id=" + this.chapter.taskId)
            }, 1500)
          } else {
            this.$message({
              type: 'error',
              message: res.message
            });
          }
        }).catch(() => {
          this.$message.error(t("提交失败，请稍后重试"))
        })
      }).catch(() => {})
    },
    getApeHomeworkStudentFlag() {
      getApeHomeworkStudentFlag({ id: this.chapterId }).then(res => {
        if(res.code == 1000) {
          this.flag = true
        } else {
          this.flag = false
        }
      }).catch(() => { /* The request layer already reports failed or cancelled requests. */ })
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    var chapterId = this.$route.query.id
    if (chapterId) {
      this.chapterId = chapterId
    }
    this.getApeChapterById()
    this.getApeHomeworkStudentList()
    this.getApeHomeworkStudentFlag()
  }
}
</script>

<style scoped>
@import url("../../assets/css/assign/assignInfo.css");
</style>