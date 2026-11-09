<template>
  <div class="notice">
    <headerPage></headerPage>
    <div class="notice-page-content">
      <!-- 公告内容卡片 -->
      <div class="notice-page-font">
        <div class="notice-page-desc" v-html="sanitizedNoticeContent">
        </div>
      </div>
      
      <!-- 页面标题 -->
      <div class="notice-page-title">
        <i class="title-icon el-icon-bell"></i>{{ $t("公告列表") }}<span style="font-size: 14px; color: #94a3b8; margin-left: 12px; font-weight: normal;">{{ $t("共") }}{{ total }}{{ $t("条公告") }}</span>
      </div>
      
      <!-- 公告列表 -->
      <div class="notice-page-list">
        <div 
          v-for="(item,index) in tableData" 
          :key="index" 
          class="notice-page-item"
          @click="getNoticeInfo(index)"
          :class="{ 'active-notice': notice.id === item.id }"
        >
          <div>
            <i class="el-icon-bell" style="margin-right: 12px; color: #20B486;"></i>
            {{index + 1}}. {{item.title}}
          </div>
          <div>
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="notice-page-fenye" v-if="total > 10">
        <el-pagination
          background
          v-model:page-size="pageSize"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
      
      <!-- 底部装饰 -->
      <div class="notice-page-bottom">
        <img src="../../assets/image/20.jpg" alt="" @error="$event.target.style.display='none'">
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { t } from '@/i18n'

import DOMPurify from 'dompurify'
import { getNoticePage } from '../../api/api'
import headerPage from "../../components/header/header.vue"
import bottomPage from "../../components/bottom/bottom.vue"

export default {
  name: 'Notice',
  data() {
    return {
      pageSize: 10,
      pageNumber: 1,
      tableData: [],
      total: 0,
      notice: {}
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  computed: {
    sanitizedNoticeContent() {
      return this.notice.content ? DOMPurify.sanitize(this.notice.content) : (this.tableData.length ? t("请选择公告查看内容") : t("暂无已发布公告"))
    }
  },
  methods: {
    getNoticeInfo(index) {
      this.notice = this.tableData[index] 
    },
    query() {
      var param = {
        pageSize: this.pageSize,
        pageNumber: this.pageNumber,
      }
      getNoticePage(param).then(res => {
        if(res.code == 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
          if(this.tableData.length > 0 && !this.notice.id) {
            this.notice = this.tableData[0]
          }
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message
          });
        }
      }).catch(() => {
        this.$notify.error({
          get title() { return t("错误") },
          message: '获取公告列表失败，请稍后重试'
        });
      })
    },
    handleCurrentChange(val) {
      this.pageNumber = val
      this.query()
      window.scrollTo({ top: 400, behavior: 'smooth' })
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

.notice-page-item.active-notice {
    border-color: #20B486;
    background: rgba(32, 180, 134, 0.05);
}

.notice-page-item.active-notice::before {
    opacity: 1;
}

.notice-page-item.active-notice > div:first-child {
    color: #20B486;
    font-weight: 600;
}
</style>
