# 工序工作区与 IPC 按原型续作

用户已识别本机 mes-demo「上方路线带＋左侧工序＋右侧配置」，纠正 IPC 按具体工序及前置关系判断、不能全工艺统一必检；本轮进一步明确「继续按原型开发」。沿用同一任务，不重置累计41。

本轮先落实已明确的原型规则纠错及源码保真交接准备，不将这句话冒充新源码哈希或旧审批生命周期的整体批准。当前原型源码 `D:/codex/_project/gmp/mes-demo/src/features/processes/ProcessPrototype.vue`，修改前SHA256 `67264A831056D900F69DE8920E22683117D2CC9A8E898C62DF2485EA0A88EB74`；同批三张历史IPC原图见 `../process-form-cpp-ebr/20261101-final/IPC_PROTOTYPE_TRACE_20261102.md`。

设计白名单：保持路线带、工序清单、当前工序配置及三个局部分区；仅纠正 validateDraft 全局强制IPC的错误和当前IPC提示的条件归属。不增加检验引擎，不更改已有逐工序质量门禁、签名、独立复核、历史快照或任何正式API/数据库。旧工艺审批、版本动作与当前定义合同仍有冲突，不纳入正式实施。

源码白名单：上述 mes-demo 原型组件和其直接验证脚本；本任务设计/需求映射、源摘要、证据、MES_TASKS、ownership、Planning。正式MES业务源码、后端、迁移和持久化业务数据不在本次范围。

本轮使用普通10分钟窗口：2026-11-02 10:09:01–10:19:01（Asia/Shanghai），最多1批规则修复，累计41→42，重复同根因失败仍立即停止，不自动延长。首次提取原函数进行源码级行为测试，不用新断言覆盖旧失败；只运行定向测试和原型typecheck/build。浏览器未执行即记NOT RUN，不能把原型验证当正式5173/8080验收。

已真实读取两个工程AGENTS、MES_DEV_RULES、MES_TASKS/PROJECT_BASELINE、旧原型设计记录、用户纠偏追踪、BASIC_CURRENT_DEFINITION_CONTRACT、UI V2/T1–T6及 business-product-manager/G3.5、systematic-debugging、test-driven-development、mes-ui-ux-audit/ui-ux-pro-max。依赖检查：原型本地内存，正式已有Operation、ipcDefinitions、完成规则及前置依赖可复用；无新DB/API需求。正式交接仍须补齐确切原型版本、详细动作映射、适用UI审查及真实验收，不伪造READY。
