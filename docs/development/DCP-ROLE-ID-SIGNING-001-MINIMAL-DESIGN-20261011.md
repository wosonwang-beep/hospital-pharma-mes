# DCP-ROLE-ID-SIGNING-001 最小设计审阅稿（2026-10-11）

本稿承接用户 09:44 的角色 ID 缺陷修正授权，供父任务审核具体实现边界。本轮只保存设计，不改 DEV 数据、产品代码、权限或会话。此前宽版审阅稿中的全局授权版本、组织模型、QA 资格重构和大规模历史迁移不纳入本方案。业务权威仍为 FINAL BASELINE COMPLETE v1.0.23 及已批准补充；实施前须完成本稿的有界设计锁和受影响契约同步。

## 1. 已核实的起点和边界

七个角色编码改码事务已提交并由父任务接受，不再执行该 DML。角色 ID 285–291 保持不变，当前编码分别为 MES_MASTER、MES_WAREHOUSE、MES_PRODUCTION、MES_SAMPLER、MES_QC_ANALYST、MES_QC_REVIEWER、MES_QA。用户、角色成员关系及 353 条角色权限关系未改。

当前只有草稿模板 291 包含岗位规则：签名规则 180–185、复核规则 269–274 共 12 条为 MES_QC_REVIEWER，对应角色 290；签名规则 186、复核规则 275 共 2 条为 MES_QA，对应角色 291。模板仍 DRAFT，version_no=2；上述子规则 version_no=1；content_hash、approved_by、approved_at 均为空。当前已发布模板 6、7、9、10 没有签名或复核岗位规则；未发现需要本次转换的旧岗位表达式、批次规则快照或复核记录。

只修改 eBR 岗位配置、选项读取、当前资格匹配及必要会话支持。保持签署人为用户 ID，保留历史签名、摘要、审计、模板发布状态和所有现有权限。模板发布、两项增权提案及签名物理删除继续暂停。

## 2. 三类身份事实

1. **签署主体**：gxp_signature.signer_id、审计 actor_id、复核 reviewer_id 继续保存用户 ID，不替换成角色 ID。
2. **当前资格**：新规则以 requiredRoleId 匹配签署时该用户当前启用的角色 ID，同时核验当前用户启用状态、现有操作权限、组织/对象绑定、重新认证及职责分离。编码和中文名称只供显示。
3. **历史事实**：以当时冻结的规则、用户 ID、记录摘要、签名证据和审计证据核验；后续撤销权限、调岗或角色改码/改名，不使已完成的签名因此失效。已有核验链目前没有查询当前角色成员关系，不把此行为描述成已经存在的历史失效缺陷。

## 3. 最小数据库变更

新增一个物理 Flyway 迁移，版本号实施前从实际成功历史重新确定；不得编辑已执行迁移。

| 表 | 新增字段 | 约束及用途 |
| --- | --- | --- |
| ebr_signature_rule | required_role_id BIGINT NULL | 外键到 sys_role.id，ON DELETE RESTRICT；建立索引 |
| ebr_signature_rule | required_role_name_snapshot VARCHAR(128) NULL | 冻结中文显示名 |
| ebr_review_rule | required_role_id BIGINT NULL | 同上，独立外键和索引 |
| ebr_review_rule | required_role_name_snapshot VARCHAR(128) NULL | 冻结中文显示名 |
| ebr_template_version | definition_schema_version SMALLINT NOT NULL DEFAULT 1 | 只允许 1/2，区分旧/新定义序列化 |

保留现有 required_role 列；schema 1 仍表示旧 requiredRole，schema 2 用它保存 roleCodeSnapshot。不删除或重命名旧字段，不给 sys_user 添加全局授权版本，不新增组织、别名或签名表。

新草稿和从旧版复制出的新版本使用 schema 2。草稿 291 的转换只接受上述已核实的 14 个主键及唯一角色映射，保存角色当前中文名称，并通过受控草稿保存递增根/子记录乐观锁版本。实施时重新核验状态、版本和映射；任一变化阻塞转换。旧已发布行和历史证据不回填、不改摘要。本轮不执行迁移或转换。

## 4. API 与定义契约

沿用 GET/PUT /api/v1/ebr/templates/{id} 及现有创建、版本复制、lint、提交、批准、发布路由、If-Match、Idempotency-Key 和权限。

schema 2 的定义根使用 schemaVersion="2.0"；签名/复核规则输入新增 requiredRoleId，采用十进制 ID 字符串以避免浏览器整数精度损失。客户端只提交选择的 ID，服务端校验角色存在且启用，并填写只读 roleCodeSnapshot、roleNameSnapshot；不接受客户端伪造显示快照或用旧编码授权。现有规则其他字段、必填性及职责分离契约不变。schema 1 读取保持原字段及原序列化，不能把新的空字段注入旧规范内容。

