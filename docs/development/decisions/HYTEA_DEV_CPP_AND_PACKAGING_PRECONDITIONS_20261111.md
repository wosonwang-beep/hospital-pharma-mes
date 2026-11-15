# 喉咽茶生产批655：CPP与包装来源前置差异（2026-11-11）

状态：PARTIAL / 待补充授权与来源。依据：用户要求继续OP02～OP08、成品QC/QA/入库归档；FINAL BASELINE v1.0.23、MES_DEV_RULES、已批准HYTEA_FULL_SOFTWARE_FLOW_20261111。限本机DEV，单一main。

## 真实执行
OP01/779已COMPLETED、eBR822 VERIFIED。OP02/780清场记录、独立复核、START已成功，状态IN_PROGRESS；首条CPP参数POST `/operations/780/parameter-values`返回403 FORBIDDEN，requestId=0f3ad4d0-b12a-4e50-9bb7-1d0692c16cf3，参数0；OP03～OP08未执行。
SecurityConfiguration:320与ExecutionService.parameter均要求`mes:param:record`。正式IAM盘点仅SYSTEM_ADMIN(角色1)持有，MES_PRODUCTION(287)没有。冻结OP02/03各3、OP04 2、OP05 1共9项CPP，全为MANUAL；拟值符合现有限度/单位。

## 最小权限方案（未批准、未执行）
仅限DEV角色287增加现有`mes:param:record`，前后差异恰一项，重登production11572后仅从OP02参数继续。不得改网关、权限码、质量/签名合同，也不能由admin代录制造生产岗位验收。此IAM变更需用户另行明确批准。

## 包装来源缺口
物料14（BZ-0002 100ml药用高密度聚乙烯瓶）、物料16（BZ-0004喉咽茶合剂标签）均无批准供应商关系，也无现存物料批。现有十家供应商未证明为两物料的合格供方；既往2023图片不等于新批真实收货。按docs/review/MASTER_DATA_COMPLETION_20261012.md，不能猜测供应商、制造商、批号或代做合格审批。须有可核验的DEV供应源信息，循既有物料关联、收货、检验/QC、QA、库存可用流程创建适用批次。OP06/07所需批号、领退用损、分装/包装数量和物料平衡字段已盘点；OP08待前序完成。

证据：private-data-audit/hytea-resume-20261111/next-stage-readonly-inventory.json、cpp-permission-frozen-preflight.json、remaining-forms-field-inventory.json及api-execution.jsonl。仅执行正式REST，不改源码/数据库结构/迁移、无worktree/Git提交推送。SELF-REVIEW，整批非ACCEPTED。

## 2026-11-11 两项DEV补充方案已获用户批准
用户针对上文两项方案明确回复「批准」：1）仅限本机DEV角色287增加已有mes:param:record；2）允许有业务意义、在独立元数据注明合成DEV的软件联调包装供货来源/批次，经正常供应商关联、收货、QC/QA和库存接口验证，不是实际采购/厂家资质声明。任何新业务记录仍须满足冻结权限、状态、签名与物料来源逻辑，禁止无意义占位。此批准不等于真实供货资格或实际药品质量结论。
