export interface EbrOption {
 optionCode:string
 optionLabel:string
 optionValue:string
 sequenceNo:number
 activeFlag:boolean
}
export interface EbrField {
 fieldCode:string
 groupCode:string|null
 label:string
 fieldType:"NUMBER"|"TEXT"|"TEXTAREA"|"ENUM"|"MULTI_ENUM"|"BOOLEAN"|"DATE"|"TIME"|"DATETIME"|"BARCODE"|"MATERIAL_LOT"|"CONTAINER"|"EQUIPMENT"|"PERSON"|"ATTACHMENT"|"IMAGE"|"TIMER"|"CALCULATED"|"INSTRUMENT_VALUE"|"SIGNATURE_PLACEHOLDER"
 sourceType:"MANUAL"|"BARCODE"|"INSTRUMENT"|"SYSTEM"|"DERIVED"
 dataType:string
 unitId:string|null
 precisionScale:number|null
 requiredFlag:boolean
 readonlyFlag:boolean
 defaultExpr:string|null
 placeholder:string|null
 helpText:string|null
 sequenceNo:number
 validationJson:string|null
 options:Array<EbrOption>
}
export interface EbrGroup {
 groupCode:string
 title:string|null
 sequenceNo:number
 layoutColumns:number
 repeatMode:"NONE"|"LIST"
 minOccurs:number|null
 maxOccurs:number|null
}
export interface EbrSection {
 sectionCode:string
 title:string
 sequenceNo:number
 repeatMode:"NONE"|"LIST"
 visibilityRuleCode:string|null
 pageBreakFlag:boolean
 groups:Array<EbrGroup>
}
export interface EbrForm {
 formCode:string
 formName:string
 operationDefId:string|null
 schemaVersion:string
 sequenceNo:number
 fields:Array<EbrField>
}
export interface EbrRule {
 ruleCode:string
 formCode:string|null
 fieldCode:string|null
 ruleType:"VALIDATION"|"CALCULATION"|"VISIBILITY"|"BRANCH"|"COMPLETION"|"SIGNATURE"|"REVIEW"|"DEVIATION"
 triggerPoint:"ON_CHANGE"|"ON_SAVE"|"ON_SUBMIT"|"ON_OPERATION_COMPLETE"|"ON_BATCH_CLOSE"
 expression:string
 severity:"BLOCK"|"WARN"|null
 errorCode:string|null
 messageTemplate:string|null
 deviationTrigger:boolean
 activeFlag:boolean
}
export interface EbrSignatureRule {
 objectScope:string
 objectCode:string
 meaning:string
 requiredRole:string
 reauthRequired:boolean
 sequenceNo:number
 invalidateOnChange:boolean
}
export interface EbrReviewRule {
 objectScope:string
 objectCode:string
 reviewType:"VERIFY"|"APPROVE"
 requiredRole:string
 independentUserRequired:boolean
 sequenceNo:number
}
export interface EbrDefinition {
 sections:Array<EbrSection>
 forms:Array<EbrForm>
 rules:Array<EbrRule>
 signatureRules:Array<EbrSignatureRule>
 reviewRules:Array<EbrReviewRule>
}
export interface EbrCreate {
 packageVersionId:string
 templateCode:string
}
export interface EbrSave {
 versionNo:number
 reason:string
 definition:EbrDefinition
}
export interface EbrVersionCreate {
 versionNo:number
 reason:string
 copyDefinition:boolean
}
export interface EbrCommand {
 versionNo:number
 reason:string
}
export interface EbrInput {
 fieldCode:string
 values:Array<string|number|boolean>
}
export interface EbrSimulation {
 versionNo:number
 reason:string
 triggerPoint:"ON_CHANGE"|"ON_SAVE"|"ON_SUBMIT"|"ON_OPERATION_COMPLETE"|"ON_BATCH_CLOSE"
 inputs:Array<EbrInput>
}
export interface EbrSummary {
 id:string
 packageVersionId:string
 templateCode:string
 version:number
 status:"DRAFT"|"SUBMITTED"|"APPROVED"|"EFFECTIVE"|"WITHDRAWN"
 contentHash:string|null
 effectiveFrom:string|null
 approvedBy:string|null
 approvedAt:string|null
 versionNo:number
 allowedActions:Array<string>
}
export interface EbrDetail {
 id:string
 packageVersionId:string
 templateCode:string
 version:number
 status:"DRAFT"|"SUBMITTED"|"APPROVED"|"EFFECTIVE"|"WITHDRAWN"
 contentHash:string|null
 effectiveFrom:string|null
 approvedBy:string|null
 approvedAt:string|null
 versionNo:number
 allowedActions:Array<string>
 definition:EbrDefinition
 versions:Array<EbrSummary>
 operationChoices:Array<EbrOperationChoice>
}
export interface EbrPage {
 items:Array<EbrSummary>
 total:number
 page:number
 size:number
}
export interface EbrIssue {
 path:string
 code:string
 message:string
}
export interface EbrLint {
 valid:boolean
 issues:Array<EbrIssue>
}
export interface EbrRuleResult {
 ruleCode:string
 passed:boolean
 severity:string|null
 message:string|null
 inputSnapshot:Array<EbrInput>
 outputValue:string|number|boolean|null
 executedAt:string
 engineVersion:string
 ruleVersion:string
}
export interface EbrSimulationResult {
 allowed:boolean
 results:Array<EbrRuleResult>
}
export interface EbrDifference {
 path:string
 before:string|null
 after:string|null
}
export interface EbrComparison {
 leftId:string
 rightId:string
 differences:Array<EbrDifference>
}
export interface EbrOperationChoice {
 id:string
 operationCode:string
 operationName:string
}