增加有界只读 GET /api/v1/ebr/role-options：复用 ebr:template:view，支持分页/关键词，返回 id、code、name、enabled，只给出可选角色，不暴露成员、权限或管理动作，不给用户新增 IAM 权限。通过已有 SystemReferenceQuery 扩展内部角色 ID 查询与选项查询，不跨模块直接操作 IAM 表。

新规则的运行时复核 DTO 使用 requiredRoleId 和冻结的代码/名称显示字段；旧规则 DTO 保持兼容。新签名审计政策证据记录固定 rule ID、requiredRoleId 和政策摘要；旧政策证据按原格式核验。复核 frozenRule 保存同一组冻结字段，签名规范封套及 signer_id 的含义不变。

## 5. 具体代码边界和界面

后端 eBR：EbrCommands、EbrDefinitions、EbrController、EbrService，以及 TemplateEntity、SignatureRuleEntity、ReviewRuleEntity 增加 schema 2 读写。EbrFormSignableProvider、EbrRuntimeTransactions 改为当前角色 ID 匹配；EbrRoleEvidence、EbrRuntimeModel 分别按冻结 schema 核验旧/新证据，不能加入历史签名的当前授权查询。

系统模块：SystemReferenceQuery 增加 requireRoleId/roleOptions；SysRolePermissionMapper 增加 activeRoleIds 及必要的当前资格查询；SystemIdentityDirectory 提供服务端可信角色 ID，不使用前端角色值。

会话模块：LoginSnapshot、PlatformPrincipal、CurrentPlatformContext、SpringSecurityPlatformContextResolver、SessionSnapshot、RedisSessionStore 增加可信 roleIds 和格式版本，roleCodes 保留旧读取/显示用途。签名服务在实际签署和 eBR 复核命令边界核验当前资格。

前端：frontend/mes-web/src/views/ebr/types.ts、attributes.ts、model.ts、DefinitionDesigner.vue，以及 RuntimeFormView.vue、api/ebrRuntime.ts 更新类型和显示。把现有“角色编码”文本框替换为角色选择器，value 为角色 ID，label 为“中文名称（编码）”；旧发布版本显示冻结名称/编码并保持只读。不新增页面、不改页面结构，继续当前 UI V2 和既有 T1–T6 模板。查询标签与控件始终同一水平行，窄屏整对换行。若实现需要实质页面重设计，先走 Section 2A 原型审批，本稿不豁免该规则。

## 6. 会话兼容和撤权生效

新登录从数据库取得启用角色 ID。缺少 roleIds/格式版本的旧 Redis 会话不能凭旧 roleCodes 推算 ID；受影响的新签署/复核请求提示正常重新登录。保留现有认证、密码及重新认证绑定流程，不暗中升级旧缓存或撤销所有用户会话。

仅把缓存 roleIds 用于显示或初步判断。实际签署/复核命令必须在操作事务内重新读取当前用户启用状态、角色 ID 和现有权限；当前 ebr:sign 及对应复核权限被撤销后，旧 token 也不能继续签署。查询限定当前操作者，复用现有 IAM 查询服务，不建设全局逐请求权限引擎，不改变 QA 资格规则。并发撤权按当前读与现有锁/事务约束处理，测试必须覆盖撤权先提交时旧会话被拒绝。

同一用户拥有多个角色时可按匹配角色获得资格，但仍按用户 ID 执行职责分离，不能靠换角色绕过同人限制。角色只改编码/名称而 ID、启用状态和权限保持时，当前新规则仍可匹配；冻结名称不随改名重算。

## 7. 旧发布版本读取边界

schema 1 继续原 canonical 路径、原 policy 和原摘要；schema 2 使用新冻结字段。禁止读取时实时 JOIN sys_role 填充旧或已发布规则的名称，以免规范内容发生漂移。通用签名封套的 schemaVersion 与模板定义 schemaVersion 分开，不重写旧签名封套。

从旧定义复制新草稿时，只在新草稿中解析唯一的启用角色 ID；无法唯一解析则要求选择或阻塞，不推测角色、不通过双编码放宽资格。当前没有相关旧发布岗位规则，因此不引入历史别名表、不改通用 hasRole DSL、不进行批量历史迁移。

## 8. 七个账号及中文名称：独立建议，尚未执行

账号 ID、密码、成员关系、权限保持不变。以下人员姓名只是数据库已有显示值，未核验真实人员身份且无需替换；以下仅统一岗位用语。登录名需在实施前重新按 LoginNameNormalizer 的 strip/NFKC/小写规则检查唯一性。admin、qc.documents 不在改名范围。

