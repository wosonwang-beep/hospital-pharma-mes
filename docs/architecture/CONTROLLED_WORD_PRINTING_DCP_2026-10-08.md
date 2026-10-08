# DCP-CONTROLLED-WORD-PRINTING-001

## Approval and boundary

2026-10-08: the user explicitly authorized Word template management and business PDF generation, inspection reports first; database additions were explicitly authorized. The user selected A: upload an existing DOCX and edit Word in the browser using an online Office service. Existing main / FINAL BASELINE COMPLETE v1.0.23 is retained. This is the bounded printing implementation delta for design-authority review, not an implicit acceptance or an authority-pointer switch. Prior frozen releases and executed V001–V032 are unchanged.

Included: reusable `mes-reporting` module, inspection-report provider in mes-boot, template list/new version, Word editing, Chinese searchable field dictionary, loop-table column selection, real PDF validation, publish/deactivate/bind, business draft/formal PDF, immutable archived output. No inspection method/instrument fields; no SQL/SpEL or JSON layout designer; no other business module integration. No general audit, role grants, production deploy, push or runtime security changes.

## Database and domain

New physical V033 creates organization-scoped template-version, business-binding, artifact and editor-session tables. Template code/revision is unique per organization; source DOCX and creation identity are immutable. Publication freezes preview and publication time; published versions can only become inactive. Upload and authenticated Word callbacks append independent draft versions. Binding has immutable organization/business/template identity and mutable enabled flag with optimistic version. Artifact is append-only with business version, template revision, canonical server DTO JSON, snapshot SHA256, rendered DOCX, PDF and PDF SHA256. Formal key uniqueness provides formal-reprint identity. Database triggers forbid regulated deletes and artifact updates. Editor sessions are expiring IAM-session-bound technical records.

V033 is authored but **not executed**. Highest successful history was read-only verified as 032. The user requires isolated migration validation before target application. AGENTS.md forbids another local database/Docker DB and permits only the persistent native DEV database. No suitable isolated target has been authorized; migration/transaction/concurrency integration remains a blocker. Do not restart mes-boot against DEV while V033 is pending. No seed business data or role assignments are inserted by this delta.

## State, identity and signature

Template `DRAFT -> VALIDATED -> PUBLISHED -> INACTIVE`; revalidation of a draft/validated row requires expected version and reason. Validation renders backend example data and performs real server conversion. Publish requires validated preview. No published overwrite or reactivation. Business binding requires published compatible version.

Generation accepts identifiers and requested formal mode only. Backend authorizes organization and `qms:report:view`, locks the report, reads all server-selected items plus original results, source lot/material, frozen criteria and result units. Formal mode requires existing APPROVED report and record status, existing approval signature ID and successful SignatureTransactionService verification. Draft explicitly shows draft mode and does not reuse approval as a formal signature. Names represent attributable actors, not forged handwritten signatures. No new approval workflow is introduced.

Formal reprint for the same report/template/business revision returns stored artifact bytes even if the template was subsequently deactivated. Viewing any artifact revalidates report-read permission and organization and verifies stored PDF hash. History allows selecting older archived business revisions without regenerating from current data. Browser preview, download and print use the same PDF Blob.

## Permission and UI

New permissions: `print:template:view` (menu `/admin/print-templates`); `print:template:manage` (upload/editor/validation); `print:template:publish` (publish/deactivate/bind); `print:document:generate` (business generation/archive read). No role is automatically granted these permissions. Report view permission is additionally mandatory on business read/generation. Editor transfer endpoints validate scoped signed expiring capability and active IAM session; callback also requires Office JWT with matching payload, key and actor. The dedicated stateless transfer chain does not alter the existing MES security chain.

UI V2 retained. Template management selects T1; Word editor selects T3; report printing is a subordinate panel within existing T3 report details. Shared navigation filters by existing auth store. Chinese dictionary groups report/material/items/signature with descriptions/examples and search. Word plugin inserts tagged content controls at cursor; loop uses dedicated column selection and generated DOCX fragment through the Community plugin API, not paid Automation/createConnector. Published source stays intact; Office saves a new draft and only a signed successful callback reports completion.

