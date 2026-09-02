<template>
  <div class="menu">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding: 10px">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">菜单名称:</span>
            <el-input
              style="margin-top: 10px"
              size="mini"
              placeholder="请输入菜单名称"
              v-model="search.menuName"
            >
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">菜单状态:</span>
            <el-select
              clearable
              style="margin-top: 10px"
              size="mini"
              v-model="search.status"
              placeholder="请选择"
            >
              <el-option label="正常" value="0"> </el-option>
              <el-option label="停用" value="1"> </el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-button
              style="margin-top: 10px"
              size="mini"
              icon="el-icon-search"
              type="primary"
              @click="query()"
              >查询</el-button
            >
            <el-button
              style="margin-top: 10px"
              size="mini"
              icon="el-icon-refresh"
              @click="refresh()"
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
            @click="addMenu"
            >新增</el-button
          >
          <el-button
            class="action-btn btn-edit"
            size="mini"
            icon="el-icon-sort"
            @click="changeExpand"
            >展开/折叠</el-button
          >
        </el-row>

        <el-table
          v-if="refreshTable"
          v-loading="loading"
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
          :data="tableData"
          style="width: 100%; margin-bottom: 20px"
          row-key="id"
          :default-expand-all="expand"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        >
          <el-table-column prop="menuName" label="菜单名称" width="180">
          </el-table-column>
          <el-table-column prop="icon" label="图标" width="180">
            <template slot-scope="scope">
              <i :class="scope.row.icon"></i>
            </template>
          </el-table-column>
          <el-table-column prop="orderNum" label="排序"> </el-table-column>
          <el-table-column prop="perms" label="权限标识"> </el-table-column>
          <el-table-column prop="componentUrl" label="组件路径">
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.status == 0" type="success">正常</el-tag>
              <el-tag v-if="scope.row.status == 1" type="danger">停用</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间">
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-dropdown trigger="hover" @command="handleCommand">
                <span class="el-dropdown-link menu-action-link">
                  操作<i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item
                    icon="el-icon-plus"
                    :command="scope.row.idArrary + '#add#' + scope.row.id"
                    >新增</el-dropdown-item
                  >
                  <el-dropdown-item
                    icon="el-icon-edit"
                    :command="scope.row.id + '#edit'"
                    >修改</el-dropdown-item
                  >
                  <el-dropdown-item
                    icon="el-icon-delete"
                    :command="scope.row.id + '#remove'"
                    >删除</el-dropdown-item
                  >
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <add-menu
      @addMenuFalse="addMenuFalse"
      :disable="disable"
      :parentId="parentId"
      :dropData="tableData"
      :addMenuVisible="addMenuVisible"
    ></add-menu>
    <update-menu
      @updateMenuFalse="updateMenuFalse"
      :updateId="updateId"
      :dropData="tableData"
      :updateMenuVisible="updateMenuVisible"
    ></update-menu>
  </div>
</template>

<script>
import { getMenuList, removeMenu } from "../../../api/api";
import addMenu from "../../../components/system/menu/addMenu";
import updateMenu from "../../../components/system/menu/updateMenu";
export default {
  data() {
    return {
      loading: true,
      refreshTable: true,
      expand: true,
      addMenuVisible: false,
      updateMenuVisible: false,
      search: {
        menuName: "",
        status: "",
      },
      tableData: [],
      parentId: [],
      disable: false,
      updateId: "",
    };
  },
  components: {
    addMenu,
    updateMenu,
  },
  methods: {
    query() {
      getMenuList(this.search).then((res) => {
        if (res.code == 1000) {
          this.tableData = res.data;
          this.loading = false;
        } else {
          this.$notify.error({
            title: "错误",
            message: res.message,
          });
        }
      });
    },
    // 重置
    refresh() {
      this.search.menuName = "";
      this.search.status = "";
      this.query();
    },
    changeExpand() {
      this.refreshTable = false;
      this.expand = !this.expand;
      this.$nextTick(() => {
        this.refreshTable = true;
      });
    },
    addMenu() {
      this.addMenuVisible = true;
    },
    addMenuFalse() {
      this.addMenuVisible = false;
      this.parentId = [];
      this.disable = false;
      this.query();
    },
    updateMenu() {
      this.updateMenuVisible = true;
    },
    updateMenuFalse() {
      this.updateId = "";
      this.updateMenuVisible = false;
      this.query();
    },
    handleCommand(command) {
      var data = command.split("#");
      if (data[1] == "add") {
        var idArrary = data[0].split(",");
        var id = data[2];
        if (idArrary[0] != "0") {
          this.parentId = idArrary;
        }
        this.parentId.push(id);
        this.disable = true;
        this.addMenu();
      } else if (data[1] == "edit") {
        this.updateId = data[0];
        this.updateMenu();
      } else {
        this.deleteDate(data[0]);
      }
    },
    deleteDate(id) {
      this.$confirm("确定删除选中的数据?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          removeMenu({ id: id }).then((res) => {
            if (res.code == 1000) {
              this.$message({
                type: "success",
                message: "删除成功!",
              });
              this.query();
            } else {
              this.$notify.error({
                title: "错误",
                message: res.message,
              });
            }
          });
        })
        .catch(() => {});
    },
  },
  created() {},
  mounted() {
    this.query();
  },
};
</script>

<style scoped>
.menu {
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
  color: #ffffff;
}
.btn-create:hover,
.btn-create:focus {
  background: #3a4044;
  border-color: #3a4044;
  color: #ffffff;
}
.btn-edit {
  background: #f3f3f0;
  border-color: #d9d9d4;
  color: #3f3f3c;
}
.action-btn.is-disabled,
.action-btn.is-disabled:hover,
.action-btn.is-disabled:focus {
  background: #f3f3f0;
  border-color: #e1e1dc;
  color: #b0b0aa;
}
.menu-action-link {
  color: #3f3f3c;
  font-weight: 600;
}
.menu-action-link:hover {
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
::v-deep .el-tag--warning {
  background: #fdf4e7;
  border-color: #e8c89c;
  color: #9a6428;
}
::v-deep .el-tag--success {
  background: #e9f6ee;
  border-color: #8ec9a2;
  color: #2f7d50;
}
</style>
