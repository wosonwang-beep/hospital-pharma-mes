Approved current receipt-template normalization (2026-10-12): [normal version workflow](development/decisions/PRINT_RECEIPT_NORMAL_VERSION_20261012.md), same human Sentinel_88767a63e37c81918a08f7c75b92cb38 plus parent authorization to use existing append/validate/publish/binding. Original published content remains immutable history, no extra deletion/guard/permission/design change. Runtime status only MES_TASKS.md.

Approved bounded remaining print cleanup (2026-10-12): [six-ID decision](development/decisions/PRINT_REMAINING_CLEANUP_20261012.md), human Sentinel_88767a63e37c81918a08f7c75b92cb38; V061 exact unpublished delete exception only. Published-content update protections unchanged; current68 normalization has an explicit immutable-version conflict. Runtime status only MES_TASKS.md.

Approved bounded printing correction (2026-10-12): [PRINT-ONCE-001](development/decisions/PRINT_ONCE_DESIGN_LOCK_20261012.md), explicit human Sentinel_6642032ed088819184d73710254d3ea9. Current-data PDF responses replace new generic print archives; old files stay immutable and template-independent. Exact ten-template/binding DEV cleanup only, no production/eBR/signature/permission change. [Current API supplement](api/print-once.openapi.json). Runtime status only MES_TASKS.md.

## 2026-10-11 产品成品根因受控补充

设计已由父对话授权设计负责人批准；[DCP-PRODUCT-FINISHED-ROOT-001](development/DCP-PRODUCT-FINISHED-ROOT-001-APPROVED.md)仅覆盖产品标准/V2计划/独立成品库存及必要消费者，v1.0.23历史不改；新流水应用层不可变，不新增触发器。部署须先隔离验证和父任务审核。运行进度仅MES_TASKS.md。

# Hospital Pharmaceutical MES V2.0 Project Baseline

This document is the compact, stable entry point for product and engineering context. It does not track task status or reproduce detailed domain design.

## 2026-10-13 生产工艺动态表单 CPP 规则设计补充

用户已批准生产工艺功能内的动态表单 CPP 规则设计基线。工序的动态表单设计器包含“表单设计”和“CPP规则设计”两个页签；表单设计字段库的每个可插入字段前统一显示独立 `+` 图标，点击 `+` 按当前 Canvas 光标/选区位置插入对应字段；CPP 规则只绑定当前动态表单字段，不直接绑定产品，适用产品关系继续由生产工艺维护。CPP 支持规则基本信息、AND/OR 与条件组的可视化条件配置、业务/系统规则预览及超限动作配置；设计页删除“运行校验效果”，真实判定和动作留在生产/eBR执行阶段。详细批准边界见 [DCP-PROCESS-DYNAMIC-FORM-CPP-001](development/DCP-PROCESS-DYNAMIC-FORM-CPP-001-APPROVED.md)，[需求](development/PROCESS_DYNAMIC_FORM_CPP_REQUIREMENTS.md)，[详细设计](architecture/PROCESS_DYNAMIC_FORM_CPP_DETAILED_DESIGN.md)和[一致性检查](review/PROCESS_DYNAMIC_FORM_CPP_CONSISTENCY_20261013.md)。本补充已获原型源码级批准并完成正式增量实现：复用现有 `DefinitionDesigner`、`ebr_rule_def`、安全 DSL 与运行证据机制，追加 V064 保存可视化 CPP 配置；不建立第二套表单设计器、规则引擎或产品适用关系。FINAL BASELINE v1.0.23 冻结历史保持不变，运行状态只见 `MES_TASKS.md`。

## 2026-10-11 统一质量检验对象固定字典补充（业务原则已批准，实施中）
用户最新确认：物料与成品沿用原请验、取样、检验、报告、放行流程，页面只显示一个「检验对象」字段，后台使用已存在的物料批次 ID 或生产批次 ID 查询关联；不建立第二套质量单据/独立绑定/新冻结快照。详细锁定见 [QC_OBJECT_ASSOCIATION_20261011](development/decisions/QC_OBJECT_ASSOCIATION_20261011.md)。原 [DCP-QC-OBJECT-TYPE-001](development/DCP-QC-OBJECT-TYPE-001-APPROVED-PRINCIPLES.md) 的复杂多态绑定方案已被用户最新决策收窄为历史。固定五类 QC_OBJECT_TYPE 字典 V053 保留，V055 历史迁移保留，V058 仅撤销其未使用的空表；V016/V030 既有质量单据及签名历史不改。此次不宣称已将原有两个内部质量服务完全合并为一套代码。FINAL BASELINE v1.0.23 的其他条款仍权威，运行状态见 MES_TASKS.md。

