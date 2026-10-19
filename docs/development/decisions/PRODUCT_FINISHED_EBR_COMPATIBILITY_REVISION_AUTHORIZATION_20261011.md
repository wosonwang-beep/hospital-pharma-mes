# eBR 历史摘要兼容与新版本处理设计 — 2026-10-11

APPROVED / FROZEN。来源：设计负责人在源线程01a115d2-8ace-7574-9a08-ac48dfc970cc审核只读根因后明确批准A–D，最多两轮定向实现/验证；不授权扩大权限或改写历史。

## 1. 已读依据和复用

AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、BASIC_CURRENT_DEFINITION_CONTRACT.md、DCP-EBR-TEMPLATE-NAME-001-APPROVED.md及只读根因报告。权威仍为v1.0.23和已批准补充；原冻结文件/发布摘要/签名/快照不变。复用现有eBR版本API、状态机、独立批准、发布、生产下达及原八项验收。无新业务字段、表、接口、角色授权或UI设计。

## 2. 确定的兼容投影

历史packageVersionId绑定保留。仅当历史表单保存发布schema为schemaVersion=1.0，含匹配formCode/schemaVersion的form对象，且该对象不存在canvasDocument时，规范化输入省略重建器新增的null canvasDocument。以保存schema的字段存在性判别，不依赖模板ID/时间阈值/预期hash，不使用不断删字段直到摘要匹配算法。排序、其他字段、表单及绑定均原路径完整计算。

新processPackageId绑定定义一律保留canvasDocument，包括显式null；历史保存schema已有该字段时也保留，包括null及真实内容。非null重建canvas永不省略。缺少匹配保存schema或schema非法不能兼容省略。原content_hash绝不改写。旧真实样本原hash、新显式null、新真实canvas、历史新增canvas及字段/canvas篡改拒绝均需回归，published完整性门禁不变。

## 3. 新版本和语义

真实FK：模板10→历史工艺版本12→工艺根12→产品1。29/332全部业务属性一致：配制混合、顺序1、无前置、完成规则/角色/设备空、清场0、IPC空、参数0条；历史/当前配方均1000ml/物料1需20g/超量0。模板仅MIX_RECORD，唯一必填MANUAL STRING观察字段OBS，无单位、默认/校验表达式、选项；无章节、规则、签名/复核规则或其他数据源引用。

用既有copyDefinition=true从10创建同家族版本2/DRAFT，直接root12绑定，按原功能映射operation29→332，补名称“氯化钾溶液电子批记录”，正常保存/lint/提交/独立批准/发布。保留10全部内容/hash，不用251/252，不重建工艺/产品。230仍DRAFT/version0/无快照，正常选择新ID下达；633/314及原签名不改。语义或其他引用不一致立即停止，不改内容凑验收。

## 4. 账号依赖、写范围、执行界限

只用现有授权账号。预查只有admin/SYSTEM_ADMIN持有ebr:template:approve，正常独立门禁禁止其批准自己创建/提交的版本。可准备新DRAFT及提交，但没有另一现有授权批准人时发布/下游为DEPENDENCY_NOT_READY；兼容代码可独立继续。不得给QA临时/永久加权限、伪造审批或直接改状态。

源写范围：EbrDefinitions.java和定向回归测试/固定样本；本设计、当前绑定契约补充链接、结果报告、MES_TASKS.md。备份和真实模板证据只留本机。原checkout/DEV/Java21/唯一8080，无迁移、额外DB/端口、Git提交推送。最多两轮；同失败定向修复后复现即停止。阶段先报告再继续独立工作。

## 5. 验收边界

兼容测试不代替真实链。部署后检查旧hash/内容不变、新模板完整性；发布后继续原八项OUTPUT、独立入库/QC报告/QA、发运、幂等并发。历史eBR归档0条为无样本/不适用；27仅非正式打印归档只读验证。READY/ACCEPTED以实际门禁和人类确认决定。

执行授权边界记录：本文件为设计负责人批准的技术设计记录。模板业务写入命令被自动审批在启动前拒绝，理由是可识别的直接人类授权仅为原数据库清理；不得将设计负责人消息冒充已通过的人类业务写入授权，需原用户直接确认。不改变本设计的独立审批与禁止扩大权限要求。阶段结果/执行状态仅MES_TASKS.md。
