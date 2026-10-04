# 08 医院制剂 MES V2.0 UI/页面详细设计说明书 V1.0.12 — FROZEN

## 1. 强制页面分离规范
- **Query/List Page 只做查询**：查询条件、查询、重置、结果表格、排序、分页、导出（有权限时）以及导航到 Create/View/Edit。
- **Create Page 独立 Route**：只负责新增表单和保存/取消，不与列表共页。
- **View Page 独立 Route**：只读详情、状态、版本、关联信息、审计入口；不能以列表抽屉替代。
- **Edit Page 独立 Route**：只负责允许状态下的修改；Approved/历史版本默认只读并通过“创建新版本”进入新页面。
- 禁止在 Query/List Page 中以内嵌表单、Modal、Drawer 完成新增、查看详情或正式编辑。
- 删除/停用等轻量确认可以使用 Confirm Dialog；电子签名使用专用 GxpSignatureDialog，不视为详情页。
- 高频生产工作台（ExecutionUnit、称量、投料、eBR Renderer）属于事务型工作页面，不套用CRUD列表规则；其查询入口仍与执行页面分离。

## 2. Query Page统一结构
顶部 PageHeader；其下 SearchPanel；再下 Toolbar（New/Export等导航动作）；主体 ResultTable；底部 Pagination。
SearchPanel 默认2行以内，支持展开高级条件；按钮固定 Query / Reset。回车触发查询。查询条件同步URL query，返回列表时保留。
ResultTable 操作列只允许 View / Edit / 状态动作 / 跳转，不在当前列表打开详情编辑表单。

## 3. Create/View/Edit统一结构
Create/Edit：PageHeader + Form Sections/Tabs + Footer Action Bar（Save/Cancel，必要时Submit）。View：PageHeader + Status/Version + Summary/Tabs + StateActionBar + Audit/Revision入口。

## 4. 页面族详细拆分

### UI-IAM-USR-Q 用户查询列表
Route: `/admin/users`  | Requirement: IAM-001,IAM-002 | Permission: `iam:user:view`
查询条件：用户名、姓名、状态、角色
结果列：用户名、姓名、状态、角色、最后登录、更新时间
功能：Query、Reset、分页、排序；`新增`导航 `/admin/users/create`；View导航 `/admin/users/:id`；Edit导航 `/admin/users/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-IAM-USR-C 用户新增
Route: `/admin/users/create` | Permission: `iam:user:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-IAM-USR-V 用户详情
Route: `/admin/users/:id` | Permission: `iam:user:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/admin/users/:id/edit`，不得切换当前详情页为编辑态。

### UI-IAM-USR-E 用户修改
Route: `/admin/users/:id/edit` | Permission: `iam:user:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-IAM-ROL-Q 角色查询列表
Route: `/admin/roles`  | Requirement: IAM-002 | Permission: `iam:role:view`
查询条件：角色编码、名称、状态
结果列：角色编码、名称、状态、更新时间
功能：Query、Reset、分页、排序；`新增`导航 `/admin/roles/create`；View导航 `/admin/roles/:id`；Edit导航 `/admin/roles/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-IAM-ROL-C 角色新增
Route: `/admin/roles/create` | Permission: `iam:role:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-IAM-ROL-V 角色详情
Route: `/admin/roles/:id` | Permission: `iam:role:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/admin/roles/:id/edit`，不得切换当前详情页为编辑态。

### UI-IAM-ROL-E 角色修改
Route: `/admin/roles/:id/edit` | Permission: `iam:role:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-MAT-Q 物料查询列表
Route: `/master/materials`  | Requirement: MD-MAT-001 | Permission: `master:material:view`
查询条件：物料编码、名称、类别、规格、状态、关键物料
结果列：编码、名称、类别、规格、基本单位、质量标准、关键物料、状态、版本、更新时间
功能：Query、Reset、分页、排序；`新增`导航 `/master/materials/create`；View导航 `/master/materials/:id`；Edit导航 `/master/materials/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-MAT-C 物料新增
Route: `/master/materials/create` | Permission: `master:material:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-MAT-V 物料详情
Route: `/master/materials/:id` | Permission: `master:material:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/master/materials/:id/edit`，不得切换当前详情页为编辑态。

