# QA page visual verification

Source visual truth: C:/Users/Administrator/.codex/codex-remote-attachments/01a0fec2-ada4-7e03-be89-85a3db77f1c9/C465CBCB-3C2F-42BC-8622-BC0611859E2F/1-照片-1.jpg
Implementation: docs/acceptance/mes-013/ui-refinement-2026-10-05/qa-desktop.png and qa-mobile.png.
Full-view comparison: docs/acceptance/mes-013/ui-refinement-2026-10-05/comparison-full.png.
Focused content comparison: docs/acceptance/mes-013/ui-refinement-2026-10-05/comparison-content.png.

Viewport: desktop1280×853 CSS px; mobile393×727 CSS px. Source1280×853 pixels; implementation full-page1280×1085 /393×1903 pixels. Screenshots use CSS scale (one pixel per CSS px); no comparison resizing. Mobile has no supplied source mock, so checked responsive usability rather than claiming mobile pixel fidelity.
State: PENDING_QA, six server Gates passed, FINAL pending. Browser fixture supplies three REVIEW_COPY archives and real-shaped raw evidence. Data counts differ intentionally from the illustrated34 records. Actual Gates exclude post-QA FINAL archive, unlike the source illustration.

## Comparison history and findings

1. P2: oversized rows pushed lower panels down; reduced Gate padding/line height/gaps, compacted descriptions and table actions, paginated directory at5 categories. Final visual evidence shows compact single-column checklist, right summary/actions and paired lower panels.
2. P2: archive action could fall outside the narrow table; fixed download column on right. Final desktop/mobile captures show download control accessible while remaining columns scroll internally.
3. Accessibility regression: decorative lock changed exact button name; explicit Chinese accessible name and hidden decorative icon restore the original contract.

No remaining actionable P0/P1/P2 in the authorized page scope.

## Required fidelity surfaces

- Typography: existing Chinese font stack and22px page heading retained;13px checklist titles,11–12px supporting text, compact line heights match the reference hierarchy.
- Spacing: single-column rows,16px section gaps, right stacked cards and lower paired cards follow source composition. Existing268px navigation/global shell is outside scope; source uses a narrower/light shell, so full-page offsets intentionally differ.
- Colors: existing blue tokens plus green passed state, amber pending/warning, white cards and pale backgrounds. Application shell unchanged.
- Assets: Ant Design icon library; no fabricated product photo, avatar or business values. Product photograph in reference lacks a supported business source and is omitted.
- Copy/content: existing frozen DTO facts only; raw state retained for provenance, Chinese state shown in banner. No invented size/status/name fields. FINAL cannot be generated before QA decision.

## Verification and limitations

Typecheck PASS;14 direct existing API-mocked browser cases PASS; final2 responsive/filter/drawer cases PASS, no captured page errors. Forms checked for horizontal labels in existing signed-release/PDF flows. No database writes or native backend rerun.
IAB unavailable; repository Playwright captured mocked page evidence. Chrome preview reached normal login at http://127.0.0.1:5173/qa/batches/41/release, without authentication bypass. Local Vite server retained for user login/preview.

Checklist: layout adjusted; original facts accessible; signatures and controlled actions preserved; responsive capture reviewed; download visible; no backend/migration changes.
P3: full product names and imagery remain unavailable from current DTO; no contract extension is implied.

final result: passed

## T3 inspection record reference review — 2026-10-05

Source: C:/Users/Administrator/.codex/codex-remote-attachments/01a0fec2-ada4-7e03-be89-85a3db77f1c9/946A87DD-4F8C-4A18-8195-2A5DDF02ED3F/1-照片-1.jpg
Scope: inspection record content only; existing T3, Global UI V2 shell and frozen business contracts retained.
Captures: docs/acceptance/incoming-quality/t3-inspection-visual-2026-10-05/inspection-preview-chromium-desktop.png and inspection-preview-chromium-mobile.png; item drawers captured beside them.
Source and desktop viewport: 1280x853, no image resizing. Mobile: Pixel 5 393x727 CSS px, device scale 2.75; no mobile reference supplied.

