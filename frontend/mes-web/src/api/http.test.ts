import {afterEach,expect,it,vi} from 'vitest'
import axios,{AxiosError} from 'axios'
import {http,setAccessToken,setSessionLostHandler} from './http'
afterEach(()=>{vi.restoreAllMocks();setAccessToken(null);setSessionLostHandler(()=>{})})
it.each(['REAUTH_TOKEN_INVALID','REAUTH_FAILED'])('signed command %s does not renew login or replay consumed token',async(code)=>{
 const refresh=vi.spyOn(axios,'post').mockRejectedValue(new Error('refresh must not run'))
 const lost=vi.fn();setAccessToken('still-valid-login');setSessionLostHandler(lost)
 const adapter=vi.fn(config=>Promise.reject(new AxiosError('Reauthentication rejected','ERR_BAD_REQUEST',config,undefined,{config,data:{code,message:'Use fresh signature credential'},status:401,statusText:'Unauthorized',headers:{}})))
 await expect(http.post('/quality/specification-versions/21/approve',{reauthToken:'consumed'},{adapter})).rejects.toMatchObject({response:{status:401,data:{code}}})
 expect(adapter).toHaveBeenCalledTimes(1);expect(refresh).not.toHaveBeenCalled();expect(lost).not.toHaveBeenCalled()
})

it('expired login still renews once and retries with the renewed access token',async()=>{
 const refresh=vi.spyOn(axios,'post').mockResolvedValue({data:{data:{accessToken:'renewed-login'}}})
 const lost=vi.fn();setAccessToken('expired-login');setSessionLostHandler(lost)
 let calls=0;const adapter=vi.fn(config=>{calls++;if(calls===1)return Promise.reject(new AxiosError('Login expired','ERR_BAD_REQUEST',config,undefined,{config,data:{code:'UNAUTHORIZED'},status:401,statusText:'Unauthorized',headers:{}}));expect(config.headers.Authorization).toBe('Bearer renewed-login');return Promise.resolve({config,data:{code:'OK',data:{id:'21'}},status:200,statusText:'OK',headers:{}})})
 await expect(http.get('/quality/specification-versions/21',{adapter})).resolves.toMatchObject({data:{data:{id:'21'}}})
 expect(refresh).toHaveBeenCalledTimes(1);expect(adapter).toHaveBeenCalledTimes(2);expect(lost).not.toHaveBeenCalled()
})
