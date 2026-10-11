# mes-feature-workflow 安装与验收报告

日期：2026-10-10（Asia/Shanghai）。结论：**安装完成，工作流自身验证 PASS；未启动任何业务功能开发，不构成任何 MES 功能 ACCEPTED。**

## 1. 最终用户授权与调用

最初要求创建完整五阶段技能及可执行配套。用户随后明确选择“仅调用 $mes-feature-workflow 时使用”，并确认自然语言“使用 MES 标准流程开发 XX 功能”具有相同显式启用含义。最终配置 `allow_implicit_invocation: false`；普通开发请求不启用新增流程。自然语言是明确请求技能的语义约定，未伪称注册平台 alias API。

CI 仅保存未启用示例，不在 `.github/workflows`。**未修改 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、既有 UI/业务基线或既有 CI。** AGENTS.md 新增强制规则：**无**，这是用户后续收窄授权的结果。原有工程约定保持独立适用。

安装路径：`D:/codex/_project/gmp/hospital-pharma-mes/.agents/skills/mes-feature-workflow`。由真实 skill-creator/init_skill.py 初始化；保持项目级安装，没有额外用户级副本、工作区副本、提交或推送。

后续启动：**“使用 MES 标准流程开发 XX 功能”**，或 `$mes-feature-workflow 开发 XX 功能`。先检查现有任务/基线/批准范围；不是授权跳过设计或原型确认。

## 2. 基线与环境读取

确认指定路径及 `main`，已有大量未提交业务/设计/配置修改，全部保留。本次写白名单只含新技能、配套治理/模板/脚本/自身证据/报告。真实读取 AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md、docs/PROJECT_BASELINE.md、v1.0.23 当前冻结 manifest、Global UI V2/T1–T6、页面目录及现有 CI约定；依现有文档路由继承 API、权限、状态/数据库规则，无业务契约改动。

既有原型 `D:/codex/_project/gmp/mes-demo/package.json` 含 Vue3/TS/Vite/Ant Design Vue/Pinia/Router，但目录无 Git 仓库；如后续 G2 要求提交号，必须提供真实提交或具体获批的哈希例外，不虚构提交。未修改原型工程。正式页面仍5173/8080，原型仍5174；本次没有启动/访问业务运行服务、MariaDB或Redis，没有创建迁移、修改表/API/页面。

## 3. 实际新增与修改

所有交付文件为新增；新 Skill 的初始 scaffold 在安装过程中被完善。**既有文件由本任务修改：零。** 完整逐文件清单和 SHA256 见 [install-manifest.json](evidence/mes-feature-workflow-install/install-manifest.json)。文件分组：

|目录/文件|交付内容|
|---|---|
|.agents/skills/mes-feature-workflow/SKILL.md|主控五阶段、显式启用、继承原有规则|
|.agents/skills/mes-feature-workflow/agents/openai.yaml|UI元数据、allow_implicit_invocation=false|
|.agents/skills/mes-feature-workflow/references/workflow-v2-source.md|用户完整十五章原文，逐字保留|
|.agents/skills/mes-feature-workflow/references/tool-handoff.md|实际工具能力、跨环境交接、Vue/提交缺口|
|.agents/skills/mes-feature-workflow/scripts/workflow.py|G1–G5、状态推进、真实测试执行、SSH签名验证、范围CI入口、自然语言识别辅助|
|.agents/skills/mes-feature-workflow/scripts/verify_discovery.py|真实 app-server skills/list 只读探针|
|.agents/skills/mes-feature-workflow/scripts/tests/fixture_suite.py|真实子进程模拟断言/JUnit，标明非业务测试|
|.agents/skills/mes-feature-workflow/scripts/tests/test_workflow.py|42项隔离正反例，不触碰业务数据|
|.agents/skills/mes-feature-workflow/scripts/tests/record_gate_examples.py|保存各gate实际正/反向模拟结果|
|docs/governance/MES_FEATURE_WORKFLOW.md|完整五阶段规范及十五章覆盖映射|
|docs/governance/MES_GATE_POLICY.md|产物、契约、实际证据、签名与CLI说明|
|docs/governance/MES_CHANGE_CONTROL.md|继承既有RFC/冻结/影响/重开/重新验收机制|
|docs/governance/ci-examples/mes-feature-workflow.yml|未启用GitHub Actions示例|
|docs/templates/FEATURE_TASK_TEMPLATE.md|功能任务卡|
|docs/templates/UI_REVIEW_TEMPLATE.md|G1 UI审查|
|docs/templates/PROTOTYPE_ACCEPTANCE_TEMPLATE.md|G2原型验收|
|docs/templates/DESIGN_FREEZE_TEMPLATE.md|G3冻结及字段/操作清单|
|docs/templates/INTEGRATION_ACCEPTANCE_TEMPLATE.md|G5实际集成报告|
|docs/templates/FEATURE_WORKFLOW_TEMPLATE.yaml|初始阶段manifest，JSON-compatible YAML|
|scripts/validate-workflow.ps1、scripts/validate-workflow|Windows/portable真实CLI包装与退出码|
|docs/review/evidence/mes-feature-workflow-install/|实际自测、原始调用、识别、门禁和范围验证证据|
|本报告|最终交付、测试、限制和状态|

