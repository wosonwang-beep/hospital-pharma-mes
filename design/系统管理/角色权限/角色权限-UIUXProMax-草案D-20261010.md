# 角色权限优化：UI UX Pro Max 草案 D

2026-10-10；PENDING HUMAN DESIGN IMAGE APPROVAL。用户明确指定使用 UI UX Pro Max 优化角色权限；本次延续设计图评审阶段。

## 1. 基线依据

实际读取：AGENTS.md；docs/development/MES_DEV_RULES.md；MES_TASKS.md 系统管理当前入口；docs/PROJECT_BASELINE.md 权威入口；docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md；docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md 的 T2；docs/development/DCP-DATABASE-MENU-AND-USABILITY-001-APPROVED.md；docs/development/decisions/ROLE_PERMISSIONS_DESIGN_CONFIRMATION_20261010.md；frontend/mes-web/src/views/admin/RoleDetailView.vue 的字段/授权方法；frontend/mes-web/src/global-ui-v2.css 的 token。
业务权威 FINAL BASELINE v1.0.23 + 已批准 DCP，视觉仍 Global UI V2，模板 T2。现有设计确认记录保留；本草案没有获人工确认，不能作为第二套权威。

使用 C:/Users/Administrator/.agents/skills/ui-ux-pro-max/SKILL.md 的桌面 Web 适用规则、Query Contract、定向检索和可访问性检查；图像交付使用内置 image_gen。没有调用 UI UX Designer。
产品为医院制剂 MES 桌面管理工具，目标用户为角色配置管理员；目标是准确判断可见菜单、普通权限与独立特殊权限。既有技术栈为 Vue 3 / Ant Design Vue。

## 2. 设计差异

| 项目 | 原参考问题 | 草案 D |
| --- | --- | --- |
| 角色信息 | 左侧白色区域拉伸至整棵树高度 | 三字段表单按内容结束，释放留白 |
| 权限定位 | 需要向列头确认框的含义 | 同行重复只查看/管理文字，固定位置对齐 |
| 树层级 | 目录与业务菜单视觉较接近 | 目录淡中性底色和稍强字重，子项缩进/连接线 |
| 可读性 | 未选菜单名称偏浅 | 名称保持深色，关联权限控件才禁用 |
| 变更检查 | 权限区与变更摘要分散 | 权限区底部稳定显示新增/移除及明细入口 |

继承：菜单前唯一可见性 checkbox；目录行不展示普通权限；业务菜单行只查看/管理互斥；首次勾选默认现有普通管理，既有权限按原数据回显；共享权限按依赖保留；特殊权限独立配置。没有修改 menuCodes / permissionCodes、业务字段、权限编码、菜单顺序或路由/API。
图中的角色及数量仅为示例。字段沿用 roleCode（编辑只读）、roleName、status；读取角色/菜单/权限及保存仍使用既有 /roles、/menus、/permissions、/roles/{id}/permissions 契约。

标注规格目标：侧栏 228px、顶栏 56px、间距 24px、表单与搜索控件 34px、标题 22px、正文 14px、树行 44px、缩进 24px、圆角不超过 8px。以上为后续原型目标，栅格图不证明精确 DOM 尺寸。所有表单/搜索标签与控件水平同行。窄屏整组换行；左/右区域在 <=900px 可改为纵向，业务菜单的权限仍同行，375px 几何验证待原型阶段完成。

原型交互验收目标：菜单搜索保留全部已选授权和匹配祖先；目录支持展开收起/半选态；已选筛选不清空授权；普通权限可键盘操作、互斥且具备可见焦点；标签点击区域扩展至行内合理大小，避免勾选菜单时同时触发目录展开；只读/无权限禁用保持当前数据；保存失败保留选择并允许重试；取消按原契约处理未保存修改；特殊权限不随普通管理默认授予。这些为既有交互契约的检查目标，不宣称已实现。

## 3. 修改范围

仅新增本说明与角色权限-UIUXProMax-草案D-20261010.png。保留先前图、设计确认记录、所有用户文件与未提交变更；正式 Vue/Java、数据库、配置、迁移及任务状态未修改。迁移无，业务数据写入无，没有提交或推送。

## 4. 验收证据

Python 3.12.3；实际执行以下 UI UX Pro Max 本地检索，全部退出码 0：

- search.py "checkbox group selection desktop" --domain ux -n 4：0 条，未引用为数据库匹配证据。
- search.py "visual hierarchy" --domain ux -n 3：按 Query Contract 重试一次，3 条。首项 Web / Interaction / Hover States，与行反馈适用；其余 Breadcrumbs、Heading Hierarchy 可用于现有页头结构。树层级/控件语义具体依据技能 Quick Reference 与项目设计确认记录，不冒充搜索返回内容。
- search.py "form checkbox tree accessibility" --stack vue -n 3：0 条。
- search.py "forms" --stack vue -n 3：重试一次，2 条。结果为表单验证库及 v-model modifiers；不证明权限树规则，且不采用新增验证库建议，优先现有 Ant Design Vue 与 REUSE GATE。

设计图：内置 image_gen 编辑生成 1 次，定点修正 1 次（加深未选菜单名称、为六个目录增加淡底）。已直接查看图像；目录普通权限留空、逐行权限文字、特殊权限分隔、短左表单及底部变更区符合本次提案。没有重复修正或声称浏览器视觉 PASS。
最终文件 SHA-256：64001F62996BB06849EF8A0FD06E0E22ED2CD1D3D75CE8D9BBF81199BAEF48B2。
提示概要：以先前完整角色编辑图为参考，按 V2/T2 保留真实字段与七域导航；缩短基本信息区域，建立稳定权限列与44px行高目标，增加树层级、深色菜单名及区内变更摘要，特殊权限独立选择。原始生成与修正图均保留在 .codex/generated_images 中。

token 对比度计算：#20324d / 白 12.92:1；#617189 / 白 4.96:1；#1677ff / 白 4.10:1。前两者满足正常文字 4.5:1 目标；现有主色与白色的正常小字组合低于该目标，属于继承的全局 token 问题，未擅自换色或宣称 AA 全面合规。正式原型阶段需测量实际控件，并按全局设计治理核对该问题。
门禁：图像目视检查已执行；静态构建、Vue 交互原型、键盘/屏幕阅读器、375/900/1280/1440 DOM 几何、真实 5173/8080、数据库、多角色、回归全部 NOT RUN。

## 5. 结论

本次优化图已交付，待人工评审；运行状态继续以 MES_TASKS.md 的 IN PROGRESS 为准。未获草案确认及 mes-demo 5174 具体原型源码哈希确认前，不替代原 DESIGN LOCK，不进入正式 MES 集成，不宣称 READY FOR ACCEPTANCE / ACCEPTED。
尚需：批准具体设计方案后制作 Vue 3 / Ant Design Vue 原型，完成交互、异常、响应式与可访问性证据，锁定源码版本，再进入正式集成门禁。
