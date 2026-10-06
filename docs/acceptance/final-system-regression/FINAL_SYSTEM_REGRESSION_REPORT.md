# FINAL SYSTEM REGRESSION — v1.0.17

日期：2026-10-06；时区：Asia/Shanghai。业务权威：FINAL BASELINE COMPLETE v1.0.17。UI 权威：Global UI Design System V2 / Page Template Standard V2。

**FINAL SYSTEM REGRESSION: PASS**

**CRITICAL = 0**

**HIGH = 0**

**FINAL BASELINE v1.0.17 VERIFIED**

**UI V2 VERIFIED**

本结论对应下述完整自动化 Gate 及明确列出的验证边界。MES-001～MES-013（含 MES-008A）保持 ACCEPTED，既有 Functional Baseline/UI V2 CLOSED 状态不变。没有开始 Global UI V3、DCP-WORKBENCH-GAPS-001 或任何新功能。

## Final tested HEAD

`661785ecfd51f04406a510d3b10bce3886e48941`，分支 `main`；比较基线为 `4a42cf21a453f0da518d46cc72df043fbed1eac5`。

最终后台、前端及浏览器完整 Gate 均在该候选 HEAD 下重新执行。随后提交仅增加报告及验收证据，不改变已测试的源代码、迁移、测试或 CI 工作流。原始失败与授权修复记录保留在[此前回归记录](../final-system-regression-v1.0.17-2026-10-06/REPORT.md)、[小问题修复](../final-system-regression-v1.0.17-2026-10-06/minor-fixes/REPORT.md)、[权限注册修复](../final-system-regression-v1.0.17-2026-10-06/permission-registration/REPORT.md)，不追溯改写历史结论。

## Migration 最终确认

使用现有原生 MariaDB：`localhost:3306 / hospital_pharma_mes_dev`；MariaDB 服务 Running。Java 21 / Maven 3.9；既有 Redis 容器 `hospital-pharma-mes-redis-1`，localhost:6380，认证 PING 返回 PONG。没有启动替代 MariaDB 或建立替代数据库。

| 检查 | 最终结果 |
|---|---|
| Flyway history | V001～V027，27 个连续版本，全部 SUCCESS |
| failed migration | 0 |
| 当前最高成功 schema version | V027 |
| 显式 Flyway validate | 测前、测后均 PASS；27 migrations validated |
| migration checksum | 无异常 |
| V001～V026 源文件 | 相对基线无修改；工作树无迁移改动 |
| reset / drop / rebuild / repair | 均未执行 |
| Frozen Contract Changed | **NO** |
| Role Grants Changed | **NO** |

逐版本描述、SUCCESS 与 checksum 见 [Flyway history](evidence/flyway-history.csv)。验证使用 Flyway Java API 对源迁移目录执行 `validate()`，没有 repair 或重新写入 history。

V027 的唯一用途是按用户“仅修复现有权限注册缺失”授权，将冻结 API 与现有后台授权规则已使用的 `production:batch:view`、`mes:weigh:view` 补注册到 `sys_permission`。两项均为 ACTION、enabled=true、menu_route=NULL，使用 NOT EXISTS 避免重复。没有新增 permission semantics，没有 DDL、账户种子或 role grants。冻结生产 OpenAPI 已包含这两个定义；SQL 仅补齐实现缺失。

V027 应用前后及最终 Gate 前后，角色权限映射 SHA-256 均为：

`61b32f1c095ecde80efd4e6d053cb0a47c04582799502d0ac3dd368aee31ba31`

排除上述两项后的既有 permission records SHA-256 均为：

`0911e4e4f1a6b663db83c0cecaa3e153f9cad1240c88e221d78b34cf1df8661f`

SYSTEM_ADMIN 保持原有 106 项授权。证据：[测前](evidence/permission-scope-before.txt)、[测后](evidence/permission-scope-after.txt)及权限注册修复记录中的 V027 前后证据。

本机忽略的 `.env` 与 `application-local.yml` / `application-ci.yml` 两份本机配置覆盖只用于既定 localhost 环境，不进入提交。Hosted CI 服务容器配置未变。测试采用回滚、唯一编号或已批准的受控并发 fixture；未删除历史 GxP 记录。

## 完整最终 Gate

