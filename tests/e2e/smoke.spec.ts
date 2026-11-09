if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import { test, expect } from '@playwright/test'
import { navigate, login, origin, api, errors, type Role } from './helpers'
for (const role of ['student','teacher','admin'] as Role[]) {
 test(`${role}: login, production routes and refresh`, async ({page}, info) => {
  const failures=errors(page)
  await login(page,role)
  const routes=role==='student'?['/myTask','/myWrongAssign','/ai','/addArticle','/center']:role==='teacher'?['/teacherTask','/teacherChapter','/homework','/teacherTest','/aiData']:['/user','/role','/aiData','/school']
  for (const route of routes) {
    await navigate(page,origin(role)+route)
    await expect(page.locator('#app')).not.toBeEmpty()
    await expect(page.locator(role==='student'?'.header':'.notion-aside').first()).toBeVisible().catch(async()=>{await expect(page.locator('#app')).toContainText('智学云途')})
    if(role!=='student') {
      await expect(page.locator('.el-main')).toBeVisible()
      const box=await page.locator('.el-main').boundingBox();expect(box!.x).toBeLessThan(400);expect(box!.width).toBeGreaterThan(600)
    }
    await page.waitForTimeout(250)
    expect(page.url()).toContain(route)
  }
  await page.screenshot({path:`output/playwright/modernization/current/${info.project.name}-${role}.png`,fullPage:true})
  await page.reload();await expect(page.locator('#app')).not.toBeEmpty()
  expect(failures).toEqual([])
 })
}
test('server authorization and session expiry',async({page})=>{
 await navigate(page,origin('teacher')+'/teacherTask');await expect(page).toHaveURL(/\/login$/)
 await login(page,'teacher')
 expect((await api(page,'/user/getUserPage', {pageNumber:1,pageSize:10})).status).toBe(403)
 await navigate(page,origin('teacher')+'/user')
 await page.waitForURL('**/403')
 await page.evaluate(()=>localStorage.setItem('token','invalid-test-token'))
 await navigate(page,origin('teacher')+'/teacherTask')
 await page.waitForURL('**/login')
 expect(await page.evaluate(()=>localStorage.getItem('token'))).toBeNull()
})
test('menu failure ends loading and permits a fresh login',async({page})=>{
 await page.route('**/menu/getMenuByUser*',route=>route.fulfill({json:{code:1001,message:'菜单暂不可用',data:null}}))
 await navigate(page,origin('teacher')+'/login')
 await page.locator('input').first().fill('portfolio_teacher_01');await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD)
 await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click()
 await expect(page.getByText('菜单暂不可用').first()).toBeVisible()
 await expect(page).toHaveURL(/\/login$/)
 await page.unroute('**/menu/getMenuByUser*')
 await login(page,'teacher');await expect(page.locator('.notion-aside')).toContainText('教师课程管理')
})
test('narrow-screen login and student content remain usable',async({page}, info)=>{
 await page.setViewportSize({width:390,height:844})
 await login(page,'student')
 await navigate(page,origin('student')+'/ai')
 await expect(page.locator('#app')).toContainText('AI')
 await page.screenshot({path:`output/playwright/modernization/current/${info.project.name}-student-narrow.png`,fullPage:true})
})
