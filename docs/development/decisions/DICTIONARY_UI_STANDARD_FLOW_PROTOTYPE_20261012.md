# 数据字典功能界面标准流程原型锁定（待人工确认）

状态：`PASS FOR PROTOTYPE / READY FOR HUMAN DESIGN CONFIRMATION / NOT FORMALLY INTEGRATED`。需求来源：用户于2026-10-12要求按MES标准流程重新设计数据字典，并最终明确新增/修改/查看页面、数据项列表、连续新增弹窗、重复校验及按钮顺序。本记录锁定5174原型；用户未确认修复后重新审查通过的最终确切源码哈希前，不得集成到正式5173项目。

## 基线与范围

已读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md相关状态、Global UI V2、Page Template V2 T1/T2/T3、DCP-DATABASE-MENU-AND-USABILITY-001、现有字典模型/页面/路由/直接测试及mes-demo规则。业务权威仍为FINAL BASELINE v1.0.23及批准补充。此次只重组既有页面和交互，不增加数据库字段、API、权限、状态、路由语义或GxP控制。

## 待人工确认的精确设计

- 字典列表页 `#/system/dictionaries` 使用T1：页头、查询条件、字典列表。每行明确提供“查看 / 修改 / 添加数据项”，中小屏改为完整信息卡式行，关键操作不进入不可见的横向区域。
- 新增字典页 `#/system/dictionaries/new` 与修改字典页 `#/system/dictionaries/{id}/edit` 使用T2：独立完整表单页，字段严格复用现有DictionaryTypePayload（CODE、名称、类型、结构、排序、状态、描述）；修改时CODE只读。字典描述与操作区保持明确间距。
- 字典查看页 `#/system/dictionaries/{id}` 使用T3：顶部对象标题和“修改字典 / 新增数据项”，基本信息只读展示，下方数据项列表展示名称/CODE、业务说明、树形上级、排序、状态和操作，并支持名称/CODE搜索。
- 从列表或查看页进入数据项新增时复用同一弹窗：上方为DictionaryItemPayload录入区，下方为所属字典已有数据项列表。
- 点击“添加保存”后，新项立即进入下方列表并刷新数量；录入字段全部恢复新增默认值、焦点回到编码，弹窗保持打开。列表自动滚动到新增行并以浅蓝背景标记最新项，可继续添加。必填或重复失败时保留输入并在顶部与字段旁同时提示。
- 重复规则严格复用V039 `uk_dict_item_code(org_id,dict_type_id,item_code)`：同一组织、同一字典内数据项编码唯一，按MariaDB当前不区分大小写排序语义判重；不同字典可使用相同编码，名称不新增唯一约束。正式集成仍以数据库唯一键作并发最终裁决并显示409冲突。
- 修改数据项复用同一弹窗，编码只读；启停保留确认及历史数据说明。
- 本功能按钮顺序按用户明确规则锁定：查询区左“查询”、右“重置”；字典表单左“保存字典”、右“取消”；数据项弹窗左“添加保存/保存修改”、右“关闭”。
- 1440/1280保持桌面布局；900隐藏侧栏并使用可读信息行；375纵向排列。所有查询和表单标签/控件在同一水平行，整体配对换行，不拆开标签与控件。
- 原型只使用自然业务词和本地内存数据，不连接正式API/数据库，不写正式业务记录。

## Product Design审查结论

此前由Codex使用`product-design:audit`执行的审查结论已撤销。用户提供的新增字典截图仍存在明显栅格断裂：描述项从表单中部开始、左侧留下大面积无意空白、标签脱离其它字段锚点；Codex曾在看到截图后仍错误报告PASS。根因是审查只关注重叠/溢出和既定功能断言，没有执行完整的视觉构图、锚点、留白和对抗式复查。该记录只能证明已运行的功能与几何检查，不能证明产品体验通过。必须使用[完整MES UI/UX审查技能](../../../.agents/skills/mes-ui-ux-audit/SKILL.md)重新审查并关闭问题后，才能恢复原型体验结论；历史错误报告保留并标注撤销。

## 完整 MES UI/UX 审查结论

2026-10-12 首次由 `Codex SELF-REVIEW` 使用 `mes-ui-ux-audit` 和 `ui-ux-pro-max` 完成5界面×4视口审查并判定 `FAIL`，原始5项问题和失败清单完整保留。经用户授权直接修复后，UX-001至UX-005全部关闭，并补正主按钮文字、占位文字和输入边界对比度。相同20张截图矩阵、45项维度、19项WCAG、动态交互和几何测量重新执行，最终清单校验exit0，结论为 `PASS FOR PROTOTYPE`。详见[完整审查报告](../../review/DICTIONARY_MES_UI_UX_COMPLETE_AUDIT_20261012.md)、[原始失败清单](../../review/evidence/dictionary-ui-ux-audit-20261012/audit-manifest.json)及[最终复审清单](../../review/evidence/dictionary-ui-ux-audit-20261012/recheck/audit-manifest.json)。原型可提交精确哈希供人工确认，但尚未获准集成。

## 正式集成门禁

本阶段只允许修改mes-demo的PrototypeRoot.vue、features/dictionaries/DictionaryPrototype.vue、原型检查与证据，以及本记录/MES_TASKS状态。正式MES既有未提交改动保持不动。人工明确批准下列源码哈希后，才能把同一设计集成到正式Vue 3 / Ant Design Vue项目，随后必须验证真实5173/8080读写、权限、持久化、409重复冲突和返回定位；原型PASS不等于正式发布或验收。

## 验证结果与冻结哈希

- `npm run build`：PASS（vue-tsc + Vite，795ms）；保留既有大包告警。
- 动态交互：PASS。基础字典必填摘要/内联错误/首错聚焦；详情搜索可访问名称；CODE只读说明；数据项必填、大小写不敏感重复拒绝、连续两次新增、保存后清空回焦和新行定位均已实测。
- 响应式门禁：1440/1280/900/375全部PASS；字典表单7对及数据项弹窗7对标签/控件同行；页面无水平溢出；弹窗不超出视口。
- 对比度：主按钮文字`6.16:1`、占位文字`4.83:1`、输入边界`3.36:1`，达到对应阈值。
- 完整审查清单校验：exit0，`PASS FOR PROTOTYPE`，截图覆盖20/20。
- `DictionaryPrototype.vue` SHA256：`62C7E714AD339FBFA7539DC270F7CEB38245D21A0E99C8B0D767DAC0DB903A25`。
- 最终证据：[复审清单](../../review/evidence/dictionary-ui-ux-audit-20261012/recheck/audit-manifest.json)及[完整审查报告](../../review/DICTIONARY_MES_UI_UX_COMPLETE_AUDIT_20261012.md)。
- 旧`dictionary-prototype-check.py`因Python环境缺少playwright且仍断言旧disabled CODE语义，未计入最终PASS；最终检查使用CUAWright和内置浏览器Playwright执行，未修改审查阈值或产品断言制造通过。
本次原型自动修复未达到20轮门限。正式MES源码、数据库、Flyway、8080/5173运行服务、构建发布、提交和推送均未发生变化。
