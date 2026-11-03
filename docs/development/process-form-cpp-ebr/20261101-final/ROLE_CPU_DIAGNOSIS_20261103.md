# 角色页超时 CPU 诊断结果

## 基线依据

本阶段已获得PROCESS_CI_ROLE_PERFORMANCE_20261103_APPROVED，实际读取AGENTS/MES_DEV_RULES、任务索引、候选范围、原角色源码/原测试以及系统化排错和完成前验证技能。窗口16:06:10–16:46:10；累计48保留，本轮仅诊断，尚未使用49/50，不以诊断重置历史计数。

## 设计差异与已测根因

原RoleDetailView.test.ts未经改动。普通单文件11/11通过，35.34秒；观测CPU的同一单文件11/11通过，36.08秒，两条重点用例3845ms、3669ms。采样为原测试提供观测，未调整5000ms、断言、隐藏状态查询或隔离配置，未修改RoleDetailView.vue。

有效工作进程role-48680.cpuprofile覆盖34703毫秒：window.getComputedStyle的祖先样本26386毫秒（76.03%）；任意角色组件帧的去重祖先样本87.64毫秒（0.25%）。具体加权精确值、SHA和源码不变证明在role-cpu-findings-20261103.json。调用链：Testing Library getByRole → dom-accessibility-api computeAccessibleName/isHidden → JSDOM getComputedStyle/style-rules → @asamuzakjp/dom-selector check/filterSelector → CSS正则匹配。全部正则自身合计9.42秒，主要选择器模式约8.6秒。不能再用未经证明的角色权限计算优化代替真正环境根因。

第一次Vitest execArgv CPU自动文件没有产出，原日志保留；随后采用Node inspector仅采样的预加载器，在实际测试工作进程取得CPU数据。预加载器只记录/结束Profiler，不修改原测试、计时门槛或业务。工具进程的idle档案与有效测试工作进程分开分析。

## 修改范围

仅新增批准记录、诊断脚本与CPU证据/本报告。角色组件源码SHA仍d52c10e3d947aaa478b2a00502693caeb4d34fa46ba259d1387407a8aef9d62f；package.json和package-lock.json均未改动；无新增实际修复、无业务代码/数据库/迁移更改。上阶段回调及CI候选差异保留原样，未提交或推送。

## 依赖核对与候选

实际安装JSDOM27.4.0、dom-selector6.8.1；本机Node24.19.0。npm官方元数据确认JSDOM30.1.3要求Node22.22.2+/24.15.0+，本机满足，原CI的Node22最新补丁需实际验证。上游[30.1.3发布说明](https://github.com/jsdom/jsdom/releases/tag/v30.1.3)明确改进getComputedStyle、计算CSS属性及交互测试性能；[30.1.0说明](https://github.com/jsdom/jsdom/releases/tag/v30.1.0)也记录计算样式、DOM和表单查询性能改进。这只是有依据的升级候选，尚未安装或宣称可解决本项目超时。

## 结论

当前允许修改的角色页业务代码并非主要耗时位置，保持不改。根因落在白名单外的测试DOM依赖，按批准边界先报告，不能私自替换测试环境。需具体追加package.json/package-lock.json权限后才能安装候选并运行原完整套件和CI验证。CI和整体生产工艺仍IN PROGRESS，未READY/ACCEPTED。
