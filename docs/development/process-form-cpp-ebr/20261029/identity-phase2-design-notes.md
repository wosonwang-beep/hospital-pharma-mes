# 工序表单来源识别 — 原型设计说明（草稿）

状态：IN PROGRESS / NOT READY / NOT APPROVED。来源：用户批准方向的 `../../decisions/PROCESS_FORM_TEMPLATE_IDENTITY_20261029.md`；当前工艺T4和Global UI V2；不恢复工艺版本审批。原型源：`D:/codex/_project/gmp/mes-demo/src/features/processes/ProcessTemplateIdentity20261029.vue`，SHA256 `D7189AED1FE6E842EDF6C6EB32B611E2EB01AFA6E0C8A9E89D6970FEC8AAE44B`。这里的哈希不代表用户批准。

## 页面与数据来源

- 工艺维护人员先识别工艺、工序，然后定位表单所属模板，再进入对应模板；工艺当前定义与eBR模板版本分开显示。
- 结构沿现有T4：工艺标识/返回 → 工艺路线、工序表单、过程检验IPC、参数上下限 → 工序标题及表单列表。现原型仅OP01可见，其余工序仍缺失。
- 列顺序：表单名称、表单编码、所属模板（名称上、编码下）、模板版本及状态、字段数、操作。`EbrDetail.templateName/templateCode/id/version/status` 与 `form.formCode/formName/fields` 是正式数据来源；本原型数据内存隔离，不连8080。
- 名称为空显示“未命名（历史模板）”及原编码；名称非唯一，显示编码；行key由模板ID+表单编码组成。每行设计/查看动作应保留具体模板与表单上下文。
- 视口大于1100px使用表格；900/375改为每行纵向字段组，标签和内容同行，所属模板名称及编码换行，操作显示在本组。该布局仅为待审原型，并非已冻结设计。
- 查询页面中的标签、输入、重置和查询一组同行；空间不足时整组换行。新建模板使用现有名称、编码、工艺关系及取消/创建，不能为缺失依赖伪造成功。

## 状态与可用性

已在浏览器观察：列表返回/查看、两条相同表单分辨、历史空名称、窄屏行操作显示、新建空字段错误。只读/空/加载失败由现有查询参数模拟；本阶段未完成完整实际走查，不能作为权限或后端验证。

缺失：真实既有Canvas/CPP设计器的完整交互、创建成功及保存回读、其余工序及工艺页签内容、原有“编辑工艺”入口、移动导航、恢复/长名/所有角色状态、原图逐图九维和WCAG检查、交互与几何断言、对抗复核和manifest。现有旧CPP原型包含超出最终批准范围的动作与工艺版本信息，不得直接接入冒充正确设计器。

## 审查与证据

审查者 Codex SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max，后者SKILL.md SHA256 `8B827FE03B349AEEA99A7D6CF4CCD892E6065B672859FDB11E13A1919453EDDC`。八组查询原始结果在同目录 `identity-knowledge-*.txt`；执行退出0不代表已完成规则逐项评审。

修复前：`1793253620155_identity-phase2-1440.png`、`1793253620215_identity-phase2-1280.png`、`1793253620275_identity-phase2-900.png`、`1793253620330_identity-phase2-375.png`。视觉观察900/375表格右侧操作不可见，HIGH。

修复后：`1793253755249_identity-phase2-repair-1440.png`、`1793253755329_identity-phase2-repair-1280.png`、`1793253688816_identity-phase2-repair-900.png`、`1793253688873_identity-phase2-repair-375.png`。肉眼观察大视口表格清晰，窄视口模板名称、编码和行操作出现在各记录块；这一局部修复不关闭其余HIGH和整个审查。尚缺各surface×state×viewport截图、逐图完整八类记录及九维/WCAG矩阵；manifest未提交/未验证。

## 结论

设计器入口、其他工序与原有编辑入口尚未闭环，HIGH；移动导航缺失，HIGH；审查覆盖缺失，NOT RUN。不得请求用户批准本原型，不得声称PASS FOR PROTOTYPE或启动正式集成。需继续补齐现有行为与真实交互，完成适用矩阵后提交确切源版本人审。
