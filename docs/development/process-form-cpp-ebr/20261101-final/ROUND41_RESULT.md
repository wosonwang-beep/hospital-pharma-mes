# 第40–41批结果与生产工艺原型纠偏

## 1. 基线依据

实际读取 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、UI V2/T1–T6、CPP 原型锁及 PROCESS_G5_A11Y_SUPPLEMENT_20261102_APPROVED.md；按 business-product-manager 的用户优先、源码保真、端到端责任参考执行。业务仍 v1.0.23＋批准补充。窗口09:32:31–10:12:31，最多两批，累计39→41；不自动第42批。

用户本轮确认所指为本机 mes-demo 的「上方路线带＋左侧工序＋右侧配置」历史原型，并纠正：IPC 判断适用于具体工序配置和前置关系，不是所有工艺统一必检。完整记录及原图在 [原型核对](IPC_PROTOTYPE_TRACE_20261102.md) 和 [原图对照](IPC_PROTOTYPE_COMPARE_20261102.html)。识别历史布局不批准照搬旧稿的强制 IPC 或已退役工艺审批。

## 2. 设计差异

第40批修复 CPP 外层标签不能命名实际 combobox 的问题，复用 useId＋独立 label/Ant id 关联；正常/悬停/按下主蓝及 Ant 主题改为获批三色。布局、业务字段、角色ID、DSL、控件高度和原断言保持。新增3个实际可访问名称测试先RED；合并验证28/29，失败是新增测试的按钮名称定位含图标文字，构建另发现测试工具类型不支持 exact 选项。第41批只纠正这些测试装配，App.vue 纳入采证源码闭包。首轮失败日志保留。

用户反馈证实了更大的产品缺口：正式工艺页没有原型的工序主从工作区；工艺1实际为六道工序，IPC 检验项挂在 OP03；现规格和170格采证未包含 IPC。登记 BPM-DESIGN-001、BPM-IPC-001、BPM-IPC-002 三项 HIGH：工作区不符、IPC追踪遗漏、旧原型全局强制 IPC。不能把它们当作颜色和可访问名称修复后的测试尾项。

## 3. 修改范围

- `frontend/mes-web/src/views/ebr/CppRuleDesigner.vue`：实际输入标签关联。
- 同目录 `CppRuleDesigner.accessibility.test.ts`：三个名称、增组ID、只读禁用检查。
- `frontend/mes-web/src/global-ui-v2.css`、`src/App.vue`：已批准主蓝三态及主题。
- `docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md`：本次有界颜色补充来源。
- 本批准记录、采证脚本 App.vue 闭包、当前审查/集成清单、原型追踪/对照页、ownership、续验、MES_TASKS 和 Planning 更新。

迁移：NONE。无本轮正常业务保存、新业务记录、数据库清理、后端/mes-demo正式修改、部署或Git提交推送。过去获批的保存往返证据保留。本次用户纠偏后未再扩改业务源码。九文件逐项 SHA256 与本轮采证一致，组合摘要 `E56F99F7EA21C07ADC6E2C1E6FE310919EE3D70CECB38D8AAF0266B5BB3B06BA`。

## 4. 验收证据

| 门禁 | 本轮结果 | 证据 |
|---|---|---|
| 组件/模型回归 | 7套件29测试，0失败；17.86s，exit0 | round41-tests.log |
| typecheck＋build | exit0；Vite22.42s，已有大包warning保留 | round41-build.log |
| 真实5173/8080局部恢复/权限 | 53/53，errors=[]，完整模板定义不变 | formal-captures/20261102_093837/evidence.json |
| CPP名称/下拉键盘 | 四宽度准确名称各唯一；每实际输入一个关联标签；ArrowDown展开、Escape关闭且保留焦点 | round41-a11y/20261102_094343/result.json |
| 三态主按钮对比 | 5.188／6.159／8.974；实际高度仍32px | round41-evidence-summary.json，三张原图已查看 |
| 标签/控件抽样几何 | 168格888配对，同行失败0、中心偏差>8px为0、页面扩宽0；全控件覆盖NOT RUN | 同上 |
| 窄屏Canvas预览 | 等待实际绘制后原图可见正文；只关闭预览正文采证缺口，不关闭横向可用性债 | preview-canvas-375-settled.png |
| 当前源码视觉审查 | 170原图第一遍实际查看；逐图结构化分类未全部登记、对抗第二遍0/170；另外12张专项图已原图查看 | round41-visual-observations.json |
| 完整ui-ux-audit | FAIL，validator实际exit1；同一manifest绑定集成报告，无降门禁 | audit-validator-round41.log，audit-manifest.json，integration-acceptance.json |
| 用户识别原型/IPC端到端 | 历史原图已定位，完整保真与IPC实际业务闭环REWORK_REQUIRED | IPC_PROTOTYPE_TRACE_20261102.md |

原生Chrome控制因工具无法可靠确定当前网址而停止，未绕过。Headless快捷键前后缩放指标未变，不能当作原生200%通过。完整键盘/播报、错误关联、全控件对比度与锚点等仍未证明。53项检查后的浏览器清理曾报告Targetclosed，未冒称所有控制台/清理无错误。

执行者：Codex SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max；无独立评审人。

## 5. 结论

局部修复有明确验证，**生产工艺整体未完成，G5 FAIL / REWORK_REQUIRED，不能READY或ACCEPTED**。先前轮次过多既含实际缺陷，也含测试定位、类型和采证装配返工；同时缺少完整原型/IPC追踪，导致测试数量增长而完整业务目标没有闭环。产品经理技能调用不等于完整履职，这项遗漏由本任务承担。

主线现为：用户识别的工序工作区 → 各工序自身配置/前置关系 → 适用IPC检验及既有质量门禁 → 表单/CPP/eBR。无IPC工艺不强制增加检验，不能将某一检验工序条件泛化为全工艺统一要求。既有待验事项和累计计数继续保留，不另开任务或重跑登录规避。

Planning同步：远端checkpoint两次传输失败，最后读取revision49；不声称远端已更新。最新事实已落盘，恢复入口planning-sync-pending-round41.md。原图payload四张与九文件源码哈希均已重新核对。
