# 本机部署与交付 — 2026-10-08

已完成授权的本机 DEV 后端更新。原监听 PID21896 已按命令行确认其为 V034 JAR 后停止；仅更新该后端，新监听 PID20084。

新文件：D:/codex/_project/gmp/_mes_deployments/mes-boot-V036-ebr-book-20261008.jar

SHA256：FEEAD00FF69542E9D3030889A3BBF90DC66FA496B3B2C1ACF69EB3FAA6FEE0C9

2026-10-08 14:11:59 +08:00 启动成功，localhost8080 /actuator/health 返回 UP。真实 /v3/api-docs 包含六种 book 路径；未认证模板和批次记录册 GET 均返回401。启动时 Flyway 成功校验36项，schema036 无新待执行迁移。

只读 DEV 核对：V035/V036 success=1；原 ebr_template_version 4、prd_main_batch 12，与迁移前一致；新增 book_templates 0、book_pdf 0，合成测试没有持久业务记录。旧批次继续 legacy 行为；不能用空新表声称新册的真实持久浏览器链路已完成。

数据库3306 PID4608、Redis6379 PID10344、前端5173 PID24428及4174 PID21388未重启。前端 HMR 复用已有服务，生产构建完成，没有新服务器、Docker、密钥或权限设置。

回退文件：D:/codex/_project/gmp/_mes_deployments/mes-boot-V034-securityfix-20261008-1234.jar，原配置/.env保留。需要回退时核实20084仍归属本次JAR，停止该后端，再用同一repo工作目录和local profile启动保留JAR；数据库只允许前向兼容，不删除新表、回写迁移或Flyway修复。

入口：

- http://localhost:5173/ebr/book-templates
- 从 http://localhost:5173/production/batches 进入某批次的“批记录册”，路由 /production/batches/{真实ID}/book。
- 原 eBR 填写/复核仍进入既有实际表单，册页只读映射。

最终验收：后端18项（含真实HTTP权限）全部通过；前端6项、类型与构建通过；合成捕获桌面/移动浏览器8项全部通过；新部署真实只读浏览器1项通过。持久化新册完整生成链路仍未冒称完成。

合成文件目录：C:/Users/Administrator/Documents/Codex/2026-10-08/task/ebr-evidence。DOCX、FOP12页/LibreOffice3页、实际回滚草稿8页及实际QA正式PDF、桌面/移动页面截图均已生成。只有合成交付文件允许保存 Library；数据库备份、真实业务页面、认证材料和私有传输文件均排除。Library保存失败：使用当前技能提供的完整批量保存流程，本机执行通道报告所需 prepare_uploads 不可用；没有附件 ID，未改用绕过流程、未猜测共享链接、未上传真实数据。所有合成交付文件保留上述本机目录。

没有提交、推送、对外部署或基线自动验收。


## 最终 HTTP 修复部署

2026-10-08 14:43:31 +08:00：停止已核实归属的旧V036 PID20084，启动最终 PID32520。

最终 JAR：D:/codex/_project/gmp/_mes_deployments/mes-boot-V036-ebr-book-httpfix-20261008.jar

SHA256：F25762A6137C06B759369CDE8A01264323E7C5F72F567802AA7322A3F041861F

健康UP，36项迁移校验通过，schema036且无新迁移。精确安全路由补齐新册API已有权限，默认拒绝及各业务来源权限保留。原V036和V034 JAR均未覆盖，回退须先核实PID32520仍属于本次最终JAR；不回写迁移或删除新表。

实际4174浏览器已通过模板列表→批详情“打开生产批记录册”→旧册提示→原电子批记录页签，五项原权限存在且页面无错误。详见ebr-browser-live-httpfix.log；后端ebr-backend-http-acceptance.log 18项通过。没有持久化演示模板/批次，也没有绕过旧批冻结限制。


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


## 目录与示例标签复核收尾

仅修两处标签：目录长名称保留开头与执行单元后缀，中间缩略；完整名仍在正文/书签，已验证两条PREPARE的/1与/2可区分。16页第10页正文为“分装包装记录”，与目录一致。后端8项全过（真实FOP/LibreOffice、目录/书签断言、事务回滚），浏览器8项全过，package成功，指定两页已视觉复核。渲染版本EBR_BOOK_V5_TOC_LABEL。

最新DEV PID22852，JAR mes-boot-V036-ebr-book-toclabel-20261008.jar，SHA256 E8D23F85D7CDFC7E710B4C866FE2332C1B9B759AB91A9BE833A089FCE737539F；既有配置原样沿用，称量策略未添加/改变。旧titlewrap包保留回滚。前面的PID/附件版本为历史检查点。

真实持久闭环仍未完成：订单139/批次142不动；最小单规格需要受控确认是否恢复历史组织1/物料1的KCL-DEMO-2026完整称量策略作为当前部署输入（required=true，precision=0.001克，tolerancePct=0.5），不得从历史冻结对象自动启用。跨规格需明确KCL第二规格及独立批准工艺/eBR。
