$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$composePath = Join-Path $root 'docker-compose.yml'
$envExamplePath = Join-Path $root '.env.example'
if (-not (Test-Path -LiteralPath $composePath)) { throw 'docker-compose.yml is missing.' }
if (-not (Test-Path -LiteralPath $envExamplePath)) { throw '.env.example is missing.' }
$compose = Get-Content -LiteralPath $composePath -Raw
foreach ($token in @('mariadb:', 'redis:', 'minio:', 'healthcheck:', 'mariadb_data:', 'redis_data:', 'minio_data:')) {
  if (-not $compose.Contains($token)) { throw "Compose contract is missing: $token" }
}
foreach ($token in @('hospital_pharma_mes_dev', 'name: hospital-pharma-mes-mariadb-dev', 'name: hospital-pharma-mes-redis-dev')) {
  if (-not $compose.Contains($token)) { throw "Persistent DEV contract is missing: $token" }
}
if ($compose -match '(?m)^\s*image:\s*\S+:latest\s*$') { throw 'Floating latest image tags are forbidden.' }
if ($compose -match '(^|/)minio/minio:') { throw 'Archived legacy MinIO binary images are forbidden.' }
if ($compose -notmatch 'quay\.io/minio/aistor/minio:RELEASE\.2026-') { throw 'A pinned maintained 2026 AIStor image is required.' }
$envExample = Get-Content -LiteralPath $envExamplePath -Raw
foreach ($name in @('MES_DB_USERNAME', 'MES_DB_PASSWORD', 'MES_REDIS_PASSWORD', 'MES_MINIO_ACCESS_KEY', 'MES_MINIO_SECRET_KEY', 'MES_MINIO_LICENSE_FILE')) {
  if ($envExample -notmatch "(?m)^$name=") { throw ".env.example is missing $name." }
}
if ($envExample -notmatch '(?m)^MES_DB_NAME=hospital_pharma_mes_dev$') { throw '.env.example must default to hospital_pharma_mes_dev.' }
if ($envExample -notmatch '(?m)^MES_DB_URL=jdbc:mariadb://localhost:3306/hospital_pharma_mes_dev$') { throw '.env.example has an invalid persistent DEV JDBC URL.' }
Write-Host 'Compose static contract: PASS'
$docker = Get-Command docker -ErrorAction SilentlyContinue
if ($null -eq $docker) {
  Write-Host 'Docker runtime validation: UNAVAILABLE (docker command not installed)'
  exit 0
}
& docker compose --env-file $envExamplePath -f $composePath config --quiet
if ($LASTEXITCODE -ne 0) { throw 'docker compose config failed.' }
Write-Host 'Docker Compose configuration: PASS'
