# 生产工艺配置：第38–39轮结果 — 2026-11-02

**当前任务 IN PROGRESS；G5 FAIL；未 READY / ACCEPTED。** 产品负责人/执行及审查者为 Codex SELF-REVIEW。技能及工具不构成独立QA或人工验收。

用户“批准”对应 [有界整改窗口](../../decisions/PROCESS_G5_REPAIR_WINDOW_20261102.md)：06:02:44–06:42:44（Asia/Shanghai），最多两批，保留累计37。本阶段第38批修复原五项问题并重新采证，第39批仅修正补充采证的滚动定位及字体过渡等待。累计39；两批额度已用，不自动第40轮。最终报告/校验时间见记录；未为补图重置轮次。

## 实现与边界

- `ProcessDetailView.vue`：所属模板名称/编码在原单元内换行，保留六列、宽度、固定操作及模板ID+表单编码身份；工序表单读取中、失败、成功空配置分开，失败显示重试且不显示假0份/未配置。新建仅在读取成功且可编辑时可用。
- `AppLayout.vue`：先实测原导航Escape未关闭，再复用原Ant下拉控制open；Escape关闭并返回触发器焦点，导航后关闭，权限过滤/菜单/路由/布局不变。
- 采证恢复每个视口业务目标焦点、断言popup可见及边界、断言Escape后隐藏和焦点返回；保留原170格及原断言。
- 无后端/数据库/API/权限/业务字段/DSL/阈值/四动作/审计签名修改；**无新增迁移**，无正常业务保存/创建。真实409只为被拒绝的stale版本请求，完整definition前后相等。历史496保存恢复及审计证据保持不变。

## 验证证据

- 新增两条错误/加载回归先RED后GREEN，原身份断言保留。最终 **6套件26测试通过，0失败，11.06秒**：[日志](round38-regression.log)。早期命令误列两个不存在的测试文件，已有19条通过不冒充最终26；原日志保留。
- vue-tsc及Vite构建退出0，18.02秒；既有大chunk警告未隐藏：[构建](round38-build.log)。局部diff检查无空白错误，CRLF提示保留。
- 实际5173/8080 CUAWright：**53检查全通过、case errors为0、170张同版本截图**：[运行证据](formal-captures/20261102_060722/evidence.json)。真实409→重新加载200、受限身份403及路由守卫、角色/工序读取失败恢复均覆盖。浏览器清理时两条Target closed异步消息发生在全部检查之后，不称console全清。
- 实现前后探针：[前](boundary-probes/before-20261102_060354/result.json)、[后](boundary-probes/after-20261102_060632/result.json)。原来源文字越界消失；失败不再六处假空/假0且有重试；Escape菜单1→0、焦点回原按钮。
- 新源码闭包8文件，SHA256 `3777DE4FCA5DA77E76D8A0B0D50435986DF149AF30C3D817F0250797DA79A5A2`；登记脚本逐文件重算匹配，不混用旧7文件摘要。同版170原图已实际按原尺寸看完两遍，1360类逐图记录、81维度、19个最低WCAG准则登记：[首遍](visual-round38-first-pass.json)、[二遍](visual-round39-adversarial.json)、[源码/覆盖](round39-source-and-coverage.json)。
- 原5项HIGH仅在各自边界关闭：长名串列、失败假空、8下拉隐藏、12失焦、Escape未关闭。关闭焦点采证缺陷不表示全控件焦点对比度已通过。
- 原21格已更正；另发现375预览正文没有被有效捕获，集成报告单列1格内容证据不足。170文件齐备不等于170格内容已全部有效证明，不以截图数量越过门禁。

## 新发现及剩余验收

