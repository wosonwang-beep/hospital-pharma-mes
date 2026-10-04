# 07 功能详细设计说明书 V1.0.7 FROZEN

本文件是Codex施工图。每个功能固定映射：目的→前置→页面→API→Application Service→Domain Rule→数据表→状态→权限→审计→异常→事务→测试。

## FD-IAM-001 RBAC认证授权
**功能目的**：实现RBAC认证授权受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：登录/用户/角色/权限
**API**：/auth,/users,/roles
**Application Service**：AuthService/RbacService
**Domain Rule**：User-Role-Permission invariants
**数据表**：sys_user,sys_role,sys_permission,sys_user_role,sys_role_permission
**状态变化**：账户/RBAC状态
**权限**：iam:*
**审计/签名**：认证授权审计
**异常**：越权/禁用/锁定
**事务边界**：认证与授权事务分离
**测试点**：越权403、禁用拒绝
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-MAT-001 物料主数据
**功能目的**：实现物料主数据受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：/master/materials
**API**：/materials
**Application Service**：MaterialApplicationService
**Domain Rule**：批准版本不可原地修改
**数据表**：md_material,md_material_version,md_material_quality_spec,md_material_storage_rule,md_material_production_rule
**状态变化**：DRAFT→APPROVED→INACTIVE
**权限**：master:material:*
**审计/签名**：版本/审批审计
**异常**：重复编码/非法单位/历史版本修改
**事务边界**：Material+Audit
**测试点**：字段完整、版本不可变
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-PROC-001 工艺包/BOM/路线
**功能目的**：实现工艺包/BOM/路线受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：工艺设计器
**API**：/process-packages/*
**Application Service**：ProcessPackageApplicationService
**Domain Rule**：发布前lint；批准版不可改
**数据表**：proc_package*,proc_formula*,proc_route*,proc_operation_def,proc_parameter_def
**状态变化**：DRAFT→SUBMITTED→APPROVED→EFFECTIVE
**权限**：process:package:*
**审计/签名**：提交/审批/发布审计
**异常**：lint失败/职责分离
**事务边界**：版本发布事务
**测试点**：旧批不受新版影响
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-EBR-001 eBR Designer
**功能目的**：实现eBR Designer受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：eBR设计器
**API**：/ebr/templates/*
**Application Service**：EbrDefinitionService
**Domain Rule**：stable ID；白名单规则；Draft-only edit
**数据表**：ebr_template_version,ebr_section_def,ebr_group_def,ebr_form_def,ebr_field_def,ebr_rule_def
**状态变化**：DRAFT→SUBMITTED→APPROVED→EFFECTIVE
**权限**：ebr:designer:edit
**审计/签名**：定义/审批审计
**异常**：规则引用/循环/角色缺失
**事务边界**：定义版本事务
**测试点**：lint/simulate/publish测试
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-EBR-002 eBR Renderer/运行
**功能目的**：实现eBR Renderer/运行受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：车间动态表单
**API**：/forms/*
**Application Service**：EbrRuntimeService
**Domain Rule**：只读Snapshot；revision append-only
**数据表**：ebr_batch_snapshot,ebr_form_instance,ebr_field_value_revision,ebr_rule_execution,ebr_review_record
**状态变化**：DRAFT→SUBMITTED→VERIFIED→APPROVED
**权限**：ebr:form:*
**审计/签名**：提交/更正/复核/签名审计
**异常**：409并发/BLOCK rule/更正
**事务边界**：Form revision+Audit
**测试点**：旧批稳定、签名失效重签
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-WMS-001 收货库存预留发退
**功能目的**：实现收货库存预留发退受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：WMS
**API**：/inventory/*,/material-issues
**Application Service**：InventoryApplicationService
**Domain Rule**：Ledger append-only；FEFO；质量Gate
**数据表**：md_material_lot,wms_inventory_ledger,wms_reservation,wms_material_issue*
**状态变化**：各业务单据状态
**权限**：wms:*
**审计/签名**：库存调整审计
**异常**：负库存/冻结/过期/未放行
**事务边界**：movement+audit
**测试点**：流水可重算
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-BATCH-001 订单与正式批
**功能目的**：实现订单与正式批受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：生产管理
**API**：/production-orders,/main-batches
**Application Service**：BatchApplicationService
**Domain Rule**：MainBatch唯一Release主体；SubBatch只执行
**数据表**：prd_production_order,prd_main_batch,prd_sub_batch,prd_execution_unit,prd_process_snapshot
**状态变化**：MainBatch状态机
**权限**：production:*
**审计/签名**：状态转换审计
**异常**：定义未批准/非法SubBatch release
**事务边界**：release snapshot事务
**测试点**：快照原子性
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-OP-001 工序设备参数
**功能目的**：实现工序设备参数受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：车间执行
**API**：/operations/*
**Application Service**：OperationApplicationService
**Domain Rule**：资格/设备/清场/参数/IPC Gate
**数据表**：mes_operation_execution,mes_equipment_usage,mes_equipment_run,mes_parameter_value
**状态变化**：Operation状态机
**权限**：mes:operation:*
**审计/签名**：开始/暂停/完成审计
**异常**：Gate失败BLOCKED
**事务边界**：operation+audit
**测试点**：非法转换/设备重发
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-WGH-001 称量
**功能目的**：实现称量受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：称量页面
**API**：/weighings
**Application Service**：WeighingApplicationService
**Domain Rule**：lot RELEASED+BOM+资格+秤+偏差+独立复核
**数据表**：mes_weighing_record,md_material_lot,proc_formula_item,md_equipment,md_qualification
**状态变化**：DRAFT→CONFIRMED/VERIFIED
**权限**：mes:weigh:*
**审计/签名**：称量复核审计
**异常**：错料/过期/超偏差/自我复核
**事务边界**：weighing+audit
**测试点**：全部异常测试
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-CHG-001 投料与追溯
**功能目的**：实现投料与追溯受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：投料页面
**API**：/material-charges,/trace
**Application Service**：MaterialChargeApplicationService
**Domain Rule**：Charge是真源；不得用Issue替代
**数据表**：mes_material_charge,mes_quantity_event,wms_inventory_ledger,mes_genealogy
**状态变化**：CONFIRMED/REVERSED
**权限**：mes:charge:*
**审计/签名**：投料/反向审计
**异常**：未放行/错BOM/错工序/数量
**事务边界**：Charge+Event+Ledger+Genealogy+Audit
**测试点**：原子性/幂等/追溯
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-BAL-001 物料平衡
**功能目的**：实现物料平衡受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：批次物料平衡
**API**：/balances/*
**Application Service**：MaterialBalanceService
**Domain Rule**：只从QuantityEvent计算；规则冻结
**数据表**：mes_quantity_event,mes_balance_rule,mes_balance_result,mes_balance_investigation
**状态变化**：PASS/FAIL→INVESTIGATING→APPROVED
**权限**：balance:*
**审计/签名**：计算/调查/批准审计
**异常**：超限阻断
**事务边界**：计算版本+audit
**测试点**：专属工序口径/重算
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-QMS-001 IPC/QC/Deviation
**功能目的**：实现IPC/QC/Deviation受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：质量工作台
**API**：/ipc,/samples,/deviations
**Application Service**：QmsApplicationService
**Domain Rule**：原始结果不可覆盖；关键偏差Gate
**数据表**：qms_ipc_instance,qms_sample,qms_test_result_revision,qms_deviation,qms_capa
**状态变化**：IPC/QC/Deviation状态机
**权限**：qms:*
**审计/签名**：结果修订/调查审计
**异常**：OOS/重测/开放偏差
**事务边界**：quality+audit
**测试点**：修订链/Gate
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-REL-001 QA批审与放行
**功能目的**：实现QA批审与放行受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：QA批审
**API**：/release-decisions
**Application Service**：ReleaseApplicationService + BatchReleaseGateService
**Domain Rule**：ProductionComplete!=QCPass!=QARelease；仅MainBatch
**数据表**：qms_release_decision,prd_main_batch,md_material_lot,gxp_signature
**状态变化**：immutable decision
**权限**：qa:release
**审计/签名**：Release+Signature+Audit
**异常**：任一Gate失败
**事务边界**：Decision+lot availability+audit+outbox
**测试点**：所有Gate/不可变决定
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

## FD-AUD-001 审计电子签名
**功能目的**：实现审计电子签名受控业务闭环。
**前置条件**：用户已认证；组织上下文有效；相关上游对象存在且状态满足05状态机。
**页面**：审计/签名弹窗
**API**：/audit-events,/records/*/sign
**Application Service**：AuditService/SignatureService
**Domain Rule**：审计同事务；签名绑定digest
**数据表**：gxp_audit_event,gxp_signature,ebr_review_record
**状态变化**：VALID/INVALIDATED
**权限**：audit:view,ebr:sign
**审计/签名**：自身即证据
**异常**：篡改/重认证失败
**事务边界**：与业务同事务
**测试点**：摘要/失效/重签
**完成定义**：API契约、数据库迁移、后端服务、前端交互、权限、审计和自动测试全部通过；无未解释MISSING SPECIFICATION。

