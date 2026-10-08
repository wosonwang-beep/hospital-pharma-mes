# 生产批记录册字段来源与模板使用

品种通过显式 productId / packageVersionId / ebrTemplateVersionId 映射不同规格；产品身份和既有工艺一致性校验不变。字段、工序和表单使用稳定 code。发布后不可覆盖；新版本另存草稿，由独立人员发布。旧批次没有冻结册模板时保持原 eBR，不自动补配。

|目录来源|真实来源与边界|
|---|---|
|FORM|既有冻结定义与运行态；只读选定 fieldCode、实际值、单位名称、来源、重复行和电子签名引用。实际填写/复核进入原表单。|
|PROCESS_INSTRUCTIONS|已冻结工艺路线和参数；未提供的 SOP、容差、配方含义不推断。|
|PRODUCTION_ORDER|原生产指令；不创建第二份可修改指令。|
|MATERIAL_ISSUES|服务端批次全部领料记录和真实物料批次；无来源的厂家明确不可提供。|
|CHARGES|原实际投料记录和批次，不再扣料，不由最终体积反推实际投水量。|
|CLEARANCE|既有清场记录与复核引用，正式归档另受既有有效签名/QA 门禁约束。|
|INSPECTION_REPORTS|批次检验申请及全部报告；不取前端当前分页。|
|ATTACHMENTS|指定且有权读取的受控原件。PDF 合并副本，PNG/JPEG 真实图像页；其他格式明确拒绝生成，不静默漏页。原件不改写。|

默认记录打印是生成真实 DOCX 后调用现有 docx4j/FOP；既有配置可显式选择 LibreOffice，分别验证。可在 FORM 目录绑定已发布 `EBR_PROCESS_FORM` Word 打印版本。可用字段为 `modeLabel/reportNo/formName/formStatus/definitionHash/signatureReference`，循环明细 `sequence/fieldCode/label/standard/actual/unit/source/occurrence`。仅白名单数据，无 SQL 或任意表达式；复用现有 DOCX 安全上传与模板生命周期。样例为合成数据，未伪造手写签名。

配制照片可见结构为：指令、标准处方量、实际投料量、批号/来源、操作人/复核人、两段清场和配液实际参数。系统标准与实际分开；检查组件不默认勾选正常。照片中的历史数值、勾选和手写签名没有导入。照片版式逐像素复刻和灌装/包装第二张照片未完成核验。

灌装/包装只有在发布册模板中将相应 FORM 设为 BATCH 才共享一个物理记录，内部可有多行。其他工序保留 OPERATION 范围。原记录的 owner operation、审核和签名关系保持不变。

整册包括封面、目录、真实记录表格、证据版本清单、页码、书签，正式册还附既有完整受控证据 PDF。浏览器预览、下载、打印共享同一 PDF。档案保留冻结模板/来源快照、摘要和原产物；旧版重打不重新渲染。正式生成复用前重新校验有效 QA 签名；无效签名不能生成新的正式册。

权限复用现有 ebr:template:view/create/publish、ebr:form:view、ebr:pdf:generate 及各来源所属权限；没有新增角色授权。批次基本读取另需 production:batch:view、master:product:view、master:uom:view。归档产物读取也重新校验来源访问权限。
