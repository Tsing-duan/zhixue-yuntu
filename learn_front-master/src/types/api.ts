/** Existing backend contracts; no wire-format changes. */
export interface ApiResult<T = any> { code: number; message: string; data: T }
export interface User { id: string; userType: number; userName: string; loginAccount: string; avatar?: string }
export interface Menu { id: string; parentId: string; menuName: string; menuType: number; visible: number; routeUrl: string; componentUrl: string; target: number; param?: string; perms?: string; icon?: string; child?: Menu[] }
export interface Tab { name: string; url: string }
