-- User-authorized simplification: new process versions activate directly.
-- Historical statuses, content hashes, signatures, snapshots and grants remain intact.
ALTER TABLE proc_package_version ADD COLUMN activation_mode VARCHAR(32) NOT NULL DEFAULT 'INDEPENDENT_APPROVAL';
-- Retire approval/submission privileges from active catalog and menu function definitions.
DELETE mp FROM sys_menu_permission mp JOIN sys_permission p ON p.id=mp.permission_id
 WHERE p.permission_code IN ('process:package:submit','process:package:approve')
 OR (p.permission_code REGEXP '^(master|iam):[^:]+:(approve|review|verify)$');
UPDATE sys_permission SET enabled=0,version=version+1
 WHERE permission_code IN ('process:package:submit','process:package:approve')
 OR (permission_code REGEXP '^(master|iam):[^:]+:(approve|review|verify)$');
