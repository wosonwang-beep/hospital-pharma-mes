#!/usr/bin/env python3
"""Read-only Codex app-server discovery probe. Creates no chat or business task."""
import argparse
import hashlib
import json
from pathlib import Path
import queue
import subprocess
import sys
import threading
import time

ROOT = Path(__file__).resolve().parents[4]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', required=True)
    args = parser.parse_args()
    messages = queue.Queue()
    process = subprocess.Popen(['codex', 'app-server', '--stdio'], cwd=ROOT, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True, encoding='utf-8')
    def read():
        for line in process.stdout:
            try:
                messages.put(json.loads(line))
            except ValueError:
                pass
    threading.Thread(target=read, daemon=True).start()
    def request(method, params, identifier):
        process.stdin.write(json.dumps({'id': identifier, 'method': method, 'params': params}) + '\n')
        process.stdin.flush()
        deadline = time.monotonic() + 40
        while time.monotonic() < deadline:
            try:
                message = messages.get(timeout=1)
            except queue.Empty:
                continue
            if message.get('id') == identifier:
                if message.get('error'):
                    raise RuntimeError(str(message['error']))
                return message.get('result')
        raise TimeoutError(method + ' response timeout')
    report = {'probe': 'actual-codex-app-server-skills/list', 'cwd': str(ROOT), 'business_task_created': False}
    try:
        request('initialize', {'clientInfo': {'name': 'mes-workflow-verification', 'version': '2.0'}, 'capabilities': {'experimentalApi': True}}, 1)
        process.stdin.write(json.dumps({'method': 'initialized', 'params': {}}) + '\n')
        process.stdin.flush()
        result = request('skills/list', {'cwds': [str(ROOT)], 'forceReload': True}, 2)
        entries = [skill for item in result.get('data', []) for skill in item.get('skills', []) if skill.get('name') == 'mes-feature-workflow']
        if len(entries) != 1 or not entries[0].get('enabled'):
            raise RuntimeError('Expected exactly one enabled project skill: ' + str(entries))
        skill = entries[0]
        path = Path(skill['path'])
        if path.resolve() != (ROOT / '.agents/skills/mes-feature-workflow/SKILL.md').resolve():
            raise RuntimeError('Discovered unexpected skill path')
        text = path.read_text(encoding='utf-8')
        policy = path.parent / 'agents/openai.yaml'
        if 'allow_implicit_invocation: false' not in policy.read_text(encoding='utf-8-sig'):
            raise RuntimeError('Explicit-only policy missing')
        if '使用 MES 标准流程开发 XX 功能' not in text:
            raise RuntimeError('Explicit language instruction missing')
        report.update(result='PASS', discovered=skill, source_sha256=hashlib.sha256(path.read_bytes()).hexdigest(), policy_sha256=hashlib.sha256(policy.read_bytes()).hexdigest(), explicit_only=True, loaded=True)
    except Exception as exc:
        report.update(result='FAIL', error=str(exc))
    finally:
        process.terminate()
        try:
            process.wait(timeout=5)
        except subprocess.TimeoutExpired:
            process.kill()
    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({k: v for k, v in report.items() if k != 'discovered'}, ensure_ascii=False))
    return 0 if report['result'] == 'PASS' else 1


if __name__ == '__main__':
    sys.exit(main())
