# MES 全功能 PC 页面设计清单

数据库菜单 + 真实前端路由核对；设计图尚未生成的页面不得标记通过。

菜单数：44；路由数：144；业务上下文：14

| 一级菜单 | 功能菜单 | 主入口 | 分页面设计图（待生成） |
|---|---|---|---|
| 首页 | 首页 | / | 首页-查询.png |
| 基础管理 | 物料主数据 | /master/materials | 物料主数据-查询.png、物料主数据-新增.png、物料主数据-查看.png、物料主数据-编辑.png |
| WMS管理 | 原辅料收货记录 | /wms/receipts | 原辅料收货记录-查询.png、原辅料收货记录-新增.png、原辅料收货记录-查看.png、原辅料收货记录-编辑.png |
| 质量管理 | 请验单 | /quality/inspection-requests | 请验单-查询.png、请验单-新增.png、请验单-查看.png |
| 生产管理 | 生产订单 | /production/orders | 生产订单-查询.png、生产订单-新增.png、生产订单-查看.png、生产订单-编辑.png |
| 成品管理 | 成品入库申请 | /finished/inbound | 成品入库申请-查询.png、成品入库申请-新增.png、成品入库申请-查看.png |
| 系统管理 | 打印模板 | /admin/print-templates | 打印模板-查询.png、打印模板-新增.png、打印模板-编辑.png、打印模板-编辑-print-template-editor.png |
| 系统管理 | 用户管理 | /admin/users | 用户管理-查询.png、用户管理-新增.png、用户管理-查看.png、用户管理-编辑.png |
| 基础管理 | 供应商 | /master/suppliers | 供应商-查询.png、供应商-新增.png、供应商-查看.png、供应商-编辑.png |
| WMS管理 | 库存管理 | /wms/inventory | 库存管理-查询.png |
| 质量管理 | 取样记录 | /quality/sampling-tasks | 取样记录-查询.png、取样记录-新增.png、取样记录-查看.png、取样记录-执行.png |
| 生产管理 | 生产批 | /production/batches | 生产批-查看.png、生产批-查询.png、生产批-新增.png、生产批-查看-production-batches-view.png、生产批-编辑.png、生产批-查看-production-batch-book.png |
| 成品管理 | 成品生产入库（待检） | /finished/receiving | 成品生产入库（待检）-查询.png |
| 系统管理 | 角色与权限 | /admin/roles | 角色与权限-查询.png、角色与权限-新增.png、角色与权限-查看.png、角色与权限-编辑.png |
| 基础管理 | 组织 | /master/organizations | 组织-查询.png、组织-新增.png、组织-查看.png、组织-编辑.png |
| WMS管理 | 领料申请 | /wms/requests | 领料申请-查询.png、领料申请-新增.png、领料申请-查看.png、领料申请-编辑.png |
| 质量管理 | 样品 | /quality/samples | 样品-查询.png、样品-查看.png |
| 生产管理 | 生产执行 | /production/execution | 生产执行-查询.png |
| 成品管理 | 成品请验 | /finished/requests | 成品请验-查询.png、成品请验-新增.png、成品请验-查看.png |
| 基础管理 | 单位 | /master/units | 单位-查询.png、单位-新增.png、单位-查看.png、单位-编辑.png |
| WMS管理 | 出库管理 | /wms/issues | 出库管理-查询.png、出库管理-新增.png、出库管理-查看.png、出库管理-编辑.png |
| 质量管理 | 检验记录 | /quality/inspection-tasks | 检验记录-查询.png、检验记录-新增.png、检验记录-查看.png、检验记录-执行.png |
| 生产管理 | 批记录册模板 | /ebr/book-templates | 批记录册模板-查询.png |
| 成品管理 | 成品取样 | /finished/sampling | 成品取样-查询.png、成品取样-查看.png |
| 系统管理 | 菜单管理 | /admin/menus | 菜单管理-查询.png |
| 生产管理 | eBR模板 | /ebr/templates | eBR模板-查询.png、eBR模板-新增.png、eBR模板-查看.png、eBR模板-设计.png |
| 基础管理 | 单位换算 | /master/unit-conversions | 单位换算-查询.png、单位换算-新增.png、单位换算-查看.png、单位换算-编辑.png |
| WMS管理 | 退料管理 | /wms/returns | 退料管理-查询.png、退料管理-新增.png |
| 质量管理 | 检验报告 | /quality/inspection-reports | 检验报告-查询.png、检验报告-新增.png、检验报告-查看.png |
| 成品管理 | 成品检验 | /finished/tests | 成品检验-查询.png、成品检验-查看.png |
| 系统管理 | 完整追溯 | /trace | 完整追溯-查询.png |
| 基础管理 | 设备 | /master/equipment | 设备-查询.png、设备-新增.png、设备-查看.png、设备-编辑.png |
| 质量管理 | QC质量标准 | /quality/specifications | QC质量标准-查询.png、QC质量标准-新增.png、QC质量标准-查看.png |
| 生产管理 | 物料平衡 | /production/balances | 物料平衡-查询.png |
| 成品管理 | QA批审核 | /finished/qa-reviews | QA批审核-查询.png |
| 基础管理 | 人员资格 | /master/qualifications | 人员资格-查询.png、人员资格-新增.png、人员资格-查看.png、人员资格-编辑.png |
| 质量管理 | 生产质量计划 | /quality/production-plans | 生产质量计划-查询.png、生产质量计划-新增.png、生产质量计划-查看.png、生产质量计划-编辑.png |
| 成品管理 | 成品放行 | /finished/releases | 成品放行-查询.png |
| 基础管理 | 产品管理 | /process/products | 产品管理-查询.png、产品管理-新增.png、产品管理-查看.png、产品管理-编辑.png |
| 质量管理 | 生产检验 | /quality/production-tests | 生产检验-查询.png、生产检验-新增.png、生产检验-查看.png |
| 成品管理 | 成品库存 | /finished/inventory | 成品库存-查询.png |
| 基础管理 | 处方与工艺管理 | /process/packages | 处方与工艺管理-查询.png、处方与工艺管理-新增.png、处方与工艺管理-查看.png、处方与工艺管理-编辑.png |
| 质量管理 | 质量调查 | /deviations | 质量调查-查询.png、质量调查-新增.png、质量调查-查看.png |
| 成品管理 | 成品发货出库 | /finished/shipments | 成品发货出库-查询.png、成品发货出库-新增.png、成品发货出库-查看.png |

