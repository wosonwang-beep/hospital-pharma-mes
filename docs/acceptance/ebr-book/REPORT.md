# DCP-EBR-BOOK-001 验收记录（待设计负责人复核）

基线：PC-202401011703，真实仓库 D:/codex/_project/gmp/hospital-pharma-mes，main 8f61f75。保留既有大量未提交并行修改；没有切旧分支、提交、推送或全系统审查。冻结 v1.0.23 和已执行旧迁移不改写。

实现范围：版本化册模板与规格映射、发布冻结、显式 BATCH 共享实际表单、业务来源只读目录/表格、既有运行态填写链接、真实 DOCX/PDF 整册及不可变档案；字段来源见 FIELDS_AND_TEMPLATES.md，API 见 ../../api/ebr-book.openapi.yaml。

DEV：既有 localhost MariaDB3306 hospital_pharma_mes_dev、Redis6379。V035/V036 成功追加，36 项 Flyway 校验通过。迁移前完整数据库备份 2,981,440 bytes，SHA256 5D262A41275A62BD04D0E17334F7B1967EF073FB3059AF56696F424A97467C8F；备份含真实数据，保留本机私有 Temp，不作为附件上传。测试独特合成标识且事务回滚，不清库、不修历史、不新增 Redis/Docker。

## 验证和证据分层

|检查|结果/范围|
|---|---|
|BookDefinition / BookState / BookPdfComposer|12 项：闭合白名单、状态、空/损坏来源拒绝、真实 FOP 和真实 LibreOffice、中文长文本/75行多页、真实续页表头断言、封面/目录/最后行/页码/书签、图像附录。|
|ProductionBookIT|真实 DEV 回滚集成：跨规格冻结、两个子批灌装/包装相同物理 formId 且数据库实际计数 2、其他工序按实例、真实单位名称、原始数据读取、保存/提交、源变更产生新 PDF、停用保留旧批、旧版字节重打、来源权限撤销、SQL 不可变；命令幂等重放不再次执行并确认只存在一个版本、首返/重放 JSON 相同。|
|ProductionBookFinalIT|真实 QA/实际业务流程正式归档，正式册包含原受控证据，生成复用重新核验；失效 QA 签名阻止新生成，旧产物仍返回原字节。|
|既有 EbrRuntimeIT / EbrArchiveIT 定向回归|4 项：完成签名/审核门禁、空重复组、QA 前/失效签名拒绝、旧 PDF/摘要历史。|
|前端|类型检查和生产构建；2 个测试文件共 6 项；继承大 bundle 警告保留。|
|浏览器|Playwright 桌面/移动 8 项：真实回滚捕获数据的目录/原表格、模板映射抽屉、权限拒绝、真实归档 PDF 预览下载同 SHA256、原表单实际填写入口。使用合成 API 路由，属于浏览器隔离证据，不能称为新部署全链路现场验收。Browser 插件技能未提供，因此使用仓库既有 Playwright。|

最终后端定向总计 18 项，0 失败、0 错误、0 跳过；日志 ebr-backend-http-acceptance.log。前端类型/构建通过、模型 6 项通过、合成捕获浏览器 8 项通过。新 JAR 已在本机 DEV 更新、健康 UP；具体部署与 Library 状态见 DEPLOYMENT_AND_DELIVERY.md。

## HTTP 与实际入口修复

按钮实际名称为“打开生产批记录册”，位于批次详情标题栏。原现场403原因是新 book API 未加入精确安全路由；授权HTTP红灯复现403，补齐既有权限后18项全通过，覆盖授权200、拒绝403、归档字节与幂等生成。没有删除权限检查、修改角色或手改业务数据。

新部署实际浏览器1项通过（ebr-browser-live-httpfix.log）：登录→模板列表（真实空表、无错误）→生产批次详情→册入口→旧批说明→原电子批记录页签。未配置/未下达状态说明合法配置与原QA归档入口；旧批不自动套新模板。

## 残留和限制

尚未完成在持久化新部署 HTTP 服务上的新批次目录→录入→表格→整册单次全链路验收；真实后端链路已由 DEV 回滚集成覆盖，浏览器链路使用其合成捕获，不能将两者合并冒充实机全链路。旧批次不会自动获得新册冻结映射，不向真实业务库造演示数据。

源照片 Library 下载 403；用户新发配制照片现在直接可读，未导入历史签字/数值；另一张灌装/包装照片像素核验未完成。目录默认表格是规范的只读映射，照片精确合并格版式不宣称完成。

非 PDF/PNG/JPEG 附件明确拒绝整册生成并保留原件。模板列表当前最多 200 版本，尚非分页接口。清场历史状态投影保守，历史未通过记录可能保留待审核状态；正式受既有 QA 门禁约束，没有擅自忽略历史失败。转换超时/隔离由现有转换器能力决定，当前纯 Java 转换器的强进程超时验收未完成；本轮没有更改并行打印引擎。

