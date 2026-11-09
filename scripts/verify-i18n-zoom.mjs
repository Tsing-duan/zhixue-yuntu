if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import {chromium,expect} from '@playwright/test'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
const report=[]
for(const lang of ['zh-CN','en']) for(const role of ['student','admin']) for(const zoom of [1,1.25,1.5,2]) {
 const origin=role==='student'?'http://127.0.0.1:14001':'http://127.0.0.1:14000'
 const profile=fs.mkdtempSync(path.join(os.tmpdir(),'zhixue-zoom-'))
 const context=await chromium.launchPersistentContext(profile,{channel:'chromium',headless:true,viewport:null,args:['--window-size=1440,1000']})
 try {
  const page=context.pages()[0];await page.goto('chrome://settings/appearance');await page.locator('#zoomLevel').selectOption({label:`${zoom*100}%`});await page.goto(origin+'/login')
  const actual=await page.evaluate(()=>({width:innerWidth,outerWidth,ratio:devicePixelRatio,height:innerHeight}));console.log({zoom,actual})
  expect(actual.ratio).toBeCloseTo(zoom,1);expect(actual.width).toBeCloseTo(1440/zoom,0)
  await page.locator('input').first().fill(`portfolio_${role}_01`);await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD);await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click();await page.waitForURL(u=>u.pathname!='/login');await page.getByLabel('界面语言 / Interface language',{exact:true}).selectOption(lang)
  await page.goto(origin+(role==='student'?'/ai':'/index'));await page.waitForLoadState('networkidle');await page.waitForTimeout(400)
  const bounds=await page.evaluate(()=>({width:innerWidth,height:innerHeight,docWidth:document.documentElement.scrollWidth,docHeight:document.documentElement.scrollHeight,mainWidth:document.querySelector('.workspace-body')?.getBoundingClientRect().width,footer:document.querySelector('.app-footer')?.getBoundingClientRect().toJSON()}))
  expect(bounds.docWidth).toBeLessThanOrEqual(bounds.width+1)
  await expect(page.getByLabel('界面语言 / Interface language',{exact:true})).toBeInViewport()
  if(role==='student') {
   if(await page.locator('.navigation-toggle').isVisible()){await page.locator('.navigation-toggle').click();await expect(page.locator('.header nav a')).toHaveCount(10);await page.keyboard.press('Escape')}
   await expect(page.locator('.header .el-dropdown-link')).toBeInViewport();await page.locator('.header .el-dropdown-link').click();await expect(page.getByRole('menu')).toBeVisible();await page.keyboard.press('Escape')
   expect(Math.abs(bounds.footer.bottom-Math.max(bounds.height,bounds.docHeight))).toBeLessThanOrEqual(2)
  } else { await expect(page.locator('.avatar-wrap')).toBeInViewport(); if(bounds.width<768) expect(bounds.mainWidth).toBeGreaterThan(bounds.width-80) }
  const cdp=await context.newCDPSession(page);const shot=await cdp.send('Page.captureScreenshot',{format:'png',captureBeyondViewport:false});fs.writeFileSync(`output/playwright/i18n/${role}-${lang}-real-zoom-${zoom*100}.png`,Buffer.from(shot.data,'base64'));await cdp.detach();report.push({role,lang,zoom,actual,bounds,controls:true})
 } finally {await context.close();fs.rmSync(profile,{recursive:true,force:true});fs.writeFileSync('output/playwright/i18n/browser-zoom-verification.json',JSON.stringify(report,null,2))}
}
