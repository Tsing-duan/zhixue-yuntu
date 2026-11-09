import fs from 'node:fs'
import path from 'node:path'
import { execFileSync } from 'node:child_process'
import { createRequire } from 'node:module'
const require=createRequire(path.resolve('learn_front-master/package.json'))
const {parse}=require('@babel/parser')
const inventory=JSON.parse(fs.readFileSync('docs/modernization/baseline-inventory.json','utf8'))
let apis=0,components=0
function contracts(source){
 const result=[]
 for(const node of parse(source,{sourceType:'module'}).program.body){
  if(node.type!=='ExportNamedDeclaration'||node.declaration?.type!=='VariableDeclaration')continue
  for(const declaration of node.declaration.declarations){
   const call=declaration.init?.body
   if(call?.type==='CallExpression'&&['get','post'].includes(call.callee.name)) result.push({name:declaration.id.name,method:call.callee.name,path:call.arguments[0].value})
  }
 }
 return result
}
for(const [app,entry] of Object.entries(inventory.apps)){
 const original=execFileSync('git',['show',`72391f6:${app}/src/api/api.js`],{encoding:'utf8'})
 const baseline=contracts(original);const current=contracts(fs.readFileSync(`${app}/src/api/api.js`,'utf8'))
 if(JSON.stringify(baseline)!==JSON.stringify(current))throw new Error(`${app}: API contract changed`)
 entry.apiFunctions=baseline;apis+=baseline.length
 for(const component of entry.components){if(!fs.existsSync(`${app}/${component}`))throw new Error(`Missing component ${component}`);components++}
}
for(const row of inventory.menuRows){
 const component=row.match(/'(\/system\/[^']+)'/)
 if(component&&!fs.existsSync('learn_front_mange-master/src/views'+component[1]+'.vue'))throw new Error('Missing menu component '+component[1])
}
// --record is explicit: normal verification never rewrites the baseline manifest.
if(process.argv.includes('--record'))fs.writeFileSync('docs/modernization/baseline-inventory.json',JSON.stringify(inventory,null,2)+'\n')
for (const row of JSON.parse(fs.readFileSync('docs/modernization/component-actions.json','utf8'))) {
  const source=fs.readFileSync(row.component,'utf8')
  const current=new Set([...source.matchAll(/@([\w.:-]+)="([^"]*)"/g)].map(m=>JSON.stringify([m[1].replace('.native',''),m[2].trim()])))
  for (const action of row.baselineActions) if (!current.has(JSON.stringify(action))) throw new Error(`Missing event binding ${row.component}: ${action}`)
}
console.log(`${components} baseline components retained; ${apis} API function contracts unchanged; all SQL menu components resolve`)
