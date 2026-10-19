# 业务命名清理：本轮执行记录

**BLOCKED / 2 CURRENT NAMES SAVED / NOT ACCEPTED / NOT ZERO.** 授权及范围见 [执行锁](../development/decisions/BUSINESS_NAMING_CLEANUP_AUTHORIZATION_20261012.md)。本轮只使用PC-202401011703、DEV137表、原main checkout和既有8080/5173，最高V059，未迁移、重启、提交或推送。保留菜单、角色、质量和角色ID既有并行修改，未重做已验收打印功能或角色停修项。

## 已执行与恢复证据

新完整备份：`C:\Users\Administrator\AppData\Local\Temp\mes-business-naming-20261012\20261012T041649Z\hospital_pharma_mes_dev-before-naming-20261012T041649Z.sql`，6,065,025B，SHA256 `919a2023b8a57ab0858ca8150ea91125aac883f5c49586279e1388adbb1638d7`，137表4249行。native dump exit0；完整SQL重建、每个存储字节、SHOW CREATE和触发器一致。未实际导入/新增库；原始全字段快照、API计划、错误和SQL均留本机Temp，不提交Git。

扫描全部存储文本字段及17份打印DOCX XML/JSON正文，初始188处候选；逐值排除98处正常控制枚举TEST_RESULT_REVISION/TEST_SAMPLE/TEST_COMPLETED/IN_TEST后，90处当前业务显示或标识命中。另1459处技术/不可变审计证据命中独立分类；技术permission/route/JSON键不全局替换。PDF归档正文未做完整文本扫描，不能宣称整个数据库无命中。

仅实际编辑2条当前数据源：

| 原ID | 原名称 → 当前名称 | 正常命令结果 |
| --- | --- | --- |
| 19 | 收货单打印数据（DEV专项验收）→ 收货单打印数据 | Chrome真实编辑/保存草稿；version4→5，新增DRAFT33；published22保留 |
| 21 | 双类型与CRUD（DEV专项验收）→ 收货验收记录数据 | Chrome真实编辑/保存草稿；version9→10，新增DRAFT34；published26保留 |

所有其他配置值相等；原发布修订22/24/26逐字节不变，绑定ID及关系完全相等，没有发布/重绑/清除字段。原17模板、10打印artifact、9批归档、50签名、菜单58、权限209、成员9、角色权限353、字典29、产品2、物料16均完整单元格相等。普通登录审计及用户登录维护属于正常认证，没有改凭据或授权。

真实Chrome列表回读及截图：[sources-after.png](evidence/business-naming-20261012/sources-after.png)。名称已变，但截图明确仍显示两项“DEV测试”单据类型，不遮掩未清理字段。拟改新业务值经现有check_business_seed_payload.py核验PASS；未执行任何种子/import。

## 打印模板/菜单明细及可决策建议

| 原记录 | 命中与拟规范文本 | 当前阻塞/引用 |
| --- | --- | --- |
| 打印7/8/9/10/13/14 | TEST_CANVAS…、测试专用Canvas Word导入字段绑定；业务标题建议“检验报告打印模板” | 当前正常编辑/上传会INSERT新版本，没有原ID改名/删除命令。组内同用途重复记录不能用数字后缀掩饰；需明确保留/处置范围，未写入 |
| 打印29/30/32/40/43 | TEST_BATCH…_DOCUMENT、测试专用收货单据；建议“收货单打印模板” | 同上；30/32/40/43分别引用2个artifact、1个batch；29未曾发布但仍无原ID修改命令 |
| 打印31/33/41/44 | TEST_BATCH…_LIST、测试专用收货列表；建议“收货记录汇总模板” | 每条有1个batch；不是无差别删除归档 |
| 打印42 | TEST_QC_BATCH…、测试专用检验单据；建议“检验报告打印模板” | 原artifact1/batch1，需保留受控证据 |
| 打印68 | 收货记录打印（DEV专项验收）；DOCX正文“收货单（DEV专项验收）”；建议当前业务文本“收货记录打印”/“收货单” | PUBLISHED，artifact1及当前绑定1；不覆盖冻结名称/正文/hash |
| 菜单100 | TEST_MENU_1791624207、验收测试菜单…；拟名称“菜单配置管理” | INACTIVE，route/admin/menus、iam:menu:view及ID保留。当前API无menuCode改码能力；名称可编辑但本轮批处理未成功到达该项。当前角色引用0 |
| 菜单101 | TEST_MENU_1791624503、验收测试菜单…；拟名称“菜单功能权限管理” | 同上，当前角色引用0 |

