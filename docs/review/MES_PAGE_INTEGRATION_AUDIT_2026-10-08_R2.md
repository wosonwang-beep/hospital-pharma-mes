# 医院制剂 MES — 逐页真实集成审查（2026-10-08，本轮）

**环境：** 本地 `D:\codex\_project\gmp\hospital-pharma-mes`，主分支 `main`，MariaDB 3306、Redis 6379、Spring Boot 8080、Vue 5173。  
**执行原则：** 浏览器真实登录 / 真实 API，读取真实批次；不得直接修改已签名/已放行历史或绕过受控状态机。

## 1. 完整追溯（T4）

- 实际批次 `20261007-KCL-001`：**71 个节点、81 条关系，未发现悬空边**。供应商、收货、检验、称量/投料、生产、成品检验、放行与发货的实际证据可连通。
- 原界面一次性展示 71 节点、英文内部关系码，较难阅读且引用名称会触发大量并发请求。
- 已在 `TraceView.vue` 改为六个业务环节摘要；关系图按需展开，**每批最多加载 18 个节点**，可继续分页展开，保留原始节点、边、证据和来源跳转。
- `traceRelationLabels.ts` 将常用关系改为中文文案，原始关系码保持不变。
- 真实浏览器验证：展开 → 显示 18 节点 → 继续到 36 节点 → 点击来源详情 → 跳回生产批，关键交互成功。

## 2. 来料 OOS 调查与 QA 放行阻断

- 开发库实际存在两条 `RM-KCL` 原辅料 OOS 调查，均为待调查，检验不合格；原始失败证据/检验执行能解析到现有记录。
- 读取待调查氯化钾批 `RM-KCL-20261007223208-02` 的 QA 放行审查：服务端明确报告 **未关闭调查 + 缺少有效已批准检验报告**，库存禁止使用，且没有签名放行操作。
- 已修 `IncomingDocument.vue`：`INVESTIGATION_OPEN:<内部ID>` 按错误码族显示为“存在尚未关闭的来料调查”，未删后台原始证据。
- 已修 `IncomingListView.vue`：列表标题为“调查编号”，超长 OOS 编号以可读形式显示，完整编号保存在元素标题及详情中。
- 对历史 OOS 自动生成的英文情况描述，只识别为演示数据质量问题，不覆写受控异常记录。

## 3. 跨账号引用缓存隔离

- 原 `referenceCache.ts` 全局按字段+ID缓存业务名称，存在账号/组织切换后短暂复用旧会话标签的风险。
- 已加 **orgId/userId 缓存作用域、登录/退出清理、旧请求 generation 失效和并发 promise 安全去重**。
- `ReadReference.vue` 对身份/组织/权限变化重新解析，避免页面组件跨账号保留旧名称。
- `referenceCache.test.ts` 覆盖重复请求去重、多用户/组织隔离、旧请求返回不得污染新会话，共 3 项通过。

## 4. 实测

- 前端 `npm run typecheck`：PASS。
- 前端 `npm test -- --run`：**28 文件 / 101 项 PASS**。
- 后端负向用例 `FinishedReleaseIT#actualLateOriginalFailOpensOosAndBlocksFinalQaWithoutOverwritingOriginal`：**1 PASS，0 FAIL/ERROR/SKIP，Maven BUILD SUCCESS**。
- OOS 真实业务路径：调查列表 → 调查详情 → 物料批详情 → QA 放行审查，接口均 200，QA 放行按钮不可用。
- 未改变 MariaDB schema、Redis 实例、已有签名或放行决定；本轮未执行任何 `@Commit` Seed。

## 5. 未关闭的 HIGH 问题（保留阻断）

1. **已放行演示批的产品标示规格与冻结处方浓度不一致（5倍）**：参见 `MES_DEMO_STRENGTH_FORMULA_INCONSISTENCY_2026-10-08.md`；当前八项 QA Gate 不包含经过药学批准的结构化规格/处方一致性校验，需正式 DCP / QA 规则和负向测试后实施。
2. **历史演示批次人员岗位与 QC 检验/仓库确认职责不一致**：参见 `MES_DEMO_ROLE_EVIDENCE_INCONSISTENCY_2026-10-08.md`；后续生成器已按岗位调整，历史不可原地改写。
3. 现有 OOS 随机自动单号与英文机器描述可读性仍需后续受控编号/提示语治理；**禁止直接改写历史单号**。

**Git：** 所有改动留在本机 `main` 工作区，未提交、未推送。
