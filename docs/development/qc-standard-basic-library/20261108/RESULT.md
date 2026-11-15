# QC质量标准基础资料库 — 2026-11-08 本轮正式开发和回归

**状态：核心功能可用 / 定向回归通过 / QC模块正式G5及完整交付尚未ACCEPTED**

## 基线和界限
- 本机唯一工作区：`D:\codex\_project\gmp\hospital-pharma-mes`，仅 `main`，本轮HEAD及origin/main均为 `77e1c677`。不新建worktree，不清理既有未提交内容，本轮不stage/commit/push。
- 强制设计依据：`docs/development/DCP-QC-SPEC-BASIC-LIBRARY-001-APPROVED.md`、`docs/development/decisions/QC-STANDARD-BASIC-LIBRARY-DESIGN-LOCK.md`、`docs/development/qc-standard-basic-library-contract.md`。QC标准是一个当前可维护的基础资料，不设审批、电子签名和标准版本生命周期，依据与多物料/多产品适用范围不可省略。已冻结下游业务单据继续保留自身的QC标准快照与摘要。
- V070/V072/V073属此前经用户批准的QC专属迁移；禁止擅自重写迁移或修复Flyway历史。此轮**没有**再次执行破坏性迁移、删除任何原记录或改动签名证据。
- QC发布边界涉及未提交的跨后端、前端、迁移和下游改造，不能只提交QC API的类型声明使GitHub TypeScript侥幸通过。

## 本轮修正的实际功能
1. 正式 `frontend/mes-web/src/views/quality/QcSpecificationDetailView.vue` 新增“**维护记录**”只读业务表（新增/修改/删除、原因、时间）；不展示内部记录ID、不重新引入版本/审批/签名。即使旧标准没有维护记录，仍保留可理解的区域。
2. 同页新增“**删除标准**”入口，仅 `qms:specification:delete` 角色可见；有未保存修改时禁止删除。确认弹窗必填业务原因，按钮为“确认删除→取消”，由已有 `qcApi.remove` 携 recordVersion、If-Match和Idempotency-Key 调用后端删除；仅后端确认 `deleted=true` 才返回列表。弹窗失败保留提示，不跳过组织/乐观锁/审计。
3. 修复只读QC账号“能输入却不能保存”的问题：生产标准编码、名称、依据、状态、备注、适用范围及检验项目控件全部随 `qms:specification:view` / `edit` 实际授权切换只读。保存按钮也受权限限制；未扩大只读角色的编辑权限。
4. 新建定向真实Chrome E2E：`frontend/mes-web/e2e/qc-standard-current-live.spec.ts`。从正式5173登录→列表→后台API真实详情→维护记录/无版本页签→删除确认可用性与取消**零 DELETE 请求**→三个现存只读身份权限对照。没有创建/删除或编辑正式业务标准。

## 运行环境修复（供回归继续运行）
- 原本从11月6日持续运行的旧8080 Java进程PID27420已不能处理健康请求：IPv4和IPv6 TCP可连接却在请求后无响应，5173登录经代理返回500。JVM现场线程记录保存在用户本机TEMP，未触碰数据库数据。
- 只针对经过命令行精确匹配的旧MES服务进行了停机；停止前备份JAR至 `%TEMP%\mes-boot-before-restart-20261108.jar`，SHA256 `BFC4B4D2B381095333BF2DDC5C6617715F978851F2DF20E90605873E02951A63`。
- 按当前本机正式代码重新 `mvn -pl backend/mes-boot -am -DskipTests package`：20模块 **BUILD SUCCESS**；启动新8080进程PID26432。Flyway报告 **validated 76 migrations / database version 076 / up to date**，健康 GET HTTP **200 {"status":"UP"}**。5173继续使用既有Vite实例，3306未停止也未重建。

## 已执行回归（全部实际返回PASS）
| 类别 | 实际结果 |
|---|---|
| Vue 3 / TypeScript + Vite `npm run build`（修正后） | PASS，4179 modules |
| `QcSpecificationModel.test.ts` | **6/6 PASS** |
| Java `QcSpecificationRulesTest` | **7/7 PASS** |
| Java真实DB `QcSpecificationIT` | **7/7 PASS** |
| Java `QcFinalContractIT` | **13/13 PASS** |
| 下游 `IncomingClosureIT` | **9/9 PASS** |
| 下游 `IncomingProductionIT` | **13/13 PASS** |
| 正式Chrome `qc-standard-current-live.spec.ts` | **2/2 PASS，0 FAIL/0 SKIP** |
| 代码差异 `git diff --check` | EXIT 0 |
| 8080健康及迁移 | HTTP200/UP，V076有效 |

Java累计本轮定向 **49/49 PASS**，前端模型6/6，Chrome2/2。定向测试通过不能代表全部MES或正式生产环境验收完成。

## 未结案事项
- 开发库QC标准列表当前共15条，真实Chrome只读扫描发现 **2条**业务字段带 `E2E/DBG/调试` 等不合规字样。这与用户禁止在业务字段出现调试/演示/测试等命名约定冲突；本轮没有擅自删改这些已有标准及其冻结关联。需要受控来源核对和数据治理处理，不能通过填充伪数据来改善页面。
- 本轮只做删除弹窗**取消/零写入**的前端实证；实际删除与维护记录保留已由真实数据库 `QcSpecificationIT` 验证，但还需隔离DEV标准的完整HTTP删除后追溯验收以及更多浏览器保存/异常用例。
- QC整套待交付项仍含V070/V072/V073、后端标准快照、来料/生产/成品下游适配和权限合同。此轮未审计全部待提交文件的发布门禁，也未执行完整G5 UI/UX / 数据迁移影响审查，故**不能提交或宣布ACCEPTED**。
- GitHub main的CI前端QC TypeScript失败与QC当前工作区完整API/后端未一起交付有关；暂不采用临时stub遮盖契约缺失。

## 当前精确文件SHA
- `frontend/mes-web/src/views/quality/QcSpecificationDetailView.vue`：`1FCB6D1B3084907A1CCB886742B312B6567B581A0CC0FE5523B423AB30E5D3E5`
- `frontend/mes-web/e2e/qc-standard-current-live.spec.ts`：`9D661C7B530D05388E51D25E6CEE0731E0FA5C8B752956C2C28F714B4613E1A7`

**下一门禁：** 根据已批准QC质量标准库冻结合同完成独立范围文件核查、外部引用快照与迁移负例、正式Chrome CRUD及角色负例，再决定成套提交/推送并观察GitHub CI。原工作区其他512+项改动必须完整保留。


## 2026-11-08 22:04 — 验收状态补正（用户直接确认）

**QC 基础信息功能：USER ACCEPTED（已通过验收）。** 用户最新明确：“qc基础信息功能修改过。已通过验收”。本文件开头记录的“G5 未通过/尚待人工验收”是 2026-11-08 17:18 当时的阶段结论，**不再代表 QC 基础信息功能的最新验收状态**。无需重复开展该功能的设计返工、页面重做或重复请用户签核。

此接受结论只适用于用户指出的 QC 基础信息功能；并不自动证明 GitHub 全仓库 CI、所有 QC 下游集成、V070/V072/V073 的远端交付和开发库历史命名治理问题已关闭。它们是独立的技术交付或数据治理事项，应记录并按需处理，不应与用户已接受的功能混为一谈。

此轮只作文本状态更新，未修改已验收功能源码、数据库、权限或生产/测试数据。
