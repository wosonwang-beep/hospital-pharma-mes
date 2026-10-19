# External audit sources

Use these maintained sources as method references. Recheck them online when accuracy depends on the current version.

## Skill structure and evaluation

- OpenAI Skills concept: https://developers.openai.com/plugins/concepts/skills
- OpenAI Skills API guide: https://developers.openai.com/api/docs/guides/tools-skills
- OpenAI systematic skill evals: https://developers.openai.com/blog/eval-skills

Applied principle: keep the repeatable workflow in `SKILL.md`, detailed matrices in references, deterministic completeness checks in scripts, and validate the skill with realistic positive and negative cases.

## Accessibility and forms

- WCAG 2.2: https://www.w3.org/TR/WCAG22/
- WAI Forms Tutorial: https://www.w3.org/WAI/tutorials/forms/
- WAI labels/instructions: https://www.w3.org/WAI/WCAG22/Understanding/labels-or-instructions
- WAI focus visible: https://www.w3.org/WAI/WCAG22/Understanding/focus-visible
- WAI focus order: https://www.w3.org/WAI/WCAG22/Understanding/focus-order

Applied principle: visible labels, programmatic relationships, logical focus order, visible focus, state announcements, and recoverable validation are separate checks. Passing one does not imply the others.

## Form layout and validation patterns

- Ant Design alignment: https://4x.ant.design/docs/spec/alignment
- Ant Design form page research: https://ant.design/docs/spec/research-form/
- Ant Design Form component: https://ant.design/components/form/
- GOV.UK error summary: https://design-system.service.gov.uk/components/error-summary/
- GOV.UK error message: https://design-system.service.gov.uk/components/error-message/
- GOV.UK validation recovery: https://design-system.service.gov.uk/patterns/validation/

Applied principle: forms need consistent visual anchors and reading order. Multiple columns require a justified grouping model. Errors appear both in a summary and beside the field, focus moves predictably, and user input is preserved.

## Heuristic review

- Nielsen Norman Group ten usability heuristics: https://www.nngroup.com/articles/ten-usability-heuristics/
- Nielsen Norman Group heuristic evaluation method: https://www.nngroup.com/articles/how-to-conduct-a-heuristic-evaluation/

Applied principle: evaluate system status, consistency, error prevention/recovery, recognition, efficiency, and aesthetic minimalism. A single evaluator can miss defects; when only one agent is available, disclose `SELF-REVIEW`, use an adversarial second pass, and require human approval instead of claiming independent review.
