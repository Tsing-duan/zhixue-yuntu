<template>
  <div class="param">
      <div class="search-table">
        <div class="search">
            <el-row :gutter="10" style="padding:10px">
                <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                    <span class="search-title">参数名称:</span>
                    <el-input
                        style="margin-top:10px"
                        size="mini"
                        placeholder="请输入参数名称"
                        v-model="search.paramName">
                    </el-input>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                    <span class="search-title">参数键名:</span>
                    <el-input
                        style="margin-top:10px"
                        size="mini"
                        placeholder="请输入参数键名"
                        v-model="search.paramKey">
                    </el-input>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                    <span class="search-title">系统内置:</span>
                    <el-select clearable style="margin-top:10px" size="mini" v-model="search.within" placeholder="请选择">
                      <el-option
                        label="是"
                        value="0">
                      </el-option>
                      <el-option
                        label="否"
                        value="1">
                      </el-option>
                    </el-select>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                    <el-button style="margin-top:10px" size="mini" icon="el-icon-search" type="primary" @click="searchPage">查询</el-button>
                    <el-button style="margin-top:10px" size="mini" icon="el-icon-refresh" @click="refresh">重置</el-button>
                </el-col>
            </el-row>
        </div>
        <div class="table">
          <el-row class="action-toolbar">
            <el-button class="action-btn btn-create" size="mini" icon="el-icon-plus" @click="addParam">新增</el-button>
            <el-button :class="['action-btn', 'btn-edit', { 'btn-edit-active': update.length == 1 }]" :disabled="update.length != 1 ?true:false" size="mini" icon="el-icon-edit" @click="updateParamBtn">修改</el-button>
            <el-button class="action-btn btn-delete" :disabled="update.length <= 0 ?true:false" size="mini" icon="el-icon-delete" @click="deleteDateBtn">删除</el-button>
                <!-- <el-button type="warning" size="mini" icon="el-icon-download" plain>导出</el-button> -->
            </el-row>
            <el-table
            v-loading="loading"
            :data="tableData"
            :header-cell-style="{
              'color': '#3f3f3c',
              'background-color': '#f1f1ee',
              'border-bottom': '1px solid #e3e3e0',
            }"
            :row-style="{
              'color': '#585854',
              'font-size': '15px',
              'font-family':'黑体',
              'white-space': 'nowrap'
            }"
            @selection-change="handleSelectionChange"
            stripe
            style="width: 100%">
            <el-table-column
              type="selection"
              width="55">
            </el-table-column>
            <el-table-column
              prop="paramName"
              label="参数名称"
              width="180"
              >
            </el-table-column>
            <el-table-column
              prop="paramKey"
              label="参数键"
              width="180"
              >
            </el-table-column>
            <el-table-column
              prop="paramValue"
              label="参数值"
              width="180"
              >
            </el-table-column>
            <el-table-column
              prop="within"
              label="系统内置"
              width="180">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.within == 0">是</el-tag>
                <el-tag v-if="scope.row.within == 1" type="warning">否</el-tag>
              </template>
            </el-table-column>
            <el-table-column
              prop="createTime"
              label="创建时间"
              >
            </el-table-column>
            <el-table-column
              label="操作"
              width="180"
              >
              <template slot-scope="scope">
                <el-button class="row-action-btn row-edit-btn" size="mini" icon="el-icon-edit" @click="updateParam(scope.row.id)">修改</el-button>
                <el-popconfirm
                  style="margin-left:5px"
                  confirm-button-text='确认'
                  cancel-button-text='取消'
                  icon="el-icon-info"
                  icon-color="red"
                  title="确认删除选中的数据？"
                  @confirm="deleteDate(scope.row.id)"
                >
                  <el-button class="row-action-btn row-delete-btn" size="mini" slot="reference" icon="el-icon-delete">删除</el-button>
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
            :total="total">
          </el-pagination>
          </div>
        </div>
      <add-param @addParamFalse="addParamFalse" :addParamVisible = "addParamVisible"></add-param>
      <update-param @updateParamFalse="updateParamFalse" :updateId = "updateId" :updateParamVisible = "updateParamVisible"></update-param>
    </div>
  </div>
</template>

