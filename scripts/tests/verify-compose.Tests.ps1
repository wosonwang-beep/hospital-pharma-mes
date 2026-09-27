Describe 'Docker Compose Foundation contract' {
  BeforeAll {
    $script:root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
    $script:compose = Get-Content -LiteralPath (Join-Path $script:root 'docker-compose.yml') -Raw
    $script:envExample = Get-Content -LiteralPath (Join-Path $script:root '.env.example') -Raw
  }
  It 'pins required images and rejects legacy or floating images' {
    $script:compose | Should Match 'mariadb:11\.8\.9-noble'
    $script:compose | Should Match 'redis:8\.2\.9-bookworm'
    $script:compose | Should Match 'quay\.io/minio/aistor/minio:RELEASE\.2026-02-02T23-40-11Z'
    $script:compose | Should Not Match '(?m)^\s*image:\s*\S+:latest\s*$'
    $script:compose | Should Not Match '(^|/)minio/minio:'
  }
  It 'defines health checks, named volumes, and the local network' {
    ([regex]::Matches($script:compose, 'healthcheck:')).Count | Should Be 3
    foreach ($name in @('mariadb_data:', 'redis_data:', 'minio_data:', 'hospital-pharma-mes')) { $script:compose | Should Match ([regex]::Escape($name)) }
  }
  It 'documents application-compatible variables without committing an env file' {
    foreach ($name in @('MES_DB_USERNAME', 'MES_DB_PASSWORD', 'MES_REDIS_PASSWORD', 'MES_MINIO_ACCESS_KEY', 'MES_MINIO_SECRET_KEY', 'MES_MINIO_LICENSE_FILE')) { $script:envExample | Should Match "(?m)^$name=" }
    Test-Path -LiteralPath (Join-Path $script:root '.env') | Should Be $false
  }
}
