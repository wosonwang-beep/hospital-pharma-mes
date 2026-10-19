# 业务页打印入口现状核验 — 2026-10-15

范围：只读源码核验；未修改产品、权限或配置；尚非浏览器集成验收。

当前没有页面级打印入口配置 UI。系统管理现有「打印模板」(/admin/print-templates) 与「打印数据源配置」(/admin/print-data-sources)，负责模板、单据类型及取数配置，不能配置任意业务页按钮或记录 ID 取值。

现有页面需接通用组件：BatchPrintButton.vue 的必填入参 businessType:string、recordIds:string[]。收货单列表/详情使用 WMS_RECEIPT；检验报告列表使用 INSPECTION_REPORT。列表 selectedKeys 来自 a-table rowSelection.onChange，统一 String(id)，row-key=id；详情 ReceiptDetailView 的 printIds 取已加载 record.id。检验报告详情使用 InspectionPrintPanel.vue，入参 businessId:string、approved:boolean，固定 INSPECTION_REPORT。

权限：useAuthStore.can 检查从 /auth/me 加载的 identity.permissionCodes；按钮检查 print:document:generate，后端同样检查该权限，并通过业务数据 provider 校验记录访问权限。现有权限不因配置模板而自动授予。

模板选择：打开按钮时 GET /printing/applicable?businessType=...，后端按当前组织、单据类型、enabled 绑定筛选 PUBLISHED 模板并按绑定 id 升序返回。前端默认 choices[0]，用户可切换。生成 POST /printing/batches 或 /printing/artifacts 时明确传 templateVersionId；后端再校验发布状态、类型、启用绑定及内容摘要。因此不是后端自动唯一选择“最新模板”。取数由后端按所选模板绑定的数据源完成。

最短现状路径：已接入页面，配置并发布该单据类型的数据源与模板、启用模板绑定，具备业务读取及打印权限后，在业务页选记录→打印选中记录→选模板→生成预览。新业务页仍需代码接入通用组件；不能把页面级可配置能力描述为已实现。

证据：frontend/mes-web/src/components/printing/BatchPrintButton.vue；InspectionPrintPanel.vue；frontend/mes-web/src/views/wms/WmsListView.vue；ReceiptDetailView.vue；frontend/mes-web/src/views/quality/IncomingListView.vue；IncomingDocument.vue；frontend/mes-web/src/router/index.ts；frontend/mes-web/src/stores/auth.ts；backend/mes-reporting/src/main/java/com/hospital/mes/reporting/application/PrintService.java:63,83–90。