## 2026-10-11 角色权限原型冻结与正式集成

用户再次确认角色权限交互原型，冻结本机 `mes-demo/src/features/roles/RolePermissionsApproved.vue` 源码 SHA256 `26216113176821780456FC9336CC6DBFB2C376DDF383EFC745654231D6592A97`；[详细设计锁定](development/decisions/ROLE_PERMISSIONS_PROTOTYPE_FROZEN_20261011.md)。正式 MES 必须复用 T1/T2/T3，菜单名称前勾选可见、同一行只查看/管理互斥，初选管理不自动授予审核/签名/放行等独立特殊权限；历史权限和现有接口安全机制不重置。运行进度仅在 MES_TASKS.md。


本轮实现及运行复核见 [MENU_ROLE_CLOSEOUT_20261011](review/MENU_ROLE_CLOSEOUT_20261011.md)。该记录不改变设计和业务基线，也不代表真实授权保存已通过；当前任务状态仅以MES_TASKS.md为准。
## Authority

Current printing design-review entry (2026-10-10): [DCP-PRINT-DATA-SOURCE-CONFIGURATION-005](development/DCP-PRINT-DATA-SOURCE-CONFIGURATION-005-PROPOSED.md), same existing printing design, confirmed four-step mes-demo prototype and source hashes; domain/database/state/API/OpenAPI/UI/permission/audit/test/RTM/migration/integration/task mapping in its linked matrix. **MAIN DESIGN APPROVED**, the user authorized approval by the main design thread; metadata write/policy directory, independent datasets, dedicated lifecycle actions, immutable sidecar and RFC8785 golden vectors are specified. No authority-pointer switch or backend implementation claim. DCP005 and its explicitly approved source-selection supplement govern only the bounded printing increment alongside v1.0.23 and printing 001–004; prior releases remain immutable. Runtime progress only MES_TASKS.md.

Approved bounded menu supplement (2026-10-10): [DCP-MENU-CONFIGURATION-001](development/DCP-MENU-CONFIGURATION-001-APPROVED.md), permission description and atomic menu configuration plus additive formal seed preservation only; [cross-document review](review/MENU_CONFIGURATION_CONSISTENCY_20261010.md). v1.0.23 and previous releases remain immutable, UI V2/T1–T6 unchanged; runtime readiness only MES_TASKS.md.

Current authority is frozen FINAL BASELINE v1.0.23 plus approved bounded supplements. For basic audit/version controls and new production/eBR process bindings, [DCP-BASIC-NO-AUDIT-VERSION-001](development/DCP-BASIC-NO-AUDIT-VERSION-001-APPROVED.md) supersedes the affected clauses following [cross-document consistency PASS](review/DCP_BASIC_CURRENT_CONSISTENCY_REVIEW.md). All other frozen contracts and historical releases remain authoritative and immutable.

### Current approved navigation / real entry authority — 2026-10-07

Current business authority: FINAL BASELINE COMPLETE v1.0.23 — AUTHORITATIVE, approved DCP-PRODUCTION-FINISHED-ENTRY-001, cross-document consistency PASS. [Contract](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.23/00_PRODUCTION_FINISHED_ENTRY_CONTRACT_V1.0.23.md), [manifest](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.23/00_MANIFEST_FINAL_FROZEN_V1.0.23.md), [review](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.23/00_PRODUCTION_FINISHED_ENTRY_CONSISTENCY_REVIEW.json). Shared seven-domain navigation and real production/finished context selectors only; original v1.0.22 business, DB/migration, state, permission codes, QA/signature/stock and no-mandatory-weighing rules inherited. UI V2/T1–T6 unchanged. Parent v1.0.22 immutable; runtime readiness only MES_TASKS.md.


### Historical v1.0.22 trace / finished inventory authority — 2026-10-06

