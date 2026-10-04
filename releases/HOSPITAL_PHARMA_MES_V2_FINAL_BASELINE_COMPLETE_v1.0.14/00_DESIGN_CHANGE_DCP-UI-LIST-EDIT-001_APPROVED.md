# DCP-UI-LIST-EDIT-001 — approved, 2026-10-03

Human request: “按列表页面和编辑页面的风格修改”, with attached index.html; prior authorization to autonomously modify remains applicable. Bounded scope: presentation of existing lists and edit/detail forms, including master data and IAM. No business fields, table, API, permission, state, route, audit/signature, revision or lineage change. No migration. v1.0.5 is preserved as immutable predecessor.

Reference: white page header/cards on #f0f2f5 background, #2c5cdc primary buttons and 3px section title bar, 4px corner radius, compact 13px tables, 18px page titles, three-column desktop query controls, and two-column edit forms with 130px right-aligned labels. Query label/control remain inline on all viewport sizes; mobile edit forms have one column with label/control remaining side by side. Existing navigation chrome retained. Existing technical contract-strip metadata removed from master list presentation; operation controls and contract behavior retained.

Implementation source: frontend/mes-web/src/list-edit.css and existing list/edit templates. Shared style applies to existing admin/platform list cards; user/role edit forms use horizontal labels. Material unit and supplier controls retain complete existing behavior.

Reference SHA256: cb95e99164bf50327c5bd5a5a18e94c77e1e8ad35eef6eb472009f7114354000
