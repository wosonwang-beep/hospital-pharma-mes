# 第21轮：错误态重试按钮定位限定诊断

用户明确批准第20轮停止报告后最多10分钟、最多一次脚本定位修复，仅诊断错误态按钮并重跑同一采集脚本；不修改产品、断言、阈值、跳过条件和截图矩阵。开始 `2026-10-29 16:13:55 UTC`，截止 `16:23:55 UTC`，诊断/复测及服务恢复结束 `16:19:30 UTC`，用时5分35秒。累计20轮保留，本次一次修复后为21轮。已物理读取AGENTS、MES_DEV_RULES、mes-demo AGENTS、第20轮报告与续验记录。

## 根因与一次修复

原错误态 `.ant-alert` 中唯一按钮DOM为 `<button ... type="button"><!----><span>重 试</span></button>`；Playwright ARIA snapshot为 `button "重 试"`，精确名称“重试”匹配0，精确名称“重 试”匹配1；按钮可见、未禁用。只读诊断脚本 `frontend/mes-web/process-identity-retry-diagnostic-20261030.mjs` 返回退出0，阻断非GET/HEAD与非本机请求，blocked=[]。这证实先前超时由脚本名称定位引起，不能据此推断服务错误。

第21轮仅修改原 `frontend/mes-web/process-identity-capture-current-20261029.mjs` 的一处：在“工序表单加载失败”提示内精确定位名称“重 试”；不改变原截图状态、原waitFor、断言或阈值。重跑同一脚本退出0，`round21-capture.log` 显示 `count=57, blocked=[], pageErrors=[]`；原第20轮10张截图在重跑前保存于 `prototype-capture-round20/`。

## 覆盖和局限

`prototype-capture-current/capture-index.json` 保存57项：14种状态各1440/1280/900/375各1张，另375移动导航1张。脚本在各视口点击重试后观察到OP01列表出现，并输出对应截图；但亲看 `forms-recovered-1440.png` 和 `forms-recovered-375.png` 的原图仍可见加载动画，意味着等待OP01出现不足以证明加载结束后的稳定状态；故完整重试恢复为 NOT VERIFIED。不得对这种现象再作第二次脚本修改，当前阶段仅授权一次修复。

本索引不是审查manifest；未完成全部截图逐图九维/WCAG评审、稳定恢复截图、DOM锚点与对抗式复查或manifest validator。已有 `HANDOFF_MANIFEST.json` 和 `source-evidence-manifest.json` 是其他用途，不是本次UI/UX审查manifest。不得声称PASS FOR PROTOTYPE或READY FOR ACCEPTANCE。原型全部为本机内存数据，无业务写请求，不证明真实5173/8080保存重读；实体手机、确切原型版本设计锁、G3.5和G5仍未完成。

原型使用唯一5173，结束后恢复正式checkout前端，PID17116、HTTP200、工作目录 `D:/codex/_project/gmp/hospital-pharma-mes/frontend/mes-web`；8080 PID28928工作目录为checkout根目录。未执行迁移、数据库业务写入、发布、提交或推送。
