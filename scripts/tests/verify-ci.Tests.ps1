$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$workflowPath = Join-Path $root '.github/workflows/ci.yml'
$validator = Join-Path $root 'scripts/verify-ci.ps1'
$source = Get-Content -LiteralPath $workflowPath -Raw

function Assert-Rejected {
  param([string]$Label, [string]$WorkflowSource)
  $rejected = $false
  try {
    & $validator -WorkflowSource $WorkflowSource | Out-Null
  } catch {
    $rejected = $true
  }
  if (-not $rejected) { throw "CI validator accepted $Label." }
}

& $validator -WorkflowSource $source | Out-Null

$unpinnedAction = $source -replace 'actions/checkout@[0-9a-f]{40}', 'actions/checkout@v4'
if ($unpinnedAction -eq $source) { throw 'Action-pin fixture did not change the workflow.' }
Assert-Rejected 'an unpinned action' $unpinnedAction

$topLevelWrite = $source -replace '(?m)^  contents: read$', "  contents: read`n  actions: write"
if ($topLevelWrite -eq $source) { throw 'Top-level permission fixture did not change the workflow.' }
Assert-Rejected 'an additional write permission' $topLevelWrite

$jobLevelWrite = $source -replace '(?m)^  backend:$', "  backend:`n    permissions: write-all"
if ($jobLevelWrite -eq $source) { throw 'Job-level permission fixture did not change the workflow.' }
Assert-Rejected 'a job-level write-all override' $jobLevelWrite

$extraJob = $source + "`n  unexpected-job:`n    runs-on: ubuntu-latest`n    steps: []`n"
Assert-Rejected 'an unreviewed fifth job' $extraJob

Write-Host 'CI contract negative tests: PASS'
