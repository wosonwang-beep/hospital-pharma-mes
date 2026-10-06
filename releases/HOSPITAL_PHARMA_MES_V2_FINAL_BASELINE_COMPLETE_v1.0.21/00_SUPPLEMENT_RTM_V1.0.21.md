# Supplemental RTM

EBR runtime: TC-EBR-RUN-001..009 → EBR-003/004/005/006/007 → MES-007/009/010.
WMS returns: TC-WMS-RET-001..006 → original WMS issue/return requirement → MES-008/011.
Weigh policy: TC-WGH-001..003 and TC-CHG-001..003 gain mandatory independent VERIFY, real configured scale/calibration, exact immutable envelope and failure cases in the weigh supplement.
Trace: TC-TRC-001 includes actual inspection task/item nodes.
V018 review evidence/FK ordering and V021 immutable issue returns are pending physical migrations, never claimed applied.
No task marked ready or accepted by publication.


## Approved functional closure delta — v1.0.21

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.21.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.21.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.
