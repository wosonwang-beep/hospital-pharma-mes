# Incoming Baseline Review — H / I / J controls, tests and traceability

Review date: 2026-10-03. Status: **mapping/review only; awaiting owner confirmation**. This document changes no frozen contract, task status, product code, migration, or database. No Maven, application test, browser test, or database command was run for this review. Historical PASS records are identified as historical evidence, never as execution of the six requested scenarios.

## Evidence and authority

`B/` below means `releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.9/`. Numbers after `:` are one-based source lines. To keep references readable, these aliases refer to the exact files:

| Alias | File under B/ |
| --- | --- |
| GMP | `11_GMP_AUDIT_ESIGNATURE_V1.0.9_FROZEN.md` |
| DOM | `04_DOMAIN_MODEL_DETAILED_V1.0.9_FROZEN.md` |
| DB | `05_DATABASE_DESIGN_V1.0.9_FROZEN.md` |
| STATE | `07_STATE_MACHINE_DETAILED_V1.0.9_FROZEN.md` |
| API | `08_API_DETAILED_V1.0.9_FROZEN.md` |
| FUNC | `09_FUNCTIONAL_DETAILED_V1.0.9_FROZEN.md` |
| CAT | `12_TEST_CASE_CATALOG_V1.0.9_FROZEN.csv` |
| DETAIL | `12_TEST_CASE_DETAILED_V1.0.9_FROZEN.md` |
| COVER | `12_TEST_COVERAGE_MATRIX_V1.0.9.csv` |
| ACCEPT | `12A_TEST_ACCEPTANCE_V1.0.9_FROZEN.md` |
| RTM | `13_REQUIREMENT_TRACEABILITY_MATRIX_V1.0.9_FROZEN.csv` |
| INT | `15_INTEGRATION_CONTRACT_MATRIX_V1.0.9_FROZEN.csv` |
| DEP | `15_TASK_DEPENDENCY_MATRIX_V1.0.9_FROZEN.csv` |
| MOD | `15_MODULE_DEPENDENCY_MIGRATION_INTEGRATION_CONTRACT_V1.0.9_FROZEN.md` |
| CARD008A / CARD011 / CARD012 | `tasks/MES-008A-R2.md` / `tasks/MES-011-R2.md` / `tasks/MES-012-R2.md` |

The frozen v1.0.9 documents and their approved amendments remain authoritative. The material-basic amendment supersedes inherited MaterialVersion language (GMP:35; ACCEPT:29; DETAIL:564). Stage appendices expressly preserve deferred full-task gates (GMP:48–52; ACCEPT:42–46; DETAIL:584–588). `MES_TASKS.md:149–156` records bounded 008A route/FK design authorization and separate unresolved 009/010 conflicts. Neither draft development contracts nor proposals authorize a new incoming model. `MES_TASKS.md` remains the only status index.

Classification: **MATCH** = the compared contracts/implementation agree within stated evidence; **MISSING** = required implementation/evidence absent; **UNAUTHORIZED IMPLEMENTATION** = implemented change outside authorization; **SEMANTIC MISMATCH** = an artifact or implementation changes the required meaning; **BASELINE CONFLICT** = formal artifacts disagree; **DESIGN GAP** = a required detail is not settled by the formal artifacts inspected. A match to a written test is not a test PASS.

## H — Audit and regulated history