## 4. 五阶段与证据门禁

G1 UI设计与具体版本确认 → G2 Vue交互原型/实际浏览器/源码确认 → G3详细设计/字段-API-DB/权限/状态/RTM一致性及冻结 → G4复用原型/真实业务接入 → G5完整真实集成与人类最终确认。

每阶段独立NOT_STARTED/IN_PROGRESS/REVIEW_PENDING/BLOCKED/REJECTED/ACCEPTED；前阶段需ACCEPTED且签名/版本/文件/实际测试均仍有效。程序不会更新MES_TASKS.md；人类批准不能由脚本PASS代替。必需产物与套件固定；结构化JSON和Markdown报告；失败非零。实际测试执行绑定输入源哈希、命令、时间、退出、JUnit、日志，SSH签名，手写passed=true/篡改/过期证据不通过。视觉审美仍由实际截图和有效审查批准承担，程序只核查证据、版本和完整性。

## 5. 实际自测结果

命令：`python .agents/skills/mes-feature-workflow/scripts/tests/test_workflow.py --output docs/review/evidence/mes-feature-workflow-install/selftest.json`。

**42 tests / 0 failures / 0 errors / 0 skipped / 69.003秒（结构化报告四舍五入69.0秒），退出0。** [JSON](evidence/mes-feature-workflow-install/selftest.json)、[JUnit](evidence/mes-feature-workflow-install/selftest.xml)。首轮37项通过，独立审查指出三项覆盖缺口后增强并重跑42项；未修改业务验收断言或既有质量规则。

模拟记录均WF-TEST-*、SIMULATION，隔离临时fixture和临时SSH测试身份；测试私钥已随临时目录清理，不作为人类批准或真实MES证据。模拟完整交付物的测试子进程实际执行，并不代表运行过MES页面、API或数据库。

## 6. G1–G5 正反向结果

命令：`python .agents/skills/mes-feature-workflow/scripts/tests/record_gate_examples.py --output-dir docs/review/evidence/mes-feature-workflow-install/gates`。保存实际执行结果，未手工填写PASS。

|Gate|完整模拟交付|典型违规|实际反向结果|
|---|---|---|---|
|G1|PASS|缺设计稿|FAIL，阻止推进|
|G2|PASS|缺Vue原型|FAIL，阻止推进|
|G3|PASS|字段/API不一致|FAIL，阻止推进|
|G4|PASS|前序基线未冻结|FAIL，阻止推进|
|G5|PASS|实际测试进程集成断言失败|FAIL，阻止推进|

