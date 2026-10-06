# TI-01～05 执行映射

批准契约：DCP-TRACE-FINISHED-INVENTORY-001；权威 v1.0.22。证据范围见 [REPORT.md](REPORT.md)，状态仅 MES_TASKS.md。

| Requirement | 实施 | 执行证据 | 结果 |
|---|---|---|---|
| TI-01 实际投料原料正向至成品发货，反向保留，无强制称量 | TraceService actualChargeBatches + FinishedTracePort | TraceInventoryClosureIT.actualChargeForwardTraceReachesFinishedShipmentWithoutInventedAssignment；TraceChargeProjectionTest.unweighedChargesReachEachActualFinishedBatchOnce | PASS |
| TI-02 WMS/供应来源真实关系与权限 | WmsTraceQueryService / WmsTracePort / TraceService.wmsGraph | WmsRequestManagementIT.managementLinkedDemandThroughSignedProductionChargeKeepsStockAndTrace；TI-01 方法中权限/无悬空边校验 | PASS |
| TI-03 成品库存服务器筛选/精确数量/分页/组织范围 | FinishedInventoryContextPort，boot适配，WmsManagementQueryService | TraceInventoryClosureIT.finishedInventoryFiltersBeforePagingKeepsExactStockAndScopedProductContext | PASS |
| TI-04 封闭 API 投影与既有质量/发货/原料库存回归 | FrozenSchemaAssertions 对 v1.0.22；不变 stock/write controls | 两个新 IT 方法的 schema 校验；WmsRequestManagementIT.managementLotInventoryAndReferenceFiltersDoNotInventLocationReservations；FinishedGoodsLifecycleIT.completeChainRequiresQaThenShipsExactStockOnceAndTracesAllSources；父文件/迁移哈希及 OpenAPI refs 检查 | PASS |
| TI-05 PC T1/T4 展示/来源跳转/稳定成品范围 | finished inventory route/menu、WmsManagementListView、TraceView/tracePresentation | Chromium三个场景；tracePresentation与WMS model四项unit；三张实际截图 | PASS |

后端1 unit +5 native integration；前端4 unit +3不同PC场景。重跑不重复计入案例数。库存竞态复查已关闭；审批、数据库、状态机和电子签名规则未修改。
