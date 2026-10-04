# v1.0.4 cross-document consistency review — 2026-10-03

PASS, reviewed before switching entry points. Human reply “授权” to the exact DCP-MES-004-005-R2-001 proposal authorizes only its bounded changes. Historical v1.0.2 and v1.0.3 remain unchanged.

- Database: existing seven LG-004/005 tables and fields retained; LG-004 owns the previously approved nullable conversion-material FK handoff. Physical versions V007/V008 are new, not Section 15 logical version numbers.
- Domain/state: root DRAFT/APPROVED/INACTIVE, explicit version DRAFT/SUBMITTED/APPROVED; immutable approved rules and policy, historical explicit version reads; supplier UNAPPROVED/APPROVED/INACTIVE with inclusive UTC validity. No new signature point or downstream implementation. Rule edits select DRAFT version; root identity is editable only while root DRAFT. Approved root permits its newly created DRAFT rule edit with data omitted.
- API/OpenAPI: all existing material/version/supplier/relationship paths have full closed DTOs; material POST 201; explicit versionId; root If-Match/body versionNo; child recordVersionNo separate. Reference traversal PASS. Paths/permissions preserved, including relationship master:material:update.
- UI/Permission: existing UI-MAT/UI-SUP query/create/view/edit routes; full frozen fields and nested rules, version selection/policy, command permission checks and relationship editor under existing material edit route. Existing prototypes remain authoritative.
- Audit/Test/RTM/Integration/Tasks: affected numbered artifacts and cards carry the approved delta; TC-MAT-001..005 and TC-SUP-001..002 retained; MaterialVersion and ApprovedSupplier producer contracts unchanged in ownership; batch/receipt consumers remain later tasks.
- Debt retained outside scope: inherited duplicate UI-WMS-REC-Q/V matrix rows. No WMS route redesign made.

Authority pointers may now select v1.0.4. Execution evidence and readiness remain in MES_TASKS.md and docs/acceptance, not this design approval.

Execution ledger follow-up: DEV V007/V008 and additive V009 frozen indexes APPLIED. Final read-only history query confirms V001–V009 successful; targeted fixture-user/material checks return zero residue. No executed SQL changed.
