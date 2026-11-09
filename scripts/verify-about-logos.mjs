if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import {chromium,expect} from '@playwright/test'
import fs from 'node:fs'
const browser=await chromium.launch(),report=[],errors=[]
try{
 const page=await browser.newPage();page.on('pageerror',error=>errors.push(error.message));await page.goto('http://127.0.0.1:14001/login');await page.locator('input').first().fill('portfolio_student_01');await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD);await page.locator('button').filter({hasText:/登\s*[录陆]/}).first().click();await page.waitForURL(u=>u.pathname!='/login');await page.goto('http://127.0.0.1:14001/about');await page.waitForLoadState('networkidle')
 for(const language of ['zh-CN','en'])for(const width of [390,768,1440]){
  await page.setViewportSize({width,height:width===390?844:1000});await page.getByLabel('界面语言 / Interface language',{exact:true}).selectOption(language);await page.waitForTimeout(350)
  const main=page.locator('main.about-logos');await expect(main.locator('img')).toHaveCount(2);await expect(main.locator('p')).toHaveCount(1);await expect(main.locator('a')).toHaveAttribute('href','https://github.com/Tsing-duan/zhixue-yuntu/issues');await expect(main.locator('a[href^="mailto:"]')).toHaveCount(0)
  const metrics=await page.evaluate(()=>({viewport:innerWidth,document:document.documentElement.scrollWidth,height:innerHeight,documentHeight:document.documentElement.scrollHeight,footer:document.querySelector('.app-footer').getBoundingClientRect().toJSON(),logos:[...document.querySelectorAll('.about-logos img')].map(image=>({alt:image.alt,loaded:image.complete&&image.naturalWidth>0,naturalRatio:image.naturalWidth/image.naturalHeight,box:image.getBoundingClientRect().toJSON()}))}))
  expect(metrics.document).toBeLessThanOrEqual(width);for(const logo of metrics.logos){expect(logo.loaded).toBe(true);expect(logo.box.x).toBeGreaterThanOrEqual(0);expect(logo.box.right).toBeLessThanOrEqual(width);expect(logo.box.width/logo.box.height).toBeCloseTo(logo.naturalRatio,2)}
  expect(Math.abs(metrics.footer.bottom-Math.max(metrics.height,metrics.documentHeight))).toBeLessThanOrEqual(2);await expect(page.locator('.header .el-dropdown-link')).toBeInViewport();await page.screenshot({path:`output/playwright/about-logos/${language}-${width}.png`,fullPage:true});report.push({language,width,twoLogosIntroductionAndFeedback:true,metrics})
 }
 expect(errors).toEqual([])
}finally{await browser.close();fs.writeFileSync('output/playwright/about-logos/verification.json',JSON.stringify({report,errors},null,2))}
console.log(`${report.length} layout/language cases passed; no page errors`)
