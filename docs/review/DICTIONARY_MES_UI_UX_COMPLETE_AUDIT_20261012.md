# 数据字典原型完整 MES UI/UX 审查 — 2026-10-12

当前结论：`PASS FOR PROTOTYPE / READY FOR HUMAN DESIGN CONFIRMATION / NOT FORMALLY INTEGRATED`。首次审查的 `FAIL` 及原始证据保留在本文下方。

审查执行者为 `Codex SELF-REVIEW`。本轮按仓库 `mes-ui-ux-audit` 技能执行，并使用本机 `ui-ux-pro-max` 知识库；不是 Product Design 插件或独立人员审查。被审查对象为 5174 本地原型。首次失败源码 SHA256 为 `6F68FAF54B2EB9318ADBFD59346CA68E402FD41C3FE1F26E170B058A7437B2B3`；修复并复审通过的精确源码 SHA256 为 `62C7E714AD339FBFA7539DC270F7CEB38245D21A0E99C8B0D767DAC0DB903A25`。本结论不适用于正式 5173/8080 页面，也不构成设计批准、集成验收或发布确认。

## 首次审查阻断问题（历史证据）

### UX-001 — HIGH — 新增/编辑页描述行破坏表单栅格

1440 视口的普通左列字段标签/控件锚点为 `x=355/465`，字典描述却为 `x=619/741`，分别偏离 `264/276px`。描述项从页面中部开始，左侧留下无业务意义的大块空白。900 视口仍偏离 `16/20px`，375 视口仍偏离 `12/14px`，均超过技能规定的 8px 锚点阈值。

建议让描述项占满双列，但标签和控件起点继续复用左列 `label/control` 锚点；操作区与同一表单容器对齐。新增和编辑页必须使用同一栅格实现。

### UX-002 — HIGH — 桌面查询框宽度不足且断点行为反向

列表页 CODE/名称查询框在 1440 视口仅 `108px`，1280 视口仅 `68px`，长 CODE 和占位文本被截断；到 900 视口反而扩展为约 `246.5/286.5px`。查询标签与控件虽然保持同行，但桌面布局不能有效录入或核对查询值。

建议在 1440/1280 使用两列查询布局或给输入控件设置 180–240px 的有效最小宽度；空间不足时让完整的“标签+控件”成组换行，保持查询在左、重置在右。

### UX-003 — HIGH — 新增字典校验失败后无法定位具体字段

CODE 和名称留空点击“保存字典”后，只出现顶部总提示；焦点仍停留在保存按钮。两个字段没有内联错误、`aria-invalid` 或 `aria-describedby`。用户需要自行猜测错误位置，键盘和读屏用户无法直接恢复任务。

建议复用数据项弹窗已经通过的错误模式：顶部汇总、字段级错误、程序化错误关联，并把焦点移到第一个无效字段；修复后要验证输入保留、修正后错误清除和再次提交。

## 非阻断但必须处理的问题

- **UX-004 — MEDIUM：**查看页数据项搜索框在可访问树中是无名称的文本框，仅依赖 placeholder。应提供可见标签或 `aria-label`，并保持查询标签/控件同行。
- **UX-005 — MEDIUM：**编辑页 CODE 使用低对比度禁用态。若业务规则只是禁止修改，应使用可复制的只读语义和足够对比度，并保留明确的不可修改说明。

## 已通过并应保留的交互

- 列表每行“查看 / 修改 / 添加数据项”在四个视口均可见，页面没有水平溢出。
- 数据项弹窗上下分区明确，1440/1280/900/375 均可使用内部滚动；“添加保存”在左，“关闭”在右。
- 空提交时数据项弹窗提供总提示和字段级提示，并把焦点移到 CODE。
- `material_receipt` 能按不区分大小写规则命中已有 `MATERIAL_RECEIPT`，拒绝重复且保留输入。
- 原型内存新增后数量从 3 变为 4，录入区清空、焦点回到 CODE、弹窗保持打开，并滚动和高亮新行，可继续添加。
- 启停确认说明后续选择与历史数据影响；浏览器控制台未发现 error 或 warn。

以上新增仅发生在 5174 原型的本地内存中，没有调用正式 API，也没有写数据库。

## 审查覆盖与证据

本轮覆盖字典列表、新增、编辑、查看和新增数据项弹窗共 5 个界面，逐一检查 1440、1280、900、375 四个视口，共 20 张默认态截图。每张截图记录层级、栅格、间距、字体颜色、控件内容、操作、响应式和交互可访问性八类结论；另有 45 项界面维度审查、19 项最低 WCAG 2.2 检查、动态交互和几何测量。

- [审查清单](evidence/dictionary-ui-ux-audit-20261012/audit-manifest.json)
- [几何测量](evidence/dictionary-ui-ux-audit-20261012/geometry.json)
- [交互观察](evidence/dictionary-ui-ux-audit-20261012/interaction-observations.json)
- [清单校验结果](evidence/dictionary-ui-ux-audit-20261012/manifest-validation.txt)
- [截图目录](evidence/dictionary-ui-ux-audit-20261012/screenshots/1791792059022_dictionary-create-default-1440.png)

清单校验器返回 exit 1，明确拒绝基础字典错误恢复、详情搜索可访问名称、表单锚点和桌面查询宽度四项非 PASS 检查。该失败与本审查结论一致，未修改审查规则、阈值或断言来制造通过结果。

## 修复与完整复审结果

UX-001 至 UX-005 已全部关闭：描述字段恢复与普通字段相同锚点；查询区改为可读的两列布局并在窄屏成组回流；基础字典必填失败提供摘要、字段级错误、ARIA关联和首错聚焦；详情搜索增加可见标签和可访问名称；编辑CODE改为可复制的只读态并增加说明。复审过程中补充检查出默认主按钮文字和输入边界对比度不足，已在该原型范围内修正；最终实测主按钮 `6.16:1`、占位文字 `4.83:1`、输入边界 `3.36:1`。

最终以同一 5界面×4视口矩阵重新生成20张截图，45项界面维度、19项WCAG 2.2最低检查、动态交互和几何测量全部满足通过条件。清单校验器保持原规则与阈值，返回 exit 0：

- [复审清单](evidence/dictionary-ui-ux-audit-20261012/recheck/audit-manifest.json)
- [复审几何测量](evidence/dictionary-ui-ux-audit-20261012/recheck/geometry.json)
- [复审交互观察](evidence/dictionary-ui-ux-audit-20261012/recheck/interaction-observations.json)
- [对比度与状态公告证据](evidence/dictionary-ui-ux-audit-20261012/recheck/contrast-accessibility.json)
- [复审清单校验结果](evidence/dictionary-ui-ux-audit-20261012/recheck/manifest-validation.txt)
- [最终1440新增页截图](evidence/dictionary-ui-ux-audit-20261012/recheck/screenshots/1791794119271_dictionary-create-final-1440.png)

`npm run build` 已通过（vue-tsc + Vite，795ms），仅保留既有大包告警。Python旧原型脚本依赖本机未安装的`playwright`且仍断言旧的disabled CODE语义，因此没有把它作为最终通过证据；同等及更完整的检查由当前可用的CUAWright和Codex内置浏览器Playwright实际执行并写入上述证据。

## 后续门禁

当前原型可提交人工确认的精确源码哈希为 `62C7E714AD339FBFA7539DC270F7CEB38245D21A0E99C8B0D767DAC0DB903A25`。没有人工批准这个精确源码哈希，不得集成到正式5173项目。原型PASS不等于正式项目集成、真实API/数据库验收或发布。
