# QC_OBJECT_TYPE 收尾 — 2026-10-12

状态：**CLOSED / ACCEPTED SCOPE RETAINED**

## 当前权威范围
用户已在 2026-10-11 将本任务明确收敛为最小增量：原请验→取样→检验→报告→放行流程不变，只显示一个“检验对象”字段并复用现有业务主键。来料使用 materialLotId，成品使用 mainBatchId；取样/检验/报告继续通过 inspectionRequestId 继承。不得恢复独立对象绑定表、独立冻结服务、双写或类型+ID两套输入。

## 当前实现核对
- V053 保留固定系统字典 QC_OBJECT_TYPE：MATERIAL、FINISHED_PRODUCT、INTERMEDIATE、ENVIRONMENT、PROCESS。
- V055 历史迁移保留不可变。
- V058 明确撤销零行、零入站FK的 qms_inspection_object_binding 空表。
- 当前 backend Java 源码无 InspectionObjectBinding / qms_inspection_object_binding 运行时引用；仅历史/隔离测试保留迁移断言。
- InspectionObjectBindingService 已不在当前源码。
- 现有质量页面继续使用一个“检验对象”可见字段；不新增路由、权限、状态和业务API。

## 本轮重新验证
- QcInspectionObjectDictionaryIT：2/2 PASS。
- Maven reactor：BUILD SUCCESS。
- 真实 CI profile 连接本地 DEV MariaDB；Flyway 成功 validate **62 migrations**，当前 schema **062**，无需迁移。
- 已有 2026-10-11 单字段验收：Backend 定向13/13、Frontend 24/24、typecheck/build PASS、5173四视口、真实 materialLotId 查询、原质量父子链孤儿0。
- 用户已于 2026-10-11 对本轮“单字段纠偏”明确确认验收。

## 结论
原 DCP 中“多态统一绑定表/解析器/冻结快照”的未完成项属于被用户撤回的过度设计，不能继续作为当前待办。本任务按最新设计锁定关闭。成品质量真实新数据正向链仍属于“产品/成品完整链”任务，不反向阻塞 QC_OBJECT_TYPE 收尾。
