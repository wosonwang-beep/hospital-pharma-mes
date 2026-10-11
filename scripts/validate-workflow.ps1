# PowerShell preserves the validator exit status.
$workflowScript = Join-Path $PSScriptRoot '../.agents/skills/mes-feature-workflow/scripts/workflow.py'
& python $workflowScript validate-workflow @args
exit $LASTEXITCODE
