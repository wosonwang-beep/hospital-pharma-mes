# Current full-task RTM closure — 2026-10-04

[Current formal-case mapping and final review](FINAL-CLOSEOUT-2026-10-04.md) supersedes the historical partial/dependency entries below with exact evidence for MES-008/008A/010/011.13 additional native cases,11 QC units,3 HTTP interceptor units,4 desktop/mobile QC UI cases and typecheck PASS. V001..V024 validated; no migration or repair. Real password/Redis token expiry/binding/single-use and actual HTTP401/403 boundaries are now verified; full browser-login proof is not claimed. All four tasks READY FOR ACCEPTANCE in MES_TASKS.md, not human ACCEPTED. Previous reports remain intact as history. Exact old partial-row dispositions and retained LOW debt are recorded in the current mapping.

---

# Incoming quality implementation-to-evidence RTM

Latest bounded functional closure: [approved V024 recovery / 26 scoped native scenarios](../functional-closure/RECOVERY-2026-10-04.md), authority v1.0.15. Actual inventory freeze/unfreeze and both orders of all four final-use gates now have signed business-command evidence. Production IPC PENDING/PASS/original FAIL protection, independently signed clearance and current-record competition are physically implemented and verified. Exact V2/V1 freeze, original record SQL protection, permission/HTTP/audit rollback, and charged-lot inventory-decision/signature trace are mapped in the recovery evidence. This supersedes those earlier missing-producer/contract statements only; unrelated partial/unverified formal rows below remain visible and are not waived. Whole-task status remains in MES_TASKS.md.

Latest [R3 exact7-method closure](CLOSEOUT-2026-10-04-R3.md) supersedes older missing two-required-item numeric/text report aggregation, retention label/storage/disposal, concurrent competing release successor, concurrent verified-weighing consumption, QC signer rollback/permission/version/replay/no-auto-retire and attachment empty/oversize proof. It does not certify HTTP multipart, real expired reauthentication, exhaustive schema/type matrices, inventory freeze or IPC/clearance producer flows. Required evidence and other remaining rows remain visible; no whole-task readiness inferred.

Current second increment: [exact10 new passing cases and contract/producer boundaries](CLOSEOUT-2026-10-04-R2.md). It closes actual final-command expiry/retest refusal for all four consumers, missing required result at submit-review, exhausted retest quota and signed VALID original FAIL closure refusal. These supersede those specific older partial statements below; actual business freeze workflow, real production IPC/clearance and other unclosed rows remain. Date tests are native real command/database with controlled time, not actual wall-clock crossing or concurrency. MES-007 is human ACCEPTED; MES-009 READY; other requested tasks remain IN PROGRESS. No new migration.

## Current native concurrency mapping — 2026-10-04

User approved retained DEV fixtures. [Exact evidence](evidence/native-concurrency-summary-2026-10-04.json):11 native concurrency cases PASS;32 archived lots all signed REJECTED/BLOCKED. No new migration; V001–V023 validated. Earlier pending-concurrency and zero-residue statements are historical where superseded here.

| Requirement / case | Current evidence | Remaining boundary |
|---|---|---|
| PRD-001 / approved order concurrency | NativeConcurrencyIT.concurrentOrderAllocationHasOneWinnerAndNoRejectedCommandFacts; firstConcurrentAllocationPreventsChangingOrderProduct; concurrentOrderPlanReductionPreventsStaleBatchAllocation | PASS: native contention, valid final quantities/product, precise fresh retry refusal, no rejected-command idempotency success |
| TC-ELG-001 | finalGateSerializesWithActualSignedQualitySupersession RESERVE × both orders | QA-first refuses reserve/no ledger/key; command-first once-only fact/replay then signed rejection. Freeze/expiry/retest variants not certified |
| TC-ELG-002 | Same method ISSUE × both orders | QA-first refuses confirmIssue/no ledger/key; command-first exact replay then signed rejection. Freeze/expiry variants remain |
| TC-ELG-003 | Same method WEIGH × both orders | QA-first refuses weighing/no ledger/key; command-first exact replay then signed rejection. Full remaining final-gate matrix not waived |
| TC-ELG-004 | Same method CHARGE × both orders | QA-first refuses charge/no ledger/key; command-first exact replay, fresh QA rejection if existing409, preserved history. Not proof of an unavailable freeze-command producer |
| TC-BAT-001..004 / TC-UI-002 | [MES-009 full mapping](../mes-009/CLOSEOUT-2026-10-04.md) | MES-009 READY FOR ACCEPTANCE; SubBatch action and mapped release/QA routes explicitly absent under approved replacement contract |

