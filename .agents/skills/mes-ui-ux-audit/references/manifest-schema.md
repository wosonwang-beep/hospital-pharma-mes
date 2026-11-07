# Audit manifest schema

## Approved process PC scope (2026-11-04)

The default remains `[1440,1280,900,375]`. Only the process workspace (`#/processes`) can use `applicability: {"mode":"PC_ONLY","approval":"<path to PROCESS_PC_ONLY_SCOPE_20261104_APPROVED.md>","approval_sha256":"2dfae1d451bff072d418ad843addc2b14f2794991d10ed50e710b0d327195d0e"}` with `[1440,1280,900]`. Approval paths resolve relative to the manifest and must identify the actual repository decision, whose hash is checked against the approved bytes. A missing, copied, changed or unrelated approval cannot activate the exception. All screenshot, dimension, WCAG, interaction, geometry, adversarial and open CRITICAL/HIGH gates remain unchanged. This does not grant prototype approval or formal acceptance. Authorization: `PROCESS_WORKSPACE_FINAL_GAPS_20261104_APPROVED.md`.

`reviewer.executor` accepts exactly `Codex SELF-REVIEW` or `ChatGPT SELF-REVIEW`. Select the actual executor; both denote self-review, not independent QA or human acceptance. Empty, unknown, non-string, whitespace/case variants and independent-review claims are rejected. Historical Codex manifests remain unchanged. All other evidence, coverage, severity and acceptance rules are unchanged. See the approved `AUDIT_EXECUTOR_COMPATIBILITY_20261018.md` decision.

The validator accepts a JSON object with this shape:

```json
{
  "audit_id": "DICTIONARY-UI-20261012",
  "source": {
    "url": "http://127.0.0.1:5174/#/system/dictionaries",
    "source_hash": "SHA256",
    "design_lock": "docs/development/decisions/example.md"
  },
  "reviewer": {
    "executor": "Codex SELF-REVIEW",
    "skill": "mes-ui-ux-audit",
    "ui_ux_pro_max_path": "C:/Users/Administrator/.agents/skills/ui-ux-pro-max/SKILL.md",
    "ui_ux_pro_max_sha256": "SHA256"
  },
  "knowledge_queries": [
    {"group": "layout", "query": "form field alignment", "domain": "ux", "evidence": "evidence/ui-ux/layout.json"}
  ],
  "required_viewports": [1440, 1280, 900, 375],
  "expected_surfaces": [
    {"surface": "dictionary-create", "states": ["default", "validation-error"]}
  ],
  "screenshots": [
    {
      "surface": "dictionary-create",
      "state": "default",
      "viewport": 1440,
      "path": "evidence/01-create-default-1440.png",
      "review": {
        "hierarchy": {"status": "PASS", "note": "..."},
        "grid_alignment": {"status": "PASS", "note": "..."},
        "spacing_density": {"status": "PASS", "note": "..."},
        "typography_color": {"status": "PASS", "note": "..."},
        "controls_content": {"status": "PASS", "note": "..."},
        "actions": {"status": "PASS", "note": "..."},
        "responsive": {"status": "PASS", "note": "..."},
        "interaction_accessibility": {"status": "PASS", "note": "..."}
      }
    }
  ],
  "interaction_checks": [
    {"name": "duplicate recovery", "status": "PASS", "evidence": "evidence/duplicate.json"}
  ],
  "geometry_checks": [
    {"name": "form anchor clusters", "status": "PASS", "evidence": "evidence/geometry.json"}
  ],
  "adversarial_pass": {
    "completed": true,
    "alignment": "evidence-backed note",
    "whitespace": "evidence-backed note",
    "hierarchy": "evidence-backed note",
    "action_discoverability": "evidence-backed note",
    "recovery": "evidence-backed note"
  },
  "issues": [
    {"id": "UX-001", "severity": "HIGH", "status": "RESOLVED", "evidence": "evidence/before.png", "recheck": "evidence/after.png"}
  ],
  "verdict": "PASS FOR PROTOTYPE"
}
```

Coverage is the Cartesian product of every expected surface/state and every required viewport. If a state is intentionally viewport-specific, list separate expected surfaces with an optional `viewports` array.

Full-dimension review additionally requires `dimension_reviews`: one object for every expected surface and each of `layout`, `typography`, `spacing_alignment`, `forms`, `tables_lists`, `query_filters`, `component_interaction`, `component_states`, `wcag22`. Each object contains `surface`, `dimension`, `status` (PASS/FAIL/NOT RUN), a concrete `note` (at least eight characters), and `evidence` (a real file path relative to the manifest, or absolute). Whole dimensions cannot be N/A or omitted. Where a widget is absent, the note must record the inspected requirements and rendered absence; a generic exemption is insufficient.

`wcag22_checks` must contain the 19 minimum criterion IDs listed in review-matrix.md, each as `{criterion,status,note,evidence}`. Status can be PASS/FAIL/NOT RUN/N/A; N/A requires a specific applicability explanation and evidence. Additional criteria may be appended. Missing dimensions, missing criteria, FAIL or NOT RUN cannot produce a PASS verdict. These records supplement, rather than replace, screenshot/state/viewport coverage, interaction/geometry evidence, knowledge queries and adversarial review.

Every review category needs a non-empty note. `N/A` is allowed only when the note explains why. A PASS verdict is rejected when a screenshot is missing, a review category fails, a critical/high issue remains open, a required interaction/geometry check is not PASS, provenance is incomplete, or the adversarial pass is absent.
