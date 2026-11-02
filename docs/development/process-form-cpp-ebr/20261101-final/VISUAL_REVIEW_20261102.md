# 工序动态表单、CPP、eBR：首轮逐图审查

Codex SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max。用户“继续”后推进现有证据审查，不自动开始第38轮业务/采证修复。普通10分钟窗口00:06:35–00:16:35（Asia/Shanghai）。

## 基线与范围

实际读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、Global UI V2、Page Template Standard V2及ROLE_ID_SIGNING、PROCESS_FORM_TEMPLATE_IDENTITY、PROCESS_DYNAMIC_FORM_CPP原型批准锁和CPP需求/详细设计；业务v1.0.23及批准补充，未发现授权重做整页原型的记录。按T1列表、T2创建、T4工艺聚合与批准专用设计器审查。源码七文件摘要仍30279C80855199A7F2BF995D792A1B00E4E02D4C53A147F75960E70E3177496A；ui-ux-pro-max真实文件摘要仍8B827FE03B349AEEA99A7D6CF4CCD892E6065B672859FDB11E13A1919453EDDC。git status已查，所有既有业务/其他模块修改保留。

本轮只写当前审查JSON/报告、MES_TASKS、continuation、ownership和Planning，无业务源码、数据库、配置、迁移或保存。八组已存知识检索证据继续有效，不为凑数重跑。WCAG方法源在线核对https://www.w3.org/TR/WCAG22/；未据此宣称认证。

## 设计差异与证据

170张截图全部通过view_image original逐张读取，1360项八类观察写入visual-first-pass-20261102.json。首轮原图审查00:07:55–00:12:12；不是二次对抗审查完成。每图有具体范围限制，不能用文件名当状态证明。

五项HIGH保持OPEN：

1. G5-UX-001：所属模板长名称跨列并与V1·草稿重叠，1440/900的默认、长内容、恢复及焦点图可见。源码ProcessDetailView第67行未约束名称/编码文本边界。整改应限定单元文本换行/宽度，不改六列、ID和入口。
2. G5-UX-002：读取失败仍显示0份/该工序尚未配置表单。loadEbrDefinitions先清空数组，catch仅全局error，无法区分未知和真实空。应独立呈现加载/失败/重试，不能引导误建记录。
3. G5-EVID-001：角色open/search八图拍到页首，下拉在画面外；六格记录documentWidth超过viewport。capture脚本popup分支仍scrollTo(0,0)，resize后又保留旧弹层。不能据此断言真实下拉坏或通过。
4. G5-EVID-002：Escape后的mobile-navigation keyboard-close图菜单仍展开。脚本未断言关闭；先测焦点/事件再区分源实现和测试装配，不准删关闭要求。
5. G5-EVID-003：12张focus图实际active为折叠侧栏按钮（1440/1280）或BODY（900/375）。resize/侧栏操作后未重新聚焦目标，必须补真实业务焦点证据。

其余MEDIUM候选保留在逐图观察：375宽表来源/动作发现性、普通空态重复高留白、新建必填缺事前标记、CPP错误缺局部定位、画布工具栏横滚提示、英文元信息。尚未完成适用合同/DOM复验，不能擅自重画。A4纸张留白具有明确文档用途，不将其当普通空卡压缩。

## 验收与状态

170为现有文件数；其中21格目标状态证据不成立（8展开搜索、12焦点、1关闭），不是170格有效验收。原采证运行14检查/errors0仍是当时脚本事实，不改写为完整覆盖通过。

重新汇总168格记录DOM、888条标签控件配对：同行失败0、中心线差>8px为0；六格弹层文档溢出。选择器并非全控件，未证明完整锚点/目标尺寸或页面几何通过。报告geometry-summary-20261102.json保留限制。

81个九维登记与19条WCAG最低项已建；未完成项明确NOT RUN，未伪签PASS。已运行manifest validator退出1；同一audit-manifest.json绑定integration-acceptance.json仍FAIL。原业务20/20/typecheck/build/真实403与409证据保留为历史，本轮未重跑，无新测试通过声明。

当前IN PROGRESS / NOT READY FOR ACCEPTANCE；累计37未增加。后续最小范围：ProcessDetailView单元边界与读取状态、capture脚本目标滚动/逐视口焦点/断言；AppLayout仅在实测证实Escape实现错误时做同控件键盘修复。原测试/矩阵不减，补21格后复查受影响状态，再完成九维/WCAG、全图二遍、同版参考对照和验证器。原窗口与一次采证授权已用尽，新的源码/采证修复须按§6A明确批准下一范围；不修改全局规则或静默开启第38轮。
