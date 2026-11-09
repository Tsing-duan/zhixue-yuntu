<template>
  <div class="role">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding: 10px">
          <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
            <span class="search-title" style="margin-left: 20px"
              >{{ $t("角色名称:") }}</span
            >
            <el-input
              style="margin-top: 10px; width: 250px; margin-left: 5px"
              size="small"
              :placeholder="$t('请输入角色名称')"
              v-model="search.roleName"
            >
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
            <span class="search-title">{{ $t("权限字符:") }}</span>
            <el-input
              style="margin-top: 10px; width: 250px; margin-left: 5px"
              size="small"
              :placeholder="$t('请输入权限字符')"
              v-model="search.roleKey"
            >
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
            <span class="search-title">{{ $t("角色状态:") }}</span>
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

            @click="addRole"
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

            @click="updateRoleBtn"
            ><template #icon><i class="el-icon-edit" aria-hidden="true"></i></template>{{ $t("修改") }}</el-button
          >
          <el-button
            class="action-btn btn-delete"
            :disabled="update.length <= 0 ? true : false"
            size="small"

            @click="deleteDateBtn"
            ><template #icon><i class="el-icon-delete" aria-hidden="true"></i></template>{{ $t("删除") }}</el-button
          >
          <!-- <el-button type="warning" size="mini" icon="el-icon-download" plain>导出</el-button> -->
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
          <el-table-column prop="roleName" :label="$t('角色名称')" width="200">
          </el-table-column>
          <el-table-column prop="roleKey" :label="$t('权限标识')" width="200">
          </el-table-column>
          <el-table-column prop="status" :label="$t('状态')" width="200">
            <template #default="scope">
              <el-tag v-if="scope.row.status == 0" type="success">{{ $t("正常") }}</el-tag>
              <el-tag v-if="scope.row.status == 1" type="danger">{{ $t("停用") }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" :label="$t('创建时间')" width="250">
          </el-table-column>
          <el-table-column :label="$t('操作')" width="250">
            <template #default="scope">
              <el-button
                class="row-action-btn row-edit-btn"
                size="small"

                @click="updateRole(scope.row.id)"
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
    <add-role
      @addRoleFalse="addRoleFalse"
      :menu="menu"
      :addRoleVisible="addRoleVisible"
    ></add-role>
    <update-role
      @updateRoleFalse="updateRoleFalse"
      :updateId="updateId"
      :menu="menu"
      :updateRoleVisible="updateRoleVisible"
    ></update-role>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { getRolePage, removeRole, getMenuList } from "../../../api/api";
import addRole from "../../../components/system/role/addRole.vue";
import updateRole from "../../../components/system/role/updateRole.vue";
export default {
  data() {
    return {
      loading: true,
      update: [],
      remove: [],
      updateId: "",
      addRoleVisible: false,
      updateRoleVisible: false,
      search: {
        roleName: "",
        roleKey: "",
        status: "",
        pageNumber: 1,
        pageSize: 10,
      },
      total: 0,
      tableData: [],
      menu: [],
    };
  },
  components: {
    addRole,
    updateRole,
  },
  methods: {
    searchPage() {
      this.search.pageNumber = 1;
      this.query();
    },
    query() {
      getRolePage(this.search).then((res) => {
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
      this.search.roleName = "";
      this.search.roleKey = "";
      this.search.status = "";
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
    addRole() {
      this.addRoleVisible = true;
    },
    addRoleFalse() {
      this.addRoleVisible = false;
      this.query();
    },
    updateRole(id) {
      this.updateId = id;
      this.updateRoleVisible = true;
    },
    updateRoleFalse() {
      this.updateRoleVisible = false;
      this.updateId = "";
      this.query();
    },
    updateRoleBtn() {
      this.updateRole(this.update[0]);
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
      removeRole({ ids: ids }).then((res) => {
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
    getMenu() {
      getMenuList(this.search).then((res) => {
        if (res.code == 1000) {
          this.menu = res.data;
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
    this.query();
    this.getMenu();
  },
};
</script>

<style scoped>
.role {
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
  color: #fff;
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
  background: #fff;
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
:deep(.el-button--danger) {
  background: #fff5f5;
  border-color: #efc9c9;
  color: #b54747;
}
:deep(.el-button--danger:hover), :deep(.el-button--danger:focus) {
  background: #ffefef;
  border-color: #e8b2b2;
}
:deep(.el-tag--warning) {
  background: #fdf4e7;
  border-color: #e8c89c;
  color: #9a6428;
}
:deep(.el-tag--success) {
  background: #e9f6ee;
  border-color: #8ec9a2;
  color: #2f7d50;
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
