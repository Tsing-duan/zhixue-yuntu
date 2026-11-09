if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import { chromium } from '@playwright/test'
import fs from 'node:fs'
const inventory=JSON.parse(fs.readFileSync('docs/modernization/baseline-inventory.json','utf8'))
const fixtures=JSON.parse(fs.readFileSync('.run/modernization/fixtures.json','utf8'))
const browser=await chromium.launch();const results={}
for(const role of ['student','teacher','admin']){
 const context=await browser.newContext();const page=await context.newPage();const origin='http://127.0.0.1:'+(role==='student'?14001:14000)
 await page.goto(origin+'/login');await page.locator('input').first().fill('portfolio_'+role+'_01');await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD);await page.getByRole('button',{name:/登\s*[录陆]/}).first().click();await page.waitForURL(url=>url.pathname!='/login')
 const menus=role==='student'?[]:await page.evaluate(async()=>{const res=await fetch('http://127.0.0.1:18080/menu/getMenuByUser',{headers:{x_access_token:localStorage.getItem('token')}});return(await res.json()).data})
 const paths=role==='student'?inventory.apps['learn_front-master'].staticPaths.filter(p=>!['/login','/teacherRegister','/editArticle'].includes(p)&&!p.includes(':pathMatch')):[...new Set(menus.filter(m=>m.menuType===1).map(m=>m.routeUrl)),'/aiData']
 const detail={taskInfo:'1729751427215790082',teacherInfo:'c864ceb6e258c75d183ba54a2033bd77',assignInfo:'1730032800929447937',testInfo:'1730068371957653506',articleInfo:fixtures.article,forumInfo:fixtures.forum,rateOfLearning:'1729751427215790082',studyInfo:'1729751427215790082',taskStudent:'1729751427215790082',taskComment:'1729751427215790082',chapterStudy:'1730032800929447937',testItem:'1730068371957653506',testStudent:'1730068371957653506',articleComment:fixtures.article,forumItem:fixtures.forum}
 const rows=[];let current
 page.on('pageerror',e=>current?.errors.push(e.message))
 page.on('response',r=>{if(r.url().includes(':18080')&&r.status()>=400)current?.failedRequests.push({path:new URL(r.url()).pathname,status:r.status()})})
 for(const path of paths){
  current={path,errors:[],failedRequests:[]}
  const target=path+(detail[path.slice(1)]?'?id='+detail[path.slice(1)]:'')
  await page.goto(origin+target);await page.waitForLoadState("networkidle");await page.waitForTimeout(350)
  current.finalPath=new URL(page.url()).pathname;rows.push(current)
 }
 results[role]=rows;await context.close()
}
await browser.close();fs.writeFileSync('docs/modernization/baseline-route-results.json',JSON.stringify(results,null,2));console.log(Object.fromEntries(Object.entries(results).map(([role,rows])=>[role,{count:rows.length,blocked:rows.filter(r=>r.failedRequests.length||r.errors.length||r.path!==r.finalPath)}])))
