<template><div v-if="isDemo" class="demo-notice" role="status"><strong>{{ $t('演示环境') }}</strong><span>{{ $t('演示内容为示例，不代表真实用户活动') }}</span></div></template>
<script setup lang="ts">import { isDemo } from '@/i18n/demo'</script>
<style scoped>.demo-notice{display:flex;align-items:center;justify-content:center;flex-wrap:wrap;gap:4px 10px;min-height:30px;box-sizing:border-box;padding:5px 12px;background:#fff7df;color:#79602e;font-size:11px;line-height:1.5;border-bottom:1px solid #efe5c8}.demo-notice strong{font-weight:650}</style>
