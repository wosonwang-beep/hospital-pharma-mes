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
|G5|backend-unit、frontend-component、api-integration、migration、role-permission、state-transition、real-business、browser-e2e、visual-comparison、ui-ux-audit、boundary-negative、regression；菜单另需 menu-authorization|

每个 `test_plans[suite]` 含 `argv` 数组、`timeout_seconds`、environment、必要 runtime URL。仅 `run-evidence` 实际子进程运行后产生签名 receipt，捕获命令、开始时间、耗时、退出码、JUnit testcase 实际数量/失败/skip、原始日志及输出文件校验，绑定 plan 和整个前序 subject。手写 `passed=true`、未签名报告、篡改报告、复制旧结果、改代码/计划/产物/契约后继续使用旧确认均失败。

命令可用 `{python}`/`{root}`/`{junit}`，`MES_JUNIT_OUTPUT` 提供唯一新输出路径。测试适配器必须把真实测试框架的报告写入此路径，不能把 exit=0 伪造成 JUnit 通过；截图/控制台等原始附件写同一 run 输出目录，签名者审查测试脚本与实际环境。程序不运行 shell 拼接；子进程不继承签名私钥环境变量。失败命令、无 testcase、测试 failure/error 一律阻断；skip 需绑定本 gate、suite、确切 testcase 和原因的独立签名 SKIP_EXCEPTION。默认不跳过 Migration：无新增迁移仍须验证适用当前迁移状态并留执行结果，不接触本次任务的 DB。

G1 的 page_mapping.json：pages 中 id/template/requirements/fields/buttons/states/label_control="same-row"/viewports/tokens，模板 T1–T6，覆盖 scope.requirements。UI 实际美观、人眼审查在签名确认中承担，字段完整不等于审美自动 PASS。

G2 prototype_manifest.json：root、严格 stack 数组、commit、files（源路径→hash）、tree_sha256、page_sources、mock_adapter。源码只存在 mes-demo；本机复核真实文件/hash与提交。无 Git commit 必须独立有效批准 PROTOTYPE_HASH_EXCEPTION，不由脚本自创。截图清单含匹配视口 reference/rendered 对、hash 和审查标识，至少 1440/1280/900/375。Hosted CI 不读取工作站绝对路径，仅复核受信签名源清单/证据，不能声称重新验收本机原型。

G3 contracts.json：详细字段定义 + projections.page/database/api 完全一致、operations 按钮/前后端 permission/API/状态数据验证/成功失败/测试、states==backend_states、rtm、test_ids、prototype_sha256。freeze.json：FROZEN/id/owner/time/commit及此前设计/原型/当前详细设计每份产物的版本/hash，不得漏 API、权限、测试。DESIGN LOCK 复用现有 decisions 路径，不另造设计权威。

G4 lineage.json：原型 tree hash、每个 prototype_file/hash → implementation ref，frozen_ui 文件校验、unapproved_changes=[]；real_api.backend=8080 + operations；no_mock.production_mock_paths=[]。编译/真实 API/后端权限/安全/继承都须实际套件，不能仅宣告。G5 issues.json 阻断清零，CRITICAL/HIGH 例外需既有安全政策及签名 SECURITY_EXCEPTION；视觉 pairs 原型/正式截图同视口及审查，major_deviations=[]；完整集成报告 unapproved_contract_changes=[]，且 `ui_ux_audit={status:"PASS",verdict:"READY FOR ACCEPTANCE",audit_manifest:<标准文件引用>}`。该引用必须与 G5 `product_experience_review.audit_manifest` 完全一致。G2 browser 与 G5 实际 API/业务/E2E/视觉/UI-UX审查/角色/菜单证据必须 local-real；分别 5174 与 5173/8080，不接受 Mock。

## G2/G5 产品体验审查产物（2026-10-12）

完整流程 G2/G5 均强制新增 `artifacts.product_experience_review`，使用 JSON 文件及标准 `{path,version,sha256}` 引用；缺文件或校验不符会阻止推进。两阶段报告独立，不能复制原型体验结论冒充正式验收。已批准的低风险简化流程继续按现有范围决策办理，不自动扩大豁免。

报告字段：`reviewer={executor:"Codex SELF-REVIEW",skill:"mes-ui-ux-audit",skill_path:<仓库技能路径>,version:<实际版本或hash>,ui_ux_pro_max_path:<实际技能路径>,ui_ux_pro_max_sha256:<SHA256>}`，并包含 `visual_first=true`、`adversarial_pass=true`、`manifest_validation="PASS"` 和标准 `audit_manifest` 文件引用；`runtime.kind="local-real"`，G2 frontend 为 `http://localhost:5174`，G5 frontend 为 `http://localhost:5173` 且 backend 为 `http://localhost:8080`。`source_sha256` 在 G2 等于 prototype_manifest.tree_sha256，在 G5 等于 engine `digest(stage.source_files)`（规范 JSON 编码后的 SHA256）；另记录可读 commit/版本和时间。`inputs.requirements` 与 `inputs.detailed_design` 各为非空文件引用数组。

