# 物料收货 SOURCE_A 开发合同与源码映射

状态：DEVELOPMENT / VALIDATION_DURING_DEVELOPMENT；不是最终验收或全局新基线。依据：docs/development/decisions/RECEIPT_DEVELOPMENT_ENTRY_20261018.md，用户最新明确原型生成后进入开发、在开发环节验证。v1.0.23及历史DCP原文件不修改。

## 一、前端源码复用
当前入口为 frontend/mes-web/src/views/wms/ReceiptLiveView.vue，对应既有 /wms/receipts、/create、/:id、/:id/edit 四个路由。业务模板直接适配 mes-demo 的 ReceiptFlowPrototype.vue（输入SHA256 f539cd92a68f1ba9f84073297d4fdf2177a880dfd6f25a6cb6e8655eb8ebedca），独立原型导航移除，继承正式MES外壳。保留原型源码及原正式页面文件，不重画业务布局。
receiptLiveRuntime.ts 为每个页面实例提供真实API与实体ID映射；不得引入seedReceipts、sessionStorage权限、假库存或随机生产批号。列表使用服务端page/size/keyword/recordStatus/supplierId/warehouseId，不照搬原型全量数组筛选。
选择器复用 WmsLookup / ProcessLookup / IncomingReferencePicker；写入使用useControlledRequest保持Idempotency-Key/If-Match和错误保值；按钮同时受auth.can和服务端allowedActions约束。

## 二、严格请求与兼容
新增客户端携带 inputContract=RECEIPT_SOURCE_A_V1。ReceiptCreate/ReceiptUpdate增加可选inputContract、arrivalDate、acceptanceText；原Java构造函数保留，旧接口调用不默认为新合同。
- lotNo：服务端预约的内部批号，保存后不可替换。
- manufacturerLotNo：新合同要求的生产批号，独立录入。
- supplierLotNo：仅供货方确有不同标识时提供，允许空；旧合同保留旧校验，历史值不迁写。
- arrivalDate：实际到货日期，新合同必填；旧空值不从createdAt/receivedAt回填。
- acceptanceText：独立验收说明，显式空字符串可清除；不借用reason。
- reason：修改/确认的操作原因，仍受原审计链控制。
- transport及五项明细检查：新合同草稿可null；确认必须true。旧合同草稿仍按原非null要求。
来源、日期、单位换算、仓库库位、确认事务、免验事件与放行逻辑继续复用WmsService及既有查询服务。

## 三、新增两个有界接口
GET /api/v1/wms/receipts/source-context?materialId=&supplierId=：沿用wms:receipt:view，调用MaterialQueryService.requireUsable及ApprovedSupplierQueryService.requireApproved，返回有效物料与已批准关系，生产企业不兜底成供应商名称。
POST /api/v1/wms/receipts/number-reservations：沿用wms:receipt:create；body为materialId/clientLineId，必需Idempotency-Key。严格拒绝未知JSON字段。SecurityConfiguration仅新增对应精确请求匹配，其他权限和denyAll不变。
原GET/POST/PUT收货与confirm路径不改。确认响应补充真实qualityStatus/inventoryStatus；MaterialLot投影通过既有receiptItem关联补充manufacturerLotNo，不复制到supplierLotNo。

## 四、编号与数据库
V065__receipt_source_identity_and_number_reservation.sql：只追加migration，已执行的V001–V064不修改。增加接收合同/到货日期/说明列并允许未知检查；增加组织编号规则表及预约表。预约含物料、稳定意图、编号、规则批准引用和绑定明细；组织下编号、意图分别唯一。
规则行通过FOR UPDATE串行分配，沿用MasterMutation幂等与审计；同意图重试返回原号，新意图不同号。草稿绑定预约但不创建库存；确认按原链创建批次/流水。已分配但放弃的号不回收。
具体prefix、datePattern、sequenceWidth、timezoneId、approvalReference、enabled来自受控规则。当前没有启用真实规则，不把原型示例当获批SOP。无批准引用或未启用时返回RECEIPT_NUMBER_RULE_NOT_APPROVED；历史编号冲突、规则容量耗尽均阻断。
当前实现序列按组织单调增长，不自动按日归零；若实际批准SOP要求日内重置、不同物料前缀或其他范围，必须按该具体规则完善后启用，不能以示例代替。

## 五、既有业务组件
请验提交到原有 /quality/inspection-requests，返回草稿，不自动submit/accept/取样/QA放行；标准选择来自真实已批准质量版本。打印使用BatchPrintButton(WMS_RECEIPT)，附件使用ReceiptAttachments，审计使用原有audit入口。页面不伪造审计行或已上传附件。

## 六、验证与待收尾
真实MariaDB服务集成与MockMvc权限边界、前端映射单测、真实5173/8080浏览器读取和编号未配置负例分别留证。集成测试的编号SOP引用仅存在于回滚事务构造数据，不是实际医院审批，规则未保留在DEV。
还需：实际批准的编号SOP配置；真实登录用户完整新建→确认→请验的正向链；真实PDF打印/附件上传；完整权限、免验与受影响回归及产品经理最终视觉验收。开发授权不等于这些项目已经通过。

## 2026-10-19 已批准并启用的内部物料批号规则
最新用户指定“年月+四位流水号”，按RECEIPT_NUMBER_YYYYMM4_20261019.md实施为YYYYMMNNNN。此段取代第四节原“组织单调序列/规则未启用”的开发时状态：当前组织按Asia/Shanghai自然月共用0001–9999，无前缀分隔符，跨月自动取新周期，既有行不重编号。V066只添加sequence_period，V065和历史批号保留。两个精确配置命令由既有iam:dict:view/update控制，经实际登录管理员调用、幂等和审计。已通过真实双批新增、保存、刷新、编辑、确认链，结果和剩余范围见RECEIPT_MONTHLY_NUMBER_20261019.md。此前历史报告不回填通过。
