# DCP-CANVAS-PRINT-EDITOR-003 — bounded editor supplement

## Approval and boundary

The user's 2026-10-09 instructions selected canvas-editor for printing templates and data-field binding, placed “从 Word 导入” in the new/edit template header immediately before “预览填充数据”, required query labels and controls to stay horizontally paired at every viewport, and explicitly instructed “继续开发”. This supplement records that bounded authorization. It does not change the v1.0.23 authority pointer or prior frozen releases.

Included: replace the existing native printing UI with canvas-editor 1.0.4; reuse the upstream MIT DOCX plugin 1.0.0; load available business types and field definitions from existing authenticated server endpoints; insert header fields and repeating detail rows; import .docx content into the editable document; append a canonical DOCX draft with its canvas layout/binding descriptor; reopen that exact version; fill server example data and validate real server PDF. Template centre T1, independent new/edit page T2, preview subordinate to the editor/business detail. The list has one primary “＋ 新增打印模板”; no separate upload/Word import entry.

Not added: new database schema, new business fields/read projections, new provider registrations, new permissions/routes/state transitions, removal of printing audit/revision/signature controls, or business-formal eligibility changes. Existing provider registrations determine actual available types. WMS/finished entries shown in early design illustrations are proposals, not implemented adapters.

## Database, domain, state and API contracts

V033 template source DOCX remains the only canonical source and is still stored/hash-checked through PrintService. Standard DOCX custom property `mes.canvas.print.v1` contains `{schema,businessType,templateCode,templateName,document:{version,data,options}}`; it is part of that same immutable version, not a second table or external authoritative store. No Flyway migration is created, edited or repaired.

The existing `/printing/types`, `/printing/fields?businessType=...`, `/printing/templates` upload/list, `/{id}/docx`, `/{id}/validate`, `/{id}/preview`, publish/deactivate/bind and business artifact endpoints are reused. No request/response DTO, URL, permission or state-machine delta. The older bounded-block `/designer` API is retained for legacy callers; canvas editing consumes canonical DOCX instead. Native legacy and uploaded DOCX templates can be opened from the list with the existing row metadata.

Binding metadata is `{businessType,fieldKey,repeated,label,group}`. `fieldKey` is an existing stable provider key; labels are presentation only. Non-repeating controls export `{{fieldKey}}`; detail controls export `[fieldKey]` only in a row bound to `items`, with exactly one `{{items}}` marker for that row. The backend whitelist/guard remains authoritative. The editor rejects mixed business-source bindings and detail fields outside a repeating row. No arbitrary JavaScript, SQL, SpEL, embedded active block or computed control is introduced.

Draft save continues to append a new immutable version. Real conversion is still required for VALIDATED; publication and business binding retain their existing gates and optimistic version checks. Business generation accepts existing IDs only and preserves provider-owned permissions, signatures, snapshots, archive hashes and historical reprints.

## UI and integration

2026-10-09 user correction “和设计图的字段有出入”: the editor now preserves the supplied reference's four metadata items (模板编码、模板名称、适用功能、单据类型), three-tier source selection (业务功能、单据类型、数据区域), grouped field list, explicit selected-field insertion and binding property/info panels. This is a presentation correction over the same existing businessType/field definitions, not authorization to fabricate WMS/finished fields or metadata. The module/document labels are presentation labels for registered types; available choices and field keys remain server-driven. Missing unit metadata is stated explicitly; raw example values identify displayed primitive types, and “原样显示” describes the existing renderer rather than a new formatting contract. No schema/API delta.

List query labels and controls are `flex-nowrap` pairs; responsive wrap moves the complete pair, never the label alone. This explicit user requirement is also recorded in AGENTS.md. Metadata, data-region selection and field search use horizontal label/control pairs. V2 font, spacing, border, action and status conventions remain.

Import checks .docx and 5MB, replaces content only after the user resolves an unsaved-content confirmation, and restores the previous editor document on parsing failure. Word content becomes editable; authorized legacy placeholder tokens are restored as bound controls. Complex Word layout may need correction before real PDF validation; exact compatibility for every Office feature is not asserted.

The upstream plugin is vendored with its MIT license and provenance. Its unconditional download is removed so its Blob can be uploaded through the existing MES endpoint. Local integration normalizes equivalent percentage widths to numeric OOXML, exports CJK-capable placeholder fonts, and serializes PAGE/NUMPAGES as standard simple fields for the existing docx4j/FO consumer. It removes only an empty legacy page-counter footer before creating the real page counters. Conversion, upload and immutable history checks are not bypassed.

Canvas fill preview explicitly uses backend example data. Server PDF validation is separate; browser preview/download/print still consume the same generated PDF Blob. This increment does not claim real business-data acceptance for newly proposed modules.

## Test, RTM, migration and dependency review

Evidence and requirement mapping: [Canvas printing validation](../review/CANVAS_PRINT_EDITOR_20261009.md). Targeted frontend tests cover stable binding export, mixed-source/unknown-field rejection, repeated rows, legacy token restoration, custom-property roundtrip and PDF-format compatibility. Live 5173 interactions and real 8080 template persistence/PDF are checked; fixtures alone are not acceptance. The added boot integration test uses a synthetic exported DOCX and transaction rollback.

Dependencies stay acyclic; no backend module dependency is added. Existing reporting adapters and provider contracts are consumed unchanged. No production/eBR aggregate state machine is modified. Prior frozen documents/migrations are preserved. Runtime status remains exclusively MES_TASKS.md.

Cross-document consistency: PASS within this bounded supplement: database/domain/state/API/permissions/migration unchanged; UI/integration/RTM/tests recorded here and in linked evidence. No baseline pointer switch or human acceptance is inferred.
