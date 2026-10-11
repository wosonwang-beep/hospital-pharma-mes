# G1–G5 可执行门禁与证据信任

仅由显式 mes-feature-workflow 调用。未改变项目原有规则，未自动启用 CI。

## CLI

Python 3.10+，仅标准库；签名依赖已有 OpenSSH `ssh-keygen` 支持 `-Y sign/verify`。本机已找到 Windows OpenSSH，Hosted Ubuntu 同样需支持。所有命令从唯一正式仓库运行。

```powershell
python .agents/skills/mes-feature-workflow/scripts/workflow.py init --feature MES-NEW-001
python .agents/skills/mes-feature-workflow/scripts/workflow.py route-request --request "使用MES标准流程开发XX功能"
./scripts/validate-workflow.ps1 --feature MES-NEW-001 --gate G1
./scripts/validate-workflow.ps1 --feature MES-NEW-001 --gate G2
./scripts/validate-workflow.ps1 --feature MES-NEW-001 --gate G3
./scripts/validate-workflow.ps1 --feature MES-NEW-001 --gate G4
./scripts/validate-workflow.ps1 --feature MES-NEW-001 --gate G5
python .agents/skills/mes-feature-workflow/scripts/workflow.py advance --feature MES-NEW-001 --gate G1 --state IN_PROGRESS
python .agents/skills/mes-feature-workflow/scripts/workflow.py run-evidence --feature MES-NEW-001 --gate G1 --suite design-review
python .agents/skills/mes-feature-workflow/scripts/workflow.py subject --feature MES-NEW-001 --gate G1
```

POSIX 可 `python scripts/validate-workflow --feature ... --gate G1`；接口不需要全局 PATH 安装。失败退出 1，通过 0。各 gate 重验全部前序链，不仅信任状态文本。报告默认 feature/evidence/Gn-gate.json + .md；未登记 feature 返回结构化 FAIL，`--output` 可将此类失败也持久化。

## Manifest 与必要证据

workflow.yaml 使用 JSON-compatible YAML 1.2，避免引入 YAML 解析依赖，初始结构在 FEATURE_WORKFLOW_TEMPLATE.yaml；不能使用任意 YAML 标签/表达式。每产物 `{path,version,sha256}` 相对仓库路径，禁止逃逸。强制 reads 对应基线 SHA256；stage version/commit 要真实可解析 Git commit，source_files 绑定测试及源代码输入；功能写白名单不能整仓通配。前序 ACCEPTED 与有效确认缺一失败。

必要产物列表由 engine `ARTIFACTS` 固定；必要测试由 `SUITES` 固定，不能从 manifest 删除套件降低门槛：

|Gate|实际套件|
|---|---|
|G1|design-review|
|G2|prototype-build、prototype-browser、prototype-console、prototype-visual|
|G3|contract-consistency、rtm|
|G4|backend-compile、frontend-build、backend-unit、frontend-component、api-wiring、permission-validation、migration、security、ui-inheritance|
|G5|backend-unit、frontend-component、api-integration、migration、role-permission、state-transition、real-business、browser-e2e、visual-comparison、boundary-negative、regression；菜单另需 menu-authorization|

每个 `test_plans[suite]` 含 `argv` 数组、`timeout_seconds`、environment、必要 runtime URL。仅 `run-evidence` 实际子进程运行后产生签名 receipt，捕获命令、开始时间、耗时、退出码、JUnit testcase 实际数量/失败/skip、原始日志及输出文件校验，绑定 plan 和整个前序 subject。手写 `passed=true`、未签名报告、篡改报告、复制旧结果、改代码/计划/产物/契约后继续使用旧确认均失败。

命令可用 `{python}`/`{root}`/`{junit}`，`MES_JUNIT_OUTPUT` 提供唯一新输出路径。测试适配器必须把真实测试框架的报告写入此路径，不能把 exit=0 伪造成 JUnit 通过；截图/控制台等原始附件写同一 run 输出目录，签名者审查测试脚本与实际环境。程序不运行 shell 拼接；子进程不继承签名私钥环境变量。失败命令、无 testcase、测试 failure/error 一律阻断；skip 需绑定本 gate、suite、确切 testcase 和原因的独立签名 SKIP_EXCEPTION。默认不跳过 Migration：无新增迁移仍须验证适用当前迁移状态并留执行结果，不接触本次任务的 DB。

G1 的 page_mapping.json：pages 中 id/template/requirements/fields/buttons/states/label_control="same-row"/viewports/tokens，模板 T1–T6，覆盖 scope.requirements。UI 实际美观、人眼审查在签名确认中承担，字段完整不等于审美自动 PASS。

