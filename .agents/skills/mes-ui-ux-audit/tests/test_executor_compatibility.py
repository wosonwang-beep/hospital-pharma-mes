"""Synthetic validator-contract tests, NOT product acceptance or real UI evidence.
The baseline is a byte-for-byte snapshot of the actual pre-change validator.
Run: python -B -m unittest discover -s .agents/skills/mes-ui-ux-audit/tests -v
"""
from __future__ import annotations
import base64
import contextlib
import copy
import io
import json
import os
from pathlib import Path
import runpy
import sys
import tempfile
import unittest

SKILL = Path(__file__).resolve().parents[1]
ROOT = Path(__file__).resolve().parents[4]
VALIDATOR = SKILL / 'scripts/validate_audit_manifest.py'
BASELINE = Path(os.environ.get('AUDIT_EXECUTOR_BASELINE', str(ROOT / 'docs/review/evidence/AUDIT_EXECUTOR_COMPATIBILITY_20261018/validator-before.py')))
CURRENT = Path(os.environ.get('AUDIT_CURRENT_MANIFEST', str(ROOT.parent / 'mes-demo/evidence/receipt-bpm-standard-20261018/continuation-04/audit-manifest.json')))
OLD_GUARD = '    require(reviewer.get("executor") == "Codex SELF-REVIEW", "reviewer.executor must disclose Codex SELF-REVIEW", errors)'
NEW_GUARD = '    require(reviewer.get("executor") in ("Codex SELF-REVIEW", "ChatGPT SELF-REVIEW"), "reviewer.executor must disclose Codex SELF-REVIEW or ChatGPT SELF-REVIEW", errors)'
OLD_ERROR = '- reviewer.executor must disclose Codex SELF-REVIEW'
EXECUTORS = ('Codex SELF-REVIEW', 'ChatGPT SELF-REVIEW')
CATEGORIES = ('hierarchy','grid_alignment','spacing_density','typography_color','controls_content','actions','responsive','interaction_accessibility')
DIMENSIONS = ('layout','typography','spacing_alignment','forms','tables_lists','query_filters','component_interaction','component_states','wcag22')
CRITERIA = ('1.3.1','1.4.1','1.4.3','1.4.4','1.4.10','1.4.11','2.1.1','2.1.2','2.4.3','2.4.7','2.4.11','2.5.3','2.5.7','2.5.8','3.3.1','3.3.2','3.3.3','4.1.2','4.1.3')
GROUPS = ('product_style','layout','forms','tables_lists','modal_feedback','accessibility','typography_color','stack')
NOTE = 'Synthetic validator-contract fixture only; NOT a real UI review or approval.'

def invoke(script: Path, manifest: Path) -> tuple[int, str]:
    previous, output = sys.argv, io.StringIO()
    try:
        sys.argv = [str(script), str(manifest)]
        with contextlib.redirect_stdout(output), contextlib.redirect_stderr(output):
            try:
                runpy.run_path(str(script), run_name='__main__')
            except SystemExit as exc:
                return int(exc.code or 0), output.getvalue()
        return 0, output.getvalue()
    finally:
        sys.argv = previous

def fixture() -> dict:
    return {
        'audit_id':'VALIDATOR_CONTRACT_FIXTURE_NOT_PRODUCT_ACCEPTANCE',
        'fixture_metadata':{'synthetic':True,'not_product_evidence':True},
        'source':{'url':'http://127.0.0.1:5174/','source_hash':'1'*64,'design_lock':'fixture-evidence.txt'},
        'reviewer':{'executor':EXECUTORS[0],'skill':'mes-ui-ux-audit','ui_ux_pro_max_path':'fixture-evidence.txt','ui_ux_pro_max_sha256':'2'*64},
        'knowledge_queries':[{'group':g,'query':NOTE,'evidence':'fixture-evidence.txt'} for g in GROUPS],
        'required_viewports':[1440,1280,900,375],
        'expected_surfaces':[{'surface':'fixture','states':['default']}],
        'screenshots':[{'surface':'fixture','state':'default','viewport':w,'path':'fixture-pixel.png','review':{k:{'status':'PASS','note':NOTE} for k in CATEGORIES}} for w in (1440,1280,900,375)],
        'dimension_reviews':[{'surface':'fixture','dimension':k,'status':'PASS','note':NOTE,'evidence':'fixture-evidence.txt'} for k in DIMENSIONS],
        'wcag22_checks':[{'criterion':k,'status':'PASS','note':NOTE,'evidence':'fixture-evidence.txt'} for k in CRITERIA],
        'interaction_checks':[{'name':NOTE,'status':'PASS','evidence':'fixture-evidence.txt'}],
        'geometry_checks':[{'name':NOTE,'status':'PASS','evidence':'fixture-evidence.txt'}],
        'adversarial_pass':{'completed':True,**{k:NOTE for k in ('alignment','whitespace','hierarchy','action_discoverability','recovery')}},
        'issues':[],'verdict':'PASS FOR PROTOTYPE',
    }

class ExecutorCompatibilityTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory(prefix='audit-validator-contract-')
        self.addCleanup(self.temp.cleanup)
        self.directory = Path(self.temp.name)
        (self.directory/'fixture-evidence.txt').write_text(NOTE,encoding='utf-8')
        (self.directory/'fixture-pixel.png').write_bytes(base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/lWQAAAAASUVORK5CYII='))
    def run_fixture(self,value:dict,script:Path=VALIDATOR)->tuple[int,str]:
        target=self.directory/'fixture-manifest.json'
        target.write_text(json.dumps(value,ensure_ascii=False),encoding='utf-8')
        return invoke(script,target)
    def test_only_authorized_executor_statement_changed(self):
        old,new=BASELINE.read_bytes(),VALIDATOR.read_bytes()
        self.assertEqual(old.count(OLD_GUARD.encode()),1)
        # Reverse only the exact user-approved process PC scope supplement.
        supplement=json.loads((SKILL/'tests/fixtures/process_pc_scope_20261104.json').read_text(encoding='utf-8'))
        approved=base64.b64decode(supplement['approved_block_base64'])
        original=base64.b64decode(supplement['original_block_base64'])
        self.assertEqual(new.count(approved),1)
        new=new.replace(approved,original,1)
        self.assertEqual(new,old.replace(OLD_GUARD.encode(),NEW_GUARD.encode(),1))
    def test_current_real_manifest_errors_unchanged_except_identity(self):
        before=CURRENT.read_bytes()
        self.assertEqual(json.loads(before)['reviewer']['executor'],'ChatGPT SELF-REVIEW')
        a,old=invoke(BASELINE,CURRENT);b,new=invoke(VALIDATOR,CURRENT)
        self.assertEqual((a,b),(1,1))
        self.assertEqual(old.splitlines().count(OLD_ERROR),1)
        self.assertEqual(new.splitlines(),[line for line in old.splitlines() if line!=OLD_ERROR])
        self.assertNotIn('reviewer.executor must disclose',new)
        self.assertEqual(CURRENT.read_bytes(),before)
    def test_missing_executor_rejected(self):
        d=fixture();d['reviewer'].pop('executor');code,out=self.run_fixture(d)
        self.assertEqual(code,1);self.assertIn('reviewer.executor must disclose',out)

def positive_case(executor,verdict):
    def test(self):
        d=fixture();d['verdict']=verdict;old=self.run_fixture(d,BASELINE)
        d['reviewer']['executor']=executor;new=self.run_fixture(d)
        self.assertEqual(new,old);self.assertEqual(new[0],0);self.assertIn(verdict,new[1])
    return test
for executor in EXECUTORS:
    for verdict in ('PASS FOR PROTOTYPE','READY FOR ACCEPTANCE','IN PROGRESS','FAIL'):
        setattr(ExecutorCompatibilityTests,'test_valid_'+executor.split()[0]+'_'+verdict.lower().replace(' ','_'),positive_case(executor,verdict))

def invalid_case(executor):
    def test(self):
        d=fixture();d['reviewer']['executor']=executor;code,out=self.run_fixture(d)
        self.assertEqual(code,1);self.assertIn('reviewer.executor must disclose',out);self.assertNotIn('Traceback',out)
    return test
INVALID=(None,'','ChatGPT','Codex','ChatGPT INDEPENDENT REVIEW','Codex INDEPENDENT REVIEW','Independent QA APPROVED','chatgpt SELF-REVIEW','ChatGPT SELF-REVIEW ',' ChatGPT SELF-REVIEW','Other SELF-REVIEW','ChatGPT SELF-REVIEW\n',[],{},0,False)
for i,value in enumerate(INVALID):
    setattr(ExecutorCompatibilityTests,f'test_invalid_executor_{i:02d}',invalid_case(value))

# New unit mutations, never changes to existing product acceptance assertions.
MUTATIONS={
 'missing_screenshot_file':lambda d:d['screenshots'][0].update(path='absent.png'),
 'missing_screenshot_coverage':lambda d:d['screenshots'].pop(),
 'failed_screenshot_review':lambda d:d['screenshots'][0]['review']['hierarchy'].update(status='FAIL'),
 'unrun_screenshot_review':lambda d:d['screenshots'][0]['review']['hierarchy'].update(status='NOT RUN'),
 'missing_screenshot_note':lambda d:d['screenshots'][0]['review']['hierarchy'].update(note=''),
 'missing_source_url':lambda d:d['source'].pop('url'),
 'missing_source_hash':lambda d:d['source'].pop('source_hash'),
 'missing_design_lock':lambda d:d['source'].pop('design_lock'),
 'wrong_skill':lambda d:d['reviewer'].update(skill='other'),
 'missing_skill_path':lambda d:d['reviewer'].pop('ui_ux_pro_max_path'),
 'missing_skill_hash':lambda d:d['reviewer'].pop('ui_ux_pro_max_sha256'),
 'missing_knowledge_group':lambda d:d['knowledge_queries'].pop(),
 'missing_knowledge_file':lambda d:d['knowledge_queries'][0].update(evidence='absent.txt'),
 'missing_viewport':lambda d:d['required_viewports'].pop(),
 'empty_surfaces':lambda d:d.update(expected_surfaces=[]),
 'missing_dimension':lambda d:d['dimension_reviews'].pop(),
 'unrun_dimension':lambda d:d['dimension_reviews'][0].update(status='NOT RUN'),
 'failed_dimension':lambda d:d['dimension_reviews'][0].update(status='FAIL'),
 'skipped_whole_dimension':lambda d:d['dimension_reviews'][0].update(status='N/A'),
 'missing_dimension_file':lambda d:d['dimension_reviews'][0].update(evidence='absent.txt'),
 'missing_wcag_criterion':lambda d:d['wcag22_checks'].pop(),
 'unrun_wcag_check':lambda d:d['wcag22_checks'][0].update(status='NOT RUN'),
 'failed_wcag_check':lambda d:d['wcag22_checks'][0].update(status='FAIL'),
 'missing_wcag_file':lambda d:d['wcag22_checks'][0].update(evidence='absent.txt'),
 'failed_interaction':lambda d:d['interaction_checks'][0].update(status='FAIL'),
 'unrun_interaction':lambda d:d['interaction_checks'][0].update(status='NOT RUN'),
 'missing_interaction_file':lambda d:d['interaction_checks'][0].update(evidence='absent.txt'),
 'failed_geometry':lambda d:d['geometry_checks'][0].update(status='FAIL'),
 'missing_geometry_file':lambda d:d['geometry_checks'][0].update(evidence='absent.txt'),
 'missing_adversarial_review':lambda d:d['adversarial_pass'].update(completed=False),
 'missing_adversarial_note':lambda d:d['adversarial_pass'].update(recovery=''),
 'open_high_issue':lambda d:d['issues'].append({'id':'unit-high','severity':'HIGH','status':'OPEN'}),
 'open_critical_issue':lambda d:d['issues'].append({'id':'unit-critical','severity':'CRITICAL','status':'OPEN'}),
 'resolved_without_evidence':lambda d:d['issues'].append({'id':'unit-resolved','severity':'HIGH','status':'RESOLVED'}),
 'invalid_verdict':lambda d:d.update(verdict='ACCEPTED'),
}
def negative_case(change):
    def test(self):
        d=fixture();change(d);old=self.run_fixture(d,BASELINE);self.assertEqual(old[0],1)
        for executor in EXECUTORS:
            with self.subTest(executor=executor):
                new=copy.deepcopy(d);new['reviewer']['executor']=executor
                result=self.run_fixture(new);self.assertEqual(result,old)
                self.assertNotIn('reviewer.executor must disclose',result[1])
    return test
for name,mutation in MUTATIONS.items():
    setattr(ExecutorCompatibilityTests,'test_quality_guard_'+name,negative_case(mutation))

if __name__=='__main__':
    unittest.main(verbosity=2)
