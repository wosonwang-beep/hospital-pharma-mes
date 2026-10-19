#!/usr/bin/env python3
"""Validate evidence completeness for the MES UI/UX audit skill."""

from __future__ import annotations

import json
import sys
from pathlib import Path

CATEGORIES = (
    "hierarchy",
    "grid_alignment",
    "spacing_density",
    "typography_color",
    "controls_content",
    "actions",
    "responsive",
    "interaction_accessibility",
)
PASS_VERDICTS = {"PASS FOR PROTOTYPE", "READY FOR ACCEPTANCE"}
DIMENSIONS = (
    "layout", "typography", "spacing_alignment", "forms", "tables_lists",
    "query_filters", "component_interaction", "component_states", "wcag22",
)
WCAG22_CRITERIA = (
    "1.3.1", "1.4.1", "1.4.3", "1.4.4", "1.4.10", "1.4.11",
    "2.1.1", "2.1.2", "2.4.3", "2.4.7", "2.4.11", "2.5.3",
    "2.5.7", "2.5.8", "3.3.1", "3.3.2", "3.3.3", "4.1.2", "4.1.3",
)
VALID_REVIEW = {"PASS", "FAIL", "N/A"}
BLOCKING_SEVERITIES = {"CRITICAL", "HIGH"}
CLOSED_ISSUES = {"RESOLVED", "ACCEPTED DEBT", "WONT FIX APPROVED"}


def require(condition: bool, message: str, errors: list[str]) -> None:
    if not condition:
        errors.append(message)


def load(path: Path) -> dict:
    with path.open("r", encoding="utf-8-sig") as handle:
        value = json.load(handle)
    if not isinstance(value, dict):
        raise ValueError("manifest root must be an object")
    return value


