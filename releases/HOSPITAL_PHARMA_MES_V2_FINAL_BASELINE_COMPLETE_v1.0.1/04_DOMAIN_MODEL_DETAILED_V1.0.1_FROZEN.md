# 04 医院制剂 MES V2.0 领域模型与业务规则详细设计说明书 V1.0.1 FROZEN

## 1. 领域原则
领域模型优先于Controller/CRUD。跨Aggregate通过Application Service协调。历史事实采用追加事件/Revision。数据库FK不等于Aggregate边界。
Source of Truth：MainBatch正式批；MaterialCharge实际投料；InventoryLedger库存事实；FieldValueRevision eBR值历史；EquipmentRun设备运行；ReleaseDecision QA放行；AuditEvent审计。

## 2. Aggregate Catalog
### Material Aggregate
Root: Material。Entities: MaterialVersion, QualitySpec, StorageRule, ProductionRule, ApprovedSupplier。
VO: MaterialCode, Quantity, Unit, EffectivePeriod。
Commands: CreateMaterial, CreateMaterialVersion, SubmitMaterialVersion, ApproveMaterialVersion, DisableMaterial。
Events: MaterialCreated, MaterialVersionApproved, MaterialDisabled。
Invariants: materialCode组织内唯一；已批准版本不可原地修改；历史批次引用版本不可删除；质量/储存/生产控制变更产生新版本。
Repository: MaterialRepository。
Forbidden: 直接update已批准规则；硬删除。

### ProcessPackage Aggregate
Root: ProcessPackageVersion。Entities: FormulaVersion/Item, RouteVersion/OperationDef/ParameterDef, EbrTemplateVersion。
Commands: CreateDraft, EditDraft, Submit, Approve, Publish, Withdraw。
Events: ProcessPackageSubmitted/Approved/Published。
Invariants: APPROVED/EFFECTIVE不可原地修改；BOM单位量纲合法；Route可达无死循环；eBR规则lint通过；审批职责分离。
Repository: ProcessPackageRepository。

### MainBatch Aggregate
Root: MainBatch。Entities: SubBatch, ExecutionUnit。References: ProductionOrder, Product, ProcessSnapshot。
VO: BatchNumber, Quantity, BatchStatus。
Commands: CreateMainBatch, ReleaseMainBatch, StartProduction, CompleteProduction, SubmitForQA, CloseBatch。
Events: MainBatchCreated, MainBatchReleased, ProductionStarted, ProductionCompleted, BatchSubmittedForQA, BatchClosed。
Invariants: batchNo唯一；必须绑定immutable ProcessSnapshot；SubBatch只能属于一个MainBatch且不能独立QA Release；DIRECT ExecutionUnit不得有subBatchId；SUB_BATCH必须有subBatchId；所有required ExecutionUnit完成才能ProductionCompleted。
Repository: MainBatchRepository。

### OperationExecution Aggregate
Root: OperationExecution。References: ExecutionUnit, OperationDef snapshot。
Commands: MarkReady, Start, Pause, Resume, Complete, Block, Unblock。
Events: OperationStarted/Paused/Completed/Blocked。
Invariants: 人员资格、设备、清场、物料、IPC、eBR完成条件均由服务端Gate；非法状态转换拒绝。
Repository: OperationExecutionRepository。

### Inventory Aggregate
Root/Fact: InventoryLedger append-only。Supporting: Reservation, MaterialIssue, Location, Container, MaterialLot。
Commands: Receive, Reserve, Issue, Return, Move, Adjust, Consume, Output。
Events: InventoryMovementRecorded。
Invariants: 不允许直接修改历史ledger；负库存默认阻断；冻结/过期/未放行不得发料/投料；每次movement有sourceRef+idempotencyKey。
Repository: InventoryLedgerRepository/MaterialLotRepository。

### Weighing Aggregate
Root: WeighingRecord。
Commands: CreateWeighing, ConfirmWeighing, VerifyWeighing, VoidWeighing。
Events: MaterialWeighed, WeighingVerified。
Invariants: MaterialLot RELEASED；BOM匹配；人员资格；电子秤有效；数量/单位/精度/偏差合规；要求独立复核时verifier!=operator。
Repository: WeighingRepository。

### MaterialCharge Aggregate
Root: MaterialCharge。
Command: ConfirmMaterialCharge, ReverseMaterialCharge。
Event: MaterialChargeConfirmed/Reversed。
Invariants: ExecutionUnit和Operation IN_PROGRESS；MaterialLot RELEASED；BOM匹配；称量满足策略；数量合法；复核满足。
Transaction side effects: MaterialCharge + QuantityEvent(CHARGE) + InventoryLedger(CONSUME) + Genealogy + eBR reference + AuditEvent 同事务/可靠outbox。
Forbidden: 以Issue数量代替Charge。
Repository: MaterialChargeRepository。