Matched visual surfaces: inline formal number/status; six-column desktop business summary; compact results table with pale red failed row; conclusion/review strip; collapsed evidence card; existing blue/neutral typography and spacing; installed Ant icons. Mobile uses two-column facts and internal table scrolling without document overflow.
Intentional differences: global navigation unchanged; one actual mocked item rather than three illustrated items; reviewed state from read model rather than illustrated pending state; UTC dates and exact numeric precision retained; unsupported print/return buttons omitted. Raw FAIL remains visible even when approved invalidation changes effective conclusion. All separate execution results remain distinguishable; complete methods, instruments, raw observations, revisions and signature evidence remain accessible.
Iteration: corrected scoped-selector leakage discovered by inspecting the desktop/mobile captures, then recaptured and reran affected browser checks. Final page has normal body typography and no global background/padding leakage.
Validation: npm run typecheck PASS; 3 affected unit suites / 15 tests PASS; npm run build PASS (existing bundle-size warning); 34 targeted Chromium desktop/mobile read-detail and controlled-action tests PASS (38.4s). Detail tests assert no console/page errors and document horizontal overflow; item/evidence/signature drawers and audit routing verified. API fixtures are contract-shaped mocks, not persistent database acceptance evidence.
No backend, API, permissions, state machine, migrations or baseline changes. No remaining CRITICAL/HIGH presentation issue observed in this scope.
final result: passed

## T3 inspection report reference review — 2026-10-05

Source: C:/Users/Administrator/.codex/codex-remote-attachments/01a0fec2-ada4-7e03-be89-85a3db77f1c9/0D4E8EF1-E008-46AD-9138-59CC71256435/1-照片-1.jpg
Scope: T3 inspection report presentation only, FINAL BASELINE v1.0.17 incoming chain contract; existing shell retained.
Captures: docs/acceptance/incoming-quality/t3-report-visual-2026-10-05/inspection-reports-chromium-desktop.png and inspection-reports-chromium-mobile.png; expanded report evidence beside them.
Comparison: source 1280x853; desktop viewport 1280x853 with full-page capture; mobile Pixel5 393x727 CSS px at device scale 2.75, no supplied mobile reference. No reference resizing.
Matched: document icon/header/no/conclusion, two-column horizontal label/value facts, compact selected-result table, semantic green conclusion panel and review/approval facts. Existing 13–14px body/20–22px title hierarchy and UI V2 cards/tokens preserved. Mobile labels remain horizontal and table scroll stays inside its card.
Intentional contract differences: actual sample/item counts and exact decimal precision retained, UTC execution completion timestamps displayed; review and approval shown separately; no remarks field exists in report DTO, so none invented; unsupported print/PDF omitted; already approved reports have no approve action. Original FAIL, investigation references, methods, revisions, signatures and record metadata retained in expanded evidence. No result calculation or editable report values introduced; source standard comes from immutable inspection-item snapshot, not latest specification.
Iteration: compressed summary links/card padding after desktop capture review; scoped test assertion to the report table to distinguish selected result from its retained hidden evidence. Fixture preparer/reviewer/approver distinct. Final screenshots reviewed for desktop/mobile layout.
Verification: build including vue-tsc PASS; 32 direct detail/action regression browser cases PASS; 2 report desktop/mobile cases PASS after selector fix and final recapture. Report cases verify selected result in table, original FAIL preserved in expanded evidence, audit route, zero page/console errors and zero document horizontal overflow. Fixtures use existing contract read models; this is frontend validation, not persistent database acceptance.
Business contract, API, DTO, database, migration, permissions, allowedActions, signature rules and baseline unchanged. Existing bundle-size warning remains. No CRITICAL/HIGH presentation finding remains within this scope.
final result: passed

## Production T5 + report T3 reference review — 2026-10-05

