# MES 真实集成审查 R5 — 页面导航、成品链、权限与生产/来料回归

日期：2026-10-08；本机 main 工作区，复用现有本机 Vite 4174 与后端/数据库；未提交、未推送。

## 修复与验证
- 修改 DEV-only Playwright 临时脚本：finished-chain-review.tmp.spec.ts、finished-detail-review.tmp.spec.ts、finished-stock-closure.tmp.spec.ts，手机端改为每次打开可见下拉菜单并选择目标，避免隐藏 PC 菜单误命中。
- 修改 quality-plan-role-ui.tmp.spec.ts，手机端使用“打开导航”而非不可见的 PC 侧边菜单。
- 六套用例在 PC / Mobile 共 12 项：12 PASS，0 FAIL，exit 0。
- 生产批/生产执行/称量/投料/eBR 草稿、来料 OOS + QA 放行阻断、完整追溯共 4 项 PC 浏览器测试：4 PASS，0 FAIL，exit 0。
- 前端 Vitest：28 个文件、102 个测试全部 PASS，exit 0。
- 前端 vue-tsc + Vite build：PASS，exit 0；有首页单测 Ant 组件未注册警告，以及大于 500 kB 的 bundle 警告（不阻断编译）。

## 核实数据
- 已放行 20261007-KCL-001：成品入库 1000 mL、确认发货 200 mL、结存 800 mL；其它三个成品批次 995-300=695 mL，账实比对通过。
- 来料批 RM-KCL-20261007223208-02 存在未关闭 OOS；QA 页面展示阻断，未执行放行。
- 完整追溯图无 dangling edges。
- SYSTEM_ADMIN 在登录响应有各功能权限；权限测试证明非授权岗位返回 403，生产岗位无质量计划批准按钮，QA 有。

## 尚未关闭的 GxP 风险（不能因测试 PASS 而解除）
1. 已放行演示批 KCL-001 的标示 30ml:3g 与冻结处方 20g/1000mL 不一致，浓度差 5 倍；依照现有 DCP 评审，不回写受控历史。
2. 历史演示批成品检验、报告生成者为仓储岗位人员，发货确认者为 QA 岗位人员，需核实资质与职责。
3. 最终 eBR PDF 页面仍显示待归档。
4. QA 生产质量计划批准资格 Gate 设计候选仍待 Design Authority 决定。

## 安全边界
本轮仅改临时浏览器测试文件并执行只读核查/构建；未修改已冻结批次、检验数据、库存流水、QA 决定、电子签名；不把当前通过视为完整 GxP 合规验收。

## 后端及构建补充验证
- mes-security Maven 单元测试：15 PASS，0 FAIL，0 ERROR，exit 0。
- Maven 全项目 `-DskipTests compile`：exit 0。
- Maven 全项目 `-DskipTests package`：失败；mes-boot Spring Boot repackage 无法将当前 JAR 重命名为 `.jar.original`，疑似运行中的服务持有文件句柄；为避免中断本机服务未强制停进程或删除 JAR。该失败是打包阻断，尚非源码编译错误。
