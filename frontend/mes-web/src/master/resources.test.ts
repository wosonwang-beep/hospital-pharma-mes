import { describe,it,expect } from 'vitest'
import {commandBody,resource,resources} from './resources'
describe('MES-003 controlled master forms',()=>{
 it('TC-UI-001 has distinct query/create/view/edit route definitions',()=>{expect(resources.map(r=>r.key)).toEqual(['materials','suppliers','organizations','units','unit-conversions','equipment','qualifications'])})
 it('cannot send client status or immutable equipment code in a command',()=>{const body=commandBody(resource('equipment'),{equipmentCode:'spoof',equipmentName:'Mixer',equipmentType:'MIXER',calibrationDueDate:null,location:'A',status:'INACTIVE'},true,'DISABLE','停用');expect(body).toEqual({equipmentName:'Mixer',equipmentType:'MIXER',calibrationDueDate:null,location:'A',action:'DISABLE',reason:'停用'})})
 it('requires reason and required fields before request',()=>{expect(()=>commandBody(resource('units'),{},false)).toThrow('请填写');expect(()=>commandBody(resource('units'),{unitName:'kg',dimension:'MASS',scale:3},true)).toThrow('变更原因')})
})
