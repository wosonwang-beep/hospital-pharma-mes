# CPP、签署与复核角色：真实保存/重开/恢复结果

日期：2026-11-01 Asia/Shanghai。执行者：Codex SELF-REVIEW，沿用business-product-manager；不是独立审查。基线/已读文件、当前授权及写入边界见[persistence-execution-20261101.md](persistence-execution-20261101.md)。v1.0.23、UI V2、现有批准编辑器不变。

## 已完成的业务闭环

使用同一checkout的5173实际Vue页面、8080后端及原MariaDB。模板496始终DRAFT/V1/schema2，批准人/批准时间/发布内容摘要始终空。CPP_KCL_ASSAY的9.5～10.5阈值、字段、条件ID/顺序未调整。所有操作由标准MCP客户端调用注册CUAWright UI工具；无直接SQL写业务数据。

1. 完整GET基线versionNo4，业务载荷预检通过。
2. 在既有OP03 CPP增加“要求QA复核”，原三动作保留；将OP03签署角色从生产岗位ID287改为当前QA岗位ID291。缺操作原因时页面阻止保存并保留编辑；填原因后保存成功，versionNo5。
3. 关闭并从工艺1→工序表单→OP03/496真实入口重开。复核角色首次locator命中隐藏菜单，未选成功；GET明确仍287，未当成通过。改为实际可见选项locator后，将OP03复核角色保存为291，versionNo6；再次由相同入口重开，完整GET与保存响应一致，CPP四动作及两个角色ID/服务端快照均在。
4. 通过原UI取消新增QA动作，将签署/复核角色恢复287，保存versionNo7；再次关闭/通过工艺入口重开。完整对象严格比较只存在 `/versionNo` 和 `/versions/0/versionNo` 的4→7变化；整个definition（含CPP JSON原字符串、角色快照、全部字段）与原基线完全相等。
5. 只读原MariaDB独立确认当前versionNo7/DRAFT/schema2；三次正常SAVE审计29076、29077、29078均存在旧/新摘要与真实操作原因。未回写版本号或删除审计。

预期两次保存调整为实际三次：第一轮复核选项未选中，第二次单独保存复核角色，第三次恢复；保留原执行方案与失败日志，不将三次写入描述成两次。没有新建业务记录；OP01最初内存新建的规则从未保存。

## 真实证据

原始目录 `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/`：

| 证据 | 文件 |
|---|---|
| 基线完整GET200 | 1793503239322_native_mcp_export_mes_detail.json |
| 原因校验/恢复成功 | 1793503372365、1793503373005_native_mcp_inspect_current.json |
| 首次保存GET200/v5 | 1793503373764_native_mcp_export_mes_detail.json |
| 复核保存GET200/v6 | 1793503434536_native_mcp_export_mes_detail.json |
| 重开完整GET200/v6 | 1793503446094_native_mcp_export_mes_detail.json |
| 恢复保存、重开GET200/v7 | 1793503520996、1793503523556_native_mcp_export_mes_detail.json |
| 实际关闭浏览器 | 1793503533962_native_mcp_close_browser.json |

不要把click工具的http_status200当成PUT响应码：该字段是导航状态；本报告的200只指独立真实GET。保存成功由页面提示、版本递增、完整GET与数据库审计共同证明。

已按原分辨率查看本轮四张截图：review-qa-picker-before-save、roles-saved-reopened、cpp-four-actions-saved-reopened、cpp-restored-reopened（均后缀20261101）。角色重开截图只截到属性区顶部，不能单独证明两个角色；完整DOM及GET另提供证据。CPP截图只到条件区顶部，四动作的真实持久化由完整JSON确认，不把截图裁切解释为全页已验收。

## 验证、修改范围及结论

`python .../verify-persistence-20261101.py`：12/12记录证据核验，退出0；不是12个独立浏览器场景。输出persistence-checks-20261101.json。原始完整差异为persistence-restoration-diff-20261101.json。基线、拟输入自然业务值及已保存完整详情三次业务值检查均通过；原数据库查询为显式只读事务，日志persistence-db-audit-20261101.log。

11:19:47开始，11:25:33浏览器关闭、客户端退出0；后续仅证据检查与文档收尾。业务源码修复0，原累计32轮保留，定位修正及首次失败单独留证。业务代码/冻结设计/测试断言/迁移均未修改，未重复旧构建/单元/MockMvc回滚IT。新增任务目录执行/结果报告、JSON基线及校验脚本/日志，更新MES_TASKS和Planning；用户其它未提交改动保留，无提交推送/服务重启。

本轮**CPP与两类角色真实保存、入口重开、完整恢复、原因校验恢复、审计核对完成**。这部分不再列为待验证，也不需要重跑旧名称往返。完整G5的截图/锚点/九维/WCAG/恢复与角色权限矩阵仍未齐备；真实HTTP409/403、服务中断恢复与生产执行动作本轮NOT RUN。历史事务IT结果不能替代这些真实运行验收。未批准整包原型未集成；当前同位缺陷修复不扩展为整页重做。整体仍 **IN PROGRESS / NOT READY FOR ACCEPTANCE**。

Planning追加和随后resume均发生传输错误，更新结果未知，未声称同步成功；待恢复先核对revision。ownership中补充F-001真实持久化证据，仍保留HIGH未关闭及历史失败/修复计数；不擅自推进G5或人工接受。
