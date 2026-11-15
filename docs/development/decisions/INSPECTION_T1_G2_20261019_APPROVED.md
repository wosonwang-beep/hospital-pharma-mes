# 检验记录列表 G2-20261019 设计锁与有界正式接入合同

状态：APPROVED / FROZEN。日期2026-10-19。用户原文：“确认 G2-20261019 这个 Vue 交互版本，继续补齐并冻结接口合同，接入正式 MES，把检验记录列表开发、验证到收尾，不要再停在原型。”本记录落实用户明确授权的列表只读合同补齐、冻结和正式集成，不授权此前跨岗位DCP其他改变。

## PRD
检验员先按样品名称与物料批号认样，用样品编号核对实物；用任务状态、检验人和类型找任务，选择正确记录进入既有填写或独立复核。报告号与任务号不可混淆。权威v1.0.23及已批准补充不改历史，Global UI V2/T1保持。

## BUSINESS_WORKFLOW
质量管理→检验记录→查询/重置/翻页→查看只读详情，或具备服务端能力时填写/复核→既有正式受控详情及签名→返回保留列表条件和页码。列表不发业务写命令；不创建新记录或自动QA放行。异常保留查询、清空失效行并可重试；后发查询优先。

## SCREEN_SPEC
精确批准源码位于 `D:/codex/_project/gmp/mes-demo/src/features/quality`：InspectionTaskList.vue SHA256 `9CBDB85F8BFB0B1D6AC77BCFCE8752AC8CDEEB78797D8FB9CC883FF4FAF0576D`；inspectionListModel.ts `71AA26DB274724190B7A6CCB745A8CF1AFAACCC659C407D75495A28DAA068D63`；InspectionTaskPrototype.vue `52CA084982F7BBDA854A3EFD5B6D82C50FF15B8BA77C24CE4F40C30E08AE88C8`。原型入口src/PrototypeRoot.vue hash `7417EDEF19E64B601BB4720367F477BC8F9587C479F093F647162E9DBB957C65`。访问5174/#/quality/inspection-tasks，依赖mes-demo/package.json，npm run dev / npm run build，Vue3/TS/Ant Design Vue。

正式原样复用列表和控制器，保留样品名称150、批号104、样品编号142、类型82、状态88、人员62、日期112、判定80、报告号146、操作100宽度及前两列突出。四个查询条件、默认全部、重置→查询同行、标签控件同行、空/错/加载状态、分页与返回行为不重画。允许仅集成真实外壳、路由、API、权限、来源选项、已批准Token和错误信息；不迁入原型工具或内存构造数据。1440/1280主验，900/375健壮性；密集表格允许独立横滚且提示保留。

## DESIGN_DECISIONS
1. 复用批准Vue源，不重写UI。原型壳替换为正式现有应用壳，不复制侧栏。
2. GET原任务列表的默认行为保持兼容，新增 `projection=t1` 选择列表投影，不新建菜单/权限/路由。
3. 所有关联报告号完整展示（按报告ID排序、去重），不静默选最新或第一条；无报告为null→未生成。草稿号仍为真实报告号而非批准证明。报告审批独立。
4. 类型/任务状态是正式固定schema枚举，不是臆造可配置字典。复用incoming-contract及display映射；检验人来自同组织任务指派人员的只读选项，不要求额外users管理权限。
5. 这是后端+跨表只读+正式前端+真实服务集成，不是普通样式任务。预先限定本轮实施/验证预算60分钟，从首次实施起计；自动修复累计不超20轮，重复同根因修后仍失败立即停止；到限如实记录未完成，不放宽门禁。

## API_CONTRACT
GET `/api/v1/quality/inspection-tasks?projection=t1&page=0&size=20&keyword=&status=&assignedTo=&requestType=`，沿用 `qms:test:view`。page零基非负，size1..100；keyword最长200，匹配样品业务名称、物料批号、样品编号、任务号及所有关联报告号，字面量包含匹配，不将%/_当通配符；状态/人员/类型精确匹配。既有materialLotId/inspectionRequestId过滤继续可用。总数与分页使用同一筛选，ID倒序稳定排序。组织ID只能取认证上下文。

