<template>
  <div class="_task">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding: 10px">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">{{ $t("课程名称:") }}</span>
            <el-input
              style="margin-top: 10px"
              size="small"
              :placeholder="$t('请输入课程名称')"
              v-model="search.name"
            >
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">{{ $t("所属专业:") }}</span>
            <el-select
              clearable
              style="margin-top: 10px"
              size="small"
              :model-value="$demoSource(search, 'major')" @update:model-value="search.major = $event"
              :placeholder="$t('请选择')"
            >
              <el-option
                v-for="(item, index) in major"
                :key="index"
                :label="item.name"
                :value="$demoSource(item, 'name')"
              >
              </el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">{{ $t("所属分类:") }}</span>
            <el-select
              clearable
              style="margin-top: 10px"
              size="small"
              :model-value="$demoSource(search, 'classification')" @update:model-value="search.classification = $event"
              :placeholder="$t('请选择')"
            >
              <el-option
                v-for="(item, index) in classification"
                :key="index"
                :label="item.name"
                :value="$demoSource(item, 'name')"
              >
              </el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-button
              style="margin-top: 10px"
              size="small"

              type="primary"
              @click="searchPage"
              ><template #icon><i class="el-icon-search" aria-hidden="true"></i></template>{{ $t("查询") }}</el-button
            >
            <el-button
              style="margin-top: 10px"
              size="small"

              @click="refresh"
              ><template #icon><i class="el-icon-refresh" aria-hidden="true"></i></template>{{ $t("重置") }}</el-button
            >
          </el-col>
        </el-row>
      </div>
      <div class="table">
        <el-row class="action-toolbar">
          <el-button
            class="action-btn btn-create"
            size="small"

            @click="add"
            ><template #icon><i class="el-icon-plus" aria-hidden="true"></i></template>{{ $t("新增") }}</el-button
          >
          <el-button
            :class="[
              'action-btn',
              'btn-edit',
              { 'btn-edit-active': update.length == 1 },
            ]"
            :disabled="update.length != 1 ? true : false"
            size="small"

            @click="updateDataBtn"
            ><template #icon><i class="el-icon-edit" aria-hidden="true"></i></template>{{ $t("修改") }}</el-button
          >
          <el-button
            class="action-btn btn-delete"
            :disabled="update.length <= 0 ? true : false"
            size="small"

            @click="deleteDataBtn"
            ><template #icon><i class="el-icon-delete" aria-hidden="true"></i></template>{{ $t("删除") }}</el-button
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
          <el-table-column prop="name" :label="$t('课程名称')" width="100">
          </el-table-column>
          <el-table-column prop="major" :label="$t('所属专业')" width="100">
          </el-table-column>
          <el-table-column prop="classification" :label="$t('所属分类')" width="80">
          </el-table-column>
          <el-table-column prop="teacherName" :label="$t('教师名称')" width="80">
          </el-table-column>
          <el-table-column prop="state" :label="$t('状态')" width="80">
            <template #default="scope">
              <el-tag v-if="scope.row.state == 0" type="success">{{ $t("已上架") }}</el-tag>
              <el-tag v-if="scope.row.state == 1" type="warning">{{ $t("已下架") }}</el-tag>
              <el-tag v-if="scope.row.state == 2" type="warning">{{ $t("待审核") }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="num" :label="$t('学生数量')" width="100">
          </el-table-column>
          <el-table-column :label="$t('操作')">
            <template #default="scope">
              <el-button
                class="row-action-btn row-warning-btn"
                size="small"

                @click="toTaskComment(scope.row.id)"
                ><template #icon><i class="el-icon-chat-dot-round" aria-hidden="true"></i></template>{{ $t("评论") }}</el-button
              >
              <el-button
                class="row-action-btn row-success-btn"
                size="small"

                @click="toTaskStudent(scope.row.id)"
                ><template #icon><i class="el-icon-user" aria-hidden="true"></i></template>{{ $t("学生") }}</el-button
              >
              <el-button
                class="row-action-btn row-primary-btn"
                size="small"

                @click="toTaskStudy(scope.row.id)"
                ><template #icon><i class="el-icon-reading" aria-hidden="true"></i></template>{{ $t("学习情况") }}</el-button
              >
              <el-button
                class="row-action-btn row-info-btn"
                size="small"

                @click="toChapter(scope.row.id, scope.row.name)"
                ><template #icon><i class="el-icon-tickets" aria-hidden="true"></i></template>{{ $t("章节") }}</el-button
              >
              <el-button
                class="row-action-btn row-warning-btn"
                size="small"

                @click="toArticle(scope.row.id, scope.row.name)"
                ><template #icon><i class="el-icon-document" aria-hidden="true"></i></template>{{ $t("笔记") }}</el-button
              >
              <el-button
                class="row-action-btn row-edit-btn"
                size="small"

                @click="updateData(scope.row.id)"
                ><template #icon><i class="el-icon-edit" aria-hidden="true"></i></template>{{ $t("修改") }}</el-button
              >
              <el-popconfirm
                style="margin-left: 5px"
                :confirm-button-text="$t('确认')"
                :cancel-button-text="$t('取消')"

                icon-color="red"
                :title="$t('确认删除选中的数据？')"
                @confirm="deleteDate(scope.row.id)"
              ><template #icon><i class="el-icon-info" aria-hidden="true"></i></template>
                <template #reference><el-button
                  class="row-action-btn row-delete-btn"
                  size="small"


                  ><template #icon><i class="el-icon-delete" aria-hidden="true"></i></template>{{ $t("删除") }}</el-button
                ></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          v-model:page-size="search.pageSize"
          v-model:current-page="search.pageNumber"
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
import { t } from '@/i18n'

import {
  getApeTaskPage,
  removeApeTask,
  getApeClassificationList,
  getApeMajorList,
  editApeTask,
} from "../../../api/api";
import add from "../../../components/system/task/addTask.vue";
import update from "../../../components/system/task/updateTask.vue";
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
      this.$menus.addActiveMenu(param);
      this.$router.push("/studyInfo?id=" + id);
      this.$menus.setActiveMenu("/studyInfo?id=" + id);
    },
    toTaskComment(id) {
      var param = {
        name: "评论列表",
        url: "/taskComment?id=" + id,
      };
      this.$menus.addActiveMenu(param);
      this.$router.push("/taskComment?id=" + id);
      this.$menus.setActiveMenu("/taskComment?id=" + id);
    },
    toTaskStudent(id) {
      var param = {
        name: "学生列表",
        url: "/taskStudent?id=" + id,
      };
      this.$menus.addActiveMenu(param);
      this.$router.push("/taskStudent?id=" + id);
      this.$menus.setActiveMenu("/taskStudent?id=" + id);
    },
    toArticle(id, name) {
      var param = {
        name: "学生笔记管理",
        url: "/teacherArticle?id=" + id + "&name=" + name + "&type=0",
      };
      this.$menus.addActiveMenu(param);
      this.$router.push(
        "/teacherArticle?id=" + id + "&name=" + name + "&type=0",
      );
      this.$menus.setActiveMenu("/teacherArticle?id=" + id + "&name=" + name + "&type=0",
      );
    },
    toChapter(id, name) {
      var param = {
        name: "教师章节管理",
        url: "/teacherChapter?id=" + id + "&name=" + name + "&type=0",
      };
      this.$menus.addActiveMenu(param);
      this.$router.push(
        "/teacherChapter?id=" + id + "&name=" + name + "&type=0",
      );
      this.$menus.setActiveMenu("/teacherChapter?id=" + id + "&name=" + name + "&type=0",
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
            get title() { return t("成功") },
            get message() { return t("上架成功") },
          });
          this.query();
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    offShelf(id) {
      var param = {
        id: id,
        state: 1,
      };
      editApeTask(param).then((res) => {
        if (res.code == 1000) {
          this.$notify.success({
            get title() { return t("成功") },
            get message() { return t("下架成功") },
          });
          this.query();
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    getApeMajorList() {
      getApeMajorList().then((res) => {
        if (res.code == 1000) {
          this.major = res.data;
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    getApeClassificationList() {
      getApeClassificationList().then((res) => {
        if (res.code == 1000) {
          this.classification = res.data;
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
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
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
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
      this.$confirm(t("确定删除选中的") + this.remove.length + t("条数据?"), t("提示"), {
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
            get message() { return t("删除成功!") },
          });
          this.pageNumber = 1;
          this.query();
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
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
:deep(.el-input__inner), :deep(.el-select .el-input__inner) {
  border-radius: 10px;
  border-color: #dfdfda;
  color: #3f3f3c;
  background: #fff;
}
:deep(.el-input__inner:focus), :deep(.el-select .el-input__inner:focus) {
  border-color: #bdbdb6;
}
:deep(.el-table) {
  background: transparent;
}
:deep(.el-table th.el-table__cell) {
  background: #f1f1ee !important;
}
:deep(.el-table td.el-table__cell), :deep(.el-table tr) {
  background: transparent;
}
:deep(.el-table--striped
  .el-table__body
  tr.el-table__row--striped
  td.el-table__cell) {
  background: #f8f8f6;
}
:deep(.el-button--primary) {
  background: #2f3437;
  border-color: #2f3437;
}
:deep(.el-button--primary:hover), :deep(.el-button--primary:focus) {
  background: #3a4044;
  border-color: #3a4044;
}
:deep(.el-button--success) {
  background: #f3f3f0;
  border-color: #d9d9d4;
  color: #3f3f3c;
}
:deep(.el-button--success:hover), :deep(.el-button--success:focus) {
  background: #ebebe7;
  border-color: #cfcfc8;
  color: #2f2f2c;
}
:deep(.el-button--warning) {
  background: #fdf4e7;
  border-color: #e8c89c;
  color: #9a6428;
}
:deep(.el-button--warning:hover), :deep(.el-button--warning:focus) {
  background: #fbeed9;
  border-color: #dfb982;
  color: #84541f;
}
:deep(.el-button--info) {
  background: #f1f1ee;
  border-color: #d8d8d2;
  color: #5b5b56;
}
:deep(.el-button--info:hover), :deep(.el-button--info:focus) {
  background: #e9e9e5;
  border-color: #c7c7c0;
  color: #42423f;
}
:deep(.el-button--danger) {
  background: #fff5f5;
  border-color: #efc9c9;
  color: #b54747;
}
:deep(.el-button--danger:hover), :deep(.el-button--danger:focus) {
  background: #ffefef;
  border-color: #e8b2b2;
}
:deep(.el-tag--success) {
  background: #e9f6ee;
  border-color: #8ec9a2;
  color: #2f7d50;
}
:deep(.el-tag--warning) {
  background: #fdf4e7;
  border-color: #e8c89c;
  color: #9a6428;
}
:deep(.el-pagination.is-background .el-pager li), :deep(.el-pagination.is-background .btn-next), :deep(.el-pagination.is-background .btn-prev) {
  background: #f3f3f0;
  color: #585854;
}
:deep(.el-pagination.is-background .el-pager li.active) {
  background: #2f3437;
  color: #fff;
}
</style>
