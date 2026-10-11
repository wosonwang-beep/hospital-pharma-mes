import {expect,test} from 'vitest'
import {isOrdinaryPermission,permissionChanges} from './menuConfigurationModel'
import type {Permission} from '../../api/http'
const permission=(code:string):Permission=>({id:'1',version:3,permissionCode:code,permissionName:'名称',permissionType:'ACTION',status:'ACTIVE'})
test('sensitive permissions never enter ordinary presets',()=>{for(const action of ['approve','sign','release','invalidate','export'])expect(isOrdinaryPermission(permission(`qms:report:${action}`))).toBe(false);expect(isOrdinaryPermission(permission('qms:report:update'))).toBe(true)})
test('description changes retain permission identity and expected version',()=>{const old=permission('qms:report:export');expect(permissionChanges([{...old,description:'说明'}],[old],[old.permissionCode])).toEqual([{id:'1',version:3,permissionCode:old.permissionCode,permissionName:'名称',description:'说明',status:'ACTIVE'}])})
test('unlink never creates a global permission deletion or unrelated update',()=>{const old=permission('qms:report:export');expect(permissionChanges([{...old,description:'编辑后解除'}],[old],[])).toEqual([]);expect(permissionChanges([old],[old],[old.permissionCode])).toEqual([])})
