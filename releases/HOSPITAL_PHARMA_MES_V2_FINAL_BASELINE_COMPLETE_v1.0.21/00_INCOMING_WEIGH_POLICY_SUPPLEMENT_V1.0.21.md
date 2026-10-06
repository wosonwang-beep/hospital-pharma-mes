# Incoming weighing policy completion — publication input

2026-10-03. This bounded supplement resolves the remaining implementation ambiguity under the human-approved full incoming contract-completion scope. It is not effective independently: root must include it in the next cumulative release and pass cross-document review before affected behavior is enabled. v1.0.11 remains immutable.

## Exact policy

Every actual WeighingRecord created in this scope must receive independent verification with an existing-platform VERIFY electronic signature before it can support a MaterialCharge. The recorder cannot verify their own weighing. POST `/weighings/{id}/verify` requires both `mes:weigh:verify` and `ebr:sign`, a current version, reason and `reauthToken`. This concretizes the existing verification/signature point; it adds no CREATE or charge signature point. A material whose frozen `weighingRequired=false` may still follow the existing permitted direct-charge path without creating a weighing, but if a weighing is supplied it must be VERIFIED and carry valid verification evidence. `criticalMaterial` is not a substitute verification policy.

No new material property or mutable qualification dictionary is introduced. All actual weighings use the same explicit mandatory rule, so there is no default-false verification/signature configuration and no client-authored `verifiedBy`, policy flag or signature ID. Failed independent verification, credential consumption, signature creation, evidence persistence or audit rolls back the business transaction. Reauthentication is consumed on the first signing attempt after replay is ruled out and remains consumed if a later business write rolls back; successful idempotent replay consumes no new token.

## Scale deployment binding and evidence

`mes.weighing.scale-equipment-types` is a nonempty list of exact existing `md_equipment.equipment_type` values. The controlled deployment operator chooses real equipment type values; there is no invented hard-coded SCALE enum. Missing, empty or blank configuration fails closed with `WEIGHING_POLICY_REQUIRED`. The chosen equipment must have one of those exact types, be ACTIVE and have non-null `calibrationDueDate` valid at weighing time under the existing equipment validity rule (due date before current UTC date is expired). A usable reactor or uncalibrated unspecified instrument cannot pass because the request field happens to be named scaleEquipmentId.

Persist the weighed fact's existing approved `equipment_evidence_json` with equipmentId, versionNo, equipmentType, status, calibrationDueDate, checkedAt plus policy evidence `{policyVersion:"INCOMING-WEIGH-1", independentVerificationRequired:true, signatureRequired:true, scaleEquipmentTypes:[...sorted distinct exact codes], configurationHash}`. The hash is the canonical digest of that complete policy object excluding its own configurationHash. No new equipment or material master column is required. Existing evidence JSON is immutable consumed history; subsequent configuration or equipment changes do not rewrite it. Verification consumes the persisted policy/equipment evidence and rechecks current actor qualification and material eligibility; it does not replace the historical scale evidence with the latest master state. Command audit binds the full fact digest.

## Stable signature identity

The existing v1.0.11 target is unchanged: objectType `WEIGHING_VERIFICATION`, objectId `{weighingId}:VERIFY:{expectedVersion}`, recordVersion `expectedVersion`, meaning `VERIFY`. The transaction-scoped intent contains the post-verification business fact and its exact evidence before the aggregate update. Persist the exact original canonical payload/evidence IDs and returned signatureId in `signature_evidence_json`. Later generic verification resolves that immutable envelope by the complete action-specific objectId, even after charge increments the weighing root version. Do not reconstruct an old signature using mutable current data, or include its own signatureId in its canonical payload.

## API, UI and tests

- `ProductionCommand.reauthToken` becomes required for the weighing verify operation; other command request contracts retain their existing requirements. Weighing CREATE and charge CREATE still have no reauthToken.
- `EquipmentEvidence` read schema gains the immutable policy object above. No route or permission is added; weighing workbench shows the existing independent verifier and controlled reauthentication dialog, never an unchecked "already verified" control.
- Charge rejects CONFIRMED/unverified weighing, missing/invalid signature, wrong actor verification and signature envelope tampering. Successful charge remains Charge + QuantityEvent + CONSUME ledger + Genealogy + Audit in one transaction.
- Required tests: valid real configured scale, absent/blank/wrong-type configuration, missing/expired calibration, same-actor rejection, missing signing permission, invalid credential, correct signed verification, successful replay without a second token, post-signature transaction rollback with consumed token, changed config preserving old evidence, valid old signature after a later root-version increment, and charge rejection of an unsigned or tampered weighing.

Publish consistent Database evidence shape, Domain invariants, API/OpenAPI verify request/read schemas, UI fields/actions, GMP/signature controls, Test/RTM and integration contracts together. No executed migration edits or DEV data reset are required; only the new unexecuted MES-011 evidence persistence uses the clarified existing JSON columns. Task states remain in MES_TASKS.md.


## Approved functional closure delta — v1.0.21

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.21.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.21.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.
