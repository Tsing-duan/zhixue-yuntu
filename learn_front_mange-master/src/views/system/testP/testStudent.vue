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
        <el-row class="action-toolbar"> </el-row>
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
          stripe
          style="width: 100%"
        >
          <el-table-column type="selection" width="55"> </el-table-column>
          <el-table-column prop="createBy" :label="$t('学生姓名')"> </el-table-column>
          <el-table-column prop="testName" :label="$t('考试名称')"> </el-table-column>
          <el-table-column prop="totalScore" :label="$t('总分')"> </el-table-column>
          <el-table-column prop="totalGetScore" :label="$t('得分')"> </el-table-column>
          <el-table-column :label="$t('状态')">
            <template #default="scope">
              {{ scope.row.remark === 'RETAKE_GRANT' ? $t("已授权补考") : $t("已交卷") }}
            </template>
          </el-table-column>
          <el-table-column :label="$t('操作')">
            <template #default="scope">
              <el-button
                class="row-action-btn row-warning-btn"
                size="small"

                :disabled="scope.row.remark === 'RETAKE_GRANT'"
                @click="yuejuan(scope.row.userId)"
                ><template #icon><i class="el-icon-edit-outline" aria-hidden="true"></i></template>{{ $t("阅卷") }}</el-button
              >
              <el-popconfirm
                style="margin-left: 5px"
                :confirm-button-text="$t('确认重置')"
                :cancel-button-text="$t('取消')"

                icon-color="red"
                :title="$t('确定要重置该学生的考试吗？此操作将清空该学生的所有答卷，不可恢复。')"
                @confirm="resetStudentTest(scope.row.userId)"
              ><template #icon><i class="el-icon-info" aria-hidden="true"></i></template>
                <template #reference><el-button
                  class="row-action-btn row-delete-btn"
                  size="small"


                  ><template #icon><i class="el-icon-refresh" aria-hidden="true"></i></template>{{ $t("重置") }}</el-button
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
    <el-dialog append-to-body
      :title="$t('阅卷')"
      v-model="dialogVisible"
      width="60%"
      :before-close="handleClose"
    >
      <div class="testInfo-item" v-for="(item, index) in assign" :key="index">
        <div style="margin-top: 10px; margin-left: 10px">
          {{ index + 1 }}.{{ item.title }}
          <span style="color: red">{{ $t("正确答案：") }}{{ item.answer }}</span>
          <span style="color: green; margin-left: 10px; width: 50px"
            >{{ $t("得分：") }}<el-input
              style="width: 100px"
              type="number"
              v-model="item.point"
              size="small"
            ></el-input
          ></span>
        </div>
        <div style="margin-top: 10px; margin-left: 10px; margin-bottom: 10px">
          <el-radio-group
            disabled
            v-model="item.solution"
            v-if="item.type == 0"
          >
            <el-radio v-for="(e, s) in item.content" :key="s" :value="e.value"
              >{{ e.value }}.{{ e.option }}</el-radio
            >
          </el-radio-group>
          <el-checkbox-group
            disabled
            v-model="item.solution"
            v-if="item.type == 1"
          >
            <el-checkbox
              v-for="(e, s) in item.content"
              :key="s"
              :value="e.value"
              >{{ e.value }}.{{ e.option }}</el-checkbox
            >
          </el-checkbox-group>
          <el-input
            disabled
            v-model="item.solution"
            v-if="item.type == 2"
            size="small"
            :placeholder="$t('请输入答案')"
          ></el-input>
          <el-radio-group
            disabled
            v-model="item.solution"
            v-if="item.type == 3"
          >
            <el-radio value="正确">{{ $t("正确") }}</el-radio>
            <el-radio value="错误">{{ $t("错误") }}</el-radio>
          </el-radio-group>
          <el-input
            type="textarea"
            disabled
            v-model="item.solution"
            v-if="item.type == 4"
            size="small"
            :placeholder="$t('请输入答案')"
          ></el-input>
          <el-input
            type="textarea"
            disabled
            v-model="item.solution"
            v-if="item.type == 5"
            size="small"
            :placeholder="$t('请输入答案')"
          ></el-input>
        </div>
      </div>
      <template #footer><span  class="dialog-footer">
        <el-button size="small" @click="handleClose">{{ $t("取 消") }}</el-button>
        <el-button size="small" type="primary" @click="submit">{{ $t("确 定") }}</el-button>
      </span></template>
    </el-dialog>
  </div>
</template>

<script>
import { t } from '@/i18n'

import {
  getTestStudent,
  getTestStudentItem,
  editApeTestStudent,
  resetTestStudent,
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
      dialogVisible: false,
      search: {
        testId: "",
        userName: "",
        pageNumber: 1,
        pageSize: 10,
      },
      assign: [],
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
    submit() {
      var param = {
        list: this.assign,
      };
      editApeTestStudent(param).then((res) => {
        if (res.code == 1000) {
          this.$notify.success({
            get title() { return t("成功") },
            get message() { return t("阅卷成功") },
          });
          this.handleClose();
          this.query();
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    query() {
      getTestStudent(this.search).then((res) => {
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
    yuejuan(userId) {
      getTestStudentItem({ testId: this.search.testId, userId: userId }).then(
        (res) => {
          if (res.code == 1000) {
            this.assign = res.data;
            for (let i = 0; i < this.assign.length; i++) {
              var item = this.assign[i];
              if (item.type == "0" || item.type == "1") {
                if (item.content) {
                  item.content = JSON.parse(item.content);
                }
                if (item.type == 1) {
                  if (item.solution) {
                    item.solution = item.solution.split(",");
                  } else {
                    item.solution = [];
                  }
                }
              }
            }
          }
        },
      ).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
      this.dialogVisible = true;
    },
    resetStudentTest(userId) {
      resetTestStudent({ testId: this.search.testId, userId: userId }).then(
        (res) => {
          if (res.code == 1000) {
            this.$notify.success({
              get title() { return t("成功") },
              message: res.message || "考试已重置，已授权补考",
            });
            this.query();
          } else {
            this.$notify.error({
              get title() { return t("错误") },
              message: res.message,
            });
          }
        }
      ).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    handleClose() {
      this.dialogVisible = false;
      this.assign = [];
    },
    refresh() {
      this.search.userName = "";
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
  },
  mounted() {
    this.search.testId = this.$route.query.id;
    this.query();
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
.action-toolbar {
  padding-top: 10px;
  margin-left: 10px;
  min-height: 10px;
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
.testInfo-item {
  margin-bottom: 12px;
  border: 1px solid #e8e8e5;
  border-radius: 10px;
  background: #f8f8f6;
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
:deep(.el-pagination.is-background .el-pager li), :deep(.el-pagination.is-background .btn-next), :deep(.el-pagination.is-background .btn-prev) {
  background: #f3f3f0;
  color: #585854;
}
:deep(.el-pagination.is-background .el-pager li.active) {
  background: #2f3437;
  color: #ffffff;
}
</style>
