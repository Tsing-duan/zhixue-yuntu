if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import {chromium} from '@playwright/test'
import fs from 'node:fs'
const browser=await chromium.launch()
try {
 const page=await browser.newPage({viewport:{width:1280,height:900}})
 await page.goto('http://127.0.0.1:14001/login');await page.locator('input').first().fill('portfolio_student_01');await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD);await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click();await page.waitForURL(u=>u.pathname!='/login');await page.waitForLoadState('networkidle')
 const report=[]
 for(const route of ['/teacher','/notice','/ai']) {
  await page.goto('http://127.0.0.1:14001'+route);await page.waitForLoadState('networkidle');await page.screenshot({path:'output/playwright/student-access/before-'+route.slice(1)+'.png',fullPage:true})
  report.push({route,text:(await page.locator('body').innerText()).slice(-900),bounds:await page.evaluate(()=>({width:innerWidth,account:document.querySelector('.header .center')?.getBoundingClientRect().toJSON(),footer:document.querySelector('.app-footer')?.getBoundingClientRect().toJSON(),height:innerHeight}))})
 }
 fs.writeFileSync('output/playwright/student-access/before.json',JSON.stringify(report,null,2));console.log(JSON.stringify(report,null,2))
} finally {await browser.close()}
