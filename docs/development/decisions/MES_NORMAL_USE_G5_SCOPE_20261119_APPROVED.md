# MES G5 正常业务使用验收范围确认（2026-11-19）

状态：**USER APPROVED / BOUNDED SCOPE**。

## 用户最新决定
用户明确表示“不需要这个功能”、“不要发散功能，正常使用就可以”。因此真实 Narrator/NVDA/JAWS 屏幕阅读器语音播报、语音采集、Speech Recap、读屏专用适配及其环境建设**均不属于本次医院制剂 MES 的功能范围或交付阻断门禁**。不要求用户启用屏幕共享、安装辅助工具、更换 Windows 版本或提供特殊语音环境。

## 保留的正常使用验收
- 业务页面信息正确，查询、列表、查看、编辑、保存、取消、校验错误、只读权限、异常提示与恢复符合已批准 T1–T6/Global UI V2。
- PC 页面布局、字段/控件同行、操作按钮顺序、常规键盘操作、焦点返回、弹窗关闭、无抽屉式操作按已有门禁执行。
- 生产工艺的工序、表单、CPP、eBR、质检单据、QA 只读门禁和历史归档展示保持真实可核验。
- **不重跑批次 655 生产、QC、QA 放行、签名或归档**，不修改业务数据、数据库/Flyway 或冻结 API/状态。
- 现有额外读屏/WCAG 专项证据属于历史诊断材料，未执行项如实记录 NOT RUN，不冒充通过或 WCAG AA 认证；不作为本次正常业务验收的额外开发要求。

## G5 清单调整
从当前生产工艺 G5 必需 interaction_checks 中移出“Actual screen reader announcements and accessible live-region speech in native assistive technology”。这一项记录为 **OUT OF SCOPE / N/A（用户排除）**，不是 PASS，也不是新增业务功能。保留普通键盘、焦点、控件标签等既有测试。不得删除历史诊断报告。

历史 G5 严格全维度清单本身还有未完成检查，仍为 IN PROGRESS；不得仅因剔除语音项就把完整 WCAG/G5 标为 READY。另行按正常业务使用的验收结果和剩余具体功能缺陷评估交付，不重新启动语音专项。
