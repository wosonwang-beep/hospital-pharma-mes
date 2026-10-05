# MES-013 finished QA implementation evidence — 2026-10-05

## Authority and bounded scope

User explicitly approved DCP-MES-012-013-CONTRACT-001 and sequential MES-012 then MES-013 implementation. FINAL BASELINE COMPLETE v1.0.16 remains authoritative. This implementation covers finished-product QA only; accepted incoming release decisions are preserved. V026 was already applied before these changes and was not edited. No acceptance, commit, push, repair, database reset, or shared-seed mutation is implied.

## Implemented contracts

- Frozen `/qa/batches/{id}/review-model`, `/release-decisions` POST and `/release-decisions/{id}` GET return closed FinishedReviewModel/FinishedDecision projections. POST enforces closed fields, exact quoted If-Match, body version, reviewDigest, FULL_INSPECTION and controlled RELEASED/REJECTED.
- Actual root/plan/operation/eBR review/QC/OOS/deviation/CAPA/balance/owned finished-output inventory evidence drives gates and canonical digest. Order→batch→operation/quality→finished lot/ledger locks protect current proof. Computed actor actions do not enter the digest.
- `QA_RELEASE_DECISION` accepts RELEASE/REJECT only, target `mainBatchId:currentBatchVersion`. Existing platform signing/reauth/idempotency/audit infrastructure is reused. Generic sign cannot perform the controlled QA command.
- Independent qualified QA signs an immutable FINISHED_PRODUCT decision chain. Explicit immediate predecessor and independent superseding actor are required; original decisions/signatures remain immutable. A consumed released lot cannot be retrospectively rejected through ordinary supersession.
- Production owns controlled MainBatch QA_RELEASED/REJECTED transition; WMS owns RELEASED/AVAILABLE or REJECTED/BLOCKED. Decision/signature/root/lot/audit/transactional domain-event outbox effects commit or roll back together.
- FinishedDecisionQuery exposes independent closed decisions and complete immutable source evidence to archive; there is no circular archive→QA-review dependency or second release Source of Truth.

## Targeted native evidence

All fixtures create uniquely identified real records in native `hospital_pharma_mes_dev`, under rollback-only transactions; no shared seed modification. Exact method selectors avoid inherited broad regression.

`FinishedReleaseIT` first targeted gate: **3/3 PASS**, 23.83 s suite / 52.849 s build; `$env:TEMP/mes013-qa-first.log`.

1. `realBatchLifecycleReleaseIsAtomicSignedIdempotentAndMakesFinishedInventoryAvailable`: real incoming QC/QA→reservation/issue→signed weighing→charge/genealogy→finished output→sample/test/review→balance/eBR→production complete→QA pending→finished release/AVAILABLE; real signature, outbox, immutable decision, replay.
2. `changedDisplayedEvidencePermissionAndIndependentQaBlockWithoutDecision`: stale digest, independent QA, revoked permission and no decision/root changes.
3. `auditFailureRollsBackDecisionSignatureAvailabilityAndOutbox`: injected audit failure rolls back every effect and permits exact replay after recovery.

Additional exact-method gate: **5/5 PASS**, 29.24 s suite / 47.621 s build; `$env:TEMP/mes013-qa-additional.log`.

4. `actualGateChangeInvalidatesDisplayedReviewAndRejectNeedsNoFabricatedPassingGate`: actual late CRITICAL record invalidates displayed proof; latest RELEASE remains blocked; signed REJECT succeeds without fabricated passing evidence.
5. `explicitIndependentSupersessionPreservesEveryOriginalDecisionAndSignature`: exact predecessor, independent actor, three immutable decisions and historical signature validation.
6. `actualCompetingQaCommandCannotBypassRootLockOrCreateDecision`: real separate-transaction order/batch contention, controlled 409, no decision/root writes; existing actor identity is read only to avoid fixture-parent FK contention.
7. `outboxFailureRollsBackAllFinishedDecisionEffects`: actual platform outbox injection rolls back decision/signature/root/inventory/idempotency and recovery succeeds.
8. `exactHttpContractPermissionAndGenericSignCannotBypassQaBusinessCommand`: real MockMvc frozen nested-schema checks, permission 403, missing If-Match 400, generic sign 4xx/no side effects, controlled command 201.

Final new-method gate: **2/2 PASS**, 22.50 s suite / 40.095 s build; `$env:TEMP/mes013-qa-oos-stock.log`.

9. `pairedInventoryMovementIsNotConsumptionButPhysicalRemovalBlocksRetroactiveRejection`: real paired WMS MOVE_OUT/MOVE_IN permits independent supersession; real negative ADJUST removes physical stock and blocks retroactive rejection while preserving prior decision/signature/root facts.
10. `actualLateOriginalFailOpensOosAndBlocksFinalQaWithoutOverwritingOriginal`: additional actual Sample/Test is prepared before production completion; its late original FAIL opens actual OOS after QA submission, invalidates displayed proof and blocks fresh release. Original FAIL cannot be corrected into PASS. Frozen nested BatchEbrReadModel validation confirms the FAIL and exact OOS source link; actual generated REVIEW_COPY attachment bytes are parsed as PDF and contain FAIL/OOS. Canonical positive ID and required signature request shapes are checked before any key/decision effect.

All gates validated **26 migrations**, schema **026**, no migration pending. **Ten distinct new QA native methods** passed across three targeted runs; no claim of one ten-method suite. Final gate compiles and exercises the current production-domain delegation, finished lot/ledger proof locks, request validation and final QC/archive projections. Passing earlier suites were not repeated.

## Review/readiness

Independent read-only review reported no QA HIGH findings and confirmed paired movement versus physical-removal semantics and owner proof locks. Final cross-scope review and archive/UI completion are recorded by the root execution ledger. This note does not set task status or human acceptance. No full regression or hosted CI claim. Remaining low-level aggregation pagination follows existing scoped query patterns and can be optimized separately without changing frozen behavior.