### UI-MAT-E 物料修改
Route: `/master/materials/:id/edit` | Permission: `master:material:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-SUP-Q 供应商查询列表
Route: `/master/suppliers`  | Requirement: MD-SUP-001 | Permission: `master:supplier:view`
查询条件：供应商编码、名称、资质状态、有效期
结果列：编码、名称、资质状态、有效期、更新时间
功能：Query、Reset、分页、排序；`新增`导航 `/master/suppliers/create`；View导航 `/master/suppliers/:id`；Edit导航 `/master/suppliers/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-SUP-C 供应商新增
Route: `/master/suppliers/create` | Permission: `master:supplier:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-SUP-V 供应商详情
Route: `/master/suppliers/:id` | Permission: `master:supplier:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/master/suppliers/:id/edit`，不得切换当前详情页为编辑态。

### UI-SUP-E 供应商修改
Route: `/master/suppliers/:id/edit` | Permission: `master:supplier:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-EQP-Q 设备查询列表
Route: `/master/equipment`  | Requirement: MD-EQP-001 | Permission: `master:equipment:view`
查询条件：设备编码、名称、类型、状态、校准到期
结果列：编码、名称、类型、状态、校准到期、位置
功能：Query、Reset、分页、排序；`新增`导航 `/master/equipment/create`；View导航 `/master/equipment/:id`；Edit导航 `/master/equipment/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-EQP-C 设备新增
Route: `/master/equipment/create` | Permission: `master:equipment:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-EQP-V 设备详情
Route: `/master/equipment/:id` | Permission: `master:equipment:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/master/equipment/:id/edit`，不得切换当前详情页为编辑态。

### UI-EQP-E 设备修改
Route: `/master/equipment/:id/edit` | Permission: `master:equipment:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-PKG-Q 工艺包查询列表
Route: `/process/packages`  | Requirement: PROC-001,PROC-BOM-001,PROC-ROUTE-001,PROC-PARAM-001 | Permission: `process:package:view`
查询条件：产品、工艺包编码、版本、状态、生效时间
结果列：产品、工艺包、版本、状态、生效时间、审批人
功能：Query、Reset、分页、排序；`新增`导航 `/process/packages/create`；View导航 `/process/packages/:id`；Edit导航 `/process/packages/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-PKG-C 工艺包新增
Route: `/process/packages/create` | Permission: `process:package:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-PKG-V 工艺包详情
Route: `/process/packages/:id` | Permission: `process:package:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/process/packages/:id/edit`，不得切换当前详情页为编辑态。

### UI-PKG-E 工艺包修改
Route: `/process/packages/:id/edit` | Permission: `process:package:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-EBR-TPL-Q eBR模板查询列表
Route: `/ebr/templates`  | Requirement: EBR-001,EBR-002,EBR-003 | Permission: `ebr:template:view`
查询条件：模板编码、产品、工艺包、版本、状态
结果列：模板编码、名称、产品、版本、状态、更新时间
功能：Query、Reset、分页、排序；`新增`导航 `/ebr/templates/create`；View导航 `/ebr/templates/:id`；Edit导航 `/ebr/templates/:id/designer`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-EBR-TPL-C eBR模板新增
Route: `/ebr/templates/create` | Permission: `ebr:template:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-EBR-TPL-V eBR模板详情
Route: `/ebr/templates/:id` | Permission: `ebr:template:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/ebr/templates/:id/designer`，不得切换当前详情页为编辑态。

### UI-EBR-TPL-E eBR模板修改
Route: `/ebr/templates/:id/designer` | Permission: `ebr:template:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-WMS-REC-Q 收货单查询列表
Route: `/wms/receipts`  | Requirement: WMS-001 | Permission: `wms:receipt:view`
查询条件：收货单号、物料、供应商、批号、状态、日期
结果列：单号、物料、供应商、供应商批号、数量、状态、日期
功能：Query、Reset、分页、排序；`新增`导航 `/wms/receipts/create`；View导航 `/wms/receipts/:id`；Edit导航 `/wms/receipts/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-WMS-REC-C 收货单新增
Route: `/wms/receipts/create` | Permission: `wms:receipt:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-WMS-REC-V 收货单详情
Route: `/wms/receipts/:id` | Permission: `wms:receipt:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/wms/receipts/:id/edit`，不得切换当前详情页为编辑态。

