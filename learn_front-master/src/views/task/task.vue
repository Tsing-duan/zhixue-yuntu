<template>
  <div class="task">
    <headerPage></headerPage>
    <div class="task-page-content">
      <!-- 搜索区域 -->
      <div class="task-page-search">
        <div class="task-search-item">
          <div>课程名称</div>
          <div>
            <el-input 
              size="mini" 
              v-model="search.name" 
              placeholder="输入课程名称"
              clearable
            ></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <div>教师名称</div>
          <div>
            <el-input 
              size="mini" 
              v-model="search.teacherName" 
              placeholder="输入教师名称"
              clearable
            ></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <div>专业名称</div>
          <div>
            <el-input 
              size="mini" 
              v-model="search.major" 
              placeholder="输入专业名称"
              clearable
            ></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <div>简介内容</div>
          <div>
            <el-input 
              size="mini" 
              v-model="search.taskDescribe" 
              placeholder="输入课程简介"
              clearable
            ></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <el-button icon="el-icon-search" size="mini" type="primary" plain @click="searchPage()">搜索课程</el-button>
          <el-button icon="el-icon-refresh" size="mini" type="warning" plain @click="refresh()">重置筛选</el-button>
        </div>
        <div class="task-search-item">
          <span>热门分类</span>
          <span 
            class="task-search-type" 
            @click="searchClass(item.name)" 
            v-for="(item,index) in classification" 
            :key="index"
            :class="{ 'active-type': search.classification === item.name }"
          >{{item.name}}</span>
        </div>
      </div>
      
      <!-- 课程列表标题 -->
      <div class="task-font">
        <i class="title-icon el-icon-reading"></i>
        探索精品课程
        <span class="task-font-count">共 {{ total }} 门课程</span>
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
            <img :src="$store.state.HOST + item.image" :alt="item.name" loading="lazy">
          </div>
          <div class="task-card-body">
            <div class="task-card-label">{{item.classification || '精品课程'}}</div>
            <h3>{{item.name}}</h3>
            <p>{{item.taskDescribe || '暂无简介'}}</p>
            <div class="task-card-meta">
              <span><i class="el-icon-user"></i>{{item.teacherName || '暂未设置'}}</span>
              <span><i class="el-icon-collection-tag"></i>{{item.major || '通用方向'}}</span>
            </div>
          </div>
          <div class="task-card-footer">
            <span><i class="el-icon-user"></i>{{item.num || 0}} 人学习</span>
            <span><i class="el-icon-star-off"></i>{{item.favorCount || 0}} 收藏</span>
            <el-button @click.stop="toTaskInfo(item.id)" size="mini" type="success" plain>
              查看课程
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="task-page-fenye" v-if="total > 12">
        <el-pagination
          background
          :page-size="search.pageSize"
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
import { getApeTaskPage, getApeClassificationList } from '../../api/api' 
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

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
            title: '错误',
            message: res.message
          });
        }
      }).catch(() => {
        this.$notify.error({
          title: '错误',
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
            title: '错误',
            message: res.message
          });
        }
      })
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
