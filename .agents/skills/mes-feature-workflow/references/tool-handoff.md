# 工具可用性与标准交接（2026-10-10）

|能力|实际入口|本次证明|边界|
|---|---|---|---|
|skill-creator|C:/Users/Administrator/.codex/skills/.system/skill-creator/SKILL.md|真实读取；init_skill.py 实际创建|quick_validate 需要 PyYAML|
|UI UX Pro Max|C:/Users/Administrator/.agents/skills/ui-ux-pro-max/SKILL.md|search.py --help 实际执行；支持 --stack vue|不是独立 MCP；技能读取 + 本机 Python 命令；建议不能覆盖 V2 token|
|Product Design|C:/Users/Administrator/.codex/plugins/cache/openai-curated-remote/product-design/0.1.56/skills/index/SKILL.md|读取 router/image-to-code；本环境暴露产品设计技能、ImageGen、Browser|没有一个叫 Product Design 的自动 RPC；不能由名称推断 Vue 工程生成成功|
|本机原型|D:/codex/_project/gmp/mes-demo/package.json|已存在 Vue、Ant Design Vue、Pinia、Router、TS、Vite|目前没有 Git 仓库：不得捏造提交号；G2 要求真实提交或有效批准的哈希例外|

可用性验证不是任何 MES 设计/原型阶段的执行或批准。此任务不生成设计稿、不创建业务页面。

UI UX Pro Max 的实际调用示例：先读其 SKILL.md，再运行 `python C:/Users/Administrator/.agents/skills/ui-ux-pro-max/scripts/search.py "form validation" --stack vue --max-results 3`；设计建议必须筛选到现有 Global UI V2，禁止 `--persist` 生成第二视觉权威。

Product Design 在 Codex Desktop 可按 router 读取 audit/ideate/image-to-code 等具体技能，并使用实际可用的 Browser。局部 MES 浏览器测试遵循 cuawright-mes/cuawright-local 的项目配置。不能假设其它 Work 会话的工具、登录状态和本地文件可共享。若环境只支持 React/HTML 或云端单页原型，标记 `DEPENDENCY_NOT_READY`，不给 G2 PASS；提交请求批准“在既有本机 mes-demo 使用上述 Vue 栈构建、Product Design 负责设计/视觉交接”的限定替代方式。已有原型优先复用，不能未经授权重新设计。

跨工具交接必须保存：feature/gate/工具环境/发起人/接收人；工具技能路径与版本；需求和批准设计 hash；文件真实位置、SHA256、Git commit（若无则明示并申请例外）；页面与源文件映射；Mock/真实服务声明；启动命令、URL/视口；原始截图、控制台、JUnit/日志；执行命令与退出码；未解决问题；批准来源、批准对象和时间。接收方复核 hash 与 commit 后重跑适用门禁，不能把聊天摘要当源码或批准。

生产原型 5174 与正式页面 5173/8080 各用既有项目；禁止新端口、新开发 checkout、新数据库。Hosted CI 不能访问本机原型或服务，只验证签名源清单和证据；真实本机集成由受信执行者运行，不在 GitHub Actions 伪造本机 localhost 成功。
