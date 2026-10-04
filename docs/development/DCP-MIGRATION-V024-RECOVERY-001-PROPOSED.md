# V024 失败迁移的透明恢复提案

日期：2026-10-04。状态：PROPOSED，待用户明确批准。**本文件不授权恢复操作。**

## 已发生事实

Codex 生成的 V024 将库存决定表的 MaterialLot FK 错写成 `wms_material_lot`，实际已有表为 `md_material_lot`。原生 MariaDB 的非事务 DDL 已执行 `proc_operation_def.ipc_definitions_json` 列新增，随后第一张新表创建失败。Flyway 记录 V024 success=0；V001–V023 成功历史和业务数据保持。

原始 V024 文件、SHA-256 和失败历史快照保存于 `docs/acceptance/functional-closure/migration-failure/`。不得将此次失败描述为 PASS，不得以忽略失败、关闭 validation、基线重建、替代数据库或伪造成功记录绕过。

## 推荐的最小恢复操作（需要本次明确例外批准）

1. 复核原始证据与数据库仍为 V001–V023 成功、V024 唯一失败；逐项确认 V024 仅完成了新增空数组定义列，没有任何新业务表或记录成功创建。
2. **仅对此失败且未完成的 V024 允许一次脚本修正**：FK 指向实际已有 `md_material_lot(id)`；首次 ALTER 改为 `ADD COLUMN IF NOT EXISTS`，并先核实已存在列类型/默认值/校验与批准契约相符，防止静默忽略不同结构。
3. **仅对此失败 V024 允许一次 Flyway repair 清除失败标记**，前提是原始失败行和旧脚本已保存、V001–V023 当前脚本校验值均与数据库一致。repair 前后比较全部成功历史，任何旧成功记录被改动立即停止并保留证据。
4. 原生 DEV 按正常 Flyway 再执行 V024；保留新的迁移日志和旧失败证据。继续跑已经编写的真实签名冻结、IPC、清场及直接回归；不再次生成无关测试。

此批准是 AGENTS.md“Executed migrations are append-only”“never ... checksum-change ... or ... flyway repair”的**单次、精确例外**；不会扩展为今后修改已执行迁移的常规授权。

## 明确不包含

不删除、不重建、不清空 DEV；不改 V001–V023；不创建另一个数据库；不改成功迁移的校验值；不删除/覆盖任何已存在生产、QC、GxP、签名或审计事实。原始失败证据永久保留，验收报告明确记录第一次失败及恢复过程。

若用户不批准这个例外，数据库相关开发验收保持阻断；已有代码、UI、候选基线和测试保留，不擅自选择绕过方式。
