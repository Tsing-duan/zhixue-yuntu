<template>
  <div class="testInfo">
    <headerPage></headerPage>
    <div class="testInfo-content">
      <!-- 考前须知卡片 -->
      <div class="test-page-desc">
        <div class="test-page-image">
          <img src="../../assets/image/testP/Accent.png" alt="考试图标">
        </div>
        <div class="test-page-num" v-if="!flag">
          <div class="test-page-num1">⏰ 剩余时间</div>
          <div class="test-page-num2">{{hr}}小时 {{min}}分 {{sec}}秒</div>
        </div>
        <div class="test-page-font">
          <div class="test-page-font1">
            <div>📋 考前须知：</div>
            <div>1、请提前进行基础设备检测，包括鼠标、键盘、显示器或其他硬件设备；</div>
            <div>2、请拔掉U盘、移动硬盘或其他与考试无关的外接设备；</div>
            <div>3、如果您使用的是移动电脑，请确保电源连接正常；</div>
            <div>4、请确保系统内未开启与考试无关的非必要软件；</div>
            <div>5、请确保当前使用的是谷歌浏览器，且页面（浏览器）是最大化状态；</div>
            <div>6、考试过程中，请勿执行任何会导致考试页面失焦的操作。</div>
          </div>
          <div class="test-page-font2">
            <div>⚠️ 注意事项：</div>
            <div>7、考试过程中，请勿修改浏览器和页面的大小；</div>
            <div>8、有些考试可能会禁用复制粘贴功能；</div>
            <div>9、如有视频监控，请保持面部处于画面中心位置；</div>
            <div>10、考试结束后，系统将自动提交试卷；</div>
            <div>11、祝您考试顺利，取得好成绩！</div>
          </div>
        </div>
      </div>
      
      <!-- 考试内容区域 -->
      <div class="testInfo-top">
        <div class="testInfo-title">
          {{test.taskName}} - {{test.name}}
        </div>
        <div class="testInfo-state">
          <span>📊 总分：{{total}}分</span>
          <span v-if="flag">🏆 得分：{{defen}}分</span>
          <span v-if="!flag">⏳ 考试进行中</span>
        </div>
        
        <!-- 题目列表 -->
        <div class="testInfo-list">
          <div class="testInfo-item" v-for="(item,index) in assign" :key="index">
            <div>
              {{index + 1}}.{{item.title}} 
              <span>({{item.score}}分)</span>
              <span v-if="item.type == 0">【单选题】</span>
              <span v-if="item.type == 1">【多选题】</span>
              <span v-if="item.type == 2">【填空题】</span>
              <span v-if="item.type == 3">【判断题】</span>
              <span v-if="item.type == 4">【简答题】</span>
              <span v-if="item.type == 5">【论述题】</span>
              <!-- 批改后显示正确答案 -->
              <span v-if="flag && item.score != item.point" style="background: #fee2e2; color: #dc2626;">
                正确答案：{{item.answer}}
              </span>
              <span v-if="flag && item.score == item.point" style="background: #e8f5ed; color: #20B486;">
                正确答案：{{item.answer}}
              </span>
              <span v-if="flag" style="background: #e8f5ed; color: #20B486;">
                得分：{{item.point}}
              </span>
            </div>
            <div>
              <!-- 单选题 -->
              <el-radio-group v-model="item.solution" v-if="item.type == 0" :disabled="flag">
                <el-radio v-for="(e,s) in item.content" :key="s" :label="e.value">
                  {{e.value}}.{{e.option}}
                </el-radio>
              </el-radio-group>
              
              <!-- 多选题 -->
              <el-checkbox-group v-model="item.solution" v-if="item.type == 1" :disabled="flag">
                <el-checkbox v-for="(e,s) in item.content" :key="s" :label="e.value">
                  {{e.value}}.{{e.option}}
                </el-checkbox>
              </el-checkbox-group>
              
              <!-- 填空题 -->
              <el-input 
                v-model="item.solution" 
                v-if="item.type == 2" 
                size="mini" 
                placeholder="请输入答案"
                :disabled="flag"
                clearable
              ></el-input>
              
              <!-- 判断题 -->
              <el-radio-group v-model="item.solution" v-if="item.type == 3" :disabled="flag">
                <el-radio label="正确">✔️ 正确</el-radio>
                <el-radio label="错误">✖️ 错误</el-radio>
              </el-radio-group>
              
              <!-- 简答题 -->
              <el-input 
                type="textarea" 
                v-model="item.solution" 
                v-if="item.type == 4" 
                size="mini" 
                placeholder="请输入答案"
                :disabled="flag"
                :rows="3"
              ></el-input>
              
              <!-- 论述题 -->
              <el-input 
                type="textarea" 
                v-model="item.solution" 
                v-if="item.type == 5" 
                size="mini" 
                placeholder="请输入答案"
                :disabled="flag"
                :rows="5"
              ></el-input>
            </div>
          </div>
          
          <!-- 提交按钮 -->
          <div v-if="!flag">
            <el-button @click="submit" size="mini" type="primary">📝 提交试卷</el-button>
          </div>
        </div>
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { getApeTestItemByTestId, saveApeTestStudent, getTestUserState } from '../../api/api' 
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'TestInfo',
  data() {
    return {
      id: "",
      assign: [],
      test: {},
      total: 0,
      hr: "00",
      min: "00",
      sec: "00",
      end: "",
      timeOut: null,
      five: false,
      ten: false,
      flag: false,
      defen: 0,
      submitting: false,
      allowLeave: false
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    submit() {
      if (this.submitting) {
        return
      }
      this.$confirm('确定要提交考试内容吗？提交后将无法修改！', '交卷提醒', {
        confirmButtonText: '确定提交',
        cancelButtonText: '继续答题',
        type: 'warning',
        center: true
      }).then(() => {
        this.saveApeTestStudent()
      }).catch(() => {})
    },
    submitEnd() {
      if (this.submitting || this.flag) {
        return
      }
      this.$message({
        message: '考试时间已到，系统自动提交试卷...',
        type: 'warning',
        duration: 3000
      });
      this.saveApeTestStudent()
    },
    buildSubmitList() {
      return this.assign.map(item => {
        var submitItem = Object.assign({}, item)
        if(submitItem.type == 1 && Array.isArray(submitItem.solution)) {
          submitItem.solution = submitItem.solution.join(",")
        }
        return submitItem
      })
    },
    saveApeTestStudent(options = {}) {
      if (this.submitting) {
        return Promise.resolve(false)
      }
      this.submitting = true
      var redirect = options.redirect !== false
      var silent = options.silent === true
      var param = { list: this.buildSubmitList() }
      return saveApeTestStudent(param).then(res => {
        if (res.code == 1000) {
          this.flag = true
          this.allowLeave = true
          clearTimeout(this.timeOut)
          window.removeEventListener('beforeunload', this.handleBeforeUnload)
          if (!silent) {
            this.$message({
              type: 'success',
              message: '交卷成功！'
            });
          }
          if (redirect) {
            this.$router.push("/test")
          }
          return true
        } else {
          this.$message({
            type: 'warning',
            message: res.message
          });
          if (this.isDeadlineMessage(res.message)) {
            this.stopExamAndAllowLeave()
            if (redirect) {
              this.$router.push("/test")
            }
          }
          return false
        }
      }).catch(() => {
        return false
      }).then(success => {
        this.submitting = false
        return success
      })
    },
    shouldConfirmLeave() {
      return !this.flag && !this.allowLeave && this.assign.length > 0
    },
    isDeadlineMessage(message) {
      return message && message.indexOf('截止') !== -1
    },
    stopExamAndAllowLeave() {
      this.allowLeave = true
      clearTimeout(this.timeOut)
      window.removeEventListener('beforeunload', this.handleBeforeUnload)
    },
    handleBeforeUnload(e) {
      if (!this.shouldConfirmLeave()) {
        return
      }
      e.preventDefault()
      e.returnValue = ''
      return ''
    },
    countdown() {
      const now = Date.parse(new Date())
      const msec = this.end - now

      if(msec <= (10 * 60 * 1000) && !this.ten) {
        this.$message({ message: '考试时间还剩十分钟', type: 'warning', duration: 5000 });
        this.ten = true
      }

      if(msec <= (5 * 60 * 1000) && !this.five) {
        this.$message({ message: '考试时间还剩五分钟', type: 'warning', duration: 5000 });
        this.five = true
      }

      if(msec <= 0) {
        this.submitEnd()
        return
      }
      
      let hr = Math.floor(msec / 1000 / 60 / 60)
      let min = Math.floor(msec / 1000 / 60 % 60)
      let sec = Math.floor(msec / 1000 % 60)
      this.hr = hr > 9 ? hr : '0' + hr
      this.min = min > 9 ? min : '0' + min
      this.sec = sec > 9 ? sec : '0' + sec
      
      this.timeOut = setTimeout(() => {
        this.countdown()
      }, 1000)
    },
    getApeTestItemByTestId() {
      getApeTestItemByTestId({ id: this.id }).then(res => {
        if (res.code == 1000) {
          this.assign = res.data.testItem
          this.test = res.data.test
          // 使用考试配置的时长，默认120分钟
          var totalMinutes = (this.test && this.test.totalTime) ? this.test.totalTime : 120
          this.end = Date.parse(new Date()) + totalMinutes * 60 * 1000
          for(let i = 0; i < this.assign.length; i++) {
            var item = this.assign[i]
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
        } else {
          this.$message({
            type: 'warning',
            message: res.message || '当前考试暂不能参加'
          })
          this.stopExamAndAllowLeave()
          this.$router.replace("/test")
          return
        }
        this.getTestUserState()
      })
    },
    getTestUserState() {
      getTestUserState({ id: this.id }).then(res => {
        if (res.code == 1000) {
          this.flag = true
          clearTimeout(this.timeOut)
        } else {
          this.countdown()
          this.flag = false
        }
      })
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    var id = this.$route.query.id
    if (id) {
      this.id = id
    }
    window.addEventListener('beforeunload', this.handleBeforeUnload)
    this.getApeTestItemByTestId()
  },
  beforeRouteLeave(to, from, next) {
    if (!this.shouldConfirmLeave()) {
      next()
      return
    }
    if (this.submitting) {
      this.$message.warning('正在交卷，请稍候...')
      next(false)
      return
    }
    this.$confirm('当前考试正在进行中，离开页面将自动交卷且无法继续修改，是否强制离开？', '关闭考试提醒', {
      confirmButtonText: '强制交卷并离开',
      cancelButtonText: '继续答题',
      type: 'warning',
      center: true
    }).then(() => {
      this.saveApeTestStudent({ redirect: false, silent: true }).then(success => {
        if (success || this.allowLeave) {
          next()
        } else {
          next(false)
        }
      })
    }).catch(() => {
      next(false)
    })
  },
  beforeDestroy() {
    clearTimeout(this.timeOut)
    window.removeEventListener('beforeunload', this.handleBeforeUnload)
  }
}
</script>

<style scoped>
@import url("../../assets/css/testP/testInfo.css");
</style>
