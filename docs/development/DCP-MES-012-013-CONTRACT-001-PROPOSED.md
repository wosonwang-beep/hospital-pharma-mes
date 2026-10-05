# MES-012 / MES-013 baseline review and bounded completion proposal

Date: 2026-10-04. Status: PROPOSED / NOT APPROVED. This document is not an authoritative design or permission to change frozen contracts.

## Request and dependency check

The user requests continued development of MES-012 and MES-013. Use FINAL BASELINE COMPLETE v1.0.15 and existing UI layouts, with inline label/input rows. MES-008A/009/010/011 are ACCEPTED, satisfying MES-012 hard dependencies. MES-013 also consumes complete MES-012 BalanceResult and production quality evidence; those producers are not yet physically available. Existing incoming-quality and bounded IPC approvals explicitly exclude complete production CAPA/balance/finished-product release contracts.

## Verified implementation map

| Scope | Frozen source | Existing implementation | Finding |
|---|---|---|---|
| Production IPC | MES-012 task; functional closure §§3–8 | IpcService, signed result/review revisions and current operation gates | MATCH for the approved staged producer; reuse it |
| Incoming investigations | Incoming chain contract / scope dispatch | IncomingQualityService, incoming deviations and immutable retest facts | MATCH for incoming scope; not a production investigation contract |
| Material balance | PRD BAL-001..003; DB mes_quantity_event/rule/result/investigation; FD-BAL-001 | CHARGE quantity-event producer exists; balance application/approval absent | MISSING and DESIGN GAP |
| Production deviation/OOS/OOT/CAPA | PRD QMS-DEV-001; DB qms_deviation/qms_capa; Deviation aggregate | Production branch of shared deviation endpoints fails closed | MISSING and DESIGN GAP |
| Finished QA / archive | MES-013; FD-REL-001; qms_release_decision / ebr_pdf_manifest | mes-release contains package marker; QA submission adapter deliberately rejects unavailable quality/balance contracts | MISSING; DEPENDENCY_NOT_READY and DESIGN GAP |

## Concrete blocking contracts

1. **Material-balance rules and approval.** DB defines formula_expr/basis/check_point/tolerances but no frozen expression grammar, event polarity/grouping, safe evaluation semantics, UOM conversion and missing-output/WIP treatment, rule-authoring/freezing handoff or reproducible input boundary. PRD forbids fixed thresholds and manual totals. OpenAPI only has generic bodies for recalculation and opening investigations; no investigation approval operation exists despite the detailed API requiring balance:approve. FD-BAL-001 states PASS/FAIL→INVESTIGATING→APPROVED, whereas the state-machine document uses CALCULATED_PASS/CALCULATED_FAIL/APPROVED_EXCEPTION/RECALCULATED. These names and persisted targets must be reconciled explicitly, not independently implemented as duplicate enums.
2. **Production quality/investigation/CAPA.** Production deviation requests still use GenericRequest (additionalProperties=true). The typed incoming commands bind incoming lots/results and cannot be reused as production DTOs. The baseline specifies qms_capa and lifecycle requirements without complete CAPA command/state/permission contracts. Production sample/test standard selection, applicable evidence and approved retest linkage need an explicit production contract; incoming facts must remain unchanged.
3. **Finished-product QA and archive.** POST /release-decisions and POST /main-batches/{id}/ebr/pdf still use GenericRequest. Need exact release/reject/supersession and QA review-model DTOs, effective decision chains, complete upstream gate/current-lock/recheck rules, finished-lot/output ownership and inventory consequences, and PDF generation/file/digest/manifest lifecycle. Reuse shared release/signature/file facts rather than another release source. Existing immutable PDF manifest columns and QA_RELEASE_DECISION RELEASE/REJECT provider requirements remain mandatory.
4. **UI and consistency.** The formal UI routes, DESIGN_SYSTEM, runnable prototype mappings and UI-BAL-V/UI-QA-RELEASE screenshots were inspected. Preserve their existing summary cards, detail table, investigation panel, QA gate checklist, signature dialog and inline label/input geometry. A prototype button does not supply its missing typed API, permission, state or signature contract. Complete only the missing field/action mappings and resolve conflicting inherited labels against authoritative route/state definitions.

## Proposed approval boundary

Authorize a bounded DCP-MES-012-013-CONTRACT-001 to complete only the above missing contracts needed by BAL-001..003, QMS-IPC-001/QMS-001/QMS-DEV-001, REL-001 and EBR-008. Reuse existing entities and infrastructure by default. Any necessary additive columns/constraints/records, state/action definitions, typed APIs, permissions, signing controls and UI action bindings must be explicitly specified in the new cumulative release before code implementation. Preserve the six incoming records, accepted-task behavior and all historical releases. Do not invent unrelated features or another source of truth.

Publish a new cumulative baseline only after Database, Domain, State, OpenAPI, UI, Permission, GMP, Test/RTM, Integration and task dependency consistency review. Previous v1.0.15 remains unchanged. Use new physical Flyway migrations allocated only after querying the highest successful native DEV version; never edit V001..V024 or run repair/reset.

## Execution and validation after approval

Complete MES-012 first: approved migration → domain/state → mapper → application/API → audit/signature → existing UI → targeted tests/RTM. Then develop MES-013 when its real consumed contracts are available; the user's request authorizes both tasks in that sequence, with no automatic acceptance.

Map TC-BAL-001..004 and TC-QMS-001..004; test quantity-event provenance/reversal, reproducible calculations, approved exception, critical deviation blocks, protected original FAIL and signed independent review. Map TC-REL-001..004 and TC-EBR-010; test signed immutable finished decision, stale gate recheck/concurrency, inventory atomicity and exact PDF manifest/hash. Run affected upstream checks and the mandatory final batch-lifecycle integration gate. Local tests preserve the native database; no repetitive full regression during implementation.

## Current outcome

DESIGN CHANGE REQUIRED for the blocking portions. No product code, migration, database operation or test run was performed in this review. MES-012 remains IN PROGRESS for its existing approved scope; MES-013 remains NOT STARTED. User approval of this bounded completion proposal is required before adding or reconciling frozen contracts.
