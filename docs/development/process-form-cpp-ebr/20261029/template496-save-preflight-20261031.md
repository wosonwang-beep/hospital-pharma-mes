# 模板496两次受控保存：预检与待执行方案

时间：2026-10-31 20:03:16起，20:06:45完成只读核对（Asia/Shanghai）。普通UI窗口截止20:13:16；本轮无代码修复，累计25轮保持。

## 基线与修改范围

用户在具体两次保存方案后要求“继续开发”。沿用业务产品经理技能、FINAL BASELINE v1.0.23及已有CPP/eBR批准；实际读取AGENTS、MES_DEV_RULES、MES_TASKS、PROJECT_BASELINE相关条目、BASIC_CURRENT_DEFINITION_CONTRACT、数据库验证策略、business-product-manager与cuawright-mes技能。此次只修改本报告、MES_TASKS、continuation及Planning进度；不改业务源码、正式设计、迁移或工具配置。保留用户已有代码修改。

## 已完成预检

- CUAWright已有会话仍活动，直接读取496设计器成功；页面显示草稿V1和保存草稿按钮，表单属性的名称输入可见，未修改输入。
- 原生DEV数据库在显式READ ONLY事务内SELECT、结束ROLLBACK：模板496，组织1，编码EBR-KCL30-BATCH-RECORD，名称氯化钾溶液批生产记录，status=DRAFT，业务version=1，version_no=2，definition_schema_version=2；approved_by/approved_at/content_hash均NULL。
- 表单584，form_code=OP01_RECORD，form_name=称量与投料记录，schema_version=1.0，status=DRAFT，version_no=1；存储schema为有效JSON，未含Canvas文档属性。模板definition schema2与表单schema1.0为不同层级，不据此转换表单schema。
- 签署6条、复核6条，角色ID及名称快照缺失数均0；与当前有效角色进行精确字节比较后，变化/不可用数各0。
- 第一次字符串比较SELECT因两列排序规则不同报1267；仅将本次只读查询改为二进制比较后成功，没有更改数据库排序规则、结构、数据或产品代码。
- 未取得完整定义基线与全部关联快照，因此不能称“保存前全部条件已通过”；正式写入前仍须完整抓取并深比较。

截图：`C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1793448372411_template496-formname-preflight-20261031.png`，1440×900，已查看；只证明当前属性显示，不代表完整UI审查。

## 确定的执行能力缺口

CUAWright返回mutating_requests_allowed=false。只读核对本机server_local.py的_route_guard与click_control确认：非认证业务写请求和名称含“保存”的按钮在此模式下受阻；现有CUAWRIGHT_ALLOW_MUTATIONS=1会全局放开写请求，现有工具没有两次定向放行接口。本次没有点击保存、没有试图用其他工具或直连API绕过保护，没有改变该开关。

## 可供批准的最小定向通道

拟为CUAWright增加单次会话内的受控验证能力；这属于工具能力变更，尚未实施或授权启用：

1. 仅本机5173、仅PUT `/api/v1/ebr/templates/496`，最多两次实际发出；其他业务写请求继续阻止，不改全局写开关，不自动重试。
2. 写入前重新确认模板496仍DRAFT、业务V1、完整定义、versionNo、EDIT及更新权限；只允许表单OP01_RECORD的formName从“称量与投料记录”变为“称量与投料操作记录”，第二次恢复。其他业务定义必须深比较一致；角色schema/快照、Canvas、CPP、签署/复核及绑定变化均停止。
3. 必须由真实前端保存按钮产生请求，保留现有原因、幂等键及If-Match。第一次采用现时versionNo，第二次仅采用首次保存后最新版本；不能硬编码旧版本。
4. 第一次成功后离开、重开并读回，确认仅名称和预期版本/审计元数据变化，再允许恢复；恢复后再次重开读回。
5. 409/403、状态改变、其他人修改、意外差异或结果不确定时停止自动写入，只读核查。不得将整个旧快照覆盖回去；不得以新幂等键盲目重交。
6. 两次成功保存预计使模板versionNo由当前2递增到4（执行时以重新读取值为准），业务V1保持，新增两次保存审计；子项元数据/版本也可能更新。恢复名称不等于无痕回滚。

执行前还须核实定向通道具备完整请求/响应、基线深比较和停止保护；如不具备则不写。不开新版本、不提交/审批/发布/签名、不改变权限、不新建业务记录、不执行新迁移。

## 结果

本轮只有只读UI与数据库预检，无业务保存、源码修复、数据库写入、迁移、服务或配置修改；未重跑单元/集成测试。业务闭环的真实保存与恢复尚未执行，完整UI/G3.5/G5未完成。IN PROGRESS / NOT READY；本报告不是对工具变更或全局放开写入的批准。
