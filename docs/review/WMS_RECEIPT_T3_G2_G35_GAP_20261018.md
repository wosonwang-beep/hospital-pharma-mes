# 物料收货详情 T3 — G2/G3/G3.5 集成差异核对（2026-10-18）

状态：**IN PROGRESS / G2 PENDING HUMAN APPROVAL / 正式 UI 集成禁止启动**。本记录是只读核对与交接准备，不替代批准 DCP/冻结基线，不构成验收。

## 1. 实际读取的依据
- `AGENTS.md`；`docs/development/MES_DEV_RULES.md`；`MES_TASKS.md`；`docs/PROJECT_BASELINE.md`。
- 正式业务权威：FINAL BASELINE v1.0.23 + 有界批准 DCP；UI：Global UI V2 / T1–T6；MES-008-R2 与 MES-008A-R2 在任务索引中均为既有 ACCEPTED，不得重做既有能力。
- `releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/tasks/MES-008-R2.md`（既有收货合同）。
- `D:\codex\_project\gmp\mes-demo\evidence\receipt-prototype-20261016\SUBMISSION.md`：仅设计效果已确认，**原型精确源码版尚未获用户 G2 批准**。
- T3 目标：`mes-demo/src/features/wms/ReceiptPrototype.vue` SHA256 `e7e6bdcf333d2ce4ced705f7c7df80fddac37df994c27fad97a039d0e99e6702`；
  CSS `receipt-prototype.css` SHA256 `c36c789538a1e9c2a84335347c2ff4af976b00e10a511905df8a1c89196d76b1`。
- 正式源码：`frontend/mes-web/src/views/wms/ReceiptDetailView.vue`、`frontend/mes-web/src/router/index.ts`、`frontend/mes-web/src/components/printing/BatchPrintButton.vue`；
  `backend/mes-wms/.../api/WmsController.java`、`application/WmsService.java`、`infrastructure/ReceiptEntity.java`；
  `backend/mes-qms/.../application/IncomingQualityService.java`。

## 2. 对照结果与不得擅自处理的合同差异
| 目标 T3 交互 | 现有正式业务 | 结论/处理 |
|---|---|---|
| 收货基础信息、独立物料批次 | `GET /api/v1/wms/receipts/{id}` 已返回明细和 materialLotId；新增/编辑/确认已有接口 | **复用**，不另建收货模型、表、权限或状态 |
| 已收货状态 | 正式记录使用 `recordStatus=APPROVED`，各 lot 独立质量/库存状态 | 仅在批准设计映射中指定中文显示，不创建新的“已收货”枚举 |
| 每批发起/查看请验 | 存在 `POST /api/v1/quality/inspection-requests` 和详情/列表；正式收货详情无入口 | G3 写明 lot ID/权限/过滤和异常恢复，G4 复用 QMS，禁止新建质量单据 |
| 发起请验简易弹窗 | 正式 createRequest 要求 `materialLotId`、`qcSpecificationVersionId`、`requestedQuantity`、`requestedUnitId`、`requestedPackageCount`、`requestedDate`、`priority`、`requestType`、`reason`，并做适用性/资格/数量校验 | **设计缺口**：原型只含说明，G3 必须指定字段选择、已批准默认策略与校验；禁止在 G4 静默硬编码值 |
| 批次质量抽屉 | 既有物料批记录、QMS 请求筛选和 QA 放行链；收货页面尚未整合 | G3 确定只读聚合字段/空态/刷新/权限；**不得把 QC PASS 当 QA RELEASE** |
| “验收信息”整句备注 | `ReceiptEntity` 仅有 `transportCheckPassed` 和各物料收货检查；没有相应自由文本持久化列 | **DESIGN CHANGE REQUIRED（若业务要求持久化）**；不能借用受控操作 `reason`、签名或审计字段。若仅为展示说明，必须在 G3 标清字段来源及非持久化含义 |
| 打印收货单 | 正式页面已接 `BatchPrintButton business-type=WMS_RECEIPT` | 继续复用统一打印中心，不发明新的打印 API/入口配置 |
| T1 列表、T2 新增/编辑 | 已有正式页面和合同，G2 原型只覆盖 T3 | **本次不得改 T1/T2**，除非另有获批设计锁 |

## 3. Gate 与执行证据
- G1 设计效果：历史视觉方向已获用户确认；非本次精确原型源码批准。
- G2：`SUBMISSION.md` 明记 PENDING HUMAN APPROVAL；2026-10-18 在原本机端口 5174 复启原型，运行 `node scripts/receipt-prototype-check.cjs`：**25/25 PASS**，日志见 `mes-demo/evidence/receipt-prototype-20261016/prototype-check.json`；G2 正式全维度 UI 审查/用户源码确认 **NOT RUN/PENDING**。
- G3：精确源码版 G2 未批准；业务字段/质量创建命令映射有缺口，**NOT FROZEN**。
- G3.5：不得生成标称 READY 的 Codex 交接包，**NOT READY**。
- G4：本轮未改正式前后端/数据库/迁移，正式 UI 接入 **NOT STARTED**。
- 静态检查：正式项目 `frontend/mes-web/npm run typecheck` **PASS**（exit 0）；非集成验证。
- G5：5173/8080 在本轮核对时未运行，真实业务 HTTP、MariaDB、打印、权限负例、截图对照和受控 UI/UX 审查均 **NOT RUN**；不得声称整体验收 PASS。
- 环境：3306 MariaDB、6379 Redis 处于监听；本轮不改持久化业务数据、不运行 Flyway、不清理/覆盖既有修改。Git 初始 `main` HEAD `8acfd8a` 且有大量其它任务未提交内容，保留原状。

## 4. 后续白名单与执行顺序
1. 人工在 `http://127.0.0.1:5174/#/wms/receipts` 对**精确 SHA 版本**确认；按仓库 G2 矩阵完成原型体验审查并固定设计锁。
2. 产品经理 G3 补 PRD/字段来源/请求 DTO/质量标准选择与数量单位规则/权限/错误态/RTM/设计锁；验收备注若需持久化，走有界 DCP 批准；不得修改原冻结基线。
3. G3.5 生成完整 `CODEX_DEVELOPMENT_TASK.md` 和 `g35-development-handoff.json`，列明复用现有正式 WMS/QMS/打印、不可重做 UI、测试断言/例外及源码哈希。
4. 仅在 G3.5 READY 后，由 Codex 将 T3 原型源代码适配正式路由和既有实体 API；保留 T1/T2，禁止第二套业务逻辑。
5. G5 运行真实 5173/8080/3306/6379 下的权限、逐批请验、QA 不越权放行、打印、幂等/并发、审计、页面 UI/UX、GxP 和受影响回归，并保留实际证据。只有最终人验收才可记 ACCEPTED。

## 5. 本轮写入边界
仅新增本核对文件，**未触碰**原型源码、正式业务文件、既有任务状态、Git 分支/提交、持久化 DEV 业务数据。此记录不能作为解除 G2/G3 门禁的许可。