模板10/30/31/32/33/40/41/42/43/44虽INACTIVE但published_at非空，属于已发布历史；不能将停用等同可篡改。模板7为DRAFT，8/9/13/14/29为VALIDATED且未发布。精确FK引用见template-dependency-readonly.json；物理删除、历史处置或新原ID编辑命令需单独明确确认/设计，不自动执行。

## API执行失败与两轮上限

官方CUAWright缺少chromium_headless_shell-1243可执行文件，错误保留。没有下载安装或改全局适配器。Browser Use首次请求头策略加载失败；按工具提示重试后恢复，真实Chrome既有admin会话可用，已正常保存两条源。工具审批均成功，**无自动审批拒绝**；没有读取被拒绝的凭据目录或终止其他浏览器。官方驱动结束写模式false；回退Chrome使用逐动作权限，不修改全局写模式。

13项常规API编辑计划（3字典显示名、2菜单名称、8权限显示名）在读计划/首个编辑处失败，实际完成0：

1. 首次默认Python缺win32cred，导入阶段0写入；改用本机已有CUAWright venv是运行环境选择，无源代码修复。
2. 自动修复1：只将字典GET实际数组适配为数组/分页对象兼容；原保护断言不改。
3. 自动修复2：只按控制器实际/api/v1/menus和/permissions修正误写/admin前缀。先前错误/admin/menus/100返回403(default deny)，该动作未执行；使用正式契约路径，不修改授权、不扩大权限。
4. 修正后首个PUT /dictionaries/1/items/1返回400 INVALID_INPUT，0项完成。原因已由IamRequestVersion.java物理核实：要求带引号强ETag，脚本发If-Match `0`而非`"0"`。这不是数据不可编辑或权限不足；属于执行脚本错误。**第三轮未修、不换脚本/会话重跑，不绕过失败项。**

MES_DEV_RULES.md §6A：“每个任务最多执行2轮自动代码修复……达到上限仍不通过时立即STOP: BLOCKED，不得重置计数、换测试名称或重新启动代理绕过限制。”本轮需要明确有界追加执行修正才可更改If-Match并继续原13项；已备好每条原值/拟值/版本，不能因此自动恢复。

## 剩余与生成入口

逐个原命中业务字段SQL回读：**90处中2处已纠正，88处仍未清理**（不含技术/证据分类）。分布：打印模板34、冻结数据源修订3、设备3、资格13、部门编码12、字典4、菜单4、权限显示名8、登录名7。DOCX全字节保持且命中仍在；新DRAFT33/34名称干净。完整清单见remaining-original-hits.json。

物理核查生成入口：RealisticIntegrationSeedIT仍写MES_DEMO_GMP并使用现有demo.*账号引用，且原因含“集成测试”；RealisticProductionFinishedSeedIT仍写MES_DEMO_GMP/DEMO_SCALE；DemoRoleQualificationSeedIT仍包含MES_DEMO_*资格编码/现有demo.*账号查询。技术类/包/文件名不改。资格与设备代码还关联既有执行策略，登录名无获批改码API；不能仅替换常量令资格失效或生成虚假资格。本轮未执行/修正这些入口；按AGENTS新规则禁止再运行，自动前置门禁接线尚未实施。原源文件及其它并行修改均保留，不宣称防再生成工程已完成。

证据目录：docs/review/evidence/business-naming-20261012。日志：business-naming-initial-audit-20261012.log；business-naming-browser-launch-error.txt；business-naming-normal-api-venv-20261012.log；business-naming-normal-api-round1-20261012.log；business-naming-normal-api-round2-20261012.log。备份和私有原始数据不提交；没有全库替换、删除、增权、签名、角色/成品链续跑。


## 最新规则更新及第3轮审批阻塞

全项目规则现为10轮，覆盖旧2/中间5；历史报告原事实保留。原命名任务2轮不清零，第3轮仅强ETag引号已按现有契约修正。执行13条实时API名称编辑的工具启动被自动审批拒绝，原因是它只认可直接用户DEV物料清理，不认可委派历史中的命名写入授权；进程未创建、零新业务写入。未以浏览器/其它脚本/默认权限绕过。直接人类确认问题已提出，批准前暂停实时写入。本轮规则文件修改获审查批准，实际有效引用见AUTOMATIC_REPAIR_LIMIT_20261012.json及round3-approval-block.json。17模板、生成入口及其它暂停任务未执行。


