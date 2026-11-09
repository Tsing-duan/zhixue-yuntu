"""Compile reviewed UI messages and regular field-label variants; never translate user data."""
import json,re
from pathlib import Path
root=Path(__file__).resolve().parents[1]
base={line.split('|',1)[0]:line.split('|',1)[1] for line in (root/'shared/i18n/translations.txt').read_text().splitlines() if '|' in line}
# Repeated labels use the same reviewed field nouns; generated results remain inspectable JSON.
def translate(s,depth=0):
 if s in base:return base[s]
 if depth>4:return None
 stripped=s.strip()
 if stripped!=s:return translate(stripped,depth+1)
 normalized=re.sub(r'(?<=[\u4e00-\u9fff])\s+(?=[\u4e00-\u9fff])','',s)
 if normalized!=s:return translate(normalized,depth+1)
 for prefix,english in [('请输入','Enter '),('请选择','Select '),('新增','Add '),('编辑','Edit '),('修改','Edit '),('重新加载','Reload '),('正在加载','Loading ')]:
  if s.startswith(prefix):
   tail=s[len(prefix):].rstrip('… .');part=translate(tail,depth+1)
   if part:return english+part.lower()+('…' if prefix=='正在加载' else '')
 for suffix,english in [('加载失败',' could not load'),('列表',' list'),('信息',' information'),('内容',' content'),('失败',' failed')]:
  if s.endswith(suffix):
   part=translate(s[:-len(suffix)],depth+1)
   if part:return part+english
 if s.startswith('获取'):
  part=translate(s[2:],depth+1)
  if part:return part
 if s.endswith('，请稍后重试'):
  part=translate(s[:-7],depth+1)
  if part:return part+'. Please try again.'
 if s.endswith(('：',':','!','！','。','?','？',' →',' ↗')):
  m=re.search(r'([：:!！。?？]| →| ↗)$',s);part=translate(s[:m.start()],depth+1)
  if part:return part+{'：':': ',':':': ','！':'!','。':'.','？':'?'}.get(m[0],m[0])
 if s.startswith('【') and s.endswith('】'):
  part=translate(s[1:-1],depth+1)
  if part:return '['+part+']'
 m=re.match(r'^([^\w\u4e00-\u9fff]+)(.+)$',s)
 if m:
  part=translate(m[2],depth+1)
  if part:return m[1]+part
 return None
inventory_path=root/'output/playwright/i18n/inventory.json'
inventory=json.loads(inventory_path.read_text()) if inventory_path.exists() else {}
existing=json.loads((root/'shared/i18n/en.json').read_text())
keys=set(inventory)|set(base)|set(existing)
catalog={k:translate(k) for k in sorted(keys) if translate(k) is not None}
(root/'shared/i18n/en.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n')
missing={k:v for k,v in inventory.items() if k not in catalog and any(x['kind']!='script' for x in v)}
(root/'output/playwright/i18n').mkdir(parents=True,exist_ok=True)
(root/'output/playwright/i18n/untranslated-static.json').write_text(json.dumps(missing,ensure_ascii=False,indent=2))
print('Catalog:',len(catalog),'Static untranslated:',len(missing))
print('\n'.join(missing))
