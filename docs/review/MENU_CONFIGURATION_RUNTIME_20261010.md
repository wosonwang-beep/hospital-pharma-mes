# Menu Configuration Runtime Evidence

2026-10-10 17:32 +08:00. Current task readiness is maintained only in MES_TASKS.md. No human final acceptance, commit or push was performed.

## Baseline / Read Gate

Physically read AGENTS.md, docs/development/MES_DEV_RULES.md, MES_TASKS.md menu entry, docs/PROJECT_BASELINE.md authority, database-and-validation-strategy.md, UI V2/T1/T2, approved database-menu DCP and DCP-MENU-CONFIGURATION-001, and MENU_FUNCTION_DESIGN_LOCK_20261010.md. Existing IAM CRUD, audit, idempotency, security, navigation, V037/V044-V050 and prototype source were reviewed before implementation. Authority remains v1.0.23 with bounded approved supplements; historical releases/migrations remain unchanged.

## Scope / Design Differences

Implemented frozen T1 menu list and T2/T3 configuration, fine query/list heading dividers, horizontal labels, tree/paging/search/reset, special-permission drafts, readonly role projection, cancel/restore, and aggregate save. Real formal route/code/id/version/catalog replace DEMO fixtures; no business data is persisted in localStorage. Existing formal breadcrumb/account controls are retained, rather than copying prototype simulation labels.

Compared actual 1440/1280/900/375 list screenshots with mes-demo/evidence/menu-list-dividers/menus-{width}.png and configuration structure with the frozen Vue source/reference. Every final captured screenshot was visually inspected. Heading dividers, section order, controls and local table scrolling match the locked business design. Real data changes row lengths/count and table track allocation. Existing formal breadcrumb adds its existing vertical band. Shared shell duplication at375 was found and repaired after explicit human authorization at17:26; final title is unobscured. Full-page capture now explicitly scrolls to the top, preserving first-run screenshots as failure evidence. Low visual debt: mobile pager count can wrap; large tables require local horizontal scrolling as specified.

Actual source files: backend/mes-system IamContractService.java, IamContractController.java, SysPermissionEntity.java, SysPermissionMapper.java; backend/mes-security SecurityConfiguration.java exact endpoint matchers; backend/mes-boot V051 and MenuConfigurationIT.java; frontend/mes-web/src/api/http.ts, router/index.ts, views/admin/MenusView.vue, MenusView.test.ts, MenuDetailView.vue, menuConfiguration.css, menuConfigurationModel.ts and its test. Human-approved extension: layouts/dashboard-shell.css and vite.config.ts. Governance/API/UI appendices and the two scripts/menu-configuration-*-20261010.py evidence runners complete the scoped changes. Existing local application-local.yml/application-ci.yml edits, unrelated files and user records were preserved.

## Migration / Data

V051__menu_configuration_description_and_links.sql executed successfully in the existing native DEV database. Flyway validates51 migrations; highest success051. Adds nullable permission_description VARCHAR512 and missing links for already initialized formal menu/view permissions and existing production-prescription definitions. No duplicate menu tree, DEMO import, role-grant expansion, user-setting overwrite, clean/repair/reset or second database. Missing existing page view links after migration:0.

Existing V037 and subsequent migration seeds remain the formal initialization baseline; V051 only completes missing associations. These are versioned Flyway records, not prototype runtime fixtures. Upgrade insertion is NOT EXISTS-based and does not overwrite customized menu/permission definitions.

Rollback tests used unique records, not shared seed mutations. Live created only TEST_MENU_1791624207 (id100) and TEST_MENU_1791624503 (id101), plus their unique iam:menu-check-*:export permissions. Both menus and both permission definitions were retired via supported APIs (INACTIVE), not physically deleted. Actual SQL confirmed first permission description persisted as 验收修改说明 and enabled0/version2. Role permission count observed350; unchanged grants are proved by the targeted unlink integration assertion, not inferred solely from this count.

## Gate Evidence

Static/build PASS: npm --prefix frontend/mes-web run typecheck; npm --prefix frontend/mes-web run build (including vue-tsc), exit0. Final build16.47s, existing large-chunk warning retained. Java21 Maven source compile/package exit0. Actual source JAR running PID5908 on8080, health UP; frontend5173 HTTP200. No alternate checkout or service ports.

