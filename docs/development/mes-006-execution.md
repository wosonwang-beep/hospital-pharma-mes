# MES-006 execution ledger

Approval: explicit “确认授权” for DCP-MES-006-R2-001. Execute inline per executing-plans, reuse current upstream changes on codex/mes-006-process; no commits/push requested.

1. DONE: publish approved v1.0.7 contracts and closed OpenAPI; verify physical history (V010 success read).
2. DONE: append V011; implement products/process persistence, commands/lint/signature/query/events/API/security.
3. DONE: independent product/process UI using accepted list/edit style; BOM/route/parameters and reauthentication approval.
4. DONE: focused domain, safe native MariaDB migration/integration/consumed contract gate and frontend build/browser; failure-only followups.
5. DONE: fresh scoped review, fix blockers, consistency/hash evidence and MES_TASKS READY FOR ACCEPTANCE.

Rulings: authoritative approved document records precise DTO/state/signature/content and row-retention choices. Reuse MasterMutation/ScopedStore and platform signature services; no parallel platform infrastructure. Only one draft per package; saved definitions retained; optional empty new version supports deliberate replacement without physical deletion. Local clean-database replay forbidden by persistent DEV policy; hosted CI retains clean-chain gate.


Verification: backend build, 5 unit / 9 MES006 IT / 1 UOM direct regression and frontend build PASS; 6 distinct browser cases across scoped gates PASS. Real two-transaction conflict verified. First migration V010→V011 succeeded and subsequent 11 checksums validated. First proxy-access and bad-request mapping failures fixed; failures/affected cases only rerun. Reviewer HIGH leaf-rule roundtrip and MEDIUM rename/busy-edit loss fixed; product loading race fixed. See docs/acceptance/mes-006/ACCEPTANCE.md. Existing user changes preserved; no commit/push.

Final rulings: user explicitly required existing design, so implementation followed original mandatory artifacts/prototype plus already approved bounded completion; no new design approval loop. Saved definitions are retained per approved contract. Hosted clean-chain CI remains a merge gate; local persistent database was preserved. LOW query-volume/bundle debt recorded.
