# INCOMING QUALITY IMPLEMENTATION MAP — A/B/C/D data review

2026-10-03. Read-only baseline review. No product code, migration, baseline, task status or database changed; no tests executed. This report records evidence and gaps, not an implementation contract or proposed design. The earlier `mes008a-contract.md/.json` are unapproved candidate material and must not be implemented as authority.

## Evidence and classification

Authority is `docs/PROJECT_BASELINE.md:9–19`: FINAL BASELINE COMPLETE v1.0.9. Abbreviations below refer only to exact files under `releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.9/`:

- PRD = `01_PRD_V1.0.9_FROZEN.md`.
- Domain = `04_DOMAIN_MODEL_DETAILED_V1.0.9_FROZEN.md`.
- DB = `05_DATABASE_DESIGN_V1.0.9_FROZEN.md`.
- State = `07_STATE_MACHINE_DETAILED_V1.0.9_FROZEN.md`.
- Functional = `09_FUNCTIONAL_DETAILED_V1.0.9_FROZEN.md`.
- OpenAPI = `08_OPENAPI_FULL_V1.0.9_FROZEN.yaml` (formal result/report schemas cross-checked after API/UI review).
- GxP = `11_GMP_AUDIT_ESIGNATURE_V1.0.9_FROZEN.md` (consulted for candidate identity/signature audit only; H is reviewed separately).
- Incoming-DCP = `00_DESIGN_CHANGE_PROPOSAL_DCP-MES-002-R2-001_APPROVED.md`.
- Material-DCP = `00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md`.
- WMS-stage = `00_MES008_STAGE_CONTRACT_V1.0.9.md`.

MATCH means the requirement is explicitly supported. MISSING means the named field/entity/enum is absent. DESIGN GAP means intent exists but physical/cardinality/state details are not frozen. SEMANTIC MISMATCH means similar terms cannot be assumed equivalent. BASELINE CONFLICT is reserved for incompatible formal contracts or an explicit new requirement incompatible with them; errors in an unapproved candidate are not formal baseline conflicts or product implementation findings.

## A. Six records and formal requirements

| Record | Formal requirement / domain / functional evidence | Finding |
|---|---|---|
| 原辅料收货记录 | PRD:515–519; Domain:137–138; Functional:248; WMS-stage:7–11 | MATCH. Receipt/header/items capture physical facts, material/policy snapshots and ledger. Inspection-required and exempt lots start QUARANTINE/BLOCKED. MaterialVersion wording in older prose is superseded by Material-DCP:7–9,16 and DB:840–842. |
| 请验单 | PRD:523; Domain:139; Functional:249 | MATCH for separate WMS→QMS request, receipt/lot, standard/specification versions, quantity/package/date/priority/requester. Request type is exactly INITIAL/RETEST/SUPPLEMENTARY/INVESTIGATION. DESIGN GAP for authoritative standard-version entity and physical FK. |
| 取样记录 | PRD:524; Domain:140; Functional:250 | MATCH for SamplingTask→SamplingDetail→Sample and each container/point, plan/SOP, sampler, quantity/tool/conditions/reseal and labels. “SamplingRecord” is not the formal entity name. One task with multiple details and physical samples is supported conceptually; precise detail→sample cardinality and a separate SamplingRecord table are not frozen. |
| 检验记录 | PRD:525; Domain:141–142; Functional:251 | MATCH for InspectionTask→InspectionItem→TestExecution→TestResultRevision, frozen criteria/method and original execution facts with append-only corrections. TestInstance/TestResult names and PASS/FAIL/INVALID result enum are not specified as that chain. TestInstance is not presumed to require a new table: the formal distinction is task=assigned execution, item=frozen criterion, execution=original execution facts; none explicitly establishes the user's independent retest-instance identity. |
| 检验报告 | PRD:526; Domain:143; Functional:252 | MATCH for independent prepared/reviewed/approved summary, each item references final approved result revision. DESIGN GAP for root aggregation by InspectionRequest, sample coverage and report-request FK. Formal OpenAPI:20675–20714 requires a single inspectionTaskId and result-reference items. Request-wide aggregation compatibility is unresolved if it crosses tasks; multiple executions within the same task need not conflict. |
| 物料放行记录 | PRD:528–542; Domain:144–156; Functional:253–257 | MATCH for immutable scoped decision, inspection/exemption branches, independent release availability gate, revision/supersession evidence. Frozen decision/basis/source dictionaries are Domain:148. |

