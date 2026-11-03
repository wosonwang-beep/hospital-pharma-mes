# 生产工艺提交后 CI 收尾方案

状态：PROPOSED。仅恢复现有 CI；不是业务设计变更、全局 UI 改版或生产工艺验收。累计修复46保留，本轮仅诊断，尚无第47批。

## 基线与实际状态

已真实读取 AGENTS.md、MES_DEV_RULES.md §6A、MES_TASKS.md、PROJECT_BASELINE.md、MES_GLOBAL_UX_INTERACTION_20261102_APPROVED.md、现有 CI、受影响测试与直接实现。业务仍 v1.0.23＋既有批准补充，UI V2/T1–T6；不改全局交互规范。

2026-11-03 查询 GitHub 确认 PR #6 已于2026-11-02 15:38:44（Asia/Shanghai）合入，提交182b0d563be64b68ed95d75a3a9513599a6080ca已在main，本机与远端main均为ecb1f97。上次未合并报告保留其当时事实。本轮不重复合并，继续定位遗留CI失败。

## 实测和分类

1. FACT：本机原RoleDetailView.test.ts原门槛、原断言11/11通过，耗时43.38秒；旧CI为220通过、3个角色页用例在5000ms超时。并行资源争用为INFERENCE，尚不能宣称已证明或已修复。
2. FACT：本机PureJavaPdfSmokeTest四项、WordTableComposerTest三项全部通过；旧Linux CI同一组有四个中文内容断言失败，提取内容为井号。转换器只映射已安装的Microsoft YaHei/Noto Sans CJK SC，CI没有安装匹配中文字体步骤。字体依赖缺失为待Linux复验的环境根因候选，禁止改业务代码或PDF内容断言绕过。
3. FACT：本机EditorCallbackTest为3通过、1错误，与旧CI同签名saved为null。实现PrintEditorService调用uploadFromEditor的9参数方法，末两参为sourceTemplateVersionId及choice；测试stub和verify仍针对7参数方法，Mockito返回null。这是测试装配与已实施源模板版本合同不一致。不能改生产实现迁就旧mock。

复验命令与原日志保留docs/development/process-form-cpp-ebr/20261101-final/ci-resume-20261103-backend-before.log（11项，10通过1错误）及ci-resume-20261103-role-before.log（11通过）。未运行全库事务回归、无DB/业务写入、无迁移、本轮未改实现或测试。

## 具体待批准差异

- EditorCallbackTest只将既有stub和verify改为真实9参数方法：在原anyString()后追加eq("10"), isNull()。原错误码、真实DOCX字节匹配、名称/业务类型、从不更新源模板等断言保留；新增参数验证源版本必须为10、不得擅自更换数据源。该verify调用属于测试合同，依据§6A需具体批准，不能当普通自动修复。
- CI的两个Maven任务在运行原命令前安装并检查开源中文字体；以现有转换器匹配的字体名称为准，实际Linux验证中文输出。保留Java21、隔离转换、原断言及超时，不分发Microsoft专有字体。
- 前端先保留原测试完整内容和5000ms门槛；若完整并行运行的资源证据吻合，在CI原测试命令后加--maxWorkers=2。仅调整调度，不减测试、不加重试、不增加超时、不改Vitest排除或断言。不能仅凭本机11通过认定CI已恢复。
- 不修改打印业务、角色授权、数据库、权限、GxP、历史迁移、页面布局、原型或生产工艺字段；不提交其他QC/QMS/WMS改动、本机.env及数据库配置。

## 实施与停止边界

批准后最多40分钟、2批，累计46→48；包括本机定向测试、原CI四任务及受影响集成验证。新失败先分类；原签名在针对性修正后重复立即停止，不继续盲试。若发现新业务/数据/权限合同冲突或必须改其他验收断言，重新提出具体差异。CI全部通过才提交合并结果，不把CI通过冒称生产工艺整体G5通过。

白名单：backend/mes-reporting/src/test/java/com/hospital/mes/reporting/application/EditorCallbackTest.java、.github/workflows/ci.yml、当前批准记录和执行证据、MES_TASKS.md本任务段。已有main及其他未提交改动保留，不创建其他checkout或本地数据库。
