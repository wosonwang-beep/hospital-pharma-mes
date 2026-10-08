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


## MaterialLot T4 reference refinement — 2026-10-06

Source: user attachment817E0F56-F2CD-406D-AD29-70D0916FE0D4/1-照片-1.jpg (1280×853).
Implementation: docs/acceptance/material-lot-reference-2026-10-06/material-lot-desktop.png.
Combined comparison: docs/acceptance/material-lot-reference-2026-10-06/comparison.png.
Viewport1280×853 CSS px/device scale1; full-page capture retains existing regulated facts. Fixture state is different from illustration; frontend browser verification, not live database acceptance. Existing authorized Playwright Chromium used.

Initial P2 findings: lifecycle below tabs rather than summary, count labels without connecting markers; different object icon/hierarchy; oversized fact spacing and missing three-column bottom grouping.
Fixes: cube object identity, lifecycle in summary, existing receipt quantity/date/supplier reference matching receiptItemId; connected stages with completion derived from node statuses; compact six colored source cards with existing dates/statuses/deep links; aligned facts and honest image availability panel. Existing inventory controls remain in Inventory tab. Original FAIL independent of report/release is preserved. Post-fix screenshot checked after correcting fact spacing.

Remaining exclusions: frozen global Shell (228px sidebar/56px header) retained; permission-driven original actions retained rather than introducing Print/Export. Deleted English name not restored, unsupported CAS/photo/release timestamp not fabricated. Existing additional lot metadata retained. Exact1:1 whole-page reproduction is not claimed.

Typecheck PASS; final targeted Chromium desktop2 tests PASS10.8s, covering source tabs, permission/no trace requests, summary/time/quantity, no overflow/console/page errors. Build PASS9.66s; inherited LOW chunk-size warning. No backend/API/DB/migration/permission/signature/frozen baseline change. PC-only scope. No scoped CRITICAL/HIGH finding.

final result: passed (authorized PC presentation scope with documented contract exclusions)


### MaterialLot flow-node follow-up — 2026-10-06

User requested the reference's connecting lines, completed markers and dates. Replaced isolated arrow icons with long continuous blue connectors ending in library arrows. Dates sit below the marker/title, all six stages remain horizontal on PC. Completed markers derive from existing statuses, not record existence. An existing permission-guarded QA release-review read resolves each trace decision by its id to obtain the real decisionAt; missing/unreadable dates remain explicit. No API/DTO/permission/state contract change.
Evidence: docs/acceptance/material-lot-reference-2026-10-06/material-lot-timeline-desktop.png; focused source/implementation comparison flow-comparison.png. Same1280×853 Chromium viewport. Six completed markers, five lines and sixth stage date verified with existing-contract fixtures. Typecheck and2 desktop cases PASS10.8s, no page/console errors or overflow. Build PASS, inherited LOW chunk-size warning. Visual comparison: no scoped P0/P1/P2 finding.
final result: passed


## Production Execution T5 image-to-code — 2026-10-06

Source:46F18B58-1B7B-4BEC-BFA8-8535927A9023/1-照片-1.jpg. Source and Chromium screenshot both1280×720 pixels, deviceScaleFactor1/CSS scale, operation2 IN_PROGRESS. Current source screenshot inspected before editing; same-size final combined comparison and focused core-workspace comparison inspected after changes. Saved evidence: docs/acceptance/t5-reference-2026-10-06/workbench-pc-1280x720.png, comparison-1280x720.png, comparison-workspace.png. Actual Vue with existing-contract fixtures; no backend/data acceptance claim.

Initial findings P2: isolated navigation arrows instead of connected lifecycle; no left step connectors; old workspace y304/height268 vs reference y298/height254; lower region y580 vs560; one-row fixture did not expose multi-record density; no existing-material evidence entry. Corrected all within the T5 template. Multiple charges now have one selected primary record, with all original evidence retained in material tab/drawer and table View selecting context. A stricter first-screen assertion exposed table bottom713px; reduced lower card padding and passed unchanged <=710px assertion. Final support rows fully visible.

