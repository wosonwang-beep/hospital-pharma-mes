# Incoming quality acceptance evidence

2026-10-03 · Evidence status: **26 distinct native methods PASS; final scope-readiness assessment below; not full MES-task acceptance**.

## Authority and boundary

The authoritative contract is [FINAL BASELINE COMPLETE v1.0.14](../../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14/), including the incoming QC specification, chain, attachment and production contracts, TC catalog and RTM. The user approved the bounded DCP-INCOMING-QUALITY-GAPS-001 scope and full acceptance work on 2026-10-03. The [implementation map](../../development/INCOMING_QUALITY_IMPLEMENTATION_MAP.md) records the initial six-record review and gaps; its older baseline/status statements are historical, not the current implementation status. This evidence record neither changes design nor updates MES_TASKS.md. Only explicit human approval may mark ACCEPTED.

Implemented chain: actual receipt/lot → approved independent QC specification version → request → sampling task/detail and separate samples → inspection task/item → execution and append-only result revisions → request-root report → independent material QA decision → final-boundary material eligibility. Incoming OOS, signed retest authorization and selected effective result use the existing investigation facts. The original FAIL remains immutable; INVALID is a separately derived investigation disposition. Production/charge/trace and eBR runtime have passing integrated evidence recorded below. Finished-product QA, production IPC/CAPA/material-balance scope is not implicitly accepted.

## Recorded native evidence

The root coordinator confirmed **26 distinct passing native test methods** across initial runs and targeted retries. A retry is not an additional distinct test. This is not a claim that every formal TC or every assertion in a TC has been exercised; [RTM.md](RTM.md) identifies partial coverage and unverified cases.

| Suite | Distinct passing methods | Evidence boundary |
|---|---:|---|
| QcSpecificationIT | 4 | Actual draft/edit/tombstones, author independence, signing/retirement/history, scoped reads, immutable SQL guards, CAS/replay and audit rollback |
| AttachmentIT | 3 | Actual byte/hash retention, upload replay/conflict, scoped source/link access, stale/duplicate linking, approved receipt preservation, immutable SQL guards and audit rollback |
| IncomingQualityIT | 6 | Actual happy chain and signed QA, immutable original FAIL/OOS, missing-report/cross-org gates, result audit rollback, signed approved retest, owner lot audit rollback |
| IncomingProductionIT | 2 | Latest combined gate PASS 2/2, 2.609 s: real A/B reservation gating, formula-line entitlement isolation, frozen policy, independent weighing, real charge/return/reversal, QA rejection at final charge boundary, eBR completion and full reverse trace/signatures. Earlier positive-chain run 11.45 s. |
| WmsIT affected regression | 3 | Root reports 3 affected regression methods PASS, including controlled direct-receive exemption; exact command selectors to be attached. |
| EbrRuntimeIT | 5 | Final signature gate PASS 5/5, 13.82 s, including same-transaction exact-signature-ID correlation and evidence-listener rollback. Earlier gate 4/4, 13.14 s. |
| IncomingQualityAcceptanceIT | 3 | Two combined lifecycle methods passed; final HTTP retry PASS 1/1, 1.691 s after non-leaking 404 mapping fix. Three distinct methods passed across gates. |

Passing method inventory:

- **QC:** `signedLifecycleSnapshotsRetirementAndReplay`; `draftTombstonesAndAllAuthorsAreRetained`; `scopeLocksAndSignedDefinitionsFailClosed`; `approvalAuditFailureRollsBackAllEvidence`.
- **Attachment:** `exactBytesReplayAndApprovedReceiptAppend`; `organizationSourceAccessAndStaleLinkFailClosed`; `auditFailureRollsBackAttachmentAndIdempotency`.
- **Incoming:** `completeChainOnlyIndependentQaMakesLotEligibleAndHistoryVerifies`; `quarantineAndOpenOosBlockReleaseAndFailCannotBeOverwritten`; `missingRequiredResultAndCrossOrganizationCannotProduceReport`; `resultAuditFailureRollsBackSignatureResultOosAndTaskVersion`; `approvedRetestCreatesIndependentExecutionAndNeverChangesOriginalFail`; `lotOwnerAuditFailureRollsBackCoupledRequestAndLot`.

