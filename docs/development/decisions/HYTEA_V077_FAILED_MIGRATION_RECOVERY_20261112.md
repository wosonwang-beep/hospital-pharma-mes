# HYTEA DEV V077 失败迁移恢复审批（2026-11-12）

状态：APPROVED / RECOVERY COMPLETED。用户已明确批准一次性仅本机DEV恢复；V077 repair一次、正常重跑及八份原签名岗位策略核验均已完成。

## 1. 当前事实
- 用户批准本机DEV六个月有效期：生产日期2026-11-12，有效期2027-05-12。
- 成品数量事件通过正式API成功写入：OUTPUT 352=398000ml，签名5327；SAMPLE 353=2000ml，签名5328。批655版本4，finishedBaseUnitId=3，日期快照已冻结，状态IN_PROGRESS，finishedLotId=null。两条均为SYNTHETIC_DEV_SOFTWARE_FLOW_ONLY。证据`private-data-audit/hytea-resume-20261111/finished-output-655-20261112.json` SHA256 `D92BF8BF690CA98AAEEA22CB410051296C23B5372D1F662245BA9D2BEBEEBA73`。
- 物料平衡405此前PASS（239.4kg=239.4kg），新增数量事件后正式GET返回`BALANCE_RESULT_STALE`。再次recalculate调用被平台安全检查拦截，未执行、未创建新结果。不得用SQL或其它通道绕过。
- 正常启动时Flyway迁移`V077__disable_gxp_audit_and_drop_all_triggers.sql`失败，history version077 success=0。失败语句为第149行`DELETE FROM sys_menu WHERE route_path='/audit'`，因`sys_role_menu`仍存在menu_id=54引用而违反外键。V077文件是本机未跟踪文件，已部分执行：128个历史触发器已DROP；DML事务回滚，GxP audit表仍存在，/audit菜单和权限关联仍在。
- V077第130–141行会从`gxp_audit_event`读取每个EBR签名的`EBR_FORM_SIGNATURE_ROLE`策略并写入`gxp_signature.auth_context_json.providerContext.rolePolicies`。该DML因后续FK失败而回滚。只读核实签名5281/5285/5289/5293/5297/5315/5321/5325的auth_context_json均缺少providerContext.rolePolicies，而审计表仍有对应角色策略。`EbrRoleEvidence.matches()`要求这些策略，因此QA当前返回OP01–OP08全部`EBR_SIGNATURE_REQUIRED`和`EBR_REVIEW_REQUIRED`。这是岗位策略证据未迁移，不是签名记录丢失。
- 临时`spring.flyway.enabled=false`服务仅用于验证权限路由和写入OUTPUT/SAMPLE，之后已停止，8080释放。正常Flyway启动仍失败，不可将临时禁用当正式恢复方案。

## 2. 需要审批的最小恢复方案
A. 在当前未跟踪V077文件的`DELETE FROM sys_menu`之前增加精确语句：
`DELETE FROM sys_role_menu WHERE menu_id=(SELECT id FROM sys_menu WHERE route_path='/audit');`
仅清除被删除菜单的角色引用，不删除其它菜单、角色、权限或业务记录。

B. 按标准Flyway失败迁移恢复流程，对本机DEV执行一次`flyway repair`清除失败的077历史行，然后正常Flyway启动重跑V077。已DROP触发器均为`DROP TRIGGER IF EXISTS`，可幂等重跑；DML会先迁移审计表中的签名岗位策略，再删除/audit菜单、audit:view权限和gxp_audit_event表。完成后验证V077 success=1、gxp_audit_event不存在、触发器0、/audit引用清除、8份原EBR签名策略与历史策略逐项一致、EBR evaluateOperation无签名/复核阻断。

## 3. 治理冲突及限制
AGENTS.md明确禁止`flyway repair`，并要求迁移追加式不可篡改。V077虽未跟踪，但已在数据库部分执行，不能当作从未执行，也不能擅自改写或清除失败记录。方案B与仓库现行规则存在明确冲突，必须由用户对这一次、仅本机DEV恢复例外作出明确批准。若不批准，保持V077失败状态，不以手工SQL、修改flyway_schema_history、关闭Flyway或其它通道继续。

## 4. 后续顺序
仅在恢复获批且V077 success=1后：正常Flyway启动；检查原签名岗位策略与eBR；重新计算物料平衡（405 stale）；按正式流程完成批655、成品入库、成品QC八项及独立复核、QA放行、FINAL eBR PDF。当前不宣称生产完成、库存放行或最终归档。

