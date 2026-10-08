# 医院制剂 MES — V034 数据库更新与后端本机部署审查记录

日期：2026-10-08（开发机当地时间，Asia/Shanghai）。工作环境：Windows PC-202401011703；`main`；FINAL BASELINE COMPLETE v1.0.23 + 打印模块已批准技术变更。数据库仅 `hospital_pharma_mes_dev` (localhost:3306)，Redis 仅既有 6379，不使用 Docker/第二数据库。本文件是**新完成状态记录**，不覆盖旧 R12/R13 当时的阻断证据。

## 一、改动前的备份与现状

正式更新前创建数据库完整逻辑备份：
- `D:\codex\_project\gmp\_mes_database_backups\hospital_pharma_mes_dev_before_V033_reconcile_20261008-121316.sql`
- 大小 **2,970,142 字节**，SHA-256 `26C18598A096E4349D3FD451191FCBE592211A4FA474FB75F15011C0E96F1730`
- MariaDB 13、原表/数据/Flyway history 未删除、未清空。保存了一份迁移编辑状态 `V033_CURRENT_UNAPPLIED_SOURCE_20261008-121316.sql`。

开始时数据库 `flyway_schema_history` 版本 033 success=1、checksum `1576484630`，包含三张 `mes_print_template_version`、`mes_print_binding`、`mes_print_artifact` 表；无打印触发器、无 `mes_print_editor_session`，且权限只有原先的 `print:generate`。当时源码 V033 被后续并行开发扩写至含第四张表、六触发器及 `print:document:generate`，checksum 变为 `595506057`，阻断 Flyway 校验。

## 二、V033 源码复原与 append-only V034

使用实际数据库物理元数据与 Flyway **逐行规范化 CRC32** 精确校验，在隔离备份文件中构造移除**后加编辑会话表、6 个触发器**，并将后加权限名恢复为 **`print:generate`** 的 V033 内容。恢复后的 SQL 归一化 checksum **1576484630**，与原数据库已执行历史记录完全一致；同样与当时实际的三表/无触发器/旧权限物理状态一致。注意：原始文件已无独立历史字节副本，恢复依据为物理结构 + SQL 差异 + Flyway checksum 一致，而非声称具有原始逐字节来源。

还原后 V033 SQL 文件保留 3 张原有表、旧权限码，数据库 `flyway_schema_history` **没有修改**。**没有使用 `flyway repair`、`flyway clean`、`baseline`、忽略/关闭 checksum 校验，也没有重写任何已执行数据库记录。**

新的 **`backend/mes-boot/src/main/resources/db/migration/V034__controlled_printing_editor_and_immutability.sql`** 承接后续功能：
- `mes_print_editor_session`（组织范围、编辑身份/会话/期限与模板版本 FK）；
- 六个打印模板/业务绑定/不可变 PDF 工件的删除/更新防篡改触发器；
- `print:document:generate` 显式权限；
- 按先前 V032 批准的全权限策略，为 `SYSTEM_ADMIN` 补齐新增已启用权限，**不授予普通岗位**。
- V034 源码 SHA256：`DB54520BFB98E0A11296AE8F0EE692582E7177E89C961277AE6F51187917EC3D`。

通过 `NativePrintDesignerIT` 启动 Spring+Flyway 正式应用 V034（实际运行耗时约 0.092 秒），之后本机 MariaDB 只读校验：
- V033 success=1 checksum=1576484630（保持原历史）；
- V034 success=1 checksum=81055846；
- 打印数据库表 **4**，防篡改触发器 **6**；
- 新权限记录 **1**，SYSTEM_ADMIN 对新权限有效授权记录 **1**；
- 更新时 `mes_print_artifact` 真实业务工件数 **0**。
- `NativePrintDesignerIT` rollback-only 事务集成 **1/1 PASS**：真实生成两个不可变模板版本、调用 docx4j/FOP 生成 PDF、验证、发布与绑定；测试结束自动回滚测试账户及打印模板，`NATIVE_DESIGNER_ROLLBACK_CONFIRMED`。
- 日志 `C:\Users\Administrator\AppData\Local\Temp\mes-dbdeploy-V034-and-native-integration.log`。

## 三、后端构建与部署

为避免此前运行 JAR 被 Maven package 覆盖，先将源代码复制到仓库外临时构建目录 `D:\codex\_project\gmp\_mes_deployments\build-src-V034-20261008-122342`，排除 `.git`、`node_modules`、各模块 `target`、根 `.env`；**不修改运行中的仓库 Jar**。独立执行多模块 `mvn -pl backend/mes-boot -am -DskipTests package`，Maven Enforcer 依赖收敛和 20 模块构建全 PASS。日志 `mes-dbdeploy-staged-v034-package.log`。

首次 staging JAR `mes-boot-V034-20261008-1224.jar` (SHA256 `6F5A9BAF303A2F61C3D961B8F839CFAAEF2BB645D4C936F26DE1F1340970ECC2`) 含 V033/V034、`mes-reporting`、docx4j/FOP。曾在临时端口 18080 启动，`/actuator/health` 状态 UP，Flyway **34 个迁移校验通过**，OpenAPI 254 条路由其中打印类 16 条，匿名访问打印模板 401。

