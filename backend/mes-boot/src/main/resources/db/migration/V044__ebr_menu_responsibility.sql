-- EBR responsibility alignment:
-- 1) Electronic batch record template remains an independent production-management feature.
-- 2) Process form templates are reached only from Production Process -> Process Forms.
-- Historical routes and records remain available for controlled links; only the duplicate navigation entry is retired.

UPDATE sys_menu
SET menu_name = '电子批记录模板',
    sort_no = 40,
    permission_code = 'ebr:template:view',
    required_permissions = 'process:package:view',
    status = 'ACTIVE',
    version_no = version_no + 1
WHERE route_path = '/ebr/book-templates';

UPDATE sys_menu
SET status = 'INACTIVE',
    version_no = version_no + 1
WHERE route_path = '/ebr/templates';

UPDATE sys_menu
SET sort_no = 50,
    version_no = version_no + 1
WHERE route_path = '/production/balances';
