# 物料主数据编辑页两类子列表弹窗 — G2 交互原型候选（2026-11-03）

状态：**G2 CANDIDATE / PENDING EXACT-SOURCE HUMAN APPROVAL / G3–G5 NOT AUTHORIZED**。本记录不是正式开发放行，不能用用户批准全局 UX 原则取代此原型的具体版本批准。

## 来源与设计范围
- 基线：FINAL BASELINE COMPLETE v1.0.23 及已批准的物料/供应商受控合同；Global UI V2 + T2 模板。
- 统一 UX 决策：`docs/development/decisions/MES_GLOBAL_UX_INTERACTION_20261102_APPROVED.md`。
- 正式目标：`frontend/mes-web/src/views/master/MaterialConversions.vue` 与 `MaterialSuppliers.vue`；父页 `MaterialDetailView.vue` 的既有字段、业务接口和数据保护保留。
- 新增、编辑分别使用居中弹窗；禁止行内输入和列表下展开表单。子列表增加按钮在分区标题右侧。弹窗“保存 → 取消”、主表“保存基本信息 → 取消”，危险操作在最后；水平标签控件配对。普通 T2 标题仅保留“编辑物料”。

## 单位换算：字段 / 后端接口（保持现有契约）
- 新增与编辑弹窗：原单位 `fromUnitId`（单位选择）、目标单位 `toUnitId`（单位选择）、换算因子 `factor`（正十进制）；唯一原/目标组合、不能相同。
- 子表保留原单位、目标单位、换算因子、编辑操作；只读/无权限时不显示维护入口。
- 正式读取：`GET /unit-conversions?materialId=...`；单位来源 `GET /units`；保存：`POST /unit-conversions` 或 `PUT /unit-conversions/{id}`，保留现有物料关联、幂等、权限及更新原因。
- 保存失败与并发冲突必须保留弹窗草稿；仅刷新对应子表，不重新加载物料基本信息。

## 供应商关系：字段 / 后端接口（保持现有契约）
- 新增/编辑弹窗：合格且启用供应商 `supplierId`（编辑时固定）、生产厂家 `manufacturerName`（可空，最多200字）、关系有效 `approved`、首选 `preferred`、关系有效截止日期 `validTo`（可空）。资格状态从供应商源读取、只读。
- 供应商可多家，但**有效关系必须且只能有一家首选**。切换首选须同时取消原首选；撤销已关联关系保留历史，不作物理删除。对首选关系撤销时，若存在其他有效关系，须先选定替代首选。
- 正式读取：`GET /materials/{id}/suppliers`；可选供应商：`GET /suppliers?qualificationStatus=合格` 并过滤启用；持久化保持 `PUT /materials/{id}/suppliers` **一次提交整份关系清单**，用 `supplierPayload` 校验，再写入。弹窗“保存”对应这个整份原子写操作；去掉歧义的表格底部第二个“保存供应商关系”。
- 不改变已批准的“基础资料维护原因自动生成”、原有权限和源厂家冻结规则；不得新增单条关系写入 API，也不能绕过已有资格/日期校验。

## 主表数据保护与错误恢复
- 弹窗打开、关闭、保存、关系清单刷新、409及网络失败均不能覆盖物料主表未保存的名称、规格、单位等输入。
- 取消有改动的弹窗需要确认放弃或继续编辑；保存按钮禁重复提交；成功应显示并可定位新/更新的行。
- 浏览器返回/取消主表需草稿保护。保留既有 T3 只读操作；不增改 GMP、审计、权限、状态和数据库。

## 本次原型（待人工对精确版本确认）
- 源文件：`D:\codex\_project\gmp\mes-demo\src\features\materials\MaterialChildListsUX.vue`
  SHA-256：`4B25E76B653B57039EF821B44EC865AC3EFB37276C12B2532239821C43717E8C`
- 预览入口：`D:\codex\_project\gmp\mes-demo\src\material-children-preview.ts`
  SHA-256：`244D7B52B6800A02894CAE4C9BABA680952AD55B131B562449DD27F7CB729F9B`
- HTML：`D:\codex\_project\gmp\mes-demo\material-children-review.html`
  SHA-256：`F3CC0A044D934FF30FAA2438F13D4FA7AA9269C14049F408D0CBE2DB77FFC70B`
- 本机地址：`http://127.0.0.1:5174/material-children-review.html`；原型数据为隔离内存记录，不连接正式 5173/8080 数据源。
- 截图：`C:\Users\Administrator\AppData\Local\Temp\mes-material-children-20261103\`（1440/1280/900/375、子列表与弹窗）。
- 定向 Chrome 交互：新增/编辑单位换算、取消保护、供应商新增及首选唯一切换、撤销保留历史、主表未保存名称保持、四视口页面无 JS 异常：PASS；`npm run build`（vue-tsc + Vite）exit 0，有既有包体积警告。
- **尚未完成**：权限负例、服务端409/重试、真实 API/数据库读写、全状态与四视口完整九维 UI UX Pro Max 审查、完整 G2 清单及 G5。不得将这次定向 PASS 写成正式页面已验收。

## G3.5/正式移植的放行要求
仅在用户明确批准**以上精确原型文件哈希和字段/交互版本**，并完成 G1/G2 适用审查、G3 设计锁与 G3.5 源码映射后，才允许 Codex 将原型交互忠实集成入正式 `MaterialConversions.vue`、`MaterialSuppliers.vue`，执行真实后端回归与 G5 验收。正式仓库已有其他未提交更改必须保留，不得创建 worktree。
