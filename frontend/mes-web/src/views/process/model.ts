export interface Base {id:string;status:string;allowedActions:string[]}
export interface Product extends Base {productCode:string;productName:string;dosageForm:string|null;specification:string|null;baseUnitId:string}
export interface FormulaLine {lineNo:number;materialId:string;requiredQty:string;unitId:string;overagePct:string|null;critical:boolean}
export interface Formula {formulaCode:string;batchBasisQty:string;unitId:string;items:FormulaLine[]}
export interface Rule {kind:string;rules?:Rule[]}
export interface Parameter {parameterCode:string;parameterName:string;acquisitionMode:string;unitId:string|null;lowerLimit:string|null;upperLimit:string|null}
export interface IpcDefinition {ipcCode:string;name:string;required:boolean;resultType:string;lowerLimit:string|null;upperLimit:string|null;expectedText:string|null;unitId:string|null;methodReference:string}
export interface Operation {operationCode:string;operationName:string;operationType?:'PRODUCTION'|'IPC'|null;sequenceNo:number;requiredRole:string|null;completionRule:Rule|null;predecessorCodes:string[];requiredEquipmentType:string|null;clearanceRequired:boolean;parameters:Parameter[];ipcDefinitions?:IpcDefinition[]}
export interface RouteDefinition {routeCode:string;operations:Operation[]}
export interface ApplicableProduct {id:string;productId:string;productCode:string;productName:string;dosageForm:string|null;specification:string|null;status:string}
export interface Package extends Base {packageName?:string|null;description?:string|null;processVersion?:string|null;effectiveApprovedDate?:string|null;packageCode:string;productId?:string|null;productIds?:string[];applicableProducts?:ApplicableProduct[];productName:string;currentDefinition:{id:string;status:string;formula:Formula|null;route:RouteDefinition|null}}
export const states:Record<string,string>={ACTIVE:'启用',INACTIVE:'停用',DRAFT:'草稿',SUBMITTED:'已提交',APPROVED:'已批准',EFFECTIVE:'已生效'}
export const blankFormula=():Formula=>({formulaCode:'',batchBasisQty:'',unitId:'',items:[]})
export const blankRoute=():RouteDefinition=>({routeCode:'',operations:[]})
export const clone=<T>(value:T):T=>JSON.parse(JSON.stringify(value))
