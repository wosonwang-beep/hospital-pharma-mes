export interface MaterialField {key:string;label:string;kind:string;required:boolean;max:number|null}
export const materialFields:MaterialField[] = [
  {
    "key": "materialCode",
    "label": "物料编码",
    "kind": "text",
    "required": true,
    "max": 50
  },
  {
    "key": "materialName",
    "label": "物料名称",
    "kind": "text",
    "required": true,
    "max": 200
  },
  {
    "key": "materialType",
    "label": "物料类型",
    "kind": "text",
    "required": true,
    "max": 30
  },
  {
    "key": "specification",
    "label": "规格",
    "kind": "text",
    "required": false,
    "max": 200
  },
  {
    "key": "gradePurity",
    "label": "等级 / 纯度",
    "kind": "text",
    "required": false,
    "max": 100
  },
  {
    "key": "appearance",
    "label": "外观",
    "kind": "text",
    "required": false,
    "max": 500
  },
  {
    "key": "baseUnitId",
    "label": "基本单位",
    "kind": "unit",
    "required": true,
    "max": null
  },
  {
    "key": "packSpec",
    "label": "包装规格",
    "kind": "text",
    "required": false,
    "max": 200
  },
  {
    "key": "packUnitId",
    "label": "包装单位",
    "kind": "unit",
    "required": false,
    "max": null
  },
  {
    "key": "manufacturerName",
    "label": "生产厂家",
    "kind": "text",
    "required": false,
    "max": 200
  },
  {
    "key": "lotControlled",
    "label": "批号管理",
    "kind": "boolean",
    "required": true,
    "max": null
  },
  {
    "key": "effectiveFrom",
    "label": "生效时间（UTC）",
    "kind": "datetime",
    "required": false,
    "max": null
  },
  {
    "key": "effectiveTo",
    "label": "失效时间（UTC）",
    "kind": "datetime",
    "required": false,
    "max": null
  },
  {
    "key": "remark",
    "label": "备注",
    "kind": "text",
    "required": false,
    "max": 1000
  },
  {
    "key": "requiresIncomingInspection",
    "label": "是否入库必验",
    "kind": "boolean",
    "required": false,
    "max": null
  }
]
export function initialMaterialForm(){return Object.fromEntries(materialFields.map(f=>[f.key,f.kind==='boolean'?true:null])) as Record<string,unknown>}
export function materialPayload(form:Record<string,unknown>,editing=false){
 const body:Record<string,unknown>={}
 for(const f of materialFields){if(editing&&f.key==='materialCode')continue;let value=form[f.key];if(value===''||value===undefined)value=null;if(f.required&&value===null)throw new Error(`请填写${f.label}`);if(f.kind==='datetime'&&value!==null)value=new Date(String(value)+'Z').toISOString();body[f.key]=value}
 return body
}
export interface SupplierLink {id?:string;supplierId:string;supplierCode?:string;supplierName?:string;approved:boolean;preferred:boolean;validTo:string|null}
export function supplierPayload(items:SupplierLink[]){
 const active=items.filter(x=>x.approved),preferred=items.filter(x=>x.preferred)
 if(preferred.length!==(active.length?1:0)||preferred.some(x=>!x.approved))throw new Error('有效供应商关系必须选择一个首选供应商')
 if(new Set(items.map(x=>x.supplierId)).size!==items.length)throw new Error('供应商不能重复选择')
 return items.map(({supplierId,approved,preferred,validTo})=>({supplierId,approved,preferred,validTo:validTo||null}))
}
