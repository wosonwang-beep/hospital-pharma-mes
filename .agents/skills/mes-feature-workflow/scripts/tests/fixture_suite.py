"""Real subprocess tests of the workflow fixture; NEVER a MES business suite."""
import argparse
import os
from pathlib import Path
import sys
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--output', required=True)
parser.add_argument('--fail', action='store_true')
parser.add_argument('--skip', action='store_true')
args = parser.parse_args()
start = time.monotonic()
suite = ET.Element('testsuite', name='WF-TEST-simulation')
checks = {
    'new-output-path': lambda: not Path(args.output).exists(),
    'signing-secrets-not-in-child': lambda: 'MES_WORKFLOW_APPROVAL_PRIVATE_KEY' not in os.environ and 'MES_WORKFLOW_EVIDENCE_PRIVATE_KEY' not in os.environ,
    'targeted-failure': lambda: not args.fail,
}
failures = 0
for name, check in checks.items():
    case = ET.SubElement(suite, 'testcase', name=name, classname='workflow.fixture')
    if args.skip and name == 'targeted-failure':
        ET.SubElement(case, 'skipped', message='intentional negative fixture')
    elif not check():
        ET.SubElement(case, 'failure', message='observable fixture invariant failed')
        failures += 1
suite.set('tests', str(len(checks)))
suite.set('failures', str(failures))
suite.set('time', str(time.monotonic() - start))
Path(args.output).parent.mkdir(parents=True, exist_ok=True)
ET.ElementTree(suite).write(args.output, encoding='utf-8', xml_declaration=True)
print('SIMULATION: actual subprocess fixture assertions executed')
sys.exit(1 if failures else 0)