没有通过 test/it.test/grep 等过滤替代最终全量验证，也没有排除最后管理员测试。下面是最后一次完整运行的计数，不拼接此前 targeted 或失败运行的结果。JUnit 计数包括测试框架在具体子类中执行的继承用例。

| Gate | 最终结果 | 最终计数 / 时间 |
|---|---|---|
| Backend full Maven unit suite | PASS | 71 suites，195 PASS，0 FAIL/ERROR/SKIP；1 分 24 秒 |
| Full native/integration regression | PASS | 52 suites，506 项：481 PASS、25 条件 SKIP、0 FAIL/ERROR；6 分 09 秒 |
| Authorization / RBAC regression | PASS | 完整 IT 内 IAM 77/77、Mes001AuthorizationIT 2/2；不额外重复累计 |
| 九类代表能力身份 | PASS | FinalSystemPermissionIT 9/9，属于上述 IAM 77 项 |
| Signature / audit / idempotency / optimistic lock | PASS | 完整单元及原生 IT 内相关 suites 全部通过 |
| Frontend full unit suite | PASS | 19 文件，72 PASS，0 FAIL；15.76 秒 |
| Frontend typecheck | PASS | vue-tsc 全项目检查，exit 0 |
| Frontend production build | PASS | exit 0；8.73 秒 |
| Full Playwright | PASS | 200 项：197 PASS、3 approved PC-only SKIP；0 FAIL、0 flaky；295.416 秒 |
| npm production dependency audit | PASS | 0 vulnerabilities |
| Repository / Compose / CI contract / CI negative | PASS | 合同脚本通过；Pester 6 PASS、0 FAIL/SKIP |
| 最终只读代码复核 | PASS | CRITICAL 0 / HIGH 0 / MEDIUM 0 / LOW 1 |

执行位置及命令：

```text
# repository root
mvn -B -ntp test
mvn -B -ntp -Pci-integration verify
scripts/verify-repository.ps1
scripts/verify-ci.ps1
Invoke-Pester -Path scripts/tests

# frontend/mes-web
npm run typecheck
npm test -- --run --maxWorkers=2
npm run build
npm audit --omit=dev --audit-level=high
npm run test:e2e -- --workers=2 --reporter=line,json --output <local temporary artifact directory>
```

worker 数仅限制资源并发，没有过滤文件、用例或浏览器项目。Browser plugin 未提供，使用仓库正式 Playwright runner。两个项目为 `chromium-desktop`、`chromium-mobile`。

证据：[最终汇总](evidence/final-counts.json)、[Backend suites](evidence/backend-unit-suites.csv)、[Backend cases](evidence/backend-unit-cases.csv)、[Integration suites](evidence/native-integration-suites.csv)、[Integration cases](evidence/native-integration-cases.csv)、[Playwright cases](evidence/playwright-cases.csv)。所有 52 份最终 Failsafe suite 报告均由本次完整 Gate 新生成；没有使用旧报告补足计数。

## Contract-approved skips 与既有条件跳过

浏览器仅跳过以下 3 项，均为 chromium-mobile：

| 文件 / 场景 | 依据 |
|---|---|
| batch-reference-visual：PC Production Batch T4 reference | 既有批准 PC 参考图验证；常规 T4 Mobile 页面仍由其他完整用例验证 |
| execution-workbench-visual：T5 operation navigation | 已批准 T5 PC-only，不实施 Mobile T5 |
| functional-closure：execution workbench frozen IPC signature binding | design-qa.md，2026-10-05 批准 T5 PC-only；Desktop 用例执行通过 |

原生 IT 的 25 项是未改变的既有测试生命周期条件：

| 条件 | 数量 | 解释 |
|---|---|---|
| NativeConcurrencyIT 基类条件未开启 | 22 | 具体 FunctionalClosureConcurrencyIT / QcProducerConcurrencyIT 执行同一继承 harness；分别 44/38 PASS，没有用基类跳过替代并发验证 |
| 两个具体子类的历史 red-trial reconciliation 未开启 | 2 | 避免本次验证修改历史保留业务记录；需要专门 reconciliation 开关，未为通过测试而改条件 |
| ProductionQualitySchemaIT 的 V025 前一次性空库 preflight | 1 | 当前 V027，不再断言后续生产数据为空；其他 schema 测试通过 |

