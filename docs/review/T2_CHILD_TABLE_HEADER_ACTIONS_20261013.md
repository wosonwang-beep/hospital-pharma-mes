# T2 Child-table Header Actions — 2026-10-13

Status: **IN PROGRESS / SELF-REVIEW / HUMAN RECHECK REQUIRED**

## Confirmed design rule

The user confirmed that every add action for a child table or child-record collection on a create/edit page belongs at the right side of the corresponding section/card title bar and uses the blue primary-button treatment. A duplicate action below or to the left of the child content is not permitted. Read-only pages hide the action; existing permission and editability conditions remain unchanged.

This rule is now recorded in:

- `docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md`
- `docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md`
- `docs/development/decisions/GLOBAL_T1_T2_UI_UNIFICATION_20261013.md`

## Implementation inventory

| Surface | Child section | Result |
|---|---|---|
| Production prescription create/edit | 配料明细 | Header-right blue `＋ 添加原辅料`; old lower-left action removed |
| WMS receipt create/edit | 收货明细 | Header-right blue `＋ 添加收货明细`; old lower action removed |
| WMS issue create/edit | 出库明细 | Header-right blue add allocation/detail action; old lower action removed |
| WMS material request create/edit | 申请物料 | Header-right blue `＋ 添加申请物料`; old lower action removed |
| Material master create/edit | 供应商 | Header-right supplier selector plus blue `＋ 添加供应商`; old lower action removed |
| Material master create/edit | 单位换算 | Header-right blue `＋ 添加单位换算`; old lower action removed |
| QC specification version create/edit | 检验项目 | Header-right blue `＋ 添加检验项目`; old lower action removed |
| eBR book composition editor | 工序表单 / 业务单据 | Existing header-right actions changed to blue primary treatment |
| Production process editor | 工艺路线 / IPC / 参数 | Add-operation action moved to the subsection header; IPC and parameter actions moved to each operation card header; all use blue primary treatment |
| Print data-source editor | 数据表关联 | Existing header-right add-relation action changed to blue primary treatment |
| eBR form designer | 表单列表 | Existing panel-title add action changed to a blue `＋ 新增表单` primary action |

Ordinary T1 list-page create actions, detail/workflow commands, upload controls, runtime occurrence controls, and row-specific split/remove actions are not child-table add actions and are not moved by this rule.

## Validation state

- Vue/TypeScript typecheck: PASS after the implementation changes. Targeted Vitest: 7 files / 35 tests PASS (WMS, QC specification, print-source acceptance, eBR book/model and material model coverage).
- Real 5173 visual checks: 1440 px checked for WMS receipt, WMS issue, material request, production prescription, material edit, production-process route/IPC tabs and print data-source create; 900 px checked for material edit, WMS receipt and WMS issue. Header placement and blue primary treatment are visible with no duplicate lower add action on those inspected surfaces.
- Read-only browser interactions: WMS receipt and issue header actions each added a second local row successfully; business-write guard remained disabled and no form was submitted.
- The recurring unrelated shell request returned one existing `401` console entry on inspected routes; affected page APIs and rendered pages otherwise loaded. This is retained as a non-blocking environment observation, not hidden from the review.
- Remaining active eBR composition/form-designer routes and QC new-version flow have source/typecheck coverage in this pass but do not yet have a complete live desktop screenshot matrix.
- Formal G5 manifest: not rerun for the changed source; no new `READY FOR ACCEPTANCE` or `ACCEPTED` claim is made.
