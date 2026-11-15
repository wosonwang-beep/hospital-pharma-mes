# MATERIAL-CHILD-MODAL-001 — 物料编辑页两个子列表弹窗 DESIGN LOCK

状态：**USER APPROVED EXACT SOURCE / DESIGN FROZEN FOR SCOPED FRONTEND INTEGRATION**（2026-11-03）。完整 G2 全维度审查及正式 G5 验收独立待证，不因原型签字自动变为 PASS。

## 批准证据与原型版本

用户明确原话：“我明确批准物料子列表弹窗原型 MaterialChildListsUX.vue，SHA-256 4B25E76B653B57039EF821B44EC865AC3EFB37276C12B2532239821C43717E8C。请完成必要的设计冻结与G3.5检查，然后将交互正式集成到 MaterialConversions.vue 和 MaterialSuppliers.vue，保留原业务接口和权限并执行回归验收。”

本机实际核对原型路径：`D:\codex\_project\gmp\mes-demo\src\features\materials\MaterialChildListsUX.vue`，SHA-256 `4B25E76B653B57039EF821B44EC865AC3EFB37276C12B2532239821C43717E8C`，与批准一致；原型源文件不得在移植过程中自行改写。预览：`http://127.0.0.1:5174/material-children-review.html`，独立 Vue3 + Ant Design Vue 内存数据原型。原型审核记录：`MATERIAL_CHILD_LIST_MODAL_G2_20261103_DRAFT.md`。

## 界面 DESIGN LOCK（普通 T2 中的两个子列表）

1. 主 T2 不重新设计，保留仅标题“编辑物料”、基本信息、单位换算、供应商关联、状态与底部“保存基本信息 → 取消”。查看 T3 使用既有模板。
2. 单位换算/供应商关联的新增按钮均位于各自标题栏右侧。每一行维护仅通过**居中弹窗**，列表无行内输入，列表下方无展开编辑表单；弹窗底部“保存 → 取消”，主按钮靠左于同组辅助按钮。表单标签与控件始终同一行，窄屏整对换行。
3. 单位换算新增/编辑：原单位、目标单位（实体单位选择）、换算因子；原目标不能相同；有效正数及重复关系校验，回显记录并更新，支持取消更改确认；无权限/只读不显示新增/编辑。
4. 供应商新增/编辑：供应商选择、生产厂家、资格状态只读、关系有效、首选供应商、有效截止日期（UTC）；原供应商 ID 编辑不可更改；唯一首选，切换自动取消旧首选；撤销二次确认、保留历史行；没有剩余有效关系时首选可以为空。
5. 子列表在弹窗保存、取消、失败、409、刷新时不得重置主表尚未保存内容；失败保持草稿可恢复，保存成功在本子列表可定位最新行，防重复提交；无权限操作不可见。
6. 状态、权限和业务含义由服务端现有合同决定，不得从原型的内存占位数据、简化字段或通知文本重新发明规则。

## 源码保真与必要集成适配

正式两个组件仅迁入已批准原型的**弹窗布局、操作顺序、草稿隔离、取消保护、编辑/首选/撤销交互**。正式 API 数据来自后端，不使用原型内存数据。保留：
- `MaterialConversions.vue`：`GET /unit-conversions` 按 `materialId` 分页、`GET /units` 搜索、`POST /unit-conversions` 与 `PUT /unit-conversions/{id}`，现有幂等键、自动维护原因、单位读写权限。
- `MaterialSuppliers.vue`：`GET /materials/{id}/suppliers`、`GET /suppliers?qualificationStatus=合格`，`supplierPayload` 的唯一首选及去重校验，`PUT /materials/{id}/suppliers` 一次原子提交整份关系清单。不能改写成新的单条 API。
- 历史撤销状态不可物理删除；资格/有效期与生产厂家快照按既有业务规定；现有 T3/父页面权限、服务端校验继续生效。

## 受控范围与门禁

允许修改：`frontend/mes-web/src/views/master/MaterialConversions.vue`、`MaterialSuppliers.vue`，以及只针对上述组件新增的隔离/浏览器测试与本任务验收/任务记录。已有修改中的 `MaterialDetailView.vue` 不覆盖。禁止改动后端、Flyway、权限配置、接口、DTO、生产数据和其它业务模块；不创建 worktree，不擅自 Git 提交/推送。

实施前已确认两份正式组件原始 SHA-256：
- MaterialConversions.vue: `F4CA55CD56B4B1C4DAD32F14F5C33F99278BD67EDA46CDB80AF8F3444E48EABD`
- MaterialSuppliers.vue: `123A611422C6B5FC757399E08B6CAAF0735FD05782DEBA5BE283697ADBDA2835`

G3/G3.5 设计锁与源码映射按此文件及 `MATERIAL_CHILD_LIST_MODAL_G35_PREFLIGHT_20261103_DRAFT.md` 执行。需要定向组件单测、typecheck、build、浏览器正/负流程、视觉审查，真实 API/数据库检查与完整 G5 应独立据实记录；未执行项标 NOT RUN，不提前标 FINAL ACCEPTED。
