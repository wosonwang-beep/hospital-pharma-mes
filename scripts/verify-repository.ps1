$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

foreach ($relativePath in @(
  'AGENTS.md', 'README.md', '.gitignore', '.env.example', 'docker-compose.yml',
  'pom.xml', 'backend/pom.xml', 'backend/mes-boot/pom.xml',
  'frontend/mes-web/package-lock.json', 'database/migration', 'docs/architecture',
  'deploy/docker', 'scripts/verify-compose.ps1'
)) {
  if (-not (Test-Path -LiteralPath (Join-Path $root $relativePath))) {
    throw "Repository contract is missing $relativePath."
  }
}

$ignore = Get-Content -LiteralPath (Join-Path $root '.gitignore')
foreach ($rule in @('.env', 'deploy/docker/minio.license', 'target/', 'node_modules/', 'dist/', '*.tsbuildinfo')) {
  if ($ignore -cnotcontains $rule) { throw ".gitignore is missing $rule." }
}

Push-Location $root
try {
  $tracked = @(& git ls-files)
  if ($LASTEXITCODE -ne 0) { throw 'Could not inspect the Git index.' }
  foreach ($path in $tracked) {
    if ($path -match '(^|/)target/|(^|/)node_modules/|(^|/)dist/|\.tsbuildinfo$|^\.env$|^deploy/docker/minio\.license$|^frontend/mes-web/vite\.config\.(js|d\.ts)$') {
      throw "Generated output or a local secret is tracked: $path"
    }
  }
  & (Join-Path $PSScriptRoot 'verify-compose.ps1')
  if ($LASTEXITCODE -ne 0) { throw 'Compose contract verification failed.' }
  Write-Host 'Repository contract: PASS'
} finally {
  Pop-Location
}
