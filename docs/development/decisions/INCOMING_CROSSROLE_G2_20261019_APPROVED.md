# INCOMING-QMS-CROSSROLE-002 — G2 精确原型确认记录（2026-10-19）

**确认来源：** 用户本会话明确回复“原型确认，继续开发”，针对上一条展示的收货逐批一键请验→QC确认两单→新取样任务入列表→取样员实取提交→仓管确认扣减→新检验任务待样品，以及本机 5174 四个页面入口。业务方向/可见原型确认，不等于真实数据库状态、法规签名认证或最终 G5 验收。

**本记录只批准上述可见页面流程的设计版本及其忠实集成方向。** 既有正式检验记录 T1 的独立已获批源和 READY FOR ACCEPTANCE 结论保持不变，未经确认的“样品接收以后原始检验执行/综合判定/报告编辑、打印”等页面未因本次批准自动获批。原型的浏览器会话角色切换、单库位库存及最多三位小数属于隔离技术替身，**不是获批的正式安全/数据库实现**，正式实现必须用受控后端合同替代，并继续走G3技术门禁。

## 精确原型文件锁定

源清单根目录：`D:\codex\_project\gmp\mes-demo`；本轮实测/哈希原件：`evidence/incoming-crossrole-sampling-g2-20261019/FINAL_G2_STATUS.json`，其 `source_hashes` 下九个源文件 SHA256 均与 2026-10-19 本次真实文件逐字节核对 **9/9 MATCH**（详见同目录的登记文件）。禁止此确认后在不修改锁版本的情况下继续编辑上述源文件并声称仍是同一批准原型。

- 入口 `http://127.0.0.1:5174/#/wms/receipts/list`：收货已验收布局不变，一批一键请验，生成后按钮「查看」，不弹窗。
- 入口 `http://127.0.0.1:5174/#/quality/incoming-crossrole/list`：QC查看已提交请验并补质量标准、计划取样量与方案，确认后展示两单，缺项/重复阻断。
- 入口 `http://127.0.0.1:5174/#/quality/sampling-outbound/list`：取样任务进入列表并展示批准数量和单位，检验员提交明细不扣，仓管员确认才扣一次，仍待验。
- 入口 `http://127.0.0.1:5174/#/quality/inspection-tasks`：QC提前生成的检验任务可在列表按样品名称/批号找到，样品未接收时显示待样品只读。

## 验证证据与实际边界
- `npm run build` vue-tsc/Vite exit0；
- `crossrole-browser.cjs` 7/7，`linked-sampling-browser.cjs` 5/5，`negative.cjs` 4/4，`inspection-wait.cjs` 3/3，合计 19/19 仅隔离浏览器逻辑；
- `visual-matrix.json` 6个页面×1440/1280/900/375=24张截图，DOM整页横向溢出0、已测QC控件锚点离群0、JS0；**不是完整 UIUX 审计器 PASS 或实际服务端权限/签名证明**。
- 样品接收→检验执行→复核签名→报告生成与打印这半条链本次仅有既存正式功能及静态设计，未形成本版完整批准的可操作 G2；属于后续单独锁定范围。

## DESIGN LOCK 范围与白名单
准予 G3 详细设计：请求标准延迟到 QC 确认、QC一次双单、可空待样品 task/sample 绑定、仓库受控取样出库负库存与幂等、跨岗资格签名、旧单据历史兼容和正式前端/后端映射。以下细节必须先设计校验一致：
- 既有 `qms_inspection_request.qc_specification_version_id/specification_content_hash` NOT NULL；
- 既有 `qms_inspection_task.sample_id` NOT NULL；
- 既有 SamplingTask complete 由取样员触发且立即变为 SAMPLED；
- 现有 WMS ledger 含 `material_lot_id/location_id/container_id/unit_id/delta_qty`；
- 隔离原型模拟三位小数/单库位，正式必须按现有批准基本单位和 BigDecimal 精确写入，防止真实库存不一致。
- 适用G3新增/变更要通过追加 Flyway、原业务权限/签名/审计与API RTM、MariaDB事务回滚和真实UI验证，不能修改已执行 V001–V066。

**阶段状态：G2 局部精确源码已获用户确认；G3 设计合同准备中；G3.5 未 READY，G4/正式 DB 迁移未开始。** 禁止擅自把原型测试量说成真实DB库存通过，也禁止覆盖已批准历史源码/数据。
