# DCP005 有界验收补充（主设计批准）

批准来源：主对话01a115d2-8ace-7574-9a08-ac48dfc970cc，2026-10-10本次明确委派。批准只涵盖本模块验收约定、测试与文档；不改变业务路由、全局认证/权限、迁移或已部署服务。冻结v1.0.23和UI V2/T1–T6不变。

## 已批准断言

1. 已证实的auth/me401→refresh200→原请求重试200允许存在资源日志。要求最多一次续期、原请求成功重试、身份保留、无未处理脚本异常。未恢复业务401、失败续期后未退出、循环续期仍为失败。区分pageerror与HTTP资源错误，不能忽略所有401。旧FAIL与原报告保持不可改写，新测试另列。
2. 本模块无view权限，接受既有守卫回首页。拒绝必须发生在源组件创建/挂载前，不调用受限数据API。保留服务端403，不新增403页面。用实际router与组件的隔离自动化观察挂载钩子和API调用；最终截图不能独立证明全程无闪现。
3. 现有真实API16项/浏览器13项PASS是前置证据；无真实多行样本/拆分角色的部分采用同DEV数据库事务回滚fixture。权限以隔离上下文测试，不写sys_user/role/permission或凭据。业务字段采用正常收货单号、物料、批号、数量、单位，测试来源只放报告元数据与审计reason。多行取数实际走生产resolver/SQL；多个独立dataset不得相乘。并发测试用独立数据库事务验证已有唯一归属下两个同时竞争绑定均被唯一约束拒绝，不把它夸大成空类型首次竞争赢家测试。

## 执行约束和白名单

非普通UI任务的本次定向测试预算30分钟：16:15–16:45 UTC，最多两轮测试夹具/实现定位修复，同根因复发即停止；不延续产品自动修复，不降低断言。白名单：本补充；同一DCP005追加批准引用；MES_TASKS执行结果；两份frontend打印验收test.ts、一份backend打印验收IT.java；本机报告/日志/哈希。未完成用例明确NOT RUN。

物理读门：根AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS相关末尾、docs/PROJECT_BASELINE相关权威、同一DCP005、docs/development/database-and-validation-strategy.md；前次认证规范原文定位延用FINAL_READONLY_REVIEW_20261010.md。开始git status已记录，现有dirty文件保留。测试本机原checkout/main、同root.env与local/ci、MariaDB3306/hospital_pharma_mes_dev、Redis6379。禁止复制工程、另库、Flyway clean/repair、写浏览器、重新部署、大范围回归、提交推送。

不将隔离mock身份/传输/DOM测试描述为真实窄权限账号或真实浏览器；真实多行fixture不描述为已存在临床/生产样本。PDF工具限制保持已接受，不再扩大调查。
