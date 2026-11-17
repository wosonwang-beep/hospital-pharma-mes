# MES 后端回归旧测试契约对齐（2026-11-15）

状态：REGRESSION PASS / 392 TESTS / 0 FAILURE / 0 ERROR。来源：用户要求完整后端回归与代码复核；本机 main、基线 v1.0.23、既有 V077/成品Schema V2 冻结证据。

本轮完整 Surefire 实测：100套件、392例、失败2、错误4、跳过6；不改业务生产逻辑，不靠跳过、调低断言或延长超时求绿。

1. AuditContractTest.java 源码已由批准废除审计功能的提交正式删除，但旧target残留class导致伪失败：先仅清理 mes-audit 模块编译输出，再确认该旧类不参与。
2. Mes002MigrationContractTest 旧断言只允许 V001..V031，与目前已执行77条 Flyway冲突：保留 V001/V002/V028..V031 原SHA断言，改为精确要求 V001..V077 连续，无重号缺号（比原检查覆盖更完整）。
3. TraceChargeProjectionTest 原测试桩仍使用 lotForBatch→forLot，生产 TraceService 已依照成品V2契约使用 forBatch：保留3条CHARGE、2条FINISHED_SHIPMENT与零WEIGHING的原断言；更正 mocked provider 接口为 forBatch，模拟V2成品出库。
4. FoundationControllerTest、MesApplicationTest 明确排除 DataSourceAutoConfiguration，故需对已有 PrintSourceCatalog 依赖的 JdbcTemplate 提供 test-only MockitoBean，不接触DEV数据库，不改变正常应用生产代码。

变更白名单限以上四项后端测试文件和本记录；程序/接口/业务字段/签名/存档不变。回归必须重跑完整同命令，并核对测试报告而不是只信命令退出码。
5. 前端完整单元回归最初238PASS/1FAIL，唯一旧契约 platform-contract.test.ts 仍要求已按V077废除的 /audit 路由存在；保留其它MES基础路由及集成路由断言，改为明确断言 /audit 不存在。