响应沿用ApiResponse<Page>：items,total,page,size；附inspectors[{value,label}]为同组织已指派人员选项，仅ID和显示名称。item含id、sampleName|null、lotNo|null、sampleNo|null、inspectionTaskNo、requestType、status、assignedTo|null、assignedName|null、requestedDate|null、sampledAt|null、overallResult=PASS/FAIL/null、reportNo|null（多号以顿号连接）、reportId|null（仅唯一报告时）、reportIds[]、allowedActions[]。所有ID为字符串。失败401沿用登录恢复，403明确无权限，400非法参数；其他错误保留条件、清除旧行、重试。前端不全量拉取假筛选、不逐行跨API补字段。

查看→现有GET详情并路由`/quality/inspection-tasks/:id`；填写→现有`/:id/execute`；复核→现有`/:id`及既有review操作入口。返回query传递keyword/status/assignedTo/requestType/page/size。列表显示动作仍需auth.can相应现有权限。

## DATA_MODEL
零新增表列、零迁移、零业务数据修改。sampleName是明确业务投影：task.sample_id→qms_sample，来料样品material_lot_id→md_material_lot.material_snapshot_json.materialName；只有该受控来源可映射，缺失为null，不假称qms_sample存在sample_name，也不拼接“检验样”。lotNo取同一样品关联批次lot_no，sampleNo取sample_no。requestType/requestedDate取task.inspection_request_id关联父请验。sampledAt取sample.sampled_at（UTC原值）。assignedName通过task.assigned_to关联sys_user.display_name；人员选项仅该组织任务指派关系泄露最小字段。

reportIds/No必须report_item.inspection_item_id→inspection_item.inspection_task_id→当前任务，且report和task的inspection_request_id相同，所有org关联相同；不能只按批号关联。综合原始判定复用任务现有taskResults最新结果逻辑：任一原始FAIL保留FAIL，完整全部PASS才PASS，缺失null；不以QA/OOS有效处置覆盖原始FAIL。投影仅汇总不产生质量决定。

## STATE_MACHINE
不新增/改变状态。固定任务CREATED/ASSIGNED/IN_PROGRESS/PENDING_REVIEW/QC_PASSED/QC_FAILED保持；结果与状态分别表达。view由qms:test:view允许。execute仅具备qms:test:execute且现有allowedActions start/submit-review，或IN_PROGRESS同指派人且原有项目能力可执行；review需原有allowedActions.review及qms:test:review。必须复用正式服务资格、指派、样品接收与独立性校验，不用前端状态模拟权限。

## PERMISSIONS
沿用qms:test:view/execute/review，不新权限、不改角色。后端先校验view，再依现有服务allowedActions及资格收窄动作；不返回密码/完整人员资料。默认任务API、详情、签名、审计、原始结果、WMS/QA冻结规则完全保持。只读查询无业务审计写入；原受控详情命令继续原审计签名事务。

## ACCEPTANCE_RTM
NQ-11名称/批号置首且来源正确；NQ-12四筛选实际服务端生效、计数/分页一致、标签控件同行；NQ-13/14报告号与任务号区分、同任务项目关联、未生成语义；NQ-07/08仅验证正确原详情入口与返回，不重做签名工作流。必须新增真实DB回滚测试（名称/批号/报告搜索、分页/组合/组织隔离/多报告/无值、原API兼容、原始FAIL保留、权限），前端适配器及控制器测试，构建；真实5173+8080浏览器四视口、筛选/返回/空态/失败恢复、权限正反、全维UIUX清单校验。对照批准1440/1280截图，不冒称整条签名流程本次重新验收。

## WRITE_SCOPE / G3.5接收
允许：新建列表只读query/mapper、IncomingQualityService的有界list分支与能力包装；前端新列表适配器及复用组件/控制器、仅该列表路由、测试；本合同、API补充、RTM、审查/交接/状态文档。禁止更改既有命令实现/DB/迁移/权限/其他列表、用户数据、历史release或提交推送。

Codex已真实读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、Global UI V2/T1、正式IncomingQualityService/Store/ActionPolicy/Controller与sample/task/report/item模型、IncomingList/Detail/Reference与已批准Vue源；核对上述SHA256，接受复用边界。产品负责人SELF-REVIEW确认只读合同与用户目标一致，无新增质量/库存决策。本文件是用户明确授权补齐冻结的列表合同；人工G2批准与当前授权均是原文记录，G5仍必须真实验证和最终用户验收。
