# DCP-EBR-ADMIN-DIRECT-PUBLISH-20261109 — 已批准限域设计确认

状态：APPROVED / FROZEN（用户2026-11-09明确确认“ebr就不需要批准了”“都是同系统管理员完成配置工作”）。
权威：FINAL BASELINE v1.0.23 + 本限域DCP；历史已批准DCP与既有GxP条款在未涉及范围维持不变。
原设计冲突：此前eBR模板需要DRAFT→SUBMITTED→APPROVED→EFFECTIVE、作者与审核人不得相同；用户此轮仅撤销**配置性工序eBR模板**的独立批准步骤。不是撤销运行时电子签名、工序独立复核、QA审查及批次正式放行规则。

## 业务及交互冻结
- 责任人：沿用现有系统管理员/配置人员及既有 `ebr:template:publish` 权限、组织隔离、版本锁、审计，不生成伪造审核身份。
- 新定义：DRAFT 经保存+发布前强制Lint可直接执行PUBLISH并成为EFFECTIVE；无需提交审批或APPROVE。当前历史SUBMITTED（含喉咽茶模板497）可由同一合法配置账号直接PUBLISH；历史APPROVED保持原有发布兼容和原有approvedBy/approvedAt，不篡改已保存的批准证据。已发布模板不可编辑。
- 发布必须保留当前工艺绑定、schema2角色ID、字段及CPP校验、打印源版本、内容哈希及不可变发布快照、幂等/If-Match版本锁。发布时非历史APPROVED模板从当次真实canonical生成contentHash并写入effectiveFrom与审计，不填写虚假的approvedBy或approvedAt。
- UI：已有模板详情T3“受控操作”不再出现“提交审批/批准模板”；DRAFT/SUBMITTED/APPROVED且具有发布权限时显示“发布模板”；旧历史状态仍可阅读，但不再要求审批记录成为前置依赖。显示旧批准记录只读用于历史追溯，不虚构新的审批；可将该区域提示为历史审批信息。
- API：保留现有POST /api/v1/ebr/versions/{id}/publish与既有权限及HTTP契约，不新增API、不改现有可执行服务路由；旧SUBMIT/APPROVE兼容历史调用及存量集成测试，但正式UI不再提供它们。
- 现有UI V2/T3结构、按钮主次、同行label、Canvas Designer、CPP规则设计不改变；只是显隐与文字调整（不是新增/重做业务页面，不涉及新原型）。
- 运行态 eBR 表单SUBMIT/VERIFY、电子签名、独立操作人、CPP异常偏差、QC/QA以及成品冻结和FINAL PDF门禁全部保持原合同。

## 文件白名单
backend/mes-ebr/src/main/java/com/hospital/mes/ebr/domain/EbrDefinitionRules.java
backend/mes-ebr/src/main/java/com/hospital/mes/ebr/application/EbrService.java
backend/mes-ebr/src/main/java/com/hospital/mes/ebr/infrastructure/TemplateEntity.java
backend/mes-ebr/src/test/java/com/hospital/mes/ebr/domain/EbrDefinitionRulesTest.java
backend/mes-boot/src/test/java/com/hospital/mes/ebr/integration/EbrIT.java（仅新增限域集成验证，若必要）
backend/mes-boot/src/test/java/com/hospital/mes/ebr/integration/EbrRuntimeIT.java（仅修复既有schema2测试夹具，验证运行态签名/复核不回归）
frontend/mes-web/src/views/ebr/TemplateDetailView.vue
MES_TASKS.md 与本报告所在决策/验收证据文件

不修改数据库现有结构或迁移、IAM/权限列表、现有已批准QC页面、生产批次已签历史、审批电子签名系统、审计历史、老批准记录，不创建worktree或第二数据库。

## 验收标准
- 单元：DRAFT+PUBLISH=EFFECTIVE；SUBMITTED+PUBLISH=EFFECTIVE；APPROVED+PUBLISH仍有效；EFFECTIVE不可再次发布/更改；错误版本/权限拒绝。
- 集成：发布保留原始定义校验，contentHash与published canonical一致；admin(既有授权)能够操作模板497，审批字段为空不阻碍有效发布；数据重读状态EFFECTIVE、有效日期及内容哈希一致；模板运行时签名/QA门禁不回归。
- 前端：正式5173/8080真实管理员登录后可以在模板497页面找到“发布模板”并执行，不能把UI自动测试或直接数据库SQL冒充实际批准；状态重读持久化。
- Git：当前单一main工作区及其大量其他未提交修改全部保留，不擅自stage/commit/push。
