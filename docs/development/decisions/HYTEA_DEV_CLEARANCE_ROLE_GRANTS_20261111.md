# 喉咽茶DEV完整流程：两项清场角色授权与生产复核账号

## 当前授权：APPROVED — 2026-11-11

用户对本文件已列出的两项清场权限与生产复核账号方案明确回复“确认批准”。仅允许本机DEV角色287新增mes:clearance:record、角色290新增mes:clearance:review；正常IAM创建production.reviewer／生产复核岗位账号并绑定287、完成首次凭据设置、凭据仅保存在本机Windows Credential Manager。允许按新账号正常登录和重新认证执行独立生产eBR复核。不新增权限码、资质或虚构培训，不修改冻结独立性。下面“待确认/未执行”为批准前盘点事实，批准后的实际运行状态以MES_TASKS.md为准。

来源：本机正式API与只读权限关联查询，2026-11-11。批655已RELEASED、计划666已APPROVED、执行单元628及8工序已建立；8工序冻结clearanceRequired=true。用户已批准完整软件测试及既有岗位自动认证，但未明确授予修改IAM权限的范围。

事实：mes:clearance:record和mes:clearance:review两个权限码已存在且启用，仅admin拥有。生产账号11572没有记录权限，QC复核账号11568没有复核权限；同一admin不能同时记录并独立复核。任务DCP要求缺少岗位权限时停止受影响步骤，不擅自扩权。

最小拟授权仅本机hospital_pharma_mes_dev：

| 现有角色 | 当前ID/版本 | 仅新增既有权限 | 对应账号 |
| --- | --- | --- | --- |
| MES_PRODUCTION／生产岗位 | 287 / 3 | mes:clearance:record | 11572 |
| MES_QC_REVIEWER／QC复核岗位 | 290 / 3 | mes:clearance:review | 11568 |

实施时采用现有POST /api/v1/admin/roles/{roleId}/permissions/{permissionCode}，逐角色先重读身份与原权限，新增后验证原权限全保留、仅差上述一项；重新登录岗位核对会话权限。保留记录人与复核人不同、两次签名、版本、资格及业务门禁。不改其他角色、权限码、菜单、资质、业务字段、数据库结构或生产环境，不通过SQL写IAM。

角色原始快照：private-data-audit/hytea-resume-20261111/clearance-role-before.json。当前仅形成待确认方案，尚未调用授权API。批准及实际结果另追加记录，运行状态只写MES_TASKS.md。

## 同批完整预检：生产岗位独立eBR复核账号缺失

8份实际render-model（form822–829）均冻结requiredRoleId=287、independentUserRequired=true，并要求VERIFY签名；现有角色287只有11572。不能修改已冻结模板/快照，不能让同一人自审，不能用管理员代录来制造表面独立。

额外最小可审方案：仅本机DEV，经正常IAM新建loginName=`production.reviewer`、displayName=`生产复核岗位`的明确职责账号，绑定已有生产角色287，用于本次及后续DEV生产记录独立复核，不虚构真实员工培训或资质证据。通过现有用户创建、角色绑定和首次凭据设置流程完成；凭据只在内存及本机Windows Credential Manager受保护存储，绝不输出或写入脚本/日志。其实际权限继承既有生产角色，不新增权限码，不授系统管理或QA角色。执行前记录原角色权限，执行后以新会话验证角色ID与复核资格；若现有合同要求额外资质但无合法来源，停止并报告，不自动添加资质。

本节是另一个待用户决定的账号/访问授予范围，不包含在此前仅“两项清场权限”的问题内；除非明确批准，不创建账号或设置凭据。用户若仅批准两项清场授权，仍不得创建该账号或声称完整eBR已能验收。

## 执行后发现的网关权限遗漏 — 待补充批准

批准的两项清场权限及生产复核账号已通过正式IAM执行；新账号19289已绑定287并正常认证。OP01已完成清场记录/独立复核、4项投料、eBR822提交及生产签名。新账号对POST /forms/822/reviews返回403 FORBIDDEN，requestId e5cdf45d-9130-4c83-8fd3-ac87a42d771c。

根因：预检只核对了EbrRuntimeService的ebr:sign和模板requiredRoleId，遗漏SecurityConfiguration.java的网关约束：POST /api/v1/forms/{id}/reviews需要ebr:review:verify或ebr:review:approve。角色287有ebr:sign但没有ebr:review:verify。不是签名、角色ID或独立性错误；不能修改网关、冻结规则或管理员代审。

最小补充方案仅本机DEV：角色287／MES_PRODUCTION新增既有权限`ebr:review:verify`，使两名生产岗位成员可按现有独立性规则互相核对，其他权限不变；不新增权限码或角色、不授ebr:review:approve/QA/系统管理权限。采用相同正常IAM授权API，前后差异必须恰好一项，重新登录后继续同一未完成复核，不重复创建、投料、签名或清场。该第三项权限未包含于前次明确两项清场授权，当前等待用户补充批准，尚未执行。

## 2026-11-11 第三项 eBR 独立审核权限批准
用户在会话明确回复「授权批准」。范围仅本机DEV角色287新增现有ebr:review:verify，保留既有权限/独立性，不新增其它权限，不修改网关、模板或数据库。执行和复核证据见private-data-audit/hytea-resume-20261111/ebr-review-role-before.json和ebr-review-role-after.json，业务状态见MES_TASKS.md。

## 2026-11-11 第三项授权执行验证
获批准后仅使用正式IAM接口为角色287增加现有ebr:review:verify，正常GET读回37项、原36项全保留；角色290无变化，新生产复核账户19289重新登录持有新权限。未输出凭据，权限前后JSON见private-data-audit/hytea-resume-20261111/。OP01工序779/eBR822后续读回为COMPLETED/VERIFIED、独立复核19289、下一OP02工序780 READY，未改变冻结独立性或网关规则。
