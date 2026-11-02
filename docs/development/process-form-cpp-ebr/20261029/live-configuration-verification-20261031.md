# CPP 与角色配置真实续验 — 2026-10-31

执行者：Codex SELF-REVIEW，使用 business-product-manager、cuawright-local、mes-ui-ux-audit 与 ui-ux-pro-max。结论限于本报告范围；完整 UI/G5 **未完成**。

## 基线与范围

实际读取 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、PROCESS_DYNAMIC_FORM_CPP_REQUIREMENTS.md、PROCESS_DYNAMIC_FORM_CPP_DETAILED_DESIGN.md 相关条件组章节、PROCESS_FORM_TEMPLATE_IDENTITY_20261029.md、当前三组件与页面保存/离开保护。继承 v1.0.23、已批准 CPP DCP、UI V2 及既有设计；未重画正式页面。当前整包原型批准/G3.5 缺口保留。

本阶段从21:17:44开始，最后浏览器于21:25:46关闭，两个客户端均退出0，未超过本阶段十分钟；原32轮保留。本轮没有业务代码修复。复核下拉定位由隐藏菜单限定到可见菜单，是运行操作装配纠正；不修改测试断言或产品合同。所有角色/规则选择仅存在未保存草稿，**未点击页面“保存草稿”**，无新增业务记录、迁移、审批、签名、发布、服务重启或Git提交。

## 结果与证据

所有原始 JSON/PNG 位于 `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence`。文件前缀是实际时间戳；没有打印凭据、令牌或图片数据。

| 核验 | 观察与边界 | 原始证据 |
|---|---|---|
| 工具/身份 | 全操作模式true，复用会话，identity 200 | 1793452663915、1793452668270 |
| CPP编辑 | 当前表单23字段；新增局部规则、两个条件组、OR联动可读预览；局部保存明确要求再用页面保存提交 | 1793452735282_native_mcp_inspect_current.json |
| 不完整配置 | 空名称/比较值点击局部保存，提示“请完善规则名称和全部条件” | 1793453021669_native_mcp_inspect_current.json；1793453021673_cpp-incomplete-validation-live-20261031.png |
| 签署角色 | 真实搜索与选项选择后，OP01显示质量标准维护；仅验证控件，不认定该角色适用于业务签署 | 1793452815563_native_mcp_inspect_current.json；1793452814947_signature-role-changed-unsaved-20261031.png |
| 复核角色 | 搜索QA后选择当前可见菜单，OP01复核显示QA岗位；未持久化或授予权限 | 1793453145461_native_mcp_inspect_current.json；1793453145463_review-role-selected-visible-20261031.png |
| 离开保护 | 未保存时点击关闭仍停在设计器且选择保留；工具默认dismiss原生确认。本轮没有验证“确认放弃”分支 | 1793452815563_native_mcp_inspect_current.json |
| 未保存不落库 | 基线、编辑后、从真实工艺入口重开后、末次角色选择后完整详情对象相等，versionNo均4 | export_mes_detail前缀1793452702696、1793452849991、1793452885228、1793453075310、1793453146368 |
| 入口重开 | 工艺1→工序表单→496/OP01设计器，CPP恢复无规则；保存的6签署/6复核及原定义不变 | 1793452851741、1793452861408、1793452885228 |

## 视觉及失败分类

已以原始分辨率检查本报告引用截图。375/1280初次截图发生在视口变化后的侧栏过渡中，出现留白/遮挡；源码 `layout-fixes.css` 明确 margin-left 过渡0.2秒。稳定后重新截图375及1280，留白/遮挡不再出现，原瞬态图片保留，不据此修产品。稳定图为1793452973032_cpp-empty375-settled-20261031.png和1793453020394_cpp-empty1280-settled-20261031.png。900及1440截图也已目视查看；不把顶部局部截图当整页/全部状态验收。1280字段名称/编码仍截断，完整值可通过当前字段搜索/条件选择定位，悬停/键盘可获取完整值尚未验证。

复核角色首个选项locator命中此前保留的隐藏下拉，超时；限定 `.ant-select-dropdown:not(.ant-select-dropdown-hidden)` 后选中成功，原失败1793453074413保留。签署输入点击被已选span遮挡后改点Select容器，是标准控件操作修正，未改业务代码。

通用水平标签工具两次返回 `total=0 / pass=false`，不能当同行检查通过；需要专用锚点探针。浏览器日志含401，身份与目标GET均200，未取得401对应URL及因果，不将其归为已解决或无错误。

## 验证命令、修改清单与剩余门禁

`python docs/development/process-form-cpp-ebr/20261029/verify-live-configuration-20261031.py`：16/16记录证据检查，exit0，结果为同目录live-configuration-checks-20261031.json。该脚本只重核已记录真实证据，不是16个独立端到端业务用例。未改业务源码，故未重复旧单元/构建；旧15/15与54方法不冒充本轮新结果。

本轮写入仅本报告、证据校验脚本/JSON、continuation.md、MES_TASKS.md和Planning进展；无迁移，现有用户修改保留。

当前证据仅关闭“真实编辑控件能否操作”的部分疑问。CPP/角色的真实持久化与重开、服务错误/冲突/窄权限、生产执行动作、完整UI九维/WCAG/全部状态视口及有效G5清单仍未完成；F-001不能据此整项关闭。递归CPP能力边界、整包原型确切版本批准/G3.5继续保留。任务 **IN PROGRESS / NOT READY FOR ACCEPTANCE**。