Required fidelity surfaces:
- Fonts/typography: inherited V2 UI font stack,20px title,12–13px operational content; no new font dependency. Label/value alignment checked in focused capture. Original font file is not supplied; exact glyph pixel identity not claimed.
- Spacing/layout: shell's existing authorized T5 reference variant180px/38px retained; content x191, workspace y298/minheight254, lower panels y560, three columns204px/flexible/252px at1280. Connected stages, active rings and row rhythm inspected. No clipping or horizontal overflow.
- Colors/tokens: existing blue primary, green completion, neutral pending; shield from installed icon library. Completion/parameter status remains truthful; no fabricated “normal”.
- Image/asset fidelity: existing supplied hospital design mark retained; library icons retained. No fake production photo. Formal blueprint§9 explicitly prohibits static illustrative photos without a photo Contract; photo and enlarge control omitted as a contract exclusion.
- Copy/content: headings/tabs/order match reference. Actual times replace unsupported planned intervals. Completion progress counts COMPLETED, not current step index, so1/6 vs screenshot2/6 intentionally preserved. CONFIRMED remains confirmation, not invented completion. Unsupported deviation action/SOP method hidden or unresolved per existing contract; menus and permissions preserved.

Validation: targeted desktop scenario PASS11.6s, verifies tabs/geometry/count3/selected charge/full evidence/control ownership/pending operation prohibition/instruction drawer, zero console/page errors and overflow. Typecheck PASS; Build PASS9.71s (inherited LOW bundle warning). No backend/API/DTO/database/migration/permission/state/signature/baseline change. PC-only; no mobile implementation.

No actionable scoped P0/P1/P2 visual or interaction finding remains. Expected business-contract exclusions listed above mean this is not an exact full-image copy. New functionality/contracts for photo/deviation/scheduling would require DESIGN CHANGE REQUIRED.
final result: passed (existing-contract PC layout scope; full-image100% fidelity not claimed)

T5 direct regression: existing frozen IPC result/signature binding Chromium desktop case PASS9.7s. No full regression.

Pre-commit validation2026-10-06: fresh typecheck PASS; five affected Chromium desktop cases PASS14.3s (MaterialLot chain/trace permission, signed inventory freeze in Inventory tab, T5 selected-record/evidence/commands, frozen IPC signature binding). Existing inventory test navigation updated for owning tab, assertions unchanged. No full regression.


## Production Batch T4 reference — 2026-10-06

Target: user attachment700456C0-FBBC-46F7-8059-6E9CBB346C0A; actual Vue route /production/batches/{id}. Both source and capture1280×853, deviceScaleFactor1. Production-in-progress, production-admin context, Basic Information Tab. Actual records differ from illustrative source figures; no pixel-error percentage or identical business-state claim.

**Findings and iteration**
- [P2, fixed] First capture pushed lifecycle/module cards and the bottom information panel down by roughly30–95px. Replaced inherited broad padding with scoped card/heading/row dimensions. Recapture aligns summary147px, lifecycle296px, modules442px and lower panel622px to the reference region rhythm.
- [P2, fixed] Additional source-evidence Tabs plus inherited tab gaps hid the reference Related Batch Tab. Kept exactly nine business Tabs; moved frozen snapshot/QA evidence to existing source navigation and a More drawer; corrected sibling-gap selector. Final Chromium proves Related Batch visible.
- [P1, fixed] Generic OPEN dictionary labelled an unfinished production order as 待调查. Contextual 未完成 presentation now preserves its source status and does not fabricate a completed lifecycle node.
- [P3, remaining] Reference sidebar182px versus reused approved compact Shell180px; font rasterization and nearest installed library icon forms differ. No unrelated Shell redesign/new font system.

**Five required fidelity surfaces**
- Typography: inherited V2 system/Segoe UI/Microsoft YaHei stack;22px/700 heading,16px product name,12px small business text; normal and emphasized facts checked in paired crops. Original screenshot font binary unavailable; exact glyph identity not claimed.
- Layout rhythm: original and rendered images opened; reference and implementation joined in the same full comparison input, plus a focused paired summary/lifecycle/modules input. Three summary columns, connected six nodes, six cards, two-to-one lower split, nine Tabs and compact basic rows inspected. No clipping, overlapping card tracks or page-level horizontal overflow at the target PC viewport.
- Colors/tokens: quiet white cards, neutral blue-gray canvas, thin borders,8px corners, blue primary and source-driven green/neutral/orange states. Library arrows and completion marks follow source truth. No false green QA release or copied sample progress.
- Image quality/assets: existing supplied hospital mark and installed icons reused. Reference product photograph is catalogued but excluded from runtime because no approved batch product-image contract exists; the document icon denotes the record, not a photographic substitute. No generated/fake manufacturing or product evidence. Saved reference image is comparison-only.
- Copy/content: matches aggregate heading and section/Tab hierarchy. Actual COMPLETED operations yield4/6 (67%) instead of illustrative3/5 (60%); reservation record count is not converted into fabricated8/10 fulfillment; balance is current-rule pass count, not invented98.5% yield. Unsupported plan finish/workshop/person/remarks/upload fields explicitly excluded under v1.0.17. Core source actions work and remain permission/state controlled.

