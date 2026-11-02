# 模板496真实UI保存、重开与恢复

执行窗口：2026-10-31 20:50:46–21:00:37（Asia/Shanghai）；原10分钟截止21:00:46，用户中途要求自行接入未重置窗口。21:00:37已关闭本次浏览器及标准MCP客户端，随后仅整理既有证据。

## 基线、批准与实际修改

重读AGENTS、MES_DEV_RULES、MES_TASKS、business-product-manager和cuawright-mes技能。保留原v1.0.23及有界DCP/UI V2，保留全部历史修复计数。原两次保存批准继续有效；本轮MCP发出精确原生确认后，用户明确回复“确认执行这两次保存”，客户端将该人类答复传回MCP elicitation，20:58:13范围建立成功。没有程序自动批准或跳过确认。

宿主旧工具目录仍不能发现新接口。用户要求自行接入后，新增本机工具目录`mes_client_session.py`，使用标准SDK ClientSession与已登记的同一server_local.py通过stdio连接；实际发现15工具并读到新版范围状态。客户端自身不操作浏览器/HTTP，不读取凭据，不更改全局写保护，全部业务操作继续经过已验证的MCP工具和route guard。安全关闭旧CUAWright浏览器后复用同一本机持久会话；专用登录工具初次登录/后续会话复用均通过身份HTTP200。

初版客户端的大JSON参数受Windows终端约511字符行长度影响，JSON解析失败，尚未调用范围工具或发出业务PUT；仅修客户端为读取自身evidence目录下的JSON参数文件，再经同一MCP协议传递。无其他路径绕过；未改server_local.py、MES业务代码或验收断言。保守累计31轮：原29 + 客户端接入30 + 长行参数传递修复31；不同于原业务失败，不清零。

## 保存前核对

新版export_mes_detail成功返回完整对象，complete=true、redacted_paths=[]。模板496为DRAFT，业务V1，versionNo2，definition schema2，OP01_RECORD位于forms[0]，名称“称量与投料记录”、23字段、无持久Canvas文档。allowedActions含EDIT，真实UI显示可用保存按钮；审批人/审批时间/内容摘要NULL。

只读数据库核对仍为DRAFT/versionNo2；6条签署、6条复核的角色快照与当前有效角色一致。完整定义基线保存在下列导出响应中；没有以DOM摘要或mock代替完整对象。请求基线按现有definitionForSave合同只去掉服务端角色快照输入，保留全部其余业务值；guard对实际请求逐项检查。

## 实际业务结果

| 步骤 | 真实结果 |
|---|---|
| 第一次UI保存 | 20:58:24.851证据，PUT HTTP200，versionNo2→3，名称改为“称量与投料操作记录”；新GET读回校验通过，范围剩余1 |
| 离开并重开 | 关闭返回工艺，点击工序表单及496对应行设计入口；20:58:49.622页面证据含新名称、正确496/OP01_RECORD上下文 |
| 第二次UI保存 | 20:59:17.551证据，保存前再次GET确认无并发变化；PUT HTTP200，versionNo3→4，恢复“称量与投料记录”；新GET读回校验通过 |
| 再次离开重开 | 关闭→工序表单→496对应设计入口，20:59:35.244页面证据确认原名与正确上下文 |
| 独立完整比较 | 对初始完整GET与第二次保存后GET逐项比较，仅允许目标顶层versionNo及versions[id=496].versionNo由2变4；其余完整响应严格一致，业务定义全部恢复，业务V1和DRAFT保持 |
| 保护终态 | 21:00:37状态显示全局mutating_requests_allowed=false、范围active=false、attempts=2、remaining=0、state=exhausted；随后正常关闭浏览器/客户端，exit0 |

没有第三次写入、没有创建版本、签名、提交、审批或发布，没有删除审计或整份旧快照覆盖。恢复业务名称不是无痕回滚：并发版本实际增长到4；保存审计及子项元数据按原合同正常保留，本次没有单独查询审计表计数，不将应产生两条审计写成独立实测结论。

## 原始证据

均位于`C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/`，不含凭据、token或请求鉴权头：

- `1793451436838_native_mcp_export_mes_detail.json`：完整保存前GET。
- `1793451493512_native_mcp_arm_mes_write_scope.json`：真实原生确认后范围建立。
- `1793451504851_native_mcp_perform_mes_scoped_save.json`：第一次PUT200及独立GET结果。
- `1793451529622_native_mcp_click_control.json`：第一次保存后通过工艺入口重开。
- `1793451557551_native_mcp_perform_mes_scoped_save.json`：第二次PUT200及独立GET结果。
- `1793451575244_native_mcp_click_control.json`：恢复后重开UI。
- `1793451575248_template496-restored-roundtrip-20261031.png`：恢复后1440×900截图，已按原图查看，只作本次局部观察。
- `1793451637075_native_mcp_browser_status.json`：全局只读及额度耗尽终态。
- `1793451637134_native_mcp_close_browser.json`：安全结束。

已有工具错误数组仍保留401文本，登录和详情成功不能证明全部网络请求零错误。本报告不宣称零日志或完整视觉验收。客户端Python编译exit0；原45项策略测试对应的server/helper未改，本次没有无理由重跑该套件。没有新数据库迁移、服务或Git操作。

## 业务产品结论

“工艺→工序表单→对应设计器→展示名称保存→原入口重开→恢复→再重开”本次真实闭环已验证，工具接入与一次性确认不再是此链路阻塞。仅此名称往返不能证明新字段/CPP配置/角色变更的完整保存、生产执行超限动作、多角色权限、失败恢复或全视口UI审查通过。精确整原型/G3.5及完整G5仍未完成；整体IN PROGRESS / NOT READY，不能将本局部结果当作生产工艺全部完成。
