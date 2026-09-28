$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

function Invoke-Checked {
  param([string]$Label, [scriptblock]$Action)
  Write-Host "`n== $Label =="
  & $Action
  if ($LASTEXITCODE -ne 0) { throw "$Label failed with exit code $LASTEXITCODE." }
}

Push-Location $root
try {
  Invoke-Checked 'Backend Maven tests' { mvn -B -ntp test }
  Push-Location (Join-Path $root 'frontend/mes-web')
  try {
    Invoke-Checked 'Frontend clean install' { npm ci }
    Invoke-Checked 'Frontend tests' { npm test -- --run }
    Invoke-Checked 'Frontend type check' { npm run typecheck }
    Invoke-Checked 'Frontend production build' { npm run build }
  } finally {
    Pop-Location
  }
  Write-Host "`n== Repository verification =="
  & (Join-Path $PSScriptRoot 'verify-repository.ps1')
  if ($LASTEXITCODE -ne 0) { throw "Repository verification failed with exit code $LASTEXITCODE." }
  Write-Host "`n== CI workflow verification =="
  & (Join-Path $PSScriptRoot 'verify-ci.ps1')
  if ($LASTEXITCODE -ne 0) { throw "CI workflow verification failed with exit code $LASTEXITCODE." }
  & (Join-Path $PSScriptRoot 'tests/verify-ci.Tests.ps1')
  if ($LASTEXITCODE -ne 0) { throw "CI workflow negative tests failed with exit code $LASTEXITCODE." }
  Write-Host "`nFoundation verification: PASS"
} finally {
  Pop-Location
}