正式将 PID1968 的旧 8080 Java 进程停止，切换到首次新版 PID11788 后发现**管理员实际打印列表和中文字段 GET 返回403**。定位为 `mes-security/SecurityConfiguration.java` 只允许旧接口，默认 `anyRequest().denyAll()` 未加入新的打印端点；数据库已有 SYS_ADMIN 授权，但无法越过 HTTP 安全层。

沿用既有 `print:template:view`、`print:template:manage`、`print:template:publish`、`print:document:generate` 权限代码补齐 **HTTP GET/POST 精确路由白名单**，不使用宽泛 permitAll 或全路径授权；默认 `denyAll` 和应用服务组织/业务独立复核保持。在线 Word 编辑器的 `/editor/transfer/**` 沿用单独有序的安全链/签名能力令牌。

从原仓库复制该单文件修复到已隔离的 staging 构建目录，重新 Maven package，生成正式稳定 JAR：
- `D:\codex\_project\gmp\_mes_deployments\mes-boot-V034-securityfix-20261008-1234.jar`
- SHA256 `80F2838D059052C6BEDEA8FFA8233647F55124838F9A9DB8CB5FE95E038DC6C8`、128,544,863 字节。
- 原 Java 启动 JAR 额外备份 `D:\codex\_project\gmp\_mes_deployments\mes-boot-previous-8080-backup-20261008.jar`，SHA256 `7DF8BA2C460E499F34893F43C3067CE7D4900CD7FE97701B934AA7EF9C39A782`。
- 第二次在 18080 临时验证 PASS（34 个迁移 validate），随后关闭临时实例并替换 8080。
- **当前本机 8080 后端 PID21896** 使用上述 securityfix jar，`--spring.profiles.active=local`、原项目根目录为工作目录（使用受忽略的 `.env`），持久运行日志 `C:\Users\Administrator\AppData\Local\Temp\mes-backend-v034-securityfix-live.log`。日志显示 Spring 启动成功、Flyway 34/34 校验 PASS。

## 四、实际运行后的 HTTP 与页面验收

- `GET http://127.0.0.1:8080/actuator/health` 返回 `UP`。
- `GET /v3/api-docs` 含 254 路由、打印子路由 16。
- 未认证 `GET /api/v1/printing/templates` 返回 **401**。
- 真实管理员账号从 PC 和 Mobile 分别访问 `/admin/print-templates`，读取 `GET /api/v1/printing/templates`、`/fields`、`/designer/default` 全部 **200**，正常出现中文字段、A4 画布和 Word 表格粘贴入口；不进行业务写入。首次修复前 403 已复现，最终回归后全部 200。
- `demo.qc.reviewer` 无打印权限，PC/Mobile 分别验证 4 个打印 GET 返回 **403**；未将模板操作权限放宽给 QC 岗位。
- Playwright 最终现场 4/4 PASS，0 FAIL，`mes-dbdeploy-v034-live-ui-plus-role-denial.log`。
- 已有 Vue/Vitest 114 PASS、打印模块 34 PASS/5 opt-in skip、合并表格与中文绑定的 docx4j/FOP PDF 真实转换验证仍有效；未在部署中修改前端代码或触碰历史受控签名。

## 五、未完成项与保护边界

- 打印模块当前开发数据库业务模板/产物尚无真实人工审批归档记录；**实际生产格式的打印模板发布**仍需现场模板内容核对、经授权发布及 QA 原签署 Gate；不能凭回滚测试直接签发历史质量文件。
- 之前记录的医院制剂演示批标示浓度不一致、生产 QC 个人资格门禁缺口、历史人员资质记录创建时间滞后等 HIGH 风险仍另行 OPEN；此次数据库/部署没有实施未经批准的业务合规 Gate。
- **没有提交或推送 Git；`main` 保持原有并行脏工作区。** 保留数据库和旧 JAR 备份，未创建额外 Redis/数据库实例；部署是本机 DEV 而非 PROD。

## 六、部署后原业务权限回归

部署完成后继续运行现有浏览器真实七岗位 GET 200/403 访问隔离矩阵，分别检查生产、仓储、取样、QC 分析、QC 审核、QA、主数据 7 个演示岗位对自己的业务接口和非本职接口的读取权限；**1 套包含 7 个岗位 / 21 个 HTTP GET 的测试全部通过**（每个角色一个 200，两个 403），Playwright EXIT 0。日志 `C:\Users\Administrator\AppData\Local\Temp\mes-dbdeploy-v034-sevenrole-regression.log`，未改变生产/QC/QA 权限合同。

该项与打印模块浏览器专项 **4/4 PASS** 为不同测试套件；均执行在已部署的新 8080 服务上，浏览器前端仍沿用既有 4174 开发服务器，未启动第二个 Redis。
