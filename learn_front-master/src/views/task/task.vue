<template>
  <div class="task">
    <headerPage></headerPage>
    <div class="task-page-content">
      <!-- 搜索区域 -->
      <div class="task-page-search">
        <div class="task-search-item">
          <div>{{ $t("课程名称") }}</div>
          <div>
            <el-input 
              size="small"
              v-model="search.name" 
              :placeholder="$t('输入课程名称')"
              clearable
            ></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <div>{{ $t("教师名称") }}</div>
          <div>
            <el-input 
              size="small"
              v-model="search.teacherName" 
              :placeholder="$t('输入教师名称')"
              clearable
            ></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <div>{{ $t("专业名称") }}</div>
          <div>
            <el-input 
              size="small"
              v-model="search.major" 
              :placeholder="$t('输入专业名称')"
              clearable
            ></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <div>{{ $t("简介内容") }}</div>
          <div>
            <el-input 
              size="small"
              v-model="search.taskDescribe" 
              :placeholder="$t('输入课程简介')"
              clearable
            ></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <el-button  size="small" type="primary" plain @click="searchPage()"><template #icon><i class="el-icon-search" aria-hidden="true"></i></template>{{ $t("搜索课程") }}</el-button>
          <el-button  size="small" type="warning" plain @click="refresh()"><template #icon><i class="el-icon-refresh" aria-hidden="true"></i></template>{{ $t("重置筛选") }}</el-button>
        </div>
        <div class="task-search-item">
          <span>{{ $t("热门分类") }}</span>
          <span 
            class="task-search-type" 
            @click="searchClass($demoSource(item, 'name'))" 
            v-for="(item,index) in classification" 
            :key="index"
            :class="{ 'active-type': search.classification === item.name }"
          >{{item.name}}</span>
        </div>
      </div>
      
      <!-- 课程列表标题 -->
      <div class="task-font">
        <i class="title-icon el-icon-reading"></i>{{ $t("探索精品课程") }}<span class="task-font-count">{{ $t("共") }}{{ total }}{{ $t("门课程") }}</span>
      </div>
      
      <!-- 课程网格 -->
      <div class="task-page-list">
        <div 
          @click="toTaskInfo(item.id)" 
          v-for="(item,index) in tableData" 
          :key="index" 
          class="task-page-item"
        >
          <div class="task-page-image">
            <img v-if="item.image" :src="$config.HOST + item.image" :alt="item.name" loading="lazy">
          </div>
          <div class="task-card-body">
            <div class="task-card-label">{{item.classification || $t("精品课程")}}</div>
            <h3>{{item.name}}</h3>
            <p>{{item.taskDescribe || $t("暂无简介")}}</p>
            <div class="task-card-meta">
              <span><i class="el-icon-user"></i>{{item.teacherName || $t("暂未设置")}}</span>
              <span><i class="el-icon-collection-tag"></i>{{item.major || $t("通用方向")}}</span>
            </div>
          </div>
          <div class="task-card-footer">
            <span><i class="el-icon-user"></i>{{item.num || 0}}{{ $t("人学习") }}</span>
            <span><i class="el-icon-star-off"></i>{{item.favorCount || 0}}{{ $t("收藏") }}</span>
            <el-button @click.stop="toTaskInfo(item.id)" size="small" type="success" plain>{{ $t("查看课程") }}</el-button>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="task-page-fenye" v-if="total > 12">
        <el-pagination
          background
          v-model:page-size="search.pageSize"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
    </div>
    
    <!-- 底部装饰 -->
    <div class="task-page-bottom">
      <img src="../../assets/image/15.jpg" alt="" @error="$event.target.style.display='none'">
    </div>
    
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { getApeTaskPage, getApeClassificationList } from '../../api/api' 
import headerPage from "../../components/header/header.vue"
import bottomPage from "../../components/bottom/bottom.vue"

export default {
  name: 'Task',
  data() {
    return {
      search: {
        name: "",
        state: 0,
        teacherName: "",
        major: "",
        taskDescribe: "",
        classification: "",
        pageSize: 12,
        pageNumber: 1,
        type: 0,
      },
      tableData: [],
      classification: [],
      total: 0
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    searchPage() {
      this.search.pageNumber = 1
      this.query()
    },
    searchClass(name) {
      this.search.pageNumber = 1
      this.search.classification = name
      this.query()
    },
    query() {
      getApeTaskPage(this.search).then(res => {
        if(res.code == 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message
          });
        }
      }).catch(() => {
        this.$notify.error({
          get title() { return t("错误") },
          message: '获取课程列表失败，请稍后重试'
        });
      })
    },
    refresh() {
      this.search = {
        name: "",
        teacherName: "",
        major: "",
        state: 0,
        taskDescribe: "",
        classification: "",
        pageSize: 12,
        pageNumber: 1,
        type: 0,
      }
      this.query()
    },
    handleCurrentChange(val) {
      this.search.pageNumber = val
      this.query()
      window.scrollTo({ top: 400, behavior: 'smooth' })
    }, 
    toTaskInfo(id) {
      this.$router.push("taskInfo?id=" + id)
    },
    getApeClassificationList() {
      getApeClassificationList().then(res => {
        if(res.code == 1000) {
          this.classification = res.data
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
    }
  },
  mounted() {
    this.getApeClassificationList()
    window.scrollTo({ top: 0, behavior: 'smooth' });
    
    const name = this.$route.query.name
    if (name) {
      this.search.name = name
    }
    const major = this.$route.query.major
    if (major) {
      this.search.major = major
    }
    this.query()
  }
}
</script>

<style scoped>
@import url("../../assets/css/task/task.css");
</style>