def evidence_exists(manifest_path: Path, value: object) -> bool:
    if not isinstance(value, str) or not value.strip():
        return False
    path = Path(value)
    if not path.is_absolute():
        path = manifest_path.parent / path
    return path.is_file()


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: validate_audit_manifest.py <audit-manifest.json>", file=sys.stderr)
        return 2

    manifest_path = Path(sys.argv[1]).resolve()
    try:
        data = load(manifest_path)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"FAIL: cannot read manifest: {exc}", file=sys.stderr)
        return 1

    errors: list[str] = []
    source = data.get("source") or {}
    reviewer = data.get("reviewer") or {}
    require(bool(data.get("audit_id")), "audit_id is required", errors)
    require(bool(source.get("url")), "source.url is required", errors)
    require(bool(source.get("source_hash")), "source.source_hash is required", errors)
    require(bool(source.get("design_lock")), "source.design_lock is required", errors)
    require(reviewer.get("executor") in ("Codex SELF-REVIEW", "ChatGPT SELF-REVIEW"), "reviewer.executor must disclose Codex SELF-REVIEW or ChatGPT SELF-REVIEW", errors)
    require(reviewer.get("skill") == "mes-ui-ux-audit", "reviewer.skill must be mes-ui-ux-audit", errors)
    require(bool(reviewer.get("ui_ux_pro_max_path")), "ui-ux-pro-max path is required", errors)
    require(bool(reviewer.get("ui_ux_pro_max_sha256")), "ui-ux-pro-max SHA256 is required", errors)

    queries = data.get("knowledge_queries") or []
    query_groups = {q.get("group") for q in queries if isinstance(q, dict)}
    required_groups = {"product_style", "layout", "forms", "tables_lists", "modal_feedback", "accessibility", "typography_color", "stack"}
    require(required_groups <= query_groups, f"knowledge query groups missing: {sorted(required_groups - query_groups)}", errors)
    for index, query in enumerate(queries):
        require(isinstance(query, dict) and evidence_exists(manifest_path, query.get("evidence")), f"knowledge_queries[{index}] evidence file missing", errors)

    viewports = data.get("required_viewports") or []
    require(all(width in viewports for width in (1440, 1280, 900, 375)), "required_viewports must include 1440, 1280, 900, 375", errors)

    expected: set[tuple[str, str, int]] = set()
    for item in data.get("expected_surfaces") or []:
        if not isinstance(item, dict):
            continue
        widths = item.get("viewports") or viewports
        for state in item.get("states") or []:
            for width in widths:
                expected.add((str(item.get("surface")), str(state), int(width)))
    require(bool(expected), "expected_surfaces coverage is empty", errors)

    actual: set[tuple[str, str, int]] = set()
    screenshot_failed = False
    for index, shot in enumerate(data.get("screenshots") or []):
        if not isinstance(shot, dict):
            errors.append(f"screenshots[{index}] must be an object")
            continue
        key = (str(shot.get("surface")), str(shot.get("state")), int(shot.get("viewport") or 0))
        actual.add(key)
        require(evidence_exists(manifest_path, shot.get("path")), f"screenshot file missing for {key}", errors)
        review = shot.get("review") or {}
        for category in CATEGORIES:
            cell = review.get(category) or {}
            status = cell.get("status")
            note = cell.get("note")
            require(status in VALID_REVIEW, f"{key} {category} status must be PASS/FAIL/N/A", errors)
            require(isinstance(note, str) and len(note.strip()) >= 8, f"{key} {category} needs an evidence note", errors)
            if status == "FAIL":
                screenshot_failed = True
    require(expected <= actual, f"missing screenshot coverage: {sorted(expected - actual)}", errors)

    required_dimensions = {(surface, dimension) for surface, _, _ in expected for dimension in DIMENSIONS}
    dimension_cells = set()
    for index, cell in enumerate(data.get("dimension_reviews") or []):
        if not isinstance(cell, dict):
            errors.append(f"dimension_reviews[{index}] must be an object")
            continue
        dimension_cells.add((cell.get("surface"), cell.get("dimension")))
        require(cell.get("status") in {"PASS", "FAIL", "NOT RUN"}, f"dimension_reviews[{index}] must not skip a whole dimension", errors)
        require(isinstance(cell.get("note"), str) and len(cell["note"].strip()) >= 8, f"dimension_reviews[{index}] needs a concrete note", errors)
        require(evidence_exists(manifest_path, cell.get("evidence")), f"dimension_reviews[{index}] evidence missing", errors)
        if cell.get("status") != "PASS":
            screenshot_failed = True
    require(required_dimensions <= dimension_cells, f"missing full-dimension coverage: {sorted(required_dimensions - dimension_cells)}", errors)

    criteria = set()
    for index, cell in enumerate(data.get("wcag22_checks") or []):
        if not isinstance(cell, dict):
            errors.append(f"wcag22_checks[{index}] must be an object")
            continue
        criteria.add(cell.get("criterion"))
        require(cell.get("status") in {"PASS", "FAIL", "NOT RUN", "N/A"}, f"wcag22_checks[{index}] invalid status", errors)
        require(isinstance(cell.get("note"), str) and len(cell["note"].strip()) >= 8, f"wcag22_checks[{index}] needs result/applicability evidence", errors)
        require(evidence_exists(manifest_path, cell.get("evidence")), f"wcag22_checks[{index}] evidence missing", errors)
        if cell.get("status") not in {"PASS", "N/A"}:
            screenshot_failed = True
    require(set(WCAG22_CRITERIA) <= criteria, f"missing WCAG 2.2 checks: {sorted(set(WCAG22_CRITERIA) - criteria)}", errors)

    for group_name in ("interaction_checks", "geometry_checks"):
        checks = data.get(group_name) or []
        require(bool(checks), f"{group_name} must not be empty", errors)
        for index, check in enumerate(checks):
            require(isinstance(check, dict) and check.get("status") == "PASS", f"{group_name}[{index}] must PASS", errors)
            require(isinstance(check, dict) and evidence_exists(manifest_path, check.get("evidence")), f"{group_name}[{index}] evidence file missing", errors)

    adversarial = data.get("adversarial_pass") or {}
    require(adversarial.get("completed") is True, "adversarial pass is required", errors)
    for field in ("alignment", "whitespace", "hierarchy", "action_discoverability", "recovery"):
        require(isinstance(adversarial.get(field), str) and len(adversarial[field].strip()) >= 8, f"adversarial_pass.{field} needs evidence", errors)

    open_blocking = []
    for issue in data.get("issues") or []:
        if not isinstance(issue, dict):
            continue
        if issue.get("severity") in BLOCKING_SEVERITIES and issue.get("status") not in CLOSED_ISSUES:
            open_blocking.append(issue.get("id") or "unnamed")
        if issue.get("status") == "RESOLVED":
            require(evidence_exists(manifest_path, issue.get("evidence")), f"resolved issue {issue.get('id')} before evidence missing", errors)
            require(evidence_exists(manifest_path, issue.get("recheck")), f"resolved issue {issue.get('id')} recheck evidence missing", errors)

    verdict = data.get("verdict")
    require(verdict in {"FAIL", "IN PROGRESS", *PASS_VERDICTS}, "invalid verdict", errors)
    if verdict in PASS_VERDICTS:
        require(not screenshot_failed, "PASS verdict conflicts with failed screenshot review", errors)
        require(not open_blocking, f"PASS verdict has open blocking issues: {open_blocking}", errors)
        require(expected <= actual, "PASS verdict has missing coverage", errors)

    if errors:
        print("FAIL")
        for error in errors:
            print(f"- {error}")
        return 1

    print(f"PASS: {data['audit_id']} — {verdict}; screenshots={len(actual)} coverage={len(expected)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
