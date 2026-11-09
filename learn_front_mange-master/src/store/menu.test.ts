// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
vi.mock('@/api/api', () => ({ getMenuByUser: vi.fn() }))
import { getMenuByUser } from '@/api/api'
import { useMenuStore } from './menu'
import { buildMenuRoutes, displayMenus } from '@/router/menu'
import { resetSessionResources } from '@/utils/sessionLifecycle'
import type { Menu } from '@/types/api'
const menu: Menu = {id:'one',parentId:'0',menuName:'课程',menuType:1,visible:0,routeUrl:'/task',componentUrl:'/system/task/task',target:1}
beforeEach(() => { setActivePinia(createPinia()); vi.resetAllMocks() })
describe('dynamic route boundary', () => {
 it('preserves server paths and rejects unknown components before registration', () => {
   const routes=buildMenuRoutes([menu]);expect(routes.find(r=>r.name==='main')?.children?.[0].path).toBe('/task')
   expect(()=>buildMenuRoutes([{...menu,componentUrl:'/missing'}])).toThrow('菜单组件不存在')
   expect(()=>buildMenuRoutes([menu,{...menu,id:'two'}])).toThrow('重复')
 })
 it('keeps distinct URLs even when legacy menus share the same display name', () => {
   const children=buildMenuRoutes([menu,{...menu,id:'two',routeUrl:'/other'}]).find(r=>r.name==='main')!.children!
   expect(children).toHaveLength(2);expect(new Set(children.map(r=>r.name)).size).toBe(2)
   expect(children.map(r=>r.meta?.title)).toEqual(['课程','课程'])
 })
 it('builds display trees without mutating the original response', () => {
   const data=[{...menu,menuType:0},{...menu,id:'child',parentId:'one'}]
   expect(displayMenus(data)[0].child).toHaveLength(1);expect(data[0]).not.toHaveProperty('child')
 })
 it('rejects both network and business failures without hanging or marking ready', async () => {
   vi.mocked(getMenuByUser).mockResolvedValue({code:1001,message:'failed',data:null})
   const store=useMenuStore();await expect(store.generateRoutes()).rejects.toThrow('failed');expect(store.flag).toBe(false)
   vi.mocked(getMenuByUser).mockRejectedValue(new Error('offline'));await expect(store.generateRoutes()).rejects.toThrow('offline')
 })
 it('discards a response received after an account switch', async () => {
   let finish: (value: any)=>void=()=>{};vi.mocked(getMenuByUser).mockImplementation(()=>new Promise(resolve=>{finish=resolve}))
   const store=useMenuStore();const pending=store.generateRoutes();resetSessionResources();finish({code:1000,message:'ok',data:[menu]})
   await expect(pending).rejects.toThrow('身份已变化');expect(store.menus).toEqual([]);expect(store.flag).toBe(false)
 })
 it('deduplicates tabs and can reset all account state', () => {
   const store=useMenuStore();store.addActiveMenu({name:'one',url:'/task'});store.addActiveMenu({name:'one',url:'/task'});expect(store.activeMenuArrary).toHaveLength(1)
   store.flag=true;store.$reset();expect(store.activeMenuArrary).toEqual([]);expect(store.flag).toBe(false)
 })
})