Historical authority at that approval: `FINAL BASELINE COMPLETE v1.0.22` — `AUTHORITATIVE`, approved DCP-TRACE-FINISHED-INVENTORY-001 and cross-document consistency PASS. [Contract](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.22/00_TRACE_FINISHED_INVENTORY_CONTRACT_V1.0.22.md), [manifest](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.22/00_MANIFEST_FINAL_FROZEN_V1.0.22.md), [review](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.22/00_TRACE_FINISHED_INVENTORY_CONSISTENCY_REVIEW.json). Actual-charge forward/WMS/source trace; server-filtered finished inventory and T1 entry only. No database/migration/QA/signature/stock/weighing change. Parent v1.0.21 immutable; UI V2/T1–T6 retained; runtime readiness only MES_TASKS.md.


### Retained v1.0.21 optional inspection draft authority — 2026-10-06

Historical authority at that approval: `FINAL BASELINE COMPLETE v1.0.21` — `AUTHORITATIVE`, approved DCP-FINISHED-INBOUND-INSPECTION-DRAFT-001 and cross-document consistency PASS. [Frozen manifest](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.21/00_MANIFEST_FINAL_FROZEN_V1.0.21.md), [consistency](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.21/00_FINISHED_INBOUND_DRAFT_CONSISTENCY_REVIEW.json). Optional atomic inbound/inspection DRAFT pair uses existing contracts; physical warehouse confirmation remains mandatory for QC execution. All138 v1.0.20 files unchanged. Existing v20 finished-chain/WMS/source contracts inherited; no new migration/permission/endpoint/route/signature rule. UI V2/T1–T6 unchanged. Runtime readiness and explicit human acceptance only MES_TASKS.md. Earlier dated pointers remain historical.

### Retained v1.0.20 approved finished-goods authority — 2026-10-06

Historical authority at that approval: `FINAL BASELINE COMPLETE v1.0.20` — `AUTHORITATIVE`, approved DCP-FINISHED-GOODS-CHAIN-001 and cross-document consistency PASS. [Frozen manifest](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.20/00_MANIFEST_FINAL_FROZEN_V1.0.20.md), [consistency](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.20/00_FINISHED_GOODS_CONSISTENCY_REVIEW.json). Complete controlled finished receiving/quality/QA/shipment/trace delta supersedes only its bounded inherited contracts. Parent v1.0.19/126files unchanged; UI V2/T1–T6 unchanged. Runtime readiness and explicit human acceptance are maintained only in MES_TASKS.md. Earlier dated pointers remain historical.

### Retained v1.0.19 WMS authority — 2026-10-06

Historical WMS authority at that maintenance: `FINAL BASELINE COMPLETE v1.0.19` — `AUTHORITATIVE`, following approved DCP-WMS-REQUEST-INVENTORY-RETURN-001 and detailed-design confirmation; design consistency PASS. [Manifest](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.19/00_MANIFEST_FINAL_FROZEN_V1.0.19.md). Existing v1.0.18 and earlier entries below are retained history. UI remains Global UI V2 / T1–T6; runtime status and explicit human acceptance are maintained only in MES_TASKS.md.


### Retained v1.0.18 maintenance authority — 2026-10-06

At that approval, `FINAL BASELINE COMPLETE v1.0.18` was `AUTHORITATIVE`. User approved database changes and chose manufacturer ownership on each material-supplier relationship with receiving-source freeze. [Approved DCP](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.18.md), [frozen manifest](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/00_MANIFEST_FINAL_FROZEN_V1.0.18.md), [consistency PASS](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/00_MATERIAL_STORAGE_SOURCE_CONSISTENCY_REVIEW.json). v1.0.17 and all prior frozen releases remain unchanged historical authority. Current UI authority remains Global UI V2 / Page Template V2 (`T1–T6`). Current maintenance readiness is only in MES_TASKS.md.

The v1.0.17 Closeout and subsequent dated approval descriptions below retain their original scope; earlier “current” references are historical and do not override this v1.0.18 pointer. No new final-system regression or human acceptance of this maintenance is inferred. Workbench image/planned-time/SOP proposals remain PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED; UI V3 remains NOT IN CURRENT SCOPE.

### Retained v1.0.17 authority and Closeout — 2026-10-06

