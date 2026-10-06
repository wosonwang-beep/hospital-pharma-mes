# FINAL SYSTEM REGRESSION — v1.0.17

日期：2026-10-06。业务权威：FINAL BASELINE COMPLETE v1.0.17。UI 权威：Global UI Design System V2 / Page Template Standard V2。

## 验收结论

**最终验收尚未通过：DEPENDENCY_NOT_READY。** 当前自动化已完成的结果见下表；共享管理员权限变更测试缺少符合持久开发库安全约束的运行条件，正式业务角色配置也未齐备，且 QA 归档默认展示仍暴露技术摘要及内部引用。不得将安全子集通过或测试身份探测表述为完整系统验收通过。

MES-001～MES-013（含 MES-008A）保持 ACCEPTED；Functional Baseline 和 UI V2 保持既有 CLOSED 状态，本报告不代替人工最终回归确认。Global UI V3 为 PAUSED / OUT OF SCOPE；DCP-WORKBENCH-GAPS-001 四项扩展为 PAUSED / NOT AUTHORIZED，本轮未实施。

## 环境、迁移与数据保护

- 工作目录为仓库根目录；Java 21、Maven 3.9、原生 MariaDB localhost:3306 / hospital_pharma_mes_dev；local/ci 使用忽略的根 .env。
- 测前显式 Flyway validate 成功，history V001～V026 连续且全部 SUCCESS，校验和一致。测后再次显式 validate 成功，见 evidence/environment.txt。
- 启动既有 hospital-pharma-mes-redis-1 容器，localhost:6380 PONG；没有启动替代 MariaDB 或创建替代库。
- 没有执行 reset/drop/rebuild/repair，没有新增或修改迁移，没有删除历史 GxP 记录。现有本机 application-ci.yml / application-local.yml 改动保持原样，未提交。
- 原有集成测试多数使用唯一编号及事务回滚；并发用例保留自身受控业务记录与审计证据，证据文件另存。未删除这些保留记录。
- 最后管理员测试的原始实现会临时删除共享 SYSTEM_ADMIN 权限。曾尝试外层回滚隔离，但其改变请求事务边界，导致回滚断言失败；该试验修改已经完全撤销，原 CI 断言保持不变。该方法不得在本库以原始方式再次运行。

## 验证结果

| Gate | 结果 | 说明 |
|---|---|---|
| 显式 Flyway validate | 测前 / 测后 PASS | V001～V026，未修改 checksum |
| Maven 全量 mvn test | PASS | 195 项；修复后全量重跑 46.344 秒 |
| 原生安全完整 IT 集合 | PASS（明确排除一方法） | 496 项：471 通过、25 跳过，11 分 22 秒 |
| 无排除原生 IT gate（隔离试验） | FAIL | 497 项：471 通过、1 失败、25 跳过；14 分 15 秒。事务隔离试验失败，已撤销 |
| 九类测试能力身份授权探测 | FAIL：5 PASS / 4 FAIL | 三项缺失注册权限（其中只读/操作员重复同一权限）；QA 所选保留批返回 404，fixture 不足 |
| 授权修复受影响回归 | PASS | Reviewer 的生产质量计划读取由 403 变为 200；AuthFilterIT 3 项全部通过 |
| frontend typecheck | PASS | npm run typecheck |
| frontend unit | PASS | 19 文件、72 项 |
| frontend build | PASS | 15.46 秒；有非阻塞 bundle size 提示 |
| npm production audit | PASS | 0 vulnerabilities |
| 完整 Playwright Desktop/Mobile | PASS | 196 项：193 通过、3 跳过；7.0 分钟。T5 按批准 PC-only 合同 |
| Repository / Compose / CI contract / CI negative | PASS | 原有必需脚本与断言未削弱 |
| Scoped independent code review | 无新增 CRITICAL/HIGH/MEDIUM | 已撤销不正确的事务调整，并限定新增前端断言范围 |

安全 IT 命令：

```text
mvn -B -ntp -Pci-integration -Dtest=NoUnitTestsForThisGate -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=*IT,!AssignmentAdministrationIT#ordinaryRoleCarryingIamPermissionCannotBeRemovedFromLastAdministrator verify
```

跳过 25 项的原因：NativeConcurrencyIT 基类 22 项由具体并发子类运行，基类条件未开启；历史 red-trial reconciliation 两项未开启，避免修改保留业务记录；一项 V025 之前的一次性空库 preflight 在 V026 正确不适用。具体子类并发、乐观锁、重复重放仍在 gate 中执行。

