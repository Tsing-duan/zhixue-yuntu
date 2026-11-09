<template>
  <div class="_student">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding: 10px">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">{{ $t("学生姓名:") }}</span>
            <el-input
              style="margin-top: 10px"
              size="small"
              :placeholder="$t('请输入学生姓名')"
              v-model="search.userName"
            >
            </el-input>
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
        <el-row style="padding-top: 10px; margin-left: 10px"> </el-row>
        <el-table
          v-loading="loading"
          :data="tableData"
          :header-cell-style="{
            color: '#4A2B90',
            'background-color': '#ECE9F4',
          }"
          :row-style="{
            color: '#888897',
            'font-size': '15px',
            'font-family': '黑体',
            'white-space': 'nowrap',
          }"
          @selection-change="handleSelectionChange"
          stripe
          style="width: 100%"
        >
          <el-table-column type="selection" width="55"> </el-table-column>
          <el-table-column prop="userName" :label="$t('学生姓名')"> </el-table-column>
          <el-table-column prop="taskName" :label="$t('课程名称')"> </el-table-column>
          <el-table-column :label="$t('章节名称')">
            <template #default="scope">
              {{ chapter.name }}
            </template>
          </el-table-column>
          <el-table-column :label="$t('视频')">
            <template #default="scope">
              {{ scope.row.video }}
            </template>
          </el-table-column>
          <el-table-column :label="$t('作业')">
            <template #default="scope">
              {{ scope.row.homework }}
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
  </div>
</template>

<script>
import { t } from '@/i18n'

import {
  getApeTaskStudentPage,
  getApeChapterById,
  getHomeworkStudentFlag,
  getTaskStudentPage,
} from "../../../api/api";
export default {
  data() {
    return {
      loading: true,
      update: [],
      remove: [],
      updateId: "",
      addVisible: false,
      updateVisible: false,
      chapterId: "",
      chapter: {},
      search: {
        chapterId: "",
        taskId: "",
        userName: "",
        state: "",
        pageNumber: 1,
        pageSize: 10,
      },
      total: 0,
      tableData: [],
    };
  },
  components: {},
  methods: {
    searchPage() {
      this.search.pageNumber = 1;
      this.query();
    },
    query() {
      getTaskStudentPage(this.search).then((res) => {
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
      this.search.userName = "";
      this.search.state = "";
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
  },
  mounted() {
    this.chapterId = this.$route.query.id;
    this.search.chapterId = this.$route.query.id;
    getApeChapterById({ id: this.chapterId }).then((res) => {
      if (res.code == 1000) {
        this.chapter = res.data;
        this.search.taskId = res.data.taskId;
        this.query();
      }
    }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
  },
};
</script>
<style scoped>
._student {
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
:deep(.el-input__inner) {
  border-radius: 10px;
  border-color: #dfdfda;
  color: #3f3f3c;
  background: #ffffff;
}
:deep(.el-input__inner:focus) {
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
:deep(.el-pagination.is-background .el-pager li), :deep(.el-pagination.is-background .btn-next), :deep(.el-pagination.is-background .btn-prev) {
  background: #f3f3f0;
  color: #585854;
}
:deep(.el-pagination.is-background .el-pager li.active) {
  background: #2f3437;
  color: #ffffff;
}
</style>
