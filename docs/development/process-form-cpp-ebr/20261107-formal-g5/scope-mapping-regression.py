#!/usr/bin/env python3
"""Regression-test only the approved, exact MES process PC_ONLY route mapping."""
from __future__ import annotations
import copy
import json
import subprocess
import sys
from pathlib import Path

repo=Path(__file__).resolve().parents[4]
validator=repo/".agents/skills/mes-ui-ux-audit/scripts/validate_audit_manifest.py"
original=repo/"docs/development/process-form-cpp-ebr/20261107-formal-g5/source-final/audit-manifest.json"
base=json.loads(original.read_text(encoding="utf-8"))
dest=Path(__file__).resolve().parent/"scope-mapping-regression-cases"
dest.mkdir(exist_ok=True)
cases=[
 ("approved_formal_127", "http://127.0.0.1:5173/process/packages", None, False, False),
 ("approved_formal_localhost", "http://localhost:5173/process/packages", None, False, False),
 ("approved_formal_slash", "http://localhost:5173/process/packages/", None, False, False),
 ("approved_old_prototype", "http://127.0.0.1:5174/#/processes", None, False, False),
 ("reject_other_module", "http://127.0.0.1:5173/warehouse/materials", None, True, True),
 ("reject_process_details", "http://127.0.0.1:5173/process/packages/829", None, True, True),
 ("reject_external_domain", "https://example.com/process/packages", None, True, True),
 ("reject_unapproved_port", "http://127.0.0.1:5174/process/packages", None, True, True),
 ("reject_unapproved_hash", "http://127.0.0.1:5173/process/packages", "0"*64, False, True),
]
results=[]
for name,url,approval_hash,should_scope_fail,should_viewport_fail in cases:
    data=copy.deepcopy(base)
    data["source"]["url"]=url
    if approval_hash:data["applicability"]["approval_sha256"]=approval_hash
    path=dest/(name+".json")
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding="utf-8")
    cmd=subprocess.run([sys.executable,str(validator),str(path)],capture_output=True,text=True,encoding="utf-8",timeout=12)
    lines=[x.strip() for x in (cmd.stdout+"\n"+cmd.stderr).splitlines() if x.strip()]
    scope=any("PC_ONLY approval applies only" in x for x in lines)
    widths=any("required_viewports must include" in x for x in lines)
    tamper=any("PC_ONLY requires the exact approved process-scope reference" in x for x in lines)
    still_requires_reader=any("interaction_checks[14] must PASS" in x for x in lines)
    passed=cmd.returncode==1 and scope==should_scope_fail and widths==should_viewport_fail and still_requires_reader
    if name=="reject_unapproved_hash":passed=passed and tamper
    results.append({"case":name,"result":"PASS" if passed else "FAIL","code":cmd.returncode,"deniedScope":scope,"requires375":widths,"wrongHashRejected":tamper,"screenReaderGateStillEnforced":still_requires_reader,"messages":lines})
    print(("PASS" if passed else "FAIL")+" "+name+": "+", ".join(lines[:5]))
summary={"verdict":"PASS" if all(x["result"]=="PASS" for x in results) else "FAIL",
 "description":"Exact approved production-process PC_ONLY mapping; other modules and altered approvals denied; screenreader gate unchanged.",
 "tests":results}
output=Path(__file__).resolve().parent/"scope-mapping-regression-result.json"
output.write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding="utf-8")
print("SCOPED_VALIDATOR_REGRESSION="+str(output)+" / "+summary["verdict"])
if summary["verdict"]!="PASS":sys.exit(1)
