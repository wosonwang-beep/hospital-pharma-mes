# MES 统一操作风格：禁止侧滑抽屉（2026-11-15）

状态：USER APPROVED / IMPLEMENTED / QA G5 SPECIAL PASS / FULL G5 IN PROGRESS
授权来源：用户在当前会话明确要求「检查代码，保持页面操作风格统一，禁止抽屉式的操作风格」。
基线：FINAL BASELINE v1.0.23、Global UI V2、T1–T6、MES_GLOBAL_UX_INTERACTION_20261102_APPROVED。

## 冻结范围
正式 Vue 页面禁止新增、编辑、查看、质量证据使用 Ant Design Drawer/侧滑抽屉；只读详情与证据统一使用居中 Modal，复杂受控编辑保留原有完整字段与保存动作，改为宽居中 Modal（限制可视区高度、内部滚动），后续独立页面设计仍按冻结 T2 原型执行。T1列表/T2表单/T3详情/T4批次/T5工序/T6质量页面的原有入口与路由不变。

## 本次源码白名单
- frontend/mes-web/src/views/{ebr/BookTemplateView.vue,integration/IntegrationOperationsView.vue,production/BatchOverview.vue,production/ExecutionOverview.vue,production/TraceView.vue,quality/FinishedReleaseView.vue,quality/IncomingDocument.vue,wms/ReceiptLiveView.vue}
- frontend/mes-web/src/{global-ui-v2.css,views/wms/live/receipt-flow.css}
- 仅在该范围内将9处a-drawer/Drawer改为居中a-modal/Modal；保持内部交互、弹出状态、表单值、列表、API、权限、业务状态及服务器调用完全不变；只读 Modal 无提交 footer，受控编辑保留原保存按钮。

## 验收门禁
- 全源码生产Vue/TS无可用侧滑Drawer实例；既有用户操作和键盘可关闭、模态框中文可读；1440/1280/900/375无横向滚动；QA门禁/归档/下载、批次与检验/收货证据可查看；build/vue-tsc/Vitest和相关Playwright回归真实运行。
- 不改数据库、Flyway、QA决定/签名/归档、后端API或主流程；P95基准独立验收，不以页面整改重新运行生产链。正式九维视觉矩阵未做完不得写G5 COMPLETE。