| Control | Frozen evidence | Current evidence and classification |
| --- | --- | --- |
| No hard deletion or overwriting regulated history; append correction with before/after, reason, actor and time | GMP:3,14,18; DB:799–800,814; CARD008A:59–63 | **MATCH, platform capability only:** audit append uses mandatory existing transaction (`backend/mes-audit/src/main/java/com/hospital/mes/audit/application/AuditApplicationService.java:16–17`); V003 guards reject audit UPDATE/DELETE (`backend/mes-boot/src/main/resources/db/migration/V003__mes_001_platform_base.sql:141–147`). **MISSING:** incoming result/report/release revision and supersession producers. An append-only audit table alone does not make absent business aggregates immutable. |
| Actor/role/action/object, canonical old/new digests, reason, UTC milliseconds, transaction/request/source/idempotency correlation; secrets excluded | GMP:6 | Platform schema supports these fields (V003:1–30). WMS calls audit on lot receive and receipt confirmation (`backend/mes-wms/src/main/java/com/hospital/mes/wms/application/WmsService.java:115–118`). **MATCH** for audited receipt stage; **MISSING** for incoming submit/review/approval/retest/disposition events. |
| Audit all incoming record operations, including resampling/retest, correction, release/reject, exemption, freeze/unfreeze and supersession | GMP:14 | No incoming command services exist in QMS/QC/release modules. **MISSING**; future event names and object bindings must be mapped before implementation. Generic platform audit availability is not incoming coverage. |
| Shared Attachment, Comment, Revision and Lineage evidence accompanies six incoming records | GMP:14; CARD008A:38,42; DETAIL:548 | **MISSING** for incoming associations and retrieval. Current QMS module shells provide no record-level linkage. This review does not assert that every named shared infrastructure feature is already implemented merely because the GMP chapter requires its use. |
| Same local transaction for business write and audit | INT:21; CAT:55; DETAIL:429–435 | **MATCH** to platform transaction boundary and historical MES-001 acceptance only. **MISSING** incoming rollback proof. Each new incoming transition must assert both business and audit rollback, including signature/idempotency behavior. |
| Before/after reconstruction and transaction correlation limitations | GMP:3,6,14 | `backend/mes-masterdata/src/main/java/com/hospital/mes/masterdata/application/MasterMutation.java:33,36` stores digests, not original values, and creates a fresh UUID per audit call. A multi-event receipt transaction can therefore have different audit transaction IDs. **DESIGN GAP / traceability limitation**, not a proven violation of a frozen requirement that all events share one UUID: the formal chapter requires a transaction identifier and same-DB-transaction controls but does not expressly define that identifier's grouping algorithm. Digests prove integrity only when the corresponding old/new evidence remains retrievable; they do not reconstruct old values. Incoming revision/snapshot/history retention must supply that evidence. Do not add payload bodies to audit to work around this limitation: GMP:6 excludes them. |

## I — Electronic signatures

| Control | Frozen evidence | Current evidence and classification |
| --- | --- | --- |
| Server-built RFC 8785 envelope; SHA-256; object/version/evidence binding | GMP:8; DOM:119 | **MATCH, platform capability:** `backend/mes-audit/src/main/java/com/hospital/mes/audit/signature/Rfc8785SignatureCanonicalizer.java:19–40` builds canonical envelope, sorts/deduplicates evidence, rejects floating-point values and hashes. Business-specific completeness still belongs to each provider. |
| Exactly five-minute, single-use reauthentication bound to user/session/org/object/meaning/version, consumed on first attempt despite rollback | GMP:8; CAT:65–66 | **MATCH, static:** `backend/mes-security/src/main/java/com/hospital/mes/security/reauth/ReauthenticationService.java:23,45–48`; `backend/mes-audit/src/main/java/com/hospital/mes/audit/signature/SignatureApplicationService.java:13–17` consumes before the transactional signing service. This is not a newly executed timing/rollback test. |
| Provider validates organization, business version, permission and signability; signature inserted with audit in transaction | GMP:8,16; DOM:119 | `SignatureTransactionService.java:34–54` and `SignableObjectProviderRegistry.java:12–25` implement the platform path. Registry refuses missing providers. **MISSING incoming integration:** the only concrete production `implements SignableObjectProvider` found is `backend/mes-process/src/main/java/com/hospital/mes/process/application/ProcessSignableProvider.java:8–13` for `ProcessVersion`. No sampling/result/inspection/report/incoming-release providers exist. Fail-closed missing provider is preferable to claiming a signature exists. |
| Signature-required points: sampling completion, result confirmation/correction, inspection review, report approval, human QA release/reject and approved disposition | GMP:16; DETAIL:543–545,548 | **MISSING** incoming providers, canonical record/evidence selection, permission/meaning/state checks and end-to-end bindings at every point. A signature ID or boolean is insufficient. Existing ProcessVersion signing does not cover incoming records. |
| SYSTEM_RULE exemption decision uses technical actor, rule/version, qualification snapshots, deterministic decision digest and audit; no fake human signature | GMP:18; ACCEPT:12; CAT:79–80 | **MISSING** exemption decision producer. Current WMS receipts deliberately create QUARANTINE/BLOCKED lots, even for exempt policy snapshots (`WmsService.java:114–116`; `docs/acceptance/mes-008/ACCEPTANCE.md:8–11`). This is the accepted stage boundary, not unauthorized exemption behavior. |
| Controlled invalidation; new row on re-sign; immediately superseded invalidated signature | GMP:10 | **SEMANTIC MISMATCH, static finding:** `SignatureTransactionService.java:132–142` accepts explicit `revokedSignatureId` if it is INVALIDATED and has matching object/meaning; it does not check that it is the latest predecessor. Reproduction sequence: sign S1 → invalidate S1 → sign S2 linked to S1 → invalidate S2 → sign S3 explicitly naming S1. The validation accepts S1 although S2 is the immediately superseded invalidated signature. The omitted-ID path does query latest (`:144–148`). This finding was not tested or fixed in this review. A future targeted regression should reject the explicit stale predecessor. |
| Provider ownership in general integration matrix | RTM:44; INT:22–24; MOD:33 versus GMP:14–18; CARD008A:38 | **BASELINE CONFLICT / incomplete cross-reference:** older general platform rows name MES-007/013 consumers/providers but incoming amendment explicitly adds signature requirements to 008A. The amendment supplies the incoming obligation; it does not justify omitting incoming providers. Reconcile owner/consumer lists when publishing an authorized consolidated baseline. |

