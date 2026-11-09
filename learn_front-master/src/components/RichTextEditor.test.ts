// @vitest-environment jsdom
import { describe, it, expect, vi } from 'vitest'
import { createApp, defineComponent, h, nextTick, ref } from 'vue'
import Editor from './RichTextEditor.vue'
async function editor(html: string) {
  const container = document.createElement('div'); document.body.append(container)
  const value = ref(html); const onUpdate = vi.fn((v: string) => { value.value = v }); const component = ref<any>()
  const app = createApp(defineComponent({ setup: () => () => h(Editor, { ref: component, modelValue: value.value, 'onUpdate:modelValue': onUpdate }) }))
  app.mount(container); await nextTick()
  return { container, value, onUpdate, quill: component.value.quill, close: () => { app.unmount(); container.remove() } }
}
describe('legacy HTML compatibility', () => {
  it('does not rewrite unedited historical HTML or emit on initialization', async () => {
    const html = '<h2>旧标题</h2><p><strong>粗体</strong> 与 <em>斜体</em></p><ul><li>旧列表</li></ul><pre class="ql-syntax">const n = 1;</pre><p><img src="https://example.invalid/image.png"></p>'
    const e = await editor(html)
    expect(e.value.value).toBe(html); expect(e.onUpdate).not.toHaveBeenCalled()
    expect(e.container.textContent).toContain('旧列表'); expect(e.container.querySelector('img')?.getAttribute('src')).toBe('https://example.invalid/image.png')
    e.close()
  })
  it('updates after real editor changes and keeps formatting and images', async () => {
    const e = await editor('<p><strong>旧内容</strong></p><p><img src="https://example.invalid/image.png"></p>')
    e.quill.insertText(0, '新增', 'user'); await nextTick()
    expect(e.value.value).toContain('新增'); expect(e.value.value).toContain('<strong>'); expect(e.value.value).toContain('image.png')
    expect(e.onUpdate).toHaveBeenCalledTimes(1); e.close()
  })
  it('filters unsafe imported HTML without emitting an unsolicited save', async () => {
    const e = await editor('<p>safe</p><img src="x" onerror="alert(1)"><script>alert(1)</script>')
    expect(e.container.querySelector('[onerror],script')).toBeNull()
    expect(e.onUpdate).not.toHaveBeenCalled(); e.close()
  })
})