Incoming-DCP:17–23 explicitly authorizes these six records, chain, sole release source, separated statuses and shared eligibility service. It does not independently specify every physical field/FK for the nine new QMS tables.

## B. Formal database table map

Table names below are formally present, not newly proposed. “Column intent” must not be presented as a completed physical FK definition.

| Record | Tables | Formally defined content | Missing physical detail |
|---|---|---|---|
| 收货 | `wms_material_receipt`, `wms_material_receipt_item`, `md_material_lot`, `wms_inventory_ledger` | DB:804–805 receipt field groups; DB:85–93 and 797 lot IDs/snapshot/status/dates; WMS-stage:7–11 receipt/ledger invariants | Incoming review does not redesign delivered MES-008 physical contracts. DB:797 receipt_item_id is named BIGINT NULL without explicit FK keyword there. |
| 请验 | `qms_inspection_request`, `qms_inspection_request_item` | DB:809 names; DB:814 common header number/org/status/version/metadata; Functional:249 field intent | No individual table column dictionary, qc_specification_version_id, version table target, nullability, UK or explicit parent FK list. |
| 取样 | `qms_sampling_task`, `qms_sampling_detail`, `qms_sample` | DB:810 names; DB:814 parent lineage; DB:225–232 original sample columns; DB:798 added scope/task/request-item/type/status and collection/receipt/storage fields | No standalone sampling_record table; detail FK/cardinality/sample-generation physical constraints unspecified. Additional sample field types/nullability largely absent. |
| 检验 | `qms_inspection_task`, `qms_inspection_item`, `qms_test_execution`, `qms_test_result_revision` | DB:811 names; DB:563–571 revision fields; DB:799 added item/execution/result/unit/conclusion/reason/recorder/reviewer/signature and unique(item,revision) | No qms_test_instance table or definition equating Instance to task/item/execution. No QMS lower/upper snapshot column dictionary. Formal result enum is PASS/FAIL/INCONCLUSIVE at OpenAPI:20658–20664; INVALID is absent. New item/execution references named without explicit FK target clauses. |
| 报告 | `qms_inspection_report`, `qms_inspection_report_item` | DB:812 names; DB:814 header/lineage; Domain:143 and Functional:252 final approved revision references | DB does not enumerate those physical FKs; formal OpenAPI:20675–20714 does require inspectionTaskId plus items carrying inspectionItemId/resultRevisionId. Request aggregation across tasks remains unresolved. |
| 放行 | `qms_release_decision` | DB:244–252 original fields; DB:800 incoming extension/scope/target/supersession; Domain:148 dictionaries | Original main_batch_id NN becomes nullable in incoming extension. Exact scope CHECK expression and extension FK clauses not written. Original signature_id NN and SYSTEM_RULE no-human-signature rule require explicit consistency treatment; no fabricated signature is valid. |

`qc_specification_version_id` and a `qc_specification_version` producer are MISSING from the current formal text package searched. DB:611–617's `md_material_quality_spec` is a different legacy table with material_version_id/standard_code/spec_json; DB:840–842 and Material-DCP:9 retire its active use. It cannot be represented as the required active QC specification-version FK merely because names resemble each other.

## C. PK/FK and lineage: defined versus inferred