Sources: C:/Users/Administrator/.codex/codex-remote-attachments/01a0fec2-ada4-7e03-be89-85a3db77f1c9/0DA7AD44-E5A8-456F-A21F-6E5E3B66BA64/1-照片-1.jpg (1280x720) and 2-照片-2.jpg (1280x853).
Captures: docs/acceptance/ui-reference-2026-10-05/workbench-chromium-desktop.png, workbench-chromium-mobile.png, inspection-reports-chromium-desktop.png and inspection-reports-chromium-mobile.png. Desktop viewports match respective source dimensions; full-page captures preserve scrollable content. Mobile uses existing Pixel5 viewport, no supplied mobile reference. No source resizing.

T5 follows source composition: batch/current operation/progress context, horizontal step navigation, left operation list, central tabbed execution/material/equipment/parameter/IPC-clearance/eBR workspace, right operational controls, bottom charge and parameter tables. UI V2 typography/colors/cards reused. Pending status has Chinese presentation; parameters preserve manual/device provenance; progress counts completed operations, not the ordinal of the active operation. Current selection only changes presentation. Original start/pause/resume/complete and weighing/charge command owners, signing and concurrency behavior retained. Full operation, charge, gate and signature facts remain accessible in evidence disclosures. Existing permission-gated product/unit/lot reads resolve business names, falling back to original identifiers when unavailable.
T3 report keeps prior reviewed layout and now matches green header conclusion styling; original FAIL and selected results remain distinct, evidence and approval facts retained. No fabricated print/PDF action or remarks field.
Intentional differences: existing shell untouched, no unprovided operational photo, expected times or assumed normal parameter verdict. Real record counts, timestamps/precision, authorization and QA separation take precedence over illustrated sample values. IPC/clearance uses existing controls rather than an invented initiation API. Screenshots are contract-shaped browser fixtures, not production database evidence.

Review iterations: compacted central facts while retaining full evidence; made grid tracks shrink safely so cards do not intrude beneath the sidebar; fixed existing mobile reference-picker label alignment locally to the execution workbench; corrected Ant auto-spaced Cancel test locator. Reviewed final desktop/mobile captures. Mobile step navigation and tables scroll internally; no document overflow.
Validation: build including typecheck PASS; 10 targeted Chromium desktop/mobile cases PASS (22.0s), covering T5 navigation/command form, batch release, independent signed weighing retry, charge concurrency/trace and report original-result evidence. Final T5 recapture 2 cases PASS (13.1s) after bounded grid/unit display adjustments. Tests assert no page/console errors. No full regression, backend or persistent database tests run for this presentation change. Existing bundle-size warning remains.
Business/API/DTO/state machine/permissions/allowedActions/signature/baseline/database/migrations unchanged. Existing local DB configuration edits preserved. No CRITICAL/HIGH visual issue remains in the authorized scope.
final result: passed

## MaterialLot T4 reference review — 2026-10-05

Source: C:/Users/Administrator/.codex/codex-remote-attachments/01a0fec2-ada4-7e03-be89-85a3db77f1c9/2A0AD7E0-0B98-49D5-B0B2-C69E1D6DD962/1-照片-1.jpg, 1280x853.
Captures: docs/acceptance/material-lot-t4-visual-2026-10-05/lot-overview-chromium-desktop.png and lot-overview-chromium-mobile.png. Desktop viewport matches source dimensions, full-page capture; mobile existing Pixel5 CSS viewport, CSS-scale captures. No resizing or mobile source available.
Scope: existing MaterialLot T4 page and subordinate trace presentation. Summary shows actual material snapshot, lot and distinct quality/inventory states. Overview groups six formal record types with source references, actual status and permission-gated detail routes. All existing sampling/sample/result/report/release/inventory/production/audit tabs retained. Each group includes every returned source record, never silently chooses one as effective. Navigation does not mutate business data.
Source fidelity: header/identity, compact summary, six-stage relationship strip, lightly tinted six-card overview, business/batch facts and secondary actions; existing Global UI V2 shell/font hierarchy/spacing preserved. Mobile cards use two columns and bounded tab/table scrolling.
Intentional contract differences: graph does not provide record business dates, receipt quantity or supplier name, so no illustrated values invented; relation count is not a completed Gate. Unknown status remains explicit. Historical failed inspection is not replaced by a passing report or current released lot. English name/CAS/image/print/export absent from existing frozen contracts and are omitted. Material labels reuse existing master field definitions; historical unit name comes from frozen material snapshot.
Review: removed programmer field labels using existing master metadata, retained unavailable required-inspection snapshot as unknown rather than falsely displaying No, reviewed final responsive screenshots. No current CRITICAL/HIGH visual finding observed in authorized scope.
Verification: build including typecheck PASS; 6 targeted desktop/mobile cases PASS (13.8s): T4 lineage and deep-linked sections, denied trace permission causes zero graph requests, signed inventory freeze preserving reason/password/concurrency/signature behavior. Final two T4 recaptures PASS (13.2s) after read-label fixes. Browser fixtures prove UI, not production database acceptance. Console/page errors and document overflow checks PASS. Existing chunk-size warning remains.
Database/migration/backend/API/DTO/permissions/routes/state machines/signature/allowedActions/final baseline unchanged. Prior unrelated work and local DB config preserved. No commit/push.
final result: passed

