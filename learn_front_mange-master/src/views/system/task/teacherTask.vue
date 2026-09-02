<template>
  <div class="_task">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding: 10px">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">课程名称:</span>
            <el-input
              style="margin-top: 10px"
              size="mini"
              placeholder="请输入课程名称"
              v-model="search.name"
            >
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">所属专业:</span>
            <el-select
              clearable
              style="margin-top: 10px"
              size="mini"
              v-model="search.major"
              placeholder="请选择"
            >
              <el-option
                v-for="(item, index) in major"
                :key="index"
                :label="item.name"
                :value="item.name"
              >
              </el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">所属分类:</span>
            <el-select
              clearable
              style="margin-top: 10px"
              size="mini"
              v-model="search.classification"
              placeholder="请选择"
            >
              <el-option
                v-for="(item, index) in classification"
                :key="index"
                :label="item.name"
                :value="item.name"
              >
              </el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-button
              style="margin-top: 10px"
              size="mini"
              icon="el-icon-search"
              type="primary"
              @click="searchPage"
              >查询</el-button
            >
            <el-button
              style="margin-top: 10px"
              size="mini"
              icon="el-icon-refresh"
              @click="refresh"
              >重置</el-button
            >
          </el-col>
        </el-row>
      </div>
      <div class="table">
        <el-row class="action-toolbar">
          <el-button
            class="action-btn btn-create"
            size="mini"
            icon="el-icon-plus"
            @click="add"
            >新增</el-button
          >
          <el-button
            :class="[
              'action-btn',
              'btn-edit',
              { 'btn-edit-active': update.length == 1 },
            ]"
            :disabled="update.length != 1 ? true : false"
            size="mini"
            icon="el-icon-edit"
            @click="updateDataBtn"
            >修改</el-button
          >
          <el-button
            class="action-btn btn-delete"
            :disabled="update.length <= 0 ? true : false"
            size="mini"
            icon="el-icon-delete"
            @click="deleteDataBtn"
            >删除</el-button
          >
        </el-row>
        <el-table
          v-loading="loading"
          :data="tableData"
          :header-cell-style="{
            color: '#3f3f3c',
            'background-color': '#f1f1ee',
            'border-bottom': '1px solid #e3e3e0',
          }"
          :row-style="{
            color: '#585854',
            'font-size': '15px',
            'font-family': '黑体',
            'white-space': 'nowrap',
          }"
          @selection-change="handleSelectionChange"
          stripe
          style="width: 100%"
        >
          <el-table-column type="selection" width="55"> </el-table-column>
          <el-table-column prop="name" label="课程名称" width="100">
          </el-table-column>
          <el-table-column prop="major" label="所属专业" width="100">
          </el-table-column>
          <el-table-column prop="classification" label="所属分类" width="80">
          </el-table-column>
          <el-table-column prop="teacherName" label="教师名称" width="80">
          </el-table-column>
          <el-table-column prop="state" label="状态" width="80">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.state == 0" type="success">已上架</el-tag>
              <el-tag v-if="scope.row.state == 1" type="warning">已下架</el-tag>
              <el-tag v-if="scope.row.state == 2" type="warning">待审核</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="num" label="学生数量" width="100">
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button
                class="row-action-btn row-warning-btn"
                size="mini"
                icon="el-icon-chat-dot-round"
                @click="toTaskComment(scope.row.id)"
                >评论</el-button
              >
              <el-button
                class="row-action-btn row-success-btn"
                size="mini"
                icon="el-icon-user"
                @click="toTaskStudent(scope.row.id)"
                >学生</el-button
              >
              <el-button
                class="row-action-btn row-primary-btn"
                size="mini"
                icon="el-icon-reading"
                @click="toTaskStudy(scope.row.id)"
                >学习情况</el-button
              >
              <el-button
                class="row-action-btn row-info-btn"
                size="mini"
                icon="el-icon-tickets"
                @click="toChapter(scope.row.id, scope.row.name)"
                >章节</el-button
              >
              <el-button
                class="row-action-btn row-warning-btn"
                size="mini"
                icon="el-icon-document"
                @click="toArticle(scope.row.id, scope.row.name)"
                >笔记</el-button
              >
              <el-button
                class="row-action-btn row-edit-btn"
                size="mini"
                icon="el-icon-edit"
                @click="updateData(scope.row.id)"
                >修改</el-button
              >
              <el-popconfirm
                style="margin-left: 5px"
                confirm-button-text="确认"
                cancel-button-text="取消"
                icon="el-icon-info"
                icon-color="red"
                title="确认删除选中的数据？"
                @confirm="deleteDate(scope.row.id)"
              >
                <el-button
                  class="row-action-btn row-delete-btn"
                  size="mini"
                  slot="reference"
                  icon="el-icon-delete"
                  >删除</el-button
                >
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="search.pageSize"
          :current-page="search.pageNumber"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :total="total"
        >
        </el-pagination>
      </div>
    </div>
    <add @addFalse="addFalse" flag="2" :addVisible="addVisible"></add>
    <update
      @updateFalse="updateFalse"
      flag="2"
      :updateId="updateId"
      :updateVisible="updateVisible"
    ></update>
  </div>
