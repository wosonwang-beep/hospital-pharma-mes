# 产品成品链 eBR 阻塞只读根因核对 — 2026-10-11

BLOCKED / READONLY DIAGNOSIS COMPLETE / NOT ACCEPTED。此次仅诊断、读取现有归档、记录证据；没有新建、修订、提交、批准、发布模板，没有覆盖摘要，没有改代码/权限/迁移，没有重试失败下达，没有提交或推送 Git。

## 1. 规则、范围与实际身份

实际读取 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、已批准 DCP-BASIC-NO-AUDIT-VERSION-001、DCP-EBR-TEMPLATE-NAME-001 及当前绑定契约；业务权威仍为 v1.0.23 + 已批准补充。沿用原 checkout/main、8080/5173、本机 MariaDB hospital_pharma_mes_dev；DEV V057，外键检查1。此次没有执行数据库清理事务。此前明确路线/UI两轮额度已用尽，未扩大修复范围。

实际 information_schema 外键链：

- ebr_template_version.id=10 的 package_version_id=12 → proc_package_version.id=12；该版本的 package_id=12 → proc_package.id=12；根12.product_id=1 → md_product.id=1。
- proc_current_definition.id=12 的 package_id=12 → 同一工艺根12。模板10、历史工艺版本12、工艺根12、当前定义12是不同表的不同实体；数字相同不构成身份依据。
- 模板10：KCL30-EBR-20261007230246，业务版本1，EFFECTIVE，org1，template_name=NULL，process_package_id=NULL；approved_at=2026-10-07 15:02:46.731（SQL UTC）。历史工艺版本12为EFFECTIVE；工艺根12为ACTIVE、产品1；当前定义12为EFFECTIVE。

因此模板10历史上确实属于正确产品/工艺根，不能描述为选错无关工艺。它缺少的是新生产消费契约要求的直接 processPackageId 绑定。

## 2. 摘要失配的精确原因

原发布 form 存在于 ebr_form_def.schema_json.form。逐字段递归比较原发布 MIX_RECORD 与当前详情重建结果，唯一差异是新加入 `/canvasDocument = null`；原发布 JSON 没有此字段。字段/章节/规则的排序逻辑未改变，templateName=NULL 仍按原 canonical 条件省略，绑定仍为旧 packageVersionId，未发现数字归一化或数组重排造成该差异。

Git提交 3c24d2e20ea471dbf99a1f59f7e70a95d3ef2342（2026-10-09 23:23:38 +08）在 EbrDefinitions.load/formExtras/canvasDocument 中加入此字段重建。该提交前生成逻辑未加入此字段；提交晚于10月7日模板发布。不能仅凭Git历史确定当时实际运行JAR的精确提交，但保存的发布 JSON、源码变更及摘要复现共同确定该兼容性差异。

| 摘要对象 | SHA-256 |
| --- | --- |
| 模板10保存的原发布摘要 | deaf79582492a7ae474a72c39fb031e32b2fdfdd5e8947affdd3f23bd25b707f |
| 当前 canonical 辅助离线计算 | 155a47e5453d0e2eac9f622a7f4f3aa3899c9c74cca398043dfd5568564565c9 |
| 仅离线移除新增 null canvasDocument | deaf79582492a7ae474a72c39fb031e32b2fdfdd5e8947affdd3f23bd25b707f |

后两项是按当前 canonical 结构的辅助离线计算，不冒充服务器原始摘要输出。服务器真实下达请求返回422 LINT_FAILED / Published definition hash mismatch，trace 488987c0-e7cd-4a9d-8f34-f05bb8eb4ba1。EbrService.published:79 重建并核对保存hash，符合该证据。分类：历史发布内容重建的代码兼容性缺陷；此证据不支持“保存定义损坏”或业务产品归属错误。25张保留表内容也与07:46备份一致。

## 3. 即使摘要恢复，仍有第二道直接绑定阻塞

EbrQueryService.requirePublished 原样返回 published 内容；没有 legacy-to-current 适配。ProductionService.releaseBatch:85 要求 published 返回非空 processPackageId 且等于选定当前工艺的 processPackageId。模板10 canonical 仍返回 packageVersionId12，所以单独恢复旧摘要重建后，按代码仍会遇到 BATCH_DEFINITION_NOT_READY / Production process/eBR mismatch。此结论来自直接消费代码，没有为了复现而再次执行写请求。

批次230 KCL20261011001：org1/product1，计划1000ml，DRAFT/version0/process_snapshot_id=NULL。计划633：schema2/product1，APPROVED/version1，QC标准版本314，signature2021；QC根316/版本314已批准，signature2020。没有冻结处方/工艺/eBR可改写；此前下达请求没有 prescriptionId。当前 release API 的既有兼容分支允许无处方时选择产品匹配且有配方的当前工艺，并不是绕过新权限或修改模型。

实际 root12 定义：历史 operation29与当前operation332均为MIX；历史formula12与当前formula190均以1000ml为基准，物料1需20g，overage0。当前路线无需清场/设备绑定，未引入称量强制。产品1的处方1/3/4/5/6全部DRAFT，其中处方6绑定root12；没有可直接使用的ACTIVE处方，不能悄悄激活处方或换其他工艺。

## 4. 全部已发布候选与最小待批准方案

实际外键联合查询涵盖产品1全部7份模板：6/7/9/10为EFFECTIVE，全部旧版绑定，分别属于root8/9/11/12；251/252为DRAFT且直接绑定root12；291为DRAFT且直接绑定root1（当前定义也是DRAFT）。不存在EFFECTIVE的直接root12模板。不能靠切换6/7/9或发布其他人的草稿避开消费契约。