**Implementation and interaction checks**
Reference case final PASS13.5s; Batch T4 source/operation-time/snapshot regression final PASS11.7s; T5 shared navigation/command ownership regression PASS in the focused20.3s group. New case checks counts/latest versions, all nine Tabs, quality source, permitted command owner, permission-filtered read APIs, no console/page errors or overflow. Typecheck and final build PASS11.43s. API fixtures validate presentation; no live database acceptance inferred. PC only. Scope excludes new business contracts and full-image identity; no remaining actionable P0/P1/P2 within that scope.

Evidence: docs/acceptance/batch-reference-2026-10-06/reference.png, batch-pc-1280x853.png, comparison.png, comparison-detail.png, REPORT.md / REPORT.html. The overview capture is unchanged by restoring date columns in the inactive Execution Tab; that Tab was separately verified in the last regression.

final result: passed (existing-contract PC presentation scope; full-image100% identity not claimed)

## Login and Home Workbench reference implementation — 2026-10-07

User-selected references: `C:/Users/ADMINI~1/AppData/Local/Temp/codex-clipboard-69dd5a55-3a3c-4d1e-9747-dbb0634279c6.png` (login, 384×198) and `C:/Users/ADMINI~1/AppData/Local/Temp/codex-clipboard-72cab86a-0e5d-4c8e-8705-ef52a38bed90.png` (home, 1536×1024). These are the selected design, not a new ideation direction. Existing Specialized pages, as catalogued in the approved blueprint, remain Specialized; no T7 introduced. FINAL BASELINE COMPLETE v1.0.23 and shared permission-filtered navigation remain authoritative.

Evidence directory: `C:/Users/Administrator/.codex/artifacts/login-dashboard-2026-10-07/`. Final captures: `dashboard-desktop.png` at 1536×1024, deviceScaleFactor 1; `login-error-desktop.png` at 1536×792, preserving the login source aspect ratio at 4×. Actual local Vue application and native local services, no API mocks. `browser-validation.json` records measured geometry and final console observations. Source and final capture were displayed together for both screens; the full-size workbench source is readable for the key regions.

Fidelity review:
- Typography: inherited V2 font stack, 32px greeting, 17px panel headings, 13–15px supporting text. Login left title retains the selected two-line text and white emphasis. Original font binary is unavailable; glyph rasterization is not asserted identical.
- Layout: home sidebar 256px, header 64px; five metric cards and six shortcuts; left column 816px and right column 408px separated by 16px. Measured panel starts y290/461/793 and y290/540/766, bottoms within y990 at the reference 1024px viewport. Final document scrollWidth=1536 and scrollHeight=1024. Login right translucent card follows the source's proportional position and size. Corrected inline sider width, excessive extra audit-panel height and form button/input styling during visual iteration.
- Colors and icons: pale blue canvas, white rounded cards, blue emphasis with orange/red KPI accents; colored library navigation icons. Existing library icons are the closest available equivalents. Final login button computed background rgb(8,68,146), matching the selected dark-blue direction; inline Chinese authentication error is shown only after an actual failed login.
- Assets: supplied-reference-inspired blue-wave background and transparent flower mark generated as static visual assets; existing hospital illustration reused. These are decorative, not manufactured business evidence. Generated wave details/logo shapes differ from original pixels. Reference watermark is not copied.
- Content and controlled behavior: actual signed-in name, clock/date and permission-filtered links replace illustrated values. Reference sample metrics, task rows, notifications and announcements are never persisted or displayed as actual business facts. Empty states remain explicit. Seven approved navigation groups retain their approved names/order; reference-only standalone equipment/personnel/dashboard menu groups are not added. This is an intentional frozen-contract exclusion from whole-image identity.

