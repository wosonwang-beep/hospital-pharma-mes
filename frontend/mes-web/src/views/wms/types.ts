export interface Warehouse {
 id?:string
 orgId?:string
 versionNo?:number
 createdBy?:string
 createdAt?:string
 updatedBy?:string
 updatedAt?:string
 allowedActions?:Array<string>
 warehouseCode?:string
 warehouseName?:string
 warehouseType?:string
 status?:"ACTIVE"|"INACTIVE"
}
export interface WarehouseCreate {
 warehouseCode:string
 warehouseName:string
 warehouseType:string
}
export interface WarehouseUpdate {
 warehouseCode?:string
 warehouseName?:string
 warehouseType?:string
 reason:string
 versionNo:number
 action:"UPDATE"|"ENABLE"|"DISABLE"
}
export interface Location {
 id?:string
 orgId?:string
 versionNo?:number
 createdBy?:string
 createdAt?:string
 updatedBy?:string
 updatedAt?:string
 allowedActions?:Array<string>
 warehouseId?:string
 locationCode?:string
 locationName?:string
 status?:"ACTIVE"|"INACTIVE"
}
export interface LocationCreate {
 warehouseId:string
 locationCode:string
 locationName:string
}
export interface LocationUpdate {
 warehouseId?:string
 locationCode?:string
 locationName?:string
 reason:string
 versionNo:number
 action:"UPDATE"|"ENABLE"|"DISABLE"
}
export interface Container {
 id?:string
 orgId?:string
 versionNo?:number
 createdBy?:string
 createdAt?:string
 updatedBy?:string
 updatedAt?:string
 allowedActions?:Array<string>
 containerCode?:string
 containerType?:string
 status?:"ACTIVE"|"INACTIVE"
}
export interface ContainerCreate {
 containerCode:string
 containerType:string
}
export interface ContainerUpdate {
 containerCode?:string
 containerType?:string
 reason:string
 versionNo:number
 action:"UPDATE"|"ENABLE"|"DISABLE"
}
export interface ReceiptItemInput {
 materialId:string
 lotNo:string
 supplierLotNo:string
 manufacturerLotNo?:string
 manufactureDate?:string
 expiryDate?:string
 retestDate?:string
 receivedQty:string
 unitId:string
 packageSpec?:string
 packageCount:number
 locationId:string
 containerId?:string
 packageCheckPassed:boolean
 sealCheckPassed:boolean
 labelCheckPassed:boolean
 damageCheckPassed:boolean
 contaminationCheckPassed:boolean
}
export interface ReceiptItem {
 id?:string
 orgId?:string
 versionNo?:number
 createdBy?:string
 createdAt?:string
 updatedBy?:string
 updatedAt?:string
 allowedActions?:Array<string>
 materialId?:string
 lotNo?:string
 supplierLotNo?:string
 manufacturerLotNo?:string
 manufactureDate?:string
 expiryDate?:string
 retestDate?:string
 receivedQty?:string
 unitId?:string
 packageSpec?:string
 packageCount?:number
 locationId?:string
 containerId?:string
 packageCheckPassed?:boolean
 sealCheckPassed?:boolean
 labelCheckPassed?:boolean
 damageCheckPassed?:boolean
 contaminationCheckPassed?:boolean
 receiptId?:string
 materialSnapshot?:Record<string,unknown>
 requiresIncomingInspectionSnapshot?:boolean
 materialLotId?:string
}
export interface ReceiptCreate {
 receiptNo:string
 supplierId:string
 purchaseOrderNo?:string
 deliveryNoteNo?:string
 warehouseId:string
 transportCheckPassed:boolean
 items:Array<ReceiptItemInput>
}
export interface ReceiptUpdate {
 receiptNo:string
 supplierId:string
 purchaseOrderNo?:string
 deliveryNoteNo?:string
 warehouseId:string
 transportCheckPassed:boolean
 items:Array<ReceiptItemInput>
 versionNo:number
 reason:string
}
export interface Command {
 versionNo:number
 reason:string
}
export interface Receipt {
 id?:string
 orgId?:string
 versionNo?:number
 createdBy?:string
 createdAt?:string
 updatedBy?:string
 updatedAt?:string
 allowedActions?:Array<string>
 receiptNo?:string
 supplierId?:string
 purchaseOrderNo?:string
 deliveryNoteNo?:string
 warehouseId?:string
 transportCheckPassed?:boolean
 items?:Array<ReceiptItem>
 recordStatus?:"DRAFT"|"APPROVED"
 receivedBy?:string
 receivedAt?:string
 confirmedBy?:string
 confirmedAt?:string
}
export interface MaterialLot {
 id?:string
 orgId?:string
 versionNo?:number
 createdBy?:string
 createdAt?:string
 updatedBy?:string
 updatedAt?:string
 allowedActions?:Array<string>
 materialId?:string
 lotNo?:string
 supplierLotNo?:string
 manufactureDate?:string
 expiryDate?:string
 retestDate?:string
 receiptItemId?:string
 materialSnapshot?:Record<string,unknown>
 requiresIncomingInspectionSnapshot?:boolean
 qualityStatus?:"QUARANTINE"|"PENDING_SAMPLING"|"SAMPLING"|"SAMPLED"|"TESTING"|"PENDING_QC_REVIEW"|"QC_PASSED"|"QC_FAILED"|"PENDING_QA_RELEASE"|"PENDING_DISPOSITION"|"RELEASED"|"REJECTED"
 inventoryStatus?:"BLOCKED"|"AVAILABLE"|"FROZEN"
}
export interface Ledger {
 id?:string
 orgId?:string
 versionNo?:number
 createdBy?:string
 createdAt?:string
 updatedBy?:string
 updatedAt?:string
 allowedActions?:Array<string>
 materialLotId?:string
 locationId?:string
 containerId?:string
 eventType?:"RECEIVE"|"MOVE_OUT"|"MOVE_IN"|"ADJUST"
 deltaQty?:string
 unitId?:string
 sourceType?:string
 sourceRef?:string
 idempotencyKey?:string
 occurredAt?:string
}
export interface Inventory {
 materialLotId?:string
 lotNo?:string
 materialId?:string
 materialName?:string
 locationId?:string
 containerId?:string
 unitId?:string
 quantity?:string
 qualityStatus?:string
 inventoryStatus?:string
 expiryDate?:string
 versionNo?:number
}
export interface InventoryReceive {
 receiptId:string
 versionNo:number
 reason:string
}
export interface InventoryMove {
 materialLotId:string
 fromLocationId:string
 fromContainerId?:string
 toLocationId:string
 toContainerId?:string
 quantity:string
 unitId:string
 versionNo:number
 reason:string
}
export interface InventoryAdjust {
 materialLotId:string
 locationId:string
 containerId?:string
 deltaQty:string
 unitId:string
 versionNo:number
 reason:string
}
export interface Eligibility {
 allowed?:boolean
 failureCode?:string
 message?:string
 requiredActions?:Array<string>
}
export interface MaterialIssue {
 id?:string
 orgId?:string
 versionNo?:number
 createdBy?:string
 createdAt?:string
 updatedBy?:string
 updatedAt?:string
 allowedActions?:Array<string>
 mainBatchId?:string
 issueNo?:string
 status?:"DRAFT"|"CONFIRMED"|"CLOSED"
 issuedAt?:string
 items?:Array<Record<string,unknown>>
}
export interface DependencyCommand {
 mainBatchId?:string
 issueNo?:string
 versionNo?:number
 reason?:string
 items?:Array<Record<string,unknown>>
}
export interface WarehousePage {
 items?:Array<Warehouse>
 total?:number
 page?:number
 size?:number
}
export interface LocationPage {
 items?:Array<Location>
 total?:number
 page?:number
 size?:number
}
export interface ContainerPage {
 items?:Array<Container>
 total?:number
 page?:number
 size?:number
}
export interface ReceiptPage {
 items?:Array<Receipt>
 total?:number
 page?:number
 size?:number
}
export interface MaterialLotPage {
 items?:Array<MaterialLot>
 total?:number
 page?:number
 size?:number
}
export interface InventoryPage {
 items?:Array<Inventory>
 total?:number
 page?:number
 size?:number
}
export interface MaterialIssuePage {
 items?:Array<MaterialIssue>
 total?:number
 page?:number
 size?:number
}
export type LedgerList = Array<Ledger>
export interface Reservation {
 id?:string
 orgId?:string
 versionNo?:number
 mainBatchId?:string
 formulaItemId?:string
 materialLotId?:string
 reservedQty?:string
 unitId?:string
 status?:string
}
export type ReservationList = Array<Reservation>