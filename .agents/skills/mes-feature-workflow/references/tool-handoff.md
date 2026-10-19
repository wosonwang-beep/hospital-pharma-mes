# 工具可用性与标准交接（2026-10-10）

|能力|实际入口|本次证明|边界|
|---|---|---|---|
|skill-creator|C:/Users/Administrator/.codex/skills/.system/skill-creator/SKILL.md|真实读取；init_skill.py 实际创建|quick_validate 需要 PyYAML|
|MES UI/UX 完整审查|仓库 .agents/skills/mes-ui-ux-audit/SKILL.md + C:/Users/Administrator/.agents/skills/ui-ux-pro-max/SKILL.md|清单校验器正反测试；ui-ux-pro-max search.py 支持 --stack vue|执行者是 Codex SELF-REVIEW；Product Design 可协助设计/截图，不能冒充独立审查人；建议不能覆盖 V2 token|
|Product Design|C:/Users/Administrator/.codex/plugins/cache/openai-curated-remote/product-design/0.1.56/skills/index/SKILL.md|读取 router/image-to-code；本环境暴露产品设计技能、ImageGen、Browser|没有一个叫 Product Design 的自动 RPC；不能由名称推断 Vue 工程生成成功|
|本机原型|D:/codex/_project/gmp/mes-demo/package.json|已存在 Vue、Ant Design Vue、Pinia、Router、TS、Vite|目前没有 Git 仓库：不得捏造提交号；G2 要求真实提交或有效批准的哈希例外|

可用性验证不是任何 MES 设计/原型阶段的执行或批准。此任务不生成设计稿、不创建业务页面。

UI UX Pro Max 的实际调用示例：先读其 SKILL.md，再运行 `python C:/Users/Administrator/.agents/skills/ui-ux-pro-max/scripts/search.py "form validation" --stack vue --max-results 3`；设计建议必须筛选到现有 Global UI V2，禁止 `--persist` 生成第二视觉权威。

Product Design 在 Codex Desktop 可按 router 读取 audit/ideate/image-to-code 等具体技能，并使用实际可用的 Browser。局部 MES 浏览器测试遵循 cuawright-mes/cuawright-local 的项目配置。不能假设其它 Work 会话的工具、登录状态和本地文件可共享。若环境只支持 React/HTML 或云端单页原型，标记 `DEPENDENCY_NOT_READY`，不给 G2 PASS；提交请求批准“在既有本机 mes-demo 使用上述 Vue 栈构建、Product Design 负责设计/视觉交接”的限定替代方式。已有原型优先复用，不能未经授权重新设计。

第一阶段调用或交接前，UI UX Pro Max 实际读取该功能需求文档与当前适用的详细设计；先形成业务分析，再按现有 V2/T1–T6 出设计稿。G1 → G2 输入包必须包含所读文件路径/版本/章节、业务分析、需求—页面—交互映射、设计稿和美观/可读性/操作友好性检查，以及原型待验证的典型操作、键盘/焦点、异常恢复和截图场景。跨环境不能访问输入文件时先完成真实文件交接；关键输入缺失或冲突未决则停止，不声称已读取或已执行。Product Design 使用同版本输入，在实际浏览器记录体验验证证据。

G2 原型运行后及 G5 正式页面接入真实服务后，分别由Codex SELF-REVIEW执行`mes-ui-ux-audit`并调用UI UX Pro Max知识库及批准的浏览器工具，不存在按名称自动成功的审查RPC。交接需求/适用详细设计、设计基线、运行URL、源码版本和获准角色；审查美观、可读性、完整业务操作及异常恢复。问题交给Product Design/获准Vue构建者（G2）或Codex（G5）在批准范围内修复，再执行完整复查。每批20轮后报告，真实批准后新批重新从1计数，历史证据保留；其它停止条件按规范§5.4B。保存两阶段独立`product_experience_review`、问题前后证据和批准记录；G5必须另有`ui-ux-audit`集成测试套件执行记录，并将同一验证清单和`READY FOR ACCEPTANCE`结论绑定到`integration_acceptance.ui_ux_audit`。不可访问运行页面则阻断，不用静态稿或原型结论替代正式产品体验验收。

跨工具交接必须保存：feature/gate/工具环境/发起人/接收人；工具技能路径与版本；需求和批准设计 hash；文件真实位置、SHA256、Git commit（若无则明示并申请例外）；页面与源文件映射；Mock/真实服务声明；启动命令、URL/视口；原始截图、控制台、JUnit/日志；执行命令与退出码；未解决问题；批准来源、批准对象和时间。接收方复核 hash 与 commit 后重跑适用门禁，不能把聊天摘要当源码或批准。

生产原型 5174 与正式页面 5173/8080 各用既有项目；禁止新端口、新开发 checkout、新数据库。Hosted CI 不能访问本机原型或服务，只验证签名源清单和证据；真实本机集成由受信执行者运行，不在 GitHub Actions 伪造本机 localhost 成功。

完整评审交接必须包含 mes-ui-ux-audit 的全套 B端矩阵、全部页面/弹窗/抽屉等 surface 及状态/视口清单、九维 dimension_reviews、基础 WCAG 2.2 单项记录、知识检索/逐图/操作/DOM/对抗复查证据及实际校验结果。禁止简化交接成几条肉眼可见问题；漏维度/NOT RUN 或无法访问运行环境即报告未完成，不能传递完整评审 PASS。每批20轮及真实批准后新批计数规则保留。
