# 领域模型与业务规则

Product→ProcessPackageVersion→Formula/BOM+Route+EBRTemplate。ProductionOrder→MainBatch→SubBatch(optional)→ExecutionUnit→OperationExecution。Material→MaterialLot→InventoryLedger。Weighing→MaterialCharge→QuantityEvent→BalanceResult→Genealogy。MainBatch→Sample/Deviation→ReleaseDecision。

## v1.0.7 Incoming Material Quality Chain

`Material → MaterialSnapshot → MaterialReceipt → MaterialLot → InspectionRequest → SamplingTask → SamplingDetail → Sample → InspectionTask → InspectionItem → TestExecution → TestResultRevision → InspectionReport → ReleaseDecision → Inventory AVAILABLE → MaterialIssue → Weighing → MaterialCharge → ProductionBatch`

The inspection-exempt branch skips only the sampling/inspection objects. It still terminates in an immutable `ReleaseDecision` before inventory becomes available.


## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.

## DCP-MES-006-R2-001 current contract

The approved 00_DESIGN_CHANGE_DCP-MES-006-R2-001_APPROVED.md is normative for MES-006 and supersedes prior contradictory process/product/eBR scope prose. Product is owned here; material has no business version; eBR is independently owned by MES-007. Process signature binds immutable business version and definition content. New physical V011 only. Test requirements include TC-PROC-004.
