# eBR template name — bounded approved contract

Authority: frozen v1.0.23 plus [DCP-EBR-TEMPLATE-NAME-001](../development/DCP-EBR-TEMPLATE-NAME-001-APPROVED.md), explicitly approved by the human on 2026-10-09. Existing approved supplements remain applicable. Prior releases remain immutable.

## Database / migration

V043 follows successful V042 and adds nullable template_name VARCHAR(100) to ebr_template_version. No existing row is backfilled, renamed or deleted. No old migration, signature, hash, snapshot or PDF is rewritten.

## Domain / state / permissions

Name belongs to an eBR template revision. New creation requires nonblank trimmed text <=100 characters; duplicate names are allowed and code remains the family identity. Save optionally supplies templateName; omission retains the existing value, explicit blank is rejected. Save is restricted to DRAFT with existing permission, reason, audit, optimistic concurrency and idempotency. New revisions inherit the name. New submission requires a name, including revisions copied from unnamed history. Previously submitted/approved/effective historical definitions retain their existing lifecycle and exact canonical content. No state, role, signature or permission code is added.

## API / integration

POST /api/v1/ebr/templates requires templateName alongside processPackageId and templateCode. PUT /api/v1/ebr/templates/{id} accepts templateName in the existing controlled save. Summaries/details return templateName, nullable for history; keyword matches name or code. Version comparison includes name differences. The [current OpenAPI supplement](../contracts/BASIC_CURRENT_DEFINITION_OPENAPI.json) records the schemas.

Canonical definitions include templateName only when non-null. Thus new named approvals/frozen production snapshots carry the name; historical null-name canonical JSON has exactly its previous shape and hash. New revision name changes cannot alter an earlier published definition or frozen batch. Production continues validating current production process + effective eBR template; WMS/QA/runtime consumers and immutable evidence boundaries remain unchanged.

## UI / dependencies

UI V2 / existing page structures remain. Create has required 模板名称. Draft designer permits editing the name through existing Save. List, detail, designer identifiers and production selectors display name + code/revision, with 未命名（历史模板） for absent names. Search uses name or code. No whole-book entity redesign, new route or new dependency/module is introduced.

## RTM and validation

EBR-NAME-001 required/trimmed/max length + search → EbrIT.requiredNameIsValidatedTrimmedAndSearchable.

EBR-NAME-002 controlled draft editing, revision inheritance, canonical freeze and immutable effective name → EbrIT.draftNameIsAuditedInheritedFrozenAndCannotRenameEffective + existing audit/concurrency regression.

EBR-NAME-003 unchanged legacy canonical hashes → EbrIT.historicalCanonicalNamesAreAbsentAndApprovedHashesRemainValid + pre/post V043 native evidence comparison.

EBR-NAME-004 actual create/list/detail/selection → direct 5173 / 8080 browser/API checks. Tests use existing DEV and rollback or clearly identified own fixtures. Existing signature/production snapshot/archive regressions remain required. Runtime readiness is only MES_TASKS.md.
