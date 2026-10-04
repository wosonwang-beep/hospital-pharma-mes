# MES-008/008A/010/011 current full-task closeout

Authority: user explicitly requested completion of these four tasks and previously approved bounded incoming and functional-closure contracts. FINAL BASELINE COMPLETE v1.0.15 remains authoritative and immutable. This is current acceptance evidence, not a new design release. Old acceptance increments retain their dates and limitations. MES_TASKS.md alone defines task status; human acceptance is never inferred.

## Evidence sources

- [Original actual chain](ACCEPTANCE.md), [original evidence](evidence/verification-summary.json).
- [Earlier production/eBR closure](MES-007-011-2026-10-04.md), [second increment](CLOSEOUT-2026-10-04.md), [dated eligibility/quota](CLOSEOUT-2026-10-04-R2.md), [numeric/text/retention/successor/charge competition](CLOSEOUT-2026-10-04-R3.md).
- [Actual inventory freeze, IPC and clearance recovery acceptance](../functional-closure/RECOVERY-2026-10-04.md), [exact26-case evidence](../functional-closure/recovery-evidence.json). This closes the earlier unavailable producer classifications and all four final production gate competition orders; it is not evidence for other missing scenarios.
- Current additional exact scenarios: AttachmentIT.actualMultipartHttpPreservesBytesReplayPermissionsAndSizeFailures; QcFinalContractIT eightActualHttpOperationsPreserveEtagsClosedDtoPermissionsAndScope, identityUniquenessAndOrphanConstraintsRejectWithoutExtraFacts, definitionBoundsExclusivityPrecisionAndRequiredValuesAreRejectedAtomically, realPasswordRedisExpiredBoundAndSingleUseTokensProtectActualApproval, everyQcHttpOperationRequiresItsPermissionAndSignedActionsRequireSign, namedLifecycleRejectsEveryInvalidTransitionAndRetiredContentMutation, expiredRealRedisTokenOnActualQcHttpReturnsUnauthorizedWithoutFacts; QcProducerConcurrencyIT editAndApprovalSerializeWithoutSigningStaleDefinition and requestAndRetirementSerializeWhileHistoricalSnapshotRemainsValid (each both orders); IncomingFinalContractIT.qcHandoffAuditRecordsActorTimeReasonAndBothSnapshotDigests. Current logs and counts are recorded separately after verification.

## Mandatory task-card mapping

