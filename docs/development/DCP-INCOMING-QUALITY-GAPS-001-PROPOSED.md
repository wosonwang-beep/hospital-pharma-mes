# 来料质量剩余契约补齐提案

批准记录（2026-10-03）：用户明确“确认批准其中的契约补齐及完整验收所需任务范围”。本提案范围及完整验收所需 MES-011、来料调查子范围已批准；下文待批准措辞保留为提出时历史。实际契约以随后一致性审查通过的累积基线为准。

2026-10-03，待批准。本文件不是正式基线，不得直接按此创建表、接口或页面。

用户已确认业务主链及实现映射，并授权继续开发。此前批准的六条独立页面、工作台映射和 QMS 外键安装时序已进入 v1.0.10，无需再次批准。本提案只处理映射中尚未有明确设计的部分；以下均为建议裁决，不冒充原设计。

## 建议一次批准的边界

| 缺口 | 建议裁决 | 需要修改的契约 |
|---|---|---|
| DG-01 质量标准版本 | 建立独立 QC 标准及版本，采用 `qc_specification`、`qc_specification_version`、`qc_specification_item`，不恢复物料版本审批。请验必须引用已批准版本；历史引用和限值/方法快照不可随新版本改变。 | 新增标准管理契约；请验的 `qc_specification_version_id` FK，检验项目限值/方法快照；详见下一节。 |
| DG-02 取样记录和样品 | 保留正式 SamplingTask/Detail。SamplingTask 对应一次受控取样记录，Detail 对应位置/容器；每个 Sample 必须引用一个 Detail，一个 Detail 可产生多个 Sample。样品用途保留 TEST_SAMPLE、RETENTION_SAMPLE，补 RETEST_SAMPLE、OTHER_APPROVED。后者必须有名称、理由和取样计划批准签名，不能由录入人随意填写一个类型就生效。 | 补 `qms_sample.sampling_detail_id`、用途字典和取样计划批准命令/权限/签名摘要；不增并行 SamplingRecord 真源。 |
| DG-03 独立复检事实 | 将每次 TestInstance 明确映射为 `qms_test_execution`，通过 InspectionItem/Task 关联 Sample。批准复检创建新 execution，保留原 execution/result 链；新增原执行引用和批准调查引用。结果 revision 归属 execution，修订唯一性按 execution+revision 管理。 | 执行/结果 FK、唯一键、复检动作和调查批准消费契约。原 FAIL 不可通过普通修订变 PASS；其失效处置必须另留调查与签名证据。 |
| DG-04 结果字典 | 保留正式 PASS/FAIL/INCONCLUSIVE，增加 INVALID。INCONCLUSIVE 表示尚不能判定，INVALID 表示经批准认定无效；两者均不能满足合格放行。将 FAIL 判为无效时追加处置证据，仍保留原 FAIL。 | 结果/处置字典、API、UI、判定与验收断言。禁止静默改名或覆盖历史枚举。 |
| DG-05 报告聚合 | 报告以 InspectionRequest 为根，允许覆盖同一请验下多个 task/sample/execution。后端自动汇总冻结标准中的全部必检项目，记录原始结果及获批准的最终有效 result revision 引用；请求不得提交一套新的数值结果。 | 报告 request FK；现有单 task 必填契约改为请验必填、任务来源由服务端派生；报告项目保留精确结果引用。未完成必检、未复核或最终有效结果不唯一时禁止生成可批准报告。 |
| DG-06 随货资料 | 建立共享受控附件事实与收货关联，保存文件标识、名称、类型、长度、SHA256、上传者/时间、记录版本与保留状态；文件内容不可原位覆盖。COA 是附件用途，不用任意 URL 代替文件证据。 | 共享附件上传/读取、收货关联命令、权限、存储及保留契约。此项包含必要平台附件能力，不扩展 eBR 附件业务。 |
| DG-07 OOS/偏差 | 沿用正式 QMS 调查实体，增加来料 scope、MaterialLot/TestExecution/原始结果引用；复用统一调查记录，禁止建立来料专用第二套调查真源。没有有效关闭/批准处置证据一律阻断放行。 | 将 MES-012 中“来料 OOS/阻断偏差调查及复检批准”这一必要子范围纳入本轮，先补目标关系、状态、签名、命令及放行消费契约。其余生产 IPC、CAPA/平衡范围不自动启动。 |
| DG-08 UI 覆盖 | 沿用既有列表、编辑、查看、执行和签名规范，所有标签与输入框同行。补齐上述新增契约对应的字段/列/动作映射及可审查原型；放行查询继续使用现有 Lot 360°入口，不另增放行列表路由。 | 新增 QC 标准页面，其余仅补已有来料页面契约。不得套用成品 QA 的 eBR、平衡或成品批字段。 |
| BC-01/02 一致性 | USER_QA 决定必须有有效人工签名；SYSTEM_RULE 免验 signature_id 可空并保留规则证据。统一生产使用失败为 `MATERIAL_NOT_ELIGIBLE`，按现有资格契约返回原因。 | 新迁移约束及原测试文案同步；不造签名，不修改已执行迁移，不删除历史记录。 |

