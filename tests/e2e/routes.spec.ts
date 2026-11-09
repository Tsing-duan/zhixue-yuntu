import { test, expect } from '@playwright/test'
import fs from 'node:fs'
import { settle, navigate, login, origin, api, errors, type Role } from './helpers'
const fixtures=JSON.parse(fs.readFileSync('.run/modernization/fixtures.json','utf8'))
const oldRoutes=JSON.parse(fs.readFileSync('docs/modernization/baseline-route-results.json','utf8'))
const baseline=JSON.parse(fs.readFileSync('docs/modernization/baseline-inventory.json','utf8'))
for(const role of ['student','teacher','admin'] as Role[]) {
 test(`${role}: all registered business pages and add forms`,async({page},info)=>{
  test.setTimeout(180000)
  const failures=errors(page);await login(page,role)
  const data=role==='student'?null:(await api(page,'/menu/getMenuByUser')).body.data
  const paths:string[]=role==='student'?baseline.apps['learn_front-master'].staticPaths.filter((p:string)=>!['/login','/teacherRegister','/editArticle'].includes(p)&&!p.includes(':pathMatch')):[...new Set<string>(data.filter((m:any)=>m.menuType===1).map((m:any)=>m.routeUrl)),'/aiData']
  const detail:Record<string,string>={taskInfo:'1729751427215790082',teacherInfo:'c864ceb6e258c75d183ba54a2033bd77',assignInfo:'1730032800929447937',testInfo:'1730068371957653506',articleInfo:fixtures.article,forumInfo:fixtures.forum,rateOfLearning:'1729751427215790082',studyInfo:'1729751427215790082',taskStudent:'1729751427215790082',taskComment:'1729751427215790082',chapterStudy:'1730032800929447937',testItem:'1730068371957653506',testStudent:'1730068371957653506',articleComment:fixtures.article,forumItem:fixtures.forum}
  const visited=[]
  for(const path of paths) {
   const target=path+(detail[path.slice(1)]?'?id='+detail[path.slice(1)]:'')
   await navigate(page,origin(role)+target)
   await expect(page.locator('#app')).not.toBeEmpty()
   await settle(page)
   const blocked=oldRoutes[role].find((r:any)=>r.path===path)?.failedRequests.some((r:any)=>r.status===403)
   expect(new URL(page.url()).pathname,`Unexpected redirect from ${target}`).toBe(blocked?'/403':path)
   if(!blocked&&detail[path.slice(1)])expect(new URL(page.url()).searchParams.get('id')).toBe(detail[path.slice(1)])
   if(role!=='student'&&!['/403','/404'].includes(path))await expect(page.locator('.el-main'),target).not.toBeEmpty()
   const buttons=await page.getByRole('button').allTextContents()
   const add=page.getByRole('button',{name:'新增',exact:true}).first()
   if(role!=='student'&&await add.isVisible()&&await add.isEnabled()) {
     await add.click();const dialog=page.getByRole('dialog').last();await expect(dialog).toBeVisible()
     const close=dialog.getByRole('button',{name:/取消|取 消|Close|关闭/}).first()
     if(await close.isVisible())await close.click();else await page.keyboard.press('Escape')
     await expect(dialog).not.toBeVisible()
   }
   visited.push({path:target,status:blocked?'baseline-permission-blocked':'page-and-form-checked',buttons:buttons.map(b=>b.trim()).filter(Boolean)})
  }
  fs.writeFileSync(`output/playwright/modernization/${info.project.name}-${role}-routes.json`,JSON.stringify({role,visited,errors:failures},null,2))
  expect(failures).toEqual([])
 })
}
test('teacher cached tabs preserve values, closing evicts, logout removes private routes',async({page})=>{
 const failures=errors(page);await login(page,'teacher')
 await page.getByRole('menuitem',{name:/课程管理/}).first().click()
 await page.getByRole('menuitem',{name:/教师课程管理/}).click()
 const input=page.getByPlaceholder('请输入课程名称');await input.fill('unsaved tab state')
 await page.getByRole('menuitem',{name:/教师章节管理/}).click()
 await page.locator('.menu-item').filter({hasText:'教师课程管理'}).click();await expect(input).toHaveValue('unsaved tab state')
 await page.locator('.menu-item').filter({hasText:'教师课程管理'}).locator('.el-icon-close').click()
 await page.getByRole('menuitem',{name:/教师课程管理/}).click();await expect(input).toHaveValue('')
 await page.locator('.notion-header .avatar-wrap .el-dropdown').click()
 await page.getByRole('menuitem',{name:/退出/}).click();await page.waitForURL('**/login')
 await login(page,'admin');await expect(page.locator('.notion-aside')).not.toContainText('教师课程管理');expect(failures).toEqual([])
})