- Business: `FINAL BASELINE COMPLETE v1.0.17` — `AUTHORITATIVE`; the v1.0.17 release directory and frozen manifest linked below remain unchanged.
- UI: [Global UI Design System V2](ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md) and [Page Template Standard V2](ui/MES_PAGE_TEMPLATE_STANDARD_V2.md), `T1–T6`.
- Human-confirmed Functional Baseline v1.0.17 and UI V2 Implementation: `CLOSED`. Current task acceptance is maintained in [MES_TASKS.md](../MES_TASKS.md).
- [Historical v1.0.17 Closeout record](review/MES_V1.0.17_FUNCTIONAL_UI_V2_FINAL_CLOSEOUT.md) records acceptance and existing evidence; this administrative update creates no new release or business contract.
- DCP-WORKBENCH-GAPS-001: Material Master Image, Operation Execution Photo, Planned Operation Time, SOP/Method Reference remain `PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED`, outside the closed scope.
- Global UI V3: `NOT IN CURRENT SCOPE`. Current UI authority is not switched.

### Retained approval history

The dated approval entries below retain their original release descriptions. Any earlier use of “current” or “authoritative” is historical; only the current v1.0.17 pointer above applies now.


- Current approved audit HIGH closure: user explicitly approved DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001 on 2026-10-05. Cumulative v1.0.17 is authoritative after cross-document consistency PASS. Scope: existing productionOrderId query, incoming read allowedActions and exclusive active equipment occupancy. [Current contract](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/00_AUDIT_HIGH_CONTRACT_V1.0.17.md). No schema/migration, new permission/route/state, signature or QA release-rule change. Global UI V2 and T1–T6 are unchanged. All earlier entries below are preserved approval history; the current authoritative pointer takes precedence.

- Current authorized completion: user approved DCP-MES-012-013-CONTRACT-001 on 2026-10-04; reviewed cumulative v1.0.16 is authoritative after 2026-10-05 consistency review. Production quality, material balance, finished QA/archive and narrow producer/consumer extensions are authorized. v1.0.15 remains immutable historical authority; runtime completion is not inferred. Read [current contract](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md).

- Approved bounded functional closure: user explicitly replied “批准方案” on 2026-10-04 for [DCP-MES-008-011-FUNCTIONAL-CLOSURE-001](development/DCP-MES-008-011-FUNCTIONAL-CLOSURE-001-PROPOSED.md). Inventory decisions, controlled clearance and only the staged MES-012 IPC producer are authorized. v1.0.15 is now the authoritative cumulative release after cross-document consistency review. The user separately approved the one-time failed V024 recovery; migration and scoped native verification passed. Original failure evidence and bounded approval are retained in [recovery/acceptance evidence](acceptance/functional-closure/RECOVERY-2026-10-04.md); no general authorization to rewrite migration history is implied.

- Current approved completion: DCP-INCOMING-QUALITY-GAPS-001 plus referenced MES-009/010 decisions and full acceptance scope; explicit approval 2026-10-03. Reviewed v1.0.11 core, v1.0.12 runtime/return, v1.0.13 weighing producer and v1.0.14 frozen-standard trace contracts are current. Prior releases are preserved; verification is recorded in docs/acceptance/incoming-quality.

- Current bounded incoming delta: DCP-MES-008A-CONTRACT-001 A/B, previously approved routes and staged QMS FKs; cross-consistency review PASS. v1.0.9 retained unchanged. That earlier gap register is superseded by the approved completion contracts.

- Current approved bounded change: DCP-MES-007-008-SEQUENCING-001; explicit approval 2026-10-03, staged eBR/WMS, delayed FKs and receipt PUT; v1.0.8 preserved.

- Authoritative design: `FINAL BASELINE COMPLETE v1.0.17`.
- Release directory: [`releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/`](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/).
- Frozen manifest: [`00_MANIFEST_FINAL_FROZEN_V1.0.17.md`](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/00_MANIFEST_FINAL_FROZEN_V1.0.17.md).
- Current bounded correction: `DCP-MATERIAL-NAMES-UI-001` removes three material name fields from business use and enforces horizontal form labels; v1.0.7 retained, no physical migration.
- Approved MES-006 bounded completion: `DCP-MES-006-R2-001` (explicit “确认授权”, 2026-10-03); original design retained, v1.0.6 immutable.
- Approved MES-004/005 bounded change: `DCP-MES-004-005-R2-001` (human approval 2026-10-03); prior v1.0.3 retained.
- Approved MES-003 bounded change: `DCP-MES-003-R2-001` (human approval 2026-10-03); prior v1.0.2 retained.
- Approved change trace: `DCP-MES-002-R2-001` → v1.0.2 (cumulative over retained v1.0.1 / DCP-MES-001-R2-001).
- Task state and next-task selection: [`MES_TASKS.md`](../MES_TASKS.md).