| Relationship / key | Explicit formal status | Classification |
|---|---|---|
| Common `id BIGINT AUTO_INCREMENT PRIMARY KEY` | DB:7 and 17; mutable/immutable profiles DB:5–23 | MATCH for common PK patterns. Selection of immutable profile for every new detail/execution table is not individually enumerated. |
| Controlled header metadata/version/business number | DB:814 | MATCH. Scope and presence specified; no full per-table type/UK list. |
| `md_material_lot.material_id` | DB:87 BIGINT NN FK | MATCH; canonical lot per DB:792. |
| `qms_sample.main_batch_id`, `.material_lot_id` | DB:227–228 BIGINT NULL FK | MATCH for declared references; stage installation belongs to approved sequencing decisions, not a new temporary batch model. |
| `qms_sample.sampling_task_id`, `.inspection_request_item_id` | DB:798 names; Domain:140 lineage | MATCH for reference intent; DESIGN GAP for explicit physical FK clause/nullability. No `sampling_detail_id` column is named in formal DB. |
| Request→items; task→details; task→inspection items; report→items | DB:809–814 names/parent lineage, Domain:139–143 | MATCH at logical lineage level; DESIGN GAP for exact physical columns and FK/cardinality constraints. |
| SamplingRecord 1:N Sample | Formal chain PRD:524, Domain:140, Functional:250 | SEMANTIC MISMATCH if SamplingRecord is treated as a separate already-frozen entity. Logical task/detail sampling record can yield samples, but exact 1:N at that user's abstraction is not explicitly frozen. |
| Sample→TestInstance→TestResult | Formal chain PRD:525 and Domain:141–142; revision sample_id DB:565 | DESIGN GAP: cannot confirm an alias of TestInstance to TestExecution, InspectionItem or InspectionTask from formal text. Task assignment, item criterion and original execution facts are defined, but the requested attempt/retest identity is not. No new table is presumed. |
| `qms_test_result_revision.sample_id`, `.previous_revision_id` | DB:565,568 explicitly BIGINT FK / nullable FK | MATCH. Revision uniqueness is explicitly `(inspection_item_id,revision_no)` at DB:799. |
| revision inspection_item_id / test_execution_id | DB:799; Domain:141–142 | MATCH for columns/references; exact physical FK declarations not specified in that section. |
| report item→approved result revision | Domain:143; Functional:252; OpenAPI:20675–20714 | MATCH logical invariant and API inspectionTaskId/item IDs. DESIGN GAP for physical FK clauses and request-wide cross-task aggregation compatibility. |
| InspectionRequest→qc_specification_version_id real FK | Functional:249 mentions standard/specification versions | MISSING producer/key definition; DESIGN GAP cannot be closed with a VARCHAR label or arbitrary JSON. |
| release main_batch_id / finished_lot_id / decision_by / signature_id | DB:246–252 declares FK | MATCH for original FK declarations. Finished lot canonical table is `md_material_lot` under DB:596; incoming target scope extended DB:800. |
| release material_lot_id / inspection_report_id / supersedes_decision_id | DB:800 names nullable fields and scope/target CHECK | MATCH logical references; exact FK clauses and one-current-decision constraint not individually frozen. |
| audit actor / signature signer | DB:258,279 explicitly sys_user(id) FKs | MATCH. A string “SYSTEM” is not a substitute for a registered identity. |
| qualification→user | DB:350 and 830 | MATCH. Sampling/test task qualification_code and SOP/method→qualification association are absent from formal QMS table definitions. |

## D. State machine and current requirement checks

| Object / requested behavior | Formal frozen state / rule | Result |
|---|---|---|
| InspectionRequest | DRAFT→SUBMITTED→ACCEPTED→IN_PROGRESS→COMPLETED (State:81) | MATCH. These are workflow states; do not add ACCEPTED to the separate record_status dictionary. |
| SamplingTask | PLANNED→ASSIGNED→IN_PROGRESS→COMPLETED (State:82) | MATCH. |
| Sample | CREATED→COLLECTED→RECEIVED→IN_TEST→TEST_COMPLETED→RETAINED or DISPOSED (State:83) | MATCH; supersedes older general Sample progression at State:40 for incoming scope. |
| InspectionTask | CREATED→ASSIGNED→IN_PROGRESS→PENDING_REVIEW→QC_PASSED or QC_FAILED (State:84) | MATCH; no TestInstance lifecycle specified. |
| Test result PASS/FAIL/INVALID | DB:570/799 omit an enum, but formal OpenAPI:20642–20674 explicitly requires resultConclusion with PASS/FAIL/INCONCLUSIVE | DESIGN GAP / explicit pending adjudication of latest requirement versus frozen contract. INVALID is absent and cannot silently map to INCONCLUSIVE or FAIL. OOS/OOT are investigation conditions (Domain:84,90), not the resultConclusion enum. |
| OOS approved retest creates new instance | PRD:409–416, Domain:84,90, State:40 require investigation/approved retest and retain OOS | MATCH approval/history requirement; DESIGN GAP for new TestInstance identity, attempt link and state because TestInstance is undefined. A correction revision is not evidence of an approved retest instance. |
| Numeric lower/upper snapshots | Domain:141 and Functional:251 freeze acceptance criteria/method | MATCH concept of frozen criteria; MISSING explicit QMS lower/upper snapshot fields, data types, inclusivity and evaluation rule. DB:410–411 limits belong to process parameters, not QMS. |
| Report by InspectionRequest | Functional:252 independent approved summary; Domain:143 approved result references; OpenAPI:20675–20714 requires inspectionTaskId plus items | DESIGN GAP / explicit pending adjudication: same-task multiple executions can fit the existing command; request-wide cross-task aggregation compatibility and coverage are not defined. Do not infer a formal-document contradiction or silently remove inspectionTaskId. |
| InspectionReport | DRAFT→REVIEWED→APPROVED; approved content superseded, not edited (State:85) | MATCH. |
| MaterialLot required quality path | QUARANTINE→PENDING_SAMPLING→SAMPLING→SAMPLED→TESTING→PENDING_QC_REVIEW→QC_PASSED→PENDING_QA_RELEASE→RELEASED; failure QC_FAILED→PENDING_DISPOSITION→REJECTED or approved PENDING_QA_RELEASE (State:67–69) | MATCH; QC_PASSED is not available stock. |
| Inventory | BLOCKED/AVAILABLE/FROZEN (DB:819; State:77) | MATCH. |
| User quality “LOCKED” | No LOCKED quality enum in DB:818 | MATCH to existing dictionary behavior: inventory FROZEN and eligibility rejection (State:77, PRD:542). LOCKED is the user-facing concept here, not a requested additional enum; no new dictionary conflict is inferred. |
| User quality “EXPIRED” | No EXPIRED quality enum in DB:818; expiry_date/retest_date DB:91–92,797 | MATCH to existing expiry/retest eligibility block at PRD:542, Domain:156 and WMS-stage:11. EXPIRED is the user-facing gate condition here; no additional quality enum or conflict is inferred. |
| Exemption | QUARANTINE→RELEASED atomically with system decision and BLOCKED→AVAILABLE; no intermediate fabricated states/records (State:73; PRD:534–538) | MATCH. |
| Decision | RELEASED / REJECTED / OTHER_DISPOSITION; basis FULL_INSPECTION / INSPECTION_EXEMPT / RETEST / OTHER_APPROVED_BASIS; source USER_QA / SYSTEM_RULE (Domain:148) | MATCH; these are the exact formal dictionaries. |

