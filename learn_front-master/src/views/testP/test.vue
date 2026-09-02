<template>
  <div class="test">
    <headerPage></headerPage>
    <div class="test-page-content">
      <!-- 考前须知卡片 -->
      <div class="test-page-desc">
        <div class="test-page-image">
          <img src="../../assets/image/testP/Accent.png" alt="考试图标">
        </div>
        <div class="test-page-num">
          <div class="test-page-num1">我的考试</div>
          <div class="test-page-num2">
            <span>{{test.length}}</span> 场
          </div>
        </div>
        <div class="test-page-font">
          <div class="test-page-font1">
            <div>📋 考前须知：</div>
            <div>1、请提前进行基础设备检测，包括鼠标、键盘、显示器或其他硬件设备；</div>
            <div>2、请拔掉U盘、移动硬盘或其他与考试无关的外接设备；</div>
            <div>3、如果您使用的是移动电脑，请确保电源连接正常；</div>
            <div>4、请确保系统内未开启与考试无关的非必要软件；</div>
            <div>5、请确保当前使用的是谷歌浏览器，且页面（浏览器）是最大化状态；</div>
            <div>6、考试过程中，如非特殊要求，请勿执行任何会导致考试页面失焦的操作。</div>
          </div>
          <div class="test-page-font2">
            <div>⚠️ 注意事项：</div>
            <div>7、考试过程中，请勿修改浏览器和页面的大小；</div>
            <div>8、有些考试可能会禁用复制粘贴功能；</div>
            <div>9、如果有考试开启了防作弊视频监控，请提前完成摄像头检测工作；</div>
            <div>10、考试过程中请保持面部处于监控画面中心位置；</div>
            <div>11、如遇技术问题，请及时联系监考教师；</div>
            <div>12、祝您考试顺利，取得好成绩！</div>
          </div>
        </div>
      </div>
      
      <!-- 页面标题 -->
      <div class="test-page-title">
        <i class="title-icon el-icon-edit-outline"></i>
        考试中心
        <span style="font-size: 14px; color: #94a3b8; margin-left: 12px; font-weight: normal;">
          共 {{ test.length }} 场考试
        </span>
      </div>
      
      <!-- 考试列表网格 -->
      <div class="test-page-list">
        <div v-for="(item,index) in test" :key="index" class="test-page-item">
          <span>考试名称：{{item.name}}</span>
          <span>所属课程：{{item.taskName}}</span>
          <span>发布讲师：{{item.createBy}}</span>
          <span>考试时长：{{item.totalTime}} 分钟</span>
          <span>总分：{{item.totalScore}} 分</span>
          <span>开始时间：{{item.startTime}}</span>
          <span>截止时间：{{item.endTime}}</span>
          <span>考试进度：{{item.schedule}}</span>
          <span class="test-score">成绩：{{ formatScore(item) }}</span>
          <el-button 
            v-if="item.schedule == '未开始'" 
            @click="toTestInfo(item.id)" 
            icon="el-icon-edit" 
            size="mini" 
            type="success"
          >开始考试</el-button>
          <el-button 
            v-if="item.schedule == '已完成'" 
            @click="toTestInfo(item.id)" 
            icon="el-icon-view" 
            size="mini" 
            type="success"
          >查看详情</el-button>
          <el-button 
            v-if="item.schedule == '进行中'" 
            @click="toTestInfo(item.id)" 
            icon="el-icon-edit" 
            size="mini" 
            type="success"
          >继续考试</el-button>
          <el-button
            v-if="item.schedule == '可补考'"
            @click="toTestInfo(item.id)"
            icon="el-icon-refresh"
            size="mini"
            type="warning"
          >进入补考</el-button>
          <el-button
            v-if="item.schedule == '已截止'"
            icon="el-icon-lock"
            size="mini"
            type="info"
            disabled
          >已截止</el-button>
        </div>
      </div>
      
      <!-- 底部装饰 -->
      <div class="test-bottom-image">
        <img src="../../assets/image/16.jpg" alt="" @error="$event.target.style.display='none'">
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script> 
import { getTestListByUser } from '../../api/api' 
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'Test',
  data() {
    return {
      test: [],
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    getTestListByUser() {
      getTestListByUser().then(res => {
        if(res.code == 1000) {
          this.test = res.data
        } else {
          this.$notify.error({
            title: '错误',
            message: res.message
          });
        }
      }).catch(() => {
        this.$notify.error({
          title: '错误',
          message: '获取考试列表失败，请稍后重试'
        });
      })
    },
    toTestInfo(id) {
      this.$router.push("/testInfo?id=" + id)
    },
    formatScore(item) {
      if (!item || item.schedule === '未开始') {
        return '未开始'
      }
      if (item.schedule === '进行中') {
        return '进行中'
      }
      if (item.schedule === '可补考') {
        return '可补考'
      }
      if (item.schedule === '已截止') {
        return '已截止'
      }
      const score = item.scoreTotal === null || item.scoreTotal === undefined ? 0 : item.scoreTotal
      return score + ' 分'
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.getTestListByUser()
  }
}
</script>

<style scoped>
@import url("../../assets/css/testP/test.css");
</style>