### UI-WMS-REC-E 收货单修改
Route: `/wms/receipts/:id/edit` | Permission: `wms:receipt:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-WMS-ISS-Q 发料单查询列表
Route: `/wms/issues`  | Requirement: WMS-003,WMS-004 | Permission: `wms:issue:view`
查询条件：发料单号、MainBatch、物料、状态、日期
结果列：单号、MainBatch、状态、发料时间、创建人
功能：Query、Reset、分页、排序；`新增`导航 `/wms/issues/create`；View导航 `/wms/issues/:id`；Edit导航 `/wms/issues/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-WMS-ISS-C 发料单新增
Route: `/wms/issues/create` | Permission: `wms:issue:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-WMS-ISS-V 发料单详情
Route: `/wms/issues/:id` | Permission: `wms:issue:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/wms/issues/:id/edit`，不得切换当前详情页为编辑态。

### UI-WMS-ISS-E 发料单修改
Route: `/wms/issues/:id/edit` | Permission: `wms:issue:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-ORD-Q 生产订单查询列表
Route: `/production/orders`  | Requirement: PRD-001 | Permission: `production:order:view`
查询条件：订单号、产品、计划日期、状态
结果列：订单号、产品、计划量、日期、状态
功能：Query、Reset、分页、排序；`新增`导航 `/production/orders/create`；View导航 `/production/orders/:id`；Edit导航 `/production/orders/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-ORD-C 生产订单新增
Route: `/production/orders/create` | Permission: `production:order:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-ORD-V 生产订单详情
Route: `/production/orders/:id` | Permission: `production:order:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/production/orders/:id/edit`，不得切换当前详情页为编辑态。

### UI-ORD-E 生产订单修改
Route: `/production/orders/:id/edit` | Permission: `production:order:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-BAT-Q 生产批查询列表
Route: `/production/batches`  | Requirement: PRD-002,PRD-003,PRD-004 | Permission: `production:batch:view`
查询条件：批号、产品、订单、状态、计划日期
结果列：批号、产品、订单、计划量、状态、生产日期
功能：Query、Reset、分页、排序；`新增`导航 `/production/batches/create`；View导航 `/production/batches/:id`；Edit导航 `/production/batches/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-BAT-C 生产批新增
Route: `/production/batches/create` | Permission: `production:batch:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-BAT-V 生产批详情
Route: `/production/batches/:id` | Permission: `production:batch:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/production/batches/:id/edit`，不得切换当前详情页为编辑态。

### UI-BAT-E 生产批修改
Route: `/production/batches/:id/edit` | Permission: `production:batch:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

### UI-DEV-Q 偏差查询列表
Route: `/quality/deviations`  | Requirement: QMS-DEV-001 | Permission: `qms:deviation:view`
查询条件：偏差号、批号、工序、严重度、状态、日期
结果列：偏差号、批号、工序、严重度、状态、责任人、日期
功能：Query、Reset、分页、排序；`新增`导航 `/quality/deviations/create`；View导航 `/quality/deviations/:id`；Edit导航 `/quality/deviations/:id/edit`。
禁止：在列表页Modal/Drawer/内嵌表单新增、查看或编辑。

### UI-DEV-C 偏差新增
Route: `/quality/deviations/create` | Permission: `qms:deviation:create`
结构：独立新增表单；Save Draft/Save（按领域）；Cancel返回Query Page并保留查询条件。
校验：即时格式校验 + 服务端最终业务校验；422保留表单输入并定位字段。

### UI-DEV-V 偏差详情
Route: `/quality/deviations/:id` | Permission: `qms:deviation:view`
结构：只读Header + Summary/Tabs + 关联对象 + Version/Audit；StateActionBar按allowedActions显示。
Edit按钮导航 `/quality/deviations/:id/edit`，不得切换当前详情页为编辑态。

### UI-DEV-E 偏差修改
Route: `/quality/deviations/:id/edit` | Permission: `qms:deviation:update`
结构：独立编辑表单；进入时加载versionNo；Save使用If-Match/versionNo；409打开Conflict提示并要求重新加载。
Approved/不可变对象不得进入普通Edit，必须按领域规则创建新版本。

