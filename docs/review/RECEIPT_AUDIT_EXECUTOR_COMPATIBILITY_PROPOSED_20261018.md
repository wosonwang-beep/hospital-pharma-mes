# 审查执行者兼容修复提案（未批准、未实施）

关联任务：RECEIPT-CLOSEOUT-001。本轮真实执行者为ChatGPT SELF-REVIEW。
现有validate_audit_manifest.py仅允许Codex SELF-REVIEW，导致如实记录ChatGPT时必然拒绝。

拟批准的最小变更：仅将该执行者校验改为接受Codex SELF-REVIEW或ChatGPT SELF-REVIEW两种明确身份；可额外保存实际使用工具路径。未知身份、空身份和虚构独立审查仍拒绝。
绝不更改截图覆盖、尺寸、颜色/几何阈值、严重度、未执行项阻断、原型和最终人工批准要求；不以兼容修复替代未完成的G2审查。
回归必须证明：两种真实身份的相同完整证据采用同一质量判定；缺图、无来源、高风险问题、NOT RUN、伪称独立QA等仍然FAIL。
这是提案，不是批准，不修改当前验证器或技能规则。
