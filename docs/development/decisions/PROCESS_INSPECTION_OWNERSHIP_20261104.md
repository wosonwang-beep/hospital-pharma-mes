# 生产工艺检验归属纠正记录

> 2026-11-04后续人类澄清：本文件下文保留为当时核查历史，其“不能解释成IPC类型”不再是当前决定。用户已明确“IPC只是工序的其中一种”并确认“是，统一新增，工序内选择类型”。以[最新批准及澄清](PROCESS_INSPECTION_OWNERSHIP_REPAIR_20261104_APPROVED.md)为准：保留工序内生产/IPC类型，统一新增入口，不另设独立强制过程；正式历史与既有类型合同保留。

本记录固化已明确的人类需求，不授权未知原型版本、续修批次或正式数据转换。

2026-11-04协调对话01a187f9-9fe2-7665-9647-43d1958260fc转交用户原话「都说一次，没有IPC这个过程」；原负责人完成只读核查后，本主线用户要求「继续任务」。最新约束是没有独立IPC过程，不能继续将其解释成“IPC只是可选工序类型”。此前关于没有手机端的范围决定继续有效。

## 已证实的冲突

- mes-demo/src/features/processes/ProcessPrototype.vue:33–34将IPC01中间产品检验放入路线，灌装依赖它；42行检验表单归属IPC01；99–101、188、196行提供新增、类型选择和“IPC是工艺路线中的工序”提示。这些是独立步骤模型，不等于附属检验配置。
- 同组件211行的检验项目与判定标准，通过157行保存为当前工序的ipcDefinitions；这属于真实工序配置。当前样例parameters初始为空，不能虚构已有检验项目或合格证据。
- 正式ProcessDetailView.vue:56及model.ts:8已有PRODUCTION/IPC类型；80行检验页签是逐工序配置。OperationEntity.operationType保存为proc_operation_def.operation_type，V071/V074已建立列和约束。类型不是检验结果状态，也没有新增独立IPC工序表。
- 冻结v1.0.23的00_FUNCTIONAL_CLOSURE_CONTRACT §4定义附属于operation_execution_id的检验实例、不可覆盖结果及复核，UI在原工艺/执行页内，不新增独立菜单/路由。IpcService.requireComplete按ipcDefinitions/required/有效结果/独立复核及签名判断；ExecutionService按真实前置关系推进，不以operationType驱动检验Gate。
- 本次定向核查未在工艺/执行/QMS及相关原型源码找到inspectionRequired字段；不凭空新增它代替已有required语义。

## 权威处理

原DCP-PROCESS-OPERATION-TYPE-001确有用户明确批准，不将历史批准否认成从未存在；其类型呈现与最新需求冲突，需要有界补充纠正。IPC_PROTOTYPE_TRACE_20261102.md中“是否有IPC工序”等解释是历史转述，不能覆盖最新原话。原发布、原DCP、迁移、截图和审查失败事实保留。

最新需求方向已明确，无需再次询问是否存在IPC过程。尚未授权或确定的是具体的样例关联调整、正式旧类型兼容/退役方式和历史数据处理；不能将需求纠正自动扩大为删除全部检验业务、记录、参数、表单或控制规则。

当前定义、签名/审计、检验复核、有效FAIL阻断、完成条件、前置依赖、冻结快照均保留。当前正式字段合同尚未被执行层更换；仅登记冲突并暂停沿用旧解释的新增开发。后续实施范围见[原型修正方案](PROCESS_INSPECTION_OWNERSHIP_REPAIR_20261104_PROPOSED.md)。

## 本次执行边界与证据

真实读取：AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md；business-product-manager及用户优先/保真参考；BASIC_CURRENT_DEFINITION_CONTRACT、PROCESS_OPERATION_TYPE_CONTRACT、原类型DCP/字段设计锁、v1.0.23检验闭环合同；上述直接源码和原型四份定向测试。基线仍v1.0.23加有界补充、UI V2/T1–T6。

本次仅新增本记录/修正提案、更新MES_TASKS和原Planning。无源码、测试断言、数据库、服务、浏览器、迁移、Git发布操作。测试及UI门禁未新运行，历史29/29不能证明新需求满足。累计58，无59；G2/G3.5/G5仍未通过。唯一运行状态索引为MES_TASKS.md。
