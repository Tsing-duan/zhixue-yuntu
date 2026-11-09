<template>
  <div>
    <el-dialog append-to-body
      :title="$t('编辑考试')"
      width="40%"
      class="notion-dialog"
      :destroy-on-close="true"
      :model-value="updateVisible"
      :before-close="handleClose"
    >
      <el-form :model="form" :rules="rules" ref="ruleForm">
        <el-row :gutter="10">
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("考试名称:") }}</span>
            <div style="width: 100%">
              <el-form-item prop="name" style="margin-bottom: 0">
                <el-input
                  v-model="form.name"
                  size="small"
                  :placeholder="$t('请输入考试名称')"
                  autocomplete="off"
                ></el-input>
              </el-form-item>
            </div>
          </el-col>
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("所属课程:") }}</span>
            <div style="width: 100%">
              <el-form-item prop="taskId" style="margin-bottom: 0">
                <el-select
                  clearable
                  style="margin-top: 10px"
                  size="small"
                  v-model="form.taskId"
                  :placeholder="$t('请选择')"
                >
                  <el-option
                    v-for="(item, index) in task"
                    :key="index"
                    :label="item.name"
                    :value="item.id"
                  >
                  </el-option>
                </el-select>
              </el-form-item>
            </div>
          </el-col>
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("总分:") }}</span>
            <div style="width: 100%">
              <el-form-item prop="totalScore" style="margin-bottom: 0">
                <el-input
                  type="number"
                  v-model="form.totalScore"
                  size="small"
                  :placeholder="$t('请输入总分')"
                  autocomplete="off"
                ></el-input>
              </el-form-item>
            </div>
          </el-col>
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("考试时长:") }}</span>
            <div style="width: 100%">
              <el-form-item prop="totalTime" style="margin-bottom: 0">
                <el-input
                  type="number"
                  v-model="form.totalTime"
                  size="small"
                  :placeholder="$t('请输入考试时长')"
                  autocomplete="off"
                ></el-input>
              </el-form-item>
            </div>
          </el-col>
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("开始时间:") }}</span>
            <div style="width: 100%">
              <el-form-item prop="startTime" style="margin-bottom: 0">
                <el-date-picker
                  size="small"
                  v-model="form.startTime"
                  type="date"
                  value-format="YYYY-MM-DD"
                  :placeholder="$t('开始时间')"
                >
                </el-date-picker>
              </el-form-item>
            </div>
          </el-col>
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <span class="search-title">{{ $t("截止时间:") }}</span>
            <div style="width: 100%">
              <el-form-item prop="endTime" style="margin-bottom: 0">
                <el-date-picker
                  size="small"
                  v-model="form.endTime"
                  type="date"
                  value-format="YYYY-MM-DD"
                  :placeholder="$t('截止时间')"
                >
                </el-date-picker>
              </el-form-item>
            </div>
          </el-col>
          <el-col
            style="margin-top: 20px"
            :xs="24"
            :sm="24"
            :md="24"
            :lg="24"
            :xl="24"
          >
            <span class="search-title">{{ $t("备注:") }}</span>
            <div style="width: 100%">
              <el-form-item prop="remark" style="margin-bottom: 0">
                <el-input
                  type="textarea"
                  v-model="form.remark"
                  size="small"
                  :placeholder="$t('请输入备注')"
                  autocomplete="off"
                ></el-input>
              </el-form-item>
            </div>
          </el-col>
        </el-row>
      </el-form>
      <template #footer><div  class="dialog-footer">
        <el-button
          class="notion-primary-btn"
          size="small"
          type="primary"
          @click="submit"
          >{{ $t("确 定") }}</el-button
        >
        <el-button class="notion-ghost-btn" size="small" @click="handleClose"
          >{{ $t("取 消") }}</el-button
        >
      </div></template>
    </el-dialog>
  </div>
</template>

<script>
import { t } from '@/i18n'

