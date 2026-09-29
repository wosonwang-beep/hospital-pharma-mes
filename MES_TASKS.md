# MES Task Status Index

This is the sole task-status index for future Codex sessions. It records status and navigation only; requirements remain in `FINAL BASELINE COMPLETE v1.0.1`. Update status here when a task starts or reaches a verified gate. Only human approval may set `ACCEPTED`.

## Completed

### MES-001-R2

- Status: `ACCEPTED`
- Summary: Platform foundation completed for GxP audit, electronic signature, idempotency, integration inbox/outbox, and formal operations routes.

### MES-002-R2

- Status: `ACCEPTED`
- Summary: Authentication/RBAC, IAM administration APIs and UI, permission-filtered navigation, audit/idempotency integration, and physical migration V005 completed.

## Current and future

| Task | Status | Title | Dependencies | Task Card |
|---|---|---|---|---|
| MES-003-R2 | `NEXT / NOT STARTED` | 组织、单位、设备与人员资格 | Hard: MES-001, MES-002 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-003-R2.md) |
| MES-004-R2 | `NOT STARTED` | 完整物料主数据 | Hard: MES-001, MES-003; Soft: MES-005 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-004-R2.md) |
| MES-005-R2 | `NOT STARTED` | 供应商与物料合格关系 | Hard: MES-001, MES-003, MES-004 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-005-R2.md) |
| MES-006-R2 | `NOT STARTED` | BOM处方、工艺路线与参数版本 | Hard: MES-003, MES-004; Soft: MES-007 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-006-R2.md) |
| MES-007-R2 | `NOT STARTED` | 动态eBR定义与运行引擎 | Hard: MES-001, MES-002, MES-006; Soft: MES-009 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-007-R2.md) |
| MES-008-R2 | `NOT STARTED` | WMS库存、预留与发退料 | Hard: MES-003, MES-004, MES-005; Soft: MES-009 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-008-R2.md) |
| MES-009-R2 | `NOT STARTED` | 订单与正式批模型 | Hard: MES-003, MES-004, MES-006, MES-007; Soft: MES-008 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-009-R2.md) |
| MES-010-R2 | `NOT STARTED` | 工序、设备运行与参数采集 | Hard: MES-003, MES-006, MES-007, MES-009; Soft: MES-008 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-010-R2.md) |
| MES-011-R2 | `NOT STARTED` | 称量、投料与Genealogy | Hard: MES-004, MES-006, MES-008, MES-009, MES-010; Soft: MES-007 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-011-R2.md) |
| MES-012-R2 | `NOT STARTED — USER DECISION REQUIRED BEFORE MES-012` | IPC、QMS与物料平衡 | Hard: MES-009, MES-010, MES-011; Soft: MES-007 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-012-R2.md) |
| MES-013-R2 | `NOT STARTED` | QA放行与eBR归档 | Hard: MES-007, MES-009, MES-011, MES-012 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-013-R2.md) |

MES-012 retains its frozen Task Card title and scope. Existing repository guidance that described MES-012 as CI-only conflicts with that card; resolve this through explicit user/design authority before MES-012. The conflict does not block MES-003 through MES-011.
