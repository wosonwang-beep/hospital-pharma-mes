# Audit HIGH closure — 2026-10-05

Status: READY FOR ACCEPTANCE. Explicit approval: user “批准” after review of DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001. Current business authority: FINAL BASELINE COMPLETE v1.0.17; inherited v1.0.16 is immutable. Global UI V2/T1–T6 is unchanged. Original classifications and code evidence remain in [CURRENT STATE CONFIRMATION](MES_V1.0.16_CURRENT_STATE_CONFIRMATION.md).

## Results

| Issue | Result |
|---|---|
| HIGH-01 | FIXED — material-lot guidance reflects the implemented incoming eligibility chain. |
| HIGH-02 | FIXED — issue guidance describes actual prerequisites instead of permanent unavailability. |
| HIGH-03 | FIXED — existing trace-source incoming/stock/production lineage in T4 tabs, permission-controlled source navigation. |
| HIGH-04 | FIXED — production-order product/date/status queries and navigation restoration. |
| HIGH-05 | FIXED — production-batch order/product/date/status queries; existing positive productionOrderId formally published in v1.0.17 OpenAPI and producer contract. |
| HIGH-06 | FIXED — independent frozen Order/MainBatch state enums and localized selectors. |
| HIGH-07 | FIXED — incoming read DTOs return server-derived allowedActions; nested item/execution/revision controls and root controls fail closed without actions and intersect existing operation permissions. Assigned actor, qualification, independent review, completion evidence, report digest, retest approval/quota and signing permission narrow availability. Existing transactional command checks remain authoritative for current evidence and submitted inputs. |
| HIGH-08 | FIXED — equipment identity mutex acquired in sorted ID order before material gates, current locking reads check active runs, and PAUSE/COMPLETE acquire the same mutex. Conflicting operations cannot both acquire active occupancy; all segments/audits retained. Existing paused binding appends a usage, rather than overwriting prior usage/run identity. |
| MEDIUM-05 | FIXED as direct companion of HIGH-03. |
| MEDIUM-01–04 | STILL VALID — list edit navigation, trace root picker, menu grouping and duplicated desktop/mobile menu definitions remain outside this HIGH-first scope. |

## Verification

- Backend targeted unit tests: 15 PASS — IncomingRulesTest 4, IncomingRecordStatesTest 6, IncomingEvidenceTest 1, ExecutionRulesTest 2, IncomingActionPolicyTest 2. Affected modules compile on Java 21.
- Native MariaDB targeted integration: 11 distinct cases PASS — IncomingAllowedActionsIT 1; IncomingQualityIT 6 (complete chain, unhandled OOS, original FAIL protection, approved retest, rollback and org isolation); IncomingFinalContractIT 1 (audit); IncomingProductionIT 2 (production-order filter and equipment segments/predecessors); NativeConcurrencyIT equipment occupancy 1.
- Final signing-action check additionally verified that missing ebr:sign removes signed result actions, result signatures still verify, and QC/report completion remains separate from QA release. No full regression.
- Native concurrency uses separate connections and different production orders. At most one active owner is asserted, then a fresh losing command must return EQUIPMENT_OCCUPIED; PAUSE frees occupancy, occupied RESUME is rejected, COMPLETE frees occupancy, and all three ended segments remain. Native MariaDB snapshot isolation can return error 1020 on a locking read of a row inserted after the transaction snapshot. The existing CONCURRENT_MODIFICATION translation fails closed; the test does not accept a second successful start. Diagnostic evidence and unique retained fixtures are preserved; no global privilege, database rebuild, deletion or Flyway repair was used.
- Flyway validates all 26 migrations; schema version 026 is up to date. No new migration or physical schema change.
- Frontend typecheck PASS; 16 related unit tests across three files PASS (1.10s); build PASS (9.45s), inherited LOW bundle-size warning only.
- 26 distinct targeted Chromium desktop/mobile cases PASS across scoped runs: 10 existing audit query/lineage/permission cases; 12 incoming root action/permission cases; 2 nested result controls; 2 QA material-decision action cases. No unrelated full browser regression. Initial fixture permissions and Chinese two-character button spacing were corrected; no application assertion or business gate was weakened.
- Browser tests render the actual Vue app with API fixtures, not a native backend. Fixtures intentionally prove that actions are consumed from server arrays instead of inferred from status; they are not fabricated GMP records. Native integration provides the separate business/state/signature proof.
- Checked root screens have zero console/page errors and document overflow. Existing screenshots and incoming desktop/mobile screenshots were saved; representative mobile rendering was inspected. Browser plugin was unavailable, so existing Playwright Chromium was used.
- Deterministic cross-document consistency PASS: 109 parent files unchanged; exact bounded query/DTO delta; unchanged paths, permissions, UI routes, business enum schemas and executed migrations; generated DTO schemas aligned; Test/RTM mappings present. Current release hash manifests refreshed before completion.

## Evidence

- [Approved contract](../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/00_AUDIT_HIGH_CONTRACT_V1.0.17.md)
- [Consistency review](../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/00_AUDIT_HIGH_CONSISTENCY_REVIEW.json)
- Screenshots: `C:/Users/Administrator/AppData/Local/Temp/mes-audit-high-existing/` and `C:/Users/Administrator/AppData/Local/Temp/mes-audit-incoming-actions/`.
- Scoped build/test summaries in module target reports; local command logs under `%TEMP%/mes-audit-*.log`. Native test-owned history/evidence stays in the shared DEV database and docs/acceptance/incoming-quality/evidence; no regulated history is concealed or removed.

## Review and limits

Scoped code/contract review: no remaining CRITICAL/HIGH finding among HIGH-01–08. Dynamic actions are removed recursively from audit fact decoration and never enter signature canonical evidence; availability is not an authorization or release bypass. No generic status API, new business table/state/permission/route, GxP weakening, future MES task or Phase 2B. Remaining debt: MEDIUM-01–04 and inherited LOW frontend bundle warning. Persistent unique diagnostic fixtures are retained and their material lot blocked by the existing audited QA decision.

Authority pointers in AGENTS.md and docs/PROJECT_BASELINE.md now identify v1.0.17. Existing accepted MES statuses remain unchanged. User-owned local database configuration changes are preserved. This work is uncommitted/unpushed; task readiness does not imply human acceptance.
