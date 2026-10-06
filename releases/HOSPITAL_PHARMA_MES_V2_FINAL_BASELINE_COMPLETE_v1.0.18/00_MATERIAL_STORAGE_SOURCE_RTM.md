# Storage/source maintenance traceability

| Requirement | Database | API | UI | Test |
|---|---|---|---|---|
| MAT-STORAGE-001 | existing md_material.storage_condition | MaterialCreate/Update/Response | T1/T2/T3 | TC-MAT-STORAGE-001 |
| MAT-SOURCE-001 | V028 md_material_supplier.manufacturer_name | existing supplier assignment/read | relationship section | TC-MAT-SOURCE-001 |
| RCV-SOURCE-001 | V028 wms_material_receipt_item.source_snapshot_json; existing lot FK | ReceiptItem / MaterialLot sourceSnapshot | receiving T3 / lot T4 source | TC-RCV-SOURCE-001 |
| UI-MAT-CLEAN-001 | no additional delta | unchanged query/sort/pagination | Material T1/T2/T3 whitelist | TC-UI-MAT-CLEAN-001 |

Permissions, GxP and test acceptance criteria are in the approved contract; existing task boundaries remain unchanged.
