<template>
  <div class="about">
    <headerPage />
    <main class="about-logos" :aria-label="$t('关于')">
      <div class="about-logo-pair">
        <img class="about-project-logo" src="../../assets/image/brand/zhixue-mark.svg" :alt="$t('智学云途品牌标记')" />
        <img class="about-personal-logo" src="../../assets/image/about/qingduan-ax.png" :alt="$locale === 'en' ? 'Qingduan AX' : '倾断 AX'" />
      </div>
      <div class="about-introduction">
        <p>{{ $locale === 'en' ? "Built on the original course team's work and maintained by Qingduan, Zhixue Cloud brings together courses, discussion and AI-assisted learning." : '智学云途由原课程小组共同建设，现由倾断持续维护，连接课程学习、交流与 AI 辅助。' }}</p>
        <a href="https://github.com/Tsing-duan/zhixue-yuntu/issues" target="_blank" rel="noopener noreferrer">{{ $locale === 'en' ? 'Feedback on GitHub' : '在 GitHub 反馈' }} <span aria-hidden="true">↗</span></a>
      </div>
    </main>
    <bottomPage />
  </div>
</template>

<script>
import headerPage from '../../components/header/header.vue'
import bottomPage from '../../components/bottom/bottom.vue'

export default {
  name: 'About',
  components: { headerPage, bottomPage },
  mounted() { window.scrollTo({ top: 0, behavior: 'smooth' }) }
}
</script>

<style scoped>
@import url('../../assets/css/about/about.css');
</style>
