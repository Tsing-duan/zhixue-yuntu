"""Read-only snapshot of explicitly synthetic fixtures; no business database writes."""
import json,re,subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[1];state=json.loads((root/'.run/modernization/state.json').read_text())
assert state['ports']=={'mysql':13306,'redis':16379,'backend':18080} and re.fullmatch(r'zhixue_migration_\d{8}_\d{6}_[a-f0-9]+',state['database'])
def query(q):return subprocess.check_output(['mysql','--no-defaults','--protocol=TCP','-h127.0.0.1','-P13306','-uroot','-N','-B','--raw',state['database']],input=q,text=True).splitlines()
def camel(s):return re.sub('_([a-z])',lambda m:m[1].upper(),s)
fields={
'ape_user':['user_name','school','major','remark'],
'ape_task':['name','task_describe','teacher_name','major','classification'],
'ape_chapter':['name','task_name','remark'],
'ape_article':['title','article_desc','content','task_name','create_by'],
'ape_forum':['name','content','create_by'],
'ape_test':['name','task_name','create_by'],
'ape_task_student':['task_name','teacher_name','user_name'],
'ape_major':['name'],'ape_classification':['name'],'ape_school':['name'],
'ape_homework':['title','chapter_name','content'],
'ape_homework_student':['title','chapter_name','content'],
'ape_test_item':['title','content'],'ape_test_student':['title','content'],
'ape_account':['title','content']}
manifest={};counts={}
for table,columns in fields.items():
 pairs=','.join("'"+camel(c)+"',"+c for c in ['id']+columns)
 sql='SELECT JSON_OBJECT('+pairs+') FROM '+table
 if table=='ape_user':sql+=" WHERE login_account LIKE 'portfolio_%'"
 rows=[json.loads(line) for line in query(sql)]
 for n,row in enumerate(rows,1):
  if not any(re.search(r'(Portfolio|Migration|Synthetic|canary|mig-|portfolio-demo|^Demo (Major|Category|Classification|School))',str(v),re.I) for v in row.values()):continue
  ident=row['id'];record={}
  for field,original in row.items():
   if field=='id' or not isinstance(original,str) or not original:continue
   # Only fixture text is eligible; changed or new user-authored text will not match.
   if not re.search(r'(Portfolio|Migration|Synthetic|canary|Demo|demo)',original,re.I):continue
   zh=en=None
   if field in ['teacherName','userName','createBy']:
    if 'teacher' in original.lower():zh,en='演示教师 01','Demo Teacher 01'
    elif 'admin' in original.lower():zh,en='演示管理员','Demo Administrator'
    else:zh,en='演示学生 01','Demo Student 01'
   elif field=='school' or table=='ape_school':zh,en='演示学校','Demo School'
   elif field=='major' or table=='ape_major':zh,en='演示专业：学习与实践','Demo Major: Learning and Practice'
   elif field=='classification' or table=='ape_classification':zh,en='演示分类：基础课程','Demo Category: Fundamentals'
   elif field=='taskName':zh,en='演示课程：学习与实践','Demo Course: Learning and Practice'
   elif field=='chapterName':zh,en='演示章节：开始学习','Demo Chapter: Getting Started'
   elif table=='ape_task':
    if field=='name':zh,en=('演示课程：学习与实践','Demo Course: Learning and Practice') if 'Portfolio' in original else (f'演示课程 {n}：课程探索',f'Demo Course {n}: Course Discovery')
    else:zh,en='用于体验课程、章节和作业流程的演示内容，不代表真实教学活动。','Example content for exploring courses, chapters and assignments; not real teaching activity.'
   elif table=='ape_chapter':zh,en='演示章节：开始学习','Demo Chapter: Getting Started'
   elif table=='ape_article':
    if field=='title':zh,en=f'演示笔记 {n}：整理学习思路',f'Demo Note {n}: Organizing Study Ideas'
    elif field=='content':zh,en='<p>这是一篇演示笔记。可以先记录课程主线，再写下疑问，最后安排一次复习。</p>','<p>This is a demo note. Start with the main ideas, record questions, and schedule a review.</p>'
    else:zh,en='示例：用笔记记录重点、疑问与下一步行动。','Example: record key ideas, questions and next steps.'
   elif table=='ape_forum':
    if field=='name':zh,en=f'演示讨论 {n}：如何安排每周学习？',f'Demo Discussion {n}: Planning a Study Week'
    else:zh,en='这是用于体验讨论功能的示例问题：你会怎样安排课程学习、做笔记和复习的时间？','This demo question illustrates discussions: how would you divide time between courses, notes and review?'
   elif table=='ape_test':zh,en=f'演示考试 {n}：基础练习',f'Demo Exam {n}: Basic Practice'
   elif table in ['ape_homework','ape_homework_student','ape_test_item','ape_test_student']:
    if field=='title':zh,en='演示题目：请选择示例答案','Demo question: choose an example answer'
    elif field=='content':
     try:
      a=json.loads(original);b=json.loads(original)
      for i,item in enumerate(a):
       if isinstance(item,dict) and 'option' in item:item['option']='演示选项 '+str(item.get('value',i+1));b[i]['option']='Example option '+str(item.get('value',i+1))
      zh,en=json.dumps(a,ensure_ascii=False),json.dumps(b,ensure_ascii=False)
     except (ValueError,TypeError):pass
   elif table=='ape_account':zh,en=('演示公告：欢迎体验学习平台','Demo announcement: welcome to the learning platform') if field=='title' else ('<p>本环境用于体验学习流程，展示内容均为演示样例。</p>','<p>This environment demonstrates learning workflows. All sample content is for demonstration.</p>')
   elif table=='ape_user' and field=='remark':zh,en='演示账号介绍，用于体验平台功能。','Demo account profile for exploring platform features.'
   if zh:record[field]={'original':original,'zh-CN':zh,'en':en}
  if record:manifest.setdefault(ident,{}).update(record);counts[table]=counts.get(table,0)+1
(root/'shared/i18n/demo-content.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
(root/'output/playwright/i18n/demo-snapshot.json').write_text(json.dumps({'readOnly':True,'databaseGuard':'isolated migration database on 13306','records':counts},indent=2))
print('Whitelisted synthetic display records:',counts)
