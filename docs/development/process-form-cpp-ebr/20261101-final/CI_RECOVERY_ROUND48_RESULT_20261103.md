# 第47–48批 CI 收尾结果

## 基线依据

用户明确批准PROCESS_CI_RECOVERY_20261103_PROPOSED；批准与白名单在PROCESS_CI_RECOVERY_20261103_APPROVED。实际读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS、PROJECT_BASELINE、当前全局UX批准和直接CI/回调/角色测试源码。业务v1.0.23、UI V2及历史证据不改。窗口15:00:40–15:40:40（Asia/Shanghai），本次15:06:21确认运行结束及原签名复现，提前停止；累计46→48，不自动49。

## 设计差异与修改

第47批：EditorCallbackTest两处uploadFromEditor的stub/verify由7参数对齐现有9参数，追加源模板eq("10")和选择isNull()。原成功响应、业务类型、实际DOCX字节、禁止覆盖源模板及三个安全/重放用例不减。未改PrintEditorService或业务实现。

CI两个Maven任务增加fonts-noto-cjk/fontconfig安装、字体缓存和Noto Sans CJK SC名称检查；原完整命令及四CI任务保留。前端原完整命令追加--maxWorkers=2，未改5000ms、测试断言、测试清单/排除、重试或跳过条件。

第一次本机CI负向自检失败：Python文本写入将工作流LF转换为CRLF，原正则夹具未命中contents: read。第48批仅按.gitattributes恢复LF；字面内容不变，原正向和负向自检随后通过。装配修正如实累计，不重置轮次。

## 修改范围

实现白名单仅.github/workflows/ci.yml、backend/mes-reporting/src/test/java/com/hospital/mes/reporting/application/EditorCallbackTest.java；同步批准、执行证据和MES_TASKS本任务状态。所有启动前其他已修改文件SHA核对未变化；无数据库/业务记录写入、无迁移、无本机配置修改、无新checkout。尚未暂存、提交或推送本轮修正，未触发新的远端CI或合并。

## 验收证据

- Maven命令：mvn -f backend/pom.xml -pl mes-reporting -am test。退出0，打印模块54测试、0失败、0错误、5原有跳过；EditorCallbackTest4/4、中文PDF原7/7通过。反例：原测试11项10通过1错误，saved为null；修改后异常消除。完整日志ci-recovery-round47-backend.log，1分15秒。
- npm test -- --run --maxWorkers=2：退出1，工作树55文件中54通过、1失败；234项232通过、2失败，126.70秒。工作树含其他既有未提交测试，不能冒充远端原223项同树计数。
- 重复签名1：RoleDetailView.test.ts:150，ordinary view preserves independently selected export while removing only management actions，Test timed out in 5000ms。
- 重复签名2：RoleDetailView.test.ts:216，role information partial success retains permission draft and uses updated version on retry，Test timed out in 5000ms。
- 原单文件11/11通过和完整运行超时同时保留；worker限制没有消除同一签名，不能再断言只是CPU并行度或擅自提高门槛。具体耗时根因仍未证明，未新增另一种补丁。
- Repository/Compose、CI正向策略PASS；CI负向首次FAIL、LF修正后PASS。未改校验器或负向夹具。字体Linux运行及原CI四任务NOT RUN，禁止把Windows PDF通过当作Linux字体修复已验。
- 数据化结果ci-recovery-result-20261103.json，修改前其他文件SHA快照ci-recovery-pre-edit-20261103.json。

## 结论

前端自动修复STOP / 当前整体IN PROGRESS。两个批准批次用完，原5000ms签名复现触发§6A提前停止；原合同和原失败保留。回调修正定向通过；Linux字体仍待实际验证，完整CI未恢复，不能合并或宣称READY/ACCEPTED。既有PR #6已在前一阶段合入的事实不变；生产工艺整页工作区、IPC业务链、G5仍未完成。本轮SELF-REVIEW。