逐项原因见原生与浏览器 cases CSV。本轮没有新增上述条件或把失败转为 SKIP。

## 业务、UI 与权限覆盖

| 范围 | 完整 Gate 内的主要证据来源 |
|---|---|
| Login/JWT/Session/RBAC/Permission/Reauthentication | AuthFlowIT、AuthFilterIT、RedisSessionIT、IAM suites、Mes001AuthorizationIT |
| Audit/Signature/Idempotency/Optimistic Lock/Inbox/Outbox | FoundationInfrastructureIT、Mes001IntegrationMessageIT、领域签名及两个具体并发 suites |
| Organization/Unit/Conversion/Equipment/Qualification/Material/Supplier/Preferred Supplier | MasterResourcesIT、MaterialSupplierIT、ProcessIT |
| Product/Formula/BOM/Process Package/Version/Operations/Parameters | ProcessIT、IncomingProductionIT |
| WMS Receipt/Lot/Inventory/Reservation/Issue/Return/Reversal/Ledger | WmsIT、IncomingProductionIT、FunctionalClosureIT |
| Incoming Quality 完整链、exempt、FAIL/OOS/retest、原始 FAIL、独立复核、签名/QA | IncomingQualityIT、IncomingQualityAcceptanceIT、IncomingFinalContractIT、IncomingClosureIT、IncomingAllowedActionsIT |
| Production Order/Main Batch/DIRECT/SUB_BATCH/Execution/Sequence/Snapshot | IncomingProductionIT、FunctionalClosureIT |
| Equipment binding/calibration/qualification/occupancy/lifecycle/history/conflict | IncomingProductionIT、具体并发 suites |
| Weighing/Independent Verification/Charge/Consume/Quantity/Genealogy/tolerance/conversion/ceiling/replay/reversal | IncomingProductionIT、ProductionQuantityIT、具体并发 suites |
| IPC/Production Test/Independent Review/PASS/FAIL/invalidation/Deviation/OOS/CAPA | ProductionQualityIT、ProductionQualityPlanIT、FunctionalClosureIT、具体并发 suites |
| Material Balance Rule/Calculation/PASS/FAIL/Investigation/release blocking | MaterialBalanceIT |
| Finished QA 完整链、六 Gate 正负向、签名与决定 | FinishedReleaseIT，完整运行 32 PASS |
| eBR template/version/runtime/validation/review/snapshot/correction/history | EbrIT、EbrRuntimeIT、EbrRuntimeArchiveIT |
| QA Decision → FINAL PDF → Manifest/Storage/SHA-256 | EbrArchiveIT，完整运行 36 PASS；FinishedArchiveSchemaIT、mes013-release E2E |
| MaterialLot → Charge → Operation → MainBatch；Quality → Decision；Batch → eBR → QA → Archive | IncomingProductionIT、FunctionalClosureIT、EbrArchiveIT、Trace E2E |

FINAL PDF 仍是 QA decision 后的归档步骤，没有变成第七个放行前 Gate。六项放行检查、负向阻断、独立审核、签名及不可变历史均保持原合同。

UI V2 完整 E2E 包含 T1、T2、T3 Inspection Record/Report、T4 MaterialLot/Production Batch、PC T5、T6、eBR Designer、Trace、Audit。Document overflow、sidebar/header、label/input、tag、表格作用域、Mobile、默认技术信息隐藏与 allowedActions 检查通过。另复核本次 [Desktop QA](evidence/qa-refinement-chromium-desktop.png) / [Mobile QA](evidence/qa-refinement-chromium-mobile.png) 截图。107 个被浏览器更新的历史截图已另存本机 artifact 后恢复，索引见 [screenshot artifacts](evidence/screenshot-artifact-index.json)。

九类测试能力身份覆盖 System Admin、Production Operator、Production Reviewer/Supervisor、Warehouse、Weighing、QC、QA、Equipment Admin、Read-only：真实 JWT/Redis session、身份权限快照、获授权读取、未授权写入 403、未知路由 fail closed；QA 返回六 Gate、阻断码和空 allowedActions。前端既有完整权限用例验证 menu、route、action，并验证 missing/empty/malformed allowedActions 默认关闭。UI 隐藏动作没有代替后台授权。

