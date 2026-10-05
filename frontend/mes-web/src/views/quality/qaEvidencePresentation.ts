import { labels } from './incomingModel'

// Presentation only: keys and their source values remain unchanged.
const qaLabels: Record<string, string> = {
  form: '基本信息', values: '原始记录与修订', reviews: '复核记录',
  ruleExecutions: '规则执行记录', signatures: '电子签名记录',
  value: '记录内容', valueJson: '记录内容', revisionNo: '修订版本',
  formInstanceId: '表单记录编号', formTemplateId: '表单模板编号',
  templateVersionId: '模板版本编号', fieldId: '字段编号', fieldKey: '字段编码',
  fieldName: '字段名称', formCode: '表单编码', formName: '表单名称',
  formRevision: '表单记录版本', executionUnitId: '执行单元编号',
  operationId: '工序编号', mainBatchId: '生产批编号',
  invalidated: '是否失效', invalidatedBy: '失效操作人',
  invalidatedAt: '失效时间', invalidationReason: '失效原因',
  superseded: '是否被后续修订替代', previousValueId: '前序记录编号',
  supersedesValueId: '替代的记录编号', reason: '操作原因',
  definitionHash: '定义摘要', recordDigest: '记录摘要',
  contentHash: '内容摘要', recordVersion: '记录版本',
  ruleCode: '规则编码', ruleName: '规则名称', ruleVersion: '规则版本',
  ruleId: '规则编号', ruleVersionId: '规则版本编号',
  ruleType: '规则类型', executionResult: '执行结果',
  inputSnapshot: '执行输入快照', outputSnapshot: '执行输出快照',
  executedBy: '执行人', executedAt: '执行时间',
  reviewConclusion: '复核结论', reviewComment: '复核意见',
  reviewerId: '复核人编号', meaning: '签名含义',
  signedBy: '签名人', signedAt: '签名时间',
  signerId: '签名人编号', signerName: '签名人姓名',
  objectType: '记录类型', objectId: '记录编号',
  evidenceIds: '证据记录编号', record: '签名绑定记录',
  gateCode: '放行检查编码', passed: '是否通过', blockingCodes: '阻断原因编码',
}

export function qaEvidenceLabel(key: string): string {
  // Unknown extension fields stay visible, without exposing a JSON property name.
  return qaLabels[key] ?? labels[key] ?? '补充信息'
}
