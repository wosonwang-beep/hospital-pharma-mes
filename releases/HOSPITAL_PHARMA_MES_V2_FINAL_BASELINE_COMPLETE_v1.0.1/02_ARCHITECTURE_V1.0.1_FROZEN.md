# 系统架构设计说明书

模块化单体：iam/masterdata/process/production/wms/execution/qms/release/audit/integration/reporting。业务写入与审计同事务；集成使用outbox；受控定义发布后不可原地修改；写接口幂等+乐观锁。

# V1.0.1 Controlled Platform Policies — DCP-MES-001-R2-001
The following platform policies are normative and centrally owned by MES-001:

- `IntegrationRetryPolicy` is the only source of the automatic retry limit and delay schedule. V1.0.1 freezes maximum attempts at **8** and delays after failures 1–7 at **PT1M, PT5M, PT15M, PT1H, PT4H, PT12H, PT24H**. Business services, controllers, connectors and downstream modules must not hard-code or override these values.
- `SignatureCanonicalizer` owns RFC 8785 JCS canonicalization and lowercase SHA-256 digest production. Business modules contribute data only through registered `SignableObjectProvider` contracts.
- `PlatformIdempotencyService` owns generic HTTP idempotency using `platform_idempotency_record`. A downstream module may add a specialized idempotency store only after an approved business-specific design change.
- `EnterpriseValidationProperties` binds RPO, RTO, GxP retention, site and approved time-source configuration. It supplies validation gates, not invented enterprise values.

Changes to any of these policies require controlled configuration approval or a new Design Change Proposal. DCP-MES-001-R2-001 does not authorize code or migration execution.
