# DCP-DEV-HYTEA-SAMPLER-ACCEPT-001 — 喉咽茶 DEV 取样岗位请验受理权限补齐

状态：APPROVED / DEV-ONLY / BOUNDED
依据：用户持续授权以仓库 file/2-1.jpg 至 file/2-6.jpg 作为本机 DEV 业务流程测试资料，并明确要求完成喉咽茶合剂全流程。批准日期：2026-11-10。

## 1. 根因与选择
- 本机 `application-local.yml` 已冻结 `sample-execute -> MES_DEMO_SAMPLING`。
- 现有 DEV 用户 `demo.sampler` 已有有效 `MES_DEMO_SAMPLING` 资格和 `qms:sampling:execute` 权限，但缺少 `qms:inspection-request:view` 与 `qms:inspection-request:accept`。
- `demo.qc.analyst` 具备请验查看/受理权限，但只有 `MES_DEMO_QC_EXECUTION`，不满足取样岗位资格。
- 不给QC分析人员补造取样资格；不让admin冒充取样人；不修改生产业务权限或资格映射。采用最小修复：仅为已有DEV取样角色增加查看/受理权限，使有既有取样资格的取样岗位完成请验受理与生成取样任务。

## 2. 允许写入范围
- 仅DEV本地角色 `DEMO_MES_SAMPLER` 增加 `qms:inspection-request:view`、`qms:inspection-request:accept`。
- 通过既有 `DemoRoleAssignmentsSeedIT` + `IamContractService` 的正式审计化IAM命令持久化；不直接SQL写权限，不新建用户、不修改资格、不授予admin以外的业务操作权限。
- 只在本机 `hospital_pharma_mes_dev` 生效；不生成Flyway迁移，不影响CI/PROD。
- 其余岗位维持职责分离：QC分析执行检验，QC复核执行独立复核，QA执行物料/成品决定。

## 3. 禁止事项
不修改 `sample-execute` 资格映射；不将历史纸质签名转成电子签名；不把历史检验结果记作2026新批实测；不跳过独立复核/QA；不改冻结快照；不修改已验收QC基础资料。

## 4. 验收
1. DemoRoleAssignmentsSeedIT 通过且真实本地角色读回含上述两项权限；原有其它权限全部保留。
2. demo.sampler 通过正式接口对请验721–726执行受理/生成取样任务，资格检查正常通过，审计及签名按既有命令生成。
3. 之后仍由各自的demo.qc.analyst / demo.qc.reviewer / demo.qa身份完成检验、独立复核与QA决定。
4. 失败时保留原状态，不直接更新业务表或重试非幂等命令。


## 5. Cross-role sampling extension (2026-11-10)

The first request-acceptance fix passed. The next existing route contract requires the QC analyst to create/assign the sampling plan, the qualified sampler to perform/receive samples, and a separately qualified warehouse operator to confirm physical sample outbound. Extend the same DEV-only boundary:
- `DEMO_MES_QC_ANALYST`: add `qms:sampling:assign` only; it already has sampling-task create and QC test-execute capabilities.
- `DEMO_MES_WAREHOUSE`: add `qms:sampling:warehouse-confirm` and `wms:inventory:sample-outbound` only.
- `demo.warehouse`: add a local DEV qualification code `WAREHOUSE_SAMPLE_OUT` through the existing qualification service seed. This is explicitly a DEV test qualification, not real employee training evidence and never a production credential.
- Existing sampler receives/executes the sampling work; QC analyst performs lab tests; QC reviewer performs independent review; QA makes release decisions. Preserve those identities and separation.
- Use the existing IAM audited role service and existing QualificationService seed; no direct SQL writes. Require existing active MES_DEMO_SAMPLING and MES_DEMO_QC_EXECUTION qualifications before role grants. Require only the new DEV warehouse qualification for the local warehouse test account.
- No PROD permission, GxP workflow, quality limit, real employee qualification, or frozen API contract changes.
