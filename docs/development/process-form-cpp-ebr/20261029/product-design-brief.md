# 交互原型任务包：DRAFT / G0 IN REVIEW

先读input.md与analysis.md，角色/JTBD及AS-IS/TO-BE引用完整规格。保留Canvas/CPP及既有签名复核合同，先验证进入工序、字段与CPP、角色选择、保存结果、受控流程可达性与返回定位。

必须包含空态、权限只读、长内容、字段错误、角色服务失败、超时/并发及恢复。资料中的建议不是已批准新布局；新重设计需本地Vue原型完整走查与确切源码人工批准，再补齐G3/G3.5；不创建第二套设计器。

## 产品设计决策（不可省略）

### DEC-001 修复当前配置链还是重画整页？
- 决定：先复用现有已批准设计，修角色保存并验证配置链
- 被比较的方案：现有设计有界修复；整页工艺工作区重画
- 依据与取舍：保留用户目标，先隔离证据充分的阻断；不以代码方便削减原有行为
- 来源：SRC-001, SRC-002, SRC-004, SRC-007
- 批准状态：PROPOSED；影响：REQ-004, PAGE-002

## 原始用户需求与基线输入（不可被摘要替代）

# 用户输入与推进边界

2026-10-29 Asia/Shanghai，本聊天中的直接人类输入：

1. “mes的生产工艺还没有做好，使用产品业务经理的技能推进”。
2. 用户回答范围问题：“工序动态表单、CPP 与 eBR 配置”。
3. “加入到planing的管理”。

产品负责人：Codex using business-product-manager；评审为 SELF-REVIEW。
Planning 计划：`0df9356d-675b-4f37-8b7d-948948c8d694`，标题“MES 工序动态表单、CPP 与 eBR 配置闭环”。

本次范围：工序定位、表单设计、CPP、签名/复核配置、模板保存和受控发布、eBR消费及异常恢复。岗位分析属于基于规范的推演，没有声称进行了现场访谈。

当前仅形成需求与证据基线，不把本次推进指令当成对新页面版本、变更合同或实际发布/签名/迁移的批准。业务规则继续引用 v1.0.23 及已批准 DCP；正式任务状态只在 MES_TASKS.md。


## 权威完整规格（务必逐项读取，不得仅依赖上文摘要）

以下 JSON 是与源规格一致的完整业务合同，包括全部字段、动作、权限、
校验、状态、异常、流程、非目标、决策与验收。任何变化需返回产品需求源头。
上文仅作阅读导航，若有差异以本规格和冻结基线为准。
交接目标：Product Design。源规格 SHA-256：`1bee4683635f77607a6fb46cd53db9f058e6403b6b6b8b5e35e4b6936b2608ea`。

## 产品设计决策（不可省略）

### DEC-001 修复当前配置链还是重画整页？
- 决定：先复用现有已批准设计，修角色保存并验证配置链
- 被比较的方案：现有设计有界修复；整页工艺工作区重画
- 依据与取舍：保留用户目标，先隔离证据充分的阻断；不以代码方便削减原有行为
- 来源：SRC-001, SRC-002, SRC-004, SRC-007
- 批准状态：PROPOSED；影响：REQ-004, PAGE-002

## 原始用户需求与基线输入（不可被摘要替代）

# 用户输入与推进边界

2026-10-29 Asia/Shanghai，本聊天中的直接人类输入：

1. “mes的生产工艺还没有做好，使用产品业务经理的技能推进”。
2. 用户回答范围问题：“工序动态表单、CPP 与 eBR 配置”。
3. “加入到planing的管理”。

产品负责人：Codex using business-product-manager；评审为 SELF-REVIEW。
Planning 计划：`0df9356d-675b-4f37-8b7d-948948c8d694`，标题“MES 工序动态表单、CPP 与 eBR 配置闭环”。

本次范围：工序定位、表单设计、CPP、签名/复核配置、模板保存和受控发布、eBR消费及异常恢复。岗位分析属于基于规范的推演，没有声称进行了现场访谈。

当前仅形成需求与证据基线，不把本次推进指令当成对新页面版本、变更合同或实际发布/签名/迁移的批准。业务规则继续引用 v1.0.23 及已批准 DCP；正式任务状态只在 MES_TASKS.md。


## 权威完整规格（务必逐项读取，不得仅依赖上文摘要）

以下 JSON 是与源规格一致的完整业务合同，包括全部字段、动作、权限、
校验、状态、异常、流程、非目标、决策与验收。任何变化需返回产品需求源头。
上文仅作阅读导航，若有差异以本规格和冻结基线为准。
交接目标：Product Design。源规格 SHA-256：`1bee4683635f77607a6fb46cd53db9f058e6403b6b6b8b5e35e4b6936b2608ea`。

## 产品设计决策（不可省略）

### DEC-001 修复当前配置链还是重画整页？
- 决定：先复用现有已批准设计，修角色保存并验证配置链
- 被比较的方案：现有设计有界修复；整页工艺工作区重画
- 依据与取舍：保留用户目标，先隔离证据充分的阻断；不以代码方便削减原有行为
- 来源：SRC-001, SRC-002, SRC-004, SRC-007
- 批准状态：PROPOSED；影响：REQ-004, PAGE-002

## 原始用户需求与基线输入（不可被摘要替代）

# 用户输入与推进边界

2026-10-29 Asia/Shanghai，本聊天中的直接人类输入：

1. “mes的生产工艺还没有做好，使用产品业务经理的技能推进”。
2. 用户回答范围问题：“工序动态表单、CPP 与 eBR 配置”。
3. “加入到planing的管理”。

产品负责人：Codex using business-product-manager；评审为 SELF-REVIEW。
Planning 计划：`0df9356d-675b-4f37-8b7d-948948c8d694`，标题“MES 工序动态表单、CPP 与 eBR 配置闭环”。

本次范围：工序定位、表单设计、CPP、签名/复核配置、模板保存和受控发布、eBR消费及异常恢复。岗位分析属于基于规范的推演，没有声称进行了现场访谈。

当前仅形成需求与证据基线，不把本次推进指令当成对新页面版本、变更合同或实际发布/签名/迁移的批准。业务规则继续引用 v1.0.23 及已批准 DCP；正式任务状态只在 MES_TASKS.md。


## 权威完整规格（务必逐项读取，不得仅依赖上文摘要）

