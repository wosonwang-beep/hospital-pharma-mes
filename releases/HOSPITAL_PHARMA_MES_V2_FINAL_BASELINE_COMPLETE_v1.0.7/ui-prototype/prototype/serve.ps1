$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$python = 'C:\Users\Administrator\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
& $python -m http.server 4173 --bind 127.0.0.1 --directory $root
