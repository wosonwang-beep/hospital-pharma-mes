# Material policy RTM

TC-WGH-POL-001..005 maps approved release/weigh/charge invariants to MES-009/011, ProductionMaterialPolicyService, immutable ProcessSnapshot, weighing workbench and native incoming production acceptance. Execution pending; no acceptance inferred. Existing receipt/material DTOs and six incoming record meanings remain unchanged.


## Approved functional closure delta — v1.0.21

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.21.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.21.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.
