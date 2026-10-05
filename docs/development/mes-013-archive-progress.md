# MES-013 archive implementation evidence — 2026-10-05

Approved DCP-MES-012-013-CONTRACT-001; immutable FINAL BASELINE COMPLETE v1.0.16 remains authoritative. No design / executed migration change. Root owns task status and final acceptance.

## Implemented scope

mes-release owns the archive command, insert-only manifest mapper, aggregation and the three exact eBR routes. Existing owner queries supply MainBatch children / process snapshot; operation equipment / runs / parameter facts; original eBR value revisions, corrections, invalidated reviews and rule executions; production QC original / corrected / retest results and deviation / CAPA history; quantity observations / weighing / charges / genealogy; balance rule / input / conversion / calculation / investigation evidence; actual finished lot / ledger; and immutable QA successor decisions.

Private source proof additionally preserves the actual production-plan approval row/envelope and related original audit / signature actor, time, digest and invalidation identities. Public projections remain frozen closed DTOs. ProductionTest specificationSnapshot now projects the persisted wrapped item as the exact flat SpecificationItemSnapshot, with string item/unit IDs; the original wrapper, signature canonical record, frozen plan, persisted facts and source proof are unchanged. The existing UI accepts both shapes. Independent nested frozen-schema validation caught and verified this correction.

Owner read extensions: EbrRuntimeService.archiveEvidence / runtime record projections; ProductionQueryService.batchFact; ExecutionQueryService.operationFact equipment / runs / parameters; MaterialBalanceService.evidence/sourceEvidence; ProductionQualityPlanService.sourceEvidence. The old eBR GET route is served by the exact BatchEbrReadModel aggregator; no duplicate route or foreign-owner writes.

## Integrity and archive behavior

Generation locks order / batch, then the WMS owner's actual finished lot / ledger before fresh evidence. SHA256 record digest excludes manifest, file, generated metadata, archive-generation audit / outbox effects and computed actions. Exact digest / kind reuses the immutable manifest/file; changed original evidence generates the next version. FINAL requires the actual effective finished QA decision, valid signature and matching batch / lot state. Existing Attachment delegation authorizes only manifest-associated files in the caller organization; unrelated files retain platform permissions. Generation failure rolls back file / manifest / audit / idempotency together.

Historic form signatures are reconstructed only from original immutable value-lineage prefixes and actual form-revision candidates whose platform canonical digest matches. This handles same-millisecond boundaries and invalidated original signatures. Missing reproducible evidence fails closed, rather than fabricating or omitting a historical envelope.

PDFBox 3.0.8 uses the root-provided licensed static Noto Sans SC subset. Sorted source evidence, digest-derived document IDs and fixed PDF metadata produce byte-identical PDFs for identical facts independent of JSON object insertion order. Chinese, original failures/retests, revisions, audit and signature identities are retained; credentials and reauthentication tokens are excluded.

## Targeted validation

Five distinct native business methods PASS across scoped / failure-only gates, not a repeated full suite:

- EbrArchiveIT (4 distinct): review-copy reuse → real QA decision → FINAL generation 2 / retained immutable predecessor / downloaded SHA256 / readable Chinese; pre-QA and invalidated-signature FINAL blocked; injected generation-audit rollback / key replay; real HTTP delegated download / unrelated and cross-org denial / unknown body rejection / exact nested BatchEbrReadModel and EbrPdfManifest schemas.
- EbrRuntimeArchiveIT (1): actual form signature and independent review → controlled correction → retained original signature / invalidated review / old and new values / exact frozen read-model schema. PASS 16.53 s.
- EbrPdfRendererTest (1 focused unit): insertion-order-independent byte-identical Unicode PDF. PASS 1.261 s.
- Root FrozenSchemaAssertionsTest verifies legal formal null unions and rejects wrong non-null types. PASS 0.055 s.

First native gate had three PASS and one missing attachment:upload fixture permission. Later independent helper initially mishandled the already-frozen type:[string,null] union: a test-helper defect, not a design conflict. Root corrected and tested the helper; no formal baseline or real nullable fact was changed. The helper then identified the real flat specification projection mismatch, fixed as described above. Only failed methods were rerun. Final HTTP / full-schema case PASS 19.54 s; gate BUILD SUCCESS, 39.530 s, completed 2026-10-05 12:17:30 +08:00, with final source-plan proof and WMS lock changes compiled.

Logs in workstation TEMP: mes013-archive-native.log, mes013-archive-targeted.log, mes013-archive-failed-only.log, mes013-archive-http-projection.log. Existing successful methods were not repeated without a relevant change.

Root-applied append-only V026 / schema 026 is unchanged; targeted startup validated all 26 migrations and applied none. Persistent fixtures roll back. .env and local / CI database configuration remain excluded from staging. Existing LOW scoped pagination / N+1 debt remains. No full regression, hosted CI, commit, push, human acceptance or task-readiness claim is made by this subtask. Root's separate final QA gate also passed actual late FAIL / OOS archive PDF, exact nested read-model schema, paired warehouse move and physical consumption behavior: 2 cases / 22.50 s, build 40.095 s, TEMP mes013-qa-oos-stock.log. These remain QA-agent cases and are not double-counted among this subtask's five native methods.
