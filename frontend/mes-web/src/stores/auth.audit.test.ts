import {createPinia,setActivePinia} from 'pinia'
import {AxiosError,type AxiosResponse} from 'axios'
import {beforeEach,describe,expect,it,vi} from 'vitest'
import {useAuthStore} from './auth'
import {api,setAccessToken} from '../api/http'
vi.mock('../api/http',()=>({api:vi.fn(),setAccessToken:vi.fn(),setSessionLostHandler:vi.fn()}))
vi.mock('../auth/PlatformAuthContext',()=>({usePlatformAuthContext:()=>({setSnapshot:vi.fn()})}))
beforeEach(()=>{setActivePinia(createPinia());vi.clearAllMocks()})
describe('expired-session logout audit regression',()=>{
 it('finishes local logout when the server already rejects the expired session',async()=>{
  vi.mocked(api).mockRejectedValue(new AxiosError('Expired session','ERR_BAD_REQUEST',undefined,undefined,{status:401} as AxiosResponse))
  const auth=useAuthStore()
  await expect(auth.logout()).resolves.toBeUndefined()
  expect(api).toHaveBeenCalledWith({method:'POST',url:'/auth/logout'})
  expect(setAccessToken).toHaveBeenCalledWith(null)
  expect(auth.identity).toBeNull()
 })
 it('does not conceal a failed server revocation while still clearing local authentication',async()=>{
  vi.mocked(api).mockRejectedValue(new Error('Server unavailable'))
  const auth=useAuthStore()
  await expect(auth.logout()).rejects.toThrow('Server unavailable')
  expect(setAccessToken).toHaveBeenCalledWith(null)
  expect(auth.identity).toBeNull()
 })
})
