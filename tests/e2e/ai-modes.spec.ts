import {test,expect} from '@playwright/test'
import {navigate,login,origin,errors} from './helpers'
test('teacher AI course and exam modes use real local statistics with mocked generation; admin usage stays local',async({page})=>{
 const failures=errors(page);await login(page,'teacher');await navigate(page,origin('teacher')+'/aiData')
 await page.route('**/ai/data-agent',route=>route.fulfill({json:{code:1000,data:{reply:'模拟 '+route.request().postDataJSON().agentType}}}))
 await page.locator('.agent-params .el-select').click();await page.getByRole('option',{name:'Portfolio Demo Course',exact:true}).click();await expect(page.locator('.chart-box canvas')).toHaveCount(1)
 await page.getByRole('button',{name:'生成诊断',exact:true}).click();await expect(page.locator('.agent-result')).toContainText('模拟 course-health')
 await page.locator('.agent-mode').filter({hasText:'考试质量分析'}).click();await page.locator('.agent-params .el-select').click();await page.getByRole('option').filter({hasText:'Portfolio Demo Exam'}).first().click();await expect(page.locator('.chart-box canvas')).toHaveCount(2)
 await page.getByRole('button',{name:'生成分析',exact:true}).click();await expect(page.locator('.agent-result')).toContainText('模拟 exam-analysis')
 await page.evaluate(()=>localStorage.clear());await login(page,'admin');await navigate(page,origin('admin')+'/aiData');await expect(page.locator('.chart-box canvas')).toHaveCount(1)
 await page.getByRole('button',{name:'生成周报',exact:true}).click();await expect(page.locator('.agent-result')).toContainText('模拟 platform-report');expect(failures).toEqual([])
})