## Conversion, upload and editor security

DOCX: zipped <=5MB, expanded <=40MB, per-part <=8MB, <=1000 entries; reject traversal, duplicate entries, abnormal compression, DTD/external entities, macros/VBA/ActiveX/embedded OLE, external relationships, attached templates, data bindings, executable fields and unsupported media. Only whitelisted flat placeholders and reserved tagged controls accepted; Word fields only PAGE/NUMPAGES. Rendering uses strict default EL. No arbitrary evaluation.

JODConverter launches a per-job LibreOffice profile/temp directory using an ephemeral loopback UNO connection, one task per process, semaphore serialization, 10s process/30s conversion/5s queue limits. PDF must have valid magic and <=25MB. Stop and temporary cleanup in finally; failures do not create an artifact. Conversion never uses an external website.

Online Word: session key unique per editing actor; TTL2h bound to active IAM session. Configuration and callbacks use HS256 JWT, capability audience/expiry, exact payload/key/actor checks, idempotent hash-dedup. Callback download restricted to configured server's same-origin `/cache/files/`, no redirects, bounded connection/read/total time and <=5MB; downloaded DOCX is revalidated before version append. No disabled JWT or automatically generated persistent key. Existing secret approval is pending. DocumentServer/MES/plugin URLs must be privately mutually reachable; localhost-only host exposure is preferred, no public exposure/firewall change inferred. Do not relax its private-address policy without explicit review/approval.

## Dependencies and licenses

poi-tl 1.12.2 and JODConverter local 4.4.11: Apache-2.0. Apache POI 5.5.1 / commons libraries pinned for dependency convergence with existing MinIO/PDFBox; unused Batik/SVG pipeline excluded. LibreOffice 26.2.6.3: MPL-2.0/LGPL licensing per official distribution; installer signature/hash verified. ONLYOFFICE Docs Community: AGPL-3.0, no commercial purchase. Preserve its license/branding; distribution or modifications must follow applicable license obligations. No Developer Automation API is used. Final server version/image not yet installed/validated.

Official references: https://github.com/Sayi/poi-tl ; https://github.com/Sayi/poi-tl/blob/master/poi-tl/src/test/java/com/deepoove/poi/tl/example/PaymentExample.java ; https://jodconverter.github.io/jodconverter/latest/samples/spring-boot-rest/ ; https://api.onlyoffice.com/docs/plugins/interacting-with-editors/document-api/Methods/AddContentControl/ ; https://api.onlyoffice.com/docs/plugins/interacting-with-editors/document-api/Methods/InsertAndReplaceContentControls/ ; https://api.onlyoffice.com/docs/docs-api/usage-api/callback-handler/ .

## Integration and task contracts

`PrintDataProvider` is the future module extension point; current provider only INSPECTION_REPORT. Reporting depends on existing masterdata/audit common infrastructure; mes-boot owns QMS adapter, avoiding a qms/reporting cycle. No BPM/workflow, Redis replacement, public conversion or mandatory Docker introduced for PDF. Office server is separately enabled by `mes.print.editor.enabled`; enabling requires approved `jwt-secret`, document-server-url and public-base-url. PDF requires `mes.print.office-home=C:/Program Files/LibreOffice`.

Consumed: existing QMS report selection/approval/lineage, IAM permissions/session/organization, signature verification, MasterMutation audit and scoped store. Produced: bounded printing API, immutable template/artifact records and Chinese Word plugin. Existing MES task dependencies/accepted statuses unchanged. Runtime task state only MES_TASKS.md.

## Validation and consistency review

RTM and acceptance evidence: `docs/acceptance/controlled-printing/REPORT.md`. API: `docs/api/controlled-printing.openapi.yaml`. Migration: V033. Review status: implementation authored; focused tests in progress; database and online Word integration blocked. All old baseline pointers remain unchanged until design-authority cross-document review is complete. No claim READY FOR ACCEPTANCE while those gates remain open.