Sources are the current test files under `backend/mes-boot/src/test/java/com/hospital/mes/{qc/integration,wms/integration,incoming,production}/` and coordinator execution records. Local diagnostic logs include `%TEMP%/incoming-native-gate.log`, `incoming-native-startup.log`, `incoming-native-chain.log` and `incoming-native-recheck.log`; these contain initial failures as well as later attempts and must not be described as one all-green run. The final machine-readable evidence is archived in [verification-summary.json](evidence/verification-summary.json); final read-only history/residue outcomes are recorded below.

Two incoming scenarios initially failed when the MaterialLot quantity was emitted as a floating-point JSON node in signable evidence. The fix preserves the exact BigDecimal as a decimal string in the local lot evidence projection; the global canonicalizer still rejects floating-point nodes. Their targeted retry passed 2/2, giving 6/6 distinct incoming methods across the runs. An added unit test proves full decimal precision and unchanged signature digest after envelope serialization/reload. No executed migration was edited to fix these failures.

## Migration and persistent database safety

The root coordinator confirmed successful native application of **V014–V022** to the existing `hospital_pharma_mes_dev`, following successful history through V013. They are now executed, immutable history. This record makes no clean-install/hosted-CI claim.

| Physical migration | Produced facts |
|---|---|
| V014 | Independent QC specification/version/item |
| V015 | Immutable attachment bytes and receipt associations |
| V016 | Incoming request, sampling, sample, testing/revisions, report, investigation and release |
| V017 | Actual production order/batch/execution-unit and deferred MainBatch FKs |
| V018 | Actual eBR runtime snapshots/forms/revisions/review evidence |
| V019 | Operation execution/equipment/parameter facts and deferred operation FKs |
| V020 | Weighing, charge, quantity-event and genealogy |
| V021 | Append-only issue-return entitlement evidence |
| V022 | eBR review collation guard; root confirms applied in latest combined gate |

Static preflight compared 37 new tables with 37 persistence entities and checked FK target creation order, duplicate FK/trigger names and trigger NEW/OLD columns. No unmatched field/target was found. Root read-only preflight reported no legacy WMS MainBatch references blocking the new FKs. The checked native suites use uniquely named test-owned users/materials/receipts and transaction rollback; production inherits IncomingQualityFixture. Negative UPDATE/DELETE assertions target only newly created test IDs. No reset, alternate database, seed mutation, repair, commit escape or REQUIRES_NEW fixture path was found. Final root read-only verification confirms V001–V022 successful and test sentinels **0**. No test-owned residue was detected by those sentinel checks.

## Other verification and review

- QMS unit gate: IncomingRulesTest **4/4** and IncomingEvidenceTest **1/1**, zero failures/errors. The precision regression report records 0.252 s; rules 0.012 s. Command: `mvn -q -pl backend/mes-qms -am test '-Dtest=IncomingEvidenceTest,IncomingRulesTest' '-Dsurefire.failIfNoSpecifiedTests=false'`.
- Frontend: `npm run typecheck` executed the actual `vue-tsc -b` build and passed; focused `incomingModel.test.ts` **5/5** passed (870 ms). These prove typed payload and model behavior, not native HTTP permission coverage.
- Browser verification: **18 distinct API-mocked cases PASS**: incoming 6, production 6 (18.2 s), eBR runtime 6 (12.8 s). This verifies UI behavior, not real browser-to-native E2E. Signature-modal width and Chinese evidence labels were corrected after screenshot review.
- Material fixes include exact signed-envelope persistence, original FAIL preservation, approved retest lineage/selection, request-level passing-state aggregation, lot freeze preservation, controlled release supersession version advancement, scoped deviation references and atomic owner lot audit. Native assertions cover only the explicitly mapped scenarios.
- Final incremental static review found trace reading `signatureId` instead of actual `reviewSignatureId`/`approvalSignatureId`. Root reports both fields corrected and a production-native assertion added. Root subsequently reports the production native scenario passed with both signature-edge assertions; this finding now has passing regression evidence.

## Remaining acceptance work

The passing methods recorded above are useful evidence, but do not satisfy every required case in the v1.0.14 contract. Controlled positive exemption now has affected WmsIT evidence. Reject/supersession, signed Revision2, OTHER_APPROVED and exact 5-required/4-recorded completeness, report supersession and unresolved-retest ambiguity now have passing native evidence. Production final-boundary QA supersession/rejection also passed. HTTP wrong-org status mapping now passed its final targeted retry. The approved current scope has no formal freeze-command producer: no synthetic SQL freeze workflow or new API is introduced; FROZEN workflow/concurrency coverage remains an explicit boundary, not an inferred PASS. Existing platform tests may be reused only after their exact assertions and applicable run are linked. The final single-method native gate passed for A/B actual-Issue rejection and E exact frozen-standard trace. Wider formal-TC coverage limits remain explicit in RTM.md. No formal TC is waived by this document.

