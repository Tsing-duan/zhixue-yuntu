if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import {chromium,webkit,expect} from '@playwright/test'
import fs from 'node:fs'
const state=JSON.parse(fs.readFileSync('.run/modernization/state.json','utf8'))
if(state.ports.backend!==18080 || !/^zhixue_migration_/.test(state.database))throw Error('Isolated preview required')
const out='output/playwright/student-access'
const report=[]
async function api(path,data,token) {const r=await fetch('http://127.0.0.1:18080'+path,{method:data===undefined?'GET':'POST',headers:{'Content-Type':'application/json',...(token?{x_access_token:token}:{})},body:data===undefined?undefined:JSON.stringify(data)});const body=await r.json();if(body.code!==1000)throw Error(JSON.stringify(body));return body.data}
const token=(await api('/login',{username:'portfolio_admin_01',password:process.env.ZHIXUE_DEMO_PASSWORD})).token
const fixtureId='layout-'+Date.now()
await api('/account/saveAccount',{id:fixtureId,title:'布局验证公告（临时样本）',state:0,type:0,content:'<p>学生可读取的临时验证内容</p>'},token)
try {
for(const [engine,browserType] of [['chromium',chromium],['webkit',webkit]]) {
 const browser=await browserType.launch()
 try {
  const page=await browser.newPage({viewport:{width:1440,height:1000}});const errors=[];page.on('pageerror',e=>errors.push(e.message))
  await page.goto('http://127.0.0.1:14001/login');await page.locator('input').first().fill('portfolio_student_01');await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD);await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click();await page.waitForURL(u=>u.pathname!='/login');await page.waitForLoadState('networkidle')
  await page.getByRole('navigation',{name:'主导航'}).getByRole('link',{name:'教师',exact:true}).click();await expect(page.locator('.teacher-page-item').first()).toBeVisible();await page.locator('.teacher-page-item').first().click();await expect(page.locator('.teacherInfo-title').first()).not.toBeEmpty();await page.waitForLoadState('networkidle');await page.screenshot({path:`${out}/${engine}-teacher-detail.png`,fullPage:true})
  await page.getByRole('navigation',{name:'主导航'}).getByRole('link',{name:'公告',exact:true}).click();await expect(page.locator('.notice-page-item').first()).toBeVisible();await expect(page.locator('.notice-page-desc')).not.toHaveText('请从右侧列表选择查看公告');await page.screenshot({path:`${out}/${engine}-notice.png`,fullPage:true})
  for(const [width,height,label] of [[1920,1080,'wide'],[1440,1000,'desktop'],[1400,900,'breakpoint'],[1280,900,'reported'],[1152,800,'125pct-equivalent'],[960,667,'150pct-equivalent'],[720,500,'200pct-equivalent'],[390,844,'mobile'],[320,740,'small-mobile']]) {
   await page.setViewportSize({width,height});await page.goto('http://127.0.0.1:14001/ai');await page.waitForLoadState('networkidle')
   if(width<1100)await page.getByRole('button',{name:'打开导航菜单'}).click()
   const bounds=await page.evaluate(()=>{const r=e=>e.getBoundingClientRect().toJSON();return {width:innerWidth,docWidth:document.documentElement.scrollWidth,docHeight:document.documentElement.scrollHeight,account:r(document.querySelector('.header .center')),header:r(document.querySelector('.header')),footer:r(document.querySelector('.app-footer')),links:[...document.querySelectorAll('.header .menu-list a')].map(r)}})
   expect(bounds.docWidth,`${engine} ${label} overflow`).toBeLessThanOrEqual(width+1)
   expect(bounds.account.right).toBeLessThanOrEqual(width);expect(bounds.account.left).toBeGreaterThanOrEqual(0)
   expect(bounds.links).toHaveLength(10);for(const link of bounds.links){expect(link.width).toBeGreaterThan(0);expect(link.right).toBeLessThanOrEqual(width);expect(link.left).toBeGreaterThanOrEqual(0)}
   expect(Math.abs(bounds.footer.bottom-Math.max(height,bounds.docHeight)),`${label} footer`).toBeLessThanOrEqual(2)
   const account=page.getByRole('button',{name:/账号菜单/});await account.click();await expect(page.getByRole('menuitem',{name:'个人中心'})).toBeVisible();await page.getByRole('menuitem',{name:'个人中心'}).click();await expect(page).toHaveURL(/\/center$/)
   await page.goto('http://127.0.0.1:14001/ai');await page.waitForLoadState('networkidle');await page.screenshot({path:`${out}/${engine}-${label}.png`,fullPage:true});report.push({engine,label,width,height,bounds})
  }
  await page.setViewportSize({width:1440,height:1200})
  for(const path of ['/','/ai','/teacher','/notice','/about','/forum']) {
   await page.goto('http://127.0.0.1:14001'+path);await page.waitForLoadState('networkidle')
   const layout=await page.evaluate(()=>({viewport:innerHeight,height:document.documentElement.scrollHeight,footer:document.querySelector('.app-footer').getBoundingClientRect().bottom,overflow:document.documentElement.scrollWidth>innerWidth}))
   expect(Math.abs(layout.footer-Math.max(layout.viewport,layout.height)),`${path} footer`).toBeLessThanOrEqual(2);expect(layout.overflow,`${engine} ${path} horizontal overflow`).toBe(false)
   report.push({engine,path,layout});await page.screenshot({path:`${out}/${engine}-page-${path.slice(1)||'home'}.png`,fullPage:true})
  }
  await page.goto('http://127.0.0.1:14001/ai');await page.waitForLoadState('networkidle');await page.getByRole('button',{name:'开始提问'}).click();await expect(page.getByRole('button',{name:'收起问答'})).toBeVisible();await page.getByRole('button',{name:'收起问答'}).click();await expect(page.getByRole('button',{name:'开始提问'})).toBeVisible()
  await page.goto('http://127.0.0.1:14001/');await page.waitForLoadState('networkidle')
  await page.getByRole('button',{name:/发现课程/}).click();await expect(page).toHaveURL(/\/task$/)
  await page.goto('http://127.0.0.1:14001/');await page.waitForLoadState('networkidle');await expect(page.locator('.note-avatar').first()).toBeVisible();await expect(page.locator('.note-author time').first()).toBeVisible()
  await page.getByRole('link',{name:'去讨论区交流学习中的问题 →'}).click();await expect(page).toHaveURL(/\/forum$/)
  const accountButton=page.getByRole('button',{name:/账号菜单/});await accountButton.focus();await accountButton.press('Enter');await expect(page.getByRole('menuitem',{name:'个人中心'})).toBeVisible();await page.keyboard.press('Escape')
  const teacherLink=page.getByRole('navigation',{name:'主导航'}).getByRole('link',{name:'教师',exact:true});await teacherLink.focus();await teacherLink.press('Enter');await expect(page).toHaveURL(/\/teacher$/)
  const notesUrl='**/article/getIndexArticleList*'
  await page.route(notesUrl,route=>route.fulfill({json:{code:9999,message:'Test unavailable'}}));await page.goto('http://127.0.0.1:14001/');await expect(page.getByText('笔记暂时加载失败',{exact:true})).toBeVisible();await page.unroute(notesUrl);await page.getByRole('button',{name:'重新加载'}).click();await expect(page.locator('.note-avatar').first()).toBeVisible()
  await page.route(notesUrl,route=>route.fulfill({json:{code:1000,data:[]}}));await page.goto('http://127.0.0.1:14001/');await expect(page.getByText('还没有笔记',{exact:true})).toBeVisible();await expect(page.getByText('笔记暂时加载失败',{exact:true})).toHaveCount(0);await page.unroute(notesUrl)
  report.push({engine,homeMetadata:true,discoveryLink:true,discussionLink:true,keyboardNavigation:true,notesErrorRetryAndEmpty:true})
  expect(errors).toEqual([]);report.push({engine,errors,teacherDetail:true,noticeRead:true,accountMenu:true,aiDisclosure:true})
 } finally {await browser.close();fs.writeFileSync(`${out}/layout-verification.json`,JSON.stringify(report,null,2))}
}
} finally {await api('/account/removeAccount?ids='+fixtureId,undefined,token)}
console.log(`Passed ${report.length} browser checks`)