Three reviewed HIGH production stale-read paths fixed, no remaining concrete CRITICAL/HIGH in scoped review. MES-007/009 READY; MES-008/008A/010/011 IN PROGRESS. Other partial assertions below remain required unless the exact later closeout explicitly closes them; actual production IPC/clearance producer integration is DEPENDENCY_NOT_READY. No blanket full-task or human-acceptance inference.

Current closeout: [2026-10-04 exact nine new native methods and rulings](CLOSEOUT-2026-10-04.md). It closes the named request selection/conversion, sampling allocation/authorization, unreviewed report/caller payload, QA actor/permission/reauthentication/audit rollback and controlled-exemption assertions below. MES-007 required TC mapping is complete; actual order allocation/final quality concurrency and unavailable IPC/clearance remain. Older per-run counts are retained as history, not current blockers where explicitly superseded.

2026-10-04 increment: [MES-007–011 evidence and exact remaining gates](MES-007-011-2026-10-04.md) adds16 distinct native methods,14 units and affected regressions. Migration history is now V001–V023. Earlier run counts/dates remain historical; wider incomplete formal-TC assertions remain required.

2026-10-03. Authority: [v1.0.14 TC catalog](../../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14/12_TEST_CASE_CATALOG_V1.0.14_FROZEN.csv), [formal RTM](../../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14/13_REQUIREMENT_TRACEABILITY_MATRIX_V1.0.14_FROZEN.csv), [chain contract §7](../../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14/00_INCOMING_CHAIN_CONTRACT_V1.0.14.md), [QC contract targeted tests](../../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14/00_INCOMING_QC_SPEC_CONTRACT_V1.0.14.md). The initial [implementation map](../../development/INCOMING_QUALITY_IMPLEMENTATION_MAP.md) supplies six-record intent; current frozen contracts resolve its historical gaps.

`Observed` means the listed assertion is exercised by a passing method; it does **not** certify all requirements in a formal TC. `Partial` names the missing assertions. `Not evidenced` means no passing matching scenario is currently recorded here, even if implementation exists. `Pending` is an active verification with no PASS claim. Test abbreviations below refer to full method names in [ACCEPTANCE.md](ACCEPTANCE.md).

| Evidence key | Passing native method |
|---|---|
| Q1 | QcSpecificationIT.signedLifecycleSnapshotsRetirementAndReplay |
| Q2 | QcSpecificationIT.draftTombstonesAndAllAuthorsAreRetained |
| Q3 | QcSpecificationIT.scopeLocksAndSignedDefinitionsFailClosed |
| Q4 | QcSpecificationIT.approvalAuditFailureRollsBackAllEvidence |
| A1 | AttachmentIT.exactBytesReplayAndApprovedReceiptAppend |
| A2 | AttachmentIT.organizationSourceAccessAndStaleLinkFailClosed |
| A3 | AttachmentIT.auditFailureRollsBackAttachmentAndIdempotency |
| I1 | IncomingQualityIT.completeChainOnlyIndependentQaMakesLotEligibleAndHistoryVerifies |
| I2 | IncomingQualityIT.quarantineAndOpenOosBlockReleaseAndFailCannotBeOverwritten |
| I3 | IncomingQualityIT.missingRequiredResultAndCrossOrganizationCannotProduceReport |
| I4 | IncomingQualityIT.resultAuditFailureRollsBackSignatureResultOosAndTaskVersion |
| I5 | IncomingQualityIT.approvedRetestCreatesIndependentExecutionAndNeverChangesOriginalFail |
| I6 | IncomingQualityIT.lotOwnerAuditFailureRollsBackCoupledRequestAndLot |

## Six records and incoming gates

