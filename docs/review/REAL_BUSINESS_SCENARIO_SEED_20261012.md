# 真实业务格式 DEV 场景数据 — 2026-10-12

状态：**PERSISTED / UI-READBACK PASS / BUSINESS-NAMING PASS / NOT PRODUCTION EVIDENCE**

## 目的
为医院制剂 MES 正式 5173 / 当前 DEV 数据库建立可串联的业务场景数据，用于页面、查询、状态、成品链和后续集成测试。数据按真实医院制剂业务格式构造，不冒充医院历史实际发生记录，也不作为生产/GMP正式证据。

## 本轮场景
产品：氯化钾溶液（KCL30，30ml:3g/瓶）。

冻结工艺按 1000ml 批量构造：
- 氯化钾 100g；
- 纯化水 900ml；
- 工序：配制 → 过滤灌装；
- eBR：氯化钾溶液配制电子批记录；
- 成品标准：氯化钾含量 9.5%～10.5%，场景结果 10.0%。

建立三种生产状态，便于页面测试：
- 批次 323 / KCL-20261012234332-D：DRAFT；
- 批次 324 / KCL-20261012234332-R：IN_PROGRESS；
- 批次 325 / KCL-20261012234332-F：QA_RELEASED，finishedQualityStatus=RELEASED。

完整批 325 串联：
生产 → 原辅料预留/出库 → 称量/复核 → 投料 → eBR填写提交 → 产出/损耗 → 物料平衡 → 生产检验 → 成品入库 → 成品请验 → 成品取样 → 成品检验 → 成品报告批准 → QA放行 → 成品库存 → 发货出库。

## 关键持久化对象
- 工艺包：577 / KCL30-INT-20261012234332；
- eBR模板：478 / KCL30-EBR-20261012234332 / EFFECTIVE；
- 成品入库：125 / FG-IN-20261012234332 / CONFIRMED；
- 成品请验：107 / FG-IQ-20261012234332 / COMPLETED；
- 成品检验报告：97 / FG-RP-20261012234332 / APPROVED；
- QA放行决定：248；
- 发货出库：27 / FG-SHIP-20261012234332 / CONFIRMED；
- 发货接收方：门诊药房；
- 成品当前在库量：695ml（995ml产出 - 300ml发货），质量 RELEASED / 库存 AVAILABLE。

## 验证
- DEV Flyway 62 migrations validate PASS。
- 场景事务第一次因 eBR definition 未显式 schemaVersion=2.0 被门禁拒绝并整体回滚；独立API复核确认无残留。
- 修正为显式 schema2 后重跑：1/1 PASS，Maven reactor BUILD SUCCESS。
- 8080 API 独立回读批次323/324/325、入库、请验、库存、发货、工艺、eBR均HTTP200。
- 5173真实浏览器回读：
  - 生产批列表可见三批；
  - 成品库存可见批325、氯化钾溶液、695；
  - 成品入库、请验、报告、发货列表均可见对应编号；
  - 浏览器 pageerror=0。
- 新场景业务标识字段（Code/Name/No/reason/remark/接收方/取样位置/方法/sourceRef/policyVersion）禁止词扫描：0 命中。
- 合法业务枚举 TEST_SAMPLE 表示“检验样”，属于质量业务语义，不是测试数据标记。

## 边界
本轮持久化场景通过真实业务服务、数据库约束和状态机生成，但测试入口使用受控 CurrentPlatformContext / ReauthenticationPort fixture 来构造不同岗位和签名上下文。因此这些记录适合 DEV 页面与集成测试，不得解释为真实人员完成的正式电子签名或医院实际生产证据。正式全链验收仍需真实登录/真实二次认证的端到端流程。
