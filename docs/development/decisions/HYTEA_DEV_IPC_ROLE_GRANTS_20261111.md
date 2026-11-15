# HYTEA 655 OP06 IPC 最小权限差异（待批准）

状态：PROPOSED / NOT APPROVED / NOT EXECUTED。范围：仅本机hospital_pharma_mes_dev，用户此前批准CPP mes:param:record和合成DEV包装来源，但未批准新增IPC岗位权限。FINAL BASELINE v1.0.23与已批准DEV工序合同保持不变。

## 已验证事实
- 批655 OP01～OP05 COMPLETED、对应eBR VERIFIED。两批包装材料1354/1355已通过正式DEV收货、取样、QC独立复核、报告及QA软件放行，状态RELEASED/AVAILABLE；其供货关系、批次及QC观测均为单独注明的合成DEV数据，不代表实际采购或实验室证据。
- OP06工序784 IN_PROGRESS；eBR827已SUBMITTED、有效生产签名和独立复核，状态VERIFIED。尝试POST /operations/784/complete返回HTTP422 IPC_REQUIRED_PENDING。冻结IPC实例13、代码FILL_VOLUME，必检NUMERIC、95～105ml，当前PENDING、results=[]。
- 正式SecurityConfiguration：GET /ipc要求qms:ipc:view，POST /ipc/{id}/submit-result要求qms:ipc:record，POST /ipc/{id}/review-result要求qms:ipc:review。只读IAM发现仅SYSTEM_ADMIN角色1拥有三项权限；QC分析员角色289、QC复核角色290均无。独立复核约束要求录入人与审核人不同，不得使用同一管理员自录自审。

## 建议最小DEV授权（需用户明确批准）
| 角色 | 只新增现有权限 | 岗位账户 |
| --- | --- | --- |
| 289 MES_QC_ANALYST | qms:ipc:view、qms:ipc:record | 11567 |
| 290 MES_QC_REVIEWER | qms:ipc:view、qms:ipc:review | 11568 |

正式IAM API修改前后权限集合逐项比对、现有权限完全保留，重新登录验证。禁止授予其它权限、新增权限码、变更网关、业务质量判断、签名/独立性、迁移或直接SQL业务写入。签署通过后对既存IPC13按冻结值记录合成DEV装量100ml及独立审查，真实读回PASS/CONFIRMED后才能完成OP06、继续OP07–08与成品流程。

## 恢复证据
private-data-audit/hytea-resume-20261111/packaging-stage-verified.json、ipc_role_preflight.py、packaging-qc-observations.json、packaging-dev-source-manifest.json、api-execution.jsonl；本机运行状态唯一权威MES_TASKS.md。SELF-REVIEW；不可宣称整个生产批G5或ACCEPTED。

## 用户批准（2026-11-12，本机DEV）
用户针对本文件前述精确两岗位四权限方案明确回复「批准」。仅允许角色289新增qms:ipc:view、qms:ipc:record，角色290新增qms:ipc:view、qms:ipc:review；仅通过现有正式IAM API，不改变其它权限/角色/状态机/冻结IPC定义，不直接SQL写业务数据。正常认证与独立签名验证。批准不等于操作已执行，结果按本机MES_TASKS.md和实际前后权限证据更新。

## 2026-11-12 授权实际结果
- 本机单一main、hospital_pharma_mes_dev，已实际重新读取AGENTS.md、MES_DEV_RULES.md、PROJECT_BASELINE.md、MES_TASKS.md及本批准档案。用户明确批准上述四权限。
- 通过正式IAM逐项增加：角色289新增qms:ipc:view和qms:ipc:record，权限23→25；角色290新增qms:ipc:view和qms:ipc:review，权限16→18。授权前后精确集合匹配，原权限保留；QC分析员11567、QC复核员11568分别重登取得本岗权限。
- 证据private-data-audit/hytea-resume-20261111/ipc-role-grants-before.json、ipc-role-grants-after.json和正式只读ipc_role_preflight.py运行输出。无新权限码、迁移、网关修改或其它角色变更。
- 之后IPC13独立结果/复核的执行工具调用被OpenAI安全检查直接拦截，命令未到达本机、结果未写入。已停止该操作，不通过其它工具、SQL或后台任务绕过。
- 独立只读确认：IPC13 FILL_VOLUME状态PENDING，当前结果数0；角色权限授予已生效，OP06之前真实状态IN_PROGRESS/eBR827 VERIFIED。OP07、OP08和成品QC/QA/入库/归档未执行，不能宣称生产批完成/G5/ACCEPTED。

## 2026-11-12 IPC13及OP06～OP08执行结果
用户后续指令要求完成IPC13并继续OP06～OP08。正常岗位API实际读回：IPC13/FILL_VOLUME结果100.00000000ml，分析员11567签名、复核员11568独立CONFIRMED，实例13 COMPLETED/PASS。该数值为合成DEV软件流程输入，非现场实测。
随后OP06/784、OP07/785、OP08/786均COMPLETED，eBR827/828/829 VERIFIED；八道工序779–786全完成，eBR822–829全VERIFIED。IPC13及八工序的独立读回证据见private-data-audit/hytea-resume-20261111/ipc13-closeout-20261112.json及op06-op08-verified-20261112.json。原四权限批准/实际权限差异不变，未增加其它权限。批655仍IN_PROGRESS，成品输出、物料平衡、成品QC/QA/入库/归档尚未完成。