ONLYOFFICE/JWT 仍未获密钥配置确认，不造密钥、不关闭认证；未将在线 Word 编辑的未完成项算入已通过。补齐新增 book API 的精确 SecurityConfiguration 路由，复用原权限；未增加角色授权，默认拒绝保留。NotoSansSC 字体 OFL 已随资源保留；PDFBox Apache-2.0，FOP/POI/docx4j 的既有许可证范围不变。

本报告是实施证据，不是人类验收或权威基线切换。


## 2026-10-08 本机 DEV 实际验收补充（16:05）

此前“部署阻塞、尚未发布”的状态已被本节取代。用户后续明确批准本机 DEV 后端部署更新；首次拒绝后仅按主助手提供的明确授权重试一次，部署成功。当前排版候选运行 PID 25628，健康 UP，JAR mes-boot-V036-ebr-book-layout-20261008.jar，SHA256 F6D06A37F694511DF30A0F3474C2A466CF727B426E16DFB0A931F671586C5B25。旧隔离转换包保留回滚。

真实浏览器 admin 已新建并自行发布合成册模板 21（SYNTHETIC_BOOK_UI_20261008）；新建生产订单 139、批次 142（SYNTHETIC_BOOK_BATCH_20261008）。均明确 DEV 合成，不作生产或签名证据。账号自行发布例外仅针对册/打印模板，ProcessService/EbrService 未改审核规则。

下达实际拒绝 WEIGHING_POLICY_REQUIRED：Invalid or missing deployment weighing policy for material 1。批次仍 DRAFT，无执行表单，未保存业务表单、未生成该持久批次 PDF。既有部署生产者要求 mes.production.material-weighing-policies 中精确组织/物料绑定及 required、precision、tolerancePct、policyVersion；没有擅自补容差或放宽门禁。第二同品种规格亦缺独立批准的工艺/eBR基础版本。

下达选择器另有真实缺陷：把版本筛选 status 设为产品状态 ACTIVE。已仅移除此错误条件，仍筛选 APPROVED/EFFECTIVE 版本。回归测试及册前端测试 7 项通过，类型/构建通过；浏览器已能选择匹配的有效工艺。

排版第一轮后端 7 项、浏览器 8 项通过，但逐页复核发现追溯表来源长码仍跨列，继续收紧保留全文的单元格换行预算。最终排版待再次验证。已有 Library v0 PDF 属失败或空数据样例，不能声称完成真实表单保存验收。


# 当前验收状态：实施与回滚验证通过；实际持久流程受上游配置阻碍

2026-10-08。本节为最新事实，后文旧状态仅保留历史。

- 基线：main / 8f61f75；保护并行未提交修改，不提交或推送，不修改既有迁移。
- 本机 DEV 当前后端：PID 30432，JAR mes-boot-V036-ebr-book-titlewrap-20261008.jar，SHA256 AE97A58F930525CE6B991430DF33D2C6DBCB941891CDEFE3A361CF1B70F34428；健康 UP。此前布局、隔离转换包均保留回滚。用户后续已明确批准 DEV 部署更新，首次审批拒绝后按补充授权正常重试获准。
- 真实浏览器：admin 发布合成册模板 21；新建合成生产订单 139、批次 142。批次下达失败，仍 DRAFT。没有表单保存或持久批次 PDF，不声称完整真实业务验收。
- 具体阻碍：WEIGHING_POLICY_REQUIRED / material 1。部署未配置 mes.production.material-weighing-policies 精确组织/物料映射。规则要求 required、precision（当前基本单位）、tolerancePct、policyVersion，禁止按物料类型等推定。需受控部署负责人给定这些值及完整配方物料映射；没有擅自设置 false 或容差。第二同品种规格还缺独立批准的工艺/eBR 基础版本。
- 自行发布例外仅册/打印模板；ProcessService/EbrService 无差异，运行审核、称量核验和 QA 独立签名未放宽。
- 选择器缺陷已修复：移除误用于版本查询的 ACTIVE，仍仅允许 APPROVED/EFFECTIVE。前端相关 7 项通过，类型/构建通过。
- PDF：固定列宽、显式保留全文换行、长标题换行、五列业务表、中文状态和真实单位名称、执行单元区分；内部完整源快照和64字符摘要保留。渲染版本 EBR_BOOK_V4_TITLE_WRAP 避免复用旧错误排版。
- 测试：单元格修复阶段后端 7 项全过；最终标题阶段真实转换 5 项全过（含已安装 LibreOffice），package 成功。16 页长明细样例逐页缩略图复核，表头/页码连续；最终标题版本后端另2项事务回滚测试通过，8页册已重生成并逐页复核；明细和追溯列不重叠、完整哈希保留。回滚测试有真实保存/提交合成字段及不可变旧产物校验，权限上下文和独立身份为测试夹具，不冒充持久业务。
- 最终浏览器回归8项通过（任务专属4176，未干扰占用4174服务），使用真实回滚 JSON/PDF 捕获配合接口路由夹具，验证 UI、同 PDF 下载哈希、拒绝访问和原表入口；与真正持久化 UI 配置发布/订单批次创建证据分开。原始表单截图展示录入前空数据，不能当作真实填表闭环。
- Library：目录截图 libfile_0ed73503344881919c19b012fc780ad3 已更新 v1（视口截图，页头 y=0）。DOCX libfile_b75fa641aa98819195004173c693c603、长明细 PDF libfile_a3e2b44066d4819195fc6975f1a6590e 已更新 v1。失败旧版保留，不新建重复附件。Windows 本地扩展属性写回不受支持，Library 写入成功且返回身份已保存私有 JSON。

