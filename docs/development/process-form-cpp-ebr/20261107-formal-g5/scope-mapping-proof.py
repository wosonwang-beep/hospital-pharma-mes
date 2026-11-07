#!/usr/bin/env python3
"""Verify a proposed exact-path PC_ONLY mapping without modifying the real validator."""
import json
import os
import re
import subprocess
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[4]
original_path = root / ".agents/skills/mes-ui-ux-audit/scripts/validate_audit_manifest.py"
original = original_path.read_text("utf-8")
manifest_path = root / "docs/development/process-form-cpp-ebr/20261107-formal-g5/source-final/audit-manifest.json"
base = json.loads(manifest_path.read_text("utf-8"))
old = 'process_scope = str(source.get("url", "")).split("#")[-1].rstrip("/") == "/processes"'
new = '''process_scope = (
                    str(source.get("url", "")).split("#")[-1].rstrip("/") == "/processes"
                    or str(source.get("url", "")).rstrip("/") in (
                        "http://127.0.0.1:5173/process/packages",
                        "http://localhost:5173/process/packages",
                    )
                )'''
if original.count(old) != 1:
    raise SystemExit("Original validator implementation changed, stopping without assumption")
patch = original.replace(old, new)

# Running the validator in a subprocess with an in-memory one-statement replacement
# preserves __file__, so strict approval SHA and all other checks are unchanged.
runner = """
import sys
script_path=sys.argv[1]
contents=sys.argv[2]
manifest=sys.argv[3]
ns={'__name__':'__main__','__file__':script_path}
sys.argv=[script_path,manifest]
exec(compile(contents,script_path,'exec'),ns)
"""
source_json = root / "docs/development/process-form-cpp-ebr/20261107-formal-g5/scope-mapping-proof.json"
temp_dir = root / "docs/development/process-form-cpp-ebr/20261107-formal-g5/scope-probe-cases"
temp_dir.mkdir(parents=True,exist_ok=True)
def check(name, new_url=None, approved_hash=None, is_original=False):
    data=json.loads(json.dumps(base))
    if new_url is not None:data['source']['url']=new_url
    if approved_hash is not None:data['applicability']['approval_sha256']=approved_hash
    m=temp_dir/f"{name}.json"
    m.write_text(json.dumps(data,ensure_ascii=False,indent=2),"utf-8")
    if is_original:
        command=[sys.executable,str(original_path),str(m)]
    else:
        command=[sys.executable,"-c",runner,str(original_path),patch,str(m)]
    run=subprocess.run(command,capture_output=True,text=True,timeout=15,encoding="utf-8",errors="replace")
    lines=[x.strip() for x in (run.stdout+'\n'+run.stderr).splitlines() if x.strip()]
    return {'case':name,'validator':'original' if is_original else 'in-memory exact mapping','exit':run.returncode,'errors':lines[:20],'scope_error':any("PC_ONLY approval applies only" in x for x in lines),'viewport_error':any("required_viewports must include" in x for x in lines),'remaining_gates':[x for x in lines if any(k in x for k in ["interaction_checks","geometry_checks","WCAG","adversarial_pass"])]}
cases=[
 check("before_formal_root",is_original=True),
 check("after_formal_root"),
 check("proposed_formal_localhost",new_url="http://localhost:5173/process/packages"),
 check("proposed_original_prototype",new_url="http://127.0.0.1:5174/#/processes"),
 check("reject_other_module",new_url="http://127.0.0.1:5173/warehouse/materials"),
 check("reject_nested_process_route",new_url="http://127.0.0.1:5173/process/packages/829"),
 check("reject_untrusted_host",new_url="https://example.com/process/packages"),
 check("reject_modified_scope_hash",approved_hash="0"*64),
]
passed=(
 cases[0]['scope_error'] and cases[0]['viewport_error']
 and not cases[1]['scope_error'] and not cases[1]['viewport_error'] and cases[1]['exit']==1
 and not cases[2]['scope_error'] and not cases[2]['viewport_error']
 and not cases[3]['scope_error'] and not cases[3]['viewport_error']
 and all(x['scope_error'] and x['viewport_error'] for x in cases[4:7])
 and any("PC_ONLY requires the exact" in x for x in cases[7]['errors'])
 and all(c['exit']==1 for c in cases)
)
proof={
    "status":"PROPOSAL ONLY -- actual validator unchanged",
    "approved_scope_reference":str(base['applicability']['approval']),
    "validator_source":str(original_path),
    "old_predicate":old,
    "proposed_predicate":new,
    "cases":cases,
    "proof_passed":passed,
    "warning":"Positive formal cases still fail due to remaining real G5 checks; this probe cannot award READY."
}
source_json.write_text(json.dumps(proof,ensure_ascii=False,indent=2),"utf-8")
print(json.dumps({"proof_passed":passed,"cases":[{"case":x['case'],"exit":x['exit'],"scopeError":x['scope_error'],"viewportError":x['viewport_error'],"remaining":x['remaining_gates']}for x in cases]},ensure_ascii=False,indent=2))
if not passed:raise SystemExit(1)
