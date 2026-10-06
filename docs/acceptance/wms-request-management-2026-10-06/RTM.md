# WMS delta execution mapping

Authoritative definitions: FINAL BASELINE COMPLETE v1.0.19 / 00_WMS_REQUEST_READ_CONTRACT_V1.0.19.md §§8–9. This maps targeted executed methods to requirement groups; it does not claim a new full-system regression or human acceptance. Detailed executed names/counts are in verification-summary.json.

`M` = backend/mes-boot/src/test/java/com/hospital/mes/production/WmsRequestManagementIT.java. `C` = WmsRequestConcurrencyIT.java in the same directory. `L` = existing IncomingProductionIT.realIncomingReleaseThroughSignedWeighChargeReturnAndReversePreservesTrace. `P` = frontend/mes-web/e2e/wms-management.spec.ts.

| Requirement / formal test | Native or unit evidence | PC UI evidence |
|---|---|---|
| WMS-REQ-001 / TC-WMS-REQ-001 | M.managementRequestLifecycleRejectsStateAndRetainsSnapshotAndReplay; M.managementRetainedLinesAndInvalidLinkedIdentitiesFailClosed | P formal request create/submit and draft edit |
| WMS-REQ-002 / TC-WMS-REQ-002 | M lifecycle, permission/linked cancellation, invalid linked identities and HTTP/org methods; C.concurrentLinkedDraftCreationPreventsCancellation | P controlled version/idempotency payloads and discarded-edit reload |
| WMS-REQ-003 / TC-WMS-REQ-003 | M.managementPartialFullAndOverIssueAreAtomicAndReturnDoesNotReopen; C.concurrentConfirmationsCannotExceedDemand | P linked outbound subset/lot allocations |
| WMS-REQ-004 / TC-WMS-REQ-004 | M.managementMixedUnitsKeepGrossDemandAndOriginalReturnUnit; M retained/invalid identity method; MaterialRequestRulesTest | P bound request-line/formula/unit identities |
| WMS-ISS-REQUEST-001 / TC-WMS-ISS-REQUEST-001 | L; M linked identity and real signed production charge | Existing ui-blueprint.spec.ts: Issue selectors preserve existing IDs and command contract; P linked payload |
| WMS-INV-READ-001 / TC-WMS-INV-READ-001 | M multi-location/multi-batch method; M lot reference filters; M linked production charge verifies remaining reservation after consumption | P inventory row, quantities, location context and bounded layout |
| WMS-INV-READ-002 / TC-WMS-INV-READ-002 | M zero-balance integrity and multi-location inconsistent reservation; M/L QUARANTINE and pre-QA reservation rejection; L superseding QA rejection | P truthful quality/inventory status and explicit eligibility distinction |
| WMS-RET-READ-001 / TC-WMS-RET-READ-001 | M mixed-unit original/event distinction; partial/full return history; scoped queries and org denial | P return list/source quantities |
| WMS-RET-READ-002 / TC-WMS-RET-READ-002 | L consumed/excess entitlement, actual return/replay controls; M no demand reopen/stock double count | P exact existing return payload and scoped history navigation |
| UI-WMS-001 / TC-UI-WMS-001 | Actual permission catalog/routes and source-to-view mappings | P ordered five-entry menu, T1/T2/T3, actual lookup labels, request/issue/return actions, query preservation and screenshots |
| UI-WMS-002 / TC-UI-WMS-002 | Existing immutable return facts; no new return write entity | P independent return entry, actual history ID/context and no invented status |
| WMS-FLOW-001 / TC-WMS-FLOW-001 | M.managementLinkedDemandThroughSignedProductionChargeKeepsStockAndTrace; L | P new entry flows; backend/native chain tested independently |

Concurrent losers may receive existing controlled `CONCURRENT_MODIFICATION` under MariaDB repeatable-read; fresh retries must receive `REQUEST_QTY_EXCEEDED` or `REQUEST_HAS_ISSUES`. The tests verify both rejection and retained source facts, rather than requiring a race winner's obsolete read snapshot to be accepted.

The existing trace graph remains its frozen API; request detail's `linkedIssues` and returns expose the new request→issue association, while charge→MaterialLot→incoming quality continues through the existing graph. No additional trace-node contract was invented.