1. **HIGH G5-A11Y-001：CPP真实下拉输入没有可访问名称。** 外层div有aria-label，rc_select_2–6的内层input无aria-label/labelledby/labels；aria树为无名combobox，三个预期名称定位均0。角色控件12项名称和键盘展开/关闭正常，不应混为同一控件已修。由 [24格名称/颜色探针](accessibility-probes/20261102_061238/result.json) 及 [独立DOM补核](supplemental-probes/20261102_061709/result.json) 重复确认。`CppRuleDesigner.vue`不在本次白名单，尚未修改。
2. **HIGH G5-A11Y-002：普通蓝色文字对比度不足。** 实际普通13–14px蓝字#1677ff/白底及白字/蓝底约4.104:1，低于4.5:1。根因是共享V2 primary令牌及组件使用；图标、大字和禁用态未误算成普通文本问题。需有界V2对比度补充授权，不能擅改全局视觉基线或降阈值。
3. **MEDIUM G5-A11Y-003：200%文字放大压力图裁切按钮文字。** 稳定等待700ms后14→28px，新增/查询按钮仍固定高度：[图](supplemental-probes/20261102_061709/text-200-percent-1280.png)。这是受控CSS文字压力检查，尚非完整原生浏览器200%缩放验收；准则1.4.4保留NOT RUN，不能把初次过渡态14.99px当200%。
4. **MEDIUM G5-MOBILE-001：375来源可用宽度及预览补证不足。** 单元内部换行修复，但固定操作列遮住来源右部；补图仍没显示Canvas预览正文，不能称预览已读。1280来源横滚补图已可完整辨识，900/1440同样可读。遵守2026-10-13“本项目只按桌面端处理”，移动债单列，但既定375矩阵未删除或弱化。原错误滚动/过渡采证也保留。

完整所有控件键盘序列、错误逐字段关联/状态播报、24px目标逐项例外、320CSS重排及共享锚点全覆盖仍有NOT RUN。既有测量只是选择器抽样，不能据数量声明全页PASS；见 [几何摘要](geometry-round38-summary.json)。已冻结CPP参考源码与现实现的同视口并排对照仍未执行；不能把未批准的20261029整页原型当新权威。本阶段未启动第二个5174运行或复制工程补参考。

历史F-005（角色名称回退）、F-006（字段改码选择断裂）、F-007（画布/窄屏动作）在manifest继续保留 `FIX VERIFIED / FULL G5 PENDING`，没有擅自改为整体验收关闭。因此manifest中**5项HIGH尚未最终关闭：3项局部修复已验证待完整G5，2项本轮新缺陷未修**；原G5-UX/EVID五项的有界关闭与这个计数不同。

必需 `ui-ux-audit` 与同一绝对路径 [audit-manifest.json](audit-manifest.json) 和同一源码绑定到 [integration-acceptance.json](integration-acceptance.json)。validator实际结果另存 [audit-validator-round39.log](audit-validator-round39.log)，完整门禁保持FAIL，不能进入READY。原旧manifest及集成报告已先备份，历史结论不覆写。

validator实际exit1：CPP实际combobox名称检查FAIL、批准参考同视口对照NOT RUN、完整锚点NOT RUN，未改validator或将缺项标PASS。888条已采配对测量全部同行、偏差不超过8，168格document宽度均无越界；这不覆盖固定列实际可读区。

最终一致性核对通过：8文件摘要仍与采证相等，集成报告与manifest同摘要/同路径，170截图文件无缺失，本报告及下一阶段草案的本地证据链接无缺失：[核对记录](round39-final-consistency.json)。四个本任务Python脚本AST解析通过；capture脚本既有普通字符串`\s`转义SyntaxWarning记录为LOW债，本轮没有追加修复它或重跑UI采证。

本轮在线核对方法依据：[W3C普通文本对比度](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html)、[程序名称/角色/值](https://www.w3.org/WAI/WCAG22/Understanding/name-role-value.html)、[文字放大](https://www.w3.org/WAI/WCAG22/Understanding/resize-text.html)。标准阈值与所有测试断言均未放宽。

## 下一阶段的具体边界

当前批准的两批已耗尽。下一次需要的范围为：仅修CPP下拉实际输入的标签关联并跑原断言；提出可复核的普通文字/按钮对比度V2有界补充，获批准后才修改共享令牌/样式；核对原生200%文字/缩放以及遗留键盘/错误恢复准则。业务字段、权限、API、数据库、阈值和原170格断言不变。共享颜色若需改变已冻结视觉值，先记录设计补充，不视作一般开发批准。遵循 [MES_DEV_RULES §6A](../../../development/MES_DEV_RULES.md)，保留累计39以及本次失败签名。

具体拟议值及影响边界见 [下一阶段整改草案](../../decisions/PROCESS_G5_A11Y_SUPPLEMENT_20261102_PROPOSED.md)，状态PROPOSED，未在产品中实施。

2026-11-02 06:29已向用户提交完整方案/仅CPP名称两种有界选择，当前等待真实答复。未经答复不启动第40轮、不自动续40分钟；Planning最新revision48记录收尾。当前阶段于批准窗口内安全结束，5173/8080服务保留。