`checks` 每项含 page、area、scenario、result、evidence；覆盖 G1 每个页面的 `aesthetics/readability/interaction` 三类，每项 result 为 PASS，evidence 为非空标准引用数组，指向实际截图/DOM/操作日志等。`issues` 必须是数组，零问题用空数组；每项含 id/scenario/severity/blocking/resolved。resolved=true 必须有 `fix` 和 `recheck` 标准引用及 `rechecked_by="Codex SELF-REVIEW"`；未关闭项仅允许非阻断 MEDIUM/LOW/P3/P4 且有 deferred_reason，人工批准仍须明确这些技术债。模板另记录问题依据、修改文件、前后差异及复查结论。G5还必须执行独立签名的`ui-ux-audit`套件；其它集成套件通过不能抵消该套件或清单失败。

按工作流 §5.4B 在阶段报告记录整改批次和轮号：每批20轮未通过则先报告，真实批准后新批从1开始，旧批证据保留但不累计扣减新批额度；其它停止条件保留。此行为由执行者遵守并留记录，脚本不运行自动整改循环，也不自动生成继续批准。新增产物纳入既有 source/subject/签名与前序冻结清单，整改或报告变化使旧确认失效；浏览器与 E2E 等现有实际执行套件仍必需。脚本检查报告、源码绑定、覆盖、证据哈希和问题闭环，不自动判断审美或仅凭技能名称证明真实调用；真实执行由原始证据、受信执行记录和人类确认共同审查。

## 批准与信任配置

全维度补充（2026-10-12）：product_experience_review 的三类 checks 是摘要，不能替代 audit_manifest 全维度矩阵。每个 expected_surface 必须在 dimension_reviews 中覆盖 layout/typography/spacing_alignment/forms/tables_lists/query_filters/component_interaction/component_states/wcag22；整维度只能 PASS/FAIL/NOT RUN，不能 N/A/SKIP，每行有具体记录及真实证据。wcag22_checks 逐项覆盖审查矩阵列出的19个基础准则，真实不适用单项需原因/证据，漏项或未执行不能通过。G2/G5 门禁实际运行 mes-ui-ux-audit/scripts/validate_audit_manifest.py，并核对清单源码哈希及阶段结论，不信任手填 manifest_validation=PASS。原有所有套件、逐图/状态/视口、交互/测量、对抗审查和人类确认继续必需；不能以九维摘要简化流程。基础 WCAG 检查不等同完整合规声明。

采用 OpenSSH Ed25519 等 `ssh-keygen -Y` 支持的非对称签名。CI 只需要公钥 allowed_signers，**不需要且不应获得私人批准/证据密钥**。由真实批准主体/受信测试执行者保管各自私钥，AGENT 不得生成真实身份或批准。这次只在临时模拟目录生成测试身份，不用于真实 MES。

外部授权方设置 `MES_WORKFLOW_APPROVAL_SIGNERS` / `MES_WORKFLOW_EVIDENCE_SIGNERS` 为 trusted allowed_signers 文件路径；每行 `<principal> <public-key>`。其来源须为操作者信任库或 GitHub repository variables，不能由 PR 自己声明身份。运行者签名需要 `MES_WORKFLOW_EVIDENCE_PRIVATE_KEY` 和 `MES_WORKFLOW_EVIDENCE_PRINCIPAL`；真实人类确认记录签名使用 APPROVAL_PRIVATE_KEY/APPROVAL_PRINCIPAL。本程序不生成真实密钥、不改用户配置、不储存密钥于仓库。签名能防止未持钥者篡改，不是拥有工作站/私钥或修改受信 gate 的攻击者的安全边界；branch protection/可信代码审查仍必要。

批准载荷包括 feature/gate/decision/authority/approver/principal/timestamp/subject/source ref/simulation；签名独立覆盖全部。source 必须是实际用户确认或授权审批原始记录的文件及 hash。各 gate ACCEPTED 确认都必须签名；委托主体另需可核验委托来源，不允许 Codex 自封。operator-only `sign-approval` 可封装 **已存在** 的真实批准，不能创造批准。subject 每次产物/契约/版本/测试计划变更失效。脚本 `advance ACCEPTED` 必须重验 gate，并不自动将 MES_TASKS.md 接受。

## CI 状态和不可绕过范围

未启用示例在 `docs/governance/ci-examples/mes-feature-workflow.yml`，不在 .github/workflows。用户此次明确拒绝自动应用；既有 CI 未更改，未推送、未设必需检查/branch protection。仅未来获明确授权后才移入工作流目录并配置 trusted public signer variables、审批/分支保护。需 GitHub 权限时如实报告，不伪造启用。

示例保留真实 `pull_request` 触发、相同测试命令、diff范围选取、JSON artifact、失败 exit=1。业务/UI 改动必须匹配 feature.write_paths 且绑定 source_files；新增 feature 或该范围合并要求 G5 整条链。未登记的新业务变更阻断。治理自身改变运行 self-tests，不伪造业务 feature。测试及 validator 构成需要保护/审查的信任边界；不能把通过示例本机测试称为 GitHub 已实际运行。

## 低风险与专用页面

简化范围必须 scope.mode=simplified/risk=low/reason + G1 subject 下签名 SIMPLIFICATION；G1–G3 以 task_card/exemption_review和 scope-review 执行保留检查点，不直接略过阶段。仍需引用既有批准原型/冻结基线供 G4/G5 继承，不必重画。无既有原型证明就保持阻断并申请具体范围决策。专用编辑器提供批准专用交互/几何与实际套件，不一律跳过。
