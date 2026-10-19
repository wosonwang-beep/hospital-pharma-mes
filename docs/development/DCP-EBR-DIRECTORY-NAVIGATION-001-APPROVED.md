# DCP-EBR-DIRECTORY-NAVIGATION-001 — APPROVED

Date: 2026-10-13  
Status: **APPROVED BY USER / IMPLEMENTATION AUTHORIZED**

## Approval

The user explicitly approved the following electronic-batch-record organization on 2026-10-13:

```
电子批记录
├─ 批记录目录模板
├─ 工序表单模板
└─ 批记录实例
```

This approval supersedes only the affected navigation/name responsibility in V044. It does not alter regulated business states, signature rules, permissions, source-document ownership, or historical records.

## Functional responsibility

### 批记录目录模板
- Existing route family: `/ebr/book-templates`.
- Defines the directory/composition skeleton of a complete production-batch EBR.
- Selects one current production process and one effective process-form template version.
- Process nodes come from the selected production process; the directory does not create a second operation definition.
- Under each process operation, the directory references one or more existing operation-form definitions.
- Business records are references to their owning modules and are not recreated in EBR.

### 工序表单模板
- Existing route family: `/ebr/templates`.
- Defines the controlled forms used by process operations.
- One process operation may own one or more forms.
- Form fields, rules, signatures, reviews, canvas definition and effective version remain owned by the existing EBR definition module.

### 批记录实例
- Reuses the existing production-batch list/read contracts and the existing whole-book route `/production/batches/:id/book`.
- Adds a navigation/list entry `/ebr/batches`; it is a read-oriented T1 entry and must not create another batch aggregate.
- Opening a row goes to the existing whole-book instance for that actual production batch.
- No duplicate EBR instance table or second source of truth is authorized.

## Runtime relationship

```
产品
  → 生产处方
  → 生产工艺
      → 工序 1..N
          → 工序表单模板 1..N

批记录目录模板
  → 引用生产工艺中的工序
  → 引用工序对应的受控表单
  → 引用 WMS/QC/QMS/QA/生产执行实际记录

生产批下达
  → 冻结工艺快照
  → 冻结工序表单模板版本及定义摘要
  → 冻结批记录目录模板版本、定义摘要与目录定义
  → 形成批记录实例
```

## Freeze contract

The existing release path is authoritative:
- production snapshot stores the current production-process snapshot;
- production snapshot stores the effective EBR form-template version and definition;
- when a published directory template matches the process + EBR form-template version, the production snapshot stores `book.templateVersionId`, `templateRevision`, `definitionHash`, full directory `definition`, and binding;
- EBR runtime stores a batch snapshot and form instances referencing form definitions from the frozen template version;
- deactivating or superseding a directory/form template must not change an already released batch.

No new freeze table is authorized unless a verified gap proves the existing immutable snapshot is insufficient.

## Navigation and permission scope

Under Production Management, create a directory node `电子批记录` with:
1. `批记录目录模板` → `/ebr/book-templates`
2. `工序表单模板` → `/ebr/templates`
3. `批记录实例` → `/ebr/batches`

Permissions are reused:
- directory template: `ebr:template:view` + `process:package:view`
- operation-form template: `ebr:template:view` + `process:package:view`
- batch-record instance: `ebr:form:view` + `production:batch:view` + `master:product:view`

No new permission code is authorized.

## UI template scope

- 批记录目录模板 list: T1.
- 批记录目录模板 editor: approved specialized T2 composition editor.
- 工序表单模板 list: T1.
- form designer: approved specialized T2 designer.
- 批记录实例 list: T1 reuse of production-batch query contract.
- whole EBR instance: existing T4 aggregate detail.

## Explicit prohibitions

- Do not merge directory-template editing into operation-form-template editing.
- Do not duplicate process operations inside the directory.
- Do not duplicate WMS/QC/QMS/QA source records.
- Do not create another production-batch or EBR-instance aggregate.
- Do not mutate historical published template content or previously frozen batch snapshots.
