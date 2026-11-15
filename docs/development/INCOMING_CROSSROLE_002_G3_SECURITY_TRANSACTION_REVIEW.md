# INCOMING-QMS-CROSSROLE-002 — G3 权限、电子签名和原子库存事务核查（2026-10-19）

**状态：G3 详细设计审查候选；不表示真实权限已配置或G4已执行。** 已真实读 `AGENTS.md`、`MES_DEV_RULES.md`、`INCOMING_CROSSROLE_002_G3_DETAILED_DESIGN_DRAFT.md`、`IncomingQualityService.java`、`IncomingMaterialPort.java`、`IncomingActorAdapter.java`、`IncomingSignatures.java`、`IncomingSignatureConfiguration.java`、`IncomingActionPolicy.java`、`WmsService.java`、`WmsQualityService`相关引用、`V013__mes_008_wms.sql`、`V016__incoming_quality.sql`、真实MariaDB预检。

## 已验证事实

1. WMS `wms_inventory_ledger` 原有索引 `uk_ledger(org_id,idempotency_key)`，且有来源/物料批次/时间相关索引，delta_qty 精度 **6位小数**，`container_id`可空。真实库历史 `SAMPLE_OUT` 数量 **0**；这说明尚未有本功能的正式扣减流水，不能据此声称已实现取样出库。
2. 真实权限当前有 `qms:sampling:execute`、`qms:sampling:complete`；**没有** `wms:sampling-outbound:confirm`。普通 WMS `move/adjust` 在 `WmsService` 中使用 `WmsRules.movable`（只允许适当可用库存），不适用于待验批次的受控样品出库。
3. `IncomingActorAdapter.requireQualified` 仅接受 `sample-execute`、`test-execute`、`test-review`、`qa-release`；没有仓管专用 GxP 岗位资格映射。`IncomingSignatures.SLOTS` 仅有取样计划/取样完成、结果/报告、QA等，`IncomingSignatureConfiguration` 也未注册仓管取样出库独立签名对象。
4. QMS `IncomingActionPolicy` 的 `qms_sampling_task` 状态 `IN_PROGRESS` 允许旧 `details/complete`，但没有 `PENDING_WAREHOUSE` 中间阶段；`qms_inspection_task` 在 CREATED 会列出 `assign`，须在 `sample_id=NULL` 时从 allowedActions 移除并由服务端强制阻断。
5. `IncomingMaterialPort` 当前只提供lot facts/quality transition；跨模块不得让QMS直接写WMS表。现有 SignedAction uses intent-based provider and immutable envelope,不能直接拿取样员的 completionSignature 冒充仓管出库签名。

## G3 拟冻结实现合同（需由工程质量门禁验证）

