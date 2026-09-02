<template>
  <div class="task">
    <headerPage></headerPage>
    <div class="task-page-content">
      <div class="task-font"><i class="title-icon el-icon-reading"></i>我的课程</div>
      <div class="task-page-list">
        <div @click="toTaskInfo(item.taskId)" v-for="(item,index) in tableData" :key="index" class="task-page-item">
          <div class="task-page-image">
            <img style="width:100%;height:100%" :src="$store.state.HOST + item.image">
          </div>
          <div class="task-page-name">
            <div class="task-page-name-one">{{item.taskName}}</div>
            <div class="task-page-name-two">{{item.taskDescribe}}</div>
          </div>
          <div class="task-page-user">
            <div>讲师：{{item.teacherName}}</div>
            <div>
              <span style="margin-right: 12px;">
                <i class="el-icon-user"></i> {{item.num || 0}}
              </span>
              <span>
                <i class="el-icon-star-off"></i> {{item.favorCount || 0}}
              </span>
            </div>
          </div>
        </div>
      </div>
      <div class="task-page-fenye">
        <el-pagination
          background
          :page-size="search.pageSize"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
    </div>
    <div class="task-page-bottom">
      <img style="width:70%" src="../../assets/image/49.png">
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script> 
  import {getApeMyTaskPage} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        search: {
          state: 0,
          pageSize: 12,
          pageNumber: 1
        },
        tableData: [],
        total: 100
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
      query() {
        getApeMyTaskPage(this.search).then(res => {
          if (res.code == 1000) {
            this.tableData = res.data.records
            this.total = res.data.total
          }
        })
      },
      handleCurrentChange(val) {
        this.search.pageNumber = val
        this.query()
      },
      toTaskInfo(id) {
        this.$router.push("taskInfo?id="+id)
      }
    },
    created() {
     
    },
    mounted() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
      this.query()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/task/task.css");
</style>
