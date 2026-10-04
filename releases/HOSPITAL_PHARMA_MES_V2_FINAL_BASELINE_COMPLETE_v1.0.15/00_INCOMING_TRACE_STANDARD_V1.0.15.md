# Exact frozen-standard trace completion

2026-10-03. Within explicit approved incoming contract completion and full acceptance scope. The six-record chain already requires request-frozen qcSpecificationVersionId and actual standard traceability; this completes the missing read projection enum without new business facts.

TraceNode.type adds exactly QC_SPECIFICATION_VERSION. Its id is the actual immutable InspectionRequest.qcSpecificationVersionId from the same-organization incoming query producer; never the currently active/latest version. Emit one node per distinct identity, and an edge sourceType=INSPECTION_REQUEST/sourceId=request.id, targetType=QC_SPECIFICATION_VERSION/targetId=request.qcSpecificationVersionId, relationship=FROZEN_STANDARD. Unknown historical version status/revision remains null, never inferred. Each request edge is retained even if multiple requests share the version. Retired referenced standards retain the same identity. Other node/edge fields remain unchanged.

No table, column, state, mutation API, permission, menu or business route is added. Existing trace:view controls the graph; the existing QC specification-version route /quality/specification-versions/:id/edit opens the exact historical version with existing qms:specification:view enforcement and approved/retired read-only controls. No second standard data source or editable result is created.

TC-TRC-001 and IQ-E2E-01 now assert actual frozen version node plus request-to-version FROZEN_STANDARD edge alongside receipt, sampling, test, original/final result, report, release and signature lineage. A/B acceptance separately asserts real issue creation/confirmation boundaries, not only reservation. Database/Domain/State/API/UI/Permission/Integration/Test/RTM review: only typed read graph projection changes; no physical migration is needed. V022 is the append-only technical collation fix for the existing review guard, unrelated to this read projection.

Implementation of exact signature-policy evidence remains inside the existing frozen role/signature requirements: synchronous SignatureAppliedEvent(context, persisted SignatureRecord) is emitted in the existing signature transaction. eBR policy audit is keyed by the exact signature ID; requestId is correlation only and never authorizes a different signature. Listener failure rolls back the signature and idempotency success together. No new platform service, business API, storage entity or permission is created.


## Approved functional closure delta — v1.0.15

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.


## Verified functional closure recovery — 2026-10-04

The approved bounded delta and one-time V024 exception are recorded in [functional closure §§9–10](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md). V024 is successful; V001–V023 remain immutable. Trace inventory decisions use INVENTORY_DECISION / INVENTORY_CONTROL with original SIGNED_EVIDENCE, decimal signing values are exact strings, and mandatory missing headers return 400. Scope-level native verification PASS does not waive other formal task RTM or authorize full MES-012/013.