Frozen requirements, data models, states, APIs, permissions, routes, integration contracts, and GxP controls may change only through approved design change and cross-consistency review.

- Approved current presentation change: `DCP-UI-LIST-EDIT-001` (attached reference list/edit style, 2026-10-03); prior v1.0.5 retained.

## Product baseline

Hospital Pharmaceutical MES V2.0 supports regulated hospital-preparation manufacturing from master data and process definition through eBR, inventory, production execution, genealogy, quality, material balance, QA release, and archive evidence.

Records and state transitions must preserve traceability, data integrity, attributable audit evidence, and required electronic signatures. `RELEASED` means finished-product release, not production completion.

## Technical baseline

- Backend: Java 21, Spring Boot 3.x, Maven modular monolith, MyBatis-Plus, Flyway, MariaDB, Redis, Spring Security/JWT, Springdoc OpenAPI.
- Frontend: Vue 3, TypeScript, Vite, Pinia, Vue Router, Ant Design Vue; Vitest and Playwright for focused verification.
- Runtime boundary: `mes-boot` is the only executable backend module. Domain modules are library JARs and must remain acyclic.
- Module structure: `api`, `application`, `domain`, and `infrastructure`; domain events for notifications and query services for complex cross-module reads.

## Architecture and GxP invariants

- Domain commands and methods own business state changes; no generic status-update endpoint.
- Core production state machines operate without BPMN/workflow runtime availability.
- API envelopes remain outside domain services.
- Mutable regulated aggregates use optimistic locking; regulated records are never physically deleted.
- Critical writes are transactionally designed with authorization, audit, idempotency where required, and electronic-signature binding where specified.
- Audit/signature evidence is append-only or superseded through explicit lineage; credentials and sensitive reauthentication material never enter logs or audit payloads.
- Dynamic-form expressions never execute arbitrary JavaScript, SQL, or SpEL.

## Database and environment baseline

- MariaDB is the production-compatible SQL target; formal schema evolution is Flyway-only and append-only.
- Section 15 migration identifiers are logical groups, not physical Flyway versions. New physical versions follow the actual highest successful repository/database version.
- Local development and local database tests share the persistent native `hospital_pharma_mes_dev` using unique rollback-owned fixtures. Hosted CI retains its isolated service container. Tests never connect to PROD.
- Persistent DEV data is not deleted, rebuilt, or cleared without explicit destructive-action authorization.
- Operational details: [`development/database-and-validation-strategy.md`](development/database-and-validation-strategy.md).

## Minimal document routing

Start with `AGENTS.md` and `MES_TASKS.md`. After selecting one task, open its Task Card and only the referenced sections needed for that task:

- Product and domain: PRD, Domain Model, and relevant state-machine sections in the current authoritative release.
- Data and interfaces: Database Design, API Detailed Design, Full OpenAPI, and Section 15 contracts named by the Task Card.
- Behavior and UI: Functional Design, UI/Page Design, Route Matrix, and prototype mapping only when the task touches them.
- Compliance and tests: GMP/Audit/eSignature, required Test Cases, and RTM rows named by the Task Card.
- Repository implementation guidance: [`architecture/`](architecture/), [`api/`](api/), [`development/`](development/), and accepted evidence under [`acceptance/`](acceptance/) only when directly relevant.

Do not read every baseline artifact or all repository documentation as a startup step. Detailed domain documents remain at their existing locations and are loaded on demand.

Current approved material-only change: DCP-MATERIAL-BASIC-001, direct basic maintenance with multiple suppliers/one preferred and consumer snapshots; v1.0.4 retained as historical authority.

## Approved shared navigation supplement — 2026-10-07