Current incoming delta already supersedes older quality BLOCKED wording (State:25) with separated quality/inventory dictionaries (State:63–85; DB:816–820), and supersedes original MainBatch-only release (Domain:97) with scoped incoming release (Domain:146–152). These historical remnants must not be treated as authorization to add new enums or remove incoming scope. Residual original signature_id NN versus no-signature SYSTEM_RULE is a physical consistency gap (DB:252,800 versus GxP:18), not permission to fabricate signature evidence.

## Candidate-document deviation audit (not product implementation findings)

| Candidate location | Formal comparison | Finding |
|---|---|---|
| `mes008a-contract.json` IncomingReleaseDecision.releaseBasis (`INSPECTION_REPORT`) and decision subset | Domain:148 exact dictionaries | SEMANTIC MISMATCH / UNAUTHORIZED DESIGN candidate: wrong basis and missing allowed values. Must not implement as written. |
| JSON IncomingRequestCreate.requestType arbitrary string | Domain:139 exact four types | SEMANTIC MISMATCH: formal enum was lost. |
| Markdown:22 and JSON IncomingReportCreate.inspectionTaskId | Formal OpenAPI:20675–20714 requires inspectionTaskId and items with inspectionItemId/resultRevisionId | MATCH for the task reference itself; the earlier claim that it was invented is withdrawn. Candidate omits required input items and freezes a narrower shape without proving compatibility. Latest request-based cross-task aggregation remains DESIGN GAP / pending adjudication, not unauthorized merely because inspectionTaskId exists. |
| Markdown:19 and JSON lowerLimit/upperLimit | Domain:141 freezes criteria but no QMS columns/scale/boundary rules | DESIGN GAP elaborated without a reviewed cross-document contract. Not proof fields were already frozen. |
| Markdown:15,18 and JSON required qualificationCode; Markdown paragraph associating codes with plan/method | DB:348–354 defines person qualification but no QMS plan/method association | UNAUTHORIZED DESIGN candidate for required QMS fields/association. Universal qualification gating is formal; named codes and mapping are not. |
| Markdown:70 automatic IAM registration for rule actor | GxP:6,18 requires registered technical actor; DB:258 FK; docs/development/iam-bootstrap.md:1–3 is admin bootstrap only | DESIGN GAP in provisioning method. Technical actor itself is MATCH; automatic creation/no-role/no-login lifecycle was invented in candidate and is not established by cited formal contracts. |
| Markdown:9 blanket DECIMAL(20,6), metadata projections and all-detail Profile A; Markdown:17 sampling_detail_id and duplicate status/sampled_at mapping | DB:5–23,798,814 define common metadata and some fields, not these exact per-table choices | Unfrozen candidate elaboration / DESIGN GAP, not an approved physical contract. Detail→sample lineage intent is formal; precise new column constraints must not be asserted frozen. Ordinary physical elaboration alone is not a business blocker; relationship/semantics changes require review. |
| Markdown:28 immutable revision reviewer columns left empty and review represented only by task content | DB:799 names recorder/reviewer timestamps; Functional:252 requires final approved revision | DESIGN GAP / SEMANTIC MISMATCH candidate: no complete authoritative proof of how per-revision approval and review linkage are satisfied. |
| Candidate result conclusion PASS/FAIL/OOS/OOT | Formal OpenAPI:20658–20664 is PASS/FAIL/INCONCLUSIVE; latest user requests PASS/FAIL/INVALID | SEMANTIC MISMATCH candidate: it removes INCONCLUSIVE and adds OOS/OOT. This error is separate from latest INVALID requirement needing explicit adjudication. |
| Candidate generic provider meanings VERIFY/APPROVE/RELEASE/REJECT | Domain:117 and GxP:16 list ownership/business signing points | Provider extensibility is supported; exact mapping of every new QMS point/digest to enum is not a new formal business definition. No new CORRECT enum is formally authorized. |

