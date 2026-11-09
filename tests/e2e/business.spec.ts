import { test, expect } from '@playwright/test'
import fs from 'node:fs'
import { navigate, login, origin, api, errors } from './helpers'
const fixtures=JSON.parse(fs.readFileSync('.run/modernization/fixtures.json','utf8'))
const course='1729751427215790082'
const unique = () => `Migration ${Date.now()}`
async function successful(response: any) { expect((await response.json()).code).toBe(1000) }
test('admin school create, edit, pagination and delete',async({page})=>{
 const failures=errors(page);await login(page,'admin');await navigate(page,origin('admin')+'/school')
 const name=unique();await page.getByRole('button',{name:/新增/}).first().click()
 let dialog=page.getByRole('dialog');await expect(dialog).toBeVisible()
 await dialog.getByPlaceholder('请输入名称').fill(name)
 await Promise.all([page.waitForResponse('**/school/saveApeSchool').then(successful),dialog.getByRole('button',{name:/确\s*定/}).click()])
 await expect(dialog).not.toBeVisible();let row=page.locator('.el-table__row').filter({hasText:name});await expect(row).toBeVisible()
 await row.getByRole('button',{name:/修改/}).click();dialog=page.getByRole('dialog')
 await expect(dialog.getByPlaceholder('请输入名称')).toHaveValue(name)
 await dialog.getByPlaceholder('请输入名称').fill(name+' edited')
 await Promise.all([page.waitForResponse('**/school/editApeSchool').then(successful),dialog.getByRole('button',{name:/确\s*定/}).click()])
 await expect(page.locator('.el-table__row').filter({hasText:name+' edited'})).toBeVisible()
 await page.locator('.el-pagination .el-select').click();await page.getByRole('option',{name:'20条/页',exact:true}).click()
 await row.getByRole('button',{name:/删除/}).click();await page.getByRole('button',{name:'确认',exact:true}).click()
 await expect(page.locator('.el-table__row').filter({hasText:name})).toHaveCount(0);expect(failures).toEqual([])
})
test('student historical rich text remains unchanged on save, then edits and image upload persist',async({page})=>{
 const failures=errors(page);await login(page,'student')
 const title=unique();const html='<h2>迁移标题</h2><p><strong>原有粗体</strong> 与 <em>斜体</em></p><ul><li>原有列表</li></ul><pre class="ql-syntax">const n = 1;</pre><p><img src="http://127.0.0.1:18080/img/avatars/zhixue-default.png"></p>'
 expect((await api(page,'/article/saveApeArticle',{title,articleDesc:'Synthetic legacy HTML',content:html,taskId:course,state:2})).body.code).toBe(1000)
 const list=(await api(page,'/article/getApeArticlePage',{pageNumber:1,pageSize:100,title})).body.data.records
 const id=list.find((record:any)=>record.title===title).id
 await navigate(page,origin('student')+'/editArticle?id='+id)
 await expect(page.locator('.ql-editor')).toContainText('原有列表')
 const saved=page.waitForRequest('**/article/editApeArticle')
 await page.getByRole('button',{name:/保存修改/}).click()
 expect((await saved).postDataJSON().content).toBe(html)
 await expect.poll(async()=>(await api(page,'/article/getApeArticleById?id='+id)).body.data.content).toBe(html)
 await page.waitForURL('**/articleInfo?id='+id)
 await navigate(page,origin('student')+'/editArticle?id='+id)
 const editor=page.locator('.ql-editor');await expect(editor.locator('img')).toHaveCount(1);await editor.locator('h2').click();await page.keyboard.press('Home');await page.keyboard.type('新增内容 ');await expect(editor.locator('img')).toHaveCount(1)
 const upload=page.waitForResponse('**/common/uploadImg')
 await page.locator('.Quill-uploader input[type=file]').setInputFiles('learn_server-master/img/avatars/zhixue-default.png')
 await successful(await upload)
 await expect(editor.locator('img')).toHaveCount(2)
 await Promise.all([page.waitForResponse('**/article/editApeArticle').then(successful),page.getByRole('button',{name:/保存修改/}).click()])
 const after=(await api(page,'/article/getApeArticleById?id='+id)).body.data.content
 expect(after).toContain('新增内容');expect(after).toContain('<strong>');expect(after).toContain('原有列表');expect(after).toMatch(/<(ul|ol)>/);expect(after).toContain('const n = 1;');expect(after).toContain('<pre');expect(after.match(/<img/g)).toHaveLength(2)
 await navigate(page,origin('student')+'/forumInfo?id='+fixtures.forum);const reply=unique();await page.getByPlaceholder('分享你的观点...').fill(reply);await Promise.all([page.waitForResponse('**/item/saveApeForumItem').then(successful),page.getByRole('button',{name:'发布回复',exact:true}).click()]);await expect(page.getByPlaceholder('分享你的观点...')).toHaveValue('')
 await login(page,'admin');const replies=(await api(page,'/item/getApeForumItemPage',{forumId:fixtures.forum,pageNumber:1,pageSize:100})).body.data.records;expect(replies.find((r:any)=>r.content===reply).state).toBe(2);expect(failures).toEqual([])
})
test('student homework and exam answer selection, submission and readback',async({page},info)=>{
 const f=fixtures[info.project.name];const failures=errors(page);await login(page,'student')
 await navigate(page,origin('student')+'/assignInfo?id='+f.chapter)
 await page.locator('.el-radio').nth(1).click()
 await expect(page.getByRole('radio').nth(1)).toBeChecked()
 await page.getByRole('button',{name:/提交作业/}).click()
 await Promise.all([page.waitForResponse('**/student/saveApeHomeworkStudent').then(successful),page.getByRole('button',{name:'确定提交',exact:true}).click()])
 await page.waitForURL('**/taskInfo?id='+course)
 await navigate(page,origin('student')+'/assignInfo?id='+f.chapter);await expect(page.getByRole('radio').nth(1)).toBeDisabled();await expect(page.getByRole('radio').nth(1)).toBeChecked()
 await navigate(page,origin('student')+'/myWrongAssign');const wrong=page.locator('.el-collapse-item').filter({hasText:f.chapterName});if(await wrong.locator('.el-collapse-item__header').getAttribute('aria-expanded')!=='true')await wrong.locator('.el-collapse-item__header').click();await wrong.locator('.el-radio').first().click();await Promise.all([page.waitForResponse('**/student/editApeHomeworkStudent').then(successful),wrong.getByRole('button',{name:'保存修改',exact:true}).click()]);await expect(wrong).toHaveCount(0)
 expect((await api(page,'/student/getApeHomeworkStudentList',{chapterId:f.chapter})).body.data[0].solution).toBe('A')
 await navigate(page,origin('student')+'/testInfo?id='+f.exam)
 await page.locator('.el-radio').first().click()
 await expect(page.getByRole('radio').first()).toBeChecked()
 await page.getByRole('button',{name:/提交试卷/}).click()
 await Promise.all([page.waitForResponse('**/student/saveApeTestStudent').then(successful),page.getByRole('button',{name:'确定提交',exact:true}).click()])
 await page.waitForURL('**/test')
 await navigate(page,origin('student')+'/testInfo?id='+f.exam);await expect(page.getByRole('radio').first()).toBeDisabled();await expect(page.getByRole('radio').first()).toBeChecked();expect(failures).toEqual([])
})
test('teacher chapter uploads, student playback and downloaded file bytes',async({page},info)=>{
 const failures=errors(page);const f=fixtures[info.project.name]
 await login(page,'teacher');await navigate(page,origin('teacher')+'/teacherChapter')
 await page.getByPlaceholder('请输入章节名称').fill(f.chapterName);await page.getByRole('button',{name:'查询',exact:true}).click()
 const row=page.locator('.el-table__row').filter({hasText:f.chapterName})
 await row.getByRole('button',{name:/修改/}).click()
 const dialog=page.getByRole('dialog');await expect(dialog.getByPlaceholder('请输入章节名称')).toHaveValue(f.chapterName)
 await Promise.all([page.waitForResponse('**/common/uploadVideo').then(successful),dialog.locator('input[type=file]').nth(0).setInputFiles('.run/modernization/media/sample.mp4')])
 await Promise.all([page.waitForResponse('**/common/uploadFile').then(successful),dialog.locator('input[type=file]').nth(1).setInputFiles('.run/modernization/media/courseware.txt')])
 await Promise.all([page.waitForResponse('**/chapter/editApeChapter').then(successful),dialog.getByRole('button',{name:/确\s*定/}).click()])
 const chapter=(await api(page,'/chapter/getApeChapterById?id='+f.chapter)).body.data
 expect(chapter.video).toMatch(/^\/video\//);expect(chapter.courseware).toMatch(/^\/file\//)
 const download=await page.request.get('http://127.0.0.1:18080'+chapter.courseware)
 expect(await download.body()).toEqual(fs.readFileSync('.run/modernization/media/courseware.txt'))
 await login(page,'student');await navigate(page,origin('student')+'/taskInfo?id='+course)
 await page.locator('.chapter-item').filter({hasText:f.chapterName}).click()
 const video=page.locator('video');await expect(video).toBeVisible()
 await expect.poll(()=>video.evaluate((v: HTMLVideoElement)=>v.readyState)).toBeGreaterThanOrEqual(2)
 await video.evaluate((v:HTMLVideoElement)=>v.play());await expect.poll(()=>video.evaluate((v:HTMLVideoElement)=>v.currentTime)).toBeGreaterThan(0)
 await page.locator('.chapter-item').filter({hasText:'Portfolio Demo Chapter'}).click();await expect(page.getByText('本章节暂无视频，可继续查看作业或课件')).toBeVisible()
 expect(failures).toEqual([])
})
test('AI mocked success, provider failure, cooldown and transport timeout',async({page})=>{
 await login(page,'student');await navigate(page,origin('student')+'/ai')
 let scenario='success'
 await page.route('**/ai/study-plan',route=>{
  if(scenario==='timeout')return route.abort('timedout')
  return route.fulfill({json:scenario==='success'?{code:1000,data:{reply:'确定性学习建议',historyId:'synthetic-history',createTime:'2026-11-06 20:00:00'}}:{code:1001,message:scenario==='cooldown'?'请等待 15 秒后重试':'模型服务暂不可用'}})
 })
 await page.locator('.generate-btn').click();await expect(page.getByText('确定性学习建议',{exact:true})).toBeVisible()
 for(const value of ['failure','cooldown','timeout']) {
  scenario=value;
  if ((await page.locator('.generate-btn').innerText()).includes('新聊天')) await page.locator('.generate-btn').click()
  await page.locator('.generate-btn').click()
  await expect(page.getByText(value==='failure'?'模型服务暂不可用':value==='cooldown'?'请等待 15 秒后重试':'网络请求失败，请稍后重试',{exact:true}).last()).toBeVisible()
  await expect(page.locator('.generate-btn')).not.toHaveClass(/is-loading/)
 }
})
test('teacher ownership canary rejects writes without changing foreign course',async({page})=>{
 await login(page,'teacher')
 const result=await api(page,'/task/editApeTask',{id:fixtures.foreignCourse,name:'must-not-write'})
 expect(result.status).toBe(403)
 // Verify unchanged through a separate administrator session, not frontend visibility.
 await page.evaluate(()=>localStorage.clear());await login(page,'admin')
 const course=(await api(page,'/task/getApeTaskById?id='+fixtures.foreignCourse)).body.data
 expect(course.name).toBe('Foreign ownership canary')
})
test('student avatar upload persists and renders after refresh',async({page})=>{
 const failures=errors(page);await login(page,'student');await navigate(page,origin('student')+'/center')
 await Promise.all([page.waitForResponse(/\/user\/setUserAvatar\//).then(successful),page.locator('input[type=file]').setInputFiles('learn_server-master/img/avatars/zhixue-student-default.png')])
 const user=(await api(page,'/user/getUserInfo')).body.data;expect(user.avatar).toMatch(/^\/img\//)
 await page.reload();await expect.poll(()=>page.locator('img').evaluateAll((imgs:HTMLImageElement[],avatar:string)=>imgs.some(img=>img.src.endsWith(avatar)&&img.complete&&img.naturalWidth>0),user.avatar)).toBe(true)
 await page.route('**/user/setUserAvatar/*',route=>route.fulfill({status:401,json:{code:401,message:'Synthetic expired upload session'}}))
 await page.locator('input[type=file]').setInputFiles('learn_server-master/img/avatars/zhixue-student-default.png');await page.waitForURL('**/login');expect(await page.evaluate(()=>localStorage.getItem('user_token'))).toBeNull();expect(failures).toEqual([])
})
