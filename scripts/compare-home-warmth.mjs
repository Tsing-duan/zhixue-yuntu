if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import {chromium} from '@playwright/test'
import fs from 'node:fs'
import path from 'node:path'
const browser=await chromium.launch();const report=[]
try {
 for(const version of ['baseline','current']) {
  const context=await browser.newContext({viewport:{width:1440,height:1000}})
  await context.grantPermissions(['local-network-access'],{origin:'http://127.0.0.1:14001'})
  if(version==='baseline') {
   const root=path.resolve('.run/modernization/baseline/student');const mime={'.html':'text/html','.js':'application/javascript','.css':'text/css','.json':'application/json','.png':'image/png','.jpg':'image/jpeg','.svg':'image/svg+xml','.woff':'font/woff','.ttf':'font/ttf','.webp':'image/webp'}
   await context.route('http://127.0.0.1:14001/**',async route=>{
    const pathname=decodeURIComponent(new URL(route.request().url()).pathname)
    if(/^\/(img|file|video)\//.test(pathname))return route.continue()
    let file=path.resolve(root,'.'+pathname);if(!file.startsWith(root+path.sep))file=path.join(root,'index.html')
    if(!fs.existsSync(file)||fs.statSync(file).isDirectory())file=path.join(root,'index.html')
    await route.fulfill({body:fs.readFileSync(file),contentType:mime[path.extname(file)]||'application/octet-stream'})
   })
  }
  const auth=await fetch('http://127.0.0.1:18080/login/student',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:'portfolio_student_01',password:process.env.ZHIXUE_DEMO_PASSWORD})}).then(r=>r.json())
  const token=auth.data.token;const user=await fetch('http://127.0.0.1:18080/user/getUserInfo',{headers:{x_access_token:token}}).then(r=>r.json())
  await context.addInitScript(({token,user})=>{localStorage.setItem('user_token',token);localStorage.setItem('user_info',JSON.stringify(user));},{token,user:user.data})
  const page=await context.newPage();const issues=[];page.on('requestfailed',r=>issues.push({url:r.url(),error:r.failure()?.errorText}));page.on('console',m=>{if(m.type()==='error')issues.push({message:m.text()})})

  for(const route of ['/']) {await page.goto('http://127.0.0.1:14001'+route);await page.waitForLoadState('networkidle');await page.screenshot({path:`output/playwright/home-warmth/compare-${version}-${route.slice(1)||'home'}.png`,fullPage:true});report.push({version,route,issues:[...issues],text:(await page.locator('body').innerText()).slice(0,5000)})}
  await context.close()
 }
} finally {await browser.close();fs.writeFileSync('output/playwright/home-warmth/experience-comparison.json',JSON.stringify(report,null,2))}
console.log('Captured baseline/current home, courses, discussion and notes using the same isolated backend; no global preview changes')
