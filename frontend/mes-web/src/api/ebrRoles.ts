import {api,type Page} from './http'
import type {EbrRoleOption} from '../views/ebr/types'
export const ebrRoleOptions=(keyword='',page=0)=>api<Page<EbrRoleOption>>({url:'/ebr/role-options',params:{keyword,page,size:100}})