## T5 PC-only reference refinement — 2026-10-05

Source: C:/Users/Administrator/.codex/codex-remote-attachments/01a0fec2-ada4-7e03-be89-85a3db77f1c9/0DA7AD44-E5A8-456F-A21F-6E5E3B66BA64/1-照片-1.jpg (1280x720).
Captures: docs/acceptance/execution-workbench-pc-refinement-2026-10-05/workbench-pc-1280x720.png (viewport) and workbench-chromium-desktop.png (full page).
Scope: PC production execution T5 only. No mobile development or validation. Existing Global UI V2 sidebar/header dimensions retained; production breadcrumb moves into header only on this desktop route. Other routes retain their shell.

Compared source and final Chromium capture at the same 1280x720 viewport. Matched content structure: T5 title/toolbars; paired batch/current-operation context; six-step horizontal navigation; aligned narrow step list / execution tabs / action panel; compact horizontal facts; paired material and process parameter tables. Corrected excessive height from grid stretching after inspecting actual browser geometry. Primary workspace height is checked, columns cannot overlap, and document horizontal overflow is checked. Bottom table content now appears within the PC viewport. Complete evidence remains accessible via the record drawer and frozen instructions via the header drawer.

Known fidelity gaps: reference operational photo has no source field in the existing execution/charge read model; actual frozen instructions occupy that area instead. Planned step times and the illustrated normal quality verdict are not invented. Progress counts completed operations (1/6 in the fixture), not the current step index. Parameter provenance remains visible instead of manufacturing a normal verdict. Record-deviation button is visibly disabled with an explanation because the present operation contract has no production-deviation creation action. The supplied reference's 180px sidebar/38px header differs from approved Global UI V2's 228px/56px targets, which remain in force. These differences prevent claiming literal 1:1 fidelity.

Validation: npm run typecheck PASS; final npm run build PASS (9.48s, existing bundle-size warning). Six distinct targeted Chromium desktop scenarios PASS across affected runs: operation navigation/completion form/evidence/instruction drawers; signed IPC with exact binding; production batch list/detail routing and label alignment; batch release contract; independent signed weighing retry; operation-version charge and trace. Final T5 capture/interaction test PASS (11.0s). Corrected existing stale T1 test selector and missing production-order lookup fixture without changing assertions. No full regression, backend or persistent database tests. Fixtures prove UI interactions, not production database acceptance.

Database/migrations/backend/API/DTO/business state machine/permissions/signature rules/final baseline unchanged. Unrelated working-tree changes preserved. No commit/push. PC layout checks: passed. Full reference fidelity: blocked on the listed source/contract gaps; not ready to claim exact-image acceptance.
final result: blocked

## T5 supplied-design PC implementation — 2026-10-05

