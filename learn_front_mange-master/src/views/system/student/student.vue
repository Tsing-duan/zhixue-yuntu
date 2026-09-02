<template>
  <div class="user">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding: 10px">
          <el-col :xs="24" :sm="12" :md="4" :lg="4" :xl="4">
            <span class="search-title" style="margin-left: 10px">学生名称:</span>
            <el-input
              style="margin-top: 10px; margin-left: 5px"
              size="mini"
              placeholder="请输入学生名称"
              v-model="search.userName"
            >
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="4" :lg="4" :xl="4">
            <span class="search-title">学校:</span>
            <el-select
              clearable
              style="margin-top: 10px; margin-left: 5px"
              size="mini"
              v-model="search.school"
              placeholder="请选择"
            >
              <el-option
                v-for="(item, index) in school"
                :key="index"
                :label="item.name"
                :value="item.name"
              >
              </el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="4" :lg="4" :xl="4">
            <span class="search-title">专业:</span>
            <el-select
              clearable
              style="margin-top: 10px; margin-left: 5px"
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
          <el-col :xs="24" :sm="12" :md="4" :lg="4" :xl="4">
            <el-button
              style="margin-top: 10px; margin-left: 20px"
              size="mini"
              icon="el-icon-search"
              type="primary"
              @click="searchPage"
              >查询</el-button
            >
            <el-button
              style="margin-top: 10px; margin-left: 20px"
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
            @click="addUser()"
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
            @click="updateUserBtn"
            >修改</el-button
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
          <el-table-column label="用户名称">
            <template slot-scope="scope">
              <div style="display: flex; align-items: center">
                <img
                  style="width: 35px; height: 35px; border-radius: 50%"
                  :src="$resolveAvatar(scope.row.avatar, '/img/avatars/zhixue-student-default.png')"
                  @error="$avatarError($event, '/img/avatars/zhixue-student-default.png')"
                />
                <div style="margin-left: 10px">{{ scope.row.userName }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="loginAccount" label="登陆账号">
          </el-table-column>
          <el-table-column prop="country" label="国家"> </el-table-column>
          <el-table-column prop="school" label="学校"> </el-table-column>
          <el-table-column prop="major" label="专业"> </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-dropdown trigger="hover" @command="handleCommand">
                <span class="el-dropdown-link table-action-link">
                  操作<i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item
                    icon="el-icon-edit"
                    :command="scope.row.id + '#edit'"
                    >修改</el-dropdown-item
                  >
                  <el-dropdown-item
                    icon="el-icon-check"
                    v-if="scope.row.userType != 0"
                    :command="scope.row.id + '#pass#' + scope.row.userName"
                    >重置密码</el-dropdown-item
                  >
                  <el-dropdown-item
                    icon="el-icon-remove"
                    v-if="scope.row.userType != 0"
                    :command="scope.row.id + '#remove'"
                    >删除</el-dropdown-item
                  >
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          background
          layout="total, prev, pager, next"
          :page-sizes="[20, 30, 50, 100]"
          :page-size="search.pageSize"
          :current-page="search.pageNumber"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :total="total"
        >
        </el-pagination>
      </div>
    </div>
    <add-user
      @addUserFalse="addUserFalse"
      :addUserVisible="addUserVisible"
    ></add-user>
    <update-user
      @updateUserFalse="updateUserFalse"
      :updateId="updateId"
      :updateUserVisible="updateUserVisible"
    ></update-user>
    <el-dialog
      title="重置密码"
      :visible.sync="passwordDialogVisible"
      width="30%"
      :before-close="handlePasswordClose"
    >
      <span>请输入{{ userName }}的新密码：</span>
      <el-input
        style="margin-top: 10px"
        show-password
        v-model="newPassword"
        size="mini"
        autocomplete="off"
      ></el-input>
      <span>请再次输入{{ userName }}的新密码：</span>
      <el-input
        style="margin-top: 10px"
        show-password
        v-model="confirmPassword"
        size="mini"
        autocomplete="off"
      ></el-input>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="handlePasswordClose">取 消</el-button>
        <el-button size="mini" type="primary" @click="passwordSubmit"
          >确 定</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getUserPage,
  removeUser,
  resetPassword,
  getApeSchoolList,
  getApeMajorList,
} from "../../../api/api";
import addUser from "../../../components/system/student/addStudent";
import updateUser from "../../../components/system/student/updateStudent";
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
      search: {
        userName: "",
        tel: "",
        status: "",
        deptId: "",
        userType: 2,
        school: "",
        major: "",
        pageNumber: 1,
        pageSize: 10,
      },
      userName: "",
      userId: "",
      newPassword: "",
      confirmPassword: "",
      total: 0,
      tableData: [],
      post: [],
      role: [],
      school: [],
      major: [],
    };
  },
  components: {
    addUser,
    updateUser,
  },
  methods: {
    getApeSchoolList() {
      getApeSchoolList().then((res) => {
        if (res.code == 1000) {
          this.school = res.data;
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
    searchPage() {
      this.search.pageNumber = 1;
      this.query();
    },
    query() {
      getUserPage(this.search).then((res) => {
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
      this.search.userName = "";
      this.search.tel = "";
      this.search.status = "";
      this.search.deptId = "";
      this.search.school = "";
      this.search.major = "";
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
        this.openPassword(data[0]);
      } else {
        this.$confirm("确定删除选中的数据?", "提示", {
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
      removeUser({ ids: ids }).then((res) => {
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
    openPassword(id) {
      this.userId = id;
      this.passwordDialogVisible = true;
    },
    handlePasswordClose() {
      this.userName = "";
      this.userId = "";
      this.newPassword = "";
      this.confirmPassword = "";
      this.passwordDialogVisible = false;
    },
    passwordSubmit() {
      const passwordMessage = getPasswordError(this.newPassword);
      if (passwordMessage) {
        this.$message({ message: passwordMessage, type: "warning" });
        return;
      }
      if (this.newPassword !== this.confirmPassword) {
        this.$message({
          message: "两次输入的新密码不一致",
          type: "warning",
        });
        return;
      }
      resetPassword({ id: this.userId, newPassword: this.newPassword }).then(
        (res) => {
          if (res.code == 1000) {
            this.$notify.success({
              title: "成功",
              message: "重置成功",
            });
            this.handlePasswordClose();
          } else {
            this.$notify.error({
              title: "错误",
              message: res.message,
            });
          }
        },
      );
    },
  },
  mounted() {
    this.getApeSchoolList();
    this.getApeMajorList();
    this.query();
  },
  watch: {},
};
</script>

<style scoped>
.user {
  width: 100%;
  display: flex;
  color: #3f3f3c;
}
.tree {
  width: 20%;
  background: #fcfcfb;
  height: 83vh;
  overflow: scroll;
  border-radius: 12px;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
}
.tree::-webkit-scrollbar {
  width: 0px;
  height: 0px;
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
.table-action-link {
  color: #3f3f3c;
  font-weight: 600;
}
.table-action-link:hover {
  color: #2f3437;
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
  background: #ffffff;
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
::v-deep .el-dropdown-menu__item:hover,
::v-deep .el-dropdown-menu__item:focus {
  background: #f1f1ee;
  color: #2f3437;
}
::v-deep .el-dialog {
  border-radius: 12px;
}
::v-deep .el-pagination.is-background .el-pager li,
::v-deep .el-pagination.is-background .btn-next,
::v-deep .el-pagination.is-background .btn-prev {
  background: #f3f3f0;
  color: #585854;
}
::v-deep .el-pagination.is-background .el-pager li.active {
  background: #2f3437;
  color: #ffffff;
}
</style>
