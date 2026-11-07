**2026-11-04用户否定当前正式交付：IN PROGRESS / REWORK_REQUIRED，累计81不变。** 已找到并实际查看用户识别的三张历史mes-demo原图，确认正式实现省略路线箭头、左工序角色/配置标记、页签计数，替换角色/清场控件并改变记录表单层级，原型保真不合格。先前“已按原型接入”的评价撤回；通过的技术验证事实不等于设计交付合格。本次仅来源核对与缺陷登记，无源码/业务/DB/测试/门槛修改；停止碎片补丁。后续以已识别原图及用户后续IPC规则还原正式页，历史数据不改。详见[保真返工记录](docs/development/process-form-cpp-ebr/20261101-final/today-closeout/DESIGN_FIDELITY_REWORK_20261104.md)。以下保留前阶段事实。

**17:36屏幕返工准备：仍REWORK_REQUIRED / 累计81。** 新图libfile_640a4c026510819185ddf70bea308249（file_000000000ae481fdbdebace9da5b6ac1）本机Library能力不可用，未物化/未亲看，父方观察单独标注。已定位正式ProcessDetailView.vue第96行说明、第103行重复卡片、第108行所属模板；保留所属模板，字段数列已有，旧“未保留”判断已纠正。历史原图及本机旧正式截图实际重看，八组知识查询退出0；没有继续代码/测试/服务/DB操作。已形成[最小合并草案](docs/development/process-form-cpp-ebr/20261101-final/screen-rework-1736/MINIMAL_MERGED_PLAN.md)及[部分审查与图片状态](docs/development/process-form-cpp-ebr/20261101-final/screen-rework-1736/SCREEN_REWORK_REVIEW.md)，建议30分钟1批81→82尚未批准/启动；不按旧58重开，不以无新图阻塞源码/历史核对，也不冒充完整审查通过。

**2026-11-04正式接入前阶段记录，累计81。** 真实保存及测试事实见[实施与证据](docs/development/process-form-cpp-ebr/20261101-final/today-closeout/FORMAL_INTEGRATION_PROGRESS.md)，设计评价受上方最新返工结论覆盖；完整G5未通过，无ACCEPTED，未Git发布。

**2026-11-04今日集中收尾：IN PROGRESS / NOT READY，累计69。** 按用户“今天全部收尾、不反复确认”执行[集中授权](docs/development/decisions/PROCESS_TODAY_CLOSEOUT_20261104.md)。67集中修正空态、分页、配置分区语义、字段名称/可访问状态与文字对比度；68补空表提示继承色；69修实际聚焦输入被固定底栏遮挡。原32/32不改断言；共享11+7共18/18；两边typecheck及实际workspace构建通过。原型37项工艺交互、31项焦点恢复、12项分页/焦点、25项明确CPP双侧范围保存回读检查通过。81格首遍及第69批81格对抗二遍均逐张原尺寸复核；648标准标签配对无错行，81状态无整页横向溢出。清单校验PASS仅表示IN PROGRESS证据结构完整，不是完整UI/G2/G5通过；19条WCAG全表面审查仍有NOT RUN。原生200%检查被Computer Use因无法可靠识别窗口网址终止，已停止原生窗口操作，未改工具策略。Planning已成功同步revision65。整页原型具体版本尚未人工冻结，新版整页未正式接入5173/8080；无DB、迁移、历史数据或Git发布。[本轮实际交付与剩余项](docs/development/process-form-cpp-ebr/20261101-final/today-closeout/CLOSEOUT_RESULT.md)，[SELF-REVIEW记录](docs/development/process-form-cpp-ebr/20261101-final/today-closeout/review-notes.md)。

**2026-11-04第66批：单批两项收尾完成，整体IN PROGRESS / NOT READY。** 原生隐藏label/useId已让真实字号input计算名称准确为“字号”，三电脑宽度可编辑/只读/键盘19项通过、6图复核，工具栏78px保持；历史校验器保留完整字节比对并仅逆变换已批准PC_ONLY区块，69/69通过。原32/32且四文件SHA不变，共享原5+新增1共6/6、两边typecheck、workspace构建通过。四个已知HIGH定向关闭；完整81格视觉/几何/对抗二遍及专项WCAG仍未闭合，当前清单FAIL，整页未冻结/G3.5/G5未通过。累计66，未67；无DB/迁移/历史/服务/CI/Git操作。[本批结果](docs/development/process-form-cpp-ebr/20261101-final/WORKSPACE_ROUND66_RESULT_20261104.md)。

**2026-11-04第62–65批：IN PROGRESS / NOT READY，累计65。** 用户批准完整剩余方案后，路线长名、原型链接三态、设计器背景焦点隔离/循环/恢复已在三电脑宽度复验；原32项、共享5项、两边typecheck、workspace构建与37+31项浏览器检查通过，原四测试SHA不变。PC_ONLY新7项通过，历史校验器61/62（旧唯一字节差异合同冲突）保留；字号外层aria-label未传实际input，Chrome计算名称仍空，同签名停止追加修补。重新捕获81格不等于完整视觉审查，当前manifest仍FAIL；旧债务/完整WCAG及G2/G3.5/G5未闭合。四批已用，未66；仅字号单点正式源码，DB/迁移/历史/服务/CI/Git未改。[结果与证据](docs/development/process-form-cpp-ebr/20261101-final/WORKSPACE_ROUND62_65_RESULT_20261104.md)，[待批准单批收尾](docs/development/decisions/PROCESS_WORKSPACE_NAME_CLOSEOUT_20261104_PROPOSED.md)。

**2026-11-04第60–61批：IN PROGRESS / 获批可用性修正已验证，整体NOT READY。** 12:04:48–12:44:48批准窗口内两批已用：表单固定操作列、空计数/新建标题、V2三态/禁用及包装层画布滚动已修；第61批修正scoped选择器编译问题。原4测试文件hash不变，32/32、typecheck及实际workspace构建通过；真实5174有37项显式检查通过、无pageerror/越界HTTP。当前81格全部选图两遍SELF-REVIEW（15动画帧保留并稳定补拍），390标准配对无错行；完整manifest仍FAIL：当前四项HIGH：900长路线名称叠压、设计器Tab聚焦被遮住底层、14px链接对比4.104:1、共享字号下拉无计算名称，部分WCAG未闭合，原校验器仍强制375与已批准电脑范围冲突。无第62批、正式源码/DB/历史/迁移/服务/CI/Git修改；G2/G3.5/G5未通过，整页确切版本未冻结。详见[第60–61批结果](docs/development/process-form-cpp-ebr/20261101-final/WORKSPACE_ROUND60_61_RESULT_20261104.md)与[剩余缺陷及电脑适用性方案](docs/development/decisions/PROCESS_WORKSPACE_FINAL_GAPS_20261104_PROPOSED.md)。

**2026-11-04第59批同窗口续查：IN PROGRESS / 原型可用性收尾待追加范围批准。** 同一组件SHA不变，新增33张原图首遍，当前69/81、余12格；查询/重置、模板必填取消、只读查看、新建校验、未保存主工艺保护已实际操作，完整审查validator仍退出1。已定位空结果1–0、新建标题仍编辑，已有主色/禁用/表单操作列及PC画布问题继续开放。[续查报告](docs/development/process-form-cpp-ebr/20261101-final/WORKSPACE_ROUND59_CONTINUATION_20261104.md)与[具体收尾方案](docs/development/decisions/PROCESS_WORKSPACE_USABILITY_20261104_PROPOSED.md)已保存；方案拟40分钟/2批60–61，仅原型两个组件及必要验证，不改正式历史/原断言，等待明确追加。累计59，未60；原11:42:49截止不重置，无正式源码/DB/迁移/服务/CI/Git操作。

**2026-11-04第59批最新澄清与实施：IN PROGRESS / 有界原型功能修正完成，整体NOT READY。** 用户确认“统一新增，工序内选择生产/IPC类型”，覆盖下文取消类型入口的历史解释。[最新批准](docs/development/decisions/PROCESS_INSPECTION_OWNERSHIP_REPAIR_20261104_APPROVED.md)已落实：统一新增、类型内选、六工序样例、OP04前置OP03、2104表单归OP03，正式历史/类型字段不变。原29项断言不改，追加3项后32/32、typecheck与实际workspace构建通过；真实5174验证类型/检验保存重开、取消保值、表单改名保存重开及停用查看。36张新原图首遍，当前81格缺45格；完整审查validator退出1，几何/键盘/恢复/二遍及已知色彩/操作列/画布问题仍开放。累计59，60–61未用，无正式源码/DB/迁移/服务切换/CI/Git发布。组件SHA8cf5bd4d36a4532b044fa458343b1b2fbf7a8f616ddfe9503d7ff9ef23f10005；确切整页原型未冻结，G2/G3.5/G5未通过。详见[第59批结果](docs/development/process-form-cpp-ebr/20261101-final/WORKSPACE_ROUND59_RESULT_20261104.md)。

**2026-11-04检验归属纠正：IN PROGRESS / 代码修正待有界方案批准，累计58不变。** 用户最新明确“没有IPC这个过程”，已固化[需求与旧合同冲突](docs/development/decisions/PROCESS_INSPECTION_OWNERSHIP_20261104.md)，不得继续按“IPC只是可选工序类型”开发。检验定义/结果/复核属于真实工序，与独立IPC路线节点不同；旧DCP类型批准、正式字段及历史证据保留待受控处理。已准备[具体修正方案](docs/development/decisions/PROCESS_INSPECTION_OWNERSHIP_REPAIR_20261104_PROPOSED.md)：原型取消独立IPC步骤/类型入口，样例检验表单2104拟归属OP03，保留附属检验及兼容元数据，明确原型测试合同纠正，最多40分钟/3批59–61。方案尚未批准/实施，业务方向无需再问；本次仅文档及原Planning，无代码/测试/浏览器/DB/迁移/服务/Git操作。原型SHA仍b931675c4a6c93c0258965dcf73573602cd90d4e8f41322a2fb9c338962d0e69，G2/G3.5/G5未通过，手机撤出与电脑75格/缺27格事实保留。

**2026-11-04电脑端范围更正：IN PROGRESS / NOT READY，累计58不变。** 用户明确“手机列表不需要”“没有手机端”，已记录[本任务范围补充](docs/development/decisions/PROCESS_PC_ONLY_SCOPE_20261104_APPROVED.md)。旧100格逐格映射：375手机25格撤出（原有16、原缺9）；电脑1440/1280/900保留75格，原有48格、仍缺27格。“有截图”不是验收通过。WS58-LIST-MOBILE及WS58-CANVAS-NARROW的手机部分为用户撤出，非修复/非PASS；PC主色、空计数、禁用表现和键盘/恢复/几何/二遍等仍未闭合。历史100/64/36、95个证据文件及validator FAIL保留。本次仅文档和原Planning，无代码/测试/DB/迁移/服务/Git操作、无第59批或窗口重置。详见[适用性核对](docs/development/process-form-cpp-ebr/20261101-final/PROCESS_PC_SCOPE_RECONCILIATION_20261104.md)。G2/G3.5/G5未通过，确切原型版本未批准；下一代码修复仍需有界续修授权，手机范围无需再确认。

**2026-11-04第56–58批：IN PROGRESS / 原型定向修正通过，完整审查FAIL，三批已用。** 获批09:26:01–10:06:01窗口执行56类型/计数/启停分离、57装配及唯一说明必填断言例外、58历史NULL点击/停用入口/空态图标/标题锚点修正；29/29、typecheck和默认/实际工作区构建通过。25条原型观察含纯生产路线和模板保存重开；当前64/100原图首遍查看，validator退出1，36格及几何/键盘/恢复/二遍缺失，手机列表和V2色彩有待修HIGH。没有第59批、正式集成、DB/迁移/CI/提交推送合并。准确结果见[第58批报告](docs/development/process-form-cpp-ebr/20261101-final/WORKSPACE_ROUND58_RESULT_20261104.md)。G2/G3.5/G5未通过，确切原型版本未代签。

**2026-11-04生产工艺续作核对：IN PROGRESS / 原型收尾范围待明确批准。** 实读当前源码确认历史空类型映射、第二工艺计数与空路线、启停/配置状态三项差异；说明可空的业务批准保留，旧说明必填断言尚未授权修改。已形成[一次性原型收尾方案](docs/development/decisions/PROCESS_WORKSPACE_CLOSEOUT_20261104_PROPOSED.md)，拟40分钟/最多3批56–58；当前累计55，未56，无代码/测试/DB/迁移修改。原Planning恢复revision53后已补同步54–55及测速/本次核对，成功revision54；G3.5/G5未通过。

**2026-11-04正式页面有限测速：正式5173/8080已启动并保留，整体任务状态不变。** 显式关闭Flyway/SQL初始化/bootstrap，正常登录200；工艺列表/577只读详情/物料列表各3次重复打开与3次SPA切换，主接口处理时间已记录。浏览器精确资源/渲染计时不可用，工具往返不作页面加载值；无业务保存、源码/config/DB/迁移/CI修改。累计55，原G3.5/G5及IN PROGRESS / NOT READY保持。见[测速证据](docs/development/process-form-cpp-ebr/20261101-final/FORMAL_PAGE_SPEED_20261104.md)。

**2026-11-03第54–55批：IN PROGRESS / 原型关联修复及改码回显已验证，整体未验收。** 原批准窗口22:40:54–23:10:54保持；两批已用，累计55。第55批修复重复/空编码拒绝后输入仍显示错误值的问题，新增直接检查后8/8通过、typecheck/build通过；真实5174已核对重复编码回显恢复及有效改码去空白。续查补充只读设计器、模板取消、复制参数独立性及说明可空证据；原21项20通过1失败（旧说明必填断言与批准可空冲突）保留，未改原断言。完整UI审查仍FAIL，正式MES/DB/迁移/CI未改，未提交推送合并。Planning传输错误后同步结果未知。见[第54–55批结果与交接](docs/development/process-form-cpp-ebr/20261101-final/WORKSPACE_ROUND54_RESULT_20261103.md)。

**2026-11-03第51–53批：IN PROGRESS / 工作区原型已改并运行，尚未达到交接条件。** 用户批准完整收尾方案后，保留路线带/左工序/右配置，接入当前定义保存/取消、多前置保留、IPC与参数弹窗、安全完成规则及现有Canvas设计器。53批typecheck/build、新增7项合同检查通过；原7项因截取到defineExpose在断言前报错，原断言未改。真实5174已进入IPC弹窗和设计器，三图局部查看；模板样本计数/实际定义、单位ID、复制子项、完整状态及审查仍有缺口。累计53，三批已用，不自动54；正式MES/DB/迁移未改，G3.5/G5未通过。见[本轮结果与剩余范围](docs/development/process-form-cpp-ebr/20261101-final/WORKSPACE_ROUND53_RESULT_20261103.md)。

**2026-11-03生产工艺主线复核：IN PROGRESS / 整页工作区尚未交付，完整收尾候选待确认。** 已重新实读 mes-demo 原型、正式页及保存/IPC/执行合同，形成[逐项交付核对](docs/development/process-form-cpp-ebr/20261101-final/WORKSPACE_DELIVERY_MAP_20261103.md)：正式页仍为全局四页签，旧原型还含未获准工艺版本审批、未持久化的名称/说明和自动线性改写依赖等冲突；工序类型字段已完成，不重复列为缺失。已准备[完整收尾方案](docs/development/decisions/PROCESS_WORKSPACE_COMPLETION_20261103_PROPOSED.md)，请求一次确认当前定义/真实名称说明/保留显式依赖及40分钟最多3批原型收拢边界。候选未批准，未改业务源码/原型/测试/DB/迁移、未运行新测试；累计50保留，无第51批，G3.5/整体G5尚未通过，原CI阻断保留。

**2026-11-03第50批远端结果：IN PROGRESS / PR #7已推送并转草稿 / CI FAIL未合并。** 1682fc5仅本次21文件。远端51文件223/223通过、回调4/4通过，仓库合同通过；前端随后因main的QC页面/API/模型提交闭包缺失而typecheck失败，两个Maven字体安装检查已找到Noto SC但原4个中文PDF断言仍失败，未进入DB集成。累计50，原PDF同签名复现已停止，不第51批、不混入其他QC工作、不绕过CI。原Planning恢复并追加同步revision50。见[远端结果与停止记录](docs/development/process-form-cpp-ebr/20261101-final/ROUND50_REMOTE_CI_RESULT_20261103.md)，生产工艺整体仍未验收。

**2026-11-03第50批：IN PROGRESS / CI修复本机验证通过并送审 / 生产工艺整体未验收。** 已批准单包follow-redirects1.16.2，Axios与其他123生产锁包不变；原55文件234/234、类型/构建、原生产审计0漏洞、回调4/4及仓库/CI正负策略通过。此前已批准JSDOM30.1.3及9参回调、字体/worker修正一并送原四CI，不冒称Linux字体或数据库集成在本机通过。无DB写入/迁移，其他任务/本机配置不提交；累计50，不自动51。原窗口过期及最新具体批准恢复执行均如实留证。见[50批本机送审记录](docs/development/process-form-cpp-ebr/20261101-final/ROUND50_LOCAL_RESULT_20261103.md)。

**2026-11-03第49批：IN PROGRESS / 原前端超时已消除，234/234通过 / 原生产依赖HIGH审计阻断待单包例外。** 已批准JSDOM精确升30.1.3，原角色11/11、完整55文件234/234、类型/构建通过，重点用例782/876ms；124生产锁包及角色业务/测试断言/5000ms保持不变。原审计实际检出follow-redirects1.16.0高危，候选1.16.2，未擅自更新生产依赖或关闭审计。回调原4项提交前再次通过，无DB写入/迁移；Linux字体及原四CI尚未运行，本轮未提交推送。累计49，见[本轮结果](docs/development/process-form-cpp-ebr/20261101-final/ROUND49_JSDOM_RESULT_20261103.md)及[单包补充候选](docs/development/decisions/PROCESS_CI_REDIRECTS_SECURITY_20261103_PROPOSED.md)。

**2026-11-03角色CPU诊断：IN PROGRESS / 已定位测试DOM计算样式开销 / 未新增修复。** 获准诊断后原11测试两次通过；有效CPU档案34.70秒中getComputedStyle占26.39秒（76.03%），角色组件仅0.25%，不支持改角色业务计算。实际JSDOM27.4.0/dom-selector6.8.1；源码及原断言未改，累计48不清零，49/50尚未使用。依赖位于现批准白名单外，已准备仅扩package.json/lock的具体候选，未安装或宣称通过。详见[CPU诊断](docs/development/process-form-cpp-ebr/20261101-final/ROLE_CPU_DIAGNOSIS_20261103.md)及[依赖补充候选](docs/development/decisions/PROCESS_CI_JSDOM_SUPPLEMENT_20261103_PROPOSED.md)。原Linux字体/完整CI和生产工艺整体G5继续未完成。

**2026-11-03第47–48批：IN PROGRESS / 回调测试装配已修、打印54项无失败 / 前端原超时复现，按§6A停止。** 原回调9参stub/verify定向通过；两个CI任务补开源中文字体但Linux未验，原前端命令限制2worker后234项232通过2失败，RoleDetailView:150/216仍5000ms。工作流CRLF装配偏差计第48批恢复LF，原策略正/负校验通过。累计48，不自动49；无DB写入/迁移，本轮未提交推送，CI尚未恢复。详见[48批结果](docs/development/process-form-cpp-ebr/20261101-final/CI_RECOVERY_ROUND48_RESULT_20261103.md)和[下一阶段候选](docs/development/decisions/PROCESS_CI_ROLE_PERFORMANCE_20261103_PROPOSED.md)。

**2026-11-03续作：IN PROGRESS / PR #6已确认合入 / CI收尾待具体合同批准。** 本机与远端main一致，182b0d5已合入，不重复合并。原角色页11测试通过；原打印11项复验为10通过1错误，EditorCallback的9参数真实调用与7参数mock/verify不一致。中文PDF原7项本机通过，Linux字体环境仍待修正验证。未改实现/断言/CI、无DB写入或迁移；累计46保留。本轮具体方案见[CI收尾草案](docs/development/decisions/PROCESS_CI_RECOVERY_20261103_PROPOSED.md)，整体生产工艺仍未验收。

**最新2026-11-02第43–46批：IN PROGRESS / 工序类型真实保存已实现并定向验证 / 整体未验收。** 已获DCP具体批准，追加V071/V074并接通DTO、校验、保存回读及原型双单选局部控件；旧NULL不回填，缺省/NULL不清除已存类型，无全局IPC必检。后端18测试、前端7测试通过，真实运行页11检查/20图，现有整对象不变；本地8080已加载新JAR。46批修正提示零字号及T2窄屏15px偏移，typecheck和原组件断言通过。旧数据/事务回滚保留，未执行浏览器正向业务保存。累计46，不自动47；整页工序工作区移植、IPC岗位链及完整G5仍未完成。见[本轮开发结果](docs/development/process-form-cpp-ebr/20261101-final/OPERATION_TYPE_RESULT_20261102.md)。

**最新2026-11-02第42批：IN PROGRESS / 已纠正原型全局IPC规则 / 正式移植未完成。** 用户要求继续按已识别工序工作区原型开发；本轮移除每套工艺必须有IPC的错误草稿校验，提示按当前工序配置及前置依赖生效，原路线带/工序清单/右配置布局保留。真实Vue校验函数7用例先3通过4失败，修正后7/7，原型typecheck/build退出0。累计41→42，不清零；无正式MES源码/DB/迁移/业务数据改动。工序类型当前DTO无持久化字段、完成条件及旧审批与当前定义的映射仍须闭合G3.5，不能冒称已正式交付。当前主组件70577B39...，完整G5仍FAIL。见[第42批结果](docs/development/process-form-cpp-ebr/20261101-final/ROUND42_RESULT.md)。

**最新2026-11-02第40–41批：IN PROGRESS / 产品保真REWORK_REQUIRED / G5 FAIL。** CPP实际输入标签及获批主蓝三态已修，7套件29测试、typecheck/build和53真实检查通过；九文件同版170图首遍已看，结构化评审及第二遍未完成，validator实际exit1。用户识别本机mes-demo「上方路线带＋左侧工序＋右侧配置」；正式页未实现该工作区，IPC未纳入现需求/RTM/170格采证。另用户明确检验判断属具体工序及前置关系，不能全工艺强制IPC；旧原型相反校验登记HIGH。三项产品缺口保持OPEN，局部通过不代表生产工艺完成。累计41，两批耗尽，无第42批；无新迁移/正常业务写入。见[第41批结果](docs/development/process-form-cpp-ebr/20261101-final/ROUND41_RESULT.md)、[原型核对](docs/development/process-form-cpp-ebr/20261101-final/IPC_PROTOTYPE_TRACE_20261102.md)和[原图对照](docs/development/process-form-cpp-ebr/20261101-final/IPC_PROTOTYPE_COMPARE_20261102.html)。

**最新2026-11-02第38–39轮：IN PROGRESS / 原五项问题有界关闭 / G5仍FAIL。** 在批准的40分钟、最多2批范围内修复所属模板长名串列、工序表单失败假空并增加重试、原导航Escape关闭及焦点返回；补齐原8下拉/12焦点/1导航状态证据。6套件26测试、typecheck/build、53项真实5173/8080检查通过，完整定义不变、无新迁移或正常业务保存。同版170原图两遍实际审查，1360逐图分类/81维/19WCAG登记；888抽样配对无同行/8px偏差失败、168格无document扩宽。新发现CPP实际combobox无名称及共享蓝字4.104:1两项HIGH，另历史3项局部修复待完整G5；原生缩放/完整键盘等、参考对照及完整锚点尚有NOT RUN，validator实测exit1。累计39，两批已用；下一CPP名称/V2对比度草案待批准，不改原断言，不标READY/ACCEPTED。见[第39轮结果](docs/development/process-form-cpp-ebr/20261101-final/ROUND39_RESULT.md)及[具体补充草案](docs/development/decisions/PROCESS_G5_A11Y_SUPPLEMENT_20261102_PROPOSED.md)。

**历史2026-11-02首遍：IN PROGRESS / 170张原图首轮已审，五项HIGH未关闭。** 1360类观察已登记；发现模板长名串列、服务失败显示未配置，以及21格状态采证无效（8下拉、12焦点、1Escape关闭）。888条配对测量不替代完整UI验收；九维/WCAG及二次审查当时仍未完成，validator退出1，G5 FAIL。无业务代码/DB/迁移修改，累计37保留。见[逐图审查](docs/development/process-form-cpp-ebr/20261101-final/VISUAL_REVIEW_20261102.md)。

## 2026-10-29 工序动态表单—CPP—eBR配置产品经理推进

**最新2026-11-01第37轮：IN PROGRESS / 170格采证全部齐备 / 完整G5评审仍未完成。** 用户“批准，继续”授权一次仅采证修正；累计36→37，不改业务代码或170格断言。最终采证14检查通过、case errors0，同业务源码170/170截图齐备；403/409、返回、只读角色及末次完整对象相等均有证据。原中间失败保留，未缩减覆盖。运行23:50:17前结束；无新迁移或正常业务写入，既有20/20/typecheck/build不重跑。逐图八类、九维/WCAG及对抗审查尚未全部登记，validator仍退出1、集成必需ui-ux-audit继续FAIL；不得标READY/ACCEPTED。见[第37轮结果](docs/development/process-form-cpp-ebr/20261101-final/ROUND37_RESULT.md)。

**最新2026-11-01 23:32：IN PROGRESS / 当前业务修复20/20、构建和真实403/409恢复通过 / 完整G5仍FAIL。** 用户批准22:33:26–23:33:26的60分钟、最多2轮窗口；实际累计34→36，不清零。已修新字段改码后的属性/Canvas身份断裂、画布键盘定位及375表单操作裁切；只改既有组件/测试，不改业务合同或迁移。5套件20测试、typecheck/build退出0，真实恢复/权限16检查通过；496仍7/DRAFT，最后SAVE29078，浏览器已关闭。正式G5先定170格，现126格、缺44格及完整评审；validator退出1，集成报告必需ui-ux-audit仍FAIL。原采证脚本错误/原图路径覆盖缺陷均如实留证，未改判PASS。2轮额度已耗尽，额外1次仅采证脚本修正申请待用户回复；未进行第37轮，不擅自延时。见[窗口结果](docs/development/process-form-cpp-ebr/20261101-final/FINAL_WINDOW_RESULT.md)。

**最新2026-11-01晚间：IN PROGRESS / 角色服务错误恢复、真实409及重载已验证 / F-005中文角色名称回退已修复。** 原控件在选项失败/筛选后显示数字ID，现用已有名称缓存/服务端快照保留已选中文显示，缺失项禁选，不改ID/权限/DTO。新增2条先RED后GREEN，原三套件17/17、typecheck退出0，1440/375真实断网状态12名称复验通过。真实仓储会话登录200但被前端守卫拦回，服务端403尚未测到，整脚本exit1；完整UI/G5未完成。原32+包装装配修正1+控件修正1，保守累计34轮；无新增迁移/正常业务保存，496仍7/DRAFT、末次SAVE29078。最后渲染专项比十分钟窗口超19秒，已停止新UI运行并如实记录，不能宣称预算内或整体READY。见[收尾结果](docs/development/process-form-cpp-ebr/20261101-final/RESULT.md)。

**最新2026-11-01：IN PROGRESS / CPP与签署、复核角色真实保存—工艺入口重开—恢复闭环已完成。** 496/OP03既有CPP四动作和两角色ID/服务端快照真实持久化，正常三次保存versionNo4→5→6→7；最终完整详情只版本元数据变化，definition与原基线严格相等。原MariaDB只读核对三条SAVE审计与前后摘要均保留，12/12证据检查exit0；缺原因阻止保存、填写后恢复成功。无新业务记录、迁移、源码修复或发布，原32轮保留。完整UI/G5及真实HTTP冲突/窄权限/服务错误恢复仍未完成；此前“CPP/角色持久化未验证”仅为历史。见[本轮结果](docs/development/process-form-cpp-ebr/20261029/persistence-result-20261101.md)。

**最新续验2026-10-31 21:25：IN PROGRESS / CPP与签署、复核角色真实编辑已验证 / 整体验收未完成。** 真实5173页面完成CPP条件组OR预览、不完整配置拒绝、两类角色搜索选择与未保存关闭保护；完整详情多次重读一致、versionNo仍4。16/16记录证据检查exit0，无业务保存/新记录/迁移或源码修复，原32轮保留。窄屏初图处于侧栏过渡，稳定重拍后恢复；通用标签探针0对，不能判同行PASS。CPP/角色真实持久化、全UI/G5及原型/G3.5仍未完成，详见[本轮报告](docs/development/process-form-cpp-ebr/20261029/live-configuration-verification-20261031.md)。下方保留阶段历史。

**最新：IN PROGRESS / 测试工具全部操作模式已生效 / 业务整体验收未完成。** 用户明确“测试工具允许所有的操作”；21:09–21:11（北京时间）仅注册CUAWright环境设ALLOW_MUTATIONS=1，标准客户端同步读取并真实返回mutating_requests_allowed=true。9项离线方法/按钮放行检查通过，无真实业务写入/删除/审批；未改MES权限、签名、审计或数据保护。旧宿主连接需正常重连，当前标准客户端已生效。累计32轮（原31+BOM修复）；无新增迁移。原496真实往返已通过，完整CPP/角色/UI/G5仍未完成。见[工具模式记录](docs/development/process-form-cpp-ebr/20261029/cuawright-full-operation-mode-20261031.md)。

**最新：IN PROGRESS / 496真实保存、重开及恢复闭环通过 / 整体未验收。** 用户要求自行接入后，已用标准stdio MCP客户端连接同一CUAWright新服务，15工具发现与真实原生确认成功，默认全局只读保持。20:50:46–21:00:37（北京时间）窗口内完成两次精确批准的UI PUT，均HTTP200，versionNo2→3→4；分别离开后从工艺入口重开，最终原名恢复，完整响应除目标版本元数据外与基线严格一致。范围2次耗尽失效，已安全结束。新增客户端及长行参数修复保守计累计31轮；原server/helper45项离线测试实现未改。无新迁移/审批/发布/Git操作，完整CPP/角色/执行动作及UI/G3.5/G5仍未完成。见[真实闭环报告](docs/development/process-form-cpp-ebr/20261029/template496-real-roundtrip-20261031.md)。

**最新：IN PROGRESS / 用户已重载，但当前回合仍连接旧MCP / NOT READY。** 2026-10-31 20:46开始续作，用户回复已重载；20:48状态仍为旧12工具且无mes_write_scope字段。只读检查注册配置确实指向已更新server_local.py；进程信息确认20:47:54启动新MCP，旧08:01进程同时仍存活。新进程启动已确认，但当前回合调用仍未切换，新增导出/范围/保存接口不可调用。本轮未杀进程、改配置或绕过，无业务保存/DB操作、无新修复（累计29）；需当前宿主连接切换后再核验能力及执行原已批准保存。离线45/45保持历史事实，不能当运行生效。详见continuation。

**最新：IN PROGRESS / CUAWright通用受控编辑工具离线45/45 / 待正常重载与业务验收。** 用户20:16:27明确批准通用范围；20:18:05–20:33:23（北京时间）完成完整详情脱敏导出、原生人工确认短期编辑范围、真实UI专用保存通道的磁盘实现及离线测试。45/45、py_compile exit0；默认全局只读保持，错误目标/字段/额外定义、超额/过期/重开不续额均覆盖。保守累计29轮（原25+4组实施/补强），不清零。当前宿主仍原12工具、未重载，新能力仅离线注册15项；实际宿主确认未验证。本阶段真实登录/业务PUT/DB写入均0，496两次保存及完整G5未运行。见[离线实施结果](docs/development/process-form-cpp-ebr/20261029/cuawright-scoped-tools-offline-20261031.md)。

**最新：IN PROGRESS / 两次保存已获批准，工具定向写通道缺失 / NOT READY。** 用户于2026-10-31 20:05:47（北京时间）明确批准模板496/OP01_RECORD名称修改保存、重开、恢复保存及重读，最多两次。沿用20:03:16–20:13:16窗口，不重置25轮；已有预检DRAFT/V1/versionNo2/schema2和角色快照一致，完整定义基线未取得。CUAWright现有保护同时阻止保存按钮和业务PUT，仅有全局放开开关、无两次定向能力；未点击保存或绕过，两个保存/重读均NOT RUN，原名未改变，无本轮版本增长/保存审计。详见[批准及停止点](docs/development/process-form-cpp-ebr/20261029/template496-controlled-save-authorization-20261031.md)。

**最新：IN PROGRESS / 模板496保存预检完成部分，定向写能力未就绪 / NOT READY。** 2026-10-31 20:03–20:06（北京时间），CUAWright确认496草稿及名称属性；DEV只读SELECT确认DRAFT/V1/versionNo2/schema2，OP01_RECORD原名“称量与投料记录”，签署6/复核6角色快照完整且与当前角色一致。未取得完整定义基线，未点击保存。工具现有写保护开启且无两次定向放行接口，未全局放开或绕过。无业务源码/数据库/迁移/配置修改、无新单测；累计25轮，真实保存与完整G5仍未完成。见[预检及定向通道方案](docs/development/process-form-cpp-ebr/20261029/template496-save-preflight-20261031.md)。

**最新：IN PROGRESS / CUAWright正式入口复验通过 / NOT READY。** 2026-10-31 19:53–19:56（北京时间），CUAWright从无活动会话正常建立会话，专用登录/身份HTTP200；列表KCL30“查看”、工序表单页签、同码两行来源展示均成功，分别进入模板496/291对应设计器并关闭返回，CPP及已有签名角色配置可见。旧工具无变化现象未复现，未改业务代码，累计修复25轮；业务写保护保持、无保存/迁移。真实保存、完整UI/权限、精确原型批准及G3.5/G5仍未完成。见[定向复验报告](docs/development/process-form-cpp-ebr/20261029/cuawright-entry-verification-20261031.md)。

**最新：IN PROGRESS / 已重新登录，入口与页签无变化待诊断 / NOT READY。** 2026-10-31 18:40（北京时间）在同一正式5173新标签确认密码显示控件可切换，并依既有授权登录成功；直接打开工艺列表可见7条数据，但KCL30“查看”点击后无可见变化。直接打开其既有详情后，点击“工序表单”仍未切换；两处事件是否送达未知，不能判为应用缺陷，直接地址不代表列表入口通过。保留已登录详情页；未改业务代码/数据库/服务，累计修复25轮。来源列、真实保存、完整UI和G5仍未完成；详见[续验](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 登录及交互诊断未闭环 / NOT READY。** 2026-10-31 18:39（北京时间），用户要求继续，首选CUAWright无活动会话，改用现有Chrome正式5173标签，按既有授权尝试登录；提交及回车后页面未跳转、未显示错误，无法判断请求是否发出或是否得到响应。页面`#app`带Vue挂载标记；点击密码显示图标前后输入类型未变，不能区分自动化事件未送达与应用未响应。已清空密码输入，未在任务记录保存凭据；未改业务代码/数据库/服务，修复累计25轮。正式页面验收和G5仍未完成，详见[续验](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / eBR 隔离回归通过 / NOT READY。** 2026-10-31 15:50（本机时间）离线 Maven 定向运行历史模板规范化兼容、角色历史和职责分离、运行时边界三类测试，16/16 通过、0 失败/跳过；均为隔离测试，不证明真实页面或数据库保存。现有 5173 浏览器标签仍显示登录页，未诊断本次跳转原因。未修改业务代码、数据库或迁移，累计修复 25 轮不变；真实页面、完整 UI 审查、原型准确版本确认、G3.5/G5 和人工验收仍未完成。详见[续验](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 会话状态只读补查 / NOT READY。** 原03:20:34–03:30:34 UTC诊断窗口续查，03:24:07–03:24:48完成并同步Planning revision 31：现页为`/login?redirect=/process/packages`，浏览器可见最近30条error/warn为空，未取得该次导航网络状态，不能认定401、会话过期或其与点击无变化存在因果关系。源码有身份缺失跳转逻辑但不是当次根因证据。已登录页面事件送达及列表入口仍NOT RUN；无源码/业务数据/服务操作，累计25轮。详见[续验](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 两入口只读诊断部分受会话阻断 / NOT READY。** 用户批准最多10分钟、零修复；2026-10-31 03:20:34 UTC开始、03:30:34截止，03:21:41操作及Planning revision 30同步结束。详情工序表单页签可见、中心命中自身、pointer-events正常，键盘/点击后选中仍为工艺路线，事件是否送达及根因未知；准备列表核验时导航后显示登录页，未重新登录，列表本次NOT RUN。不改源码或业务数据，累计25轮；详见[续验](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 只读复核页签未切换 / NOT READY。** 用户授权自行登录，现有会话已登录；工艺详情页视觉确认后点击工序表单页签仍未切换，最近可见浏览器error/warn日志为空。无法区分自动化与应用原因，未修改代码或业务数据，所属模板/设计器入口仍NOT RUN，累计25轮。见[续验](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 正式工艺只读检查未完成 / NOT READY。** 用户要求自行登录后已进入正式首页；工艺列表“查看”两次尝试无页面变化，历史详情URL可只读打开工艺，但“工序表单”页签点击后仍为工艺路线。原因未定位，不将直接URL当列表入口通过，所属模板列、同码与设计器入口仍NOT RUN。无业务数据写入/源码修复，累计25轮，Planning revision 28；详见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 后端运行规则补充验证 / NOT READY。** 用户继续开发；正式5173现有标签地址仍为登录页，未登录或读取凭据。离线运行不加载Spring或数据库的 `EbrEngineTest`、`EbrRuntimeRulesTest`，2类9项通过（Maven 3.295秒），覆盖CPP超限提示/提交阻断及运行字段约束，Planning同步revision 27。未改源码/数据库/迁移/服务，累计25轮；真实保存、正式业务页面、完整UI与G5仍NOT RUN。详见[续验](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 正式前端登录页局部只读检查 / NOT READY。** 用户要求前端页面检查；2026-10-31 00:43:21 UTC开始、00:53:21截止，00:44:17页面检查及Planning revision 26 同步结束。Chrome现有正式5173标签仍在登录页；可访问性树可识别标题、账号/密码文本框与登录按钮，1920×919下文档无横向溢出；不判定响应式或完整无障碍。不能安全证明凭据框为空，本次未截图、未读取输入值或登录；生产工艺来源列、同码及入口均NOT RUN。无源码/数据库/服务变化，累计25轮不变；完整UI及G5未完成。详见[续验](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 后端隔离定向验证 / NOT READY。** 用户询问后端测试；2026-10-31 00:29:27 UTC开始、00:30:19结束并同步Planning revision 25，确认所选JUnit/Mockito不启动Spring或连接数据库后，离线Maven运行4类29项全部通过（定义规则6、DSL 6、角色schema 12、角色授权5；总4.822秒），未写DEV、不运行迁移或改源码，累计25轮不变。既往DEV回滚事务1/1为历史证据，本次未重跑；真实8080保存读回、版本冲突、正式页面及G5仍NOT RUN。见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 现有会话只读验收未运行 / NOT READY。** 用户批准限10分钟、需要登录则停止；2026-10-30 15:27:35 UTC开始、15:37:35截止，15:28:35核对及Planning同步结束、revision 24。CUAWright报告无活动会话，现有浏览器库存也未发现正式5173 MES标签；未创建会话或登录，现有其它隐含登录状态未知。正式来源识别、设计器入口和当前DRAFT/版本/权限/原值均NOT RUN，未核实可写候选；历史291/496不视为授权目标。未写数据库、改代码或操作服务，累计25轮不变。详见[续验](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 用户批准来源识别局部实施 / NOT READY。** 用户明确批准现有工序表单子表显示同列模板名称及编码、模板ID＋表单编码复合行身份，限定一次修复及直接测试；2026-10-30 14:59:21 UTC开始、15:09:21截止，15:01:29实施验证和Planning同步结束、revision 23。正式页面已按有界批准修改，历史空名回退保留编码，入口权限/状态未改；预期RED缺少模板编码，实施后定向1文件1测试通过（10.91秒）、typecheck与diff-check退出0。该测试隔离API/路由，不证明真实页面；累计第25轮，未写数据库/迁移/操作服务。完整UI、真实保存、G3.5/G5仍未完成。见[批准记录](docs/development/decisions/PROCESS_FORM_TEMPLATE_IDENTITY_20261029.md)与[续验](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 原型验证受策略阻断 / NOT READY。** 用户批准单次限定服务切换重试；2026-10-30 12:22:19 UTC开始、12:32:19截止，原先被拒的整条切换/几何命令在创建进程前再次被自动审批以`blocked by policy`拒绝，未执行，未绕过。12:22:57只读核对正式5173仍PID23704、HTTP200，8080仍PID28928；无需恢复。只读核对及Planning同步12:23:17结束、revision 22。工具栏键盘、属性区、原型交互和几何本阶段NOT RUN，无业务代码/DB/迁移变化，累计24轮不变。详见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 当前原型部分依赖版本清单 / NOT READY。** 继续原10:14:06–10:24:06 UTC窗口，本补充10:20:00开始、10:21:28完成核对和Planning同步至revision 21；静态递归记录31个本地源码、样式及包/TS配置文件SHA256并核对一致，输出[部分清单](docs/development/process-form-cpp-ebr/20261029/identity-source-inventory-20261030.json)，条目摘要`0d65104160f048f41c1492b0ee067b003d66252f84a55d6997a9599c75dc8694`；11项外部包/模块及动态引用、CSS资产、TS配置引用、插件与安装包内容未闭合，当前hash不证明旧截图版本或用户批准。历史清单未覆盖；完整依赖集、原型审查、G3.5与真实保存/G5仍未完成。未改业务代码/数据库/迁移，累计24轮不变；具体边界见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 来源识别交接缺口核对 / NOT READY。** 沿用10:14:06–10:24:06 UTC窗口，在定向验证后核对正式候选文件、既有名称/编码与读取路径、无新增迁移的有界合同及G3.5缺项；补充核对及Planning同步10:17:18结束、revision 20；未改业务代码。现有HANDOFF清单属于早期G0，不能代替当前原型或G3.5批准；历史source-evidence中的设计器hash与现源码不同，已记录版本边界，历史清单未更改。完整依赖集、原型审查和确切版本确认仍缺，真实保存需另行数据库写入授权；此前服务切换被策略拒绝，未重试。累计24轮不变。详见[交接准备](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 已批准角色配置当前源码定向验证 / NOT READY。** 用户已解除先前只读限制，授权按基线改源码和运行非数据库写入测试；不含数据库写入/迁移、发布或被拒服务切换。2026-10-30 10:14:06 UTC开始，普通UI截止10:24:06，验证及Planning同步10:16:05结束、revision 19；核对设计锁及当前源码后，角色设计器/控件/模型单测3文件15项通过（11.04秒，Canvas及HTTP隔离），typecheck退出0；无新源码修复，累计24轮不变。eBR类型已有模板名称/编码，但工艺表格来源识别的精确原型与G3.5尚未批准，不能提前正式接入；真实持久化、完整UI和G5仍NOT RUN。详见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 列表12图限定静态复核 / NOT READY。** 用户批准最多10分钟，2026-10-30 05:04:48 UTC开始、05:14:48截止，05:06:41完成审查及Planning同步至revision 17；已原始分辨率查看工艺列表default/no-match/reset四视口12图并逐图记录。查询标签和输入在图中同行，无结果有说明，重置图有结果；900/375空态留白较大待进一步核对，不能由静图证明交互、无障碍或实际恢复。没有运行浏览器/validator、修改代码、数据库、迁移或断言，累计修复24轮；审查manifest及真实保存、设计锁、G3.5/G5仍未完成。[逐图记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / G2证据整理 / NOT READY。** 2026-10-30 04:17:25 UTC开始，截止04:27:25，04:24:38完成审查及Planning同步；按业务产品经理与Planning接续同一任务和24轮记录，恢复入口列明基线、批准、写入边界及压缩后重读清单。静态索引57/57文件存在，另查看工序表单四种状态、新建模板两种状态及工艺列表查询结果、各四视口共28张原图；空态纵向空间、375页签可达性及错误提示焦点待实测。manifest预期102单元、逐图登记0，validator复验退出1；源码核对确认来源识别尚未正式接入。Planning同步至revision 16；前次5173服务切换遭策略拒绝，未重试或绕过。未改业务代码、数据库或迁移；完整审查、真实保存、原型版本确认、G3.5/G5仍未完成。见[恢复与证据记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 局部视觉复核 / NOT READY。** 本次原图查看表单设计、CPP空态与新建三个状态各四视口，共12张；记录小屏工具栏溢出和属性区操作距离待核实，不能仅凭截图判定交互失败或完成视觉审查。计划临时切换5173原型测量时被系统自动审批拒绝，未绕过；正式5173和8080仍在运行。Planning同步至revision 13后发现超过普通UI预算31秒，停止新增审查测试并记录。未修改业务代码、数据库或迁移，累计修复24轮不变；完整逐图、交互/几何、设计锁、G3.5/G5仍未完成，详见[局部复核](docs/development/process-form-cpp-ebr/20261029/designer-partial-review-20261030.md)。

**最新：IN PROGRESS / 原型重试稳定态已验证 / NOT READY。** 用户要求继续并完成重试及验收；当前原型1440/1280/900/375实测加载动画先出现再消失，模板B行可定位并进入对应设计器，4张原图已查看，均为内存数据。首次稳定验证的375截图仍含加载动画，第22轮仅补充等待动画出现后消失，保留原断言及原始截图；后续原图无动画。审查发现原型列表无结果缺少说明、900/375操作列不可见，第23轮加空态及窄屏行布局，第24轮将断点覆盖900，四视口定向检查通过；重新采集57张并保留旧证据。原型build及专用入口build退出0；未改正式业务页、数据库或迁移。累计24轮未清零。建立`IN PROGRESS`审查清单，validator退出1：逐图覆盖、完整交互、几何、对抗复查等未完成；不报原型PASS/G5/READY。首次自动阶段未及时记录起点且超过普通UI 10分钟预算，发现后停止新增修改和测试并恢复正式5173；详见[本轮记录](docs/development/process-form-cpp-ebr/20261029/retry-stable-audit-20261030.md)。

**最新：IN PROGRESS / 第21轮采集完成但恢复稳定态未证 / NOT READY。** 用户批准最多10分钟、一次限定脚本定位修复；16:13:55–16:19:30 UTC（截止16:23:55）。错误态按钮实际DOM为 `<span>重 试</span>`，计算名称“重 试”；原“重试”角色定位0个，实际精确名称1个、可见且启用。第21轮只将同一采集脚本的重试定位限定于错误提示内实际名称；脚本退出0，生成57张截图和索引，无拦截请求/pageerror。四视口均出现重试后的列表，但1440和375原图仍有加载动画，未验证加载结束的稳定恢复，不得称该状态完整通过；本阶段只获一次修复授权，停止进一步补丁。截图不是审查manifest，完整逐图审查/validator、原型设计锁、真实5173/8080保存及G3.5/G5仍未完成。累计21轮不清零，详见[第21轮记录](docs/development/process-form-cpp-ebr/20261029/round21-retry-diagnosis.md)。

**最新：STOP: BLOCKED / 第20轮未通过 / NOT READY。** 继续原任务；CPP/eBR MockMvc受控保存、读取、幂等重放、版本409和权限403在真实DEV事务内回归1/1通过，专属记录回滚，Flyway69验证、无迁移。原型截图采集首次“查询”定位失败，第20轮仅调整其及重置按钮的空白字符定位；重跑进入错误态后“重试”按钮定位超时，完整截图矩阵及manifest未生成。累计20轮未清零，停止任何第21轮修复；未修改验收断言、阈值、跳过条件或业务合同。所有轮次、失败、已排除原因和建议限定续诊范围见[第20轮停止报告](docs/development/process-form-cpp-ebr/20261029/round20-stop-report.md)。真实浏览器业务保存、确切原型锁、完整UI审查和G3.5/G5仍未完成。正式5173已恢复、HTTP200；8080保留。

**最新：IN PROGRESS / 受控API事务回归通过 / NOT READY。** 在既有唯一事务回滚夹具中，增加MockMvc经正式安全过滤与控制器的CPP/eBR保存和读取：成功保存并读回规则与签署/复核角色ID；同幂等键重放返回相同结果，旧版本新键返回409、无查看权限返回403，版本保持不变。专属业务/角色、审计及幂等记录回滚后均为零；载荷checker通过，定向1/1通过（13.88秒），Flyway验证69且无迁移。此为进程内MockMvc与真实数据库事务，不是实际8080网络或浏览器保存。累计修复19轮未变；实体手机、完整原型/UI审查、真实5173/8080保存重读、G3.5/G5仍未完成。详见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 触屏模拟插入定向验证通过 / NOT READY。** 用户批准限定续修；开始2026-10-29 14:34:27 UTC，截止14:44:27，验证和服务恢复结束14:39:20。OP02首次选择引发两个异步Canvas挂载，原触屏点击第一个实例、插入引用第二个实例；第19轮只为异步挂载增加代次校验和卸载失效。原触屏断言、鼠标有效选区插入与未保存提示、无选区保护均在隔离原型通过；OP01/OP02挂载各为一个实例，前端3文件15测试及build通过。累计19轮未清零；结果不代表实体手机、真实业务保存或整体验收，完整UI审查、原型版本确认、G3.5/G5仍未完成。详情见[触屏限定续修记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：BLOCKED / 触屏选区未解决 / NOT READY。** 第18轮修复既有设计器无有效选区时生成孤立字段：现提示选择插入位置，隔离复测无选区字段3→3，鼠标有效选区插入字段及画布内容通过。375触屏模拟左右滑动通过，但原触屏点击画布后插入仍不成功；修复前为字段增加而画布无字段，修复后为字段未增加，同一触屏目标未达成，按§6A重复失败停止。3文件/15项前端测试通过、build退出0，未修改验收断言；累计18轮。不是实体手机测试，触屏编辑、完整UI审查、真实浏览器业务保存及G3.5/G5未完成。详见[失败与根因证据](docs/development/process-form-cpp-ebr/20261029/canvas-touch-selection-failure.md)。

**最新：IN PROGRESS / 原型新建闭环局部验证 / NOT READY。** 原型新建模板原先自动填入表单与字段，与现有正式流程不符；第16轮改为先创建空模板、再由用户新增表单，保持同一模板多表单和模板编码去重。隔离浏览器验证新增、关联OP02、保存后结果定位与重开；均为内存数据，无业务写请求。第17轮修正新建页操作与表单右边界对齐，四视口DOM和截图局部复核通过；原型类型检查和独立入口构建退出0。当前累计17轮，不得清零；完整UI审查、Canvas手机触屏、真实浏览器业务保存重读及G3.5/G5仍未完成。CPP/角色后端数据库事务回滚验证保持通过，详见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 原型体验整改 / NOT READY。** CPP及签署/复核角色配置真实数据库事务回滚往返1/1通过，Flyway校验69、无新增迁移；只读401定位为认证刷新前首次身份请求。原型补采36张截图（9状态×4视口，仍非完整覆盖），部分原图自审发现窄屏CPP字段截断和Canvas横向内容缺少提示；第14轮修正CPP条件行后四视口几何复测及截图复查，第15轮增加画布横向滚动提示与键盘可聚焦滚动，375实测可滚动。Canvas仍无法在手机宽度完整同屏显示，触屏操作、完整矩阵及G5尚未验证；仅记录局部自审，不报完整PASS。定向3文件15测试、隔离交互脚本及前端build通过。累计修复15轮，详情见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / CPP与角色数据库往返已验证 / NOT READY。** 401定位为身份首次读取401后刷新200、再次读取身份200；本次只读链路到工艺详情成功，未观察业务接口失败。新增独立事务回滚夹具，以已通过业务载荷检查的自然业务值建立专属记录；CPP规则、签署及复核角色ID和快照可保存读回，回滚后核对模板/工艺/产品/物料/单位/角色记录均不存在。最终定向集成1/1通过、13.60秒，Flyway校验69项、当前V069且无迁移。首轮测试未找到载荷文件，修正模块工作目录相对路径后通过，累计修复13轮；既有EbrIT未运行、未改断言。真实浏览器业务保存重读、完整UI审查、G3.5/G5仍未完成。详情见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 画布误报定向修复 / NOT READY。** F-005已在既有设计器内增加初始化画布比对，原型隔离回归确认不编辑切CPP不误报、实际画布输入及新增字段仍提示未保存、同码不同模板内存隔离；请求守卫未记录业务写请求。修复前失败及复测证据保留，正式定向3文件/15测试通过、build退出0；真实5173/8080模板496只读切CPP后关闭可返回，浏览器记录一条401待定位。累计修复12轮；真实保存重读、完整UI审查、G3.5和G5仍未完成，不能报READY或ACCEPTED。无新增迁移或数据库业务写入；详情见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 新发现待修复 / NOT READY。** 用户继续推进；Playwright以禁止非GET/HEAD及非本地请求的隔离策略，验证原型CPP内存保存重开、放弃未保存修改和工艺编辑后的路线/表单入口更新；不能代表真实DB保存。进一步发现F-005：未修改时从表单切到CPP后关闭，默认Canvas被捕获为变更并弹未保存提示；已有复现脚本和失败日志，正式页面是否同样受影响待真实只读确认。累计修复11轮未变，未改断言；完整原型审查、G3.5和正式G5未完成。[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。

**最新：IN PROGRESS / 原型局部验证 / NOT READY。** 用户明确要求持续开发、不再逐阶段请求批准；既有有界模板来源方向和修复计数保留。原型接入正式Canvas/CPP设计器及工艺编辑源码，接口改为内存数据；新增移动导航、未保存离开保护，完善各工序、IPC与参数入口。正式工艺页和数据库未改；原型不证明真实保存。mes-demo typecheck及专用原型入口构建均退出0，真实浏览器验证OP02与CPP入口、工艺编辑/取消及部分视口；修正移动端编辑字段挤压和模板名称裁切。累计修复按保守口径11轮，详见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。完整设计器保存/重读、错误恢复、状态/视口矩阵、原图九维审查、manifest、G3.5及真实5173/8080 G5均未完成，禁止宣称完成或验收；Planning远端传输失败，待恢复同步。无新增迁移或业务写入。

**最新：原型第二阶段 IN PROGRESS / NOT READY。** 用户明确批准10分钟继续，13:55:17起；沿现行T4及已批准来源识别方向，复用原型导航，补工艺列表查询/重置、返回、页签、新建入口/必填提示，未擅用包含越界动作/版本的旧CPP原型。mes-demo构建通过，八组知识查询退出0；原截图900/375操作列裁切，1轮修复为窄屏记录组，前后4视口图片保存，肉眼复查局部操作可见；累计修复5轮（历史4+本阶段1）。原型hash `D7189AED1FE6E842EDF6C6EB32B611E2EB01AFA6E0C8A9E89D6970FEC8AAE44B`，未获用户版本批准。Canvas设计器、其他工序/页签、工艺编辑入口及移动导航仍未完成；surface×state×viewport完整审查/manifest未完成，不能报PASS或正式集成。详见[原型设计说明](docs/development/process-form-cpp-ebr/20261029/identity-phase2-design-notes.md)及[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。单一5173原型检查后正式前端恢复PID33428；无正式业务源码/DB/迁移修改。下一阶段应继续在已批准方向内完善，累计轮次不清零。

**最新：局部原型草稿 / IN PROGRESS / NOT READY。** 用户已批准“模板名称+编码、模板ID+表单编码”有界方向，设计确认记录已建；本机mes-demo新建局部Vue草稿，构建通过，真实浏览器只读验证第二行进入对应模板及返回、只读状态可达。草稿源hash FC8AAF4285D5376648139D6BB6F83A4017A6F65BB7250C0CB6C65A68EE46AB0C；局部截图自审发现导航与正式UI不一致、相关非范围按钮及完整设计器未实现，完整视口/状态/知识查询及G2 manifest NOT RUN，未获确切版本批准，禁止正式集成。复用单一5173暂时查看mes-demo后，正式前端已恢复PID22696；backend8080保留。普通UI10分钟预算达到，暂停新修改/测试，任务累计原4修复轮保留。详见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。无新增迁移、业务数据写入或正式页面源码修改；下一阶段需明确预算并继续完善原型及完成完整审查。

**用户批准方向：PROTOTYPE PENDING。** 用户明确批准工序表单列表展示现有eBR模板名称和编码、使用模板ID+表单编码区分行；具体布局和精确原型尚未批准，见[方向记录](docs/development/decisions/PROCESS_FORM_TEMPLATE_IDENTITY_20261029.md)。不得恢复旧工艺版本审批，也不得以方向批准代替DESIGN LOCK/G3.5或正式集成授权。其余状态保留，历史累计修复4轮不清零。

**最新只读根因：IN PROGRESS / DESIGN REVIEW REQUIRED。** 用户继续推进；工艺1的OP01两行实际分别进入模板496和291，均为V1草稿，故是不同模板的同码表单，不是重复投影。当前列表缺少模板名称/编码，用户无法区别来源；已形成[有界产品候选](docs/development/process-form-cpp-ebr/20261029/F004-template-identity-proposal.md)，候选复用既有模板名称和编码及模板ID+表单编码行身份，尚未获具体设计确认，未改正式页面。现有EbrIT实际fixture片段经业务载荷checker退出1，命中模板名称`Test eBR template`；未对共享DEV运行该fixture，真实保存重读与G5仍NOT RUN。本轮仅记录证据/提案，业务源码/数据库/迁移未改，累计修复4轮保留；Planning先恢复revision4后待同步。

**本次续验：IN PROGRESS / LIVE READ-ONLY / G5 NOT RUN。** 用户“继续开发”，沿用同一任务及累计4轮。DEV只读历史核对最高V069/69项/无待执行，当前源码后端package 21.019秒通过；启动同源8080/5173，Flyway校验69成功且无需迁移，后端health UP。CUAWright真实浏览器登录200：工艺1→工序表单→模板496对应工序→角色规则→CPP可访问；只读查看模板详情可见提交审批按钮，尚未验证自然返回路径及受控写入。实际列表OP01两行名称/编码/版本/字段数完全相同，F-004定位歧义已真实观察，重复投影与不同模板归属尚待确认，暂未擅自增加字段或改页面。既有EbrIT fixture在共享DEV写入占位式业务值，未完成载荷checker前未运行；真实保存重读、角色窄权限、完整矩阵、截图审查、G5集成及用户验收均NOT RUN。详见[续验记录](docs/development/process-form-cpp-ebr/20261029/continuation.md)。本轮未改业务源码、未运行迁移/写业务数据/签名审批发布；当前前后端服务运行，Planning resume连接失败，本轮尚未同步。

**最新：IN PROGRESS / SOURCE FIX VERIFIED / G5 NOT RUN。** 用户再次明确批准最多10分钟限定诊断修复；10:14:26–10:19:47内实测角色input计算名为“签署或复核角色 请选择角色”，将同行容器与独立关联label分离后名称定位通过。原测试随后发现全局选项文字定位歧义（已选值/多个下拉同名）；仅按当前输入aria-controls限定选项菜单，11条原expect逐行一致，未削弱验收标准。3 suites / 15tests全部通过（0失败/跳过，6.48s），npm run build退出0（含vue-tsc；Vite17.05s，仅既有chunk警告），git diff --check退出0。日志role-stage3-name-diagnostic.log、role-stage3-green.log、role-stage3-final.log、role-stage3-build.log及修改前测试均保留，详见同一[完整记录](docs/development/process-form-cpp-ebr/20261029/analysis.md)。累计本轮4次修正：两次生产标签、两次测试构造/操作定位；历史及前两阶段失败不清零。已改业务源码仅DefinitionDesigner.vue/RolePicker.vue，角色ID校验、冻结历史、API/权限/布局合同不变。真实5173/8080保存重读、完整UI审查/G5和其余CPP/eBR候选仍NOT RUN，不报READY或ACCEPTED。没有新迁移、服务启动、数据库写入、签名、审批、发布、提交推送。以下暂停描述作为前阶段事实保留，不再表示等待此前已获得的授权。

最新限定续修：用户直接批准下一阶段；扩大原失败DOM输出确认aria-label仅落Ant外层容器，实际combobox缺同名名称。RolePicker一次唯一ID/label关联修复后原定向仍14/15、退出1（role-stage2-green.log），同签名复现再次按§6A停止，当前等待用户批准“最多10分钟、先测实际可访问名称再修标签”的范围；未改断言/布局/角色ID合同。累计本轮夹具1+标签修复1，历史不清零；第二阶段尚未新typecheck/build/G5，不沿用前次类型检查冒充新验证。Planning连接恢复并同步revision3，早前传输失败保留。当前仍IN PROGRESS / PAUSED / NOT READY。

**IN PROGRESS / ROLE CONTROL REPAIR PAUSED / NOT READY。** 用户要求使用business-product-manager推进生产工艺，明确聚焦“工序动态表单、CPP与eBR配置”，纳入Planning，并直接授权“修改不符合的地方”。已建立Planning计划`0df9356d-675b-4f37-8b7d-948948c8d694`（初始保存revision2，后续同步MCP传输错误，最新事实留存在本索引及仓库证据）；形成[G0规格/发现及暂停报告](docs/development/process-form-cpp-ebr/20261029/analysis.md)、完整规格、RTM、双设计任务包与ownership ledger。资料结构、RTM、双包、文件保真、ownership五项校验通过，仅证明资料自洽，不证明G0/功能/体验验收。整包G0仍IN REVIEW，G3.5/正式重设计均未获批准。

源码确认角色输入仍写legacy requiredRole而保存要求requiredRoleId；纯内存两例复现拒绝编码/接受有效ID。按既有ROLE_ID_SIGNING_DESIGN_LOCK第5项已批准的同位替换例外，DefinitionDesigner接入现有RolePicker及ebrRoleOptions，保留冻结显示、搜索响应防乱序、加载失败重试和原ID保存校验；未改API/权限/数据库或历史证据。新增组件用例先3/3 RED；首次实现14/15，一轮仅调整测试输入响应式构造（原断言不变）后仍14/15，失败签名为角色combobox按“签署或复核角色”名称不可定位，因此按§6A提前停止后续补丁/测试，待用户批准限定下一阶段诊断。typecheck退出0；修复计数本轮夹具1，历史不清零。普通UI预算09:49:01起十分钟，没有自行延长。完整日志在同目录role-red.log、role-green.log、role-green-fixture.log、typecheck.log；未宣称根因已经证实。

首次5173只读访问ERR_CONNECTION_REFUSED，8080/5173/5174未监听；本轮真实页面/API保存、服务启动、build、完整G5及数据库持久化均NOT RUN。无新迁移、业务数据写入、签名、审批、发布、提交或推送。CPP两级条件组与“嵌套”文义、模板生命周期可达性/多版本聚合为待验证项，未擅自扩充合同；未批准整页工艺原型仍不集成。后续从同一证据和累计计数继续，不重复实施历史ACCEPTED任务。

## 2026-10-20 质量经理角色已创建（ID 404）

**ROLE CREATED / NO PERMISSIONS / REPORT APPROVAL PENDING。** 按用户要求通过正式角色管理 API 创建并回读 `QUALITY_MANAGER`（质量经理，ID 404，启用，权限0），不修改其他用户/资格/签名/原报告；当前尚未绑定个人质量经理账号，报告265不具备真实独立复核及批准证据。详情见 [决策执行记录](docs/development/decisions/INCOMING_REPORT_QUALITY_MANAGER_REVIEW_20261020.md) 和 `docs/review/evidence/quality-manager-role-result-20261020.json`。

## 2026-10-20 取样单与报告打印配置和实际PDF已打通

**PRINT RUNTIME CONFIG + REAL PDF VERIFIED / 整体G5及新流程报告最终审批仍未关闭。** 本轮DC在真实5173/8080上补齐SAMPLING_RECORD及INSPECTION_REPORT数据源、元数据、默认模板校验/发布/绑定，已消除PRINT_SOURCE_UNBOUND。配置工具bootstrap及businessTypes对象数组幂等问题已修复，5/5回归；前端打印两文件9/9 PASS。实际打印首版发现时间列覆盖和中文页脚乱码，以普通DOCX新增V2修正：当前取样模板76、报告模板77；本轮旧74/75仅取消适用绑定，发布DOCX历史保留。最终真实打印9/9检查与四视口8图完成，PDF正文/页脚已单页目视；未批准报告265正式输出被422 PRINT_APPROVAL_REQUIRED拒绝，草稿PDF正常；既有已批准报告11真实正式打印200且原批准/签名98不变。**报告265仍DRAFT，不能当成已正式批准**：现有验收角色缺少与其制单人独立且有资格的报告复核人员，未改账号/角色/资格、未代签或自动QA放行。完整G5 UIUX清单及375取样预览动态缩放标题/焦点恢复仍需后续复核，不能标整体READY/ACCEPTED。未新建迁移、未改库存或原始检验事实，未Git提交推送。详见 [本轮打印收尾报告](docs/review/evidence/INCOMING_CROSSROLE_PRINT_CLOSEOUT_20261020-074616/PRINT_CLOSEOUT_REPORT.md) 与 [真实证据状态](docs/review/evidence/INCOMING_CROSSROLE_PRINT_CLOSEOUT_20261020-074616/PRINT_CLOSEOUT_STATUS.json)。下文“打印未绑定”仅为历史，不覆盖本条；已确认收货UI和原型不重开。

## 2026-10-20 06:50 收尾状态校正：新后端已部署，主链到报告草稿，打印绑定仍缺

**G4 DEPLOYED / LIVE CHAIN 15/15 TO REPORT DRAFT / PRINT CONFIGURATION BLOCKED / G5 FINAL PENDING。** 现场核对运行PID35116为2026-10-20 00:45新包，健康UP、Flyway69/current069；此前22:40条目的未部署状态已过时。真实开发环境已完成QC确认、取样提交、仓管独立签名扣库、收样绑定、逐项检验、独立复核到报告草稿的15项检查。新真库负向7/7、取样打印Provider4/4及DOCX/PDF集成1/1；取样打印Provider已存在，不能重复开发。当前认证只读打印探针仍返回422 `PRINT_SOURCE_UNBOUND`，SAMPLING_RECORD数据源0、默认模板0，需要现有配置API实际建立/发布/绑定再验证打印；报告265目前DRAFT，报告批准及打印和完整UIUX/最终用户验收未完成。事实详见 [当前收尾核查](docs/review/evidence/INCOMING_CROSSROLE_STATUS_20261020-065047/STATUS_VERIFIED.md)。本輪仅更新证据/任务状态，未改业务数据/源码、未提交推送。

## 2026-10-19 22:40 来料质量闭环状态复核与前端环境复验

**G4 SOURCE IMPLEMENTED / 本轮前端BUILD及定向TEST PASS / 整链G5未完成。** DC现场核对正式B实施报告、源文件及Surefire证据；重跑正式前端 `npm run build` exit0、四个来料Vitest文件 **22/22** exit0，先前sandbox esbuild spawn EPERM本轮未复现，未修改业务实现或断言。B源码已含取样提交、仓管签名扣库、样品绑定和复核后自动报告草稿；历史后端回归30/30、B合同/预检8/8、安全路由23/23和领域策略4/4保持原统计、不加总。V069已经测试应用，但运行中的8080仍是19:04启动旧服务，B版本尚未重新部署；取样单统一打印Provider尚缺，真正5173跨岗位签名/库存/报告PDF与完整UIUX尚未验收。不因构建通过标完成，不重复要求已批准原型确认。见 [本轮核查](docs/review/evidence/INCOMING_CROSSROLE_COMPLETION_20261019/DC_STATUS_CHECK_20261019_2240.md)。

## 2026-10-19 来料质量闭环 B 正式工程适配

**G4 SOURCE IMPLEMENTED / TARGETED BACKEND PASS / G5 AND HUMAN ACCEPTANCE PENDING。** 按本会话用户批准的 G2 九文件精确版本完成 B1–B4 正式适配：新链取样提交不动库存、仓管双权限/资格/独立签名后按来源明细原子扣减、收样后显式绑定检验任务、必检项复核完成自动建立报告草稿；旧手工链、既有 QA 放行和已签名历史保持不变。V068 已真实应用；V016 不可变触发器阻止受控 ledger 回写后，按追加迁移规则新增并应用 V069，V067/V068/V069 均不得再编辑。后端七个回归套件 30/30、B 合同/预检 8/8、安全路由 23/23、领域策略 4/4 通过；前端 vue-tsc exit 0。Vite/Vitest 均因当前 sandbox 无权启动 esbuild 子进程而 `spawn EPERM`，原始失败已保留；未启动/停止 8080，未运行真实 5173 登录签名、打印 PDF、浏览器 G5 或完整 UI/UX 清单，因此不得标记 READY FOR ACCEPTANCE/ACCEPTED。检验报告已有 `INSPECTION_REPORT` 统一打印 Provider；取样任务无已注册统一打印 businessType，本轮未虚构。证据与未完成矩阵见 [B 实施报告](docs/review/evidence/INCOMING_CROSSROLE_COMPLETION_20261019/BATCH_B_IMPLEMENTATION_REPORT.md)。下文 G3 阻塞条目为历史，不覆盖本条。

## 2026-10-19 G3真实权限与原子库存审查（新增）

**G3安全与事务审查进行中 / G3.5 BLOCKED / G4新仓库扣减未执行。** 本轮新增只读 `IncomingCrossroleSecurityPreflightIT.java`，真实MariaDB **3/3 PASS**；原权限已含 `qms:sampling:execute/complete`，但 `wms:sampling-outbound:confirm`不存在，`SAMPLE_OUT`流水0，WMS ledger已有 `uk_ledger(org_id,idempotency_key)`。详细源码核对发现 warehouse资格码及签名slot/Provider未实现，原QMS`complete`归检验取样员，普通WMS`move/adjust`不允许待验库存，绝不可直接复用。已完成 [G3双权限+资格+新签名+同事务多库位精确扣库审查](docs/development/INCOMING_CROSSROLE_002_G3_SECURITY_TRANSACTION_REVIEW.md)，并在 DO_NOT_RUN V067草案补QC首批自动双单的可空关联唯一键，避免将合法追加任务误判重复。既有66迁移历史及正式数据未修改，未启动真实仓库出库新接口；不得以预检PASS充作正式 G5。

## 2026-10-19 G3真实MariaDB安全预检与历史兼容

**READ-ONLY PREFLIGHT PASS 3/3; G3仍待签名/事务冻结；G3.5 BLOCKED。** 现有IncomingQualityIT Maven exit0、Flyway 66/current066，新增 `IncomingCrossrolePreflightIT.java` 真库只读3/3 PASS；INITIAL历史重复0、无样品非法状态历史0、源字段8/8存在、ledger delta_qty真实6位精度。已登记到 [G3详设候选](docs/development/INCOMING_CROSSROLE_002_G3_DETAILED_DESIGN_DRAFT.md) 和 [G3.5交接草案](docs/development/inspection-crossrole/g35-development-handoff-DRAFT.json)。新增DDL仍是DO_NOT_RUN草案，尚未变更真实QMS/WMS服务、DB、Flyway或权限；真正的仓管签名/审核权限、QMS与WMS原子库存事务仍需下一阶段核实，不以预检PASS代替整体G3/G5。

## 2026-10-19 用户确认跨岗位G2准确版本 / G3合同设计进度

**G2 HUMAN PROTOTYPE APPROVED (bounded)/G3 TECHNICAL DRAFT/G3.5 BLOCKED/G4 NOT STARTED FOR NEW CHAIN。** 用户明确“原型确认，继续开发”；已现场比对mes-demo九份源码 SHA256 9/9 MATCH，保存 [G2精确批准记录](docs/development/decisions/INCOMING_CROSSROLE_G2_20261019_APPROVED.md)，不扩大到未确认的检验结果/报告界面。已读取 V016 实际约束、既有 QMS 命令和 WMS 库位/容器/单位流水，形成 [G3受控详细设计候选](docs/development/INCOMING_CROSSROLE_002_G3_DETAILED_DESIGN_DRAFT.md)、[不可执行V067数据库迁移草案](docs/development/INCOMING_CROSSROLE_V067_PROPOSED_DO_NOT_RUN.sql)及 [G3.5交接草案](docs/development/inspection-crossrole/g35-development-handoff-DRAFT.json)；表结构与真实GxP角色/签名和DB preflight仍需核实，当前不能宣称G3/G3.5通过或执行正式库存写入。已有独立检验记录T1 READY FOR ACCEPTANCE保持原样。没有修改正式QMS/WMS代码、Flyway或数据库，没有Git提交/推送。

## 2026-10-19 来料质量跨岗位G2定向回归 / 真实开发仍待冻结

**G2 IN REVIEW / G3–G5 NOT READY。** 已在 mes-demo 复用确认的收货明细：逐批一键自动请验、成功变查看；QC补标准和计划并在隔离存储中同时登记取样单及待样品检验任务；取样员提交实际记录不扣库存，仓管确认后原型单次扣减；QC取样任务及待样品检验任务均可在原型列表定位，单位沿用该批收货单位；完整菜单已接入。回归 **19/19**、浏览器四尺寸六页 **24张截图**、整页溢出0、vue-tsc/Vite exit0。仍缺多来源库位/容器真实ledger及批准单位换算、正式六位精度、服务器权限/签名、样品接收后检验项目填写和报告批准打印、完整UIUX清单及本精确G2源码人工确认；正式QMS/WMS联调尚未执行。不能把隔离存储自动登记双单当作真实MariaDB原子事务、不能把原型扣减当作真库存。已有正式检验记录T1保持 READY FOR ACCEPTANCE/等待人工终验。产品经理本轮报告： [G2闭环进展](docs/review/INCOMING_CROSSROLE_G2_PROGRESS_20261019.md)，哈希/证据：`mes-demo/evidence/incoming-crossrole-sampling-g2-20261019/FINAL_G2_STATUS.json`。未提交Git、未改正式DB/Flyway/权限。

## 2026-10-19 取样与仓管出库岗位拆分 G2 定向进展

**G2 REWORK IN PROGRESS / 正式WMS扣减G4 NOT AUTHORIZED。** 按用户已确认的跨岗位来料质量流程，mes-demo原型已将旧“取样员完成立即扣库存”拆成检验员提交实际取样记录（库存不动）→仓管核对后独立确认扣减一次。vue-tsc/Vite build exit0，真实浏览器7/7定向正向检查通过；原型样例125.000kg→仓管确认实际取样0.200kg后124.800kg，刷新只保留一笔模拟流水，0真实API写。负向测试0/1：测试脚本误从未初始化sessionStorage读null发生TypeError，后续负向场景未执行，已留原错误，不作为PASS。完整菜单、受控单位换算/多库位、真实角色权限和电子签名、收货自动请验、QC确认两单及报告衔接尚未完成G2。完整G3/G3.5/G4/G5不得启动。已有正式检验记录T1 READY FOR ACCEPTANCE独立保留。见 [产品经理定向报告](docs/review/INCOMING_CROSSROLE_SAMPLING_G2_PROGRESS_20261019.md)。本轮没改正式MES业务代码、迁移、权限或数据库，没有Git提交/推送。

## 2026-10-19 检验记录正式接入完成验证 / READY FOR ACCEPTANCE / 人工终验待确认

用户已明确批准G2-20261019并授权补齐冻结只读合同、正式开发及验证。批准Vue组件与控制器逐字复用，正式5173检验记录列表已接8080真实API；样品来源业务名称、物料批号前两列，样品编号第三，检验单号使用实际关联报告号，不以任务号冒充。服务端筛选/计数/分页、多报告关联、组织隔离、原始FAIL和既有权限保持。后端9/9回滚集成测试、前端48文件209/209、正式build exit0；浏览器17项只读检查，四尺寸29截图加hover/focus两图均逐图复查，31/31完整性校验PASS；G3.5 READY。查询按钮第一次颜色覆盖未生效后按6A暂停，用户明确批准“仅修该项并完成复验”，第6轮局部主色恢复后default/hover/focus均5.72:1，测试构建重跑通过。零新增迁移、零业务数据写入、不改角色/全局CSS/既有签名命令，未提交推送，其他CR候选不扩权。保留两个非阻断提示：加载暂沿用通用空态说明、200%下紧凑下拉值需展开完整读取；不称无缺陷或人工产品经理已验收。证据：[正式交付结论](docs/review/INSPECTION_T1_FORMAL_ACCEPTANCE_20261019.json)、[审查报告](docs/review/INSPECTION_T1_FORMAL_REVIEW_20261019.md)、[RTM](docs/development/inspection-t1/formal-acceptance-rtm.csv)、[同一正式UIUX清单](docs/review/evidence/INSPECTION_T1_FORMAL_20261019/audit-manifest.json)。下文状态均为历史，不覆盖本条。

## 2026-10-19 检验记录：G1已确认 / G2可操作版已实现 / 正式接入未完成（历史）

用户“原型确认，继续开发到收尾”对应前一轮展示的G1 v0.7检验记录列表，已记录批准源哈希与范围，未扩展批准其他跨岗位页面。已实现可复用Vue T1列表、筛选/重置/分页/返回保持/空态/失败重试/请求竞态与动作能力检查，最终5/5模型测试与vue-tsc/Vite构建exit0，真实1440/1280/900/375截图已逐图审查。当前填写/复核仅对应身份摘要，**非正式工作流；G2精确Vue版本人验及完整UIUX/真实权限待完成，G3/G3.5未就绪、G4/G5未运行**。现有正式列表缺少样品业务名称/批号/关联报告号完整只读投影，不造字段或把任务号当报告号。未修改正式业务源码/API/数据库/迁移/权限，未提交推送，不冒称产品经理验收或开发收尾。详见 [G2交付及剩余合同](docs/review/INSPECTION_T1_G2_20261019.md) 和 [精确源版本](docs/review/INCOMING_CROSSROLE_002_DESIGN_HANDOFF.json)。以下旧状态仅历史。

## 2026-10-19 G1-v0.7 检验记录最终设计提交审阅

**G1 FINAL DESIGN SUBMITTED / AWAITING HUMAN REVIEW；G2–G5 NOT APPROVED。** 已按最新要求将样品名称、物料批号放在前两列，样品编号第三；检验单号显示关联报告report_no，未生成如实提示，任务号留详情。查询默认全部与四条记录一致，1440/1280查询重置同排；修正样品名称无来源后缀、总取样量/检验样数量混淆、T3原始判定旧文案及所有行打开首条样品。真实浏览器1440/1280/900/375四图HTTP200、JS0，已逐图审查；第二条样品身份对应核对完成。当前仅最终G1审阅稿，不宣称产品经理验收通过。静态稿查询/填写/复核/权限/错误恢复及正式DTO均待G2/G3；正式源码/API/DB/迁移未修改，既有数据和历史失败保留。证据：[v0.7审查清单](docs/review/evidence/INCOMING_INSPECTION_T1_G1_V07_AUDIT.json)，源SHA256 `37397CCD62E20E55BEE46DEED53D8EFC2557F2F1B1DD6F222920592DC8B438DC`。

## 2026-10-19 G1-v0.6 检验记录T1用户视角业务审查

**G1 REWORK IN PROGRESS / PRODUCT MANAGER APPROVAL NOT GRANTED / G2–G5 NOT AUTHORIZED。** 用户指出功能审查是第一关、业务审查同样必要，并明确样品名称和批号最重要、检验单号即检验报告号。真实1440浏览器首轮确认旧稿缺少样品名称/样品编号、查询拥挤、日期和操作裁切、让步接收提前覆盖原始不合格等关键缺陷。已仅修改未冻结 `mes-demo/public/design/incoming-crossrole-002.html` 与 G1规格/RTM/复核状态：样品名称→样品编号→物料批号置首，查询压缩为样品身份/任务状态/检验人/类型，操作列固定可见；样品名称明确为 `qms_sample` 检验对象/来源派生业务投影，不能把物料名称换标签；报告列表使用“检验单号（报告号）”，任务号仍为任务号。正式跨表只读DTO缺口标记G3 DESIGN_CHANGE_REQUIRED，未改正式源码/API/DB/权限。1440修后复查可见，完整四视口/状态/UIUX证据尚未完成，**不得宣称产品经理验收通过**。见 [G1规格](docs/development/INCOMING_CROSSROLE_002_G1_PAGE_SPEC.md)、[RTM](docs/development/INCOMING_CROSSROLE_002_RTM.csv)、[复核记录](docs/review/INCOMING_CROSSROLE_002_G1_STATIC_REVIEW.md)。

## 2026-10-19 G1-v0.5 四张质量列表对齐正式物料列表及完整Demo菜单

**PARTIALLY COMPLETED / G1 VISUAL REVIEW_REQUIRED / G2–G5 NOT APPROVED。** 用户批准QC四张列表按正式 \`MasterListView.vue\` 与Global UI V2 T1规范，整套Demo采用 \`prototype-navigation.ts\` 完整六组45叶子菜单。已调整 \`mes-demo/public/design/incoming-crossrole-002.html\` 的列表卡片、表格、筛选及菜单；基础1440px结构4/4 PASS，截图已目视审查。当前检验记录宽表的右侧操作列和日期筛选仍有截断，本轮两次针对性补丁遇相同脚本定位断言失败，未覆盖正式实现；遵守anti-loop门禁停止，G1不宣称通过。见 [本轮设计回顾](docs/review/INCOMING_CROSSROLE_002_G1_STATIC_REVIEW.md) 及 [G1版本状态](docs/review/INCOMING_CROSSROLE_002_DESIGN_HANDOFF.json)。未变更正式 MES 源码、API、数据库、权限或历史内容，未提交/推送。

## 2026-10-19 G1-v0.4 四张 T1列表及综合检验结论补漏

**G1 STATIC v0.4 GENERATED / G2 PROTOTYPE NOT APPROVED / G3–G5 NOT AUTHORIZED。** 用户指出此前G1仅有详情/操作稿，遗漏质量管理请验单、取样记录、检验记录、检验报告四张T1列表，新增检验单类型与合格/不合格/让步接收综合结论。已在`mes-demo/public/design/incoming-crossrole-002.html`加入四张列表，带查询状态/日期、数据列表/分页/查看入口；检验记录T1/T3含类型、批量、取样量及相关日期，综合结论有未完成待判定；让步接收作为需批准质量决定的G1候选，不覆盖FAIL、未获QA批准不放行。1440px静态浏览器4张列表+6张业务详情均完成基本布局检查，4/4+6/6、JS0；此为G1静态，不是可操作Vue G2和数据库集成验收。设计、DCP、RTM及源码hash更新于 [G1复核](docs/review/INCOMING_CROSSROLE_002_G1_STATIC_REVIEW.md)、[设计合同](docs/development/INCOMING_CROSSROLE_002_G1_PAGE_SPEC.md)、[交接状态](docs/review/INCOMING_CROSSROLE_002_DESIGN_HANDOFF.json)。保留既有质量菜单、正式WMS/QMS、已执行迁移及历史数据不变。

## 2026-10-19 用户最新决定：收货一键生成请验单

**G0 BUSINESS DECISION CONFIRMED / G1-v0.3 STATIC DRAFT / G2–G5 NOT RUN FOR THIS VERSION。** 已确认收货页面按明细物料批次直接「生成请验单」并自动提交QC，不需要弹窗/手动填写；成功后查看、允许时修改。QC继续原有请验单列表/编辑，QC确认标准、计划取样量、单位后同事务生成取样单及待样品检验任务；取样员记录实际量后由仓管核对出库扣真实库存；检验任务逐项目填写、综合判定、独立签名复核，检验报告可查询/统一打印。已将最新决策追加至 [DCP候选](docs/development/decisions/DCP-INCOMING-CROSSROLE-FLOW-002-PROPOSED.md)、[G1页面合同](docs/development/INCOMING_CROSSROLE_002_G1_PAGE_SPEC.md)、[RTM](docs/development/INCOMING_CROSSROLE_002_RTM.csv)；本机新版G1静态页面及6图核查记录见[设计复核](docs/review/INCOMING_CROSSROLE_002_G1_STATIC_REVIEW.md)，哈希见[交接状态](docs/review/INCOMING_CROSSROLE_002_DESIGN_HANDOFF.json)。尚未获得本版静态设计和可交互原型确认，**不得修改正式业务API/DB/迁移、权限或自动QA放行**。

## 来料质量跨岗位流程修订 — 2026-10-19

**G0 用户岗位与业务顺序已明确 / G1 PRODUCT DRAFT / G2 HUMAN APPROVAL PENDING / G3–G5 NOT AUTHORIZED。** 用户明确修正：仓管员填写并提交请验给QC（仓管不填质量标准）→QC补充批准检验依据/标准、计划取样数量及单位，**确认请验时同时生成取样单和待样品检验任务**→检验人员到仓库实际取样并提交取样明细→**仓管员**确认样品出库，按实取数量扣减来源物料批/库位库存→样品交接后检验人员逐项目填写原始数据、结果和判定→独立复核/批准检验报告，QA放行独立。此前DCP-INCOMING-SAMPLING-OUTBOUND-001中的“取样员完成即扣库存”候选被本次更新业务决定替代，旧证据保留。源代码核对发现当前请验创建即必填qcSpecificationVersionId、QC accept只改状态、检验任务create必填RECEIVED sampleId、取样complete由取样员直接完成，无仓库出库确认。已在新[受控DCP候选](docs/development/decisions/DCP-INCOMING-CROSSROLE-FLOW-002-PROPOSED.md)逐项记录CR-01..07、[G1页面合同](docs/development/INCOMING_CROSSROLE_002_G1_PAGE_SPEC.md)及[RTM](docs/development/INCOMING_CROSSROLE_002_RTM.csv)，并记录SHA256[交接状态](docs/review/INCOMING_CROSSROLE_002_DESIGN_HANDOFF.json)。严格先G1设计确认、再按同源规格G2 Vue交互原型及人验、G3/G3.5冻结/交接后才允许改正式API、库表、权限或WMS扣减。现有QMS/QA/eBR/收货验收不重开，本轮没有修改正式业务代码、数据库、迁移、权限或推送Git。
## 取样出库A（待验原辅料按实际取样扣减）— 2026-10-19

**G1/G2 候选原型已生成；G2人验及完整UIUX清单未完成，G3/G3.5/G4/G5未授权/未运行。** 用户已明确批准业务A；DCP仅PROPOSED。mes-demo新增取样出库T1/T2/T3交互原型，按已存在QMS取样任务显示批次与库存，保存明细不扣减、完成时原型一次扣减、仍待验禁用。原型构建exit0，浏览器主链11/11、负向4/4，产品规格/RTM/责任账本校验exit0。没有正式WMS/QMS源码、迁移、DB写入或Git提交。精确版本/截图/剩余审查见 [G1G2报告](docs/review/INCOMING_SAMPLING_OUTBOUND_A_G1G2_20261019.md)。

## 取样出库 A 方案原型与合同门禁 — 2026-10-19

**G0 BUSINESS A CONFIRMED / G1 DRAFT / G2 REWORK_REQUIRED / G3–G5 NOT READY。** 用户明确取样出库＝从待验原辅料物料批次按实际取样数量扣减库存，并关联取样任务/明细；已记录[有界DCP草案](docs/development/decisions/DCP-INCOMING-SAMPLING-OUTBOUND-001-PROPOSED.md)，未冻结。mes-demo 已存在 T1/T2/T3 隔离取样原型（#/quality/sampling-outbound/list）且 vue-tsc/Vite build PASS，但本轮浏览器 T2→T3 跳转及另一轮断言仍有缺陷；只能记 G2 返工。产品经理发现原型固定千克/3位精度以及未覆盖多库位容器，不能直接当正式WMS扣减合同。提出的“取样完成签名后同事务、按逐容器实际取样量扣减一次、足额检查、幂等、原批次继续待验/BLOCKED”目前为需与原型共同确认的 G1 候选，未修改正式API、迁移、权限或历史记录。完整[产品经理复验报告](docs/review/INCOMING_SAMPLING_OUTBOUND_A_PM_REVIEW_20261019.md)。禁止跳过精确G2人工验收和G3/G3.5基线冻结。
## 来料请验—取样—检验—报告产品经理全链（2026-10-19）

**G0 PARTIAL BLOCKED / 既有链路可定向复验；取样出库 DESIGN_CHANGE_REQUIRED。** 本轮已真实核对 v1.0.23、来料质量批准合同、QC对象单字段决策、现有四模块前后端路由及权限；前端定向22/22通过，后端隔离回滚IncomingQualityIT执行通过。已生成业务G0/RTM/双设计任务草案/责任账本。用户新增的取样出库未在冻结合同发现独立库存动作；必须先确认是原辅料取样扣库存还是样品从暂存区出库交检验室，不自行新增单据、迁移、权限或库存流水。未取得该决策及确切原型人验前，G3.5/G4/G5不可标完成。见 [产品经理G0](docs/review/INCOMING_QMS_BPM_G0_20261019.md)。

## 物料收货正式开发与收尾 — 2026-10-19

**CURRENT: DEVELOPMENT CLOSEOUT IN PROGRESS / 正式原辅料库收货→逐批请验→PDF→附件下载链已通过 / 尚未整体最终验收。** 承接原型直接开发授权及年月四位批号规则，不重做原型。本轮在正确原辅料库(ID1)新建SH-20261019-231415(ID485)，两批2026100003/0004已实际保存、刷新、编辑检查并确认；物料批459/460保持待验/BLOCKED。第一批常规、第二批加急请验220/221均真实DRAFT、采用已批准质量标准版本22；修复原型遗留HIGH值与正式URGENT合同不符，从既有schema派生选项。真实已发布模板69生成46,968字节PDF，预览/下载/附件132上传关联与下载SHA256一致。旧466仓库/确认事实保持不变。数量末尾零、UTC界面显示、历史到货空值、操作td背景、空态可见性、横滚提示、请验错误聚焦已修复；不改payload含义。后端隔离回滚21/21、最新前端3文件22/22、正确仓库实时收货9/9、文档链10/10、加急3/3、定向UI17/17及最新build通过，服务器最终只读校验通过。当前V066无新迁移，org1编号规则启用且不重置，未改角色/放行/发布模板历史，未提交Git。**剩余：模板69仍旧四列且原UTC日期，未完整包含生产企业/生产批号；需按既有追加版本流程对齐。完整正式G5/更广并发角色回归及用户最终验收未完成，不冒称ACCEPTED。** 见[本轮实际收尾](docs/review/RECEIPT_CLOSEOUT_20261019.md)、[编号批准](docs/development/decisions/RECEIPT_NUMBER_YYYYMM4_20261019.md)。所有历史失败/报告和其他工作区改动保留。

## 全系统 T1/T2 UI 统一审查 — 2026-10-13

**CURRENT: IN PROGRESS / HUMAN RECHECK REQUIRED。** 用户于 2026-10-13 追加确认：本项目只按桌面端处理，普通 T1 列表查询控件必须缩小，查询操作尽量与条件同行；所有新增/编辑页的子表新增动作统一放在对应标题栏右侧并使用蓝色主按钮，正文下方/左侧不得保留重复入口。已修改共享 Global UI V2 查询密度规则，并补齐字典下拉、业务引用、WMS/工艺/领料选择器等公共组件外层宽度，关闭供应商资格状态和物料类型下拉缩成箭头框的回归；随后补齐 `ProcessLookup` 组件自身的全宽契约，并固定生产处方配料表的业务列宽。子表标题栏规则已写入 Global UI V2、Page Template Standard V2 和 T1/T2 决策记录，并已应用到生产处方、WMS收货/出库/领料申请、物料供应商/单位换算、QC检验项目及eBR册编辑器。当前 typecheck PASS；此次源码哈希已经变化，原完整 G5 清单不再代表当前源码，尚未完成本次子表动作的真实5173全量桌面矩阵及完整 G5 manifest，因此不得沿用旧 `READY FOR ACCEPTANCE`。见 `docs/review/GLOBAL_T1_QUERY_DENSITY_20261013.md`、`docs/review/PRODUCTION_PRESCRIPTION_LOOKUP_WIDTH_20261013.md`、`docs/review/T2_CHILD_TABLE_HEADER_ACTIONS_20261013.md`。

**历史变更前 G5 记录：** 用户确认普通列表全部 T1、普通新增/编辑全部 T2、特殊功能按业务职责保留 T4/T5/T6/专用编辑器。本轮已将该规则写入 Global UI V2/Page Template Standard 和独立决策记录，并实际整改用户/角色/部门/数据字典/菜单/打印数据源/生产处方/WMS/QC等偏差。部门管理原列表内新增/编辑弹窗已拆为独立 T2 create/edit 路由；生产执行模板标识纠正为 T5；手机端隐藏桌面 sider 仍生成 Ant popup 的幽灵菜单问题已关闭。最终真实5173矩阵：49个T1列表×4视口=196/196 PASS，30个普通T2×4视口=120/120 PASS，8个特殊页面×4视口=32/32 PASS，pageerror=0，mobile shell PASS；typecheck/build PASS。补审 GMP Audit Trail 与 Integration Operations 后，修复了 Integration 人工重试原因缺失/T1按钮顺序/状态筛选可测试性、Audit Trail T1壳层、移动导航幽灵菜单及测试契约漂移。最终全量前端 44/44 test files、186/186 tests PASS。完整 G5 manifest validator PASS，verdict=READY FOR ACCEPTANCE。见 docs/review/GLOBAL_T1_T2_UI_AUDIT_20261013.md。

## 生产工艺动态表单 CPP 规则设计 — 2026-10-13

**READY FOR ACCEPTANCE / HUMAN ACCEPTANCE PENDING。** 用户批准的动态表单 CPP 精确原型已完成正式增量集成：保留既有 Canvas Editor/字段/签名/复核，新增“表单设计 / CPP规则设计”双页签；CPP 只引用当前表单字段、不绑定产品、不含设计期运行结果。物理层复用 `ebr_rule_def`/安全 DSL，仅追加 V064 `cpp_config_json`，不建立第二套规则引擎或 CPP 三表。最终验证：前端 build PASS、44/44 test files / 186/186 tests PASS；`mes-ebr` 61/61 PASS；CPP配置真实数据库事务回滚往返 IT 1/1 PASS；mes-boot package PASS；DEV Flyway 64 migrations validate PASS、当前 V064；当前源码 8080 health=200；正式5173/8080 G5 20/20 screenshot coverage、manifest validator PASS，verdict=`READY FOR ACCEPTANCE`，console/page failures=0；2026-10-14 表单设计字段库已统一改为“字段前独立 + 图标 → 按当前 Canvas 光标/选区插入”，并重新 build/G5 PASS。当前正式 UI 组合 SHA-256 `0D5B011F24A6F81B0FBC119CA688E20F79F8CE692760872F3A7A04E9234D61EB`。待用户最终人验，未标记 `ACCEPTED`。见 [实现收尾](docs/review/PROCESS_DYNAMIC_FORM_CPP_IMPLEMENTATION_20261013.md) 和 [设计锁](docs/development/decisions/PROCESS_DYNAMIC_FORM_CPP_PROTOTYPE_20261013.md)。

## 2026-10-12 收尾执行顺序（用户确认）

严格按以下顺序执行，不并行开启后续业务任务：
1. 角色/权限 T1+T2 整改与四视口复验；
2. Global UI V2 公共缺陷修复与全局回归；
3. 打印模板列表最终验收；
4. ROLE-ID / 电子签名关闭；
5. MES_TASKS 历史/当前状态清理；
6. QC_OBJECT_TYPE 收尾；
7. 基础资料有据补齐；
8. 产品/成品完整链收尾。

当前进度：第1～6项已完成/关闭。第7项基础资料的所有有据可执行维护已完成并复验；剩余储存条件、有效期、11条包装来源、8类供应商及羟苯乙酯供应商评审均缺真实外部资料，明确保留为空/待评审，不允许杜撰或自动批准。第8项已建立真实业务格式DEV场景：三种生产状态及一条完整成品链已持久化，5173/8080回读和业务命名扫描PASS；该场景用于页面/集成测试，不冒充实际生产或正式电子签名证据，正式全链仍待真实登录/真实二次认证端到端验收。

## 当前权威状态索引 — 2026-10-13

> 本节是 `MES_TASKS.md` 的当前状态唯一入口。下方同名任务的旧 `BLOCKED / IN PROGRESS / NOT ACCEPTED / READY` 段落均作为历史执行证据保留，**不得覆盖本节较新的状态**。后续状态变化只先更新本节，再追加证据记录。

| 当前任务 | 当前状态 | 当前依据 |
|---|---|---|
| 生产工艺动态表单 CPP 规则设计 | READY FOR ACCEPTANCE / HUMAN ACCEPTANCE PENDING | 原型已批准并完成正式集成；V064/64 Flyway PASS，前端186/186、mes-ebr 61/61、CPP配置DB往返IT 1/1、mes-boot package PASS，8080 health200；正式5173/8080 G5 manifest 20/20 coverage PASS，当前 UI hash `0D5B011F24A6F81B0FBC119CA688E20F79F8CE692760872F3A7A04E9234D61EB`；仅待用户最终人验 |
| 全系统普通 T1/T2 统一 | IN PROGRESS / HUMAN RECHECK REQUIRED | 2026-10-13已补齐T1查询和T2表单公共下拉/引用组件宽度，并将“新增/编辑页子表新增按钮位于标题栏右侧、蓝色主按钮、无正文重复入口”固化为全局规范并应用于已盘点普通页面和相关专用编辑器；typecheck、定向7文件/35测试PASS，真实5173已检查1440/900代表页面及新增行交互；当前源码尚未完成Formal G5全量桌面矩阵，旧READY仅对应变更前源码 |
| 角色/权限 T1+T2 | READY FOR ACCEPTANCE | 已纳入全系统T1/T2统一矩阵；独立RoleDetailView 11/11；真实四视口/折叠/按钮对齐PASS |
| Global UI V2 公共缺陷 | PASS / CLOSED | 控件边框 #8895a5≈3.048:1；结构边框不变；四视口与build PASS |
| 打印模板列表 | READY FOR ACCEPTANCE | Formal G5 validator PASS；最终四视口/空态/动态播报证据齐全 |
| 统一打印流程 | READY FOR ACCEPTANCE / HUMAN ACCEPTANCE PENDING | 当前源码单条/批量、预览/下载、重复/取消重开、断网/无模板、键盘、匿名401和既有受限账号真实403均有证据；文字对比度与200%纯文本缩放四视口PASS；Formal G5 36/36、validator PASS；无新增打印归档 |
| ROLE-ID / 电子签名 | READY FOR ACCEPTANCE | 核心/安全/Redis/真实非管理员403/Formal G5 全部通过 |
| MES_TASKS 状态清理 | PASS / CLOSED | 已建立顶部唯一当前状态入口；冲突旧段明确为历史/被最新验收覆盖 |
| QC_OBJECT_TYPE | PASS / CLOSED | 单字段最小范围已用户验收；V058撤销空绑定表；当前Java无Binding运行时引用；QcInspectionObjectDictionaryIT 2/2，DEV Flyway 62 validate PASS |
| 基础资料 | EXECUTABLE SCOPE CLOSED / EXTERNAL SOURCE GAPS | 有据7物料+2单位+10换算已落地；MATERIAL_TYPE成品项已停用；Legacy字典维护7/7 PASS；16条储存/有效期等缺真实来源继续留空 |
| 产品/成品完整链 | REALISTIC DEV DATA SEEDED / LIVE-AUTH ACCEPTANCE PENDING | 已持久化DRAFT/IN_PROGRESS/QA_RELEASED三批及完整成品入库→请验→取样→检验→报告→QA放行→库存→发货链；5173/8080回读PASS；正式验收仍需真实登录/真实二次认证端到端 |
| 打印数据源配置 | IMPLEMENTED / SEPARATE ACCEPTANCE HISTORY | 正式路由、页面、后端及数据源配置已存在；2026-10-10“正式开发未开始”仅为历史阶段记录 |



## 打印模板列表 T1 验收 — 2026-10-12

**READY FOR ACCEPTANCE / FORMAL G5 PASS / NOT HUMAN ACCEPTED。** 正式5173页面已按T1统一并移除标题说明；1440/1280/900/375最终截图、四筛选标签控件同行0px、Enter查询/空态/重置、真实printing接口200、JS错误0、typecheck/build PASS。Global UI V2独立控件边框#8895a5对白色约3.048:1；900/375增加左右滑动提示；结果计数role=status+aria-live真实从共1条更新至共0条。完整G5 manifest validator PASS，verdict=READY FOR ACCEPTANCE。见 docs/review/PRINT_TEMPLATE_LIST_ACCEPTANCE_20261012.md。

## ROLE-ID / 电子签名验收 — 2026-10-12

**READY FOR ACCEPTANCE / CORE FUNCTION + SECURITY + FORMAL UI G5 PASS / NOT HUMAN ACCEPTED。** 真实5173规则与签名页四视口14组角色选择器同行、无页面级横向溢出；管理员role-options 200，现有非管理员真实登录且无ebr:template:view时role-options 403。RolePicker8/8、前端定向57/57、EbrRoleSchema12/12、RoleIdSession3/3、CurrentSigningAuthority6/6、Signature13/13、Reauth2/2、真实Redis11/11全部PASS；8080 health200。ROLE-ID角色目录新增role=status+aria-live，控件边框#8895a5满足3:1；完整G5 manifest validator PASS，verdict=READY FOR ACCEPTANCE。旧one-shot guarded deployment harness本轮仅因正式8080占用而无法重复启动，未进入业务断言，不记产品回归失败。见 docs/review/ROLE_ID_SIGNING_ACCEPTANCE_20261012.md。

## PRINT-ONCE-001 — 2026-10-12

**2026-10-13 最新收尾：17条历史模板物理清理已完成／当前69已核实／完整打印流程G5仍未完成／NOT ACCEPTED。** [最终执行及恢复连接后只读复核](docs/review/PRINT_HISTORY68_FINAL_EXECUTION_20261012.md)。原17目标 `7,8,9,10,13,14,29,30,31,32,33,40,41,42,43,44,68` 实库均不存在；仅69“收货记录打印”第2版PUBLISHED、versionNo2，绑定50→69唯一启用，DOCX/预览摘要正确。最后68/49事务于2026-10-12 10:59:35UTC提交，各删除1行，137表保留字节与402外键独立检查通过；19份旧PDF598,008字节今日逐项只读匹配保存的下载/摘要证据。V060–V062成功记录/checksum匹配，当前最高V064属于后续工作，本轮不改迁移。保存的21项HTTP200及唯一69页面显示证据已核对；69干净正文渲染属于正常版本阶段证据，最后删除后新渲染断线未确认完成，本轮仅补文档不重跑。备份4,970,075字节/SHA256核验匹配，等效恢复重建通过、实际恢复未运行。累计修复仍7/10，不重放删除、不写业务数据、不改并行T1/其他代码、不提交推送。打印模板列表T1另有[独立G5通过记录](docs/review/PRINT_TEMPLATE_LIST_ACCEPTANCE_20261012.md)，仅限列表，予以保留；完整业务打印流程仍缺状态/视口、错误恢复、键盘焦点/WCAG、对抗复查及清单/集成绑定等证据，不冒称整体READY/ACCEPTED。

**2026-10-15 统一打印当前状态：BLOCKED / G5 MANIFEST VALID / NOT ACCEPTED。** 用户批准追加修复窗口后，历史10轮保留，当前窗口9/10、累计19。现源码定向前端15/15、后端15/15、typecheck/build、真实HTTP7/7、CUAWright单条/批量/四视口错误恢复与键盘均PASS；PDF.js可见预览，单条及25→22双记录下载与响应SHA一致，双记录2页/中文正文/顺序确认；数据库和打印归档前后指纹相同，无新增artifact/batch/source-evidence/snapshot/signature/audit。完整27/27截图清单经validator exit0，但manifest verdict仍FAIL：无可用已授权受限账号，真实403 NOT RUN，匿名401不能替代；新鲜对比度与200%纯文本缩放亦保守NOT RUN。未创建账号或授权，未删除物料/模板，未增加页面级入口配置。详见[打印集成报告](docs/review/PRINT_INTEGRATION_20261015.md)、[追加修复报告](docs/review/PRINT_REPAIR_WINDOW_20261015.md)及`docs/review/evidence/print-repair-window-20261015/integration-acceptance.json`。

**轮次最终纠正：** 上段打印状态中的“窗口9/10、累计19”是生成G5清单时的阶段值；窗口10仅纠正HTTP证据旧轮次元数据，最终为窗口10/10、累计20，已达上限并停止后续自动修复。功能、测试及BLOCKED结论不变。

**2026-10-16 第三批准窗口最终状态：READY FOR ACCEPTANCE / HUMAN ACCEPTANCE PENDING。** 用户批准新窗口最多10轮，历史累计20保留；本窗口实际5/10、总累计25。最终源码真实CUAWright已补齐第二页正文浏览器截图、批量弹窗及检验报告面板四视口实际文字对比度与200%纯文本缩放；既有非管理员真实登录确认无打印生成权限后POST返回403。局部修复仅涉及三个共享打印组件的主按钮对比度、按钮/选择器内容驱动高度和placeholder可读性；前端定向15/15、build PASS，数据库/打印归档前后不变，无账号/权限/模板/数据源/物料/业务记录写入。Formal G5 manifest 36/36、validator exit0、verdict=`READY FOR ACCEPTANCE`，集成报告引用同一清单：`docs/review/evidence/print-repair-window3-20261016/`。未增加页面级打印入口配置或打印归档，尚待用户最终人验，未标记ACCEPTED。

以下为2026-10-12分阶段历史事实，保留原文；其中旧68保留、规范化阻塞或删除待批准等描述已由上述最终收尾状态取代。

**BLOCKED FOR FORMAL UI ACCEPTANCE / DEV CODE AND EXACT CLEANUP COMPLETED / NOT ACCEPTED.** [Execution report](docs/review/PRINT_ONCE_EXECUTION_20261012.md). V060 applied checksum-1875766076;2026-10-12 08:21:06 UTC exact10 templates+10 disabled bindings committed,137-table retained-byte and402-FK independent checksPASS;18 oldPDF549200B downloads200/hash unchanged; single/batch return directPDF with zero print persistence/audit additions. Backend23 units+3 DEV readonly IT/front3 tests/buildsPASS, current8080healthUP. Remaining7 templates explicitly preserved including6 test-named unpublished and68 published; targetIDs absent. G5 auditFAIL (matrix incomplete/embedded preview blank), so overallintegration acceptanceFAIL and noREADY. Prior repaircount4 retained; no production/signature/rights change or commit/push. Follow-up: actual frontend download saved/hash equal; two real selected records returned200 directPDF and nativePDFBox contents/order/CJK match live records. AnonymousGET401; real narrow-role403NOT RUN. Cumulative repairs5/10; nativePDFwindow DOM/URL checks still fail, item stopped. G5 remainsincomplete/FAIL;6-template deletion and68 rename pending approval, not executed. [Latest content/download proof](docs/review/evidence/print-once-20261012/pdf-render/content-checks.json).

**2026-10-12 remaining cleanup: SIX DELETIONS COMMITTED /68 NORMALIZATION BLOCKED / NOT ACCEPTED.** Human Sentinel_88767a63e37c81918a08f7c75b92cb38;[report](docs/review/PRINT_REMAINING_EXECUTION_20261012.md).V061checksum-1234558906/61 migrationsvalidated;10:17:24UTC six exact unpublished templates7,8,9,13,14,29 deleted, other deletes/updates0;137-table retained-byte and402-FK independentchecksPASS.Only68PUBLISHED remains, all68/b49 bytes unchanged;19oldPDF598008B download200/hash equal.8080PID27736healthUP;single/batch directPDF200/no new persistence/signatures/print audits. Fresh full backup5361866B/equivalent reconstructionPASS. Prepared clean-titleDOCX candidate, checkerPASS;original68 immutable-name/DOCX guard andarchive27 provenance prevent in-place correction without violating constraints, normal append-version changesID/binding/disposition;DESIGN CHANGE REQUIRED. Repairs7/10 retained, no stoppedpopup retry/noT1style modification/no commitpush. G5 andclean68-title gatesnotpassed;noREADY/ACCEPTED.

**Latest normal version update: CURRENT TEMPLATE69 CLEAN/PUBLISHED/ONE ENABLED BINDING — VERIFIED / NOT ACCEPTED.** Parent authorized normalversion workflow for sameapproved normalization;[execution](docs/review/PRINT_RECEIPT_NORMAL_VERSION_EXECUTION_20261012.md). Exactly one69revision2 created;6 normalAPIcommands200 (upload/realPDFvalidate/publish/newbind/oldunbind/deactivate68).69 name/body/native design title收货记录打印, same source19/revision22/DOCUMENT/fields/layout;binding50→69enabled,49→68disabled, onlyone active.Original68INACTIVE retainsoriginalname/DOCX/preview/hash/source/publication evidence; no deletion/guard bypass/newmigration. Single1page/batch2pages realPDF clean title/currentfacts/order verified locally, actualHTTP200/no newprint persistence.19oldPDF598008B download/hash andprotectedarchives/signatures50/rolepermissions/product/material/dictionary bytes unchanged;normaltemplate maintenanceaudit+6 allowed. Actual5173list shows69V2已发布 and68V1已停用 history; T1styles untouched. Repairs7/10 unchanged, no popupretry/commitpush. Current-name normalization blocker resolved;priorfullG5/reader/narrow403 unfinished, nofullUIREADY/ACCEPTED. Historical68 label retained by explicitscope, notglobalplaceholderzero.

## 基础资料有据补齐 — 2026-10-12

**PARTIAL / 有据增量已完成，整体缺资料项未关闭 / NOT ACCEPTED。** 用户要求处理基础资料补齐；已通过已有正式API新增桶/袋2个单位、补5种原辅料包装单位及规格、补2种标签用途规格、新增10条带materialId双向包装换算，共19次维护成功。五种外包装沿用既有开发收货场景，标签容量来自原批记录照片，不冒充供应商原始标签/资质。最终物料16且无成品，编号/ID/组织/原基础单位不变；产品2、供应商7/原8关系和资格结论完整保留；单位15、换算14，旧4通用换算不变，新5对乘积均1。独立新会话7条API回读+4次5173页面/刷新通过，JS错误0。storageCondition/shelfLifeDays16条仍缺来源，其他11条外包装未杜撰；8种药材/瓶/标签无供应商资料，羟苯乙酯现有供应商仍待评审，不自动批准。未改应用代码/迁移/环境/已验收权限、未重做成品清理，未提交推送；处方额外取数脚本被安全检查拒绝且未绕过，深层处方核验未完成。[数据边界与来源](docs/development/decisions/MASTER_DATA_COMPLETION_20261012.md)；[执行及剩余清单](docs/review/MASTER_DATA_COMPLETION_20261012.md)。本机before/after/逐操作journal及独立readback均保留。

## ROLE-ID-SIGNING-001 bounded implementation — 2026-10-11

**HISTORICAL / SUPERSEDED BY 2026-10-12 CURRENT ACCEPTANCE ABOVE。** [可决策核验记录](docs/review/ROLE_ID_SIGNING_READONLY_CLOSEOUT_20261012.md)。该段保留的是在真实非管理员403尚未补测时的阶段事实；后续已经使用现有非管理员账号完成真实登录并验证 /ebr/role-options=403，ROLE-ID/电子签名核心、安全、Redis与Formal G5均已达到 READY FOR ACCEPTANCE。原脚本FAIL和旧截图证据继续保留，不再作为当前状态。root12/eBR/成品链依赖仍属于产品成品完整链任务，不反向阻塞本 ROLE-ID/电子签名验收。

以下为此前部署时点记录，后续只读结论以上段为准，原失败证据不删除。

**IN PROGRESS / DEV JAR DEPLOYED / DRAFT291 CONVERSION COMMITTED / ACCEPTANCE BLOCKED / NOT ACCEPTED（2026-10-12）。** [本轮证据](docs/review/ROLE_ID_SIGNING_DEV_DEPLOYMENT_20261012.md)。现有DEV V059/checksum -503587429已校验，本轮未重复迁移。新完整本机备份5,980,902B，137表4175行等效恢复核验通过。当前main源码JAR打包PASS15.225s/989构建输入无漂移，正式8080 PID22620/health200 UP。真实CUAWright admin登录、角色选项和一次草稿291 PUT200，事务提交前137表严格比较：恰好85更新（15业务+70维护），子新增/删除0，6份schema_json原字节不变，4旧模板canonical/hash和50签名保留；COMMIT回调及独立根查询确认DRAFT/version_no3/businessVersion1/schema2。提交后原生137表快照比较断言通过，但完整备份保护脚本最终因sys_user实际version列被误写为version_no而FAIL；两次自动执行/验证修正后停止，不宣称完整保护PASS。真实1440选择/保存、发布模板6只读已检查；375截图裁切/角色面板不可见，视觉FAIL，14对角色标签控件DOM同行不足以代表验收。控制台401资源原因、真实非管理员403和正式JAR认证页面重开未完成。临时浏览器写模式false且关闭。仅测试事务守卫/本机执行脚本及证据新增，不改变既有页面设计/业务模型/授权；并行菜单/IAM/QC改动保留，无发布、增权、签名删除、成品链续跑、提交推送。

历史最终代码与隔离证据：[81/81及严格85行回滚](docs/review/ROLE_ID_SIGNING_ISOLATION_VALIDATION_20261011.md)。此前DEV V058/未部署结论已被本轮实际部署事实取代；已有失败证据继续保留。用户指定设计者确认页面处理及有界验证fixture修正后，才可补齐剩余门禁。

## 角色权限确认原型正式开发 — 2026-10-11

**ACCEPTED — HUMAN CONFIRMED（2026-10-12，用户明确回复：权限可以了）。** 本次人工确认限定于角色与权限配置：菜单可见勾选、只查看/管理、独立特殊权限、保存及重开回显，不扩大到电子批记录角色签名、其他模块或全系统验收。此前构建、专项测试及真实保存/菜单刷新验证证据保留。独立SQL核验和非管理员真实账号检查仍为NOT RUN，不因人工确认改写成PASS，也不删除历史失败。本次仅记录确认，无源码/数据库/授权/配置变更，无测试重跑、迁移、重启、提交推送。此功能不再重复开发或要求重新确认原型；新缺陷及后续回归单独记录。见[收尾记录第7节](docs/review/MENU_ROLE_CLOSEOUT_20261011.md)。

**视觉一致性回归修复（2026-10-12）：READY FOR ACCEPTANCE / TARGETED PASS。** 用户指出的正式页面与冻结原型差异已按回归缺陷整改：角色列表回归T1；新增/编辑T2底部四按钮同一Y；菜单树支持真实展开/收起（59→10节点）；1440/1280/900/375标签同行且无页面横向溢出。保留真实API、权限计算、乐观锁、安全提示和既有授权。typecheck PASS，RoleDetailView 11/11 PASS；Global UI V2控件对比度公共缺陷也已关闭。未写数据库/角色权限。当前状态以上方权威状态索引为准。见[回归修复记录](docs/review/ROLE_PERMISSIONS_PROTOTYPE_VISUAL_ALIGNMENT_20261012.md)。

## 七个角色编码规范化 — 2026-10-11

**READY FOR ACCEPTANCE（数据库改码范围） / DEV COMMITTED / NOT ACCEPTED（10:29 UTC独立复核）。** [精确用户/设计负责人授权](docs/development/decisions/ROLE_CODE_NORMALIZATION_AUTHORIZATION_20261011.md)，[事务及独立复核证据](docs/review/ROLE_CODE_NORMALIZATION_EXECUTION_20261011.md)。7个DEMO_MES_*角色编码改为对应MES_*，同roleID/中文名/成员/353权限关系不变；仅模板291的14条DRAFT规则同步，7+7子规则版本递增，根version_no1→2仍DRAFT/空批准hash。实际更新22行（7角色+14规则+1必要根元数据），真实本地维护安全审计追加22条、actor_user_id=NULL、不冒充API。首轮保护断言ROLLBACK；正常管理员LOGIN元数据漂移核查后按批准重试锁定同事务用户基线，COMMIT后解码异常先独立查证、无DML重放。137组保留检查无差异、402外键0孤儿；全部50签名完整内容与VALID状态、已发布模板/历史审计/归档不变。完整本机备份5947380B、SHA4a19a2f1…9810ded，137表4112行离线重构一致、未实际导入恢复。无账号登录名/凭据/权限变更、无迁移/部署/提交推送。受影响账号须正常重新登录；会话未自动刷新。role_id设计与签名清理仍独立未实施。

## QC 检验对象单字段纠偏 — 2026-10-11

**ACCEPTED — 用户于 2026-10-11 明确确认验收（仅本轮单字段纠偏）。** 用户明确仅增加一个可见「检验对象」字段，复用物料批次/成品生产批主键及原请验→取样→检验→报告→放行链。已精确撤回 IncomingQualityService 新增独立绑定逻辑、移出 InspectionObjectBindingService；旧 V055 不动，空绑定表经零行/零外键预检及备份后由 V058 撤销，Flyway58 validate PASS。Java test-compile/package PASS；质量后端定向单元13/13、字典集成2/2、前端定向24/24、typecheck/build PASS。真实 CUAWright 四视口单字段/标签同行、查询及新增对象选择 PASS；8080 原始请验/取样/检验/报告 13/11/9/5 行，指定 materialLotId=25 每链命中1条，孤儿数0、已批准报告5条保持；已更新后端新 JAR，health UP。成品请验现无记录，未执行成品质量完整新单据正向链，不宣称全系统验收。V056/V057 及其它并行未提交代码均保留，未提交/推送。锁定：[QC_OBJECT_ASSOCIATION_20261011](docs/development/decisions/QC_OBJECT_ASSOCIATION_20261011.md)；[本轮验收证据](docs/review/QC_OBJECT_ONE_FIELD_ACCEPTANCE_20261011.md)。

## 产品成品全链根因修复 — 2026-10-11

**兼容源代码首轮验证（09:15 UTC）：SOURCE TARGETED PASS / NOT DEPLOYED / BUSINESS WRITE BLOCKED / NOT ACCEPTED。** 设计负责人新批准最多2轮A–D，已记录有界设计；第1轮仅EbrDefinitions按保存发布schema字段存在性恢复历史canvas缺失输入，无试删hash/改旧摘要。原历史JCS及新canvas/篡改拒绝等18/18，实际DEV只读requirePublished(1,10)原hash1/1，0skip；无失败后修复重试。新完整备份5953960B/SHA d0ab4aca…e3249，138表4109行离线重建一致，未实际导入；25张保留表不变。版本创建/保存/lint/提交命令在启动前被自动审批拒绝：可识别人类授权仅数据库清理，受监管模板业务写入缺少直接授权；未绕过/重试，无新模板。同时仅admin有ebr模板批准权限，正常独立门禁不允许其自批，缺现有独立批准账号。230仍DRAFT/version0/无快照，633APPROVED/signature2021，DEV57；8080原JAR仍运行未部署本兼容源码。新版本与下游等待真实授权/账号依赖，未增加8项PASS。见[首轮源码与拒绝/账号证据](docs/review/PRODUCT_FINISHED_EBR_COMPATIBILITY_ROUND1_WRITE_BLOCKED_20261011.md)。

**只读根因核对（09:02 UTC）：BLOCKED / NOT ACCEPTED。** 模板10→历史工艺版本12→工艺根12→产品1由实际FK证明，归属正确；Oct9提交3c24d2e新增重建MIX_RECORD.canvasDocument=null，离线仅移除此字段即精确复现原发布deaf7958…707f，属历史canonical兼容性缺陷，未覆盖hash。另有新生产要求直接processPackageId的独立消费阻塞，产品1无可直接消费的EFFECTIVE模板；现有root12模板251/252仍DRAFT。最小待批准方案为从10正常创建新业务版本2、按MIX将operation29映射332、正常独立批准发布，再下达现有230；尚未执行。已有非formal打印归档27原GET200/48808字节/hash一致；历史eBR PDF/manifest/snapshot均0，不能伪报通过。25张保留表完整字节内容一致（原编码诊断错误及纠正证据均保留）；独立库存/目标页面既有检查通过，全链下游仍NOT RUN。无新代码/模板/业务写入/迁移/提交推送。见[精确只读根因、候选、最小方案及八项边界](docs/review/PRODUCT_FINISHED_EBR_READONLY_ROOT_CAUSE_20261011.md)。

**当前状态：BLOCKED / ROUTE UI DEPLOYED PASS / EBR DEFINITION DEPENDENCY / NOT ACCEPTED（08:48 UTC）。** 负责人批准路由/UI最多2轮，已用2轮；安全路由18/18、真实DEV跨组织消费查询1/1、前端10/10及typecheck/build通过，原失败保留。精确4条既有权限匹配已部署唯一8080 PID34308/JAR3aaf266a…b053e，health UP。实际计划633创建201/QA独立批准200（signature2021），库存GET200空态；实际无权限三写403/未登录401/批准计划PUT422冻结保护。移动列表6对及库存7对同排，详情390px无溢出/无内部工单外显。下达230现被422 LINT_FAILED Published definition hash mismatch阻塞：EFFECTIVE eBR10保存hash不符当前重建、实际仍旧package_version_id12且process_package_id NULL，无直接匹配现行root12发布模板；23张保留/EBR定义表内容不变。未覆盖hash或绕过门禁，批次DRAFT/version0/快照NULL、计划APPROVED、成品流水0，后续链未运行，见[本轮部署与新eBR阻塞](docs/review/PRODUCT_FINISHED_ROUTE_UI_DEPLOYED_EBR_BLOCKED_20261011.md)。两轮已用，新范围待负责人审核；未提交推送。

**只读复核补充（08:31 UTC，仍BLOCKED）：** 三条计划写路由及qms:plan:create/update/approve同时存在于v1.0.23冻结API、当前补充和Service实际权限检查；SecurityConfiguration遗漏method/path匹配，未修改配置/授权记录。实际批次230读取200，库存API对有wms:inventory:view的管理员及仓储仍403（同类GET匹配遗漏，另列待审，不自动扩大）。完成3页x2视口6张实际截图并查看：库存7对查询两视口同排但接口FAIL；生产列表移动状态几何FAIL；详情移动610px溢出及内部订单外显。CUAWright写模式false/浏览器closed，未新增业务数据或推送，详情见[精确只读路由复核](docs/review/PRODUCT_FINISHED_PLAN_ROUTE_READONLY_REVIEW_20261011.md)。

**当前状态：BLOCKED / REAUTH DEPLOYED PASS / PLAN HTTP403 / NOT ACCEPTED（08:26 UTC）。** 负责人明确追加仅1轮method写前非空校验；真实Redis11/11、相关安全9/9共20/20，0 skip，原失败断言保留。原checkout打包成功，唯一8080 PID31552/JAR4a15fc28…a5cfa，health UP；真实/auth/reauth200，标准316/版本314已独立签名批准（signature2020），正常批次230已创建DRAFT。管理员实际有qms:plan:create，质量计划POST仍403；SecurityConfiguration仅GET匹配，缺3条现有写操作权限映射落入denyAll。未自动修改安全配置或权限，停止后续链路待负责人决定。14张保留表内容不变，物料16/FINISHED0/产品2/FK1，计划及成品流水0。固定8项未整体完成，见[本轮部署及新阻塞证据](docs/review/PRODUCT_FINISHED_REAUTH_ROUND2_DEPLOYED_PLAN_BLOCKED_20261011.md)。历史失败和轮次保留。

**当前状态：BLOCKED / REAUTH EXTRA ROUND1 13 OF 14 PASS / NOT ACCEPTED（08:13 UTC）。** 负责人已批准追加1轮，仅Redis3.0.504令牌存储兼容及PRODUCT详情标签；设计记录PRODUCT_FINISHED_REAUTH_REPAIR_AUTHORIZATION_20261011.md。本轮真实Redis4/5、既有安全回归9/9，唯一失败为新增实际Redis用例期望null method写入抛异常但实际未抛；保留断言并停止代码修改，建议只追加存储前置非空校验。标签typecheck/build通过，原MATERIAL保留。新后端未部署、8080/DEV57保持上一版本，标准316/314仍草稿、无下游业务；CUAWright只读false写模式。详情见[本轮精确结果](docs/review/PRODUCT_FINISHED_REAUTH_ROUND1_RESULT_20261011.md)。原失败和所有已用轮次保留。

**当前状态：BLOCKED / REAL REAUTH HTTP503 / NOT ACCEPTED（2026-10-11真实验收续阶段）。** DEV V057和8080部署保持成功；新PRODUCT标准316/草稿版本314已真实创建并保留。独立审批人的/auth/reauth失败：本机Redis3.0.504不支持存储Lua的多字段HSET，Wrong number of args；未绕过签名，未创建后续批次/库存。14保留表完整内容复查无差异，物料16/FINISHED0/产品2，权限不改。CUAWright写模式已关闭并核验false。原2+3+2修复轮数已用完；请求最多1轮二次认证Redis兼容定向修复，并明确记录PRODUCT详情静态“物料”标签差异，均未修改实现。固定8项状态见[具体真实验收阻塞报告](docs/review/PRODUCT_FINISHED_REAL_ACCEPTANCE_REAUTH_BLOCKED_20261011.md)，历史隔离PASS不能替代当前失败。

**IN PROGRESS / TARGETED ISOLATED VALIDATION PASS / NOT READY FOR FULL ACCEPTANCE / NOT ACCEPTED（07:18 UTC）。** 原2+3轮失败历史保留；父会话再批准最多2轮，已用2轮。正向全链和拒绝负例改为独立fixture/测试事务，各自回滚，正式业务异常传播不改。当前后端23/23 PASS（19单元、迁移IT、真实链/独立负例/指令竞争3案例），0 skip。实际归档MainBatch/Product/两条新流水和正式eBR provider读取已检查；入库/发运同键重放不重复。前端原沙箱EPERM经正常权限审核解决；发现原工艺productId筛选遗漏后补传已有参数，5文件27/27与typecheck/build PASS，断言未放宽。

2026-10-11 用户07:40 UTC已批准[整体部署执行](docs/development/decisions/PRODUCT_FINISHED_DEPLOYMENT_AUTHORIZATION_20261011.md)。本机DEV已成功应用V055/V056/V057，checksum依次1211285628/-2096839805/1304788399；V054仍为供应商任务。已刷新完整本机备份并完成136表/4055行存储字节、表结构及触发器离线重建核验；此前实际恢复证据保留，新备份未实际导入、不另建环境。原工作区Java21打包成功，唯一8080已恢复health=UP、5173 HTTP200。CUAWright实际登录与生产批列表1440×900的6对查询标签同行检查通过；本次会话写模式已关闭并核验false。真实验收业务链尚未创建，固定八项验收仍未完成；不报告READY/ACCEPTED、不推送Git。旧归档再生/覆盖和新增成品报告专用打印已按用户决定移出本次待验范围，旧归档仅仅读取/hash，新成品报告独立生成。详见[部署阶段进度](docs/review/PRODUCT_FINISHED_DEPLOYMENT_PROGRESS_20261011.md)。

独立只读复核35保护表摘要一致、隔离记录全回滚、402外键0孤儿；DEV16物料/0成品物料/2产品、外键开启。详见[当前复验报告](docs/review/PRODUCT_FINISHED_TRANSACTION_BOUNDARY_RETEST_20261011.md)、[文件归属清单](docs/review/PRODUCT_FINISHED_B_FILE_REVIEW_20261011.json)；原阻塞及三轮追加报告保留历史。不得拿早期一次全链PASS替代上述覆盖边界。

## QC_OBJECT_TYPE 固定基础字典与统一质量对象关系（2026-10-11）
**HISTORICAL BROAD DESIGN SUPERSEDED / CURRENT MINIMAL SCOPE CLOSED。** 本段原“统一QMS对象解析器/冻结快照/独立绑定模型”待办已被用户在2026-10-11明确撤回；当前范围仅为一个“检验对象”可见字段 + 既有 materialLotId/mainBatchId 主键关联，原请验→取样→检验→报告→放行链不变。单字段范围已用户确认验收；V058已撤销零行空绑定表；当前Java无InspectionObjectBinding运行时引用；2026-10-12重跑QcInspectionObjectDictionaryIT 2/2 PASS，DEV Flyway已validate 62 migrations/current 062。详见 [QC_OBJECT_TYPE_CLOSEOUT_20261012](docs/review/QC_OBJECT_TYPE_CLOSEOUT_20261012.md)。成品质量新数据正向链归“产品/成品完整链”任务。

# MES Task Status Index

## 角色名称去除演示前缀 — 2026-10-10

**READY FOR ACCEPTANCE — NOT HUMAN ACCEPTED（20:40）。** 用户明确要求更新数据库，全部角色名称去掉“演示”。通过现有8080版本化/幂等/审计PUT接口更新7个sys_role.display_name，去掉“演示·”前缀；主数据/仓储/生产/取样/QC检验/QC复核/QA岗位名称已持久化。完整API前后比对角色数量/id/编码/状态/菜单及权限集合不变，仅目标版本+1；真实DEV SQL确认名称残留0、7条更新审计。无新迁移，原Flyway51不变，无UI/业务源码/登录账号/历史证据修改、提交或推送。未运行构建/视觉/全量回归（数据维护且源码/布局不变）；本次范围内CRITICAL/HIGH0，无新增技术债。依据和证据：[数据修改设计与核查记录](docs/development/decisions/ROLE_NAME_CLEANUP_20261010.md)。其他任务状态保留。

## 打印业务字典与数据源配置增量 — 2026-10-10

**当前阶段：原型已由用户确认；后端详细设计主设计复核中（用户已授权主对话批准）；正式开发未开始。** 2026-10-10主设计委派明确确认UI UX Pro Max四步原型，关系为源可绑多类型、组织内每类型唯一源、模板单类型自动源；本轮仅完成同一详细设计补充及文档入口。交审入口：[005详细设计](docs/development/DCP-PRINT-DATA-SOURCE-CONFIGURATION-005-PROPOSED.md)、[OpenAPI](docs/api/print-data-source-configuration.openapi.json)、[跨域契约矩阵](docs/development/PRINT_DATA_SOURCE_CONTRACT_MATRIX_20261010.csv)、[文档复核](docs/review/PRINT_DATA_SOURCE_DESIGN_REVIEW_20261010.md)。正式编译/测试/DB/Flyway/PDF/部署均NOT RUN；迁移号UNALLOCATED，不能锁V052。主设计已定稿单据unitId的LIVE_LOOKUP及displayValue显式HALF_UP；独立类型元数据读写/策略目录、多dataset、安全键/顺序、专用状态/恢复、不可变sidecar和固定摘要样例已由主设计明确，待准确修正文档复核下发；不等待用户重复批准。下方旧PENDING静态/V2/V3条目保留历史，不表示当前原型未确认。

用户授权“把这个任务做到收尾为止”“授权所有执行”，但主对话仍要求先审核详细设计并明确下发才编码。后续DEV操作须真实目标、备份回滚和保留现有数据/代码；无生产、不可恢复删除、持久安全权限、付款、推送或无关功能授权。本轮不执行DEV迁移或部署。

**当前阶段：独立静态设计已交付 / PENDING HUMAN REVIEW。** 用户明确纠正“先出设计图，再出交互原型”；暂停修改原型。V2/V3及其hash仅为未批准工程探索，不能作为已确认设计。新增四张独立imagegen页面稿（T1源列表、T2来源维护、T2收货/出库完整配置）及取数/明细/关联/字段操作注解，保存于`design/系统管理/打印数据源配置/静态设计-20261010/`。说明由Codex编写、内置imagegen绘制，非原型截图。已视觉自查并针对现有字段名修正图稿；未运行正式构建/真实接口/持久化/打印验收；本次无源码/数据库/迁移/推送变更。静态图确认后才制作匹配交互原型，具体原型版本仍需单独批准；已接受打印模板功能保持原契约。

V3完整取数配置原型：用户授权补齐来源、主表、明细、关联和字段，模板保持不变。已完成来源目录新增/编辑JSON结构、主表/记录参数/页面、多明细区域、关联键/基数/缺失处理、字段映射维护及按配置试取；收货/出库与新自定义来源全流程12项原型检查PASS，4视口38对标签同行无溢出，原型typecheck/build PASS。仅mes-demo/设计记录/检查证据修改；正式MES及数据库未改、无迁移/推送。PENDING HUMAN APPROVAL：正式完整schema/源权限/数据持久化/真实业务打印均NOT RUN；跨多级关联、集合排序和多账号不宣称已实现/验收。新证据printing-source-v3-20261010，旧原型和数据保留。

**IN PROGRESS — REVISED PROTOTYPE PENDING HUMAN APPROVAL。** 原三页设计已被用户拒绝，历史哈希/11项检查保留为非当前设计证据。用户明确要求保持已可用打印模板，仅补齐数据库可配置业务/单据/源/字段，并要求打印数据源配置与打印模板同级。已按此方向制作5174独立T1/T2/T3收货/出库数据源原型与独立设计说明：同级菜单、查询、字段选择、来源映射、分组/名称/格式/启停、试取示例及浏览器保存重开。13项原型检查PASS，CUAWright1440查询4对/375配置6对同行无溢出。正式字典基础数据入库已获授权但本轮尚未执行；正式MES源码/数据库未改，无新Flyway/提交推送。数据读取定义维护、多页面绑定/关联配置、完整新增/校验/权限、真实源数据和模板联动/旧历史兼容仍待；当前原型未完成功能全覆盖，不宣称正式验收。源哈希、限制与证据见[设计锁定记录](docs/development/decisions/PRINT_CONFIGURATION_DESIGN_LOCK_20261010.md)、[独立设计说明](docs/development/decisions/PRINT_CONFIGURATION_UI_SPEC_20261010.md)。

## 全工作区代码提交与远端同步 — 2026-10-09

用户在已获知全量后端迁移契约失败与前端8项失败后，再次明确要求“没提交的代码都提交和推送”。本次按该指令提交打印之外的生产处方/eBR/系统管理现有实现、对应测试、V044/V045/V046/V048/V050及开发规范/设计记录，并推送原main；这是代码同步指令，不代表其他功能已验收或全量门禁PASS。不修改测试断言、门禁配置、冻结历史或已执行迁移。本机.env、application-local.yml、application-ci.yml、含本机登录信息的临时验证脚本、缓存、日志和参考图片保留在工作区。

实际读取AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md、docs/PROJECT_BASELINE.md及部门设计锁定记录；保留FINAL BASELINE v1.0.23与UI V2/T1–T6。git diff --check通过；沿用同一工作树刚完成的构建/依赖审计/仓库策略PASS及全量测试FAIL证据，本次没有新增业务实现或重新声明验收PASS。所有现有IN PROGRESS/NOT HUMAN ACCEPTED状态保留，不把本次Git同步当作业务验收。远端同步结果以Git实际返回与远端HEAD校验为准。

## Canvas 打印模板编辑器及模板打印类型增量 — 2026-10-09

**ACCEPTED — HUMAN CONFIRMED。** 用户在本会话明确回复“验收”，确认本打印增量交付；记录时间 2026-10-09 23:07（本机时间）。 批量PDF合并25MB上限的末轮14单元及2真实DEV回滚集成PASS，当前源JAR启动和历史PDF逐字节复核通过。用户批准“对，按照这个设计进行开发”：模板版本 `printType=DOCUMENT/LIST` 决定完整单据/选中列表，`businessType` 绑定业务数据源；调用页面仅传模板、来源及选中 ID，不传 outputMode 或事实正文。Canvas 字段、Word 导入、草稿/重开、预览、发布保留，列表 T1 / 编辑 T2 / 收货详情 T3 继承 UI V2；全部查询标签与控件保持同行。WMS 收货真实 provider、收货和检验报告查询勾选打印、收货详情单条入口及不可变 batch 归档已接入。同两条实际收货记录通过 UI 输出两页单据或一页两行列表，中文、数量、单位、顺序、分页实测通过。隔离 PDF 转换进程的 Windows windir 大小写遗漏与非法 JSON 500 已修复。当前来源为收货单/检验报告/eBR 过程表单，不宣称所有业务来源或全部系统管理已完成。

V047 模板类型/批量输出与 V049 不可变保护已正常执行；共享任务的 V048/V050 内容保留，执行后源迁移/checksum 无改动；最终 Flyway 50 validate PASS，未 repair/清库。Java 21 源构建与实际 8080 health UP、5173 HTTP 200。前端打印 4 文件 15 测试、typecheck/build PASS；打印单元 30（原 28 + 新循环/环境 2）及 provider 签名/批准回归 5 PASS；末轮 NativePrintDesignerIT 2 + PrintTypeBatchIT 2 真实 DEV 回滚 PASS，中文内容断言与跨组织/权限/不可变触发器/源变更冻结检查通过。真实 5173/8080 页面与匹配尺寸截图、未知参数/重复 ID 400、未认证 401、停用后历史读取、QC 实际打印通过；测试模板仅停用并解除自己的绑定，不删除证据或改既有业务记录。

范围内自审 CRITICAL/HIGH 0，跨文档一致性 PASS；剩余 LOW 为 Word 插件大包及复杂 Word 文件排版逐件核验；MEDIUM 为同步串行批量转换的100条重载耗时未做压力验收。用户已明确授权本次提交、推送及 main 合并；提交前全量 CI 检查出现旧契约失败，禁止绕过，远端 main 发布待该阻断解决。批准与完整证据：[DCP-PRINT-TYPE-BATCH-004](docs/development/DCP-PRINT-TYPE-BATCH-004-APPROVED.md)、[验证及一致性记录](docs/review/PRINT_TYPE_BATCH_20261009.md)、[当前 API 补充](docs/api/print-type-batch.openapi.yaml)。早期 outputMode 讨论已被该模板属性设计取代，历史参数草案与 [Canvas 编辑器早期验证](docs/review/CANVAS_PRINT_EDITOR_20261009.md) 仅保留阶段证据，不能当作当前实现状态。

人工验收记录：本次仅更新 MES_TASKS.md 的上述任务状态，白名单为本文件；其他系统管理状态保留。已实际读取 AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md、docs/PROJECT_BASELINE.md 权威章节、DCP-PRINT-TYPE-BATCH-004-APPROVED.md 与 PRINT_TYPE_BATCH_20261009.md；依据仍为 FINAL BASELINE v1.0.23、批准的打印补充及 UI V2/T1–T6。设计差异：本次无实现/设计修改或新增差异。迁移：没有新迁移/数据库写入，原 V047/V049 证据保留。验收证据：引用上次已记录的定向测试、真实页面/PDF、50 个迁移校验和一致性复核；本次状态记录未重新运行构建/测试，不新增运行 PASS。结论：仅本打印模板与单据/列表增量由明确人工确认转 ACCEPTED，剩余 LOW/MEDIUM 技术债保留，原用户数据与历史证据保留；该验收状态记录步骤没有提交/推送操作；后续提交授权与门禁结果见下方提交准备记录。

提交准备记录：本次白名单为打印后端/适配器/测试、Canvas 与选中打印前端及依赖、V047/V049、打印批准/API/证据文档和本任务索引；共享 SecurityConfiguration 只纳入打印 allowlist 代码块。其他生产/eBR/系统管理修改、V044–046/V048/V050、本机配置、缓存与日志保留未纳入。仓库/Compose/CI policy 正反例 PASS；前端构建 PASS，生产依赖审计0漏洞。全量前端137/145通过（8失败），全量后端因旧 Mes002MigrationContractTest 仍限定001–031失败；当前原main已至043。打印相关定向与此前真实回滚/页面证据保留，不将全量失败改写为PASS。需要旧测试契约受控复核后才能完成远端main推送。

## 菜单原型正式集成与基础数据初始化 — 2026-10-10

**READY FOR ACCEPTANCE — NOT HUMAN ACCEPTED（2026-10-10 17:32）。** 用户明确批准权限说明字段和聚合保存API，并批准共享顶栏/本地刷新代理的定向修复。正式菜单T1列表与T2/T3配置已按冻结5174原型接入5173/8080：查询/树/分页、特殊权限草稿、关联/解除、只读角色、统一事务保存、版本/幂等/回滚；不自动扩张角色授权。V051新增说明字段并补齐正式既有基础菜单/权限关联，51迁移validate PASS，旧迁移及自定义数据保留。Java源码构建、前端typecheck/build、前端10测试、真实DEV8集成和真实浏览器35检查PASS；四视口标签同行/局部滚动和最终截图已审查。测试菜单/权限仅停用、不删除。剩余MEDIUM为非法空permission-change id的错误响应规范化，LOW为移动分页计数换行及既有大包/Flyway版本警告；未运行全量回归，未提交推送。依据：[批准补充](docs/development/DCP-MENU-CONFIGURATION-001-APPROVED.md)、[设计锁定](docs/development/decisions/MENU_FUNCTION_DESIGN_LOCK_20261010.md)、[实施与验收证据](docs/review/MENU_CONFIGURATION_RUNTIME_20261010.md)。仅本菜单增量待人工终验，其他任务状态不变。

以下为授权前的历史门禁记录，不是当前状态：

**BLOCKED / DESIGN CHANGE REQUIRED — NOT ACCEPTED。** 用户确认当前5174菜单原型并要求完成正式开发，追加菜单/权限作为正式基础数据初始化固定。已读取AGENTS.md、MES_DEV_RULES.md、v1.0.23权威路由、UI V2/T1/T2、数据库菜单批准DCP、当前原型哈希、正式菜单/权限/角色接口与V037等现有种子数据。当前原型UI版本已确认，基础数据初始化范围已记录；发现正式权限说明字段缺失、菜单+权限定义+关联无聚合保存事务，两者需要受控字段/API变更授权，停止业务施工。记录：[菜单原型确认与门禁](docs/development/decisions/MENU_FUNCTION_DESIGN_LOCK_20261010.md)、[最小变更提案](docs/development/DCP-MENU-CONFIGURATION-001-PROPOSED.md)。本轮只增加文档和此任务索引，无源码/数据库/迁移修改，无提交推送，既有用户数据与其他任务状态保留；构建/正式浏览器/真实持久化验收NOT RUN，未以原型旧结果冒充正式通过。

## 系统管理报错目标修复 — 2026-10-09

**IN PROGRESS — NOT HUMAN ACCEPTED。** 部门/字典接口明确声明参数名称，修复 Spring 参数绑定导致的读取/下拉 400 与 Invalid input。5 个绑定测试及 18 个直接集成用例跨目标运行/修复重跑通过；OpenAPI 断言匹配现有全限定类名配置，字段约束保留。真实管理员 7 个系统菜单、21 个页面/弹窗状态读取与查询检查通过。全写操作组合、多角色真实 UI 和设计差异完整验收仍未关闭，不能宣称系统管理全部完成。与上述打印增量同在原工作区/本机服务；无本次修复迁移，历史和无关修改保留。

## V040 系统管理部门实施 — 2026-10-09

**IN PROGRESS — NOT HUMAN ACCEPTED.** 原工作区 `main` 与同一 MariaDB DEV：V040 部门/用户归属表迁移成功、40 migrations validate；IAM 授权和数据库菜单已接入。后端部门树/组织用户下拉/CRUD/归属关系 + Vue T1/T2/T3 完成局部施工并部署至原项目 8080、前端 5173。 `SystemDepartmentIT` 3/3 真实 DEV 数据库回滚集成通过；前端 typecheck/build PASS、Chromium 1440×900 1/1 fixture 视觉测试 PASS，全部查询/编辑字段水平同行，操作列在可视区域。后端 HTTP 200 health、未授权部门 API 401。仍需真实管理员会话 E2E 和原稿差异闭环、权限多账号回归。仅本地修改未提交/推送。证据：`design/系统管理/部门管理/部门管理-实施视觉与回归记录.md`。后续用户、菜单、角色、角色权限按相同工序逐页设计与终验。


## 系统管理增量进度 · 2026-10-08 23:31

**数据字典 V039：核心实施和目标测试完成；正式验收仍未关闭。**
- 在原项目 `main` 工作区维护后端 `DictionaryImportService`、导入 API 预检/提交、CSV 模板、Vue 导入弹窗和树形字典编辑。仅接受新字典 CODE；已存在/重复/不合法项在预检中标示，正式提交再次校验；受事务、管理员授权、幂等和审计保护。CSV 模板只有表头，避免误导入演示数据。
- 真实 MariaDB 回滚测试 `SystemDictionaryCrudIT` + `SystemDictionaryIT` 4/4 PASS；CSV 解析 Vitest 3/3 PASS；Chromium T1/T2/字典项/导入/树形 UI 2/2 PASS，接口业务数据为 fixture。
- 后端最新版从原项目 `backend/mes-boot/target/mes-boot-0.1.0-SNAPSHOT.jar` 运行（8080 health 200），数据库 V039 校验通过、未登录字典 API 401。前端 production build PASS。
- 导入只支持 UTF-8 CSV（Excel 可另存 CSV），尚未提供 .xlsx 原生解析；不能宣称 Excel 导入已完成。
- 仍需真实管理员浏览器操作、角色权限多账号检查、动态下拉业务表单真实数据回归及设计图逐项对照。**NOT HUMAN ACCEPTED**。

**部门管理：完成 4 张 1440×900 PC 静态设计图和独立视觉自评，未改业务代码/数据库。**
- `design/系统管理/部门管理/部门管理-{查询,新增,编辑,查看}.png` 和 `部门管理-ProductDesign.html`。
- 三列同行查询、左右部门树/列表、居中 T2 简单新增/编辑、T3 只读详情；Chromium 4/4 场景几何检查无标签分行或页面横向溢出。
- 需求/模型契约记于 `部门管理-独立设计评审.md`：部门不同于现有 md_organization，用户主属部门需要独立真实关系；未完成正式 DCP/新增 Flyway/后端服务及用户归属功能。
- 系统管理余下用户、菜单、角色、角色权限仍需逐个按稿视觉和业务验收。不要合并任务状态、不要宣称全系统完成。



## V039 数据字典增量实现记录 — 2026-10-08
- 直接在当前 `main` 工作区和本机 MariaDB DEV 中实施；`V039__system_dictionary.sql` 已成功应用并通过 39 项 Flyway 迁移校验，原有业务数据不清理、原 Flyway 文件不改写。
- `SystemDictionaryController`、Vue T1/T2/T3、字典项弹窗、动态下拉已新增；物料类型、设备类型、组织类型选项读取数据库，不复用前端类型硬编码。
- 2026-10-08 晚间后端 3/3 真实 MariaDB 回滚集成测试通过；1440×900 Chromium 1/1 mock 展示层测试通过，测试断言五个查询标签及各表单标签同一行、PC 查询三列；Maven package 和前端 typecheck/build 通过。
- 增补创建日期筛选、全量 CSV 导出、历史已停用字典项禁选但回显原名称（`selectedValue`）。
- 已从项目 `backend/mes-boot/target` 的最新 JAR 重启单一 8080；健康检查 HTTP 200，未登录请求两个受保护字典接口均返回 401。
- **IN PROGRESS / NOT ACCEPTED**：图片参考里的 CSV/Excel 批量导入、树形可视编辑完善、真实管理员网页登录到数据库及正式 UI 同屏逐像素验收仍未闭环。不同权限账户、业务主数据关联的完整回归亦待验证。
- 仅做本地变更，尚未提交 Git；其他系统管理功能：部门/用户/菜单/角色/角色权限仍为待逐页设计/整改/验收。

## Database menus / usability / direct maintenance — 2026-10-08

`IN PROGRESS — NOT HUMAN ACCEPTED`. Scope: [approved supplement](docs/development/DCP-DATABASE-MENU-AND-USABILITY-001-APPROVED.md). Database tree menu shell deployed with V037; role grants remain unchanged by agent validation. V038 direct activation applied in native DEV during rollback integration validation; executable deployment and remaining browser flow checks pending. Frontend typecheck/build PASS; role/menu/master units15 PASS; picker/menu units6 PASS (menu3 overlap); backend navigation4/domain5/menu integration3/direct activation3 PASS. Full system/visual acceptance is not claimed. Browser screenshot confirms inline production-batch search; remaining click-based visual checks are pending. No commit; unrelated local configuration, seeds and user changes preserved.


## Finished T2/T3 visual refinement — 2026-10-07

`ACCEPTED` — explicit human acceptance and commit/push authorization “验收，提交和推送”, 2026-10-07; user requested “成品页面视觉收口”, within the approved complete-page blueprint / Global UI V2 T2–T3. Scope: existing finished inbound, inspection-request, sampling, report and shipment create/controlled-action/detail presentation only. Business authority remains FINAL BASELINE COMPLETE v1.0.23; accepted incoming records/reports, T4/T5/T6 and frozen releases unchanged. No Backend/API/DTO/DB/migration/state/permission/signature/QA Gate change. Typecheck/build PASS; finished command units4 PASS; final visual Chromium4 plus direct warehouse-signature/sample-handoff regressions2 PASS. 12 rendered screenshots (1440×900 PC plus one 768px usability smoke) saved outside repository at `C:/Users/Administrator/.codex/artifacts/finished-ui-2026-10-07/`. Fixture-backed UI/command validation only, not a new real-database full-system acceptance. Browser plugin unavailable; repository Playwright Chromium used. Source/read-only results, original FAIL revisions, independent review/signature metadata, allowedActions/permission gates and atomic optional inspection draft verified. Scoped self-review CRITICAL/HIGH0. Build bundle-size LOW and inherited aggregation/N+1 MEDIUM remain; historical baseline checksum issue unchanged. MEDIUM-03 existing finished T2/T3 presentation RESOLVED and human-accepted; accepted incoming/T4/T5/T6 not changed. Pre-commit typecheck and finished command units4 rechecked PASS. No new MES task, business baseline switch or historical release change. Main commit/push authorized for this visual refinement; local database configuration and unrelated user-owned changes excluded.

## Approved shared navigation / production-finished entries — 2026-10-07

`ACCEPTED` — explicit human acceptance and commit/push authorization “验收，提交和推送”, 2026-10-07, for the approved DCP-PRODUCTION-FINISHED-ENTRY-001 scope. Current authority `FINAL BASELINE COMPLETE v1.0.23` — `AUTHORITATIVE`; cross-document consistency PASS; parent v1.0.22 immutable. One shared permission-filtered seven-domain navigation; production five entries and finished nine entries; actual execution/balance/receiving/finished-test/QA selectors reuse existing controlled pages. Five readonly APIs and receivingOnly query supplement only; no DB/migration/state/permission-code/signature/QA Gate or mandatory-weighing change. Existing accepted MES tasks unchanged.

Backend compile/native integration3 (21.76s), frontend units7/typecheck/build, PC Chromium shared-shell2 + real-entry1 PASS. New closed DTOs, permission/organization isolation, original IPC exclusion and true pre-pagination filters verified. Scoped self-review CRITICAL/HIGH0. HIGH-01/HIGH-02 and scoped MEDIUM-01/02 ready; MEDIUM-03 full finished T2/T3 visual refinement remains PARTIAL/outside the bounded entry DCP. Remaining inherited projection/N+1 MEDIUM and bundle-size LOW debt. [Current report/screenshots/RTM](docs/acceptance/shared-navigation-2026-10-07/ENTRY-CLOSEOUT.md), [review](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.23/00_PRODUCTION_FINISHED_ENTRY_CONSISTENCY_REVIEW.json). Human acceptance and main commit/push explicitly authorized; the historical checksum issue and remaining visual refinement are not implicitly closed.

Historical v1.0.22 manifest discrepancy LEGACY-BASELINE-SHA-01 remains OPEN outside this DCP:90 of143 SHA entries differ, while all150 parent bytes are unchanged by this task. Current v1.0.23 candidate checksums PASS. No full historical-baseline integrity or global HIGH0 claim; separate controlled metadata remediation required. See current closeout report.

## Approved trace / finished inventory closure — 2026-10-06

`ACCEPTED` — explicit human acceptance and commit/push/main merge authorization: “验收，提交和推送，合并到main”, 2026-10-06. User approved audit gaps1/4 only: “授权修改批准，投料记录是关键，不是称量。” Current authority FINAL BASELINE COMPLETE v1.0.22 / approved DCP-TRACE-FINISHED-INVENTORY-001 / consistency PASS. [Contract](docs/development/DCP-TRACE-FINISHED-INVENTORY-001-APPROVED.md), [ledger](docs/development/TRACE-FINISHED-INVENTORY-IMPLEMENTATION.md). Actual-charge forward trace/WMS sources and finished inventory read/T1 menu; no mandatory weighing, new DB/migration, QA/stock/signature rule. Historical acceptance unchanged. Earlier dated pointers below retain their historical scope.

Targeted backend unit1 + native integration5, frontend units4/typecheck/build, PC Chromium3 distinct scenarios PASS. Independent review CRITICAL/HIGH0; MEDIUM stale inventory response fixed and reviewed/verified. Parent144 files and migration hashes unchanged; sealed148-file manifest/SHA256SUMS and current pointers PASS; native031 retained. [Evidence/screenshots](docs/acceptance/trace-finished-inventory-2026-10-06/REPORT.md), [executed RTM](docs/acceptance/trace-finished-inventory-2026-10-06/RTM.md). Inherited aggregation/N+1 and bundle-size debt remain. No other audit gaps implemented; human runtime acceptance confirmed.

## Approved optional finished inspection draft — 2026-10-06

`ACCEPTED` — explicit human approval “入库申请同时生成请验草稿”的流程变更明确批准. Current business authority v1.0.21 — AUTHORITATIVE, approved DCP / cross-document consistency PASS, all138 frozen v1.0.20 parent files unchanged. Only optional atomic draft generation / warehouse execution gates / existing T2/T3 controls; no other finished-image page/menu scope implicitly authorized. [Approved DCP](docs/development/DCP-FINISHED-INBOUND-INSPECTION-DRAFT-001-APPROVED.md), [inline plan](docs/superpowers/plans/2026-10-06-finished-inbound-inspection-draft.md), [frozen manifest](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.21/00_MANIFEST_FINAL_FROZEN_V1.0.21.md). No new schema migration for this optional change. Explicit human acceptance and commit/push authorization: “验收，提交和推送”, 2026-10-06; includes the completed finished-goods chain and optional inspection draft in current v1.0.21.

Native new4+direct receiving/full-chain2 PASS; closed schema1 PASS; expanded rollback/audit/changed-payload/lost-permission case rerun1 PASS. Frontendunits9/typecheck/build PASS; PC Chromium1/3screenshots PASS with neutral draft source label and no errors/overflow. One scoped final review: no CRITICAL/HIGH/MEDIUM, LOW evidence suggestions resolved/verified. [Verification](docs/acceptance/finished-inbound-inspection-draft-2026-10-06/REPORT.md), [executed RTM](docs/acceptance/finished-inbound-inspection-draft-2026-10-06/RTM.md). Inherited load/performance and paging debt retained. Human runtime acceptance confirmed 2026-10-06; prior MES acceptance unchanged.

## Approved finished-product chain maintenance — 2026-10-06

`ACCEPTED` — retained authority at this maintenance `FINAL BASELINE COMPLETE v1.0.20` (AUTHORITATIVE), approved DCP-FINISHED-GOODS-CHAIN-001 / consistency PASS. Human explicitly approved the audited four-gap design change and requested implementation; human accepted the completed cumulative implementation on 2026-10-06 (“验收，提交和推送”). Scope: completed-production inbound request/independent warehouse receipt; post-completion finished inspection/sampling/results-derived report; extended signed QA evidence; finished shipment and full reverse trace. [Approved bounded contract](docs/development/DCP-FINISHED-GOODS-CHAIN-001-APPROVED.md), [implementation ledger](docs/development/FINISHED-GOODS-CHAIN-IMPLEMENTATION-PLAN.md). Successor v1.0.20 consistency PASS is recorded; v1.0.19 is retained immutable history; existing MES acceptance remains unchanged. Explicit human acceptance/commit/push authorization recorded above; no automatic acceptance inferred.


V030/V031 APPLIED (31 success/0 failure); two domain rules and all11 new native scenarios plus affected QA/archive/OOS gates PASS across targeted runs; frontend units7/typecheck/build PASS; PC Chromium4 scenarios/15screenshots PASS. Independent review two HIGH repaired/verified; known CRITICAL/HIGH0, MEDIUM race-load/paging debt recorded. [Acceptance evidence](docs/acceptance/finished-goods-chain-2026-10-06/REPORT.md), [executed RTM](docs/acceptance/finished-goods-chain-2026-10-06/RTM.md), [frozen manifest](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.20/00_MANIFEST_FINAL_FROZEN_V1.0.20.md). Acceptance and commit/push explicitly authorized 2026-10-06; see current v1.0.21 acceptance above. UI V2/T1–T6 remains current; prior MES-001–013 including008A remain ACCEPTED.

## Retained v1.0.19 WMS authority — 2026-10-06

Historical WMS authority at that maintenance: `FINAL BASELINE COMPLETE v1.0.19` — `AUTHORITATIVE`, following approved DCP-WMS-REQUEST-INVENTORY-RETURN-001 and detailed-design confirmation; design consistency PASS. [Manifest](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.19/00_MANIFEST_FINAL_FROZEN_V1.0.19.md). Existing v1.0.18 and earlier entries below are retained history. UI remains Global UI V2 / T1–T6; runtime WMS maintenance is ACCEPTED following explicit human acceptance “确认验收，提交和推送” on 2026-10-06.


## Current authority / bounded storage-source maintenance — 2026-10-06

At that approval, `FINAL BASELINE COMPLETE v1.0.18` was `AUTHORITATIVE`, approved DCP-MATERIAL-STORAGE-SOURCE-001, consistency PASS (114 v1.0.17 parent files unchanged). Global UI V2 / T1–T6 remains current. [Manifest](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/00_MANIFEST_FINAL_FROZEN_V1.0.18.md). This pointer supersedes dated authority declarations below; previous v1.0.17 Functional/UI Closeout remains accepted history, not an assertion of full v1.0.18 regression.

Bounded maintenance: `ACCEPTED` — explicit human acceptance on 2026-10-06 ("验收，提交和推送"). Storage condition read/write reuses existing DB column; V028 adds relationship manufacturer and immutable receipt-item source snapshot; MaterialLot uses existing receiptItemId lineage. Current reads / sorted locks prevent stale-source freeze. Existing audit/reason/optimistic version/idempotency and QA/permissions unchanged. Seven distinct native methods passed across targeted gates (six initial cases, three current-read/direct/audit cases with two overlaps); five backend relationship rule tests and nine frontend unit tests PASS; three distinct desktop Chromium scenarios PASS across focused gates; typecheck/final build PASS. [Current evidence](docs/acceptance/material-field-cleanup-2026-10-06/SOURCE-CLOSEOUT.md). No new full-system regression is claimed; human acceptance and main commit/push are explicitly authorized for this maintenance. MES-001–013 including008A remain ACCEPTED. Workbench image/planning/SOP proposals remain unapproved; V3 stays outside scope.

## Material page Product Design maintenance — 2026-10-06

`ACCEPTED` — explicit human acceptance: “验收，提交和推送”. Selected third Product Design concept implemented on material T3 detail and T2 create/edit: left basic/unit facts, right quality/remarks, compact conversion and audit controls, stable form actions. Existing eleven fields, payload, routes, permissions and GxP controls retained. Business authority v1.0.18 / Global UI V2 unchanged. Targeted Chromium flow, nine affected unit tests, typecheck and build PASS; no new full-system regression claimed. [Design QA and screenshots](docs/acceptance/material-product-design-2026-10-06/design-qa.md). Earlier taste refinement is a superseded visual checkpoint. Existing MES task acceptance remains unchanged.

## MaterialIssue outbound display naming — 2026-10-06

`ACCEPTED` — explicit human acceptance “确认验收，提交和推送” on 2026-10-06 covers the outbound terminology delivered with WMS. User authorized replacing 发料管理/发料单 UI terminology with 出库管理/出库单. Menu, route metadata titles, document headings, action/field labels and related evidence display names updated. MaterialIssue entity, API, payload, routes, permission keys, DB and business rules unchanged; no migration or baseline authority switch. Typecheck and two targeted Chromium scenarios PASS; exact command payload assertion retained. [Evidence](docs/acceptance/material-issue-outbound-naming-2026-10-06/REPORT.md). Existing accepted MES task status unchanged.

## Authorized WMS contract completion — 2026-10-06

`ACCEPTED` — explicit human runtime acceptance “确认验收，提交和推送” on 2026-10-06, following scope approval “批准并补齐” and detailed design “确认”. Current authority FINAL BASELINE COMPLETE v1.0.19; design consistency PASS and immutable119 v1.0.18 parent files retained. Formal MaterialRequest, linked existing MaterialIssue, lot inventory projection and independent existing IssueReturn history/entry implemented under Global UI V2 T1/T2/T3. Five ordered WMS menu entries; existing receipt/QA/Reservation/weigh/verify/charge/signatures preserved. V029 APPLIED: native highest29/success29/failure0. Targeted backend rules3, native integration13, frontend units5, PC Chromium7, typecheck/build PASS; no full regression or hosted CI claim. Independent review's two HIGH findings addressed and targeted verified; remaining MEDIUM debt is tenant-wide/N+1 projection paging. [Acceptance report and screenshots](docs/acceptance/wms-request-management-2026-10-06/REPORT.md), [RTM execution](docs/acceptance/wms-request-management-2026-10-06/RTM.md), [approved DCP](docs/development/DCP-WMS-REQUEST-INVENTORY-RETURN-001-APPROVED-SCOPE.md). Existing accepted MES statuses and unapproved workbench/V3 exclusions unchanged. Human acceptance explicitly authorizes committing and pushing this bounded maintenance to main.

## Retained human-confirmed final closeout — 2026-10-06

Historical business authority at that Closeout: `FINAL BASELINE COMPLETE v1.0.17` — `AUTHORITATIVE` at the time. UI authority remains Global UI Design System V2 / Page Template Standard V2 (`T1–T6`).

| Scope | Current status |
|---|---|
| MES-001–MES-013, including MES-008A | `ACCEPTED` |
| Functional Baseline v1.0.17 | `CLOSED` |
| UI V2 Implementation | `CLOSED` |
| CRITICAL / HIGH | `0 / 0` |
| MEDIUM-01–04 | `CLOSED` |
| MaterialLot T4 | `ACCEPTED` |
| Production Batch T4 | `ACCEPTED` |
| Production Execution T5 | `ACCEPTED` |
| QA Decision T6 | `ACCEPTED` |

Approval: the user's explicit instruction to record these statuses after the completed Functional / UI V2 Final Closeout Audit. Accepted implementation HEAD: `652feeda634c9b2ccf35864106fabadd4179e7ad`. This current record supersedes the dated READY FOR ACCEPTANCE / IN PROGRESS statements below for the delivered v1.0.17 and UI V2 scope; those statements remain historical evidence.

The four new contracts in [DCP-WORKBENCH-GAPS-001](docs/development/DCP-WORKBENCH-GAPS-001-PROPOSED.md) remain `PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED`: Material Master Image, Operation Execution Photo, Planned Operation Time, SOP/Method Reference. They are outside the closed scope, not active authorized implementation. Global UI V3: `NOT IN CURRENT SCOPE`; no implementation or UI authority switch.

[Current Closeout and evidence references](docs/review/MES_V1.0.17_FUNCTIONAL_UI_V2_FINAL_CLOSEOUT.md). The task table and historical records below are retained; no frozen release is rewritten.

## Historical delivery and approval records

Dated entries below describe their original delivery state. For current status, use the human-confirmed Closeout above and the task-status table.


## Production Batch T4 reference — 2026-10-06

User explicitly invokes Product Design image-to-code + design-qa for the attached1280×853 Production Batch reference. Existing-contract PC presentation is `READY FOR ACCEPTANCE`: summary/circular factual operation progress, six connected lifecycle nodes, six bounded source module cards, nine Tabs and notes/files column; parent-owned controlled actions, operation dates, snapshot/Trace/Audit and query-preserving return retained. Typecheck/final build PASS; three distinct targeted Chromium PC cases PASS across scoped gates; same-size full/focused comparison inspected and density/tab issues fixed. [Report/gallery](docs/acceptance/batch-reference-2026-10-06/REPORT.html). Full-image100% fidelity is not claimed: unavailable product photo/extra business fields and sample numbers remain contract exclusions. No backend/API/DB/migration/permission/state/signature/baseline change; original inspection/report components unchanged. Existing MES ACCEPTED statuses retained; no new MES task, commit or push. Pending data-ownership proposal from the preceding functional follow-up is not implicitly approved.

## Human acceptance and functional gap follow-up — 2026-10-06

Human instruction “验收，然后补齐功能缺口，再提交和推送” explicitly accepts the four pending deliveries reported immediately before it: Complete UI Blueprint, MEDIUM-01–04 technical resolution, MaterialLot T4 reference refinement, and T5 PC reference refinement. Their current delivery status is `ACCEPTED`; dated readiness entries below remain historical evidence. Accepted code is `f235bdedb105c6c6c715f53893c37e4676cfd0c4`. MES-001–013 including MES-008A remain ACCEPTED.

Functional follow-up is `IN PROGRESS`, not accepted: reuse existing production deviation creation and frozen parameter/read facts first. Missing image association, planned operation times and frozen SOP method contracts require an explicitly bounded design decision and consistency review; no sample screenshot data may become production facts. Current FINAL BASELINE COMPLETE v1.0.17 remains authoritative. User also authorizes committing and pushing the verified follow-up, excluding local database configurations.

Existing-contract follow-up delivery: `READY FOR ACCEPTANCE`. PC T5 production deviation context/permission/final-QA guard, exact same-unit frozen range presentation and production save-navigation correction completed. Typecheck/build,9 focused unit tests and3 final Chromium PC cases PASS; scoped delivered increment has no remaining CRITICAL/HIGH. [Report and screenshots](docs/acceptance/workbench-gap-followup-2026-10-06/REPORT.md). No backend/API/DB/migration/frozen baseline change. The overall functional follow-up remains `IN PROGRESS` / `USER DECISION REQUIRED` for the new image/planning/SOP contracts in [the bounded proposal](docs/development/DCP-WORKBENCH-GAPS-001-PROPOSED.md). No complete-gap closure or acceptance of this new increment is inferred.

Latest verified increment (2026-10-05): [MES-012 closeout](docs/acceptance/mes-012/CLOSEOUT-2026-10-05.md) and [MES-013 closeout](docs/acceptance/mes-013/CLOSEOUT-2026-10-05.md) are ACCEPTED following explicit human confirmation “确认验收” on 2026-10-05. Native V001..V026 validated/schema026; MES-01315 distinct native business methods and required targeted gates PASS, independent CRITICAL0/HIGH0/MEDIUM0. Earlier MES-001..011 human acceptance remains unchanged. FINAL BASELINE COMPLETE v1.0.17 is authoritative. Acceptance is explicitly authorized by the user.

This is the sole task-status index for future Codex sessions. It records status and navigation only; requirements remain in `FINAL BASELINE COMPLETE v1.0.17`. Update status here when a task starts or reaches a verified gate. Only human approval may set `ACCEPTED`.

Current business authority is governed by [PROJECT_BASELINE.md](docs/PROJECT_BASELINE.md) and the [v1.0.17 frozen manifest](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/00_MANIFEST_FINAL_FROZEN_V1.0.17.md). Dated records below preserve historical baseline versions and statuses; they do not override this current pointer or the current task-status table.

## Completed

### MES-001-R2

- Status: `ACCEPTED`
- Summary: Platform foundation completed for GxP audit, electronic signature, idempotency, integration inbox/outbox, and formal operations routes.

### MES-002-R2

- Status: `ACCEPTED`
- Summary: Authentication/RBAC, IAM administration APIs and UI, permission-filtered navigation, audit/idempotency integration, and physical migration V005 completed.

## Current and future

| Task | Status | Title | Dependencies | Task Card |
|---|---|---|---|---|
| MES-003-R2 | `ACCEPTED` | 组织、单位、设备与人员资格 | Hard: MES-001, MES-002 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-003-R2.md) |
| MES-004-R2 | `ACCEPTED` | 物料基本信息与单位换算 | Hard: MES-001, MES-003; Soft: MES-005 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-004-R2.md) |
| MES-005-R2 | `ACCEPTED` | 多供应商关系与唯一首选供应商 | Hard: MES-001, MES-003, MES-004 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-005-R2.md) |
| MES-006-R2 | `ACCEPTED` | BOM处方、工艺路线与参数版本 | Hard: MES-003, MES-004; Soft: MES-007 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-006-R2.md) |
| MES-007-R2 | `ACCEPTED` | 动态eBR定义与运行引擎 | Hard: MES-001, MES-002, MES-006; Soft: MES-009 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-007-R2.md) |
| MES-008-R2 | `ACCEPTED` | WMS收货、库存、预留与发退料 | Hard: MES-003, MES-004, MES-005; Soft: MES-009 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-008-R2.md) |
| MES-008A-R2 | `ACCEPTED` | Incoming Material Quality & Material Release | Hard: MES-001, MES-002, MES-003, MES-004, MES-005, MES-008 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-008A-R2.md) |
| MES-009-R2 | `ACCEPTED` | 订单与正式批模型 | Hard: MES-003, MES-004, MES-006, MES-007; Soft: MES-008 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-009-R2.md) |
| MES-010-R2 | `ACCEPTED` | 工序、设备运行与参数采集 | Hard: MES-003, MES-006, MES-007, MES-009; Soft: MES-008 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-010-R2.md) |
| MES-011-R2 | `ACCEPTED` | 称量、投料与Genealogy | Hard: MES-004, MES-006, MES-008, MES-008A, MES-009, MES-010; Soft: MES-007 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-011-R2.md) |
| MES-012-R2 | `ACCEPTED` | IPC、生产质量调查与物料平衡 | Hard: MES-008A, MES-009, MES-010, MES-011; Soft: MES-007 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-012-R2.md) |
| MES-013-R2 | `ACCEPTED` | QA放行与eBR归档 | Hard: MES-007, MES-008A, MES-009, MES-011, MES-012 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-013-R2.md) |

DCP-MES-002-R2-001 resolves the former MES-012 ambiguity. Incoming material request/sampling/testing/report/release belongs to MES-008A; MES-012 owns production IPC, OOS/OOT, Deviation, CAPA, investigation, and material balance.

## MES-003–MES-005 pre-implementation review — 2026-10-03

- Authorized request: develop MES-003 through MES-005 sequentially and autonomously, with minimal targeted validation. No frozen-design change was authorized.
- MES-003 review outcome: `DESIGN CHANGE REQUIRED`; implementation has not started. MES-001/MES-002 are accepted, but the following frozen-contract gaps prevent completing MES-003 without inventing contracts:
  - `tasks/MES-003-R2.md` section 3 requires organization/unit/equipment/qualification UI. `10_UI_PAGE_ROUTE_MATRIX_V1.0.2_FROZEN.csv` defines equipment routes only for this scope; organization/unit/qualification routes are absent. They require an approved route/UI contract.
  - `08_API_DETAILED_V1.0.2_FROZEN.md` DTO rules require enums to be listed in OpenAPI. MES-003 operations in `08_OPENAPI_FULL_V1.0.2_FROZEN.yaml` use `GenericRequest`/`GenericResponse` (unrestricted objects), without the organization/equipment/qualification status enums. `07_STATE_MACHINE_DETAILED_V1.0.2_FROZEN.md` does not define their state transitions. These business contracts require specification before implementation.
  - `05_DATABASE_DESIGN_V1.0.2_FROZEN.md`, `md_unit_conversion`, specifies nullable `material_id BIGINT FK`. The migration matrix assigns conversion to LG-003 and material to dependent LG-004; it does not specify how the forward foreign key is staged. Approve its creation timing/ownership without introducing an early material table or silently omitting the required FK.
  - `10_UI_PAGE_DETAILED_V1.0.2_FROZEN.md`, UI-EQP-Q, requires a location column, while the frozen equipment schema defines no location field or relationship. Specify its authoritative source or approve a bounded UI/schema correction.
- Proposed approval boundary: complete only the MES-003 DTO/enums/state/UI contracts and the LG-003→LG-004 foreign-key handoff; resolve the equipment location source. Follow AGENTS.md design-change governance, preserve v1.0.2, and review affected authoritative artifacts together before switching baseline. This proposal is not approval and is not a second source of truth.
- MES-004/MES-005 remain `NOT STARTED`: their required MES-003 contracts are not physically available (`DEPENDENCY_NOT_READY`). Their task cards and implementation are deferred until MES-003 is unblocked.
- Evidence: read-only contract review; no product code, migration, database operation, or tests executed. Existing user-owned worktree changes were preserved. No task is ready for acceptance.

## MES-003 approval and execution — 2026-10-03

Human reply “授权” approves the four previously listed gaps. DCP-MES-003-R2-001 is recorded in v1.0.3; cross-document consistency review completed before authority-pointer switch. MES-003 is IN PROGRESS. Prior pre-implementation review is historical; its “no authorization” condition is superseded. MES-004/005 will begin after the required producer contracts are physically available.

## MES-004/005 approval — 2026-10-03

Human reply “授权” to DCP-MES-004-005-R2-001 explicitly approves its bounded material/supplier contract completion. v1.0.4 review PASS; old releases retained. Required MES-003 contracts available. MES-003 targeted verification: 8 unit tests, 12 native MariaDB IT, 15 frontend unit tests, 6 browser cases across desktop/mobile (API mocked); no remaining CRITICAL/HIGH in final scoped review. MES-004/005 IN PROGRESS under the user-authorized sequential scope.

## MES-003–005 verified delivery — 2026-10-03

- MES-003/004/005: READY FOR ACCEPTANCE. Human design authorization is not acceptance; no task was marked ACCEPTED.
- Implemented organization/UOM/conversion/equipment/qualification; complete material master and immutable versioned quality/storage/production/inspection policy; supplier qualification and retained approved relationships; frozen routes and permission-filtered UI.
- Native MariaDB: V006, V007, V008, V009 successfully applied, including the delayed conversion-material FK and frozen indexes. V001–V009 validation/history intact; no data reset or executed migration modification. Test-created users/materials checked with zero residue.
- Targeted backend: 13 distinct domain tests and 23 distinct integration cases passed across task gates. MES-004/005 first gate: 20/22 integration PASS including all 12 upstream cases; two test-construction errors fixed and only those two rerun PASS. One additional relationship replay/stale/expiry contract case PASS. No full regression.
- Frontend build PASS; latest 15 focused unit tests PASS (2.25s). Browser: 6 MES-003 and 4 MES-004/005 desktop/mobile cases passed across task gates. Material same-URL state refresh bug fixed; only its two failed flows rerun (2/2 PASS, 8.5s). Browser APIs mocked; actual API/database/audit behavior verified separately by MockMvc/native MariaDB.
- Final read-only scoped and increment reviews: CRITICAL=0, HIGH=0, remaining necessary MEDIUM=0. Preserved existing frontend bundle-size warning and inherited duplicate WMS route-matrix rows as LOW/out-of-scope debt. Batch/receipt snapshot consumer integration remains in its respective later task, not preimplemented here.
- Evidence: docs/acceptance/mes-003/ACCEPTANCE.md, docs/acceptance/mes-004/ACCEPTANCE.md, docs/acceptance/mes-005/ACCEPTANCE.md. No commit/push, no local credential/config staging. Stop after MES-005 as requested scope.

## Material basic maintenance correction — 2026-10-03

Human explicitly cancels material version/approval and confirms exactly one preferred supplier per material. DCP-MATERIAL-BASIC-001 authorized; v1.0.5 cross-document review PASS. MES-004/005 returned to IN PROGRESS for this bounded rework; previous verification remains historical. MES-003 UOM consumed contract stays ready. No future task started.

## Material basic correction verified delivery — 2026-10-03

- MES-004/005 READY FOR ACCEPTANCE under v1.0.5 DCP-MATERIAL-BASIC-001. Material has direct basic maintenance, no business version/approval, named units/material-specific conversion, multiple suppliers and exactly one preferred among active relationships. Supplier qualification retained.
- Native MariaDB V010 applied; history 001–010 successful, append-only and no rebuild. Zero current test material/actor residue.
- Focused Maven gate: 10 unit tests + 14 integration cases PASS; only review-affected legacy ACTIVE-filter integration case rerun PASS. Frontend build and 8 focused unit tests PASS. Six desktop/mobile browser flows PASS across scoped gates; API mocked, actual native API/DB/audit verified separately. No full regression.
- Final review no CRITICAL/HIGH/necessary MEDIUM; OpenAPI 1405 local refs valid and cross-document alignment recorded. Prior releases immutable; receipt/batch consumer work remains future scope. Acceptance records in docs/acceptance/mes-004 and mes-005, historical v1.0.4 behavior explicitly superseded. MES-003 remains READY FOR ACCEPTANCE; later tasks not started. No commit/push/config staging.

## Reference list/edit presentation — 2026-10-03

User-authorized DCP-UI-LIST-EDIT-001 published in v1.0.6; v1.0.5 preserved. Shared list/edit style adopts supplied index.html, inline query labels and horizontal edit labels with desktop/mobile adaptation. No data/API/permission/state change or migration. Frontend build PASS, 12 existing desktop/mobile flows PASS, four IAM form layout checks PASS, scoped review no blocking findings. Evidence: docs/acceptance/ui-list-edit/ACCEPTANCE.md. MES-003/004/005 remain READY FOR ACCEPTANCE; authorization is not acceptance; future tasks remain NOT STARTED.

## MES-006 startup contract review — 2026-10-03

User requested “开发mes-006”. Required Material/UOM contracts are physically available (Task Card 4A satisfied; upstream acceptance not inferred). Status IN PROGRESS; implementation paused at DESIGN CHANGE REQUIRED: generic/conflicting API DTO/path contracts, missing route dependency/equipment/clearance persistence, process lifecycle/signature/eBR soft-dependency conflict, and product write/UI scope inconsistency. Concrete bounded proposal: docs/development/DCP-MES-006-R2-001-PROPOSED.md. Current v1.0.6 unchanged; no business code/migration/test execution in this startup review. No later task started.

## MES-006 design authorization — 2026-10-03

Human “确认授权” explicitly approves DCP-MES-006-R2-001. Approved v1.0.7 contracts and pre-implementation consistency review replace prior blocker. MES-006 IN PROGRESS. V010 DEV history verified; V011 reserved. No later task authorized.


## MES-006 verified delivery — 2026-10-03

- MES-006 READY FOR ACCEPTANCE under original design and authorized DCP-MES-006-R2-001 / v1.0.7. Products, BOM, route/parameters, controlled versions, signature approval, publication and independent UI completed. No MES-007 or later task started; no ACCEPTED status inferred.
- Native DEV V011 applied; 001–011 history and checksums validated, no old migration edits or reset. Final test actor/product residue=0.
- Targeted validation: 5 domain unit tests + 9 distinct MES-006 integration tests + 1 direct UOM regression PASS across focused gates; frontend build PASS; 6 distinct desktop/mobile browser cases PASS. Only failed/affected cases were rerun. Actual signature/audit rollback and two concurrent transactions verified.
- Scoped review findings fixed: no remaining CRITICAL/HIGH/required MEDIUM. 1,424 OpenAPI references valid, 17 original operations retained, cross-document review and hashes completed.
- Evidence: [MES-006 acceptance record](docs/acceptance/mes-006/ACCEPTANCE.md). LOW debt: existing bundle-size warning and process-list query volume; clean migration/CI gate remains required before merge. No commit/push/local-config staging.


## Material name retirement and inline layout correction — 2026-10-03

User explicitly removes 通用名/英文名/别名 and reiterates horizontal labels/controls. DCP-MATERIAL-NAMES-UI-001 / v1.0.8 is the current bounded correction. Removed the fields from material UI, current DTOs/read snapshots and search; retained legacy physical values and audits. Shared master/query styling and process sidebar inputs align labels with controls; current prototype CSS/design-system synchronized. No new migration or later task.

MES-003/004/005 remain READY FOR ACCEPTANCE after the correction: 4 targeted native integration cases, 5 frontend tests, build and 4 distinct desktop/mobile browser cases PASS, including 28 master list/form viewport checks. Scoped self-review has no CRITICAL/HIGH; inherited bundle/CI debt remains. Evidence: [material/UI correction](docs/acceptance/material-ui-correction/ACCEPTANCE.md). Historical delivery entries above remain historical, not claims that superseded fields/layout are current.


## MES-006～008 baseline review and MES-006 correction — 2026-10-03

- User explicitly requested continued MES-006/007/008 work, first reading all baseline designs including UI standards and prototype. Full numbered design-document review was shared between the implementer and two read-only domain reviewers; exact coverage/evidence is in docs/development/mes-006-008-baseline-review.md. v1.0.8 manifest: 80/80 hashes match. Authority unchanged.
- MES-006 remains READY FOR ACCEPTANCE after correcting the existing UI contract: dedicated approval signature dialog, object-filtered audit links, query preservation across create/edit/version/lifecycle/return, and correct restoration of keywords containing uppercase T. No backend/schema changes. Fresh vue-tsc PASS; 6 desktop/mobile flows PASS (14.8s); 2 signature geometry/cancel-credential cases PASS. API mocked in browser; prior native evidence retained, not rerun or claimed fresh.
- MES-007/008 are IN PROGRESS for authorized pre-implementation review, stopped at DESIGN CHANGE REQUIRED for full delivery. MES-007 runtime migration belongs to MES-009 and references MES-010 operations, while its own acceptance requires actual runtime integration. MES-008 has a receipt-edit UI without save API, unsequenced future MainBatch FKs, and cross-task eligibility/test ownership. Required producer contracts are not yet physically available. Product code for these two tasks has not been implemented; no READY/ACCEPTED claim.
- Concrete bounded resolution for review: docs/development/DCP-MES-007-008-SEQUENCING-001-PROPOSED.md. No frozen API/dependency/migration change has been applied. Definition/receipt portions are technically implementable; partial implementation would not satisfy current full task acceptance. No temporary production models, permissive release substitutes, extra migration, DB operation or full regression.


## MES-007/008 approved staged execution — 2026-10-03

Human explicitly approved DCP-MES-007-008-SEQUENCING-001. v1.0.9 pre-implementation consistency review PASS and authoritative pointer switched; v1.0.8 retained immutable. MES-007/008 continue IN PROGRESS under approved stages; no repeated approval is required within that boundary. V012 eBR and V013 WMS allocated after read-only native DEV history confirmation at V011. Definition/Designer/DSL and receipt/lot/ledger are current delivery; original real runtime/eligibility/batch acceptance remains pending later dependencies.


## MES-007/008 approved staged execution — 2026-10-03

Human explicitly approved DCP-MES-007-008-SEQUENCING-001. v1.0.9 pre-implementation consistency review PASS and authoritative pointer switched; v1.0.8 retained immutable. MES-007/008 continue IN PROGRESS under approved stages; no repeated approval is required within that boundary. V012 eBR and V013 WMS allocated after read-only native DEV history confirmation at V011. Definition/Designer/DSL and receipt/lot/ledger are current delivery; original real runtime/eligibility/batch acceptance remains pending later dependencies.


## MES-007/008 approved current-stage delivery — 2026-10-03

- Human “批准按该方案补齐契约并继续开发” implemented through approved DCP-MES-007-008-SEQUENCING-001 and cumulative v1.0.9. Prior v1.0.8 80/80 hashes preserved; v1.0.9 83/83 valid. Concrete 11 eBR + 35 WMS operations and exact permission mappings verified; no unapproved route.
- MES-007 LG-007A implemented: normalized definitions, independent Designer, safe DSL/lint/simulation, controlled lifecycle and immutable published query contract. Runtime LG-007B, real batch snapshots/corrections/review/signature/PDF and later operation FK remain scheduled with the actual producer tasks.
- MES-008 current stage implemented: draft/save/confirm receipts, material/policy snapshots, blocked lots, append-only ledger, inventory/move/adjust and fail-closed dependent writes. Real MES-008A eligibility/release and MES-009 reservation/issue/FEFO integration and delayed main_batch_id FKs remain required; no quantity-only gate or placeholder batch.
- Native DEV upgrade: V012 and V013 applied, 13/13 history successful and validated, no reset/repair/history rewrite. Read-only post-checks: test actor/unit residue 0, nine eBR definition tables and two ledger guards present.
- Targeted validation: backend 20 unit + 17 distinct native integration PASS. First native run 16/17; one invalid-date test collided with its own receiptNo, corrected only the fixture and reran that one case PASS. Includes two-transaction family/lot locks, optimistic version, replay, audit rollback, permissions, organization and direct producer contracts. Frontend typecheck + 6 unit + 12 distinct desktop/mobile cases PASS (browser APIs mocked). No full regression.
- Fresh scoped review: CRITICAL=0, HIGH=0, necessary MEDIUM=0 after fixes. LOW: process picker only first 100 packages; per-row aggregation reads; inherited Flyway tested-version warning for MariaDB 13. Hosted CI and full future integration gates remain mandatory before their relevant milestones.
- MES-006 remains READY FOR ACCEPTANCE. MES-007 and MES-008 remain IN PROGRESS by the approved sequencing rule; current-stage delivery is not original full-task acceptance. MES-008A/009/010 remain NOT STARTED; no further task automatically begun.
- Evidence: docs/acceptance/mes-007/ACCEPTANCE.md and docs/acceptance/mes-008/ACCEPTANCE.md, with desktop/mobile screenshots. No commit/push, credential staging or unrelated worktree cleanup.


## MES-006 human acceptance — 2026-10-03

Human explicitly stated “mes 006验收”. MES-006-R2 is ACCEPTED; prior readiness entries remain historical evidence. This acceptance does not change MES-003/004/005/007/008 statuses. Next recommended task is MES-008A-R2 (incoming material quality and release), consuming the delivered receipt/lot/ledger contracts; it remains NOT STARTED because this turn asks for sequencing, not implementation. No code, migration or test change.


## MES-007/008/008A completion request — 2026-10-03

- Human requested “完成mes007，mes008，mes-008A的开发”. MES-008A moved to IN PROGRESS for authorized startup; MES-006 stays ACCEPTED, MES-007/008 stay IN PROGRESS with prior stage evidence preserved.
- Current read-only task-card/backend/UI contract review found DESIGN CHANGE REQUIRED: incoming Create/execute pages lack routes despite frozen independent-page rules; qms_sample/qms_release_decision MainBatch FKs lack staged-install ownership before MES-009. Nullable MainBatch, retired MaterialVersion wording, GenericDTO and extensible signature providers were expressly ruled out as separate blockers.
- Concrete bounded proposal: docs/development/DCP-MES-008A-CONTRACT-001-PROPOSED.md. Existing 007/008 sequencing approval covers only the previously named WMS/eBR FKs, not these QMS references/routes. v1.0.9 authority retained; no new business code, migration or tests run.
- Full MES-007/008 runtime and production integration still requires actual MES-009/010 producer work; merely adding008A cannot complete those gates. No unapproved future business module was started or mock gate installed. Await bounded contract decision before affected implementation.


## MES-008A approval and MES-009/010 scope authorization — 2026-10-03

Human explicitly approved `docs/development/DCP-MES-008A-CONTRACT-001-PROPOSED.md` and added MES-009/010 to this development round. The six incoming create/execute routes, existing quality workbench mapping, nullable incoming MainBatch references, delayed QMS MainBatch FKs and finished_lot_id → md_material_lot.id target are authorized. MES-009/010 are IN PROGRESS for dependency and contract review; this does not imply acceptance or authorize additional frozen design changes.

Read-only native DEV history check: V001–V013 all successful. No new migration executed and no historical migration changed. Candidate allocations: V014 incoming quality, V015 real production + delayed batch FKs, V016 eBR runtime, V017 execution + operation FK; these are not claims of completed implementation. v1.0.9 remains authoritative until cumulative cross-document review passes. Existing worktree and persistent data preserved.


MES-009/010 contract review found additional frozen conflicts outside the approved incoming change: MainBatch snapshot NOT NULL vs release-time creation; required batch edit page without save API; plan-date UI without columns; undefined ProductionOrder lifecycle; conflicting Operation gate-failure states. Status remains IN PROGRESS, current integration implementation stopped at DESIGN CHANGE REQUIRED. Concrete bounded proposal: docs/development/DCP-MES-009-010-CONTRACT-001-PROPOSED.md; supporting scoped contracts: mes009-contract.md and mes010-contract.md. Incoming candidate extracted 32 existing operations/36 schemas. Current authority v1.0.9 integrity rechecked: 83/83 hashes match. No business-code change, new migration, or application test was performed in this review; no new readiness claim. Prior approvals remain valid.


## Incoming workflow clarification — 2026-10-03

User supplied the six-record required-inspection workflow from supplier delivery through QA release and downstream consumption. Scoped comparison is recorded in docs/development/mes008a-contract.md: existing frozen main chain agrees; QC_PASS is not material release, unresolved OOS blocks, report references exact result revisions, QA release atomically controls availability, TEST/retention samples preserve sampling lineage. Current WMS lacks a COA delivery-document association in receipt DTO/entity/UI; this remains a concrete implementation-contract gap, not a passed capability. No product/schema change or application test in this clarification; MES-008A stays IN PROGRESS. Existing exemption policy and future-task boundaries were not silently removed or expanded; prior approvals remain valid.


## Incoming quality full Baseline Review — 2026-10-03

At the user's explicit request, review and implementation mapping only: docs/development/INCOMING_QUALITY_IMPLEMENTATION_MAP.md plus incoming-review-data.md, incoming-review-api-ui.md and incoming-review-controls-tests.md. No product code, SQL, configuration, frozen release, or earlier candidate was changed; no database access, migration or application tests. User confirmation of the map/review is required before implementation.

Current v1.0.9 manifest rechecked 83/83. Six-record main chain maps to frozen requirements; current WMS receipt/QUARANTINE ledger stage matches, while incoming QMS/QA and real downstream consumer/trace producers are missing. New requirement-to-contract gaps include the QC specification-version producer/FK, sampling/retest-instance semantics, INVALID vs INCONCLUSIVE, request-level report aggregation, delivery attachments and incoming investigation linkage. Old draft enum/association assumptions must not become authority. Explicit stale signature predecessor is a static SEMANTIC MISMATCH requiring a later targeted fix/test. See the map for bounded DESIGN GAP/BASELINE CONFLICT findings and already-approved/resolved exclusions.

MES-008A remains IN PROGRESS; no task newly marked READY FOR ACCEPTANCE or ACCEPTED. Prior bounded approvals remain valid; no scope expansion to future tasks or implementation occurs through this review.

## Incoming quality confirmed foundation implementation — 2026-10-03

User confirmed “确认流程已清楚就可以开发”. Implemented the unambiguous frozen request/sampling/sample/inspection/report named state transitions and request-type dictionary; corrected explicit re-sign so it cannot skip the latest invalidated predecessor. No new database contract, API or UI was invented. Evidence: docs/development/mes-008a-confirmed-execution.md.

Targeted verification: 17 unit/contract tests PASS; 1 native MariaDB transaction regression PASS, unique fixtures rolled back. Flyway validated 13 migrations, current V013, no new migration. Scoped independent review: CRITICAL=0, HIGH=0. No full regression run. Existing MariaDB/Flyway compatibility and Mockito agent warnings remain non-blocking debt.

MES-008A remains IN PROGRESS. Domain helpers are not the six-record application workflow; request persistence, quality-standard version producer, retest identity, report aggregation, incoming investigation contracts and UI gaps remain bounded by the implementation map DG-01..08 / BC-01..02. Full incoming E2E and production reverse trace are not implemented or accepted. No task newly marked READY FOR ACCEPTANCE or ACCEPTED.

## Confirmed incoming mapping and approved-contract publication — 2026-10-03

Human confirmed “确认这份映射和审查结果，继续开发”. That review confirmation is recorded; it is not re-requested. Published only the earlier explicitly approved DCP-MES-008A-CONTRACT-001 A/B as cumulative v1.0.10: six create/execute routes, existing workbench mapping, staged QMS batch FK ownership and canonical finished-lot reference. Database/Domain/State/API/UI/Permission/GxP/Test/RTM/Integration/Task references were reconciled within that bounded delta before switching authority pointers.

Validation: previous v1.0.9 83/83 manifest hashes unchanged; new v1.0.10 84/84 hashes verified; 1,558 OpenAPI local references resolve; seven approved route mappings unique; API content otherwise unchanged. Independent bounded review found no blocking issues. No product-code change, migration, database operation or application test in this document-only step. Earlier test evidence is not counted again.

Remaining DESIGN GAP boundaries are unchanged. A concrete proposed completion boundary is docs/development/DCP-INCOMING-QUALITY-GAPS-001-PROPOSED.md; it is not approved and not a second formal authority. The first blocking application contract is the missing QC specification-version producer/FK required at request creation. The proposal also addresses sampling/retest/report/attachment/investigation/UI gaps and separates real MES-011 integration scope from mere flow approval. MES-008A and other in-progress tasks retain their status; no new READY FOR ACCEPTANCE or ACCEPTED claim.


## Incoming full-acceptance contract completion authorization — 2026-10-03

Human explicitly approved DCP-INCOMING-QUALITY-GAPS-001 completion and full acceptance scope, including the referenced MES-009/010 contract decisions, real MES-011 weighing/charge/genealogy and only the incoming OOS/blocking-deviation/retest subset of MES-012. Existing production IPC/CAPA/material-balance and MES-013 are not expanded. No repeated approval is needed for these bounded details. MES-011 and MES-012 (limited subset) move to IN PROGRESS for contract completion.

Plan: docs/development/incoming-approved-implementation-plan.md. Independent QC, incoming-chain and production contracts are being completed; root owns attachment contract and baseline integration. Native read-only Flyway history confirms successful V001–V013, next allocation starts V014. No new migration executed yet. Preserve all existing data and unrelated worktree changes.


## Approved full incoming contracts published — 2026-10-03

v1.0.11 cross-consistency review PASS and authority switched after approval. Exact QC/incoming/attachments/production appendices, typed OpenAPI, original-scope protection, UI field prototype and TC/RTM/dependency deltas complete. Source v1.0.10 84/84 hashes unchanged; merged 233 operations / 1,953 local refs checked. No runtime PASS inferred. Physical plan V014–V020 begins next; no new migration applied by publication.


## Incoming approved completion supplement — 2026-10-03

Existing explicit approval covers v1.0.12 bounded runtime DTO/review evidence, issue-return fact, deterministic independent weighing verification and actual trace node identities. Cross-document review passed before authority pointer switch; v1.0.11 preserved byte-for-byte. New migrations remain unexecuted at publication. MES-007/008/008A/009/010/011 and incoming-only MES-012 scope remain IN PROGRESS. No runtime acceptance or wider MES-012/013 authorization inferred.


## Incoming implemented chain and bounded acceptance evidence — 2026-10-03

The explicit contract-completion/full incoming acceptance authorization has been implemented through cumulative v1.0.14. Six distinct records, frozen QC standard, immutable original FAIL and approved retest, independent QA release, receipt attachments, real production reservation/issue/weigh/charge and reverse trace are integrated. eBR required-role review evidence binds the exact persisted signature ID in the original signature transaction. Frozen standard trace nodes reference the actual request FK, never the latest standard.

V014–V022 applied successfully; final Flyway validation/history shows 22 successes, 0 failures, current V022. Executed migrations and earlier releases remain immutable. v1.0.14 manifest 98/98 hashes verified. Read-only final test sentinel counts: incoming actors 0, incoming material fixtures 0; no database reset/repair used.

Targeted evidence: 26 distinct native integration methods PASS across scoped runs; final affected production-chain rerun 1/1 PASS (11.423s suite), covering actual Issue blocking before QA, confirmation blocking after QA rejection, and exact frozen-standard trace node/edge. 18 desktop/mobile browser cases PASS with API mocks; these are not native browser E2E. Frontend vue-tsc -b PASS. Platform signature unit tests 8/8 and eBR rules/boundaries 9/9 PASS. Scoped HIGH signature correlation and trace omissions closed with regression evidence.

The requested incoming positive chain and A–E scenarios have verified evidence and are available for bounded human acceptance. Broader MES-007/008/008A/009/010/011/012 task statuses remain IN PROGRESS: their full mandatory RTM cases are not all proven or waived. Production IPC/CAPA/material balance and MES-013 were not implemented under this bounded approval. No task is automatically ACCEPTED. Remaining formal coverage/producer boundaries are listed in docs/acceptance/incoming-quality/RTM.md; acceptance and compact evidence are in docs/acceptance/incoming-quality/ACCEPTANCE.md and evidence/verification-summary.json. No full regression/hosted CI/merge claim.


## Incoming quality bounded human acceptance — 2026-10-03

- Human approval: user explicitly replied “验收” after the final implementation/verification report.
- Accepted scope: this round's six-record incoming-quality chain and user A–E acceptance scenarios, including real production issue/weigh/charge and reverse trace, against v1.0.14 and the archived verification-summary.json.
- Scope status: `ACCEPTED` by explicit human approval. Evidence: docs/acceptance/incoming-quality/ACCEPTANCE.md and evidence/verification-summary.json.
- Broader MES-007/008/008A/009/010/011/012 task rows retain IN PROGRESS for their remaining formal RTM obligations. This acceptance does not waive those cases or expand production IPC/CAPA/material balance/MES-013 scope.
- Documentation-only acceptance recording; existing passing evidence reused. No additional tests, migration or automatic next-task implementation performed.


## MES-003–MES-005 human acceptance — 2026-10-03

Human explicitly confirmed “MES-003～005 确认验收”. MES-003-R2 (organization, units, equipment and personnel qualification), MES-004-R2 (material basic information and unit conversion), and MES-005-R2 (multiple supplier relationships and unique preferred supplier) move from READY FOR ACCEPTANCE to ACCEPTED. Existing implementation and verification evidence is retained. This documentation-only status update introduces no code/schema change or additional test run and does not start another task.

## MES-007–011 continued implementation — 2026-10-04

Latest closeout supersedes this earlier increment's MES-007 IN PROGRESS limitation: MES-007 is READY FOR ACCEPTANCE after TC-EBR-001 Designer/API gap correction and exact001..009 mapping. Evidence: docs/acceptance/mes-007/CLOSEOUT-2026-10-04.md and docs/acceptance/incoming-quality/CLOSEOUT-2026-10-04.md.34 distinct native methods,30 units,10 API-mocked browser cases and typecheck passed in this closeout; no full regression. No new physical migration; V001–V023 retained. Independent review has no remaining actionable HIGH. TC-BAT-002 stale errors were already resolved by the approved009/010 contract; no fresh DCR inferred. MES-008/008A/009/010/011 stay IN PROGRESS for actual concurrency and IPC/clearance boundaries; owner selection on docs/development/mes-007-011-native-concurrency-fixture-decision.md is pending before committed test-fixture retention. No human acceptance inferred.

- Authorization: human “继续完善和开发MES-007～011”; existing bounded v1.0.14 incoming/runtime/production approvals remain applicable. No frozen fields/routes/permissions/state/task redesign or future-task expansion.
- Fixed demonstrated eBR required repeated empty-array and optional zero-row defects; real production/execution/trace HTTP parameter binding; immutable production snapshot enforcement via new V023. All executed prior migrations retained unchanged.
- Verification:16 distinct new native methods PASS,14 affected units PASS; final affected regression EbrRuntimeIT3/3 (12.83s) and IncomingProductionIT2/2 (3.442s). Final HTTP/snapshot gate3/3 (10.62s). No full regression/repeated browser loop; frontend unchanged. Independent review findings corrected, no remaining demonstrated CRITICAL/HIGH in this increment.
- Native DB read-only check:23 successful/0 failed migrations, highest23;2 snapshot guards; test actor/role/material sentinels0. Existing data/configuration preserved, no reset/repair/alternate database or commit/push.
- Per-task exact methods, TC mapping and remaining obligations: docs/acceptance/incoming-quality/MES-007-011-2026-10-04.md and RTM.md. MES-007/008/008A/009/010/011 remain IN PROGRESS: positive/sequential flows do not waive unproven concurrent/negative matrices. MES-010 actual production IPC/clearance integration is DEPENDENCY_NOT_READY and remains fail closed. No whole-task READY/ACCEPTED claim; prior bounded incoming and MES-001–006 human acceptance preserved.


## Approved native concurrency closeout — 2026-10-04

Human “批准” approves only the bounded retained DEV concurrency fixtures in docs/development/mes-007-011-native-concurrency-fixture-decision.md. This supersedes the earlier pending owner-selection statement; no further design change or task acceptance is inferred.

MES-009 READY FOR ACCEPTANCE: required TC-BAT-001..004/TC-UI-002 and PRD-001..004 mapped in docs/acceptance/mes-009/CLOSEOUT-2026-10-04.md. Actual competing allocation exposed repeatable-read oversubscription; production now uses locked current allocation facts, current locked target order and one current child list for completion. Three related HIGH review findings are fixed. Native concurrency11/11 PASS (5.589s); affected production regressions2/2 PASS (9.995s); ProductionRules3/3 PASS. Added SubBatch no-release/no-QA mapped-route and no-action assertions passed in a single targeted retry. Reruns do not add cases.

MES-007 remains READY FOR ACCEPTANCE. MES-008/008A/010/011 remain IN PROGRESS: QA-supersession final-boundary concurrency is now evidenced, but wider required incoming/expiry/retest/freeze cases and actual IPC/clearance producer integration remain unclosed. No future-task producer invented. See updated incoming RTM and closeout for exact boundaries.

No new migration: V001–V023 validate successfully,23 success/0 failed.32 exact archived test lots all REJECTED/BLOCKED; intentional retained fixtures are not zero residue. Two RED-trial draft orders69/70 were reconciled through audited ProductionOrder:EDIT, preserving original allocation/audit evidence. No database reset/delete/repair, executed migration edit, commit/push or credential/config staging. Scoped review no remaining CRITICAL/HIGH; LOW pagination/N+1 and Flyway supported-version warning retained. Evidence: docs/acceptance/incoming-quality/evidence/native-concurrency-summary-2026-10-04.json.


## MES-007 human acceptance — 2026-10-04

User explicitly confirmed “MES-007 确认验收”. MES-007-R2 is ACCEPTED for the completed dynamic eBR definition/Designer/DSL/runtime scope and required TC-EBR-001..009 mapping in docs/acceptance/mes-007/CLOSEOUT-2026-10-04.md. Prior validation and recorded LOW debt remain preserved; PDF/archive TC-EBR-010 remains MES-013 scope. This status-only acceptance does not accept other MES tasks or change the frozen baseline. No implementation, migration or test rerun accompanies this record.


## MES-008–011 second closeout increment — 2026-10-04

User requested “MES-008～011 继续收尾”. Scope remains v1.0.14 and previously approved bounded completion, not implicit new frozen contracts or production MES-012/013 expansion. MES-007 stays ACCEPTED and MES-009 stays READY FOR ACCEPTANCE; no accepted task reimplemented.

10 new native cases passed:8 expiry/retest-date boundary cases across actual reserve/confirmIssue/weigh/charge, plus2 incoming guards (missing required result cannot submit review; exhausted retest quota and signed VALID original FAIL closure still block). Date tests use real authored receipt dates, full QC/signed-QA and physical database, advancing only thread-local Instant.now; no stored date/state SQL alteration. Initial gate6/8 passed,2 failed only on test postcondition column name; only those2 rerun,2/2 PASS. Incoming2/2 PASS. No production/UI change or full regression. Increment review no concrete CRITICAL/HIGH.

MES-008/008A/010/011 remain IN PROGRESS. Exact remaining contract/producer boundaries and formal TC mappings are recorded in docs/acceptance/incoming-quality/CLOSEOUT-2026-10-04-R2.md: frozen inventory command/API/permission/signature contract is unavailable (DESIGN CHANGE REQUIRED for that portion); actual required IPC/clearance producer remains DEPENDENCY_NOT_READY. Existing broad incoming partial RTM assertions are not silently waived. Do not fabricate a freeze command, generic updateStatus, IPC fact, clearance evidence or broaden MES-012.

No new migration: native Flyway23 successful/0 failed,max023,validated. All new fixtures rolled back; DATE-prefixed RELEASED fixture lot count0. Existing approved32 retained concurrency fixtures/history remain. LOW pagination/N+1 and Flyway supported-version warning retained. No reset/delete/repair/seed changes/commit/push/config staging. Evidence: docs/acceptance/incoming-quality/evidence/closeout-2026-10-04-r2.json.


## MES-008/008A/010/011 third closeout increment — 2026-10-04

User again requested continued closeout.7 new passing native methods close two-required-item numeric/text aggregation and exact review, retention label/storage/quantity/unit/source and signed disposal, real competing QA successors, actual competing consumption of one verified weighing, QC approval permission/version/signer failure/replay and V2 approval preserving V1, and empty/oversized delivery-document zero-write boundaries. Required mapping and limitations: docs/acceptance/incoming-quality/CLOSEOUT-2026-10-04-R3.md.7 is a distinct-method count, not rerun count; failed fixture assumptions corrected with failure-only gates. No production code/UI change or full regression. Scoped review no concrete CRITICAL/HIGH.

V001–V023 validate;23 success/0 failed,max023, no migration.35 approved retained native trial lots all REJECTED/BLOCKED (32 prior plus3 trials); original failed-trial evidence preserved, no physical cleanup. Other new facts rollback. No reset/repair/seed changes/commit/push/config staging. Existing LOW pagination/N+1/Flyway tested-version warning remains.

Statuses unchanged: MES-007 ACCEPTED, MES-009 READY FOR ACCEPTANCE, MES-008/008A/010/011 IN PROGRESS. Freeze workflow contract and production IPC/clearance producer boundaries remain; wider unclosed RTM clauses are not waived. A concise request for any already approved supplemental document paths is pending; it is not new design authorization. Existing-contract work proceeded without waiting. Evidence: docs/acceptance/incoming-quality/evidence/closeout-2026-10-04-r3.json.

## MES-008/008A/010/011 final readiness — 2026-10-04

- Status: all four `READY FOR ACCEPTANCE`; only human approval may set `ACCEPTED`.
- [Formal TC-by-TC closure / independent review](docs/acceptance/incoming-quality/FINAL-CLOSEOUT-2026-10-04.md); [exact evidence](docs/acceptance/incoming-quality/evidence/final-closeout-2026-10-04.json).
-13 new native cases,11 QC units,3 frontend interception units,4 QC desktop/mobile cases and typecheck PASS. Required existing receipt→QA→production chain and actual four-gate concurrency evidence reused with exact references; no repeated full regression.
- Fixed business signature HTTP401/403 response and prevented login renewal/replay on reauthentication errors. Independent review CRITICAL0, HIGH0 remaining after one verified fix pass. Existing LOW pagination/N+1 and MariaDB13/Flyway warning retained.
- V001..V024 validated, no migration/repair/reset/config staging. Accepted tasks, MES-009 readiness, MES-012 bounded IPC and MES-013 not-started preserved.

## MES-008/008A/010/011 human acceptance — 2026-10-04

- Explicit human approval: user replied “验收” to the completed four-task closeout report.
- MES-008-R2, MES-008A-R2, MES-010-R2 and MES-011-R2: `ACCEPTED`.
- Accepted scope and existing validation evidence: [final closeout](docs/acceptance/incoming-quality/FINAL-CLOSEOUT-2026-10-04.md).
- Administrative acceptance only: no product code, baseline, migration or database change; no repeat tests. Prior readiness entries remain historical evidence.
- MES-009 remains `READY FOR ACCEPTANCE`; MES-012 remains its approved IPC stage `IN PROGRESS`; MES-013 remains `NOT STARTED`. No next task started.

## MES-009 human acceptance — 2026-10-04

- Explicit human approval: user replied “mes009验收” after the task-status report confirming MES-009 was READY FOR ACCEPTANCE.
- MES-009-R2 (订单与正式批模型) is `ACCEPTED`. Scope and existing verification evidence: [MES-009 closeout](docs/acceptance/mes-009/CLOSEOUT-2026-10-04.md), including TC-BAT-001..004, TC-UI-002 and PRD-001..004. Earlier readiness entries remain historical evidence.
- Administrative acceptance only: no product code, frozen baseline, migration or database change; no repeat tests.
- MES-012 remains `IN PROGRESS` within its approved scope; MES-013 remains `NOT STARTED`. No next task started.

## MES-012/013 continued-development baseline review — 2026-10-04

User requests continued development of both tasks. MES-012 hard dependencies are ACCEPTED; MES-013 needs the remaining actual MES-012 producers. Scoped review found missing material-balance grammar/approval, production investigation/CAPA contracts and typed finished QA/PDF contracts, plus conflicting inherited balance state names. DESIGN CHANGE REQUIRED for these portions; existing accepted incoming and staged IPC contracts are preserved. Concrete findings and bounded approval proposal: [DCP-MES-012-013-CONTRACT-001-PROPOSED](docs/development/DCP-MES-012-013-CONTRACT-001-PROPOSED.md). No frozen redesign authorization inferred from the implementation request. No product-code change, migration, database operation or tests; statuses remain MES-012 IN PROGRESS / MES-013 NOT STARTED.

## MES-012/013 bounded contract completion approval — 2026-10-04

User explicitly approved “批准该范围，补齐正式契约后依次完成 MES-012、013”. DCP-MES-012-013-CONTRACT-001 is authorized within the proposal's listed balance, production quality/CAPA, finished release/PDF and consistency boundaries. The prior approval stop is superseded. [Execution ledger](docs/development/mes-012-013-approved-execution-plan.md) records contract-first implementation and validation. MES-012 stays IN PROGRESS; MES-013 starts after its real producer dependencies are available. Neither task is accepted by this design/development authorization.

## Reviewed MES-012/013 contract publication — 2026-10-05

v1.0.16 is the reviewed authoritative cumulative release under explicit DCP-MES-012-013-CONTRACT-001 approval. OpenAPI references resolve,35 added/typed operation entries checked, scoped independent HIGH findings closed, v1.0.15 remains unchanged. Native physical history24 successes/0 failures, highest24; planned V025 is not yet applied. Design publication does not imply runtime completion. MES-012 IN PROGRESS, MES-013 NOT STARTED until actual producer contracts are available.

## MES-012 readiness and authorized MES-013 handoff — 2026-10-05

MES-012-R2 READY FOR ACCEPTANCE after scoped plan/quantity/balance/production QC/OOS/CAPA implementation, native required producer/gate tests, direct IPC/incoming regressions, original UI targeted checks and independent CRITICAL0/HIGH0 review. No human acceptance inferred. Evidence: [MES-012 closeout](docs/acceptance/mes-012/CLOSEOUT-2026-10-05.md). Native V025 applied; latest read-only highest25/success25/fail0. User explicitly requested MES-012 then MES-013 and had approved the bounded completion contract; real MES-012 consumed producers are now physically available. MES-013 moves to IN PROGRESS for finished release/PDF/final lifecycle only. Its scoped Task Card and mandatory contract/reference sections were reviewed; append-only next physical allocation V026, no executed migration edits. Existing accepted tasks and local configuration exclusions remain preserved.

## MES-013 readiness — 2026-10-05

MES-013-R2 READY FOR ACCEPTANCE after finished QA/inventory/audit/signature/outbox atomicity, actual late OOS/original FAIL, explicit immutable supersession, locked source evidence, deterministic immutable eBR/PDF and original desktop/mobile UI were verified. Runtime RTM and15 distinct new native business methods (QA10/archive4/form-history1), schema2, focused backend4 units/platform signature13 regressions, frontend5 units/14 mock-browser cases/typecheck and CRITICAL0/HIGH0/MEDIUM0 scoped review: [closeout](docs/acceptance/mes-013/CLOSEOUT-2026-10-05.md). V026 append-only applied, latest26 validated/schema026; no failed migration or repair/reset. MES-012 public QC item projection corrected to its existing frozen contract and checked through actual downstream full-schema/native OOS chain; original source/signature facts unchanged. No design baseline change, no future task start, no automatic ACCEPTED. Remaining inherited LOW pagination/N+1 debt; native local configurations preserved/excluded.

## Human acceptance — MES-012 / MES-013 — 2026-10-05

The user explicitly replied “确认验收” to the joint MES-012 / MES-013 closeout. Both MES-012-R2 and MES-013-R2 are now ACCEPTED. Prior verification and readiness records remain historical evidence; no new test result or baseline change is implied. No subsequent MES task started.

## QA page presentation refinement — 2026-10-05

User supplied the selected screenshot and requested “调整 QA 批放行审核页面”. Scoped UI-QA-V / UI-REL-W presentation updated: compact checklist, batch/Gate banner, summary/actions, PDF history and derived evidence directory. No fields/API/permission/state/migration change; FINAL remains after QA decision. Typecheck and14 existing direct mock-browser cases PASS; final2 responsive/filter/original-fact cases PASS. [UI refinement review](docs/acceptance/mes-013/ui-refinement-2026-10-05/REVIEW.md). MES-012/013 ACCEPTED remains unchanged; no new human acceptance inferred, no next task started.


## Global UI V2 Phase 1 readiness — 2026-10-05

User explicitly approved global visual foundations only: Shell, typography, palette, spacing, cards and forms; preserve business layouts. Phase 1 is READY FOR ACCEPTANCE after frontend build/typecheck,20 existing targeted desktop/mobile browser cases and4 final new foundation cases PASS. Actual Vue pages were rendered in Chromium with API fixtures;11 final screenshots saved. Expanded sidebar228px/header56px, horizontal labels, mobile evidence drawer and collapsed icons verified. No business/database/API/permission/GxP contract change, no migration, no native database writes, no commit/push. [Phase 1 report](docs/acceptance/ui-v2-phase-1/REPORT.md). Inherited LOW bundle-size warning remains. Existing accepted MES statuses remain unchanged. Phase 2 NOT STARTED; STOP after this phase.


## Global UI V2 Phase 1 human acceptance — 2026-10-05

The user replied “确认” to the Phase 1 readiness report. Global UI V2 Phase 1 is ACCEPTED. Previous verification and screenshots remain evidence; no new test result, product-code change or commit/push is implied. Phase 2 NOT STARTED.


## Global UI V2 Phase 1.1 visual refinement readiness — 2026-10-05

User explicitly scoped FIX-01..04 only. Phase 1.1 READY FOR ACCEPTANCE: Material T1 query120px/natural responsive height, compact empty table; T2 form max1100px/mobile labels95px; Chinese recursive QA evidence labels retain original facts; FINAL PDF separated as post-decision archive per v1.0.16/DCP §5, server Gate count/progress unchanged. Typecheck,15 affected frontend tests,24 targeted Chromium cases and build PASS; current-run before/after screenshots saved and inspected. [Report](docs/acceptance/ui-v2-phase-1.1/REPORT.md). Business/API/DTO/state/permission/signature/database/baseline unchanged; no migration, no native business write, no commit/push. Scoped CRITICAL0/HIGH0; inherited LOW bundle warning. Phase 1 and existing MES ACCEPTED statuses unchanged. Phase 2 NOT STARTED; no new MES task. STOP.


## QA screenshot reference refinement readiness — 2026-10-05

User requested QA page follow the newly attached image. T6 presentation READY FOR ACCEPTANCE: compact header/checklist, summary/actions, parallel PDF history/evidence, Chinese status and category icons. Final PDF remains a labelled post-decision step inside PDF history under v1.0.16/DCP; no fictitious seventh Gate, product facts/photo or unauthorized Edit action. Typecheck/7 related frontend tests/build PASS;18 related desktop/mobile browser cases PASS,6 affected status/icon and4 final column-width cases PASS. [Latest QA visual report/screenshots](docs/acceptance/qa-reference-refinement/REPORT.md). Existing business script unchanged except visual imports; no backend/database/API/DTO/permission/state/signature/baseline change. Scoped CRITICAL0/HIGH0; inherited LOW bundle warning. Prior Phase1.1 evidence and user-owned configurations retained, no commit/push, no Phase2/new MES task or implicit acceptance.


## QA batch summary border refinement — 2026-10-05

User scoped batch-summary border only. Light1px outer/header border; remove inherited duplicate table rules, retain one light separator per row and none after final row. CSS only; no business/schema changes.2 targeted desktop/mobile Chromium cases PASS,14.2s; screenshots and note in docs/acceptance/qa-reference-refinement/REPORT.md. READY FOR ACCEPTANCE; prior accepted MES tasks unchanged, no Phase2/commit/push.


## Global UI V2 Phase 2A T1 pilot readiness — 2026-10-05

Authorized scope: Material master list, Production Batch list and Inspection Request list only. UI Template: T1 Query/List. READY FOR ACCEPTANCE: common compact Query Card, Result Card/count, external pagination, horizontal label/control pairing, single-column mobile filters and controlled table scrolling; primary identifiers emphasized. Batch state picker uses the existing MainBatch enum with localized QA labels. Other shared-resource lists retain their previous structures/action ordering. Existing query parameters, page sizes, routes, APIs, permissions and allowedActions preserved. Typecheck/build PASS;14 affected frontend unit tests PASS;6 targeted Playwright Chromium desktop/mobile cases PASS (15.7s), covering query/reset/pagination/state/empty results and zero console/page errors or page overflow.12 actual-app screenshots (API fixtures) saved outside the repository at C:/Users/Administrator/AppData/Local/Temp/mes-ui-v2-phase2a; populated desktop/mobile captures inspected. Scoped review: no CRITICAL/HIGH UI findings; inherited LOW bundle-size warning remains. No migration, database/business/API/DTO/state-machine/signature/baseline change. User-owned local database configuration changes preserved. Existing accepted MES statuses unchanged; no Phase2B, commit or push performed.


## v1.0.15 audit current-state remediation — 2026-10-05

ACCEPTED. Human acceptance: user explicitly replied “验收” on 2026-10-05 for HIGH01–08 and direct companion MEDIUM05. User explicitly approved DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001 on 2026-10-05. HIGH01–08 and direct companion MEDIUM05 are implemented and verified. ProductionOrderId existing query is formally published; incoming server read allowedActions fails closed and respects actor/qualification/evidence/signature concerns; equipment mutex/current-read gates enforce exclusive active occupancy and preserve segments. Cumulative FINAL BASELINE COMPLETE v1.0.17 is authoritative after cross-document consistency PASS;109 v1.0.16 parent files remain unchanged. [Final report](docs/review/MES_V1.0.17_AUDIT_HIGH_CLOSURE_REPORT.md) and [original current-state confirmation](docs/review/MES_V1.0.16_CURRENT_STATE_CONFIRMATION.md). Targeted15 backend unit/11 native integration/16 frontend unit/26 distinct Chromium cases PASS; typecheck/build PASS; Flyway26 migrations/V026 validated with no migration or physical schema delta. No remaining scoped CRITICAL/HIGH; MEDIUM01–04 and inherited LOW bundle warning remain. Existing accepted MES statuses and Global UI V2/T1–T6 preserved. Unique retained native fixture/audit evidence preserved; local database configs untouched. No full regression, commit/push, Phase2B or next MES task. MEDIUM01–04 is not included in this acceptance.


## Complete UI Blueprint delivery — recorded 2026-10-06

Status: `READY FOR ACCEPTANCE`. User authorized all remaining page families in `MES_COMPLETE_PAGE_DESIGN_BLUEPRINT_V1.md`; ordered implementation and UI consistency review completed on 2026-10-05. Human-approved inspection record/report Overview components remained unchanged; T5 retained its approved PC-only structure. [Implementation and verification report with screenshot gallery](docs/acceptance/ui-blueprint-2026-10-05/REPORT.html), [screenshot manifest](docs/acceptance/ui-blueprint-2026-10-05/screenshots.json), and [implementation ledger](docs/superpowers/plans/2026-10-05-complete-ui-blueprint.md) retain the scoped results and remaining limitations. Browser evidence uses actual Vue rendering with mocked existing API responses; no live database acceptance is inferred. Submitted and pushed to GitHub main as `092b219` on 2026-10-06. Submission/push is not human acceptance. MES-001–013 (including MES-008A) ACCEPTED statuses remain unchanged. No new test result, business contract, migration, or baseline release is introduced by this administrative record.

## Current authority pointer reconciliation — 2026-10-06

User requested correction of the conflicting current authority pointers. MES_TASKS current declarations and Task Card links now reference FINAL BASELINE COMPLETE v1.0.17, consistent with AGENTS.md, PROJECT_BASELINE.md and the approved v1.0.17 release manifest. The Global UI V2 and Page Template V2 current business-authority references were aligned without changing their approved visual/template rules. Frozen releases and dated historical records remain unchanged. This correction does not reclassify MEDIUM01–04 or infer additional acceptance.


## MEDIUM-01–04 technical resolution — 2026-10-06

User explicitly requested current-HEAD confirmation and minimum repair of remaining audit MEDIUM-01–04. Starting HEAD `092b219`: MEDIUM-01 remained valid; MEDIUM-02 was partially fixed; MEDIUM-03/04 were already fixed by the UI implementation. All four are now technically `RESOLVED` with code references and fresh targeted Chromium evidence in the [updated current-state closure report](docs/review/MES_V1.0.17_AUDIT_HIGH_CLOSURE_REPORT.md). Delivery status `READY FOR ACCEPTANCE`; resolution does not imply human acceptance. Eight distinct focused + eight direct regression Chromium cases,12 frontend unit cases, typecheck and build PASS across scoped final runs. No backend, API, database, migration or frozen business/visual contract change. Existing accepted MES statuses unchanged; prior MEDIUM01–04 remaining-debt entries are historical and superseded by this review. Inherited LOW bundle warning remains. No new MES task, commit or push.


## MaterialLot T4 screenshot refinement — 2026-10-06

READY FOR ACCEPTANCE. User authorized screenshot-based PC correction. UI Template:T4. Summary/lifecycle, six source cards and bottom three-column layout refined; existing receipt-item identity/quantity/date reused and unavailable imagery/metadata stated honestly. Original QC failure and independent report/release preserved. Typecheck/build PASS; affected Chromium desktop2 tests PASS10.8s, no overflow or console/page errors. Evidence: [desktop](docs/acceptance/material-lot-reference-2026-10-06/material-lot-desktop.png), [comparison](docs/acceptance/material-lot-reference-2026-10-06/comparison.png), [review](design-qa.md). No business/API/DB/migration/permission/frozen baseline changes. Existing accepted MES statuses unchanged; no commit/push. LOW inherited bundle warning remains; unsupported metadata and global Shell differences documented.


MaterialLot T4 follow-up (2026-10-06): READY FOR ACCEPTANCE. Reference long connecting lines/arrows, completed circles and below-node dates implemented. Release date now reads the existing authorized release-review decisions matching trace decision ID. Typecheck/build and2 targeted desktop Chromium cases PASS; no business/DB/migration changes. [Latest screenshot](docs/acceptance/material-lot-reference-2026-10-06/material-lot-timeline-desktop.png). No commit/push.


## T5 execution screenshot refinement — 2026-10-06

READY FOR ACCEPTANCE for the authorized existing-contract PC UI scope. User explicitly invoked Product Design image-to-code + design-qa with1280×720 reference. Connected horizontal/vertical steps, exact three-column primary geometry, compact tables and existing material evidence View/selection implemented. Full original charge evidence and controlled operation ownership retained. Typecheck/build PASS; targeted Chromium desktop scenario PASS11.6s, no console/page errors or overflow and three support rows visible. [Evidence/report](docs/acceptance/t5-reference-2026-10-06/REPORT.html), [visual review](design-qa.md). No backend/API/DTO/database/migration/permission/allowedActions/signature/frozen baseline change. Blueprint§9 exclusions preserved: no unsupported production photo/static illustration or deviation command; progress/time/status remain factual, so100% full-image identity is not claimed. Existing accepted MES task status unchanged. No commit/push; earlier MaterialLot work and local DB config preserved.

T5 companion validation: existing frozen IPC/signature binding desktop case PASS9.7s. Two distinct targeted Chromium cases total; no full regression.

Pre-commit validation2026-10-06: fresh typecheck PASS; five affected Chromium desktop cases PASS14.3s (MaterialLot chain/trace permission, signed inventory freeze in Inventory tab, T5 selected-record/evidence/commands, frozen IPC signature binding). Existing inventory test navigation updated for owning tab, assertions unchanged. No full regression.

## Material Master UI field cleanup — 2026-10-06

Authorized controlled presentation maintenance on main `15d4741`; T1/T2/T3 retain UI V2. Existing-contract cleanup `READY FOR ACCEPTANCE`: default columns/ordered fields simplified, hidden write values preserved, existing supplier qualification/material context shown. Typecheck/build PASS,9 affected unit tests PASS,1 targeted Chromium PC flow PASS. [Report/screenshots](docs/acceptance/material-field-cleanup-2026-10-06/REPORT.md). `DESIGN CHANGE REQUIRED` only for storage environment read/write (DB column exists but closed read/write DTOs exclude it) and genuine supplier/receipt/lot-source manufacturer ownership (no current association). No inferred manufacturer, new API/DTO/DB/migration/state/permission/signature or frozen release changes. Functional v1.0.17/UI V2 Closeout and accepted MES task statuses remain unchanged; this maintenance has not been human-accepted. Unapproved workbench proposals/UI V3 remain out of scope. No commit/push.

## Login / Home Workbench visual maintenance — 2026-10-07

ACCEPTED. The user explicitly approved the delivered reference-based login and home workbench presentation with “验收，提交和更新”. Existing Specialized page classification, UI V2 and FINAL BASELINE COMPLETE v1.0.23 remain unchanged. Five metric cards, six existing-route shortcuts, task/announcement positions and three chart panels follow the selected desktop reference. Shared seven-group navigation and permission filtering are preserved. Actual data and explicit unavailable states replace illustrated statistics; no unified task/announcement/inventory-warning contract or permission expansion is introduced. Actual browser checks at 1536×1024 passed with no overflow or final console errors; login error and successful login, menu search and requisition entry were verified. Pre-commit typecheck PASS, three affected frontend files/eight tests PASS, build PASS (13.40s). No backend/API/DTO/database/migration/state/signature/frozen release changes. LOW inherited bundle warning and documented visual/contract exclusions remain. [Design QA and acceptance evidence](design-qa.md). No new MES task started; local DB configuration and unrelated files remain excluded from the delivery.

## Home Workbench proactive audit follow-up — 2026-10-07

Final verification: typecheck and build PASS12.68s after the identity-loss fix. Six affected suites/20 cases PASS. Runtime delivery remains READY FOR ACCEPTANCE.

READY FOR ACCEPTANCE. User requested proactive audit/repair and current-data inspection. Nine confirmed homepage/shared-shell issues repaired: frozen production/sample counting, collapsed brand, chart container resizing, undated/cross-midnight trends, late async cleanup, expired-session logout and immediate removal/navigation of controlled content after identity loss. [Scoped audit and evidence](docs/review/MES_HOME_WORKBENCH_PROACTIVE_AUDIT_2026-10-07.md). Six targeted frontend suites/20 tests PASS3.51s; typecheck PASS. Actual browser desktop/side-bar/normal logout-relogin evidence passed before the final nonvisual identity-loss supplement, which has component regression evidence; further local browser navigation was blocked by browser security policy. Readonly DEV data audit covered98 business tables with no named test-marker hits;18 meaningful materials/two products/seven suppliers retained, trading tables empty. Two immutable supplier-code transcription inconsistencies remain MEDIUM debt, along with unavailable dashboard projections and inherited LOW bundle warning. No database/schema/migration/backend/API/permission/signature/frozen-release changes; no data deletion, new MES task, automatic acceptance, commit or push. Prior accepted maintenance record remains historical.

## Controlled Word printing — 2026-10-08

IN PROGRESS. Explicit user-authorized DCP-CONTROLLED-WORD-PRINTING-001: foundational Word template management and inspection-report PDF integration; user selected A (existing Word plus browser Office editing), separately approved schema additions and official LibreOffice installation. [Bounded contract](docs/architecture/CONTROLLED_WORD_PRINTING_DCP_2026-10-08.md), [API](docs/api/controlled-printing.openapi.yaml), [RTM/evidence](docs/acceptance/controlled-printing/REPORT.md). main 8f61f75 retained; user-owned parallel modifications preserved. Prior authority v1.0.23 and UI V2/T1–T6 remain unchanged pending design-authority review.

New V033 is NOT EXECUTED. No shared business data, existing Flyway history, role grants or running mes-boot restart. Required isolated migration validation awaits an authorized environment compatible with AGENTS.md; online Word JWT persistent-secret approval is pending. Backend unit and real LibreOffice PDF tests, frontend type/build and isolated browser UI validation are being recorded; API-fixture browser checks do not constitute real Office save/DB integration. No READY FOR ACCEPTANCE or human acceptance, push, deployment, environment reset or next MES task inferred.


## Production batch book — 2026-10-08

IMPLEMENTED / SCOPED CHECKS PASS, pending design-owner review and documented residuals. Explicit approved DCP-EBR-BOOK-001 covers versioned book templates/applicability mapping, frozen shared BATCH records only where configured, actual owner-source read-only directory, original runtime entry/review, real DOCX/PDF book and immutable history. [Contract](docs/architecture/EBR_BOOK_DCP_2026-10-08.md), [API](docs/api/ebr-book.openapi.yaml), [fields/templates](docs/acceptance/ebr-book/FIELDS_AND_TEMPLATES.md), [acceptance evidence and limitations](docs/acceptance/ebr-book/REPORT.md). V035/V036 applied to authorized localhost DEV after full private backup; prior executed migrations and frozen v1.0.23 unchanged. Synthetic transaction rollback only; no business data cleanup, new grant, push or authority switch. Existing online Office/JWT pending item remains separate. Final checks/deployment status recorded in the scoped delivery document.

EBR book final scoped execution: backend18/frontend6/captured-browser6 PASS; final local V036 JAR pid20084 healthy UP, old V034 JAR retained. No persistent synthetic business rows (book tables0); true new-book live browser recording flow remains unverified. [Deployment and delivery](docs/acceptance/ebr-book/DEPLOYMENT_AND_DELIVERY.md).

EBR book HTTP closeout: precise existing-authority routes added after authenticated403 red reproduction; final backend18/frontend6/captured-browser8/live-readonly-browser1 PASS. Final local V036 HTTP-fix JAR pid32520 UP (2026-10-08 14:43+08:00), 36 migrations valid; both prior JARs retained. Actual button is “打开生产批记录册”; old batches explain legacy scope and link original entry/review/QA evidence, without retrofit or grants. New persistent-book browser full flow remains an explicit gap. Library prepare_uploads unavailable; no attachment IDs or bypass upload.

2026-10-08 eBR book scoped supplement: book-template author publish approved; process/eBR/QA approval gates preserved. Backend47/frontend6/browser8 PASS; packaged worker real PDF PASS. New JAR not running: automatic approval rejected DEV service replacement against no-deploy instruction. Persistent new-book chain PENDING. Evidence: docs/acceptance/ebr-book/FINAL_SCOPED_ACCEPTANCE_20261008.md. Runtime acceptance awaits design owner; no push/commit.


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
- 测试：单元格修复阶段后端 7 项全过；最终标题阶段真实转换 5 项全过（含已安装 LibreOffice），package 成功。16 页长明细样例逐页缩略图复核，表头/页码连续；8 页实际数据库回滚册正在最终重生成。回滚测试有真实保存/提交合成字段及不可变旧产物校验，权限上下文和独立身份为测试夹具，不冒充持久业务。
- 浏览器回归 8 项使用真实回滚 JSON/PDF 捕获配合接口路由夹具，验证 UI、同 PDF 下载哈希、拒绝访问和原表入口；与真正持久化 UI 配置发布/订单批次创建证据分开。原始表单截图展示录入前空数据，不能当作真实填表闭环。
- Library：目录截图 libfile_0ed73503344881919c19b012fc780ad3 已更新 v1（视口截图，页头 y=0）。DOCX libfile_b75fa641aa98819195004173c693c603、长明细 PDF libfile_a3e2b44066d4819195fc6975f1a6590e 已更新 v1。失败旧版保留，不新建重复附件。Windows 本地扩展属性写回不受支持，Library 写入成功且返回身份已保存私有 JSON。

---



2026-10-08 最终标题版本另2项回滚测试及8项浏览器测试通过；8页册与16页明细PDF复核并更新Library原身份v1。真实持久流程仍受称量策略阻碍；当前DEV PID30432健康UP。


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

## 基础管理取消审计与版本控制 — 2026-10-09

**READY FOR ACCEPTANCE — NOT HUMAN ACCEPTED.** Human-approved [DCP-BASIC-NO-AUDIT-VERSION-001](docs/development/DCP-BASIC-NO-AUDIT-VERSION-001-APPROVED.md), including production/eBR binding redesign and preservation of historical evidence. [Contract](docs/architecture/BASIC_CURRENT_DEFINITION_CONTRACT.md), [bounded OpenAPI](docs/contracts/BASIC_CURRENT_DEFINITION_OPENAPI.json), [cross-document review](docs/review/DCP_BASIC_CURRENT_CONSISTENCY_REVIEW.md).

- 实现：基础管理各功能前后端取消审计追加、客户端版本前置条件及计数递增；工艺包只有一个可维护的当前定义，不再创建/选择/比较/审批/发布工艺版本。基础维护保留权限、组织隔离、校验、事务、行锁和幂等。新生产/eBR 绑定 processPackageId；生产快照、eBR 自身修订/签名/审计、WMS/QA 证据继续受控。历史 eBR 复制按工序编码映射当前身份，缺失工序拒绝复制；新整册模板拒绝旧工艺版本绑定。
- 迁移：原 DEV 已成功执行新增 V041/V042，42 migrations validated/successful，失败 0；执行前完整私有备份成功。原审计、签名和工艺版本 INSERT 字节一致；原配方/路线/子项/eBR 模板/生产快照共 67 行的迁移前列值全部保持不变。未修改旧迁移，未 repair/clean/重建数据库。
- 验证：Maven 编译/package PASS。10 类定向真实 DEV 集成共 99 个独立测试 PASS（MasterResources 12、MaterialSupplier 17、Process 9、DirectProcessActivation 5、BasicNoVersion 7、BasicMaintenanceConcurrency 1、Ebr 9、EbrRuntime 13、ProductionBook 3、FunctionalClosure 23；重跑不重复计数）。覆盖真实并发连接、旧证据复制、失效绑定拒绝、签名/独立复核、冻结快照及已发布整册/PDF 不变。前端 6 suites / 22 tests PASS、typecheck/build PASS。未宣称 full regression 或 hosted CI 已运行。
- 运行页面：原 checkout 编译的最终 JAR 已启动于唯一 8080，health HTTP 200/UP；复用真实 5173。Browser skill/plugin workflow 不可用，使用标准 Playwright Chromium。11 项真实页面/API 检查 PASS、相关 console/API errors 0；九类基础页面无审计/版本入口，真实单位新增和页面编辑保存不携带 versionNo/If-Match，单一工艺包选择器可实际选择。1440×900 与 1366×768 截图已人工查看，保留 UI V2；无本次另供设计参考图。证据：`C:/Users/Administrator/AppData/Local/Temp/mes-basic-live/`。唯一 NVUI 临时单位及其幂等记录已按准确 ID/编码清理，计数未递增、审计 0；其他业务和历史记录未删除。
- 评审：跨文档一致性 PASS，批准范围内无未解决 CRITICAL/HIGH；基础旧审计/版本 CI 断言已按批准契约替换，生产/eBR 受控断言未削弱。既有 ACCEPTED 任务不重开，其他系统管理工作状态不改变。当前权威为 v1.0.23 + 有界批准补充，历史冻结发布不改写。
- 非阻塞债务：MEDIUM，本轮浏览器直接刷新观察到登录态丢失，未在本次基础管理范围内改造登录/续期；真实验收采用页面内导航。LOW，现有前端主 bundle 大小警告仍在。源码、部署、定向验证已完成，等待人验收；未提交/推送，保留无关工作区修改及本机 `.env`/local/ci 配置。

- 2026-10-09 用户称谓更正：当前菜单、路由标题、列表/维护字段、权限显示及生产/eBR 选择器统一为“生产工艺”。真实数据库菜单 ID 10 经既有受控菜单 API 更新，保留权限、路由及系统配置审计；无数据库结构变更或新迁移，未修改已执行迁移/历史证据。同步批准补充、当前契约和 UI 文档；技术标识 processPackageId 等保持兼容。typecheck PASS，2 suites / 6 tests PASS，真实 5173/8080 菜单、列表、新增页和 eBR 选择器 4 项检查 PASS。评审无新增 CRITICAL/HIGH，原非阻塞债务与 READY FOR ACCEPTANCE 状态不变。截图：`C:/Users/Administrator/AppData/Local/Temp/mes-process-terminology/`。

## eBR 模板名称缺口 — 2026-10-09

DESIGN CHANGE REQUIRED — 用户指出关键模板名称缺失。定向源码检查确认创建/列表页面、DTO 与持久化实体均只有 templateCode，没有 templateName。已有基础管理补充不包含 eBR 名称新增字段。具体方案见 [DCP-EBR-TEMPLATE-NAME-001-PROPOSED](docs/development/DCP-EBR-TEMPLATE-NAME-001-PROPOSED.md)：新建必填名称，草稿受控编辑，列表/详情/选择器展示及名称搜索；新修订/快照包含名称，旧签名/摘要/快照不改写。等待明确设计变更批准；本轮仅检查及方案文档，无业务代码/数据库变更，未运行测试，既有 ACCEPTED/READY 状态不改变。

## eBR 模板名称实施 — 2026-10-09

**ACCEPTED — HUMAN CONFIRMED 2026-10-09.**  用户明确“批准” DCP-EBR-TEMPLATE-NAME-001；[批准补充](docs/development/DCP-EBR-TEMPLATE-NAME-001-APPROVED.md)、[有界契约](docs/architecture/EBR_TEMPLATE_NAME_CONTRACT.md)。范围仅模板名称存储/受控草稿维护/查询展示/生产选择及新冻结内容，旧历史不回填。V043 已分配在真实 DEV 最高成功 V042 后；迁移前旧模板、生产快照和签名私有备份已保存。实现和定向验证已完成，既有任务验收状态不改变。

- 实现：名称新建必填、草稿受控编辑，按修订继承；列表/详情/设计器及生产 eBR 选择显示名称+编码/版本，名称或编码可查询。更新 DTO、实体、当前 OpenAPI、批准 DCP/契约/RTM/两份 UI 权威及 [一致性评审](docs/review/DCP_EBR_TEMPLATE_NAME_CONSISTENCY_REVIEW.md)。生产快照冻结名称；旧 null 名称不进入原 canonical JSON，既有签名/摘要/批次记录不改写。
- 迁移：V043 成功，前序迁移未改动；原 4 条模板、8 条生产快照、111 条签名所有原列值逐条比对不变。私有备份与验证 JSON 位于工作站 backups，未进入仓库。未 repair/clean/重建数据库。
- 验证：Maven package PASS，8 unit tests PASS；EbrIT 12、EbrRuntimeIT 13、ProductionBookIT 3 共 28 项独立真实 DEV 集成 PASS；加强的新名称冻结/新修订改名不影响原批次测试单项重跑 PASS（不重复计数）。前端 typecheck/build PASS、2 suites / 8 tests PASS。真实 5173/8080 新增、受控保存、详情/列表展示、名称查询 5 项 PASS；页面截图已查看，证据 `C:/Users/Administrator/AppData/Local/Temp/mes-ebr-name-live/`。
- 评审：cross-document PASS，新增 CRITICAL/HIGH 0。已有 ProductionViewsTest 过时字段断言按此前批准的 processPackageId 契约补齐，保持精确断言。既有 MEDIUM 刷新登录态问题及 LOW bundle 警告保留，本次未扩大处理。
- DEV 测试数据：两条明确 EBRNAME 前缀草稿（ID 251/252）及其审计证据保留，未发布，未删除任何受控或历史记录。已人验收；未提交/推送，保留无关编辑及本机数据库配置。

- 最终运行：真实 checkout 最终 JAR 已重启于 8080（PID 15968），health HTTP 200/UP；5173 继续复用该源码。新名称冻结与后续修订改名隔离的加强断言已在真实 DEV 通过。

- 人工验收：用户明确回复“验收”，确认本次 DCP-EBR-TEMPLATE-NAME-001 模板名称交付。仅将本项标记 ACCEPTED，既有验证/评审与非阻塞债务保留；不扩展其他任务验收、不自动开始下一任务，不推断提交或推送授权。

## 用户要求的真实数据电子批记录模板样张 — 2026-10-09

READY FOR ACCEPTANCE — TEMPLATE DRAFT / NOT PUBLISHED. 用户请求“做一份电子批记录的模板给我看看，用真实的数据”，授权通过既有设计器/API创建模板内容；未修改冻结业务字段、状态或系统代码。

- 数据来源：真实 8080 API读取当前 DEV 的产品 ID 1（KCL30 氯化钾溶液，30ml:3g/瓶）、生产工艺 ID 1、处方基准120000ml，以及物料 ID 1–4：氯化钾12000g、羟苯乙酯60g、95%酒精600ml、甘油600ml；绑定实际当前工序定义 IDs。DEV 数据包含演示/验证记录，不声称来自真实生产现场。
- 已创建并受控保存模板 ID 291，编码 EBR-KCL30-120L-PREVIEW-20261009，名称“氯化钾溶液 30ml:3g · 120L 电子批生产记录”，V1/DRAFT。六工序、52字段，按现有角色配置独立复核/重新认证签名。实际生产值及签名留空，处方值标明来源；未预填正常/合格结论，未新增强制称量或无来源工艺限度。适用生产工艺的当前完整性状态为 DRAFT，仅供查看；未提交、审批、发布或下达批次，未改写历史记录。
- 样张：docs/examples/ebr/KCL30-120L-20261009/ 内 HTML、8页 PDF及读取时source-snapshot.json。8页包括封面、六工序、独立历史参考附录。附录记录来源是 GET /main-batches/16/ebr（批号20261007-KCL-001，另一个旧工艺快照）；不会作为本模板执行数据。理论4000瓶明确标注120000ml÷30ml换算参考且未计损耗。
- 验证：真实 POST/PUT/GET HTTP200，既有服务定义/引用/单位/DSL/角色校验通过；真实5173设计器显示正确名称、工序与定义。六表52字段、所有MANUAL字段默认值为空检查PASS；PDF文件有效、8页，封面及第一工序截图人工查看，修正打印分页后首工序单页完整。无系统代码变化，本轮未重跑无关编译/回归。
- 评审/迁移：无新增Schema/Flyway迁移，无新增业务/GxP契约；旧证据保持原状。保留现有刷新登录态和bundle非阻塞债务。仅交付查看草稿及样张，不代表可投产的受控归档或现场真实性认证；不更改既有已验收任务，不提交/推送。


## 主设计批准与实施下发 — 2026-10-10T13:02:40.670492+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION IN PROGRESS。来源：主对话01a115d2-8ace-7574-9a08-ac48dfc970cc本次委派，已审阅DCP005第1–11节及修正实体/API，批准同一文件及18操作/61schema契约作为实施基线。用户先前已明确授权主对话批准、任务做到收尾及必要DEV迁移/后端更新/测试；本记录不把主设计批准写成用户最终验收，不改历史审批。

本次白名单：mes-reporting打印领域/API/应用/持久化及直接消费provider、mes-boot定向测试与新增Flyway、正式Vue打印页面/API/路由/现有导航入口、当前同一设计/契约/复核/任务索引。现有其他菜单/权限/UI未提交改动保留，开始清单和文件备份位于本机print-source-implementation/worktree-before.txt及file-backup/。不修改旧迁移、旧归档正文/hash/触发器、冻结releases、无关业务，不提交或推送。

专项环境覆盖：本次主设计下发明确要求迁移先在隔离测试库验证，仅该验证允许专项隔离库；正式DEV仍为已有localhost:3306/hospital_pharma_mes_dev、Redis6379、后端8080、前端5173；不drop旧库、不repair旧checksum、不增加持久安全开关。真实目标/版本/备份可恢复确认后才执行DEV迁移。复杂打印集成初始预算90分钟，最多2轮自动修复，重复同根因立即报告；不改批准验收断言。当前实施开始，迁移/编译/业务/UI/PDF验证未运行，不能宣称验收或部署完成。


## 打印配置当前执行门禁 — 2026-10-10T13:33:06.877264+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION BLOCKED（非用户验收）。本次已保存193文件备份和工作树清单；真实目标localhost:3306/hospital_pharma_mes_dev最高成功V051。V052新增源码已在专项隔离库执行成功，约5.8MB DEV备份已恢复验证；循环组合FK、跨组织拒绝、源编码/类型唯一、sidecar不可更新删除与事务回滚检查通过。原DEV未迁移/部署。相关模块编译PASS；定向单元30项中29通过、1错误（Mockito测试夹具嵌套stubbing），没有修改原断言。两轮自动修复已用完，遵守MES_DEV_RULES.md §6A暂停新代码/测试，已请求本复杂集成专项放宽。

未完成：主设计刚批准的baseTemplateVersionId/sourceSelection显式升级增量同步及实施；真实DEV集成/权限并发/校验竞态/旧打印全链回归；前端真实API接入、浏览器刷新/重启持久化与截图、真实收货/检验PDF及部署验证。不得宣称后端闭环、功能READY、用户验收或生产部署完成。本机准确清单/日志/约束证据：C:\Users\Administrator\Documents\Codex\2026-10-10\task\print-source-implementation\implementation-status.json。保留所有他人未提交改动、旧迁移/历史归档，未提交推送。


## 打印配置当前执行门禁 — 2026-10-10T13:33:58.507059+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION BLOCKED（非用户验收）。本次已保存193文件备份和工作树清单；真实目标localhost:3306/hospital_pharma_mes_dev最高成功V051。V052新增源码已在专项隔离库执行成功，约5.8MB DEV备份已恢复验证；循环组合FK、跨组织拒绝、源编码/类型唯一、sidecar不可更新删除与事务回滚检查通过。原DEV未迁移/部署。相关模块编译PASS；定向单元30项中29通过、1错误（Mockito测试夹具嵌套stubbing），没有修改原断言。两轮自动修复已用完，遵守MES_DEV_RULES.md §6A暂停新代码/测试，已请求本复杂集成专项放宽。

未完成：主设计刚批准的baseTemplateVersionId/sourceSelection显式升级增量同步及实施；真实DEV集成/权限并发/校验竞态/旧打印全链回归；前端真实API接入、浏览器刷新/重启持久化与截图、真实收货/检验PDF及部署验证。不得宣称后端闭环、功能READY、用户验收或生产部署完成。本机准确清单/日志/约束证据：C:\Users\Administrator\Documents\Codex\2026-10-10\task\print-source-implementation\implementation-status.json。保留所有他人未提交改动、旧迁移/历史归档，未提交推送。


## 本任务修复上限有界例外及显式升级批准 — 2026-10-10T13:36:48.414288+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION IN PROGRESS。来源同一主对话本次委派：用户已授权主对话全权批准当前任务并做到收尾，主设计批准DCP005复杂前后端集成对MES_DEV_RULES §6A两轮修复上限作专项放宽，只覆盖本任务兼容/契约集成，不修改全局规则、不弱化断言和安全要求；逐原因定向修复/复测，同原因复发或需要改变设计立即报告，不无限循环。原Mockito撤权拒绝断言保留；失败是夹具在thenReturn参数构造时再次stubbing，先构造夹具即可修复，非权限校验通过。复杂集成预算仍按本任务90分钟记录，不因此扩至无限。

主设计批准现有Word/Canvas保存增加可选baseTemplateVersionId与sourceSelection(mode=INHERIT|UPGRADE,targetSourceRevisionId,targetConfigHash)。默认INHERIT，首次新模板解析唯一当前已发布源，同编码后续版本继承旧引用。UPGRADE需base及显式目标、同businessType/同源、当前绑定与ENABLED/PUBLISHED/类型覆盖，hash/目标冲突409，缺字段/集合兼容422带问题；复制新模板版本并重验，不修改旧版或静默删除绑定。LEGACY转CONFIGURED必须显式UPGRADE及完整兼容；前端仅增加版本升级差异预览/确认，不提供任意换源。本增量沿同一文件身份同步契约/矩阵后实施。此前BLOCKED仅为已获专项放宽的阶段记录，不是当前终止。


## DCP005执行进度（非最终验收）— 2026-10-10T14:17:01.430749+00:00

正式实现候选已接入。V052在第二隔离库恢复备份并执行通过后，经后续明确授权及自动审批重试，于13:58:57 UTC通过Flyway升级本机hospital_pharma_mes_dev；当前最高成功V052、136表。V001–V051原始文件哈希均保留。打印纯单元30/30、真实DEV回滚集成10/10通过，原断言保留、回滚检查通过；收货实际PDF与批次单据/LIST已覆盖，检验模板真实PDF转换已覆盖，尚未覆盖实际检验artifact/PDF。

正式Vue数据源API、四路由、列表/四步/属性检查器、目录关联和字段、多类型冲突、元数据管理（中文同行等值筛选）、草稿/取消导航保护、真实样本校验及明确版本发布、最小字段预览已接入。Canvas独立datasetKey循环、固定版本字段加载和显式源升级差异确认已接入；Word导入保存同契约，OnlyOffice回调明确继承base。最新类型检查和构建PASS（既有大包提示保留），Canvas6旧+3新增测试9/9通过。未运行正式浏览器，不将构建或mock原型作为正式验收。

自动审批拒绝且未执行：print-source-final-backend-checks.py（历史正式归档重用当前策略复核、单位LIVE_LOOKUP语义及真实检验PDF/升级回滚追加用例）；print-source-deploy.ps1（停止明确8080 MES PID24276、重打包、local8080及正式5173启动）。拒绝理由分别为额外后端行为范围与停止既有服务/部署授权未被审查接受。没有间接执行或绕过。8080仍旧进程health200，5173未启动，原型5174正常。正式计划URL http://127.0.0.1:5173/admin/print-data-sources 当前不可打开。临时正式浏览器写开关仍待专用stdio进程专项批准。

当前状态 IMPLEMENTATION IN PROGRESS / BLOCKED DEPLOYMENT AUTO-REVIEW，非READY/ACCEPTED/收尾。仍需处理精确审批、后端补充检查与DEV部署、真实浏览器写路径/重开/导航/权限/控制台/设计图对照、实际检验PDF、升级/绑定并发/发布竞态。完整候选清单及证据C:\Users\Administrator\Documents\Codex\2026-10-10\task\print-source-implementation\implementation-status.json 与 CURRENT_OUTCOME.md。保留193文件初始备份、file-backup及5.8MB已恢复验证的DEV备份。未提交/推送/合并。


## 最新专项授权执行与门禁 — 2026-10-10T14:32:25.101208+00:00

历史归档补充已按用户准确动作授权应用；PrintService.generate命中正式归档前调用现有archiveAuthorize，保留provider.load、组织隔离、当前业务权限、旧字节/hash，单位目录语义准确LIVE_LOOKUP。30项单元通过；显式升级/默认继承/错误hash/旧模板不变用例通过。实际检验artifact/PDF/evidence用例首次因夹具遗漏正常模板业务绑定被规则拦截；补齐标准publish->bind步骤后定向1/1通过，未弱化断言。修正后WMS真实PDF/批次历史回归2/2、旧生命周期13/13再次通过。合计12个不同DEV用例分别通过，不宣称一次新全套12/12执行。

同一部署动作在用户准确列表后回复授权的证据下重试，仍被自动审批拒绝且未执行。拒绝理由：授权回复缺乏对具体服务中断副作用明确逐项确认。再次STOP，不间接启动/换端口/杀其他进程。8080仍旧local进程24276，5173未启动；正式运行/浏览器验收NOT RUN。CUAWright临时写客户端仅准备未启动，开关未变更，不宣称已完成正式截图。完整构建/模块包及hash已保存，boot薄包不可直接运行，重打包及回滚说明保留。

本任务90分钟窗口截至14:32:40 UTC；已申请专项延长30分钟至15:02:40 UTC，尚未收到明确批准，届时停止新修改和新测试，保持上述部署门禁。尚缺正式服务启动、真实API/浏览器核心闭环、并发与竞态验收；不是READY/ACCEPTED/任务收尾。未提交推送或合并。



## 2026-10-10 本次授权重新打包/重启与核心验收收尾

DEV已部署（后端PID25580/前端26676，8080/5173 HTTP200），V052与52迁移验证通过；源码修复精确HTTP权限路由、参数显式命名、实际单位字典SQL及按授权元数据生成默认设计。最终定向回归9/9、安全7/7、前端构建PASS。真实收货取数/发布、中文字段模板预览、409冲突回滚及真实PDF生成成功；归档PDF字节/摘要不变。浏览器完成保存重开、取消与只读等核心流程，1280标签同行10/10。CUAWright PDF嵌入阅读空白，完整多类型/字段关系CRUD浏览器矩阵及多行真实样本等未完成，状态为DEV_DEPLOYED_CORE_API_PASSED_BROWSER_ACCEPTANCE_PARTIAL，不能宣称全量验收通过。原型和正式代码均保留、无提交推送。完整证据：C:\Users\Administrator\Documents\Codex\2026-10-10\task\print-source-implementation\CURRENT_OUTCOME.md。

## 2026-10-10 15:59 UTC DCP005补齐验收
同一用户明确批准的MES DEV临时CUAWright测试已继续：真实API16检查PASS；浏览器CRUD8断言PASS、多选解除/恢复/重开3PASS、模板中文字段及只读详情2PASS。新QA源21绑定两个独立DEV测试类型，复用现有收货读策略；最终两类型真实取数通过后发布修订26，测试附加字段/关联已删除保存，保留全部版本/审计。原源19完整不变、模板68固定修订22、归档27 PDF摘要不变。无产品源码、迁移、权限、凭据或服务改变，无提交推送。
保留自动断言FAIL：正常refresh401资源日志触发console全空断言；低权限UI测试期待403但现有守卫实际重定向首页。既有生产账号7条API403和匿名401已通过；无既有仅查看/管理/发布拆分角色，不新建权限。WMS及5个现有APPROVED检验报告均仅1行，无真实多行样本，NOT RUN。PDF拦截已被主设计接受工具限制，先前未弹窗未复现，不再改PDF或安全保护。
全部专用临时写进程已退出，15:58:53默认mutation=false/browser inactive检查PASS，15:59:27专用进程清单空。状态DEV_DEPLOYED_CORE_FLOW_VERIFIED_ACCEPTANCE_PARTIAL / IN PROGRESS，非READY/ACCEPTED。完整报告与原始失败证据：C:\Users\Administrator\Documents\Codex\2026-10-10\task\print-source-implementation\ROUND2_ACCEPTANCE_20261010.md。


## 2026-10-10 打印数据源限定DEV验收补充收尾

同一主设计明确批准的认证/首页回退/隔离fixture约定已写入DCP005与docs/development/decisions/PRINT_SOURCE_ACCEPTANCE_SUPPLEMENT_20261010.md。新增认证5项+实际router/源组件11项=16/16 PASS，vue-tsc PASS；同DEV回滚IT5/5 PASS（真实三行SQL排序、双独立集合各3行、source view/manage/publish上下文正反例、两个独立事务竞争已有归属被唯一键1062/23000拒绝）。0失败/0错误/0跳过。每项回滚验证源21恢复、fixture收货单不存在，用户/角色/授予数量不变；产品指纹无变化。未开写浏览器、部署、打包、新迁移、真实角色/凭据或Git提交推送。

原2浏览器FAIL和原报告保留，不改判历史；另列批准修订后21项结果。前置实机API16与浏览器13项PASS沿用。两次新测试夹具/观测修正均保留首轮日志，未修改旧断言/产品：Antd插件注册、按受控十进制字符串读取数量且3.750期望不变。限定DEV交付未发现阻断问题，可审阅使用；整体IN PROGRESS，待主设计/用户最终验收，不写ACCEPTED。

环境/未运行限制：真实窄权限账号及已有多行单据现场实机（本轮获批隔离上下文/回滚fixture代替）；空类型首次竞争赢家、发布压力竞态、正式375及全量UI矩阵未测；已接受PDF内嵌工具限制不再扩大。完整最终报告C:\Users\Administrator\Documents\Codex\2026-10-10\task\PRINT_SOURCE_FINAL_DEV_DELIVERY_20261010.md；机器结果print-source-implementation/acceptance-supplement/results.json；最终源清单FINAL_CHANGED_FILES_20261010.md/json。


## 2026-10-10 打印数据源限定DEV交付明确批准

当前状态：DEV_ACCEPTED／本次开发交付完成。批准来源：主对话01a115d2-8ace-7574-9a08-ac48dfc970cc设计与验收负责人本次明确委派；用户已授权由其批准。依据已核实DEV部署、16项真实API、13项浏览器通过断言及本轮21/21针对性补测，批准本次限定DEV交付，不再设置待用户人工验收阻塞已约定开发范围。此前IN PROGRESS/待验收文字保留为阶段历史，本条是当前任务状态。

保留范围限制：PDF内嵌阅读器工具限制；真实窄权限账号/已有多行业务单据现场实机未验证（已批准事务fixture/隔离上下文补测）；空类型首次竞争赢家、发布压力及正式375视口未覆盖。不能写成生产验证或全系统回归完成。原2条FAIL、首轮夹具失败及后续批准补充21/21验证均保持原始历史，不改判旧报告。

本次仅更新任务索引和最终交付报告；未运行新测试、打开浏览器、修改产品/权限/凭据/迁移、部署或扩张任务；未提交推送。完整最终报告：C:\Users\Administrator\Documents\Codex\2026-10-10\task\PRINT_SOURCE_FINAL_DEV_DELIVERY_20261010.md。既有入口http://127.0.0.1:5173/admin/print-data-sources；原型http://127.0.0.1:5174/#/printing/data-sources。

## 业务测试数据禁用标识清理 — 2026-10-12

**IN PROGRESS / NOT ACCEPTED。** 用户再次明确业务数据不得含Demo/Test、测试/演示/模拟/模似及明显占位词。正常API完整读取12类当前业务目录和7个字典项目，发现51处命中；已通过正式部门PUT逐条清除12个description中的精确“测试样例｜”前缀，12次HTTP200及GET回读一致，原ID/组织/父子关系/负责人/成员/状态/角色授权不变。剩余39处（部门代码12、资格代码13、登录名7、设备类型3、字典值4）明确未完成，不能只改显示或报清零。新增JSON业务载荷预检工具，14个正反例自检PASS；AGENTS和MES_DEV_RULES已按用户要求明确元数据不得混入业务字段。未改权限、凭据、资格判定、数据库结构或历史签名，未提交推送。详见[当前缺陷及执行记录](docs/review/BUSINESS_DATA_NAMING_20261012.md)和[用户要求/纠正边界](docs/development/decisions/BUSINESS_DATA_NAMING_20261012.md)。本任务优先于继续生成新业务数据。

## 数据字典 CODE / 名称重叠局部修复 — 2026-10-12

**ACCEPTED — 用户于2026-10-12明确回复“验收”，仅确认本次CODE / 名称重叠局部修复。** 用户截图要求修复CODE文字覆盖名称；仅将现有T1列表CODE/名称列加宽并复用ellipsis/title、表格内部横向滚动。源码三处显示调整，无API/路由/权限/业务值或数据库/Flyway变化，未提交推送。typecheck/build和diff-check exit0；真实5173/8080五视口70单元格边界/完整title检查及6项只读功能断言通过，4字典读取HTTP200。1452/1440/1280查询5对同行通过；900/375既有查询标签换行/拥挤、375全页宽384失败保留，故不宣称整页视觉验收完成。既有bundle警告与认证恢复401日志保留，自动修复轮0。依据[设计锁定](docs/development/decisions/DICTIONARY_COLUMN_OVERLAP_20261012.md)及[详细证据/范围评审](docs/review/DICTIONARY_COLUMN_OVERLAP_20261012.md)；截图/JSON位于 `C:/Users/Administrator/AppData/Local/Temp/mes-dictionary-overlap-20261012/`。

验收登记：本次只更新任务状态和补充人工确认记录，沿用已有实机/构建/评审证据，未重跑测试、修改源码、部署、迁移或业务数据。原IN PROGRESS阶段和900/375失败证据在详细记录中保留；本次人工确认不改判旧失败、不豁免查询标签同行规则、不扩展到数据字典整页验收或其他任务，未提交推送。


## 业务占位内容命名清理 — 2026-10-12

**IN PROGRESS / BLOCKED AT TWO REPAIR LIMIT / 2 CURRENT SOURCE NAMES SAVED / NOT ACCEPTED。** 用户Sentinel_2536f5ce9be48191a843adb89ba7de7f明确启动；[执行及阻塞报告](docs/review/BUSINESS_NAMING_CLEANUP_EXECUTION_20261012.md)。新完整DEV本机备份6,065,025B/137表4249行等效恢复核验通过。全存储文本+17DOCX初扫188候选，98正常检验枚举逐值分类后90业务显示/标识命中，技术及不可变证据另列。真实Chrome（CUAWright二进制缺失后允许回退）正常保存源19/21为收货单打印数据/收货验收记录数据，version5/10，新草稿33/34，旧发布22/24/26和所有绑定ID/配置其它值不变。17模板/10artifact/9batch/50签名/菜单权限成员/字典/产品物料完整存储字节不变。原命中逐项SQL复读2已改/88仍在；不能宣称清零。13项名称API批处理0完成，数组适配/正式路由两次修正后首项400，根因If-Match未按现有IAM契约带引号；第三轮未修。打印原ID改名接口不存在、已发布及归档不得覆盖；菜单编码/资格设备/登录关联需受控成组处理。生成入口已识别但未纠正/未运行，规则禁止再运行，自动门禁接线未完成。无新迁移/重启/增权/签名/物理删除/提交推送；其它角色/成品链任务保持暂停。

## 数据字典项目入口及新增表单可用性 — 2026-10-12

**READY FOR ACCEPTANCE — 仅管理入口、现有项目展示及新增/编辑表单交互，未人工验收。** 用户要求已有项目清楚直观、方便新增，并调用ui-ux-pro-max授权评审后直接修改。现有T1及简单弹窗保留：显式“管理字典项（数量）”、字典名称及现有项目/启停统计、主新增按钮、新增/编辑标题与保存文案、焦点/必填反馈及小屏同行配对。最终typecheck/build通过；字典两项定向fixture回归2/2；真实5173/8080只读交互13/13及1440/1280/900/375弹窗几何通过。无迁移、业务写入、提交推送，未复测真实新增保存持久化。范围评审未发现新增阻断问题；既有全页窄屏查询失败、认证恢复401、大包警告及存量命名清理问题保留，不能宣称整页/全部CRUD验收完成。自动产品修复轮0；一次脚本按钮定位修正及原失败证据保留。依据[设计锁定](docs/development/decisions/DICTIONARY_ITEM_ENTRY_20261012.md)与[体验评审、验证及截图](docs/review/DICTIONARY_ITEM_ENTRY_20261012.md)。此前CODE/名称修复ACCEPTED不扩展至本条。

### 正式项目复查及状态纠正 — 2026-10-12 13:28

**当前状态 IN PROGRESS / NOT RELEASED / NOT ACCEPTED，取代上条局部READY结论。** 用户要求复查正式项目并询问打包发布。实际确认5173运行本checkout Vite新源码，8080为Java classpath开发服务；13:24:45已重新生成前端dist，未发布/重启。实机发现行编辑会覆盖新增未保存内容，补齐行/树/启停保护；弹窗内增加成功反馈。最终build通过、原2项+新增1项fixture回归3/3、真实只读17/17和四视口弹窗检查通过，diff-check通过。无迁移/业务数据写入/提交推送。真实新增保存、重开及持久化/窄权限负例仍NOT RUN；详情编辑只管基本信息、须返回列表管理项目的绕行和主列表横向操作仍存在，整页窄屏/存量命名债保留。不能把局部技术检查等同整体体验或功能交付；旧证据保留、不改判失败。详见[正式复查和交付判断纠正](docs/review/DICTIONARY_FORMAL_RECHECK_20261012.md)与[本轮有界修复记录](docs/development/decisions/DICTIONARY_FORMAL_RECHECK_20261012.md)。自动产品修复轮0，预算内收尾。

## 数据字典功能界面MES标准流程重做 — 2026-10-12

**PASS FOR PROTOTYPE / READY FOR HUMAN DESIGN CONFIRMATION / NOT FORMALLY INTEGRATED / NOT ACCEPTED。** 用户最终明确页面结构：列表每行“查看 / 修改 / 添加数据项”；新增/修改字典为独立T2表单页；查看字典为独立T3详情页并以列表展示数据项；数据项弹窗上方录入、下方已有列表，可连续添加。添加保存后字段清空、焦点回到编码、数量刷新、列表滚动并高亮最新行；同一字典编码按MariaDB大小写不敏感语义拒绝重复。按钮顺序按用户规则固定为查询/重置、保存/取消、添加保存/关闭；字段/API/权限/状态契约不变。首次原型功能检查和FAIL证据保留为历史；当前体验结论以最终复审清单为准。

2026-10-12 已由 `Codex SELF-REVIEW` 使用[完整MES UI/UX审查技能](.agents/skills/mes-ui-ux-audit/SKILL.md)和 `ui-ux-pro-max` 完成5界面×4视口20张截图、45项维度、19项WCAG及动态交互审查。结论FAIL：UX-001新增/编辑描述行栅格断裂（HIGH）、UX-002桌面查询框过窄且断点反向（HIGH）、UX-003基础字典保存错误不可定位（HIGH）、UX-004详情搜索缺少可访问名称（MEDIUM）、UX-005编辑CODE低对比禁用态（MEDIUM）。数据项弹窗必填、大小写不敏感重复拒绝、连续新增、清空回焦和新行定位通过。清单校验器因2项交互和2项几何检查失败而exit1，未改规则或断言强制通过。详见[完整审查报告](docs/review/DICTIONARY_MES_UI_UX_COMPLETE_AUDIT_20261012.md)和[证据清单](docs/review/evidence/dictionary-ui-ux-audit-20261012/audit-manifest.json)。正式5173源码/服务未继续修改或发布，无迁移、数据库写入、提交或推送。

经用户授权已直接修复上述5项，并在复审中补正默认主按钮文字、占位文字和输入边界对比度。最终源码SHA256 `62C7E714AD339FBFA7539DC270F7CEB38245D21A0E99C8B0D767DAC0DB903A25`；构建PASS；同一5×4矩阵重新生成20张最终截图，45项维度、19项WCAG、动态交互及几何检查满足通过条件，清单校验exit0，结论`PASS FOR PROTOTYPE`。原始FAIL及证据保留。等待用户确认精确哈希后才能正式集成；尚未发布、未验收。最终证据见[复审清单](docs/review/evidence/dictionary-ui-ux-audit-20261012/recheck/audit-manifest.json)。


## MES UI/UX 完整审查技能与门禁 — 2026-10-12

**IMPLEMENTED / VALIDATED / EFFECTIVE。** 用户要求减少反复补充审查规则并让`ui-ux-pro-max`执行完整审查，随后明确要求纳入集成测试验收。新增仓库技能`mes-ui-ux-audit`、外部权威参考、逐图八类评审矩阵和证据清单校验器；AGENTS、MES_DEV_RULES及MES五阶段G2/G5已强制接入。G5现在必须实际运行`ui-ux-audit`套件，且`integration_acceptance.ui_ux_audit`必须绑定与正式体验审查完全相同的验证清单和`READY FOR ACCEPTANCE`结论；缺失或不一致会使整个集成验收FAIL。审查者固定披露Codex SELF-REVIEW，Product Design不再冒充独立审查主体。技能结构校验PASS；清单正例exit0、缺栅格证据+开放HIGH反例exit1；工作流测试及新增G5负例通过；py_compile和diff-check PASS。无业务源码、数据库、迁移、服务、部署或发布变更。详见[变更与验证记录](docs/development/decisions/MES_UI_UX_COMPLETE_AUDIT_SKILL_20261012.md)。

## 全项目自动修复上限更新及命名第3轮恢复 — 2026-10-12
**规则10轮已写入 / BUSINESS NAMING LIVE EDIT BLOCKED BY AUTO REVIEW。** 用户最新Sentinel_8428024124d08191b12f4d19a70b82d3及Sentinel_87506368c214819183fb1b92917cb51f覆盖中间5轮指令；AGENTS.md、MES_DEV_RULES.md §6A、mes-feature-workflow技能及MES_FEATURE_WORKFLOW有效引用均更新10轮。第10轮失败先暂停报告问题/尝试/验证/原因，用户批准后再分析下一阶段；旧计数不清零，历史报告/其它安全和设计门禁不改。[文件摘要与原文快照](docs/development/decisions/AUTOMATIC_REPAIR_LIMIT_20261012.json)。原补丁工具读取报reparse point，本机精确目标/父目录属性检查为普通文件/目录，受审查native编辑获批并完成，无审批拒绝于规范编辑。

业务命名已用2轮，授权恢复第3轮只修强ETag引号，代码已保存、断言不变、原备份摘要核验一致。实时13项API编辑启动被自动审批拒绝：审批仅认可直接用户DEV物料清理，未将委派历史视为可信命名写入授权。进程未创建、13条本轮新增写入0；没有默认权限或浏览器/其它脚本绕过。已请求直接人类确认具体13条菜单/字典/权限显示名编辑；确认前停止该写入，生成入口未执行，17模板及其它冻结数据未修改。上轮2条源名称真实保存与88处剩余事实保留，不报READY/ACCEPTED。证据round3-approval-block.json。

## 全项目自动修复上限调整为20轮 — 2026-10-12

**APPROVED / EFFECTIVE。** 用户明确删除最多两轮规则，并将全项目每任务自动修复上限调整为20轮，覆盖此前2/5/10轮全局指令。第20轮仍未通过时，任何后续修复前必须暂停并报告全部尝试、关键修改、验证结果、重复失败签名、已排除原因、剩余根因、下一阶段最小范围和风险；取得用户明确批准后才能继续，轮次累计不清零。已同步AGENTS.md、MES_DEV_RULES.md §6A、mes-demo/AGENTS.md、mes-feature-workflow技能、MES_FEATURE_WORKFLOW治理入口及当前数据字典原型未来执行约束；[批准记录](docs/development/decisions/AUTO_REPAIR_LIMIT_20_20261012.md)。普通UI十分钟、相同失败提前停止、设计/写入/视觉/验收门禁不变。历史任务报告、10轮快照及此前停止事实保留原文。本次未继续原型修复/验证，未改业务源码、数据库、迁移、服务或发布状态。


## 业务命名清理第3/4轮恢复收尾 — 2026-10-12

**IN PROGRESS / CURRENT NAMES15 VERIFIED / ORIGINAL HITS75 REMAIN / NOT ACCEPTED。** 全项目10轮有效，原2轮保留；第3轮强ETag格式修正后补交明确用户授权，同一工具只重试一次获批：菜单100/101、字典1/48/49和8权限显示名13次HTTP200/独立GET全部通过，累计15名称。第4轮只读核验fixture按实际source_revision_id归属修正，137表完整单元格保护PASS：50签名/17模板/10artifact/9batch/角色权限成员/旧发布源及绑定不变。当前4/10修复，没有重复写入。最新原命中SQL复读75仍在（部门12旧码当前仍存在），不宣称全库零。新13项Chrome回看两次请求头策略加载失败NOT RUN；源前轮截图保留。三持久化Java种子入口接现有命名checker，禁止遗留违规码再写；编译PASS10.290s/三码载荷预期拒绝，完整seed未运行/未造资格证据；其它生成入口未宣称全覆盖。17模板6未发布/10历史停用/1发布68，无原ID改名接口或冻结/归档约束，明确现有干净草稿/校验/发布/绑定替换可选下一阶段及需用户指定设计者确认的历史处置，尚未执行。审批阻塞已解除，不再重复确认已完成13项。无迁移/部署/删库/增权/签名/角色成品续跑/提交推送。详见BUSINESS_NAMING_CLEANUP_EXECUTION_20261012.md最新节及latest-closeout-summary.json。


## 2026-10-15 生产工艺—工序—EBR—CPP 定向集成回归

**ACCEPTED — 用户于2026-10-15 22:44（Asia/Shanghai）明确回复“验收”，确认本次生产工艺—工序—EBR表单—CPP定向集成回归及其修复。** 用户要求测试现有生产工艺、工序、EBR表单和CPP关联。实际本机同DEV：后端工艺5+EBR61=66/66；ProcessIT9+DirectProcessActivationIT5+EbrIT5=19/19，0失败/0错误/0跳过；前端定向7/7；最终typecheck/build PASS；真实5173/8080只读链路与面板边界16/16；5状态×4视口20图G5 manifest validator exit0。首轮19项4 errors源于旧EBR夹具schema2/角色ID输入未更新，修正构造后通过，68个原断言片段不变。修复CPP及Canvas属性栏按视口断点导致的侧栏展开裁切，改用编辑容器断点，未改业务合同。当前UI组合hash `C10BA53A8E6C78C5BE97260889977870A847C1E89E4BC95AA2009C96789EB567`；旧UI hash及验收记录为历史，不改判。64迁移validate PASS/当前V064，无新迁移；业务测试事务回滚，无浏览器业务保存、签名、权限或历史改写；启动现有8080/5173服务，未提交推送。

范围限制：未跑真实生产批次完整填写/签名/偏差/QA放行、窄权限页面全矩阵、全工艺CRUD状态或全系统回归；生产工艺新版待批准原型未擅自集成。不能称全系统/真实生产全链验收。详见 [本轮证据与范围](docs/review/PROCESS_EBR_CPP_INTEGRATION_20261015.md)，`artifacts/process-integration-20261015/acceptance-summary.json` 绑定相同G5清单。本轮不改变其它已完成功能的人验状态。

## QC质量标准改造为基础信息库 — 2026-11-02

**READY FOR ACCEPTANCE — 功能、契约、迁移与定向回归已完成，真实5173/8080页面人工验收 NOT RUN，未提交推送。** 用户于2026-11-02明确要求"检验标准只做为一个基础信息库维护，增删改查就可以，不涉到审批和批准。不需要做版本的管理。标准基础信息有一个标准的依据…还有标准适用于那些物料和产品，也需要管理"，并在同一日的设计确认中选定：标准库全量重构 + 下游全部改为「标准快照」冻结（授权删除历史签名证据）、保留检验项目、一个标准可同时挂多个物料和多个产品。授权与有界范围见[DCP-QC-SPEC-BASIC-LIBRARY-001](docs/development/DCP-QC-SPEC-BASIC-LIBRARY-001-APPROVED.md)，当前契约见[QC质量标准基础信息库契约](docs/development/qc-standard-basic-library-contract.md)、[OpenAPI补充](docs/development/qc-standard-basic-library-openapi.json)、[设计锁定](docs/development/decisions/QC-STANDARD-BASIC-LIBRARY-DESIGN-LOCK.md)。

实现：`qc_specification` 成为唯一可变当前定义（新增 basis_type/basis_reference/effective_date/remark/status，移除 subject_type/product_id/单主体三触发器）；新增 `qc_specification_scope` 多对多适用范围、`qc_specification_revision` 维护记录（含前后状态与原因，删除条目后保留）；检验项目改为挂在标准上；删除 `qc_specification_version` 及其版本、审批、签名、内容摘要与独立性整套控件和 `qms:specification:approve|retire` 权限与 `ebr:sign` 要求；新增 `qms:specification:delete`；字典 `QC_STANDARD_BASIS` 7 项。API 收敛为7个端点（列表/创建/详情/改头/改范围/改项目/删除），全部带 ETag+If-Match+Idempotency-Key，未知字段（含任何审批/版本字段）一律 VALIDATION_ERROR。下游四表 `qms_inspection_request`/`qms_production_plan`/`qms_sample`/`qms_finished_inspection_request` 的既有引用列就地翻译为标准id并改建外键到 `qc_specification(id)`（列名按冻结契约保留，不改名）；来料请验、生产质量计划、成品检验与追溯改存标准id+冻结快照摘要，追溯节点类型改为 `QC_SPECIFICATION`。前端重做 T1 列表与 T2/T3 编辑详情（标准的依据用 DictionarySelect，适用范围用多选 QcScopePicker，维护记录展示），删除版本页与版本路由，并清理 ReadReference/IncomingDocument/IncomingReferencePicker/成品与生产质量计划中的旧端点与"版本"文案。

迁移：新增并已成功应用 `V070__qc_standard_basic_library.sql`、`V072__qc_standard_revision_after_nullable.sql`、`V073__qc_standard_revision_trail_survives_deletion.sql`（DEV 最高 v073，均 success=1，未改动任何历史迁移、未 flyway repair、未重建库）。DEV 实测量：标准13、适用范围13、项目13、全部 basis 已填且 ACTIVE，version 表已删，四表引用值均落在标准id上。

验证：后端 `mvn -o -DskipTests compile`、`mvn -o test-compile` BUILD SUCCESS；`QcSpecificationRulesTest` 7/7、`QcSpecificationIT` 7/7、`QcFinalContractIT` 13/13；下游回归 `IncomingClosureIT` 9/9、`IncomingQualityIT` 6/6、`IncomingCrossroleBatchAIT` 6/6、`IncomingCrossroleBatchBIT` 2/2、`IncomingFinalContractIT` 1/1、`IncomingQualityAcceptanceIT` 3/3、`IncomingCrossroleNegativeIT` 7/7、`IncomingCrossrolePreflightIT` 3/3、`IncomingProductionIT` 13/13。前端 `npx vue-tsc --noEmit` exit0；`QcSpecificationModel.test.ts` 6/6、`IncomingReferencePicker.test.ts` 7/7、production/trace/master/api 25/25（全量 vitest 95文件/229通过，RoleDetailView 与 printSourceAcceptance 两文件在并行下超时，单文件复跑 11/11、11/11 通过，属并发超时非回归）。`ProductionQualityPlanIT` 15项中我方的1项失败已修复，仍余2项 `New product plans cannot bind legacy batches`：属既有夹具用 schema1 批次建计划导致，与本改造无关，未改无关代码。

范围限制：真实5173/8080页面交互、几何与视觉验收未执行，故不宣称正式验收通过；未跑全系统回归；按用户授权删除的仅限 QC 标准审批/版本证据，其它历史证据未动；工作区既有其它未提交改动全部保留。

## QC质量标准新增/编辑页交互重构 — 2026-11-02

**READY FOR ACCEPTANCE — 两页签（基本信息/适用范围）+ 检验项目常显的新增/编辑页，无删除标准、无底部操作条；列表页零改动；真实5173/8080保存链路已实测通过；未提交推送。** 用户当日原话："编缉的操作很不友好"、"适用物料/产品是一个列表，点击填加按钮出现弹窗选择物料或产品，不要维护记录。就是很简单的增删改查"、"是修改检验标准的编缉或新增页面，检验标准的列表页面不用修改"、"适用范围做成一个页签，编缉和新增，没有删除标准，还有［底部操作条截图］这个不要"、"检验项目是重要的，适用范围是次要的，适用范围可以做成页签，检验项目不需要"。设计确认记录见 [QC-STANDARD-EDIT-UX-DESIGN-LOCK](docs/development/decisions/QC-STANDARD-EDIT-UX-DESIGN-LOCK.md)，业务/数据/API/权限仍受 [DCP-QC-SPEC-BASIC-LIBRARY-001](docs/development/DCP-QC-SPEC-BASIC-LIBRARY-001-APPROVED.md) 约束，页面模板仍为 T2。

改造内容：①适用范围由多选下拉改为「两者列表 + 主色添加入口 + 搜索/多选/分页选择弹窗」，列表显示名称/编码/行内移除，已选但不在搜索页的主体按 id 回读补全；②检验项目由「一项一张大卡」改为 8 列表格（编码/名称/必检/结果类型/合格标准/方法/操作），行编辑在 720 宽弹窗内完成，数值限值尾零归一、单边限值显示 ≥/≤ 并带单位名，移除走二次确认；③删除「维护记录」卡片（`qc_specification_revision` 后端照旧写入，仅前端不再展示）；④底部操作条（未保存状态点 + 维护原因 + 返回/保存）沉到内容流末尾，页头新增主色「保存标准」，两处入口消除"保存按钮难找"；⑤新增页不再预置空白检验项目，改显空态；⑥校验前移：本地校验给出首个错误并滚动定位到对应区块 + 1.6s 描边高亮，弹窗内错误就地显示。

文件：`frontend/mes-web/src/views/quality/QcSpecificationDetailView.vue`、`QcSpecificationItems.vue`、`QcScopePicker.vue`；新增设计记录与验收页面；**未改** `QcSpecificationListView.vue`（mtime 保持 11:34，早于本次开工）、`src/api/qcSpecifications.ts` 契约、router、任何后端代码与迁移。

验证：`npx vue-tsc -p tsconfig.app.json --noEmit` exit0；`vitest run src/views/quality` 12文件/53通过；真实 5173/8080 Playwright：清空限值后弹窗内拦截"数值项目至少填写一个限值"；搜索"氯化钾"勾选 2 个物料→列表 2 行→保存条转"有未保存的修改"；未填原因保存被拦"请填写本次维护原因（页面底部操作条）"；填原因真实保存 `versionNo 2→3`、后端 scopes 变为 `MATERIAL:YL-0001, MATERIAL:BZ-0003, PRODUCT:KCL30`；移除后再次保存 `versionNo 4`、scopes 复原为 `PRODUCT:KCL30`（matchesOriginal=true）。几何：1280 视口标签右边界 431/910、控件左边界 443/922，间距一致，标签与控件中心线偏差 0；页面三卡片无遮挡，操作列「编辑/移除」完整可见；新增页 0 项目空态、两个范围空态、无维护记录卡。截图与可复跑脚本在 `artifacts/qc-spec-edit-review-20261102/`。

环境修复（非本次代码改动）：8080 原进程加载的仍是改造前打包（`mes-security` 内含 `qms:specification:approve|retire`），导致 PUT/DELETE 全部 403；已重新 `mvn -o package` 并重启，新包规则为 create/delete/edit/view，保存链路随之打通。该实例当前由本次会话启动，若会话结束被回收，按 `java -jar backend/mes-boot/target/mes-boot-0.1.0-SNAPSHOT.jar --spring.profiles.active=local --server.port=8080 --spring.flyway.target=074` 重启即可。

第二轮（同日，页签化，用户截图指掉底部操作条）：页面由「三张卡片纵向堆叠 + 底部操作条」改为 **三页签**（基本信息 / 适用范围 / 检验项目），新增页与编辑页同构；删除页头「删除标准」入口（后端 DELETE 与 `qms:specification:delete` 权限码保留，仅本页不暴露）；移除底部操作条（未保存状态点 + 维护原因 + 返回列表 + 保存标准），保存入口只留页头主色按钮，未保存提示改为页头右侧小字；维护原因改为点保存后弹出的「确认保存标准」窗内必填，新增态不弹窗、`reason` 记「新增标准」。校验失败自动切到出错页签再描边定位。同轮修复既有缺陷：新增保存成功后 `router.replace` 被 `onBeforeRouteLeave`（`dirty=true` 时 `window.confirm`）拦截，导致创建成功却停在新增页且弹「放弃未保存内容」——改为创建成功后先同步 `original` 快照再跳转。

第二轮验证：`vue-tsc` exit0；`vitest src/views/quality` 12文件/53通过；真实 5173/8080 Playwright：三页签渲染正常、两页均无删除标准与底部条、1280 视口页面高度 1586px→900px；清空标准名称点保存→alert「请填写标准名称」且自动切到基本信息页签；编辑保存弹「确认保存标准」，`versionNo 5→6`，复原后 `7→8`（remark 还原为 null）；新增完整链路（填表→加产品→加检验项目→保存）创建 `E2E-TABS-540188` 并自动跳转详情页，测试数据已 DELETE 清理。截图：`artifacts/qc-spec-edit-review-20261102/v7-tab-basic|scope|items.png`、`v6-validation-locate.png`、`v6-created.png`。

第三轮（同日，检验项目移出页签）：按「检验项目是重要的，适用范围是次要的，适用范围可以做成页签，检验项目不需要」，页签缩为 **两项**（基本信息 / 适用范围），**检验项目改为页签区下方的常显卡片**，切换页签时始终可见；校验定位相应调整为「错误在页签内先切页签，错误在检验项目则保持当前页签只滚动过去」。

第三轮验证：`vue-tsc` exit0；`vitest src/views/quality` 12文件/53通过；真实 5173/8080 Playwright：`tabTexts=["基本信息","适用范围"]`、基本信息页签下 `itemsVisible=true`（`itemsTop=565 > tabsBottom=549`，1 行数据）、切到适用范围后 `itemsRows=1` 仍在；清空名称且停在适用范围点保存→alert「请填写标准名称」+ 自动回基本信息 + 卡片描红；移除全部检验项目保存→alert「至少需要一个检验项目」且不切页签、检验项目卡片滚入视口；编辑保存 `versionNo 8→9`，复原 `9→10`（remark 回 null、scopes `PRODUCT:KCL30`、items 1 条不变）；新增链路创建 `E2E-TABS-922638`（scopes `PRODUCT:HYTEA100`、items `E2E_ASSAY`）自动跳详情，DELETE 清理 200。截图：`v8-edit-basic.png`、`v8-edit-scope.png`、`v8-validation-basic2.png`、`v8-create-basic.png`；脚本 `verify-items-persist.mjs`、`verify-edit-save.mjs`、`verify-tabs-flow.mjs`。

范围限制：仅做前端交互重构，未加原型环节（用户已在对话中直接指定形态）；未跑后端 IT（未改后端）；未做 375 窄屏专项审查；验证期间对标准 521 产生 4 组真实维护原因记录（"验证适用范围维护交互（临时，验证后恢复）""恢复验证前的适用范围（验证收尾）""页签结构调整回归验证（稍后复原）""复原验证前的备注（验证收尾）"），业务数据已复原（`versionNo` 现为 10，`remark=null`，scopes/products 与原值一致），未删除审计痕迹。



## 2026-11-02 基础管理 UX 业务产品经理专项首批

**IN PROGRESS / PARTIAL FIX / G2原型待确切版本人验 / G5 NOT RUN**。用户确认全局 UX 按钮次序、T2 标题栏、子列表居中弹窗及原型同步。已新增 docs/development/decisions/MES_GLOBAL_UX_INTERACTION_20261102_APPROVED.md 并更新 AGENTS/强制开发规范/Global UI V2/T1–T6；通用 MasterList 行操作改为编辑→查看，通用 MasterDetail 和物料 T2 头部简化，供应商保存不再整体刷新未保存主表。正式前端 typecheck 和2套件12单测通过。物料弹窗仅在 mes-demo 独立原型中实现，build 通过，1440本机 Chrome 正向弹窗/供应商保存保留主表草稿通过，未做真实 API、全视口/UIUX清单或正式弹窗改造；其他基础管理逐页走查待续。完整证据与剩余任务见 [产品经理首批审查报告](docs/review/MASTERDATA_UX_PM_FIRST_PASS_20261102.md)。不授权违反原型设计锁直接改正式业务页面，未提交/推送/改DB/迁移/生产数据。

## 2026-11-03 物料编辑页两类子列表 G2 原型完善

**G2 CANDIDATE / SOURCE HASH PENDING HUMAN APPROVAL / FORMAL INTEGRATION NOT AUTHORIZED / G5 NOT RUN**。从正式 `MaterialConversions.vue`、`MaterialSuppliers.vue` 及 `supplierPayload` 读取真实合同，按用户全局 UX 决策另建完整子表交互的 mes-demo 原型（原单位/目标单位/因子，合格供应商/厂家/关系有效/首选/有效期/撤销历史）。独立居中新增/编辑弹窗、保存→取消、取消草稿确认；供应商首选原子切换与整份清单 PUT 语义保留。原型 `npm run build` exit0；本机 Chrome 定向验证新增、编辑、供应商首选切换、撤销留痕、主表草稿保留和四视口基本无 JS 错误通过。**尚未改正式两个子表**；原型只使用内存数据，没有正式 API/DB 操作，未完成权限/409/全量 UIUX/G5。确切文件 SHA、映射及放行限制见 [G2 原型候选记录](docs/development/decisions/MATERIAL_CHILD_LIST_MODAL_G2_20261103_DRAFT.md)。保持原有未提交工作不变，不建 worktree，不提交/推送。

## 2026-11-03 物料子列表正式移植 G3.5 开发前检查

**G35 PREFLIGHT ONLY / G2 EXACT-SOURCE APPROVAL MISSING / G4 BLOCKED / G5 NOT RUN**。用户提出继续将已批准弹窗交互集成入正式 `MaterialConversions.vue` 和 `MaterialSuppliers.vue`；重新实读强制规则、候选 G2 原型、原生业务接口与受影响正式组件，核实前次原型 SHA256 `4B25E76B653B57039EF821B44EC865AC3EFB37276C12B2532239821C43717E8C`；候选原型记录仍明确为待针对确切源码人验，未取得 DESIGN LOCK / G3.5 放行。已新增 [精确字段/API/源码映射与实现前检查](docs/development/decisions/MATERIAL_CHILD_LIST_MODAL_G35_PREFLIGHT_20261103_DRAFT.md)，保留原生整份供应商关系 PUT、首选唯一、厂家/日期/权限、单位换算双单位+因子与主表未保存数据保护，后端/DB/API不变。**未修改正式子列表源码、后端或数据库**，原任务仍未正式验收；获得对应确切原型版本人验与完整门禁后才进入 G4。保留本机其他未提交工作，不创建 worktree。

## 2026-11-03 物料主数据子列表弹窗正式集成

**G3.5 有界映射已核对 / G4 INTEGRATED / SCOPED FRONTEND & CHROME CHECKS PASS / FULL G5 NOT RUN / AWAITING FINAL USER ACCEPTANCE。** 用户针对确切 `MaterialChildListsUX.vue` SHA256 `4B25E76B653B57039EF821B44EC865AC3EFB37276C12B2532239821C43717E8C` 明确批准；已冻结 [DESIGN LOCK](docs/development/decisions/MATERIAL_CHILD_LIST_MODAL_DESIGN_LOCK_20261103.md)。正式只修改 `MaterialConversions.vue`、`MaterialSuppliers.vue`：新增/编辑居中弹窗、保存→取消、字段横排、取消保护；保留单位换算原 POST/PUT 和供应商整份关系清单 PUT/唯一首选/撤销历史，权限/后端 API/DB/迁移不变，父页未保存值隔离。Typecheck exit0、定向单测12/12、最终build exit0；本机 Chrome 正式5173页面+隔离API回归exit0，含新增/编辑/首选/撤销/409保值+新幂等键恢复、权限负例；四宽1440/1280/900/375文档无横向溢出、两个弹窗375px宽度修复后343px、左16/right359，截图保存。**这是隔离API形状回归，真实8080/MariaDB写入、完整九维UIUX/WCAG、系统G5均未执行，不得宣称ACCEPTED。** 结果及确切目标源码 SHA：[报告](docs/review/MATERIAL_CHILD_MODAL_FORMAL_INTEGRATION_20261103.md)、[截图/脚本](docs/review/evidence/material-child-modal-formal-20261103/)。其他未提交内容保留；没有Git提交/推送/部署、未建立worktree。

## 2026-11-03 物料子列表弹窗 G5 续验（真实后端事务与键盘）

**G5 IN PROGRESS / Scoped backend transactional & UI keyboard verification PASS / Full G5 NOT READY / NOT ACCEPTED.** 确认 5173/8080 存活、8080健康200与未认证受保护端点401；同版本 Maven 纯业务规则 exit0，`MaterialSupplierIT` 针对供应商首选/撤销、单位换算、幂等权限、厂家来源四项实际连接本机 MariaDB，Surefire 4/4通过、使用默认 @Transactional 回滚测试事务，Flyway V074/74迁移有效，未执行新迁移。原型保真正式两个子组件修复真实 Chrome 暴露的焦点进入、Esc取消与关闭后触发按钮焦点恢复；四宽 1440/1280/900/375 的新增弹窗字段同行、键盘与页面几何定向检查27观察通过，无JS错误。原7次形状接口 mock 写入场景全回归仍PASS、正式前端typecheck/build与12单测通过。**8080已认证真实写入/读取、全状态九维与19条WCAG、完整UI manifest/G5仍 NOT RUN，禁止标最终验收。** 报告：[MATERIAL_CHILD_MODAL_G5_PROGRESS_20261103](docs/review/MATERIAL_CHILD_MODAL_G5_PROGRESS_20261103.md)，浏览器记录：[G5 partial evidence](docs/review/evidence/material-child-modal-formal-20261103/g5-partial/)。保留所有并行未提交更改，不建worktree，不提交/推送，不修改后端/权限/API/DB业务记录。


## 2026-11-05 生产工艺表单优先改造 — Desktop Commander 本机第一轮

**G2 PARTIAL / TYPECHECK BLOCKED / BROWSER NOT RUN / NOT ACCEPTED。** 用户确认表单优先设计并要求 DC 本机开发，已修改现有 mes-demo 的 `src/features/processes/ProcessPrototype.vue`：默认展示工序表单，工序新增/查看/编辑改居中弹窗且隔离父数据草稿，简化表单列表并沿用现有表单/CPP设计器。源码SHA256 `78e52715372f273365055fdf8ab56479f921dac4f9835295cfa47f8ebdac8939`。`npx vite build` exit0（6423 modules）；完整 `vue-tsc -b` exit1，仅剩正式 `ProcessDetailView.vue:9` 的 `ProcessLookup` 未使用导入，本轮未改该正式文件或降低检查。完整三栏视觉、表单元数据弹窗、设计器进出恢复及浏览器/CPP真实动作验收尚未完成；不是按图完整交付或正式MES集成。历史累计81及旧证据保留，本轮补丁脚本/原型修正另记，未清零；普通UI执行窗口结束后仅收尾记录。无正式源码/DB/迁移/权限/Git提交推送。详见 [DC局部实施记录](docs/review/PROCESS_FORM_FOCUS_DC_20261105_PARTIAL.md)。


### 2026-11-05 生产工艺表单优先原型 DC 续作（仅 G2）
**G2 PARTIAL / VISUAL REWORK_REQUIRED / G3–G5 NOT READY。** 用户续作授权下，DC 仅修改本机 `mes-demo/src/features/processes/ProcessPrototype.vue`，新增草稿表单资料查看/编辑居中弹窗与工序返回定位，保留原生设计器/CPP和 eBR 受控生命周期。实际原型文件 SHA256 `9A611B9F3652079178B94256A0B76184C6973327625BDFFE37D1DD454C56327C`。Vite 6423 模块打包 exit0；完整 typecheck 因正式 `ProcessDetailView.vue:9` 的现存未使用 `ProcessLookup` 导入 exit1（本轮未碰正式文件）。本机 Chrome `5174` 原型 12 步定向操作 exit0/0 finding，含表单信息弹窗、设计入口、CPP页签和返回；不代表真实后端持久化/G5。1440 首屏表单列表位置过低、375 表格滚动导致表单身份裁切为 HIGH OPEN；未完成视觉验收，不声明按图完成。截图、操作及差异详见 `docs/review/PROCESS_FORM_FOCUS_DC_20261105_PARTIAL.md` 与 `mes-demo/process-form-browser-20261105/`。保留原有累计81返工事实，不清零；无正式代码、DB、迁移、Git 发布改动。


### 2026-11-05 生产工艺原型表单首屏与手机卡片修复（DC）
**G2 SCOPED LAYOUT PASS / G2 FULL ACCEPTANCE PENDING / G3–G5 NOT READY。** 用户继续开发本轮仅在 `mes-demo/src/features/processes/ProcessPrototype.vue` 修改视觉/响应式：移除重复横向工序路线，左工序导航保持业务前置关系，压缩基本信息/工作台，900px及以下把当前工序表单宽表改为完整身份与动作卡片，桌面表格列宽整理；本机源文件 SHA256 `3BE81FBF48262D94DEFA51E9B3D51448FF865FC6C926617554C394ACFD1B3E7D`。本机 Chrome 1440/1280/900/375 布局检查 exit0、errors=[]（1440 表单标题 y563、375 卡片完整显示且无页面横溢）；12步定向业务点击回归 exit0；Vite打包前一版布局 exit0 6423 modules，最终 TS 仅被**未修改**正式 `ProcessDetailView.vue:9` 的 `ProcessLookup` TS6133 阻断。没有正式G4集成、G5真实API/DB、CPP执行四动作验收或整页原型来源哈希人验；旧累计81和全部未提交数据保留。证据：`docs/review/PROCESS_FORM_FOCUS_DC_20261105_PARTIAL.md`、mes-demo `process-form-layout-evidence-20261105/`。普通UI十分钟窗口已到，停止新修改/重测。

### 2026-11-05 11:36–11:49 DC 生产工艺表单优先原型续作
**G2 PARTIAL / exact source hash pending human approval / G3.5–G5 NOT READY。** 用户要求继续开发；仅在本机 mes-demo 修改 ProcessPrototype.vue，最终SHA256 6F372077FC074DCCC1267E0D2E881E9ED97CE7341B98B5939F85D164B1705640。工序信息改为轻量“工序设置”弹窗入口；左侧导航取消误导性绿色配置勾号；右侧保持表单列表主体；参数/检验项目补齐查看只读弹窗，编辑取消保值；表单设计仍进入独立工作区，CPP继续复用现有设计器。桌面视觉层次作小幅整理。
Vite build 6423 modules PASS；原12步Chrome流程0 findings；参数弹窗专项4/4 PASS。npm run build/vue-tsc仍被未修改正式ProcessDetailView.vue:9的TS6133 ProcessLookup未使用阻断；本轮未修改正式MES文件。CPP当前数据结构仅rootLogic+groups[].conditions[]，不支持批准需求的嵌套条件组；四动作运行闭环未验收。浏览器仅为原型内存操作，不证明真实API/DB。截图与报告见 docs/review/PROCESS_FORM_FOCUS_DC_20261105_PARTIAL.md、mes-demo/process-form-browser-20261105/；Planning revision 72。
本轮普通UI 10分钟预算从11:36:17至11:49:56，超时3分39秒，已停止新代码/测试。历史累计81不清零；未修改正式代码/DB/迁移/权限，未提交推送合并。main原有大量未提交内容保留不动。当前确切原型hash尚未获用户G2人工批准，不得直接进入新版正式页面集成。

### 2026-11-05 12:24–12:40 CPP 嵌套条件组续作与开发预算变更
用户明确要求取消普通 UI 任务10分钟限制，改为30分钟。已正式修改 docs/development/MES_DEV_RULES.md §6A，普通UI墙钟预算改为30分钟，包含等待、修复、重测；到时仍停止新修改/新测试。20轮自动修复上限、同根因停止、DESIGN LOCK、验收门禁均保持不变。AGENTS.md通过引用MES_DEV_RULES继承此规则；已核查两份治理文件无残留的现行10分钟条款。历史记录中的10分钟事实不回写。
CPP嵌套条件组：
- 正式 EbrDefinitionRules 允许旧组结构和可选递归 groups，严格封闭字段、校验当前表单字段归属、AND/OR、非空节点、每组最多20条件、全树最多20组、最大嵌套深度4；从同一树生成括号化安全DSL。未新增表、列、API或迁移。
- EbrDefinitionRulesTest 新增嵌套AND/OR表达式匹配及空子组拒绝用例；Maven定向测试 PASS，8/8。
- CppRuleDesigner.vue 增加递归条件组UI、添加/删除子组、旧JSON归一化兼容、递归DSL/业务预览/有效性校验、深度/数量限制和标签关联；修正根组新增事件把DOM Event误当父组的问题。
- 新增 CppRuleDesigner.nested.test.ts；当前嵌套UI及原可访问性测试 2 files / 4 tests PASS。npx vue-tsc -b --pretty false 在最终源码上 exit0。
- Vite build 4179 modules PASS（1m17s），但发生在最后两处小幅标签/根组点击处理修正之前；最终版本尚未再次跑完整Vite build。最终版本的vue-tsc与4项定向Vitest均通过。
- Vitest输出仍有Ant Design Vue render-button default-slot性能警告，不影响本轮测试通过；尚未做真实5173/8080持久化、G5、四种超限动作全闭环运行验收。
源码SHA：CppRuleDesigner.vue 49AAF6302F5D3ED0DF464711704B0FEA0044727D68D739478041A39C104FD204；EbrDefinitionRules.java B104780E3F26FB3E998D3EA68F31179D16DFA939FAB784C86C5B235A68C642E2；EbrDefinitionRulesTest.java 4501018ADDF8ED972AFB52FBD704D1F53655B55A63E90A956A3C685BE0FC3018；CppRuleDesigner.nested.test.ts B521A8EB5FB66AE0C71F96972F39C1D42EA5D42D0FFC3A8E90A1EE19ECEEE7AC。
临时拼接脚本已清理。无DB/迁移/后端API变化，无Git stage/commit/push；main原有大量未提交修改保留。


### 2026-11-05 DC生产工艺表单优先续作：编译恢复与PC原型定向通过
**IN PROGRESS / G2 PARTIAL / G3.5–G5 NOT READY。** 用户明确持续授权；通过Desktop Commander在既有唯一工作区清理正式CppRuleDesigner.vue中先前未完成递归组辅助残留（7个TS6133）及正式ProcessDetailView.vue未使用ProcessLookup（1个TS6133），保留既有CPP两级组/DSL/旧JSON合同。正式前端vue-tsc -b exit0、Vite 4179模块构建exit0，原CPP无障碍及工艺草稿恢复两个Vitest文件通过。
既有mes-demo ProcessPrototype.vue继续G2表单优先布局；处理900px工序名称被挤成竖排及基本信息高度，工序卡220px横滚、900px基本信息双列、700px以下单列；T2标题“生产工艺 / 新增或编辑”。最终原型SHA256 c6ae0b1792bc6f805b0085232d9da083fb4586a94ae91b1fd9522507fbec5b4c；npm run build（vue-tsc+Vite 6423模块）exit0。Chromium内存原型12步主链+4项参数弹窗+4项CPP保存/返回/重开共20项通过；已读取1440及900前后真实截图，900原竖排问题得到纠正。原型CPP保存重开不能替代正式8080/DB持久化、CPP四超限动作运行G5。完整PC UI审查、原型确切版本人验、G3.5、正式页保真集成和G5仍未完成；SELF-REVIEW，不冒称ACCEPTED。
全程未修改DB、Flyway、已发布eBR、权限或GMP签名/快照；无Git提交/推送，新旧未提交文件全部保留。历史累计81及此前错误/修复事实不清零。详细5栏记录见 docs/review/PROCESS_FORM_FOCUS_DC_20261105_PARTIAL.md 新增同日章节。


### 2026-11-05 18:46–19:15 生产工艺 G2 完整审查推进及 G3.5 预检
**G2 IN PROGRESS / 审查校验 FAIL（证据未完成）/ EXACT HASH PENDING HUMAN APPROVAL / G3.5 DRAFT BLOCKED / G4–G5 NOT READY。** 依据已实际读取的AGENTS.md、MES_DEV_RULES §1/2A/4/6A、FINAL BASELINE v1.0.23、UI V2/T1–T6、PROCESS_WORKSPACE_COMPLETION_20261103_APPROVED、MES_GLOBAL_UX_INTERACTION_20261102_APPROVED及mes-ui-ux-audit/ui-ux-pro-max/BPM V2.5。PC范围1440/1280/900按PROCESS_PC_ONLY_SCOPE_20261104_APPROVED，手机375不计本功能G2。
通过Desktop Commander在既有本机 mes-demo 原型继续整改：1280表单列表“字段数”隐藏、DRAFT三操作换行；900 T1列表“配置状态/工序数”隐藏；新建无效工艺取消不返回列表。全部改动仅 ProcessPrototype.vue（正式MES业务未改），源文件最终SHA256 **738cb39c3d49674916ae29207cb8a17122e2ec07da8ac1d69ff7f7dfc06b8410**。
同版npm run build含Vue TS+Vite6423模块exit0，定向Chromium 1440/1280/900：15种区域状态×3共45截图，42项浏览器检查均PASS、0 pageerror/脚本错误，三个PC宽度document无整页溢出，四字段标签/控件中心偏差0px。真实读取多张原尺寸截图，修后表单身份/字段数/操作及900列表主要字段可见，弹窗过渡帧已改为稳定后截图。八组ui-ux-pro-max查询原文保留。完整九维/19条WCAG和逐图对抗二遍尚未核签：audit-manifest.json保留NOT RUN/open HIGH，验证器FAIL，不假报G2通过。
证据：D:/codex/_project/gmp/mes-demo/process-g2-evidence-20261105/，含audit-partial.json/additional-partial.json、45 PNG、knowledge八组、source-hashes.json、audit-manifest.json。待人工确认版本记录 docs/development/decisions/PROCESS_FORM_FOCUS_G2_20261105_PENDING.md；G3.5预检草案 docs/development/process-form-cpp-ebr/20261105-g35-preflight/PRE_HANDOFF.md 包含逐页源码复用/Mock→正式API映射/阻断项，明确DRAFT不授权G4。生产工艺“适用产品”和版本/生效批准日期的旧期望与当前冻结合同关系待一致性审查，不自行扩DB/API/审批。
未修改真实DB/Flyway/已发布eBR/签名/权限，没有新工作区或Git提交推送，保留main大量未提交变化和历史累计81不清零。下一步先完成剩余G2全维度验证/人验精确hash，再G3/G3.5冻结正式交接，随后忠实集成和5173/8080 G5；此前不得称ACCEPTED。


## 2026-11-05 19:56 G2 扩展验收阶段收尾（NOT FROZEN）
执行者：ChatGPT SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max。已实际读取本机 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、当前 PROCESS_FORM_FOCUS_G2_20261105_PENDING、G3.5预检、UI V2/T1–T6及PC范围批准记录。普通UI墙钟预算30分钟本轮已到，停止新业务源码变更和新测试，以下仅归档现有证据。
当前确切原型：D:/codex/_project/gmp/mes-demo/src/features/processes/ProcessPrototype.vue；源码SHA256 7ac72a79d52b1c1774de250d35d909bac118523f2213dcb8bf6557d1f708d29f。唯一实际业务源修改是原型查看表单信息的只读值文字对比度优化，保留只读属性和业务数据合同；原型Vue TS+Vite 6423模块构建 exit0，PC 1440/1280/900 真实Chromium已采45张原始分辨率状态截图，定向交互42/42 PASS，另36/36键盘焦点及错误保值PASS，三宽专业设计器固定全屏盖住底层业务页、无全局水平溢出。额外320/400/450/640 CSS窄视口的T1、T2、设计器共12格文档无横向溢出；Canvas独立+字段插入实操成功，字段3→4并在CPP页显示FIELD_4。
已完整逐张原图核对45张，给每张图填充8类观察共360条；对15个页面/弹窗状态填充9维审查共135条，视觉相关120项有观察和证据，未闭合的WCAG整维仍NOT RUN；八组ui-ux-pro-max知识检索和第二轮对抗审查有记录。重要文本配色抽检：正文12.92:1、次级5.74:1、代码4.76:1、状态5.44:1、只读业务值9.4:1；PC控件22px+图标属于WCAG2.2 2.5.8邻近目标间距例外，最小可命中目标中心距离28.4px。19个WCAG基本准则目前17项有专项结果，仍留 1.4.4（浏览器真实文字缩放）与1.4.11（完整非文本UI对比度）NOT RUN，未谎称完整认证。证据保存在 mes-demo/process-g2-evidence-20261105/：audit-manifest.json、45图、两个Chrome检查JSON、a11y-browser-checks.json、wcag-six-probe.json、reflow-small-css-viewport.json、pointer-spacing-minimum.json、field-add-keyboard-alternative.json、designer-viewport.json、source-hashes.json 和知识检索原文。
已实际运行仓库 validator：PASS: PROCESS-FORM-G2-20261105-PENDING — IN PROGRESS; screenshots=45 coverage=45。**这是IN PROGRESS证据结构校验通过，不是G2整体验收通过。** 剩余长CODE、服务失败/409、权限变体等完整状态闭环、用户同hash人工批准和合同差异确认（适用产品、工艺版本/生效批准日期）未完成。G2 IN PROGRESS / PENDING HUMAN APPROVAL；G3 未冻结，G3.5 DRAFT/BLOCKED，G4/G5 NOT READY。没有更改正式MES功能/数据库/Flyway/权限/电子签名/历史快照，没有提交推送合并，保留历史累计81及工作区所有未提交改动。此记录只结束本轮30分钟窗口，不清零历史返工数。


## 2026-11-05 21:26–21:56 DC G2 正式缩放/非文本对比度专项收尾
本轮为新30分钟普通UI窗口。再次实际读取本机AGENTS.md、MES_DEV_RULES、MES_TASKS、PROJECT_BASELINE、已批准UI V2 §6、现有G2待批确认记录、G3.5预检、mes-ui-ux-audit以及原型AGENTS和实际源码；保留FINAL BASELINE v1.0.23/G1–G5、历史累计81和唯一本机工作区，用户授权继续不等于人工批准确切原型。
**原型文件当前最终SHA256：f9a1492393fe1136f7bb8ef8e56c4d83865b28ac26b8348cba0fa605976ded24**（D:/codex/_project/gmp/mes-demo/src/features/processes/ProcessPrototype.vue）。本轮仅在此原型追加UI V2非文本边界中性 token #7d8a9b（输入、选择、单复选、Canvas字段插入按钮）及浏览器字体放大兼容的控件高度、工序导航弹性换行、弹窗标签flex宽度、长工序条件区域内部滚动样式。未改变工艺/CPP业务字段、API/DSL、表/迁移/权限/签名或Mock回写边界。
**实际验证**：本轮源码npm run build含Vue TS+Vite6423模块 exit0；正式G2既有42项三PC宽浏览器操作、36项键盘焦点/错误保值专项，以及12项旧主链、4项普通弹窗、4项CPP草稿保存-返回-重开，全链连续运行 exit0（closing-main/additional/a11y/original/modal/cpp.log），保留所有失败历史。非文本边界修复前普通控件1.41:1、字段加号1.74:1；修复后1440/1280/900共18个操作状态117个可见控件边界实测最低3.51:1、低于3:1为0。浏览器隔离原生最小字体minimum_font_size=28px（常规body14px→200%）下发现原900工序竖排/保存按钮裁切并做受控修复，修复后1440/1280/900共18项关键操作、弹窗、全页设计器/CPP、工序不竖排、布局不全局横溢出均PASS；另等效浏览器200%缩放(半CSS视口+DPR2)15/15通过。原始前后PNG和JSON：mes-demo/process-g2-evidence-20261105/native-font28-regression.json、wcag-1411-all-controls.json、zoom200-equivalent-reflow.json、closing-*.log及对应截图。限定为G2原型证据，不代表全站WCAG认证/真实5173/8080G5。
长名称专项：工序长名称在1440/1280/900 3/3保值且没有整页横溢出，停用工艺查看/禁编辑及重复工序编码拒绝等另9项成功；“先改父工序→未经父保存直接改表单资料”在三视口被原型正确保护并提示“请先保存工艺，再编辑表单信息”，该路径不该绕过；**父工艺保存后再次编辑长表单名仍NOT RUN**，不可据失败驱动脚本宣称它已通过。尝试调整针对该测试的定位/重试时工具安全检查拒绝，未换执行方式规避。无真实HTTP 409/403与不同角色RBAC执行证据。
G2审查manifest现45张截图、逐图360条视觉观察、15种surface九维135条评审已登记；19项WCAG基础范围均有定向实测PASS，validator实际输出“PASS for IN PROGRESS; screenshots=45 coverage=45”。**G2仍IN PROGRESS/未获用户同hash人工批准，剩余长名称父保存→表单编辑回读、真实服务错误/权限负例及源设计合同差异确认；G3未冻结、G3.5仍DRAFT/BLOCKED，G4/G5不可放行**。源hash清单与G2/G3.5候选文档已更新本轮f9a...，旧版历史证据保留。未修改正式MES业务文件/数据库/Flyway，未Git提交、推送、合并。


## 2026-11-05 22:07 DC G2 长名称/父草稿保护回归补齐（30分钟窗口到期收尾）
用户要求“继续30分钟任务”。本轮沿用本机单一mes-demo与计划0df9356d-675b-4f37-8b7d-948948c8d694，实际重新读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、现有G2待批/G3.5预检、批准的全局UX交互规范及当前ProcessPrototype.vue、原型回归脚本和audit-manifest.json。当前原型SHA256复核 **f9a1492393fe1136f7bb8ef8e56c4d83865b28ac26b8348cba0fa605976ded24**，业务源码本轮零改动，正式MES/DB/Flyway/权限/受控快照零改动。
本轮新增原型真实Chromium专项脚本 mes-demo/process_g2_parent_long_roundtrip_20261105.cjs；在PC 1440/1280/900的完整链路：编辑OP03长名称→未经父工艺保存点击表单编辑出现预期“请先保存工艺，再编辑表单信息”→保存父工艺并切到只读详情→再次进入编辑→修改同工序草稿表单的长名称→列表显示修改后名称→返回查看再重开编辑→对应表单行与只读查看弹窗完整名称一致。最终 **18/18 PASS、errors=[]、6张专项截图**；证据 mes-demo/process-g2-evidence-20261105/parent-save-long-form-roundtrip.json、g2-parent-form-long-*.png。初版专项脚本失败属于隐藏 Ant 表格行定位与底部取消按钮非唯一定位，修正为对应视口可见表单行与bottom-actions作用域后全部通过；保留前失败记录，不改业务断言、不降低阈值。原型自身保存结果仅内存，不等于8080真实持久化。
审查manifest中的“长表单保存后回读NOT RUN”已经按本轮证据纠正，**G2仍IN PROGRESS**：正式HTTP409/403和不同角色权限负例没有专项证据，用户尚未对同哈希原型人工签核，G3详细设计与G3.5交接仍DRAFT/BLOCKED，G4/G5 NOT READY。历史修复累计81及新旧失败/状态均保留。
执行窗口由本轮开始约21:26至实际记录约22:07，已超30分钟上限，已停止新增业务源码/测试，只允许补证据与交接记录。无Git stage/commit/push、无新worktree、未动其它未提交文件。


## 2026-11-05 22:52 DC G2 原型409/403与VIEWER权限状态补齐（30分钟窗口）
执行者：ChatGPT SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max。沿用唯一 MES任务、v1.0.23与 UI V2，不新建工作区；真实读取 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、已批准CPP及MES_GLOBAL_UX_INTERACTION_20261102、G2待批记录、G3.5草稿、正式 ProcessDetailView.vue/model.ts 和相关现有测试。代码白名单只有 mes-demo/src/features/processes/ProcessPrototype.vue 以及新增G2浏览器脚本；正式MES业务源/DB/迁移/角色/签名/历史记录未修改。
**当前候选原型 SHA256：2bbbacd1b4c62be2fc31d3ad3f28fad6b36522ccf3e2ce0fc6ae90f44ec2b82b**。在仅 DEV 原型中通过URL参数 `__g2SaveStatus=409|403` 注入可控保存失败，`__g2Role=VIEWER` 表示只读人物；无页面新增业务按钮、权限定义、API/存储结构变更，**G4不可直接把这两个专用夹具迁移正式应用**。正式必须复用 auth.can()、Axios错误、真实REST、幂等及冲突重载保护。
G2 Chrome实际验证：409/403×1440/1280/900共6组、0 errors，409输入保留/显示错误/模拟冲突解除后保存重读，403无虚假成功/未丢输入；VIEWER三PC宽21个状态检查（3×7）PASS，新增编辑入口隐藏、只读查看、工序弹窗禁用和草稿表单仅查看。两组专项分别有6及3张稳定截图。最初VIEWER图拍在弹窗关闭动画中，已保留误帧发现并修正脚本，等待稳定后重拍；只读业务值computed为#314364，对比度不靠渐变截图判断。npm run build (TS+Vite6423模块) PASS；重新连续执行G2 42操作、36键盘、12主链+4弹窗+4CPP及18长名回读，均成功退出0；正式前端已存在的ProcessDetailView只读/草稿恢复及CppRuleDesigner无障碍三Vitest文件6/6PASS，但仍有原有 maxlength prop type警告，不是G5证据。
G2证据目录：D:/codex/_project/gmp/mes-demo/process-g2-evidence-20261105/，追加 prototype-fault-409-403.json、prototype-viewer-role.json、9张稳定截图、g2-next-*.log 及原45图/关联JSON；审查manifest现 **54张截图/54覆盖单元/162九维条目/51交互条目**，validator真实返回 PASS for IN PROGRESS (54/54)；G2-UNCLOSED-EXCEPTION-RBAC 仅就 **原型模拟UI范围** 有据关闭，真实HTTP409/403/实际角色权限仍归G5的独立验收，绝非后端PASS。G2-VERIFIED-HUMAN-HASH仍OPEN HIGH，故G2整体IN PROGRESS/PENDING HUMAN APPROVAL，G3未冻结，G3.5仅PRE_HANDOFF草案/DRAFT BLOCKED，G4/G5 NOT READY。
G3.5预映射：工艺GET/POST/PUT复用ProcessDetailView.vue中load/createPackage/save/failure/reloadConfirmed，403由auth.can与服务端权限共同限制，409由conflict置位/重载前confirm保值，实际JWT、对象归属与current-definition原子提交不改变；eBR模板GET分页/草稿PUT保留templateId+formCode身份与版本/If-Match。复用OperationItemDialog、DefinitionDesigner与CppRuleDesigner，禁止用原型内存保存结果代替正式持久化。产品“适用产品”与版本/生效批准日期的合同差异及表单名称正式更新语义仍需G3决策，未经用户批准不扩库/审批。
未Git stage/commit/push，没有清理其它未提交文件；此前累计修复81及本阶段事实保留，不清零。


### 2026-11-05 22:56 新30分钟G2任务最终检查与Planning容量处理
本轮当前源码SHA256于22:56再次核实为 2bbbacd1b4c62be2fc31d3ad3f28fad6b36522ccf3e2ce0fc6ae90f44ec2b82b，源闭包13/13匹配，G2 manifest审查validator PASS for IN PROGRESS（54截图/54覆盖、162九维、51交互条目），Git diff --check PASS。正式5173、8080 health HTTP200仅为只读环境预检；正式系统真实409/403/RBAC及持久化仍NOT RUN/G5。G3.5 PRE_HANDOFF追加了正式 TemplateDetailView.vue save()/load()/newVersion()的接口源码复用核验：PUT /ebr/templates/{id}带versionNo、reason、definitionForSave，草稿formName更名可使用现有端点，无需增API；但确切交互合同和多表单版本/审批影响仍须G3冻结决定。无正式MES业务代码、数据库、Flyway、权限或GMP历史改动，无Git提交推送合并。
Planning唯一ID 0df9356d-675b-4f37-8b7d-948948c8d694 的 progress.md 因UTF-8 64KiB限制无法再append；本轮没有新建/重置Planning，改在同计划的task_plan.md与findings.md简要checkpoint并成功更新为 revision79；完整新过程事实继续以本文件MES_TASKS.md与G2审查报告为权威。保留progress.md历史及累计返工81，不把容量错误当丢失进度。
当前门禁：G2 IN PROGRESS（唯一剩余HIGH为用户对当前具体源码hash的人工确认），G3/G3.5 DRAFT/BLOCKED，G4/G5 NOT READY。DEV测试夹具不可迁入正式应用。当前30分钟窗口约22:29开始，22:56已停止新增源码及测试，仅做归档。


### 2026-11-06 生产工艺「检验作为普通工序」原型验证
用户明确要求从生产工艺整体移除独立「检验项目」概念；检验仅作为工序流程中的普通工序，不特殊分类、不特殊着色、不设独立项目表或管理入口。沿用新决策记录 PROCESS_INSPECTION_AS_ORDINARY_OPERATION_20261106.md；旧 DCP-PROCESS-OPERATION-TYPE-001 保留为历史批准记录，不回写。
当前 mes-demo 原型 ProcessPrototype.vue SHA256 `9802E4CF5F4DFBD01DACDBB411E392F7F0A60B6A0EECCA54FC897CC1E928E7BA`。当前 Operation 不含 operationType/ipcDefinitions/IPC参数类型；左侧七道工序同样式，OP04「中间检验」仅作为普通工序；关联普通 EBR 表单「中间检验记录」，表单设计和 CPP 走统一入口。工序内仅「记录表单」「工艺参数」，不显示独立检验项目。
验证：npm.cmd run build PASS（vue-tsc PASS、Vite 6431 modules）；普通工序/中间检验/CPP/参数/产品专项 13+10+7+5+3 步均 PASS、0 errors，覆盖1440/1280/900无整页横溢出、普通工序新增/编辑/查看只读、取消恢复、产品多选取消/保存/重开/只读。截图和脚本见 mes-demo/process-inspection-normalization-20261106/、process-inspection-ordinary-*.png、process-inspection-ordinary-operation-smoke-20261106.cjs、process-operation-crud-smoke-20261106.cjs、process_inspection_normalization_smoke_20261106.cjs、process_product_scope_smoke_20261106.cjs。
当前仅为原型专项验证，G2完整UI/UX审查及用户对精确hash人工确认仍待完成；G3/G3.5未冻结，正式集成未放行。正式 operationType/ipcDefinitions/数据库/API/历史快照/生产执行和QC/QMS仍保持兼容，本轮未改正式MES页面、后端、API、DB、Flyway、权限、签名、历史数据；未Git提交/推送。


### 2026-11-06 生产工艺开发恢复核对（ChatGPT/DC）
本轮恢复后重新实读本机 AGENTS.md、MES_DEV_RULES §2A/§4/§5/§6A、PROJECT_BASELINE、MES_TASKS、最新普通工序决策、mes-demo/AGENTS.md、mes-ui-ux-audit 与 UI V2。当前原型 ProcessPrototype.vue SHA256：`9802E4CF5F4DFBD01DACDBB411E392F7F0A60B6A0EECCA54FC897CC1E928E7BA`。已有普通工序/表单/CPP/参数/产品专项 13 步 errors=[]；仍仅是原型证据，G2 完整 UI/UX 审查和该哈希人工确认未完成，G3/G3.5未冻结，正式集成不得放行。
只读核对发现：正式 ProcessDetailView 仍保留 IPC 工序类型及 IPC 专属区域；正式 ProcessService/ProcessCommands/ProcessStore 仍以单一 productId/product_id 关联产品。当前原型已改为工艺适用多产品，但 currentDefinitionPayload() 尚未包含 selectedProductIds，正式关系模型/API 映射需在 G3/G3.5明确。原型 mock Process 有 version/approvedAt 属性但编辑界面未展示；正式基础定义合同不含工艺版本生命周期，简单版本/生效日期元数据是否保留需在 G3明确，不得擅自恢复版本/审批流程。
视觉证据复核：已查看1440/1280/900主界面及产品页。普通工序查看弹窗截图脚本在 dialog.waitFor() 后立即截图、未等待过渡动画，现有 process-inspection-ordinary-view-1440.png 不可作为稳定视觉证据；900产品截图含“当前工艺已保存”瞬态提示，需稳定重拍。900工序导航使用 overflow:auto 横向滚动，完整审查需验证末端工序鼠标/键盘可发现性。
本轮无业务源码、API、数据库、Flyway、权限或测试断言修改。Git仍为唯一 main，HEAD 1682fc5，ahead 1；工作区547条状态项（108 modified、439 untracked），全部保留，未 stage/commit/push。下一步为当前哈希的完整G2审查与稳定证据补齐；完成后提交精确哈希供用户确认，再进入G3/G3.5。


#### 2026-11-06 G2 稳定截图补证
新增 `mes-demo/process-inspection-stable-evidence-20261106.cjs` 并实际运行，结果见 `mes-demo/process-inspection-stable-evidence-20261106/result.json`，绑定原型 SHA256 `9802E4CF5F4DFBD01DACDBB411E392F7F0A60B6A0EECCA54FC897CC1E928E7BA`。稳定截图为 `operation-view-stable-1440.png` 与 `product-scope-stable-900.png`。查看工序弹窗实测白色不透明背景、780×402；900产品页 documentWidth=900、无瞬态toast；900工序导航7项，键盘聚焦末项后自动滚动到OP07且完整可见；errors=[]。因此旧查看弹窗过渡帧和900成功toast截图不再作为视觉证据。此补证仅关闭截图稳定性与键盘到达末工序这两个局部问题，完整G2逐图/九维/WCAG/对抗审查仍未完成，G2仍IN PROGRESS。


#### 2026-11-06 UI/UX Pro Max 定向检索
按当前 MES V2/PC_ONLY 范围执行8组本地检索（enterprise admin/style、horizontal scroll/ux、form labels/ux、table row/ux、modal/ux、focus/ux、contrast/ux、forms/vue），全部 exitCode=0；结果保存在 `mes-demo/process-inspection-stable-evidence-20261106/knowledge/`。检索仅作为G2审查参考，不覆盖冻结 MES UI V2/T1–T6 token；重点核验可见焦点、表单标签/错误反馈、正文4.5:1对比度、窄视口表格卡片化、弹窗焦点。horizontal-scroll规则针对移动端；当前批准范围PC_ONLY，900px工序导航仍需检查指针发现性与键盘可达性（键盘末项已通过）。


#### 2026-11-06 当前哈希 G2 部分清单
新建 `mes-demo/process-inspection-stable-evidence-20261106/audit-manifest.json`，绑定当前原型哈希 `9802E4CF5F4DFBD01DACDBB411E392F7F0A60B6A0EECCA54FC897CC1E928E7BA`；Node JSON解析验证通过。当前登记8组知识检索、2张稳定截图、4项定向交互检查、9个审查维度、19项WCAG准则。为防止误报，9个完整维度和19项WCAG均明确标记NOT RUN，manifest verdict=IN PROGRESS；旧 `2bb.../f9a...` 哈希截图不复用为当前哈希的通过证据。


## 2026-11-06 用户要求直接修改正式 MES 代码 — 正式实施开始
用户明确指令：“继续开发到完成，对着正式代码修改”。已建立 [PROCESS_FORMAL_DIRECT_IMPLEMENTATION_20261106.md](docs/development/decisions/PROCESS_FORMAL_DIRECT_IMPLEMENTATION_20261106.md)。本次为生产工艺模块任务级直接实施授权：不再以 mes-demo 精确哈希 G2 人工签核作为正式代码修改前置条件；**不代表 G2 已通过或任何原型哈希获批**，G5真实验收和用户最终接受保留。
业务边界：一个工艺可关联多个产品；工艺基本信息包含名称、当前版本标签、生效批准日期、说明；版本/日期是当前元数据，不新增工艺历史版本或审批生命周期。工序统一普通工序，隐藏IPC专属UI但保留历史operationType/ipcDefinitions兼容数据。每道工序复用现有EBR表单和CPP设计器/安全DSL，不另建引擎。处方与生产下达必须校验产品在工艺适用范围内。
本轮开始保护唯一main与所有现有未提交改动。当前 HEAD 1682fc5、本地ahead 1；全工作区547条状态（108 modified、439 untracked），仅按决策白名单做精确补丁，不清理/覆盖/提交/推送。


## 2026-11-06 正式代码实现与实测补充

正式实施已落地：V076 已在 `hospital_pharma_mes_dev` 成功迁移、76 条 Flyway 已验证；多产品关系的创建、保存、查询与历史单产品兼容均已通过事务集成测试；生产处方和批次下达适用性检查、当前工艺资料（名称/版本/生效批准日期/说明）、工序与 EBR 表单/CPP 入口已进入正式 Java/Vue 代码。

最终定向验收：Maven 20 模块 BUILD SUCCESS；前端 TypeScript + Vite 构建通过；全量前端 Vitest 249/249 PASS（111 suites）；定向 Java 24/24 PASS（ProcessOperationType 7、Prescription 3、CPP 1、Ebr 13）；真实 Chrome 生产工艺列表/详情/适用产品及工序表单操作 1 PASS。

**G5 非全通过**：Chrome CPP 表单直达用例 1 SKIP，因为本机 9 条工艺均未找到已绑定的现有 EBR 表单。不制造、篡改用户业务表单数据；由真实 MariaDB 事务集成 CppControlledRoundTripIT 证明 CPP 写入/回读。完整证据：`docs/development/process-form-cpp-ebr/20261106-formal-code/RESULT.md`。

未创建 worktree；单一 main；未 stage/commit/push；之前大量未提交源码保持原样。此轮任务级直接开发授权不等于原型 G2 已批准或 G5 产品已最终接受。


### 2026-11-06 20:13：生产工艺/CPP 真实 Chrome 端到端闭环
已在正式 DEV 库新增工艺829、工序OP1、EBR模板663、字段MIX_TEMP及CPP规则CPP_OP1_RECORD_1（配制温度20–25，MARK_EXCEPTION+BLOCK_SUBMIT），通过正式 UI 新增/关联/设计/保存及真实 HTTP GET 回读，实际 PUT HTTP 200。原 1 PASS/1 SKIP 的 Chrome 专项现为 **2 PASS/0 SKIP/0 FAIL**，已解决无表单造成的 CPP 验收空缺。详情与哈希证据：`docs/development/process-form-cpp-ebr/20261106-formal-code/RESULT.md`。本轮只在 DEV 留下业务语义明确的隔离核验记录，未改既有业务记录，仍为唯一main/未提交推送。G5 完整审查清单、用户最终 ACCEPTED 尚未完成，状态保留 IN PROGRESS/G5 PARTIAL。


### 2026-11-06 21:32 G5 正式界面实审与产品弹窗修复
唯一main正式代码修复适用产品弹窗确认→取消按钮顺序及900下拉遮挡footer（ProcessDetailView.vue SHA256 DF080891D4289BB939B5C500F6968F8186D00A3B9DF39E10C40F278F7B2C263C）。真实Chrome新增/取消/确认/保存KCL30产品关联后GET读回，正式Chrome 3/3 PASS、0 SKIP；全前端Vitest249/249 PASS，前端build通过。G5真实5173/8080 PC1440/1280/900共30截图、0整页横溢出、0业务API非401错误、0 pageerrors；8组知识检索exit0。g5-readonly/audit-manifest.json记录54九维、19WCAG未完，validator EXIT1 FAIL：正式路由PC_ONLY批准例外未在校验器范围、375矩阵缺项及逐图复评/权限/缩放未完成。不得称G5通过/READY/ACCEPTED。详见`docs/development/process-form-cpp-ebr/20261106-formal-code/RESULT.md`。未 stage/commit/push、不建worktree、不动原有业务记录。


## 2026-11-06 22:04 G5 阶段续作
正式 ProcessDetailView.vue 修复：重复工艺编码409不再触发伪版本冲突消息；工序保存栏position:static，避免覆盖表单行。受控源hash 252A1EE2FA90E1A03FFF2F37F2331EB01EC694ED7B07811F51B57287A0D70256。Chrome真实角色403+重复编码409 2PASS；生产工艺/EBR/CPP/产品关联/底栏6PASS；PC 200%等效3物理宽、共9页1PASS；vue-tsc+Vite BUILD PASS、全前端Vitest249/249PASS（111 files）。G5后照30张逐张八类review、九维54个条目与WCAG19项部分NOTRUN；validator EXIT1：PC_ONLY仅适配原型而不认正式路由、强制375，另完整对抗/全RBAC/原生缩放待审。G5 IN PROGRESS，不能称最终READY或ACCEPTED。详细报告：docs/development/process-form-cpp-ebr/20261106-formal-code/RESULT.md，截图和manifest：.../g5-footer-after。无Git提交推送、无新worktree、原有未提交文件仍全数保留。


### 2026-11-06 23:17 当前正式源码的键盘与Canvas草稿修复
正式单一 main/HEAD1682fc5：对DEV草稿EBR模板663通过受控PUT/GET把MIX_TEMP字段移到文书正文、保留CPP_OP1_RECORD_1（versionNo3、HTTP200），三宽1440/1280/900原图已查看，原Canvas MEDIUM关闭。正式ProcessDetailView.vue新增工艺/产品tab左/右/Home/End键及产品弹窗开闭焦点恢复，源码SHA E2B9E82F0650848B7F7D894E39CC0A6F8EB2040C1822C7083D7E6B4D6DCDC937。当前定向真实Chrome **10/10 PASS**、全前端Vitest **249/249 PASS**、TS/Vite BUILD PASS、diff--check EXIT0；重采当前源码PC 30图、0整页横溢出/0业务API错误/0pageerror。已批准工艺当前定义是最后合法保存生效，不应错误添加旧版本409门禁。审查器正式PC_ONLY路由范围冲突及19项WCAG全矩阵/原生缩放/全角色对抗复查仍未完成，G5 validator EXIT1 **IN PROGRESS/NOT READY**。详情 `docs/development/process-form-cpp-ebr/20261106-formal-code/RESULT.md` 及 `g5-current-20261106/audit-manifest.json`。本地工作区原未提交内容保护、未另建工作区、未git提交推送。


### 2026-11-07 08:17–08:38 G5正式列表与产品弹窗二项可用性整改
在本机唯一main正式代码，对生产工艺列表长名称穿越版本列问题修复：原170px名称td scrollWidth207，正式1440/1280/900 Chrome重新测得 scrollWidth=clientWidth=170，完整名称换行可见。正式适用产品多选弹窗添加可见同行标签、input aria-labelledby及局部v-select-accessibility，Chrome AX tree combobox名称稳定“适用产品”、展开状态false→true；无业务逻辑变更。最新完整真实Chrome 12/12 PASS，Vitest249/249 PASS（111 files），前端TS/Vite构建PASS，17项代表性颜色对比度均≥4.5:1（仅抽查）；1440/1280/900共30张当前源码原图无整页横溢出，当前 G5完整manifest在docs/development/process-form-cpp-ebr/20261107-formal-g5/final/audit-manifest.json。ProcessDetailView SHA D755132FA34DB1B34E15328C29FB7CE0081FF9DC1E86213EDF99271F8385DA24，ProcessListView SHA 5989133C54603D6D8041F65E12291585B89F401DAC29AADF2EB6F08949D6617A。G5 validator 真实EXIT1：PC_ONLY批准例外仅认原型路径、375依旧被强制，19项WCAG/全角色/对抗二审等未完成；未改门禁，G5 NOT READY/NOT ACCEPTED。完整报告 docs/development/process-form-cpp-ebr/20261107-formal-g5/RESULT.md。唯一main未stage/commit/push，不动历史生产记录、保留所有未提交文件。


### 2026-11-07 10:05–10:33 正式生产工艺G5专项继续
本机唯一main/HEAD1682fc5继续正式代码（无其它工作区、未Git提交推送、未清理原有未提交内容）。补充真实IAM GET 用户13/角色15中7类业务身份与工艺权限的只读配置核对：生产岗位只有process:package:view，其余六类岗位无工艺授权；这**不是七身份各自登录**。真实仓管403和重复工艺编码409输入保留已通过。PC1440/1280/900工艺和产品弹窗键盘Left/Right/Home/End、8次Tab焦点留在弹窗、Esc返回来源按钮、2px蓝色焦点轮廓通过。T1列表结果数量与T2工艺状态增role=status、aria-live=polite（是DOM/Chrome断言，不是假称真人读屏器播报）。正式ProcessDetailView的12处AntD maxlength字符串prop告警改数字绑定，长度阈值不变，定向Vitest通过。CPP目标尺寸扫描及WCAG专项证据已记（仍非19条全面PASS）。最终源码Detail hash E61FAF54F9F0540E0F10B03A81B67DE833806ADFDB41112F242A0D0FD48B28AE、List hash ECE7535B816B8CEA25DD4A8A48DCDB0C7910EA5D6E7DEE061C081548A8D80F47；按此源重新采PC三宽×10状态30张PNG，0整页水平溢出/0业务API错误/0pageerror。最后**完整Chrome13/13 PASS（2 workers）、完整Vitest249/249 PASS（111files，独立运行）、TS+Vite build PASS**；高并行时曾有一个Vitest5秒timeout和一个Chrome CPP5秒等待timeout，原断言未修改，分别单独重跑及2-worker全量复测通过。正式G5审查器原样EXIT1，PC_ONLY只认原型#/processes、不认正式/process/packages，同时完整19WCAG/七角色逐一登录/原生缩放及人工签核未完，绝不标READY/ACCEPTED。详细报告和提案：docs/development/process-form-cpp-ebr/20261107-formal-g5/RESULT.md、WCAG_TARGETED_EVIDENCE.md、G5_SCOPE_FIX_PROPOSAL.md、source-final/audit-manifest.json。


### 2026-11-07 12:35 G5 七身份真实Chrome与对抗二次自审
本机唯一main/HEAD1682fc5，真实正式UI/后端：新增七个现存业务身份各自真实登录、GET工艺829、直接页面权限E2E，生产岗200只读且无编辑，仓储/取样/QC分析/QC复核/QA/基础资料6岗403且页面阻止，1/1专项PASS，原“仅IAM授权配置核对”缺项关闭。整体真实Chrome 14/14 PASS（2 workers），原全前端249/249 PASS、TS/Vite BUILD PASS、Java定向24/24事实保持。正式源码SHA未变：Detail E61FAF54F9F0540E0F10B03A81B67DE833806ADFDB41112F242A0D0FD48B28AE、List ECE7535B816B8CEA25DD4A8A48DCDB0C7910EA5D6E7DEE061C081548A8D80F47。对当前源码30张PC1440/1280/900原图逐图进行了第二遍ChatGPT SELF-REVIEW（非独立QA），对齐/留白/层级/动作可发现性/恢复均有具体原缺陷修复对照，未发现新增HIGH。已更新 `docs/development/process-form-cpp-ebr/20261107-formal-g5/source-final/audit-manifest.json`：7身份actual sessions PASS、对抗自审completed=true，保留19WCAG全量、原生浏览器缩放NOT RUN。无头Chrome Ctrl+=不改变真实zoom尺度，不能替代native验证。G5 validator仍EXIT1：PC_ONLY例外不认正式route、强制375和native zoom/geometry缺项；不得伪造PASS。详见 `docs/development/process-form-cpp-ebr/20261107-formal-g5/RESULT.md`、`SECOND_PASS_20261107.md`、`role-login-e2e-evidence.json`。未提交/推送/另建工作区或覆盖原改动。


### 2026-11-07 12:38 正式页面辅助技术名称定向扫描
当前正式源码7页面/弹窗通过真实Chrome AX snapshot扫描，158项交互控制无未命名role项，原始证据`docs/development/process-form-cpp-ebr/20261107-formal-g5/aria-comprehensive-probe.json`。只证明可访问名称定向覆盖，不替代WCAG19条全矩阵或原生缩放。真实Chrome14/14、7身份各自登入、30图第二遍自审已通过；validator EXIT1，G5仍IN PROGRESS/NOT ACCEPTED。无源码、权限或数据库变更。



### 2026-11-07 14:42 原生Chromium浏览器200%缩放实际验证完成
本机Playwright Chromium1248通过独立临时MV3扩展调用chrome.tabs.setZoom(2)，浏览器真实getZoom从1→2，DPR1→2，PC1440/1280/900CSS可用宽720/640/450。按真实正式5173/8080/MariaDB验证工艺列表/详情/编辑/EBR CPP/Canvas/产品Modal三宽×六状态**18/18专项PASS**，关键控件DOM边界不出视口、0整页横溢出/0业务API错误/0JS错误，底栏正常、弹窗Escape焦点回归。Playwright page.screenshot的原生缩放截图存在CSS宽裁成半页的工具问题，已采用CDP Page.captureScreenshot生成**18张设备物理像素完整截图**并复核900列表/编辑/CPP/弹窗，原裁图仅保留作误诊追溯，不能列为有效视觉证据。G5 source-final/audit-manifest.json更新native zoom交互/几何PASS，实际屏幕阅读器播报保持NOT RUN。PC_ONLY路由校验器未改，8组正负验证scope-mapping-proof.py均PASS（仅精确正式路由可继承用户批准的三宽，历史其它路径/文件哈希拒绝仍有效），原validator依旧EXIT1：正式路由范围未映射/强制375+读屏实际播报未验；完整19WCAG/人工ACCEPTED尚未结案。正式源码/单一main/HEAD1682fc5不变，所有原未提交文件保护、无worktree/commit/push。证据docs/development/process-form-cpp-ebr/20261107-formal-g5/native-zoom-investigation/native-zoom-final-evidence.json和scope-mapping-proof.json；详情追加RESULT.md。


### 2026-11-07 18:34 已批准的生产工艺 PC_ONLY 正式路由修复
实际在本机唯一main精确修复G5审核器的正式工艺URL映射：在用户原批准PC_ONLY文档及严格SHA检验下，仅允许本任务的正式 `/process/packages` 根URL（本机127/localhost:5173）和原 `#/processes` 使用1440/1280/900矩阵，所有其它模块/子路由/域/错误SHA仍要求原375，9/9组正负回归PASS（docs/development/process-form-cpp-ebr/20261107-formal-g5/scope-mapping-regression-result.json）。原 G5 validator如今不报范围/375错误，但仍因`interaction_checks[14]`真实读屏器语音NOT RUN而EXIT1。新增正式Chrome无障碍树动态状态专项：工艺编辑状态“配置已生效→有未保存的修改”、列表检索“共9→共1”通过role=status/live=polite/atomic=true编程树验证，不产生DB写，专项2/2PASS；合并正式浏览器套件16/16PASS，原功能不回归。系统有Narrator但未在用户桌面擅启；真正语音播报与完整19项WCAG/54维度及人工G5仍NOT RUN，禁止自动改成ACCEPTED。手工核验步骤已固化 G5_REMAINING_HUMAN_ACCEPTANCE.md。正式Vue/Java/DB源码不动、main HEAD1682fc5、工作区原未提交代码全保留，未stage/commit/push/建worktree。详情追加正式G5的RESULT.md。