Coordinator completion checklist: retain the final targeted A/B/standard-trace gate reports and hashes; reconcile outstanding mandatory TCs before any wider task-readiness claim. Then assess readiness against the frozen acceptance criteria without setting ACCEPTED automatically.

## Supplemental gates and resolved review

Root reports `IncomingQualityAcceptanceIT.signedRevisionReportSupersessionAndRejectPreserveEveryPredecessor` and `.otherPurposeRequiresSignedPlanAndEveryContainerBeforeCompletion` PASS. The latter uses a real five-package receipt/request, rejects completion at four distinct sampled containers and succeeds after the fifth, with signed OTHER_APPROVED plan and sample-label lineage. The existing approved-retest method also passed its new unresolved-report-ambiguity assertion on targeted rerun.

`actualHttpRejectsMissingPermissionClosedDtoAndWrongOrganization` initially returned 500 instead of expected 404 for the final cross-org lookup. Root added generic non-leaking NoSuchElementException-to-404 handling. Final targeted retry PASS 1/1 (1.691 s), so this method is now counted once as PASS.

Passing production methods: `sameMaterialDifferentFormulaLineCannotBorrowReservationEntitlement`; `realIncomingReleaseThroughSignedWeighChargeReturnAndReversePreservesTrace`. They include actual pre-QA reservation blocks, same-material/different-formula entitlement isolation and a real QA REJECTED successor making subsequent charge fail MATERIAL_NOT_ELIGIBLE with unchanged charge count and on-hand quantity. This is sequential final-boundary revocation evidence, not a concurrent FROZEN workflow test.

Passing eBR methods: `blockRetainsSqlEvidenceAnd422ReplayWithoutMarkingTransactionRollbackOnly`; `signedReviewsRemainVerifiableAcrossStatesAndCorrectionInvalidatesDependencyClosure`; `draftChangesInvalidateAlreadySubmittedCrossFormDependency`; `historicalAuthorCannotReviewAndImmutableEvidenceCannotBeOverwritten`. These prove retained rule evidence/422 replay, signed review/correction dependency invalidation, historical-author independence and immutable evidence.

A later HIGH finding identified eBR signature-policy correlation by requestId rather than exact signature identity. Root replaced it with a same-transaction SignatureAppliedEvent carrying the exact signature ID; the final independent gate passed EbrRuntimeIT 5/5 (13.82 s), including `roleEvidenceListenerFailureRollsBackSignatureAndIdempotency`. Platform SignatureTransactionServiceTest 8/8 and EbrRuntimeRules/Boundary 4+5 unit tests also passed. This HIGH now has closure evidence. Outstanding scope/RTM boundaries below still limit readiness. No task status changes here.

## Final verified counts and scope readiness

Root final-gate exit code is 0. **26 distinct native methods PASS across minimal gates:** QC 4 + attachments 3 + incoming chain 6 + supplemental incoming acceptance 3 + affected WMS 3 + production 2 + eBR runtime 5. Counts exclude reruns as additional cases. Test evidence does not automatically imply full frozen-TC coverage. Root confirms V001–V022 successful and final read-only test sentinels 0; the verification summary is archived.

| Requested scenario from implementation map §I | Current evidence | Readiness distinction |
|---|---|---|
| Main six-record E2E and production use | Actual receipt→QC standard/request→sampling/sample→result/report→QA release→reserve/issue/weigh/charge and trace passed | Backend integrated positive chain demonstrated; browser workflows use mocked APIs, not a real browser-to-native end-to-end run |
| A QUARANTINE cannot issue | Production native asserts reservation rejected before release with no reservation rows | Final native gate PASS: actual createIssue rejected in QUARANTINE with zero issue writes |
| B QC PASS without QA cannot issue | Production callback asserts reservation rejected before QA; direct eligibility false | Final native gate PASS: actual createIssue rejected before QA with zero issue writes; confirmIssue also rejects after real QA revocation |
| C Open OOS blocks release | Native OPEN/DECIDED investigation blocks release | Demonstrated |
| D Retest never overwrites original FAIL | Signed authorized separate execution and selected PASS; immutable original FAIL and signature retained | Demonstrated |
| E Actual charge reverse trace of complete incoming chain | Actual charge/genealogy, receipt, request, sampling, samples, results, report/release and signatures demonstrated | Final native gate PASS: exact frozen QC_SPECIFICATION_VERSION node and FROZEN_STANDARD relation match the originating request version |