## QC 标准版本的具体建议

- `qc_specification`：组织、物料 FK、标准编码、标准名称，组织内标准编码唯一；沿用平台主键、创建/修改元数据和乐观锁规则。
- `qc_specification_version`：标准 FK、版本号、状态、批准人/时间/签名、记录版本；同一标准版本号唯一。状态 DRAFT → APPROVED → RETIRED。新版本批准不自动改写或退役旧版；APPROVED 才能被新请验选择，已引用的 RETIRED 版仍可完成历史检验。
- `qc_specification_item`：版本 FK、项目编码/名称、是否必检、NUMERIC/TEXT 结果类型、数值上下限（可单侧）、单位 FK、文本合格标准、方法编码/版本。项目编码在版本内唯一；数字和文本判定字段互斥；无数值时不得默认 PASS。
- 版本批准后内容不可改；修改必须创建新 DRAFT 版本。批准者不得为该版本编制人，批准与退役需要理由、再认证及绑定内容摘要的电子签名；所有关键动作同事务审计和幂等。
- 标准 API：`GET/POST /quality/specifications`、`GET /quality/specifications/{id}`、`POST /quality/specifications/{id}/versions`、`GET/PUT /quality/specification-versions/{id}`、`POST /quality/specification-versions/{id}/approve`、`POST /quality/specification-versions/{id}/retire`。PUT 仅限 DRAFT；无通用状态 API、无物理删除。
- 页面：`/quality/specifications`、`/quality/specifications/create`、`/quality/specifications/:id`、`/quality/specification-versions/:id/edit`。版本创建和批准动作沿用已有受控交互，不新增页面风格。
- 权限建议：`qms:specification:view/create/edit/approve/retire`，签名另需现有 `ebr:sign`；具体 DTO、错误码及菜单映射随正式补齐一并冻结，未完成一致性审查前不实现接口。
- 请验创建由标准版本 FK 派生质量标准显示文本，禁止同时维护可独立修改的第二套标准版本字符串。检验项目复制并保存所选版本的不可变标准快照及来源引用。

## 已有集成依赖，不隐含批准

MES-009/010 已获开发授权，其额外设计裁决见 [DCP-MES-009-010-CONTRACT-001-PROPOSED.md](DCP-MES-009-010-CONTRACT-001-PROPOSED.md)，尚未被本文件自动批准。若要求此次完成“实际生产投料反查完整来料链”，还必须明确将真实 MES-011 称量/投料/谱系实现纳入范围；仅验证库存资格不能冒充该场景。

本提案不要求重置 DEV、删除数据或修改执行过的 Flyway。批准后先把上述裁决展开为完整 Database/Domain/State/API/UI/Permission/Audit/Signature/Test/RTM/Integration/Dependency 契约，再做一致性审查并发布新的累积基线；版本从当时最新基线继续，不覆盖 v1.0.9/v1.0.10。之后按既定顺序开发，并只运行改动所需的定向验证。

**建议确认内容：批准本文件的补齐边界；如要求本轮完成全部最终场景，同时批准已有 MES-009/010 补齐提案，并将 MES-011 与本文件限定的来料调查子范围纳入本轮。** 这与再次确认业务流程或原映射不同，批准对象是上表明确新增/调整的契约和任务边界。