| Requirement / formal TC | Implementation source | Recorded assertion / evidence | Coverage and remaining proof |
|---|---|---|---|
| WMS-001 / attachment contract extension | WmsService, AttachmentService, WmsAttachmentService; V015 | A1 exact bytes/hash, replay/conflict, approved receipt unchanged, immutable data; A2 scope/source/stale link; A3 rollback | Partial: empty/oversized file and UI upload/link retry assertions need linked evidence; no fabricated COA |
| QMS-IN-001 / TC-QMS-IN-001, IQ-STD-01 | IncomingQualityService.createRequest/requestAction, QC query; V014/V016 | I1 creates/submits/accepts real receipt lot and frozen approved specification; I3 rejects other-org request read; I6 rolls back request+lot on owner audit failure | Partial: wrong-material/DRAFT/RETIRED new request, approved unit-conversion quantity bounds, explicit handoff audit field assertions not all separately exercised |
| QMS-SMP-001 / TC-QMS-SMP-001 | samplingAction, qms_sampling_detail/sample | I1 confirms one detail yields two separately traceable samples | Supplemental five-container lineage PASS, with exact 5-required/4-recorded rejection and fifth-container completion |
| QMS-SMP-001 / TC-QMS-SMP-002 | sampling completion count/quantity gates | Supplemental sampling method PASS | Actual five-package receipt/request; task stays IN_PROGRESS with four containers, completes after fifth |
| QMS-SMP-001 / TC-QMS-SMP-003, IQ-SMP-04 | samplingAction/sampleAction/sampleLabel | I1 separate TEST_SAMPLE and RETENTION_SAMPLE allocation and test sample receipt | Partial: label/storage assertions, OTHER_APPROVED signed plan, RETEST_SAMPLE, allocation/unit mismatch and wrong detail/request cases |
| QMS-TST-001 / TC-QMS-TST-001 | execution/result; qms_test_execution/result_revision | I1 execution + confirmed Revision1, stored PASS, historical signature verification and immutable SQL update refusal | Observed positive execution/revision evidence; raw JSON numerical/text variety remains separate coverage |
| QMS-TST-001 / TC-QMS-TST-002 | result(revision=true) | I2 rejects FAIL→PASS and retains original FAIL; unit rules also reject FAIL→INVALID | Supplemental PASS: signed Revision2, predecessor link, original numeric value and original signature preserved |
| QMS-TST-001 / TC-QMS-TST-003 | taskResults / independent review | I3 missing-result request cannot produce report | Partial: missing required result at submit-review/review boundary is a distinct required scenario |
| QMS-TST-001 / IQ-RET-04 | approved execution, per-execution revision, investigation quota | I5 creates distinct approved retest execution and retains original FAIL; IncomingRulesTest exercises inclusive numeric boundaries/missing value | Partial: exhausted quota, unauthorized retest, text, INCONCLUSIVE, precision and stale-revision combinations need evidence |
| QMS-RPT-001 / TC-QMS-RPT-001, IQ-RPT-03 | aggregate/createReport/reportAction | I1 signed report from reviewed result; I5 cross-task report references exact original FAIL and selected PASS with investigation; I3 missing result blocked | Unresolved retest ambiguity now PASS via I5 rerun. Partial: multi-required-item coverage, unreviewed results, caller-supplied result rejection and stale digest at approval |
| QMS-RPT-001 / TC-QMS-RPT-002 | immutable approval slot, report supersession | Supplemental report supersession PASS | Approved report reopen refused; successor approved and original approval signature still verifies |
| QMS-MREL-001 / TC-QMS-MREL-001 | releaseBlocks/eligibility | I1 eligibility false after QC review before QA release; root production E2E now proves actual reservation rejection before QA | Observed positive/negative reservation boundary in actual production E2E |
| QMS-MREL-001 / TC-QMS-MREL-002 | decideRelease, WmsQualityService | I1 signed independent QA release, replay, eligibility true and historical signature valid; I5 same after signed investigation closure | Observed positive release scenario; full invalid actor/signature variants and injected release-stage rollback need separate evidence |
| QMS-MREL-001 / TC-QMS-MREL-003 | named REJECT transition and signed release decision | Supplemental rejection PASS | Signed REJECTED successor, BLOCKED inventory, original RELEASED row and both signatures preserved |
| QMS-MREL-001 / TC-QMS-MREL-004 | immediate-predecessor supersession, lot version advancement | Supplemental supersession PASS | Immediate predecessor and history retained; concurrent competing-successor one-winner case remains unverified |
| QMS-EXM-001 / TC-WMS-006..007 | receipt domain event → evaluateExemption, SYSTEM_RULE decision | WmsIT.directReceivePreservesOriginalPermissionAndRecordsControlledExemption passed in root affected regression | Positive SYSTEM_RULE decision/AVAILABLE/replay observed; explicit no-fabricated-chain, NULL signer fields and invalid exempt supplier/receipt/policy combinations remain partial |
| WMS-ELG-001 / TC-ELG-001 | real reservation final gate | Root production E2E asserts actual pre-release reservation rejected and no reservation rows | Observed normal sequential pre-release reservation gate; concurrency is separate |
| WMS-ELG-001 / TC-ELG-002 | real issue confirm final gate | No matching freeze/expiry scenario mapped | Not evidenced: blocked issue preserves ledger |
| WMS-ELG-001 / TC-ELG-003 | weighing final gate | 2026-10-04 P6 actual signed QA REJECTED successor prevents new weighing; wrong material/formula/actor/tolerance/calibration also block | Observed normal sequential supersession; simultaneous supersession/freeze workflow remains unverified |
| WMS-ELG-001 / TC-ELG-004 | charge final lock and eligibility | No formal freeze-command producer in current scope; root selected actual QA supersession/rejection final-boundary test | Production final-boundary QA rejection PASS: subsequent charge MATERIAL_NOT_ELIGIBLE, no new charge/debit. Sequential revocation probe, not concurrent FROZEN workflow proof |
| Incoming investigation / TC-QMS-002..003, IQ-OOS-04 | existing qms_deviation, signed decide/close | I2 automatic OPEN OOS and release block; I5 signed authorization, distinct retest, DECIDED still blocks, CLOSED invalidity selects reviewed PASS; original FAIL remains; lot only reaches QC_PASSED after closure | Partial: quota exhaustion, denied authorization, valid original FAIL closure remains blocked, independence-negative and non-OOS lineage cases |
| AUD/SIG / TC-AUD-001..002, TC-SIG-001..003, IQ-SIG-04 | platform audit/signature + immutable QMS envelopes | Q1/I1/I5 historical verification; Q4/A3/I4/I6 transactional rollback; precision unit tests exact lot quantity and persisted digest | Partial: permission/token expiration, altered content/reason, reused signature, revoked/skipped predecessor; identity/reauth boundary is mocked in native fixtures, not an end-to-end login test |
| CHG/TRACE / TC-CHG-001..003, TC-TRC-001 | actual production/charge/stock/query adapters and TraceService | Prior production E2E plus 2026-10-04 P6/P7/P8 and affected original E2E regression: weighing boundaries, charge audit/genealogy atomicity, replay once and other-org trace denial | Positive production/trace and listed dedicated negatives observed; simultaneous eligibility/consumption races and remaining formal matrix still partial |

