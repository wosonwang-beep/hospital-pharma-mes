# HYTEA DEV 成品数量事件权限路由修复（2026-11-12）

状态：SOURCE PATCH APPLIED / COMPILED / RUNTIME RESTART BLOCKED。仅修复现有正式API端点与既有权限码的安全映射，不新增权限、不改变业务/API/状态机合同。

## 根因
`ProductionQuantityController`已有GET `/api/v1/main-batches/{id}/quantity-events`、POST同路径记录和POST `/api/v1/quantity-events/{id}/reverse`；服务层分别要求`balance:view`、`mes:quantity:record`、`mes:quantity:reverse`。SYSTEM_ADMIN 11556已拥有记录权限，但`SecurityConfiguration`原来遗漏三个RequestMatcher，末尾`.anyRequest().denyAll()`使POST返回403。无业务记录写入。

## 最小修复
仅在现有SecurityConfiguration增加三条映射：GET quantity-events→`balance:view`；POST quantity-events→`mes:quantity:record`；POST reverse→`mes:quantity:reverse`。使用既有权限，不授予/修改任何角色权限，不改控制器/服务/数据库。

## 验证
- SecurityConfiguration SHA256 `328CFAECCD6F3A3E2783116F9246B7401683BF5A9315F1E454C4296820CEB2C8`。
- `mvn -f backend/pom.xml -pl mes-boot -am -DskipTests package`：19个模块全部编译成功；最后Spring Boot repackage因端口8080进程锁定目标JAR，无法rename `.jar` 为 `.jar.original`。
- 已确认8080 `/actuator/health`=UP，监听PID31124。AI Commander当前未安装elevated helper，Stop-Process PID31124返回Access Denied。没有使用taskkill/SQL/替代端口或其他方式绕过。
- `mvn -f backend/pom.xml -pl mes-security -am test`在上游`mes-audit`测试阶段失败：`AuditContractTest.appendRequiresAnExistingTransactionAndRepositoryIsAppendOnly` line30断言，`mes-common` 8/8通过，`mes-audit` 23/24；因此尚未运行到mes-security测试。
- 通过正式API只读确认数量事件OUTPUT/SAMPLE仍0条，批655 IN_PROGRESS/version2，成品日期字段仍null，余额405仍PASS（239.4kg=239.4kg）。

## 下一步
需在本机以管理员权限停止/重启旧MES API（PID31124），让现有SecurityConfiguration加载新映射；然后用已批准的DEV日期2026-11-12/有效期2027-05-12记录OUTPUT398000ml、SAMPLE2000ml，重新计算余额，完成生产、成品入库/QC/QA/归档。当前不能声称映射已在运行时生效。
