<template>
  <div class="notice">
    <headerPage></headerPage>
    <div class="notice-page-content">
      <!-- 页面标题 -->
      <div class="notice-page-title">
        我的作业
        <span style="font-size: 14px; color: #94a3b8; margin-left: 12px; font-weight: normal;">
          共 {{ data.length }} 个作业
        </span>
      </div>
      
      <!-- 作业列表 -->
      <div class="notice-page-list">
        <div 
          @click="toAssignInfo(item.chapterId)" 
          v-for="(item,index) in data" 
          :key="index" 
          class="notice-page-item"
        >
          <div>
            <i class="el-icon-news" style="margin-right: 12px; color: #f59e0b;"></i>
            {{index + 1}}. {{item.taskName + ' - ' + item.chapterName + ' - 章节作业'}}
          </div>
          <div>
            <i class="el-icon-top-right"></i>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="data.length === 0" class="notice-page-empty">
        <i class="el-icon-folder-checked"></i>
        <p>暂无作业</p>
        <span>完成课程学习后，作业会在这里显示</span>
      </div>
      
      <!-- 底部装饰 -->
      <div class="notice-page-bottom">
        <img src="../../assets/image/45.jpg" alt="" @error="$event.target.style.display='none'">
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { getMyApeHomework } from "../../api/api"
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'MyAssign',
  data() {
    return {
      data: [],
      loading: false
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    query() {
      this.loading = true
      getMyApeHomework().then(res => {
        if (res.code == 1000 && res.data) {
          this.data = res.data
        }
      }).catch(() => {
        this.$notify.error({ title: '错误', message: '获取作业列表失败' })
      }).finally(() => {
        this.loading = false
      })
    },
    toAssignInfo(id) {
      this.$router.push("/assignInfo?id=" + id)
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.query()
  }
}
</script>

<style scoped>
@import url("../../assets/css/notice/notice.css");

/* 我的作业页面空状态样式 */
.notice-page-empty {
    width: 100%;
    max-width: 1280px;
    background: #ffffff;
    border-radius: 24px;
    border: 1px solid rgba(226, 232, 240, 0.6);
    padding: 60px 20px;
    text-align: center;
    margin-top: 20px;
}

.notice-page-empty i {
    font-size: 64px;
    color: #cbd5e1;
    margin-bottom: 20px;
    display: block;
}

.notice-page-empty p {
    font-size: 18px;
    font-weight: 600;
    color: #475569;
    margin-bottom: 8px;
}

.notice-page-empty span {
    font-size: 14px;
    color: #94a3b8;
}
</style>