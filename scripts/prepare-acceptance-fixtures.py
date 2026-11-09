#!/usr/bin/env python3
"""Append synthetic fixtures only to our disposable MySQL instance."""
import json, re, secrets, subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; RUN=ROOT/'.run/modernization'
state=json.loads((RUN/'state.json').read_text()); db=state['database']
if not re.fullmatch(r'zhixue_migration_\d{8}_\d{6}_[a-f0-9]+',db): raise SystemExit('Not an acceptance database')
def sql(query):
 return subprocess.check_output(['mysql','--no-defaults','--protocol=TCP','-h127.0.0.1','-P13306','-uroot','-N','-B',db],input=query,text=True)
if sql('SELECT @@port').strip()!='13306':raise SystemExit('Wrong MySQL instance')
def quote(v):return "'"+str(v).replace("'","''")+"'"
def copy(table,source,changes):
 columns=[line.split('\t')[0] for line in sql(f'DESCRIBE `{table}`').splitlines()]
 expressions=[quote(changes[c]) if c in changes else f'`{c}`' for c in columns]
 sql(f'INSERT INTO `{table}` ('+','.join(f'`{c}`' for c in columns)+') SELECT '+','.join(expressions)+f' FROM `{table}` WHERE id='+quote(source))
run=secrets.token_hex(4);data={}
for browser in ['chromium','firefox','webkit']:
 chapter=f'mig-chapter-{browser}-{run}';homework=f'mig-homework-{browser}-{run}';exam=f'mig-exam-{browser}-{run}';item=f'mig-item-{browser}-{run}'
 copy('ape_chapter','1730032800929447937',{'id':chapter,'name':f'Migration chapter {browser} {run}'})
 copy('ape_homework','1730040079170961409',{'id':homework,'chapter_id':chapter,'chapter_name':f'Migration chapter {browser} {run}'})
 copy('ape_test','1730068371957653506',{'id':exam,'name':f'Migration exam {browser}','start_time':'2026-01-01 00:00:00','end_time':'2099-01-01 00:00:00'})
 copy('ape_test_item','1730103081903628289',{'id':item,'test_id':exam})
 data[browser]={'chapterName':f'Migration chapter {browser} {run}','chapter':chapter,'homework':homework,'exam':exam,'item':item}
# A synthetic foreign-owned course for the ownership canary. No new real accounts.
foreign='mig-foreign-'+run
copy('ape_task','1729751427215790082',{'id':foreign,'name':'Foreign ownership canary','teacher_id':'migration-foreign-teacher','teacher_name':'Synthetic foreign teacher'})
data['foreignCourse']=foreign
article='mig-article-'+run;forum='mig-forum-'+run
sql("INSERT INTO ape_article (id,task_id,title,article_desc,content,state,task_name,user_id,create_by,create_time,avatar) VALUES ("+','.join(map(quote,[article,'1729751427215790082','Migration public article','Synthetic article','<p>Public synthetic content</p>',0,'Portfolio Demo Course','769dc998d617bff4e30c4bde94f37e90','portfolio-demo','2026-11-06 00:00:00','/img/avatars/zhixue-default.png']))+")")
sql("INSERT INTO ape_forum (id,name,content,user_id,state,create_by,create_time) VALUES ("+','.join(map(quote,[forum,'Migration public discussion','Synthetic discussion','769dc998d617bff4e30c4bde94f37e90',0,'portfolio-demo','2026-11-06 00:00:00']))+")")
data['article']=article;data['forum']=forum
(RUN/'fixtures.json').write_text(json.dumps(data,indent=2))
media=RUN/'media';media.mkdir(exist_ok=True)
(media/'courseware.txt').write_text('Synthetic migration courseware — UTF-8 中文\n')
subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-f','lavfi','-i','color=c=blue:s=320x180:d=1','-c:v','libx264','-pix_fmt','yuv420p','-movflags','+faststart','-y',str(media/'sample.mp4')],check=True)
print('Appended isolated synthetic fixtures for three browsers; prepared local media')
