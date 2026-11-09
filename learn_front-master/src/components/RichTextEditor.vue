<template><div class="rich-text-editor"><div ref="host" /></div></template>
<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch, toRaw } from 'vue'
import Quill from 'quill'
import DOMPurify from 'dompurify'
import type { QuillOptions } from 'quill'
const props = defineProps<{ modelValue?: string; options?: QuillOptions }>()
const emit = defineEmits<{ 'update:modelValue': [html: string]; change: [value: { html: string; text: string; quill: Quill }] }>()
const host = ref<HTMLElement>()
let quill: Quill | undefined
let applying = false
let lastEmitted: string | undefined
function apply(html: string) {
  if (!quill) return
  applying = true
  const selection = quill.getSelection()
  const delta = quill.clipboard.convert({ html: DOMPurify.sanitize(html) })
  quill.setContents(delta, 'silent')
  if (selection) quill.setSelection(Math.min(selection.index, Math.max(0,quill.getLength()-1)), 0, 'silent')
  applying = false
}
function changed() {
  if (!quill || applying) return
  lastEmitted = DOMPurify.sanitize(quill.getSemanticHTML())
  emit('update:modelValue', lastEmitted)
  emit('change', { html: lastEmitted, text: quill.getText(), quill })
}
onMounted(() => {
  quill = new Quill(host.value!, { theme: 'snow', ...toRaw(props.options || {}) })
  apply(props.modelValue || '')
  quill.on('text-change', changed)
})
watch(() => props.modelValue, value => { if (value !== lastEmitted) apply(value || '') })
onBeforeUnmount(() => { quill?.off('text-change', changed); quill?.disable(); quill = undefined })
// Preserve existing upload insertion callbacks without rewriting each business form.
defineExpose({ get quill() { return quill } })
</script>
<style scoped>.rich-text-editor { width: 100%; } :deep(.ql-editor) { min-height: 180px; }</style>
