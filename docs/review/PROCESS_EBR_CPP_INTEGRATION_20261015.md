# 生产工艺、工序、EBR 表单与 CPP 定向集成回归 — 2026-10-15

状态：ACCEPTED — 用户于2026-10-15 22:44（Asia/Shanghai）明确回复“验收”。验收限于本次定向集成回归与修复，不扩展为全系统或实际生产批次端到端验收。

## 1. 基线依据

本机读取 AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md 相关任务及 docs/PROJECT_BASELINE.md 路由。业务权威 FINAL BASELINE v1.0.23，加 DCP-PROCESS-DYNAMIC-FORM-CPP-001-APPROVED、PROCESS_DYNAMIC_FORM_CPP_PROTOTYPE_20261013 设计锁及 ROLE_ID_SIGNING_CONTRACT。读取 CPP 需求/详细设计与实施报告、Global UI V2、T1–T6、数据库验证策略和本机 mes-ui-ux-audit / ui-ux-pro-max。生产工艺新版优化原型仍标 PENDING HUMAN APPROVAL，本轮只测试既有正式功能，不将其作为重新设计授权。

本次授权来自用户“集成测试生产工艺管理：完成工艺、工序及 EBR 表单和 CPP 规则测试”。本机 main 原工作区大量未提交修改全部保留。仅使用同一 DEV MariaDB / Redis 与 8080 / 5173，无第二工作区或数据库。

## 2. 设计差异与修复

- 旧 EbrIT 定义构造夹具缺 schemaVersion 2.0，导致首轮 19 项中 4 项在保存夹具时报错，15 项通过。采用已有 EbrDefinitions.copyInput 转换到批准的角色 ID 输入合同，负例派生定义携带原 schemaVersion；不修改生产校验或断言。68 个 assertThat/andExpect 片段与本轮前文件逐段比较一致。
- CPP 网格按整个视口换列，展开侧栏时实际编辑容器不足，1440 下右栏被 overflow:hidden 裁切；改为命名容器宽度查询，不改字段/动作/权限/业务语义。
- 表单设计器属性栏在 1280 同样被裁切；沿用已有两列+底部属性栏结构，断点改为实际容器宽度。原 CSS 页面重现测量 right=1421、parentRight=1256；当前源码 right=1255、parentRight=1256。重现只注入临时页面 CSS，已移除。
- 原始失败日志保留；测试输入修复 1 轮、界面定向修复 2 轮，未触及修复上限。普通 UI 修改与复测在 10 分钟窗口内完成。

## 3. 修改范围

实际源文件：
- backend/mes-boot/src/test/java/com/hospital/mes/ebr/integration/EbrIT.java：只修改定义夹具输入。
- frontend/mes-web/src/views/ebr/DefinitionDesigner.vue：命名容器与表单设计器断点。
- frontend/mes-web/src/views/ebr/CppRuleDesigner.vue：CPP 容器断点。
- 本报告、MES_TASKS.md，以及本轮 artifacts/process-integration-20261015 和临时验证脚本。

未新增迁移，启动/IT 均 validate 64 migrations PASS，当前 V064，无待执行迁移。集成夹具在原测试事务内回滚；浏览器仅认证、只读业务请求和本地未持久化规则交互，未保存现有业务模板。未改旧迁移、历史模板、生产批次、签名、角色授权；未提交或推送。

服务原未运行，本轮启动现有本机 boot JAR（SHA256 F2EF257FC7014FB1C8D775EA431555189FFF07DD6F9ACC48E78D785DEB189E10），后端 PID36384 / 8080；Vite 当前源码 PID26660 / 5173。当前前端组合 SHA256 C10BA53A8E6C78C5BE97260889977870A847C1E89E4BC95AA2009C96789EB567。数据库集成使用 Maven 从实际工作树编译执行；本轮未重新打包后端 JAR，不据此宣称后端新发布。

## 4. 验收证据

证据根目录：artifacts/process-integration-20261015/。