以下 JSON 是与源规格一致的完整业务合同，包括全部字段、动作、权限、
校验、状态、异常、流程、非目标、决策与验收。任何变化需返回产品需求源头。
上文仅作阅读导航，若有差异以本规格和冻结基线为准。
交接目标：Product Design。源规格 SHA-256：`1bee4683635f77607a6fb46cd53db9f058e6403b6b6b8b5e35e4b6936b2608ea`。

## 产品设计决策（不可省略）

### DEC-001 修复当前配置链还是重画整页？
- 决定：先复用现有已批准设计，修角色保存并验证配置链
- 被比较的方案：现有设计有界修复；整页工艺工作区重画
- 依据与取舍：保留用户目标，先隔离证据充分的阻断；不以代码方便削减原有行为
- 来源：SRC-001, SRC-002, SRC-004, SRC-007
- 批准状态：PROPOSED；影响：REQ-004, PAGE-002

## 原始用户需求与基线输入（不可被摘要替代）

# 用户输入与推进边界

2026-10-29 Asia/Shanghai，本聊天中的直接人类输入：

1. “mes的生产工艺还没有做好，使用产品业务经理的技能推进”。
2. 用户回答范围问题：“工序动态表单、CPP 与 eBR 配置”。
3. “加入到planing的管理”。

产品负责人：Codex using business-product-manager；评审为 SELF-REVIEW。
Planning 计划：`0df9356d-675b-4f37-8b7d-948948c8d694`，标题“MES 工序动态表单、CPP 与 eBR 配置闭环”。

本次范围：工序定位、表单设计、CPP、签名/复核配置、模板保存和受控发布、eBR消费及异常恢复。岗位分析属于基于规范的推演，没有声称进行了现场访谈。

当前仅形成需求与证据基线，不把本次推进指令当成对新页面版本、变更合同或实际发布/签名/迁移的批准。业务规则继续引用 v1.0.23 及已批准 DCP；正式任务状态只在 MES_TASKS.md。

## 后续直接授权（按发生顺序保留）

- 用户“修改不符合的地方”：允许修复既有批准合同内缺陷，不授权重画待批准工艺整页。
- 首次重复失败暂停后，用户“批准，按此范围继续修复”：限角色选择控件可访问名称/装配，原断言、角色ID合同、布局不变。
- 再次暂停后，用户“批准这10分钟的限定诊断与修复”：先测实际accessible name，再仅修标签关联并重跑原定向测试；最多10分钟，累计轮次保留。

最后一次授权范围自2026-10-29 10:14:26 Asia/Shanghai记录并执行；截至10:19:47，定向15/15及build通过。未因此扩大到真实业务保存、审批、签名或发布。


## 权威完整规格（务必逐项读取，不得仅依赖上文摘要）

以下 JSON 是与源规格一致的完整业务合同，包括全部字段、动作、权限、
校验、状态、异常、流程、非目标、决策与验收。任何变化需返回产品需求源头。
上文仅作阅读导航，若有差异以本规格和冻结基线为准。
交接目标：Product Design。源规格 SHA-256：`1bee4683635f77607a6fb46cd53db9f058e6403b6b6b8b5e35e4b6936b2608ea`。

## 产品设计决策（不可省略）

### DEC-001 修复当前配置链还是重画整页？
- 决定：先复用现有已批准设计，修角色保存并验证配置链
- 被比较的方案：现有设计有界修复；整页工艺工作区重画
- 依据与取舍：保留用户目标，先隔离证据充分的阻断；不以代码方便削减原有行为
- 来源：SRC-001, SRC-002, SRC-004, SRC-007
- 批准状态：PROPOSED；影响：REQ-004, PAGE-002

## 原始用户需求与基线输入（不可被摘要替代）

# 用户输入与推进边界

2026-10-29 Asia/Shanghai，本聊天中的直接人类输入：

1. “mes的生产工艺还没有做好，使用产品业务经理的技能推进”。
2. 用户回答范围问题：“工序动态表单、CPP 与 eBR 配置”。
3. “加入到planing的管理”。

产品负责人：Codex using business-product-manager；评审为 SELF-REVIEW。
Planning 计划：`0df9356d-675b-4f37-8b7d-948948c8d694`，标题“MES 工序动态表单、CPP 与 eBR 配置闭环”。

本次范围：工序定位、表单设计、CPP、签名/复核配置、模板保存和受控发布、eBR消费及异常恢复。岗位分析属于基于规范的推演，没有声称进行了现场访谈。

当前仅形成需求与证据基线，不把本次推进指令当成对新页面版本、变更合同或实际发布/签名/迁移的批准。业务规则继续引用 v1.0.23 及已批准 DCP；正式任务状态只在 MES_TASKS.md。

## 后续直接授权（按发生顺序保留）

- 用户“修改不符合的地方”：允许修复既有批准合同内缺陷，不授权重画待批准工艺整页。
- 首次重复失败暂停后，用户“批准，按此范围继续修复”：限角色选择控件可访问名称/装配，原断言、角色ID合同、布局不变。
- 再次暂停后，用户“批准这10分钟的限定诊断与修复”：先测实际accessible name，再仅修标签关联并重跑原定向测试；最多10分钟，累计轮次保留。

最后一次授权范围自2026-10-29 10:14:26 Asia/Shanghai记录并执行；截至10:19:47，定向15/15及build通过。未因此扩大到真实业务保存、审批、签名或发布。


## 权威完整规格（务必逐项读取，不得仅依赖上文摘要）

以下 JSON 是与源规格一致的完整业务合同，包括全部字段、动作、权限、
校验、状态、异常、流程、非目标、决策与验收。任何变化需返回产品需求源头。
上文仅作阅读导航，若有差异以本规格和冻结基线为准。
交接目标：Product Design。源规格 SHA-256：`1bee4683635f77607a6fb46cd53db9f058e6403b6b6b8b5e35e4b6936b2608ea`。

