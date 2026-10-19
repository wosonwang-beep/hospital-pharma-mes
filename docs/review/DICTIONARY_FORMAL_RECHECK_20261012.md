# 数据字典正式项目复查及交付判断纠正 — 2026-10-12

## 1. 基线依据

需求是完整检查正式数据字典的已有项目、新增、保存及当前发布情况，延续用户已明确授权的体验评审和直接修改。实际读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md相关索引、PROJECT_BASELINE.md Authority、Global UI V2、Page Template V2 T1/T2、批准的数据库菜单/可用性DCP、上轮字典设计记录及正式列表/详情/DTO/直接测试。依据v1.0.23+批准补充、UI V2/T1和简单编辑弹窗；[本轮设计/范围记录](../development/decisions/DICTIONARY_FORMAL_RECHECK_20261012.md)。无新业务契约。

## 2. 设计差异与体验缺陷

- 已确认正式5173加载了新“管理字典项”入口，未误检查5174原型或另一checkout。
- 实机重现：新增输入PACKAGING_COMPONENT/包装组件后，仍能点击行编辑，立即变为FINISHED/成品，未保存输入丢失。上轮只保护顶部新增，遗漏行操作及树节点。这是实现遗漏，已补同等入口防护；编辑区打开/保存中行编辑和启停禁用，树点击不能替换编辑表单，取消后恢复。
- 源码检查发现成功提示只显示在被弹窗遮住的列表。已在弹窗内加入同一成功反馈，保存并刷新后显示；新开字典/编辑清除旧提示。真实成功保存本轮NOT RUN，此项反馈通过fixture检查，不能报实机持久化通过。
- 正式详情页能显示6项；“编辑”页只编辑字典基本信息，不显示/管理字典项，详情仍要求返回列表管理。这个绕行是用户体验问题，未因本轮局部保护修复而消失。未擅自改路由/全页结构。
- 1452视口列表右侧操作不在初始可视区域，需内部横向滚动；物料6项弹窗初始显示5行，第6行需内部滚动。此事实不等于项目缺失，但不应把几何合规等同操作直观。

## 3. 实际修改与数据边界

修改DictionaryListView.vue，新增e2e/system-dictionary-item-guard.spec.ts及本轮设计/评审记录，更新MES_TASKS.md纠正当前状态。没有修改旧测试断言/阈值、冻结设计列宽或历史验收记录；无API、权限、字段、迁移、数据库业务写入、服务重启、提交或推送。未借本轮清理既有禁止命名数据。

## 4. 验收和运行证据

| 门禁 | 本轮结果及限制 |
| --- | --- |
| 设计/范围 | 沿用局部交互设计，修复防覆盖实现遗漏；全流程体验仍有绕行 |
| typecheck/build | npm run build，exit0，Vite 19.11秒，保留既有大包警告 |
| 定向回归 | 两项既有字典用例+新增防覆盖/弹窗成功提示用例，3/3，5.3秒，exit0；fixture不算实机业务保存 |
| 实机只读功能 | 17/17检查，exit0；新输入保留、全部行操作禁用、树节点点击不覆盖、取消恢复、现有数据/切换/焦点/校验及无业务写入 |
| 实机弹窗视觉 | 1440/1280/900/375四视口表单同行与弹窗/单元格边界通过；主查询区旧窄屏失败未改判 |
| 保存/持久化/权限负例 | 真实新增及保存后重开、数据库持久化、真实窄权限角色NOT RUN，不以fixture代替 |
| 发布 | 构建产物已更新，运行仍Vite+Java classpath开发服务；没有产物部署/发布，不报发布完成 |

命令：`npm run build`；`npx playwright test e2e/system-dictionary.visual.spec.ts e2e/system-dictionary-import.visual.spec.ts e2e/system-dictionary-item-guard.spec.ts --project=chromium-desktop --output=<本机TEMP>/mes-dictionary-formal-recheck-e2e`；CUAWright环境运行本机TEMP/mes-dictionary-formal-recheck.py（正常认证、原mutation=false）。日志为同目录mes-dictionary-formal-recheck-build.log、mes-dictionary-formal-recheck-e2e.log、mes-dictionary-formal-recheck.log；实机结果`C:/Users/Administrator/AppData/Local/Temp/mes-dictionary-formal-recheck-20261012/result.json`。既有恢复401资源日志保留；无JS错误遮罩，未宣称控制台全空。

截图位于`C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/`：1791782504901_dictionary-formal-recheck-list.png、1791782518551_dictionary-formal-recheck-material-items.png、1791782542756_dictionary-formal-recheck-add.png、1791782568713_dictionary-formal-recheck-unsaved-overwritten.png、1791782753968_dictionary-formal-recheck-detail.png。全部经工具直接视觉检查，保留覆盖输入的原失败截图；修改后四视口截图及树取消截图在本轮TEMP证据目录。

运行核对：5173 PID11704，命令指向本checkout frontend/mes-web/node_modules/vite；8080 PID5552为Java21 MesApplication classpath运行。源码模块GET HTTP200且含本轮itemNotice/编辑防护。dist/index.html本机13:24:45生成，SHA256 A911481C5E506BB9614C598DECF84B8C6AE8287A8FCC508BCEBD186E1020E28C；DictionaryListView.vue SHA256 501FFA94AB46F4F091AC6A427A7FF6B73864CD42935E4E62925E909D1FC38123。构建包存在不证明正在运行产物。

## 5. 交付判断纠正

上轮把目标收窄为按钮/文字和布局修补，遗漏真实操作中输入覆盖风险，未完成真实保存闭环却登记局部READY；该局部证据不能证明用户要求的功能交付完成。本轮纠正任务当前状态至IN PROGRESS，仅保留原通过证据，不删除历史或改判原失败。构建完成、开发服务已加载源码、真实发布、完整功能验收必须分别报告。

尚缺真实新增保存后重读/重开与持久化验证；详情/编辑绕行、主列表横向操作和既有窄屏/命名问题仍在。不得宣称正式发布、全CRUD完成或用户验收。本轮自动产品修复轮0，无新失败重试；13:21启动至本轮记录完成在10分钟预算内。当前状态只在MES_TASKS.md维护。
