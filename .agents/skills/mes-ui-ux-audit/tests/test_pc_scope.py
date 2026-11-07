"""Synthetic applicability contract checks; these are not product acceptance."""
import hashlib
import json
import tempfile
import unittest
from pathlib import Path
from test_executor_compatibility import fixture, invoke, VALIDATOR, ROOT, NOTE


class PcScopeTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.directory = Path(self.temp.name)
        (self.directory / 'fixture-evidence.txt').write_text(NOTE, encoding='utf-8')
        (self.directory / 'fixture-pixel.png').write_bytes(b'synthetic-contract-only')
        self.approval = ROOT / 'docs/development/decisions/PROCESS_PC_ONLY_SCOPE_20261104_APPROVED.md'

    def pc(self):
        d = fixture()
        d['source']['url'] = 'http://127.0.0.1:5174/#/processes'
        d['required_viewports'] = [1440, 1280, 900]
        d['screenshots'] = [s for s in d['screenshots'] if s['viewport'] != 375]
        d['applicability'] = {'mode': 'PC_ONLY', 'approval': str(self.approval), 'approval_sha256': hashlib.sha256(self.approval.read_bytes()).hexdigest()}
        return d

    def run_manifest(self, data):
        path = self.directory / 'manifest.json'
        path.write_text(json.dumps(data), encoding='utf-8')
        return invoke(VALIDATOR, path)

    def test_default_still_requires_mobile(self):
        d = self.pc(); del d['applicability']
        code, output = self.run_manifest(d)
        self.assertEqual(code, 1); self.assertIn('1440, 1280, 900, 375', output)

    def test_valid_pc_scope(self):
        self.assertEqual(self.run_manifest(self.pc())[0], 0)

    def test_default_four_widths_unchanged(self):
        self.assertEqual(self.run_manifest(fixture())[0], 0)

    def test_each_pc_width_required(self):
        for width in (1440, 1280, 900):
            with self.subTest(width=width):
                d = self.pc(); d['required_viewports'].remove(width)
                self.assertEqual(self.run_manifest(d)[0], 1)

    def test_invalid_approvals_rejected(self):
        for replacement in ({}, {'mode':'PC_ONLY'}, {'mode':'PC_ONLY','approval':'missing.md'}, {'mode':'OTHER'}):
            with self.subTest(replacement=replacement):
                d = self.pc(); d['applicability'] = replacement
                self.assertEqual(self.run_manifest(d)[0], 1)
        d = self.pc(); d['applicability']['approval_sha256'] = '0'*64
        self.assertEqual(self.run_manifest(d)[0], 1)
        copied = self.directory / self.approval.name
        copied.write_bytes(self.approval.read_bytes())
        d = self.pc(); d['applicability']['approval'] = str(copied)
        self.assertEqual(self.run_manifest(d)[0], 1)

    def test_scope_cannot_apply_to_other_pages(self):
        d = self.pc(); d['source']['url'] = 'http://127.0.0.1:5174/#/roles'
        self.assertEqual(self.run_manifest(d)[0], 1)

    def test_pc_cannot_bypass_evidence_or_quality(self):
        mutations = [
            lambda d: d['screenshots'].pop(),
            lambda d: d['screenshots'][0].update(path='missing.png'),
            lambda d: d['issues'].append({'id':'HIGH-1','severity':'HIGH','status':'OPEN'}),
            lambda d: d['interaction_checks'][0].update(status='FAIL'),
            lambda d: d['geometry_checks'][0].update(status='FAIL'),
            lambda d: d['wcag22_checks'][0].update(status='NOT RUN'),
            lambda d: d['dimension_reviews'][0].update(status='FAIL'),
            lambda d: d['adversarial_pass'].update(completed=False),
        ]
        for index, mutate in enumerate(mutations):
            with self.subTest(index=index):
                d = self.pc(); mutate(d)
                self.assertEqual(self.run_manifest(d)[0], 1)


if __name__ == '__main__':
    unittest.main()
