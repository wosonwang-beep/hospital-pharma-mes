"""Fail closed on placeholder markers in a JSON business-data seed, before any write.
Run: python tools/check_business_seed_payload.py business-data.json
This validator does not read credentials, connect to services, or mutate records.
Dataset provenance belongs in a separate metadata file, never a business payload.
"""
from __future__ import annotations
import argparse
import json
import re
import sys
import unicodedata
from pathlib import Path
from typing import Any

BANNED = re.compile(r"demo|(?:^|[^a-z])test(?:[^a-z]|$)|测试|演示|模拟|模似|某某|占位|xxx", re.IGNORECASE)
# These are executable API identifiers, not business labels. Their display names
# and every other business field remain subject to the check.
PROTOCOL_FIELDS = frozenset({"permissionCode", "permissionCodes", "requiredPermissions", "routePath", "allowedActions"})

def violations(value: Any, location: str = "$", field: str = "") -> list[dict[str, str]]:
    if field in PROTOCOL_FIELDS:
        return []
    if isinstance(value, dict):
        return [issue for key, child in value.items()
                for issue in violations(child, f"{location}.{key}", str(key))]
    if isinstance(value, list):
        return [issue for index, child in enumerate(value)
                for issue in violations(child, f"{location}[{index}]", field)]
    if isinstance(value, str) and BANNED.search(unicodedata.normalize("NFKC", value)):
        return [{"field": location, "value": value}]
    return []

def self_test() -> None:
    bad = ["DEMO_SCALE", "demo.master", "MES_DEMO_QA", "TEST_001",
           "测试样例｜岗位职责", "原料（演示）", "模拟批次", "模似数据", "某某供应商", "XXX材料"]
    for text in bad:
        assert violations({"code": text}), f"Forbidden value was accepted: {text}"
    for payload in ({"materialCode": "YL-0001", "materialName": "氯化钾"},
                    {"description": "药事管理与制剂业务统筹"},
                    {"permissionCode": "qms:test:view", "permissionName": "查看检验记录"}):
        assert not violations(payload), "Valid business data was rejected"
    assert violations({"items": [{"remark": "DEMO"}]}), "Nested business values escaped validation"
    print("BUSINESS_PAYLOAD_GUARD_SELF_TEST_PASS cases=14")

def main() -> int:
    parser = argparse.ArgumentParser(description="校验业务数据中的禁止占位标识；不执行数据写入")
    parser.add_argument("payload", type=Path, nargs="?")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    if args.self_test:
        self_test()
        return 0
    if args.payload is None:
        parser.error("必须指定JSON业务载荷，或使用 --self-test")
    try:
        data = json.loads(args.payload.read_text(encoding="utf-8-sig"))
    except (OSError, UnicodeError, json.JSONDecodeError) as error:
        print(f"BUSINESS_PAYLOAD_READ_FAILED: {error}", file=sys.stderr)
        return 2
    issues = violations(data)
    print(json.dumps({"pass": not issues, "violations": issues}, ensure_ascii=True, indent=2))
    return 1 if issues else 0

if __name__ == "__main__":
    raise SystemExit(main())
