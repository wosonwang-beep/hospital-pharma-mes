$ErrorActionPreference = 'Stop'
$workflow = Join-Path (Split-Path -Parent $PSScriptRoot) '.github/workflows/ci.yml'
if (-not (Test-Path -LiteralPath $workflow)) { throw 'CI workflow is missing.' }
$source = Get-Content -LiteralPath $workflow -Raw

$required = @{
  'pull request trigger' = '(?m)^  pull_request:'
  'main push trigger' = '(?ms)^  push:\s*\r?\n\s+branches:\s*\[main\]'
  'manual trigger' = '(?m)^  workflow_dispatch:'
  'read-only permissions' = '(?ms)^permissions:\s*\r?\n\s+contents: read'
  'concurrency cancellation' = '(?m)^  cancel-in-progress: true'
  'backend job' = '(?m)^  backend:'
  'frontend job' = '(?m)^  frontend:'
  'integration job' = '(?m)^  infrastructure-integration:'
  'repository job' = '(?m)^  repository-contract:'
  'Java 21' = '(?m)^\s+java-version: .?21.?#?'
  'Node.js 22' = '(?m)^\s+node-version: .?22.?#?'
  'MariaDB service' = '(?m)^\s+mariadb:'
  'Redis service' = '(?m)^\s+redis:'
  'integration tests' = 'mvn -B -ntp -Pci-integration verify'
  'frontend install' = 'npm ci'
  'frontend tests' = 'npm test -- --run'
  'frontend types' = 'npm run typecheck'
  'frontend build' = 'npm run build'
  'dependency audit' = 'npm audit --omit=dev --audit-level=high'
  'repository check' = 'verify-repository\.ps1'
}
foreach ($entry in $required.GetEnumerator()) {
  if ($source -notmatch $entry.Value) { throw "CI contract is missing $($entry.Key)." }
}
if ($source -notmatch 'github\.event\.pull_request\.head\.ref' -or $source -notmatch 'github\.ref') {
  throw 'CI concurrency must distinguish pull-request heads and Git refs.'
}
if ($source -match '(?m)^\s+minio:|secrets\.|contents: write|packages: write') {
  throw 'CI must not request MinIO, secrets, or write permissions.'
}
foreach ($use in [regex]::Matches($source, '(?m)^\s+- uses:\s+([^\s#]+)')) {
  if ($use.Groups[1].Value -notmatch '^actions/[a-z-]+@[0-9a-f]{40}$') {
    throw "Action is not pinned to a commit SHA: $($use.Groups[1].Value)"
  }
}
Write-Host 'CI contract: PASS'
