# QC 检验对象单字段纠偏：实施与验收证据（2026-10-11）

状态：ACCEPTED（用户 2026-10-11 在最终验收报告后明确回复「确认」，仅本轮一字段改动）。原整套 MES 和成品质量业务正向链不宣称验收。

## 1. 基线依据
已实际读取 AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md、docs/PROJECT_BASELINE.md、docs/development/DCP-QC-OBJECT-TYPE-001-APPROVED-PRINCIPLES.md，以及 V016、V030、V053、现有质量服务及页面。用户 2026-10-11 最新明确：只增加一个「检验对象」，原流程不变，按现有业务主键关联，不需要额外独立绑定/快照。

## 2. 设计差异与解决
- 原错误方案：独立 qms_inspection_object_binding 表、InspectionObjectBindingService.java、另一个冻结摘要、请验/取样/检验/报告子单据重复继承校验。
- 实际纠偏：恢复 IncomingQualityService 原有来料质量逻辑；移出多余 Service；旧 Flyway V055 保留不可变，V058 撤销零行、零入站 FK 空表；固定五类 QC_OBJECT_TYPE 字典保留。
- 页面一个「检验对象」可见字段，来料引用 materialLotId、成品引用 mainBatchId；原业务表和请验父子关联全部不变。没有另外展示类型/ID 两个输入项，也没有新路由、权限或质量状态。
- 不改用户其他正在进行的生产/成品、QA、EBR、WMS 工作区变更。

## 3. 修改范围
- 新增 V058__retire_unused_qms_object_binding.sql；旧 V055/V056/V057 保持不改。
- IncomingQualityService.java 撤回本任务新增的绑定代码，跟 HEAD 内容比较一致；InspectionObjectBindingService.java 从源码移出，私人本机备份。
- IncomingFormFields.vue、IncomingListView.vue、IncomingDocument.vue、FinishedListView.vue、FinishedDetailView.vue：仅业务单字段可见名称/关联选择器标签，不更改业务请求参数和路由。
- DCP 顶部修订说明、QC_OBJECT_ASSOCIATION_20261011.md 设计锁定、PROJECT_BASELINE 与 MES_TASKS 本任务段。

## 4. 验收证据
- Flyway：应用 V058 成功，58/58 校验通过；qms_inspection_object_binding 表不存在。执行前校验零行、零外部 FK，并留有空表结构及触发器备份；原 V016/V030 结构、历史迁移校验未改。
- DEV 现存：请验 13、取样 11、检验任务 9、检验报告 5（已批准 5）、成品请验 0；三个子单据父请验关联孤儿数全部 0；QC_OBJECT_TYPE 固定五项不变。
- Java：mvn -pl mes-boot -am -DskipTests test-compile PASS；mvn -pl mes-boot -am -DskipTests package PASS。新版 8080 PID25424，health UP；已检查可执行 JAR 不再包含 InspectionObjectBindingService 及旧绑定调用。
- Backend tests：QcInspectionObjectDictionaryIT 2/2 PASS；IncomingEvidenceTest 1、IncomingActionPolicyTest 2、IncomingRecordStatesTest 6、IncomingRulesTest 4，合计 13/13 PASS。
- Frontend：vue-tsc TypeCheck PASS；Vite build PASS（有原有大包 warning）；四个 quality/finished 专项 Vitest 文件合计 24/24 PASS；git diff --check PASS。
- CUAWright：5173/8080 真页面「检验对象」查询列表、请验创建和成品请验空态均为单字段，页面 JS 异常 0；1440、1280、900、375 视口标签中心线差值依次 0、0、0、4px，无整页横向溢出。选取真实物料批次后，请验查询实际传 materialLotId=25，HTTP 200，命中1条；新增请验页面选取同一真实批次成功，不保存。首次运行中旧 JAR 导致 HTTP 500，已真实更新 8080 并复测 PASS，不隐藏该故障。
- 真实 HTTP 只读回归：原四个 quality API 分别返回 13/11/9/5，按同一个 materialLotId=25 条件分别命中1条，父请验 ID 一致；成品请验和 mainBatchId 过滤 HTTP200、当前0条。

## 5. 结论与限制
ACCEPTED（用户于 2026-10-11 明确回复「确认」，只针对本轮单字段及撤回过度设计）；成品质量新数据正向请验/取样/检验/报告由于 DEV 中无成品请验记录未执行，不得借此宣称整套质量系统或全部 GxP 回归完成。
无本轮业务单据新增/删除，无仓储、QA、签名及权限模型变更；未提交/推送 Git。
