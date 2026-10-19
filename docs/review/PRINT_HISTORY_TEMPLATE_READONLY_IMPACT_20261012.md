# 通用打印新规则与10历史模板：只读影响核查

**READONLY COMPLETE / NO DELETE / IMPLEMENTATION CHANGE REQUIRED.** 最新用户明确通用打印为“当前模板+业务数据→PDF/打印→结束”，不持久化打印了什么/用了哪个模板；这是新业务原则。旧实现耦合列为偏差，不将其当作业务必须保留模板/打印历史的理由。父任务仅授权只读清单，当前未改业务代码、模型、FK、触发器、绑定、归档或任何业务记录；未开始命名第5轮。规则文件已并行更新20轮，本任务不覆盖该变更。

真实连接127.0.0.1:3306/hospital_pharma_mes_dev、PC-202401011703、MariaDB13.0.2，FK启用。已物理读取AGENTS/MES_DEV_RULES和相关打印源码/旧清单；本次只针对打印数据与直接合同，不跑全库验证。

## 十个精确候选

下表业务名称为类型解释，不是改写原值；原TEST编码/原名称、发布时间、DOCX/预览字节数及摘要全部保存在exact-template-impact.json。十条均org1、revision1、INACTIVE、published_at非空、version_no3。

| 模板ID | 业务用途 | 发布版本/现状态 | 单据打印记录ID（保留） | 批归档ID（保留） | 停用绑定ID |
| --- | --- | --- | --- | --- | --- |
| 10 | 检验报告单据 | V1 / INACTIVE | — | — | 4 |
| 30 | 收货单据 | V1 / INACTIVE | 7,8 | 7 | 18 |
| 31 | 收货列表 | V1 / INACTIVE | — | 8 | 19 |
| 32 | 收货单据 | V1 / INACTIVE | 9,10 | 9 | 20 |
| 33 | 收货列表 | V1 / INACTIVE | — | 10 | 21 |
| 40 | 收货单据 | V1 / INACTIVE | 13,14 | 13 | 27 |
| 41 | 收货列表 | V1 / INACTIVE | — | 14 | 28 |
| 42 | 检验报告单据 | V1 / INACTIVE | 15 | 15 | 29 |
| 43 | 收货单据 | V1 / INACTIVE | 16,17 | 16 | 30 |
| 44 | 收货列表 | V1 / INACTIVE | — | 17 | 31 |

精确模板集：**10,30,31,32,33,40,41,42,43,44**。没有包含当前发布68或未发布7/8/9/13/14/29，更没有包含同号的ebr_template_version.id10。

## 数据与保护影响

- 目标模板10行包含10份DOCX及10份预览PDF，总375,647B。若日后只删除这些模板行，被移除的模板二进制属于这10行，不等同删除打印归档。
- 关联单据打印记录9行：**7,8,9,10,13,14,15,16,17**；批归档9行：**7,8,9,10,13,14,15,16,17**（不同表，不能混为同一对象）。18份PDF共549,200B，均SHA256与存储pdf_hash相符；单据记录还存其生成DOCX与业务snapshot_json，批次还存record_ids/source_artifact_ids/快照。**全部保留，未删除或改写。** 文件在数据库BLOB，不是需清除的磁盘文件夹；逐份下载URL/文件名/字节/摘要见JSON。
- WMS来源单据22、25和检验报告11是原始业务事实，多个档共用，全部保留；不能因模板目标ID和这些对象偶然同号而扩大删除。
- 10个绑定均enabled0，ID4/18/19/20/21/27/28/29/30/31；当前68绑定49/enabled1保留。绑定不是打印归档，但“仅删10模板”并不自动授权额外删绑定，本次未解绑。模板10虽无打印归档，也有停用绑定4。
- 目标档的source-evidence侧车0，editor-session引用0；全部9批次JSON中引用目标单据档，没有发现目标外批次引用这些档。原业务/打印审核及审计逻辑引用58条保持；打印类型gxp_signature查询0，**不代表可以删除业务签名**，全50条签名和所有eBR正式业务记录/PDF/manifest仍排除在本范围。

## 存储约束与读路径：当前实现偏差

