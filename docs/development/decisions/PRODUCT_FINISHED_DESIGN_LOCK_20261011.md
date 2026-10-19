> 2026-10-11 方案 B 替代说明：本文件中独立成品 lot / 待选 A-B / V054 实施草案内容属于已被替代的未执行历史。当前模型以 [PRODUCT_FINISHED_DIRECT_BATCH_B_20261011.md](PRODUCT_FINISHED_DIRECT_BATCH_B_20261011.md) 及 `docs/architecture/PRODUCT_FINISHED_CONTRACT.md` 为准。旧 UI 源码哈希证据保留；方案 B 后续实际文件摘要在本机 FINAL_SOURCE_MANIFEST.json。OpenAPI/RTM 全量同步与一致性审查尚未完成，禁止据旧草案执行。

# PRODUCT_FINISHED_DESIGN_LOCK — 2026-10-11

APPROVED / FROZEN。需求及明确批准来自父对话01a115d2-8ace-7574-9a08-ac48dfc970cc；精确模型为DCP-PRODUCT-FINISHED-ROOT-001。用户本次明确限定现有页面字段修复，不重构页面；不是新的/实质重设计页面，不创建无关原型。

基线：v1.0.23+批准DCP；UI V2。生产质量列表仍T1、创建编辑仍现有T2、详情仍T3；现有页面模式、区域顺序、宽度、按钮、路由、权限、颜色/间距不变。查询/表单标签与控件同水平，窄屏整对换行；1440/1280/900/375验收留待真实升级服务时完成。

准确字段顺序：既有mainBatchId“生产批”实体选择→productId“产品”只读显示（来自所选同org batch）→qcSpecificationVersionId“产品质量标准”实体选择（只同产品APPROVED版本）→原balanceRules/reason；禁止finishedMaterialId选择。批次变更立即清空标准并加载派生产品；不能前端编辑/发送productId，保存严格按V2操作契约。更新batch不可重分配。列表仅用productId替代成品物料列，详情旧模式保留原历史标签/引用。

QC标准原页面维持模板/样式；创建主体明确MATERIAL/PRODUCT，互斥显示现有材料或产品实体选择器，主体创建后不可换；版本/检测项/独立审核/签名控件不变。枚举是受控标准主体合同，不把五类QC对象字典当标准主体。

主写白名单：相关QC/QMS/执行/WMS/生产/QA/EBR/追溯/打印现有来源读取与boot适配；独立成品实体/存储；唯一新增V054（实施前占号核对）；两处RealisticProductionFinishedSeedIT和新隔离/回滚测试；质量/成品现有前端字段/选择器及直接契约测试；本DCP/API/DB/RTM/索引/验证记录。禁止其他业务/CI/.env/application-local.yml/application-ci.yml/旧迁移/触发器/账户/权限/旧release/历史归档修改。

复杂迁移与跨模块实施预算60分钟，最多2轮自动修复，失败分类与证据保留。当前源hash及最终源hash见旁置manifest；设计批准不代表编译/迁移/真实UI通过。DEV迁移、重启及最终验收必须父任务审核。
