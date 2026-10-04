# MES-006～008 基线核对记录 — 2026-10-03

本次请求：继续 MES-006、007、008，开发前读取现有全部设计文档、UI 规范及原型，按文档检查验证。本记录是实施审查证据，不替代冻结基线；任务状态仅见根 MES_TASKS.md。

## 阅读与依据

权威目录：`releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.8`。当前 release 的 80 个 SHA256 清单项全部匹配，本次没有改动冻结文件。

主实施者与两个只读审查者合计全文阅读：01 PRD、02 Architecture、03 Domain Overview、04 Domain Model、05 Database、06 eBR Dynamic Form、07 State Machine、08 API Detailed/Interface Guide、09 Functional、10 UI Detailed、11 GMP/Audit/Signature、12 Detailed Tests/12A Acceptance、14 Engineering Rules、15 Dependency/Migration/Integration；全部批准的 00 Design Change 和 trace、发布/manifest/consistency/enterprise register；08 endpoint catalog、10 route matrix、12 test catalog/coverage、13 RTM、15 的四个 CSV。任务卡全文覆盖 MES-006、007、008、008A。后续任务的实现不因阅读而获授权。

OpenAPI 全文件机器解析成功，1,424 个本地引用有效；语义审阅范围是 MES-006 的 17 个操作、eBR 19 个操作、MES-008 的 34 个操作及直接关联 schema/header/permission。未把无关后续模块的每个 schema 宣称为本次逐字段业务审查。

UI 已读 README、DESIGN_SYSTEM、DESIGN_QA、FREEZE_NOTE、UI_MAPPING、截图继承说明，以及 runnable prototype 的 index/model/app/styles；实际查看本次三个任务的 UI-PKG-V、UI-EBR-DESIGNER、UI-WMS-REC-Q 图片。历史 PNG 的上下标签由已批准的水平标签规范覆盖；不使用原型演示数据充当生产数据。

## MES-006 对照修正

保留已经完成的产品/BOM/工艺路线/参数/版本/签名后端和 V011。此次修正现行 UI 规范要求的遗漏：

- 独立 GxP 签名弹窗显示签名含义、对象业务版本、登录身份、原因和再认证输入；取消清空凭据，提交仍调用原再认证和审批接口。
- 产品及工艺包的创建、编辑、版本切换、生命周期操作、返回列表保留查询参数；列表排除详情专属 versionId。修正含大写 T 关键字的恢复逻辑。
- 产品、工艺包和工艺版本详情提供带对象过滤的既有 `/audit` 入口，受 `audit:view` 控制。
- 查询、编辑、操作原因和签名弹窗继续使用横向标签/输入框，适配桌面和手机。

验证：vue-tsc PASS；MES-006 桌面/手机 6 个流程 PASS（14.8s）；签名弹窗同行几何、取消后清空凭据的 2 个桌面/手机用例 PASS。浏览器 API 为 mock；本次未变更后端，因此未重复 MariaDB/全量回归。此前真实后端验证在 `docs/acceptance/mes-006/ACCEPTANCE.md`，没有冒充本次重新执行。无新增迁移。

## MES-007：完整验收的依赖时序冲突

- `tasks/MES-007-R2.md:32,73,76` 把 MES-009 列为软依赖，却要求真实运行快照、更正、复核、并发用例。
- `15_MIGRATION_DEPENDENCY_MATRIX_V1.0.8_FROZEN.csv:9-10` 把定义 LG-007A 分配 MES-007，运行 LG-007B 分配 MES-009，后者依赖 LG-009A。
- `05_DATABASE_DESIGN_V1.0.8_FROZEN.md:511` 要求 form_instance.operation_execution_id 外键，工序执行表又归 LG-010；该 FK 的分期创建规则没有闭合。
- `12_TEST_CASE_DETAILED_V1.0.8_FROZEN.md:133-145` 明确 TC-EBR-003/004 要经过真实 MainBatch Release，并验证发布 V2 后旧批继续使用 V1。
- MES-006 已批准变更明确生产定义冻结仍归 MES-009，没有批准提前建立生产/执行表。

因此定义层可按现有设计开发，但当前顺序无法独立完成 MES-007 全部真实运行验收。不可新建临时生产表、去掉 FK、用内存适配器冒充真实集成，或把完整任务提前标为 READY。TC-EBR-011 同样由 RTM 要求，不能因卡片列表漏写而省略。

## MES-008：需要闭合的三处契约

1. 收货编辑：`10_UI_PAGE_DETAILED...:193-196` 与 route matrix 第 33 行要求 `/wms/receipts/:id/edit` 和保存；OpenAPI 的 receipts 只有 GET/POST、GET detail、POST confirm，缺少保存 API。保留编辑须补 API，取消编辑须改 UI，不能隐含选定。
2. 批次外键：`05_DATABASE_DESIGN...:487,496` 要求 reservation/issue.main_batch_id FK；LG-008 在 LG-009A 建表前，却没有批准的延后 FK 规则。MES-003→004 的专用批准不等于通用授权。
3. 资格 Gate：008 card 第 96 行消费 MaterialEligibilityService；008A card 第 42–44 行和任务矩阵将其产出分配 008A，而 008A 又依赖 008 Receipt/Lot/Ledger。TC-WMS-006/007、TC-ELG-001/002 归属也交叉。收货/账本可以先做，完整预留发料须等待真实资格 Gate 和批次契约，不能假放行。

## 已消歧、不作为阻断的事项

- MaterialVersion 旧措辞已被 MATERIAL-BASIC DCP 覆盖，按根 materialId 和使用时 MaterialSnapshot，不恢复物料版本审批或三个停用名称。
- Generic DTO 本身不是不可开发的理由；已有数据库、领域和渲染规范可指导具体 DTO，不能借机添加业务字段。
- eBR 正式页面及各 API 按其明确权限执行，不自行把 template:update、template:edit、designer:edit 全局重命名。
- 原型旧的 receipt query UNRESOLVED 已被新增 GET `/wms/receipts` 覆盖。
- 缺失的 UI-WMS-INV-Q 截图导航可依据现有收货原型和正式页面说明处理，不需要重设计。
- 企业 RPO/保留期等开放项不阻断这次开发，但仍属于上线验证 Gate。

具体最小补齐范围见 `DCP-MES-007-008-SEQUENCING-001-PROPOSED.md`。未批准前保持 v1.0.8 权威指针，不变更任务边界、API 或物理外键设计。审查发现的是文档合同问题，不是测试失败；MES-007/008 尚未实现，不能宣称完成。