Frontend targeted regression PASS: npm --prefix frontend/mes-web test -- --run src/views/admin/MenusView.test.ts src/views/admin/menuConfigurationModel.test.ts src/layouts/AppLayout.auth.test.ts; 3files/10tests,0fail/0skip,12.16s, exit0. Original menu-modal test contracts were migrated before testing under the approved independent-page/atomic-save supplement, retaining required-field, If-Match, conflict-draft and retry assertions.

Backend native-DEV integration PASS: mvn -f backend/pom.xml -pl mes-boot -am -Pci-integration '-Dit.test=MenuConfigurationIT,DatabaseMenuAuthorizationIT' '-Dtest=NoSuchUnitTest' '-Dsurefire.failIfNoSpecifiedTests=false' '-Dfailsafe.failIfNoSpecifiedTests=false' verify -q; exit0. MenuConfigurationIT5 + DatabaseMenuAuthorizationIT3 =8,0fail/0error/0skip; durations0.257s/11.08s. Selected fake unit name prevents unrelated full regression, not skipping the required integration suites. Reports backend/mes-boot/target/failsafe-reports/TEST-com.hospital.mes.iam.integration.*.xml.

Real browser/API PASS: C:/Users/Administrator/.codex/tools/cuawright-venv/Scripts/python.exe scripts/menu-configuration-live-20261010.py (21checks) and scripts/menu-configuration-interactions-20261010.py (14checks), both exit0. No API fixtures/mocks. Query keyword is recorded in real HTTP; create POST and update PUT aggregate200; description persists on reread; draft has0writes; readonly cannot save; anonymous write401; existing low-right demo.production write403. Tree expansion/collapse/paging, empty search, required name, cancel decline/accept, restore fields/bindings, link/unlink draft, role projection and page-refresh session restoration all checked. Final JavaScript errors0. Evidence: evidence/menu-configuration-20261010/result.json, interactions.json, list-{1440,1280,900,375}.png and edit-{1440,1280,900,375}.png.

RTM: MENU-CONFIG-01 descriptions/compatibility -> integration and live reread;02 rollback/conflict -> integration plus frontend conflict cases;03 rights/org/idempotency -> integration,401/403live;04 grant-preserving unlink -> integration and UI draft;05 seeds -> V051 review/Flyway/SQL;06 interactions ->35live checks and10frontend tests;07 geometry/visual -> eight final screenshots and label/overflow checks. These are targeted results, not full-system acceptance.

## Repairs / Review / Debt

Budget17:03-17:33 (30min), completed within budget. Automatic implementation repair round1: after refresh readonly failed, user approved expanding shared shell/dev-proxy scope. Vite string proxy default changeOrigin:true rewrote Host but retained browser Origin; replaced with explicit changeOrigin:false to preserve the same-origin contract. No backend cookie/CSRF rule was weakened. Mobile CSS hides duplicate user strip and preserves account dropdown commands. First failure result-first-failure.json/failure.png/edit-375-before.png retained. Retest21checks passed.

Additional interaction runner initially encountered an ambiguous Ant Select locator (three labelled nodes), classified as automation-selector error. Scoped to .ant-modal .ant-select; no product code, assertion, threshold or skip-condition change; identical14checks passed. Original interactions-selector-failure.json retained. Initial Maven locked-JAR and PowerShell quoting failures, and a wrong frontend working-directory command, were environmental invocation errors; corrected invocations exited0. MariaDB13-vs-Flyway11.2 warning and build chunk warning remain. No same failure persisted after targeted repair.

Scoped self-review found no unresolved CRITICAL/HIGH issue in verified flows. MEDIUM follow-up: malformed blank permission-change id may reach null unboxing rather than a clean400; valid UI supplies null or real id, and no additional mutation is authorized by this malformed input. Multi-browser/load and full regression NOT RUN (not required for this bounded task). Runtime log .tmp-menu-backend-out.log records51validate and8080startup. git diff --check exit0. Final human acceptance remains required.