上述 496/497 项原生集成集合在最后的 SecurityConfiguration 修复之前执行；该修复随后完成受影响授权重跑，不将补充测试与全量集合拼接成全量 PASS。本轮新并发证据保留 84 个文件，另有 60 个历史文件的副本（原文件已恢复）；91 张本轮覆盖的历史截图已复制到外部 artifact 目录后恢复原文件。

## 覆盖证据索引

以下为自动化覆盖来源，不表示每个用户提出的场景均有独立现场 UAT；逐用例结果见 [完整 IT 证据](evidence/backend-full-it.csv)、[角色探测](evidence/FinalSystemPermissionIT.csv) 与 [汇总](evidence/verification-summary.json)。

| 范围 | 主要测试来源 |
|---|---|
| Login/JWT/Session/RBAC/Permission/Reauthentication | AuthFlowIT、AuthFilterIT、RedisSessionIT、Mes001AuthorizationIT、IAM integration suites |
| Idempotency/Optimistic Lock/Audit/Signature/Inbox/Outbox | FoundationInfrastructureIT、Mes001IntegrationMessageIT、各领域签名与并发 IT |
| Organization/Unit/Conversion/Equipment/Qualification/Material/Supplier/Preferred Supplier | MasterResourcesIT、MaterialSupplierIT、ProcessIT |
| Product/Formula/BOM/Process Package/Version/Operation/Parameters | ProcessIT、IncomingProductionIT |
| WMS Receipt/Lot/Inventory/Reservation/Issue/Return/Ledger | WmsIT、IncomingProductionIT、FunctionalClosureIT |
| Incoming Quality 正向、豁免、FAIL/OOS/retest/原始 FAIL/独立复核/签名/QA | IncomingQualityIT、IncomingQualityAcceptanceIT、IncomingFinalContractIT、IncomingClosureIT、IncomingAllowedActionsIT |
| Order/MainBatch/DIRECT/SUB_BATCH/Execution Unit/Sequence/Snapshot | IncomingProductionIT、FunctionalClosureIT |
| Equipment Calibration/Qualification/Occupancy/Start/Pause/Resume/Complete/History | IncomingProductionIT、FunctionalClosureConcurrencyIT、QcProducerConcurrencyIT |
| Weighing/Verification/Charge/Consume/Quantity/Genealogy/Conversion/Tolerance/Reversal | IncomingProductionIT、ProductionQuantityIT、并发子类 |
| IPC/Production Test/Deviation/OOS/CAPA/独立审查/FAIL blocking | ProductionQualityIT、ProductionQualityPlanIT、FunctionalClosureIT、并发子类 |
| Material Balance Rule/Calculation/PASS/FAIL/Investigation/Blocking | MaterialBalanceIT |
| Finished QA Release、六 Gate、负向、签名、decision | FinishedReleaseIT |
| eBR Template/Version/Runtime/Validation/Signature/Review/Snapshot/History | EbrIT、EbrRuntimeIT、EbrRuntimeArchiveIT |
| QA Decision → FINAL PDF/Manifest/Storage/SHA-256 | EbrArchiveIT、FinishedArchiveSchemaIT、mes013-release browser suite |
| MaterialLot → Charge → Operation → MainBatch；Quality → Decision；Batch → eBR → QA → Archive | IncomingProductionIT、FunctionalClosureIT、EbrArchiveIT、Trace browser suites |

FinishedReleaseIT 检验六项放行 Gate 的阻断与签名；EbrArchiveIT 的 preQaFinalAndInvalidatedFinalSignatureAreBlockedWithoutFiles 与 reviewCopyReuseAndFinalChangedSourcePreserveImmutableHashHistory 检验 FINAL 归档须在 QA decision 之后生成。FINAL PDF 没有成为第七个放行前 Gate。

后台原生 IT 使用实际 SQL/事务/业务服务与 PDF/hash 校验；部分外部适配器或再认证在领域 fixture 中使用测试替身，不声称已验证生产对象存储网络服务。浏览器套件使用 API fixtures，证明页面与交互合同；不声称完成浏览器到实际服务的全链路人工 UAT。

## UI V2 与权限

完整浏览器套件覆盖 T1 列表、T2 编辑、T3 检验记录/报告、T4 MaterialLot/Production Batch、T5 Execution、T6 QA Decision，以及 eBR Designer、Trace、Audit。既有 overflow、控件位置、侧栏/标题、tag、技术字段隐藏、表格及移动布局断言保留。人工查看新生成 QA 截图发现默认技术摘要/内部编号展示仍存在，自动化 PASS 不能覆盖此残留缺陷。

T5 继续遵循 design-qa.md 与 workbench-gap-followup-2026-10-06 中已批准的 PC-only 合同。没有为了本次回归实施 Mobile T5。

