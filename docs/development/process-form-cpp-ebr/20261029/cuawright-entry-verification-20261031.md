# CUAWright 正式工艺入口定向复验

时间：2026-10-31 19:53–19:56（Asia/Shanghai，UTC+8）。执行者：Codex；功能诊断，非完整 UI 验收。

## 基线和范围

实际读取 AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md 及 cuawright-mes/SKILL.md。业务沿用 FINAL BASELINE v1.0.23 和现有有界批准，UI V2；本次只验证正式5173现有工艺列表、详情及表单设计器入口，不改页面或业务合同。累计修复25轮保留。本次无代码修复，无迁移、业务保存、签名、审批、发布或服务操作。

## 观察结果

1. `browser_status` 初始 browser_active=false。`inspect_url` 成功创建会话并显示登录页，说明“无活动会话”不能等同工具不可用。
2. 专用 `login_mes_admin` 读取已登记的Windows凭据，经正常登录表单返回 authenticated=true、登录HTTP200、身份HTTP200。任务文件不包含凭据；business_mutations_allowed=false。
3. 正式工艺列表显示7条。通过KCL30-制备工艺行“查看”按钮进入 `/process/packages/1?page=0&size=20`，不是直接地址代替入口验证。
4. 点击“工序表单”页签成功显示子表。OP01_RECORD的两行可按现有模板名称和编码区分：EBR-KCL30-BATCH-RECORD与EBR-KCL30-120L-PREVIEW-20261009。
5. 第一行“设计表单”进入模板496、第二行进入模板291；两者URL都携带 processPackageId=1、formCode=OP01_RECORD、returnTo=/process/packages/1，页面标题和模板编码分别匹配。
6. 模板496可切换CPP规则设计，显示当前表单23字段及无CPP规则说明。未新建或保存规则。
7. 模板291可打开“规则与签名”，显示现有QC复核岗位。此处只证明已有角色配置可见，不证明角色选择搜索、资格判定或签名执行。
8. 两个设计器都通过关闭按钮返回工艺页；最后再次切换工序表单成功，未出现未保存确认。关闭按钮实际名称为“关 闭”，前两次定位未找到，采用实际名称后成功；无断言变更。

此前Chrome自动化中列表/页签无变化在CUAWright中未复现。已证明本工具下这两条正式交互链路有效，仍不能据此断定旧工具事件失败的内部根因，也没有为旧现象修改业务代码。

## 截图证据

已实际查看以下1440×900截图，仅为局部截图观察，非完整UIUX矩阵或视觉PASS：

- `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1793447670147_process496-cpp-readonly-20261031.png`
- `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1793447700245_template291-source-entry-20261031.png`
- `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1793447753706_process-form-template-identities-cuawright-20261.png`

工具错误数组保留401记录；当前登录及身份200不证明全部请求零错误，本次未取得逐请求归属信息。各操作返回blocked_http_requests=[]；业务写保护仍开启，没有业务保存操作。未运行新的单元/数据库测试。

## 结论和剩余门禁

本次列表查看、工序表单切换、模板来源展示、两个对应设计器及关闭返回均有实际操作证据。真实保存重读、角色选择与错误恢复、全部权限状态、完整视口与审查manifest、精确原型批准、G3.5和G5仍未完成。整体IN PROGRESS / NOT READY，不设为READY或ACCEPTED。
