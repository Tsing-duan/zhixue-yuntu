#!/usr/bin/env python3
import os
"""Real HTTP authorization checks; disposable fixtures only in the guarded isolated database."""
import json,re,subprocess,uuid,urllib.request,urllib.error
from pathlib import Path
state=json.loads(Path('.run/modernization/state.json').read_text())
assert re.fullmatch(r'zhixue_migration_\d{8}_\d{6}_[a-f0-9]+',state['database'])
assert state['ports']=={'mysql':13306,'redis':16379,'backend':18080}
def sql(query):
 return subprocess.check_output(['mysql','--no-defaults','--protocol=TCP','-h127.0.0.1','-P13306','-uroot','-N','-B',state['database']],input=query,text=True).strip()
assert sql('SELECT @@port')=='13306'
report=[]
def api(path,data=None,token=None):
 req=urllib.request.Request('http://127.0.0.1:18080'+path,data=json.dumps(data).encode() if data is not None else None,headers={'Content-Type':'application/json',**({'x_access_token':token} if token else {})})
 try:
  with urllib.request.urlopen(req) as r:return r.status,json.load(r)
 except urllib.error.HTTPError as e:return e.code,json.load(e)
def ok(path,data,token):
 status,body=api(path,data,token);assert status==200 and body.get('code')==1000,(path,status,body);return body.get('data')
def denied(path,data,token):
 status,body=api(path,data,token);assert status in [401,403] or body.get('code') in [1006,1008,1009,1011],(path,status,body)
 return status
ids=[uuid.uuid4().hex for _ in range(7)];active,disabled,deleted,open_notice,closed_notice,admin_notice,new_user=ids
name='portal-test-'+uuid.uuid4().hex[:8]
try:
 for id,status,delete in [(active,0,0),(disabled,1,0),(deleted,0,1)]:
  sql(f"INSERT INTO ape_user (id,user_name,user_type,status,del_flag,school,agree,remark,login_account,tel,email,address,login_ip) VALUES ('{id}','{name}',1,{status},{delete},'Test School','Lecturer','Public introduction','private-{id}','private-phone','private-email','private-address','private-ip')")
 for id,stateval in [(open_notice,0),(closed_notice,1)]:
  sql(f"INSERT INTO ape_account (id,title,state,type,content,create_by,create_time) VALUES ('{id}','{name}',{stateval},0,'<p>Read only notice</p>','private-author',NOW())")
 for role in ['student','teacher','admin']:
  token=ok('/login/student' if role=='student' else '/login',{'username':f'portfolio_{role}_01','password':os.environ['ZHIXUE_DEMO_PASSWORD']},None)['token']
  teacher=ok('/portal/teachers/'+active,None,token)
  assert set(teacher)=={'id','userName','avatar','school','agree','remark'},teacher
  assert 'private-' not in json.dumps(teacher)
  listing=ok('/portal/teachers',{'userName':name,'userType':0,'status':1,'pageNumber':1,'pageSize':99999},token)
  assert listing['size']==60 and [r['id'] for r in listing['records']]==[active],listing
  assert ok('/portal/teachers',{'userName':name,'school':'Other School'},token)['total']==0
  for id in [disabled,deleted,'missing']:
   assert api('/portal/teachers/'+id,None,token)[1]['code']!=1000
  own=ok('/user/getUserInfo',None,token)
  if role!='teacher':assert api('/portal/teachers/'+own['id'],None,token)[1]['code']!=1000
  notices=ok('/portal/notices',{'pageSize':60,'state':1},token)
  assert open_notice in [n['id'] for n in notices['records']] and closed_notice not in [n['id'] for n in notices['records']]
  for row in notices['records']:assert set(row)=={'id','title','content'}
  denied_status=[]
  if role!='admin':
   for path,data in [('/user/getUserPage',{}),('/user/getUserById?id='+active,None),('/user/getUserListByType?type=0',None),('/account/getAccountPage',{}),('/account/getById?id='+closed_notice,None),('/user/saveUser',{'id':new_user,'loginAccount':'unauthorized','password':'ValidPassword123','userType':0}),('/user/editUser',{'id':active,'userName':'tampered'}),('/user/removeUser?ids='+active,None),('/account/saveAccount',{'id':admin_notice,'title':'tampered'}),('/account/editAccount',{'id':open_notice,'title':'tampered'}),('/account/removeAccount?ids='+open_notice,None)]:
    denied_status.append({'path':path.split('?')[0],'status':denied(path,data,token)})
   assert sql(f"SELECT user_name FROM ape_user WHERE id='{active}'")==name
   assert sql(f"SELECT title FROM ape_account WHERE id='{open_notice}'")==name
  else:
   ok('/user/getUserPage',{'pageNumber':1,'pageSize':10},token)
   ok('/user/getUserById?id='+active,None,token)
   ok('/account/getAccountPage',{'pageNumber':1,'pageSize':10},token)
   ok('/account/getById?id='+closed_notice,None,token)
   ok('/account/saveAccount',{'id':admin_notice,'title':name,'type':0,'state':0,'content':'admin-create'},token)
   ok('/account/editAccount',{'id':admin_notice,'title':name+'-edited'},token)
   assert sql(f"SELECT title FROM ape_account WHERE id='{admin_notice}'")==name+'-edited'
   ok('/account/removeAccount?ids='+admin_notice,None,token)
   assert sql(f"SELECT COUNT(*) FROM ape_account WHERE id='{admin_notice}'")=='0'
  report.append({'role':role,'teacherRead':True,'noticeRead':True,'sensitiveFieldsExcluded':True,'hiddenRecordsExcluded':True,'denied':denied_status,'adminNoticeCRUD':role=='admin'})
 for path,data in [('/portal/teachers',{}),('/portal/teachers/'+active,None),('/portal/notices',{})]:denied(path,data,None)
 report.append({'anonymous':'all portal reads denied'})
finally:
 quoted=','.join("'"+i+"'" for i in ids)
 sql(f'DELETE FROM ape_account WHERE id IN ({quoted}); DELETE FROM ape_user WHERE id IN ({quoted});')
Path('output/playwright/student-access/api-verification.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
