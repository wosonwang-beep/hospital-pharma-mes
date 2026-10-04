# MES-007–011 completion plan — 2026-10-04

Goal: finish the previously implemented tasks against v1.0.14; preserve accepted incoming facts and all existing work. Execute inline using writing-plans/executing-plans; no commits/pushes or extra design. Start MES-007, then explicitly authorized MES-008/008A/009/010/011 in dependency order. Test evidence must name actual assertions, not blanket PASS.

- [x] MES-007: verify frozen V1 renderer after publishing V2, competing-client revision/idempotency/permission/tenant boundaries, independent signature/review sequencing, actual operation completion and completed-operation correction refusal. Files: EbrRuntimeIT and affected runtime service only on demonstrated failure.
- [x] MES-008/008A: exercise real FEFO and issue-not-charge; preserve existing accepted six-record chain. Reuse native existing WmsIT evidence and add exact eligibility boundaries in IncomingProductionIT.
- [x] MES-009: actual DIRECT and SUB_BATCH release, release initialization/audit failure atomicity, immutable snapshots, order allocation and production-completion guard. Files: IncomingProductionIT, ProductionService only on demonstrated failure.
- [x] MES-010: operation sequence/state/required eBR, equipment run pause/resume, trusted device append/replay/conflict/manual spoof and qualification/calibration. Files: IncomingProductionIT/ExecutionService. IPC/clearance without a real producer fail closed; never fabricate producer evidence.
- [x] MES-011: weighing wrong material/tolerance/expired calibration, charge audit/genealogy rollback, replay/return/trace isolation and final QA supersession. Files: IncomingProductionIT and affected weigh/trace services only on failure.
- [x] Targeted native gate on unique rollback fixtures; typecheck if frontend changes. Review new changes once, fix HIGH/CRITICAL, write per-task RTM/readiness and task index. Only human sets ACCEPTED. No full regression by default.

Preflight: hard dependency producers physically exist, V001–V022 executed. Current branch/worktree contains all ongoing implementations and user-owned configuration; reuse it. Prior incoming acceptance remains valid. Missing IPC/clearance/change-control producer is a scoped dependency boundary, not grounds to expand MES-012/013. No physical migration is planned unless a real in-scope schema defect is demonstrated.

## 2026-10-04 increment closeout

All six planned increment steps above have passing evidence and review. Required repeated MULTI_ENUM arrays and optional zero-row groups were reproduced and fixed; real HTTP binding was corrected; demonstrated snapshot mutability was fixed with append-only V023. V001–V022 unchanged.16 new native methods and14 affected units passed; final affected regression passed3 eBR+2 production cases. Native history23 successful/0 failed; test sentinels0. Frontend unchanged, so prior mocked browser/typecheck evidence reused.

Checked items identify this verified increment; they do not prove whole-task acceptance. Exact per-method evidence, formal-TC mapping and remaining concurrency/negative/producer obligations: [implementation increment](../acceptance/incoming-quality/MES-007-011-2026-10-04.md). All task rows remain IN PROGRESS. Actual IPC/clearance dependency gaps are fail-closed boundaries, not authorization to create MES-012/013 producers.


## 2026-10-04 second increment ledger

- Existing current allocation/QA supersession gates are reused, not rerun; MES-007 human accepted, MES-009 ready. Authorization continues MES-008–011 bounded tasks.
-8 new native final-command expiry/retest cases and2 incoming review/OOS controls PASS. Two SQL postcondition fixture errors corrected and only those cases rerun; no production behavior change. Time control is an injected test clock, not mocked eligibility or stored-fact mutation.
- Independent scoped increment review no CRITICAL/HIGH. V001–V023 validated; no migration or new retained fixture. No full regression.
- Ruling: formal FROZEN invariant exists but no authored freeze command/API/signature contract; do not fabricate one or certify its actual workflow. Required production IPC/clearance producer unavailable; don't substitute unknown DSL rejection or preimplement MES-012. Cost: affected whole-task readiness remains IN PROGRESS until formal producer/contracts exist.
- Exact evidence and remaining requirements: ../acceptance/incoming-quality/CLOSEOUT-2026-10-04-R2.md. Prior checked increment steps remain historical, not blanket whole-task completion.


## 2026-10-04 third increment ledger

7 new native methods PASS, no product/UI/schema changes. Required report/sample and competing quality/charge facts plus direct QC/attachment producer boundaries closed; exact mapping: ../acceptance/incoming-quality/CLOSEOUT-2026-10-04-R3.md. Failed test assumptions corrected to authoritative existing fields/exception types; meaningful quantity assertion uses required quantity rather than comparing missing fields. Concurrent consumption checks stale version refusal and then fresh current-version quantity exhaustion, not arbitrary409 alone.35 retained trial lots all blocked. Independent scoped review no concrete CRITICAL/HIGH. Wider freeze/IPC/clearance/remaining RTM boundaries retained; no invented contract or MES-012 expansion. User asked for continued closeout, not human acceptance.
