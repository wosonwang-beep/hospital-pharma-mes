# Hospital Pharmaceutical MES V2.0 Development Rules

1. Use Java 21 only and remain on Spring Boot 3.x.
2. Write MariaDB-compatible SQL only.
3. Use MyBatis-Plus for ordinary CRUD. Explicit SQL/XML is allowed for traceability, lineage, eBR aggregation, and reporting.
4. Never introduce Maven dependency cycles.
5. Model business state changes with commands and domain methods; never expose a generic updateStatus API.
6. Keep API response wrappers in the API layer; domain services must not return them.
7. Persistence entities and domain models may be separate types.
8. Never physically delete production or regulated records.
9. Use optimistic locking for mutable regulated aggregates.
10. Design critical operations for audit events and electronic signatures from the beginning.
11. Treat shared Flyway migrations as append-only; fix history with new migrations.
12. Do not make the core production state machine depend on BPMN or workflow runtime availability.
13. Dynamic form expressions must not execute arbitrary JavaScript, SQL, or SpEL.
14. RELEASED means finished-product release, not production completion.
15. Foundation work is limited to MES-001 through MES-011; MES-012 adds CI only. Do not pre-implement later business epics.
16. Applicable CI jobs must pass before merging. Do not bypass a failing job or weaken its assertions without an explicit, reviewed change to the CI contract.

Business modules use `api`, `application`, `domain`, and `infrastructure` boundaries. Cross-module notifications use domain events and complex cross-module reads use query services.