若业务负责人同意通过既有模板流程为新批次建立匹配定义，最小方案是：

1. 从模板10用既有 version API、copyDefinition=true、明确reason及当前versionNo，创建同模板家族的新业务版本2（新ID/DRAFT）。该家族当前没有另一份DRAFT。新版本正常绑定root12，历史模板10保持原样。
2. 既有 copyForCurrent 按 operationCode 将 MIX_RECORD 的 operation_def_id 从29映射到332；缺少对应工序即 PROCESS_BINDING_REVIEW_REQUIRED，不得人工猜测映射。保留原章节、组、字段、规则、签名规则、复核规则；本模板原发布无canvas内容。提供必需非空模板名称，名称由负责人确认，例如“氯化钾溶液电子批记录”。
3. 正常保存/校验、SUBMIT、独立 APPROVE、PUBLISH；APPROVE按新版本canonical计算新hash，PUBLISH校验该hash。独立批准人不能是创建人/提交更新人，沿用原角色/审计和状态机，不改授权。
4. 新版本发布并核对产品1/root12/工序332后，正常下达现有DRAFT批次230，选择root12与新模板ID。保留计划633、标准314、原签名与全部既有历史定义；无需重建产品、批次、标准、计划，无数据库迁移。

以上仅方案，尚未执行。新模板业务版本可以解决新批次缺少可消费定义的问题，但不会修复历史模板重建兼容性。后者若另获授权，应按发布来源/存储字段存在性保留历史 canonical，并对原保存摘要及新canvas/null摘要做兼容验证；不能全局随意省略null、覆盖旧hash或给旧模板补当前绑定。

## 5. 独立读取、保留证据和八项实际进度

已有打印归档：mes_print_artifact10条，formal=1为0条；全部保存pdf_hash与SQL SHA2(pdf,256)一致。实际 GET /printing/artifacts?businessType=WMS_RECEIPT&businessId=6 返回200；GET /printing/artifacts/27/pdf返回200，48808字节、%PDF-头，SHA-256 1f0550854d9277090fde597ecb2037c7992ee857551c61e950c3eea88783cc3d 与保存值一致。原归档只读取回，未生成新文件/归档。ebr_book_pdf、ebr_pdf_manifest、ebr_batch_snapshot及gxp_attachment当前均0条，因此历史eBR/PDF读取无实际样本，不能算PASS，也未创建测试历史。

25张保留表（原14张基础/产品/权限表、9张eBR定义表、mes_print_artifact与source_evidence）在单连接 READ ONLY / REPEATABLE READ consistent snapshot 中按原备份的 HEX(CAST(column AS BINARY)) 做逐行完整内容比较，25/25一致，显式ROLLBACK。初次诊断误用了HEX(numeric)，数值编码差异产生假变化，原失败诊断保留；改回备份原编码后无变化，不是放宽内容比较。正常登录会改变sys_user登录版本/时间及安全事件，未声称所有用户整行不变。物料16/FINISHED0/产品2/FK1；批次230仍DRAFT、计划633仍APPROVED、成品流水0。

| 固定项目 | 实际完成 | 真实阻塞/未执行 |
| --- | --- | --- |
| 1 运行及权限 | 原checkout源服务8080/5173、health、真实角色权限；安全18/18；DEV组织消费1/1 | 完整跨组织真实认证HTTP矩阵没有另一组织样本，未执行 |
| 2 产品质量标准与计划 | 标准316/314批准；计划633批准；真实403/401/冻结422保护 | 材料标准误选/其他产品全负向矩阵未全部执行 |
| 3 统一生产指令 | 原子建立批次230与schema2展示 | 下达被上述两道eBR条件阻塞；执行/OUTPUT未执行 |
| 4 成品库存 | 真实warehouse GET200空列表；PC/手机实际页只读通过 | 入库、确认、幂等重放依赖生产完成，未执行 |
| 5 成品QC/PDF/QA | 前置标准/计划批准有证据 | 成品取样、检验、报告新PDF、QA放行依赖库存/完工，未执行 |
| 6 出库 | 无下游假数据 | 出库、重放、超库存/并发依赖放行库存，未执行 |
| 7 追溯/历史 | 模板真实读取、25表保留核对；已有打印归档27原GET/hash通过 | 新批次冻结/追溯未产生；历史eBR归档0条不能验；旧打印均非formal |
| 8 页面 | 三个实际页面桌面/手机：列表6对、库存7对查询同线；详情390px无横溢/内部订单泄漏且单eBR入口，实际截图已查 | 全链全部页面及完整批准参考逐页比对未执行；详情0查询对不伪报通用query PASS |

因此只读根因已定位，独立库存/旧打印归档/目标页面检查已完成；全链仍BLOCKED / NOT ACCEPTED，没有新增修复轮次或全链PASS。

证据位于用户本机 Temp/mes-quality-plan-root-review-20261011/deployment-20261011，不提交Git：ebr-readonly-identity-fks.json、ebr-readonly-identity-and-candidates.json、ebr10-published-portable-forms.json、ebr10-portable-current-form-diff.json、ebr10-canonical-compatibility-experiment.json、ebr-final-process-archive-readonly.json、ebr-final-archive-and-prescriptions.json、historical-print-artifact-readonly.json、historical-print27-http-readonly.json、ebr-readonly-final-preservation-corrected.json（以及保留的初次编码诊断）。界面、源部署及权限证据见前一份[实际部署报告](PRODUCT_FINISHED_ROUTE_UI_DEPLOYED_EBR_BLOCKED_20261011.md)。