## J — Exact incoming TC / RTM mapping

All rows below are **NOT VERIFIED for incoming runtime in this review**. Except WMS receipt snapshot evidence explicitly noted, business implementation is absent. `COVER:62` assigns 17 tests to MES-008A. `CARD008A:53` names 20, including three tests whose catalog ownership is another task. Preserve both implementation ownership and cross-task acceptance obligations; do not silently remove the extra tests.

| Original TC | Requirement / RTM line | Frozen assertion and source | Owner / current coverage |
| --- | --- | --- | --- |
| TC-QMS-IN-001 | QMS-IN-001 / RTM:51 | Request DRAFT→SUBMITTED, receipt/lot association; CAT:82, DETAIL:541 | 008A; missing |
| TC-QMS-SMP-001 | QMS-SMP-001 / RTM:52 | Container-level detail→Sample lineage; CAT:83, DETAIL:542 | 008A; missing |
| TC-QMS-SMP-002 | QMS-SMP-001 / RTM:52 | 4 of required 5 containers blocks completion, remains IN_PROGRESS; CAT:84 | 008A; missing |
| TC-QMS-SMP-003 | QMS-SMP-001 / RTM:52 | TEST_SAMPLE and RETENTION_SAMPLE with labels/storage; CAT:85 | 008A; missing; does not enumerate all new requested sample purposes |
| TC-QMS-TST-001 | QMS-TST-001 / RTM:53 | Original execution fact and Revision1; CAT:86, DETAIL:543 | 008A; missing |
| TC-QMS-TST-002 | QMS-TST-001 / RTM:53 | Reason and signature for appended Revision2, Revision1 immutable; CAT:87 | 008A; missing; correction is not an approved retest instance |
| TC-QMS-TST-003 | QMS-TST-001 / RTM:53 | Required item without approved result blocks QC review; CAT:88 | 008A; missing |
| TC-QMS-RPT-001 | QMS-RPT-001 / RTM:54 | Report items reference final approved revisions; CAT:89, DETAIL:544 | 008A; missing |
| TC-QMS-RPT-002 | QMS-RPT-001 / RTM:54 | Approved report cannot be overwritten; controlled supersession; CAT:90 | 008A; missing |
| TC-QMS-MREL-001 | QMS-MREL-001 / RTM:55 | QC_PASSED cannot reserve, inventory BLOCKED; CAT:91, DETAIL:545 | 008A; missing |
| TC-QMS-MREL-002 | QMS-MREL-001 / RTM:55 | Approved report + all gates + signature; immutable decision and RELEASED/AVAILABLE atomic; CAT:92 | 008A; missing |
| TC-QMS-MREL-003 | QMS-MREL-001 / RTM:55 | Signed rejection, REJECTED decision, unusable inventory; CAT:93 | 008A; missing |
| TC-QMS-MREL-004 | QMS-MREL-001 / RTM:55 | Superseding decision, immutable previous decision; CAT:94 | 008A; missing |
| TC-WMS-006 | QMS-EXM-001 / RTM:50 | Valid exemption creates SYSTEM_RULE release and RELEASED/AVAILABLE without fictional QMS records; CAT:79 | 008A; deferred and missing |
| TC-WMS-007 | QMS-EXM-001 / RTM:50 | Invalid supplier/relationship blocks exemption, no decision; CAT:80 | 008A; deferred and missing |
| TC-WMS-008 | MD-MAT-002 / RTM:49 | Historical lot policy snapshot preserved; CAT:81, DETAIL:539 | Catalog owner 008, included CARD008A cross-task gate. Historical WMS stage evidence exists. **Inherited materialVersionId/new-version wording is superseded by material-basic amendment**; no business version may be reintroduced to make the old wording pass. |
| TC-ELG-001 | WMS-ELG-001 / RTM:56 | Unreleased stock cannot reserve, MATERIAL_NOT_ELIGIBLE; CAT:95 | 008A; missing real eligibility consumer integration |
| TC-ELG-002 | WMS-ELG-001 / RTM:56 | Frozen/expired stock cannot issue; no ledger write; CAT:96 | 008A; missing real eligibility consumer integration |
| TC-ELG-003 | WMS-ELG-001 / RTM:56 | Superseded nonrelease decision prevents weighing and fact creation; CAT:97 | Catalog owner 011; CARD008A cross-task gate, deferred |
| TC-ELG-004 | WMS-ELG-001 / RTM:56 | Reserve, concurrent freeze, final charge transaction rejects without charge/debit; CAT:98 | Catalog owner 011; CARD008A cross-task gate, deferred |