## 5. 事务型/查询型核心页面

### UI-EXEC-W ExecutionUnit工作台
Type: WORK | Route: `/mes/execution/:id` | Requirement: PRD-004,MES-OP-001 | Permission: `mes:operation:view`
该页按对应07功能详细设计执行。若为QUERY，只提供条件+结果；若为WORK，专注单一业务事务，不承载主数据CRUD。

### UI-WGH-W 称量工作台
Type: WORK | Route: `/mes/execution/:id/weighing` | Requirement: MES-WGH-001 | Permission: `mes:weigh:create`
该页按对应07功能详细设计执行。若为QUERY，只提供条件+结果；若为WORK，专注单一业务事务，不承载主数据CRUD。

### UI-CHG-W 投料工作台
Type: WORK | Route: `/mes/execution/:id/charge` | Requirement: MES-CHG-001 | Permission: `mes:charge:create`
该页按对应07功能详细设计执行。若为QUERY，只提供条件+结果；若为WORK，专注单一业务事务，不承载主数据CRUD。

### UI-EBR-R eBR Renderer
Type: WORK | Route: `/mes/execution/:id/forms/:formId` | Requirement: EBR-004,EBR-005,EBR-006,EBR-007 | Permission: `ebr:form:view`
该页按对应07功能详细设计执行。若为QUERY，只提供条件+结果；若为WORK，专注单一业务事务，不承载主数据CRUD。

### UI-BAL-V 物料平衡详情
Type: VIEW | Route: `/production/batches/:id/balance` | Requirement: BAL-001,BAL-002,BAL-003 | Permission: `balance:view`
该页按对应07功能详细设计执行。若为QUERY，只提供条件+结果；若为WORK，专注单一业务事务，不承载主数据CRUD。

### UI-QA-V QA批审详情
Type: VIEW | Route: `/qa/batches/:id/review` | Requirement: REL-001 | Permission: `qa:batch-review`
该页按对应07功能详细设计执行。若为QUERY，只提供条件+结果；若为WORK，专注单一业务事务，不承载主数据CRUD。

### UI-REL-W QA Release工作页
Type: WORK | Route: `/qa/batches/:id/release` | Requirement: REL-001 | Permission: `qa:release`
该页按对应07功能详细设计执行。若为QUERY，只提供条件+结果；若为WORK，专注单一业务事务，不承载主数据CRUD。

### UI-TRC-Q 追溯查询
Type: QUERY | Route: `/trace` | Requirement: TRC-001 | Permission: `trace:view`
该页按对应07功能详细设计执行。若为QUERY，只提供条件+结果；若为WORK，专注单一业务事务，不承载主数据CRUD。

### UI-AUD-Q 审计查询
Type: QUERY | Route: `/audit` | Requirement: AUD-001 | Permission: `audit:view`
该页按对应07功能详细设计执行。若为QUERY，只提供条件+结果；若为WORK，专注单一业务事务，不承载主数据CRUD。

## 6. 重点页面细化

### 6.1 物料查询 / 新增 / 详情 / 修改
`UI-MAT-Q` 顶部条件：编码、名称、类别、规格、状态、关键物料；按钮Query/Reset。表格不展示编辑表单。
`UI-MAT-C` 使用Tabs：基本信息、质量属性、仓储属性、生产/称量属性、合格供应商。创建初始Draft。
`UI-MAT-V` 完全只读，显示当前版本、状态、关联供应商、版本历史、Audit。
`UI-MAT-E` 仅Draft允许普通编辑；Approved点击“创建新版本”导航到独立版本创建页面。

### 6.2 工艺包
列表只查询工艺包/版本。创建工艺包为独立页面。详情页只读展示BOM、Route、eBR、审批历史。设计/编辑进入独立Designer路由，不在详情内切编辑模式。

### 6.3 eBR
模板列表只查询。Create创建模板Draft。View只读版本/差异/审批。Designer是独立编辑工作区 `/ebr/templates/:id/designer`。Renderer属于运行事务页面，与Designer完全分离。

### 6.4 生产批
批次列表 `/production/batches` 只查询。批次详情 `/production/batches/:id` 独立页。批次状态动作在详情页StateActionBar；不在列表行内执行复杂Release/Complete/Submit QA。需要确认的状态动作可打开确认Dialog。