各Gate正向JSON/Markdown、反向JSON/Markdown和真实执行子进程矩阵：[gate-matrix.json](evidence/mes-feature-workflow-install/gates/gate-matrix.json)。另覆盖未确认原型、非法模板、标签上下行、React替代、build冒充browser、mock冒充运行、按钮权限/状态/RTM冲突、生产Mock、源码继承漏页、各页视口截图漏项、阻断P2、未批契约变更、伪造批准/报告、无信任配置、路径逃逸、skip未批、模拟冒充真实、非法阶段推进/重开等。

Windows与portable入口的未创建feature负例均实际返回1；CI入口同失败退出码由套件验证。GitHub Actions本身**NOT RUN / NOT ENABLED**，不伪称远程执行过。

## 7. Skill实际发现与调用

`skill-creator/scripts/quick_validate.py` 实际输出 `Skill is valid!`。验证使用临时PyYAML依赖目录与 `python -X utf8`，未修改系统Python或用户配置；门禁本身仅标准库+已有OpenSSH。

实际 `codex app-server --stdio` → initialize → `skills/list(forceReload=true,cwd=项目)`：找到唯一enabled、repo-scope的mes-feature-workflow并读取正确安装路径，见 [discovery.json](evidence/mes-feature-workflow-install/discovery.json)。

实际 `codex exec --ephemeral --sandbox read-only` 使用自然语言“使用MES标准流程开发 WF-TEST-REQUEST-002 菜单功能”（明确只读安装测试），成功找到/读取新Skill和既有强制规则、正确报告G1–G5、缺批准不得推进、普通任务不触发，进程退出0。见 [实际回答](evidence/mes-feature-workflow-install/invocation-answer.txt) 与 [原始events](evidence/mes-feature-workflow-install/invocation-events.jsonl)。未持久化新业务chat或任务；没有业务修改。

独立只读forward-testing验证模拟菜单请求停在READ/G1 BLOCKED、G2–G5 NOT_STARTED；普通请求不触发。其发现的截图覆盖、委托来源、页面继承三个缺口已修复，二次复核无新阻断项。真实suite负责42项结果，独立审查未重复运行或改文件。

## 8. 工具与CI实际状态

UI UX Pro Max：实际读取本机ui-ux-pro-max技能并执行search.py --help，包含--stack vue；不是单一MCP调用，不运行业务设计任务。Product Design：实际读取router/image-to-code；可以按技能和Browser等能力组织工作，不保证环境直接生成所需Vue工程，未假称生成/验收原型。跨环境采用源码/文件SHA/Git提交/原始截图与日志/批准来源交接，必要时申请限定Vue构建方式。

CI示例YAML实际解析通过、包含selftest和feature-gates两job；diff功能白名单/未注册业务文件/源码证据绑定、整链G5检查、JSON输出与失败退出码已有本机测试。按最终用户选择不放入.github/workflows，未推送/启用/设branch protection或required checks。将来启用须单独明确授权和GitHub权限。

## 9. 遗留与限制

- 真实批准和测试执行签名身份/公钥信任库尚未配置，真实Gate会fail-closed；这是有意不伪造批准的边界。CI只需受信公钥，不能接收人类私钥。
- mes-demo未有Git提交，需要真实提交或人类批准确切哈希例外，不能由此安装隐式改变项目。
- 各功能实际测试适配器、真实原型/设计/源码/服务及确认都需在该功能显式启动后交付；本次只验证工作流自己。
- 受信执行者仍须审查测试脚本/实际服务，签名不能防持钥者或工作站控制者作假；代码审查/既有分支保护仍必要。程序不判断审美。
- CLI调用时原有cuawright marketplace配置发出升级超时warning，未影响本次只读调用结果；未修改此既有插件配置。

## 10. 范围、迁移与最终状态

本任务无Flyway新增/修改、无DB数据/业务API/UI变更、无MES_TASKS状态变更、无Git提交/推送。受保护AGENTS/MES_DEV_RULES/原ci.yml与HEAD的只读diff检查均为零；其它原有dirty文件不重置、不过度归因。[范围验证](evidence/mes-feature-workflow-install/scope-preservation.json)。

状态：**技能安装与自身验收完成；仅显式使用；CI示例未启用；真实业务批准/门禁尚未执行。**