FINAL BASELINE COMPLETE v1.0.22 remains the inherited business authority with [DCP-GLOBAL-NAVIGATION-001](development/DCP-GLOBAL-NAVIGATION-001-APPROVED.md) for the explicitly approved menu names/order and shared configuration only. Global UI V2/T1–T6 stay current. No frozen release or API/DB/permission/state/signature/route contract changes; other finished UI gaps remain outside this supplement. Runtime readiness remains only in MES_TASKS.md.


## Approved basic maintenance and current process supplement — 2026-10-09

Human-approved [DCP-BASIC-NO-AUDIT-VERSION-001](development/DCP-BASIC-NO-AUDIT-VERSION-001-APPROVED.md) supersedes only basic maintenance audit/version controls and the process binding of new production/eBR work. [Bounded contract](architecture/BASIC_CURRENT_DEFINITION_CONTRACT.md) specifies database, domain, state, API, permissions, UI, migration, integration and RTM changes. Basic pages retain UI V2 and their existing T1–T6 templates, without audit panels, client version controls or process revision actions. Production/eBR retain their own audit, signatures, revision checks and immutable frozen evidence. Historical releases and evidence remain immutable. Runtime verification/status is only MES_TASKS.md. This approved supplement does not authorize any further business/UI redesign.

## Approved eBR template name supplement — 2026-10-09

Human-approved [DCP-EBR-TEMPLATE-NAME-001](development/DCP-EBR-TEMPLATE-NAME-001-APPROVED.md) adds only the eBR template name and its current UI/API/storage/search/frozen-content contract. Historical null-name canonical definitions, signatures, snapshots and PDF evidence remain unchanged. No whole-book redesign or further audit/version removal is authorized. Current UI V2 remains. Runtime status is only MES_TASKS.md.


## 主设计批准与实施下发 — 2026-10-10T13:02:40.670492+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION IN PROGRESS。来源：主对话01a115d2-8ace-7574-9a08-ac48dfc970cc本次委派，已审阅DCP005第1–11节及修正实体/API，批准同一文件及18操作/61schema契约作为实施基线。用户先前已明确授权主对话批准、任务做到收尾及必要DEV迁移/后端更新/测试；本记录不把主设计批准写成用户最终验收，不改历史审批。

本次白名单：mes-reporting打印领域/API/应用/持久化及直接消费provider、mes-boot定向测试与新增Flyway、正式Vue打印页面/API/路由/现有导航入口、当前同一设计/契约/复核/任务索引。现有其他菜单/权限/UI未提交改动保留，开始清单和文件备份位于本机print-source-implementation/worktree-before.txt及file-backup/。不修改旧迁移、旧归档正文/hash/触发器、冻结releases、无关业务，不提交或推送。

专项环境覆盖：本次主设计下发明确要求迁移先在隔离测试库验证，仅该验证允许专项隔离库；正式DEV仍为已有localhost:3306/hospital_pharma_mes_dev、Redis6379、后端8080、前端5173；不drop旧库、不repair旧checksum、不增加持久安全开关。真实目标/版本/备份可恢复确认后才执行DEV迁移。复杂打印集成初始预算90分钟，最多2轮自动修复，重复同根因立即报告；不改批准验收断言。当前实施开始，迁移/编译/业务/UI/PDF验证未运行，不能宣称验收或部署完成。

## Approved bounded role-ID signing supplement — 2026-10-11

FINAL BASELINE COMPLETE v1.0.23 and historical releases remain unchanged. The approved [minimal role-ID design](development/DCP-ROLE-ID-SIGNING-001-MINIMAL-DESIGN-20261011.md), [six constraints / DESIGN LOCK](development/decisions/ROLE_ID_SIGNING_DESIGN_LOCK_20261011.md), and [bounded database/domain/API/UI/GxP contract](architecture/ROLE_ID_SIGNING_CONTRACT.md) authorize code and an unapplied additive migration only. Current role IDs become rule identity, signer remains user ID, and legacy published canonical/evidence is immutable. No new global role/organization model, permissions, account rename or signature removal. [API supplement](api/role-id-signing.openapi.yaml) and [RTM](development/ROLE_ID_SIGNING_RTM_20261011.csv) carry the affected contracts. No authoritative frozen-release pointer is switched here; DEV migration/deployment/conversion remains a separate coordinated phase. Runtime status is only MES_TASKS.md.
