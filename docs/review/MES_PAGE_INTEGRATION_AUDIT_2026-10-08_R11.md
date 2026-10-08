# MES R11 真实代码审查：生产 QC 允许动作与独立复核一致性

日期：2026-10-08。授权范围：针对已存在的 `INDEPENDENT_REVIEW_REQUIRED` 规则修复只读动作投影与负向回归。不新增个人资格 Gate、不改写冻结业务合同和历史签名。当前分支 main，用户及其它开发活动的脏工作区保留，未提交、未推送。

## 发现与代码修复

- 原实现 `backend/mes-qms/.../ProductionQualityService.java`：生产检验结果 COMPLETED 且尚未复核时，`view(TEST)` 不检查当前操作者身份，直接返回 `allowedActions=[REVIEW,REVISE]`；而写入路径 `review()` 已明确阻止 `recordedBy == actorId`，错误码 `INDEPENDENT_REVIEW_REQUIRED`。此前 R10 前端单独隐藏按钮，本轮在服务端只读动作也做一致性修复。
- 给 `ProductionQualityService` 注入现有 `CurrentPlatformContextResolver`，从**当前结果版本**读取 `recordedBy`；只有操作者有 `qms:test:review` 且不是原结果记录人时，才在只读结果中返回 `REVIEW`。保留原有 `REVISE` 及其它操作、签名、当前结果、更正状态与批次 Gate 行为。帮助逻辑 `canPresentIndependentReview` 为包内静态判定，不增加公开 API。
- `ProductionQualityIT.reviewModelExcludesOriginalRecorderButAllowsIndependentReviewer` 新增 CI 事务集成：本人查询无 REVIEW、独立人员有 REVIEW；列表与详情一致；无复核权限的人员无 REVIEW；签署复核后不再提供 REVIEW/REVISE。全部真实 Spring Service、MariaDB 调用，测试事务回滚。
- `backend/mes-qms/src/test/java/com/hospital/mes/qms/application/ProductionQcActionReadTest.java`：纯单元测试先因函数缺失失败，再改正通过，覆盖本人/独立/缺失记录人边界。
- 本轮没有在资格状态、角色权限、数据库模型或电子签名上新增强制规则；冻结 GxP 约束与历史记录保持原样。

## 验证结果

| 类别 | 结果 | 本机证据 |
|---|---|---|
| mes-qms 单项动作判定测试 | 1/1 PASS | `mes-r11-qms-action-green.log` |
| mes-qms 模块全部单元测试 | **27/27 PASS（8 suites）** | `mes-r11-qms-all-unit.log` |
| 新增生产 QC 只读动作 + 已有保护 | 3/3 PASS | `mes-r11-qc-read-actions-expanded.log` |
| **ProductionQualityIT 全量** | **25/25 PASS，0 FAIL，0 ERROR** | `mes-r11-production-qc-all-native.log` |
| Maven Enforcer（mes-boot 含关联模块） | PASS | `mes-r11-full-enforcer-check.log` |

以上所有日志位于 `C:/Users/Administrator/AppData/Local/Temp/`。最终 `mes-qms -am test`、`ProductionQualityIT` 都是 Maven `BUILD SUCCESS`，退出码 0。25 项包含结果、更正、OOS/复检、CAPA、权限、幂等、审计回滚等；不要将此前 3 项阶段跑次与最后完整 25 项叠加为 28 个不同用例。

## 与并行打印模块开发的环境边界（必须保留）

- 运行初期 `mes-boot/pom.xml` 及 `mes-reporting` 存在其它未提交开发改动，导致 Enforcer 依赖收敛失败（Commons IO/Compress 等），后在未更改这些 POM 的前提下重新执行 Enforcer 通过。本 R11 **没有**覆盖、删除、格式化或提交这些模块。
- 测试启动时读取到其它开发活动新增的 `V033__controlled_printing_module` 迁移文件，Flyway 在 persistent DEV `hospital_pharma_mes_dev` 中从 v032 自动应用到 **v033**，随后校验 **33 migrations PASS**。这是 R11 测试触发的**真实 DEV 数据库结构变更**（属于并行打印功能），不能描述成“数据库完全没改”；本轮没有主动编写该 migration，也没有触碰已应用迁移，未清空/回滚历史库。
- 当前线上式本地 8080 运行的是早先启动的后端进程，**未做 JAR 重打包/重启部署**，因此本次服务端更改经过真实 Spring 事务集成验证，但不宣称已在目前运行中的 8080 服务热更新。为避免影响并行打印开发，没有强制结束进程；待相关功能的受控合并/部署窗口再启动当前构建并做 HTTP 200/allowedActions 回归。

## 未关闭的 GxP 风险

- **HIGH** 生产 QC 个人资格强制 Gate 尚未补齐（与独立复核展示是两件事），现有 DCP `DCP-PRODUCTION-QC-QUALIFICATION-001-PROPOSED.md` 仍未批准，任何 QC 权限不足以替代个人资格。
- **HIGH** 标示规格与冻结处方表观浓度 5 倍差异；结构化质量门禁 DCP 仍待审批。
- **HIGH** 历史演示批次部分人员资格登记创建时间晚于实际检验与 QA 决定，继续保留原始审计证据和质量调查。
- 旧演示批 FINAL eBR 仍待受控处置，不能因为页面/UI 测试成功而自动生成归档并背书质量合规。

范围回顾：仅修改 `ProductionQualityService.java`、回滚原生测试 `ProductionQualityIT.java`，新建 `ProductionQcActionReadTest.java` 和本审查文档；其它工作区修改均保持原样。