| Task / formal cases | Exact implementation / passing scenario |
|---|---|
| MES-008 / TC-WMS-001 | WmsIT.draftSaveConfirmIdempotencyAndSnapshotAreStable; realIncomingReleaseThroughSignedWeighChargeReturnAndReversePreservesTrace rejects actual reservation before QA; IncomingQualityIT.completeChainOnlyIndependentQaMakesLotEligibleAndHistoryVerifies retains QUARANTINE and QC_PASS distinction |
| TC-WMS-002 | WmsIT.ledgerRebuildMoveAdjustmentPrecisionAndPhysicalAppendOnly; actual production issue/charge/reversal/return chain proves reconciliation and append-only histories |
| TC-WMS-003 | WmsIT.draftSaveConfirmIdempotencyAndSnapshotAreStable; receipt replay gives one lot/ledger; confirmAuditFailureRollsBackLotLedgerSnapshotAndIdempotency |
| TC-WMS-004 | IncomingProductionIT.fefoSelectsEarlierReleasedLotAndIssueIsNotConsumption; dated expiration/retest native rejection cases in R2 |
| TC-WMS-005 | Same FEFO scenario proves issue without MaterialCharge is not consumption; actual charge chain creates QuantityEvent and debit only at consumption |
| TC-WMS-006 | WmsIT.controlledExemptionHasRuleEvidenceNullHumanSignerAndNoFabricatedQcRecords; SYSTEM_RULE evidence with null human signer, no artificial QC chain |
| TC-WMS-007 | WmsIT.exemptReceiptRequiresEveryCheckAndApprovedSupplierBeforeAnyReleaseFacts; approvedNonpreferredSupplierAllowedButExpiredSourceAndFailedChecksBlocked; invalid source/checks do not create release evidence |
| TC-WMS-008 | draftSaveConfirmIdempotencyAndSnapshotAreStable changes test-owned master policy after receipt, confirms frozen snapshot unchanged |
| TC-ELG-001..002 | R2 actual reserve/issue UTC expiry/retest refusals; FunctionalClosureConcurrencyIT.signedFreezeSerializesWithEveryActualFinalProductionGate plus committedFreezeWinsAgainstWaitingReservationWithoutProductionFacts proves RESERVE/ISSUE both commit orders, no failed ledger/key facts |
| MES-008A / TC-QMS-IN-001 | actual receipt/request/accept chain, IncomingClosureIT.requestRejectsDraftRetiredAndForeignMaterialWithoutWritingFacts and convertedQuantityAndPackageBoundsPreserveFrozenRequestWhenNextStandardIsApproved; current handoff audit asserts actor/time/reason/old+new digests |
| TC-QMS-SMP-001..002 | IncomingQualityAcceptanceIT.otherPurposeRequiresSignedPlanAndEveryContainerBeforeCompletion: actual five packages, four blocked, fifth completes, one behavior record producing independently traceable samples |
| TC-QMS-SMP-003 | Same signed OTHER_APPROVED plan/self-approval rejection; retentionSampleStorageLabelAndSignedDisposalPreserveSourceLineage; original approved retest chain; samplingAllocationAndUnapprovedRetestOrOtherPurposeCannotCreateSamples rejects allocation and unauthorized purposes |
| TC-QMS-TST-001 | numericAndTextRequiredItemsRequireExactReviewAndAggregateOriginalSnapshots plus original chain: frozen standard/method/limits, immutable execution/results, exact numeric/text required-item review |
| TC-QMS-TST-002 | quarantineAndOpenOosBlockReleaseAndFailCannotBeOverwritten; signedRevisionReportSupersessionAndRejectPreserveEveryPredecessor: FAIL cannot become PASS/INVALID; authorized passing correction preserves original values/signature/predecessor |
| TC-QMS-TST-003 | missingRequiredResultCannotSubmitReviewAndPreservesTaskVersion and two-required-item scenario reject incomplete submit/review without version/key changes |
| TC-QMS-RPT-001 | Original actual chain; approvedRetestCreatesIndependentExecutionAndNeverChangesOriginalFail; two-required-item numeric/text aggregation; unreviewedResultsCannotGenerateReportAndCallerCannotSupplyIndependentResults. Reports reference original/final persisted result identities, never independent manual facts |
| TC-QMS-RPT-002 | signedRevisionReportSupersessionAndRejectPreserveEveryPredecessor: approved reopen rejected, controlled successor retains original approved signature |
| TC-QMS-MREL-001..002 | Actual QC_PASS before QA blocks reservation; real independent QA release permits production; releaseRequiresIndependentActorPermissionAndReauthenticationFailureRollsBack and releaseAuditFailureRollsBackSnapshotsAndRuntimeThenDirectReleaseReplays prove actor/permission/signature/audit atomicity |
| TC-QMS-MREL-003..004 | Signed rejection/supersession with original history/signatures; NativeConcurrencyIT.concurrentSignedReleaseSuccessorsHaveOnlyOneWinnerAndRetainOriginalSignature proves actual one-winner immediate successor |
| TC-WMS-006..008 | MES-008 mappings above, consumed without duplicate implementation |
| TC-ELG-001..004 | Actual four final gates RESERVE/ISSUE/WEIGH/CHARGE at date boundaries; both actual signed quality-supersession and inventory-freeze competition orders; new attempts reject while exact successful replay remains stable; no rejected consumption facts |
| MES-010 / TC-OP-001 | IncomingProductionIT.actualFormCompletionDrivesProductionCompleteButCannotFabricateQa rejects complete-before-start; trustedDeviceReplayEquipmentSegmentsAndPredecessorStayConsistent rejects skipped predecessor and missing equipment and preserves running intervals |
| TC-OP-002 | FunctionalClosureIT.pendingIpcBlocksCompletionThenOriginalFailRequiresSignedInvalidationAndNewResult: actual PENDING/FAIL/unreviewed IPC blocks complete, original FAIL preserved, signed invalidation/new reviewed PASS permits complete; committedIpcFailBlocksWaitingCompletionUsingCurrentResult proves concurrency |
| TC-PAR-001 | trustedDeviceReplayEquipmentSegmentsAndPredecessorStayConsistent: trusted sourceMessageId replay one parameter value, changed content rejected, manual DEVICE spoof forbidden |
| TC-EQP-002 / Clearance/equipment direct integration | Actual independent signed clearance permits start/resume; missing/current FAIL/new equipment blocks; signed audit rollback; competing new clearance FAIL vs start proven in functional closure |
| MES-011 / TC-WGH-001 | realIncomingReleaseThroughSignedWeighChargeReturnAndReversePreservesTrace: actual RELEASED/BOM/configured calibrated scale, exact quantities and independent signed verification |
| TC-WGH-002..003 | weighingRejectsWrongFormulaToleranceExpiredScaleAndRevokedRelease: wrong material/formula, independent actor, tolerance, calibration and quality revocation refusals |
| TC-CHG-001..003 | genealogyOrAuditFailureRollsBackWholeChargeThenReplayConsumesOnlyOnce; actual charge creates Charge/QuantityEvent/Ledger/Genealogy/Audit atomically, injected failures roll back, exact replay consumes once; concurrentChargesCannotConsumeSameVerifiedWeighingTwice proves one winner |
| TC-TRC-001 | Real charged material bidirectional graph and organization denial, original FAIL/retest/standard/task/item/report/QA/signatures; FunctionalClosureIT.chargedMaterialReverseTraceIncludesActualSignedInventoryDecisions adds actual inventory control evidence |

## QC producer mandatory additions

