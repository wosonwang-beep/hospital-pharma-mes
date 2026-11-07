# EBR schema2 role-identity regression — 2026-11-07

## GitHub CI failure and contract boundary

After the Linux CJK PDF fix, GitHub Actions [#57](https://github.com/wosonwang-beep/hospital-pharma-mes/actions/runs/42382331177) passed the full mes-reporting suite (54 tests, 0 failures) and advanced to mes-ebr. The first new backend blocker was the existing `EbrRoleSchemaTest.missingIdNeverFallsBackToMatchingCode` assertion: missing requiredRoleId must throw `IllegalArgumentException`, but `EbrRoleSchema.prepare` had begun converting every definition-lint issue to `ComplianceException("LINT_FAILED")`.

This is a real EBR backward-contract distinction, not a platform font issue. Existing authorizations forbid allowing legacy role codes to replace required numeric role IDs in schema2, and the editable form/CPP shape lint still needs to return `LINT_FAILED` for other malformed fields.

## Smallest implementation correction

In `EbrRoleSchema.prepare`, when `EbrDefinitionRules.lint` reports a `REQUIRED_ROLE_ID` issue, preserve the pre-existing direct argument exception. Other lint issues retain their `ComplianceException("LINT_FAILED")` behavior. The code never falls back to legacy roleCode. No test expectation or validator threshold was changed.

## Local validation

```text
mvn -pl backend/mes-boot -am -Dtest=EbrRoleSchemaTest,EbrIT -Dsurefire.failIfNoSpecifiedTests=false test

EbrRoleSchemaTest 12 tests PASS
EbrIT 13 tests PASS
BUILD SUCCESS — 25/25 total, 0 failures, 0 errors, 0 skips
```

The tests ran against the actual single local `main` checkout and existing development services. No released EBR template or production signature data was altered by this source fix.

## Separate blocker

The same GitHub run's frontend type check still has the QC basic-library release mismatch; no unrelated QC/schema/migration files are bundled with this EBR correction. Full hosted CI is not green until all jobs complete.
