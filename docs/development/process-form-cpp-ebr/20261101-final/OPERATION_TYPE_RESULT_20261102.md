# 工序类型真实保存开发结果

## 基线依据

用户明确批准 DCP-PROCESS-OPERATION-TYPE-001 和40分钟/3批实施，并随后批准第46批局部视觉修正。当前累计46，不清零、不自动47；本阶段截止2026-11-02 12:08:24（Asia/Shanghai）。已按AGENTS/MES_DEV_RULES读取当前定义、原型、UI V2、BPM交接和直接数据/API/执行合同。冻结v1.0.23历史保持原样。

## 设计差异与实现

原型的「生产／IPC」原先没有真实存储字段。现已打通 `operationType` 的数据库、Java DTO、校验、保存/回读、前端DTO和局部双单选控件。NULL旧值保持未标注；缺省/NULL输入不清除已存值；只对新工序默认PRODUCTION。未知值、大小写错误及末尾空格均被拒绝。类型切换不删除检验项、参数、角色、设备或前置关系，不新增全局必检规则。

真实图像发现并修正单选组font-size:0导致提示0×0不可见，以及T2窄屏与同行相差15px的锚点/18px额外margin。第46批只改该字段局部字体、margin和既有900断点的95px锚点；原断言和8px门禁不改。最终20张原图已逐张原分辨率查看，提示可见、窄屏起点与同行一致、键盘焦点可见、只读禁用。未完成完整对抗第二遍/九维/WCAG，不宣称UI总验收通过。

## 修改范围与迁移

- mes-process：ProcessCommands.Operation、ProcessRules、OperationEntity、ProcessDefinitions；保留旧9/10参数构造及空值省略序列化。
- frontend：process/model.ts、ProcessDetailView.vue的类型控件和新行默认值；未替换整页为工序主从工作区。
- 新增 OperationTypeContractTest、ProcessOperationTypePersistenceIT、ProcessDetailView.operation-type.test.ts。
- 追加并执行 V071（nullable类型列）及V074（二进制精确枚举检查）；V071执行后发现数据库默认不区分大小写，通过新V074收紧，绝不编辑已执行V071。072/073由其他既有任务占用，分配前已核对实际成功版本。无UPDATE回填、无旧迁移修改或Flyway repair/clean。
- 批准DCP、字段设计锁、PROCESS_OPERATION_TYPE_CONTRACT、当前定义合同/OpenAPI补充及PROJECT_BASELINE路由已同步。状态仍只在MES_TASKS。

只写事务内专属夹具，调用现有business payload检查器后才写；每项测试结束验证新工艺、用户、幂等记录均已回滚。运行页仅未保存交互，完整工艺详情前后相等，不改现有业务记录。无提交推送或生产环境操作。

## 验收证据

| 检查 | 结果 | 文件 |
|---|---|---|
| 后端及IPC/执行直接回归 | 6套件18测试，失败/错误/跳过0 | round45-test-summary.json、round45-validation.log |
| 真实控制器＋原生MariaDB | 4事务用例通过：PUT/GET、旧缺省/NULL、非法值原子性、无IPC工艺；含回滚清零断言 | ProcessOperationTypePersistenceIT、round45-validation.log |
| 前端组件 | 2套件7测试，失败/跳过0；原断言保留 | round46-ui-tests.log |
| 构建/类型 | 后端package成功；前端第45批完整build成功，第46批局部CSS后typecheck成功且Vite实际渲染 | round45-package.log、round45-frontend-build.log、round46-typecheck.log |
| 当前源码本地运行 | 新JAR PID35120，8080 health UP，Swagger暴露operationType；仅重启DEV | round45-backend-runtime.json及运行证据 |
| 真实5173/8080局部交互 | 11项检查通过，errors=[]；无工艺写请求，整对象不变；五状态×四宽度20图 | operation-type-live/20261102_120323/result.json |
| UI/整页G5 | NOT READY：完整对抗复查、全维度/恢复/真实浏览器保存未完成 | operation-type-integration-acceptance.json |

首轮失败保留：PowerShell Maven参数解析、新增测试泛型装配、事务测试表名误写proc_route、旧数据库排序规则放过小写、运行中JAR被Windows占用。分别有证据分类和定向处理，不改业务断言或伪造PASS。第44批事务初跑2通过2错误，修正夹具join后4通过；大小写负例先失败，新V074后通过。不是多轮改生产质量判定。

本次SELF-REVIEW；技能不是独立评审人。后端真实控制器证据来自MockMvc＋原生数据库事务，不冒称真实浏览器已执行正向业务保存。浏览器为实际5173/8080页面、原有账号，未保存现有工艺。

## 结论与剩余工作

工序类型真实保存功能已实现并定向验证、本地服务已加载。**生产工艺整体仍IN PROGRESS，不能READY/ACCEPTED。** 原型路线带＋左工序清单＋右侧配置的整页移植、完成条件可视化映射、IPC完整岗位业务链和完整G5仍待完成，旧工艺审批不恢复。当前已批准的类型数据缺口不再是阻断理由；下一步应回到完整工序工作区交接，不能重复做本轮登录/类型字段开发或重置累计计数。