No new named QMS table was introduced by the previous candidate; its principal deviations are invented exact columns/types/associations/cardinalities and incorrect enums. It remains a candidate, not an authoritative release or executed implementation.

## Coverage ledger

Read in full for the relevant chapter boundaries, not from previous summaries:

- PRD:15–24 invariants; 69–107 material/supplier/equipment/qualification; 229–268 WMS; 389–428 QMS/retest/release; 511–574 all incoming and applicable later deltas.
- Domain:1–190 complete file, including QC/Deviation/Release/domain services/transactions/platform and incoming appendices.
- DB:1–23 profiles; 52–93 material/lot; 225–291 sample/deviation/release/audit/signature; 348–354 qualification; 555–617 IPC/result/CAPA/frozen decisions/material quality spec; 790–859 complete incoming schema and later deltas. DB:403–411 checked only to disambiguate process lower/upper limits.
- State:1–115 complete file including original and incoming state chapters/deltas.
- Functional:85–100 WMS; 181–299 complete QMS/release/audit/platform/incoming and later-delta chapters.
- Incoming-DCP:1–42 complete; `00_DESIGN_CHANGE_TRACE_DCP-MES-002-R2-001_V1.0.9.md`:1–17 complete; Material-DCP:1–18 complete; WMS-stage:1–18 complete.
- GxP:1–18 complete applicable identity/signature chapters for candidate audit.
- Cross-review correction: `docs/development/incoming-review-api-ui.md`:55–64 schema map read; formal OpenAPI:20642–20714 TestResultRevisionCommand and InspectionReportCommand read directly. The latter explicitly defines result enums and task/item report references even though DB prose does not repeat them.
- `docs/PROJECT_BASELINE.md`, `docs/database/README.md`, `docs/compliance/README.md`, `docs/architecture/module-boundaries.md`, `docs/development/iam-bootstrap.md`: read; no additional formal incoming table/FK contract there. Earlier `mes008a-contract` read only as audit subject.
- Exact term search over all current-release md/csv/yaml: `qc_specification`, `TestInstance`, `SamplingRecord`, `test_instance`, `PASS.*FAIL.*INVALID`, `LOCKED`, `EXPIRED`. No current incoming authoritative definition of the requested QC specification-version producer or TestInstance found. Requested PASS/FAIL/INVALID differs from the existing PASS/FAIL/INCONCLUSIVE schema (verified directly, not inferred from a no-match search); BLOCKED and equipment-expiry matches are unrelated or explicitly distinguished above.

Review result: the six-record intent is present, but the latest requested physical specification FK, sampling cardinality, test-instance/result validity and request-based report aggregation cannot be claimed implementation-ready from the candidate or current sparse table lists. This report intentionally supplies no replacement model, migration, new enum or provisioning design. Parent review maps E–J separately; implementation waits for the user's confirmation of the consolidated map.

Scope qualification: sparse per-table SQL detail or GenericDTO is not by itself a stop condition for the whole module. The material gaps in this review concern a missing QC specification-version producer/FK, unresolved sample/retest-instance semantics, result validity semantics and report aggregation ownership. Other explicitly defined incoming capabilities remain mappable. No whole-module redesign is proposed.

Cross-review correction: formal OpenAPI is authoritative alongside DB/Domain. DB silence never establishes absence across the entire baseline. The result enum and required report task/item references above supersede this report's earlier broader absence claims; the old candidate remains non-authoritative and its actual incorrect release/result enums remain findings.