</template>

<script>
import {
  getApeTaskPage,
  removeApeTask,
  getApeClassificationList,
  getApeMajorList,
  editApeTask,
} from "../../../api/api";
import add from "../../../components/system/task/addTask";
import update from "../../../components/system/task/updateTask";
export default {
  data() {
    return {
      loading: true,
      update: [],
      remove: [],
      updateId: "",
      addVisible: false,
      updateVisible: false,
      search: {
        name: "",
        taskDescribe: "",
        teacherName: "",
        state: "",
        major: "",
        classification: "",
        pageNumber: 1,
        pageSize: 10,
        type: 1,
      },
      total: 0,
      tableData: [],
      major: [],
      classification: [],
    };
  },
  components: {
    add,
    update,
  },
  methods: {
    toTaskStudy(id) {
      var param = {
        name: "学习情况",
        url: "/studyInfo?id=" + id,
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push("/studyInfo?id=" + id);
      this.$store.commit("menu/setActiveMenu", "/studyInfo?id=" + id);
    },
    toTaskComment(id) {
      var param = {
        name: "评论列表",
        url: "/taskComment?id=" + id,
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push("/taskComment?id=" + id);
      this.$store.commit("menu/setActiveMenu", "/taskComment?id=" + id);
    },
    toTaskStudent(id) {
      var param = {
        name: "学生列表",
        url: "/taskStudent?id=" + id,
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push("/taskStudent?id=" + id);
      this.$store.commit("menu/setActiveMenu", "/taskStudent?id=" + id);
    },
    toArticle(id, name) {
      var param = {
        name: "学生笔记管理",
        url: "/teacherArticle?id=" + id + "&name=" + name + "&type=0",
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push(
        "/teacherArticle?id=" + id + "&name=" + name + "&type=0",
      );
      this.$store.commit(
        "menu/setActiveMenu",
        "/teacherArticle?id=" + id + "&name=" + name + "&type=0",
      );
    },
    toChapter(id, name) {
      var param = {
        name: "教师章节管理",
        url: "/teacherChapter?id=" + id + "&name=" + name + "&type=0",
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push(
        "/teacherChapter?id=" + id + "&name=" + name + "&type=0",
      );
      this.$store.commit(
        "menu/setActiveMenu",
        "/teacherChapter?id=" + id + "&name=" + name + "&type=0",
      );
    },
    listing(id) {
      var param = {
        id: id,
        state: 0,
      };
      editApeTask(param).then((res) => {
        if (res.code == 1000) {
          this.$notify.success({
            title: "成功",
            message: "上架成功",
          });
          this.query();
        } else {
          this.$notify.error({
            title: "错误",
            message: res.message,
          });
        }
      });
    },
    offShelf(id) {
      var param = {
        id: id,
        state: 1,
      };
      editApeTask(param).then((res) => {
        if (res.code == 1000) {
          this.$notify.success({
            title: "成功",
            message: "下架成功",
          });
          this.query();
        } else {
          this.$notify.error({
            title: "错误",
            message: res.message,
          });
        }
      });
    },
    getApeMajorList() {
      getApeMajorList().then((res) => {
        if (res.code == 1000) {
          this.major = res.data;
        } else {
          this.$notify.error({
            title: "错误",
            message: res.message,
          });
        }
      });
    },
    getApeClassificationList() {
      getApeClassificationList().then((res) => {
        if (res.code == 1000) {
          this.classification = res.data;
        } else {
          this.$notify.error({
            title: "错误",
            message: res.message,
          });
        }
      });
    },
    searchPage() {
      this.search.pageNumber = 1;
      this.query();
    },
    query() {
      getApeTaskPage(this.search).then((res) => {
        if (res.code == 1000) {
          this.tableData = res.data.records;
          this.total = res.data.total;
          this.loading = false;
        } else {
          this.$notify.error({
            title: "错误",
            message: res.message,
          });
        }
      });
    },
    refresh() {
      this.search.name = "";
      this.search.taskDescribe = "";
      this.search.teacherName = "";
      this.search.state = "";
      this.search.major = "";
      this.search.classification = "";
      this.query();
    },
    handleCurrentChange(val) {
      this.search.pageNumber = val;
      this.query();
    },
    handleSizeChange(val) {
      this.search.pageSize = val;
      this.query();
    },
    handleSelectionChange(val) {
      this.update = [];
      this.remove = [];
      for (let i = 0; i < val.length; i++) {
        var item = val[i];
        this.update.push(item.id);
        this.remove.push(item.id);
      }
    },
    add() {
      this.addVisible = true;
    },
    addFalse() {
      this.addVisible = false;
      this.query();
    },
    updateFalse() {
      this.updateId = "";
      this.updateVisible = false;
      this.query();
    },
    updateData(id) {
      this.updateId = id;
      this.updateVisible = true;
    },
    updateDataBtn() {
      this.updateData(this.update[0]);
    },
    deleteDataBtn() {
      this.$confirm("确定删除选中的" + this.remove.length + "条数据?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.deleteDate(this.remove.join(","));
        })
        .catch(() => {});
    },
    deleteDate(ids) {
      removeApeTask({ ids: ids }).then((res) => {
        if (res.code == 1000) {
          this.$message({
            type: "success",
            message: "删除成功!",
          });
          this.pageNumber = 1;
          this.query();
        } else {
          this.$notify.error({
            title: "错误",
            message: res.message,
          });
        }
      });
    },
  },
  mounted() {
    this.getApeMajorList();
    this.getApeClassificationList();
    this.query();
  },
};
</script>
<style scoped>
._task {
  color: #3f3f3c;
}
.search-table {
  width: 100%;
}
.search {
  background: #fcfcfb;
  border-radius: 12px;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
}
.table {
  background: #fcfcfb;
  border-radius: 12px;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
  margin-top: 10px;
}
.action-toolbar {
  padding-top: 10px;
  margin-left: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.action-btn {
  border-radius: 10px;
  font-weight: 600;
  border: 1px solid #d9d9d4;
  background: #f5f5f2;
  color: #3f3f3c;
  transition: all 0.2s ease;
}
.action-btn:hover,
.action-btn:focus {
  background: #ededea;
  border-color: #cfcfc8;
  color: #2f2f2c;
}
.btn-create {
  background: #2f3437;
  border-color: #2f3437;
  color: #fff;
}
.btn-create:hover,
.btn-create:focus {
  background: #3a4044;
  border-color: #3a4044;
  color: #fff;
}
.btn-edit {
  background: #f3f3f0;
  border-color: #d9d9d4;
  color: #3f3f3c;
}
.btn-edit-active {
  background: #e9f6ee;
  border-color: #8ec9a2;
  color: #2f7d50;
}
.btn-edit-active:hover,
.btn-edit-active:focus {
  background: #ddf1e5;
  border-color: #7fbe95;
  color: #256b43;
}
.btn-delete {
  background: #fff5f5;
  border-color: #efc9c9;
  color: #b54747;
}
.btn-delete:hover,
.btn-delete:focus {
  background: #ffefef;
  border-color: #e8b2b2;
  color: #a63f3f;
}
.row-action-btn {
  border-radius: 8px;
  font-weight: 600;
  border: 1px solid #d9d9d4;
  background: #f5f5f2;
  color: #3f3f3c;
  transition: all 0.2s ease;
}
.row-action-btn:hover,
.row-action-btn:focus {
  background: #ededea;
  border-color: #cfcfc8;
  color: #2f2f2c;
}
.row-primary-btn {
  background: #eef3fb;
  border-color: #bfd0ea;
  color: #32527a;
}
.row-primary-btn:hover,
.row-primary-btn:focus {
  background: #e4edf9;
  border-color: #aec5e0;
  color: #274766;
}
.row-success-btn {
  background: #e9f6ee;
  border-color: #8ec9a2;
  color: #2f7d50;
}
.row-success-btn:hover,
.row-success-btn:focus {
  background: #ddf1e5;
  border-color: #7fbe95;
  color: #256b43;
}
.row-warning-btn {
  background: #fdf4e7;
  border-color: #e8c89c;
  color: #9a6428;
}
.row-warning-btn:hover,
.row-warning-btn:focus {
  background: #fbeed9;
  border-color: #dfb982;
  color: #84541f;
}
.row-info-btn {
  background: #f1f1ee;
  border-color: #d8d8d2;
  color: #5b5b56;
}
.row-info-btn:hover,
.row-info-btn:focus {
  background: #e9e9e5;
  border-color: #c7c7c0;
  color: #42423f;
}
.row-edit-btn {
  background: #e9f6ee;
  border-color: #8ec9a2;
  color: #2f7d50;
}
.row-edit-btn:hover,
.row-edit-btn:focus {
  background: #ddf1e5;
  border-color: #7fbe95;
  color: #256b43;
}
.row-delete-btn {
  background: #fff5f5;
  border-color: #efc9c9;
  color: #b54747;
}
.row-delete-btn:hover,
.row-delete-btn:focus {
  background: #ffefef;
  border-color: #e8b2b2;
  color: #a63f3f;
}
.el-col {
  display: flex;
  flex-direction: row;
  align-items: center;
}
.search-title {
  font-family: "黑体";
  float: right;
  white-space: nowrap;
  font-size: 14px;
  color: #585854;
  margin-top: 10px;
  width: 63px;
  text-align: right;
}
.el-table {
  padding: 10px;
}
.el-dialog__header {
  border-bottom: 1px solid #e8e8e5 !important;
}
.el-dialog {
  border-radius: 12px !important;
}
::v-deep .el-input__inner,
::v-deep .el-select .el-input__inner {
  border-radius: 10px;
  border-color: #dfdfda;
  color: #3f3f3c;
  background: #fff;
}
::v-deep .el-input__inner:focus,
::v-deep .el-select .el-input__inner:focus {
  border-color: #bdbdb6;
}
::v-deep .el-table {
  background: transparent;
}
::v-deep .el-table th.el-table__cell {
  background: #f1f1ee !important;
}
::v-deep .el-table td.el-table__cell,
::v-deep .el-table tr {
  background: transparent;
}
::v-deep
  .el-table--striped
  .el-table__body
  tr.el-table__row--striped
  td.el-table__cell {
  background: #f8f8f6;
}
::v-deep .el-button--primary {
  background: #2f3437;
  border-color: #2f3437;
}
::v-deep .el-button--primary:hover,
::v-deep .el-button--primary:focus {
  background: #3a4044;
  border-color: #3a4044;
}
::v-deep .el-button--success {
  background: #f3f3f0;
  border-color: #d9d9d4;
  color: #3f3f3c;
}
::v-deep .el-button--success:hover,
::v-deep .el-button--success:focus {
  background: #ebebe7;
  border-color: #cfcfc8;
  color: #2f2f2c;
}
::v-deep .el-button--warning {
  background: #fdf4e7;
  border-color: #e8c89c;
  color: #9a6428;
}
::v-deep .el-button--warning:hover,
::v-deep .el-button--warning:focus {
  background: #fbeed9;
  border-color: #dfb982;
  color: #84541f;
}
::v-deep .el-button--info {
  background: #f1f1ee;
  border-color: #d8d8d2;
  color: #5b5b56;
}
::v-deep .el-button--info:hover,
::v-deep .el-button--info:focus {
  background: #e9e9e5;
  border-color: #c7c7c0;
  color: #42423f;
}
::v-deep .el-button--danger {
  background: #fff5f5;
  border-color: #efc9c9;
  color: #b54747;
}
::v-deep .el-button--danger:hover,
::v-deep .el-button--danger:focus {
  background: #ffefef;
  border-color: #e8b2b2;
}
::v-deep .el-tag--success {
  background: #e9f6ee;
  border-color: #8ec9a2;
  color: #2f7d50;
}
::v-deep .el-tag--warning {
  background: #fdf4e7;
  border-color: #e8c89c;
  color: #9a6428;
}
::v-deep .el-pagination.is-background .el-pager li,
::v-deep .el-pagination.is-background .btn-next,
::v-deep .el-pagination.is-background .btn-prev {
  background: #f3f3f0;
  color: #585854;
}
::v-deep .el-pagination.is-background .el-pager li.active {
  background: #2f3437;
  color: #fff;
}
</style>
