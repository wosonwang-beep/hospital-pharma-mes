from pathlib import Path
r=Path(r'D:\codex\_project\gmp\hospital-pharma-mes')
p=r/'docs/PROJECT_BASELINE.md'
s=p.read_text(encoding='utf-8')
anchor='## Authority'
assert anchor in s
entry='''## 2026-10-11 统一质量检验对象固定字典补充（业务原则已批准，实施中）
用户确认质量管理共用，请验、取样、检验、报告按固定对象类型区分物料/成品/中间品/环境/过程；多态对象键必须经过后端解析、组织与状态检查并冻结快照。**正式设计记录：** [DCP-QC-OBJECT-TYPE-001](development/DCP-QC-OBJECT-TYPE-001-APPROVED-PRINCIPLES.md)。固定字典 `QC_OBJECT_TYPE` 的增量迁移为 V053，既有 V016/V030 质量单据及签名历史不可改写；跨单据多态关联改造尚未完成，不能宣称全系统质量对象已统一。FINAL BASELINE v1.0.23 的其他条款保持权威，未创建新的冻结发布版。运行进度仅在 MES_TASKS.md。

'''
s=s.replace(anchor,entry+anchor,1)
p.write_text(s,encoding='utf-8')
p=r/'MES_TASKS.md'
s=p.read_text(encoding='utf-8')
entry='''## QC_OBJECT_TYPE 固定基础字典与统一质量对象关系（2026-10-11）
**IN PROGRESS / NOT ACCEPTED**：用户已确认统一质量平台及固定对象类型，DCP 见 [DCP-QC-OBJECT-TYPE-001](docs/development/DCP-QC-OBJECT-TYPE-001-APPROVED-PRINCIPLES.md)。V053 已编写但迁移执行、固定字典专项负例、统一 QMS 对象解析器、组织隔离、提交快照冻结、既有 V016/V030 业务链兼容及全链集成验收尚未完成。不得将字典编译通过当作统一质量业务通过。保留已有用户工作区变更；不得清除质量历史记录或改变冻结业务状态机。

'''
p.write_text(entry+s,encoding='utf-8')
print('baseline and task index updated')
