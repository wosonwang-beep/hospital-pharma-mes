# DCP-QC-OBJECT-TYPE-001 — 统一检验对象与固定基础字典

状态：**业务原则已确认 / 详细数据契约审查中 / NOT ACCEPTED**。确认来源：2026-10-11 用户连续确认“质量管理共用、请验/取样/检验按对象类型区分、检验对象多态引用、固定基础字典”，并要求同步设计文档与基线。

## 适用权威及冲突
- 上位基线：FINAL BASELINE COMPLETE v1.0.23；工程门禁：AGENTS.md、docs/development/MES_DEV_RULES.md；UI：Global UI V2 / T1–T6。
- 本补充**仅**确认统一质量对象分类及目标关联规则。既有 V016 来料质量表与 V030 成品质量表存在独立的 NOT NULL 外键与业务状态，不得将字典已建立误认为统一单据已完成。
- 原已签名、放行、审核、审计、库存、历史快照不可原地改写。不得关闭外键约束或自动物理删除历史记录。

## 固定基础数据
- 字典类型 `QC_OBJECT_TYPE`，`SYSTEM`、`FLAT`、`ACTIVE`，编码不可变；普通字典维护不得新增/停用业务对象类型。
- 固定代码：`MATERIAL` 物料、`FINISHED_PRODUCT` 成品、`INTERMEDIATE` 中间品、`ENVIRONMENT` 环境、`PROCESS` 过程检验。
- 标签与排序可维护；业务解析器必须显式注册。未实现的解析器对应的类型**不得用于正式业务提交**，即使字典选项存在。
- 数据库增量：`V053__qc_inspection_object_type_dictionary.sql`，只预置字典，不迁移质量记录。

## 对象引用契约（目标，尚未实现）
- `inspection_object_type` + `inspection_object_id` 为不可拆分的业务引用键；不得把不同对象的数字 ID 当作同一主键空间。
- MATERIAL → `md_material_lot.id`，校验组织、批次状态。
- FINISHED_PRODUCT → `prd_main_batch.id`，产品从 `prd_main_batch.product_id` → `md_product.id` 取得。
- INTERMEDIATE → `prd_main_batch.id` + 工序执行标识（`mes_operation_execution.id`），须明确该对象的唯一键和业务状态后才能启用。
- ENVIRONMENT → 环境监测点实体（当前未核实存在正式对象表/解析器），必须先补设计与权限，不允许任意字符串冒充实体 ID。
- PROCESS → `prd_main_batch.id` + 工序执行标识，具体与 IPC 的边界需审查，不得重复建模。
- 多态键不具备通用 FK，后端解析器需逐类检查对象存在性、组织隔离、对象状态、关联生产批次一致性，并生成不可变对象快照（包含类型、原始 ID、显示编码/名称、业务批号、组织、快照时间和哈希）。
- 请验创建时绑定对象；提交时冻结。取样、检验记录、报告继承同一请验对象及冻结快照；更正须追加审计和签名失效/重签规则，不能原地替换。

## 现状与缺口（2026-10-11 实际代码/迁移核查）
| 范围 | 当前实现 | 目标/待办 |
|---|---|---|
| 数据字典 | V039 `sys_dict_type/sys_dict_item`；V053 脚本已创建 | 迁移 validate/apply、字典 API 实测、固定代码负例 |
| 请验 | V016 `qms_inspection_request.material_lot_id NOT NULL` | 多态对象字段、解析器、组织/状态校验、冻结快照；原记录迁移 |
| 取样 | V016 `qms_sampling_task` 关联请验/请验明细 | 继承并校验请验对象，不能自行改对象 |
| 检验 | 现有质量检验链以物料批次/检验任务为中心 | 继承对象与样品，不重复写独立不一致引用 |
| 成品质量 | V030 `qms_finished_inspection_request.main_batch_id/material_lot_id NOT NULL` | 保留历史证据，设计新旧读写兼容及取消成品物料依赖的迁移 |
| 环境与过程 | 尚未确认正式监测点解析器/IPC 边界 | 先补业务契约再开放提交 |
| 产品/物料 | `md_product` 和 `md_material` 独立 | 产品仅 `md_product`；生产批次 `prd_main_batch`；不得混用成品物料 |
| WMS | 物料与成品库存曾共用 `md_material_lot` 和库存流水 | 已确认成品独立流水，但不属于本 DCP 字典落地范围 |

## 迁移、API 与验收门禁
1. V053 仅增加系统字典；原 V039、V016、V030 不得改写。确认当前已执行最大版本、校验 checksum 后应用。
2. 统一质量对象 schema/API 的后续迁移需独立受控 DCP，包含非空旧列的双写/回填/切换/读兼容/回滚策略，不能直接将旧列改为 NULL。
3. 后端测试：固定代码增删改负例、组织隔离、错误类型/ID、不存在/失效对象、提交后不可变、并发、跨单据对象一致性、审计/签名。
4. 真实 MariaDB 测试和 5173/8080 浏览器验收必须独立留证；字典预置不等于全链验收。
5. 不新建业务页面；如后续需要新/改页面，先执行原型与 DESIGN LOCK 人工确认门禁。

## 文件白名单与状态
本阶段仅允许修改：V053、`SystemDictionaryController.java`、本 DCP、`docs/PROJECT_BASELINE.md`、`MES_TASKS.md`、针对固定字典的专项测试。其余 QMS/QA/WMS/EBR 源码及历史业务数据只读。
当前状态：**IN PROGRESS**。未完成跨单据对象关联改造，不得声称统一质量管理实施完成。

## 2026-10-11 实施与验证记录
- `V053` 已通过 Spring Boot ci profile / Flyway 在本地开发 MariaDB `hospital_pharma_mes_dev` 执行：052 → 053；53 migrations validate PASS。
- `QcInspectionObjectDictionaryIT`：2 tests，0 failures，0 errors，0 skipped；Maven 退出码 0；验证系统字典结构、五个固定代码、无重复。
- `mes-system` Java compile：退出码 0。普通字典 API 已增加对 `QC_OBJECT_TYPE` 的系统字典性质、选项代码、启用状态限制；API 正反例专项测试仍未执行。
- V016/V030 的质量业务字段、后端对象解析器、统一快照及跨单据业务联动 **NOT IMPLEMENTED**；未对已签名/审核历史记录作迁移。
- 以上证据仅证明固定基础字典已落库，不证明统一质量业务已完成。不得把现有 QMS 功能视为统一多态对象模型。
