# Independent WMS review and disposition

Fresh reviewer: wms_final_review, read-only review of working-tree/new files against AGENTS, approved DCP and implementation plan. Original verdict: CHANGES REQUIRED. No mutating tests or baseline-wide runtime audit were performed by the reviewer.

| Finding | Original severity | Disposition / evidence |
|---|---|---|
| Cancellation snapshot can miss a newly linked issue | HIGH | MaterialRequestService.hasIssues/linked use current locking reads during commands. C.concurrentLinkedDraftCreationPreventsCancellation proves create/cancel serialization, unchanged submitted request and linked draft, plus fresh retry rejection. Native MariaDB13 also protects some stale snapshots via controlled CONCURRENT_MODIFICATION; this protection was observed before the current-read correction, not misreported as a reproduced cancelled request. |
| Linked outbound UI forces all request lines and cannot split lots | HIGH | New unsaved allocations permit request-line selection, removing only unsaved rows, and splitting a line across lots. Existing persisted identities remain fixed. P linked outbound subset/split case reproduced missing controls before the fix and now passes. |
| Zero buckets bypass reservation integrity validation | MEDIUM, elevated for integrity effect | Zero-total lots reach lot-level validation; location match still requires true nonzero placement. M.managementZeroBalanceCannotHideOutstandingReservationIntegrity failed before the fix and passes afterwards. |
| Invalid date filters produce500 | MEDIUM | Controlled offset-date parsing produces INVALID_REQUEST/400. M.managementHttpContractAndOrganizationScope observed500 before the fix and now passes. |
| Full tenant reads and N+1 projection pagination | MEDIUM | Deferred bounded performance follow-up; no contract change or silent architectural expansion. |

Additional author finding: request detail/edit mode switching could retain discarded local form values. Combined id/mode watch reloads server facts. The draft-edit Chromium test failed with9 retained in T3 before correction; after correction it verifies server3, retained line identity and exact update version/payload.

Remaining identified CRITICAL0/HIGH0 reflects author disposition and targeted verification of the original review. No second independent review is claimed.

## Rulings and declined review areas

- MariaDB physical race: accept existing controlled concurrency conflict followed by a fresh invariant rejection; requiring only REQUEST_HAS_ISSUES on the first stale-snapshot transaction would assert against the existing concurrency contract. Cost if wrong: incorrect retry guidance; both no-mutation and fresh rejection are tested.
- Existing trace API stays frozen; new demand lineage is available through actual request/linked issue reads, not unapproved new graph nodes. Cost if wrong: a further approved trace-contract change would be needed.
- Reviewer declined to judge migration state, test execution, screenshots, release-wide consistency and runtime acceptance. Author separately verified native Flyway29, current-parent byte hashes/OpenAPI/RTM/pointers, targeted suites and PC screenshots. Human acceptance remains unclaimed.

Deferred minors: MEDIUM tenant-wide/N+1 pagination, and existing global build chunk-size warning. Neither is hidden by weakening tests or raising warning thresholds.
