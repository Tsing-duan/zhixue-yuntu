import http from 'node:http'
import fs from 'node:fs'
import path from 'node:path'
const root = path.resolve(process.argv[2]); const port = Number(process.argv[3])
const mime = {'.html':'text/html','.js':'application/javascript','.css':'text/css','.json':'application/json','.png':'image/png','.jpg':'image/jpeg','.jpeg':'image/jpeg','.svg':'image/svg+xml','.woff':'font/woff','.ttf':'font/ttf','.mp4':'video/mp4'}
http.createServer((req,res) => {
  if (/^\/(img|video|file)\//.test(req.url)) {
    const proxy = http.request({hostname:'127.0.0.1',port:18080,path:req.url,method:req.method,headers:req.headers}, r=>{res.writeHead(r.statusCode,r.headers);r.pipe(res)})
    proxy.on('error',()=>{res.writeHead(502);res.end()});req.pipe(proxy);return
  }
  let file = path.resolve(root, '.' + decodeURIComponent(req.url.split('?')[0]))
  if (!file.startsWith(root + path.sep) && file !== root) {res.writeHead(403);res.end();return}
  if (!fs.existsSync(file) || fs.statSync(file).isDirectory()) file=path.join(root,'index.html')
  res.writeHead(200,{'Content-Type':mime[path.extname(file)]||'application/octet-stream','Cache-Control':'no-store'})
  fs.createReadStream(file).pipe(res)
}).listen(port,'127.0.0.1',()=>console.log(`Serving ${root} on ${port}`))
