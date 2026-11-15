# 物料子列表弹窗：正式 Vue 集成预检与 G3.5 交接候选

状态：**PREFLIGHT COMPLETE / G2 HUMAN APPROVAL MISSING / G3.5 NOT READY / G4 NOT AUTHORIZED**。
时间：2026-11-03。此文件是预检资料，不是批准记录、正式代码或验收报告。

## 本轮实读的权威与现有文件

- `AGENTS.md`、`docs/development/MES_DEV_RULES.md`、`MES_TASKS.md`、`docs/PROJECT_BASELINE.md`
- `docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md` 与既有全局 UX 决策
- `docs/development/decisions/MATERIAL_CHILD_LIST_MODAL_G2_20261103_DRAFT.md`
- `frontend/mes-web/src/views/master/MaterialConversions.vue`、`MaterialSuppliers.vue`、`MaterialDetailView.vue`
- `frontend/mes-web/src/master/material.ts` 及 `backend/mes-masterdata/.../api/MaterialController.java`
- `D:\codex\_project\gmp\mes-demo\src\features\materials\MaterialChildListsUX.vue`

业务基线：FINAL BASELINE v1.0.23 + 已批准限定 DCP；UI：Global UI V2 / T2。**适用的强制规则要求对精确源码哈希进行人工批准，并在 G3/G3.5 冻结后才能正式集成。** 当前 G2 记录仍明确为候选，因此不得把“下一步实施”的会话指令自动视作已批准这一原型的确切版本。

## 只读核对的精确源文件 SHA256

| 用途 | 文件 | SHA256 |
| --- | --- | --- |
| 待人验原型 | `mes-demo/src/features/materials/MaterialChildListsUX.vue` | `4B25E76B653B57039EF821B44EC865AC3EFB37276C12B2532239821C43717E8C` |
| 正式单位换算组件 | `frontend/mes-web/src/views/master/MaterialConversions.vue` | `F4CA55CD56B4B1C4DAD32F14F5C33F99278BD67EDA46CDB80AF8F3444E48EABD` |
| 正式供应商关系组件 | `frontend/mes-web/src/views/master/MaterialSuppliers.vue` | `123A611422C6B5FC757399E08B6CAAF0735FD05782DEBA5BE283697ADBDA2835` |

以上两个正式目标组件在本次 `git status --short -- ...` 检查时没有本机未提交修改；`MaterialDetailView.vue` 已存在本机改动，必须保留，不覆盖。禁止多 worktree。

## 源码/交互/真实接口映射（供批准后 G3.5 校验）

| 原型行为 | 正式落点 | 必须沿用的现有合同 |
| --- | --- | --- |
| 子表“新增单位换算”“编辑换算”打开居中弹窗 | `MaterialConversions.vue` 的 `start` / `open` 和模板 | 原单位 `fromUnitId`，目标单位 `toUnitId`，十进制字符串因子 `factor`；已有 `GET /units` 搜索/选中值保留 |
| 单位换算弹窗保存与刷新 | `MaterialConversions.vue` 的 `save` / `load(page)` | 已有 `POST /unit-conversions`、`PUT /unit-conversions/{id}`、幂等键、更新操作及自动生成的维护原因；保留旧页面分页位置和当前物料范围 |
| 合格供应商下拉、独立编辑、唯一首选 | `MaterialSuppliers.vue` 的 `search`、`supplierPayload`、弹窗草稿 | `GET /suppliers?qualificationStatus=合格` + 启用校验；`supplierId` 为实体键且编辑不可更改；厂家可空且最多200字；`validTo`、`approved`、`preferred` 均保留 |
| 供应商新增/修改/撤销保存 | `MaterialSuppliers.vue` 的 `save` | 唯一正式写入 API：`PUT /materials/{id}/suppliers`，**整份关系清单一次原子提交**，`supplierPayload` 必须保留；不能擅自发明按行 PUT/POST；撤销仅标记无效并保留历史 |
| 子弹窗取消、失败、409 不丢主表草稿 | 两个子组件 + 当前 `MaterialDetailView.vue` | 主表 `form` 不重新赋值，不调用父页 `load()`，子组件只刷新自己的数据；冲突提示不得以默认重新加载覆盖弹窗草稿 |
| 只读与权限 | 两个子组件的 `writable` 和 `auth.can` | 单位读取/创建/更新权限；供应商查看及物料更新权限，停用物料不可编辑；父页面 T3 为只读 |

## 拟定 G4 白名单和禁止项

**待人验和冻结后允许：** `MaterialConversions.vue` 与 `MaterialSuppliers.vue` 内的弹窗模板、局部状态、局部 UI 样式、同源 API 触发与可访问性处理；定向新增测试/验收证据（需冻结交接时列出具体文件）。

**仍然禁止：** 改动任何已执行 Flyway、后端业务逻辑/控制器/API/DTO、GMP 审计、权限、状态机；破坏或覆盖主表未保存数据；重做整个 T2 页面；更改冻结业务字段或把实际物料/供应商的数据源替换为原型内存样例。已存在并行未提交工作必须完整保留。

## G2/G3/G3.5 放行缺项

- [ ] 用户明确批准 **上述确切原型版本 SHA256**，而不只是批准“统一弹窗”的一般原则。
- [ ] G1/G2 全维 UI UX Pro Max 适用审查和关键异常恢复（权限、网络失败、409、窄屏、焦点）形成证据；当前仅已有 1440/1280/900/375 的定向 Chrome 冒烟与构建通过。
- [ ] 将精确原型的字段、动作、视觉和 API 映射冻结在 DESIGN LOCK；记录 G3.5 收件版本/哈希及 Codex 白名单。
- [ ] G4 开始前重新读取文件并比较哈希、检查 `git status`；任何漂移必须先审查。

## 实施后验收断言（尚未执行）

1. 两个子列表新建、编辑仅居中弹窗；行内无可写控件，列表下无展开编辑表单；操作按钮自左向右“保存→取消”，标签控件同一行。
2. 权限覆盖：物料和单位只读、禁止创建、禁止更新、停用物料限制；403 后无新增/篡改。
3. 编辑主表名称、再新增或编辑任何子表、取消或保存后主表输入完全保持。
4. 单位 API 创建/更新及分页刷新、失败不关闭弹窗、重复键/非法换算/409 有明确恢复。
5. 供应商整份 PUT：新增合格供应商、首选唯一、首选切换、厂家可选、有效期校验、撤销不删除历史；保存失败整份关系仍不破坏原值。
6. 真实 5173/8080 + MariaDB 回读、无多余记录和权限越界；完成 1440/1280/900/375 视口全状态原图/DOM/键盘/无障碍复核，走 G5 验收。

结论：**正式集成代码本轮未动；待精确原型人验与设计冻结后进入 G4。**