`DETAIL:548` additionally requires DB state, API, `allowedActions`, AuditEvent, signature binding where applicable, optimistic lock and rollback assertions for every incoming case. Exemption must assert zero request/sampling/sample/inspection/result/report records. The detailed appendix groups incoming tests in prose (`DETAIL:537–548`); it is not evidence that each executable test exists.

### Related controls and downstream original TCs

| Original TC | RTM / catalog / detail evidence | Relevance and current limit |
| --- | --- | --- |
| TC-AUD-001 / TC-AUD-002 | RTM:43; CAT:55,64; DETAIL:429–435 | Same transaction; authorized filter/DTO/DB guard and no secrets. Historical MES-001 acceptance supports platform only; add actual incoming object evidence. |
| TC-SIG-001 / TC-SIG-002 / TC-SIG-003 | RTM:44; CAT:56,65–66 | Digest verification; token binding/TTL/single use; non-restoration after rollback/JCS. Incoming signable providers and predecessor regression remain unverified. |
| TC-WMS-001 | CAT:26; COVER:45 | Quarantine reservation/issue returns MATERIAL_NOT_RELEASED. New shared eligibility cases use MATERIAL_NOT_ELIGIBLE (CAT:95). **BASELINE CONFLICT** in exact error-code expectations for overlapping unreleased reservation conditions; freeze one compatible contract per endpoint before asserting both. |
| TC-WMS-002 / TC-WMS-003 | CAT:27–28; COVER:46 | Ledger reconstruction/immutability and idempotent receive. Historical receipt-stage tests exist; no implication that QMS release or eligibility works. |
| TC-WMS-004 / TC-WMS-005 | CAT:29–30; COVER:47–48 | FEFO; issue does not become actual charge. Deferred real 008A/009 dependencies; full WMS gate remains required. |
| TC-WGH-001 / TC-WGH-002 / TC-WGH-003 | RTM:33; CAT:38–40; DETAIL:293–315 | Released material/BOM/qualified scale normal weighing; wrong material; tolerance. MES-011 not implemented. |
| TC-CHG-001 / TC-CHG-002 / TC-CHG-003 | RTM:34; CAT:41–43; DETAIL:317–339 | Atomic Charge+QuantityEvent+Ledger+Genealogy+Audit; genealogy failure rolls back all; repeated command yields one charge/consume. MES-011 not implemented. |
| TC-TRC-001 | RTM:42; CAT:44; DETAIL:341–347 | Existing Charge/Genealogy support lot forward/backward consistency. Original case does not explicitly traverse the complete incoming inspection/report/release chain from a MaterialCharge. |
| TC-QMS-001 | RTM:39; CAT:48; DETAIL:373–379 | Production-quality result revision retains original. Reuse 008A revision foundation; not a replacement for incoming-specific tests. |
| TC-QMS-002 | RTM:40; CAT:49; DETAIL:381–387 | Critical OPEN deviation blocks QA release. A deviation example is not complete incoming OOS approval/disposition coverage. |
| TC-QMS-003 | RTM:40; CAT:50; DETAIL:389–395 | OOS original result cannot simply become PASS; requires rejection or approved flow and retained OOS record. Owned by MES-012; not implemented. |
| TC-QMS-004 | RTM:38; COVER:41,60 | Production IPC gate. Future MES-012; do not implement it early to fill incoming scope. |
| TC-REL-001 / TC-REL-002 / TC-REL-003 / TC-REL-004 | RTM:41; CAT:51–54; DETAIL:397–427 | Finished-product release, QC not release, concurrent gate and immutable decision. MES-013; cannot stand in for incoming TC-QMS-MREL cases. |

