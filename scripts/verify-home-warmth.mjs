if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import {chromium,webkit,expect} from '@playwright/test'
import fs from 'node:fs'
const out='output/playwright/home-warmth',report=[]
const auth=await fetch('http://127.0.0.1:18080/login/student',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:'portfolio_student_01',password:process.env.ZHIXUE_DEMO_PASSWORD})}).then(r=>r.json())
const token=auth.data.token
const api=async(path,body)=>fetch('http://127.0.0.1:18080'+path,{method:body?'POST':'GET',headers:{'Content-Type':'application/json',x_access_token:token},body:body?JSON.stringify(body):undefined}).then(r=>r.json()).then(r=>r.data)
const [user,courses,teachers,forums]=await Promise.all([api('/user/getUserInfo'),api('/task/getApeTaskPage',{pageNumber:1,pageSize:3,state:0,type:0}),api('/portal/teachers',{pageNumber:1,pageSize:3}),api('/forum/getApeForumPage',{pageNumber:1,pageSize:3,state:0})])
for(const [engine,type] of [['chromium',chromium],['webkit',webkit]]){
 const browser=await type.launch()
 try{
  const context=await browser.newContext();await context.addInitScript(({token,user})=>{localStorage.setItem('user_token',token);localStorage.setItem('user_info',JSON.stringify(user))},{token,user})
  const page=await context.newPage(),errors=[];page.on('pageerror',e=>errors.push(e.message))
  for(const width of [1440,1100,960,768,390,320]){
   await page.setViewportSize({width,height:960});await page.goto('http://127.0.0.1:14001/');await page.waitForLoadState('networkidle')
   await expect(page.locator('.campus-scene img')).toBeVisible();expect(await page.locator('.campus-scene img').evaluate(i=>i.complete&&i.naturalWidth>0)).toBe(true)
   await expect(page.locator('.home-course-card')).toHaveCount(courses.records.length);await expect(page.locator('.home-teacher')).toHaveCount(teachers.records.length);await expect(page.locator('.discussion-link')).toHaveCount(forums.records.length)
   for(const [i,row] of courses.records.entries()){await expect(page.locator('.home-course-card').nth(i)).toContainText(row.name);await expect(page.locator('.home-course-card').nth(i)).toContainText(row.teacherName)}
   for(const [i,row] of forums.records.entries()){await expect(page.locator('.discussion-link').nth(i)).toContainText(row.name);await expect(page.locator('.discussion-link').nth(i)).toContainText(row.createBy)}
   const bounds=await page.evaluate(()=>({width:innerWidth,docWidth:document.documentElement.scrollWidth,account:document.querySelector('.header .center').getBoundingClientRect().right,footer:document.querySelector('.app-footer').getBoundingClientRect().bottom,height:Math.max(innerHeight,document.documentElement.scrollHeight)}))
   expect(bounds.docWidth).toBeLessThanOrEqual(width);expect(bounds.account).toBeLessThanOrEqual(width);expect(Math.abs(bounds.footer-bounds.height)).toBeLessThan(2)
   await page.screenshot({path:`${out}/${engine}-${width}.png`,fullPage:true});report.push({engine,width,bounds,realSourceContent:true})
  }
  await page.setViewportSize({width:1440,height:1000})
  for(const [selector,route] of [['.home-course-card','taskInfo'],['.home-teacher','teacherInfo'],['.discussion-link','forumInfo'],['.note-link','articleInfo']]){
   await page.goto('http://127.0.0.1:14001/');await page.waitForLoadState('networkidle');const href=await page.locator(selector).first().getAttribute('href');await page.locator(selector).first().click({position:{x:12,y:12}});await expect(page).toHaveURL(new RegExp('/'+route+'\\?id='));expect(new URL(page.url()).search).toBe(new URL(href,'http://127.0.0.1:14001').search)
  }
  await page.goto('http://127.0.0.1:14001/');await page.getByLabel('今天想学点什么？').fill('Portfolio');await page.getByRole('button',{name:'搜索',exact:true}).click();await expect(page).toHaveURL(/\/task\?name=Portfolio/)
  await page.goto('http://127.0.0.1:14001/');await page.getByRole('link',{name:'写一篇学习笔记 →'}).focus();await page.keyboard.press('Enter');await expect(page).toHaveURL(/\/addArticle$/)
  for(const [url,text,button] of [['**/task/getApeTaskPage','课程暂时加载失败','重新加载课程'],['**/portal/teachers','教师暂时加载失败','重新加载教师'],['**/forum/getApeForumPage','讨论暂时加载失败','重新加载讨论']]){
   await page.route(url,r=>r.fulfill({json:{code:1001,message:'test unavailable'}}));await page.goto('http://127.0.0.1:14001/');await expect(page.getByText(text,{exact:true})).toBeVisible();await page.unroute(url);await page.getByRole('button',{name:button}).click();await expect(page.getByText(text,{exact:true})).toHaveCount(0)
  }
  for(const url of ['**/task/getApeTaskPage','**/portal/teachers','**/forum/getApeForumPage'])await page.route(url,r=>r.fulfill({json:{code:1000,data:{records:[]}}}))
  await page.goto('http://127.0.0.1:14001/');for(const text of ['还没有可浏览的课程','暂无可展示的教师介绍','还没有公开讨论'])await expect(page.getByText(text,{exact:true})).toBeVisible()
  expect(errors).toEqual([]);report.push({engine,cardPaddingClick:true,search:true,keyboardWritingLink:true,threeErrorRetryStates:true,threeEmptyStates:true,errors})
 }finally{await browser.close();fs.writeFileSync(`${out}/verification.json`,JSON.stringify(report,null,2))}
}
console.log(`Passed ${report.length} home warmth checks`)
