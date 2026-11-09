// @vitest-environment jsdom
import { it, expect, vi, beforeEach } from 'vitest'
vi.mock('./request', () => ({ handleUnauthorized: vi.fn() }))
vi.mock('element-plus', () => ({ ElMessage: { error: vi.fn() } }))
import { handleUnauthorized } from './request'
import { handleUploadError } from './upload'
beforeEach(() => { vi.clearAllMocks(); vi.stubGlobal('location', { pathname: '/center', replace: vi.fn() }) })
it('applies the common logout boundary to an expired upload session', () => {
 handleUploadError({status:401}); expect(handleUnauthorized).toHaveBeenCalledTimes(1)
})
it('keeps the session for a forbidden upload and shows the permission page', () => {
 handleUploadError({status:403}); expect(handleUnauthorized).not.toHaveBeenCalled(); expect(location.replace).toHaveBeenCalledWith('/403')
})