## 最新第3/4轮恢复结果（取代上述暂停状态，不删除失败历史）

补交用户Sentinel_b4f1a264adcc819180723a94e29e55cc“执行”等明确命名清理指令后，同一被拒工具调用只重试一次并获批，没有换执行路径。第3轮已执行13项名称编辑，13次HTTP200、13次独立GET、其余响应字段保留全部通过：菜单100/101、字典1/48/49、权限显示名105/106/107/108/123/124/251/252。累计实际业务名称变更15处。权限代码、类型、状态、成员及授予不变。normal-api-edit-results.json记录每项版本/幂等键/新值，私有完整前后响应留本机。

只读全表保护脚本首次把验证记录当作直接source_id，实际列source_revision_id。第4轮只改为验证→修订→数据源的真实外键归属；保护断言未放宽。随后137表全存储单元格复核PASS，只有精确15条当前名称、正常维护元数据、源草稿33/34、正常校验证明失效和新增审计/幂等/登录事件发生预期变化。50签名、17模板、10artifact、9batch、所有旧发布源修订、授权/成员/绑定和其余保留数据不变。当前已用4/10轮，未清零；审批重试不算代码修复。

最新逐项原命中SQL复读：75仍在，15已改；部门仍12原编码命中，没有把其它对话的部门工作冒认为本任务成果。分布见latest-remaining-original-hits.json。Chrome回看菜单100时两次请求头策略加载失败，新增13项浏览器验收NOT RUN，不能用API/nativePASS替代。前轮真实源列表截图保留为前轮证据。审批拒绝已由同调用单次获批执行解除；不是当前待批准阻塞。

三个已识别持久化Java种子入口新增现有tools/check_business_seed_payload.py调用，位于各持久化seed方法第一条语句；RealisticIntegrationSeedIT业务原因中的“集成测试”改为“建立入厂检验质量标准”。没有改断言/skip/资格政策/现有数据。资格和设备旧码仍与现有引用关联，不能只改发射常量使资格失效，所以门禁先fail closed，等待成组规范化决定。已知三组码载荷checker均exit1（预期拒绝），test-compilePASS10.290s；没有运行任何种子或创建资格/签名事实。Java独立探针因Windows JShell preferences注册表拒绝未完成，已仅结束自身探针，不提升注册表权限；运行器完整验收不宣称PASS。该门禁覆盖已识别的三个持久化入口，不宣称所有生成脚本已全量规范化。

本轮没有迁移、发布模板/源、物理删除、后台部署、角色/成品任务续跑或提交推送；backendhealth200UP。当前是CURRENT EDITABLE NAMES15 PASS / REMAINING75 NEED BOUNDED DECISION / CHROME AND FULL GENERATOR COVERAGE INCOMPLETE / NOT ACCEPTED。

### 17条模板：受限分组与最小可执行路径

- **6条未发布：7 DRAFT；8/9/13/14/29 VALIDATED。** 既有POST编辑/上传始终新建版本，无原ID名称/编码改写命令。不能把创建新行当作清除旧6行；VALIDATED预览也需要重新验证。保留ID而修正它们，需要用户指定设计者明确受控纠正能力/一次性维护边界，不能自行新建接口或改状态规则。
- **10条已发布后停用：10/30/31/32/33/40/41/42/43/44。** published_at存在；其中10有历史绑定，其余关联artifact/batch/绑定。保留原文和摘要，不能以INACTIVE绕过冻结。没有物理删除。
- **1条当前已发布：68。** 名称及DOCX正文含DEV专项验收，artifact1、当前绑定1。当前正常流程可另建使用自然业务编码/名称和“收货单”正文的干净草稿，用原已批准编辑器/字段/源，后续正常PDF校验、发布及切换绑定；这是现有能力，不需要重做打印功能。但创建/发布/绑定替换不在本次名称编辑授权内，必须明确下一阶段动作范围。旧68和归档继续保留，不能承诺所有历史字符串清零。

**可实施最小下一阶段**：由用户指定设计者/数据负责人选定各真实打印用途需要保留的版本，确认自然业务编码与名称；明确是否批准正常干净草稿→校验→发布→绑定替换。16个旧用途重复/停用版本与全部归档采取何种历史处置需单独决定。若要求数据库全部历史业务字符串也绝对清零，则与当前不可变历史要求存在真实冲突，需要明确有界变更决定，不能隐藏列表或无差别删除。原ID更新、模板/归档删除、签名或授权均未自动实施。
