# 既有权限注册缺失修复

授权：2026-10-06 用户指示“仅修复现有权限注册缺失，使3项原生集成测试恢复通过”。本轮目标已完成。

仅追加 `V027__register_existing_batch_weigh_read_permissions.sql`，补齐 `production:batch:view`、`mes:weigh:view` 两条既有权限注册，类型 ACTION、enabled=true、menu_route=NULL；NOT EXISTS 防止重复插入。这两个代码已存在于冻结生产 OpenAPI 及后台 GET 授权规则，不新增权限设计、API、表或业务状态。

V027 已由 Flyway 正常应用于现有 localhost:3306 / hospital_pharma_mes_dev。最高成功物理版本由 V026 到 V027；显式 validate 通过 27 个连续成功版本，旧迁移校验和无异常。没有修改 V001～V026，没有 repair/reset/drop/rebuild。

| 验证 | 结果 |
|---|---|
| 修复前 FinalSystemPermissionIT | 9 项中 3 FAIL，目标权限注册数 0 |
| 修复后 FinalSystemPermissionIT | 9 PASS；原 3 个失败全部恢复 |
| AuthFilterIT | 3 PASS |
| 本次原生集成 gate | 12 PASS / 0 FAIL / 0 ERROR / 0 SKIP；BUILD SUCCESS；14.055 秒 |
| 显式 Flyway validate | PASS，V001～V027 全部 SUCCESS |
| 全部角色权限映射前后 SHA-256 | 一致 |
| 两项目标以外的权限记录前后 SHA-256 | 一致 |
| 独立代码复核 | 无 CRITICAL/HIGH/MEDIUM 发现 |

命令：

```text
mvn -B -ntp -Pci-integration -Dtest=NoUnitTestsForThisGate -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=FinalSystemPermissionIT,AuthFilterIT verify
```

未修改角色授权、正式角色配置、既有测试断言、业务实现、Frozen Contract、FINAL BASELINE 或 MES ACCEPTED 状态。本轮不执行无关前端/全系统回归，也不据此代替最终系统人工验收；正式角色配置矩阵仍按此前记录待验证。没有提交或推送，本机配置与此前工作树改动保留。

[逐用例结果](evidence/native-results.csv)、[前后注册与指纹](evidence/scope-after.txt)、[汇总](evidence/summary.json)。
