# 功能闭环 Implementation Plan

> 使用 superpowers:executing-plans 在当前工作区连续执行，最后一次独立审查。不提交、不推送，不重建数据库。

Goal: 实现用户于 2026-10-04 明确回复“批准方案”的 DCP-MES-008-011-FUNCTIONAL-CLOSURE-001。
Spec: [批准范围](DCP-MES-008-011-FUNCTIONAL-CLOSURE-001-PROPOSED.md)。审批依据为本会话“批准方案”；历史提案原文保留。
Architecture: 各模块持有自己的事实表，平台签名/审计/幂等复用；Execution 的 port/event 被 QMS 实现，保持 Maven 无环。冻结与最终生产动作共享 Lot 锁，IPC/清场与工序动作共享生产根和 Operation 锁。
Tech Stack: Java 21 / Spring Boot 3 / MariaDB / MyBatis-Plus / Flyway / Vue。

## 任务与验证

- [x] 1. 建立 v1.0.15 累计正式文档，完整具体 DTO/表/状态/权限/UI/依赖映射；检查一致性后切换基线。保留 v1.0.14。
- [x] 2. 数据库检查最高成功版本后追加迁移；建立不可变决定/结果/复核表，实例乐观锁及工艺 IPC 冻结定义。既有事实不回填。
- [x] 3. 先写三类实际动作的失败测试；实现 Domain/Mapper/Service/API/签名及冻结链追溯。文件范围 mes-wms、mes-execution、mes-qms、mes-process 及 boot 装配。
- [x] 4. 实现现有 Lot360°、ExecutionWorkbench、ProcessDetailView 的对应字段/动作/历史区；字段标签输入框同行，复用正式布局。更新完整 UI schema。
- [x] 5. 定向原生集成、负向/权限/签名/幂等/并发及直接回归，前端 typecheck/受影响交互检查。失败只重跑受影响用例。
- [x] 6. 最后独立审查，修复 HIGH/CRITICAL，更新 RTM、迁移/验收证据和 MES_TASKS；仅达到完整要求的任务标记 READY FOR ACCEPTANCE。

## 复核重点

签名精确绑定新事实而非历史记录；首次和后继唯一性；IPC 失败不可通过修订覆盖；清场新增设备/失败立即撤销旧通过依据；并发使用同一当前锁而非旧快照；审计/签名异常全部回滚。

## 执行账本

2026-10-04：用户明确批准提案。当前工作区含既有开发成果和本地配置，沿用且保留。不再次审批本文范围。物理历史最高版本需实施前查询。

2026-10-04 实施账本：原生历史最高成功 V023。候选 v1.0.15、完整 action DTO、六表、owner service/API/签名/工作台/工艺编辑和原生流程测试已写入；根基线指针未切换。后端编译、3 个 IPC 单元方法、前端 typecheck、4 个桌面/手机 mocked UI 方法 PASS。一次独立审查及修复复核 no HIGH/CRITICAL。

Stop: V024 第一张表 FK 写错，非事务 DDL 只新增定义列，V024 success=0。原始脚本/失败历史保留，不修复/删改迁移或数据库；透明恢复提案等待明确例外批准。原生正向验收、并发与直接回归尚未完成，全部任务保持 IN PROGRESS。详情见 ../../docs/acceptance/functional-closure/IMPLEMENTATION-2026-10-04.md。


2026-10-04 恢复与验证账本：用户明确批准仅失败 V024 的例外。旧成功迁移脚本校验与全部历史字段不变；一次 repair 后 V024 成功，六表/11 保护触发器核验。26 个不同原生场景和 3 项 IPC 单元 PASS；前端 typecheck PASS。精确十进制签名、正式权限/HTTP 参数与缺头错误码、库存决定图关系经真实失败→修复验证。独立增量审查 HIGH=0/CRITICAL=0，恢复工具 MEDIUM 误复用风险加固 target 024/更高脚本拒绝/repair 日志防再次运行。2054 OpenAPI refs、CSV 非空记录宽度、旧 v1.0.14 98 哈希、新 v1.0.15 102 哈希通过后切换正式指针。此前 STOP 为历史，现已解除。六步骤是本轮三个授权缺口的范围完成；MES 全任务剩余 RTM 不因此关闭，任务仍 IN PROGRESS。详见 ../acceptance/functional-closure/RECOVERY-2026-10-04.md。