### EbrDefinition Aggregate
Root: EbrTemplateVersion。Entities: Section, Group, FormDef, FieldDef, Option, Rule, SignatureRule, ReviewRule。
Commands: EditDraft, Lint, Simulate, Submit, Approve, Publish。
Invariants: stable IDs唯一；规则白名单；发布版不可改。
Repository: EbrTemplateRepository。

### EbrRuntime Aggregate
Root: FormInstance / BatchSnapshot boundary。Entities: FieldValueRevision, RuleExecution, ReviewRecord, Attachment。
Commands: SaveDraftValues, SubmitForm, CorrectValue, Review, Sign。
Events: FormSubmitted, FieldCorrected, ReviewInvalidated, SignatureInvalidated。
Invariants: Renderer只读BatchSnapshot；FieldValue append-only；更正保留原因；受签数据变化失效相关review/signature。
Repository: EbrRuntimeRepository。

### MaterialBalance Aggregate
Root: BalanceResult。References: frozen BalanceRule, QuantityEvents。
Commands: CalculateBalance, RecalculateBalance, OpenInvestigation, ApproveInvestigation。
Invariants: 汇总值由source events计算；规则随批冻结；FAIL阻断完工/结批；每次计算版本保留。
Repository: BalanceRepository。

### QC Aggregate
Root: Sample。Entities: TestResultRevision。
Commands: CreateSample, RecordResult, ReviseResult。
Invariants: 原始结果不可覆盖；OOS/OOT触发受控流程；QC结论不改变QA Release。
Repository: QcRepository。

### Deviation Aggregate
Root: Deviation。Entities/refs: CAPA。
Commands: Open, Investigate, Decide, Close。
Invariants: 关键开放Deviation阻断相关Gate；重测/复检须批准。
Repository: DeviationRepository。

### QA Release Aggregate
Root: ReleaseDecision。Evidence refs: MainBatch, eBR, QC, Balance, Deviation, Signature, Finished MaterialLot。
Command: MakeReleaseDecision, SupersedeDecision。
Event: BatchReleased/BatchRejected。
Invariants: Production Complete != QC Pass != QA Release；仅MainBatch可Release；所有Gate满足；decision immutable；更正使用superseding decision。
Repository: ReleaseRepository。

## 3. Domain Service
BatchReleaseGateService：聚合eBR/QC/Balance/Deviation/Signature evidence，不直接拥有其数据。
MaterialEligibilityService：检查lot质量/效期/冻结/BOM。
UnitConversionService：确定性换算并返回原值/因子/结果。
EbrRuleEngine：白名单DSL确定性执行。
GenealogyService：基于Charge创建追溯关系。

## 4. 事务边界
单Aggregate写入+Audit同事务；Charge跨库存/数量事件/谱系必须一个本地事务或outbox保证一致；外部系统永不参与本地数据库事务。

# V1.0.1 Platform Domain Contracts — DCP-MES-001-R2-001
## AuditEvent

`AuditEvent` is append-only evidence. It contains actor identity, authorizing role snapshot, action, object type/id, before/after canonical digests, reason, UTC millisecond event time, transaction ID, request ID, source and optional idempotency key. It is appended in the same local transaction as the regulated business change. No update/delete domain method exists.

## Signature

MES-001 owns generic signing, verification, invalidation and re-sign commands plus the provider registry. MES-007 owns `EBR_FORM_INSTANCE` and `EBR_REVIEW_RECORD` providers with `VERIFY`/`APPROVE`. MES-013 owns `QA_RELEASE_DECISION` with `RELEASE`/`REJECT`. The generic sign command never performs QA release.

The signable envelope is `{schemaVersion, objectType, objectId, recordVersion, record, evidenceIds}`. `evidenceIds` are unique, lexicographically sorted `TYPE:ID` strings. The digest is lowercase hexadecimal SHA-256 of UTF-8 RFC 8785 JCS bytes. Decimal values with business-significant scale are normalized strings; timestamps are UTC with millisecond precision and `Z`.

## IntegrationMessage

Inbox and outbox identities/payloads are immutable after insert. Status changes occur only through receive/process/enqueue/claim/publish/fail/recover/manual-retry commands. Worker claim uses an atomic conditional update on `id`, `version_no` and eligible status. The centralized retry policy permits 8 failed attempts with the frozen delay schedule; failure 8 enters `DEAD_LETTER`.

## PlatformIdempotencyRecord

The key is `(org_id, actor_id, operation_code, idempotency_key)`. An identical completed request replays its response. Reuse with a different request digest fails with `IDEMPOTENCY_KEY_REUSED`. This technical record never substitutes for regulated domain evidence.
