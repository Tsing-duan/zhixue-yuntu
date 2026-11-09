if (!process.env.ZHIXUE_DEMO_PASSWORD) throw new Error('Set ZHIXUE_DEMO_PASSWORD for isolated demo verification')
import { expect, type Page, type Request } from '@playwright/test'
import fs from 'node:fs'
const state = JSON.parse(fs.readFileSync('.run/modernization/state.json', 'utf8'))
if (!/^zhixue_migration_\d{8}_\d{6}_[a-f0-9]+$/.test(state.database) || state.ports.backend !== 18080) throw new Error('Tests require isolated migration environment')
export type Role = 'student' | 'teacher' | 'admin'
export const origin = (role: Role) => `http://127.0.0.1:${role === 'student' ? 15001 : 15000}`
const pendingReads = new WeakMap<Page, { requests: Set<Request>; changed: number }>()
function trackReads(page: Page) {
  let tracker = pendingReads.get(page)
  if (tracker) return tracker
  tracker = { requests: new Set(), changed: Date.now() }; pendingReads.set(page, tracker)
  const state = tracker
  page.on('request', request => {
    if (['xhr','fetch'].includes(request.resourceType()) && request.url().includes('127.0.0.1:18080')) { state.requests.add(request); state.changed=Date.now() }
  })
  const finished=(request: Request)=>{ if (state.requests.delete(request)) state.changed=Date.now() }
  page.on('requestfinished',finished); page.on('requestfailed',finished)
  return tracker
}
export async function settle(page: Page) {
  const state=trackReads(page); const deadline=Date.now()+8000
  while(state.requests.size || Date.now()-state.changed<250) {
    if(Date.now()>deadline)throw new Error('Unfinished API requests: '+[...state.requests].map(r=>new URL(r.url()).pathname).join(', '))
    await page.waitForTimeout(25)
  }
}
export async function navigate(page: Page, url: string) {
  await settle(page)
  await page.goto(url)
  await expect(page.locator('#app')).not.toBeEmpty()
  await settle(page)
}
const dialogHandled = new WeakSet<Page>()
export async function login(page: Page, role: Role) {
  if (!dialogHandled.has(page)) {
    // Route inventory leaves untouched synthetic exam forms; accept only navigation warnings.
    page.on('dialog', dialog => dialog.type() === 'beforeunload' ? dialog.accept() : dialog.dismiss())
    dialogHandled.add(page)
  }
  // Defense in depth: no browser-driven request may reach an external AI host.
  await page.route(/https:\/\/api\.deepseek\.com\/.*/, route => route.abort())
  await navigate(page, origin(role) + '/login')
  await page.locator('input').first().fill(`portfolio_${role}_01`)
  await page.locator('input[type=password]').fill(process.env.ZHIXUE_DEMO_PASSWORD)
  await page.locator('button').filter({ hasText: /登\s*[录陆]/ }).first().click()
  await page.waitForURL(url => url.pathname !== '/login')
  await expect(page.locator('#app')).not.toBeEmpty()
  await settle(page)
}
export async function api(page: Page, path: string, data?: unknown) {
  return page.evaluate(async ({path,data}) => {
    const token=localStorage.getItem('token') || localStorage.getItem('user_token') || ''
    const response=await fetch('http://127.0.0.1:18080'+path, {method:data===undefined?'GET':'POST',headers:{'Content-Type':'application/json','x_access_token':token},body:data===undefined?undefined:JSON.stringify(data)})
    return {status:response.status,body:await response.json()}
  }, {path,data})
}
export function errors(page: Page) { const list: string[]=[];page.on('pageerror', e=>list.push(e.message));return list }
