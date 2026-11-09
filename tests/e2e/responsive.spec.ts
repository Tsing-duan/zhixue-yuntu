import {test,expect} from '@playwright/test'
import {navigate,login,origin,errors,type Role} from './helpers'
for(const role of ['teacher','admin'] as Role[])test(`${role}: narrow screen navigation and form`,async({page},info)=>{
 const failures=errors(page);await page.setViewportSize({width:390,height:844});await login(page,role)
 await navigate(page,origin(role)+(role==='teacher'?'/teacherTask':'/user'))
 const sidebar=await page.locator('.notion-aside').boundingBox();expect(sidebar!.width).toBeLessThan(100)
 await page.getByRole('button',{name:'新增',exact:true}).click();const dialog=page.getByRole('dialog');await expect(dialog).toBeVisible();await expect.poll(async()=>(await dialog.locator('.el-dialog').boundingBox())?.y??-1).toBeGreaterThanOrEqual(0);const bounds=await dialog.locator('.el-dialog').boundingBox();expect(bounds!.width).toBeLessThanOrEqual(390);expect(bounds!.x).toBeGreaterThanOrEqual(0);expect(bounds!.y).toBeGreaterThanOrEqual(0);expect(bounds!.y).toBeLessThan(250);await expect(dialog.getByRole('button',{name:/取\s*消/})).toBeInViewport()
 await page.waitForTimeout(350) // Capture after the dialog enter animation finishes.
 await page.screenshot({path:`output/playwright/modernization/current/${info.project.name}-${role}-narrow.png`,fullPage:true});await dialog.getByRole('button',{name:/取\s*消/}).click();await expect(dialog).not.toBeVisible();expect(failures).toEqual([])
})
