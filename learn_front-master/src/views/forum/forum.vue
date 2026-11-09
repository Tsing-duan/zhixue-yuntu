<template>
  <div class="notice">
    <headerPage></headerPage>
    <div class="notice-page-content">
      <!-- 页面标题 -->
      <div class="notice-page-title">
        <div class="notice-page-title-text">
          <i class="title-icon el-icon-chat-dot-round"></i>{{ $t("讨论交流") }}<span class="notice-page-count">{{ $t("共") }}{{ total }}{{ $t("个话题") }}</span>
        </div>
        <el-button class="notice-page-add"  type="success" plain @click="openAddDialog"><template #icon><i class="el-icon-plus" aria-hidden="true"></i></template>{{ $t("发起讨论") }}</el-button>
      </div>
      
      <!-- 讨论列表 -->
      <div class="notice-page-list">
        <div v-for="(item,index) in tableData" :key="index" class="notice-page-item" @click="toForumInfo(item.id)">
          <div>
            {{index + 1}}. {{item.name}} - {{item.createBy}}
          </div>
          <div>
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="notice-page-fenye" v-if="total > 10">
        <el-pagination
          background
          v-model:page-size="pageSize"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>

      <el-dialog append-to-body
        :title="$t('发起讨论')"
        width="520px"
        :model-value="addVisible"
        :before-close="closeAddDialog"
      >
        <el-form :model="addForm" :rules="rules" ref="forumForm" label-width="82px">
          <el-form-item :label="$t('讨论标题')" prop="name">
            <el-input v-model="addForm.name" maxlength="80" show-word-limit :placeholder="$t('请输入讨论标题')"></el-input>
          </el-form-item>
          <el-form-item :label="$t('讨论内容')" prop="content">
            <el-input
              type="textarea"
              v-model="addForm.content"
              maxlength="800"
              show-word-limit
              resize="none"
              rows="5"
              :placeholder="$t('把你的问题或观点写清楚，提交后等待管理员审核')"
            ></el-input>
          </el-form-item>
        </el-form>
        <template #footer><div  class="dialog-footer">
          <el-button size="small" @click="closeAddDialog">{{ $t("取 消") }}</el-button>
          <el-button size="small" type="success" @click="submitForum">{{ $t("提 交") }}</el-button>
        </div></template>
      </el-dialog>
      
      <!-- 底部装饰 -->
      <div class="notice-page-bottom">
        <img src="../../assets/image/20.jpg" alt="" @error="$event.target.style.display='none'">
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { t } from '@/i18n'

import { getApeForumPage, saveApeForum } from '../../api/api'
import headerPage from "../../components/header/header.vue"
import bottomPage from "../../components/bottom/bottom.vue"

export default {
  name: 'Forum',
  data() {
    return {
      pageSize: 10,
      pageNumber: 1,
      tableData: [],
      total: 0,
      addVisible: false,
      addForm: {
        name: "",
        content: ""
      },
      rules: {
        name: [
          { required: true, get message() { return t("请输入讨论标题") }, trigger: 'blur' },
          { min: 2, max: 80, get message() { return t("标题长度在 2 到 80 个字符") }, trigger: 'blur' }
        ],
        content: [
          { required: true, get message() { return t("请输入讨论内容") }, trigger: 'blur' },
          { min: 5, max: 800, get message() { return t("内容长度在 5 到 800 个字符") }, trigger: 'blur' }
        ]
      }
    }
  },
  components: {
    headerPage,
    bottomPage
  },
  methods: {
    toForumInfo(id) {
      this.$router.push("forumInfo?id=" + id)
    },
    openAddDialog() {
      this.addVisible = true
    },
    closeAddDialog() {
      this.addVisible = false
      this.addForm = {
        name: "",
        content: ""
      }
      this.$nextTick(() => {
        if (this.$refs.forumForm) {
          this.$refs.forumForm.clearValidate()
        }
      })
    },
    submitForum() {
      this.$refs.forumForm.validate((valid) => {
        if (!valid) {
          return
        }
        const param = {
          name: this.addForm.name.trim(),
          content: this.addForm.content.trim()
        }
        saveApeForum(param).then(res => {
          if (res.code == 1000) {
            this.$message({
              type: 'success',
              get message() { return t("提交成功，请等待管理员审核") }
            })
            this.closeAddDialog()
            this.query()
          } else {
            this.$notify.error({
              get title() { return t("错误") },
              message: res.message
            })
          }
        }).catch(() => {
          this.$message.error(t("提交失败，请稍后重试"))
        })
      })
    },
    query() {
      var param = {
        pageSize: this.pageSize,
        pageNumber: this.pageNumber,
        state: 0
      }
      getApeForumPage(param).then(res => {
        if(res.code == 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
        } else {
          this.$notify.error({
            get title() { return t("错误") },
            message: res.message
          });
        }
      }).catch(() => {
        this.$notify.error({
          get title() { return t("错误") },
          message: '获取讨论列表失败，请稍后重试'
        });
      })
    },
    handleCurrentChange(val) {
      this.pageNumber = val
      this.query()
      window.scrollTo({ top: 400, behavior: 'smooth' })
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.query()
  }
}
</script>

<style scoped>
@import url("../../assets/css/forum/forum.css");
</style>
