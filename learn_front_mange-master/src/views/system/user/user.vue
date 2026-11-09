<template>
  <div class="user">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding: 10px">
          <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
            <span class="search-title" style="margin-left: 20px"
              >{{ $t("用户名称:") }}</span
            >
            <el-input
              style="margin-top: 10px; width: 250px; margin-left: 5px"
              size="small"
              :placeholder="$t('请输入用户名称')"
              v-model="search.userName"
            >
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
            <span class="search-title">{{ $t("手机号码:") }}</span>
            <el-input
              style="margin-top: 10px; width: 250px; margin-left: 5px"
              size="small"
              :placeholder="$t('请输入手机号码')"
              v-model="search.tel"
            >
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
            <span class="search-title">{{ $t("状态:") }}</span>
            <el-select
              clearable
              style="margin-top: 10px; margin-left: 5px"
              size="small"
              v-model="search.status"
              :placeholder="$t('请选择')"
            >
              <el-option :label="$t('正常')" value="0"> </el-option>
              <el-option :label="$t('停用')" value="1"> </el-option>
            </el-select>
          </el-col>
          <el-col
            class="search-action-col"
            :xs="24"
            :sm="12"
            :md="6"
            :lg="6"
            :xl="6"
          >
            <el-button
              class="search-query-btn"
              style="margin-top: 10px"
              size="small"

              type="primary"
              @click="searchPage"
              ><template #icon><i class="el-icon-search" aria-hidden="true"></i></template>{{ $t("查询") }}</el-button
            >
            <el-button
              class="search-reset-btn"
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

            @click="addUser()"
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

            @click="updateUserBtn"
            ><template #icon><i class="el-icon-edit" aria-hidden="true"></i></template>{{ $t("修改") }}</el-button
          >
          <!-- <el-button type="warning" size="mini" icon="el-icon-download" plain>导出</el-button> -->
        </el-row>
        <el-table
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
          <el-table-column :label="$t('用户名称')" width="150">
            <template #default="scope">
              <div style="display: flex; align-items: center">
                <img
                  style="width: 35px; height: 35px; border-radius: 50%"
                  :src="resolveAvatar(scope.row)"
                  @error="$avatarError($event, '/img/avatars/zhixue-default.png')"
                />
                <div style="margin-left: 10px">{{ scope.row.userName }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="loginAccount" :label="$t('登陆账号')" width="150">
          </el-table-column>
          <el-table-column prop="deptName" :label="$t('部门')" width="150">
          </el-table-column>
          <el-table-column prop="tel" :label="$t('手机号')" width="150">
          </el-table-column>
          <el-table-column prop="status" :label="$t('状态')" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.status == 0" type="success">{{ $t("正常") }}</el-tag>
              <el-tag v-if="scope.row.status == 1" type="danger">{{ $t("停用") }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" :label="$t('创建时间')">
          </el-table-column>
          <el-table-column :label="$t('操作')">
            <template #default="scope">
              <el-dropdown trigger="hover" @command="handleCommand">
                <span class="operation-dropdown-trigger">{{ $t("操作") }}<i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <template #dropdown><el-dropdown-menu >
                  <el-dropdown-item

                    :command="scope.row.id + '#edit'"
                    ><template #icon><i class="el-icon-edit" aria-hidden="true"></i></template>{{ $t("修改") }}</el-dropdown-item
                  >
                  <el-dropdown-item

                    v-if="scope.row.userType != 0 && scope.row.loginAccount !== 'admin'"
                    :command="scope.row.id + '#pass#' + scope.row.userName + '#' + scope.row.loginAccount"
                    ><template #icon><i class="el-icon-check" aria-hidden="true"></i></template>{{ $t("重置密码") }}</el-dropdown-item
                  >
                  <el-dropdown-item

                    v-if="scope.row.userType != 0 && scope.row.loginAccount !== 'admin'"
                    :command="scope.row.id + '#remove'"
                    ><template #icon><i class="el-icon-remove" aria-hidden="true"></i></template>{{ $t("删除") }}</el-dropdown-item
                  >
                </el-dropdown-menu></template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          background
          layout="total, prev, pager, next"
          :page-sizes="[20, 30, 50, 100]"
          v-model:page-size="search.pageSize"
          v-model:current-page="search.pageNumber"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :total="total"
        >
        </el-pagination>
      </div>
    </div>
    <add-user
      @addUserFalse="addUserFalse"
      :data="data"
      :post="post"
      :role="role"
      :addUserVisible="addUserVisible"
    ></add-user>
    <update-user
      @updateUserFalse="updateUserFalse"
      :updateId="updateId"
      :data="data"
      :post="post"
      :role="role"
      :updateUserVisible="updateUserVisible"
    ></update-user>
    <el-dialog append-to-body
      :title="$t('重置密码')"
      :model-value="passwordDialogVisible"
      width="30%"
      :before-close="handlePasswordClose"
    >
      <span>{{ $t("请输入") }}{{ userName }}{{ $t("的新密码：") }}</span>
      <el-input
        style="margin-top: 10px"
        show-password
        v-model="newPassword"
        size="small"
        autocomplete="off"
      ></el-input>
      <span>{{ $t("请再次输入") }}{{ userName }}{{ $t("的新密码：") }}</span>
      <el-input
        style="margin-top: 10px"
        show-password
        v-model="confirmPassword"
        size="small"
        autocomplete="off"
      ></el-input>
      <template #footer><span  class="dialog-footer">
        <el-button size="small" @click="handlePasswordClose">{{ $t("取 消") }}</el-button>
        <el-button size="small" type="primary" @click="passwordSubmit"
          >{{ $t("确 定") }}</el-button
        >
      </span></template>
    </el-dialog>
  </div>
</template>

<script>
import { t } from '@/i18n'

import {
  getDeptList,
  getUserPage,
  removeUser,
  getPostList,
  getRoleList,
  resetPassword,
} from "../../../api/api";
import addUser from "../../../components/system/user/addUser.vue";
import updateUser from "../../../components/system/user/updateUser.vue";
import { getPasswordError } from "../../../utils/accountPasswordRules";
export default {
  data() {
    return {
      loading: true,
      update: [],
      remove: [],
      updateId: "",
      addUserVisible: false,
      updateUserVisible: false,
      passwordDialogVisible: false,
      filterText: "",
      defaultProps: {
        children: "children",
        label: "deptName",
      },
      search: {
        userName: "",
        tel: "",
        status: "",
        deptId: "",
        userType: "",
        pageNumber: 1,
        pageSize: 10,
      },
      userName: "",
      userId: "",
      resetLoginAccount: "",
      newPassword: "",
      confirmPassword: "",
      total: 0,
      tableData: [],
      data: [],
      post: [],
      role: [],
    };
  },
  components: {
    addUser,
    updateUser,
  },
  methods: {
    searchPage() {
      this.search.pageNumber = 1;
      this.query();
    },
    resolveAvatar(row) {
      const avatar = row && row.avatar;
      const url = this.$resolveAvatar(avatar, "/img/avatars/zhixue-default.png");
      const cacheKey =
        row && [row.id, row.updateTime, row.createTime, avatar].filter(Boolean).join("-");
      if (!cacheKey || url.indexOf("data:") === 0) {
        return url;
      }
      return url + (url.indexOf("?") === -1 ? "?" : "&") + "v=" + encodeURIComponent(cacheKey);
    },
    getDept() {
      this.search.pageNumber = 1;
      getDeptList().then((res) => {
        if (res.code == 1000) {
          this.data = res.data;
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    getPost() {
      getPostList().then((res) => {
        if (res.code == 1000) {
          this.post = res.data;
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    getRole() {
      getRoleList().then((res) => {
        if (res.code == 1000) {
          this.role = res.data;
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.deptName.indexOf(value) !== -1;
    },
    query() {
      getUserPage(this.search).then((res) => {
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
      this.search.tel = "";
      this.search.status = "";
      this.search.deptId = "";
      this.search.userType = "";
      this.query();
    },
    handleNodeClick(data) {
      this.search.deptId = data.id;
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
    addUser() {
      this.addUserVisible = true;
    },
    addUserFalse() {
      this.addUserVisible = false;
      this.query();
    },
    updateUser(id) {
      this.updateId = id;
      this.updateUserVisible = true;
    },
    updateUserFalse() {
      this.updateUserVisible = false;
      this.updateId = "";
      this.query();
    },
    updateUserBtn() {
      this.updateUser(this.update[0]);
    },
    handleCommand(command) {
      var data = command.split("#");
      if (data[1] == "edit") {
        this.updateUser(data[0]);
      } else if (data[1] == "pass") {
        this.userName = data[2];
        this.resetLoginAccount = data[3] || "";
        this.openPassword(data[0]);
      } else {
        this.$confirm(t("确定删除选中的数据?"), t("提示"), {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        })
          .then(() => {
            this.deleteDate(data[0]);
          })
          .catch(() => {});
      }
    },
    deleteDateBtn() {
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
      removeUser({ ids: ids }).then((res) => {
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
    openPassword(id) {
      this.userId = id;
      this.passwordDialogVisible = true;
    },
    handlePasswordClose() {
      this.userName = "";
      this.userId = "";
      this.resetLoginAccount = "";
      this.newPassword = "";
      this.confirmPassword = "";
      this.passwordDialogVisible = false;
    },
    passwordSubmit() {
      if (this.resetLoginAccount !== "admin") {
        const passwordMessage = getPasswordError(this.newPassword);
        if (passwordMessage) {
          this.$message({ message: passwordMessage, type: "warning" });
          return;
        }
      }
      if (this.newPassword !== this.confirmPassword) {
        this.$message({
          get message() { return t("两次输入的新密码不一致") },
          type: "warning",
        });
        return;
      }
      resetPassword({ id: this.userId, newPassword: this.newPassword }).then(
        (res) => {
          if (res.code == 1000) {
            this.$notify.success({
              get title() { return t("成功") },
              get message() { return t("重置成功") },
            });
            this.handlePasswordClose();
          } else {
            this.$notify.error({
              get title() { return t("错误") },
              message: res.message,
            });
          }
        },
      ).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
  },
  mounted() {
    this.getDept();
    this.getPost();
    this.getRole();
    this.query();
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    },
  },
};
</script>

<style scoped>
.user {
  width: 100%;
  display: flex;
  color: #3f3f3c;
}
.search-table {
  width: 100%;
  padding-left: 20px;
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
  color: #ffffff;
}
.btn-create:hover,
.btn-create:focus {
  background: #3a4044;
  border-color: #3a4044;
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
.action-btn.is-disabled,
.action-btn.is-disabled:hover,
.action-btn.is-disabled:focus {
  background: #f3f3f0;
  border-color: #e1e1dc;
  color: #b0b0aa;
}
.operation-dropdown-trigger {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 8px;
  border: 1px solid #d9d9d4;
  background: #f5f5f2;
  color: #3f3f3c;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.operation-dropdown-trigger:hover {
  background: #ededea;
  border-color: #cfcfc8;
  color: #2f2f2c;
}
.el-col {
  display: flex;
  flex-direction: row;
  align-items: center;
}
.search-action-col {
  flex-wrap: nowrap;
  white-space: nowrap;
}
.search-action-col .el-button + .el-button {
  margin-left: 8px;
}
.search-query-btn {
  background: #2f3437;
  border-color: #2f3437;
  color: #ffffff;
}
.search-query-btn:hover,
.search-query-btn:focus {
  background: #3a4044;
  border-color: #3a4044;
}
.search-reset-btn {
  background: #f1f1ee;
  border-color: #d8d8d2;
  color: #5b5b56;
}
.search-reset-btn:hover,
.search-reset-btn:focus {
  background: #e9e9e5;
  border-color: #c7c7c0;
  color: #42423f;
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
  background: #ffffff;
}
:deep(.el-input__inner:focus), :deep(.el-select .el-input__inner:focus) {
  border-color: #bdbdb6;
}
:deep(.search-query-btn.el-button--primary) {
  background: #2f3437;
  border-color: #2f3437;
}
:deep(.search-query-btn.el-button--primary:hover), :deep(.search-query-btn.el-button--primary:focus) {
  background: #3a4044;
  border-color: #3a4044;
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
:deep(.el-tag) {
  background: #f1f1ee;
  border-color: #d8d8d2;
  color: #5b5b56;
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
  color: #ffffff;
}
:deep(.el-dropdown-menu__item:focus), :deep(.el-dropdown-menu__item:not(.is-disabled):hover) {
  background: #f1f1ee;
  color: #3f3f3c;
}
</style>
