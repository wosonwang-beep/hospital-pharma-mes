# 打印数据源配置详细设计文档复核 — 2026-10-10

状态：DESIGN REVIEW REQUIRED，正式开发NOT STARTED。本记录是文档一致性证据，不是业务测试、后端实现或最终验收。主设计契约来源为当前委派；既有原型批准和后续授权已记录在[005](../development/DCP-PRINT-DATA-SOURCE-CONFIGURATION-005-PROPOSED.md)。

读门/复用/影响分析及实体、DTO、API定义见005§1–7；直接依赖/迁移/TestCase/RTM见§9及[矩阵](../development/PRINT_DATA_SOURCE_CONTRACT_MATRIX_20261010.csv)。历史冻结版本、业务源码、现有未提交内容和已执行迁移保留。

## 本轮实际检查

- Python标准库解析OpenAPI JSON及检查本地$ref/operationId、required字段、样例字段、写操作Idempotency-Key/If-Match、一致响应包及实体/契约覆盖；命令/实际退出码与结果写入本记录后续检查节。
- 检查全部新增/修改Markdown本地链接及白名单；待安全同步后做定向git diff --check；不运行业务测试。
- 原型源码四文件hash重新读取，图稿hash来自真实原型交付记录；19项mock证据、正式NOT RUN及旧NOT RUN保持，不提升等级。
- 仓库源码迁移最高V051，未跟踪在途菜单迁移保留；当前数据库最大成功版本未查询，编号未分配。

## 待主审核

005§11第1/2项已由主设计定稿LIVE_LOOKUP和显示HALF_UP；剩余旧归档触发器兼容、类型元数据写入口/逻辑删除恢复以及规范化/组合FK验证待审核。对应项不擅自补业务代码或迁移。文档候选里的实体/DTO/API是完整可审阅定义，尚非批准实施契约。

## 修改白名单

- `docs/development/DCP-PRINT-DATA-SOURCE-CONFIGURATION-005-PROPOSED.md`
- `docs/api/print-data-source-configuration.openapi.json`
- `docs/development/PRINT_DATA_SOURCE_CONTRACT_MATRIX_20261010.csv`
- `docs/development/decisions/PRINT_CONFIGURATION_DESIGN_LOCK_20261010.md`
- `docs/development/decisions/PRINT_CONFIGURATION_UI_SPEC_20261010.md`
- `docs/architecture/CONTROLLED_WORD_PRINTING_DCP_2026-10-08.md`
- `docs/development/DCP-MES-NATIVE-DESIGNER-JAVA-PDF-002-APPROVED.md`
- `docs/development/DCP-CANVAS-PRINT-EDITOR-003-APPROVED.md`
- `docs/development/DCP-PRINT-TYPE-BATCH-004-APPROVED.md`
- `docs/PROJECT_BASELINE.md`
- `MES_TASKS.md`
- `docs/review/PRINT_DATA_SOURCE_DESIGN_REVIEW_20261010.md`

## 实际文档检查结果（本轮）

命令：`python print-source-design-stage/check_docs.py`（工作区暂存工具，非业务测试）。最终退出码0：标准库JSON解析、全部本地schema引用、operationId、schema required/示例结构、写操作幂等/版本头、现有响应包、矩阵operation映射、Markdown链接及白名单哈希，共2453条断言，错误0；53个schema、13个operation、10条RTM矩阵、12份文档。不是完整OpenAPI标准验证器或正式后端测试。首次检查发现矩阵最后一行使用操作说明而非operationId，已修正文档映射后通过，未改检查断言。

同步工具先验证全部原文件SHA256，保存原始字节到当前工作区 `print-source-design-stage/original-backup/`，再仅写入12份文档；任一并发变化则写前停止。同步结果及后续定向git diff --check另记，不宣称正式运行PASS。工作区manifest/sync-result/document-checks.json是本轮文件级证据，无凭据/业务样本。

业务源码、正式编译、单元/集成/浏览器测试、数据库、Flyway、PDF、部署：均NOT RUN/未修改。主设计已定稿单位LIVE_LOOKUP和显式displayValue舍入，剩余多个独立明细模板输出、类型元数据写入口/恢复流程、归档不可变迁移保护及规范化/组合FK验证交审核。

## 主设计八项修正 — 文档复核补充

