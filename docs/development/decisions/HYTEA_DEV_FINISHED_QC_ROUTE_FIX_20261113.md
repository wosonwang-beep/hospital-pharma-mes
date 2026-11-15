# 成品QC既有接口安全路由缺失修复（本机DEV）

状态：BUGFIX / 最小现有权限映射，2026-11-13。依据FINAL BASELINE v1.0.23与DCP-PRODUCTION-FINISHED-ENTRY-001；不新增权限、API、业务表或状态机。

## 证据
入库126已由仓库11565正式签名5329确认，成品库存流水3，数量398000ml；请验108管理员创建并由QC分析员11567 ACCEPTED；取样107由sampler11566签名生成样品1640，样品状态COLLECTED。分析员11567拥有qms:sample:receive、qms:test:record，GET /samples/1640的allowedActions=[RECEIVE]。但POST /api/v1/samples/1640/receive返回HTTP403。生产QualityController实际存在相应接口；SecurityConfiguration仅有GET /api/v1/samples，而遗漏POST receive以及production-tests创建/结果/复核路由，默认anyRequest denyAll。此为权限映射缺陷，不是用户缺角色权限。

## 白名单
仅backend/mes-security/.../SecurityConfiguration.java新增ProductionQualityController既有端点精确匹配及已有权限：POST samples→qms:sample:create、POST samples/{id}/receive→qms:sample:receive、GET production-tests[/{id}]→qms:test:view、POST production-tests→qms:test:record、POST results/revisions→qms:test:record、POST review→qms:test:review、POST retest→qms:test:retest。原GET samples及其它映射保持不变。不改控制器、服务、DB、用户角色或签名规则。

预期验证：Java编译、旧API health，正常重启后由分析员按冻结角色接收样品1640并完成八项成品结果，由独立复核员11568审核。全部仍是明确的合成DEV软件流程输入，并非实际实验室测定值。

## 2026-11-13 执行及验收结果
- 完成现有SecurityConfiguration对ProductionQualityController的精确路由匹配，未增权限或API。mes-security及mes-boot Maven package（skipTests）均成功，唯一8080按local+正常Flyway启动并health UP。
- 成品请验单108由admin11556创建提交、QC11567受理，ACCEPTED；成品取样107由sampler11566签名5330生成样品1640，QC11567通过修复后的正式`/samples/1640/receive`成功接收，证明403问题解决。
- 质量标准1419八项全部由QC11567录入原始结果修订839–846及签名5331–5345（与独立复核签名交错），QC复核11568对每项独立CONFIRMED。记录514–521均COMPLETED，样品1640 TEST_COMPLETED。外部合成DEV观测说明在private-data-audit/hytea-resume-20261111/finished-qc-655-dev-observations.json，非真实实验室结果。
- 检验报告98（FR-HYTEA-655-20261113）生成并由QC11568独立审批为APPROVED/PASS，报告签名5347，请验108 COMPLETED。QA审核PRODUCTION_QC及FINISHED_INSPECTION_REPORT均PASS。
- 无数据库迁移、无权限扩张、无业务设计修改；正式E2E接口成功，不宣称本轮全量G5 UI已完成。
