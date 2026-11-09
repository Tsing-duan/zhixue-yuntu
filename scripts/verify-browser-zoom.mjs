if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import {chromium,expect} from '@playwright/test'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
const report=[]
for(const zoom of [1,1.25,1.5,2]) {
 const profile=fs.mkdtempSync(path.join(os.tmpdir(),'zhixue-zoom-'))
 const context=await chromium.launchPersistentContext(profile,{channel:'chromium',headless:true,viewport:null,args:['--window-size=1440,1000']})
 try {
  const page=context.pages()[0];await page.goto('chrome://settings/appearance');await page.locator('#zoomLevel').selectOption({label:`${zoom*100}%`});await page.goto('http://127.0.0.1:14001/login')
  const actual=await page.evaluate(()=>({width:innerWidth,outerWidth,ratio:devicePixelRatio,height:innerHeight}));console.log({zoom,actual})
  expect(actual.ratio).toBeCloseTo(zoom,1);expect(actual.width).toBeCloseTo(1440/zoom,0)
  await page.locator('input').first().fill('portfolio_student_01');await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD);await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click();await page.waitForURL(u=>u.pathname!='/login');await page.goto('http://127.0.0.1:14001/ai');await page.waitForLoadState('networkidle')
  if(actual.width<1100){await page.getByRole('button',{name:'打开导航菜单'}).click();await expect(page.getByRole('navigation',{name:'主导航'}).getByRole('link')).toHaveCount(10);await page.getByRole('button',{name:'关闭导航菜单'}).press('Escape')}
  const bounds=await page.evaluate(()=>({width:innerWidth,height:innerHeight,docWidth:document.documentElement.scrollWidth,docHeight:document.documentElement.scrollHeight,account:document.querySelector('.header .center').getBoundingClientRect().toJSON(),footer:document.querySelector('.app-footer').getBoundingClientRect().toJSON()}))
  expect(bounds.docWidth).toBeLessThanOrEqual(bounds.width+1);expect(bounds.account.right).toBeLessThanOrEqual(bounds.width);expect(Math.abs(bounds.footer.bottom-Math.max(bounds.height,bounds.docHeight))).toBeLessThanOrEqual(2)
  await page.getByRole('button',{name:/账号菜单/}).click();await expect(page.getByRole('menuitem',{name:'个人中心'})).toBeVisible();await page.keyboard.press('Escape');await expect(page.getByRole('menuitem',{name:'个人中心'})).toBeHidden()
  const cdp=await context.newCDPSession(page);const shot=await cdp.send('Page.captureScreenshot',{format:'png',captureBeyondViewport:false});fs.writeFileSync(`output/playwright/navigation-unified/chromium-real-zoom-${zoom*100}.png`,Buffer.from(shot.data,'base64'));await cdp.detach();report.push({zoom,actual,bounds,accountMenu:true})
 } finally {await context.close();fs.rmSync(profile,{recursive:true,force:true});fs.writeFileSync('output/playwright/navigation-unified/browser-zoom-verification.json',JSON.stringify(report,null,2))}
}