## Independent QC producer

| Requirement / TC | Observed evidence | Coverage still needed |
|---|---|---|
| REQ-IN-QCS-001 / TC-IN-QCS-001 identity/schema | V014 successful migration; Q3 cross-org rejection | Root/code and business-revision uniqueness, FK/orphan rejection scenarios |
| REQ-IN-QCS-002 / TC-IN-QCS-002 item definition | Q1 NUMERIC/TEXT snapshots; Q2 retained tombstones | All bounds/exclusivity/precision/missing-value negative cases; QMS numeric unit tests are supporting rather than full producer evidence |
| REQ-IN-QCS-003 / TC-IN-QCS-003 lifecycle | Q1 draft→approved→retired and historical read; Q3 approved edit/delete refusal | Exhaustive illegal transitions/content-mutation and no-auto-retire scenarios |
| REQ-IN-QCS-004 / TC-IN-QCS-004 GxP | Q2 all participating authors excluded from approval; Q1 binding/replay and approval still verifies after retirement | Missing permissions, reason/content tamper and real expired-token boundary |
| REQ-IN-QCS-005 / TC-IN-QCS-005 atomicity | Q1 exact replay; Q3 stale version; Q4 injected audit rollback | Concurrent edit/approve and request/retire; header/body mismatch, changed-payload replay and injected signer failure |
| REQ-IN-QCS-006 / TC-IN-QCS-006 consumers | Q1 RETIRED selection rejects while historical snapshot unchanged; I1 real consumer | Wrong material/DRAFT selection and N+1 version preserving existing N task/report snapshots |
| REQ-IN-QCS-007 / TC-IN-QCS-007 UI/API | Current native service scenarios, frontend typecheck | Complete eight-operation HTTP envelopes/errors/permission tests and linked QC desktop/mobile UI evidence |

## Readiness interpretation

This matrix intentionally records gaps rather than converting 26 passing methods into blanket formal TC PASS. The approved scope includes completing required acceptance work. No listed gap is an approved waiver. Root must add exact passing scenario evidence, reuse applicable prior platform evidence with a recorded reference, or identify a real approved deferral before judging task readiness. Production/eBR positive-chain success alone will not prove every concurrency, exemption or negative gate above. This record does not change the task index or acceptance status.

