if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import { chromium, webkit } from '@playwright/test'
import fs from 'node:fs'
const report=[]
for(const [name,engine] of [['chromium',chromium],['webkit',webkit]]){
 const browser=await engine.launch()
 for(const role of ['student','teacher']){
  const context=await browser.newContext({viewport:{width:1440,height:1000}});const page=await context.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message))
  const origin=`http://127.0.0.1:${role==='student'?14001:14000}`
  await page.goto(origin+'/login');await page.locator('input').first().fill(`portfolio_${role}_01`);await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD);await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click();await page.waitForURL(u=>u.pathname!='/login');await page.waitForLoadState('networkidle');await page.waitForTimeout(700);
  await page.screenshot({path:`output/playwright/ui-polish/${name}-${role}-home.png`,fullPage:true});
  if(role==='student'){
   await page.getByRole('button',{name:/发现课程/}).click();await page.waitForURL('**/task');await page.goto(origin+'/');await page.getByLabel('今天想学点什么？').fill('Vue');await page.getByRole('button',{name:'搜索',exact:true}).click();await page.waitForURL('**/task?name=Vue');await page.goto(origin+'/ai');await page.waitForLoadState('networkidle');await page.waitForTimeout(700);await page.screenshot({path:`output/playwright/ui-polish/${name}-student-ai.png`,fullPage:true});await page.getByRole('button',{name:'开始提问'}).click();await page.getByRole('button',{name:'收起问答'}).waitFor({state:'visible'});
  } else {const summary=page.locator('summary').first();await summary.click();await page.waitForTimeout(300);await page.screenshot({path:`output/playwright/ui-polish/${name}-teacher-details.png`,fullPage:true})}
  await page.goto(origin+'/');await page.setViewportSize({width:390,height:844});await page.waitForLoadState('networkidle');await page.waitForTimeout(700);await page.screenshot({path:`output/playwright/ui-polish/${name}-${role}-mobile.png`,fullPage:true});
  const overflow=await page.evaluate(()=>({viewport:innerWidth,width:document.documentElement.scrollWidth}));report.push({name,role,errors,overflow});await context.close()
 }
 await browser.close()
}
fs.writeFileSync('output/playwright/ui-polish/verification.json',JSON.stringify(report,null,2));console.log(JSON.stringify(report))