Cross-document coverage limits:

- **MATCH with ownership qualification:** CARD008A:53 has 20 IDs; COVER:62 has 17. The difference is TC-WMS-008 (008) and TC-ELG-003/004 (011), not three mysteriously satisfied tests. Full acceptance requires explicit integration/regression evidence when their consumers exist.
- **BASELINE CONFLICT / incomplete test lists:** CARD011:73 lists the original seven cases, while COVER:59 lists nine including ELG-003/004; CARD011:92 does require that eligibility behavior. CARD012:73 lists six original tests, while COVER:60 lists eight including BAL-004 and QMS-004. Consolidated task gates must preserve the additional normative requirements.
- **BASELINE CONFLICT / stale inherited reference, resolved in meaning by amendment:** RTM:49, CAT:78,81, INT:27 and some consumed-contract rows still describe material business versions. ACCEPT:29 and DETAIL:564 require basic material ID plus historical snapshot, not material versioning. Report remaining textual inconsistency without resurrecting the old model.

## The user's six acceptance scenarios

| Scenario | Existing original TC mapping | Exact coverage limit / classification |
| --- | --- | --- |
| Main E2E: receipt → lot QUARANTINE → request → QC receive → sampling → Sample → every required test → report → QA release → RELEASED → production issue allowed | Receipt TC-WMS-003/008; request TC-QMS-IN-001; sampling TC-QMS-SMP-001/003; result TC-QMS-TST-001/003; report TC-QMS-RPT-001; release TC-QMS-MREL-002; shared gate TC-ELG-001/002; issue behavior TC-WMS-004/005 | **MATCH** of most business stages; **MISSING** implementation and real joined E2E execution. No single original TC expressly asserts this complete positive chain. QC receipt/handoff and positive post-release issue must be explicit steps, not inferred from negative eligibility tests. |
| A: QUARANTINE prevents production issue | TC-WMS-001, TC-ELG-001/002; STATE:67–69; DOM:156 | **MATCH** invariant; **MISSING** real gate. An unconditional DEPENDENCY_NOT_READY response proves deferred functionality, not a quarantine-specific eligibility decision. Resolve overlapping exact error-code expectations above. |
| B: QC PASS without QA release prevents issue | TC-QMS-MREL-001; TC-ELG-001/002; ACCEPT:10 | **MATCH** invariant. Original MREL-001 explicitly tests reservation; extend joined scenario to issue as requested. No QC/QA producer currently exists. |
| C: unresolved OOS prevents release | TC-QMS-003 plus all-gates TC-QMS-MREL-002; related deviation gate TC-QMS-002; STATE:69; FUNC:192 | **MATCH** control intent; **DESIGN GAP** for the exact incoming OOS investigation/approved disposition producer, relationship and executable case. QMS-002 tests critical deviation, not every OOS condition. Do not mark incoming OOS covered using only that test. |
| D: original FAIL cannot be overwritten by retest | TC-QMS-TST-001/002; TC-QMS-003; GMP:3,14,18; STATE:40 | **MATCH** original-fact preservation; **DESIGN GAP** in exact approved-new-TestInstance retest contract and explicit original-FAIL/retest relationship test. Appending correction Revision2 is not proof that retest created a separate approved instance. |
| E: RELEASED material traced from MaterialCharge back through complete incoming chain | TC-CHG-001/002/003 and TC-TRC-001, plus TC-QMS-SMP-001, TC-QMS-TST-001/002, TC-QMS-RPT-001 and TC-QMS-MREL-002/004 | **MATCH** component lineage requirements; **MISSING** actual Charge/Genealogy and incoming producers. **DESIGN GAP** in a single formal test that explicitly traverses charge→lot→receipt item→request→sampling/detail→sample→test execution/revisions→report approved revisions→effective/historical decision and audits/signatures. Current generic TRC-001 is narrower. |