| 用户 ID | 当前登录名 → 建议登录名 | 当前显示名 → 建议显示名 |
| --- | --- | --- |
| 11564 | demo.master → master | 林静（主数据）→ 林静（主数据维护员） |
| 11565 | demo.warehouse → warehouse | 李倩（仓储）→ 李倩（仓储管理员） |
| 11566 | demo.sampler → sampler | 陈晨（取样）→ 陈晨（取样员） |
| 11567 | demo.qc.analyst → qc.analyst | 周敏（QC检验）→ 周敏（质量检验员） |
| 11568 | demo.qc.reviewer → qc.reviewer | 王睿（QC复核）→ 王睿（质量检验复核员） |
| 11569 | demo.qa → qa | 赵宁（QA）→ 赵宁（质量保证审核员） |
| 11572 | demo.production → production | 刘峰（生产）→ 刘峰（生产操作员） |

对应角色显示名建议：285 主数据岗位→主数据维护员；286 仓储岗位→仓储管理员；287 生产岗位→生产操作员；288 取样岗位→取样员；289 QC检验岗位→质量检验员；290 QC复核岗位→质量检验复核员；291 QA岗位→质量保证审核员。七个角色编码已改完，不重复改码。

现有 PATCH /api/v1/admin/users/{userId}/profile 仅支持 displayName、expectedVersion；PATCH /api/v1/admin/roles/{roleId}/name 支持 displayName、expectedVersion，可用于独立显示名方案。现有用户 API 没有改 loginName 命令。

若该独立方案获批，需新增有界 PATCH /api/v1/admin/users/{userId}/login-name，参数 loginName、expectedVersion、reason，复用现有用户管理权限、唯一约束、乐观锁和安全审计；只改 login_name/login_name_normalized 和正常维护元数据，保持 user ID、密码及历史签名身份。变更后重新登录；旧用户名不永久保留别名，也不触发密码重置。该接口与七账号变更不得借本次角色 ID 实施自动执行；需单独确认名称及行动范围。QA 资格配置里旧账号字面量存在，相关影响必须在独立方案执行前审阅，本次不重构资格模型。

## 9. 验证与实施顺序

先审批本稿的有界设计并同步数据库、API、UI、权限、GxP、测试及 RTM/集成引用；再实施未执行迁移和代码，不恢复模板发布或增权。草稿 291 的 DEV 转换计划在执行前独立展示精确主键、版本、前后内容及新备份依据，获得该阶段审核后才写数据库。

针对性验证包括：ID 选择与 DTO 校验；唯一角色映射及外键；旧/新会话序列化与旧会话重登录；角色多选及同人职责分离；禁用用户/角色、撤销签署或复核权限后旧 token 被拒绝；同角色 ID 改码/改名后仍正确匹配；签署后撤权/调岗/改名不改变历史有效性；旧发布模板 6/7/9/10 的 canonical/hash 字节不变；新 frozenRule 和审计政策中的角色 ID 一致。

执行受影响模块编译、单元及契约测试；只有迁移/SQL/事务涉及处才跑共享 MariaDB 的可回滚或明确新记录集成测试。前端用真实 5173/8080 检查选择保存、旧版只读、权限拒绝及响应布局，记录截图和交互证据。遵守最多两轮修复及实际视觉验收门槛，不提前声明 PASS/ACCEPTED。账号改名测试与执行另行审阅。

## 10. 已验证的 sys_user 漂移和保留事项

角色改码前新备份中的 admin（11556）version_no=402、updated_at=2026-10-11 18:15:41.296662；安全重试前为 404、18:20:26.842908。逐字段比较只发现这两个字段变化，其余用户及登录名、显示名、凭据字段、启用/锁定状态等保持一致。审计 924、925 分别在 10:19:03.775149 UTC 和 10:20:26.605737 UTC 记录该用户 LOGIN SUCCESS；现有成功登录 SQL 会递增版本并写 CURRENT_TIMESTAMP。二者与正常登录维护行为一致，角色事务没有写 sys_user。更早初始摘要未保存完整行，不能推断那一时段具体变化原因；general_log 关闭，没有完整 SQL 因果日志。

已提交改码的独立复核证明：22 行预期更新、安全维护审计新增 22 条；权限/成员、50 条有效签名、旧审计、发布模板及归档保护内容未改变；402 个外键未发现孤儿。备份与私有凭据摘要均留本机，未提交 Git。相关执行报告为 docs/review/ROLE_CODE_NORMALIZATION_EXECUTION_20261011.md。

本稿的有界代码/未应用迁移方案已获批准；实施证据和部署边界见 DESIGN LOCK 与执行报告。剩余工作仅为本稿批准后的受限实现，以及另行确认的账号/中文名称方案；先前历史兼容补丁部署、模板发布、增权和数据删除仍按各自暂停状态处理，不在本稿中自动恢复。

## 审批补充

本稿已获父任务有界批准；六项补充约束及实施/部署界线记录于 decisions/ROLE_ID_SIGNING_DESIGN_LOCK_20261011.md。仅批准代码、未应用迁移及定向测试；DEV迁移/写入、发布、增权、账号改名及签名删除未批准本轮执行。