| Formal cases | Current evidence |
|---|---|
| TC-IN-QCS-001 | Current root/code/business-version duplicates and DB orphan refusal, existing tenant isolation, no legacy master-version rewrite |
| TC-IN-QCS-002 | Current atomic bounds/type/unit/precision/missing/duplicate negatives, existing draft tombstones; QcSpecificationRulesTest inclusive one/two-sided bounds and missing value never PASS |
| TC-IN-QCS-003 | Current every illegal named transition/retired edit refusal; original approved-content SQL guards; approvingNextVersionDoesNotRetireOrChangeExistingApprovedStandard |
| TC-IN-QCS-004 | Existing all-author separation and signed lifecycle historical verification; current both permissions, real password + Redis actor-bound single-use and expired token, actual expired HTTP rejection; changed reason replay rejects, QcSpecificationEvidenceTest rejects altered reason/content approval envelopes |
| TC-IN-QCS-005 | Current both real edit/approve and request/retire competition orders; prior stale/header mismatch, changed payload, injected signer/audit failure with no business/signature/key success |
| TC-IN-QCS-006 | Existing draft/retired/foreign-material/org refusals, historical standard retains N snapshot after N+1, required numeric/text report identities; current request/retire concurrency |
| TC-IN-QCS-007 | Current all eight actual HTTP operations, ETags/string IDs/closed DTOs/permission refusals. qc-specification.spec.ts list/detail/version signature retry and root creation at desktop/mobile, horizontal fields and exact binding; existing model/typecheck proof |

## Old partial-row disposition and limits

Older RTM partial rows are closed only by the exact mappings above. Empty/oversize and upload/link retry use current actual multipart plus prior incoming-quality.spec.ts conflict retry. Multi-item results, retention, OTHER_APPROVED plans, retest quota/valid original FAIL, independent release, concurrent quality successor and every freeze gate are linked to their actual later scenarios. Audit handoff and QC HTTP/schema/definition/real expired-token/races now have dedicated evidence.

Original frozen/result/report identity and signed-evidence mutation are rejected by database guards and typed services; existing QcSpecificationEvidenceTest tests digest tampering. No legitimate report API changes the original result facts to manufacture a stale digest. A fresh report approval still recalculates its digest and requires exact valid signatures. This is the controlled reachable flow, not a waiver allowing forged reports.

Platform MES-001/002 accepted signing/authorization contracts are reused; critical native incoming/production evidence has actual signing, audit and database services. Most native fixtures control actor context and reauthentication port; the current additional test uses real password checks and actual Redis tokens. No claim of an entire browser login/native API chain, exhaustive Cartesian combinations or PROD validation is made. Browser evidence uses mocked API separately. Full browser login is not an additional task-card gate invented here.

No new migration/schema/API/state/UI design, successful migration changes, repair, database reset or seed changes. Test-owned retained concurrency facts remain signed/audited and their lots are rejected/blocked; rollback fixtures disappear without deleting regulated records. Existing LOW pagination/N+1 and Flyway/MariaDB13 tested-version warning remain technical debt.

Final review and readiness are recorded after the current gates finish; this file alone does not mark acceptance.

## Final gate / review / readiness

Additional **13 distinct native cases PASS**, counting the four parameterized competition orders individually and excluding reruns. QC domain/evidence units **11 PASS**; frontend interceptor units **3 PASS** (both reauthentication codes plus actual login-renewal control). QC browser cases **4 PASS** (two flows at desktop/mobile); typecheck PASS. Affected Java21 Maven reactor compiles with native gates. V001..V024 validates; no new migration necessary.

Production fixes: method authorization exception now returns the existing403 FORBIDDEN envelope; business signature expiry returns401 REAUTH_TOKEN_INVALID (wrong reauthentication401 REAUTH_FAILED), rather than500. Client interceptor distinguishes these two signature errors from expired login, preserves input and never automatically replays the consumed signing token. Normal login401 retains its renewal behavior. Signature retry obtains a fresh exact object/version/meaning-bound token.

Independent final review: CRITICAL0; one HIGH (client incorrectly refreshed/replayed expired signature token) fixed in the single review fix pass. The real401 desktop/mobile test failed before the fix and passed afterward, with no refresh and retained reason/cleared credential;3 isolated frontend units confirm both signature codes and ordinary login renewal. No remaining demonstrated HIGH. LOW missing TC-EQP-002 mapping label corrected with already-passing equipment interval evidence.

Current result: MES-008, MES-008A, MES-010, MES-011 are READY FOR ACCEPTANCE, pending explicit human acceptance. Accepted MES-003..007 unchanged; MES-009 readiness unchanged; MES-012 remains only approved IPC stage and MES-013 not started. [Exact final evidence/log hashes](evidence/final-closeout-2026-10-04.json). Previous partial classifications are historical and superseded only by this explicit current mapping. No commit/push/merge or database reset/repair.

## Explicit human acceptance — 2026-10-04

After the final closeout report identified MES-008, MES-008A, MES-010 and MES-011 as READY FOR ACCEPTANCE, the user explicitly replied “验收”. Acceptance applies to these four tasks and their bounded approved contracts, mapped verification and recorded limitations above. MES_TASKS.md now records all four as ACCEPTED. Prior readiness and test evidence remain unchanged as history. This acceptance does not include MES-009, expand MES-012/013, alter the authoritative v1.0.15 baseline or initiate further development.
