# 2026-10-08 数据库与后端部署核查（安全收口）

## 用户授权
用户要求“更新数据库，后端部署”。以本地 main 开发工作区为目标，保持已有数据、GxP/QA 签名和其它并行开发改动，不清空数据库、不重写已应用 Flyway 历史。

## 数据库实际状态
- 当前运行服务使用 `hospital_pharma_mes_dev`（原生 MariaDB 3306），源码包含 `V033__controlled_printing_module.sql` 及 `V034__controlled_printing_editor_and_immutability.sql`。
- 当前运行的 V034 JAR 启动日志 `C:/Users/Administrator/AppData/Local/Temp/mes-backend-v034-securityfix-live.log`，2026-10-08 12:34:49 显示 `Successfully validated 34 migrations`、`Current version ... 034`、`Schema ... is up to date. No migration necessary.`
- 上一轮 V033 校验和故障在本次启动中已不再出现。**本次未编写新迁移、未执行 flyway repair/清空数据库/禁用校验，也未更改飞行历史**。当前无待执行的 schema 更新。

## 最新源码构建与部署身份
- 运行中的 8080 Java PID `21896`，启动命令指向 `D:/codex/_project/gmp/_mes_deployments/mes-boot-V034-securityfix-20261008-1234.jar`，使用 `spring.profiles.active=local`。
- 针对**当前同一工作区**执行 `mvn.cmd -pl backend/mes-boot -am -DskipTests package`，2026-10-08 12:47:42，整个 Maven reactor `BUILD SUCCESS`、进程退出 0；日志 `mes-v034-native-print-redeploy-build.log`。Enforcer dependency convergence 和 upper-bound 规则保留并通过。
- 新包位于 `backend/mes-boot/target/mes-boot-0.1.0-SNAPSHOT.jar`。为防止覆盖生产式运行文件，另复制一份到独立部署目录 `_mes_deployments/mes-boot-V034-native-print-20261008-1248.jar`（仅留作已验证副本）。
- 经 SHA-256 内容对比，**新构建 JAR 与 PID 21896 当前运行的 V034-securityfix JAR 逐字节完全一致**（80F2838D059052C6...），故本机 8080 已运行最新本轮工作区可打包代码。避免无收益重启，不改变 8080 PID、不导致会话中断。
- 2026-10-08 12:xx 实际 HTTP `GET /actuator/health` 返回 `UP`。
- `GET /v3/api-docs` HTTP 200，254 routes，包含打印新版接口 `/api/v1/printing/designer`、`/api/v1/printing/designer/default`、`/api/v1/printing/templates/{id}/design`、`/api/v1/printing/templates`、`/api/v1/printing/artifacts`、`/api/v1/printing/fields`。
- 未登录访问 `/api/v1/printing/designer/default?businessType=INSPECTION_REPORT` 得到 HTTP **401**，验证接口至少不向匿名用户公开。未调用任何业务写入 API 或自动生成/发布质量记录。

## 保留的验收边界
- “已部署并 API/health 验证”**不等于**用户的真实身份、模板编辑操作、复杂 Word 原样还原、真实报告审批后正式归档的端到端验收。需按 QA 管控规则另行用分角色真实数据和回滚测试完成。
- 旧 HIGH（生产 QC 个人资格、示范制剂标示浓度/冻结处方、历史人员资格审计）并不因为部署完成而自动关闭。
- 当前 `main` 已有大量其它并行未提交修改，本次没有 `git add/commit/push/reset/checkout`；没有替换已有运行 JAR/停止 Redis 或额外启动容器数据库。