## 系统入口
- 系统入口/登录/登录.png — /login
- 系统入口/修改密码/修改密码.png — /change-password

## 业务上下文
- 业务上下文页/QA 批审详情/QA 批审详情-审核.png — /qa/batches/:id/review
- 业务上下文页/QA 批放行审核/QA 批放行审核-放行.png — /qa/batches/:id/release
- 业务上下文页/物料放行/物料放行-审核.png — /qa/material-lots/:id/review
- 业务上下文页/物料放行/物料放行-放行.png — /qa/material-lots/:id/release
- 业务上下文页/电子批记录/电子批记录-查看.png — /mes/execution/:id/forms/:formId
- 业务上下文页/生产执行/生产执行-查看.png — /mes/execution/:id
- 业务上下文页/生产执行/生产执行-查看.png — /mes/execution/:id/weighing
- 业务上下文页/生产执行/生产执行-查看.png — /mes/execution/:id/charge
- 业务上下文页/QC质量标准版本/QC质量标准版本-编辑.png — /quality/specification-versions/:id/edit
- 业务上下文页/仓储管理/仓储管理-查看.png — /wms/material-lots/:id
- 业务上下文页/成品检验报告/成品检验报告-功能.png — /finished/reports
- 业务上下文页/成品检验报告/成品检验报告-查看.png — /finished/reports/:id
- 业务上下文页/GMP Audit Trail/GMP Audit Trail-功能.png — /audit
- 业务上下文页/Integration Inbox - Outbox Operations/Integration Inbox - Outbox Operations-功能.png — /integration/operations