### 6.5 偏差
偏差列表只查询。Create独立页面。View展示生命周期、调查、CAPA、审计。Edit只允许当前状态允许修改的Draft/调查内容；决定/关闭使用受控状态动作而非普通Edit。

## 7. Codex强制约束
前端路由必须保持Query/Create/View/Edit分离。不得为了“开发方便”将Create/Edit塞回列表Modal/Drawer。列表页操作列必须使用router navigation进入详情/编辑。Query条件应可恢复。复杂状态动作必须在详情/工作页执行并使用服务端allowedActions。
# V1.0.12 Route Amendment — DCP-MES-001-R2-001
## UI-AUD-Q — GMP Audit Trail

- Route: `/audit`
- Requirement: AUD-001
- Permission: `audit:view`
- Source: `GET /audit-events`
- Shows the paged AuditEvent table, supported filters, loading/empty/error/403 states, and today's count by querying the UTC-day range and reading `total`.

## UI-INT-OPS — Integration Inbox / Outbox Operations

- Route: `/integration/operations`
- Requirement: INT-001
- Permission: `integration:view`; retry additionally requires `integration:retry`
- Sources: `GET /integration/messages`, `POST /integration/messages/{messageRef}/retry`
- Shows inbox/outbox counts and table from the concrete API and a reason/version/idempotency-aware manual retry action.

`/platform/operations` and `PLAT-001` are removed and must not be aliases. Prototype OpenAPI-count, audit trend, compliance-health, service-health, PDF-archive and other metrics without a frozen API are excluded from production implementation. No production metric may use sample constants. MES-001 builds protected components against an auth-context contract; MES-002 supplies login, navigation visibility and login-to-page E2E.

## DCP-MES-002-R2-001 — Incoming Material UI

- `/master/materials/:id`: quality attributes include **是否入库必验**, displayed and edited directly as a basic Material attribute.
- `/wms/receipts`, `/wms/receipts/:id`: receipt list, creation, checks, confirmation and generated lots.
- `/wms/material-lots/:id`: 360° tabs for 概览、收货、请验、取样、样品、检验、检验报告、放行、库存流水、生产使用、审计.
- `/quality/inspection-requests`, `/quality/inspection-requests/:id`.
- `/quality/sampling-tasks`, `/quality/sampling-tasks/:id`.
- `/quality/samples`, `/quality/samples/:id`.
- `/quality/inspection-tasks`, `/quality/inspection-tasks/:id`.
- `/quality/inspection-reports`, `/quality/inspection-reports/:id`.
- `/quality/workbench`: role-filtered quality work queue.
- `/qa/material-lots/:id/review`, `/qa/material-lots/:id/release`.
- Finished-product QA routes remain `/qa/batches/:id/review` and `/qa/batches/:id/release`.

Every page shows `qualityStatus`, `inventoryStatus`, and `recordStatus` as separate labeled fields. Inspection-exempt lots display the system-rule basis and omitted stages as “不适用（免验）”, never as completed sampling/testing. Actions are hidden/disabled by permission and server-provided allowed actions; the server remains authoritative.




## DCP-MES-003-R2-001 approved delta

MES-003 added query/create/view/edit pages follow the route matrix and DCP-MES-003-R2-001. Equipment location is md_equipment.location; no inferred WMS join.


## DCP-MES-004-005-R2-001 approved contract completion

Material creation 201, complete typed DTOs, explicit version target/root optimistic token, Material root DRAFT/APPROVED/INACTIVE and version DRAFT/SUBMITTED/APPROVED, Supplier UNAPPROVED/APPROVED/INACTIVE and named commands, historical relationship revocation, and LG-004 delayed conversion-material FK follow 00_DESIGN_CHANGE_PROPOSAL_DCP-MES-004-005-R2-001_APPROVED.md. No existing table/column/path/permission/task dependency is added or removed. All previous unrelated contracts remain applicable.


## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.

## DCP-UI-LIST-EDIT-001 current presentation contract

