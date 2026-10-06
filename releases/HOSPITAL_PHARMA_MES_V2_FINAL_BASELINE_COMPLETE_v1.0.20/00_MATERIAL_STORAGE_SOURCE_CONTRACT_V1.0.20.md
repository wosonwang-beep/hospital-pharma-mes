# DCP-MATERIAL-STORAGE-SOURCE-001 — APPROVED

Approval: human “授权修改数据库” followed by explicit “推荐每条‘物料—供应商’关系指定厂家，收货冻结来源”, 2026-10-06. Bounded maintenance replaces only Material Master visibility/storage and supplier-source manufacturer contracts. No image, planning, SOP, V3, new MES task, generic status API, permission or signature/release rule change.

## Database / domain

Reuse md_material.storage_condition nullable VARCHAR(500). md_material_supplier.manufacturer_name nullable VARCHAR(200) belongs to the existing materialId/supplierId relationship, not Supplier or Material Master. V028 adds this column and wms_material_receipt_item.source_snapshot_json nullable LONGTEXT with valid-JSON check. Existing PK/FK, org isolation, root optimistic locking, one preferred supplier and retained revocation remain. No backfill: unknown historical source remains NULL. md_material_lot.receipt_item_id is the existing FK lineage; no duplicate mutable manufacturer data on the lot.

## Commands / state / API

Existing closed MaterialCreateRequest / MaterialUpdateRequest accept nullable storageCondition max500 and MaterialResponse returns it. Existing closed supplier-assignment items accept nullable manufacturerName max200; MaterialSupplierResponse returns it. Null/blank means unknown/cleared on a full replacement command. Legacy material manufacturerName/appearance/lotControlled/effective dates remain in old write/read contracts and are preserved by the UI, but hidden from default Material Master presentation.

Draft receipt creation/edit does not freeze source. At CONFIRM / direct RECEIVE, existing material/supplier eligibility is checked, relationship assignment and qualification changes are serialized through existing material/supplier row locks, and a sourceSnapshot is generated on each receipt item in the same audited transaction as the receipt, lot and ledger. It contains string relationshipId/materialId/supplierId, supplierCode, supplierName, nullable manufacturerName, from the selected existing relationship. Missing manufacturer remains unknown, does not fabricate identity or introduce a new quality gate. Confirmation version/actor/time/audit/idempotency remain authoritative metadata. Client ReceiptItemInput cannot write sourceSnapshot. Confirmed receipt is not editable; replay returns original frozen facts.

ReceiptItem read adds read-only nullable sourceSnapshot. MaterialLot read exposes the same receipt-item sourceSnapshot via existing same-org receiptItemId; no independent storage or live master lookup. Existing MaterialQuery/ApprovedSupplierQuery and WMS consumers remain; internal freezeSource adapter is added, no public route. Later master/relationship changes never mutate already frozen source or material snapshots. Receipt supplierId and frozen supplier identity are consistent at confirmation. QC PASS remains separate from QA RELEASED; receipt creates QUARANTINE/BLOCKED as before.

## UI / permission / GxP

UI V2 / T1-T6 retained. Material T1 columns code/name/type/spec/base unit/storage/status/actions. T2/T3 eleven approved fields; existing unit conversion, controlled reason/audit actions remain. Manufacturer only in supplier relationship and frozen receiving/lot source context, never material basics. Supplier-material-code absent: do not invent. Existing update/receipt confirm/view/inventory permissions unchanged. Audits include before/after storage and manufacturer assignment; Receipt CONFIRM and MaterialLot RECEIVE include immutable source. No new signature control or bypass; physical deletion prohibited. Deprecated material fields still round-trip unchanged.

## Tests / RTM / direct integration

MAT-STORAGE-001 -> TC-MAT-STORAGE-001: create/read/update nullable storage, max length, audit/version/idempotency and detached snapshots.
MAT-SOURCE-001 -> TC-MAT-SOURCE-001: relationship manufacturer persistence/replay/stale/qualification/preferred controls.
RCV-SOURCE-001 -> TC-RCV-SOURCE-001: draft unknown, confirm freezes current relationship, subsequent assignment changes retain receipt and lot source, direct receipt path, legacy unknown, same-org reads and audit source; original QUARANTINE/BLOCKED unchanged.
UI-MAT-CLEAN-001 -> TC-UI-MAT-CLEAN-001: T1/T2/T3 field order, hidden payload retained, storage write and source-only manufacturer, no page/console errors.
Affected producers: MES-004/005 and MES-008; consumed MaterialLot/trace contract MES-008A/009/011/013 only gains source evidence, no state/task/dependency redesign. Accepted task statuses preserved; maintenance READY FOR ACCEPTANCE only after targeted gates.

This approved delta governs inherited conflicts in cumulative v1.0.20. v1.0.17 remains immutable history; pointer switch requires recorded cross-document consistency PASS. Functional/UI V2 Closeout records remain historical acceptance, not a new regression claim.

## Concurrent source confirmation

Source confirmation uses locked current reads for material, supplier and relationship. An older transaction snapshot must never supply frozen source facts. A MariaDB current-read conflict blocks confirmation without partial facts; a fresh command transaction must revalidate the current relationship. No silent stale-source fallback is permitted.
