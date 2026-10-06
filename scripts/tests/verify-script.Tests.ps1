Describe 'Unified verification script contract' {
  BeforeAll {
    $script:root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
    $script:verify = Get-Content -LiteralPath (Join-Path $script:root 'scripts/verify.ps1') -Raw
  }
  It 'stops on errors and checks external command exit codes' {
    $script:verify | Should Match "ErrorActionPreference = 'Stop'"
    $script:verify | Should Match 'LASTEXITCODE'
    $script:verify | Should Match 'throw'
  }
  It 'runs backend and frontend verification stages' {
    foreach ($command in @('mvn -B -ntp test', 'npm ci', 'npm test -- --run', 'npm run typecheck', 'npm run build')) { $script:verify | Should Match ([regex]::Escape($command)) }
  }
  It 'delegates Compose validation with explicit Docker status' {
    $script:verify | Should Match 'verify-repository\.ps1'
    (Get-Content -LiteralPath (Join-Path $script:root 'scripts/verify-repository.ps1') -Raw) | Should Match 'verify-compose\.ps1'
    (Get-Content -LiteralPath (Join-Path $script:root 'scripts/verify-compose.ps1') -Raw) | Should Match 'UNAVAILABLE'
  }
}
