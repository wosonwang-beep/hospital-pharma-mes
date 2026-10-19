# Review matrix

Complete every applicable cell for every screenshot. `N/A` requires a reason.

## 1. Hierarchy and comprehension

- The page title, section title, object identity, instructions, and primary action have an obvious order.
- The user can state what the page does and what to do next without reading implementation details.
- Related fields and actions are grouped; unrelated sections have visible separation.

## 2. Grid and alignment

- Record the major left/right anchors and column boundaries.
- Repeated labels share an anchor; repeated controls share left and right edges.
- Two-column forms have consistent column widths and gutters.
- Full-width fields use an intentional full-row pattern. Reject floating labels, arbitrary half-row starts, and unexplained empty columns.
- Textareas, alerts, dividers, and action bars align with the form grid and do not visually collide.

## 3. Spacing and density

- Vertical gaps express grouping consistently.
- No empty region dominates a row or card without an explicit purpose.
- Dense tables remain scannable; forms do not look sparse because fields occupy unrelated bands.
- Modal content fits the task and does not hide the result of the primary action.

## 4. Typography and color

- Headings, labels, helper text, values, codes, and status text have distinct and consistent roles.
- Body text is readable; muted text is not too faint; status is not communicated only by color.
- Required markers, errors, disabled states, and focus cues are clear.

## 5. Controls and content

- Labels are visible and associated with controls.
- Placeholders are examples or hints, not substitutes for labels.
- Long names/CODEs, localized text, empty values, and validation messages do not break layout.
- Defaults and disabled/read-only values are understandable.

## 6. Actions

- Primary, secondary, cancel/close, and destructive actions follow the approved order.
- Core row actions are visible and discoverable at every viewport; hidden horizontal scrolling is a failure unless explicitly designed and signposted.
- Success leaves the result visible and the next valid action clear.

## 7. Responsive behavior

- Label/control pairs remain on one horizontal line as required by MES governance.
- Complete pairs wrap together; content does not overflow or disappear.
- Reflow preserves task order, identity, status, and actions.
- Mobile targets are large enough and not packed together.

## 8. Interaction and accessibility

- Focus order follows reading order and focus appearance is visible.
- Modal focus enters, stays contained where applicable, and returns appropriately.
- Errors are announced and connected to fields; input is preserved for recovery.
- Success/loading/state changes are perceptible and do not rely on color alone.
- Screenshot review may identify risks but cannot claim full screen-reader or WCAG compliance.

## Full-dimension B端 checklist

本矩阵与上方逐图八类检查同时执行。对每个后台页面、列表、查询栏、表单、弹窗、抽屉、详情 surface 登记全部九维，不限业务场景，禁止只看首屏或默认态。按实际需求/设计/控件确认每项是否存在，再检查；不能跳过整维度。每项记录规范来源、实际观察、复现步骤/测量、证据、严重度及结论。

|维度ID|完整检查内容|
|---|---|
|layout|页面/区域层级、栅格与分栏、容器边界、阅读/操作顺序、主次区域、响应式重排、滚动与溢出、标题/对象/操作入口定位|
|typography|标题/正文/标签/帮助/错误/状态/数字与代码的字号字重行高层级、文本对比度、长内容/缩放、截断及完整值获取、一致的业务命名|
|spacing_alignment|外边距/内边距/行列间距、分组留白/密度、基线与栅格锚点、标签控件同行、控件/按钮/分隔线对齐、跨列项、无意空白/裁切/漂移|
|forms|标签与程序化关联、必填/默认/说明、字段来源/字典/Picker、控件类型/宽度、校验时机与位置、输入约束/依赖、只读/禁用、保存取消、错误保值及恢复、重复提交|
|tables_lists|列/表头/排序/数据对齐、列宽/密度、长内容/空值/数字/日期/状态、筛选分页计数语义、行选择/批量/行操作、操作可发现性、加载空错误态、横向滚动及重要列/按钮可见性|
|query_filters|标签控件同行整组换行、条件类型/宽度/来源、条件与查询结果一致、查询重置/回车、默认与联动、已用条件可见、复杂筛选展开/收起、结果数量/分页/筛选范围及无结果恢复|
|component_interaction|按钮/链接/选择器/日期/树/页签等实际操作、键盘/焦点/快捷键、弹窗抽屉焦点进入/约束/返回、关闭取消/未保存提示、反馈位置/时机、成功后的可见结果/下一动作、失败/重试/重复操作|
|component_states|对实际组件逐项核对 default/hover/focus/active/selected/expanded/disabled/read-only/loading/empty/validation-error/service-error/success/long-content 及适用权限状态；状态外观、动作、反馈和切换均验证，不以静态截图替代交互|
|wcag22|逐项基础 A/AA 检查：语义关联/标签、非纯颜色、文字/控件对比、缩放重排、键盘及无焦点陷阱、焦点顺序/可见/不遮挡、可访问名称角色值、错误识别/建议、状态播报、拖拽替代及目标尺寸|

### 基础 WCAG 2.2 检查

以 [W3C WCAG 2.2](https://www.w3.org/TR/WCAG22/) 对应成功准则和例外为依据，在 `wcag22_checks` 中逐项记录：1.3.1、1.4.1、1.4.3、1.4.4、1.4.10、1.4.11、2.1.1、2.1.2、2.4.3、2.4.7、2.4.11、2.5.3、2.5.7、2.5.8、3.3.1、3.3.2、3.3.3、4.1.2、4.1.3。这是最低基础检查集合；需求或现有规范要求其它准则时追加，不能拿该集合宣称覆盖整份 WCAG 或完整 A/AA 合规。

检查 DOM 可访问树/名称/角色、实际键盘与焦点、对比度计算、缩放和重排、错误与状态播报证据。基础检查包含普通文本4.5:1/大文本3:1、适用非文本UI对比3:1，按准则定义和例外判断；2.5.8 最小目标24×24 CSS px或满足其规定的例外，现有44×44移动可用性要求独立保留，不能把两者混称。2.4.11 是焦点不被完全遮挡的最低准则，不能把2.4.13的AAA焦点外观规则当成AA硬性门槛。信息密集表格的重排例外需逐项说明，不能豁免整个页面；未实际执行屏幕阅读器检查不得声明屏幕阅读器兼容。

WCAG 单项确实不适用时可记 N/A，但必须核对控件/业务存在性、准则适用范围并保存原因和证据；未执行不是 N/A。整维度不能 N/A。完整评审报告须保留未完成项，NOT RUN 阻止完整评审通过。

## Issue severity

- `CRITICAL`: blocks the task, exposes the wrong action/data, or creates severe accessibility failure.
- `HIGH`: major layout break, hidden primary action, misleading hierarchy, or failed error recovery.
- `MEDIUM`: significant friction, inconsistency, or readability issue with a workaround.
- `LOW`: polish issue that does not impede the task.
