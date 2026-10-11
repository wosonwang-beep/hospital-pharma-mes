"""Isolated WF-TEST-* gate tests. No MES feature, checkout, server or DB writes."""
import base64
import contextlib
import copy
import importlib.util
import io
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import time
import unittest
import xml.etree.ElementTree as ET

SCRIPT = Path(__file__).resolve().parents[1] / 'workflow.py'
spec = importlib.util.spec_from_file_location('mes_workflow', SCRIPT)
w = importlib.util.module_from_spec(spec)
spec.loader.exec_module(w)
FIXTURE = Path(__file__).with_name('fixture_suite.py')


class Gates(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.trust = tempfile.TemporaryDirectory(prefix='mes-workflow-test-trust-')
        cls.original_env = dict(os.environ)
        for kind in ('approval', 'evidence'):
            key = Path(cls.trust.name) / kind
            result = subprocess.run(['ssh-keygen', '-q', '-t', 'ed25519', '-N', '', '-f', str(key)], capture_output=True)
            if result.returncode:
                raise RuntimeError('OpenSSH fixture identity generation failed: ' + result.stderr.decode(errors='replace'))
            signers = key.with_suffix('.signers')
            signers.write_text('wf-test-' + kind + ' ' + key.with_suffix('.pub').read_text(), encoding='utf-8')
            prefix = 'MES_WORKFLOW_' + kind.upper()
            os.environ[prefix + '_PRIVATE_KEY'] = str(key)
            os.environ[prefix + '_PRINCIPAL'] = 'wf-test-' + kind
            os.environ[prefix + '_SIGNERS'] = str(signers)
        cls.master_tmp = tempfile.TemporaryDirectory(prefix='mes-workflow-test-master-')
        cls.master = Path(cls.master_tmp.name)
        cls.build(cls.master)

    @classmethod
    def tearDownClass(cls):
        os.environ.clear()
        os.environ.update(cls.original_env)
        cls.master_tmp.cleanup()
        cls.trust.cleanup()

    @classmethod
    def ref(cls, root, path, content):
        target = root / path
        target.parent.mkdir(parents=True, exist_ok=True)
        if isinstance(content, (dict, list)):
            w.write_json(target, content)
        elif isinstance(content, bytes):
            target.write_bytes(content)
        else:
            target.write_text(content, encoding='utf-8')
        return {'path': path, 'version': 'SIMULATION-v1', 'sha256': w.file_hash(target)}

    @classmethod
    def build(cls, root):
        feature = 'WF-TEST-001'
        data = w.new_manifest(feature)
        data['simulation'] = True
        data['scope'].update(requirements=['REQ-SIM-1'], write_paths=['frontend/mes-web/src/views/simulation.vue'], menu=True)
        data['reads'] = {name: cls.ref(root, name, 'SIMULATION read evidence for ' + name) for name in w.MANDATORY_READS}
        cls.ref(root, 'tests/fixture_suite.py', FIXTURE.read_bytes())
        for gate in w.GATES:
            stage = data['stages'][gate]
            stage.update(status='REVIEW_PENDING', commit='SIMULATION-only-commit', version='SIMULATION-v1')
            stage['source_files'] = [cls.ref(root, 'tests/fixture_suite.py', FIXTURE.read_bytes())]
            for name in w.ARTIFACTS[gate]:
                stage['artifacts'][name] = cls.ref(root, f'docs/features/{feature}/{gate}/{name}.json', {'simulation': True, 'name': name})
            suites = list(w.SUITES[gate]) + (['menu-authorization'] if gate == 'G5' else [])
            for name in suites:
                stage['test_plans'][name] = {'argv': ['{python}', '{root}/tests/fixture_suite.py', '--output', '{junit}'], 'timeout_seconds': 20, 'environment': 'local-real', 'runtime': {'kind': 'local-real', 'frontend': 'http://localhost:5174' if gate == 'G2' else 'http://localhost:5173', 'backend': 'http://localhost:8080', 'simulation': True}}
        def artifact(gate, name, content):
            ref = data['stages'][gate]['artifacts'][name]
            data['stages'][gate]['artifacts'][name] = cls.ref(root, ref['path'], content)
        artifact('G1', 'page_mapping', {'pages': [{'id': page, 'template': 'T1', 'requirements': ['REQ-SIM-1'], 'fields': ['name'], 'buttons': ['query'], 'states': ['empty', 'ready'], 'label_control': 'same-row', 'viewports': [1440, 1280, 900, 375], 'tokens': ['Global UI V2']} for page in ('SIM-PAGE', 'SIM-DETAIL')]})
        source_sha = 'a' * 64
        files = {'src/Simulation.vue': source_sha, 'src/SimulationDetail.vue': 'b' * 64}
        proto_hash = w.digest(files)
        artifact('G2', 'prototype_manifest', {'simulation': True, 'root': 'SIMULATION/mes-demo', 'stack': ['Vue 3', 'TypeScript', 'Vite', 'Ant Design Vue', 'Pinia', 'Vue Router'], 'commit': 'SIMULATION-not-business-commit', 'files': files, 'tree_sha256': proto_hash, 'page_sources': {'SIM-PAGE': 'src/Simulation.vue', 'SIM-DETAIL': 'src/SimulationDetail.vue'}, 'mock_adapter': 'SIMULATION adapter'})
        png = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jOQAAAABJRU5ErkJggg==')
        image = cls.ref(root, 'docs/features/WF-TEST-001/evidence/simulation.png', png)
        artifact('G2', 'screenshots', {'simulation': True, 'viewports': [1440, 1280, 900, 375], 'pairs': [{'page': page, 'reference': image, 'rendered': image, 'viewport': v, 'review': 'SIMULATION'} for page in ('SIM-PAGE', 'SIM-DETAIL') for v in (1440, 1280, 900, 375)]})
        field = {k: 'SIMULATION' for k in w.FIELD_KEYS}
        field.update(id='name', type='string', length=64, precision=0, required=True, default='')
        shape = {k: field[k] for k in ('type', 'length', 'precision', 'required')}
        operation = {k: 'SIMULATION' for k in ('id', 'page_button', 'permission', 'backend_permission', 'api', 'state_validation', 'data_validation', 'success', 'failure')}
        operation.update(test_cases=['SIM-T1'], write=False, role='view')
        artifact('G3', 'contracts', {'fields': [field], 'projections': {k: {'name': shape} for k in ('page', 'database', 'api')}, 'operations': [operation], 'states': ['SIM-READY'], 'backend_states': ['SIM-READY'], 'rtm': {'REQ-SIM-1': ['SIM-T1']}, 'test_ids': ['SIM-T1'], 'prototype_sha256': proto_hash, 'conflicts': []})
        frozen_refs = [ref for gate in ('G1', 'G2', 'G3') for name, ref in data['stages'][gate]['artifacts'].items() if not (gate == 'G3' and name == 'freeze')]
        artifact('G3', 'freeze', {'simulation': True, 'status': 'FROZEN', 'id': 'WF-TEST-FREEZE', 'owner': 'SIMULATION', 'time': w.now(), 'commit': 'SIMULATION-only-commit', 'files': frozen_refs})
        implementation = cls.ref(root, 'frontend/mes-web/src/views/simulation.vue', '<!-- SIMULATION; not a business page -->')
        implementation_detail = cls.ref(root, 'frontend/mes-web/src/views/simulationDetail.vue', '<!-- SIMULATION detail; not a business page -->')
        data['stages']['G4']['source_files'].append(implementation)
        data['stages']['G4']['source_files'].append(implementation_detail)
        artifact('G4', 'lineage', {'prototype_sha256': proto_hash, 'mappings': [{'prototype_file': 'src/Simulation.vue', 'prototype_sha256': source_sha, 'implementation': implementation}, {'prototype_file': 'src/SimulationDetail.vue', 'prototype_sha256': 'b' * 64, 'implementation': implementation_detail}]})
        artifact('G4', 'frozen_ui', {'files': [implementation], 'unapproved_changes': []})
        artifact('G4', 'real_api', {'simulation': True, 'backend': 'http://localhost:8080', 'operations': ['SIMULATION']})
        artifact('G4', 'no_mock', {'production_mock_paths': []})
        artifact('G5', 'issues', {'items': []})
        artifact('G5', 'visual_comparison', {'simulation': True, 'prototype_sha256': proto_hash, 'major_deviations': [], 'pairs': [{'page': page, 'prototype': image, 'formal': image, 'viewport': v, 'review': 'SIMULATION'} for page in ('SIM-PAGE', 'SIM-DETAIL') for v in (1440, 1280, 900, 375)]})
        artifact('G5', 'integration_acceptance', {'simulation': True, 'unapproved_contract_changes': []})
        w.write_json(root / f'docs/features/{feature}/workflow.yaml', data)
        wf = w.Workflow(root, feature, simulation=True)
        for gate in w.GATES:
            for suite in wf.stage(gate)['test_plans']:
                wf.run(gate, suite)
            cls.approve(wf, gate)
            report = wf.validate(gate)
            if report['result'] != 'PASS':
                raise AssertionError(report)
            wf.advance(gate, 'ACCEPTED')
        wf.save()

    @classmethod
    def approve(cls, wf, gate, decision='ACCEPTED', name=None, **extra):
        source = cls.ref(wf.root, f'docs/features/{wf.feature}/evidence/{gate}-simulation-approval-source.txt', 'SIMULATION: authorized only for test fixture; NEVER human MES acceptance')
        payload = {'feature': wf.feature, 'gate': gate, 'simulation': True, 'decision': decision, 'authority': 'human', 'approver': 'WF-TEST-identity', 'subject': wf.subject(gate), 'timestamp': w.now(), 'source': source, **extra}
        path = wf.dir / 'evidence' / (name or (gate + '-simulation-approval.json'))
        w.write_json(path, w.seal(payload, 'approval'))
        relative = path.relative_to(wf.root).as_posix()
        if decision == 'ACCEPTED':
            wf.stage(gate)['approval'] = relative
        wf.save()
        return relative

    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(prefix='mes-workflow-fixture-')
        self.root = Path(self.tmp.name)
        shutil.copytree(self.master, self.root, dirs_exist_ok=True)
        self.wf = w.Workflow(self.root, 'WF-TEST-001', simulation=True)

    def tearDown(self):
        self.tmp.cleanup()

    def assert_fails(self, gate, text):
        report = self.wf.validate(gate)
        self.assertEqual('FAIL', report['result'])
        self.assertIn(text, ' '.join(report['errors']))

    def change_artifact(self, gate, name, mutate):
        ref = self.wf.stage(gate)['artifacts'][name]
        path = self.root / ref['path']
        data = w.load_json(path)
        mutate(data)
        w.write_json(path, data)
        ref['sha256'] = w.file_hash(path)
        self.wf.save()

    def rebind(self, gate):
        self.wf.stage(gate)['status'] = 'REVIEW_PENDING'
        for suite in self.wf.stage(gate)['test_plans']:
            self.wf.run(gate, suite)
        self.approve(self.wf, gate)

    def test_positive_all_five_gates(self):
        for gate in w.GATES:
            self.assertEqual('PASS', self.wf.validate(gate)['result'], gate)

    def test_g1_missing_design(self):
        del self.wf.stage('G1')['artifacts']['ui_design']
        self.assert_fails('G1', 'missing artifact ui_design')

    def test_g1_illegal_template(self):
        self.change_artifact('G1', 'page_mapping', lambda d: d['pages'][0].update(template='T7'))
        self.rebind('G1')
        self.assert_fails('G1', 'Illegal or missing T1')

    def test_g1_vertical_labels(self):
        self.change_artifact('G1', 'page_mapping', lambda d: d['pages'][0].update(label_control='above'))
        self.rebind('G1')
        self.assert_fails('G1', 'horizontal labels')

    def test_g1_missing_human_approval(self):
        self.wf.stage('G1')['approval'] = ''
        self.assert_fails('G1', 'Evidence path')

    def test_g2_missing_prototype(self):
        del self.wf.stage('G2')['artifacts']['prototype_manifest']
        self.assert_fails('G2', 'missing artifact prototype_manifest')

    def test_g2_react_substitute(self):
        self.change_artifact('G2', 'prototype_manifest', lambda d: d.update(stack=['React']))
        self.rebind('G2')
        self.assert_fails('G2', 'stack must be Vue')

    def test_g2_screenshot_pairs_must_cover_viewports(self):
        self.change_artifact('G2', 'screenshots', lambda d: d.update(pairs=d['pairs'][:1]))
        self.rebind('G2')
        self.assert_fails('G2', 'screenshot pairs do not cover')

    def test_g2_source_mapping_must_cover_pages(self):
        self.change_artifact('G2', 'prototype_manifest', lambda d: d.update(page_sources={'WRONG-PAGE': 'src/Simulation.vue'}))
        self.rebind('G2')
        self.assert_fails('G2', 'mapping does not cover approved pages')

    def test_g2_build_does_not_replace_browser(self):
        del self.wf.stage('G2')['runs']['prototype-browser']
        self.assert_fails('G2', 'no executed evidence for prototype-browser')

    def test_g2_mock_is_not_runtime(self):
        plan = self.wf.stage('G2')['test_plans']['prototype-browser']
        plan.update(environment='mock', runtime={'kind': 'mock'})
        self.rebind('G2')
        self.assert_fails('G2', 'mocks cannot count')

    def test_g3_unaccepted_prototype(self):
        self.wf.stage('G2')['status'] = 'REVIEW_PENDING'
        self.assert_fails('G3', 'G2 is not ACCEPTED')

    def test_g3_field_api_mismatch(self):
        self.change_artifact('G3', 'contracts', lambda d: d['projections']['api']['name'].update(type='integer'))
        self.rebind('G3')
        self.assert_fails('G3', 'Field/API/DB conflict')

    def test_g3_button_backend_permission_mismatch(self):
        self.change_artifact('G3', 'contracts', lambda d: d['operations'][0].update(backend_permission='different'))
        self.rebind('G3')
        self.assert_fails('G3', 'permission mismatch')

    def test_g3_missing_field_detail(self):
        self.change_artifact('G3', 'contracts', lambda d: d['fields'][0].pop('validation'))
        self.rebind('G3')
        self.assert_fails('G3', 'Incomplete field-level')

    def test_g3_missing_rtm(self):
        self.change_artifact('G3', 'contracts', lambda d: d.update(rtm={}))
        self.rebind('G3')
        self.assert_fails('G3', 'not traced')

    def test_g3_state_mismatch(self):
        self.change_artifact('G3', 'contracts', lambda d: d.update(backend_states=['WRONG']))
        self.rebind('G3')
        self.assert_fails('G3', 'State machine/backend')

    def test_g4_unfrozen_baseline(self):
        self.change_artifact('G3', 'freeze', lambda d: d.update(status='DRAFT'))
        self.rebind('G3')
        self.wf.stage('G3')['status'] = 'ACCEPTED'
        self.assert_fails('G4', 'Invalid freeze')

    def test_g4_broken_source_lineage(self):
        self.change_artifact('G4', 'lineage', lambda d: d.update(prototype_sha256='WRONG'))
        self.rebind('G4')
        self.assert_fails('G4', 'inheritance missing')

    def test_g4_production_mock(self):
        self.change_artifact('G4', 'no_mock', lambda d: d.update(production_mock_paths=['fake.ts']))
        self.rebind('G4')
        self.assert_fails('G4', 'Mock remains')

    def test_g4_inheritance_must_cover_all_pages(self):
        self.change_artifact('G4', 'lineage', lambda d: d.update(mappings=d['mappings'][:1]))
        self.rebind('G4')
        self.assert_fails('G4', 'omits an approved page')

    def test_g5_integration_failure(self):
        self.wf.stage('G5')['test_plans']['api-integration']['argv'].append('--fail')
        self.rebind('G5')
        self.assert_fails('G5', 'command failed')

    def test_g5_screenshot_pairs_must_cover_viewports(self):
        self.change_artifact('G5', 'visual_comparison', lambda d: d.update(pairs=d['pairs'][:1]))
        self.rebind('G5')
        self.assert_fails('G5', 'screenshot pairs do not cover')

    def test_g5_menu_authorization_missing(self):
        del self.wf.stage('G5')['test_plans']['menu-authorization']
        self.assert_fails('G5', 'menu-authorization missing')

    def test_g5_blocking_issue(self):
        self.change_artifact('G5', 'issues', lambda d: d.update(items=[{'severity': 'P2', 'resolved': False, 'blocking': True}]))
        self.rebind('G5')
        self.assert_fails('G5', 'Blocking issue remains')

    def test_g5_unapproved_contract_change(self):
        self.change_artifact('G5', 'integration_acceptance', lambda d: d.update(unapproved_contract_changes=['API']))
        self.rebind('G5')
        self.assert_fails('G5', 'Unapproved database/API/permission')

    def test_signed_junit_tampering_rejected(self):
        receipt = w.load_json(self.root / self.wf.stage('G1')['runs']['design-review'])
        path = self.root / receipt['payload']['junit']['path']
        path.write_text('<testsuite passed="true"/>', encoding='utf-8')
        self.assert_fails('G1', 'hash mismatch')

    def test_manual_passed_true_rejected(self):
        path = self.root / self.wf.stage('G1')['runs']['design-review']
        w.write_json(path, {'passed': True})
        self.assert_fails('G1', 'unsigned record')

    def test_approval_forgery_rejected(self):
        path = self.root / self.wf.stage('G1')['approval']
        data = w.load_json(path)
        data['payload']['approver'] = 'fake-human'
        w.write_json(path, data)
        self.assert_fails('G1', 'invalid signature')

    def test_delegated_approval_requires_source(self):
        path = self.root / self.wf.stage('G1')['approval']
        data = w.load_json(path)['payload']
        data['authority'] = 'delegated-authority'
        w.write_json(path, w.seal(data, 'approval'))
        self.assert_fails('G1', 'delegation source')

    def test_stale_source_rejected(self):
        (self.root / 'tests/fixture_suite.py').write_text('changed source', encoding='utf-8')
        self.assert_fails('G1', 'hash mismatch')

    def test_skip_requires_approval(self):
        self.wf.stage('G5')['test_plans']['regression']['argv'].append('--skip')
        self.rebind('G5')
        self.assert_fails('G5', 'decision mismatch')

    def test_next_stage_cannot_start(self):
        self.wf.stage('G2')['status'] = 'NOT_STARTED'
        self.wf.stage('G1')['status'] = 'REJECTED'
        with self.assertRaisesRegex(w.GateError, 'G1 is not ACCEPTED'):
            self.wf.advance('G2', 'IN_PROGRESS')

    def test_accepted_stage_cannot_reopen_implicitly(self):
        with self.assertRaisesRegex(w.GateError, 'frozen stages require change control'):
            self.wf.advance('G3', 'IN_PROGRESS')

    def test_simulation_cannot_accept_real_feature(self):
        with self.assertRaisesRegex(w.GateError, 'Simulation records cannot'):
            w.Workflow(self.root, 'WF-TEST-001')

    def test_path_escape_rejected(self):
        self.wf.stage('G1')['artifacts']['ui_design']['path'] = '../outside.json'
        self.assert_fails('G1', 'escapes repository')

    def test_missing_trust_fails_closed(self):
        name = 'MES_WORKFLOW_APPROVAL_SIGNERS'
        old = os.environ.pop(name)
        try:
            self.assert_fails('G1', 'not configured')
        finally:
            os.environ[name] = old

    def test_explicit_menu_feature_template_routing(self):
        cases = [('使用MES标准流程新增菜单', 'menu'), ('使用 MES 标准流程开发新功能', 'feature'), ('按MES标准流程修改T1模板', 'template')]
        for prompt, kind in cases:
            route = w.route_request(prompt)
            self.assertTrue(route['invoked'])
            self.assertEqual(kind, route['scope_kind'])
            self.assertEqual(list(w.GATES), route['stages'])

    def test_ordinary_requests_do_not_trigger(self):
        for prompt in ('开发菜单管理', '修改页面模板', '修复功能', '看看 MES 标准流程是什么'):
            self.assertFalse(w.route_request(prompt)['invoked'])

    def test_cli_failure_exit_matches_ci(self):
        with contextlib.redirect_stdout(io.StringIO()):
            gate_exit = w.main(['validate-workflow', '--feature', 'MISSING-001', '--gate', 'G5'], root=self.root)
            path = self.root / 'changed-files.txt'
            path.write_text('frontend/mes-web/src/views/unregistered.vue\n', encoding='utf-8')
            ci_exit = w.main(['ci', '--changed-files', str(path)], root=self.root)
        self.assertEqual(1, gate_exit)
        self.assertEqual(gate_exit, ci_exit)

    def test_ci_governance_only_has_no_business_gate(self):
        path = self.root / 'changed-files.txt'
        path.write_text('docs/governance/MES_GATE_POLICY.md\n', encoding='utf-8')
        with contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(0, w.main(['ci', '--changed-files', str(path)], root=self.root))

    def test_ci_scope_detection(self):
        features, uncovered = w.affected(self.root, ['frontend/mes-web/src/views/simulation.vue', 'backend/unregistered.java'])
        self.assertEqual(['WF-TEST-001'], features)
        self.assertEqual(['backend/unregistered.java'], uncovered)


class RecordingResult(unittest.TextTestResult):
    records = []
    def startTest(self, test):
        self.started = time.monotonic()
        super().startTest(test)
    def addSuccess(self, test):
        self.records.append({'name': test.id(), 'result': 'PASS', 'seconds': round(time.monotonic() - self.started, 3)})
        super().addSuccess(test)
    def addFailure(self, test, err):
        self.records.append({'name': test.id(), 'result': 'FAIL', 'error': self._exc_info_to_string(err, test)})
        super().addFailure(test, err)
    def addError(self, test, err):
        self.records.append({'name': test.id(), 'result': 'ERROR', 'error': self._exc_info_to_string(err, test)})
        super().addError(test, err)


if __name__ == '__main__':
    output = None
    if '--output' in sys.argv:
        index = sys.argv.index('--output')
        output = Path(sys.argv[index + 1])
        del sys.argv[index:index + 2]
    start = time.monotonic()
    result = unittest.TextTestRunner(verbosity=2, resultclass=RecordingResult).run(unittest.defaultTestLoader.loadTestsFromTestCase(Gates))
    if output:
        report = {'suite': 'mes-feature-workflow-selftest', 'simulation': True, 'tests': result.testsRun, 'failures': len(result.failures), 'errors': len(result.errors), 'skipped': len(result.skipped), 'duration_seconds': round(time.monotonic() - start, 3), 'result': 'PASS' if result.wasSuccessful() else 'FAIL', 'cases': result.records, 'source_sha256': w.file_hash(SCRIPT), 'test_sha256': w.file_hash(Path(__file__)), 'timestamp': w.now(), 'business_acceptance': False}
        w.write_json(output, report)
        suite = ET.Element('testsuite', name=report['suite'], tests=str(result.testsRun), failures=str(len(result.failures)), errors=str(len(result.errors)), time=str(report['duration_seconds']))
        for record in result.records:
            case = ET.SubElement(suite, 'testcase', name=record['name'], classname='workflow.selftest', time=str(record.get('seconds', 0)))
            if record['result'] != 'PASS':
                ET.SubElement(case, 'failure', message=record['result']).text = record.get('error', '')
        ET.ElementTree(suite).write(output.with_suffix('.xml'), encoding='utf-8', xml_declaration=True)
    sys.exit(0 if result.wasSuccessful() else 1)
