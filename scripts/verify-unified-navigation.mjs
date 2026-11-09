if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import {chromium,webkit,expect} from '@playwright/test'
import fs from 'node:fs'
const out='output/playwright/navigation-unified', report=[]
for(const [engine,type] of [['chromium',chromium],['webkit',webkit]]) {
 const browser=await type.launch()
 try {
  const page=await browser.newPage({viewport:{width:1280,height:900}}),errors=[];page.on('pageerror',e=>errors.push(e.message))
  await page.goto('http://127.0.0.1:14001/login');await page.locator('input').first().fill('portfolio_student_01');await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD);await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click();await page.waitForURL(u=>u.pathname!='/login');await page.waitForLoadState('networkidle')
  const nav=page.getByRole('navigation',{name:'主导航'}),toggle=page.getByRole('button',{name:/导航菜单/})
  for(const width of [1920,1440,1280,1152,1100,1099,1024,960,768,720,390,320]) {
   await page.setViewportSize({width,height:900});await page.goto('http://127.0.0.1:14001/ai');await page.waitForLoadState('networkidle')
   const bounds=await page.evaluate(()=>{const r=s=>document.querySelector(s).getBoundingClientRect().toJSON();return {header:r('.header'),logo:r('.system-title'),account:r('.header .center'),width:innerWidth,docWidth:document.documentElement.scrollWidth}})
   expect(bounds.header.height).toBe(width>=1100?72:64);expect(bounds.account.right).toBeLessThanOrEqual(width);expect(bounds.logo.left).toBeGreaterThanOrEqual(0);expect(bounds.docWidth).toBeLessThanOrEqual(width)
   for(const r of [bounds.logo,bounds.account])expect(Math.abs((r.top+r.height/2)-bounds.header.height/2)).toBeLessThan(2)
   if(width<1100) {
    await expect(nav).toBeHidden();await toggle.focus();await toggle.press('Enter');await expect(toggle).toHaveAttribute('aria-expanded','true');await expect(nav).toBeVisible()
    await expect(nav.getByRole('link')).toHaveCount(10)
    const layout=await nav.evaluate(e=>({display:getComputedStyle(e).display,cols:getComputedStyle(e).gridTemplateColumns,rect:e.getBoundingClientRect().toJSON(),items:[...e.querySelectorAll('a')].map(a=>a.getBoundingClientRect().toJSON())}));expect(layout.display).toBe('grid');expect(layout.rect.right).toBeLessThanOrEqual(width);expect(layout.rect.left).toBeGreaterThanOrEqual(0)
    expect(new Set(layout.items.map(x=>Math.round(x.left))).size).toBe(2)
    await page.screenshot({path:`${out}/${engine}-${width}-expanded.png`});await nav.getByRole('link').first().focus();await page.keyboard.press('Escape');await expect(nav).toBeHidden();await expect(toggle).toBeFocused()
    await toggle.click();await page.locator('.agent-container').click({position:{x:8,y:8}});await expect(nav).toBeHidden()
    await toggle.click();await page.getByRole('button',{name:/账号菜单/}).click();await expect(nav).toBeHidden();await expect(page.getByRole('menuitem',{name:'个人中心'})).toBeVisible();await page.getByRole('button',{name:/账号菜单/}).press('Escape')
    await toggle.click();await nav.getByRole('link',{name:'教师',exact:true}).click();await expect(page).toHaveURL(/\/teacher$/);await expect(page.getByRole('navigation',{name:'主导航'})).toBeHidden()
    await page.goto('http://127.0.0.1:14001/ai');await page.waitForLoadState('networkidle')
   } else {await expect(toggle).toBeHidden();await expect(nav).toBeVisible();const ys=await nav.getByRole('link').evaluateAll(es=>es.map(e=>Math.round(e.getBoundingClientRect().top)));expect(new Set(ys).size).toBe(1)}
   await page.screenshot({path:`${out}/${engine}-${width}.png`});report.push({engine,width,bounds,mode:width>=1100?'inline':'disclosure'})
  }
  for(const width of [1280,960,390]) {
   await page.setViewportSize({width,height:900})
   for(const path of ['/','/task','/teacher','/notice','/article','/forum','/about','/ai','/center','/myTask']) {
    await page.goto('http://127.0.0.1:14001'+path);await page.waitForLoadState('networkidle');const header=await page.locator('.header').boundingBox();expect(header.height).toBe(width>=1100?72:64)
    if(width<1100){await expect(nav).toBeHidden();await toggle.click();await expect(nav.getByRole('link')).toHaveCount(10);await toggle.press('Escape');await expect(nav).toBeHidden()}else await expect(nav).toBeVisible()
    report.push({engine,width,path,height:header.height})
   }
  }
  await page.setViewportSize({width:960,height:900});await toggle.click();await page.setViewportSize({width:1280,height:900});await expect(toggle).toBeHidden();await expect(page.locator('.navigation-toggle')).toHaveAttribute('aria-expanded','false');await page.setViewportSize({width:960,height:900});await expect(nav).toBeHidden()
  await page.setViewportSize({width:1280,height:900});await page.evaluate(()=>{const u=JSON.parse(localStorage.getItem('user_info'));u.userName='非常长的账号名称用于验证导航布局不会挤压主要入口';localStorage.setItem('user_info',JSON.stringify(u));dispatchEvent(new Event('userInfoUpdated'))});expect((await page.locator('.header .center').boundingBox()).x+(await page.locator('.header .center').boundingBox()).width).toBeLessThanOrEqual(1280)
  expect(errors).toEqual([]);report.push({engine,errors,resizeCloses:true,longName:true})
 } finally {await browser.close();fs.writeFileSync(`${out}/verification.json`,JSON.stringify(report,null,2))}
}
console.log(`Passed ${report.length} navigation checks`)
