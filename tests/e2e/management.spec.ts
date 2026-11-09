import {test,expect,type Page} from '@playwright/test'
import {navigate,login,origin,api,errors} from './helpers'
const label=()=>`Migration ${Date.now()}`
async function save(page:Page,path:string){await Promise.all([page.waitForResponse('**/'+path).then(async r=>expect((await r.json()).code).toBe(1000)),page.getByRole('dialog').getByRole('button',{name:/确\s*定/}).click()]);await expect(page.locator('[role=dialog][aria-modal=true]:visible')).toHaveCount(0)}
test('administrator creates roles with checked menu permissions and button menus',async({page})=>{
 const failures=errors(page);await login(page,'admin');await navigate(page,origin('admin')+'/role')
 const name=label();await page.getByRole('button',{name:'新增',exact:true}).click()
 let dialog=page.getByRole('dialog');await dialog.getByPlaceholder('请输入用户名称').fill(name);await dialog.getByPlaceholder('请输入权限字符').fill('migration_'+Date.now())
 await dialog.locator('.el-tree .el-checkbox').first().click()
 const request=page.waitForRequest('**/role/saveRole');await save(page,'role/saveRole');expect((await request).postDataJSON().menuIds.length).toBeGreaterThan(0)
 let roles=(await api(page,'/role/getRolePage',{pageNumber:1,pageSize:100,roleName:name})).body.data.records
 expect(roles.find((r:any)=>r.roleName===name)).toBeTruthy()
 const row=page.locator('.el-table__row').filter({hasText:name});await row.getByRole('button',{name:'修改',exact:true}).click();dialog=page.getByRole('dialog');await expect(dialog.getByPlaceholder('请输入用户名称')).toHaveValue(name);await dialog.getByPlaceholder('请输入用户名称').fill(name+' edited');await save(page,'role/editRole')
 expect((await api(page,'/role/removeRole?ids='+roles.find((r:any)=>r.roleName===name).id)).body.code).toBe(1000)
 await navigate(page,origin('admin')+'/menu');await page.getByRole('button',{name:'新增',exact:true}).first().click();dialog=page.getByRole('dialog');await dialog.locator('.el-radio').filter({hasText:'按钮'}).click();await dialog.getByPlaceholder('请输入菜单名称').fill(name);await dialog.getByPlaceholder('请输入权限字符').fill('migration:synthetic:'+Date.now());await save(page,'menu/saveMenu')
 const menus=(await api(page,'/menu/getMenuList',{})).body.data;const flatten=(items:any[]):any[]=>items.flatMap(m=>[m,...flatten(m.children||[])]);const menu=flatten(menus).find((m:any)=>m.menuName===name);expect(menu.menuType).toBe(2);expect((await api(page,'/menu/removeMenu?id='+menu.id)).body.code).toBe(1000);expect(failures).toEqual([])
})
test('teacher creates course with cover preview, homework and an exam using date controls',async({page})=>{
 const failures=errors(page);await login(page,'teacher');await navigate(page,origin('teacher')+'/teacherTask');const name=label();await page.getByRole('button',{name:'新增',exact:true}).click();let dialog=page.getByRole('dialog')
 await dialog.getByPlaceholder('请输入课程名称').fill(name);await dialog.getByPlaceholder('请输入视频和作业分数所占比例').fill('60');await dialog.getByPlaceholder('请输入课程描述').fill('Synthetic migration course')
 for(let i=0;i<2;i++){await dialog.locator('.el-select').nth(i).click();await page.getByRole('option',{name:i===0?'Demo Major':'Demo Classification',exact:true}).click()}
 await Promise.all([page.waitForResponse('**/common/uploadImg').then(async r=>expect((await r.json()).code).toBe(1000)),dialog.locator('input[type=file]').setInputFiles('learn_server-master/img/avatars/zhixue-default.png')])
 await dialog.locator('.el-upload-list__item').hover();await dialog.locator('.el-upload-list__item-preview').click();await expect(page.getByRole('dialog')).toHaveCount(2);await page.getByRole('dialog').last().locator('.el-dialog__headerbtn').click();await expect(page.getByRole('dialog')).toHaveCount(1)
 await save(page,'task/saveApeTask');const course=(await api(page,'/task/getApeTaskPage',{pageNumber:1,pageSize:100,name})).body.data.records.find((r:any)=>r.name===name);expect(course.image).toMatch(/^\/img\//)
 await navigate(page,origin('teacher')+'/homework?id=1730032800929447937');await page.getByRole('button',{name:'新增',exact:true}).click();dialog=page.getByRole('dialog');await dialog.getByPlaceholder('请输入题目').fill(name);await dialog.locator('.el-select').click();await page.getByRole('option',{name:'填空',exact:true}).click();await dialog.getByPlaceholder('请输入答案（多选多个答案用英文逗号分隔）').fill('synthetic');await dialog.getByRole('spinbutton').nth(0).fill('1');await dialog.getByRole('spinbutton').nth(1).fill('10');await save(page,'homework/saveApeHomework')
 await navigate(page,origin('teacher')+'/teacherTest');await page.getByRole('button',{name:'新增',exact:true}).click();dialog=page.getByRole('dialog');await dialog.getByPlaceholder('请输入考试名称').fill(name);await dialog.locator('.el-select').click();await page.getByRole('option',{name,exact:true}).click();await dialog.getByPlaceholder('请输入总分').fill('100');await dialog.getByPlaceholder('请输入考试时长').fill('30')
 for(const [placeholder,date] of [['开始时间','2026-11-06'],['截止时间','2099-01-01']]){await dialog.getByPlaceholder(placeholder,{exact:true}).fill(date);await dialog.getByPlaceholder(placeholder,{exact:true}).press('Enter');await dialog.getByPlaceholder('请输入考试名称').click()}
 await save(page,'test/saveApeTest');const exams=(await api(page,'/test/getApeTestPage',{pageNumber:1,pageSize:100,name})).body.data.records;expect(exams.find((r:any)=>r.name===name).startTime).toContain('2026-11-06');expect(failures).toEqual([])
})
test('administrator user form creates, edits and deletes a synthetic account',async({page})=>{
 const failures=errors(page);await login(page,'admin');await navigate(page,origin('admin')+'/user');const name=label();await page.getByRole('button',{name:'新增',exact:true}).click();let dialog=page.getByRole('dialog')
 await dialog.getByPlaceholder('请输入用户昵称').fill(name);await dialog.getByPlaceholder('请输入登陆账号').fill('migration_'+Date.now());await dialog.getByPlaceholder('请输入用户密码').fill('MigrationTest2026');await save(page,'user/saveUser')
 const users=(await api(page,'/user/getUserPage',{pageNumber:1,pageSize:100,userName:name})).body.data.records;const user=users.find((u:any)=>u.userName===name);expect(user).toBeTruthy()
 await page.getByPlaceholder('请输入用户名称').fill(name);await page.getByRole('button',{name:'查询',exact:true}).click();const row=page.locator('.el-table__row').filter({hasText:name});await row.locator('.operation-dropdown-trigger').hover();await page.getByRole('menuitem',{name:'修改',exact:true}).click();dialog=page.getByRole('dialog');await expect(dialog.getByPlaceholder('请输入用户昵称')).toHaveValue(name);await dialog.getByPlaceholder('请输入邮箱').fill('migration@example.invalid');await save(page,'user/editUser')
 expect((await api(page,'/user/getUserById?id='+user.id)).body.data.email).toBe('migration@example.invalid');expect((await api(page,'/user/removeUser?ids='+user.id)).body.code).toBe(1000);expect(failures).toEqual([])
})
