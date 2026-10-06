# Finished inbound optional inspection draft implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan inline task-by-task. Keep one fresh scoped final review. User approved the bounded in-chat design; continue without another approval loop.

**Goal:** Optional atomic linked finished inspection DRAFT during completed-production inbound creation; warehouse gates unchanged.

**Architecture:** WMS synchronous typed domain event → boot listener → MANDATORY QMS early draft method; existing tables/closed API extended only by optional command fields. UI T2 checkbox/T3 existing-read link. Current root checkout on codex/finished-management-v1-0-20 preserves prior uncommitted authorized work and native environment; no clean/reset/new database. No commits/pushes.

**Spec:** docs/development/DCP-FINISHED-INBOUND-INSPECTION-DRAFT-001-APPROVED.md.

## Task 1 — controlled transaction and guards
- [x] Add targeted native tests in FinishedInboundInspectionDraftIT: optional/replay/frozen source, before/after-confirm gates, no child permission, child audit rollback, cancelled source. Observe RED.
- [x] Extend FinishedGoodsService closed create validation/permission and synchronous domain event. Add domain record + boot listener; no module dependency cycle.
- [x] Refactor only QMS create to share confirmed API / internal early-draft paths; require active source and MANDATORY for event path; suppress premature SUBMIT from read actions.
- [x] Native GREEN plus existing warehouse/full-finished-chain direct regressions; preserve existing data/migrations.

## Task 2 — T2/T3 presentation
- [x] Add meaningful unit serialization tests and PC Chromium optional create/link/gate case; observe RED.
- [x] Existing FinishedDetailView T2 checkbox/number, existing T3 real child lookup/link/hint; shared model helper closed payload; no new pages/routes/styles.
- [x] Typecheck, affected frontend units/build, targeted Chromium screenshot/no errors.

## Task 3 — reconciliation and readiness
- [x] Cumulative v1.0.21 approved supplement, exact single create schema delta, UI/test/RTM/integration/dependency/migration NONE; v20 parent hashes unchanged. One scoped independent review; fix CRITICAL/HIGH.
- [x] Consistency PASS, current pointer update/frozen manifest, acceptance evidence and READY FOR ACCEPTANCE in MES_TASKS only. No next task or push.

## Ledger
- 2026-10-06 startup: explicit scope approval, v20 read; no new schema required. Existing uncommitted finished-chain work/local configs/installed skills preserved.

- Task 1 complete: RED native4 (unknown option + permission assertion); GREEN new4+existing receiving/full chain2, failures0/errors0, 38.769s.
- Task 2 complete: RED frontend serialization2 and missing browser checkbox; GREEN frontendunits9/typecheck/build9.11s; Chromium optional flow1/10.8s with3 PC1440x900 screenshots, no errors/overflow.
- Ruling: consume existing optional child through permission-scoped list(mainBatchId); unique inbound-per-batch contract means bounded first page is sufficient, no new lookup endpoint.
- Task 3 in progress: cumulative21 candidate built,13 consistency checks PASS,138parentfiles unchanged; independent schema RED observed thenGREEN1. One scoped fresh final reviewer dispatched; current pointers still20 until closure.

- Final review: fresh reviewer found no CRITICAL/HIGH/MEDIUM; LOW audit/conflicting-payload coverage completed. Expanded native case first used wrong ComplianceException helper; corrected to established ResourceConflictException, finalPASS1/22.457s. No production mutation for test failure.
- Screenshot review ruling: neutral “关联成品入库申请” in T3 prevents implying receipt confirmation; standalone create picker remains confirmed-only. Assertion RED thenGREENChromium1/9.5s. Draft browser fixture clears non-existent sampling/report collections. Final typecheck/build12.81s PASS.
- Task3 complete: candidate13checksPASS before switch, all138parentbytes immutable, pointers21 aligned, scoped verification evidence/RTM recorded. No commit/push or automatic human acceptance.
