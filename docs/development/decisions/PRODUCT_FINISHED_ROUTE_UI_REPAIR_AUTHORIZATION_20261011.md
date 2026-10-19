# 既有权限路由与统一指令详情定向修复 — 2026-10-11

APPROVED / FROZEN。父任务设计负责人明确批准最多2轮定向修复；第一轮修复测试，第二轮仅处理相同范围失败；新范围立即报告。08:32 UTC开始。UI修复/双视口复核普通任务预算10分钟；后端路由及已批准部署/复杂业务八项按原维护执行范围继续，不重置历史修复计数。

DESIGN LOCK：SecurityConfiguration仅新增POST /api/v1/quality/production-plans=qms:plan:create、PUT /api/v1/quality/production-plans/{id}=qms:plan:update、POST /api/v1/quality/production-plans/{id}/approve=qms:plan:approve，使用既有框架精确单ID语义并邻接计划GET；默认denyAll、Service权限/组织/独立审核/签名检查不变。无角色授权、permitAll、宽泛命名空间规则。

库存准入只读证据：精确GET /api/v1/finished-inventory，FinishedChainController.java:12；FinishedInventoryQuery.java:22 context(wms:inventory:view)、:24组织schema2过滤；当前已批准product-finished OpenAPI:471同permission，v1.0.23 WMS权限矩阵:7为既有UNCHANGED wms:inventory:view。因此本接口完全匹配当前受控成品独立库存合同及冻结原权限，允许只补这一条GET匹配，不自动补其他chain路由。

UI沿用现有T1生产列表/T4详情、UI V2，修复390px状态label/control同排整对换行；详情收敛横向溢出。schema2正常详情不显示内部兼容工单identity、不独立读取内部订单、不显示重复订单生命周期/重复批记录入口；后台内部Order关联和schema1历史显示读取保留。无业务字段/API/状态/权限/表/模板重设计。

文件白名单：SecurityConfiguration.java、ProductionListView.vue、BatchOverview.vue及对应新增定向安全/前端组件测试；设计/验收记录与MES_TASKS。编译/前端typecheck及针对性测试，实际5173两视口同尺寸截图/原几何阈值；部署后真实有权限到达业务、无权限403、未登录401、跨组织拒绝（不得伪造认证或改用户组织）；实际数据不可用须明确NOT RUN。保留31552部署前证据、316/314/230业务链，禁止重建/直接改状态/伪造签名。通过按原部署批准重打包重启唯一8080并继续八项，无新缺陷不因阶段汇报停止。
