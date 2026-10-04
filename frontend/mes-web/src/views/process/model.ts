export interface Base {id:string;versionNo:number;status:string;allowedActions:string[]}
export interface Product extends Base {productCode:string;productName:string;dosageForm:string|null;specification:string|null;baseUnitId:string}
export interface FormulaLine {lineNo:number;materialId:string;requiredQty:string;unitId:string;overagePct:string|null;critical:boolean}
export interface Formula {formulaCode:string;batchBasisQty:string;unitId:string;items:FormulaLine[]}
export interface Rule {kind:string;rules?:Rule[]}
export interface Parameter {parameterCode:string;parameterName:string;acquisitionMode:string;unitId:string|null;lowerLimit:string|null;upperLimit:string|null}
export interface IpcDefinition {ipcCode:string;name:string;required:boolean;resultType:string;lowerLimit:string|null;upperLimit:string|null;expectedText:string|null;unitId:string|null;methodReference:string}
export interface Operation {operationCode:string;operationName:string;sequenceNo:number;requiredRole:string|null;completionRule:Rule|null;predecessorCodes:string[];requiredEquipmentType:string|null;clearanceRequired:boolean;parameters:Parameter[];ipcDefinitions?:IpcDefinition[]}
export interface RouteDefinition {routeCode:string;operations:Operation[]}
export interface Version extends Base {businessVersion:number;formula:Formula|null;route:RouteDefinition|null;effectiveFrom:string|null;contentHash:string|null;approval:{signerId:string;signedAt:string;signatureId:string}|null}
export interface Package extends Base {packageCode:string;productId:string;productName:string;selectedVersion:Version;versions:Version[]}
export const states:Record<string,string>={ACTIVE:'启用',INACTIVE:'停用',DRAFT:'草稿',SUBMITTED:'已提交',APPROVED:'已批准',EFFECTIVE:'已生效'}
export const blankFormula=():Formula=>({formulaCode:'',batchBasisQty:'',unitId:'',items:[]})
export const blankRoute=():RouteDefinition=>({routeCode:'',operations:[]})
export const clone=<T>(value:T):T=>JSON.parse(JSON.stringify(value))
