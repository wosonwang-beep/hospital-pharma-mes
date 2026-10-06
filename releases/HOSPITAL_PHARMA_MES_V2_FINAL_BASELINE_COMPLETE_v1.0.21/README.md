# Current authority v1.0.21

Published cumulative release. Start with [current frozen manifest](00_MANIFEST_FINAL_FROZEN_V1.0.21.md) and [finished controlling supplement](00_FINISHED_GOODS_CONTRACT_V1.0.21.md). Inherited entries below describe their original scopes; current cumulative hashes are 00_FINISHED_GOODS_MANIFEST.json / 00_SHA256SUMS_V1.0.21.txt.

# FINAL BASELINE COMPLETE v1.0.21

[Current manifest](00_MANIFEST_FINAL_FROZEN_V1.0.21.md), [approved WMS contract](00_WMS_REQUEST_READ_CONTRACT_V1.0.21.md), [consistency review](00_WMS_CONSISTENCY_REVIEW.json). Parent v1.0.18 is immutable; current runtime readiness is only in MES_TASKS.md.


## Approved finished-goods delta — v1.0.21

[00_FINISHED_GOODS_CONTRACT_V1.0.21.md](00_FINISHED_GOODS_CONTRACT_V1.0.21.md) controls the approved finished chain. It supersedes inherited wording only for new OUTPUT stock effects (identity/quantity only), independent warehouse receipt, post-completion finished request/sampling/report, two additional QA gates, signed shipment and finished trace. Existing incoming quality, historical OUTPUT ledgers, immutable original results/OOS/retest, independent signed QA and post-decision FINAL PDF remain authoritative. V030/V031 append-only. UI V2/T1–T6 unchanged. Inherited embedded manifests/reviews attest prior deltas; current finished-goods manifest governs cumulative bytes. Acceptance/readiness remains in MES_TASKS.md.


## Approved optional inbound inspection draft — v1.0.21

[00_FINISHED_INBOUND_DRAFT_CONTRACT_V1.0.21.md](00_FINISHED_INBOUND_DRAFT_CONTRACT_V1.0.21.md) is the controlling bounded supplement: optional atomic linked inspection DRAFT during inbound creation; warehouse confirmation remains mandatory before execution. Existing tables/states/signatures/permissions/routes and 201 response unchanged. Only FinishedInboundCreate gains conditional optional command fields. No migration (native highest31); no physical stock from drafts. Existing UI T2/T3 only, no additional finished-image layout/menu scope. Test/RTM FD-01..06 and synchronous domain-event integration below are cumulative. Inherited reviews/manifests attest prior deltas, not current cumulative bytes. Current draft manifest/consistency review and SHA256SUMS govern this release. Readiness and human acceptance are only in MES_TASKS.md.