原生 IT 使用实际 MariaDB SQL、事务与业务服务；部分领域外部适配器/再认证 fixture 为测试替身，PDF/hash 本身有真实验证。Playwright 使用 API fixtures，证明 UI 与交互合同；不表述为浏览器连接实际后台的生产 UAT。部署环境的对象存储网络验收与正式角色配置 UAT 不在这些自动化证据中。

## 已授权最小修复与测试维护

1. 生产批命令 allowedActions 缺失/非数组改为 fail closed；保留业务状态及命令合同。
2. UI V2 表单中含 IncomingReference 的标签顶端对齐，修复选择器帮助内容导致的位置偏移。
3. 既有生产质量计划 GET list/detail 补回 `qms:plan:view` 精确授权 matcher，未知路由仍拒绝。
4. QA read-model 的预期 eBR/Balance 拒绝使用 savepoint 回滚检查局部写入，解决 rollback-only 导致 500；阻断码与六 Gate 不变，意外异常继续整请求回滚。
5. QA 默认摘要隐藏 hash/internal reference/versionNo，保留主动展开的原始校验证据和实际命令值；用户引用改用既有 IAM 路由及对应权限。
6. source-map-js 锁文件补丁 1.2.1 → 1.2.2，使生产依赖 HIGH 审计问题清零；未改 package.json。
7. V027 补注册两个已有权限定义，未改 role grants 或 permission semantics。

测试维护包括：共享管理员权限测试整体回滚、负向请求 NESTED 隔离、QA probe 整体回滚及 Redis finally 撤销、适应既有 V2 的 E2E 定位/fixture、`.env` 保密及脚本委托链断言。原业务、权限、签名、版本、原始 FAIL 与数据完整性断言保留。

本次最终 Gate 首轮先发现单元迁移链 `1..26` 过期；更新为精确 `1..27` 后，完整原生 Gate 又发现 IAM 迁移数量 `26` 过期。第二次失败运行是 506 项：480 PASS、1 FAIL、25 SKIP，见 [中间 suite 记录](evidence/intermediate-native-v026-count-failure.csv)。仅更新两处精确数量断言，历史哈希和逐文件账户种子禁用检查未削弱。之后在最终 HEAD 从头重跑全部后台 Gate，并重新执行全部前端及 Playwright；最后结果全部通过。

两次本轮完整原生运行产生的 84 个受控并发证据文件保留在 [retained-native-fixtures](evidence/retained-native-fixtures/)，对应业务/审计记录仍保留。未改写此前报告或删除历史 GxP 事实。

## 最终复核与剩余技术债务

最终源代码复核范围为候选 HEAD 相对基线的全部 22 个提交文件；排除本机配置。复核结果 CRITICAL 0、HIGH 0、MEDIUM 0、LOW 1；包括 V027、历史迁移、事务边界、共享数据、权限、UI 与 CI 断言检查。

综合复核及环境/覆盖债务登记：**CRITICAL 0 / HIGH 0 / MEDIUM 1 / LOW 3**。以下为非阻塞债务，没有未解决的本轮自动化失败：

| 等级 | 债务 / 验证边界 | 后续范围 |
|---|---|---|
| MEDIUM | 当前持久库仅配置启用的 SYSTEM_ADMIN，其他八类为测试能力身份；不能替代正式部署角色映射 UAT | 按既有权限合同进行部署配置和角色 UAT，本轮不新增正式角色或改变 grants |
| LOW | QA probe 选择最早存在工艺快照的保留批，但 fixture 查询本身未保证一定有 blocker | 后续仅改善测试 fixture 稳定性；当前完整 Gate 通过 |
| LOW | MariaDB 13.0 超出当前 Flyway 声明已测试的 MariaDB 11.2 范围 | 单独评估运行库/驱动兼容性；本轮 validate 与完整 SQL Gate 均通过 |
| LOW | 前端 production build 的 bundle size 提示 | 后续独立性能工作，本轮不做无关拆分或重构 |

Frozen Contract Changed: **NO**

Role Grants Changed: **NO**

本轮完成最终自动化回归、报告与证据提交并推送 main 后停止；Global UI V3、四项 DCP 扩展和所有新功能继续保持范围外。
