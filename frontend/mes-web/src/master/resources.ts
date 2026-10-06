export interface MasterField { key: string; label: string; kind?: 'number' | 'date' | 'select'; choices?: string[]; required?: boolean; immutable?: boolean; max?: number }
export interface MasterResource { key: string; title: string; permission: string; fields: MasterField[]; requirement: string; filters: MasterField[] }
const text = (key:string,label:string,required=true,immutable=false,max=200):MasterField => ({key,label,required,immutable,max})
const date = (key:string,label:string):MasterField => ({key,label,kind:'date'})
const select = (key:string,label:string,choices:string[]):MasterField => ({key,label,kind:'select',choices})
export const statusLabel:Record<string,string> = { ACTIVE:'启用',INACTIVE:'停用',MAINTENANCE:'维护中',DRAFT:'草稿',SUBMITTED:'已提交',APPROVED:'已批准',UNAPPROVED:'未批准',ENTERPRISE:'企业',FACTORY:'工厂',WORKSHOP:'车间',LINE:'产线' }
export const actionLabel:Record<string,string> = { QUALIFY:'资格批准',ENABLE:'启用',DISABLE:'停用',BEGIN_MAINTENANCE:'进入维护',RETURN_TO_SERVICE:'结束维护' }
export const resources: MasterResource[] = [
 {key:'materials',title:'物料主数据',permission:'material',requirement:'MD-MAT-001 / MD-MAT-002',fields:[text('materialCode','物料编码',true,true,50),text('materialName','物料名称'),text('materialType','物料类型',true,false,30),text('specification','规格',false),text('baseUnitName','基本单位'),text('manufacturerName','生产厂家',false)],filters:[text('materialType','物料类型',false),select('status','状态',['ACTIVE','INACTIVE'])]},
 {key:'suppliers',title:'供应商',permission:'supplier',requirement:'MD-SUP-001',fields:[text('supplierCode','供应商编码',true,true,64),text('supplierName','供应商名称'),date('validTo','资格有效截止日期（UTC）')],filters:[select('qualificationStatus','资格状态',['UNAPPROVED','APPROVED','INACTIVE'])]},
 {key:'organizations',title:'组织',permission:'org',requirement:'MD-001',fields:[text('orgCode','组织编码',true,true,64),text('orgName','组织名称'),{...select('orgType','组织类型',['ENTERPRISE','FACTORY','WORKSHOP','LINE']),required:true,immutable:true},text('parentId','上级组织',false)],filters:[select('orgType','组织类型',['ENTERPRISE','FACTORY','WORKSHOP','LINE']),select('status','状态',['ACTIVE','INACTIVE'])]},
 {key:'units',title:'单位',permission:'uom',requirement:'MD-002',fields:[text('unitCode','单位编码',true,true,32),text('unitName','单位名称',true,false,64),text('dimension','量纲',true,false,30),{key:'scale',label:'小数精度（0–12）',kind:'number',required:true}],filters:[text('dimension','量纲',false)]},
 {key:'unit-conversions',title:'单位换算',permission:'uom',requirement:'MD-002',fields:[text('fromUnitId','原单位'),text('toUnitId','目标单位'),text('factor','换算因子（十进制）',true,false,40),text('materialId','专属换算物料',false)],filters:[text('fromUnitId','原单位',false),text('toUnitId','目标单位',false)]},
 {key:'equipment',title:'设备与人员资格',permission:'equipment',requirement:'MD-EQP-001',fields:[text('equipmentCode','设备编码',true,true,64),text('equipmentName','设备名称'),text('equipmentType','设备类型',true,false,64),date('calibrationDueDate','校准到期日期（UTC）'),text('location','位置',false)],filters:[text('equipmentType','设备类型',false),select('status','状态',['ACTIVE','MAINTENANCE','INACTIVE']),date('calibrationDueDate','校准到期不晚于')]},
 {key:'qualifications',title:'人员资格',permission:'qualification',requirement:'MD-QUAL-001',fields:[text('userId','人员',true,true),text('qualificationCode','资格编码',true,true,64),date('validFrom','有效起始日期（UTC）'),date('validTo','有效截止日期（UTC）')],filters:[text('userId','人员',false),text('qualificationCode','资格编码',false),select('status','状态',['ACTIVE','INACTIVE'])]}
]
export function resource(key:string) { const found=resources.find(r=>r.key===key); if(!found)throw new Error('Unknown master resource');return found }
export interface MasterRecord {id:string;orgId:string;versionNo:number;status?:string;updatedAt:string;allowedActions:string[];[key:string]:unknown}
export function commandBody(def:MasterResource, form:Record<string,string|number|null>, editing:boolean, action='UPDATE', reason='') {
 const fields=def.fields.filter(f=>!editing || !f.immutable)
 const body:Record<string,unknown>={}
 for(const f of fields){const value=form[f.key]; if(f.required && (value===null||value===undefined||String(value).trim()===''))throw new Error(`请填写${f.label}`);body[f.key]=value===''||value===undefined?null:value}
 if(editing){if(!reason.trim())throw new Error('请填写变更原因');Object.assign(body,{action,reason:reason.trim()})}
 return body
}
