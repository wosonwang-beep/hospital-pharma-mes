# 医院制剂 MES — 真实集成审查（2026-10-08，R3：岗位资格、质量计划、QA）

## 本轮范围与环境

- 仓库：`D:\codex\_project\gmp\hospital-pharma-mes`，工作分支 `main`。
- 使用同一本地开发库、MariaDB 3306、Redis 6379、Spring Boot 8080、Vue 5173。未启动 Docker、第二个 Redis 或额外数据库。
- 本轮对人员资格、生产质量计划、QA 质量决定开展真实页面审查、Service 负向测试和不同岗位的实际登录验证。

## 1. 资格与 UI

- 初始库中 7 名演示用户都用 `MES_DEMO_GMP`，且此前**均无 RBAC 角色绑定**；页面无法直接证明不同岗位的职责隔离。
- 将资格字段误称 UTC 的标签清理为业务日期；生产质量计划展示标准名称、平衡规则名称和可读的批准时间。
- 生产质量计划后端已检查“创建者/修改者不得自行批准”、已批准标准签名、受控签名、审批后冻结。前端也已过滤创建/修改者自己的批准按钮，并解释需由独立 QA 审核。
- 已验证 `ProductionQualityPlanIT`：14/14 PASS，含独立审批、签名校验、冻结后禁止修改和草稿不能派发。

## 2. QA 资格撤销的服务端负向测试

- 新增 `FinishedReleaseIT#revokedQaQualificationBlocksReleaseDespiteRetainedPermission`。
- 临时测试批先满足 QA 放行条件，再通过正式 `QualificationService.command(DISABLE)` 停用 QA 人员资格。
- 验证 QA 审核模型移除 RELEASE，并通过 Service 直接提交放行也得到 `QUALIFICATION_REQUIRED`；无新质量决定，批次保持 `PENDING_QA`。
- 测试 1/1 PASS（事务回滚）。没有更改任何真实已签名、已放行历史。

## 3. 本地验证资格与最小角色 Seed（DEV-only）

- `application-local.yml` 新增岗位专属 `mes.qualification.required-codes` 映射，分别对应取样、QC 执行、QC 复核、QA 放行、生产操作、称量复核；保留 Redis 6379。
- 新增 `DemoRoleQualificationSeedIT`：使用正式 `QualificationService` 为演示岗位登记 6 组特定资格码，成功提交。
- 新增 `DemoRoleAssignmentsSeedIT`：通过受审计的 `IamContractService` 创建 7 个开发验证岗位角色并分配给既有演示人员。重复运行只做必要的增补，不改 SYSTEM_ADMIN 和其它岗位。
- QA 演示角色补齐只读 eBR、物料平衡、QC 样品/检验、质量标准和产品物料主数据权限，仍然**没有 QC 检验执行权限**。
- Playwright 实际登录 7/7：生产、仓储、取样、QC 检验、QC 复核、QA、主数据；每个账号的权限快照均包含岗位必需权限，不包含对照的禁止权限。
- 这些凭证和资格仅用于本地开发验证，**不是正式人员的 GMP 培训资质或身份审批**；生产环境配置未改变。

## 4. QA 放行工作页的独立性提示

- 已发现原服务端审核模型对已放行批次 `20261007-KCL-001`，向原 QA 决定人仍返回 `RELEASE/REJECT` 备选，但实际服务端在同一人试图覆盖原决定时会阻断。
- `FinishedReleaseView.vue` 和 `finishedReleaseModel.ts` 补上基于原决定人/当前账号的 UI 过滤：原签署人不再看到可执行按钮，页面解释必须由另一名独立 QA 执行。
- 不改服务端 `reviewDigest`、`allowedActions`、签名合同或已生效决定。其他独立人员仍以服务端真实 Gate 和签名鉴权为准。
- QA 与 admin 真实会话对比：QA 有岗位资格并能读取完整证据；SYSTEM_ADMIN 有完整功能权限但无 QA 资质，因此无放行操作。实际路由只读回归成功。
- 单元测试新增“原 QA 签署人不可自行覆盖质量决定”场景；前端总回归 **102/102 PASS**，类型检查通过。

## 5. 仍未关闭的受控设计问题

1. **HIGH**：历史已放行演示批次 `20261007-KCL-001` 的标示规格 `30mL:3g` 与冻结处方 `20g/1000mL` 存在 5 倍差异，而 QA Gate 并未建立结构化规格/处方一致性校验。须以 DCP 确定药学标示、质量标准及 Gate 规则；不可编辑历史签名和生产记录。
2. **HIGH（演示数据真实性）**：部分历史测试检验/仓储确认记录的操作人与岗位标签不一致。未来生成器已调整，原受控记录不得重写。
3. **需确认 GxP 基线**：`ProductionQualityPlanService.approve` 当前校验批准权限、独立身份、冻结标准和电子签名，但未看到类似 `qa-release` 的独立人员资格门禁；是否强制需受控业务规则评审，不能擅自增加。
4. **服务端审核提示一致性**：已生效 QA 决定的 `allowedActions` 仍可能对原签署人包含不可完成的二次决定，前端已按现有独立性约束拦截显示；服务端 read-model 合同调整须另走受控变更流程。

## 6. 测试结果与交付边界

- `DemoRoleQualificationSeedIT`：1 PASS，6 组资格已加入开发库。
- `DemoRoleAssignmentsSeedIT`：1 PASS，7 个岗位角色与人员关联建立，QA 只读证据权限受控增补。
- `FinishedReleaseIT#revokedQaQualificationBlocksReleaseDespiteRetainedPermission`：1 PASS。
- `ProductionQualityPlanIT`：14 PASS。
- 前端类型检查 PASS；全量前端单测 **102/102 PASS**。
- 真实浏览器登录各岗位、QA/admin 权限对比和前序签署人按钮隐藏检查通过。
- 没有重新执行生产或成品的 `@Commit` 数据生成器；没有重签/覆盖已批准质量文件。
- 代码及文档暂留本机工作区，未提交、未推送 GitHub。
