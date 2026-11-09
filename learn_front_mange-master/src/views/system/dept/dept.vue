<template>
  <div class="dept">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding: 10px">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">{{ $t("部门名称:") }}</span>
            <el-input
              style="margin-top: 10px"
              size="small"
              :placeholder="$t('请输入部门名称')"
              v-model="search.deptName"
            >
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">{{ $t("部门状态:") }}</span>
            <el-select
              clearable
              style="margin-top: 10px"
              size="small"
              v-model="search.status"
              :placeholder="$t('请选择')"
            >
              <el-option :label="$t('正常')" value="0"> </el-option>
              <el-option :label="$t('停用')" value="1"> </el-option>
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

            @click="addDept"
            ><template #icon><i class="el-icon-plus" aria-hidden="true"></i></template>{{ $t("新增") }}</el-button
          >
          <el-button
            class="action-btn btn-edit"
            size="small"

            @click="changeExpand"
            ><template #icon><i class="el-icon-sort" aria-hidden="true"></i></template>{{ $t("展开/折叠") }}</el-button
          >
        </el-row>
        <el-table
          v-if="refreshTable"
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
          row-key="id"
          :default-expand-all="expand"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          style="width: 100%"
        >
          <el-table-column prop="deptName" :label="$t('部门名称')" min-width="180">
          </el-table-column>
          <el-table-column prop="orderNum" :label="$t('排序')" width="90">
          </el-table-column>
          <el-table-column prop="leader" :label="$t('负责人')" width="140">
          </el-table-column>
          <el-table-column prop="phone" :label="$t('联系电话')" width="150">
          </el-table-column>
          <el-table-column prop="email" :label="$t('邮箱')" min-width="180">
          </el-table-column>
          <el-table-column prop="status" :label="$t('状态')" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.status == 0" type="success">{{ $t("正常") }}</el-tag>
              <el-tag v-if="scope.row.status == 1" type="danger">{{ $t("停用") }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" :label="$t('创建时间')" width="170">
          </el-table-column>
          <el-table-column :label="$t('操作')" width="260">
            <template #default="scope">
              <el-button
                class="row-action-btn row-add-btn"
                size="small"

                @click="addChildDept(scope.row)"
                ><template #icon><i class="el-icon-plus" aria-hidden="true"></i></template>{{ $t("新增") }}</el-button
              >
              <el-button
                class="row-action-btn row-edit-btn"
                size="small"

                @click="updateDept(scope.row.id)"
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
      </div>
    </div>
    <add-dept
      @addDeptFalse="addDeptFalse"
      :addDeptVisible="addDeptVisible"
      :dropData="tableData"
      :parentId="parentId"
      :disable="disable"
    ></add-dept>
    <update-dept
      @updateDeptFalse="updateDeptFalse"
      :updateDeptVisible="updateDeptVisible"
      :dropData="tableData"
      :updateId="updateId"
    ></update-dept>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { getDeptList, removeDept } from "../../../api/api";
import addDept from "../../../components/system/dept/addDept.vue";
import updateDept from "../../../components/system/dept/updateDept.vue";

export default {
  data() {
    return {
      loading: true,
      refreshTable: true,
      expand: true,
      addDeptVisible: false,
      updateDeptVisible: false,
      updateId: "",
      parentId: [],
      disable: false,
      search: {
        deptName: "",
        status: "",
      },
      tableData: [],
    };
  },
  components: {
    addDept,
    updateDept,
  },
  methods: {
    searchPage() {
      this.query();
    },
    query() {
      this.loading = true;
      getDeptList(this.search).then((res) => {
        if (res.code == 1000) {
          this.tableData = res.data || [];
          this.loading = false;
        } else {
          this.loading = false;
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    refresh() {
      this.search.deptName = "";
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
    addDept() {
      this.parentId = [];
      this.disable = false;
      this.addDeptVisible = true;
    },
    addChildDept(row) {
      var ids = [];
      if (row.idArrary && row.idArrary != "0") {
        ids = row.idArrary.split(",");
      }
      ids.push(row.id);
      this.parentId = ids;
      this.disable = true;
      this.addDeptVisible = true;
    },
    addDeptFalse() {
      this.addDeptVisible = false;
      this.parentId = [];
      this.disable = false;
      this.query();
    },
    updateDept(id) {
      this.updateId = id;
      this.updateDeptVisible = true;
    },
    updateDeptFalse() {
      this.updateId = "";
      this.updateDeptVisible = false;
      this.query();
    },
    deleteDate(id) {
      removeDept({ id: id }).then((res) => {
        if (res.code == 1000) {
          this.$message({
            type: "success",
            get message() { return t("删除成功!") },
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
  },
  mounted() {
    this.query();
  },
};
</script>

<style scoped>
.dept {
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
.row-add-btn {
  background: #eef3fb;
  border-color: #b9cff4;
  color: #2563eb;
}
.row-add-btn:hover,
.row-add-btn:focus {
  background: #dce7fa;
  border-color: #8db8e8;
  color: #1d4ed8;
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
  width: 70px;
  text-align: right;
}
.el-table {
  padding: 10px;
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
:deep(.el-table) {
  background: transparent;
}
:deep(.el-table th.el-table__cell) {
  background: #f1f1ee !important;
}
:deep(.el-table td.el-table__cell), :deep(.el-table tr) {
  background: transparent;
}
:deep(.el-button--primary) {
  background: #2f3437;
  border-color: #2f3437;
}
:deep(.el-button--primary:hover), :deep(.el-button--primary:focus) {
  background: #3a4044;
  border-color: #3a4044;
}
</style>
