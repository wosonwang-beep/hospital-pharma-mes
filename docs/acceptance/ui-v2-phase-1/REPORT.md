# Global UI V2 Phase 1 — 验收记录

日期：2026-10-05。状态：**ACCEPTED**。Phase 2：**NOT STARTED**。

## 授权与基线

用户确认本轮范围：“全局视觉基础：统一 Shell、字体、颜色、间距、卡片和表单样式，保留各页面业务布局”。已读取 AGENTS.md、Global UI Design System V2、Page Template Standard V2。业务权威仍为 FINAL BASELINE COMPLETE v1.0.16 / 已批准 DCP；UI 文档的 v1.0.15 引用不构成业务回退。

本轮不修改业务字段、API、状态、路由、权限、数据库、审计或电子签名控制，不改导航领域分组，不重构各业务页面。无 T7，也没有启动后续 MES 任务。

## 实现

- `src/global-ui-v2.css`：统一视觉 token、浅色 Shell、字体、颜色、间距、卡片、表单、表格及受控弹窗样式；桌面侧栏 228px，顶栏 56px，内容边距 24/16/12px。
- `src/main.ts`：在已有业务布局样式之后加载全局 V2 样式层。
- `src/App.vue`：Ant ConfigProvider 使用相同 CSS token，包含弹窗/抽屉及中文组件文案。
- `src/layouts/AppLayout.vue`：侧栏组件宽度与全局 token 一致；菜单内容、权限和导航逻辑保持原契约。
- 桌面和手机表单标签与控件保持同行；修正手机证据抽屉超出屏幕、折叠菜单图标被旧样式隐藏的问题。

业务页面主结构没有改写。本轮浏览器证据对应的模板：

| 页面 | UI Template | 保留结构 |
| --- | --- | --- |
| 物料列表及查询 | T1 | 标题 / 查询卡片 / 结果表格 / 分页 |
| 物料创建 | T2 | 返回 / 标题 / 分区表单 / 保存取消 |
| QA 批放行审核 | T6 | Gate Checklist / 摘要 / 受控决定 / 证据目录 |

T3、T4、T5 页面未重构，本轮没有声称对这些页面逐一完成浏览器验收。

## 针对性验证

Browser plugin not available：使用仓库已有 Playwright / Chromium，渲染实际 Vue 应用。桌面 1280×720、手机 Pixel 5 393×727；API 使用测试响应，未连接本机数据库执行业务写入。本结果为 UI/交互验证，不是数据库端到端验收。

| 验证 | 结果 |
| --- | --- |
| `npm run build`（vue-tsc + Vite） | PASS；3398 modules；最终 Vite 构建 12.54s |
| `material-layout.spec.ts`，桌面/手机 | 4 PASS；覆盖 7 类主数据查询/编辑同行布局及工艺签名弹窗 |
| `mes013-release.spec.ts`，桌面/手机 | 16 PASS；QA 权限、阻断、冲突刷新、历史证据、归档与摘要校验 |
| `global-ui-v2-phase1.spec.ts`，桌面/手机 | 最终 4 PASS，16.0s |
| Scoped diff / screenshot review | PASS；本轮自查无未解决 CRITICAL/HIGH |

共 24 个不同浏览器用例通过，来自定向回归与修正后的 Phase 1 分开运行，未运行全量回归。初轮新增检查暴露侧栏旧宽度、手机抽屉越界；另有测试将必填标签的 `*` 排除在匹配之外，以及在抽屉动画结束前测量边界的问题。分别修正产品样式和测试定位/等待方式，最终新增用例全部通过。断言未削弱；抽屉关闭仍通过真实点击验证。

检查内容：Shell 展开/折叠、移动导航、查询/重置、输入、标签同行、页面无横向溢出、QA 辅助文字至少 12px、原始证据查看、抽屉完整处于水平视口、签名弹窗打开/取消，以及新增检查无 pageerror / console error。

## 截图

11 张最终截图位于 `screens/`；普通页面为全页，弹窗/抽屉为当前视口，避免全页截图误呈现固定遮罩。

| 内容 | 桌面 | 手机 |
| --- | --- | --- |
| T1 查询列表 | [截图](screens/t1-material-query-chromium-desktop.png) | [截图](screens/t1-material-query-chromium-mobile.png) |
| T2 创建表单 | [截图](screens/t2-material-form-chromium-desktop.png) | [截图](screens/t2-material-form-chromium-mobile.png) |
| T6 QA 审核 | [截图](screens/t6-qa-review-chromium-desktop.png) | [截图](screens/t6-qa-review-chromium-mobile.png) |
| 原始证据抽屉 | [截图](screens/evidence-drawer-chromium-desktop.png) | [截图](screens/evidence-drawer-chromium-mobile.png) |
| 电子签名弹窗 | [截图](screens/signature-dialog-chromium-desktop.png) | [截图](screens/signature-dialog-chromium-mobile.png) |
| 侧栏折叠 | [截图](screens/shell-collapsed-chromium-desktop.png) | — |

## 迁移、债务与停止点

无数据库迁移或数据库操作。用户已有 `application-local.yml` / `application-ci.yml` 本机配置保留；没有提交或推送。

剩余 LOW：继承的主 bundle 大于 500kB 的 Vite 提示（当前约 1.66MB，gzip 513.40kB）。本轮未做与视觉无关的拆包。其他页面逐页视觉核查、模板迁移及导航领域分组未纳入已批准 Phase 1；没有声明这些后续工作已经完成。

Phase 1 可供人工验收；现有 MES 任务 ACCEPTED 状态不变。完成本记录及截图后停止，未进入 Phase 2。


## 人工验收确认 — 2026-10-05

用户在 Phase 1 完成报告后回复“确认”，据此记录为 ACCEPTED。原验证与截图保留；本次仅更新验收记录，无新增验证或代码修改。Phase 2 未开始。
