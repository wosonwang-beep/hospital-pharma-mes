# 物料收货：原型转正式项目开发及第一轮真实联调

日期：2026-10-18。执行者：ChatGPT 直接开发与自审，DC在用户Windows开发电脑执行。状态：**IN PROGRESS / 已进入正式开发 / 尚未最终验收**。
依据：用户最新明确“原型生成后就进入开发环节”，验证并入开发；本任务的有界记录为 docs/development/decisions/RECEIPT_DEVELOPMENT_ENTRY_20261018.md。旧G2历史不倒填PASS，不把本轮开发授权伪记为全部质量验收。最终质量、历史留存、组织隔离与放行规则不降低。

## 1. 本轮不是原型迭代
原型列表/新增/编辑/详情早已生成。本轮实际把ReceiptFlowPrototype.vue的业务模板适配到正式ReceiptLiveView.vue；收货四个正式路由均已切到该组件，复用正式导航壳、真实API、权限、受控请求、实体选择器、打印与附件组件。保留原设计的明细表、逐批检查、生产企业/生产批号等字段，不重画业务页面。
前端不再包含原型固定业务数组、sessionStorage业务存储、浏览器权限授权或模拟质量状态。源码输入及映射见 evidence/RECEIPT_DEVELOPMENT_20261018/frontend-source-map.json。

## 2. 已实施的正式后端
- ReceiptNumberingService提供组织内串行、稳定意图、幂等、只读编号预约；预约独立于库存，收货保存绑定明细，放弃后不回收给其他业务。
- 生产批号manufacturerLotNo与供货标识supplierLotNo分开。新接收合同供货标识可空、生产批号必填；旧记录值和旧构造函数保留，不批量改写历史。
- 生产企业由有效供货来源校验，确认时复核并冻结；MaterialLot返回通过原有收货明细关联追溯的生产批号，不冒用供应商批号字段。
- 新合同支持独立arrivalDate/acceptanceText；未知检查可存草稿，确认仍要求全部通过。来源/效期/单位/库位/版本/审计与原有免验事件链沿用原服务。
- SecurityConfiguration仅增加两个新具体请求路径与既有wms:receipt:view/create权限映射。没有新增角色授权或放宽其他API；原denyAll保留。

## 3. 数据库与运行环境发生的实际变化
新增V065__receipt_source_identity_and_number_reservation.sql，已由Flyway应用到本机 hospital_pharma_mes_dev；当前版本065，65个迁移校验通过。旧Flyway文件不改。新增接收合同/到货日期/说明字段、未知检查空值能力、受控编号规则和预约表。
测试创建的物料、供货关系、规则批准引用均在回滚事务中，未留下实际获批准的编号规则。无清库、关闭外键、删除历史或Git提交。
本机正式开发环境已启动：5173前端、8080后端；继续使用3306 MariaDB和6379 Redis。初次从backend目录启动未读到项目根.env而缺JWT标识，改用根目录既有.env后正常，未改或输出密钥。Windows运行jar文件锁导致一次重打包失败，仅停止本轮自己启动的后端、完成构建后重启，证据保留。

## 4. 最终定向测试与证据
| 范围 | 结果 | 证据/边界 |
|---|---|---|
| Spring服务、真实MariaDB事务与MockMvc权限路径 | 9 tests，0 failure/error/skip | ReceiptSourceIntegrationIT；实际业务服务/数据库，CurrentPlatformContext为测试替身；规则fixture随事务回滚 |
| 正式前端API映射单元测试 | 9/9 PASS | receiptLiveRuntime.test.ts；HTTP为单元测试替身，不冒充真实请求 |
| 正式前端TypeScript与打包 | PASS | frontend-build-final.log；原有大bundle告警保留 |
| 正式5173/8080浏览器 | 6项PASS | 真登录、真API：列表、详情、表格/逐批核对结构、实体控件宽度、缺批准规则阻断、无未捕获JS异常；没有完成实际新收货正向写入 |
| Flyway | V065已应用，65校验通过 | backend-integration.log、backend-run.log |

后端9项涵盖：编号稳定与不同意图区分；未启用规则拒绝；两行未知检查保存且零库存；确认创建两批与流水、幂等不重复、待验/BLOCKED；未经预约批号拒绝；保存后身份不可替换；合同不可降级；精确HTTP授权；只读预约/未知POST拒绝。确认测试还验证了物料批次投影的生产批号。
本轮真实浏览器主动发现并修复：新增预约POST未登记权限路径产生403；重复main和面包屑；WmsLookup外层span被误作固定宽度标签。修后原断言通过，不用放宽权限或跳过断言解决。已实际查看正式新增截图，实体选择框与业务表格保持可用布局。

## 5. 实际修改文件（主要）
frontend/mes-web/src/views/wms/ReceiptLiveView.vue、receiptLiveRuntime.ts、receiptLiveRuntime.test.ts、live/三份原型业务样式；src/router/index.ts。
backend/mes-wms/src/main/java/.../domain/WmsCommands.java、ReceiptNumberingPolicy.java；application/WmsService.java、WmsViews.java、ReceiptNumberingService.java；api/ReceiptSourceController.java；infrastructure/ReceiptEntity.java及两个编号实体/Mapper。
backend/mes-security/.../SecurityConfiguration.java（仅两个具体路径映射）、backend/mes-boot/.../V065__receipt_source_identity_and_number_reservation.sql、ReceiptSourceIntegrationIT.java。
新增工具脚本、开发约定、实际合同说明、执行报告与MES_TASKS当前状态。其他原有未提交文件保留，不提交或推送Git。

## 6. 未完成项，不再退回无限原型检查
1. **实际批准编号规则尚未配置。** 当前真实预约请求已正确进入业务服务并返回RECEIPT_NUMBER_RULE_NOT_APPROVED。服务已开发，但没有使用原型示例冒充SOP；需明确实际prefix/日期/序列范围/位数/时区/批准引用后启用。实际正向浏览器新建链因此尚未完成。
2. 正式请验、打印、附件已接回既有组件/接口，但真实PDF生成、附件上传、新收货到请验的正向链仍需联调。不能以组件显示或入口存在称整链验收通过。
3. 受影响旧WMS/免验全量回归、并发/跨组织完整负例、产品经理最终视觉核对与发布验收未全部完成。当前是可继续验证的正式开发版，不是上线或最终ACCEPTED。
详细实施合同见 docs/architecture/RECEIPT_SOURCE_A_DEVELOPMENT_20261018.md；证据根目录 docs/review/evidence/RECEIPT_DEVELOPMENT_20261018/。
