if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import {chromium} from '@playwright/test'
import fs from 'node:fs'
const browser=await chromium.launch(),report=[]
try{
 for(const role of ['student','teacher','admin']){
  const origin=role==='student'?'http://127.0.0.1:14001':'http://127.0.0.1:14000';const context=await browser.newContext({viewport:{width:1440,height:1000}});const page=await context.newPage(),errors=[];page.on('pageerror',e=>errors.push(e.stack));
  await page.goto(origin+'/login');await page.locator('input').first().fill(`portfolio_${role}_01`);await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD);await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click();await page.waitForURL(u=>u.pathname!='/login');await page.waitForLoadState('networkidle');await page.screenshot({path:`output/playwright/i18n/${role}-zh.png`,fullPage:true});
  await page.getByLabel('界面语言 / Interface language',{exact:true}).selectOption('en');await page.waitForTimeout(250);await page.screenshot({path:`output/playwright/i18n/${role}-en.png`,fullPage:true});
  const paths=role==='student'?['/','/task','/teacher','/notice','/article','/forum','/ai','/center']:['/index','/task','/test','/aiData'];
  for(const path of paths){await page.goto(origin+path);await page.waitForLoadState('networkidle');const text=await page.locator('body').innerText();report.push({role,path,errors:[...errors],chinese:text.split('\n').filter(s=>/[\u4e00-\u9fff]/.test(s)),width:await page.evaluate(()=>({viewport:innerWidth,document:document.documentElement.scrollWidth}))});fs.writeFileSync(`output/playwright/i18n/${role}-${path.replaceAll('/','')||'home'}-text.txt`,text)}
  await page.setViewportSize({width:390,height:844});await page.goto(origin+(role==='student'?'/':'/index'));await page.waitForLoadState('networkidle');await page.screenshot({path:`output/playwright/i18n/${role}-en-390.png`,fullPage:true});await context.close()
 }
}finally{await browser.close();fs.writeFileSync('output/playwright/i18n/inspection.json',JSON.stringify(report,null,2))}
console.log(JSON.stringify(report,null,2))
