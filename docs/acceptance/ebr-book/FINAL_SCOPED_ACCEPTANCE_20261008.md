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

# 本次册模板与隔离转换验收补充

2026-10-08；真实仓库 D:/codex/_project/gmp/hospital-pharma-mes，main 8f61f75。保护并行未提交修改，未提交或推送。此补充取代此前报告中转换隔离未完成、Library 全部不可用及最终测试仅18项的过时描述。

用户解除同账号自审自发限制，本次严格限定为 BookTemplateService 的册模板发布及其按钮。仍需原发布权限，已发布版本/映射/hash/审计不可覆盖。工艺/eBR定义独立审核、业务复核和QA/电子签署未放宽。PrintService原本没有作者排除，不做扩大修改。

最终后端47项通过：reporting28、册定义/状态/PDF12、真实DEV回滚集成7；0失败/错误/跳过。日志 ebr-scoped-final-acceptance.log，4分27秒。包含同作者创建和发布册模板、源工艺独立审批签名保持有效、真实DOCX/PDF、超时杀进程、并发拒绝、中断清理、外部URI拒绝、正式/草稿、旧版本原字节重打、权限拒绝。前端类型/构建通过，2文件6测试通过；最新合成数据桌面/移动页面8测试通过。现有大bundle警告保留。

新转换器每任务独立JVM：默认45秒、最多60秒；堆256MB、元空间128MB、直接内存32MB、并行CPU提示1；单任务门闩；输入5MB、输出25MB上限；无继承DB/JWT/JVM注入环境；受控DOCX结构/外部引用白名单、任务临时目录、真实进程终止后清理，清理失败拒绝继续接单。此为进程/资源隔离，不是独立OS身份或完整文件系统/网络沙箱。LibreOffice插件保留。逐文档超时不等于整册总时限。

最终Boot JAR打包成功，SHA256 AA2B468444073C1922F4067B3DDA67A059CEF85C412C4212817B5CC21931A0C9。PropertiesLauncher真实转换检查通过，packaged-worker.pdf 41059字节。第一次独立检查漏设user.home导致字体缓存拒绝访问；补齐实现已有的任务目录参数后通过，无新增代码修复。

持久化浏览器：用户已手动登录admin，实际册模板列表为空，入口正常。尚未创建/发布新册模板、创建跨规格批次、填写/保存、生成和下载新册PDF。不得将回滚集成与合成API页面捕获合并冒称这条链路已通过。当前生产式依赖仅有已批准KCL30模板，第二规格合法工艺/eBR独立批准仍需另行具备。

服务更新阻碍：自动审批拒绝停止并替换8080 DEV后端，理由为原始用户要求不得部署。没有绕过拒绝或替换服务。仍运行 mes-boot-V036-ebr-book-httpfix-20261008.jar，PID32520；新JAR仅位于仓库target，未更新当前后端。请主助手核实是否获得明确的本机DEV服务更新授权，再进行后续持久化验收。

样例均为合成数据：FOP册12页、LibreOffice册3页、实际回滚草稿册8页、实际QA回滚正式册65页；最新全页缩略图已检查总体排版，中文/重复表头/页码可见。65页正式册包含原受控归档证据，不能把其缩略图检查当作每页小字逐项业务审签。桌面/移动截图与下载PDF由最新回滚记录生成。

Library官方创建已成功保存示例DOCX：libfile_b75fa641aa98819195004173c693c603，file_000000008b0481f5b2a0c771fc394efa。当前官方批量创建可用；旧prepared helper不可用不代表Library不可用。Windows本地xattr助手不支持os.setxattr，远端文件创建成功但本地元数据写入失败，身份保存在私有JSON；不上传数据库备份、凭据或真实业务截图。

未安装新软件、生成密钥、修改安全设置、新增角色权限、改冻结迁移、另开Redis或第二测试库。DEV测试使用已有库事务回滚。未恢复整库备份或清理真实数据。本报告为实施证据，等待设计负责人复核，不能标为完整持久化或人工最终验收。


## 2026-10-08 本机 DEV 实际验收补充（16:05）

此前“部署阻塞、尚未发布”的状态已被本节取代。用户后续明确批准本机 DEV 后端部署更新；首次拒绝后仅按主助手提供的明确授权重试一次，部署成功。当前排版候选运行 PID 25628，健康 UP，JAR mes-boot-V036-ebr-book-layout-20261008.jar，SHA256 F6D06A37F694511DF30A0F3474C2A466CF727B426E16DFB0A931F671586C5B25。旧隔离转换包保留回滚。

真实浏览器 admin 已新建并自行发布合成册模板 21（SYNTHETIC_BOOK_UI_20261008）；新建生产订单 139、批次 142（SYNTHETIC_BOOK_BATCH_20261008）。均明确 DEV 合成，不作生产或签名证据。账号自行发布例外仅针对册/打印模板，ProcessService/EbrService 未改审核规则。

下达实际拒绝 WEIGHING_POLICY_REQUIRED：Invalid or missing deployment weighing policy for material 1。批次仍 DRAFT，无执行表单，未保存业务表单、未生成该持久批次 PDF。既有部署生产者要求 mes.production.material-weighing-policies 中精确组织/物料绑定及 required、precision、tolerancePct、policyVersion；没有擅自补容差或放宽门禁。第二同品种规格亦缺独立批准的工艺/eBR基础版本。

下达选择器另有真实缺陷：把版本筛选 status 设为产品状态 ACTIVE。已仅移除此错误条件，仍筛选 APPROVED/EFFECTIVE 版本。回归测试及册前端测试 7 项通过，类型/构建通过；浏览器已能选择匹配的有效工艺。

排版第一轮后端 7 项、浏览器 8 项通过，但逐页复核发现追溯表来源长码仍跨列，继续收紧保留全文的单元格换行预算。最终排版待再次验证。已有 Library v0 PDF 属失败或空数据样例，不能声称完成真实表单保存验收。


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