# V1.0.7 Platform Functional Boundaries — DCP-MES-001-R2-001
## MES-001

Owns platform AuditEvent append/query, signature infrastructure and generic APIs, central idempotency, integration inbox/outbox operations and central retry policy. Business write + AuditEvent + outbox/idempotency completion commit in one local MariaDB transaction where applicable. Reauthentication token consumption occurs atomically in the session store before the sign transaction and is never restored by a later rollback.

## MES-007

Owns eBR providers, exact signed snapshots/evidence identifiers, VERIFY/APPROVE gates and correction-driven signature invalidation in the correction transaction.

## MES-013

Owns QA_RELEASE_DECISION provider and the atomic release orchestration. It re-reads gates and composes immutable ReleaseDecision, signature, audit, inventory and outbox effects. The generic signature endpoint alone never changes release state.

## Controlled integration policy

Automatic retry limit 8 and delays PT1M/PT5M/PT15M/PT1H/PT4H/PT12H/PT24H come only from the controlled platform policy component. No controller, service, connector or module may embed or override them.

## DCP-MES-002-R2-001 — Six Incoming-Material Records

1. **原辅料收货记录** records supplier/order/delivery, material/version/lots/dates/quantity/packages, warehouse/location, transport and package/seal/label/damage/contamination checks, policy snapshot, receiver/reviewer and timestamps.
2. **请验单** records request type, receipt/lot, quality standard/specification versions, requested quantity/packages/date/priority and requester. It requests work but does not create a result.
3. **取样记录** records the task plan/SOP, assigned sampler and every SamplingDetail container/point, quantity/tool/conditions/reseal result, then creates traceable labeled Sample records.
4. **检验记录** records task/item frozen criteria, method, instrument/reagent/standard, raw observations/data, calculation inputs/output, attachments, performer, and append-only result revisions.
5. **检验报告** is independently prepared/reviewed/approved and references the final approved result revision for every item; it never retypes an untraceable result.
6. **物料放行记录** is the immutable scoped ReleaseDecision and its review evidence, actor/source, basis, reason, signature and supersession lineage.

