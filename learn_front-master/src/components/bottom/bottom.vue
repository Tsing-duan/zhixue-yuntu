<template>
  <footer class="app-footer">
    <div class="footer-inner"><div class="footer-brand"><img src="../../assets/image/brand/zhixue-mark.svg" alt="" /><span>{{ $t("智学云途") }}</span><small>{{ $t("让每一步学习更清晰") }}</small></div>
      <nav :aria-label="$t('补充导航')"><router-link to="/message">{{ $t("留言反馈") }}</router-link><router-link to="/notice">{{ $t("公告") }}</router-link><router-link to="/about">{{ $t("关于项目") }}</router-link><router-link to="/center">{{ $t("个人中心") }}</router-link></nav>
    </div>
  </footer>
</template>
<script>
export default { name: 'Bottom' }
</script>
<style scoped>
.app-footer{border-top:1px solid #e0e6ee;background:#fff;color:#65768a;margin-top:24px}.footer-inner{max-width:1160px;margin:0 auto;padding:22px 28px;display:flex;align-items:center;justify-content:space-between;gap:24px}.footer-brand{display:flex;align-items:center;gap:10px;font-size:14px;font-weight:600}.footer-brand img{width:24px;height:24px}.footer-brand small{font-weight:400;font-size:12px;margin-left:8px;color:#8290a0}nav{display:flex;flex-wrap:wrap;gap:20px}a{color:#65768a;text-decoration:none;font-size:12px;padding:8px 0}a:hover{color:#245d98;text-decoration:underline}a:focus-visible{outline:3px solid #8eb7de;outline-offset:3px}@media(max-width:700px){.footer-inner{align-items:flex-start;flex-direction:column;gap:12px;padding:20px}.footer-brand small{display:none}}
</style>
