# MES G5 收尾跟踪报告（2026-11-18）

## 本轮完成

- 按产品经理整改报告完成剩余业务页面复核。
- 生产工艺/工序/表单/CPP：既有 G5 证据继续保留，新增生产工艺显示标签测试并通过。
- 角色权限：7 个真实 Chrome 登录会话复核通过：详情：请验、取样、样品、检验任务、检验报告、异常、QA 物料审核等只读详情全部纳入最终浏览器复核。
- 检验任务签名证据：恢复 `trace:view` 只读用户的“查看签名证据”入口及只读证据读取流程；无该权限的账号仍不显示入口。
- 原生 Chrome 200% 缩放：1440/1280/900 三种物理窗口共 18 个状态通过，包含工艺列表、详情、编辑、CPP、EBR Canvas、产品选择弹窗及焦点恢复。
- 前端生产构建：PASS。
- 前端完整回归：主回归 246 个测试中 245 PASS、1 个 `OperationItemDialog` 超时；立即单独以 30 秒超时重跑该文件，5/5 PASS。该超时未复现。
- 后端此前完整回归：386 PASS / 6 SKIP / 0 FAIL；本轮没有后端生产流程。

## 严格遵守的范围

- 未重新执行批次 655 生产流程。
- 未重新执行 QC 检验、QA 放行、签名或归档。
- 本轮只做只读页面复核、前端测试、构建和最小必要 UI/只读证据修复。
- 未修改数据库、Flyway 或业务数据。
- `application-local.yml` 继续保留在本机，不纳入提交。

## G5 当前结论

当前正式 G5 manifest 仍保持 `IN PROGRESS`，不是虚假的 READY。

已通过的关键门禁包括：
- 角色组合复核
- 原生 Chrome 200% 缩放与几何复核
- 30 个现有工艺页面截图无水平溢出
- 当前源代码 30 个截图无文档水平溢出
- 键盘导航、模态框关闭及焦点恢复
- 生产工艺/CPP/EBR Canvas 相关既有 G5 门禁

### 唯一仍未完成的 G5 阻断项

**真实桌面屏幕阅读器（Windows Narrator 等）的实际语音播报确认尚未执行。**

因此不能把整个 G5 标记为最终 READY。19 项 WCAG 22 criterion 的完整逐项证据也仍未完成；当前 manifest 明确保留 `NOT RUN`，不冒充 WCAG AA 认证。

## 下一步

1. 完成一次真实桌面屏幕阅读器复核。
2. 复核完成后重新运行 G5 manifest validator。
3. 若通过，再执行最终代码检查、提交和推送。

## 2026-11-19 Narrator 实际语音门禁复核
- 本机 Windows 10 Pro 22H2 / Build 19045；Narrator 10.0.19041.4522 可启动、UI Automation 可读取。新版 Speech Recap/live transcription 是 2025 Windows 11 Narrator 能力，本机无该功能。
- 当前 14 个 Windows 音频端点状态全部 Unknown，FFmpeg 构建不支持 WASAPI 输入，未取得 Narrator 实际音频回环证据。Chrome DOM/UIA 名称与 aria-live 只能证明可访问结构，不证明语音播报。
- 最终 manifest interaction_checks[11] 保持 NOT RUN，verdict IN PROGRESS；没有伪造 PASS。详见 docs/development/process-form-cpp-ebr/20261107-formal-g5/NARRATOR_VALIDATION_ENVIRONMENT_LIMIT_20261119.md。
- DEV 8080/5173 已恢复，8080健康UP、5173 HTTP200；批655只读，不重跑生产/QC/QA/归档。## 2026-11-19 用户收窄 MES G5 为正常业务使用验收（覆盖此前“唯一阻断项”说法）

用户明确：“不需要这个功能”，“不要发散功能，正常使用就可以”。已正式将 Narrator/NVDA/JAWS 实际语音、Speech Recap/语音回环和读屏专用适配从本次 MES 必需交付范围**排除为 OUT OF SCOPE/N/A**，不是执行 PASS，也不是新增功能。完整决策：`docs/development/decisions/MES_NORMAL_USE_G5_SCOPE_20261119_APPROVED.md`。

- 已从 `docs/development/process-form-cpp-ebr/20261107-formal-g5/final/audit-manifest.json` 必需 `interaction_checks` 中移除这一被取消的专项条目；保留其余 11 项均为 PASS 的常规交互检查和既有历史读屏环境报告。
- 原始验证器未修改，重新运行显示：`PASS: PROCESS-FORMAL-G5-20261107 — IN PROGRESS; screenshots=30 coverage=30`。这一 PASS 只表示清单结构完整，**不是** G5 最终 ACCEPTED。
- 同时核实旧严格完整审查清单中另有 `54/54` 九维汇总项与 `19/19` WCAG 专项项为 `NOT RUN`，有 `G5-FULL-VISUAL-AND-A11Y` HIGH 项 OPEN；此前写“唯一未完成阻断项是读屏语音”不准确。历史严格清单保留 `IN PROGRESS`，不通过篡改校验器/断言或伪造 PASS 达到 READY。
- **本次交付只以正常业务场景**的页面/列表/查询/录入/错误提示、权限、普通键盘/焦点、PC 布局及既有业务结果为准；不再新增专项读屏任务，也不让“建立读屏环境”阻碍业务功能开发。已完成的正常业务浏览器/G5 专项结论仍沿用经过验证的真实证据，不再重新跑批次 655 生产/QC/QA 放行。