import {
  editApeTest,
  getApeTestById,
  getApeTaskList,
  getApeTaskByTeacherId,
} from "../../../api/api";
export default {
  data() {
    return {
      formLabelWidth: "80px",
      form: {
        name: "",
        taskId: "",
        taskName: "",
        totalScore: "",
        totalTime: "",
        startTime: "",
        endTime: "",
        state: "",
        remark: "",
      },
      task: [],
      rules: {
        name: [{ required: true, get message() { return t("请输入考试名称") }, trigger: "blur" }],
        taskId: [{ required: true, get message() { return t("请选择课程") }, trigger: "blur" }],
        totalScore: [
          { required: true, get message() { return t("请输入总分") }, trigger: "blur" },
        ],
        totalTime: [
          { required: true, get message() { return t("请输入考试时长") }, trigger: "blur" },
        ],
        startTime: [
          { required: true, get message() { return t("请输入开始时间") }, trigger: "blur" },
        ],
        endTime: [
          { required: true, get message() { return t("请输入截止时间") }, trigger: "blur" },
        ],
      },
    };
  },
  props: ["updateVisible", "updateId", "flag"],
  methods: {
    submit() {
      this.$refs["ruleForm"].validate((valid) => {
        if (valid) {
          editApeTest(this.form).then((res) => {
            if (res.code == 1000) {
              this.$notify.success({
                get title() { return t("成功") },
                get message() { return t("保存成功") },
              });
              this.handleClose();
            } else {
              this.$notify.error({
                get title() { return t("错误") },
                message: res.message,
              });
            }
          }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
        } else {
          return false;
        }
      });
    },
    handleClose() {
      this.$emit("updateFalse");
    },
    getApeTaskList() {
      getApeTaskList().then((res) => {
        if (res.code == 1000) {
          this.task = res.data;
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
    getApeTaskByTeacherId() {
      getApeTaskByTeacherId().then((res) => {
        if (res.code == 1000) {
          this.task = res.data;
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message,
          });
        }
      }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
    },
  },
  created() {},
  mounted() {
    if (this.flag == 2) {
      this.getApeTaskByTeacherId();
    } else {
      this.getApeTaskList();
    }
  },
  watch: {
    updateId(newVal) {
      if (newVal) {
        getApeTestById({ id: newVal }).then((res) => {
          if (res.code == 1000) {
            this.form = res.data;
            this.form.state = res.data.state + "";
          } else {
            this.$notify.error({
              get title() { return t("错误") },
              message: res.message,
            });
          }
        }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });
      }
    },
  },
};
</script>

<style scoped>
:deep(.notion-dialog) {
  border-radius: 12px;
  border: 1px solid #e8e8e5;
  box-shadow: 0 8px 30px rgba(15, 15, 15, 0.08);
  background: #fcfcfb;
  overflow: hidden;
}
:deep(.notion-dialog .el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ecece8;
  background: #f7f7f5;
}
:deep(.notion-dialog .el-dialog__title) {
  color: #2f2f2c;
  font-size: 16px;
  font-weight: 600;
}
:deep(.notion-dialog .el-dialog__body) {
  padding: 16px 20px 10px;
  background: #fcfcfb;
}
:deep(.notion-dialog .el-dialog__footer) {
  padding: 12px 20px 16px;
  border-top: 1px solid #ecece8;
  background: #f9f9f8;
}
.el-col {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 12px;
}
.search-title {
  font-family: "黑体";
  float: right;
  white-space: nowrap;
  font-size: 14px;
  color: #5f5f5a;
  width: 84px;
  text-align: right;
}
.notion-primary-btn {
  background: #2f3437;
  border-color: #2f3437;
  color: #ffffff;
  border-radius: 8px;
}
.notion-primary-btn:hover,
.notion-primary-btn:focus {
  background: #3a4044;
  border-color: #3a4044;
  color: #ffffff;
}
.notion-ghost-btn {
  background: #f3f3f0;
  border-color: #dcdcd7;
  color: #3f3f3c;
  border-radius: 8px;
}
.notion-ghost-btn:hover,
.notion-ghost-btn:focus {
  background: #ebebe7;
  border-color: #cecec7;
  color: #2f2f2c;
}
:deep(.el-input__inner), :deep(.el-textarea__inner) {
  border-radius: 8px;
  border-color: #deded8;
  color: #3f3f3c;
  background: #ffffff;
}
:deep(.el-input__inner:focus), :deep(.el-textarea__inner:focus) {
  border-color: #bdbdb6;
}
:deep(.el-select) {
  width: 100%;
}
:deep(.el-date-editor.el-input), :deep(.el-date-editor.el-input__inner) {
  width: 100%;
}
:deep(.el-form-item__error) {
  color: #c04a4a;
}
.el-tree {
  border: 1px solid #bdc1c9;
}
</style>