<!-- BPM-AUTHORITATIVE-SPEC-BEGIN -->
```json
{
  "acceptance_criteria": [
    {
      "given": "受控组织内、已有工艺工序与可读模板；对应状态/权限满足",
      "id": "AC-001",
      "requirement_refs": [
        "REQ-001"
      ],
      "test_case_id": "TC-001",
      "then": "完整任务及恢复流满足源合同，结果可定位；权限/只读/空态/错误/并发负例留证",
      "verification_level": "BROWSER",
      "when": "工序入口保持工艺、工序、表单和模板身份可辨识；返回能定位来源"
    },
    {
      "given": "受控组织内、已有工艺工序与可读模板；对应状态/权限满足",
      "id": "AC-002",
      "requirement_refs": [
        "REQ-002"
      ],
      "test_case_id": "TC-002",
      "then": "完整任务及恢复流满足源合同，结果可定位；权限/只读/空态/错误/并发负例留证",
      "verification_level": "BROWSER",
      "when": "保留现有Canvas与字段定义，表单切换及保存不能丢失画布或产生悬空引用"
    },
    {
      "given": "受控组织内、已有工艺工序与可读模板；对应状态/权限满足",
      "id": "AC-003",
      "requirement_refs": [
        "REQ-003"
      ],
      "test_case_id": "TC-003",
      "then": "完整任务及恢复流满足源合同，结果可定位；权限/只读/空态/错误/并发负例留证",
      "verification_level": "BROWSER",
      "when": "CPP仅引用当前表单，条件/DSL/预览一致、四类动作按既有合同及复核前提配置"
    },
    {
      "given": "受控组织内、已有工艺工序与可读模板；对应状态/权限满足",
      "id": "AC-004",
      "requirement_refs": [
        "REQ-004"
      ],
      "test_case_id": "TC-004",
      "then": "新增签名和复核能经角色选择取得ID并保存重读；只读历史显示冻结名称且无现行角色查询",
      "verification_level": "BROWSER",
      "when": "签名/复核使用有效角色ID，冻结显示原快照，不回退编码或重新查询历史身份"
    },
    {
      "given": "受控组织内、已有工艺工序与可读模板；对应状态/权限满足",
      "id": "AC-005",
      "requirement_refs": [
        "REQ-005"
      ],
      "test_case_id": "TC-005",
      "then": "完整任务及恢复流满足源合同，结果可定位；权限/只读/空态/错误/并发负例留证",
      "verification_level": "BROWSER",
      "when": "保存/检查/受控生命周期操作可达，错误恢复不覆盖输入且不自动审批发布"
    },
    {
      "given": "受控组织内、已有工艺工序与可读模板；对应状态/权限满足",
      "id": "AC-006",
      "requirement_refs": [
        "REQ-006"
      ],
      "test_case_id": "TC-006",
      "then": "完整任务及恢复流满足源合同，结果可定位；权限/只读/空态/错误/并发负例留证",
      "verification_level": "REAL_API",
      "when": "eBR消费冻结定义，CPP动作与复核/签名按原合同执行，后续维护不改历史"
    }
  ],
  "authority_refs": [
    "SRC-001",
    "SRC-002",
    "SRC-003",
    "SRC-004"
  ],
  "business_objects": [
    {
      "id": "OBJ-001",
      "name": "生产工艺当前工序",
      "owner": "mes-process",
      "source_of_truth": "currentDefinition",
      "unique_key": "org_id/package_id/operation identity"
    },
    {
      "id": "OBJ-002",
      "name": "eBR模板/表单",
      "owner": "mes-ebr",
      "source_of_truth": "受控模板定义",
      "unique_key": "template version ID/formCode"
    },
    {
      "id": "OBJ-003",
      "name": "CPP规则",
      "owner": "mes-ebr",
      "source_of_truth": "ebr_rule_def/cppConfigJson+expression",
      "unique_key": "template/ruleCode"
    },
    {
      "id": "OBJ-004",
      "name": "签名复核角色策略",
      "owner": "mes-ebr/IAM",
      "source_of_truth": "当前sys_role ID及冻结role snapshots",
      "unique_key": "requiredRoleId/独立签署用户ID"
    },
    {
      "id": "OBJ-005",
      "name": "批次eBR冻结证据",
      "owner": "production/mes-ebr",
      "source_of_truth": "批次定义快照及执行证据",
      "unique_key": "batch/form/occurrence"
    }
  ],
  "business_rules": [
    {
      "id": "RULE-001",
      "source_refs": [
        "SRC-001",
        "SRC-002"
      ],
      "statement": "工序入口保持工艺、工序、表单和模板身份可辨识；返回能定位来源",
      "status": "PROPOSED"
    },
    {
      "id": "RULE-002",
      "source_refs": [
        "SRC-001",
        "SRC-002"
      ],
      "statement": "保留现有Canvas与字段定义，表单切换及保存不能丢失画布或产生悬空引用",
      "status": "PROPOSED"
    },
    {
      "id": "RULE-003",
      "source_refs": [
        "SRC-003",
        "SRC-002"
      ],
      "statement": "CPP仅引用当前表单，条件/DSL/预览一致、四类动作按既有合同及复核前提配置",
      "status": "PROPOSED"
    },
    {
      "id": "RULE-004",
      "source_refs": [
        "SRC-004",
        "SRC-002"
      ],
      "statement": "签名/复核使用有效角色ID，冻结显示原快照，不回退编码或重新查询历史身份",
      "status": "CONFIRMED"
    },
    {
      "id": "RULE-005",
      "source_refs": [
        "SRC-001",
        "SRC-002"
      ],
      "statement": "保存/检查/受控生命周期操作可达，错误恢复不覆盖输入且不自动审批发布",
      "status": "PROPOSED"
    },
    {
      "id": "RULE-006",
      "source_refs": [
        "SRC-001",
        "SRC-002"
      ],
      "statement": "eBR消费冻结定义，CPP动作与复核/签名按原合同执行，后续维护不改历史",
      "status": "PROPOSED"
    }
  ],
  "design_constraints": [
    {
      "id": "CON-001",
      "source_refs": [
        "SRC-002",
        "SRC-003"
      ],
      "statement": "继承UI V2及当前页面模板；标签控件同行；完整审查manifest验证前不报READY"
    },
    {
      "id": "CON-002",
      "source_refs": [
        "SRC-002",
        "SRC-004"
      ],
      "statement": "工艺与eBR生命周期分离，历史证据不变；新合同须单独批准"
    }
  ],
  "design_decisions": [
    {
      "affected_refs": [
        "REQ-004",
        "PAGE-002"
      ],
      "alternatives": [
        "现有设计有界修复",
        "整页工艺工作区重画"
      ],
      "approval_status": "PROPOSED",
      "id": "DEC-001",
      "owner": "BUSINESS_PRODUCT_MANAGER",
      "question": "修复当前配置链还是重画整页？",
      "rationale": "保留用户目标，先隔离证据充分的阻断；不以代码方便削减原有行为",
      "rejected_reasons": [
        "整页尚未批准且含工艺版本合同冲突，不能解决当前保存阻断"
      ],
      "selected": "先复用现有已批准设计，修角色保存并验证配置链",
      "source_refs": [
        "SRC-001",
        "SRC-002",
        "SRC-004",
        "SRC-007"
      ]
    }
  ],
  "edge_cases": [
    {
      "expected": "配置保留，最新请求才可更新选项",
      "id": "EDGE-001",
      "recovery": "显式重试，不退回角色编码",
      "trigger": "角色选项请求失败或搜索响应乱序"
    },
    {
      "expected": "只读且不实时覆盖冻结角色标签",
      "id": "EDGE-002",
      "recovery": "沿原版本与权限流程处理",
      "trigger": "历史冻结版本或无权限"
    },
    {
      "expected": "不把超时等同失败，不自动覆盖",
      "id": "EDGE-003",
      "recovery": "沿既有幂等/版本策略核对并保留输入",
      "trigger": "保存超时或并发变化"
    }
  ],
  "engineering_contract": {
    "approval_reference": null,
    "approved_artifact_ref": null,
    "authorized_existing_exception": "ROLE_ID_SIGNING_DESIGN_LOCK_20261011第5项 + 当前直接修改授权，仅角色有界缺陷；追加诊断暂停",
    "code_quality_gate": "REQUIRE_REAL_EVIDENCE",
    "implementation_authorized": false,
    "prohibited_shortcuts": [
      "不削弱角色ID校验",
      "不以mock冒充运行验收",
      "不擅自改布局/权限/快照",
      "不将双包DRAFT视为G3.5批准"
    ],
    "requirement_refs": [
      "REQ-001",
      "REQ-002",
      "REQ-003",
      "REQ-004",
      "REQ-005",
      "REQ-006"
    ],
    "technology_conflict_action": "CHANGE_REQUEST"
  },
  "evidence": [
    {
      "claim": "用户要求以产品业务经理推进，并锁定表单/CPP/eBR范围及Planning管理",
      "id": "SRC-001",
      "source": "input.md",
      "type": "FACT"
    },
    {
      "claim": "当前工艺定义无版本生命周期；eBR保持受控模板生命周期",
      "id": "SRC-002",
      "source": "docs/architecture/BASIC_CURRENT_DEFINITION_CONTRACT.md",
      "type": "FACT"
    },
    {
      "claim": "CPP只引用当前表单字段，四类动作与安全DSL；确切批准范围由设计锁约束",
      "id": "SRC-003",
      "source": "docs/development/decisions/PROCESS_DYNAMIC_FORM_CPP_PROTOTYPE_20261013.md",
      "type": "FACT"
    },
    {
      "claim": "角色选择器同位置替换已有批准，schema2提交角色ID，历史显示冻结快照",
      "id": "SRC-004",
      "source": "docs/development/decisions/ROLE_ID_SIGNING_DESIGN_LOCK_20261011.md",
      "type": "FACT"
    },
    {
      "claim": "现有源码新增规则只绑定legacy角色编码，转换要求ID，已有最小复现",
      "id": "SRC-005",
      "source": "role-save-repro.mjs",
      "type": "FACT"
    },
    {
      "claim": "工艺维护人员优先关注当前工序、表单及版本状态，再设计字段和CPP",
      "id": "SRC-006",
      "source": "analysis.md",
      "type": "INFERENCE"
    },
    {
      "claim": "整页待批准原型含工艺版本审批，与现行当前定义合同冲突，排除该部分",
      "id": "SRC-007",
      "source": "docs/development/decisions/PRODUCTION_PROCESS_PROTOTYPE_OPTIMIZATION_20261013.md",
      "type": "CONFLICT"
    }
  ],
  "feature_id": "PROC-FORM-CPP-EBR-001",
  "frozen_baseline": {
    "allowed_changes": [
      "证据分析/设计准备",
      "既有ROLE-ID设计例外内角色输入缺陷修正，当前追加诊断暂停"
    ],
    "items": [
      "FINAL BASELINE COMPLETE v1.0.23及批准有界DCP",
      "UI V2/T1–T6",
      "工艺当前定义与eBR受控生命周期分离",
      "历史canonical/hash/签名/快照、既有API/权限"
    ],
    "requires_design_change": false
  },
  "g0": {
    "checks": {
      "BrownfieldFit": "PASS",
      "BusinessGoal": "PASS",
      "Efficiency": "ASSUMPTION",
      "Epistemics": "PASS",
      "Flows": "ASSUMPTION",
      "HandoffReady": "BLOCKED",
      "Roles": "ASSUMPTION",
      "RulesStatePermission": "ASSUMPTION",
      "ScreenBehavior": "ASSUMPTION",
      "Traceability": "PASS",
      "UserFirst": "PASS"
    },
    "continue_scope": "只允许调研/原型准备，以及既有ROLE-ID批准例外中的单独缺陷包；角色追加诊断因重复签名暂停待用户限定授权，整包不得进入G4",
    "status": "BLOCKED"
  },
  "impact_analysis": {
    "api": [
      "复用GET /ebr/role-options及现有模板读写，其他消费权限待核验"
    ],
    "audit": [
      "eBR原有受控保存与历史冻结不变"
    ],
    "data": [
      "无新迁移/业务写入"
    ],
    "documents": [
      "本目录与MES_TASKS.md"
    ],
    "integrations": [
      "工序→模板→eBR冻结消费"
    ],
    "permissions": [
      "保持现有码；执行消费精确码待核验，不作为开发授权"
    ],
    "reuse": [
      "Canvas Editor",
      "CppRuleDesigner",
      "RolePicker",
      "ebrRoleOptions",
      "definitionForSave",
      "原有规则引擎与审批"
    ],
    "screens": [
      "PAGE-001",
      "PAGE-002",
      "PAGE-003",
      "PAGE-004"
    ],
    "states": [
      "无新增状态"
    ],
    "tests": [
      "角色组件集成/序列化/冻结显示/错误恢复；后续真实G5"
    ]
  },
  "interactions": [
    {
      "acceptance_refs": [
        "AC-001"
      ],
      "action_ref": "ACT-001",
      "behavior": "工序入口保持工艺、工序、表单和模板身份可辨识；返回能定位来源",
      "id": "INT-001-1",
      "screen_ref": "PAGE-001"
    },
    {
      "acceptance_refs": [
        "AC-002"
      ],
      "action_ref": "ACT-002",
      "behavior": "保留现有Canvas与字段定义，表单切换及保存不能丢失画布或产生悬空引用",
      "id": "INT-002-1",
      "screen_ref": "PAGE-002"
    },
    {
      "acceptance_refs": [
        "AC-003"
      ],
      "action_ref": "ACT-003",
      "behavior": "CPP仅引用当前表单，条件/DSL/预览一致、四类动作按既有合同及复核前提配置",
      "id": "INT-003-1",
      "screen_ref": "PAGE-002"
    },
    {
      "acceptance_refs": [
        "AC-004"
      ],
      "action_ref": "ACT-004",
      "behavior": "签名/复核使用有效角色ID，冻结显示原快照，不回退编码或重新查询历史身份",
      "id": "INT-004-1",
      "screen_ref": "PAGE-002"
    },
    {
      "acceptance_refs": [
        "AC-005"
      ],
      "action_ref": "ACT-005",
      "behavior": "保存/检查/受控生命周期操作可达，错误恢复不覆盖输入且不自动审批发布",
      "id": "INT-005-1",
      "screen_ref": "PAGE-002"
    },
    {
      "acceptance_refs": [
        "AC-005"
      ],
      "action_ref": "ACT-006",
      "behavior": "保存/检查/受控生命周期操作可达，错误恢复不覆盖输入且不自动审批发布",
      "id": "INT-005-2",
      "screen_ref": "PAGE-003"
    },
    {
      "acceptance_refs": [
        "AC-006"
      ],
      "action_ref": "ACT-007",
      "behavior": "eBR消费冻结定义，CPP动作与复核/签名按原合同执行，后续维护不改历史",
      "id": "INT-006-1",
      "screen_ref": "PAGE-004"
    }
  ],
  "mode": "Enterprise",
  "open_decisions": [
    {
      "id": "DEC-002",
      "owner": "产品负责人/用户",
      "question": "真实CPP业务条件是否需要超出现行两级条件组？",
      "status": "OPEN"
    },
    {
      "id": "DEC-003",
      "owner": "用户",
      "question": "重复选择器失败后的限定下一阶段授权",
      "status": "AWAITING_APPROVAL"
    }
  ],
  "permissions": [
    {
      "action_ref": "ACT-001",
      "id": "PERM-001",
      "role_ref": "ROLE-001",
      "rule": "process:package:view + ebr:template:view"
    },
    {
      "action_ref": "ACT-002",
      "id": "PERM-002",
      "role_ref": "ROLE-001",
      "rule": "ebr:template:update"
    },
    {
      "action_ref": "ACT-003",
      "id": "PERM-003",
      "role_ref": "ROLE-001",
      "rule": "ebr:template:update"
    },
    {
      "action_ref": "ACT-004",
      "id": "PERM-004",
      "role_ref": "ROLE-001",
      "rule": "ebr:template:view + ebr:template:update"
    },
    {
      "action_ref": "ACT-005",
      "id": "PERM-005",
      "role_ref": "ROLE-001",
      "rule": "ebr:template:update + ebr:designer:edit"
    },
    {
      "action_ref": "ACT-006",
      "id": "PERM-006",
      "role_ref": "ROLE-001",
      "rule": "ebr:template:submit / ebr:template:approve / ebr:template:publish"
    },
    {
      "action_ref": "ACT-007",
      "id": "PERM-007",
      "role_ref": "ROLE-003",
      "rule": "ebr:form:edit / ebr:form:submit（具体代码待消费合同核验）"
    }
  ],
  "personas": [
    {
      "evidence_level": "INFERENCE",
      "id": "ROLE-001",
      "jtbd": "维护一道工序时，完成表单与规则配置并确认保存结果",
      "name": "工艺维护岗位",
      "responsibilities": [
        "配置工序记录字段、CPP与签名复核策略"
      ]
    },
    {
      "evidence_level": "INFERENCE",
      "id": "ROLE-002",
      "jtbd": "核实正确模板版本，按授权受控操作",
      "name": "模板批准岗位",
      "responsibilities": [
        "原有独立审批与发布控制"
      ]
    },
    {
      "evidence_level": "INFERENCE",
      "id": "ROLE-003",
      "jtbd": "在原工序使用正确记录及CPP规则",
      "name": "生产操作与复核岗位",
      "responsibilities": [
        "按冻结定义填写并完成既有门禁"
      ]
    }
  ],
  "problem": {
    "as_is": "配置能力分布在工艺、设计器和模板详情；角色输入与保存转换存在源码确认的不匹配；完整运行闭环待证",
    "cost": "配置可能无法保存、模板身份和下一步不清；不能以历史回归代替本轮实际验证",
    "goal": "工艺维护人员完成工序表单、CPP及角色配置，使eBR使用同一受控定义",
    "non_goals": [
      "恢复工艺版本审批",
      "重画整页未批准工艺原型",
      "新增任意递归CPP存储合同",
      "更改历史证据或自动审批发布",
      "SOP/计划工时/照片/UI V3"
    ],
    "success_metrics": [
      "角色新增/保存/重读闭环成功",
      "工序表单身份及CPP引用无歧义",
      "受控发布及冻结消费按既有合同可定位"
    ]
  },
  "profile": "hospital-mes",
  "requirements": [
    {
      "acceptance_refs": [
        "AC-001"
      ],
      "id": "REQ-001",
      "priority": "MUST",
      "screen_refs": [
        "PAGE-001"
      ],
      "source_refs": [
        "SRC-001",
        "SRC-002"
      ],
      "statement": "工序入口保持工艺、工序、表单和模板身份可辨识；返回能定位来源",
      "status": "PROPOSED",
      "workflow_refs": [
        "FLOW-002"
      ]
    },
    {
      "acceptance_refs": [
        "AC-002"
      ],
      "id": "REQ-002",
      "priority": "MUST",
      "screen_refs": [
        "PAGE-002"
      ],
      "source_refs": [
        "SRC-001",
        "SRC-002"
      ],
      "statement": "保留现有Canvas与字段定义，表单切换及保存不能丢失画布或产生悬空引用",
      "status": "PROPOSED",
      "workflow_refs": [
        "FLOW-002"
      ]
    },
    {
      "acceptance_refs": [
        "AC-003"
      ],
      "id": "REQ-003",
      "priority": "MUST",
      "screen_refs": [
        "PAGE-002"
      ],
      "source_refs": [
        "SRC-003",
        "SRC-002"
      ],
      "statement": "CPP仅引用当前表单，条件/DSL/预览一致、四类动作按既有合同及复核前提配置",
      "status": "PROPOSED",
      "workflow_refs": [
        "FLOW-002"
      ]
    },
    {
      "acceptance_refs": [
        "AC-004"
      ],
      "id": "REQ-004",
      "priority": "MUST",
      "screen_refs": [
        "PAGE-002"
      ],
      "source_refs": [
        "SRC-004",
        "SRC-002"
      ],
      "statement": "签名/复核使用有效角色ID，冻结显示原快照，不回退编码或重新查询历史身份",
      "status": "CONFIRMED",
      "workflow_refs": [
        "FLOW-002"
      ]
    },
    {
      "acceptance_refs": [
        "AC-005"
      ],
      "id": "REQ-005",
      "priority": "MUST",
      "screen_refs": [
        "PAGE-002",
        "PAGE-003"
      ],
      "source_refs": [
        "SRC-001",
        "SRC-002"
      ],
      "statement": "保存/检查/受控生命周期操作可达，错误恢复不覆盖输入且不自动审批发布",
      "status": "PROPOSED",
      "workflow_refs": [
        "FLOW-002"
      ]
    },
    {
      "acceptance_refs": [
        "AC-006"
      ],
      "id": "REQ-006",
      "priority": "MUST",
      "screen_refs": [
        "PAGE-004"
      ],
      "source_refs": [
        "SRC-001",
        "SRC-002"
      ],
      "statement": "eBR消费冻结定义，CPP动作与复核/签名按原合同执行，后续维护不改历史",
      "status": "PROPOSED",
      "workflow_refs": [
        "FLOW-002"
      ]
    }
  ],
  "review_findings": [
    {
      "evidence": [
        "role-save-repro.mjs",
        "role-green-fixture.log",
        "typecheck.log"
      ],
      "finding_id": "F-001",
      "impact": "新签名/复核及CPP要求复核配置闭环未证明",
      "mode": "IMPLEMENTATION_REVIEW",
      "recommendation": "按既有批准例外复用RolePicker，保持断言并定位重复失败",
      "severity": "HIGH",
      "spec_refs": [
        "REQ-004",
        "AC-004"
      ],
      "status": "OPEN",
      "title": "角色选择与ID保存合同不匹配，当前接入尚有失败用例",
      "verification_level": "SOURCE_REVIEW"
    }
  ],
  "sample_data": {
    "business_values": {},
    "dataset_id": "source-contract-in-memory-only"
  },
  "schema_version": "2.2.0",
  "screens": [
    {
      "actions": [
        {
          "enabled_when": "原有可编辑/状态门禁满足且无进行中提交",
          "failure_recovery": "保留输入；字段校验定位；超时先核对结果再重试；409不自动覆盖；权限改变转只读",
          "id": "ACT-001",
          "label": "设计表单",
          "permission_refs": [
            "PERM-001"
          ],
          "success": "定位同一模板/工序/表单，保留返回上下文",
          "visible_when": "原有权限且允许动作；本规格不新增权限"
        }
      ],
      "entry": "/process/packages/:id 工序表单",
      "fields": [
        {
          "id": "FIELD-001",
          "label": "生产工艺与工序",
          "readonly_when": "无权限、冻结或非编辑状态",
          "required_when": "按原有当前定义/模板/执行合同",
          "source": "OBJ-001",
          "type": "contract-defined",
          "validation_refs": [
            "VAL-001"
          ]
        },
        {
          "id": "FIELD-002",
          "label": "表单及模板版本",
          "readonly_when": "无权限、冻结或非编辑状态",
          "required_when": "按原有当前定义/模板/执行合同",
          "source": "OBJ-002",
          "type": "contract-defined",
          "validation_refs": [
            "VAL-002"
          ]
        }
      ],
      "goal": "从工序找到正确模板及表单",
      "id": "PAGE-001",
      "name": "工序表单定位",
      "return_path": "工艺列表",
      "roles": [
        "ROLE-001"
      ],
      "states": [
        "default",
        "loading",
        "empty",
        "error",
        "conflict",
        "success",
        "readonly",
        "long-content",
        "focus",
        "validation-error"
      ],
      "template": "T4"
    },
    {
      "actions": [
        {
          "enabled_when": "原有可编辑/状态门禁满足且无进行中提交",
          "failure_recovery": "保留输入；字段校验定位；超时先核对结果再重试；409不自动覆盖；权限改变转只读",
          "id": "ACT-002",
          "label": "表单设计",
          "permission_refs": [
            "PERM-002"
          ],
          "success": "当前表单字段和画布一致",
          "visible_when": "原有权限且允许动作；本规格不新增权限"
        },
        {
          "enabled_when": "原有可编辑/状态门禁满足且无进行中提交",
          "failure_recovery": "保留输入；字段校验定位；超时先核对结果再重试；409不自动覆盖；权限改变转只读",
          "id": "ACT-003",
          "label": "CPP规则设计",
          "permission_refs": [
            "PERM-003"
          ],
          "success": "条件、预览和安全DSL一致且仅引用本表单",
          "visible_when": "原有权限且允许动作；本规格不新增权限"
        },
        {
          "enabled_when": "原有可编辑/状态门禁满足且无进行中提交",
          "failure_recovery": "保留输入；字段校验定位；超时先核对结果再重试；409不自动覆盖；权限改变转只读",
          "id": "ACT-004",
          "label": "选择角色",
          "permission_refs": [
            "PERM-004"
          ],
          "success": "提交有效requiredRoleId，冻结态显示原快照",
          "visible_when": "原有权限且允许动作；本规格不新增权限"
        }
      ],
      "entry": "/ebr/templates/:id/designer?formCode=...",
      "fields": [
        {
          "id": "FIELD-003",
          "label": "当前表单字段与画布",
          "readonly_when": "无权限、冻结或非编辑状态",
          "required_when": "按原有当前定义/模板/执行合同",
          "source": "OBJ-002",
          "type": "contract-defined",
          "validation_refs": [
            "VAL-003"
          ]
        },
        {
          "id": "FIELD-004",
          "label": "CPP条件与超限动作",
          "readonly_when": "无权限、冻结或非编辑状态",
          "required_when": "按原有当前定义/模板/执行合同",
          "source": "OBJ-003",
          "type": "contract-defined",
          "validation_refs": [
            "VAL-004"
          ]
        },
        {
          "id": "FIELD-005",
          "label": "签署及复核角色",
          "readonly_when": "无权限、冻结或非编辑状态",
          "required_when": "按原有当前定义/模板/执行合同",
          "source": "OBJ-004",
          "type": "contract-defined",
          "validation_refs": [
            "VAL-005"
          ]
        }
      ],
      "goal": "设计字段及CPP，配置签名/复核并保存受控草稿",
      "id": "PAGE-002",
      "name": "动态表单与CPP设计",
      "return_path": "原生产工艺",
      "roles": [
        "ROLE-001"
      ],
      "states": [
        "default",
        "loading",
        "empty",
        "error",
        "conflict",
        "success",
        "readonly",
        "long-content",
        "focus",
        "validation-error"
      ],
      "template": "T2"
    },
    {
      "actions": [
        {
          "enabled_when": "原有可编辑/状态门禁满足且无进行中提交",
          "failure_recovery": "保留输入；字段校验定位；超时先核对结果再重试；409不自动覆盖；权限改变转只读",
          "id": "ACT-005",
          "label": "保存草稿与检查模板",
          "permission_refs": [
            "PERM-005"
          ],
          "success": "正常受控保存重读，静态错误定位",
          "visible_when": "原有权限且允许动作；本规格不新增权限"
        },
        {
          "enabled_when": "原有可编辑/状态门禁满足且无进行中提交",
          "failure_recovery": "保留输入；字段校验定位；超时先核对结果再重试；409不自动覆盖；权限改变转只读",
          "id": "ACT-006",
          "label": "提交审批/批准模板/发布模板",
          "permission_refs": [
            "PERM-006"
          ],
          "success": "按既有允许动作及独立用户推进；不代签",
          "visible_when": "原有权限且允许动作；本规格不新增权限"
        }
      ],
      "entry": "/ebr/templates/:id",
      "fields": [
        {
          "id": "FIELD-006",
          "label": "模板版本与状态",
          "readonly_when": "无权限、冻结或非编辑状态",
          "required_when": "按原有当前定义/模板/执行合同",
          "source": "OBJ-002",
          "type": "contract-defined",
          "validation_refs": [
            "VAL-006"
          ]
        },
        {
          "id": "FIELD-007",
          "label": "受控操作原因",
          "readonly_when": "无权限、冻结或非编辑状态",
          "required_when": "按原有当前定义/模板/执行合同",
          "source": "OBJ-002",
          "type": "contract-defined",
          "validation_refs": [
            "VAL-007"
          ]
        }
      ],
      "goal": "保存草稿后可定位同一模板，并按现有权限提交、批准、发布",
      "id": "PAGE-003",
      "name": "模板保存与受控生命周期",
      "return_path": "来源工艺/模板列表",
      "roles": [
        "ROLE-001",
        "ROLE-002"
      ],
      "states": [
        "default",
        "loading",
        "empty",
        "error",
        "conflict",
        "success",
        "readonly",
        "long-content",
        "focus",
        "validation-error"
      ],
      "template": "T3"
    },
    {
      "actions": [
        {
          "enabled_when": "原有可编辑/状态门禁满足且无进行中提交",
          "failure_recovery": "保留输入；字段校验定位；超时先核对结果再重试；409不自动覆盖；权限改变转只读",
          "id": "ACT-007",
          "label": "按原有执行门禁填写与提交",
          "permission_refs": [
            "PERM-007"
          ],
          "success": "超限及复核门禁按冻结定义运行且证据可定位",
          "visible_when": "原有权限且允许动作；本规格不新增权限"
        }
      ],
      "entry": "既有生产执行eBR入口",
      "fields": [
        {
          "id": "FIELD-008",
          "label": "冻结表单、规则与执行结果",
          "readonly_when": "无权限、冻结或非编辑状态",
          "required_when": "按原有当前定义/模板/执行合同",
          "source": "OBJ-005",
          "type": "contract-defined",
          "validation_refs": [
            "VAL-008"
          ]
        }
      ],
      "goal": "确认生产批次消费正确冻结表单与规则",
      "id": "PAGE-004",
      "name": "eBR冻结定义消费",
      "return_path": "原批次工序",
      "roles": [
        "ROLE-003"
      ],
      "states": [
        "default",
        "loading",
        "empty",
        "error",
        "conflict",
        "success",
        "readonly",
        "long-content",
        "focus",
        "validation-error"
      ],
      "template": "T5"
    }
  ],
  "spec_revision": "2026-10-29-r1",
  "status": "BLOCKED",
  "title": "工序动态表单、CPP与eBR配置闭环",
  "user_experience": {
    "design_quality_owner": "BUSINESS_PRODUCT_MANAGER",
    "evidence_level": "INFERENCE",
    "first_glance": "工序/表单身份及模板版本和可编辑状态",
    "information_groups": [
      {
        "field_refs": [
          "FIELD-001",
          "FIELD-002",
          "FIELD-003",
          "FIELD-004",
          "FIELD-005"
        ],
        "name": "当前对象与配置",
        "priority": "CORE",
        "rationale": "直接决定当前任务与保存是否有效"
      },
      {
        "field_refs": [
          "FIELD-007"
        ],
        "name": "受控原因与技术信息",
        "priority": "SECONDARY",
        "rationale": "操作必需时显示但不抢占设计主体"
      }
    ],
    "next_step": "按同一模板版本进入原有受控流程",
    "primary_action_refs": [
      "ACT-002",
      "ACT-003",
      "ACT-004"
    ],
    "primary_role_ref": "ROLE-001",
    "primary_task": "字段→CPP→签名复核→保存与检查→受控发布",
    "reading_order": [
      "确认对象和状态",
      "配置字段与规则",
      "保存校验并定位下一步"
    ],
    "screen_priorities": [
      {
        "core_field_refs": [
          "FIELD-001",
          "FIELD-002"
        ],
        "first_glance": "生产工艺与工序",
        "layout_rationale": "继承现有批准结构，使用当前任务信息优先级，实际视觉待完整审查",
        "page_ref": "PAGE-001",
        "primary_action_refs": [
          "ACT-001"
        ],
        "reading_order": [
          "对象",
          "状态",
          "动作"
        ],
        "secondary_field_refs": [],
        "user_task": "从工序找到正确模板及表单"
      },
      {
        "core_field_refs": [
          "FIELD-003",
          "FIELD-004",
          "FIELD-005"
        ],
        "first_glance": "当前表单字段与画布",
        "layout_rationale": "继承现有批准结构，使用当前任务信息优先级，实际视觉待完整审查",
        "page_ref": "PAGE-002",
        "primary_action_refs": [
          "ACT-002",
          "ACT-003",
          "ACT-004"
        ],
        "reading_order": [
          "对象",
          "状态",
          "动作"
        ],
        "secondary_field_refs": [],
        "user_task": "设计字段及CPP，配置签名/复核并保存受控草稿"
      },
      {
        "core_field_refs": [
          "FIELD-006",
          "FIELD-007"
        ],
        "first_glance": "模板版本与状态",
        "layout_rationale": "继承现有批准结构，使用当前任务信息优先级，实际视觉待完整审查",
        "page_ref": "PAGE-003",
        "primary_action_refs": [
          "ACT-005",
          "ACT-006"
        ],
        "reading_order": [
          "对象",
          "状态",
          "动作"
        ],
        "secondary_field_refs": [],
        "user_task": "保存草稿后可定位同一模板，并按现有权限提交、批准、发布"
      },
      {
        "core_field_refs": [
          "FIELD-008"
        ],
        "first_glance": "冻结表单、规则与执行结果",
        "layout_rationale": "继承现有批准结构，使用当前任务信息优先级，实际视觉待完整审查",
        "page_ref": "PAGE-004",
        "primary_action_refs": [
          "ACT-007"
        ],
        "reading_order": [
          "对象",
          "状态",
          "动作"
        ],
        "secondary_field_refs": [],
        "user_task": "确认生产批次消费正确冻结表单与规则"
      }
    ],
    "technical_convenience_overrode_user_need": false,
    "user_goal": "配置正确工序的记录与CPP并可受控使用"
  },
  "validation_rules": [
    {
      "id": "VAL-001",
      "message": "定位具体字段或关联原因，保留输入",
      "rule": "继承字段所属权威合同；组织/所属工序/模板一致；未知或冻结证据不得补造",
      "target_ref": "FIELD-001"
    },
    {
      "id": "VAL-002",
      "message": "定位具体字段或关联原因，保留输入",
      "rule": "继承字段所属权威合同；组织/所属工序/模板一致；未知或冻结证据不得补造",
      "target_ref": "FIELD-002"
    },
    {
      "id": "VAL-003",
      "message": "定位具体字段或关联原因，保留输入",
      "rule": "继承字段所属权威合同；组织/所属工序/模板一致；未知或冻结证据不得补造",
      "target_ref": "FIELD-003"
    },
    {
      "id": "VAL-004",
      "message": "定位具体字段或关联原因，保留输入",
      "rule": "继承字段所属权威合同；组织/所属工序/模板一致；未知或冻结证据不得补造",
      "target_ref": "FIELD-004"
    },
    {
      "id": "VAL-005",
      "message": "定位具体字段或关联原因，保留输入",
      "rule": "继承字段所属权威合同；组织/所属工序/模板一致；未知或冻结证据不得补造",
      "target_ref": "FIELD-005"
    },
    {
      "id": "VAL-006",
      "message": "定位具体字段或关联原因，保留输入",
      "rule": "继承字段所属权威合同；组织/所属工序/模板一致；未知或冻结证据不得补造",
      "target_ref": "FIELD-006"
    },
    {
      "id": "VAL-007",
      "message": "定位具体字段或关联原因，保留输入",
      "rule": "继承字段所属权威合同；组织/所属工序/模板一致；未知或冻结证据不得补造",
      "target_ref": "FIELD-007"
    },
    {
      "id": "VAL-008",
      "message": "定位具体字段或关联原因，保留输入",
      "rule": "继承字段所属权威合同；组织/所属工序/模板一致；未知或冻结证据不得补造",
      "target_ref": "FIELD-008"
    }
  ],
  "workflows": [
    {
      "id": "FLOW-001",
      "name": "现有配置链",
      "steps": [
        {
          "action": "当前工艺页聚合模板后进入表单设计",
          "actor": "ROLE-001",
          "decision": "对象/权限/可编辑状态",
          "exception_recovery": "缺失关联或权限显示原因，不选择另一版本代替",
          "input": "已保存工序与表单关联",
          "output": "选中表单上下文"
        },
        {
          "action": "表单/CPP配置；现角色编码输入与ID保存合同不匹配",
          "actor": "ROLE-001",
          "decision": "类型/字段归属/规则与角色是否有效",
          "exception_recovery": "保留字段输入，错误定位；选项服务失败可重试，不降级编码",
          "input": "当前字段与规则",
          "output": "草稿定义与验证结果"
        },
        {
          "action": "依既有合同提交、批准、发布",
          "actor": "ROLE-002",
          "decision": "原有独立角色及状态门禁",
          "exception_recovery": "状态/权限不足保留现状，拒绝自动批准",
          "input": "已保存并校验模板",
          "output": "受控可消费定义"
        },
        {
          "action": "填写/提交，按既有偏差/QA/签名门禁处理",
          "actor": "ROLE-003",
          "decision": "按冻结规则判断执行结果",
          "exception_recovery": "后续工艺/模板修改不得覆盖冻结内容",
          "input": "原有批次冻结定义",
          "output": "可定位的执行及审查证据"
        }
      ],
      "type": "AS_IS"
    },
    {
      "id": "FLOW-002",
      "name": "遵循既有批准合同的闭环",
      "steps": [
        {
          "action": "核对工序、表单与模板身份并进入同一设计器",
          "actor": "ROLE-001",
          "decision": "对象/权限/可编辑状态",
          "exception_recovery": "缺失关联或权限显示原因，不选择另一版本代替",
          "input": "已保存工序与表单关联",
          "output": "选中表单上下文"
        },
        {
          "action": "复用已批准角色选择器及字段/规则编辑；有效ID受控保存",
          "actor": "ROLE-001",
          "decision": "类型/字段归属/规则与角色是否有效",
          "exception_recovery": "保留字段输入，错误定位；选项服务失败可重试，不降级编码",
          "input": "当前字段与规则",
          "output": "草稿定义与验证结果"
        },
        {
          "action": "依既有合同提交、批准、发布",
          "actor": "ROLE-002",
          "decision": "原有独立角色及状态门禁",
          "exception_recovery": "状态/权限不足保留现状，拒绝自动批准",
          "input": "已保存并校验模板",
          "output": "受控可消费定义"
        },
        {
          "action": "填写/提交，按既有偏差/QA/签名门禁处理",
          "actor": "ROLE-003",
          "decision": "按冻结规则判断执行结果",
          "exception_recovery": "后续工艺/模板修改不得覆盖冻结内容",
          "input": "原有批次冻结定义",
          "output": "可定位的执行及审查证据"
        }
      ],
      "type": "TO_BE"
    }
  ]
}
```
<!-- BPM-AUTHORITATIVE-SPEC-END -->
