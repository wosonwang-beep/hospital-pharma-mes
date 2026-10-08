# DCP-FINISHED-STRENGTH-GATE-001 — PROPOSED / NOT AUTHORIZED

Date: 2026-10-08
Authority: FINAL BASELINE COMPLETE v1.0.23 (immutable), Global UI V2 / T1–T6.
Disposition: **DESIGN CHANGE REQUIRED**. This proposal does not change the currently approved product, formula, GMP states, QA Release Gate, signatures or stored history.

## Verified R7 evidence

Real read-only API/Playwright, batch `20261007-KCL-001` (already QA_RELEASED):
- Product `氯化钾溶液`, labeled `30ml:3g/瓶`, implying 10 g / 100 mL if interpreted as 3 g potassium chloride per 30 mL final product.
- Frozen formula `20 g` potassium chloride for a `1000 mL` basis, implying 2 g / 100 mL **if** that basis represents final measured output, a **fivefold apparent mismatch**.
- Current QA review shows 8/8 pre-release gates passed; the existing gates do not demonstrate that the product's labeled strength was reconciled with frozen formula and final measured volume.
- The existing read-only browser test `frontend/mes-web/e2e/business-consistency.tmp.spec.ts` reports `consistent:false`. This is not proof that the formula or labeled clinical strength has been pharmaceutically approved; intermediate concentration, dilution, yield, assay basis and final-volume assumptions need verification.

## Decision required from design/pharmacy/quality authority

1. Specify the approved **structured strength definition** (API mass, final volume or finished-unit count, normalized units, acceptance basis, packaging and expected yield); do not derive the regulated gate from free-text parsing of `specification`.
2. Specify whether the frozen `formula.batchBasisQty` is the actual **final dosage volume** or an intermediate/pre-dilution basis. Define documented final-volume transformation where applicable.
3. Decide at which existing approval stages to enforce the consistency evidence: product/process package and frozen formula approval; batch creation/release; QA finished-product release. Fail closed when required structured evidence is missing or contradictory.
4. Define tolerance/rounding/conversion rules and who is authorized to approve deviations. Keep genuine QC assay results and independent QA decisions as separate evidence; neither may silently override a contradictory formula.
5. Decide how to handle *historical demo* `20261007-KCL-001`: preserve all original signed records, current QA history and inventory ledger; issue a controlled quality discrepancy record rather than rewriting it or auto-generating a misleading final PDF.

## Proposed bounded implementation after approval

- Update PRD, Domain/GxP rules, state-machine gates, approved master/process/quality plan schema and API/DTO (only if necessary), QA review error codes, eBR evidence references, controlled UI, permissions, tests and RTM together.
- Append any schema change as **new** Flyway version. Never edit applied V001–V032 or already frozen snapshots.
- Add a new evidence digest and reviewed/qualified approval chain if a strength calculation is introduced into Release Gate.
- Make QA Release Gate reject a future batch with an inconsistent/absent approved formula-to-final-strength relationship; never silently convert an existing released batch back to draft.
- Keep current T1–T6 visual templates and unrelated WMS/mes APIs unchanged.

## Required transactional and browser verification

- Matching structured strength, final volume and approved formula → positive approval path with independent reviewer, signed evidence and immutable output.
- Mismatched labeled concentration vs frozen formula → specific gate block, **no new decision, no stock release, no signature**.
- Missing final-volume basis, invalid unit conversion, intermediate dilution without approved process rule → fail closed.
- Altered formula after review / stale digest → reject; old archived evidence preserved.
- Non-QA actor, expired/revoked qualification, self-approval and read-only-role attempts → rejected; allowedActions reflects actual service authorization.
- Original signed historical demo records remain byte-for-byte unchanged; new test fixtures are rollback-only and use unique keys.

Related open findings: `MES_DEMO_STRENGTH_FORMULA_INCONSISTENCY_2026-10-08.md`, `MES_DEMO_ROLE_EVIDENCE_INCONSISTENCY_2026-10-08.md`, `DCP-QA-PLAN-QUALIFICATION-001-PROPOSED.md`.
