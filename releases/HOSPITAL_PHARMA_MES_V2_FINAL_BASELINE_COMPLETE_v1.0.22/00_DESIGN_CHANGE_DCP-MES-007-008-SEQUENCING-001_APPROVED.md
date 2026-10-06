# DCP-MES-007-008-SEQUENCING-001 — APPROVED

批准：用户于 2026-10-03 明确回复“批准按该方案补齐契约并继续开发”。本累积发布承载该有界批准，v1.0.8 保持不可变历史；交叉检查通过后切换权威指针。原审查证据见仓库 docs/development/mes-006-008-baseline-review.md。

目标：保留既有功能、UI 和 GxP 规则，仅闭合 MES-007/008 与后续物理依赖、验收时序及收货编辑 API 的冲突。此前 MES-006 专项批准没有覆盖这些任务边界和 API 变更。

## 已批准范围

1. **eBR 分阶段交付，完整验收不降级。** MES-007 先交付 LG-007A 定义、Designer、白名单规则引擎、发布模板和运行消费契约；LG-007B 仍由 MES-009 在正式批模型建立后落地。运行代码的 isolated contract 测试与真实数据库/批次验收分开记录。完整 MES-007 状态保持 IN PROGRESS，直到原有 runtime、快照、更正、复核、签名和并发必测项在真实依赖下通过，不用分阶段结果冒充 READY。
2. **明确两组延后 FK 的责任。** LG-007B 建立 form_instance.operation_execution_id 列时暂不加向未来工序表的 FK，由 LG-010 建立 operation execution 后检查历史引用并在新迁移追加 FK；此前禁止写入非空、无真实工序的运行引用。LG-008 的 reservation/issue.main_batch_id 同理保留字段，LG-009A 建表后以新迁移检查并追加 FK；正式批模型到位前阻断依赖批次的写命令。不提前建立生产表、不永久省略 FK、不修改已执行迁移。
3. **保留原收货编辑页，补其唯一保存契约。** 增加 `PUT /wms/receipts/{id}`，使用既有 UI 的 `wms:receipt:update`，仅更新未确认草稿的已设计字段，要求 reason、If-Match/versionNo、Idempotency-Key、同事务 Audit；确认后禁止普通修改。DTO 只来自已有 Receipt/Item、收货检查及 MaterialSnapshot 设计，明确草稿/确认边界和 200/400/403/404/409/422 错误规则；不添加新业务字段或另一套收货入口。
4. **明确 WMS Gate 的实际交付依赖。** MES-008 先实现收货、物料批及追加库存账、现有资格 Gate 消费契约；MES-008A 实现真实 MaterialEligibilityService、免验/检验及放行证据。Gate 或批次契约未就绪时关闭生产预留/发料写入口，不以允许型替身运行。TC-WMS-006/007、TC-ELG-001/002 在 008A 集成门执行，预留/发料完整验收在 008A 和 009 依赖可用时执行。MES-008 在全部原定功能验证前保持 IN PROGRESS；不隐含授权本次开发 008A/009/010 的业务实现。

上述选择延续现有 UI/领域设计，改变的是明确列出的 API 和跨任务衔接。若要求 MES-007、008 在后续任务实施前立即完整验收，则本方案不足，需要另行授权提前交付真实依赖的明确业务范围，不能通过伪造数据解决。

## 批准后的文档与实施要求

- 发布累积新基线，保留 v1.0.8 原件；同步 Database、Domain、State、API/OpenAPI、UI/Permission、Test/RTM、Migration、Integration 和 Task Dependency。明确迟加 FK 的列约束、禁止写入窗口、依赖建表后校验与失败处理，完成一致性审查后再切换指针。
- 继续沿用现行 UI 原型结构和横向标签规范，不启动视觉重设计；物料仍只有已批准的基本信息、单位换算、多供应商和唯一首选供应商。
- 物理迁移版本在执行前读取真实 Flyway history 后分配；现有 V001～V011 以及持久开发数据不改不删。
- 仅跑受影响编译、当前阶段必测项、真实依赖就绪后的指定集成 Gate、必要桌面/手机用例；不反复全量测试。任务阶段和未验证项只在 MES_TASKS.md 维护。