Business data limits: current contracts do not expose a unified personal task projection, announcements or inventory warning/distribution metrics. Those positions show empty/unavailable states. Available batch/sample/incoming-investigation reads use existing permissions and APIs. Partial pages are not treated as complete totals; chronological charts use actual creation timestamps and Shanghai day boundaries. The current administrator lacks production batch viewing/creation permissions, so the production statistic is unavailable and its creation shortcut disabled. No permissions were broadened. Quality trend is explicitly scoped to incoming investigations, not falsely presented as an aggregate of all deviations and OOS.

Interaction evidence: successful admin login; failed-password Chinese inline error; header menu search routed to Material Master; material list/detail displayed source-derived Chinese names and correct units; requisition shortcut routed to the existing create page. No records were submitted by these UI checks. Final workbench verification logged no new console warnings/errors and no Vite error overlay. Earlier development-time missing-icon errors were fixed before final verification and are excluded from the final observation window.

Validation: fresh typecheck PASS; 3 affected frontend test files / 8 cases PASS (1.48s); build PASS (12.38s); scoped whitespace/diff checks PASS. No backend, API, DTO, schema, migration, permission, business state machine, signature or frozen-baseline changes in this UI work. Existing local database configuration changes are user-owned and excluded. LOW: existing large application bundle warning remains; generated asset optimization and original font/logo pixel fidelity remain non-blocking visual debt.

Source-record data validation completed separately: 114 API checks and 61 current-domain checks PASS; readable material list/detail verified in the browser. Historic paper batches are expired and remain subject to existing controls; no complete receipt-to-shipment historical lifecycle or new electronic signature is fabricated. Evidence resides outside the repository in `C:/Users/Administrator/.codex/artifacts/source-records-2026-10-07/`.

final result: passed for existing-contract visual implementation and actual browser checks; full-image 100% pixel identity is NOT claimed. Unsupported business projections and approved-menu differences are explicit exclusions, not silently implemented new contracts. Ready for human visual acceptance; no commit or push performed.

Human acceptance — 2026-10-07: user explicitly replied “验收，提交和更新”. Current status is ACCEPTED in MES_TASKS.md. Fresh pre-commit typecheck and three affected frontend suites/eight cases PASS; build PASS13.40s with the inherited chunk-size warning. Approved delivery contains only login/home presentation, decorative assets, existing read projections, targeted tests and acceptance documentation; local database configurations and unrelated files are excluded.

## Home Workbench proactive audit follow-up — 2026-10-07

Final verification after the identity-loss fix: typecheck PASS; build PASS12.68s with inherited LOW chunk-size warning. No new browser acceptance inferred from the build.

Audit-first evidence exposed defects missed by normal-path acceptance. Same-size1536×1024 sidebar captures `02-collapsed-before.png` / `03-collapsed-after.png` prove the corrected80px brand and responsive chart container measurements. `09-workbench-final.png` is the actual post-login home screen, inspected directly; `08-logout-after.png` is the actual normal logout result. Evidence folder: `C:/Users/Administrator/.codex/artifacts/dashboard-audit-2026-10-07/`. No source-only mock is passed off as a persisted batch, and no unsupported metrics are copied from the reference.

Fixed: wrong production enum, pending sample classification, collapsed brand overflow, stale chart sizing, missing-date zero trends, midnight axes, late-response cleanup, expired logout and controlled-page content after session loss. Twenty targeted component/helper/auth/navigation/HTTP cases PASS3.51s; typecheck PASS. Actual browser measurement: expanded chart370/370px; collapsed429/428.65625px; document scrollWidth1536 with no page overflow. Final live observation before the last nonvisual supplement contained no console/page errors. The final identity-loss supplement has red-to-green component evidence; a new browser visit was blocked by browser security policy, so it is not claimed as newly browser-accepted.

Readonly data audit:98 business tables; no clear named test markers;18 meaningful materials, two products and seven named source suppliers. Transactions remain empty, so missing aggregates stay unavailable. Two immutable source supplier-code discrepancies are recorded as MEDIUM debt; no direct DB rewrite or duplicate supplier invented. Full findings, limitations and source evidence: [proactive audit report](docs/review/MES_HOME_WORKBENCH_PROACTIVE_AUDIT_2026-10-07.md). No business/API/database/migration/permission/signature/baseline change; earlier human acceptance remains, this follow-up is READY FOR ACCEPTANCE, not whole-system reacceptance.