No scenario has a new PASS result. No fake request, Sample, result, report, decision or MaterialCharge may be manufactured and called production evidence. API-mocked browser tests and unit fixtures can validate components, but cannot satisfy the real joined acceptance chain.

### New requested model details versus original tests

| User requirement | Existing coverage | Review disposition |
| --- | --- | --- |
| Frozen `qc_specification_version_id`; result lower/upper/unit/method-version snapshots | DOM:141 requires frozen specification/method/criteria; TST-001 retains execution facts; RPT-001 binds approved revisions | **DESIGN GAP** in exact new field/schema/association and dedicated change-after-freeze assertions. Need authoritative mapping to the original InspectionTask/Item/TestExecution/Revision design, not adoption of a draft DTO. |
| PASS / FAIL / INVALID | Original tests address QC gates and OOS but do not explicitly exercise every result conclusion | **DESIGN GAP** in complete enum and INVALID handling tests. A failed/invalid execution cannot silently be treated as an approved PASS. |
| One request → many sampling records → many samples, including inspection/retention/retest/approved other | SMP-001/003 cover container detail and TEST_SAMPLE/RETENTION_SAMPLE | **DESIGN GAP** for full multiplicity and purpose mappings plus approval constraints. Existing Sample linkage evidence does not settle every requested cardinality. |
| Sample → TestInstance → TestResult; approved retest creates new instance | DOM:141–142 and DB:799 use InspectionItem/TestExecution/TestResultRevision | **DESIGN GAP / potential baseline change requiring authority:** define equivalence or approved additions before implementation. Never rename TestExecution or make Revision2 a retest implicitly. Add original FAIL immutability, approval-before-retest, new identity and full lineage assertions. |
| Report aggregates multiple instances per request | RPT-001 binds each report item to approved Revision; DOM:143 | **DESIGN GAP** for complete multi-instance aggregation, original FAIL visibility and approved selection/disposition semantics. Existing approved-revision pointer alone does not prove request-wide completeness. |

These are mapping findings, not approved schema changes or newly assigned TC identifiers. Extend the frozen Test/RTM/DB/Domain/API/State/UI/Integration artifacts together only after authority confirms the exact model.

## OOS / Deviation ownership and MaterialCharge reverse trace dependencies

