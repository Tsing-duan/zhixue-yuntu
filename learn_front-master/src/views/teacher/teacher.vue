<template>
  <div class="teacher"> 
    <headerPage></headerPage>
    <div class="teacher-page-content">
      <!-- 搜索区域 -->
      <div class="teacher-page-search">
        <!-- 第一行：教师名称搜索 + 按钮 -->
        <div class="search-row">
          <div class="search-input-group">
            <span class="label">教师名称</span>
            <el-input 
              size="mini" 
              v-model="search.userName" 
              placeholder="请输入教师名称"
              clearable
            ></el-input>
          </div>
          <div class="teacher-page-btns">
            <el-button icon="el-icon-search" size="mini" type="primary" plain @click="searchPage()">搜索教师</el-button>
            <el-button icon="el-icon-refresh" size="mini" type="warning" plain @click="refresh()">重置筛选</el-button>
          </div>
        </div>
        
        <!-- 第二行：学校分类 -->
        <div class="school-section">
          <span class="school-label">学校分类</span>
          <span 
            class="school-tag" 
            @click="searchSchool(item.name)" 
            v-for="(item,index) in school" 
            :key="index"
            :class="{ 'active-school': search.school === item.name }"
          >{{item.name}}</span>
        </div>
      </div>
      
      <!-- 页面标题 -->
      <div class="teacher-page-title">
        <i class="title-icon el-icon-user"></i>
        优秀教师团队
        <span style="font-size: 14px; color: #94a3b8; margin-left: 12px; font-weight: normal;">
          共 {{ total }} 位教师
        </span>
      </div>
      
      <!-- 教师列表网格 -->
      <div class="teacher-page-list">
        <div 
          @click="toTeacherInfo(item.id)" 
          v-for="(item,index) in tableData" 
          :key="index" 
          class="teacher-page-item"
        >
          <div class="teacher-avatar-wrap">
            <img
              :src="resolveAvatar(item)"
              :alt="item.userName"
              loading="lazy"
              @error="$avatarError($event, '/img/avatars/zhixue-teacher-default.png')">
          </div>
          <div class="teacher-card-content">
            <div class="teacher-card-head">
              <h3>{{item.userName}}</h3>
              <span>{{item.agree || '课程讲师'}}</span>
            </div>
            <p><i class="el-icon-school"></i>{{item.school || '暂无学校'}}</p>
            <p><i class="el-icon-phone-outline"></i>{{item.tel || '暂无电话'}}</p>
            <p><i class="el-icon-message"></i>{{item.email || '暂无邮箱'}}</p>
            <div class="teacher-card-footer">
              <span>查看详情</span>
              <i class="el-icon-right"></i>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="teacher-page-fenye" v-if="total > 12">
        <el-pagination
          background
          :page-size="search.pageSize"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
      
      <!-- 底部装饰 -->
      <div class="teacher-bottom-image">
        <img src="../../assets/image/17.jpg" alt="" @error="$event.target.style.display='none'">
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script> 
import { getUserPage, getApeSchoolList } from '../../api/api' 
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'Teacher',
  data() {
    return {
      search: {
        userName: "",
        userType: 1,
        school: "",
        pageSize: 12,
        pageNumber: 1
      },
      tableData: [],
      school: [],
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
    searchSchool(name) {
      this.search.pageNumber = 1
      this.search.school = name
      this.query()
    },
    query() {
      getUserPage(this.search).then(res => {
        if(res.code == 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
        } else {
          this.$notify.error({
            title: '错误',
            message: res.message
          });
        }
      }).catch(() => {
        this.$notify.error({
          title: '错误',
          message: '获取教师列表失败，请稍后重试'
        });
      })
    },
    refresh() {
      this.search = {
        userName: "",
        userType: 1,
        school: "",
        pageSize: 12,
        pageNumber: 1
      }
      this.query()
    },
    handleCurrentChange(val) {
      this.search.pageNumber = val
      this.query()
      window.scrollTo({ top: 400, behavior: 'smooth' })
    },
    toTeacherInfo(id) {
      this.$router.push("/teacherInfo?id=" + id)
    },
    resolveAvatar(item) {
      const avatar = item && item.avatar
      const url = this.$resolveAvatar(avatar, "/img/avatars/zhixue-teacher-default.png")
      const cacheKey = item && [item.id, item.updateTime, item.createTime, avatar].filter(Boolean).join("-")
      if (!cacheKey || url.indexOf("data:") === 0) {
        return url
      }
      return url + (url.indexOf("?") === -1 ? "?" : "&") + "v=" + encodeURIComponent(cacheKey)
    },
    getApeSchoolList() {
      getApeSchoolList().then(res => {
        if(res.code == 1000) {
          this.school = res.data
        } else {
          this.$notify.error({
            title: '错误',
            message: res.message
          });
        }
      })
    }
  },
  mounted() {
    this.getApeSchoolList()
    this.query()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}
</script>

<style scoped>
@import url("../../assets/css/teacher/teacher.css");
</style>