## Supplemental test execution status

| New test / assertion | Intended formal coverage | Status |
|---|---|---|
| IncomingQualityAcceptanceIT.signedRevisionReportSupersessionAndRejectPreserveEveryPredecessor | TC-QMS-TST-002; TC-QMS-RPT-002; TC-QMS-MREL-003..004 | PASS in latest combined gate |
| IncomingQualityAcceptanceIT.otherPurposeRequiresSignedPlanAndEveryContainerBeforeCompletion | TC-QMS-SMP-001..003; IQ-SMP-04 | PASS: exact 5-required/4-recorded rejection, then fifth container completes |
| IncomingQualityAcceptanceIT.actualHttpRejectsMissingPermissionClosedDtoAndWrongOrganization | Incoming HTTP authorization, envelope, closed DTO and scoped lookup extensions | PASS in final targeted retry 1/1, 1.691 s after non-leaking 404 mapping fix |
| Existing I5 additional createReport rejection while competing retest unresolved | IQ-RPT-03 ambiguity | PASS in targeted rerun |

## Final verification and remaining boundaries

Root confirms **26 distinct native methods PASS** across minimal gates: QC4 + Attachment3 + Incoming6 + IncomingAcceptance3 + affectedWMS3 + Production2 + EbrRuntime5. Reruns are not counted as additional methods. Final root gate exit 0: EbrRuntimeIT **5/5, 13.82 s**; supplemental HTTP retry **1/1, 1.691 s**; SignatureTransactionServiceTest **8/8**; eBR rules/boundary units **4+5**. Exact-signature-ID same-transaction correlation and listener-failure rollback close the reported HIGH. IncomingProductionIT **2/2, 2.609 s** includes actual reservation boundaries, frozen policy preservation, formula-line entitlement isolation, real weighing/charge/return/reversal, QA rejection at final charge boundary and reverse-trace review/approval signatures. Sequential QA revocation does not prove concurrent FROZEN workflow.

UI evidence: **18 distinct API-mocked cases PASS** — incoming6 + production6 (18.2 s) + eBR6 (12.8 s). `npm run typecheck` ran actual `vue-tsc -b` and passed. These are UI/model checks, not real browser-to-native E2E.

Database: V001–V022 successful; root's final read-only test sentinel checks returned **0**. Executed migrations remain immutable. No clean-install or full-regression claim.

| Final bounded acceptance item | Current status | Remaining verification |
|---|---|---|
| Map §I A: QUARANTINE cannot issue | Actual createIssue rejects QUARANTINE and leaves zero issue writes | Final single-method native gate PASS |
| Map §I B: QC PASS without QA cannot issue | Actual createIssue rejects QC PASS without QA and leaves zero issue writes; confirmIssue rejects after actual QA revocation | Final single-method native gate PASS |
| Map §I E: frozen QC standard in actual reverse trace | v1.0.14 QC_SPECIFICATION_VERSION node and FROZEN_STANDARD relation implemented with exact originating frozen version | Final native gate PASS; actual graph node/edge assertions |

The previous design gap is resolved by v1.0.14 and the final verification passed. C/D original-fact and investigation scenarios already have evidence. Wider formal-TC negatives/concurrency in the tables above remain partial or unverified and are not waived by the bounded final gate. No blanket MES-task READY or ACCEPTED claim; MES_TASKS.md is unchanged by this document update.

Final targeted closure: root reports exit 0 for `IncomingProductionIT#realIncomingReleaseThroughSignedWeighChargeReturnAndReversePreservesTrace`, proving the three rows above; actual `npm run typecheck` passed again. This existing-method rerun does not increase the 26 distinct native count. Main six-record E2E and user A–E scenarios now have passing bounded evidence suitable for READY FOR ACCEPTANCE review. This is not blanket completion of all formal MES tasks: the detailed untested/partial cases remain visible above, and only explicit human approval can mark ACCEPTED. Root completed evidence archival and owns task-index updates.

Archived final evidence: [verification-summary.json](evidence/verification-summary.json). Final targeted production suite **1/1 PASS, 11.423 s** (method **2.747 s**); v1.0.14 manifest **98/98** verified; native history **22 successful / 0 failed**; incoming actor/material sentinels **0**. These results close the bounded A–E gate while preserving the wider formal-TC coverage limits above.