| Producer / consumed contract | Formal evidence | Implementation and dependency conclusion |
| --- | --- | --- |
| MES-008 receipt/lot and inventory facts → incoming QMS | INT:28; DEP:9–10 | WMS receipt stage exists. `WmsService.java:114–118` freezes material policy and creates receipt-linked QUARANTINE/BLOCKED lot, RECEIVE ledger and audit. Actual incoming event/consumer chain remains missing. |
| MES-008A incoming request, sampling, results/revisions, report, decision and eligibility → 008/011/012/013 | CARD008A:24,38–44; DEP:10; INT:29–31 | QMS, QC and release modules currently contain only POM/package declarations. No effective ReleaseDecision or real MaterialEligibilityService producer exists. All four consumption boundaries must ultimately use the same decision and final transaction checks. |
| Incoming investigation foundation versus production IPC/OOS/OOT/Deviation/CAPA | CARD012:7,31,43–46,92; STATE:40,69; INT:17 | MES-012 owns production-process investigation capabilities and consumes incoming foundations; CARD012:92 refers to reused incoming result/revision and investigation foundations. **DESIGN GAP**: incoming OOS source/status/disposition integration must be made concrete within authorized 008A scope. Absence can fail closed; it cannot fabricate a resolved OOS, silently move all MES-012 scope into 008A, or bypass the release gate. |
| MES-009/010 batch and operation execution → MES-011 | DEP:13; INT:8; CARD011:31,43 | Actual production/execution context is not available in current product module shells. Necessary before real weigh/charge acceptance, although review/mapping can proceed. |
| MES-011 actual MaterialCharge → quantity, inventory, genealogy, eBR and audit | API:28; INT:11–14,33; CARD011:46,92 | No actual MaterialCharge, weighing or genealogy implementation found. Must be one local transaction; issue/reservation is not an actual charge. Late freeze/superseding nonrelease must leave no charge/debit. |
| MES-011 traceability query → full incoming evidence | RTM:42,51–56; INT:14,29–31; GMP:14 | Missing both ends of the join. TC-TRC-001 requires bidirectional lot consistency; scenario E additionally needs exact receipt/request/sampling/result/report/decision lineage. Must preserve historical revisions and superseded decisions, not only current status. |
| MES-012 critical deviations / OOS status → QA gates; MES-013 finished-product release | INT:16–20; DEP:14–15 | Unimplemented future modules. Distinguish incoming QA material decision from finished-product release. Unavailable required evidence must block, never count as gate PASS. No authorization to implement MES-011/012/013 in this review. |

Inventory evidence: scoped `rg --files` of `backend/mes-qms`, `backend/mes-qc`, `backend/mes-release`, `backend/mes-traceability`, `backend/mes-production`, `backend/mes-execution` found only each module's `pom.xml` and `src/main/java/com/hospital/mes/<module>/package-info.java`. Scoped Java search found no production Inspection/Sample/ReleaseDecision/Eligibility/Deviation/OOS/Charge services. This is an implementation inventory, not a database-history inspection. **No UNAUTHORIZED IMPLEMENTATION was found within these incoming/downstream scopes**; their current deficiency is missing functionality, not premature code. Development drafts are not implementation.

## Existing verification evidence and review disposition

`docs/acceptance/MES-001-R2.md:14,17,26–30,43–44,150` records historical accepted platform evidence against its then-current baseline; it expressly does not authorize downstream implementation. `docs/acceptance/MES-002-R2.md:25,54,101` records IAM audit/idempotency and acceptance, not incoming workflow verification. `docs/architecture/module-boundaries.md:3–7` preserves the modular monolith, only mes-boot executable, acyclic dependencies, domain events/query services and production independence from workflow runtime. `docs/api/README.md` is reserved; `docs/api/foundation-api.md:3,18` describes a historical foundation shell and cannot override newer released API contracts.

`docs/acceptance/mes-008/ACCEPTANCE.md:3,15–21,32–34` explicitly limits evidence to receipt/ledger stage: historical WmsRules/WmsIT and related tests; current-stage TC-WMS-002/003/008 only; browser checks use mocked APIs. It defers TC-WMS-001/004/005/006/007 and TC-ELG-001/002 until real dependencies. Its record that V013 was applied is historical acceptance evidence; no migration/history query was run here.

Review result: six scenarios have an identifiable original requirement/test foundation, but none is established as a completed real incoming acceptance scenario. Main blockers to claiming acceptance are missing incoming producers/providers, missing real eligibility consumers and future Charge/Genealogy, plus unresolved exact new TestInstance/retest/specification/report semantics. The signature predecessor mismatch is a separate concrete static control finding. Audit value reconstruction and transaction-ID grouping are explicit limitations, not invented frozen violations. Cross-document test ownership/enum/error-code inconsistencies require a controlled consolidated mapping before acceptance. Existing tasks and readiness remain unchanged; no migration or product test was performed.
