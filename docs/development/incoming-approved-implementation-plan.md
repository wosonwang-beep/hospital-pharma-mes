# Incoming quality approved implementation plan

Spec: DCP-INCOMING-QUALITY-GAPS-001-PROPOSED.md and DCP-MES-009-010-CONTRACT-001-PROPOSED.md, explicitly approved on 2026-10-03 including full acceptance scope. The proposed filenames preserve review history; current approval is recorded in MES_TASKS and the next cumulative release.

Goal: implement the real six-record incoming quality workflow and production reverse trace, preserving original test facts and independent QA release.

Architecture: Java 21 / Spring Boot modular monolith; MyBatis-Plus persistence, native MariaDB, existing transactional audit/signature/idempotency. No fake producer, alternate database, destructive reset, or duplicate business source. Existing checkout and unrelated changes are preserved. No commit/push is requested.

Use executing-plans for ordered integration and dispatching-parallel-agents only for independent contract/module ownership. Agents must not modify shared baseline pointers, executed migrations or integration gates concurrently. Root owns integration, migration allocation and final targeted native/E2E gate.

- [x] Complete approved QC specification, incoming-chain, attachment and production contract appendices; reconcile concrete APIs, DTOs, PK/FK, state, permissions, signatures, UI and TC/RTM. Publish cumulative v1.0.11 only after cross-consistency review. Preserve v1.0.10 and earlier hashes.
- [x] Add new physical migrations in dependency order after native history verification (observed successful V001–V013). QC standards/attachments first; incoming QMS next; real batch/eBR runtime next; execution then weigh/charge and delayed FKs. Reserve numbers centrally, never silently reuse/rewrite executed files.
- [ ] Implement QC version authoring/approval/retirement, immutable snapshots and real consumer query service. Tests cover independent signer, immutable approved content, retired historical use, limits/text criterion and org/idempotency/concurrency boundaries.
- [ ] Implement incoming request → sampling → sample → inspection execution/result → review → automatic report, with frozen standard FK and append-only original FAIL. Implement narrowly approved incoming investigation and retest disposition using the shared QMS investigation aggregate.
- [ ] Implement independent QA and exemption decisions, real MaterialEligibilityService and concurrent freeze/supersession protection. QC PASS alone never grants availability. No provider/evidence means failure, not an assumed completed check.
- [ ] Implement real MES-009/010 and deferred MES-007 runtime, then WMS reservation/issue, MES-011 weighing/charge/genealogy. Bind QMS/WMS batch and eBR operation FKs only to real persisted producers. Retain MainBatch and Operation gate requirements.
- [ ] Implement existing authorized routes and approved QC specification/attachment additions with horizontal labels at desktop/mobile, dedicated create/execute, read-only view and shared signing UI. Show exact server facts, not mock completion.
- [ ] Run targeted module compile/unit tests as each deliverable stabilizes; one safe native integration gate per materially complete stage, only failed/affected cases rerun. Test fixtures are unique and rollback. Full integrated acceptance runs once when real dependencies exist.
- [ ] Prove main positive E2E and A–E: quarantine and QC-only block use; unresolved OOS blocks release; retest cannot overwrite original FAIL; production charge reverse traces all records. Include original task required cases, audit/signature rollback, idempotency, concurrency, version freeze, report completeness, org isolation and permission checks.
- [ ] Scoped review and RTM evidence; fix CRITICAL/HIGH, record remaining debt. Only READY FOR ACCEPTANCE after every applicable gate; ACCEPTED remains human-only. No automatic finished-product QA/production IPC expansion.

Execution ledger — current work:

- Explicit approval recorded. v1.0.11 initial completion and v1.0.12 bounded runtime/return/weigh supplement published after static consistency review; prior releases unchanged. v1.0.12 resolves 1,978 local OpenAPI references. v1.0.13 freezes the missing material weighing configuration producer and resolves 1,979 references; prior v1.0.12 files remain byte-identical.
- V014–V021 applied successfully to persistent native MariaDB on 2026-10-03 (8 migrations, 4.780s). Earlier V001–V013 validated, no reset/repair/history rewrite. All executed files are now immutable.
- QC, incoming QMS, production/WMS and attachment implementations have initial targeted unit/compile evidence. eBR runtime and weighing/charge implementation continue. Unit gate counts are recorded in final verification evidence only after the last affected changes.
- Root owns boot adapters/security/controller routing, receipt attachments and shared router/menu; disjoint agents own QC/eBR, QMS/UI, production/execution/trace. No simultaneous Maven/native gates.
- Actual database integration, rendered UI and full positive/negative end-to-end acceptance are pending. Tasks remain IN PROGRESS. No runtime completion or acceptance inferred from contract publication.


Final execution update — 2026-10-03 (supersedes earlier pending implementation ledger):

- QC, six incoming records, independent release, attachments, real production/execution/eBR, weighing/charge and reverse trace are implemented within approved contracts. Cumulative v1.0.14 freezes the exact historical-standard trace projection; 98 manifest entries verified.
- V014–V022 executed successfully; history 22 successes/0 failures. No executed migration changed, no reset/repair. Final read-only incoming actor/material sentinel counts both zero.
- User positive E2E and A–E are proven by targeted native tests, including actual issue rejection before QA and immutable original FAIL after approved retest. Final affected chain method PASS 1/1, suite 11.423s. Total distinct native methods 26, browser cases 18 (mocked APIs), vue-tsc -b PASS. No repeated full regression.
- Scoped HIGH findings closed. Exact evidence and scope limitations: ../acceptance/incoming-quality/ACCEPTANCE.md and RTM.md; machine-readable evidence/verification-summary.json.
- Checklist entries that combine implementation with the broader frozen task's exhaustive mandatory cases remain open for those coverage obligations. The incoming A–E subset is reviewable; this does not waive remaining RTM negatives/concurrency or mark complete MES tasks READY/ACCEPTED. All task status remains exclusively in MES_TASKS.md.
