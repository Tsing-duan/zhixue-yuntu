if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import {chromium, expect} from '@playwright/test'
import fs from 'node:fs'
const browser=await chromium.launch(), report=[], dir='output/playwright/i18n'
const studentPaths=['/','/task','/teacher','/notice','/article','/forum','/question','/message','/about','/center','/myTask','/myFavor','/myAssign','/myWrongAssign','/myArticle','/myFavarArticle','/test','/addArticle','/ai']
try {
 for (const role of ['student','teacher','admin']) {
  const origin=role==='student'?'http://127.0.0.1:14001':'http://127.0.0.1:14000', context=await browser.newContext({viewport:{width:1440,height:1000}}), page=await context.newPage(), errors=[]
  page.on('pageerror',e=>errors.push(e.stack))
  await page.goto(origin+'/login');await expect(page.locator('html')).toHaveAttribute('lang','zh-CN');await page.locator('input').first().fill(`portfolio_${role}_01`);await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD);await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click();await page.waitForURL(u=>u.pathname!='/login');await page.waitForLoadState('networkidle')
  const switcher=page.getByLabel('界面语言 / Interface language',{exact:true});await expect(switcher).toHaveValue('zh-CN')
  let paths=studentPaths
  if(role!=='student'){
   const token=await page.evaluate(()=>localStorage.getItem('token'))
   const menus=await (await context.request.get('http://127.0.0.1:18080/menu/getMenuByUser',{headers:{x_access_token:token}})).json()
   paths=[...new Set(['/index','/aiData',...menus.data.filter(m=>Number(m.menuType)===1&&Number(m.visible)===0).map(m=>m.routeUrl)])]
  }
  await switcher.selectOption('en');await page.reload();await expect(switcher).toHaveValue('en')
  for(const path of paths){
   const start=errors.length;console.log(role,path);await page.goto(origin+path,{waitUntil:'domcontentloaded'});await page.waitForLoadState('networkidle',{timeout:5000}).catch(()=>{});
   const text=await page.locator('body').innerText();const width=await page.evaluate(()=>({viewport:innerWidth,document:document.documentElement.scrollWidth}));
   const chinese=text.split('\n').filter(x=>/[\u4e00-\u9fff]/.test(x)&&x!=='中文');
   report.push({role,path,language:'en',url:page.url(),width,chinese,errors:errors.slice(start)});expect(width.document,`${role} ${path} overflow`).toBeLessThanOrEqual(width.viewport+1)
   await expect(page.locator('html')).toHaveAttribute('lang','en');
  }
  for(const lang of ['zh-CN','en']) for(const width of [390,768,1280,1920]){
   await page.goto(origin+(role==='student'?'/ai':'/index'));await page.waitForLoadState('networkidle');await switcher.selectOption(lang);await page.setViewportSize({width,height:1000});await page.waitForTimeout(450)
   const metrics=await page.evaluate(()=>({mainWidth:document.querySelector('.workspace-body')?.getBoundingClientRect().width,viewport:innerWidth,document:document.documentElement.scrollWidth,footer:document.querySelector('.app-footer')?.getBoundingClientRect().toJSON(),docHeight:document.documentElement.scrollHeight,height:innerHeight}));
   expect(metrics.document,`${role} ${lang} ${width} overflow`).toBeLessThanOrEqual(width+1);await expect(switcher).toBeInViewport(); if(role!=='student' && width<768) expect(metrics.mainWidth).toBeGreaterThan(width-80)
   if(role==='student'){
    const account=page.locator('.header .el-dropdown-link');await expect(account).toBeInViewport();await account.click();await expect(page.getByRole('menu')).toBeVisible();await page.keyboard.press('Escape')
    const toggle=page.locator('.navigation-toggle');if(await toggle.isVisible()){await toggle.click();await expect(page.locator('.header nav a')).toHaveCount(10);await page.keyboard.press('Escape')}
    expect(Math.abs(metrics.footer.bottom-Math.max(metrics.height,metrics.docHeight)),`${lang} ${width} footer`).toBeLessThanOrEqual(2)
   }
   await page.screenshot({path:`${dir}/${role}-${lang}-${width}.png`,fullPage:true});report.push({role,language:lang,width,metrics,controls:true})
  }
  await page.setViewportSize({width:1440,height:1000});await page.goto(origin+(role==='student'?'/ai':'/center'));await page.waitForLoadState('networkidle');
  await switcher.selectOption('zh-CN');await page.reload();await expect(switcher).toHaveValue('zh-CN')
  report.push({role,persistenceBothLanguages:true,errors});await context.close()
 }
}finally{await browser.close();fs.writeFileSync(`${dir}/verification.json`,JSON.stringify(report,null,2))}
const failures=report.filter(r=>r.errors?.length);console.log(JSON.stringify({checks:report.length,errors:failures,untranslated:report.filter(r=>r.chinese?.length).map(r=>({role:r.role,path:r.path,chinese:r.chinese}))},null,2));expect(failures).toHaveLength(0)
