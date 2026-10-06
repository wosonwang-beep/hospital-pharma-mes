# Production Batch T4 — screenshot reference implementation

2026-10-06. Starting HEAD dba1b1e. User explicitly requests Product Design image-to-code and design-qa for the supplied production-batch reference. UI Template: T4. Current business authority remains FINAL BASELINE COMPLETE v1.0.17. Status: READY FOR ACCEPTANCE for existing-contract PC presentation; no human acceptance inferred.

## Implementation

- Reuses the approved compact reference Shell on production batch View only: 180px sidebar / 38px header; Create/Edit and other pages retain their current Shell. The existing navigation component is reused and selects the batch list for a batch detail route.
- Matches the image's header, three-part summary with circular progress, six-node connected lifecycle, six colored module cards, nine information Tabs, notes/files side column. Card boundaries align to the reference at 1280×853; original source and paired full/focused comparisons saved.
- Preserves parent-owned controlled commands, edit constraints, allowedActions/permissions, execution-unit navigation, quality and material-balance source routes, original operation start/completion columns, sub-batch information, frozen snapshot and Trace/Audit entrances. Additional technical evidence is in the More menu/drawer. Returning to the list preserves route query parameters.
- Corrects the shared generic OPEN label collision: an unfinished production order is no longer labelled 待调查. The source state remains unchanged.

## Source mapping and limitations

| Display | Existing source | Interpretation |
|---|---|---|
| Batch identity / plan / actual dates | GET /main-batches/{id} | Original facts, UTC actual times |
| Product / unit / order | Existing /products, /units, /production-orders reads | Permission guarded |
| Executions and operation progress | Existing execution-unit and operation reads | COMPLETED count / read count; never current-step index |
| Material preparation | Existing batch reservations | Reservation records, not fabricated fulfillment percentage |
| Quality status | Existing QA review-model PRODUCTION_QC Gate | Gate satisfaction, distinct from QA material/finished release |
| Material balance | Existing material-balance read | Latest calculation per rule; original result preserved at source |
| eBR / PDF / deviations / QA dates | Existing batch eBR archive | Existing facts only; source permissions retained |

The screenshot contains product photography, planned finish, workshop/line, responsible person, editable remarks, SOP and arbitrary uploaded file examples that the current batch contract does not provide. No fields/APIs/tables or invented facts are introduced to mimic those examples. A library document icon identifies the batch; it is not a product image. Reference packaging is retained only in the comparison artifact, never used as runtime production data. Full-image 100% identity is not claimed. The separate pending functional-gap proposal remains pending; this task does not approve it implicitly.

## Validation / review

- Fresh typecheck PASS; final build includes vue-tsc and PASS,11.43s. Inherited LOW chunk-size warning retained.
- Three distinct targeted Chromium PC scenarios passed across scoped gates: new reference/source/permission/action case (final13.5s), existing Batch T4 source/snapshot/overflow regression (final11.7s), and T5 shared Shell/operation/IPC-context navigation regression (PASS in the20.3s focused group). No full regression, native DB test or live-data acceptance claimed. Browser renders actual Vue with existing API fixtures.
- New case verifies actual4/6 operation progress,2/3 execution completion, latest balance version, six lifecycle nodes, unfinished order and no QA release, no phantom PDFs, all nine Tabs, quality source view, controlled production command ownership, permission-filtered source reads, no page/console errors or page-level horizontal overflow.
- Design QA inspected original/current images, full paired comparison and focused paired comparison. Initial density drift and hidden related-batch tab were corrected and recaptured. Scoped remaining CRITICAL/HIGH:0. No actionable scoped P0/P1/P2 remains. P3: source font rasterization/icon shape and two-pixel sidebar difference; contract exclusions above are explicitly outside pixel-identity scope.
- Backend/API/DTO/database/migration/state/permission/allowedActions/signature/FINAL BASELINE changes: NONE. Inspection record/report and T5 structure retained. Local DB config changes preserved. No commit/push performed.

## Evidence

- [Final PC screenshot](batch-pc-1280x853.png)
- [Full screenshot](batch-pc-full.png)
- [Full same-size comparison](comparison.png)
- [Focused comparison](comparison-detail.png)
- [Reference](reference.png)
- [Gallery](REPORT.html)

The final basic-information screenshots remain valid after restoring the operation-time columns in the inactive execution Tab; that Tab was independently rendered and verified in the last direct regression.
