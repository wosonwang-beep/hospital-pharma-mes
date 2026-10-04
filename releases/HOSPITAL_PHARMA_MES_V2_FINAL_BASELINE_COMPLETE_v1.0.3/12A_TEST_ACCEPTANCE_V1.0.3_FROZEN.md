# 测试与验收规格

覆盖正常、异常、权限、状态机、幂等、乐观锁、事务回滚、审计、电子签名、版本冻结、库存一致性、物料平衡、Release Gate。

# V1.0.3 Enterprise Validation Gate — DCP-MES-001-R2-001
DG-008 is an OPEN ENTERPRISE VALIDATION ITEM. It is not a MES-001 implementation blocker. It blocks production validation and go-live until enterprise-approved RPO, RTO, GxP retention, site, time source, restore-drill and referenced SOP/validation evidence are supplied. V1.0.3 supplies required no-default configuration bindings and validation tests only; it contains no invented value and no physical purge behavior.

## DCP-MES-002-R2-001 Acceptance Delta

Acceptance must prove versioned policy/snapshot history; required and exempt receipt branches; qualification failures; package-level sampling lineage; append-only result correction; approved report/result linkage; `QC_PASSED + BLOCKED`; QA release/reject; immutable/superseding decisions; separated statuses; and the shared Reservation/Issue/Weighing/Charge eligibility gate.

Inspection-exempt acceptance explicitly proves that no request, sampling, sample, inspection, result, or report row is fabricated and that `RELEASED + AVAILABLE` occurs only in the same transaction as the `INSPECTION_EXEMPT`/`SYSTEM_RULE` ReleaseDecision and audit evidence.




## DCP-MES-003-R2-001 approved delta

MES-003 readiness requires targeted domain/API/MariaDB/frontend/browser evidence and zero CRITICAL/HIGH review findings for DCP-MES-003-R2-001.