| 检查 | 结果 | 证据 |
|---|---|---|
| 工艺规则 + EBR 单元 | 5 + 61 = 66/66，失败/错误/跳过 0，reactor exit0 | backend-unit.log |
| ProcessIT + DirectProcessActivationIT + 5项 EbrIT | 9 + 5 + 5 = 19/19，失败/错误/跳过 0，Maven exit0 | database-integration-round1.log |
| 首轮集成失败保留 | 19项，4 errors；旧夹具 schema2 缺失 | database-integration.log |
| 前端 model/runtime | 2 files / 7 tests PASS，exit0 | frontend-tests.log |
| 最终 vue-tsc + Vite build | PASS，exit0；既有大包 warning 保留 | frontend-build-final.log |
| 真实 5173/8080 关联和页面链路 | 16/16 PASS；7个工艺盘点，实际有表单的2个工艺共7份表单归属通过；其余5个为空不称已有完整配置 | after/live-results.json |
| CPP 页面交互 | 空规则、必填失败、条件组增删、字段搜索恢复、预览/DSL、页签状态保留、只读禁止新增/保存 PASS | g5/interaction.json |
| CPP 边界测量 | 1440/1280 各字段/配置/说明区均在编辑容器内 | after/live-results.json |
| G5 ui-ux-audit | 5 states × 4 viewports = 20张；清单校验 exit0 | g5/audit-manifest.json，g5-validator-final.log |
| 测试断言保护 | 68片段一致 | acceptance-summary.json |
| 定向 diff --check | exit0，只有既有 CRLF 提示 | 本轮命令输出 |

核心集成命令：`mvn -f backend/pom.xml -pl mes-boot -am test -Dtest=ProcessIT,DirectProcessActivationIT,EbrIT#cppVisualConfigPersistsInExistingRuleRowAndRoundTrips+normalizedLifecyclePublishedSnapshotAndVersionCopyStayImmutable+disabledRoleAndForeignProcessOperationFailLintAndPreserveDraft+independentApprovalHashAndUnsafeExpressionsFailClosed+simulationProducesBlockEvidenceWithoutRuntimeRows -Dsurefire.failIfNoSpecifiedTests=false`。

覆盖工艺保存生效、工序环路拒绝/事务原子性、单位物料关联、组织/权限、幂等/旧命令禁用；EBR定义保存/发布/复制冻结、错误工序/停用角色、独立审批与篡改/DSL拒绝、超限阻断证据、CPP配置数据库往返。运行引擎单元包含灭菌参数范围及 ON_CHANGE 警告/ON_SUBMIT 阻断。

执行者：Codex SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max。本机 Browser 插件技能不可用，使用项目既有 Playwright。逐图查看最终20张状态图，发现并整改上述裁切；与初始截图、源快照及现有设计锁对照。全页长图中 sticky shell 可能出现在拼接中段，视口图及独立边界测量另存。原属性栏首次截图被复测覆盖，已明确记录，并用原 CSS 重现图补证，未冒称历史原图。

integration_acceptance.ui_ux_audit 与 product_experience_review.audit_manifest 均引用同一绝对路径的本轮 g5/audit-manifest.json；见 acceptance-summary.json。审查生成脚本继承的历史问题已链接原历史证据，新问题单列。原清单证据路径缺失与 stack 分组元数据错误的校验失败保留，不修改校验器、阈值或业务断言。

## 5. 结论与限制

本次定向工艺—工序—EBR—CPP 集成回归已由用户确认验收，ACCEPTED。并非整套生产工艺新版原型已集成或全系统验收。

本轮没有在真实生产批次上执行全程填写、签名、偏差生成与 QA 放行；CPP业务保存持久化由真实数据库事务IT验证，浏览器配置为未落库交互。未覆盖真实窄权限账号页面完整矩阵、所有生产工艺CRUD状态或全链路批次并发。现有业务链路初始登录前/直接跳转有认证401恢复记录（after/live-results.json）；G5目标编辑器监控区间console/pageerror与失败HTTP均为空。保留已有 bundle 大包与 MariaDB/Flyway版本兼容警告。不得将这些范围外内容写成已通过；本次 ACCEPTED 仅代表用户已确认上述有界交付。


## 人工验收登记 — 2026-10-15 22:44（Asia/Shanghai）

用户在收到本轮测试数量、修复范围和未包含真实生产批次签名/偏差/QA放行链路的报告后，明确回复“验收”。本次仅更新任务状态及本报告的人验登记，沿用原始测试/G5证据；不改判历史失败、不重跑测试、不修改业务源码或数据、不提交推送。
