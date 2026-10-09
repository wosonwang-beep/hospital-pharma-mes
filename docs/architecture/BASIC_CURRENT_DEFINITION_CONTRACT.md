# Basic maintenance and current process contract — 2026-10-09

Business terminology correction approved by the human on 2026-10-09: the public name is **生产工艺**, never 工艺包. Menu, page titles, fields, permissions display and production/eBR selectors use 生产工艺. Existing ProcessPackage / processPackageId / packageCode identifiers, routes, permission codes and historical evidence remain compatible.

Authority: [DCP-BASIC-NO-AUDIT-VERSION-001](../development/DCP-BASIC-NO-AUDIT-VERSION-001-APPROVED.md), approved directly by the human. This is the bounded supplement to frozen v1.0.23 and DCP-DATABASE-MENU-AND-USABILITY-001; it is not an alternative baseline. Historical releases and executed migrations are immutable.

## Database and migration

V041 adds proc_current_definition, unique (org_id,package_id), with no revision/version counter. Formula and route storage permits exactly one of legacy package_version_id and current_definition_id. Current formula/route rows have NULL business version; existing names are retained to preserve WMS/execution foreign keys. Items, operations and parameters retain stable physical identities. Existing version_no columns are dormant for basic/current maintenance; their historical values are not reset or incremented.

Upgrade copies the latest previously operator-visible definition and its children to independent current records. It never updates historical process rows, audits, signatures, eBR definitions or production snapshots. New eBR templates and production snapshots carry process_package_id; historical package_version_id is retained as the mutually exclusive archival binding. V042 disables the obsolete process:package:publish catalog entry without deleting historical grants. No migration repair or clean.

## Domain and state

All basic writes are validated, permission-filtered, organization-scoped, idempotent and transactional. Standalone basic mutations use READ_COMMITTED isolation: MariaDB repeatable-read snapshots can otherwise reject a lock after a concurrent writer. Updates lock the target row and serialize; the last validated save wins. No client version requirement, stale-version rejection, revision increment, change-reason evidence or audit append exists in this scope. Actor/time fields remain ordinary record metadata. Shared platform audit/idempotency infrastructure remains available to regulated consumers.

ProcessPackage retains ACTIVE/INACTIVE. Current configuration readiness is DRAFT (incomplete) or EFFECTIVE (fully validated); this is not a process revision lifecycle. A single atomic save validates formula, route, dependencies, material/unit compatibility and parameter/IPC rules. It writes that same current record and emits Maintained(organizationId,packageId). Process revision create/compare/submit/approve/publish functions are retired. Historical signable definitions remain read-only for archival consumers.

Production release locks the process package and freezes the identified current content, materials, controlled weighing policy and approved eBR definition in its transaction. Subsequent basic edits cannot change frozen JSON/hash, execution gates, WMS lineage or QA evidence. Production version checks, audit and signatures remain mandatory. eBR templates retain their own regulated revision/approval/signature lifecycle; only their process binding changes.

## API and integration

The [bounded OpenAPI supplement](../contracts/BASIC_CURRENT_DEFINITION_OPENAPI.json) contains the affected operations and schemas. Runtime and test API documents use fully qualified DTO schema names to avoid collisions between nested Create/Update records; HTTP payload names are unchanged.

- Basic CRUD endpoints remain, with Idempotency-Key and existing permissions. They omit versionNo from responses and have no If-Match precondition. Legacy versionNo fields accepted by command DTO adapters have no effect and are not part of the new contract.
- GET /process-packages/{id} returns currentDefinition {id,status,formula,route}, not selectedVersion/versions. GET list returns that same shape. Version selection/filtering is retired.
- PUT /process-packages/{id}/current-definition accepts {formula,route}; neither sourceVersionId nor sourceVersionNo nor versionNo is required or consulted.
- /process-versions/** and /process-packages/{id}/versions are denied and have no controller write mapping.
- POST /ebr/templates accepts {processPackageId,templateCode}. Existing templates still expose their original archival packageVersionId. Current operation choices come from the package's current route. Creating an eBR revision from an archival template binds its package's current definition and remaps operation identities by unchanged operationCode; a missing code fails closed with PROCESS_BINDING_REVIEW_REQUIRED. Its predecessor, canonical hash and signatures remain unchanged.
- Production release replaces packageVersionId with processPackageId; production versionNo, reason, ebrTemplateVersionId and its If-Match behavior remain regulated.
- Book mappings accept exactly one of processPackageId (new) and packageVersionId (historical); published historical JSON/hash remains unchanged. New create/publish rejects archival-only mappings with BOOK_CURRENT_PROCESS_REQUIRED. New mappings validate package/product/eBR/operation consistency. Existing published archival mappings remain read-only and parse unchanged.

Current ProcessQueryService methods are currentSnapshot, requireCurrent, requireCurrentIdentified, currentOperations and requireCurrentOperation. The legacy snapshot methods are archival consumers only. Their existence does not authorize new historical writes. WMS/execution consume frozen item/operation/parameter identifiers and values; their permissions, ledgers, statuses and signatures are unchanged.

## UI and permissions

Global UI V2 remains. Master/product details retain T3/T2, lists T1, and process maintenance T4/T2. Remove basic audit buttons/cards and all process version selectors, comparisons and lifecycle actions. Process selection in eBR and batch release selects a package's current definition. System audit continues exposing historical evidence under audit:view. No new permission codes, navigation domains or UI system.

## Test/RTM and CI contract review

| Requirement | Required evidence |
| --- | --- |
| BASIC-NV-001 | Basic update succeeds without a version; obsolete version values do not create preconditions; counter unchanged; no audit append |
| BASIC-NV-002 | Repeated current saves retain one identity and create zero process revisions; detached old content unchanged |
| BASIC-NV-003 | New eBR package binding returns correct operation choices; eBR audit remains |
| BASIC-NV-004 | Retired revision writes cannot mutate legacy evidence |
| BASIC-NV-005 | Production retains version conflicts and audit; release freezes current definition; later save leaves snapshot byte-identical |
| BASIC-NV-006 | Upgrade leaves existing audit/signature/process-version/production-snapshot evidence unchanged |
| BASIC-NV-007 | Real 5173 basic pages have no audit/version controls and render without relevant console/API errors against actual 8080 |
| BASIC-NV-008 | Permission/org isolation, invalid definitions, retained item/operation identities and idempotent replay still hold |

Obsolete CI expectations of audit appends/stale-version rejection in basic maintenance and process revision creation must be replaced by the approved assertions above. No regulated audit/signature/concurrency assertion may be weakened. Historical fixture compatibility and downstream integration suites must be reviewed; pending gates prevent READY FOR ACCEPTANCE. Runtime results and debt are recorded only in MES_TASKS.md.