数据库当前仅有启用的 SYSTEM_ADMIN 角色及 106 项权限。新增 FinalSystemPermissionIT 为 System Admin、Production Operator、Reviewer/Supervisor、Warehouse、Weighing、QC、QA、Equipment Admin、Read-only 建立测试能力身份，尝试验证真实 token/session、允许读取、非管理员写操作 403 与未知路由 fail closed。最终 5 项通过、4 项失败，不能宣告九类角色全部通过。角色名和能力集合只用于测试，不新增正式角色或业务权限。Menu/Route/Action 由现有前端权限套件验证，但不能据此确认尚不存在的八类正式角色映射。

## 最小修复与测试维护

1. ProductionDetailView 的命令 allowedActions 缺失/非数组时由默认开放改为默认关闭；增加 missing/empty/malformed 三例回归，只断言属于该合同的下达命令。
2. global-ui-v2.css 为含 IncomingReference 的表单标签顶端对齐，修复标签因选择器帮助/空状态高度而偏移。既有标签几何断言保留。
3. SecurityConfiguration 补充冻结生产质量计划 GET list/detail 的精确 qms:plan:view 匹配。RED 为持有该权限仍 403，GREEN 为 200；未知路由继续 denyAll，AuthFilterIT 3 项通过。没有新增 API 或权限。
4. source-map-js 1.2.1 → 1.2.2 锁文件补丁，生产依赖 audit 由 HIGH 转为 0；未修改 package.json 或业务能力。

测试维护仅适应已批准 V2：命令 dropdown、质量/执行 tab、精确当前事实/表格作用域、必要的单位 API fixture、已实施发料路由及 QA allowedActions fixture。保留原始 FAIL 不被覆盖、目标路由、权限、几何与命令断言。没有弱化冻结 CI 合同。

## 剩余问题与最终确认条件

| 等级/类别 | 问题 | 处理与限制 |
|---|---|---|
| 验收阻塞 / DEPENDENCY_NOT_READY | 最后管理员共享权限测试没有符合当前数据库策略的隔离运行条件 | 保留原测试；不得批准全量 IT PASS；需要安全测试环境或获批测试隔离方案 |
| 验收环境阻塞 / DEPENDENCY_NOT_READY | 当前 sys_permission 缺少 production:batch:view、mes:weigh:view；QA 探测没有适配的保留业务 fixture | 不新增 Permission/Migration/业务数据；只读探测四项失败保留在 FinalSystemPermissionIT.csv |
| 验收覆盖缺口 | 八类正式业务角色未配置 | 测试身份不能代替正式角色 Menu/Route/Action/API 403 全矩阵验收 |
| MEDIUM / UI 验收未关闭 | FinishedReleaseView.vue 默认显示 recordDigest；成品批次、QA 决定、生成者仍显示内部编号，SHA-256 列为技术内容 | 新生成 qa-refinement 截图确认；现有 E2E 未捕获，未在本轮隐式重设计；UI V2 不能宣告全部通过 |
| MEDIUM 历史验证脚本债务 | 额外旧 Pester script fixtures 与当前持久 .env / 间接 verify-compose 调用结构不一致 | 必需 repository/CI gate 通过；旧额外脚本两项失败未被隐藏或削弱 |
| LOW 兼容性 | MariaDB 13 超出当前 Flyway 声明的最新支持版本；validate 实际成功 | 记录支持范围提示，不改库或迁移 |
| LOW 性能 | 前端 bundle size 提示 | 本次未做无关重构 |

没有修改业务设计、FINAL BASELINE、权威指针、API/DTO/Table/Migration/Permission/State。没有提交或推送。新增 FinalSystemPermissionIT 是验收探测，当前环境下四项失败尚未关闭，不可将该工作树合并为已通过 CI 的发布。本轮结果需人工审阅；当前不能将最终系统回归标记为通过。

## 当前 QA 截图证据

[Desktop](evidence/qa-refinement-chromium-desktop.png) / [Mobile](evidence/qa-refinement-chromium-mobile.png)。完整新截图保存在 D:/codex/_artifacts/hospital-pharma-mes/final-system-regression-v1.0.17-2026-10-06；恢复的历史截图 SHA-256 与保留 fixture 文件索引见 evidence/artifact-index.json。

后续用户授权小问题修复见 [修复与受影响回归报告](minor-fixes/REPORT.md)；本页保留原始全量运行结果。

后续仅针对权限注册缺失的授权修复已完成，原 3 项失败恢复通过，见 [权限注册修复记录](permission-registration/REPORT.md)。本页保留对应运行时的历史结果。