G2 prototype_manifest.json：root、严格 stack 数组、commit、files（源路径→hash）、tree_sha256、page_sources、mock_adapter。源码只存在 mes-demo；本机复核真实文件/hash与提交。无 Git commit 必须独立有效批准 PROTOTYPE_HASH_EXCEPTION，不由脚本自创。截图清单含匹配视口 reference/rendered 对、hash 和审查标识，至少 1440/1280/900/375。Hosted CI 不读取工作站绝对路径，仅复核受信签名源清单/证据，不能声称重新验收本机原型。

G3 contracts.json：详细字段定义 + projections.page/database/api 完全一致、operations 按钮/前后端 permission/API/状态数据验证/成功失败/测试、states==backend_states、rtm、test_ids、prototype_sha256。freeze.json：FROZEN/id/owner/time/commit及此前设计/原型/当前详细设计每份产物的版本/hash，不得漏 API、权限、测试。DESIGN LOCK 复用现有 decisions 路径，不另造设计权威。

G4 lineage.json：原型 tree hash、每个 prototype_file/hash → implementation ref，frozen_ui 文件校验、unapproved_changes=[]；real_api.backend=8080 + operations；no_mock.production_mock_paths=[]。编译/真实 API/后端权限/安全/继承都须实际套件，不能仅宣告。G5 issues.json 阻断清零，CRITICAL/HIGH 例外需既有安全政策及签名 SECURITY_EXCEPTION；视觉 pairs 原型/正式截图同视口及审查，major_deviations=[]；完整集成报告 unapproved_contract_changes=[]。G2 browser 与 G5 实际 API/业务/E2E/视觉/角色/菜单证据必须 local-real；分别 5174 与 5173/8080，不接受 Mock。

## 批准与信任配置

采用 OpenSSH Ed25519 等 `ssh-keygen -Y` 支持的非对称签名。CI 只需要公钥 allowed_signers，**不需要且不应获得私人批准/证据密钥**。由真实批准主体/受信测试执行者保管各自私钥，AGENT 不得生成真实身份或批准。这次只在临时模拟目录生成测试身份，不用于真实 MES。

外部授权方设置 `MES_WORKFLOW_APPROVAL_SIGNERS` / `MES_WORKFLOW_EVIDENCE_SIGNERS` 为 trusted allowed_signers 文件路径；每行 `<principal> <public-key>`。其来源须为操作者信任库或 GitHub repository variables，不能由 PR 自己声明身份。运行者签名需要 `MES_WORKFLOW_EVIDENCE_PRIVATE_KEY` 和 `MES_WORKFLOW_EVIDENCE_PRINCIPAL`；真实人类确认记录签名使用 APPROVAL_PRIVATE_KEY/APPROVAL_PRINCIPAL。本程序不生成真实密钥、不改用户配置、不储存密钥于仓库。签名能防止未持钥者篡改，不是拥有工作站/私钥或修改受信 gate 的攻击者的安全边界；branch protection/可信代码审查仍必要。

批准载荷包括 feature/gate/decision/authority/approver/principal/timestamp/subject/source ref/simulation；签名独立覆盖全部。source 必须是实际用户确认或授权审批原始记录的文件及 hash。各 gate ACCEPTED 确认都必须签名；委托主体另需可核验委托来源，不允许 Codex 自封。operator-only `sign-approval` 可封装 **已存在** 的真实批准，不能创造批准。subject 每次产物/契约/版本/测试计划变更失效。脚本 `advance ACCEPTED` 必须重验 gate，并不自动将 MES_TASKS.md 接受。

## CI 状态和不可绕过范围

未启用示例在 `docs/governance/ci-examples/mes-feature-workflow.yml`，不在 .github/workflows。用户此次明确拒绝自动应用；既有 CI 未更改，未推送、未设必需检查/branch protection。仅未来获明确授权后才移入工作流目录并配置 trusted public signer variables、审批/分支保护。需 GitHub 权限时如实报告，不伪造启用。

示例保留真实 `pull_request` 触发、相同测试命令、diff范围选取、JSON artifact、失败 exit=1。业务/UI 改动必须匹配 feature.write_paths 且绑定 source_files；新增 feature 或该范围合并要求 G5 整条链。未登记的新业务变更阻断。治理自身改变运行 self-tests，不伪造业务 feature。测试及 validator 构成需要保护/审查的信任边界；不能把通过示例本机测试称为 GitHub 已实际运行。

## 低风险与专用页面

简化范围必须 scope.mode=simplified/risk=low/reason + G1 subject 下签名 SIMPLIFICATION；G1–G3 以 task_card/exemption_review和 scope-review 执行保留检查点，不直接略过阶段。仍需引用既有批准原型/冻结基线供 G4/G5 继承，不必重画。无既有原型证明就保持阻断并申请具体范围决策。专用编辑器提供批准专用交互/几何与实际套件，不一律跳过。