<script>
  import {getParamPage,removeParam} from '../../../api/api' 
  import addParam from '../../../components/system/param/addParam'
  import updateParam from '../../../components/system/param/updateParam'
  export default {
    data() {
      return{
        loading: true,
        update: [],
        remove: [],
        updateId: "",
        addParamVisible: false,
        updateParamVisible: false,
        search: {
          paramName: "",
          paramKey: "",
          within: "",
          pageNumber: 1,
          pageSize:10
        },
        total: 0,
        tableData: [],
      }
    },
    components: {
      addParam,
      updateParam
    },
    methods: {
      searchPage() {
        this.search.pageNumber = 1
        this.query()
      },
      query() {
        getParamPage(this.search).then(res => {
          if(res.code == 1000) {
            this.tableData = res.data.records
            this.total = res.data.total
            this.loading = false
          } else {
            this.$notify.error({
              title: '错误',
              message: res.message
            });
          }
        })
      },
      refresh() {
        this.search.paramName = ""
        this.search.paramName = ""
        this.search.within = ""
        this.query()
      },
      handleCurrentChange(val) {
        this.search.pageNumber = val
        this.query()
      },
      handleSizeChange(val) {
        this.search.pageSize = val
        this.query()
      },
      handleSelectionChange(val) {
        this.update = []
        this.remove = []
        for (let i = 0;i < val.length;i++) {
          var item = val[i]
          this.update.push(item.id)
          this.remove.push(item.id)
        }
      },
      addParam() {
        this.addParamVisible = true
      },
      addParamFalse() {
        this.addParamVisible = false
        this.query()
      },
      updateParamFalse() {
        this.updateId = ""
        this.updateParamVisible = false
        this.query()
      },
      updateParam(id) {
        this.updateId = id
        this.updateParamVisible = true
      },
      updateParamBtn() {
        this.updateParam(this.update[0])
      },
      deleteDateBtn() {
        this.$confirm('确定删除选中的'+ this.remove.length +'条数据?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.deleteDate(this.remove.join(","))
        }).catch(() => {
                 
        });
      },
      deleteDate(ids) {
        removeParam({ids:ids}).then(res => {
            if(res.code == 1000) {
              this.$message({
                type: 'success',
                message: '删除成功!'
              });
              this.pageNumber = 1
              this.query()
            } else {
              this.$notify.error({
                title: '错误',
                message: res.message
              });
            }
          })
      },
    },
    mounted() {
      this.query()
    }
 }
</script>

<style scoped>
  .param {
    color: #3f3f3c;
  }
  .search-table { width: 100%; }
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
  .action-btn:hover,.action-btn:focus { background: #ededea; border-color: #cfcfc8; color: #2f2f2c; }
  .btn-create { background: #2f3437; border-color: #2f3437; color: #fff; }
  .btn-create:hover,.btn-create:focus { background: #3a4044; border-color: #3a4044; color: #fff; }
  .btn-edit { background: #f3f3f0; border-color: #d9d9d4; color: #3f3f3c; }
  .btn-edit-active { background: #e9f6ee; border-color: #8ec9a2; color: #2f7d50; }
  .btn-edit-active:hover,.btn-edit-active:focus { background: #ddf1e5; border-color: #7fbe95; color: #256b43; }
  .btn-delete { background: #fff5f5; border-color: #efc9c9; color: #b54747; }
  .btn-delete:hover,.btn-delete:focus { background: #ffefef; border-color: #e8b2b2; color: #a63f3f; }
  .row-action-btn {
    border-radius: 8px;
    font-weight: 600;
    border: 1px solid #d9d9d4;
    background: #f5f5f2;
    color: #3f3f3c;
    transition: all 0.2s ease;
  }
  .row-action-btn:hover,.row-action-btn:focus { background: #ededea; border-color: #cfcfc8; color: #2f2f2c; }
  .row-edit-btn { background: #e9f6ee; border-color: #8ec9a2; color: #2f7d50; }
  .row-edit-btn:hover,.row-edit-btn:focus { background: #ddf1e5; border-color: #7fbe95; color: #256b43; }
  .row-delete-btn { background: #fff5f5; border-color: #efc9c9; color: #b54747; }
  .row-delete-btn:hover,.row-delete-btn:focus { background: #ffefef; border-color: #e8b2b2; color: #a63f3f; }
  .el-col { display: flex; flex-direction: row; align-items: center; }
  .search-title {
    font-family: '黑体';
    float: right;
    white-space: nowrap;
    font-size: 14px;
    color: #585854;
    margin-top:10px;
    width: 63px;
    text-align: right;
  }
  .el-table { padding: 10px; }
  .el-dialog__header { border-bottom: 1px solid #e8e8e5 !important; }
  .el-dialog { border-radius: 12px!important; }
  ::v-deep .el-input__inner,::v-deep .el-select .el-input__inner { border-radius: 10px; border-color: #dfdfda; color: #3f3f3c; background: #fff; }
  ::v-deep .el-input__inner:focus,::v-deep .el-select .el-input__inner:focus { border-color: #bdbdb6; }
  ::v-deep .el-table { background: transparent; }
  ::v-deep .el-table th.el-table__cell { background: #f1f1ee !important; }
  ::v-deep .el-table td.el-table__cell,::v-deep .el-table tr { background: transparent; }
  ::v-deep .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell { background: #f8f8f6; }
  ::v-deep .el-button--primary { background: #2f3437; border-color: #2f3437; }
  ::v-deep .el-button--primary:hover,::v-deep .el-button--primary:focus { background: #3a4044; border-color: #3a4044; }
  ::v-deep .el-button--success { background: #f3f3f0; border-color: #d9d9d4; color: #3f3f3c; }
  ::v-deep .el-button--success:hover,::v-deep .el-button--success:focus { background: #ebebe7; border-color: #cfcfc8; color: #2f2f2c; }
  ::v-deep .el-button--danger { background: #fff5f5; border-color: #efc9c9; color: #b54747; }
  ::v-deep .el-button--danger:hover,::v-deep .el-button--danger:focus { background: #ffefef; border-color: #e8b2b2; }
  ::v-deep .el-pagination.is-background .el-pager li,::v-deep .el-pagination.is-background .btn-next,::v-deep .el-pagination.is-background .btn-prev { background: #f3f3f0; color: #585854; }
  ::v-deep .el-pagination.is-background .el-pager li.active { background: #2f3437; color: #fff; }
</style>