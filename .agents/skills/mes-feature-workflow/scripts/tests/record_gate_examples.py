"""Persist actual positive/negative simulation gate output without business files."""
import argparse
from pathlib import Path
import shutil
import sys
from test_workflow import Gates, w

parser = argparse.ArgumentParser()
parser.add_argument('--output-dir', required=True)
args = parser.parse_args()
output = Path(args.output_dir).resolve()
output.mkdir(parents=True, exist_ok=True)
summary = {'simulation': True, 'business_acceptance': False, 'gates': [], 'real_subprocess_suites': []}
Gates.setUpClass()
try:
    wf = w.Workflow(Gates.master, 'WF-TEST-001', simulation=True)
    for gate in w.GATES:
        report = wf.validate(gate, output / (gate + '-positive.json'))
        if report['result'] != 'PASS':
            raise AssertionError(report)
        for name, path in wf.stage(gate)['runs'].items():
            receipt = w.load_json(wf.root / path)['payload']
            summary['real_subprocess_suites'].append({k: receipt[k] for k in ('gate', 'suite', 'argv', 'started', 'duration_seconds', 'exit_code', 'result', 'simulation')})
    negatives = [('G1', 'test_g1_missing_design'), ('G2', 'test_g2_missing_prototype'), ('G3', 'test_g3_field_api_mismatch'), ('G4', 'test_g4_unfrozen_baseline'), ('G5', 'test_g5_integration_failure')]
    for gate, method in negatives:
        case = Gates(methodName=method)
        case.setUp()
        try:
            getattr(case, method)()
            report_path = case.wf.dir / 'evidence' / (gate + '-gate.json')
            report = w.load_json(report_path)
            if report['result'] != 'FAIL':
                raise AssertionError(report)
            shutil.copyfile(report_path, output / (gate + '-negative.json'))
            shutil.copyfile(report_path.with_suffix('.md'), output / (gate + '-negative.md'))
            summary['gates'].append({'gate': gate, 'positive': 'PASS', 'negative': 'FAIL_AS_EXPECTED', 'negative_test': method, 'blocking_errors': report['errors']})
        finally:
            case.tearDown()
    w.write_json(output / 'gate-matrix.json', summary)
    print('Actual simulation gate reports persisted: G1-G5 positive PASS / negative FAIL as expected')
finally:
    Gates.tearDownClass()
