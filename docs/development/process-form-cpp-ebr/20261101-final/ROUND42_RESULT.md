# 第42批：按已识别工序原型纠正规则

## 基线依据

用户最新「继续按原型开发」，沿用同一任务及累计41；本批42，不重置历史计数。实际读取两工程AGENTS、MES_DEV_RULES、MES_TASKS、PROJECT_BASELINE、用户识别原型追踪、BASIC_CURRENT_DEFINITION_CONTRACT、UI V2/T1–T6及BPM/G3.5参考。续作边界见 `../../decisions/PROCESS_WORKBENCH_IPC_CONTINUE_20261102.md`。本轮10:09:01–10:19:01窗口内完成代码与定向验证。

## 设计差异

旧原型错误地要求每套工艺必须包含完成条件为「检验结论合格」的IPC。当前已移除该全局校验，保留既有工艺身份、工序存在及编码唯一性检查。IPC提示改为读取当前工序配置的完成条件，并明确只影响依赖它的后续工序。未更改路线带、工序清单和当前工序分区布局。

## 修改范围

- `D:/codex/_project/gmp/mes-demo/src/features/processes/ProcessPrototype.vue`：上述两处行为/提示纠正。
- 同工程 `tools/process-draft-validation.test.mjs`：7个直接运行实际Vue校验控制器的定向用例。
- 同工程 `design/生产工艺/20261102/工序工作区规则修订.md`：当前版本和修订依据。
- 本任务范围记录、状态、证据和交接缺口。

正式MES页面、后端、数据库、API、迁移及业务数据本轮未修改。无新增迁移。修改前组件文本内容保存在 `round42-prototype-before.vue`，仅作证据，不参与构建。当前主组件 SHA256 `70577B39E6F5E531DED573B6E60E144DBA8739B98E0977EE5D13342F9E88623A`。

## 验收证据

| 门禁 | 结果 | 证据 |
|---|---|---|
| 根因复现 | 7测试，3通过4失败，均包含错误的全局IPC校验 | round42-prototype-red.log |
| 定向行为回归 | 7/7，失败/跳过0，exit0 | round42-prototype-green.log |
| 原型typecheck/build | exit0；Vite900ms，既有大包warning | round42-prototype-build.log |
| 源码差异 | 仅删除全局IPC检查、替换现有IPC提示；完整源摘要另存 | round42-source-evidence.json |
| 当前原型真实浏览器/UI审查 | NOT RUN | 未启动第二端口或切换正式5173 |
| 正式MES工作区/IPC保存/执行验收 | NOT RUN；整体G5仍FAIL | 本批未移植正式页面 |

SELF-REVIEW。行为测试检查无IPC、有IPC、删除IPC后普通工序仍合法、不同完成条件不被全局覆盖，以及重复编码、空工序、缺必填仍拒绝；不把草稿校验当运行期质量门禁。

## 结论与具体交接缺口

本次原型规则纠正已定向验证，BPM-IPC-002为SOURCE FIX VERIFIED / FULL PROTOTYPE REVIEW PENDING；生产工艺仍IN PROGRESS，不能READY/ACCEPTED。

正式移植须先完成以下明确映射，不可用演示控件冒充真实保存：

| 原型内容 | 当前真实合同 | 交接处理 |
|---|---|---|
| 路线带、左工序选择、右配置 | currentDefinition.route.operations | 直接复用原型结构，绑定既有工序编码/顺序/前置关系，不增加业务实体。 |
| 工序类型「生产/过程检验」 | Operation DTO无类型字段；只有ipcDefinitions | 明确持久化合同后才允许迁移，不能用编码前缀或是否存在检验项猜类型，也不能只在内存保存。 |
| 工序完成条件 | CompletionRule.kind/rules；运行期另有逐工序IPC门禁 | 显示文案逐一映射既有安全规则及质量门禁，不能把中文文本当新表达式执行。 |
| 记录表单/CPP/eBR入口 | 既有工艺上下文、模板ID＋表单编码及设计器路由 | 沿用已修正的模板身份和现有设计器，不新建第二套表单或规则引擎。 |
| 检验项与标准 | ipcDefinitions的required/resultType/上下限/预期文本/单位/方法引用 | 保留逐工序定义；不将示例百分比写入真实业务数据。 |
| 角色/设备 | requiredRole/requiredEquipmentType；eBR签署角色另用ID合同 | 不把工序角色编码和eBR角色ID混用。 |
| 保存 | PUT /process-packages/{id}/current-definition＋Idempotency-Key | 原子保存当前定义；不恢复旧工艺版本创建/提交/批准。 |
| 旧版本及审批区域 | 当前定义合同已退役这些动作 | 与原型说明存在冲突，不能静默当作正式实施合同；先补齐一致的详细设计。 |

BPM技能明确「G3.5 未 READY 则不编写正式代码」；当前上述类型/条件/生命周期映射未闭合，不能把本批原型修正冒充正式移植。保留完整原型和历史证据，继续以用户识别工作区为目标补齐交接，不重新画另一套。

源摘要复核：当前原始字节SHA为70577B39...，LF归一化文本SHA为FACA9FF9...；修改前内容快照通过文本方式保存，行尾可能不同，不冒称原始字节副本。样式/结构比较按规范化文本执行。Planning远端上轮传输失败，本轮最新事实保存在MES_TASKS及ownership/continuation，未宣称远端已同步。
