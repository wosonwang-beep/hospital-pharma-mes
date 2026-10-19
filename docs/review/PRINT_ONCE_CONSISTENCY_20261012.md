# PRINT-ONCE-001 bounded consistency review

Read sources: AGENTS.md, MES_DEV_RULES.md, database-and-validation-strategy.md, PROJECT_BASELINE.md printing routing, MES_TASKS printing entries and PRINT_HISTORY_TEMPLATE_READONLY_IMPACT_20261012.md.

Approval/requirements → PRINT_ONCE_DESIGN_LOCK_20261012.md (exact human approval, preservation and deletion scopes).
API → print-once.openapi.json and PrintController: same identifiers and authorization, direct binary PDF POST, retained read-only old GET.
Domain/state → existing template lifecycle/formal business checks/print-type policy unchanged; current provider load and renderer reuse; no new aggregate.
Database/migration → V060 drops only two old-template FKs, keeps historical numeric provenance and immutable content guards; exact-ID/org/state delete guards; business deletion separate transaction.
UI → same selection/PDF controls, T1/T3/UI V2, remove obsolete report history section, consume direct Blob.
Permissions/GxP → no new rights or grants; source evidence or original provider authorizes retained files; old audit/signature/snapshots/bytes unchanged.
RTM/test/integration → authorized obsolete generation/reuse assertions replaced by direct-PDF/no-write assertions; real existing-record readonly IT and historic download checks, G5 manifest required for UI readiness.
Task dependencies → current print provider/source configuration only; no production/eBR expansion. Baseline routing points to this bounded supplement; immutable releases unchanged.

Cross-document contract review: CONSISTENT within explicit approved scope. Runtime build/database/HTTP/UI verdicts are separate, maintained only in MES_TASKS.md and execution evidence. SELF-REVIEW, no human acceptance claim.
