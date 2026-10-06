# 投料追溯 / 成品库存定向验收证据

日期：2026-10-06。批准：用户“完善1和4的缺口，其他的缺口不用处理”“授权修改批准，投料记录是关键，不是称量。” [批准契约](../../development/DCP-TRACE-FINISHED-INVENTORY-001-APPROVED.md)。人工验收及提交/推送/main合并明确授权：“验收，提交和推送，合并到main”。运行状态仅见 MES_TASKS.md；本报告不宣称全系统重新认证。

## 实施结果

1. 原料 MaterialLot 沿真实 MaterialCharge、Genealogy、母批展开成品入库、请验、取样、结果、报告、QA决定和发货；每个实际母批只展开一次。反向追溯保留。投料不关联称量时也能构建追溯，不新增强制称量控制。
2. 追溯补充真实供应商/冻结收货厂家来源、领料申请、出库单及明细、退料事件。对应类别和来源链接遵守既有权限；图无悬空边。不编造投料记录与出库明细之间的逐条分配关系。
3. 成品库存新增既有库存权限控制的 `/finished/inventory` T1 入口，显示实际母批、产品、规格和库存/质量信息。现有 GET `/wms/inventory` 增加严格 finishedOnly、productId、mainBatchId 读取筛选，在分页前执行；原料记录的新增成品上下文字段为空。查询、重置、分页保持成品范围。
4. 成品可用量仍是账面未预留量；待验库存显示“禁止发货”，既有 QA/库存/有效期发货校验保持不变。产品名称/规格来自同组织当前产品主数据，不冒充历史快照。

本轮没有改变投料写入、称量/复核工序、库存计算、QA Gate、电子签名、权限定义或业务状态机。其它审计缺口未实施。

## 设计与迁移

- 当前权威：FINAL BASELINE COMPLETE v1.0.22；Global UI V2 / T1–T6 保持。
- [冻结一致性审查](../../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.22/00_TRACE_FINISHED_INVENTORY_CONSISTENCY_REVIEW.json)：8项 PASS；144个 v1.0.21 父版本文件哈希完全一致，既有物理迁移完全一致。
- [收尾校验](CONSISTENCY.json)：148个冻结内容文件 manifest / SHA256SUMS 完全匹配，AGENTS、MES_TASKS、PROJECT_BASELINE 当前指针一致。
- 数据库 / Migration：NONE；本机原生 MariaDB 验证31个迁移、当前版本031，无新迁移、repair、重建或数据清空。测试使用既有回滚夹具。

## 定向验证

| Gate | 结果与边界 |
|---|---|
| Backend compile + unit | PASS；TraceChargeProjectionTest 1项，三个无称量投料覆盖两个实际母批、去重且无伪造称量 |
| 原生 MariaDB integration | PASS；5项、0失败/错误；TraceInventoryClosureIT 2、WmsRequestManagementIT 2、FinishedGoodsLifecycleIT 1；完整成品质量/QA/发货、原料正反追溯、来源权限、服务器筛选/精确数量及封闭响应校验 |
| Frontend typecheck | PASS；npm run typecheck |
| Affected frontend unit | PASS；tracePresentation.test.ts + wms/model.test.ts，2文件4项；来源权限/导航及数量规则 |
| Build | PASS；npm run build；最终构建10.93秒，继承既有大包警告 |
| PC Chromium | PASS；3个不同场景：库存范围/产品筛选/重置/分页/批次跳转；无称量投料和退料来源；延迟原料响应不能覆盖成品页；针对性运行，不做移动端或全量回归 |
| 浏览器错误 / 溢出 | 测试场景0 console/page error，截图场景无页面横向溢出；产品选择框宽度已修正并断言 |
| Diff / scope | PASS；受影响 tracked diff whitespace 检查通过，配置文件及本地技能安装不在此次修改范围 |

浏览器验证使用既有 Playwright Chromium，因为当前会话无 Browser plugin skill。浏览器 API 使用受控 read fixtures，证明页面呈现、导航和请求参数；真实数据库和业务链由原生 integration 独立证明，不宣称浏览器直连真实后端端到端完成。后端测试选择明确方法，未运行继承的无关全套集成测试。

执行命令：

```text
mvn -f backend/pom.xml -pl mes-boot -am verify -Pci-integration -Dtest=TraceChargeProjectionTest -Dsurefire.failIfNoSpecifiedTests=false -Dfailsafe.failIfNoSpecifiedTests=false -Dit.test=TraceInventoryClosureIT#actualChargeForwardTraceReachesFinishedShipmentWithoutInventedAssignment+finishedInventoryFiltersBeforePagingKeepsExactStockAndScopedProductContext,WmsRequestManagementIT#managementLinkedDemandThroughSignedProductionChargeKeepsStockAndTrace+managementLotInventoryAndReferenceFiltersDoNotInventLocationReservations,FinishedGoodsLifecycleIT#completeChainRequiresQaThenShipsExactStockOnceAndTracesAllSources
npm run typecheck
npm exec vitest run src/views/production/tracePresentation.test.ts src/views/wms/model.test.ts
npm run build
npm exec playwright test e2e/trace-finished-inventory.spec.ts -- --project=chromium-desktop
python scripts/trace-finished-inventory-baseline.py check
```

最后一条 Chromium 全文件命令为复现入口；实际先运行两个场景，再只重跑受影响的库存/新增竞态场景，三个不同场景均 PASS。构建前的失败均已定位：编译夹具受检异常、旧接口缺少批准读字段、测试复合单元格/选择器定位；最终没有待处理测试失败。

## 截图与审查

1440×900 PC viewport，fullPage 保存实际页面高度；逐图人工读取检查。

- [成品库存](screenshots/finished-inventory-desktop.png)
- [投料中心追溯](screenshots/actual-charge-trace-desktop.png)
- [投料来源记录](screenshots/charge-source-drawer-desktop.png)

独立只读审查：CRITICAL/HIGH 0；发现1项 MEDIUM（旧库存异步响应跨范围覆盖），已使用请求代次保护及范围切换清空修复，延迟响应 Chromium 通过；审查复查确认关闭。竞态验证等待原始 requestfinished 和浏览器渲染回合后再断言。无新增阻断发现。

审查排除项及裁定：既有租户级/N+1库存聚合性能是继承 MEDIUM 技术债，未在本轮优化；用户本地配置/技能目录保留；浏览器真实后端联通不以 mock 测试替代，独立原生集成已执行；累积冻结文档由专用一致性审查/哈希检查验证，不要求代码审查重读整个基线。

剩余技术债：继承库存聚合/N+1性能和既有 frontend bundle >500KB 警告；均非新增功能阻断项。未启动其它缺口、新 MES Task、UI V3 或未授权 Workbench 字段。人工验收已确认；提交/推送/main合并已明确授权。
