<template>
  <div class="about">
    <headerPage></headerPage>
    <div class="content">
      <div class="about-content">
        <div class="about-desc">
          <!-- 项目Logo -->
          <img class="project-logo" src="../../assets/image/brand/zhixue-mark.svg" alt="智学云途品牌标记">
          
          <!-- 标题区域 -->
          <div class="main-title-container">
            <span class="main-title">智学云途</span>
          </div>
          
          <!-- 分割线 -->
          <div class="divider"></div>
          
          <!-- 作者信息 -->
          <div class="author-info-section">
            <div class="info-item">
              <span class="info-label"><i class="el-icon-user"></i> 作者：</span>
              <span class="info-value">{{ siteInfo.authors }}</span>
            </div>
            <div class="info-item">
              <span class="info-label"><i class="el-icon-location"></i> 学校：</span>
              <span class="info-value">{{ siteInfo.school }}</span>
            </div>
            <div class="info-item">
              <span class="info-label"><i class="el-icon-school"></i> 院系：</span>
              <span class="info-value">{{ siteInfo.department }}</span>
            </div>
            <div class="info-item">
              <span class="info-label"><i class="el-icon-edit"></i> 专业：</span>
              <span class="info-value">{{ siteInfo.major }}</span>
            </div>
          </div>
          
          <!-- 版本信息 -->
          <div class="version-info">
            <span><i class="el-icon-info"></i> 智学云途 © 2026</span>
          </div>
        </div>
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: 'About',
  components: {
    headerPage,
    bottomPage
  },
  data() {
    return {
      siteInfo: {
        authors: 'RuoYi 开源社区、Tsing-duan',
        school: '开源教学系统基础',
        department: '作品化工程实践',
        major: 'Java / Vue 全栈'
      }
    }
  },
  mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}
</script>

<style scoped>
@import url("../../assets/css/about/about.css");
</style>