Approval: user explicitly instructed “按上面发的界面设计进行修改，不得另外设计”, following the PC-only request. Bounded scope: supplied production execution T5 screenshot; existing contracts and other business pages retained. This replaces the prior placeholder-based layout review for this screen only.
Source: C:/Users/Administrator/.codex/codex-remote-attachments/01a0fec2-ada4-7e03-be89-85a3db77f1c9/0DA7AD44-E5A8-456F-A21F-6E5E3B66BA64/1-照片-1.jpg.
Captures: docs/acceptance/execution-workbench-reference-match-2026-10-05/workbench-pc-1280x720.png and workbench-chromium-desktop.png. Source and Chromium viewport both 1280x720. Source and final capture viewed together in the same comparison input; no resizing or generated concept used.

Implemented source layout: scoped 180px sidebar / 38px header, compact grouped existing authorized navigation, existing user controls in a dropdown, source T5 heading and toolbar, paired production/progress cards, horizontal six-step navigation, 201/600/250px approximate workspace tracks, six tabs, horizontal charge facts, original supplied photo and view-large control, four-button action stack, paired material/parameter tables, and supplied hospital footer artwork. Removed the separately designed frozen-instruction placeholder. Assets extracted from supplied screenshot, not generated or recreated. Photo is explicitly labeled design illustration/non-site record and the preview explains its source; it is never stored as charge evidence or referenced in an audit/signature payload.

Visual comparison: primary workspace starts at y304, height268; support tables start at y580. Browser assertions check these source-derived coordinates (2px tolerance), sidebar/header dimensions, card alignment/non-overlap, no document horizontal overflow, exact six tabs, original asset presence and absence of the prior placeholder. Final matched desktop layout reviewed. Permission-filtered menu labels and data remain actual existing routes/read-model facts. No unauthorized placeholder menu/page or independent business data is added. Unavailable method/parameter-status facts remain “—”; actual UTC times, confirmed charge state, completed-operation progress/count and record counts are not copied from illustrative values. Deviation creation remains disabled with its contract explanation. Global search is visually present but disabled because no existing global-query contract exists. IPC button opens the existing controlled IPC workspace.

Controlled actions/evidence: completion retains original owner/schema/version/reason flow; existing IPC signature binding unchanged. Full operation/charge facts, device/manual provenance and signature evidence remain accessible under Batch Record → Complete Record/Audit Evidence; frozen instructions retain execution/snapshot identity. No state, quantity, gate or original result is overwritten. Other routes retain Global UI V2 shell dimensions. No mobile adaptation/test, Phase 2 continuation or new MES task.

Validation: npm run typecheck PASS; final npm run build (includes typecheck) PASS, 11.92s; existing bundle-size warning remains. Three targeted Chromium desktop cases PASS (14.1s): T5 navigation/completion/evidence/photo/instruction preview; signed IPC exact binding; production list/detail and unchanged non-T5 shell path. Final T5 geometry/capture/interaction recapture PASS (13.4s). Console/page error assertions PASS, no horizontal overflow. Mock API fixtures prove presentation/interaction, not persistent database acceptance. No unrelated full regression or DB test.

Review: no CRITICAL/HIGH visual or command regression observed in this scope. Literal illustrative data and unsupported business operations are not part of visual acceptance. Static photo annotation deliberately prevents its interpretation as regulated evidence. Business/API/DTO/state/permission/signature/database/migrations/final baseline unchanged. Unrelated edits and local DB configs preserved; no commit/push. Ready for user visual review.
final result: passed

## Pre-push verification — 2026-10-05

Scope: accumulated authorized T3 incoming detail/report presentation, T4 MaterialLot trace view and supplied T5 PC execution design, tests, assets and screenshot evidence. Fresh targeted verification: 2 affected unit suites / 8 tests PASS (2.85s); six affected Chromium desktop specs / 29 cases PASS (43.1s); npm run build including vue-tsc PASS (19.10s, existing chunk-size warning). Final EOF whitespace normalized only; no executable change after verification. Native database untouched. Root .env and workstation application-local.yml/application-ci.yml changes explicitly excluded from the commit. No task-status or frozen-baseline change.
