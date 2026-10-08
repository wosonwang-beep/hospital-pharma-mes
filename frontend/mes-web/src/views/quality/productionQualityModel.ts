import contract from './production-quality-contract.json'
import {labels,enumLabels,referenceFields,type IncomingRow} from './incomingModel'
export const qualityOperations=contract.operations
export const qualityActionLabels:Record<string,string>={approve:'批准',results:'记录原始结果',revisions:'更正结果',review:'独立复核',retest:'批准复检',recalculate:'重新计算',investigations:'建立调查',investigate:'记录调查',start:'开始整改',complete:'完成整改',verify:'独立验证',reverse:'冲销数量记录',capas:'新增CAPA','quantity-events':'记录产量与损耗'}
export function permits(row:IncomingRow,action:string){const names:Record<string,string>={results:'RECORD_RESULT',revisions:'REVISE',UPDATE:'EDIT'};return Array.isArray(row.allowedActions)&&row.allowedActions.includes(names[action]??action.split('-').join('_').toUpperCase())}
/** Presentation only: the existing signed QC review command already rejects self-review. */
export function canPresentIndependentQualityReview(row:IncomingRow,actorId:unknown){
 if(!permits(row,'review')||actorId==null||!Array.isArray(row.results)||row.currentResultRevisionId==null)return false
 const current=(row.results as IncomingRow[]).find(result=>String(result.id)===String(row.currentResultRevisionId))
 return current?.recordedBy!=null&&String(current.recordedBy)!==String(actorId)
}
export const eventTypes=['CHARGE','ISSUE','RETURN','OUTPUT','SAMPLE','LOSS','SCRAP','WIP'] as const
export type Expression={sum:string[]}|{constant:string}|{op:string;left:Expression;right:Expression}
export function validateExpression(value:unknown,depth=1,counter={nodes:0}):asserts value is Expression{
 if(++counter.nodes>64||depth>8)throw Error('表达式最多64个节点、8层')
 if(!value||typeof value!=='object'||Array.isArray(value))throw Error('请填写数量表达式')
 const v=value as Record<string,unknown>,keys=Object.keys(v)
 if(keys.length===1&&Array.isArray(v.sum)&&v.sum.length&&v.sum.every(x=>eventTypes.includes(x as typeof eventTypes[number])))return
 if(keys.length===1&&typeof v.constant==='string'&&/^-?(?:0|[1-9][0-9]{0,23})(?:\.[0-9]{1,8})?$/.test(v.constant)&&v.constant.replace(/[-.]/g,'').length<=24)return
 if(keys.length===3&&['ADD','SUBTRACT','MULTIPLY','DIVIDE'].includes(String(v.op))&&keys.every(k=>['op','left','right'].includes(k))){validateExpression(v.left,depth+1,counter);validateExpression(v.right,depth+1,counter);return}
 throw Error('请填写有效的数量表达式')
}
Object.assign(labels,{sum:'数量类型汇总',constant:'固定数量',op:'运算',left:'左侧数量',right:'右侧数量',balanceRuleId:'平衡规则',mainBatchId:'生产批',finishedMaterialId:'成品物料',materialId:'物料',balanceRules:'物料平衡规则',balanceCode:'平衡编码',basis:'计算范围',operationCode:'工序编码',formulaExpr:'计算规则',expected:'预期数量',actual:'实际数量',metric:'计算指标',toleranceLow:'允许下限',toleranceHigh:'允许上限',checkPoint:'检查时点',sourceRef:'来源标识',eventType:'数量类型',eventTime:'记录时间',amount:'数量',productionPlanId:'生产质量计划',productionTestInstanceId:'生产检验记录',qcSpecificationItemId:'质量标准项目',specificationItemId:'质量标准项目',investigationId:'批准复检调查',capaNo:'CAPA编号',actionText:'整改措施',completionText:'完成说明',investigationText:'调查说明',balanceResultId:'平衡结果',inputDigest:'输入证据摘要',calculationVersion:'计算版本',expectedValue:'预期数量',actualValue:'实际数量',differenceValue:'差异数量',differencePct:'差异百分比',blockingCodes:'阻断原因',reviews:'独立验证记录',capas:'CAPA',reversalOfId:'原数量记录',sourceType:'来源类型',sourceId:'来源记录',calculatedAt:'计算时间',calculatedBy:'计算人',inputSnapshot:'数量输入证据',ruleSnapshot:'规则快照',investigation:'平衡调查',resultRevisionId:'结果版本'})
Object.assign(enumLabels,{PRODUCTION:'生产质量',OOT:'趋势异常',BATCH:'整批',OPERATION:'工序',PACKAGING:'包装',OPERATION_COMPLETE:'工序完成',BATCH_COMPLETE:'生产完成',QA_RELEASE:'QA放行',DIFFERENCE_PCT:'差异百分比',YIELD_PCT:'收率百分比',OUTPUT:'产出',SAMPLE:'取样',LOSS:'损耗',SCRAP:'报废',WIP:'在制',CHARGE:'投料',ISSUE:'出库',RETURN:'退料',VERIFIED:'已验证',REOPEN:'重新整改',CONFIRMED:'确认有效',INVALIDATED:'批准无效',APPROVED_EXCEPTION:'已批准例外',RECALCULATE_REQUIRED:'需重新计算',ACCEPT_EXCEPTION:'接受例外',REQUIRE_RECALCULATION:'要求重新计算'})
for(const key of ['mainBatchId','finishedMaterialId','materialId','qcSpecificationItemId','specificationItemId','investigationId','productionTestInstanceId','locationId'])referenceFields.add(key)