white page header/cards on #f0f2f5 background, #2c5cdc primary buttons and 3px section title bar, 4px corner radius, compact 13px tables, 18px page titles, three-column desktop query controls, and two-column edit forms with 130px right-aligned labels. Query label/control remain inline on all viewport sizes; mobile edit forms have one column with label/control remaining side by side. Existing navigation chrome retained. Existing technical contract-strip metadata removed from master list presentation; operation controls and contract behavior retained.


Validation: affected module typecheck/build plus existing desktop/mobile query, edit/save, permission and conflict-preservation flows. No backend/data regression required for this CSS/template-only change.

## DCP-MES-006-R2-001 current contract

The approved 00_DESIGN_CHANGE_DCP-MES-006-R2-001_APPROVED.md is normative for MES-006 and supersedes prior contradictory process/product/eBR scope prose. Product is owned here; material has no business version; eBR is independently owned by MES-007. Process signature binds immutable business version and definition content. New physical V011 only. Test requirements include TC-PROC-004.


## DCP-MATERIAL-NAMES-UI-001 current correction
See `00_DESIGN_CHANGE_DCP-MATERIAL-NAMES-UI-001_APPROVED.md`. The three alternate material-name fields are retired from current API/UI/search/consumer snapshots; legacy physical values and audits are preserved. Labels and controls remain side by side on all viewport sizes. This supersedes inherited inconsistent descriptions within that boundary.


## DCP-MES-007-008-SEQUENCING-001 authoritative delta

Read `00_DESIGN_CHANGE_DCP-MES-007-008-SEQUENCING-001_APPROVED.md` and the stage contract appendices. Explicit human approval preserves the full MES-007/008 functionality and required tests, while separating current implementation from later real integration. LG-007A definition/Designer/DSL/published contracts is the current MES-007 gate; LG-007B remains MES-009 after LG-009A, with its operation_execution_id FK installed and validated by LG-010. LG-008 reservation/issue main_batch_id FKs are installed and validated by LG-009A. Before these physical producers exist, dependent writes fail closed; no placeholder batch/operation rows, bypassed qualification, or mutable regulated history. MES-008A owns actual MaterialEligibilityService/release evidence; no quantity-only eligibility.

The existing independent receipt edit UI is retained. `PUT /wms/receipts/{id}` uses `wms:receipt:update`, edits only DRAFT authored receipt fields, requires optimistic version, reason, idempotency and same-transaction audit, and returns 200; Confirmed receipt facts (recordStatus APPROVED) cannot be edited. Formal contracts are the concrete OpenAPI and stage appendices. All current labels/control pairs stay horizontal on desktop/mobile.

Current targeted tests prove only current-stage capabilities; deferred runtime/eligibility/batch tests remain required. MES-007 and MES-008 remain IN PROGRESS until every original applicable gate passes against real producer contracts. The approved stage does not authorize implementation of MES-008A/009/010 or marking tasks ACCEPTED.


## DCP-MES-008A-CONTRACT-001 approved bounded delta

Read `00_DESIGN_CHANGE_DCP-MES-008A-CONTRACT-001_APPROVED.md`. Its six independent create/execute routes, existing workbench mapping, nullable incoming MainBatch references, MES-009 delayed FK ownership and canonical finished_lot_id target supersede conflicting inherited wording only within this boundary. Existing fields, API/permissions, state machines and GxP requirements remain unchanged. Unresolved incoming implementation-map gaps are not resolved by this release.


## Approved incoming full-chain completion in v1.0.12

Read `00_DESIGN_CHANGE_DCP-INCOMING-QUALITY-GAPS-001_APPROVED.md` and the four `00_INCOMING_*_CONTRACT_V1.0.12.md` appendices. Their explicit columns, state guards, API schemas, permissions, signing envelopes, UI fields, tests and dependency ownership supersede contradictory inherited text within the approved scope only. Former DG-01..08 and BC-01/02 now have implementation contracts; delivery is still subject to actual code and required validation. No full workflow PASS follows from publication.


## Authorized v1.0.12 incoming completion supplement

Human approval covers the bounded contract completion and full incoming acceptance tasks. For eBR runtime, issue returns, weighing verification and actual trace identities, the following precise supplements supersede generic or conflicting clauses in this chapter; unaffected contracts remain unchanged.

- [00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.12.md](00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.12.md)
- [00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.12.md](00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.12.md)
- [00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.12.md](00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.12.md)
