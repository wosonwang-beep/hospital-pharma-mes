# 生产工艺G5可访问性有界补充 — 已批准

状态 **APPROVED / FROZEN FOR BOUNDED IMPLEMENTATION**。用户2026-11-02本会话明确“批准”并在原问题回复中选择“批准完整有界方案”。确切批准对象为 [原草案](PROCESS_G5_A11Y_SUPPLEMENT_20261102_PROPOSED.md) 中四项范围及原确认问题；草案保留为批准前历史。

开始2026-11-02 09:32:31，截止10:12:31（Asia/Shanghai）；最多40分钟、2批，累计39保留，最多41。第40批合并修复两项既已定位根因，41只供另一有据修复；验证本身不另计轮次。不得自动再续时/清零或为PASS改变断言。

## 确切实施合同

- CPP：原条件组逻辑/条件字段/运算符的准确可访问名称通过实际HTML label关联原Ant内层input，复用RolePicker已验证方式。保持所有可视标签、布局、控件、条件/字段ID、DSL、安全执行及四动作不变。新测试按实际combobox准确名称定位。
- V2主蓝正常 `#1668dc`、hover `#0958d9`、active `#003eb3`。同步CSS token、Ant主题/门户及原显式hover，不改白字、字号、间距、栅格、背景语义、控件/按钮高度、成功/危险等非主色。批准对白普通文本最低4.5:1，原门禁阈值不变。
- T1–T6、工艺T4及既有专用CPP/Canvas编辑器结构继续适用；无需新原型的已授权缺陷修复与确切颜色补充，不授权整页重设计。未批准20261029整页原型依旧不集成。
- 继续原170格G5，同源采证和完整适用审查，不用旧色源截图声明新源PASS。原生200%缩放先测；本补充不授权改高度/布局。375矩阵保留、移动债与桌面业务范围分列。

## READ / REUSE / WRITE检查

已物理读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md相关路由、V2/T1–T6、CPP原型锁、原草案、ROUND39_RESULT、相关CPP/RolePicker/CSS/App及直接测试。Planning恢复revision48，先前记录是历史，当前状态只MES_TASKS。业务继续v1.0.23+DCP；没有业务/DB/API/权限/状态/审计/签名影响，无迁移。

复用实际RolePicker的Vue useId+label for+Ant id，不建选择器基础设施、不替换Ant控件。CSS三个主色色值在主题中显式同步，避免Ant算法派生出不合约hover/active。现有未提交修改保留；不改历史发布或执行Flyway。

文件白名单：CppRuleDesigner.vue及其直接测试；global-ui-v2.css、App.vue对应primary主题值；本批准记录和V2补充引用；当前任务脚本/源摘要/采证/完整审查与集成报告/MES_TASKS/ownership/Planning。无后端、数据库、正常业务数据写入、部署、Git提交/推送授权。本轮真实409仍须拒绝且定义前后相等，不重做历史正常保存闭环。

剩余审查属于SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max。最终需同manifest validator通过及READY verdict才能写READY，人的本次修复批准不等于最终ACCEPTED。

## 本轮执行结果
两批累计40/41已执行；29测试/typecheck/build/53真实检查局部通过。CPP实际名称问题已定向验证，三态主按钮对比度最低5.188；完整G5仍FAIL。170新源图首遍已看，第二遍未做。用户纠偏转查工序工作区及IPC，未超白名单追加正式实现。无迁移/正常业务保存；完整结果见../process-form-cpp-ebr/20261101-final/ROUND41_RESULT.md。原型业务规则按本轮用户明确：按具体工序配置和前置关系判断，非全工艺统一必检。