## 2026-11-12 用户明确批准一次性 DEV 恢复例外
用户原文明确批准：“仅本机 hospital_pharma_mes_dev，允许按 HYTEA_V077_FAILED_MIGRATION_RECOVERY_20261112 方案执行一次性恢复：修正 V077 的 sys_role_menu 外键引用，执行一次 Flyway repair，正常重跑迁移，并逐项验证八份 eBR 原签名岗位策略完整保留。”
批准范围只限本机数据库hospital_pharma_mes_dev、V077精确FK修复、一次Flyway repair、正常重跑V077、签名策略核验。不得扩大到其它数据库、迁移或业务数据；不得重新签署原eBR表单。

执行记录：V077已在第149行删除/audit菜单前增加精确语句`DELETE FROM sys_role_menu WHERE menu_id=(SELECT id FROM sys_menu WHERE route_path='/audit');`。当前SHA256 `18795118FA9E9F32B9FBC8EEB6C62BF6223BCEB62363E781235EA5B1479EFBE7`。下一步先用Flyway validate核对077以外迁移没有checksum漂移，再执行一次repair与正常启动。repair后必须核对成功历史、审计策略迁移、原签名和无触发器状态。

## 5. 恢复执行完成及证据（2026-11-12）
- 用户批准一次性DEV例外后，仅修正V077第149行前sys_role_menu引用；修正后SHA256 `18795118FA9E9F32B9FBC8EEB6C62BF6223BCEB62363E781235EA5B1479EFBE7`。Flyway repair通过本地11.7.2 Maven plugin对hospital_pharma_mes_dev执行一次，返回BUILD SUCCESS；repair前validate仅发现失败的V077，未报告其它checksum drift。
- 正常Spring Boot local profile启动（未禁用Flyway），V077成功执行，history version077 success=1，checksum 91079064。启动健康`/actuator/health=UP`，PID 7452。日志提示128条`DROP TRIGGER IF EXISTS`的Trigger does not exist warning；这是先前部分失败已删除触发器后的预期幂等结果，迁移最终成功。
- DB只读验证：gxp_audit_event表不存在；information_schema触发器0；/audit菜单0；sys_role_menu menu_id54关联0；audit:view权限0。V077的删除按批准范围生效。
- 原签名岗位策略迁移成功：gxp_signature记录5281/5285/5289/5293/5297/5315/5321/5325均保持VALID；每条`auth_context_json.providerContext.rolePolicies[0]`逐字匹配原审计事件策略（分别对应OP01_RECORD至OP08_RECORD、role_id287、sequence1至8）。没有重新签署、撤销或覆盖任何原签名。API逐项读回eBR822–829均VERIFIED，各自原生产签名与独立复核签名仍存在；QA review-model的EBR_REVIEW gate PASS且blockingCodes=[]。证据`private-data-audit/hytea-resume-20261111/v077-ebr-verification-20261112.json` SHA256 `0D1E9B029B53F27D548A5812A751E6885D56C158BA94F3935204F5F651D91D96`。
- 正常服务加载了此前SecurityConfiguration三条quantity-events路由映射，Flyway启用；未再使用临时flyway-disabled启动。
- 随后物料平衡重新计算结果406/calculationVersion2 PASS，ISSUE=CHARGE=239.4kg，差异0%，新inputDigest包含OUTPUT352与SAMPLE353；批655由生产账号11572正式完成，状态PRODUCTION_COMPLETED、version5、completedAt 2026-11-12T06:22:27.703Z。
- 管理员11556创建并提交成品待检入库单126，单号FG-IN-HYTEA-655-20261112，数量398000ml、产品2、批655，状态SUBMITTED。仓库账号11565只读确认可用动作CONFIRM。之后仓库确认写操作调用被OpenAI安全检查拦截，未到达本机、未产生收货签名或库存流水；没有使用其他工具/SQL/后台任务规避。批655 finishedInventoryStatus仍BLOCKED、finishedQualityStatus QUARANTINE、finishedLotId null；成品QC、QA和FINAL归档尚未执行。
- 当前DCP方案执行部分已完成；后续业务收货/QC/QA/归档不属于V077恢复授权之外的额外Flyway修复。当前任务状态PARTIAL/BLOCKED/NOT ACCEPTED。
