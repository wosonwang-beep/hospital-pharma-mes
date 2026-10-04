# DCP-MES-009-010-CONTRACT-001 — PROPOSED

批准记录（2026-10-03）：用户明确“确认批准其中的契约补齐及完整验收所需任务范围”。本提案范围及完整验收所需 MES-011、来料调查子范围已批准；下文待批准措辞保留为提出时历史。实际契约以随后一致性审查通过的累积基线为准。

## 已有授权与本提案边界

2026-10-03 用户已批准 DCP-MES-008A-CONTRACT-001，并明确将 MES-009/010 纳入本轮开发。该批准持续有效，不再次请求同一授权。本文件只处理新纳入任务中经原始设计核对确认的额外冻结契约冲突；未获批准，不是新事实来源。当前权威仍为 v1.0.9。

## 已确认的冲突及最小建议

所有引用均在 releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.9/。

| 项目 | 原文证据 | 建议的有界裁决 |
|---|---|---|
| 正式批快照创建时机 | 05数据库95–104行：process_snapshot_id NOT NULL；07状态机8行及TC-BAT-001：DRAFT下达时创建冻结快照 | DRAFT允许snapshot为空；release在同一事务生成不可变快照、关联批、创建执行单元；所有非DRAFT生产状态强制存在快照。不得预造空快照或引用可变定义充当冻结证据。 |
| 批编辑保存接口 | 10 UI259–262行已有独立编辑页和production:batch:update；08端点清单无PUT /main-batches/{id} | 增加该PUT，沿用既有权限；仅DRAFT编辑原有可编辑字段，要求versionNo/If-Match、reason、Idempotency-Key、同事务审计；非DRAFT拒绝409。禁止修改生产事实或批下达后的冻结快照。 |
| 计划日期的数据来源 | 10 UI222–245行要求订单/批计划日期查询；05订单448–454行与批95–104行无对应列 | 给订单及正式批各补planned_date DATE，可空；创建/草稿修改填写，日期区间查询仅匹配该字段。历史空值显示未设置，绝不拿createdAt冒充计划日期。生产日期从正式开始事件的started_at读取，无开始则为空；补齐该原状态机已要求的时间持久化映射。 |
| 订单状态 | PRD-001269–276行要求订单状态正确，DB有status，但07无ProductionOrder状态机 | 建议DRAFT→IN_PROGRESS→COMPLETED：创建为DRAFT，首个子批下达后IN_PROGRESS并关闭普通编辑；分配量按单位换算后等于订单计划且全部子批已生产完成或处于后续QA状态时COMPLETED（含REJECTED只表示生产完成，不表示质量合格）。分配量不得超过订单量，锁订单保证并发一致；不得改写各SubBatch独立计划量。禁止任意updateStatus。 |
| 工序门禁失败的状态 | 09功能128行要求Gate失败BLOCKED；12 TC-OP-002277–283行明确422且Operation不变 | 以原验收的原子失败语义统一：命令门禁失败422，不改变Operation/EquipmentRun/参数/审计业务事务。此裁决不增加BLOCKED写入命令或新状态事件。 |
| 设备运行记录与工序动作 | MES010设备运行验收要求运行链；现API只有operation start/pause/resume/complete与bind | 明确工序动作驱动已有EquipmentRun：start建立运行段；pause结束当前段；resume追加新段；complete结束当前段。已结束运行段不覆写；沿用既有表/API，以追加运行段保存谱系。 |
| 清场与资格前置证据 | 既有required_clearance/required_role及equipment_usage.clearance_status；无已实现清场事实producer，role与qualification_code不能擅自等同 | 当前凡要求清场而缺真实证据，或缺明确资格映射，保持422 fail closed；不得把客户端PASSED或角色名称自动当资格证据。此轮不新增清场业务/API或资格业务字典；必要依赖须明确记录，不能声称该路径已验收。 |

补充边界：工艺/eBR具体版本由release请求明确选择，草稿不增加替代快照版本字段；相同组织、内容完全相同的不可变ProcessSnapshot允许按已有唯一hash复用，必须核验内容相等。TC-BAT-002使用领域禁止SubBatch放行及无QA发布入口证据，不新增仅为返回失败的SubBatch release API。设备参数建议按组织、设备、parameterDefId、sourceMessageId联合去重，同一设备消息中的不同参数分别保留；相同键且内容不同返回冲突，不覆盖原参数。

## 开发和验收边界

- 不额外启动MES-011/012/013。IPC、物料平衡、实际称量投料消费依赖按真实producer可用性校验，缺证据fail closed，不能模拟通过来声称全链验收。
- 人工/系统放行、签名、修订、审计以及全部查询/编辑标签与输入框同行要求保留。
- 批准后同步Database、Domain、State Machine、OpenAPI、UI、Permission、Test/RTM、Migration、Integration/Dependency及任务卡，完整一致性审查通过才发布累积v1.0.10。v1.0.9及已执行V001–V013保持不变。
- 当前已只读确认V001–V013成功；尚未执行V014或任何新迁移。不得将本次设计核对写成开发或测试通过。
