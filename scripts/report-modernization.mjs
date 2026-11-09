import fs from 'node:fs'
const root='output/playwright/modernization'
const results=JSON.parse(fs.readFileSync(root+'/results.json','utf8'))
if(results.stats.expected!==69||results.stats.unexpected||results.stats.flaky||results.stats.skipped)throw new Error('Only a complete, clean run can generate the acceptance checklist')
const old=JSON.parse(fs.readFileSync('docs/modernization/baseline-route-results.json','utf8'))
const browsers=['chromium','firefox','webkit']
const lines=['# 路由与功能验证清单','','生成自最近一次完整三浏览器回归。页面／新增表单检查与完整写入闭环分别记录；不能将静态保留当作全部业务验收。','','| 角色 | 路由 | Chromium | Firefox | WebKit | 旧版情况 |','| --- | --- | --- | --- | --- | --- |']
for(const role of ['student','teacher','admin']){
 const data=Object.fromEntries(browsers.map(browser=>[browser,JSON.parse(fs.readFileSync(`${root}/${browser}-${role}-routes.json`,'utf8'))]))
 for(const row of data.chromium.visited){
  const path=row.path.split('?')[0]
  const labels=browsers.map(browser=>{const entry=data[browser].visited.find(r=>r.path===row.path);return entry?.status==='baseline-permission-blocked'?'既有 403，反馈已检查':entry?.status==='page-and-form-checked'?'页面／表单通过':'未检查'})
  const prior=old[role].find(r=>r.path===path);const legacy=prior?.failedRequests.length?'既有接口拒绝访问':'页面存在'
  lines.push(`| ${role} | \`${path}\` | ${labels.join(' | ')} | ${legacy} |`)
 }
}
lines.push('','完整写入、权限、缓存、AI 模拟、媒体与响应式流程见 `tests/e2e/` 和 [验证报告](VERIFICATION.md)。登录、编辑笔记、401／403、窄屏弹窗由专门测试覆盖。','',`最近完整运行：${results.stats.startTime}；${results.stats.expected} 项通过，${results.stats.unexpected} 项失败。`,'','组件级事件绑定清单见 [component-actions.json](component-actions.json)：128 个原组件、709 个逐组件去重事件绑定静态保留。未逐按钮执行的次要操作不标作写入闭环通过。','')
fs.writeFileSync('docs/modernization/ROUTE_CHECKLIST.md',lines.join('\n'))
console.log('Wrote route checklist from',results.stats.expected,'passing browser cases')
