# 领域模型与业务规则

Product→ProcessPackageVersion→Formula/BOM+Route+EBRTemplate。ProductionOrder→MainBatch→SubBatch(optional)→ExecutionUnit→OperationExecution。Material→MaterialLot→InventoryLedger。Weighing→MaterialCharge→QuantityEvent→BalanceResult→Genealogy。MainBatch→Sample/Deviation→ReleaseDecision。

## v1.0.2 Incoming Material Quality Chain

`Material → MaterialVersion → MaterialReceipt → MaterialLot → InspectionRequest → SamplingTask → SamplingDetail → Sample → InspectionTask → InspectionItem → TestExecution → TestResultRevision → InspectionReport → ReleaseDecision → Inventory AVAILABLE → MaterialIssue → Weighing → MaterialCharge → ProductionBatch`

The inspection-exempt branch skips only the sampling/inspection objects. It still terminates in an immutable `ReleaseDecision` before inventory becomes available.