---



最终回滚册 Library 已更新同一身份 libfile_5248725d8dc081919b31b43477d32104 至v1；本地PDF SHA256 599C94BA0C829C77BD29C2D38F43251DD2094173CCCBBCA081751E954E362F1E。Library远端原始字节未另作哈希比对。


## 只读组合复核 2026-10-08

只读 JDBC setReadOnly(true) 检查当前DEV实际行，并检查现有配置/启动脚本；未调整物料策略、删除业务行或改冻结快照。

- 持久产品仅 ID1 KCL30（氯化钾溶液，30ml:3g/瓶）及ID2 HYTEA100（合剂，100ml/瓶）；无持久合成产品。
- KCL30生效组合：工艺8/eBR6、工艺9/eBR7、工艺11/eBR9、工艺12/eBR10。eBR10作者11565、批准11568，确有独立批准；其它三个为作者11572、批准11569。HYTEA100工艺2仅DRAFT，无eBR。
- 唯一册模板21已PUBLISHED，映射产品1/工艺12/eBR10。订单139及批次142明确SYNTHETIC_BOOK_*，仍DRAFT；批次process_snapshot_id为空。未撤销或删除。
- 所有已下达快照2、3、4、5、7、8、9、10的has_book=0；无法通过补写不可变快照复用为新册。
- 历史快照9、10是现成称量策略来源：组织1/物料1（KCL，基本单位1=g），required=true，precision=0.001，tolerancePct=0.5，policyVersion=KCL-DEMO-2026，configurationHash=8243be2cf0aa04d8cc5217d3fcce08843d8c9fe5f5109f7b402eba2d7b15545b。此为历史冻结证据，不等于当前部署输入。其它已生效工艺还使用物料5纯化水，需要其独立映射，故工艺12/eBR10是当前最小单物料组合。
- 当前进程只指定local profile及日志路径；根配置、.env、mes-boot application配置、scripts、已知部署脚本未找到 material-weighing-policies 配置。未访问或打印凭证。规则生产者ProductionMaterialPolicyService只读Environment明确绑定，不允许从历史快照或废弃物料列自动推断。

最小业务决定：是否将上述历史 KCL-DEMO-2026 策略完整且原值不变地重新提供为本机DEV受控部署输入，仅用于已标记合成验收的下一步；这是共享真实物料1的策略输入，尚未擅自启用。若否，需给定本次合法受控配置，不能默认required=false。跨规格另需确定一个KCL第二规格产品、其已发布工艺和已生效eBR并由独立授权身份批准，再新建册模板版本绑定；已有唯一KCL规格不足。

只读本地证据：ebr-combinations-readonly.log、ebr-frozen-policies-readonly.log。规则路径：docs/development/incoming-material-weigh-policy-supplement.md、backend/mes-production/src/main/java/com/hospital/mes/production/application/ProductionMaterialPolicyService.java。


## 目录与示例标签复核收尾

仅修两处标签：目录长名称保留开头与执行单元后缀，中间缩略；完整名仍在正文/书签，已验证两条PREPARE的/1与/2可区分。16页第10页正文为“分装包装记录”，与目录一致。后端8项全过（真实FOP/LibreOffice、目录/书签断言、事务回滚），浏览器8项全过，package成功，指定两页已视觉复核。渲染版本EBR_BOOK_V5_TOC_LABEL。

最新DEV PID22852，JAR mes-boot-V036-ebr-book-toclabel-20261008.jar，SHA256 E8D23F85D7CDFC7E710B4C866FE2332C1B9B759AB91A9BE833A089FCE737539F；既有配置原样沿用，称量策略未添加/改变。旧titlewrap包保留回滚。前面的PID/附件版本为历史检查点。

真实持久闭环仍未完成：订单139/批次142不动；最小单规格需要受控确认是否恢复历史组织1/物料1的KCL-DEMO-2026完整称量策略作为当前部署输入（required=true，precision=0.001克，tolerancePct=0.5），不得从历史冻结对象自动启用。跨规格需明确KCL第二规格及独立批准工艺/eBR。
