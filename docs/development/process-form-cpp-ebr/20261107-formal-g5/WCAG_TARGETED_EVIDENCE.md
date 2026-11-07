# 生产工艺 G5：WCAG 2.2 定向实证（2026-11-07）

执行：**ChatGPT SELF-REVIEW**。来源为本机正式 5173 Vue + 8080 Java + MariaDB，PC 1440/1280/900，实际 Chrome，非原型或图片仿真。

> 本文是已完成专项验证的映射，**不是 WCAG 2.2 AA 全套认证**。完整范围仍按 `final/audit-manifest.json` 与后续最新源码清单报告 `IN PROGRESS`；未完整覆盖的标准不能标成 G5 PASS。

| WCAG 条款 | 本轮客观证据 | 未完成部分 |
|---|---|---|
| 1.3.1 信息与关系 | T1 查询字段、表格/表头与多选框可访问树；产品 Modal 搜索字段具有独立可见标签及 `aria-labelledby` | 全部 EBR Canvas 动态组件语义逐项核对 |
| 1.4.1 使用颜色 | 表格状态除颜色外还有状态文字；CPP 上下限预览同时有数值表达 | 全按钮/警示语境逐项对抗检查 |
| 1.4.3 文本对比度 | `contrast-probe.json` 真实 DOM 前景/背景 **17/17 ≥4.5:1** | 所有状态/悬停/禁用、图标与复杂背景仍需抽查，不等于全审 PASS |
| 1.4.4 文本缩放 | `process-pc-zoom-reflow.spec.ts` 在200%等效 CSS 宽720/640/450时工艺和CPP不产生全页横溢出 | 真正浏览器原生文本缩放、其他无障碍字体设置 |
| 1.4.10 内容重排 | PC 1440/1280/900 的10状态共30页，等效200%九个组合，documentWidth不超过视口 | 移动端375已批准撤出；原生缩放长文本更多边界条件 |
| 1.4.11 非文本对比度 | 键盘焦点轮廓明确2px实线蓝色；目标在默认/焦点色态肉眼可辨 | 所有图标、边框、控件状态的完整3:1测量 |
| 2.1.1 键盘可操作 | `process-a11y-keyboard.spec.ts` 在PC三宽真实键盘 Left/Right/Home/End/Enter/Tab/Escape，操作列表页签与弹窗 | EBR Canvas编辑器全键盘操作 |
| 2.1.2 无键盘陷阱 | 打开产品Modal后连续8次Tab均留在弹窗，Escape关闭并恢复发起按钮；PC三宽实测 | 其它页面和Canvas完整键盘循环 |
| 2.4.3 焦点顺序 | 标准Tab转移、Modal搜索控件初始焦点、关闭恢复来源按钮通过 | 所有业务子弹窗/工具栏通用顺序 |
| 2.4.7 焦点可见 | 生产工艺页签键盘焦点 `outlineWidth=2px`、`outlineStyle=solid`、`outlineColor=rgb(22,104,220)`，三宽一致 | 所有自定义图标按钮和Canvas控件 |
| 2.4.11 焦点不被遮挡 | `workspace-footer` 从覆盖式 sticky 修正为正文之后，三宽实测 footerTop>formCardBottom | 全局悬浮导航、原生缩放下更多焦点位置 |
| 2.5.3 标签包含名称 | 适用产品 combobox 辅助技术名称固定为“适用产品”，不混入产品chip；查询筛选“状态”“适用产品”名称真实可见 | 其它业务组件整面扫描 |
| 2.5.8 目标尺寸 | `target-size-probe.json` 对PC三宽扫描：普通业务按钮无小于24px的点，CPP四个原生复选框17×17，但其点击关联标签卡有58px+高度；启用规则开关30×17，需要按真实周边间距例外核查 | 全控件目标与Spacing Exceptions最终结论未完成，不得直接标PASS |
| 3.3.1 错误识别 | 已存在工艺编码新建返回真实HTTP409且显示错误文本、不丢输入 | 其余全部字段/网络失效/权限错误 |
| 3.3.2 标签或说明 | 新增/编辑工艺基础字段同一行可见标签，产品弹窗多选框同样提供“适用产品”标签 | 完整字段表/动态模板核对 |
| 3.3.3 错误建议 | 重复工艺编码提示改成“工艺编码已存在，请使用其他编码”，而非误报旧版本冲突 | CPP规则拒绝、服务端跨组织错误完整建议 |
| 4.1.2 名称角色值 | Chrome AX Tree确认“适用产品” combobox准确name、`aria-expanded`闭合false/展开true、页面两个配置tab `aria-selected`准确变化 | 全动态元素状态与EBR工具栏 |
| 4.1.3 状态消息 | 工艺状态与T1查询总量增加 `role=status`、`aria-live=polite`，专向Chrome DOM断言通过 | 使用真实读屏器确认播报行为、服务端异步状态切换全部时机 |

补充：7类本机业务角色在 `role-policy-evidence.json` 中使用 admin IAM 只读查询核实当前授权配置：生产岗位仅 `process:package:view`，仓管/取样/QC分析/QC复核/QA/基础资料无工艺权限。实际HTTP403已在既存仓管角色实测。**这不是七个账号逐一登录的端到端结论**，完整角色E2E仍待完成。

**现存验收治理阻断**：已批准本任务PC_ONLY三宽，但原始 validator 只认可原型 `#/processes` 的PC_ONLY例外，不认可正式 `/process/packages`，所以仍要求375手机宽度；不能直接调整校验器来制造PASS。完整19条WCAG和独立复查仍是IN PROGRESS。