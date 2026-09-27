# Backend module boundaries

The backend is a modular monolith. `mes-boot` is the only executable Spring Boot module; all other modules produce library JARs.

`mes-common` owns cross-cutting API contracts, exception mapping, and trace context. `mes-security`, `mes-system`, and `mes-masterdata` are Foundation boundaries. Domain modules (`mes-product`, `mes-process`, `mes-form`, `mes-ebr`, `mes-wms`, `mes-production`, `mes-execution`, `mes-equipment`, `mes-qc`, `mes-qms`, and `mes-release`) must not depend on the executable module. `mes-workflow`, `mes-traceability`, `mes-integration`, and `mes-reporting` remain supporting capabilities.

Core production state machines must remain executable without Flowable. Domain services must not return HTTP envelopes, and modules must not form dependency cycles. The architecture test in `mes-boot` enforces the module list, acyclic internal dependencies, and the single executable artifact rule.
