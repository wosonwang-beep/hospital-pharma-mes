import type {EbrDefinition,EbrForm,EbrField} from './types'
import {newField} from './model'
export type RecordComponent='CHECKS'|'PARAMETERS'|'MATERIAL_REFERENCES'|'CLEARANCE'
export const recordComponents:Record<RecordComponent,string>={CHECKS:'检查项（不默认勾选）',PARAMETERS:'标准/实际过程参数',MATERIAL_REFERENCES:'投料来源引用（不扣库存）',CLEARANCE:'清场证据引用'}
export function appendRecordComponent(definition:EbrDefinition,form:EbrForm,component:RecordComponent){
 const prefix=`${form.formCode}_${component}`;let suffix=1;const existing=new Set(definition.forms.flatMap(f=>f.fields.map(v=>v.fieldCode)));while([...existing].some(c=>c.startsWith(`${prefix}_${suffix}_`)))suffix++
 const group=`${prefix}_${suffix}`;const sectionCode=`${group}_SEC`
 definition.sections.push({sectionCode,title:recordComponents[component],sequenceNo:definition.sections.length+1,repeatMode:'NONE',visibilityRuleCode:null,pageBreakFlag:false,groups:[{groupCode:group,title:recordComponents[component],sequenceNo:1,layoutColumns:2,repeatMode:'LIST',minOccurs:0,maxOccurs:500}]})
 const add=(code:string,label:string,type:EbrField['fieldType'],readonly=false)=>{const field=newField(`${group}_${code}`,type);field.groupCode=group;field.label=label;field.sequenceNo=form.fields.length+1;field.readonlyFlag=readonly;field.requiredFlag=false;field.defaultExpr=null;form.fields.push(field);return field}
 if(component==='CHECKS'){add('ITEM','检查项目','TEXT');add('STANDARD','标准/要求（引用冻结规程）','TEXT');const value=add('ACTUAL','实际检查结果','ENUM');value.options=[{optionCode:'NORMAL',optionLabel:'正常',optionValue:'NORMAL',sequenceNo:1,activeFlag:true},{optionCode:'ABNORMAL',optionLabel:'异常',optionValue:'ABNORMAL',sequenceNo:2,activeFlag:true}];add('NOTE','异常说明','TEXTAREA')}
 if(component==='PARAMETERS'){add('NAME','参数名称','TEXT');add('STANDARD','标准/规定（冻结工艺）','TEXT');add('ACTUAL','实际记录','TEXT');add('UNIT','单位','TEXT');add('RULE','已确认计算规则引用（如适用）','TEXT').helpText='计算分母和单位必须来自已确认规则；不从照片推导处方、容差或投水量。'}
 if(component==='MATERIAL_REFERENCES'){add('CHARGE','实际投料记录ID','TEXT').helpText='仅引用现有投料记录；实际数量、物料批号、厂家、单位由整册投料目录读取，不重复扣库存。';add('NOTE','关联说明','TEXTAREA')}
 if(component==='CLEARANCE'){add('RECORD','现有清场记录ID','TEXT').helpText='复核及电子签名由原清场功能完成；整册目录只读展示，不新造正常确认或签名。';add('NOTE','关联说明','TEXTAREA')}
 return group
}