For inspection-required material the complete chain is mandatory before release. For an inspection-exempt snapshot the system creates none of records 2–5, revalidates all qualification and receipt checks, and creates record 6 with `INSPECTION_EXEMPT`/`SYSTEM_RULE`. Any failure leaves the lot blocked.

`MaterialEligibilityService` is called by reservation, issue, weighing, and charge; it rechecks release, quality/inventory state, freeze and expiry/retest. Quantity on hand alone never establishes eligibility.





## DCP-MES-003-R2-001 approved delta

FD-ORG-001/FD-UOM-001/FD-EQP-001/FD-QUAL-001: query→standalone create/view/edit→named domain command→Profile M→atomic Audit/idempotency. Permissions, gates and tests are specified by DCP-MES-003-R2-001.


## DCP-MES-004-005-R2-001 approved contract completion

Material creation 201, complete typed DTOs, explicit version target/root optimistic token, Material root DRAFT/APPROVED/INACTIVE and version DRAFT/SUBMITTED/APPROVED, Supplier UNAPPROVED/APPROVED/INACTIVE and named commands, historical relationship revocation, and LG-004 delayed conversion-material FK follow 00_DESIGN_CHANGE_PROPOSAL_DCP-MES-004-005-R2-001_APPROVED.md. No existing table/column/path/permission/task dependency is added or removed. All previous unrelated contracts remain applicable.


## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.

## DCP-UI-LIST-EDIT-001 current presentation contract

white page header/cards on #f0f2f5 background, #2c5cdc primary buttons and 3px section title bar, 4px corner radius, compact 13px tables, 18px page titles, three-column desktop query controls, and two-column edit forms with 130px right-aligned labels. Query label/control remain inline on all viewport sizes; mobile edit forms have one column with label/control remaining side by side. Existing navigation chrome retained. Existing technical contract-strip metadata removed from master list presentation; operation controls and contract behavior retained.


Validation: affected module typecheck/build plus existing desktop/mobile query, edit/save, permission and conflict-preservation flows. No backend/data regression required for this CSS/template-only change.

## DCP-MES-006-R2-001 current contract

The approved 00_DESIGN_CHANGE_DCP-MES-006-R2-001_APPROVED.md is normative for MES-006 and supersedes prior contradictory process/product/eBR scope prose. Product is owned here; material has no business version; eBR is independently owned by MES-007. Process signature binds immutable business version and definition content. New physical V011 only. Test requirements include TC-PROC-004.
