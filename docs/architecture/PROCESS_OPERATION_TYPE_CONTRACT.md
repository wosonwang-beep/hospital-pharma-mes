# Current process operation type contract

Authority: human-approved [DCP-PROCESS-OPERATION-TYPE-001](../development/decisions/DCP-PROCESS-OPERATION-TYPE-001-APPROVED.md). This bounded supplement extends v1.0.23/current-definition maintenance; frozen releases remain immutable.

## Data and domain

`ProcessCommands.Operation.operationType` and `proc_operation_def.operation_type` are optional, with exact values `PRODUCTION` or `IPC`. NULL means historically unmarked, not an inferred third business type. New frontend production rows explicitly default to PRODUCTION. Existing NULL values are never inferred from operation code, name or inspection definitions and never backfilled by migration.

The existing operation aggregate, physical IDs, order, parameter/inspection definitions and predecessor relationships remain. A non-null request value changes only that operation's type. Omitted or null request values preserve its stored type. Validation rejects other non-null values before any partial update. Database validation must also reject invalid casing and padded values.

## API and serialization

GET `/api/v1/process-packages/{id}` exposes `currentDefinition.route.operations[].operationType` when known. PUT `/api/v1/process-packages/{id}/current-definition` accepts the optional field with the existing Idempotency-Key. Null is omitted when serializing the property, preserving the legacy representation. Old 9/10-argument Java constructors remain compatible; canonical DTO has the new optional component.

The authoritative bounded OpenAPI supplement is `docs/contracts/BASIC_CURRENT_DEFINITION_OPENAPI.json`, component `com.hospital.mes.process.domain.ProcessCommands.Operation`. No route, permission or command name changes.

## State, quality controls and frozen consumers

The type is descriptive configuration. No new state machine or quality command is introduced. Existing IPC definitions, required/optional semantics, valid FAIL blocking, independent review/signature checks and declared completion/predecessor gates remain authoritative. A type label alone is not proof that inspection definitions or required results exist. Processes without IPC remain legal.

Production freezes subsequent current content through the existing process snapshot contract. Known types may appear in new snapshots; historical published JSON/hash/signatures, frozen production content, revisions and lineage are never recomputed or overwritten. Empty old types do not appear as added null properties. Current process maintenance keeps its existing ACTIVE/INACTIVE and DRAFT/EFFECTIVE semantics; no process revision approval is restored.

## UI and permissions

UI V2 and current T2 edit/T4 view remain. The approved prototype's production/IPC radio field is reused under the [local field design lock](../development/decisions/PROCESS_OPERATION_TYPE_FIELD_DESIGN_LOCK_20261102.md). Legacy rows show unselected choices plus historical-unmarked text. Existing edit permission and active/busy checks control disabled state. The whole route-ribbon/operation-workbench integration remains separately pending its full G3.5 and G5 gates; this supplement does not certify that redesign.

Existing `process:package:view/create/edit` codes, organization isolation and idempotency remain. Basic maintenance does not gain an audit/signature requirement; regulated production/eBR controls are unchanged.

## Migration, dependencies and validation

Only new append-only physical migrations may add/tighten the nullable column/check constraint. Version allocation follows actual successful native DEV Flyway history; other tasks' migrations are not modified. No UPDATE/backfill, record deletion, Flyway repair/clean or local configuration change is permitted.

Producer: current process definition, existing mes-process domain/application/infrastructure. Consumers: existing process UI and future snapshots; eBR form/role/CPP contracts are unchanged. No new hard dependency, task or module boundary. Runtime status is only MES_TASKS.md.

Required tests: OperationTypeContractTest, ProcessOperationTypePersistenceIT, ProcessDetailView.operation-type.test.ts; direct ProcessRules/IPC/execution gate regression. Transaction fixtures use the existing business-payload naming gate and roll back with explicit cleanup assertions. UI/real HTTP/full integration acceptance remains evidence-based and cannot be inferred from these tests.
