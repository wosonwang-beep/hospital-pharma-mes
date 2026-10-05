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