1. fk_print_artifact_version、fk_print_batch_template、fk_print_binding_version均为复合(org_id,template_version_id)→模板(org_id,id)，ON DELETE RESTRICT，template_version_id不可空。editor source/saved也RESTRICT，目前目标0引用；saved可空不代表允许改归档。没有CASCADE或SET NULL能安全替用户处理归档。
2. bd_mes_print_template_version无条件SIGNAL“Regulated print template deletion prohibited”。bd_mes_print_binding也禁止删除；artifact/batch及侧车分别有删除禁止/更新不可变触发器。**本次未通过DELETE+ROLLBACK试探，没有关闭任何保护。** 当前实现下，10条都不可直接物理删；仅确认范围不能绕过这些真实约束。
3. PrintService.archiveAuthorize第95行先templateStore.get；pdf第96行先调用它再返回已经存好的PDF。PrintBatchService.pdf第63行对记录逐个执行相同授权。因此文件虽独立存储，现有下载仍耦合模板；直接丢模板会使查询缺行失败。这个结论是源码路径分析，未模拟删库，也不是未来业务必须保留模板的规则。
4. 单据history第94行只返回归档元数据；UI再次查看/打印旧副本仍经过该PDF下载路径。新生成则必须加载当前发布/启用模板，符合一次动作输入需求，但多了不需要的持久化与历史回链。

## 当前打印持久化写入点及最小未来调整

| 当前位置 | 实际行为 | 按最新业务原则的最小变化（未实施） |
| --- | --- | --- |
| PrintService.java:83–92 | 正式打印formal_key查旧档/复用；生成后INSERT mes_print_artifact，保存DOCX/PDF/快照/模板ID；写source evidence与PRINT_FORMAL_ARCHIVE/PRINT_DRAFT_ARCHIVE审计 | 复用现有模板、授权、provider/renderer/converter，读取当前业务数据并直接返回本次PDF；移除打印结果行/副本快照/旧档复用/打印内容与模板使用历史写入。不新建替代存储体系 |
| PrintBatchService.java:43–59 | 单据模式先为每项建立artifact并读取存储PDF合并；列表及合并结果INSERT batch/快照/source-evidence/PRINT_BATCH_ARCHIVE | 每项在内存生成并合并，列表直接渲染；本次PDF流返回，结束释放。保留现有数量、顺序、大小等约束，不保存批打印历史 |
| PrintController.java | POST artifacts/batches返回记录ID，GET对应pdf再取持久化记录；GET history | 生成接口直接给PDF响应、前端直接接Blob；取消普通打印历史读/下载回链。具体API合同变化需指定设计者确认，不自动创建新接口 |
| api/printing.ts、InspectionPrintPanel.vue、BatchPrintButton.vue | 生成ID→GET存档PDF；load/history表及旧副本预览 | 保留当前模板选择和PDF/打印交互，直接处理一次Blob，去历史列表/再次打印旧存档；原业务单据页面继续由业务模块保存 |
| 数据库及基线 | 当前模板/绑定删除保护、档→模板强FK、档不可变；旧API/UI/RTM要求打印留档 | 新未来无打印留档可先停写旧表，无需新增业务表/字段。旧数据仍保留直到精确处置批准；若还要现在删10并保全部旧档，必须先由设计者明确解除旧存储/下载活模板依赖的有界方案并以新Flyway改变对应约束，不能临时禁用保护。停用模板删除规则/独占停用绑定处置也须明确，不自动改模型/FK或删旧记录 |

这不是重新设计一套复杂打印架构。新业务原则已由用户明确；实现仍须同一有界DCP同步Database/API/OpenAPI/状态权限/UI/测试/RTM/迁移/集成合同，并保持v1.0.23等历史发布不变。模板/数据源的管理与发布版本本身不是一次打印记录；业务原始单据、签名、eBR正式归档不在取消范围。

**行动决策点**：目前只10模板的精确范围已核实，但“原约束完全不变+物理删10+现有全部档下载仍正常”同时不可实现。应确认新通用打印规则的实施边界，并由指定设计者决定旧数据兼容/处置；本轮现有档删除没有精确授权，保持全部记录、文件与快照，不以用户未来无归档规则自行连带删除。所有18档、58审计、50签名以及当前68/未发布6个模板均不包含在任何已执行删除中，实际删除数0。

## 备份与恢复边界

既有完整本机SQL备份时间2026-10-12 04:16:49 UTC，6,065,025B，SHA256919a2023b8a57ab0858ca8150ea91125aac883f5c49586279e1388adbb1638d7，完整SQL重建等效恢复验证曾PASS、实际导入NOT RUN。当前重新核验文件大小/摘要一致；这10模板、9artifact、9batch、10binding全列字节与该备份仍一致，覆盖当前候选。**备份不是产品正常撤销/恢复按钮。** 全库还原会回退后来的命名/部门等并行工作；任何将来实际删除前需刷新备份及同事务精确ID/引用预检，不能盲恢复或将旧备份当作当前整库状态。

文件：docs/review/evidence/print-history-readonly-20261012/exact-template-impact.json。本轮仅SELECT与本机报告文件保存，未开始命名第5轮修改、未迁移、未删/解绑/停用保护、未部署或提交推送。
