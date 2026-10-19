# 数据字典正式项目复查与遗漏修复

需求来源：用户要求“你再检查正式项目”，并询问“打包发布没有？”；延续同一会话已授权的产品体验评审后直接修改。2026-10-12 13:21开始复查，预算10分钟，最多2轮自动修复。

已真实读取：AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md相关字典记录、PROJECT_BASELINE.md Authority、Global UI V2第3/6/8节、Page Template V2 T1/T2、DCP-DATABASE-MENU-AND-USABILITY-001、DICTIONARY_ITEM_ENTRY_20261012设计记录、正式DictionaryListView.vue/DictionaryDetailView.vue/dictionaryModel.ts、直接e2e及Playwright配置。沿用v1.0.23+批准补充、UI V2/T1和已有简单字典项弹窗；不扩大到页面重做或新业务契约。

真实1452×800正式5173页面已加载新入口。运行11704为当前checkout Vite，8080进程5552为Java classpath开发服务；dist/index.html为12:48:53构建产物。没有把开发服务等同产物发布。

实机缺陷：新增填写PACKAGING_COMPONENT/包装组件后点击项目行编辑，表单无确认切换为FINISHED/成品，输入丢失。属于已授权“防误覆盖当前输入”的实现遗漏，不修改上一轮锁定的列宽/布局等设计契约。源码同时发现保存成功notice放在被弹窗遮住的列表，无弹窗内反馈。

限定修复：编辑区打开或保存中，行编辑/启停按钮及树节点编辑不能切换或改变正在处理的项；保留明确取消出口。startEdit/toggle入口增加同等防护和现有权限检查，不能只禁用按钮。弹窗内复用现有a-alert显示保存成功，打开其他字典/开始编辑时清除旧成功提示；保存流程仍原POST/PUT、幂等/If-Match和两处GET刷新，不改变API/数据字段。列表原成功提示继续保留。无结构/间距/字段/状态/权限/API/迁移变化。

白名单：DictionaryListView.vue；新增独立e2e/system-dictionary-item-guard.spec.ts（不改变旧断言）；本记录、docs/review/DICTIONARY_FORMAL_RECHECK_20261012.md及MES_TASKS.md。保护其他工作树修改，不提交推送。验证：构建、既有2项+新增1项fixture回归；真实5173/8080只读检查输入保护、取消恢复、树节点保护。fixture成功保存只能证明UI反馈；实际保存持久化本轮不写业务数据，NOT RUN。本轮不启动/重启/替换服务、数据库或产物发布。所有结论区分构建产物与运行服务。
