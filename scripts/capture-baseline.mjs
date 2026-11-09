if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import { chromium } from '@playwright/test'
import fs from 'node:fs'
const browser = await chromium.launch()
const report=[]
for (const role of ['student','teacher','admin']) {
 const context=await browser.newContext({viewport:{width:1440,height:1000}})
 const page=await context.newPage();const errors=[]
 page.on('pageerror',e=>errors.push(e.message))
 await page.goto(`http://127.0.0.1:${role==='student'?14001:14000}/login`)
 await page.locator('input').nth(0).fill(`portfolio_${role}_01`)
 await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD)
 await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click()
 await page.waitForURL(url=>url.pathname!='/login',{timeout:15000})
 await page.waitForTimeout(1000)
 await page.screenshot({path:`output/playwright/modernization/baseline/${role}-home.png`,fullPage:true})
 for(const route of role==='student'?['/myTask','/myWrongAssign','/ai','/addArticle']:role==='teacher'?['/teacherTask','/teacherChapter','/homework','/teacherTest','/aiData']:['/user','/role','/aiData']){
   await page.goto(`http://127.0.0.1:${role==='student'?14001:14000}${route}`)
   await page.waitForTimeout(700)
   await page.screenshot({path:`output/playwright/modernization/baseline/${role}-${route.slice(1)}.png`,fullPage:true})
 }
 report.push({role,errors});await context.close()
}
fs.writeFileSync('docs/modernization/baseline-browser.json',JSON.stringify(report,null,2))
await browser.close()
console.log(JSON.stringify(report))
