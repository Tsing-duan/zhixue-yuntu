<template>
  <label class="language-switch">
    <span class="language-label">{{ $t('界面语言') }}</span>
    <select :value="locale" @change="change" aria-label="界面语言 / Interface language">
      <option value="zh-CN">{{ $t("中文") }}</option><option value="en">English</option>
    </select>
  </label>
</template>
<script setup lang="ts">
import { locale, setLocale } from '@/i18n'
function change(event: Event) { setLocale((event.target as HTMLSelectElement).value) }
</script>
<style scoped>
.language-switch{display:inline-flex;align-items:center;flex-shrink:0;gap:6px}.language-label{position:absolute;width:1px;height:1px;overflow:hidden;clip-path:inset(50%)}select{font:inherit;font-size:12px;color:#36566b;background:#fff;border:1px solid #d7e3ea;border-radius:9px;min-height:40px;max-width:88px;padding:0 5px;cursor:pointer}select:focus-visible{outline:3px solid #8eb7de;outline-offset:2px}
</style>