状态改为主设计复核中 MAIN DESIGN REVIEW，用户已授权主对话批准，不等待用户重复确认。DCP005/OpenAPI/矩阵/原有引用已按八项决定修正；新增固定摘要样例PRINT_SOURCE_HASH_GOLDEN_V1.json是同一设计证据，不是第二基线。仅检查本轮差异、schema引用/操作映射/黄金规范字符串，未重跑旧2453断言或业务测试。现有字典V039已有org/id索引，字段key按既有DOCX字母数字白名单；多集合和sidecar已经是本次批准方向，不再列为设计未决。

本轮目的文档13份（原12份+黄金JSON），同步前完整哈希守卫与备份原始字节。Java JCS黄金验证及文档定向检查结果在执行后补记；正式编译/DB/Flyway/PDF/部署仍NOT RUN，当前未编码。

Java黄金样例实际命令：Java21源码模式运行工作区GoldenCheck.java，classpath为本机现有java-json-canonicalization-1.1.jar，退出码0、DOCUMENT_GOLDEN_VERIFIED=7。逐字比较规范化字符串及固定SHA256；中文/键乱序/字段顺序/小数字符串/类型集合/发布证据均匹配。黄金JSON记录全部输入、规范化投影、字符串和字面量。这是文档辅助验证，不是正式Java模块编译或业务测试。仅本轮差异检查结果见当前工作区print-source-design-review2/delta-checks.json，旧2453断言未重跑。

本轮定向文档差异检查退出码0：418条断言、18个操作、61个schema、12条矩阵、7个Java JCS固定样例，错误0。首次发现示例清理影响名为example的合法响应属性，已恢复该属性后通过；未改检查断言。正式业务测试仍NOT RUN。


## 主设计批准与实施下发 — 2026-10-10T13:02:40.670492+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION IN PROGRESS。来源：主对话01a115d2-8ace-7574-9a08-ac48dfc970cc本次委派，已审阅DCP005第1–11节及修正实体/API，批准同一文件及18操作/61schema契约作为实施基线。用户先前已明确授权主对话批准、任务做到收尾及必要DEV迁移/后端更新/测试；本记录不把主设计批准写成用户最终验收，不改历史审批。

本次白名单：mes-reporting打印领域/API/应用/持久化及直接消费provider、mes-boot定向测试与新增Flyway、正式Vue打印页面/API/路由/现有导航入口、当前同一设计/契约/复核/任务索引。现有其他菜单/权限/UI未提交改动保留，开始清单和文件备份位于本机print-source-implementation/worktree-before.txt及file-backup/。不修改旧迁移、旧归档正文/hash/触发器、冻结releases、无关业务，不提交或推送。

专项环境覆盖：本次主设计下发明确要求迁移先在隔离测试库验证，仅该验证允许专项隔离库；正式DEV仍为已有localhost:3306/hospital_pharma_mes_dev、Redis6379、后端8080、前端5173；不drop旧库、不repair旧checksum、不增加持久安全开关。真实目标/版本/备份可恢复确认后才执行DEV迁移。复杂打印集成初始预算90分钟，最多2轮自动修复，重复同根因立即报告；不改批准验收断言。当前实施开始，迁移/编译/业务/UI/PDF验证未运行，不能宣称验收或部署完成。


## 本任务修复上限有界例外及显式升级批准 — 2026-10-10T13:36:48.414288+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION IN PROGRESS。来源同一主对话本次委派：用户已授权主对话全权批准当前任务并做到收尾，主设计批准DCP005复杂前后端集成对MES_DEV_RULES §6A两轮修复上限作专项放宽，只覆盖本任务兼容/契约集成，不修改全局规则、不弱化断言和安全要求；逐原因定向修复/复测，同原因复发或需要改变设计立即报告，不无限循环。原Mockito撤权拒绝断言保留；失败是夹具在thenReturn参数构造时再次stubbing，先构造夹具即可修复，非权限校验通过。复杂集成预算仍按本任务90分钟记录，不因此扩至无限。

主设计批准现有Word/Canvas保存增加可选baseTemplateVersionId与sourceSelection(mode=INHERIT|UPGRADE,targetSourceRevisionId,targetConfigHash)。默认INHERIT，首次新模板解析唯一当前已发布源，同编码后续版本继承旧引用。UPGRADE需base及显式目标、同businessType/同源、当前绑定与ENABLED/PUBLISHED/类型覆盖，hash/目标冲突409，缺字段/集合兼容422带问题；复制新模板版本并重验，不修改旧版或静默删除绑定。LEGACY转CONFIGURED必须显式UPGRADE及完整兼容；前端仅增加版本升级差异预览/确认，不提供任意换源。本增量沿同一文件身份同步契约/矩阵后实施。此前BLOCKED仅为已获专项放宽的阶段记录，不是当前终止。