- **双授权、默认拒绝**：创建独立 `qms:sampling:warehouse-confirm` API动作权限与 `wms:inventory:sample-outbound` 内部库存动作权限；使用既有权限配置和有资格仓管员角色分配，不写死角色ID。`IncomingActorAdapter` 增受控资格键 `warehouse-sample-out`，配置 `mes.qualification.required-codes.warehouse-sample-out` 必须非空且查真实资格。两种权限与资格缺一不可，不能用 `sample-execute` 或 `qms:sampling:complete` 替代。新增权限的初始化只在追加迁移中声明，默认不给普通检验员；部署前需明确实际仓管角色授权配置，否则功能应403/资格错误并保持库存不变。
- **仓管签名**：新增 `SAMPLING_WAREHOUSE_OUTBOUND` 独立VERIFY签名Slot、SignableProvider/registry及任务上 `warehouseSignatureId`、`warehouseSignatureEvidenceJson`（与电子签名库列风格一致）；绑定取样任务身份、版本、已批准实取明细及来源库存流水的不可变证据与确认人。所有签名须真实再认证、签名验证与审计写入同一事务；检验员提交保留自己与仓管两个独立行为，不接受同一操作者兼任二者。
- **跨模块端口**：在QMS应用层定义受控 `SamplingOutboundInventoryPort`，Boot适配器调用WMS服务的专用 `SAMPLE_OUT`，后端共用本地MariaDB事务。QMS服务不得直接更新 `wms_inventory_ledger`；WMS锁组织下的 material_lot，再按每行实际源 `locationId/containerId` 锁对应库存流水，验证足额并使用批准的单位换算，按 source=SamplingDetail/<detailId> 追加负数事件。不可使用普通领料/ADJUST/预留接口。
- **精确数量**：QMS 请求/计划/取样明细是 `DECIMAL(24,8)`的十进制字符串；WMS ledger `DECIMAL(24,6)`，要根据目标基本单位换算后调用 `setScale(6, UNNECESSARY)`，不能四舍五入，超精度/异币单位或无批准换算阻断。分组计算按 `(org,lot,location,container,unit)`，不能把不同库位/容器混加。检验样+留样需严格等于该明细实际取样量，整单实际量不超过QC批准计划量。
- **独立幂等和原子性**：QMS锁sampling_task+父请求的当前版本，阻断非PENDING_WAREHOUSE/重复确认；WMS锁物料批与来源库存，插入每明细一个负向流水，源身份唯一关联到 sampling_detail.sampleOutboundLedgerId。因为现有 `uk_ledger` 的幂等摘要包含行为人/operation，不能只依赖它防止另一仓管员第二次扣减：必须依赖受控sampling_task状态锁+行级不变来源+DB唯一约束，多人并发也只成功一次。QMS状态/样品 COLLECTED/批次SAMPLED、仓管签名、所有流水及审计在一个事务提交；任何校验、审计、签名失败一同回滚，禁止先扣库存后补签名。
- **原任务兼容**：旧SamplingTask COMPLETE依原冻结历史，不追补仓库流水；新标记了 `crossrole_contract='V2'` 的任务只能提交到 PENDING_WAREHOUSE 然后仓管确认。旧调用不得通过非V2路径完成新单；新旧互不篡改既有不可变签名。任务在 sampleId NULL 时只允许受控 view/bind-sample，样品RECEIVED前不可assign/start/写原始结果。
- **双单防重**：QC确认可能同请验额外安排检验样/合法复检，不得给所有 `inspection_request_id` 建全局唯一限制。只对本次QC自动生成的首次取样单与待样品检验任务分别建立可空 `qc_auto_generated_request_id`，各自唯一 `(org_id,qc_auto_generated_request_id)`，历史/后续受控增补任务该字段为空，保证一次确认只生成两个初始单据且不破坏合法后续任务。

## 自动化安全预检

新增只读 `backend/mes-boot/src/test/java/com/hospital/mes/incoming/IncomingCrossroleSecurityPreflightIT.java`；执行 `mvn -q -pl mes-boot -am -Dtest=IncomingCrossroleSecurityPreflightIT -Dsurefire.failIfNoSpecifiedTests=false test` 实测 **退出0**，真实MariaDB输出：`preexisting SAMPLE_OUT rows=0`、`distinct warehouse outbound permission configured=false`，索引为 `PRIMARY,uk_ledger,material_lot_id,location_id,container_id,unit_id,idx_ledger_lot,idx_ledger_event,idx_ledger_source,idx_ledger_time`。证据：`docs/review/evidence/INCOMING_CROSSROLE_SECURITY_PREFLIGHT_20261019.log`。

## G3 门禁结论

**G3 STILL OPEN，G3.5 BLOCKED**。基本安全架构已明确，缺独立仓管角色资格/权限映射的已批准生产配置、签名Slot和跨模块事务适配实现，且新增中间状态尚未在实际QMS/WMS服务完成兼容验证。新的数据库迁移文件仍是 DO_NOT_RUN 草案，未落地到 Flyway；不能用“预检通过”冒充 G4/正式库存写入或G5签名审计PASS。需先通过技术合同一致性审查、迁移模拟、真数据库集成回滚测试，再把G3冻结并交给Codex执行。