Recommendation: the demonstrated six-record positive chain and C/D controls are reviewable for bounded acceptance. All requested A–E scenarios now have passing scoped native evidence; the bounded six-record incoming-quality chain is supported for READY FOR ACCEPTANCE review, with the archived verification summary linked below. This does not itself record human acceptance. Full MES-007/008/008A/009/010/011/012 task readiness is a separate judgment against each task's mandatory frozen cases; remaining RTM negatives/concurrency and out-of-scope producers prevent blanket readiness. Do not mark ACCEPTED without explicit human approval.

Final closure: root reports exit **0** for `IncomingProductionIT#realIncomingReleaseThroughSignedWeighChargeReturnAndReversePreservesTrace` after adding actual A/B createIssue rejection and zero-write assertions, post-QA-revocation confirmIssue rejection, and exact frozen-standard node/edge assertions. `npm run typecheck` also passed again. This is a rerun of an existing method: the distinct native total remains **26**, not 27. Root completed evidence archival and the task-index/plan ledger.

Archived final evidence: [verification-summary.json](evidence/verification-summary.json). Final targeted production suite **1/1 PASS, 11.423 s** (method **2.747 s**); v1.0.14 manifest **98/98** verified; native history **22 successful / 0 failed**; incoming actor/material sentinels **0**. These results close the bounded A–E gate while preserving the wider formal-TC coverage limits above.


## Human acceptance — 2026-10-03

User explicitly replied “验收” to the final bounded delivery report. The six-record incoming-quality chain and requested A–E scenarios are accepted, including real production use and frozen-standard reverse trace. This human decision is recorded in MES_TASKS.md, the sole task-status index. The broader task RTM coverage limitations remain applicable. Existing verification evidence is retained; no test rerun or schema change accompanies this acceptance record.


## Approved native concurrency increment — 2026-10-04

The latest [closeout](CLOSEOUT-2026-10-04.md) and [native concurrency summary](evidence/native-concurrency-summary-2026-10-04.json) supersede earlier pending fixture authorization and order/final-QA-supersession concurrency limitations.11 native concurrency cases,2 affected production regressions and3 rules passed; exact SubBatch release/QA route/action absence then passed in one scoped retry. MES-009 is READY FOR ACCEPTANCE; MES-007 remains READY. Wider MES-008/008A/010/011 mandatory matrices and actual IPC/clearance integrations remain IN PROGRESS, without blanket acceptance.

No new migration; V001–V023 validated.32 intentionally retained test lots are REJECTED/BLOCKED; earlier zero-residue checks describe earlier rollback gates only. RED-trial draft orders69/70 reconciled through audited commands without deleting history. Maintenance reconciliation is separately opt-in via mes.reconcile-retained-red-trials=true; subsequent evidence uses CREATE_NEW unique filenames. No CRITICAL/HIGH remains in scoped production increment review; no full regression/commit/push/reset/repair.


## Second closeout increment — 2026-10-04

10 new native cases PASS. Exact method mapping, controlled-clock limits, failure-only retry and stopped frozen contract/producer portions: [CLOSEOUT-2026-10-04-R2.md](CLOSEOUT-2026-10-04-R2.md). Existing broad acceptance/readiness limitations are retained except the explicitly closed assertions. No product/UI/migration change or new retained fixture; V001–V023 validated. Scoped review no concrete CRITICAL/HIGH. MES-007 ACCEPTED, MES-009 READY, MES-008/008A/010/011 IN PROGRESS in MES_TASKS.md.


## Third closeout increment — 2026-10-04

7 new native methods PASS; exact formal mapping and limits in [CLOSEOUT-2026-10-04-R3.md](CLOSEOUT-2026-10-04-R3.md). No product/UI/schema changes; V001–V023 validated.35 intentionally retained native trial lots all REJECTED/BLOCKED, including historical failed trials. Scoped review no concrete CRITICAL/HIGH. Current task statuses remain solely in MES_TASKS.md; this increment is not full-task acceptance.
