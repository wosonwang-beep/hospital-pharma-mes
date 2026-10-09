export interface PrescriptionItem {
 id?:string
 lineNo:number
 materialId:string
 requiredQty:string
 unitId:string
 overagePct:string|null
 critical:boolean
}
export interface ProductionPrescription {
 id:string
 versionNo:number
 prescriptionCode:string
 prescriptionName:string
 productId:string
 productName:string
 processPackageId:string
 processCode:string
 batchBasisQty:string
 unitId:string
 status:'DRAFT'|'ACTIVE'|'INACTIVE'
 effectiveFrom?:string|null
 effectiveTo?:string|null
 allowedActions:string[]
 items:PrescriptionItem[]
}
export const prescriptionStatus:Record<string,string>={DRAFT:'草稿',ACTIVE:'在用',INACTIVE:'停用'}
export const blankPrescription=()=>({
 prescriptionCode:'',
 prescriptionName:'',
 productId:'',
 processPackageId:'',
 batchBasisQty:'',
 unitId:'',
 items:[] as PrescriptionItem[]
})
