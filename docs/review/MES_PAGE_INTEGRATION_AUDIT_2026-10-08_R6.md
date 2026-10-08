# MES R6 — 真实问题修复与验证（2026-10-08）

**范围**：本地 `main` 未提交工作区；PC/Mobile 真实 HTTP/浏览器测试；保持 FINAL BASELINE v1.0.23、数据库、签名和既有受控记录不变。

## 问题一：生产批详情 QA / 物料平衡 HTTP 500

- 实际复现：批次 `20261007-KCL-001` 的 `/api/v1/qa/batches/16/review-model`、`/api/v1/main-batches/16/material-balance` 多次返回 HTTP 500 / `INTERNAL_ERROR`。已对生产批详情 Playwright 增加“HTTP 500 不能算 PASS”的断言。
- 同时检查发现：此前对**运行中的后端**执行 Maven package 失败，`mes-boot-0.1.0-SNAPSHOT.jar` 被留为约 102 KB 的普通 Maven JAR，manifest 没有 Spring Boot 启动结构。是强关联的**环境/打包问题**，不能在缺少原始异常堆栈的前提下断言这一定是唯一根因。
- 修复：确认 PID 属于本地 mes-boot 后，只终止其 8080 后端进程；保留 MariaDB 3306、Redis 6379、Vite 4174；重新执行 `mvn -DskipTests package -q` PASS；生成约 81.9 MB 的完整可执行 Spring Boot JAR；重新启动 Java 21 后端（日志在本机 Temp 下 `mes-backend-review.*.log`）。
- 启动时 Flyway 校验 32 migrations PASS、版本 032，未新增/修改迁移。
- 浏览器针对性回归：PC/Mobile 均验证两个 GET 接口返回 200。手机端重复 4 次打开详情（每轮两个接口约各两次请求），全部返回 200；PC 端同样重新复测。未触发任何业务写命令。

## 问题二：来料调查筛选混入成品批次

- 原因：`IncomingListView.vue` 使用通用批次 `IncomingReferencePicker`，未传入来料范围；`/wms/material-lots` 本来就同时返回成品和原辅料批次，原页面出现 FG-KCL 选项。
- 修复：只在来料查询页为引用组件传入 `investigationScope`；在通用引用组件中，当范围为 `INCOMING_MATERIAL` 时依据**服务端返回的冻结 `materialSnapshot.materialType`**排除 `FINISHED`，不使用业务编号前缀作为筛选依据。
- 真实浏览器断言：保留 `RM-KCL-20261007223208-02` 物料批次，排除所有 `FG-` 成品批次；PC/Mobile 验证通过，OOS 未关闭时 QA 放行按钮保持禁止。
- 未更改后端接口契约、数据库、状态、角色或 GxP 决策。

## 针对性验证
- PC + Mobile：两个测试文件共 4 项，全部通过（批次两个接口稳定性；来料 OOS 阻断与筛选）。
- 前端 `npm run typecheck`：通过。
- 更大范围联动测试另行汇总，不与上述 4 项混为一谈。

## 未关闭的问题
1. 演示批标示 `30ml:3g` 与冻结处方 `20g/1000mL` 浓度相差五倍：HIGH、需受控方案处理，不能更改已有放行记录。
2. 演示历史 QC/仓储/QA 操作人员岗位责任链不一致：需资格与责任证据评审。
3. 最终 eBR PDF 仍待归档。
4. 质量计划独立 QA 资格 Gate 仍待受控设计评审。

**边界**：当前工作区未提交或推送，未改写数据库记录、电子签名、QA 放行、库存流水。

## 最终本轮回归补充
- PC + Mobile 浏览器 8 套测试文件共 16 项：**16 PASS / 0 FAIL**，退出码 0（日志：本机 Temp `mes-e2e-r6.log`）。包括成品收发库存、QA、岗位授权、来料 OOS、固定批次双接口连续四轮稳定性。
- 修复后的来料调查筛选在 PC / Mobile 的真实候选结果中均无 `FG-` 成品批次，原辅料及包装批次仍可见。
- 前端 Vitest：**102 PASS / 0 FAIL**（28 个测试文件）；`npm run typecheck` PASS；Vite 正式构建 PASS，仅有大 chunk 提示。
- Maven 在停用旧 8080 JAR 后重新 `-DskipTests package` PASS；新可执行包含 Spring Boot Loader，约 81.9 MB。新后端 PID 25684 正常监听 8080；MariaDB 3306、Redis 6379、Vite 4174 均正常监听。
- 后端已恢复，请勿在 Windows 上对仍被 `java -jar` 使用的同一路径直接执行 Maven `package`；应先停目标进程、打包再启动，或先规划运行副本与构建产物分离（尚未实施）。
- 原历史演示批的 5 倍强度矛盾、岗位证据、eBR 归档仍为未关闭风险；本轮没有改动受控签名与业务数据。
